package com.bradyaiello.deepprint.tostring

import com.bradyaiello.deepprint.DeepPrint

// Annotated because this spike branches from main, which does not yet have the
// no-annotation mode. The IR rewrite being spiked is independent of how deepPrint()
// comes to exist.
@DeepPrint
data class Point(val x: Int, val y: Int)

@DeepPrint
data class Line(val start: Point, val label: String)
