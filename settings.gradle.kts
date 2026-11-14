rootProject.name = "touch-vn"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

/**
 * Superizer, as a package: always by Maven coordinates (`libs.superizer.*`), never by project path.
 * By default it resolves from mavenLocal (`./gradlew publishToMavenLocal` in ../superizer).
 * SUPERIZER_LOCAL=1 substitutes the checkout next door instead — the same text, both ways.
 */
val superizerDir = file("../superizer")
if (superizerDir.exists() && System.getenv("SUPERIZER_LOCAL") == "1") {
    includeBuild(superizerDir)
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// The host: a name, a list of register() calls, the platform entrypoints.
include(":composeApp")

// The portal's shared widgets and helpers. Apps may use it; it knows no app and no host.
include(":kit")

// The services of the portal, one app each.
include(":apps:food")
include(":apps:flowers")
include(":apps:ads")
include(":apps:visarun")
include(":apps:exchange")
