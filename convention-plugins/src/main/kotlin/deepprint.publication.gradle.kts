plugins {
    id("com.vanniktech.maven.publish")
}

/*
 * One convention plugin for every published module, JVM and multiplatform alike: the
 * Vanniktech plugin works out which publications a project has, so the two variants
 * this replaced no longer need to differ.
 *
 * It also targets the Central Portal. Publishing to Maven Central through
 * s01.oss.sonatype.org, which the previous setup did, stopped being possible when OSSRH
 * reached end of life on 30 June 2025.
 */
mavenPublishing {
    publishToMavenCentral()

    // Only sign when a key is available, so that publishToMavenLocal still works for
    // anyone without one. A release without a key fails at the Portal, which rejects
    // unsigned artifacts.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }

    pom {
        name.set("DeepPrint")
        description.set("Print Kotlin data class instances as constructor statements using KSP")
        inceptionYear.set("2023")
        url.set("https://github.com/brady-aiello/DeepPrint")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("brady-aiello")
                name.set("Brady Aiello")
                email.set("brady.aiello@gmail.com")
                url.set("https://github.com/brady-aiello")
            }
        }
        scm {
            url.set("https://github.com/brady-aiello/DeepPrint")
            connection.set("scm:git:git://github.com/brady-aiello/DeepPrint.git")
            developerConnection.set("scm:git:ssh://git@github.com/brady-aiello/DeepPrint.git")
        }
    }
}
