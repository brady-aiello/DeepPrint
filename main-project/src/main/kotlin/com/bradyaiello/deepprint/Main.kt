package com.bradyaiello.deepprint


@DeepPrint
data class Response(val code: Int, val headers:List<String>, val body: String)

fun main() {
    val response = Response(
        200,
        headers = listOf("header 1", "header 2", "header 3"),
        body = "{ \"success\": true }"
    )
    println(response.deepPrint())

    /*
    Prints:
        Response(
            code = 200,
            headers = listOf<String>( "header 1", "header 2", "header 3",)
            body = "{ "success": true }",
        )

    */
}