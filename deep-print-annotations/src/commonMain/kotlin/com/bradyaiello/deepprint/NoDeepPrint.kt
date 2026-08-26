package com.bradyaiello.deepprint

/**
 * Excludes a `data class` from processing when DeepPrint is generating for every data
 * class rather than only annotated ones.
 *
 * Only needed for the exceptions: a class whose printed form would be unhelpfully large,
 * or one holding something that should not end up in a log.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NoDeepPrint()
