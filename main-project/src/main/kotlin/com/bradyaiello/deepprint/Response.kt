package com.bradyaiello.deepprint

@DeepPrint
data class Response(val code: Int, val headers: List<String>, val body: String)
