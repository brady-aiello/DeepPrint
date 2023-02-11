package com.bradyaiello.deepprint

import kotlin.math.floor

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DeepPrint()

fun <T> Array<T>.deepPrintContents(): String = this.toList().deepPrintContents()
