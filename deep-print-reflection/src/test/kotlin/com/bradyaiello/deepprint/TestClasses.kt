package com.bradyaiello.deepprint

data class Address(
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipCode: String
)

data class Person(
    val name: String,
    val age: Int,
    val address: Address,
)

data class PrimitivesContainer(
    val boolean: Boolean = true,
    val short: Short = 5,
    val byte: Byte = 127,
    val char: Char = 'a',
    val int: Int = 42,
    val float: Float = 26.2f,
    val double: Double = 26.2,
    val string: String = "Hello World"
)

data class MutableListContainer(
    val someString: String,
    val numbers: MutableList<Int>
)

data class ListContainer(
    val someString: String,
    val numbers: List<Int>
)

data class WithMutableListOfDataClasses(
    val id: String,
    val people: MutableList<Person>,
    val mutableListContainer: MutableListContainer
)

data class WithListOfDataClasses(
    val id: String,
    val people: List<Person>,
    val listContainer: ListContainer
)

data class ArrayHolder(
    val someString: String,
    val numbers: Array<Int>,
    val primitiveContainers: Array<PrimitivesContainer>
)

data class SetContainer(
    val someString: String,
    val numbers: Set<Int>
)

data class WithSetOfDataClasses(
    val id: String,
    val people: Set<Person>
)

data class PrimitiveArraysContainer(
    val bytes: ByteArray,
    val shorts: ShortArray,
    val ints: IntArray,
    val longs: LongArray,
    val floats: FloatArray,
    val doubles: DoubleArray,
    val booleans: BooleanArray,
    val chars: CharArray,
)

internal fun createPeople(): MutableList<Person> {
    val brady = Person(
        name = "Brady",
        age = 38,
        Address(
            streetAddress = "414 Koshland Way",
            city = "Santa Cruz",
            state = "CA",
            zipCode = "95064"
        )
    )
    val prez = Person(
        name = "Joe",
        age = 80,
        Address(
            streetAddress = "1600 Pennsylvania Avenue, N.W.",
            city = "Washington",
            state = "DC",
            zipCode = "20500"
        )
    )
    return mutableListOf(brady, prez)
}

enum class DayOfWeek { MONDAY, TUESDAY }

/** Neither a primitive, a collection, nor a `data class`. */
class Opaque(private val label: String) {
    override fun toString(): String = "Opaque($label)"
}

data class NullableContainer(
    val name: String?,
    val count: Int?,
    val address: Address?,
    val numbers: List<Int>?,
)

data class EnumContainer(
    val day: DayOfWeek,
    val days: List<DayOfWeek>,
    val byDay: Map<DayOfWeek, String>,
)

data class OpaqueContainer(
    val id: Opaque,
    val name: String,
)

data class UnsignedContainer(
    val int: UInt,
    val long: ULong,
    val ints: UIntArray,
)

data class NestedContainer(
    val listOfLists: List<List<Int>>,
    val listOfArrays: List<IntArray>,
    val mapValueList: Map<String, List<Int>>,
    val mapOfMaps: Map<String, Map<String, Int>>,
    val setOfLists: Set<List<Int>>,
)

data class HoldsExternalReflect(
    val external: com.module.external.ExternalDataClass,
    val id: String,
)

data object LoneDataObject

object PlainObject

sealed class Marker {
    data object Present : Marker()
    data class Absent(val reason: String) : Marker()
}

data class HoldsObjects(
    val dataObject: LoneDataObject,
    val plain: PlainObject,
    val nested: Marker,
    val id: String,
)

@JvmInline
value class UserId(val raw: String)

@JvmInline
value class Meters(val amount: Double)

@JvmInline
value class Initial(val letter: Char)

data class HoldsValueClasses(
    val id: UserId,
    val distance: Meters,
    val initial: Initial,
    val label: String,
)
