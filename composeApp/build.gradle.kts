import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }
    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "app.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply { port = 8083 }
            }
        }
        binaries.executable()
    }

    /**
     * The iPhone app: a static framework that `iosApp/` links, as in Unitool and RestoApp. Only the
     * klibs build without Xcode (`./gradlew :composeApp:compileKotlinIosArm64`); linking takes a Mac.
     */
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)

            implementation(libs.superizer.core)
            implementation(libs.superizer.ui.theme)
            implementation(libs.superizer.ui)
            // The host module — here and nowhere else (checkDependencyRules).
            implementation(libs.superizer.host)

            // The portal's kit: the city bonus account and its screen are the host's to provide.
            implementation(project(":kit"))
            implementation(project(":apps:food"))
            implementation(project(":apps:flowers"))
            implementation(project(":apps:ads"))
            implementation(project(":apps:visarun"))
            implementation(project(":apps:exchange"))
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        val desktopMain by getting
        desktopMain.dependencies { implementation(compose.desktop.currentOs) }

        val desktopTest by getting
        desktopTest.dependencies {
            implementation(libs.superizer.testing)
            implementation(libs.compose.ui.test)
            implementation(compose.desktop.currentOs)
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "vn.touch.host"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        applicationId = "vn.touch.portal"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { buildConfig = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

compose.desktop {
    application {
        mainClass = "vn.touch.host.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "TouchVN"
            packageVersion = "1.0.0"
        }
    }
}
