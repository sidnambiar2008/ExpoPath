import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
}

kotlin {
    // Add this line to create shared iOS source set automatically
    applyDefaultHierarchyTemplate()
    
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iOS targets - add back when ready
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm() {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        // Use "val ... by getting" for ALL of them to stay safe
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(compose.materialIconsExtended)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                
                // GitLive Firebase (Cross-platform versions)
                implementation("dev.gitlive:firebase-app:2.1.0")
                implementation("dev.gitlive:firebase-auth:2.1.0")
                implementation("dev.gitlive:firebase-firestore:2.1.0")
                implementation("dev.gitlive:firebase-analytics:2.1.0")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.ktor.client.android)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.ios)
            }
        }

        val jvmMain by getting {
            // Can leave empty, but keep for structure
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                // Add these npm dependencies to provide the polyfills
                implementation(npm("browserify-zlib", "0.2.0"))
                implementation(npm("stream-browserify", "3.0.0"))
                implementation(npm("buffer", "6.0.3"))
                implementation(npm("process", "0.11.10"))
                implementation(npm("util", "0.12.5"))
            }
        }
    }
}

android {
    namespace = "org.communityday.navigation.events"  // Your actual package
    compileSdk = 35 

    defaultConfig {
        applicationId = "org.communityday.navigation.events"
        minSdk = 24
        targetSdk = 35
    }
}
