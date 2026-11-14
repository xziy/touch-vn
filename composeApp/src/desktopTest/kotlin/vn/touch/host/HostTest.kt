@file:OptIn(ExperimentalTestApi::class)

package vn.touch.host

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import cx.m42.superizer.Superizer
import cx.m42.superizer.host.storage.PrefsStorage
import java.nio.file.Files
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class HostTest {

    @BeforeTest
    fun isolateStorage() {
        PrefsStorage.useDirectory(Files.createTempDirectory("touchvn-test").toFile())
    }

    private fun host(): Superizer =
        buildTouchHost(CoroutineScope(SupervisorJob() + Dispatchers.Unconfined), debug = true)
            .also { it.settings.chooseLanguage("ru") }

    @Test
    fun everyServiceIsOnHomeAndOpens() = runComposeUiTest {
        setContent { TouchApp(host()) }
        waitForIdle()
        for (id in listOf("food", "flowers", "ads", "visarun", "exchange")) {
            onNodeWithTag("home:service-$id").performScrollTo().assertIsDisplayed()
        }
        onNodeWithTag("home:service-food").performClick()
        waitForIdle()
        onNodeWithTag("food:restaurant-pho-hung").assertIsDisplayed()
    }

    @Test
    fun aDeepLinkOpensAService() = runComposeUiTest {
        val superizer = host()
        superizer.route.deliver("touchvn://app/exchange")
        setContent { TouchApp(superizer) }
        waitForIdle()
        onNodeWithTag("exchange:amount").assertIsDisplayed()
    }

    @Test
    fun theCityBonusIsOnHomeAndOneTapAway() = runComposeUiTest {
        setContent { TouchApp(host()) }
        waitForIdle()
        onNodeWithTag("host:bonus-balance", useUnmergedTree = true).assertTextEquals("300 бонусов")
        onNodeWithTag("host:bonus").performClick()
        waitForIdle()
        onNodeWithTag("bonus:balance").assertTextEquals("300")
        onNodeWithTag("bonus:place-flowers").performScrollTo().performClick()
        waitForIdle()
        onNodeWithTag("flowers:bonus").assertIsDisplayed()
    }

    @Test
    fun everyServiceShowsTheSameBalance() = runComposeUiTest {
        val superizer = host()
        setContent { TouchApp(superizer) }
        waitForIdle()
        onNodeWithTag("home:service-exchange").performScrollTo().performClick()
        waitForIdle()
        onNodeWithTag("exchange:bonus-balance", useUnmergedTree = true).assertTextEquals("300 бонусов")
        onNodeWithTag("exchange:bonus").performClick()
        waitForIdle()
        onNodeWithTag("bonus:page").assertIsDisplayed()
    }

    @Test
    fun searchNarrowsHomeAndAPickOpensItsService() = runComposeUiTest {
        setContent { TouchApp(host()) }
        waitForIdle()
        onNodeWithTag("home:search").performTextInput("роз")
        waitForIdle()
        onNodeWithTag("home:service-food").assertDoesNotExist()
        onNodeWithTag("home:pick-pho-bo").assertDoesNotExist()
        onNodeWithTag("home:pick-red-25").performScrollTo().performClick()
        waitForIdle()
        onNodeWithTag("flowers:bouquet-red-25").assertIsDisplayed()
    }

    @Test
    fun theGreetingFollowsNhaTrangTime() {
        val hour = 3_600_000L
        assertEquals("Доброе утро!", greeting(1 * hour)) // 08:00 in Nha Trang
        assertEquals("Добрый день!", greeting(7 * hour)) // 14:00
        assertEquals("Добрый вечер!", greeting(13 * hour)) // 20:00
        assertEquals("Доброй ночи!", greeting(19 * hour)) // 02:00
    }
}
