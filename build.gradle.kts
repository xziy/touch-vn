plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
}

/**
 * The Superizer boundary, checked by the build (as in Unitool): an app may see the contract, the
 * widgets and :kit — never superizer:host, the host module or another app. :kit may see no app.
 */
val checkDependencyRules by tasks.registering {
    group = "verification"
    description = "Fails when an app reaches for the host or another app, or :kit for an app."

    val appFiles = subprojects.filter { it.path.startsWith(":apps:") }.associate { it.path to it.buildFile }
    val kitFile = project(":kit").buildFile
    appFiles.values.forEach { inputs.file(it) }
    inputs.file(kitFile)
    val stamp = layout.buildDirectory.file("dependency-rules.ok")
    outputs.file(stamp)

    doLast {
        val problems = mutableListOf<String>()
        val forbidden = listOf("libs.superizer.host", "superizer:host", "project(\":composeApp\")", "project(\":apps:")
        appFiles.forEach { (path, file) ->
            val text = file.readText()
            forbidden.filter { it in text }.forEach { problems += "$path names $it" }
        }
        val kitText = kitFile.readText()
        listOf("libs.superizer.host", "superizer:host", "project(\":composeApp\")", "project(\":apps:")
            .filter { it in kitText }.forEach { problems += ":kit names $it" }
        check(problems.isEmpty()) { problems.joinToString("\n") }
        stamp.get().asFile.writeText("ok\n")
    }
}
