
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

const val junitVersion = "5.9.2"

/**
 * Configures the current project as a Kotlin project by adding the Kotlin `stdlib` as a dependency.
 */
fun Project.kotlinProject() {
    dependencies {
        // Kotlin libs
        "implementation"(kotlin("stdlib"))

        // Logging
        "implementation"("org.slf4j:slf4j-simple:1.7.30")
        "implementation"("ch.qos.logback:logback-classic:1.4.5")
        "implementation"("io.github.microutils:kotlin-logging:3.0.5")

        // Mockk
        "testImplementation"("io.mockk:mockk:1.13.4")

        // JUnit 5
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        "runtimeOnly"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    }
}

/**
 * Configures data layer libs needed for interacting with the DB
 */
fun Project.dataLibs() {
    dependencies {
        "implementation"("org.jetbrains.exposed:exposed:0.17.14")
        "implementation"("org.xerial:sqlite-jdbc:3.40.1.0")
    }
}