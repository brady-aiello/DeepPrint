# DeepPrint
## A utility for printing kotlin data classes with the same syntax as their primary constructor.

## Benefits:

1. Data classes are easier to read in logs, as they now look like pretty JSON.
2. Creating a replica object just involves copying and pasting.

Don't print with the default `toString()` like this in your logs:
```
ThreeClassesDeep3(age=55, person=SamplePersonClass(name=Dave, sampleClass=SampleClass(x=0.5, y=2.6, name=A point)), sampleClass=SampleClass(x=0.5, y=2.6, name=A point))
```
Call `deepPrint()` or `deepPrintReflection()` to print this instead -- or apply the
[compiler plugin](#Compiler-Plugin) and get it from `toString()` without calling
anything:
```kotlin
ThreeClassesDeep3(
  age = 55,
  person = 
    SamplePersonClass(
      name = "Dave",
      sampleClass = 
        SampleClass(
          x = 0.5f,
          y = 2.6f,
          name = "A point",
        ),
    ),
  sampleClass = 
    SampleClass(
      x = 0.5f,
      y = 2.6f,
      name = "A point",
    ),
)
```

## Three Ways to Use It
Two implementations -- one using KSP, the other using reflection -- and a Gradle plugin
that puts the KSP one behind `toString()`. They have similar functionality, but don't
have exact parity. This is partly due to the limitations of reflection, and partly
because some features have not been added yet.

| | What you call | Where it runs |
| --- | --- | --- |
| [KSP](#KSP) | `deepPrint()` | Every target, generated at compile time |
| [Reflection](#Reflection) | `deepPrintReflection()` | JVM only, at runtime |
| [Compiler plugin](#Compiler-Plugin) | nothing -- `toString()` does it | JVM, JS and Native; builds on KSP |

### KSP
[Kotlin Symbol Processing](https://github.com/google/ksp) is a configurable
code generation Kotlin compiler plugin from Google. Its benefits are:

- Kotlin Multiplatform support
- More precise type information at compile time
- Likely faster at runtime

To see it in action, check out [KSP Simple Example](#KSP-Simple-Example) and
[KSP Deeper Example](#KSP-Deeper-Example).
To try it out, please refer to [KSP Quick Start](#KSP-Quick-Start).

### Reflection
The reflection implementation is only for Kotlin on the JVM, but its benefits are:

- Only 1 dependency to add
- No plugin to apply
- No need to recompile / regenerate any code on changes to your `data class`es

## Reflection Quick Start

If you're using Kotlin on the JVM or Android, just add the dependency:

```kotlin
implementation("com.bradyaiello.deepprint:deep-print-reflection:0.6.0")
```

Now, calling `deepPrintReflection()` on a `data class` will return a readable `String`
that is a valid Kotlin constructor call:

```kotlin
MapContainer(
    name = "my map",
    mapToHold =  mutableMapOf(
        "Monday" to
            Dish(
                name = "Pizza",
                ingredients =  mutableListOf(
                    "dough",
                    "tomato sauce",
                    "cheese",
                ),
            ),
        "Tuesday" to
            Dish(
                name = "Mac n Cheese",
                ingredients =  mutableListOf(
                    "mac",
                    "cheese",
                ),
            ),
    ),
    id = 12345,
)
```

### Reflection and Collection Types

There are also versions of `deepPrintReflection()` just for collection types.

```kotlin
listOf("Hi", "Hey", "How's it going?", "What's up?", "Hello")
    .deepPrintListReflection()
```

The above prints:

```kotlin
listOf(
    "Hi",
    "Hey",
    "How's it going?",
    "What's up?",
    "Hello",
)
```

Why the different function names for collections?

You may notice in the `MapContainer` that it prints `mutableMapOf()`
and not `mapOf()`.
At runtime, we can't know if we're dealing with a `Map` or a `MutableMap`.
`MutableMap` fits the bill for both, so in a `data class`, `mutableMapOf()`,
`mutableListOf()`, etc. are used.
However, If you're only printing the collection as a standalone object, then
you know the type, and may want to reflect that.
For this reason, there are functions for both mutable and immutable variants,
eg. `deepPrintListReflection()` and `deepPrintMutableListReflection()`.

The same applies to `Set`: a property declared `Set<T>` or `MutableSet<T>` prints as
`mutableSetOf()`, while standalone there are `deepPrintSetReflection()` and
`deepPrintMutableSetReflection()`.

These are the collection types reflection handles:

| Property type | Printed as | Standalone function |
| --- | --- | --- |
| `List<T>`, `MutableList<T>` | `mutableListOf(...)` | `deepPrintListReflection()`, `deepPrintMutableListReflection()` |
| `Set<T>`, `MutableSet<T>` | `mutableSetOf(...)` | `deepPrintSetReflection()`, `deepPrintMutableSetReflection()` |
| `Map<K, V>`, `MutableMap<K, V>` | `mutableMapOf(...)` | `deepPrintMapReflection()`, `deepPrintMutableMapReflection()` |
| `Array<T>` | `arrayOf(...)` | `deepPrintArrayReflection()` |
| `ByteArray` | `byteArrayOf(...)` | `deepPrintByteArrayReflection()` |
| `ShortArray` | `shortArrayOf(...)` | `deepPrintShortArrayReflection()` |
| `IntArray` | `intArrayOf(...)` | `deepPrintIntArrayReflection()` |
| `LongArray` | `longArrayOf(...)` | `deepPrintLongArrayReflection()` |
| `FloatArray` | `floatArrayOf(...)` | `deepPrintFloatArrayReflection()` |
| `DoubleArray` | `doubleArrayOf(...)` | `deepPrintDoubleArrayReflection()` |
| `BooleanArray` | `booleanArrayOf(...)` | `deepPrintBooleanArrayReflection()` |
| `CharArray` | `charArrayOf(...)` | `deepPrintCharArrayReflection()` |

The primitive arrays need no mutable/read-only distinction, so each has a single
function. Their element type is known from the array type itself, so unlike
`List` and `Set` there is nothing lost to erasure.

### Reflection and Values It Cannot Reconstruct

A `null` property prints as `null`. An enum prints qualified, so
`day = DayOfWeek.MONDAY` is valid Kotlin once the enum is imported.

Anything else that is neither a primitive, a supported collection, nor a `data class`
falls back to `toString()`:

```kotlin
OpaqueContainer(
    id = Opaque(abc),
    name = "x",
)
```

That line is not something you can paste back into a test, but the property is at
least present and says what it held.

## KSP Simple Example
For a simple example, we'll use a small class `SampleClass`:
```kotlin
data class SampleClass(val x: Float, val y: Float, val name: String)
```
Calling `sampleClass.toString()` results in:
```text
SampleClass(x=0.5, y=2.6, name=A point)
``` 
If we call `sampleClass.deepPrint()` we get readable `String` that is also a valid Kotlin constructor call:
```kotlin
SampleClass(
    x = 0.5f,
    y = 2.6f,
    name = "A point",
)
```
This can save a lot of time turning real data into test data on deeper objects.
## KSP Deeper Example
Given the classes:
```kotlin
data class SampleClass(val x: Float, val y: Float, val name: String)

data class SamplePersonClass(val name: String, val sampleClass: SampleClass)

data class ThreeClassesDeep(val person: SamplePersonClass, val age: Int)
```
If we call `threeClassesDeep.toString()` we get this output all on a single line, which is not valid code:
```text
ThreeClassesDeep(person=SamplePersonClass(name=Brady, sampleClass=SampleClass(x=0.5, y=2.6, name=A point)), age=37)
```
But, if we call 
```kotlin
threeClassesDeep.deepPrint()
```
Our text output is valid Kotlin:
```kotlin
ThreeClassesDeep(
    person = 
        SamplePersonClass(
            name = "Brady",
            sampleClass = 
                SampleClass(
                    x = 0.5f,
                    y = 2.6f,
                    name = "A point",
                ),
        ),
    age = 37,
)
```
We can just copy this from a log and use it in a test without modification.
You can see more examples in [test-project](./test-project/src/test/kotlin/com/bradyaiello/deepprint/BasicTest.kt) and [test-project-multiplatform](./test-project-multiplatform/src/commonTest/kotlin/com/bradyaiello/deepprint/BasicTest.kt)
### KSP and Collection Types

A property whose type is one of these is printed as the call that rebuilds it:

| Property type | Printed as |
| --- | --- |
| `List<T>` | `listOf<T>(...)` |
| `MutableList<T>` | `mutableListOf<T>(...)` |
| `Set<T>` | `setOf<T>(...)` |
| `MutableSet<T>` | `mutableSetOf<T>(...)` |
| `Array<T>` | `arrayOf<T>(...)` |
| `Map<K, V>` | `mapOf<K, V>(...)` |
| `MutableMap<K, V>` | `mutableMapOf<K, V>(...)` |
| `ArrayList<T>` | `arrayListOf<T>(...)` |
| `HashSet<T>` | `hashSetOf<T>(...)` |
| `LinkedHashSet<T>` | `linkedSetOf<T>(...)` |
| `HashMap<K, V>` | `hashMapOf<K, V>(...)` |
| `LinkedHashMap<K, V>` | `linkedMapOf<K, V>(...)` |
| `Collection<T>`, `Iterable<T>` | `listOf<T>(...)` |
| `UByteArray`, `UShortArray`, `UIntArray`, `ULongArray` | `ubyteArrayOf(...)`, `ushortArrayOf(...)`, `uintArrayOf(...)`, `ulongArrayOf(...)` |
| `Pair<A, B>`, `Triple<A, B, C>` | `Pair(a, b)`, `Triple(a, b, c)` |
| `ByteArray`, `ShortArray`, `IntArray`, `LongArray` | `byteArrayOf(...)`, `shortArrayOf(...)`, `intArrayOf(...)`, `longArrayOf(...)` |
| `FloatArray`, `DoubleArray`, `BooleanArray`, `CharArray` | `floatArrayOf(...)`, `doubleArrayOf(...)`, `booleanArrayOf(...)`, `charArrayOf(...)` |

Unlike the reflection implementation, KSP sees the declared type, so a `Set` prints
as `setOf()` and a `MutableSet` prints as `mutableSetOf()`.

A collection is also supported as a map value, so `Map<String, List<Int>>` prints as

```kotlin
listsByName = mapOf<String,List<Int>>(
    "a" to listOf<Int>( 1, 2,),
),
```

### KSP and Enums

An enum property prints qualified, so the output is valid Kotlin as long as the enum is
imported where you paste it. This holds wherever the enum appears -- as a property, as a
collection item, and as a map key or value:

```kotlin
WithEnums(
    direction = Direction.NORTH,
    directions = listOf<Direction>( Direction.NORTH, Direction.SOUTH,),
    bySide = mapOf<Direction,String>(
        Direction.NORTH to "up",
    ),
)
```

An enum nested in another class prints with its own simple name, eg. `Color.RED` for
`Outer.Color`, so `import com.example.Outer.Color` is what makes that output compile.

### Nested Collections

Collections nest, in both implementations. A collection can be an item of another
collection, a map key, or a map value:

```kotlin
WithNestedMaps(
    mapOfMaps = mapOf<String,Map<String, Int>>(
        "a" to mapOf<String,Int>(
            "b" to 1,
        ),
    ),
    listKeyed = mapOf<List<Int>,String>(
        listOf<Int>( 1, 2,) to "x",
    ),
)
```

Collections of primitives stay on one line whatever the depth, so
`List<List<List<Int>>>` prints as
`listOf<List<List<Int>>>( listOf<List<Int>>( listOf<Int>( 1,),),)`. A map, or a
collection of annotated `data class`es, opens a block and is indented to its depth.

### KSP and the Remaining Types

`Pair` and `Triple` print as constructor calls on one line, with each component
rendered by its own type:

```kotlin
pair = Pair("a", 1),
triple = Triple(1, true, 'x'),
```

The unsigned types print with the `u` suffix, so the value is assignable back to a
`UInt` rather than being read as an `Int`:

```kotlin
int = 3u,
ints = uintArrayOf( 5u, 6u,),
```

A `typealias` is resolved, so `typealias IntList = List<Int>` prints as a list, and an
alias for an annotated `data class` deep prints rather than falling back to
`toString()`. Generic aliases resolve too, with the arguments from the use site
substituted in by name rather than by position:

```kotlin
typealias Mapping<V> = Map<String, V>   // the parameter is not the first argument
typealias Grid<T> = List<List<T>>       // the parameter is nested
```

### Data Classes From Other Modules

A `data class` from a dependency prints in full, with no annotation and no configuration:

```kotlin
@DeepPrint
data class Order(val customer: CustomerFromAnotherModule, val id: String)
```

```kotlin
Order(
    customer = 
        CustomerFromAnotherModule(
            name = "Bruce Wayne",
            age = 42,
        ),
    id = "985270457834522",
)
```

It cannot work through the annotation: `@DeepPrint` has `SOURCE` retention, so it is gone
by the time a class is a dependency. The processor generates the extension for the
external class itself, into the package of whatever referred to it rather than into the
library's own package, so two modules doing this cannot collide.

This applies in both modes. A dependency's class can never be annotated, so making it
opt-in would have meant it never worked at all.

Across the three ways of using DeepPrint:

| | A data class from another module |
| --- | --- |
| Reflection | Works, and always did. It reads the runtime class, so a module boundary is invisible to it |
| KSP `deepPrint()` | Works |
| `overrideToString` | Works when printing *your* class; see below |

With `overrideToString`, a local class prints its external property in full, because that
goes through the generated `deepPrint()`. Printing the dependency's class **directly**
gives its own `toString()`:

```kotlin
println(order)             // deep printed, external customer included
println(order.customer)    // CustomerFromAnotherModule(name=Bruce Wayne, age=42)
```

The plugin rewrites `toString()` while compiling your module. A class that arrived as a
jar is already compiled, so its `toString()` is out of reach — no compiler plugin can
reach back into a dependency.

### KSP and Nullable Properties

A nullable property prints as the `null` literal when it is absent, and normally when
it is not. This works for every supported type -- primitives, collections, maps,
primitive arrays, and annotated `data class`es:

```kotlin
@DeepPrint
data class Maybe(val name: String?, val items: List<Int>?, val person: Person?)
```

```kotlin
Maybe(
    name = null,
    items = null,
    person = null,
)
```

Elements may be primitives or `@DeepPrint`-annotated `data class`es. Given:

```kotlin
@DeepPrint
data class Surfer(val name: String, val surfboard: Surfboard)

@DeepPrint
data class Lineup(val name: String, val surfers: Set<Surfer>)
```

`lineup.deepPrint()` prints:

```kotlin
Lineup(
    name = "Pipeline",
    surfers = setOf<Surfer>(
        Surfer(
            name = "Kelly Slater",
            surfboard = 
                Surfboard(
                    length = 5.9f,
                    width = 1.8f,
                    style = "shortboard",
                ),
        ),
    ),
)
```

## Usage
Given the previous sample classes, we just add the `@DeepPrint` annotation,
and DeepPrint generates the `deepPrint()` extension functions.
Like `@Parcelable`, all `data class` properties of a `data class` must also
be annotated.

```kotlin
@DeepPrint
data class SampleClass(val x: Float, val y: Float, val name: String)

@DeepPrint
data class SamplePersonClass(val name: String, val sampleClass: SampleClass)

@DeepPrint
data class ThreeClassesDeep(val person: SamplePersonClass, val age: Int)
```
## No-Annotation Mode

Annotating every class gets tedious, and the requirement that *every* class in a
hierarchy be annotated makes it easy to get a half-deep print. Turn that off:

```kotlin
ksp {
    arg("processAllDataClasses", "true")
}
```

Every `data class` in the source set now gets a `deepPrint()`, with no annotation
anywhere, and nested `data class` properties print in full rather than falling back to
`toString()`.

To exclude a class, annotate it — the annotation is now only needed by the exceptions:

```kotlin
@NoDeepPrint
data class HugePayload(val bytes: List<Byte>)
```

Some classes are skipped automatically, because a generated top level extension in
another file could not reach them:

| Skipped | Why |
| --- | --- |
| `private` and local classes | Not visible outside their own file or scope |
| `protected` nested classes | Not visible outside the class hierarchy |

`internal` classes are included, and their generated function is `internal` too — a
public extension on an internal receiver does not compile.

Nested classes are qualified, so `Outer.Inner` prints as `Outer.Inner(...)` and the
generated file is `DeepPrintOuter_Inner.kt` rather than colliding with a top level
`Inner`.

## KSP Quick Start

### Add KSP
You can reference the [KSP quickstart docs](https://kotlinlang.org/docs/ksp-quickstart.html#use-your-own-processor-in-a-project) for this, or check out the sample projects:
[test-project](./test-project/build.gradle.kts) is for Kotlin for the JVM
and [test-project-multiplatform](./test-project-multiplatform/build.gradle.kts) tests all targets DeepPrint supports.

1. Let Gradle know where it can find the KSP Gradle plugin in `settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
```
2. And tell it which version you want in `build.gradle.kts`:
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.11"
}
```
### Single Platform
1. Apply the KSP Plugin in `build.gradle.kts`
```kotlin
plugins {
    kotlin("jvm") // or another platform
    id("com.google.devtools.ksp")
}
```
2. Add the dependencies
```kotlin
dependencies {
    // @DeepPrint annotation and a few helper functions
    implementation("com.bradyaiello.deepprint:deep-print-annotations:0.6.0")
    // Where all the DeepPrint code generation logic resides
    implementation("com.bradyaiello.deepprint:deep-print-processor:0.6.0")
    // Run the processor over your main source set
    ksp("com.bradyaiello.deepprint:deep-print-processor:0.6.0")
    // KSP 2 no longer fans `ksp` out to every source set, so annotate-in-tests
    // needs the processor wired up for the test source set too
    kspTest("com.bradyaiello.deepprint:deep-print-processor:0.6.0")
}
```
3. Tell Gradle where to find the KSP-generated code.
```kotlin
kotlin.sourceSets {
    main {
        kotlin.srcDirs(
            layout.buildDirectory.dir("generated/ksp/main/kotlin"),
        )
    }
    test {
        kotlin.srcDirs(
            layout.buildDirectory.dir("generated/ksp/test/kotlin"),
        )
    }
}
```
4. Optionally, configure the number of spaces for indentation; it defaults to 4.
```kotlin
ksp {
    arg("indent", "2")
}
```
### Multiplatform

1. Apply the KSP Plugin in `build.gradle.kts`
```kotlin
plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}
```
2. Add the annotations dependency, and tell Gradle where it can find the generated code. 
```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("com.bradyaiello.deepprint:deep-print-annotations:0.6.0")
            }
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
        }
    }
}
```
3. Run KSP on the `commonMain` source set before any other compile or KSP task.
```kotlin
// https://github.com/evant/kotlin-inject/issues/193#issuecomment-1112930931
tasks.configureEach {
    if (name != "kspCommonMainKotlinMetadata" &&
        (name.startsWith("compile") || name.startsWith("ksp"))
    ) {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
```
4. Tell KSP what processor(s) to use, and for what configurations. Here we're assuming we just run it against the `commonMain` source set.
```kotlin
dependencies {
    add("kspCommonMainMetadata", project(":deep-print-processor"))
}
```
5. Optionally, configure the number of spaces for indentation; it defaults to 4.

```kotlin
ksp {
    arg("indent", "2")
}
```

## Compiler Plugin

The third way to use DeepPrint, alongside [KSP](#KSP) and [Reflection](#Reflection): a
`data class` prints itself, and nothing in your code calls anything.

It is a Kotlin compiler plugin, shipped as a Gradle plugin, and it builds on KSP rather
than replacing it -- what it rewrites `toString()` to call is the `deepPrint()` KSP
generates, which is why both are applied below. It works either way round: with
`@DeepPrint` on your classes, or with [No-Annotation Mode](#No-Annotation-Mode) and no
annotation anywhere.

### Compiler Plugin Quick Start

Apply it and turn it on:

```kotlin
plugins {
    kotlin("multiplatform") // or kotlin("jvm")
    id("com.google.devtools.ksp")
    id("com.bradyaiello.deepprint") version "0.6.0"
}

deepPrint {
    overrideToString.set(true)
}
```

`toString()` then returns the deep printed form, which means so do string templates, log
statements, assertion failures, and the elements of any collection you print:

```kotlin
println(point)
// Point(
//     x = 1,
//     y = 2,
// )
```

This works on JVM, JS and Native. What it replaces is the `toString()` the compiler
synthesises for a `data class`; the printing it replaces it with is the same ordinary
Kotlin on every target, so there is nothing target-specific in the output.

**It is off by default, and worth a moment's thought before turning on.** It changes
every log line, every string template and every debugger view in the module, including
when a data class is nested inside something else being printed.

Two things it will not touch:

| | |
| --- | --- |
| A `toString()` you wrote yourself | Only the compiler-synthesised one is replaced |
| A class annotated `@NoDeepPrint` | Opted out, as in [No-Annotation Mode](#No-Annotation-Mode) |

`ksp { arg("overrideToString", "true") }` is **not** how to enable this, and warns if you
try. A symbol processor can only add new files; it cannot alter an existing class, and
neither an extension nor an interface can supply `toString()` — a member always wins over
an extension, and Kotlin forbids interfaces from implementing `Any`'s methods.

## Multiplatform Support 
This project supports JVM, iOS, watchOS, macOS, Linux, Windows, NodeJS and JS for the browser.
Check out [test-project-multiplatform](./test-project-multiplatform) and the docs above for setup.
The classes for the KMP example are defined in the `commonMain` source set because KSP does not yet support the `commonTest` source set.
That is not true for single source projects, like [test-project](./test-project).

## Value Classes
A `value class` prints as a call to its own constructor, in both KSP and reflection:

```kotlin
data class Order(val id: UserId, val distance: Meters)

Order(
    id = UserId(raw = "abc"),
    distance = Meters(amount = 1.5),
)
```

The wrapped value is rendered by the same rules as any other single value, so a value
class around a `Char` or a `String` keeps its quotes. Printing it with `toString()`
would give `UserId(raw=abc)`, which reads correctly in a log and is not valid Kotlin.

## Sealed Classes and Nesting
A nested class prints qualified through its nesting, so the output resolves:

```
shape = Marker.Absent(
    reason = "gone",
),
level = EnumHost.Level.HIGH,
```

This is where the two implementations genuinely differ. A property declared as a sealed
*parent* type holds one of its subclasses at runtime, and only reflection can see which:

```kotlin
data class Diagram(val shape: Marker)
```

Reflection deep prints the subclass it finds. KSP sees the declared type, which is not a
`data class`, so it falls back to `toString()` -- there is no type to generate against.
Annotating the subclass does not help, because the property still says `Marker`.

## When DeepPrint Cannot Help
`@DeepPrint` on something it cannot generate for is a build error naming the reason,
rather than silence:

```
e: [ksp] Classes.kt:6: DeepPrint: cannot generate deepPrint() for Settings because it
is a class rather than a data class, and deepPrint() prints a call to a primary
constructor.
```

The cases are a class that is not a `data class`, and a `data class` that is `private`,
`protected` or local -- the generated extension is a separate file in the same package,
so it cannot reach one.

[No-Annotation Mode](#No-Annotation-Mode) says nothing, and walks past anything it
cannot print. Nobody asked for those, and a module is full of them.

## Cyclic Data
A `data class` that can reach itself -- a node holding its parent, two objects pointing
at each other -- has no constructor call that would rebuild it, because each object would
have to exist before the other could be written. Reflection detects it and prints a
`TODO()` naming the class rather than recursing until the stack runs out:

```kotlin
val node = Node("a", null)
node.next = node

Node(
    name = "a",
    next = TODO("DeepPrint: cycle back to Node"),
)
```

`TODO()` is `Nothing`, so this compiles wherever the property sits, nullable or not, and
cannot be run by accident. The same object appearing twice is repetition rather than a
cycle and still prints in full both times; only an object currently being printed further
up counts.

KSP does the same. The generated `deepPrint()` takes the list of data classes already
being printed as a second parameter, defaulted so nothing calling `deepPrint()` has to
know about it:

```kotlin
public fun Node.deepPrint(currentIndent: Int = 0, ancestors: MutableList<Any>? = null): String
```

The collection helpers needed no change: the recursion into elements happens inside
lambdas the processor writes, so they close over the same list. `overrideToString` is
covered too, since it delegates to the same function.

## Current Limitations
- DeepPrint only works on `data class`es.
- For KSP, for the entire printed object to be a valid constructor call, all classes in the hierarchy must be annotated,
  and a property of a non-annotated class is printed with a standard `toString()`. Neither applies in
  [No-Annotation Mode](#No-Annotation-Mode), and neither applies to a `data class` from
  another module -- see [Data Classes From Other Modules](#Data-Classes-From-Other-Modules).
- Overriding `toString()` needs the `com.bradyaiello.deepprint` Gradle plugin, not a KSP
  option. See [Compiler Plugin](#Compiler-Plugin).
- `overrideToString` rewrites `toString()` for the data classes in your own module,
  including nested and generic ones. The one thing it cannot reach is a `data class`
  from a dependency: that class is already
  compiled. It still prints in full as a property of one of your own classes. See
  [Data Classes From Other Modules](#Data-Classes-From-Other-Modules).
- A generic property is printed with `toString()`. The processor runs before type
  arguments are known, so there is nothing better it can do; `GenericBox("s")` prints
  `boxed = s`, not `boxed = "s"`. The class itself deep prints normally.
- A `Sequence` property is not reconstructed; it prints with `toString()`. Printing one
  would have to iterate it, which consumes a single-use sequence and exhausts the heap
  on an infinite one. `Collection` and `Iterable` are supported, on the assumption that
  they can be iterated more than once.
- Not every type is supported. See
  [KSP and Collection Types](#KSP-and-Collection-Types) for what KSP handles, and
  [Reflection and Collection Types](#Reflection-and-Collection-Types) for reflection.
- For KSP, a property declared as a sealed parent type prints with `toString()`. The
  runtime subclass is not knowable when the processor runs. See
  [Sealed Classes and Nesting](#Sealed-Classes-and-Nesting).
- For reflection, an `object` prints as its name, qualified through its nesting, eg.
  `marker = Marker.Present`. This is valid Kotlin, the same as the enum case.
- For reflection, a property that is neither a primitive, a supported collection, nor a
  `data class` is printed with `toString()`. Enums are the exception and print
  qualified, eg. `day = DayOfWeek.MONDAY`, which is valid Kotlin as long as the enum is
  imported.
- In KMP projects, KSP [does not yet support](https://github.com/google/ksp/issues/567) generating code from the 
  `commonTest` source set. Hence, test classes for the KMP test project are in `commonMain`.

## Changing the Public API
`deep-print-annotations` and `deep-print-reflection` are what consumers compile and link
against, so their public ABI is checked into `api/` and verified on every PR. A change
that alters it fails the build with:

```
ABI check failed for project deep-print-annotations
  <<<ABI has changed>>>
```

That is not necessarily wrong -- it is asking you to confirm the change is intended.
Regenerate the dumps and commit them alongside the change:

```
./gradlew :deep-print-annotations:updateKotlinAbi :deep-print-reflection:updateKotlinAbi
```

The annotations dump covers klibs for all 14 targets, so regenerating it needs a macOS
host. The diff on those files is the review: an added declaration is additive and safe,
while a changed or removed signature breaks consumers at link time rather than at
compile time, which is the failure this is here to prevent.

## Thanks
Thank you Pavlo Stavytskyi for the sample KSP project and its accompanying article.
https://github.com/Morfly/ksp-sample
