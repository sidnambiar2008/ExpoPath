import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget



plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    // Google Services plugin applied conditionally at bottom
}

kotlin {
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    // Add this line to create shared iOS source set automatically
    applyDefaultHierarchyTemplate()
    
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
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
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    js(IR) {
        outputModuleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }
    sourceSets {
        // Use "val ... by getting" for ALL of them to stay safe
        val commonMain by getting {
            dependencies {
                // Compose & Lifecycle (Clean and readable!)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.material)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                // Algolia InstantSearch for Compose
             //   implementation(libs.algolia.client)
              //  implementation(libs.algolia.instantsearch.compose) // Works for all platforms
                // Required for the serialization we talked about earlier
                implementation(libs.kotlinx.serialization.json)

                // Firebase (The Multiplatform way)
                implementation(libs.firebase.app)
                implementation(libs.firebase.auth)
                implementation(libs.firebase.firestore)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.activity.compose)
                implementation("androidx.activity:activity-compose:1.9.3") // Use 1.9.0 for stability
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val iosMain by getting {
            dependencies {


            }
        }

        val jvmMain by getting {
            dependencies{
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.ui)

                // Use a BOM inside a KMP SourceSet
                //implementation(project.dependencies.platform("com.algolia:algoliasearch-client-kotlin-bom:3.38.1"))
                // Now you can declare the library without a version,
                // and the BOM will force it to the correct JS-compatible variant.

               // implementation(libs.ktor.client.js)
               // implementation(libs.firebase.app)

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

    // ADD THIS BLOCK BELOW
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = "MainKt" // This must match the file where your fun main() is

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageVersion = "1.0.0"
        }
    }
}

//configurations.all {
  //  resolutionStrategy {
  //      eachDependency {
            // This is the GPS redirect. It stops Gradle from looking for
            // the non-existent 'instantsearch' and forces it to 'instantsearch-compose'
    //        if (requested.group == "com.algolia" &&
      //          (requested.name == "instantsearch" || requested.name == "instantsearch-utils")) {
        //        useTarget("com.algolia:instantsearch-compose:4.0.0")
          //  }
        //}
    //}

    // Keep ONLY these excludes. Do NOT exclude 'firebase-firestore-android'.
    //exclude(group = "com.google.firebase", module = "firebase-common-ktx")
    //exclude(group = "com.google.firebase", module = "firebase-auth-ktx")
    //exclude(group = "com.google.firebase", module = "firebase-firestore-ktx")
//}

// Apply Google Services plugin only for Android builds
if (project.plugins.hasPlugin("com.android.application")) {
    apply(plugin = "com.google.gms.google-services")
}
