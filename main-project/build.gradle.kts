repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    //application
}

// Allows running from command line using  ./gradlew :main-project:run
//application {
//    mainClass.set("com.bradyaiello.deepprint.MainKt")
//}

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/ksp/main/kotlin"),
    )
}

kotlin.sourceSets.test {
    kotlin.srcDirs(
        file("$buildDir/generated/ksp/test/kotlin"),
    )
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":annotations"))
    implementation(project(":external-module"))
    implementation(project(":processor"))
    ksp(project(":processor"))
//    implementation(":annotations")
//    implementation(":external-module")
//    ksp(":processor")
    testImplementation(Testing.Junit.jupiter)
    testImplementation(KotlinX.datetime)
}