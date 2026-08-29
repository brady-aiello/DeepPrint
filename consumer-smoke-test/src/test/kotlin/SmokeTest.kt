import com.example.consumer.Packaged
import com.example.consumer.deepPrint
import kotlin.test.Test
import kotlin.test.assertEquals

class SmokeTest {

    @Test
    fun theGradlePluginOverridesToString() {
        // Exercises the whole published chain: plugin marker, compiler plugin artifact,
        // the CLI option, and the IR rewrite.
        val expected = """
            RootPoint(
                x = 1,
                y = 2,
            )
        """.trimIndent()

        assertEquals(expected, RootPoint(1, 2).toString())
    }

    @Test
    fun theProcessorGeneratesUsableCode() {
        val expected = """
            Packaged(
                name = "a",
                values = listOf<Int>( 1, 2,),
            )
        """.trimIndent()

        assertEquals(expected, Packaged("a", listOf(1, 2)).deepPrint())
    }
}
