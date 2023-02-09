package com.bradyaiello.deepprint

const val RESPONSE_CODE = 200

fun main() {

    val response = Response(
        code = RESPONSE_CODE,
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
