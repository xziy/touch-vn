@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package vn.touch.apps.food

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
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

class FoodContractTest : AppContractTest(FoodApp())

class CartTest {
    private val pho = Catalog.restaurant("pho-hung")!!
    private val pizza = Catalog.restaurant("pizza-sea")!!

    @Test fun addingAndRemovingCounts() {
        val cart = Cart().add(pho, "pho-bo").add(pho, "pho-bo").add(pho, "nem")
        assertEquals(3, cart.count)
        assertEquals(55_000L * 2 + 45_000, cart.subtotal(pho))
        assertEquals(cart.subtotal(pho) + pho.deliveryFee, cart.total(pho))
        assertEquals(Cart(), cart.remove("pho-bo").remove("pho-bo").remove("nem"))
    }

    @Test fun anotherRestaurantStartsANewBasket() {
        val cart = Cart().add(pho, "pho-bo").add(pizza, "margherita")
        assertEquals("pizza-sea", cart.restaurantId)
        assertEquals(mapOf("margherita" to 1), cart.items)
    }

    @Test fun theMessageListsEverything() {
        val cart = Cart().add(pho, "pho-bo")
        val text = orderMessage(pho, cart, Delivery("Иван", "+84 900 000 000", "Отель X, 1205", ""))
        assertTrue("Фо бо × 1" in text)
        assertTrue("Итого: 70 000 ₫" in text)
        assertTrue("Отель X, 1205" in text)
    }
}

class FoodFlowTest {
    private fun AppTestScope<*>.tap(tag: String) { runCatching { node(tag).performScrollTo() }; click(tag) }

    @Test fun orderGoesToTheRestaurantsMessenger() = runAppTest(FoodApp()) {
        tap("food:restaurant-pho-hung")
        tap("food:dish-pho-bo-plus")
        tap("food:dish-pho-bo-plus")
        node("food:dish-pho-bo-qty").assertTextEquals("2")
        tap("food:basket")
        node("food:total").assertTextEquals("125 000 ₫")
        tap("food:checkout")
        assertShown("food:fill-in")
        type("food:name", "Иван")
        type("food:phone", "+84 900 000 000")
        type("food:address", "Hùng Vương 1")
        tap("food:contact-whatsapp")
        assertShown("food:sent")
        val url = runtime.navigation.urls.single()
        assertTrue(url.startsWith("https://wa.me/84900000101?text="), url)
    }

    private fun wallet() = StoredCityBonus(InMemoryStorage(), { 1_000L }, CoroutineScope(Dispatchers.Unconfined))

    @Test fun bonusesPayPartAndComeBack() {
        val bonus = wallet()
        runAppTest(FoodApp(), prepare = { it.provide(CityBonus.Key, bonus) }) {
            ui.onNodeWithTag("food:bonus-balance", useUnmergedTree = true).assertTextEquals("300 бонусов")
            tap("food:restaurant-pho-hung")
            assertShown("food:bonus-offer")
            tap("food:dish-pho-bo-plus")
            tap("food:dish-pho-bo-plus")
            tap("food:basket")
            tap("food:checkout")
            // 125 000 ₫, 30% may be bonuses: all 300 go, 30 000 ₫ off.
            node("food:bonus-discount").assertTextEquals("−30 000 ₫")
            node("food:total").assertTextEquals("95 000 ₫")
            type("food:name", "Иван")
            type("food:phone", "+84 900 000 000")
            type("food:address", "Hùng Vương 1")
            tap("food:contact-whatsapp")
            assertShown("food:bonus-result")
            val text = java.net.URLDecoder.decode(runtime.navigation.urls.single().substringAfter("text="), "UTF-8")
            assertTrue("К оплате: 95 000 ₫" in text, text)
        }
        // 5% of the 95 000 ₫ paid.
        assertEquals(47L, bonus.balance.value)
    }

    @Test fun theSwitchKeepsTheBonuses() {
        val bonus = wallet()
        runAppTest(FoodApp(), prepare = { it.provide(CityBonus.Key, bonus) }) {
            tap("food:restaurant-pho-hung")
            tap("food:dish-pho-bo-plus")
            tap("food:dish-pho-bo-plus")
            tap("food:basket")
            tap("food:checkout")
            tap("food:bonus-use")
            node("food:total").assertTextEquals("125 000 ₫")
        }
        assertEquals(300L, bonus.balance.value)
    }

    @Test fun theBonusScreenOpensInPlaceAndBackReturns() {
        runAppTest(FoodApp(), prepare = { it.provide(CityBonus.Key, wallet()) }) {
            tap("food:restaurant-pho-hung")
            tap("food:bonus-offer")
            assertShown("bonus:page")
            node("bonus:balance").assertTextEquals("300")
            assertTrue((instance as FoodInstance).back())
            assertShown("food:bonus-offer")
        }
    }

    @Test fun aRestaurantOutsideTheProgrammeSaysSo() {
        runAppTest(FoodApp(), prepare = { it.provide(CityBonus.Key, wallet()) }) {
            tap("food:restaurant-lac-canh")
            assertShown("food:bonus-none")
        }
    }

    @Test fun withoutTheServiceNothingAboutBonusesIsShown() = runAppTest(FoodApp()) {
        assertAbsent("food:bonus")
        tap("food:restaurant-pho-hung")
        assertAbsent("food:bonus-offer")
    }
}
