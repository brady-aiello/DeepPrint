package com.bradyaiello.deepprint

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReflectionTest {
    
    @Test
    fun `deep print data class with all primitives and only primitives`() {
        val primitivesContainer = PrimitivesContainer()
        val expected = """
            PrimitivesContainer(
                boolean = true,
                short = 5,
                byte = 127,
                char = 'a',
                int = 42,
                float = 26.2f,
                double = 26.2,
                string = "Hello World",
            )
        """.trimIndent()
        val actual = primitivesContainer.deepPrintReflection()
        
        assertEquals(expected, actual)
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
    fun `deep print list of Integers`() {
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
        val actual = myList.deepPrintListReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print mutable list of Integers`() {
        val myList = mutableListOf(1, 2, 3, 4, 5)
        val expected = """
            mutableListOf(
                1,
                2,
                3,
                4,
                5,
            )
        """.trimIndent()
        val actual = myList.deepPrintMutableListReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print data class with a list of Integers`() {
        
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
        val actual = myList.deepPrintListReflection()
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
        val actual = myList.deepPrintListReflection()
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
        val actual = myList.deepPrintListReflection()
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
        val actual = myList.deepPrintListReflection()
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
                            streetAddress = "414 Koshland Way",
                            city = "Santa Cruz",
                            state = "CA",
                            zipCode = "95064",
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
                            streetAddress = "414 Koshland Way",
                            city = "Santa Cruz",
                            state = "CA",
                            zipCode = "95064",
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
    
    @Test
    fun `deep print Map standalone primitives`() {
        val map = mapOf(
            1 to "a", 
            2 to "b", 
            3 to "c"
        )
        val expected = """
            mapOf(
                1 to "a",
                2 to "b",
                3 to "c",
            )
        """.trimIndent()
        val actual = map.deepPrintMapReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print MutableMap standalone primitives`() {
        val mutableMap: MutableMap<Int, String?> = mutableMapOf(
            1 to "a",
            2 to "b",
            3 to "c"
        )
        val expected = """
            mutableMapOf(
                1 to "a",
                2 to "b",
                3 to "c",
            )
        """.trimIndent()
        val actual = mutableMap.deepPrintMutableMapReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print MutableMap standalone data class values`() {
        val map = listOf(1, 2).zip(createMutableListOfPeople()).toMap().toMutableMap()
        val expected = """
            mutableMapOf(
                1 to
                    Person(
                        name = "Brady",
                        age = 38,
                        Address(
                            streetAddress = "414 Koshland Way",
                            city = "Santa Cruz",
                            state = "CA",
                            zipCode = "95064",
                        ),
                    ),
                2 to
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
            )
        """.trimIndent()
        
        val actual = map.deepPrintMutableMapReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print Map standalone data class values`() {
        val dayDishMap = createMap()
        val expected = """
            mapOf(
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
            )
        """.trimIndent()
        val actual = dayDishMap.deepPrintMapReflection()
        assertEquals(expected, actual)
    }

    private fun createMap(): MutableMap<String, Dish> {
        val dishes = listOf(
            Dish(
                name = "Pizza",
                ingredients = listOf("dough", "tomato sauce", "cheese")
            ),
            Dish(
                name = "Mac n Cheese",
                ingredients = listOf("mac", "cheese")
            )
        )
        val days = listOf("Monday", "Tuesday")
        val dayDishMap = days.zip(dishes).toMap().toMutableMap()
        return dayDishMap
    }

    data class Dish(
        val name: String,
        val ingredients: List<String>,
    )
    
    data class MapContainer(
        val name: String,
        val mapToHold: Map<String, Dish>,
        val id: Int,
    )
    
    @Test
    fun `deep print data class with Map`() {
        val dayDishMap = createMap()
        val mapContainer = MapContainer(
            name = "my map",
            mapToHold = dayDishMap,
            id = 12345,
        )
        val expected = """
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
        """.trimIndent()
        val actual = mapContainer.deepPrintReflection()
        assertEquals(expected, actual)
    }
    
    private fun createMutableListOfPeople(): MutableList<Person> {
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
