package com.bradyaiello.deepprint.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Spike: rewrites the compiler-synthesised toString() of every data class to delegate to
 * the deepPrint() extension KSP generates for it.
 *
 * Delegating rather than building the printed string in IR keeps the version-specific
 * surface to a single call, and works on every Kotlin target, since deepPrint() is
 * ordinary Kotlin generated into the same module.
 */
class DeepPrintIrGenerationExtension : IrGenerationExtension {

    private companion object {
        val NO_DEEP_PRINT = FqName("com.bradyaiello.deepprint.NoDeepPrint")
    }

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.acceptChildrenVoid(object : IrVisitorVoid() {
            override fun visitElement(element: org.jetbrains.kotlin.ir.IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                if (declaration.isData) {
                    rewriteToString(declaration, pluginContext)
                }
                declaration.acceptChildrenVoid(this)
            }
        })
    }

    private fun rewriteToString(irClass: IrClass, pluginContext: IrPluginContext) {
        if (irClass.hasAnnotation(NO_DEEP_PRINT)) {
            return
        }
        val toStringFunction = irClass.functions.firstOrNull {
            it.name.asString() == "toString" &&
                it.parameters.none { parameter -> parameter.kind == IrParameterKind.Regular } &&
                // Only the one the compiler synthesised for the data class. A toString()
                // the author wrote is theirs, and replacing it would be a bug rather than
                // a feature.
                it.origin == IrDeclarationOrigin.GENERATED_DATA_CLASS_MEMBER
        }
        val deepPrint = findDeepPrintFor(irClass, pluginContext)
        val dispatchReceiver = toStringFunction?.dispatchReceiverParameter
        if (toStringFunction == null || deepPrint == null || dispatchReceiver == null) {
            return
        }

        toStringFunction.body =
            DeclarationIrBuilder(pluginContext, toStringFunction.symbol).irBlockBody {
                +irReturn(
                    irCall(deepPrint).apply {
                        // Kotlin 2.4 unified the receivers and value arguments into one
                        // positional list: extension receiver first, then the parameters.
                        arguments[0] = irGet(dispatchReceiver)
                        arguments[1] = irInt(0)
                        // deepPrint() also takes the cycle guard, left unset so the
                        // default applies and it allocates its own. A toString() is
                        // always the top of a print, so there is never anything to
                        // inherit from further up.
                    }
                )
            }
    }

    /** The generated `fun <ThisClass>.deepPrint(currentIndent: Int = 0): String`. */
    private fun findDeepPrintFor(
        irClass: IrClass,
        pluginContext: IrPluginContext,
    ): IrSimpleFunction? {
        // The package, not the enclosing declaration. kotlinFqName.parent() on a nested
        // class gives the outer class -- com.example.Outer for com.example.Outer.Inner --
        // and KSP generates deepPrint() into the package, so nothing was ever found and
        // nested data classes kept their stock toString().
        val packageName = irClass.getPackageFragment().packageFqName
        val candidates = pluginContext.referenceFunctions(
            CallableId(FqName(packageName.asString()), Name.identifier("deepPrint"))
        )
        return candidates.firstOrNull { candidate ->
            val receiver = candidate.owner.parameters
                .firstOrNull { parameter -> parameter.kind == IrParameterKind.ExtensionReceiver }
            // Compare the classifier rather than the whole type. For a generic class the
            // receiver is Box<T> where T belongs to the function while irClass.defaultType
            // has the class's own T, so the types are never equal and generic data classes
            // kept their stock toString() too.
            receiver?.type?.classifierOrNull == irClass.symbol
        }?.owner
    }
}
