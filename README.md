# DeepPrint
## A utility for printing kotlin data classes with the same syntax as their primary constructor. The benefits are:

1. Data classes are easier to read in logs, as they look like pretty JSON.
2. Creating a replica object just involves copying and pasting.

## Simple Example
For a simple example, we'll use a small class `SampleClass`:
```kotlin
data class SampleClass(val x: Float, val y: Float, val name: String)
```
Calling `sampleClass.toString()` results in:

```text
SampleClass(x=0.5, y=2.6, name=A point)
``` 
If we call `sampleClass.deepPrint()` we get readable `String` that is also a valid Kotlin constructor:
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
If we call `threeClassesDeep.toString()` we get this output, which is not valid code:
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

## How To Use It
Given the previous sample classes, we just add the `@DeepPrint` annotation,
and DeepPrint generates the functions.
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
- All classes in the hierarchy must be annotated.
- DeepPrint supports Lists, but does not support collections yet.



## Thanks
Thank you Pavlo Stavytskyi for the sample KSP project and its accompanying article.
https://github.com/Morfly/ksp-sample
