package com.bradyaiello.deepprint

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

class DeepPrintProcessor(
    val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(DeepPrint::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
        if (!symbols.iterator().hasNext()) return emptyList()
        symbols.forEach { declaration ->
            val packageName = declaration.containingFile?.packageName?.asString()
            if (packageName != null) {
                println("$declaration")
                println("Properties: ${declaration.getDeclaredProperties()}")
                val fileName = "DeepPrint${declaration.simpleName.asString()}"
                if (codeGenerator.generatedFile.any { it.path.contains(fileName) }) {
                    codeGenerator.generatedFile.forEach { generatedFile ->
                        generatedFile.delete()
                    }
                }
                val file = codeGenerator.createNewFile(
                    dependencies = Dependencies(false),
                    packageName = packageName,
                    fileName = "DeepPrint${declaration.simpleName.asString()}"
                )
                val string = declaration.accept(DataClassVisitor(), Unit)
                file.appendText(string)
                file.close()
            }
        }
        return symbols.filterNot { it.validate() }.toList()
    }

    inner class DataClassVisitor
     : KSVisitor<Unit, String> {
        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): String {
            val packageStringBuilder = StringBuilder()
            val importsStringBuilder = StringBuilder()
            val functionStringBuilder = StringBuilder()

            if (classDeclaration.isDataClass()) {
                val packageName = classDeclaration.containingFile!!.packageName.asString()
                val className = classDeclaration.simpleName.asString()
                val props = classDeclaration.getDeclaredProperties()



                packageStringBuilder.append("package $packageName\n\n")

                functionStringBuilder.append("\n")
                functionStringBuilder.append("fun ${className}.deepPrint(indent: Int = 0): String {\n")
                functionStringBuilder.append("return \"\"\"")

                functionStringBuilder.append("\${\" \".repeat(indent)}$className(\n")
                props.forEach { propertyDeclaration ->
                    val type: KSType = propertyDeclaration.type.resolve()
                    functionStringBuilder.append("\${\" \".repeat(indent + 4)}${propertyDeclaration} = ")
                    val propertyAssignment = when (type.declaration.simpleName.asString()) {
                        "String" -> "\"\$${propertyDeclaration}\",\n"
                        "Byte",
                        "Short",
                        "Int",
                        "Long",
                        "Double",
                        "Boolean" -> "$${propertyDeclaration},\n"
                        "Char" -> "'$${propertyDeclaration}',\n"
                        "Float" -> "\${${propertyDeclaration}}f,\n"
                        "List"-> {
                            importsStringBuilder.append("import com.bradyaiello.deepprint.deepPrintContents\n")
                            val ksTypeArg = type.arguments[0]
                            val listType = ksTypeArg.type!!
                            val paramHasDeepPrintAnnotation = ksTypeArg.type!!.resolve().declaration.isAnnotationPresent(DeepPrint::class)
                            val opening = "listOf<${listType}>("
                            val itemsPrint: String = if (paramHasDeepPrintAnnotation) {
                                "\n\${$propertyDeclaration.map{ it.deepPrint(indent = indent + 8) +\",\\n\"}.reduce {acc, item -> acc + item}}\${\" \".repeat(indent + 4)}),\n"
                            } else {
                                "\${$propertyDeclaration.deepPrintContents()}),\n"
                            }
                            opening + itemsPrint
                        }
                        else -> {
                            val propClassDeclaration = type.declaration as? KSClassDeclaration
                            val propPackageName = propClassDeclaration!!.packageName.asString()
                            if (propClassDeclaration.isAnnotationPresent(DeepPrint::class)) {
                                importsStringBuilder.append("import $propPackageName.deepPrint\n")
                                "\n\${${propertyDeclaration}.deepPrint(indent + 8)},\n"
                            } else {
                                "\$$propertyDeclaration,\n"
                            }
                        }
                    }
                    functionStringBuilder.append(propertyAssignment)
                }
                functionStringBuilder.append("\${\" \".repeat(indent)})")
                functionStringBuilder.append("\"\"\"\n}")
            }

            return packageStringBuilder.toString() +
                    importsStringBuilder.toString() +
                    functionStringBuilder.toString()
        }

        private fun KSClassDeclaration.isDataClass() = modifiers.contains(Modifier.DATA)
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
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) = ""
        override fun visitPropertyGetter(getter: KSPropertyGetter, data: Unit) = ""
        override fun visitPropertySetter(setter: KSPropertySetter, data: Unit) = ""
        override fun visitReferenceElement(element: KSReferenceElement, data: Unit) = ""
        override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit) = ""
        override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit) = ""
        override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Unit) = ""
        override fun visitTypeReference(typeReference: KSTypeReference, data: Unit) = ""
        override fun visitValueArgument(valueArgument: KSValueArgument, data: Unit) = ""
        override fun visitValueParameter(valueParameter: KSValueParameter, data: Unit) = ""
    }

 }

