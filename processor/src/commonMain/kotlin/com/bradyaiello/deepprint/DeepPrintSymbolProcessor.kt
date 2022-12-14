package com.bradyaiello.deepprint

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import java.io.OutputStream

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

class DeepPrintProcessor(
    val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(DeepPrint::class.qualifiedName!!)
        if (!symbols.iterator().hasNext()) return emptyList()
        symbols.forEach { declaration ->
            val packageName = declaration.containingFile?.packageName?.asString()
            val fileName: String? = when(declaration) {
                is KSClassDeclaration -> {
                    declaration.simpleName.asString()
                }
                is KSPropertyDeclaration -> {
                    declaration.simpleName.asString()
                }
                else -> {null}
            }


            if (packageName != null && fileName != null) {
                val fullFileName = "DeepPrint${fileName}"
                if (codeGenerator.generatedFile.any { it.path.contains(fullFileName) }) {
                    codeGenerator.generatedFile.forEach { generatedFile ->
                        generatedFile.delete()
                    }
                }
                val file = codeGenerator.createNewFile(
                    dependencies = Dependencies(false),
                    packageName = packageName,
                    fileName = fullFileName
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
            val importsStringBuilder = StringBuilder()
            val functionStringBuilder = StringBuilder()

            if (classDeclaration.isDataClass()) {
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
                        "List", "Array", "MutableList" -> {
                            importsStringBuilder.append("import com.bradyaiello.deepprint.deepPrintContents\n")
                            val ksTypeArg = type.arguments[0]
                            val listType = ksTypeArg.type!!
                            val paramHasDeepPrintAnnotation =
                                ksTypeArg.type!!.resolve().declaration.isAnnotationPresent(DeepPrint::class)
                            val collectionConstructor = when (type.declaration.simpleName.asString()) {
                                "MutableList" -> "mutableListOf"
                                "List" -> "listOf"
                                else -> "arrayOf"
                            }
                            val opening = "$collectionConstructor<${listType}>("
                            val itemsPrint: String = if (paramHasDeepPrintAnnotation) {
                                "\n\${$propertyDeclaration.map{ it.deepPrint(indent = indent + 8) +\",\\n\"}.reduce {acc, item -> acc + item}}\${\" \".repeat(indent + 4)}),\n"
                            } else {
                                "\${$propertyDeclaration.deepPrintContents()}),\n"
                            }
                            opening + itemsPrint
                        }

                        else -> {
                            val propClassDeclaration = type.declaration as? KSClassDeclaration
                            val propPackage = propClassDeclaration!!.packageName
                            val propPackageName = propPackage.asString()
                            // TODO(Support properties that are data classes, and outside of the module)
                            if (propClassDeclaration.isDataClass() &&
                                (propClassDeclaration.isAnnotationPresent(DeepPrint::class) ||
                                    propertyDeclaration.isAnnotationPresent(DeepPrint::class))
                            ) {
                                importsStringBuilder.append("import $propPackageName.deepPrint\n")
                                "\n\${${propertyDeclaration}.deepPrint(indent + 8)},\n"
                            } else { /* no annotation on property or class */
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
    }


    fun isPrimitive(type: KSType): Boolean {
        return (type.declaration.simpleName.asString() in
            listOf(
                "String",
                "Byte",
                "Short",
                "Int",
                "Long",
                "Double",
                "Boolean",
                "Char",
                "Float"
            )
        )
    }
 }

