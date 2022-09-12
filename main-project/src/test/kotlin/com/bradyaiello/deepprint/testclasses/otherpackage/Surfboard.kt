package com.bradyaiello.deepprint.testclasses.otherpackage

import com.bradyaiello.deepprint.DeepPrint

@DeepPrint
data class Surfboard(val length: Float, val width: Float, val style: String)

data class Temperature(val fahrenheit: Float)