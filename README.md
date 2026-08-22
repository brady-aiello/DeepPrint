# DeepPrint
## A utility for printing kotlin data classes with the same syntax as their primary constructor.

## Benefits:

1. Data classes are easier to read in logs, as they now look like pretty JSON.
2. Creating a replica object just involves copying and pasting.

Don't print with the default `toString()` like this in your logs:
```
ThreeClassesDeep3(age=55, person=SamplePersonClass(name=Dave, sampleClass=SampleClass(x=0.5, y=2.6, name=A point)), sampleClass=SampleClass(x=0.5, y=2.6, name=A point))
```
Use `deepPrint()` or `deepPrintReflection()` to print this instead:
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

## KSP vs. Reflection
DeepPrint offers 2 implementations: 1 using KSP and the other using reflection.
They have similar functionality, but don't have exact parity. This is partly 
due to the limitations of reflection, and partly because some features have
not been added yet.

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
implementation("com.bradyaiello.deepprint:deep-print-reflection:<latest-version>")
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
| `ByteArray`, `ShortArray`, `IntArray`, `LongArray` | `byteArrayOf(...)`, `shortArrayOf(...)`, `intArrayOf(...)`, `longArrayOf(...)` |
| `FloatArray`, `DoubleArray`, `BooleanArray`, `CharArray` | `floatArrayOf(...)`, `doubleArrayOf(...)`, `booleanArrayOf(...)`, `charArrayOf(...)` |

Unlike the reflection implementation, KSP sees the declared type, so a `Set` prints
as `setOf()` and a `MutableSet` prints as `mutableSetOf()`.

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
    implementation("com.bradyaiello.deepprint:deep-print-annotations:0.1.0-alpha")
    // Where all the DeepPrint code generation logic resides
    implementation("com.bradyaiello.deepprint:deep-print-processor:0.1.0-alpha")
    // Run the processor over your main source set
    ksp("com.bradyaiello.deepprint:deep-print-processor:0.1.0-alpha")
    // KSP 2 no longer fans `ksp` out to every source set, so annotate-in-tests
    // needs the processor wired up for the test source set too
    kspTest("com.bradyaiello.deepprint:deep-print-processor:0.1.0-alpha")
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
                implementation("com.bradyaiello.deepprint:deep-print-annotations:0.1.0-alpha")
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
## Multiplatform Support 
This project supports JVM, iOS, watchOS, macOS, Linux, Windows, NodeJS and JS for the browser.
Check out [test-project-multiplatform](./test-project-multiplatform) and the docs above for setup.
The classes for the KMP example are defined in the `commonMain` source set because KSP does not yet support the `commonTest` source set.
That is not true for single source projects, like [test-project](./test-project).

## Current Limitations
- DeepPrint only works on `data class`es.
- For KSP, for the entire printed object to be a valid constructor call, all classes in the hierarchy must be annotated.
- For KSP, if an annotated data class has a property of a non-annotated class, the property's value is printed with a 
  standard `toString()`.
- Not all collection types are supported. See
  [KSP and Collection Types](#KSP-and-Collection-Types) for what KSP handles, and
  [Reflection and Collection Types](#Reflection-and-Collection-Types) for reflection.
  Still missing, in both implementations:
  - `Collection`, `Iterable` and `Sequence` properties, and `Pair` and `Triple`.
    These fall back to `toString()`, so the output is readable but is not a
    constructor call.
  - Nested collections, eg. `List<List<Int>>` or `Map<String, List<Int>>` with a
    collection value. The outer collection prints correctly, but the inner one prints
    via `toString()` as `[0, 1]`, which is not valid Kotlin.
  - The unsigned arrays `UByteArray`, `UShortArray`, `UIntArray` and `ULongArray`.
  - A property whose type is a `typealias` for a `@DeepPrint` `data class`. The alias
    is not resolved, so the property falls back to `toString()`.
- Nullable properties are not supported. A `val items: List<Int>?` generates code that
  does not compile. This is not specific to collections: `val name: String?` has the
  same problem.
- On Kotlin/JS, `Float`, `Double` and `Char` elements of a `List`, `Set` or `Array` are
  identified by their runtime type, which JS erases to a number. `2.0f` prints as `2` and
  `'A'` prints as `65`. `FloatArray`, `DoubleArray` and `CharArray` are not affected, and
  neither are properties declared directly on a `data class`.
- In KMP projects, KSP [does not yet support](https://github.com/google/ksp/issues/567) generating code from the 
  `commonTest` source set. Hence, test classes for the KMP test project are in `commonMain`.

## Thanks
Thank you Pavlo Stavytskyi for the sample KSP project and its accompanying article.
https://github.com/Morfly/ksp-sample
