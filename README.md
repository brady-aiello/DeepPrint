# DeepPrint
## A utility for printing kotlin data classes with the same syntax as their primary constructor, using [ksp](https://github.com/google/ksp.) 

## Benefits:

1. Data classes are easier to read in logs, as they now look like pretty JSON.
2. Creating a replica object just involves copying and pasting.

Don't print with the default `toString()` like this in your logs:
```
ThreeClassesDeep3(age=55, person=SamplePersonClass(name=Dave, sampleClass=SampleClass(x=0.5, y=2.6, name=A point)), sampleClass=SampleClass(x=0.5, y=2.6, name=A point))
```
Use `deepPrint()` to print this instead:
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
## Simple Example
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
## Deeper Sample
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
## Usage
Given the previous sample classes, we just add the `@DeepPrint` annotation,
and DeepPrint generates the `deepPrint()` extension functions.
```kotlin
@DeepPrint
data class SampleClass(val x: Float, val y: Float, val name: String)

@DeepPrint
data class SamplePersonClass(val name: String, val sampleClass: SampleClass)

@DeepPrint
data class ThreeClassesDeep(val person: SamplePersonClass, val age: Int)
```
## Current Limitations
- DeepPrint only works on `data class`es.
- For the entire printed object to be a valid constructor call, all classes in the hierarchy must be annotated.
- If an annotated data class has a property of a non-annotated class, the property's value is printed with a standard `toString()`.
- DeepPrint supports `List`, `MutableList`, and `Array` but does not support all collections yet.

## Quick Start

### Add KSP
You can reference the [KSP quickstart docs](https://kotlinlang.org/docs/ksp-quickstart.html#use-your-own-processor-in-a-project) for this, or check out the sample projects, [test-project](./test-project/src/test/kotlin/com/bradyaiello/deepprint/BasicTest.kt)
and [test-project-multiplatform](./test-project-multiplatform/src/commonTest/kotlin/com/bradyaiello/deepprint/BasicTest.kt)

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
    id("com.google.devtools.ksp") version "1.8.0-1.0.8"
}
```
### Single Platform
1. Apply the KSP Plugin in `build.gradle.kts`
```kotlin
plugins {
    kotlin("jvm") // or other platform
    id("com.google.devtools.ksp")
}
```
2. Add the dependencies
```kotlin
// These will change after publishing
dependencies {
    // @DeepPrint annotation and a few helper functions
    implementation(project(":annotations"))
    // Where all the DeepPrint code generation logic resides
    implementation(project(":deep-print-processor"))
    // Apply the KSP plugin to the processor
    ksp(project(":deep-print-processor"))
}
```
3. Tell Gradle where to find the KSP-generated code.
```kotlin
kotlin.sourceSets {
    main {
        kotlin.srcDirs(
            file("$buildDir/generated/ksp/main/kotlin"),
        )
    }
    test {
        kotlin.srcDirs(
            file("$buildDir/generated/ksp/test/kotlin"),
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
        val commonMain by getting {
            dependencies {
                implementation(project(":annotations"))
            }
            kotlin.srcDir("$buildDir/generated/ksp/metadata/commonMain/kotlin")
        }
    }
}
```
3. Run KSP on the `commonMain` source set before any other compile tasks.
```kotlin
// https://github.com/evant/kotlin-inject/issues/193#issuecomment-1112930931
tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
    kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
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
## Consume as a Dependency?
For now, you will need to clone the project to try it out.
I will try to publish this as soon as I can.
## Thanks
Thank you Pavlo Stavytskyi for the sample KSP project and its accompanying article.
https://github.com/Morfly/ksp-sample
