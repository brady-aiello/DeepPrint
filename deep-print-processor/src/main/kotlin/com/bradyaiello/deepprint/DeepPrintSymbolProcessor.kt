@file:OptIn(KspExperimental::class, KspExperimental::class, KspExperimental::class)

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
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import java.io.OutputStream

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

class DeepPrintProcessor(
    val codeGenerator: CodeGenerator,
    val indent: Int = 4
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(DeepPrint::class.qualifiedName!!)
        if (!symbols.iterator().hasNext()) return emptyList()
        symbols.forEach { declaration ->
            val packageName = declaration.containingFile?.packageName?.asString()
            val fileName: String? = getFileName(declaration)

            if (packageName != null && fileName != null) {
                val fullFileName = "DeepPrint${fileName}"
                conditionallyDeleteDuplicates(fullFileName)
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

    private fun conditionallyDeleteDuplicates(fullFileName: String) {
        if (codeGenerator.generatedFile.any { it.path.contains(fullFileName) }) {
            codeGenerator.generatedFile.forEach { generatedFile ->
                generatedFile.delete()
            }
        }
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
            val importsStringBuilder = StringBuilder()
            importsStringBuilder.append("import com.bradyaiello.deepprint.deepPrint\n")
            importsStringBuilder.append("import com.bradyaiello.deepprint.indent\n")
            val functionStringBuilder = StringBuilder()

            if (classDeclaration.isDataClass()) {
                packageStringBuilder.append("package $packageName\n\n")

                functionStringBuilder.append("\n")
                functionStringBuilder.append("fun ${className}.deepPrint(indent: Int = 0): String {\n")
                functionStringBuilder.append("val indentA = $indent\n")
                functionStringBuilder.append("return \"\"\"")

                functionStringBuilder.append("\${\" \".repeat(indent)}$className(\n")
                props.forEach { propertyDeclaration ->
                    val type: KSType = propertyDeclaration.type.resolve()
                    functionStringBuilder.append("\${\" \".repeat(indent + indentA)}${propertyDeclaration} = ")
                    val propertyAssignment = when (type.declaration.simpleName.asString()) {
                        "String", "Byte", "Short", "Int", "Long", "Boolean", "Char",
                        "Double", "Float" -> "\${${propertyDeclaration}.deepPrint()},\n"
                        "List", "Array", "MutableList" -> {
                            processList(importsStringBuilder, type, propertyDeclaration)
                        }
                        "Map", "MutableMap" -> {
                            processMap(importsStringBuilder, type, propertyDeclaration)
                        }
                        // Property assignment is an annotated data class (can deep print), 
                        // or not (cannot deep print)
                        else -> {
                            processAnnotatedDataClassOrNotSupported(type, propertyDeclaration, importsStringBuilder)
                        }
                    }
                    functionStringBuilder.append(propertyAssignment)
                }
                functionStringBuilder.append("\${indent.indent()})")
                //functionStringBuilder.append("\${\" \".repeat(indent)})")
                functionStringBuilder.append("\"\"\"\n}")
                functionStringBuilder.append("\n")
            }

            return packageStringBuilder.toString() +
                    importsStringBuilder.toString() +
                    functionStringBuilder.toString()
        }

        @OptIn(KspExperimental::class)
        private fun processAnnotatedDataClassOrNotSupported(
            type: KSType,
            propertyDeclaration: KSPropertyDeclaration,
            importsStringBuilder: StringBuilder
        ): String {
            val propClassDeclaration = type.declaration as? KSClassDeclaration
            val propPackage = propClassDeclaration!!.packageName
            val propPackageName = propPackage.asString()
            // TODO(Support properties defined outside of the module)
            return if (propClassDeclaration.isDataClass() &&
                (propClassDeclaration.isAnnotationPresent(DeepPrint::class) ||
                        propertyDeclaration.isAnnotationPresent(DeepPrint::class))
            ) {
                importsStringBuilder.append("import $propPackageName.deepPrint\n")
                "\n\${${propertyDeclaration}.deepPrint(indent + indentA + indentA)},\n"
            } else { /* no annotation on property or class */
                "\$$propertyDeclaration,\n"
            }
        }

        private fun processMap(
            importsStringBuilder: StringBuilder,
            type: KSType,
            propertyDeclaration: KSPropertyDeclaration
        ): String {
            importsStringBuilder.append("import com.bradyaiello.deepprint.deepPrintContents\n")
            val ksKeyTypeRef: KSTypeReference = type.arguments[0].type!!
            val ksValueTypeRef: KSTypeReference = type.arguments[1].type!!
            val mapConstructor = when (type.declaration.simpleName.asString()) {
                "Map" -> "mapOf"
                else  -> "mutableMapOf"
            }
            val opening = "$mapConstructor<$ksKeyTypeRef,$ksValueTypeRef>(\n"

            // myMap.deepPrintContents({ it.deepPrint() }, { it.deepPrint() }
            val entriesPrint =  "\${${propertyDeclaration}.deepPrintContents({(indent + 2 * indentA).indent() + " +
                    "it.deepPrint() }, { it.deepPrint()})}\${(indent + indentA).indent()}),\n"
            return opening + entriesPrint
        }

        /**
         * Processes collection types List, MutableList, Array
         * Returns the expression as {listOf(), mutableListOf(), 
         * or arrayOf() function calls.
         */
        @OptIn(KspExperimental::class)
        private fun processList(
            importsStringBuilder: StringBuilder,
            type: KSType,
            propertyDeclaration: KSPropertyDeclaration
        ): String {
            importsStringBuilder.append("import com.bradyaiello.deepprint.deepPrintContents\n")
            val ksTypeArg = type.arguments[0]
            val listType = ksTypeArg.type!!
            val paramHasDeepPrintAnnotation =
                ksTypeArg.type!!.resolve().declaration.isAnnotationPresent(DeepPrint::class)
            val listConstructor = when (type.declaration.simpleName.asString()) {
                "MutableList" -> "mutableListOf"
                "List" -> "listOf"
                else -> "arrayOf"
            }
            val opening = "$listConstructor<${listType}>("
            val itemsPrint: String = if (paramHasDeepPrintAnnotation) {
                "\n\${$propertyDeclaration.map{ it.deepPrint(indent = " +
                        "indent + indentA + indentA) +\",\\n\"}" +
                        ".reduce {acc, item -> acc + item}}\${\" \".repeat(indent + indentA)}),\n"
            } else {
                "\${$propertyDeclaration.deepPrintContents()}),\n"
            }
            return opening + itemsPrint
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
        override fun visitDefNonNullReference(reference: KSDefNonNullReference, data: Unit) = ""
    }
    
 }

