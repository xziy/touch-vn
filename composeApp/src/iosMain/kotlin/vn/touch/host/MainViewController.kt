package vn.touch.host

import androidx.compose.ui.window.ComposeUIViewController
import cx.m42.superizer.Superizer
import kotlin.experimental.ExperimentalNativeApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import platform.UIKit.UIViewController

/**
 * One host per process, not per controller: SwiftUI may make a second controller on a scene
 * reconnect, and it draws the same host (as MainActivity's Holder on Android).
 */
@OptIn(ExperimentalNativeApi::class)
private val host: Superizer by lazy {
    buildTouchHost(CoroutineScope(SupervisorJob() + Dispatchers.Main), debug = Platform.isDebugBinary)
}

/** A deep link (`touchvn://app/food`) from `iosApp/iOSApp.swift`'s `onOpenURL`. */
fun deliverLink(url: String) {
    host.route.deliver(url)
}

/** The whole app as one UIKit controller, which `iosApp/ContentView.swift` wraps for SwiftUI. */
fun MainViewController(): UIViewController = ComposeUIViewController { TouchApp(host) }
