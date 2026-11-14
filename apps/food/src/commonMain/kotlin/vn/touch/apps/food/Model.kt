package vn.touch.apps.food

import kotlinx.serialization.Serializable
import vn.touch.kit.BonusDeal
import vn.touch.kit.BonusTerms
import vn.touch.kit.Contact
import vn.touch.kit.bonusMessageLines
import vn.touch.kit.Glyph
import vn.touch.kit.formatVnd
import vn.touch.kit.looksLikePhone

internal enum class Cuisine(val label: String, val short: String, val glyph: Glyph) {
    Vietnamese("Вьетнамская", "Вьетнам", Glyph.Bowl),
    Seafood("Морепродукты", "Море", Glyph.Fish),
    Russian("Русская", "Русская", Glyph.Dumpling),
    Pizza("Пицца", "Пицца", Glyph.Pizza),
    Asian("Суши и роллы", "Суши", Glyph.Sushi),
    Coffee("Кофе и десерты", "Кофе", Glyph.Coffee),
}

/** A cuisine filter and a query: the query matches a restaurant's name or any of its dishes. */
internal fun filterRestaurants(all: List<Restaurant>, cuisine: Cuisine?, query: String): List<Restaurant> {
    val words = query.lowercase().split(' ').filter { it.isNotBlank() }
    return all.filter { r ->
        (cuisine == null || r.cuisine == cuisine) &&
            words.all { w -> w in r.name.lowercase() || r.menu.any { w in it.name.lowercase() } }
    }
}

internal data class Dish(
    val id: String,
    val name: String,
    val description: String,
    val price: Long,
)

internal data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: Cuisine,
    val address: String,
    val hours: String,
    val deliveryFee: Long,
    val minOrder: Long,
    val etaMinutes: IntRange,
    val rating: Double,
    val contacts: List<Contact>,
    val menu: List<Dish>,
    /** The restaurant's part in the city bonus system; null: it is not a partner. */
    val bonus: BonusTerms? = null,
) {
    fun dish(id: String): Dish? = menu.firstOrNull { it.id == id }
}

/** One restaurant's basket — a Grab-style rule: one place per order. */
@Serializable
internal data class Cart(
    val restaurantId: String? = null,
    val items: Map<String, Int> = emptyMap(),
) {
    val isEmpty: Boolean get() = items.isEmpty()
    val count: Int get() = items.values.sum()

    fun quantity(dishId: String): Int = items[dishId] ?: 0

    /** Adding from another restaurant starts a new basket rather than mixing two kitchens. */
    fun add(restaurant: Restaurant, dishId: String): Cart {
        val base = if (restaurantId == restaurant.id) this else Cart(restaurant.id)
        return base.copy(items = base.items + (dishId to base.quantity(dishId) + 1))
    }

    fun remove(dishId: String): Cart {
        val left = quantity(dishId) - 1
        val items = if (left <= 0) items - dishId else items + (dishId to left)
        return if (items.isEmpty()) Cart() else copy(items = items)
    }

    fun lines(restaurant: Restaurant): List<Pair<Dish, Int>> =
        items.mapNotNull { (id, qty) -> restaurant.dish(id)?.let { it to qty } }

    fun subtotal(restaurant: Restaurant): Long = lines(restaurant).sumOf { (dish, qty) -> dish.price * qty }

    fun total(restaurant: Restaurant): Long = subtotal(restaurant) + restaurant.deliveryFee
}

@Serializable
internal data class Delivery(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val comment: String = "",
)

/** Everything a restaurant needs before it can take the order. */
internal fun canSend(restaurant: Restaurant, cart: Cart, delivery: Delivery): Boolean =
    !cart.isEmpty && cart.restaurantId == restaurant.id &&
        cart.subtotal(restaurant) >= restaurant.minOrder &&
        delivery.name.isNotBlank() && looksLikePhone(delivery.phone) && delivery.address.isNotBlank()

/** The order as the restaurant reads it in its messenger. */
internal fun orderMessage(restaurant: Restaurant, cart: Cart, delivery: Delivery, deal: BonusDeal? = null): String = buildString {
    appendLine("Заказ с портала Touch VN — ${restaurant.name}")
    appendLine()
    cart.lines(restaurant).forEach { (dish, qty) ->
        appendLine("• ${dish.name} × $qty — ${formatVnd(dish.price * qty)}")
    }
    appendLine("Доставка — ${formatVnd(restaurant.deliveryFee)}")
    appendLine("Итого: ${formatVnd(cart.total(restaurant))}")
    if (deal != null && deal.spend > 0) appendLine(bonusMessageLines(deal))
    appendLine()
    appendLine("Имя: ${delivery.name.trim()}")
    appendLine("Телефон: ${delivery.phone.trim()}")
    appendLine("Адрес: ${delivery.address.trim()}")
    if (delivery.comment.isNotBlank()) appendLine("Комментарий: ${delivery.comment.trim()}")
}.trimEnd()
