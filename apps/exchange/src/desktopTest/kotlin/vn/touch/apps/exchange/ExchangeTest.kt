@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package vn.touch.apps.exchange

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import cx.m42.superizer.testing.AppContractTest
import cx.m42.superizer.testing.AppTestScope
import cx.m42.superizer.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import cx.m42.superizer.testing.InMemoryStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import vn.touch.kit.CityBonus
import vn.touch.kit.StoredCityBonus

class ExchangeContractTest : AppContractTest(ExchangeApp())

class QuotesTest {
    @Test fun bestRateFirstAndOnlyWhoBuys() {
        val usd = quotes(Catalog.points, Currency.USD, 100.0)
        assertEquals("gold-dam", usd.first().point.id)
        assertEquals(2_631_000L, usd.first().vnd)
        assertTrue(usd.zipWithNext().all { (a, b) -> a.rate >= b.rate })
        val rub = quotes(Catalog.points, Currency.RUB, 10_000.0)
        assertEquals("private-rub", rub.first().point.id)
        assertTrue(rub.none { it.point.id == "bank-vcb" })
    }

    @Test fun amounts() {
        assertEquals(1500.5, parseAmount("1 500,5"))
        assertNull(parseAmount(""))
        assertNull(parseAmount("0"))
        assertEquals("305", formatRate(305.0))
        assertEquals("26 310", formatRate(26_310.0))
        assertEquals("305,5", formatRate(305.5))
    }
}

class ExchangeFlowTest {
    private fun AppTestScope<*>.tap(tag: String) { runCatching { node(tag).performScrollTo() }; click(tag) }

    @Test fun roublesToDong() = runAppTest(ExchangeApp()) {
        tap("exchange:chip-RUB")
        node("exchange:amount").performTextReplacement("20000")
        ui.waitForIdle()
        tap("exchange:point-private-rub")
        tap("exchange:contact-telegram")
        val url = runtime.navigation.urls.single()
        assertTrue(url.startsWith("https://t.me/demo_rub_vnd_nt?text="), url)
        assertTrue(vn.touch.kit.urlEncode("20 000 RUB") in url, url)
    }

    @Test fun aPartnerPointGivesItsBonusOnceADay() {
        val bonus = StoredCityBonus(InMemoryStorage(), { 1_000L }, CoroutineScope(Dispatchers.Unconfined), welcome = 0)
        runAppTest(ExchangeApp(), prepare = { it.provide(CityBonus.Key, bonus) }) {
            tap("exchange:point-gold-dam")
            assertShown("exchange:bonus-offer")
            tap("exchange:contact-phone")
            tap("exchange:contact-phone")
        }
        assertEquals(50L, bonus.balance.value)
    }
}
