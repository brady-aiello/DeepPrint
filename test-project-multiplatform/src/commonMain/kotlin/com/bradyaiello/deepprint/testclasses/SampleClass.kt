package com.bradyaiello.deepprint.testclasses

import com.bradyaiello.deepprint.DeepPrint
import com.bradyaiello.deepprint.testclasses.otherpackage.Surfboard
import com.bradyaiello.deepprint.testclasses.otherpackage.Temperature
import com.module.external.ExternalDataClass

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
data class WithAMutableList(val name: String, val items: MutableList<Int>)

@Suppress("ArrayPrimitive")
@DeepPrint
data class WithAnArray(val name: String, val items: Array<Int>)

@DeepPrint
data class WithDeepPrintableList(
    val name: String,
    val surfers: List<Surfer>
)

@DeepPrint
data class WithDeepPrintableMutableList(
    val name: String,
    val surfers: MutableList<Surfer>
)

@DeepPrint
data class WithDeepPrintableArray(
    val name: String,
    val surfers: Array<Surfer>
)

@DeepPrint
data class WithASet(val name: String, val items: Set<Int>)

@DeepPrint
data class WithAMutableSet(val name: String, val items: MutableSet<Int>)

@DeepPrint
data class WithDeepPrintableSet(
    val name: String,
    val surfers: Set<Surfer>
)

@DeepPrint
data class WithDeepPrintableMutableSet(
    val name: String,
    val surfers: MutableSet<Surfer>
)

@DeepPrint
data class WithPrimitiveArrays(
    val bytes: ByteArray,
    val shorts: ShortArray,
    val ints: IntArray,
    val longs: LongArray,
    val floats: FloatArray,
    val doubles: DoubleArray,
    val booleans: BooleanArray,
    val chars: CharArray,
)

typealias PersonAlias = SamplePersonClass

@DeepPrint
data class WithJdkCollections(
    val arrayList: ArrayList<Int>,
    val linkedSet: LinkedHashSet<Int>,
    val linkedMap: LinkedHashMap<Int, String>,
    val hashSet: HashSet<Int>,
    val hashMap: HashMap<Int, String>,
)

@DeepPrint
data class WithTypeAliasProperty(val person: PersonAlias)

@DeepPrint
data class WithNullables(
    val name: String?,
    val count: Int?,
    val items: List<Int>?,
    val someMap: Map<Int, String>?,
    val ints: IntArray?,
    val person: SamplePersonClass?,
)

@DeepPrint
data class WithCollectionMapValues(
    val listsByName: Map<String, List<Int>>,
    val setsByName: Map<String, Set<String>>,
)

enum class Direction { NORTH, SOUTH }

@DeepPrint
data class WithEnums(
    val direction: Direction,
    val maybeDirection: Direction?,
    val directions: List<Direction>,
    val bySide: Map<Direction, String>,
    val toDirections: Map<String, List<Direction>>,
)

typealias IntList = List<Int>

@DeepPrint
data class WithTuples(
    val pair: Pair<String, Int>,
    val triple: Triple<Int, Boolean, Char>,
    val pairOfClasses: Pair<Surfer, Direction>,
)

@DeepPrint
data class WithReadOnlyCollections(
    val collection: Collection<Int>,
    val iterable: Iterable<String>,
    val aliased: IntList,
)

@DeepPrint
data class WithUnsigned(
    val byte: UByte,
    val short: UShort,
    val int: UInt,
    val long: ULong,
    val ints: UIntArray,
    val longs: ULongArray,
)

@DeepPrint
data class WithTypedCollectionItems(
    val floats: List<Float>,
    val doubles: List<Double>,
    val chars: List<Char>,
    val uints: List<UInt>,
)

@DeepPrint
data class WithNestedCollections(
    val listOfLists: List<List<Int>>,
    val setOfLists: Set<List<Int>>,
    val listOfArrays: List<IntArray>,
    val deep: List<List<List<Int>>>,
)

@DeepPrint
data class WithNestedMaps(
    val mapOfMaps: Map<String, Map<String, Int>>,
    val listKeyed: Map<List<Int>, String>,
    val listOfMaps: List<Map<String, Int>>,
)

typealias Mapping<V> = Map<String, V>
typealias Grid<T> = List<List<T>>

@DeepPrint
data class WithGenericAliases(
    val mapping: Mapping<Int>,
    val grid: Grid<Int>,
)

@DeepPrint
data class WithASequence(val name: String, val sequence: Sequence<Int>)

@DeepPrint
data class WithAMap(
    val id: Long,
    val someMap: Map<Int, String>
)

@DeepPrint
data class WithAMutableMap(
    val id: Long,
    val someMutableMap: MutableMap<Int, String>
)

@DeepPrint
data class WithMapDataClasses(
    val id: Long,
    val someMap: Map<Int, Surfer>
)

@DeepPrint
data class WithMutableMapDataClasses(
    val id: Long,
    val someMutableMap: MutableMap<Int, Surfer>
)

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

data class SomeExternalClass(val name: String, val age: Int)

data class MyClass(val externalClass: SomeExternalClass)
