package com.bradyaiello.deepprint.testclasses

import com.bradyaiello.deepprint.DeepPrint
import com.bradyaiello.deepprint.testclasses.otherpackage.Surfboard
import com.bradyaiello.deepprint.testclasses.otherpackage.Temperature
import com.module.external.ExternalDataClass
import kotlinx.datetime.Instant

@DeepPrint
data class SampleClass(val x: Float, val y: Float, val name: String)


@DeepPrint
data class ThreeDimLine(
    val start: ThreeDimCoordinate,
    val end: ThreeDimCoordinate,
    val lineLabel: String
)

data class ThreeDimCoordinate(
    val x: Float,
    val y: Float,
    val z: Float,
    val label: String
)
data class Name(val name: String)

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

@DeepPrint
data class Surfer(val name: String, val surfboard: Surfboard)

@DeepPrint
data class Weather(val temperature: Temperature)

@DeepPrint
data class WithAList(val name: String, val items: List<Int>)

@DeepPrint
data class WithDeepPrintableList(val name: String, val surfers: List<Surfer>)

@DeepPrint
data class WithAnnotatedProperty(
    val label: String,
    @property:DeepPrint
    val name: Name
)

@DeepPrint
data class UsingUnannotatedDataClassFromExternalModule(
    val externalDataClass: ExternalDataClass,
    val id: String
)