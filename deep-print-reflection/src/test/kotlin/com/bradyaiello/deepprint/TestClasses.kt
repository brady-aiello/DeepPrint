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
