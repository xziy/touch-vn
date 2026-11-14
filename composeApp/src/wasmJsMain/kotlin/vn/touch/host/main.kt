package vn.touch.host

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(
        viewportContainerId = "composeTarget",
        // Semantics mirrored into the DOM as ARIA: screen readers, and Playwright, can see the UI.
        configure = { isA11YEnabled = true },
    ) {
        val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
        val superizer = remember { buildTouchHost(scope, debug = true) }
        remember {
            val search = window.location.search
            queryParam(search, "link")?.let { superizer.route.deliver(it) }
            queryParam(search, "activate")?.let { superizer.route.deliver(it) }
        }
        TouchApp(superizer)
    }
}

internal fun queryParam(search: String, name: String): String? = search
    .removePrefix("?").split('&')
    .firstOrNull { it.startsWith("$name=") }?.substringAfter('=')
    ?.let { runCatching { decodeUriComponent(it) }.getOrDefault(it) }

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun decodeUriComponent(value: String): String = js("decodeURIComponent(value)")
