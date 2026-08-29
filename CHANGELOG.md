# Changelog

Notable changes per release. The entries worth your attention on an upgrade are the ones
under **Changed**: those alter output or fail a build that used to pass.

## Unreleased

Nothing yet.

## 0.6.0

### Added
- Value classes print as a call to their own constructor, in KSP and reflection:
  `id = UserId(raw = "abc")` rather than `UserId(raw=abc)`, which is not valid Kotlin
  once the wrapped string loses its quotes. ([#60](https://github.com/brady-aiello/DeepPrint/pull/60))
- Generic data classes. `@DeepPrint` on one used to emit `fun Box.deepPrint()` without
  the type parameter, which does not compile -- and because the extension was then
  unresolvable, it became a candidate for every other `deepPrint()` call in the package,
  turning one generic class into "overload resolution ambiguity" errors in unrelated
  files. ([#58](https://github.com/brady-aiello/DeepPrint/pull/58))
- Cyclic data prints `TODO("DeepPrint: cycle back to Node")` instead of recursing until
  the stack runs out. In both implementations, and through collections.
  ([#67](https://github.com/brady-aiello/DeepPrint/pull/67),
  [#68](https://github.com/brady-aiello/DeepPrint/pull/68))
- `overrideToString` now rewrites `toString()` for nested and generic data classes, which
  it had been silently skipping. ([#62](https://github.com/brady-aiello/DeepPrint/pull/62))

### Changed
- **Nested classes print qualified.** `Outer.Inner(` where it was `Inner(`, and
  `EnumHost.Level.HIGH` where it was `Level.HIGH`. The unqualified form does not resolve
  outside the enclosing class, so this is a fix, but it changes output for anyone nesting
  data classes -- including a sealed subclass reached through its parent type, which
  reflection now prints as `Marker.Absent(`.
  ([#61](https://github.com/brady-aiello/DeepPrint/pull/61))
- **`@DeepPrint` on something it cannot generate for is now a build error.** It used to
  produce an empty file and no diagnostic, so the first sign was `unresolved reference:
  deepPrint` at the call site. A build that was quietly getting nothing will now fail and
  say why. ([#66](https://github.com/brady-aiello/DeepPrint/pull/66))

### Fixed
- A `data object` property threw `NoSuchElementException` under reflection rather than
  printing its name. ([#59](https://github.com/brady-aiello/DeepPrint/pull/59))

### Internal
- The public ABI of `deep-print-annotations` and `deep-print-reflection` is checked on
  every PR. ([#57](https://github.com/brady-aiello/DeepPrint/pull/57))
- JUnit 6, current GitHub Actions, and every Dependabot advisory against the Kotlin/JS
  build toolchain cleared. ([#63](https://github.com/brady-aiello/DeepPrint/pull/63),
  [#64](https://github.com/brady-aiello/DeepPrint/pull/64),
  [#65](https://github.com/brady-aiello/DeepPrint/pull/65))

## 0.5.0

### Added
- Data classes from other modules. A property whose type comes from a dependency is deep
  printed rather than falling back to `toString()`, with the extension generated beside
  your own. ([#51](https://github.com/brady-aiello/DeepPrint/pull/51))

## 0.4.1

### Changed
- Code is generated with KotlinPoet instead of assembled as text. This fixed a bare
  `package ` line and an `import .deepPrint` for classes in the default package, which
  had shipped in 0.2.0 and 0.3.0, along with a public extension on an internal receiver.
  ([#49](https://github.com/brady-aiello/DeepPrint/pull/49))

### Internal
- A consumer smoke test that resolves the published artifacts from Maven Central the way
  a stranger would. ([#48](https://github.com/brady-aiello/DeepPrint/pull/48))

## 0.4.0

### Added
- `overrideToString`: the `com.bradyaiello.deepprint` Gradle plugin rewrites the
  compiler-generated `toString()` of a data class to delegate to `deepPrint()`, with
  `@NoDeepPrint` to opt a class out.
  ([#45](https://github.com/brady-aiello/DeepPrint/pull/45),
  [#46](https://github.com/brady-aiello/DeepPrint/pull/46))

## 0.3.0

### Added
- No-annotation mode: `processAllDataClasses` generates for every data class in the
  module, so nothing has to be annotated.
  ([#41](https://github.com/brady-aiello/DeepPrint/pull/41))

## 0.2.0

The first release in two years, and the one that moved the project onto current
toolchains and a working publish.

### Added
- `Set`, `MutableSet`, the primitive arrays, `Pair`, `Triple`, `Collection`, `Iterable`,
  the unsigned types, typealiases, nullable properties, and nested collections, across
  both implementations.
- Enum constants print qualified, eg. `day = DayOfWeek.MONDAY`.

### Changed
- `Sequence` is no longer supported. Printing one consumes it, and an infinite one
  exhausts the heap. ([#35](https://github.com/brady-aiello/DeepPrint/pull/35))
- Reflection stopped silently dropping properties, stopped throwing on null, and names
  nested properties correctly. ([#29](https://github.com/brady-aiello/DeepPrint/pull/29))

### Internal
- Gradle 9.7.1, Kotlin 2.4.10, KSP 2.
  ([#26](https://github.com/brady-aiello/DeepPrint/pull/26))
- Publishing moved to the Maven Central Portal, and runs on tags rather than on every
  push to main. ([#36](https://github.com/brady-aiello/DeepPrint/pull/36),
  [#37](https://github.com/brady-aiello/DeepPrint/pull/37))

## 0.1.0-alpha10 and earlier

See the [releases](https://github.com/brady-aiello/DeepPrint/releases).
