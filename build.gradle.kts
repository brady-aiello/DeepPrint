buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    id("com.google.devtools.ksp") apply false
    id("io.gitlab.arturbosch.detekt") apply false
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
}

allprojects {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        google()
    }
}

/*
    The Kotlin/JS build toolchain -- webpack, karma, mocha -- pins its own npm
    dependencies, and several are old enough to carry advisories. None of it ships:
    the published JS artifact declares only kotlin-stdlib-js and kotlin-dom-api-compat,
    and no module here declares an npm() dependency. The exposure is this repository's
    CI, not anyone who depends on DeepPrint.

    Left alone they still cost something. Fifteen high-severity alerts on a repository
    where none are reachable is how a real one goes unnoticed, so the ones that can be
    moved are moved.

    kotlinUpgradeYarnLock does not help here -- the lockfile already matches what the
    Kotlin plugin resolves, so these versions are pinned rather than merely stale.
    Forcing them is the only lever, and each of these is verified by running the JS
    tests rather than by reading the advisory.
*/
plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    with(the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>()) {
        // Present at one major, so these stay inside the range the toolchain asked for.
        resolution("body-parser", "1.20.3")
        resolution("braces", "3.0.3")
        resolution("cross-spawn", "7.0.6")
        resolution("flatted", "3.4.2")
        resolution("lodash", "4.18.0")
        resolution("picomatch", "2.3.2")
        resolution("tmp", "0.2.6")

        // Present at two majors, so one forced version crosses a boundary for whichever
        // consumer asked for the older line. brace-expansion survives it; its API did
        // not change in a way karma or webpack notice.
        resolution("brace-expansion", "2.0.2")

        // minimatch is deliberately not forced. It is present at ^3 and ^9, every fix
        // is on 3.1.3+ or the 9 line, and forcing 9 breaks karma:
        //     TypeError: mm is not a function
        //         at karma/lib/file-list.js:40:45
        // minimatch 3 exports a function and 9 exports an object, and karma calls it.
        // Two high advisories against a build-time file matcher are the better trade
        // than no browser tests.

        // All of 6.x is affected; the first fix is 7.0.3.
        resolution("serialize-javascript", "7.0.3")
    }
}

