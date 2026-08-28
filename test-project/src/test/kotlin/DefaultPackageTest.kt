import com.bradyaiello.deepprint.DeepPrint
import kotlin.test.Test
import kotlin.test.assertEquals

// Deliberately in the default package. Kotlin cannot import from the root package, so
// the class and its test have to live here together.

@DeepPrint
data class RootPackagePoint(val x: Int, val name: String)

@DeepPrint
data class RootPackageHolder(val point: RootPackagePoint)

class DefaultPackageTest {

    @Test
    fun aClassInTheDefaultPackageGeneratesCompilableCode() {
        // A bare `package ` line is a syntax error, so this failing looks like a broken
        // build rather than a wrong string.
        val expected = """
            RootPackagePoint(
                x = 1,
                name = "root",
            )
        """.trimIndent()

        assertEquals(expected, RootPackagePoint(1, "root").deepPrint())
    }

    @Test
    fun nestedDefaultPackageClassesNeedNoImport() {
        // The generated file would otherwise try `import .deepPrint`.
        val expected = """
            RootPackageHolder(
                point = 
                    RootPackagePoint(
                        x = 2,
                        name = "nested",
                    ),
            )
        """.trimIndent()

        assertEquals(expected, RootPackageHolder(RootPackagePoint(2, "nested")).deepPrint())
    }
}
