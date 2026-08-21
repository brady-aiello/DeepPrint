@file:OptIn(KspExperimental::class)

package com.bradyaiello.deepprint

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSCallableReference
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSDeclarationContainer
import com.google.devtools.ksp.symbol.KSDefNonNullReference
import com.google.devtools.ksp.symbol.KSDynamicReference
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSModifierListOwner
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSParenthesizedReference
import com.google.devtools.ksp.symbol.KSPropertyAccessor
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSPropertyGetter
import com.google.devtools.ksp.symbol.KSPropertySetter
import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Modifier.DATA
import com.google.devtools.ksp.validate
import java.io.OutputStream

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

class DeepPrintProcessor(
    val codeGenerator: CodeGenerator,
    val indent: Int = 4
) : SymbolProcessor {

    companion object {
        private const val DATACLASS_MAP_VAL_INDENT_MULTIPLIER = 3
        private const val PRIMITIVE_MAP_VAL_INDENT_MUTILPLIER = 2

        /**
         * Primitive arrays are not `Array<T>`: they carry no type argument and each
         * has its own factory function.
         */
        private val PRIMITIVE_ARRAY_CONSTRUCTORS = mapOf(
            "ByteArray" to "byteArrayOf",
            "ShortArray" to "shortArrayOf",
            "IntArray" to "intArrayOf",
            "LongArray" to "longArrayOf",
            "FloatArray" to "floatArrayOf",
            "DoubleArray" to "doubleArrayOf",
            "BooleanArray" to "booleanArrayOf",
            "CharArray" to "charArrayOf",
        )
    }
    /**
     * Files already written in this processing run, so that a class reached through
     * more than one annotated symbol is only generated once. Tracking the names is
     * enough; asking the [CodeGenerator] which files it has produced and deleting the
     * clashes wipes the whole output directory under KSP2.
     */
    private val writtenFiles = mutableSetOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(DeepPrint::class.qualifiedName!!)
        if (!symbols.iterator().hasNext()) return emptyList()
        symbols.forEach { declaration ->
            val containingFile = declaration.containingFile
            val packageName = containingFile?.packageName?.asString()
            val fileName: String? = getFileName(declaration)

            if (containingFile != null && packageName != null && fileName != null) {
                val fullFileName = "DeepPrint${fileName}"
                if (writtenFiles.add("$packageName.$fullFileName")) {
                    val file = codeGenerator.createNewFile(
                        // Naming the source the output was derived from lets KSP keep the
                        // file across incremental runs; without it KSP2 treats the output as
                        // orphaned and drops it on the next build.
                        dependencies = Dependencies(aggregating = false, containingFile),
                        packageName = packageName,
                        fileName = fullFileName
                    )
                    val string = declaration.accept(DataClassVisitor(), Unit)
                    file.appendText(string)
                    file.close()
                }
            }
        }
        return symbols.filterNot { it.validate() }.toList()
    }

    private fun getFileName(declaration: KSAnnotated): String? { 
        return when (declaration) {
            is KSClassDeclaration ->  declaration.simpleName.asString()
            is KSPropertyDeclaration -> declaration.simpleName.asString() 
            else -> null
        }
    }

    @Suppress("TooManyFunctions")
    inner class DataClassVisitor
     : KSVisitor<Unit, String> {
        
        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): String {
            val packageName = classDeclaration.containingFile!!.packageName.asString()
            val className = classDeclaration.simpleName.asString()
            val props = classDeclaration.getDeclaredProperties()
            return visitDeepPrintAnnotated(classDeclaration, packageName, className, props)
        }

        @OptIn(KspExperimental::class)
        private fun visitDeepPrintAnnotated(
            classDeclaration: KSClassDeclaration,
            packageName: String,
            className: String,
            props: Sequence<KSPropertyDeclaration>
        ): String {
            val packageStringBuilder = StringBuilder()
            // A set, because a class with several collection properties would otherwise
            // repeat the same import once per property. LinkedHashSet rather than
            // MutableSet: the generated file has to come out byte-identical every run,
            // so insertion order has to be part of the contract.
            val imports = linkedSetOf(
                "import com.bradyaiello.deepprint.deepPrint",
                "import com.bradyaiello.deepprint.indent",
            )
            val functionStringBuilder = StringBuilder()

            if (classDeclaration.isDataClass()) {
                packageStringBuilder.append("package $packageName\n\n")

                functionStringBuilder.append("\n")
                functionStringBuilder.append("fun ${className}.deepPrint(currentIndent: Int = 0): String {\n")
                functionStringBuilder.append("val indentWidth = $indent\n")
                functionStringBuilder.append("return \"\"\"")

                functionStringBuilder.append("\${currentIndent.indent()}$className(\n")
                props.forEach { propertyDeclaration ->
                    val type: KSType = propertyDeclaration.type.resolve()
                    functionStringBuilder.append("\${(currentIndent + indentWidth).indent()}${propertyDeclaration} = ")
                    val propertyAssignment = when (type.declaration.simpleName.asString()) {
                        "String", "Byte", "Short", "Int", "Long", "Boolean", "Char",
                        "Double", "Float" -> "\${${propertyDeclaration}.deepPrint()},\n"
                        "List", "MutableList", "ArrayList",
                        "Set", "MutableSet", "HashSet", "LinkedHashSet", "Array" -> {
                            processCollection(imports, type, propertyDeclaration)
                        }
                        in PRIMITIVE_ARRAY_CONSTRUCTORS -> {
                            processPrimitiveArray(imports, type, propertyDeclaration)
                        }
                        "Map", "MutableMap", "HashMap", "LinkedHashMap" -> {
                            processMap(imports, type, propertyDeclaration)
                        }
                        // Property assignment is an annotated data class (can deep print), 
                        // or not (cannot deep print)
                        else -> {
                            processAnnotatedDataClassOrNotSupported(type, propertyDeclaration, imports)
                        }
                    }
                    functionStringBuilder.append(propertyAssignment)
                }
                functionStringBuilder.append("\${currentIndent.indent()})")
                functionStringBuilder.append("\"\"\"\n}")
                functionStringBuilder.append("\n")
            }

            return packageStringBuilder.toString() +
                    imports.joinToString(separator = "") { "$it\n" } +
                    functionStringBuilder.toString()
        }

        @OptIn(KspExperimental::class)
        private fun processAnnotatedDataClassOrNotSupported(
            type: KSType,
            propertyDeclaration: KSPropertyDeclaration,
            imports: LinkedHashSet<String>
        ): String {
            // Not every declaration is a class: a typealias resolves to a KSTypeAlias.
            // There is nothing to deep print in that case, so fall back to toString()
            // the same way an unannotated class does, rather than throwing.
            val propClassDeclaration = type.declaration as? KSClassDeclaration
                ?: return "\$$propertyDeclaration,\n"
            val propPackage = propClassDeclaration.packageName
            val propPackageName = propPackage.asString()
            // TODO(Support properties defined outside of the module)
            return if (propClassDeclaration.isDataClass() &&
                (propClassDeclaration.isAnnotationPresent(DeepPrint::class) ||
                        propertyDeclaration.isAnnotationPresent(DeepPrint::class))
            ) {
                imports.add("import $propPackageName.deepPrint")
                "\n\${${propertyDeclaration}.deepPrint(currentIndent + 2 * indentWidth)},\n"
            } else { /* no annotation on property or class */
                "\$$propertyDeclaration,\n"
            }
        }

        private fun processMap(
            imports: LinkedHashSet<String>,
            type: KSType,
            propertyDeclaration: KSPropertyDeclaration
        ): String {
            imports.add("import com.bradyaiello.deepprint.deepPrintContents")
            val ksKeyTypeRef: KSTypeReference = type.arguments[0].type!!
            val ksValueTypeRef: KSTypeReference = type.arguments[1].type!!
            val valueDecl = ksValueTypeRef.resolve().declaration
            val mapConstructor = when (type.declaration.simpleName.asString()) {
                "Map" -> "mapOf"
                "HashMap" -> "hashMapOf"
                "LinkedHashMap" -> "linkedMapOf"
                else  -> "mutableMapOf"
            }
            val opening = "$mapConstructor<$ksKeyTypeRef,$ksValueTypeRef>(\n"

            val indentMultiplier = if (valueDecl.isDeepPrintAnnotatedDataClass()) 
                DATACLASS_MAP_VAL_INDENT_MULTIPLIER 
            else PRIMITIVE_MAP_VAL_INDENT_MUTILPLIER
            
            val valueTransform = if (valueDecl.isDeepPrintAnnotatedDataClass()) 
                "\"\\n\" + it.deepPrint(currentIndent + $indentMultiplier * indentWidth)" 
                else "it.deepPrint()"
            val entriesPrint =  "\${${propertyDeclaration}.deepPrintContents(\nkeyTransform = " +
                    "{(currentIndent + 2 * indentWidth).indent() + it.deepPrint() },\n" +
                    "valueTransform = { $valueTransform })}\${(currentIndent + indentWidth).indent()}),\n"
            return opening + entriesPrint
        }

        /**
         * Processes the collection types that take a single type argument:
         * List, MutableList, Set, MutableSet and Array.
         * Returns the expression as a listOf(), mutableListOf(), setOf(),
         * mutableSetOf() or arrayOf() function call.
         */
        @OptIn(KspExperimental::class)
        private fun processCollection(
            imports: LinkedHashSet<String>,
            type: KSType,
            propertyDeclaration: KSPropertyDeclaration
        ): String {
            imports.add("import com.bradyaiello.deepprint.deepPrintContents")
            val ksTypeArg = type.arguments[0]
            val itemType = ksTypeArg.type!!
            val paramHasDeepPrintAnnotation =
                ksTypeArg.type!!.resolve().declaration.isAnnotationPresent(DeepPrint::class)
            val collectionConstructor = when (type.declaration.simpleName.asString()) {
                "MutableList" -> "mutableListOf"
                "List" -> "listOf"
                "ArrayList" -> "arrayListOf"
                "MutableSet" -> "mutableSetOf"
                "Set" -> "setOf"
                "HashSet" -> "hashSetOf"
                "LinkedHashSet" -> "linkedSetOf"
                else -> "arrayOf"
            }
            val opening = "$collectionConstructor<${itemType}>("
            val itemsPrint: String = if (paramHasDeepPrintAnnotation) {
                "\n\${$propertyDeclaration.joinToString(separator = \"\") " +
                        "{ it.deepPrint(currentIndent = currentIndent + 2 * indentWidth) + \",\\n\" }}" +
                        "\${(currentIndent + indentWidth).indent()}),\n"
            } else {
                "\${$propertyDeclaration.deepPrintContents()}),\n"
            }
            return opening + itemsPrint
        }

        /**
         * Processes IntArray, LongArray and friends. They have no type argument,
         * so the element type is baked into the factory function name.
         */
        private fun processPrimitiveArray(
            imports: LinkedHashSet<String>,
            type: KSType,
            propertyDeclaration: KSPropertyDeclaration
        ): String {
            imports.add("import com.bradyaiello.deepprint.deepPrintContents")
            val arrayConstructor =
                PRIMITIVE_ARRAY_CONSTRUCTORS.getValue(type.declaration.simpleName.asString())
            return "$arrayConstructor(\${$propertyDeclaration.deepPrintContents()}),\n"
        }

        private fun KSDeclaration.isDeepPrintAnnotatedDataClass(): Boolean {
            return isDataClass() && isAnnotationPresent(DeepPrint::class)
        }
        
//        private fun KSClassDeclaration.isDeepPrintAnnotatedDataClass(): Boolean {
//            return isDataClass() && isAnnotationPresent(DeepPrint::class) 
//        }
        
        private fun KSDeclaration.isDataClass() = modifiers.contains(DATA)
        private fun KSClassDeclaration.isDataClass() = modifiers.contains(DATA)
        private fun KSTypeReference.isDataClass() = modifiers.contains(DATA)

        override fun visitAnnotated(annotated: KSAnnotated, data: Unit) = ""
        override fun visitAnnotation(annotation: KSAnnotation, data: Unit) = ""
        override fun visitCallableReference(reference: KSCallableReference, data: Unit) = ""
        override fun visitClassifierReference(reference: KSClassifierReference, data: Unit) = ""
        override fun visitDeclaration(declaration: KSDeclaration, data: Unit) = ""
        override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: Unit) = ""
        override fun visitDynamicReference(reference: KSDynamicReference, data: Unit) = ""
        override fun visitFile(file: KSFile, data: Unit) = ""
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) = ""
        override fun visitModifierListOwner(modifierListOwner: KSModifierListOwner, data: Unit) = ""
        override fun visitNode(node: KSNode, data: Unit) = ""
        override fun visitParenthesizedReference(reference: KSParenthesizedReference, data: Unit) = ""
        override fun visitPropertyAccessor(accessor: KSPropertyAccessor, data: Unit) = ""
        @OptIn(KspExperimental::class)
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit): String {

            return if (property.isAnnotationPresent(DeepPrint::class)) {
                val propertyType = property.type.resolve()
                when (val declaration = propertyType.declaration) {
                    is KSClassDeclaration -> {
                        val props = declaration.getDeclaredProperties()
                        val packageName = property.packageName.asString()
                        val className = declaration.simpleName.asString()
                        visitDeepPrintAnnotated(declaration, packageName, className, props)
                    } else -> {
                        ""
                    }
                }
            } else {
                ""
            }
        }

        override fun visitPropertyGetter(getter: KSPropertyGetter, data: Unit) = ""
        override fun visitPropertySetter(setter: KSPropertySetter, data: Unit) = ""
        override fun visitReferenceElement(element: KSReferenceElement, data: Unit) = ""
        override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit) = ""
        override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit) = ""
        override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Unit) = ""
        override fun visitTypeReference(typeReference: KSTypeReference, data: Unit) = ""
        override fun visitValueArgument(valueArgument: KSValueArgument, data: Unit) = ""
        override fun visitValueParameter(valueParameter: KSValueParameter, data: Unit) = ""
        override fun visitDefNonNullReference(reference: KSDefNonNullReference, data: Unit) = ""
    }
    
 }
