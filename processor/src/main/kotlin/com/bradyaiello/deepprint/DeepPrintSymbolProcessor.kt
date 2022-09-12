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
                val string = declaration.accept(DataClassVisitor(), 0)
                file.appendText(string)
                file.close()
            }
        }
        return symbols.filterNot { it.validate() }.toList()
    }

    inner class DataClassVisitor
     : KSVisitor<Int, String> {                                               // data = number of spaces to indent
        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Int): String {
            val packageStringBuilder = StringBuilder()
            val importsStringBuilder = StringBuilder()
            val functionStringBuilder = StringBuilder()

            if (classDeclaration.isDataClass()) {
                val packageName = classDeclaration.containingFile!!.packageName.asString()
                val className = classDeclaration.simpleName.asString()
                val props = classDeclaration.getDeclaredProperties()

                val indent0 = " ".repeat(data)
                if (data == 0) {
                    packageStringBuilder.append("package $packageName\n\n")
                }
                functionStringBuilder.append("\n")
                functionStringBuilder.append("fun ${className}.deepPrint(indent: Int = 0): String {\n")
                functionStringBuilder.append("${indent0}return \"\"\"")

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
                functionStringBuilder.append("$indent0\"\"\"\n}")
            }

            return packageStringBuilder.toString() +
                    importsStringBuilder.toString() +
                    functionStringBuilder.toString()
        }

        private fun KSClassDeclaration.isDataClass() = modifiers.contains(Modifier.DATA)

        override fun visitAnnotated(annotated: KSAnnotated, data: Int) = ""
        override fun visitAnnotation(annotation: KSAnnotation, data: Int) = ""
        override fun visitCallableReference(reference: KSCallableReference, data: Int) = ""
        override fun visitClassifierReference(reference: KSClassifierReference, data: Int) = ""
        override fun visitDeclaration(declaration: KSDeclaration, data: Int) = ""
        override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: Int) = ""
        override fun visitDynamicReference(reference: KSDynamicReference, data: Int) = ""
        override fun visitFile(file: KSFile, data: Int) = ""
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Int) = ""
        override fun visitModifierListOwner(modifierListOwner: KSModifierListOwner, data: Int) = ""
        override fun visitNode(node: KSNode, data: Int) = ""
        override fun visitParenthesizedReference(reference: KSParenthesizedReference, data: Int) = ""
        override fun visitPropertyAccessor(accessor: KSPropertyAccessor, data: Int) = ""
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Int) = ""
        override fun visitPropertyGetter(getter: KSPropertyGetter, data: Int) = ""
        override fun visitPropertySetter(setter: KSPropertySetter, data: Int) = ""
        override fun visitReferenceElement(element: KSReferenceElement, data: Int) = ""
        override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Int) = ""
        override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Int) = ""
        override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Int) = ""
        override fun visitTypeReference(typeReference: KSTypeReference, data: Int) = ""
        override fun visitValueArgument(valueArgument: KSValueArgument, data: Int) = ""
        override fun visitValueParameter(valueParameter: KSValueParameter, data: Int) = ""

    }

 }

