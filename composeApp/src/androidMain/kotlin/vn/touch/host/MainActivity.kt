package vn.touch.host

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import cx.m42.superizer.Superizer
import cx.m42.superizer.host.platform.AndroidHost
import cx.m42.superizer.host.storage.initPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, 0x801B1B1B.toInt()),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, 0x801B1B1B.toInt()),
        )
        super.onCreate(savedInstanceState)

        // Both before the first composition: storage, then vibrator/lifecycle/connectivity.
        initPrefs(this)
        AndroidHost.init(this)

        setContent {
            val superizer = remember { host() }
            remember { deliver(intent) }
            TouchApp(superizer)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deliver(intent)
    }

    private fun deliver(intent: Intent?) {
        intent?.data?.toString()?.let { Holder.superizer?.route?.deliver(it) }
    }

    /** One host per process, not per activity: a rotation must not re-register every app. */
    private fun host(): Superizer = Holder.superizer ?: buildTouchHost(
        CoroutineScope(SupervisorJob() + Dispatchers.Main),
        debug = BuildConfig.DEBUG,
    ).also { Holder.superizer = it }

    private object Holder { var superizer: Superizer? = null }
}
