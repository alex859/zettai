plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:11.6.1")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.1")
    implementation("com.bnorm.power:kotlin-power-assert-gradle:0.13.0")
}
