import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.jetbrains.kotlin.jvm") 
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.strikt:strikt-core:0.34.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.0.1")
    verbose.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    additionalEditorconfig.set(
        mapOf(
            "max_line_length" to "120",
        ),
    )
    reporters {
        reporter(ReporterType.PLAIN)
    }
}
