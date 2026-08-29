package com.example.consumer

import com.bradyaiello.deepprint.DeepPrint

@DeepPrint
data class Packaged(val name: String, val values: List<Int>)
