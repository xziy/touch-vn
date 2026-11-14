package vn.touch.host

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun main(args: Array<String>) = application {
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    val superizer = remember { buildTouchHost(scope, debug = true) }

    // The desktop stand-in for a QR code or a notification tap: --link=touchvn://app/food
    remember {
        args.firstOrNull { it.startsWith("--link=") || it.startsWith("--activate=") }
            ?.substringAfter('=')
            ?.let { superizer.route.deliver(it) }
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 420.dp, height = 820.dp),
        title = HOST_NAME,
    ) {
        TouchApp(superizer)
    }
}
