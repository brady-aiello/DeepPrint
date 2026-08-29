@file:OptIn(KspExperimental::class)

package com.bradyaiello.deepprint

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSCallableReference
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSDeclarationContainer
import com.google.devtools.ksp.symbol.KSDefNonNullReference
import com.google.devtools.ksp.symbol.KSDynamicReference
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.Origin
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
import com.google.devtools.ksp.symbol.Variance
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Modifier.DATA
import com.google.devtools.ksp.symbol.Visibility
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.joinToCode
import com.squareup.kotlinpoet.ksp.toClassName
import java.io.OutputStream

/** An import KotlinPoet renders, and deduplicates, itself. */
internal data class MemberImport(val packageName: String, val simpleName: String)

internal const val DEEP_PRINT_PACKAGE = "com.bradyaiello.deepprint"

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

@Suppress("TooManyFunctions")
class DeepPrintProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val indent: Int = 4,
    /** Generate for every data class, rather than only those annotated @DeepPrint. */
    val processAllDataClasses: Boolean = false,
    val overrideToString: Boolean = false,
) : SymbolProcessor {

    companion object {
        private const val DATACLASS_MAP_VAL_INDENT_MULTIPLIER = 3

        /** Indent levels, relative to the property, for a block that a value opens. */
        private const val ENTRY_INDENT_MULTIPLIER = 2
        private const val CLOSING_INDENT_MULTIPLIER = 1

        /** Visibilities a top level extension in another file cannot reach. */
        private val UNREACHABLE_VISIBILITIES = setOf(
            Visibility.PRIVATE,
            Visibility.PROTECTED,
            Visibility.LOCAL,
        )

        private val PRIMITIVE_TYPES = setOf(
            "String", "Byte", "Short", "Int", "Long", "Boolean", "Char", "Double", "Float",
            "UByte", "UShort", "UInt", "ULong",
        )

        /** Component accessors for Pair and Triple, in order. */
        private val TUPLE_COMPONENTS = mapOf(
            "Pair" to listOf("first", "second"),
            "Triple" to listOf("first", "second", "third"),
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
            // Read-only supertypes: listOf() is assignable to both.
            //
            // Sequence is deliberately absent. Printing one has to iterate it, which
            // consumes a single-use sequence and never returns for an infinite one. A
            // debugging aid should not be able to destroy what it is inspecting, so a
            // Sequence property falls back to toString() like any other type DeepPrint
            // cannot reconstruct.
            "Collection" to "listOf",
            "Iterable" to "listOf",
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
            "UByteArray" to "ubyteArrayOf",
            "UShortArray" to "ushortArrayOf",
            "UIntArray" to "uintArrayOf",
            "ULongArray" to "ulongArrayOf",
        )
    }
    /**
     * Files already written in this processing run, so that a class reached through
     * more than one annotated symbol is only generated once. Tracking the names is
     * enough; asking the [CodeGenerator] which files it has produced and deleting the
     * clashes wipes the whole output directory under KSP2.
     */
    private val writtenFiles = mutableSetOf<String>()

    private var warnedAboutOverrideToString = false

    /**
     * Data classes from other modules that a property referred to, and where to put the
     * generated extension for each.
     *
     * A library class cannot carry @DeepPrint -- the annotation is SOURCE retention, so
     * it is gone by the time the class is a dependency -- and it has no containingFile,
     * so it is never one of the symbols this round walks. It has to be reached through
     * whatever referred to it.
     */
    private val pendingExternals = mutableListOf<PendingExternal>()

    /** Where the file currently being generated lives, so externals land beside it. */
    private var currentPackageName: String = ""
    private var currentFile: KSFile? = null

    private data class PendingExternal(
        val declaration: KSClassDeclaration,
        val packageName: String,
        val originatingFile: KSFile,
    )

    /** Set at the start of each round; needed to build substituted type arguments. */
    private lateinit var resolver: Resolver

    override fun process(resolver: Resolver): List<KSAnnotated> {
        this.resolver = resolver
        if (overrideToString && !warnedAboutOverrideToString) {
            warnedAboutOverrideToString = true
            logger.warn(
                "DeepPrint: overrideToString is a Gradle plugin option, not a KSP one -- a " +
                    "symbol processor can only add files, it cannot alter an existing class. " +
                    "Apply the com.bradyaiello.deepprint Gradle plugin and set " +
                    "deepPrint { overrideToString.set(true) }. deepPrint() is being " +
                    "generated as usual."
            )
        }
        val symbols = symbolsToProcess(resolver)
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
                    currentPackageName = packageName
                    currentFile = containingFile
                    val string = declaration.accept(DataClassVisitor(), Unit)
                    file.appendText(string)
                    file.close()
                }
            }
        }
        generatePendingExternals()
        return symbols.filterNot { it.validate() }.toList()
    }

    /**
     * Generates a deepPrint() for each external data class that was referred to.
     *
     * A worklist rather than a loop over a fixed list: generating for one external class
     * reads its properties, which can refer to further external classes.
     */
    private fun generatePendingExternals() {
        while (pendingExternals.isNotEmpty()) {
            val pending = pendingExternals.removeAt(0)
            val className = pending.declaration.receiverName()
            if (className != null) {
                generateExternal(pending, className)
            }
        }
    }

    private fun generateExternal(pending: PendingExternal, className: String) {
        val fullFileName = fileNameFor(className)
        if (!writtenFiles.add("${pending.packageName}.$fullFileName")) {
            return
        }
        currentPackageName = pending.packageName
        currentFile = pending.originatingFile
        val source = DataClassVisitor().visitDeepPrintAnnotatedFor(
            classDeclaration = pending.declaration,
            packageName = pending.packageName,
            className = className,
        )
        // aggregating, because this file is derived from a dependency rather than from
        // the one source that happened to mention it first. Attributing it to that single
        // file would have KSP drop it when an unrelated consumer changes.
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true, pending.originatingFile),
            packageName = pending.packageName,
            fileName = fullFileName,
        )
        file.appendText(source)
        file.close()
    }

    /** A data class from another module: no source file here, and unannotatable. */
    private fun KSClassDeclaration.isExternal(): Boolean =
        containingFile == null && origin != Origin.KOTLIN && origin != Origin.JAVA

    /**
     * Either every eligible data class, or only the annotated symbols.
     *
     * Walking every declaration reaches classes the annotated path never saw, so this is
     * where the cases that path never had to handle get filtered out. See [isEligible].
     */
    private fun symbolsToProcess(resolver: Resolver): Sequence<KSAnnotated> =
        if (processAllDataClasses) {
            resolver.getAllFiles()
                .flatMap { it.declarations }
                .flatMap { it.withNestedDeclarations() }
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.isEligible() }
        } else {
            resolver.getSymbolsWithAnnotation(DeepPrint::class.qualifiedName!!)
        }

    /** A declaration and, recursively, anything declared inside it. */
    private fun KSDeclaration.withNestedDeclarations(): Sequence<KSDeclaration> =
        sequenceOf(this) + when (this) {
            is KSClassDeclaration -> declarations.flatMap { it.withNestedDeclarations() }
            else -> emptySequence()
        }

    @OptIn(KspExperimental::class)
    private fun KSClassDeclaration.isEligible(): Boolean = when {
        !modifiers.contains(DATA) -> false
        isAnnotationPresent(NoDeepPrint::class) -> false
        // The generated extension lives in a separate file in the same package, so it
        // cannot reach a private, protected or local class.
        getVisibility() in UNREACHABLE_VISIBILITIES -> false
        // Anonymous or otherwise unnameable.
        qualifiedName == null -> false
        else -> true
    }

    /**
     * How the class is named from inside its own package, so `Outer.Inner` rather than
     * `Inner`. Both the generated extension's receiver and the constructor call it prints
     * need this, since a bare `Inner` resolves to nothing at package level.
     */
    private fun KSClassDeclaration.receiverName(): String? {
        val qualified = qualifiedName?.asString() ?: return null
        val packagePrefix = packageName.asString()
        return if (packagePrefix.isEmpty()) qualified else qualified.removePrefix("$packagePrefix.")
    }

    /**
     * Whether DeepPrint generates a deepPrint() for [declaration], and so whether a
     * property of that type can be printed as a constructor call rather than toString().
     * With processAllDataClasses the annotation is no longer what decides this.
     */
    @OptIn(KspExperimental::class)
    private fun generatesDeepPrintFor(declaration: KSDeclaration?): Boolean {
        val classDeclaration = declaration as? KSClassDeclaration
        return when {
            classDeclaration == null -> false
            !classDeclaration.isEligible() -> false
            // @DeepPrint has SOURCE retention, so a class from a dependency can never be
            // annotated. Requiring the annotation would make external support impossible
            // rather than opt-in, so an external data class qualifies on its own.
            classDeclaration.isExternal() -> {
                enqueueExternal(classDeclaration)
                true
            }
            else -> processAllDataClasses ||
                classDeclaration.isAnnotationPresent(DeepPrint::class)
        }
    }

    @Suppress("ReturnCount")
    private fun enqueueExternal(classDeclaration: KSClassDeclaration) {
        val originatingFile = currentFile ?: return
        val className = classDeclaration.receiverName() ?: return
        if ("$currentPackageName.${fileNameFor(className)}" in writtenFiles) {
            return
        }
        val queued = pendingExternals.any {
            it.declaration == classDeclaration && it.packageName == currentPackageName
        }
        if (!queued) {
            pendingExternals += PendingExternal(
                declaration = classDeclaration,
                // Beside whatever referred to it, not in the library's own package: two
                // modules generating into a shared package would collide, and the call
                // site needs no import when it is already here.
                packageName = currentPackageName,
                originatingFile = originatingFile,
            )
        }
    }

    private fun fileNameFor(className: String): String =
        "DeepPrint${className.replace(".", "_")}"

    private fun getFileName(declaration: KSAnnotated): String? { 
        return when (declaration) {
            // A nested class carries its outer names, or Outer.Inner and a top level
            // Inner would both want to write DeepPrintInner.kt.
            is KSClassDeclaration -> declaration.receiverName()?.replace('.', '_')
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
            val className = classDeclaration.receiverName() ?: return ""
            val props = classDeclaration.getDeclaredProperties()
            return visitDeepPrintAnnotated(classDeclaration, packageName, className, props)
        }

        @OptIn(KspExperimental::class)
        fun visitDeepPrintAnnotatedFor(
            classDeclaration: KSClassDeclaration,
            packageName: String,
            className: String,
        ): String = visitDeepPrintAnnotated(
            classDeclaration = classDeclaration,
            packageName = packageName,
            className = className,
            props = classDeclaration.getDeclaredProperties(),
        )

        private fun visitDeepPrintAnnotated(
            classDeclaration: KSClassDeclaration,
            packageName: String,
            className: String,
            props: Sequence<KSPropertyDeclaration>
        ): String {
            if (!classDeclaration.isDataClass()) {
                return ""
            }
            // KotlinPoet owns the package declaration, the imports, the receiver type and
            // the visibility. Assembling those as text is what produced a bare `package `
            // line in the default package, `import .deepPrint`, a public extension on an
            // internal receiver, an unqualified nested class, and one repeated import per
            // collection property. None of those are expressible here.
            val imports = linkedSetOf(
                MemberImport(DEEP_PRINT_PACKAGE, "deepPrint"),
                MemberImport(DEEP_PRINT_PACKAGE, "indent"),
            )

            val lines = bodyLines(className, props, imports)
            val body = CodeBlock.builder()
                .addStatement("val indentWidth = %L", indent)
                .add("return %L", lines.joinToCode(separator = " +\n"))
                .build()

            val function = FunSpec.builder("deepPrint")
                .receiver(classDeclaration.toClassName())
                .apply {
                    if (classDeclaration.getVisibility() == Visibility.INTERNAL) {
                        addModifiers(KModifier.INTERNAL)
                    }
                }
                .addParameter(
                    ParameterSpec.builder("currentIndent", INT).defaultValue("%L", 0).build()
                )
                .returns(STRING)
                .addCode(body)
                .build()

            return FileSpec.builder(packageName, fileNameFor(className))
                .apply { imports.forEach { addImport(it.packageName, it.simpleName) } }
                .addFunction(function)
                .build()
                .toString()
        }

        /**
         * The printed form, one source line per printed line.
         *
         * Each is a single-line literal ending in a newline escape rather than one raw
         * string spanning the function, because KotlinPoet indents a function body and
         * that indentation would land inside a multi-line literal and show up in the
         * output.
         */
        private fun bodyLines(
            className: String,
            props: Sequence<KSPropertyDeclaration>,
            imports: MutableSet<MemberImport>,
        ): List<CodeBlock> {
            val lines = mutableListOf<String>()
            lines += "\${currentIndent.indent()}$className("
            props.forEach { propertyDeclaration ->
                val type: KSType = propertyDeclaration.type.resolve()
                lines += "\${(currentIndent + indentWidth).indent()}$propertyDeclaration = " +
                    processProperty(imports, type, propertyDeclaration)
            }
            lines += "\${currentIndent.indent()})"

            return lines.mapIndexed { index, line ->
                val terminator = if (index == lines.lastIndex) "" else "\\n"
                // %L, not %S: these are live string templates to emit verbatim, not text
                // for KotlinPoet to escape into a literal.
                CodeBlock.of("%L", "\"$line$terminator\"")
            }
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
            imports: MutableSet<MemberImport>,
            type: KSType,
            propertyDeclaration: KSPropertyDeclaration,
        ): String {
            val typeName = type.declaration.simpleName.asString()
            // The primitive deepPrint() extensions take a nullable receiver already.
            if (typeName in PRIMITIVE_TYPES) {
                return "\${${propertyDeclaration}.deepPrint()},"
            }
            val expression = valueExpression(imports, type, receiver = "value", propertyDeclaration)
            return when {
                // Nothing we can render: a string template on a null prints "null" anyway.
                expression == null -> "\$$propertyDeclaration,"
                type.isMarkedNullable ->
                    "\${$propertyDeclaration?.let { value -> $expression } ?: \"null\"},"
                else -> "\${$propertyDeclaration.let { value -> $expression }},"
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
            imports: MutableSet<MemberImport>,
            type: KSType,
            receiver: String,
            propertyDeclaration: KSPropertyDeclaration? = null,
            depth: Int = 0,
        ): String? {
            val typeName = type.declaration.simpleName.asString()
            return when {
                typeName in PRIMITIVE_TYPES -> "$receiver.deepPrint()"
                typeName in COLLECTION_CONSTRUCTORS -> collectionExpression(imports, type, receiver, depth)
                typeName in PRIMITIVE_ARRAY_CONSTRUCTORS -> primitiveArrayExpression(imports, type, receiver)
                typeName in MAP_CONSTRUCTORS -> mapExpression(imports, type, receiver, depth)
                typeName in TUPLE_COMPONENTS -> tupleExpression(imports, type, receiver, depth)
                else -> enumExpression(type, receiver)
                    ?: typeAliasExpression(imports, type, receiver, propertyDeclaration, depth)
                    ?: annotatedDataClassExpression(imports, type, receiver, propertyDeclaration)
            }
        }

        /** True when [type] deep prints, ie. it opens a nested constructor block. */
        @OptIn(KspExperimental::class)
        private fun isAnnotatedDataClass(type: KSType): Boolean =
            generatesDeepPrintFor(type.declaration)

        /**
         * Renders [accessor] according to its static [type], wrapping the result so that a
         * nullable value prints the `null` literal instead of dereferencing nothing. The
         * primitives need no wrapper: their deepPrint() extensions take a nullable
         * receiver.
         */
        private fun nullSafeValueExpression(
            imports: MutableSet<MemberImport>,
            type: KSType,
            accessor: String,
            lambdaParameter: String,
            depth: Int = 0,
        ): String? {
            val isPrimitive = type.declaration.simpleName.asString() in PRIMITIVE_TYPES
            if (!type.isMarkedNullable || isPrimitive) {
                return valueExpression(imports, type, receiver = accessor, depth = depth)
            }
            val inner = valueExpression(imports, type, receiver = lambdaParameter, depth = depth)
            return inner?.let { "($accessor?.let { $lambdaParameter -> $it } ?: \"null\")" }
        }

        /**
         * Pair and Triple print as constructor calls on one line, eg. `Pair("a", 1)`.
         * Components are rendered by their own static type, so a Pair of data classes or
         * of collections works the same way a property of that type would.
         */
        private fun tupleExpression(
            imports: MutableSet<MemberImport>,
            type: KSType,
            receiver: String,
            depth: Int,
        ): String? {
            val typeName = type.declaration.simpleName.asString()
            val components = TUPLE_COMPONENTS.getValue(typeName)
            val rendered = components.mapIndexed { index, component ->
                val componentType = type.arguments[index].type!!.resolve()
                nullSafeValueExpression(
                    imports = imports,
                    type = componentType,
                    accessor = "$receiver.$component",
                    lambdaParameter = "$component$depth",
                    depth = depth + 1,
                ) ?: "$receiver.$component.toString()"
            }
            return "\"$typeName(\" + ${rendered.joinToString(" + \", \" + ")} + \")\""
        }

        /**
         * A typealias has its own declaration, so `IntList` never matches `List` by name.
         * Resolving it lets the aliased type be rendered normally.
         *
         * Only aliases without type arguments are resolved. For a generic alias such as
         * `typealias Mapping<V> = Map<String, V>`, the underlying type still carries the
         * unsubstituted parameter, which would produce code referring to a type that does
         * not exist at the use site.
         */
        private fun typeAliasExpression(
            imports: MutableSet<MemberImport>,
            type: KSType,
            receiver: String,
            propertyDeclaration: KSPropertyDeclaration?,
            depth: Int,
        ): String? {
            val alias = type.declaration as? KSTypeAlias ?: return null
            val substitution = alias.typeParameters
                .map { it.name.asString() }
                .zip(type.arguments)
                .toMap()
            val underlying = substitute(alias.type.resolve(), substitution)
            return valueExpression(imports, underlying, receiver, propertyDeclaration, depth)
        }

        /**
         * Replaces type parameter references in [type] with the arguments supplied at the
         * use site, so that `typealias Mapping<V> = Map<String, V>` used as `Mapping<Int>`
         * resolves to `Map<String, Int>` rather than to `Map<String, V>`.
         *
         * The mapping is by name, not by position: an alias parameter can appear anywhere
         * in the aliased type, or more than once, or not at all.
         */
        private fun substitute(type: KSType, substitution: Map<String, KSTypeArgument>): KSType {
            if (substitution.isEmpty() || type.arguments.isEmpty()) {
                return type
            }
            val substituted = type.arguments.map { argument ->
                val argumentType = argument.type?.resolve()
                val parameterName = (argumentType?.declaration as? KSTypeParameter)?.name?.asString()
                when {
                    parameterName != null -> substitution[parameterName] ?: argument
                    argumentType == null || argumentType.arguments.isEmpty() -> argument
                    // A parameter nested inside the aliased type, eg. List<List<T>>.
                    else -> resolver.getTypeArgument(
                        resolver.createKSTypeReferenceFromKSType(substitute(argumentType, substitution)),
                        argument.variance.takeUnless { it == Variance.STAR } ?: Variance.INVARIANT,
                    )
                }
            }
            return type.replace(substituted)
        }

        /**
         * Enum constants print qualified, as `Direction.NORTH`, which is valid Kotlin as
         * long as the enum is imported where the output is pasted. The generated code
         * only needs the constant's own `name`, so nothing has to be imported into the
         * generated file itself.
         *
         * Returns null when [type] is not an enum.
         */
        private fun enumExpression(type: KSType, receiver: String): String? {
            val declaration = type.declaration as? KSClassDeclaration
            return if (declaration?.classKind == ClassKind.ENUM_CLASS) {
                "\"${declaration.simpleName.asString()}.\" + $receiver.name"
            } else {
                null
            }
        }

        @OptIn(KspExperimental::class)
        private fun annotatedDataClassExpression(
            imports: MutableSet<MemberImport>,
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
            val annotated = generatesDeepPrintFor(propClassDeclaration) ||
                (propClassDeclaration.isDataClass() &&
                    propertyDeclaration?.isAnnotationPresent(DeepPrint::class) == true)
            return if (annotated) {
                // An external class's extension is generated beside this file rather than in
            // the library's package, so importing from the library's package would not
            // resolve. Same package, so no import at all.
            //
            // KotlinPoet does not guard the empty case either: addImport("", "deepPrint")
            // renders as `import deepPrint`. That resolves for a root-package class, but
            // it is noise, and nothing in the same package needs importing anyway.
            if (!propClassDeclaration.isExternal() && propPackageName.isNotEmpty()) {
                imports.add(MemberImport(propPackageName, "deepPrint"))
            }
                "\"\\n\" + $receiver.deepPrint(currentIndent + 2 * indentWidth)"
            } else {
                null
            }
        }

        private fun mapExpression(
            imports: MutableSet<MemberImport>,
            type: KSType,
            receiver: String,
            depth: Int,
        ): String {
            imports.add(MemberImport(DEEP_PRINT_PACKAGE, "deepPrintContents"))
            val ksKeyTypeRef: KSTypeReference = type.arguments[0].type!!
            val ksValueTypeRef: KSTypeReference = type.arguments[1].type!!
            val keyType = ksKeyTypeRef.resolve()
            val valueType = ksValueTypeRef.resolve()
            val mapConstructor = MAP_CONSTRUCTORS.getValue(type.declaration.simpleName.asString())
            val key = "key$depth"
            val value = "value$depth"

            // An annotated data class key keeps deepPrint(), which prints it inline. Every
            // other key is rendered from its static type: a primitive has deepPrint(), an
            // enum and a collection do not, and used to generate code that did not compile.
            val keyTransform = if (isAnnotatedDataClass(keyType)) {
                "$key.deepPrint()"
            } else {
                nullSafeValueExpression(
                    imports = imports,
                    type = keyType,
                    accessor = key,
                    lambdaParameter = "nestedKey$depth",
                    depth = depth + 1,
                ) ?: "$key.deepPrint()"
            }

            val valueTransform = if (valueType.declaration.isDeepPrintAnnotatedDataClass()) {
                // A data class value opens a block, so it sits a level deeper than a
                // value that prints inline next to its key.
                "\"\\n\" + " +
                    "$value.deepPrint(currentIndent + ${DATACLASS_MAP_VAL_INDENT_MULTIPLIER + depth} * indentWidth)"
            } else {
                nullSafeValueExpression(
                    imports = imports,
                    type = valueType,
                    accessor = value,
                    lambdaParameter = "nestedValue$depth",
                    depth = depth + 1,
                ) ?: "$value.toString()"
            }

            return "\"$mapConstructor<$ksKeyTypeRef,$ksValueTypeRef>(\\n\" + " +
                "$receiver.deepPrintContents(\n" +
                "keyTransform = { $key -> " +
                "(currentIndent + ${ENTRY_INDENT_MULTIPLIER + depth} * indentWidth).indent() + ($keyTransform) }," +
                "valueTransform = { $value -> $valueTransform }) + " +
                "(currentIndent + ${CLOSING_INDENT_MULTIPLIER + depth} * indentWidth).indent() + \")\""
        }

        /**
         * Processes the collection types that take a single type argument:
         * List, MutableList, Set, MutableSet and Array.
         * Returns the expression as a listOf(), mutableListOf(), setOf(),
         * mutableSetOf() or arrayOf() function call.
         */
        @OptIn(KspExperimental::class)
        private fun collectionExpression(
            imports: MutableSet<MemberImport>,
            type: KSType,
            receiver: String,
            depth: Int,
        ): String {
            imports.add(MemberImport(DEEP_PRINT_PACKAGE, "deepPrintContents"))
            val itemType = type.arguments[0].type!!
            val resolvedItemType = itemType.resolve()
            val collectionConstructor =
                COLLECTION_CONSTRUCTORS.getValue(type.declaration.simpleName.asString())
            // Nested collections each open their own lambda, so the parameter names have
            // to differ or the inner one shadows the outer.
            val item = "item$depth"

            // An annotated data class opens a block, so those items go one per line.
            if (isAnnotatedDataClass(resolvedItemType)) {
                return "\"$collectionConstructor<${itemType}>(\\n\" + " +
                    "$receiver.joinToString(separator = \"\") " +
                    "{ $item -> $item.deepPrint(" +
                    "currentIndent = currentIndent + ${ENTRY_INDENT_MULTIPLIER + depth} * indentWidth" +
                    ") + \",\\n\" } + " +
                    "(currentIndent + ${CLOSING_INDENT_MULTIPLIER + depth} * indentWidth).indent() + \")\""
            }

            // deepPrintContents() renders items from their runtime type, which loses
            // information: an enum comes out as an unqualified toString(), a UInt without
            // its `u`, a nested collection as `[0, 1]`, and on Kotlin/JS a Float, Double
            // or Char is indistinguishable from a number. The item type is known
            // statically here, so items are rendered from it instead.
            val renderedItem = nullSafeValueExpression(
                imports = imports,
                type = resolvedItemType,
                accessor = item,
                lambdaParameter = "nested$depth",
                depth = depth + 1,
            )
            return if (renderedItem != null) {
                "\"$collectionConstructor<${itemType}>(\" + " +
                    "$receiver.joinToString(separator = \"\") { $item -> \" \" + ($renderedItem) + \",\" } + " +
                    "\")\""
            } else {
                "\"$collectionConstructor<${itemType}>(\" + $receiver.deepPrintContents() + \")\""
            }
        }

        /**
         * Processes IntArray, LongArray and friends. They have no type argument,
         * so the element type is baked into the factory function name.
         */
        private fun primitiveArrayExpression(
            imports: MutableSet<MemberImport>,
            type: KSType,
            receiver: String,
        ): String {
            imports.add(MemberImport(DEEP_PRINT_PACKAGE, "deepPrintContents"))
            val arrayConstructor =
                PRIMITIVE_ARRAY_CONSTRUCTORS.getValue(type.declaration.simpleName.asString())
            return "\"$arrayConstructor(\" + $receiver.deepPrintContents() + \")\""
        }

        private fun KSDeclaration.isDeepPrintAnnotatedDataClass(): Boolean =
            generatesDeepPrintFor(this)
        
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
