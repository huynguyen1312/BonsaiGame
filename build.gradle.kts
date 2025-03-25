
import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.25"
    application
    id("org.jetbrains.dokka") version "1.9.20"
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

group = "edu.udo.cs.sopra"
version = "1.0"

kotlin {
    jvmToolchain(11)
}

repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") } // <--- snapshot repository
}
repositories {
    mavenCentral()
    maven {
        url = uri("https://sopra-gitlab.cs.tu-dortmund.de/api/v4/projects/2128/packages/maven")
        credentials(HttpHeaderCredentials::class) {
            name = "Private-Token"
            value = "glpat-t95XRsqvU899Px-tGG8G"
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
}


application {
    mainClass.set("MainKt")
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    implementation(group = "tools.aqua", name = "bgw-gui", version = "0.9-21-130801d-SNAPSHOT")
    implementation(group = "tools.aqua", name = "bgw-net-common", version = "0.9-17-b7de59c-SNAPSHOT")
    implementation(group = "tools.aqua", name = "bgw-net-client", version = "0.9-17-b7de59c-SNAPSHOT")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json-jvm", version = "1.6.3")
    implementation(group = "edu.udo.cs.sopra", name = "ntf", version = "1.0")
}

tasks.distZip {
    archiveFileName.set("distribution.zip")
    into("") {
        from("docs")
        include("HowToPlay.pdf")
    }
    destinationDirectory.set(layout.projectDirectory.dir("public"))
}

tasks.test {
    useJUnitPlatform()
    reports.html.outputLocation.set(layout.projectDirectory.dir("public/test"))
    finalizedBy(tasks.koverReport) // report is always generated after tests run
    ignoreFailures = true
}

tasks.clean {
    delete.add("public")
}

kover {
    filters {
        classes {
            excludes += listOf("gui.*", "entity.*", "*MainKt*", "service.bot.*", "service.network.*")
        }
    }
    xmlReport {
        reportFile.set(file("public/coverage/report.xml"))
    }
    htmlReport {
        reportDir.set(layout.projectDirectory.dir("public/coverage"))
    }
}

detekt {
    toolVersion = "1.23.7"
    config.from("detektConfig.yml")
}

tasks.detektMain {
    reports.html.outputLocation.set(file("public/detekt/main.html"))
}

tasks.detektTest {
    reports.html.outputLocation.set(file("public/detekt/test.html"))
}

tasks.dokkaHtml.configure {
    outputDirectory.set(projectDir.resolve("public/dokka"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.register("verify") {
    dependsOn(tasks.test)
    doLast {
        val testResultsDir = layout.buildDirectory.dir("test-results/test")
        val xmlFiles = testResultsDir.get().asFile.listFiles { file -> file.extension == "xml" }
        val parser = XmlParser()
        xmlFiles
            ?.filter { xmlFile -> !xmlFile.startsWith("TEST-Gradle") }
            ?.forEach { xmlFile ->
                println("Processing ${xmlFile.name}")
                val parsedXml = parser.parse(xmlFile)
                val suit = parsedXml.parseTestSuite()
                val failedCases = suit?.cases?.filter { it.failure != null }
                val failed =
                    failedCases?.any { it.failure?.type != "org.opentest4j.AssertionFailedError" } ?: true
                println(failedCases)
                if (failed) {
                    throw GradleException()
                }
            }
    }
}

gradle.buildFinished {
    try {
        val applicationPIDs = file("build/application.pid").readText().split(",").map { it.toLong() }.toSet()
        println("Created JCEF_Helper PIDs: $applicationPIDs")
        killJcefHelperProcesses(applicationPIDs)
    } catch (e: Exception) {}
}

// Function to kill JCEF helper processes
fun killJcefHelperProcesses(pids: Set<Long>) {
    pids.forEach { pid ->
        ProcessHandle.of(pid).ifPresent {
            println("Killing process $pid")
            it.destroy()
        }
    }
}

fun Node.parseTestSuite(): TestSuite? {
    val name = this.name().toString()
    if (name == "testsuite") {
        val values = this.value() as? NodeList ?: return null
        val testCases = values.filterIsInstance<Node>().mapNotNull { it.parseTestCase() }
        return TestSuite(cases = testCases)
    }
    return null
}

fun Node.parseTestCase(): TestCase? {
    val name = this.name().toString()
    if (name == "testcase") {
        val values = this.value() as? NodeList ?: return null
        val failure = values.filterIsInstance<Node>().mapNotNull { it.parseFailure() }.firstOrNull()
        return TestCase(failure = failure)
    }
    return null
}

fun Node.parseFailure(): Failure? {
    val name = this.name().toString()
    if (name == "failure") {
        val type = this.attributes()["type"]?.toString() ?: return null
        return Failure(type = type)
    }
    return null
}

data class TestSuite(
    val cases: List<TestCase>,
)

data class TestCase(
    val failure: Failure?
)

data class Failure(
    val type: String,
)
