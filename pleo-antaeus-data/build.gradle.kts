plugins {
    kotlin("jvm")
}

kotlinProject()

dataLibs()

dependencies {
    api(project(":pleo-antaeus-models"))
    api(project(":pleo-antaeus-core"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}
