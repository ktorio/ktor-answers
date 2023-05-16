plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.io.ktor.plugin)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

group = "io.ktor.answers"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.serialization.kotlinx.json.jvm)
    implementation(libs.ktor.server.host.common.jvm)
    implementation(libs.ktor.server.status.pages.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.logback.classic)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.liquibase)
    runtimeOnly(libs.postgres)
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.tests.jvm)
    testImplementation(libs.testcontainers.postgres)
    testImplementation(libs.testcontainers.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.jsonpath)
}

tasks.test {
    useJUnitPlatform()
}