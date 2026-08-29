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
        // The highest patched version across every advisory open against the package,
        // not the first. Targeting the first fix is how brace-expansion landed on 2.0.2
        // inside `>= 2.0.0, < 2.1.4`, and how tmp landed on exactly the 0.2.6 that
        // `>= 0.2.6, < 0.2.7` names.
        resolution("ajv", "8.18.0")
        resolution("body-parser", "1.20.6")
        resolution("brace-expansion", "2.1.4")
        resolution("braces", "3.0.3")
        resolution("cross-spawn", "7.0.6")
        resolution("diff", "8.0.3")
        resolution("flatted", "3.4.2")
        resolution("follow-redirects", "1.16.0")
        resolution("lodash", "4.18.0")
        resolution("picomatch", "2.3.2")
        resolution("qs", "6.15.2")
        resolution("serialize-javascript", "7.0.5")
        resolution("tmp", "0.2.7")
        resolution("webpack", "5.104.1")

        // minimatch is present at two majors and only the 9 line is still affected, now
        // that the 3 line resolves to 3.1.5 on its own. It cannot be forced globally:
        // karma declares ^3.0.4 and calls minimatch as a function, which minimatch 9,
        // exporting an object, is not:
        //     TypeError: mm is not a function
        //         at karma/lib/file-list.js:40:45
        // Scoping karma back to 3.1.5 does not work either -- the global resolution
        // hoists 9 to the top and karma resolves to it anyway. So only the two packages
        // that actually asked for 9 are moved, and karma and glob keep the 3 line they
        // were written against.
        resolution("karma-webpack/minimatch", "9.0.7")
        resolution("mocha/minimatch", "9.0.7")
    }
}

