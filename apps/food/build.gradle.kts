import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

group = "vn.touch.apps"
version = "1.0.0"

kotlin {
    explicitApi()

    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }
    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    // iOS: klibs for the framework :composeApp links (iosApp/).
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.superizer.core)
            implementation(project(":kit"))
            implementation(libs.superizer.ui.theme)
            implementation(libs.superizer.ui)
            implementation(libs.compose.unstyled)
            implementation(libs.kotlinx.serialization.json)
        }
        val desktopTest by getting
        desktopTest.dependencies {
            implementation(libs.superizer.testing)
            implementation(compose.desktop.currentOs)
            implementation(libs.compose.ui.test)
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "vn.touch.apps.food"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.androidMinSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
