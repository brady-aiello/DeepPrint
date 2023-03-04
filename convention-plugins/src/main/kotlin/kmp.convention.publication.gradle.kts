import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing
import java.util.*

plugins {
    `maven-publish`
    signing
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile: File = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_KEY_PASSWORD")
    ext["signing.secretKey"] = System.getenv("SIGNING_SECRET_KEY")
    ext["mavenCentralUsername"] = System.getenv("MAVEN_CENTRAL_USERNAME")
    ext["mavenCentralPassword"] = System.getenv("MAVEN_CENTRAL_PASSWORD")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = ext[name]?.toString()

publishing {
    // Configure maven central repository
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = getExtraString("mavenCentralUsername")
                password = getExtraString("mavenCentralPassword")
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(javadocJar.get())

        // Provide artifacts information requited by Maven Central
        pom {
            name.set("DeepPrint")
            description.set("Print Kotlin data class instances as constructor statements using KSP")
            url.set("https://github.com/brady-aiello/DeepPrint")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("brady-aiello")
                    name.set("Brady Aiello")
                    email.set("brady.aiello@gmail.com")
                }
            }
            scm {
                url.set("https://github.com/brady-aiello/DeepPrint")
            }
        }
    }
}

// Signing artifacts. Signing.* extra properties values will be used
getExtraString("signing.keyId")?.let { keyId ->
    signing {
        getExtraString("signing.secretKey")?.let { secretKey ->
            useInMemoryPgpKeys(keyId, secretKey, getExtraString("signing.password"))
        }
        sign(publishing.publications)
    }
}

val dependsOnTasks = mutableListOf<String>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOnTasks.add(this.name.replace("publish", "sign")
        .replaceAfter("Publication", ""))
    dependsOn(dependsOnTasks)
}
/* ^
Solution provided here:
https://youtrack.jetbrains.com/issue/KT-46466/Kotlin-MPP-publishing-Gradle-7-disables-optimizations-because-of-task-dependencies#focus=Comments-27-6476492.0-0

Without this, there are some weird publishing errors around implicit dependencies:

> Task :deep-print-annotations:signIosSimulatorArm64Publication FAILED

FAILURE: Build failed with an exception.

* What went wrong:
A problem was found with the configuration of task ':deep-print-annotations:signIosSimulatorArm64Publication' (type 'Sign').
  - Gradle detected a problem with the following location: '/Users/bradyaiello/IdeaProjects/DeepPrint/deep-print-annotations/build/libs/deep-print-annotations-0.1.0-alpha-javadoc.jar.asc'.

    Reason: Task ':deep-print-annotations:publishIosArm64PublicationToMavenLocal' uses this output of task ':deep-print-annotations:signIosSimulatorArm64Publication' without declaring an explicit or implicit dependency. This can lead to incorrect results being produced, depending on what order the tasks are executed.

    Possible solutions:
      1. Declare task ':deep-print-annotations:signIosSimulatorArm64Publication' as an input of ':deep-print-annotations:publishIosArm64PublicationToMavenLocal'.
      2. Declare an explicit dependency on ':deep-print-annotations:signIosSimulatorArm64Publication' from ':deep-print-annotations:publishIosArm64PublicationToMavenLocal' using Task#dependsOn.
      3. Declare an explicit dependency on ':deep-print-annotations:signIosSimulatorArm64Publication' from ':deep-print-annotations:publishIosArm64PublicationToMavenLocal' using Task#mustRunAfter.

    Please refer to https://docs.gradle.org/8.0/userguide/validation_problems.html#implicit_dependency for more details about this problem.
 */