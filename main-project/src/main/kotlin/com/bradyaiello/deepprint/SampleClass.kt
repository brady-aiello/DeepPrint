package com.bradyaiello.deepprint


@DeepPrint
data class SampleClass(val x: Float, val y: Float, val name: String)

@DeepPrint
data class SamplePersonClass(val name: String, val sampleClass: SampleClass)

@DeepPrint
data class ThreeClassesDeep(val person: SamplePersonClass, val age: Int)

@DeepPrint
data class ThreeClassesDeep2(val age: Int, val person: SamplePersonClass)

@DeepPrint
data class ThreeClassesDeep3(val age: Int, val person: SamplePersonClass, val sampleClass: SampleClass)

@DeepPrint
data class AllTypes(
    val aString: String = "Hello",
    val aChar: Char = 'A',
    val anInt: Int = 0,
    val aByte: Byte = 255.toByte(),
    val aShort: Short = 2,
    val aLong: Long = 1000L,
    val aBoolean: Boolean = true,
    val aFloat: Float = 1234f,
    val aDouble: Double = 56789.0
)