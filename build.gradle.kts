import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "me.liuli.rosetta"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.9.0")

    testImplementation("com.github.liulihaocai:MCProtocolLib:e0af4caff6")
    testImplementation("com.github.GeyserMC:OpenNBT:1.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    named<ShadowJar>("shadowJar") {
        from(sourceSets.test.get().output, sourceSets.main.get().output)
        archiveBaseName.set("shadow-test")
        configurations.add(project.configurations.testImplementation.get().also {
            it.isCanBeResolved = true
        })
        configurations.add(project.configurations.implementation.get().also {
            it.isCanBeResolved = true
        })
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = "test.rosetta.Main"
        }
    }
}