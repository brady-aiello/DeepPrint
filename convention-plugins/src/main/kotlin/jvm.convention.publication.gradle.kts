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
    publications.create<MavenPublication>("maven") {
        from(components["java"])
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