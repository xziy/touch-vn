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
 * When the library's checkout is next door (../superizer, as CI puts it), `includeBuild` substitutes
 * its sources for those coordinates. When it is not — or with `SUPERIZER_FROM_MAVEN=1` on purpose —
 * the same coordinates resolve out of mavenLocal. The same text both ways, as in Unitool.
 */
val superizerDir = file("../superizer")
if (superizerDir.exists() && System.getenv("SUPERIZER_FROM_MAVEN") != "1") {
    val drawerPatch = file("gradle/patches/superizer-drawer-side.patch")
    fun applyDrawerPatch(vararg options: String): Int = providers.exec {
        workingDir(superizerDir)
        commandLine(listOf("git", "apply") + options + drawerPatch.absolutePath)
        isIgnoreExitValue = true
    }.result.get().exitValue

    if (applyDrawerPatch("--reverse", "--check") != 0 && applyDrawerPatch() != 0) {
        check(applyDrawerPatch("--reverse", "--check") == 0) {
            "Cannot apply the Touch VN drawer patch to $superizerDir. Use the Superizer revision pinned in ios.yml."
        }
    }
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
