plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-core"))
    implementation(project(":pleo-antaeus-models"))

    implementation("io.javalin:javalin:5.3.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
}
