package vn.touch.apps.flowers

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.performScrollTo
import cx.m42.superizer.testing.AppContractTest
import cx.m42.superizer.testing.AppTestScope
import cx.m42.superizer.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import cx.m42.superizer.testing.InMemoryStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import vn.touch.kit.CityBonus
import vn.touch.kit.StoredCityBonus

class FlowersContractTest : AppContractTest(FlowersApp())

class FlowersFlowTest {
    private fun AppTestScope<*>.tap(tag: String) { runCatching { node(tag).performScrollTo() }; click(tag) }

    @Test fun aBouquetIsOrderedWithACard() = runAppTest(FlowersApp()) {
        tap("flowers:chip-Box")
        tap("flowers:bouquet-box-pink")
        tap("flowers:order")
        assertShown("flowers:fill-in")
        type("flowers:recipient", "Анна")
        type("flowers:address", "Ana Mandara, 302")
        type("flowers:when", "завтра к 10:00")
        type("flowers:card", "С днём рождения!")
        type("flowers:sender", "Пётр")
        type("flowers:sender-phone", "0900000000")
        tap("flowers:contact-telegram")
        assertShown("flowers:sent")
        val url = runtime.navigation.urls.single()
        assertTrue(url.startsWith("https://t.me/demo_flowers_nt?text="), url)
    }

    @Test fun aShopOutsideTheProgrammeTakesNoBonuses() {
        val bonus = StoredCityBonus(InMemoryStorage(), { 1_000L }, CoroutineScope(Dispatchers.Unconfined))
        runAppTest(FlowersApp(), prepare = { it.provide(CityBonus.Key, bonus) }) {
            tap("flowers:bouquet-sunny")
            assertShown("flowers:bonus-none")
            tap("flowers:order")
            assertAbsent("flowers:bonus-use")
        }
        assertEquals(300L, bonus.balance.value)
    }

    @Test fun aPartnerBouquetEarnsAfterSpending() {
        val bonus = StoredCityBonus(InMemoryStorage(), { 1_000L }, CoroutineScope(Dispatchers.Unconfined))
        runAppTest(FlowersApp(), prepare = { it.provide(CityBonus.Key, bonus) }) {
            tap("flowers:bouquet-box-pink")
            // Роза ветров: 10% back, up to 20% paid. 690 000 ₫ → 300 bonuses (30 000 ₫) off, 660 000 ₫ paid.
            tap("flowers:order")
            node("flowers:total").assertTextEquals("660 000 ₫")
            type("flowers:recipient", "Анна")
            type("flowers:address", "Ana Mandara, 302")
            type("flowers:when", "завтра к 10:00")
            type("flowers:sender", "Пётр")
            type("flowers:sender-phone", "0900000000")
            tap("flowers:contact-telegram")
            assertShown("flowers:bonus-result")
        }
        assertEquals(660L, bonus.balance.value)
    }
}
