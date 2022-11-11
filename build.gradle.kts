import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.serialization") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "one.devos"
version = System.getenv("GITHUB_SHA") ?: "Unknown"

repositories {
    mavenCentral()
    maven("https://gitlab.com/api/v4/projects/26794598/packages/maven")
    maven("https://jitpack.io/")
    maven("https://m2.dv8tion.net/releases")
}

val logbackVersion: String by project
val coroutinesVersion: String by project
val ktomlVersion: String by project
val exposedVersion: String by project
val ktorVersion: String by project

dependencies {// Logger libraries for writing to the console
    api("org.slf4j:slf4j-api:2.0.3")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    // Kotlin
    implementation(kotlin("stdlib-common"))
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$coroutinesVersion")
    // JSON Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    // TOML Serialization - YW Devin
    implementation("com.akuleshov7:ktoml-core:$ktomlVersion")
    implementation("com.akuleshov7:ktoml-file:$ktomlVersion")
    // Exposed Database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.postgresql:postgresql:42.5.0")
    implementation("com.h2database:h2:2.1.214")
    // Ktor
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    // Aviation - Storm's command library made by Artrinix, will probably break
    implementation("xyz.artrinix:aviation:12174e51")
    // Audio dependencies
    //implementation("dev.schlaubi.lavakord:jda:3.7.0")
    implementation("com.sedmelluq:lavaplayer:1.3.77")
    // JDA
    implementation("com.github.DV8FromtheWorld:JDA:v5.0.0-alpha.22") {
        exclude(module = "opus-java")
    }
    // Kotlin Extensions for JDA
    implementation("com.github.minndevelopment:jda-ktx:0.9.6-alpha.22")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
    }
}

tasks.withType<ShadowJar> {
    archiveFileName.set("Yiski6-Fat.jar")
    manifest {
        attributes(
            mapOf(
                "Main-class" to "one.devos.yiski.Yiski",
                "Implementation-Version" to project.version
            )
        )
    }
}