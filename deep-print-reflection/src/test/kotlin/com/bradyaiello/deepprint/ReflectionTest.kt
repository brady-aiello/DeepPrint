package com.bradyaiello.deepprint

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReflectionTest {
    
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
    
    @Test
    fun primitives() {
        val primitivesContainer = PrimitivesContainer()
        val actual = primitivesContainer.deepPrintReflection()
        println(actual)
    }
    @Test
    fun `data class in a data class`() {
        val brady = Person(
            name = "Brady",
            age = 38,
            Address(
                streetAddress = "19 Jolley Way",
                city = "Scotts Valley",
                state = "CA",
                zipCode =  "95066"
            )
        )
        val expected = """
        Person(
            name = "Brady",
            age = 38,
            Address(
                streetAddress = "19 Jolley Way",
                city = "Scotts Valley",
                state = "CA",
                zipCode = "95066",
            ),
        )
        """.trimIndent()
        
        val actual = brady.deepPrintReflection()
        assertEquals(expected, actual)
        
    }
    
    @Test
    fun `deep print list Integers`() {
        val myList = listOf(1, 2, 3, 4, 5)
        val expected = """
            listOf(
                1,
                2,
                3,
                4,
                5,
            )
        """.trimIndent()
        val actual = myList.deepPrintListReflect()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print data class with list Integers`() {
        
        val myMutableList = mutableListOf(1, 2, 3, 4, 5)
        val someMutableListContainer = createMutableListContainer(myMutableList)
        println(someMutableListContainer.deepPrintReflection())
        val expected = """
            MutableListContainer(
                someString = "Some String",
                numbers =  mutableListOf(
                    1,
                    2,
                    3,
                    4,
                    5,
                ),
            )
        """.trimIndent()
        val actual = someMutableListContainer.deepPrintReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print list Float`() {
        val myList = listOf(1f, 2f, 3f, 4f, 5f)
        val expected = """
            listOf(
                1.0f,
                2.0f,
                3.0f,
                4.0f,
                5.0f,
            )
        """.trimIndent()
        val actual = myList.deepPrintListReflect()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print list Double`() {
        val myList = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val expected = """
            listOf(
                1.0,
                2.0,
                3.0,
                4.0,
                5.0,
            )
        """.trimIndent()
        val actual = myList.deepPrintListReflect()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print mutable list Strings`() {
        val myList = listOf("Hi", "Hey", "How's it going?", "What's up?", "Hello")
        val expected = """
            listOf(
                "Hi",
                "Hey",
                "How's it going?",
                "What's up?",
                "Hello",
            )
        """.trimIndent()
        val actual = myList.deepPrintListReflect()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print list String`() {
        val myList = listOf("Hi", "Hey", "How's it going?", "What's up?", "Hello")
        val expected = """
            listOf(
                "Hi",
                "Hey",
                "How's it going?",
                "What's up?",
                "Hello",
            )
        """.trimIndent()
        val actual = myList.deepPrintListReflect()
        assertEquals(expected, actual)
    }
    
    @Test
    fun `deep print data class with mutable list of Data Classes`() {

        val myMutableList = mutableListOf(1, 2, 3, 4, 5)
        val someMutableListContainer = createMutableListContainer(myMutableList)

        val mutableListOfDataClasses = WithMutableListOfDataClasses(
            id = "12345",
            people = createMutableListOfPeople(),
            mutableListContainer = someMutableListContainer,
        )
        println(mutableListOfDataClasses.deepPrintReflection())
        val expected = """
            WithMutableListOfDataClasses(
                id = "12345",
                people =  mutableListOf(
                    Person(
                        name = "Brady",
                        age = 38,
                        Address(
                            streetAddress = "19 Jolley Way",
                            city = "Scotts Valley",
                            state = "CA",
                            zipCode = "95066",
                        ),
                    ),
                    Person(
                        name = "Joe",
                        age = 80,
                        Address(
                            streetAddress = "1600 Pennsylvania Avenue, N.W.",
                            city = "Washington",
                            state = "DC",
                            zipCode = "20500",
                        ),
                    ),
                ),
                MutableListContainer(
                    someString = "Some String",
                    numbers =  mutableListOf(
                        1,
                        2,
                        3,
                        4,
                        5,
                    ),
                ),
            )
        """.trimIndent()
        val actual = mutableListOfDataClasses.deepPrintReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print data class with list of Data Classes`() {

        val myList = listOf(1, 2, 3, 4, 5)
        val someListContainer = ListContainer(
            someString = "Some String",
            numbers = myList
        )

        val listOfDataClasses = createWithListOfDataClasses(someListContainer)
        val expected = """
            WithListOfDataClasses(
                id = "12345",
                people =  mutableListOf(
                    Person(
                        name = "Brady",
                        age = 38,
                        Address(
                            streetAddress = "19 Jolley Way",
                            city = "Scotts Valley",
                            state = "CA",
                            zipCode = "95066",
                        ),
                    ),
                    Person(
                        name = "Joe",
                        age = 80,
                        Address(
                            streetAddress = "1600 Pennsylvania Avenue, N.W.",
                            city = "Washington",
                            state = "DC",
                            zipCode = "20500",
                        ),
                    ),
                ),
                ListContainer(
                    someString = "Some String",
                    numbers =  mutableListOf(
                        1,
                        2,
                        3,
                        4,
                        5,
                    ),
                ),
            )
        """.trimIndent()
        val actual = listOfDataClasses.deepPrintReflection()
        assertEquals(expected, actual)
    }

    private fun createMutableListOfPeople(): MutableList<Person> {
        val brady = Person(
            name = "Brady",
            age = 38,
            Address(
                streetAddress = "19 Jolley Way",
                city = "Scotts Valley",
                state = "CA",
                zipCode = "95066"
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
    
    private fun createWithListOfDataClasses(
        someListContainer: ListContainer
    ): WithListOfDataClasses {
        return WithListOfDataClasses(
            id = "12345",
            people = createMutableListOfPeople(),
            listContainer = someListContainer,
        )
    }

    private fun createMutableListContainer(myMutableList: MutableList<Int>): MutableListContainer {
        return MutableListContainer(
            someString = "Some String",
            numbers = myMutableList
        )
    }
}

