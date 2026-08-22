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

        private val PRIMITIVE_TYPES = setOf(
            "String", "Byte", "Short", "Int", "Long", "Boolean", "Char", "Double", "Float",
        )

        private val COLLECTION_CONSTRUCTORS = mapOf(
            "List" to "listOf",
            "MutableList" to "mutableListOf",
            "ArrayList" to "arrayListOf",
            "Set" to "setOf",
            "MutableSet" to "mutableSetOf",
            "HashSet" to "hashSetOf",
            "LinkedHashSet" to "linkedSetOf",
            "Array" to "arrayOf",
        )

        private val MAP_CONSTRUCTORS = mapOf(
            "Map" to "mapOf",
            "MutableMap" to "mutableMapOf",
            "HashMap" to "hashMapOf",
            "LinkedHashMap" to "linkedMapOf",
        )

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
                    functionStringBuilder.append(processProperty(imports, type, propertyDeclaration))
                }
                functionStringBuilder.append("\${currentIndent.indent()})")
                functionStringBuilder.append("\"\"\"\n}")
                functionStringBuilder.append("\n")
            }

            return packageStringBuilder.toString() +
                    imports.joinToString(separator = "") { "$it\n" } +
                    functionStringBuilder.toString()
        }

        /**
         * Renders one property assignment, including the trailing comma and newline.
         *
         * Everything except the primitives goes through an expression that takes the
         * property value as `value`. That is what makes a nullable property work: the
         * expression is placed inside `?.let { }` and the whole assignment collapses to
         * `null` when the value is absent, rather than the constructor call being
         * emitted around nothing.
         */
        private fun processProperty(
            imports: LinkedHashSet<String>,
            type: KSType,
            propertyDeclaration: KSPropertyDeclaration,
        ): String {
            val typeName = type.declaration.simpleName.asString()
            // The primitive deepPrint() extensions take a nullable receiver already.
            if (typeName in PRIMITIVE_TYPES) {
                return "\${${propertyDeclaration}.deepPrint()},\n"
            }
            val expression = valueExpression(imports, type, receiver = "value", propertyDeclaration)
            return when {
                // Nothing we can render: a string template on a null prints "null" anyway.
                expression == null -> "\$$propertyDeclaration,\n"
                type.isMarkedNullable ->
                    "\${$propertyDeclaration?.let { value -> $expression } ?: \"null\"},\n"
                else -> "\${$propertyDeclaration.let { value -> $expression }},\n"
            }
        }

        /**
         * A Kotlin expression that evaluates to the printed form of [receiver], whose
         * static type is [type]. Returns null when the type is not one DeepPrint knows
         * how to reconstruct.
         *
         * [propertyDeclaration] is only consulted for the `@property:DeepPrint` case, so
         * it is absent when rendering a value nested inside a collection.
         */
        @OptIn(KspExperimental::class)
        private fun valueExpression(
            imports: LinkedHashSet<String>,
            type: KSType,
            receiver: String,
            propertyDeclaration: KSPropertyDeclaration? = null,
        ): String? {
            val typeName = type.declaration.simpleName.asString()
            return when {
                typeName in PRIMITIVE_TYPES -> "$receiver.deepPrint()"
                typeName in COLLECTION_CONSTRUCTORS -> collectionExpression(imports, type, receiver)
                typeName in PRIMITIVE_ARRAY_CONSTRUCTORS -> primitiveArrayExpression(imports, type, receiver)
                typeName in MAP_CONSTRUCTORS -> mapExpression(imports, type, receiver)
                else -> annotatedDataClassExpression(imports, type, receiver, propertyDeclaration)
            }
        }

        @OptIn(KspExperimental::class)
        private fun annotatedDataClassExpression(
            imports: LinkedHashSet<String>,
            type: KSType,
            receiver: String,
            propertyDeclaration: KSPropertyDeclaration?,
        ): String? {
            // Not every declaration is a class: a typealias resolves to a KSTypeAlias.
            // There is nothing to deep print in that case, so fall back to toString()
            // the same way an unannotated class does, rather than throwing.
            val propClassDeclaration = type.declaration as? KSClassDeclaration ?: return null
            val propPackageName = propClassDeclaration.packageName.asString()
            // TODO(Support properties defined outside of the module)
            val annotated = propClassDeclaration.isDataClass() &&
                (propClassDeclaration.isAnnotationPresent(DeepPrint::class) ||
                    propertyDeclaration?.isAnnotationPresent(DeepPrint::class) == true)
            return if (annotated) {
                imports.add("import $propPackageName.deepPrint")
                "\"\\n\" + $receiver.deepPrint(currentIndent + 2 * indentWidth)"
            } else {
                null
            }
        }

        private fun mapExpression(
            imports: LinkedHashSet<String>,
            type: KSType,
            receiver: String,
        ): String {
            imports.add("import com.bradyaiello.deepprint.deepPrintContents")
            val ksKeyTypeRef: KSTypeReference = type.arguments[0].type!!
            val ksValueTypeRef: KSTypeReference = type.arguments[1].type!!
            val valueType = ksValueTypeRef.resolve()
            val mapConstructor = MAP_CONSTRUCTORS.getValue(type.declaration.simpleName.asString())

            val valueTransform = if (valueType.declaration.isDeepPrintAnnotatedDataClass()) {
                // A data class value opens a block, so it sits a level deeper than a
                // value that prints inline next to its key.
                "\"\\n\" + it.deepPrint(currentIndent + $DATACLASS_MAP_VAL_INDENT_MULTIPLIER * indentWidth)"
            } else {
                // Anything else -- including a collection, which has no deepPrint() of
                // its own and used to generate code that did not compile.
                valueExpression(imports, valueType, receiver = "it") ?: "it.toString()"
            }

            return "\"$mapConstructor<$ksKeyTypeRef,$ksValueTypeRef>(\\n\" + " +
                "$receiver.deepPrintContents(\n" +
                "keyTransform = { key -> (currentIndent + 2 * indentWidth).indent() + key.deepPrint() },\n" +
                "valueTransform = { $valueTransform }) + " +
                "(currentIndent + indentWidth).indent() + \")\""
        }

        /**
         * Processes the collection types that take a single type argument:
         * List, MutableList, Set, MutableSet and Array.
         * Returns the expression as a listOf(), mutableListOf(), setOf(),
         * mutableSetOf() or arrayOf() function call.
         */
        @OptIn(KspExperimental::class)
        private fun collectionExpression(
            imports: LinkedHashSet<String>,
            type: KSType,
            receiver: String,
        ): String {
            imports.add("import com.bradyaiello.deepprint.deepPrintContents")
            val itemType = type.arguments[0].type!!
            val itemIsAnnotated = itemType.resolve().declaration.isAnnotationPresent(DeepPrint::class)
            val collectionConstructor =
                COLLECTION_CONSTRUCTORS.getValue(type.declaration.simpleName.asString())

            return if (itemIsAnnotated) {
                "\"$collectionConstructor<${itemType}>(\\n\" + " +
                    "$receiver.joinToString(separator = \"\") " +
                    "{ item -> item.deepPrint(currentIndent = currentIndent + 2 * indentWidth) + \",\\n\" } + " +
                    "(currentIndent + indentWidth).indent() + \")\""
            } else {
                "\"$collectionConstructor<${itemType}>(\" + $receiver.deepPrintContents() + \")\""
            }
        }

        /**
         * Processes IntArray, LongArray and friends. They have no type argument,
         * so the element type is baked into the factory function name.
         */
        private fun primitiveArrayExpression(
            imports: LinkedHashSet<String>,
            type: KSType,
            receiver: String,
        ): String {
            imports.add("import com.bradyaiello.deepprint.deepPrintContents")
            val arrayConstructor =
                PRIMITIVE_ARRAY_CONSTRUCTORS.getValue(type.declaration.simpleName.asString())
            return "\"$arrayConstructor(\" + $receiver.deepPrintContents() + \")\""
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
