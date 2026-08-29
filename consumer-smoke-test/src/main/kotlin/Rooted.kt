import com.bradyaiello.deepprint.DeepPrint

/*
 * Deliberately in the default package. A data class with no package generated a bare
 * `package` line, which is a syntax error, and that shipped in 0.2.0 and 0.3.0. Every
 * test inside the library has a package, so nothing there could catch it.
 */
@DeepPrint
data class RootPoint(val x: Int, val y: Int)
