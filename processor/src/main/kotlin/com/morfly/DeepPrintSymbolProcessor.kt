package com.morfly

import com.google.devtools.ksp.getDeclaredProperties
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
                if (!codeGenerator.generatedFile.any { it.path.contains(fileName) }) {
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
        }
        return symbols.filterNot { it.validate() }.toList()
    }

    inner class DataClassVisitor
     : KSVisitor<Int, String> {                                               // data = number of spaces to indent
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Int): String {
            val stringBuilder = StringBuilder()
            val propertyClasses: MutableList<KSClassDeclaration> = mutableListOf()
            if (classDeclaration.isDataClass()) {
                val packageName = classDeclaration.containingFile!!.packageName.asString()
                val className = classDeclaration.simpleName.asString()
                val props = classDeclaration.getDeclaredProperties()

                val indent0 = " ".repeat(data)
                if (data == 0) {
                    stringBuilder.append("package $packageName\n")
                }
                stringBuilder.append("\n")
                stringBuilder.append("fun ${className}.deepPrint(indent: Int): String {\n")
                val temp = "val comma = if (indent == 0) \"\" else \",\""
                stringBuilder.append("$temp\n")

                stringBuilder.append("${indent0}return \"\"\"\n")

                stringBuilder.append("\${\" \".repeat(indent)}$className(\n")
//              ${" ".repeat(indent)}SamplePersonClass(
                props.forEach { propertyDeclaration ->
                    val type: KSType = propertyDeclaration.type.resolve()
                    stringBuilder.append("\${\" \".repeat(indent + 4)}${propertyDeclaration} = ")
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

                        else -> {
                            val propClassDeclaration = type.declaration as? KSClassDeclaration
                            propertyClasses.add(propClassDeclaration!!)
                            "\${${propertyDeclaration}.deepPrint(indent + 8)},\n"
                        }
                    }
                    stringBuilder.append(propertyAssignment)
                }
                stringBuilder.append("\${\" \".repeat(indent)})")
                stringBuilder.append("$indent0\"\"\"\n}")
            }

            return stringBuilder.toString()
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

/*    inner class DataClassVisitor(
        val file: OutputStream,
        ) : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            if (classDeclaration.isDataClass()) {
                val packageName = classDeclaration.containingFile!!.packageName.asString()
                val className = classDeclaration.simpleName.asString()

                val props = classDeclaration.getDeclaredProperties()
                file.appendText("package $packageName\n\n")
                file.appendText("fun ${className}.deepPrint(): String {\n")
                file.appendText("    return \"\"\"\n")
                file.appendText("        $className(\n")

                props.forEach { propertyDeclaration ->
                    visitPropertyDeclaration(propertyDeclaration, Unit)
                    val type: KSType = propertyDeclaration.type.resolve()
                    file.appendText("            ${propertyDeclaration}=")
                    val valueToAppend = when (type.declaration.simpleName.asString()) {
                        "String" -> "\"\$${propertyDeclaration}\""
                        "Byte", "Short", "Int", "Long", "Float", "Double", "Int
                       ", "Char" -> "$${propertyDeclaration}"
                        else -> visitClassDeclaration(propertyDeclaration.closestClassDeclaration()!!, Unit)
                    }
                    file.appendText("$valueToAppend,\n")
                }
                file.appendText("        )\n")
                file.appendText("    \"\"\"\n")
                file.appendText("}\n")
            }
        }

        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            val propertyName = property.simpleName.asString()
        }

        private fun KSClassDeclaration.isDataClass() = modifiers.contains(Modifier.DATA)

    }*/
}

