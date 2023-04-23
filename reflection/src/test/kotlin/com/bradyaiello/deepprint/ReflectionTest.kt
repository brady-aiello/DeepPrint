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
    
    @Test
    fun `primitives`() {
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
}