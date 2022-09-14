plugins {
    kotlin("jvm")
}

group = "com.module.external"
version = "unspecified"

dependencies {

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}