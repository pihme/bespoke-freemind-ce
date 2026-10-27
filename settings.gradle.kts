/*
 * FreeMind CE - Modern Gradle Settings
 * Denomas - 2026
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "freemind-ce"

// Include freemind as the main module
include(":freemind")

project(":freemind").projectDir = file("freemind")
