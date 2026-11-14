package vn.touch.apps.food

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cx.m42.superizer.runtime.InstanceRuntime
import cx.m42.superizer.runtime.getJson
import cx.m42.superizer.runtime.setJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.touch.kit.BonusDeal
import vn.touch.kit.CityBonus
import vn.touch.kit.PortalInstance
import vn.touch.kit.PortalScreen
import vn.touch.kit.bonusDeal

internal sealed interface FoodPage {
    data object List : FoodPage
    data class Menu(val restaurantId: String) : FoodPage
    data object Basket : FoodPage
    data object Checkout : FoodPage
    data object Sent : FoodPage
}

internal class FoodInstance(private val runtime: InstanceRuntime) : PortalInstance<FoodPage>(FoodPage.List) {

    private val _cart = MutableStateFlow(Cart())
    val cart: StateFlow<Cart> get() = _cart.asStateFlow()

    private val _delivery = mutableStateOf(Delivery())
    val delivery: State<Delivery> get() = _delivery

    /** The city bonus account; null where the host offers none. */
    val bonus: CityBonus? = runtime.service(CityBonus.Key)

    private val _useBonus = mutableStateOf(true)
    val useBonus: State<Boolean> get() = _useBonus

    /** What the last order earned and spent, for the "thank you" page. */
    var lastBonus: Pair<Long, Long> = 0L to 0L
        private set

    private val _cuisine = MutableStateFlow<Cuisine?>(null)
    val cuisine: StateFlow<Cuisine?> get() = _cuisine.asStateFlow()

    override suspend fun onLaunch() {
        _cart.value = runtime.storage.getJson<Cart>(KEY_CART) ?: Cart()
        // The address is typed once: the next order starts from the last one.
        _delivery.value = runtime.storage.getJson<Delivery>(KEY_DELIVERY)?.copy(comment = "") ?: Delivery()
        runtime.analytics.screen(FoodApp.ID)
    }

    override fun titleOf(page: FoodPage): String? = when (page) {
        FoodPage.List -> null
        is FoodPage.Menu -> Catalog.restaurant(page.restaurantId)?.name
        FoodPage.Basket -> "Корзина"
        FoodPage.Checkout -> "Оформление"
        FoodPage.Sent -> "Заказ отправлен"
    }

    private val _query = mutableStateOf("")
    val query: State<String> get() = _query

    fun filter(cuisine: Cuisine?) { _cuisine.value = cuisine }
    fun search(text: String) { _query.value = text }

    fun openRestaurant(id: String) = push(FoodPage.Menu(id))
    fun openBasket() = push(FoodPage.Basket)
    fun checkout() = push(FoodPage.Checkout)

    fun add(restaurant: Restaurant, dishId: String) = changeCart(_cart.value.add(restaurant, dishId))
    fun remove(dishId: String) = changeCart(_cart.value.remove(dishId))
    fun clearCart() = changeCart(Cart())

    fun editDelivery(transform: (Delivery) -> Delivery) {
        _delivery.value = transform(_delivery.value)
    }

    fun useBonus(on: Boolean) { _useBonus.value = on }

    /** The basket's sums with bonuses counted: spent only on checkout, when the switch is on. */
    fun deal(restaurant: Restaurant, cart: Cart, spending: Boolean): BonusDeal =
        bonusDeal(restaurant.bonus, cart.total(restaurant), bonus?.balance?.value ?: 0, spending && _useBonus.value)


    /** Called once the messenger has been opened with the order in it. */
    fun sent() {
        runtime.analytics.event("food_order")
        val cart = _cart.value
        val restaurant = Catalog.restaurant(cart.restaurantId)
        lastBonus = 0L to 0L
        if (bonus != null && restaurant != null) {
            val deal = deal(restaurant, cart, spending = true)
            val spent = if (bonus.spend(deal.spend, "Оплата заказа · ${restaurant.name}", FoodApp.ID)) deal.spend else 0
            // Spending fell through (the balance moved): earn on the full price, as the vendor will charge it.
            val earn = if (spent == deal.spend) deal.earn else restaurant.bonus?.earnFor(cart.total(restaurant)) ?: 0
            bonus.earn(earn, "Заказ · ${restaurant.name}", FoodApp.ID)
            lastBonus = earn to spent
        }
        val d = _delivery.value
        runtime.scope.launch { runtime.storage.setJson(KEY_DELIVERY, d) }
        changeCart(Cart())
        toRoot()
        push(FoodPage.Sent)
    }

    private fun changeCart(cart: Cart) {
        _cart.value = cart
        runtime.scope.launch { runtime.storage.setJson(KEY_CART, cart) }
    }

    @Composable
    override fun Content() { vn.touch.kit.PortalTheme { PortalScreen(this, bonus) { FoodScreen(this) } } }

    companion object {
        const val KEY_CART = "cart"
        const val KEY_DELIVERY = "delivery"
    }
}
