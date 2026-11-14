package vn.touch.apps.food

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import cx.m42.superizer.theme.AppTheme
import cx.m42.superizer.ui.platform.SystemBackHandler
import vn.touch.kit.Art
import vn.touch.kit.BarButton
import vn.touch.kit.CategoryRow
import vn.touch.kit.ContactButtons
import vn.touch.kit.DetailHero
import vn.touch.kit.Divider
import vn.touch.kit.DoneState
import vn.touch.kit.EmptyState
import vn.touch.kit.FeatureCard
import vn.touch.kit.Field
import vn.touch.kit.FormSection
import vn.touch.kit.Glyph
import vn.touch.kit.Carousel
import vn.touch.kit.Hint
import vn.touch.kit.Ink
import vn.touch.kit.ListRow
import vn.touch.kit.MetaLine
import vn.touch.kit.MiniHeader
import vn.touch.kit.Muted
import vn.touch.kit.Notice
import vn.touch.kit.PortalPage
import vn.touch.kit.Price
import vn.touch.kit.PrimaryButton
import vn.touch.kit.SearchField
import vn.touch.kit.SectionHeader
import vn.touch.kit.Stepper
import vn.touch.kit.SummaryRow
import vn.touch.kit.TextAction
import vn.touch.kit.Tile
import vn.touch.kit.TitleBlock
import vn.touch.kit.formatVnd
import vn.touch.kit.BonusBadge
import vn.touch.kit.BonusBalanceChip
import vn.touch.kit.BonusDeal
import vn.touch.kit.BonusOfferCard
import vn.touch.kit.BonusResult
import vn.touch.kit.BonusSpendRow
import vn.touch.kit.BonusTotals
import vn.touch.kit.bonusBadge
import vn.touch.kit.bonusRate

private const val TAG = FoodApp.ID

@Composable
internal fun FoodScreen(instance: FoodInstance) {
    val pages by instance.pages.collectAsState()
    SystemBackHandler(enabled = pages.size > 1) { instance.back() }

    when (val page = pages.last()) {
        FoodPage.List -> RestaurantList(instance)
        is FoodPage.Menu -> MenuPage(instance, page.restaurantId)
        FoodPage.Basket -> BasketPage(instance)
        FoodPage.Checkout -> CheckoutPage(instance)
        FoodPage.Sent -> SentPage(instance)
    }
}

private val Restaurant.eta: String get() = "${etaMinutes.first}–${etaMinutes.last} мин"

@Composable
private fun RestaurantList(instance: FoodInstance) {
    val cuisine by instance.cuisine.collectAsState()
    val cart by instance.cart.collectAsState()
    val query by instance.query
    val list = filterRestaurants(Catalog.restaurants, cuisine, query)
    val browsing = cuisine == null && query.isBlank()
    PortalPage(bottomBar = basketBar(instance, cart)) {
        Column(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)) {
            Text("Доставка в", style = AppTheme.footnote, color = Muted)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Нячанг, центр", style = AppTheme.subtitle, color = Ink)
                Spacer(Modifier.width(6.dp))
                vn.touch.kit.GlyphIcon(Glyph.Pin, vn.touch.kit.Coral, size = 18.dp)
            }
        }
        BonusBalanceChip(instance.bonus, instance::openBonus, TAG)
        SearchField(query, instance::search, "Ресторан или блюдо", Modifier.testTag("$TAG:search"))
        CategoryRow(
            options = Cuisine.entries.map { Tile(it.name, it.short, it.glyph) },
            selected = cuisine?.name,
            onSelect = { key -> instance.filter(key?.let(Cuisine::valueOf)) },
            tagPrefix = TAG,
        )
        if (browsing) {
            SectionHeader("Популярное")
            Carousel {
                Catalog.restaurants.sortedByDescending { it.rating }.take(3).forEach { r ->
                    FeatureCard(
                        title = r.name,
                        glyph = r.cuisine.glyph,
                        seed = r.id,
                        subtitle = r.cuisine.label,
                        rating = r.rating,
                        pill = r.eta,
                        bonus = instance.bonus?.let { bonusRate(r.bonus) },
                        onClick = { instance.openRestaurant(r.id) },
                        modifier = Modifier.width(230.dp).testTag("$TAG:popular-${r.id}"),
                    )
                }
            }
        }
        SectionHeader(if (browsing) "Все рестораны" else "Найдено: ${list.size}")
        if (list.isEmpty()) Hint("Ничего не нашлось — попробуйте другое блюдо", Modifier.testTag("$TAG:empty"))
        list.forEach { r ->
            ListRow(
                title = r.name,
                glyph = r.cuisine.glyph,
                seed = r.id,
                subtitle = "${r.cuisine.label} · ${r.eta}",
                rating = r.rating,
                meta = "доставка ${formatVnd(r.deliveryFee)}",
                bonus = instance.bonus?.let { bonusRate(r.bonus) },
                onClick = { instance.openRestaurant(r.id) },
                modifier = Modifier.testTag("$TAG:restaurant-${r.id}"),
            )
        }
        Notice("Демо-каталог: заказ уходит ресторану сообщением в WhatsApp / Telegram / Zalo.")
    }
}

/** The pinned "Корзина" bar, or nothing while the basket is empty. */
private fun basketBar(instance: FoodInstance, cart: Cart): (@Composable () -> Unit)? {
    val restaurant = Catalog.restaurant(cart.restaurantId) ?: return null
    if (cart.isEmpty) return null
    return {
        BarButton(
            text = "Корзина · ${cart.count}",
            trailing = formatVnd(cart.subtotal(restaurant)),
            onClick = instance::openBasket,
            modifier = Modifier.testTag("$TAG:basket"),
        )
    }
}

@Composable
private fun MenuPage(instance: FoodInstance, restaurantId: String) {
    val restaurant = Catalog.restaurant(restaurantId) ?: return
    val cart by instance.cart.collectAsState()
    val mine = cart.restaurantId == restaurant.id
    PortalPage(bottomBar = basketBar(instance, cart)) {
        DetailHero(restaurant.cuisine.glyph, restaurant.id, pill = restaurant.eta)
        TitleBlock(restaurant.name, restaurant.cuisine.label, rating = restaurant.rating)
        Column(Modifier.padding(horizontal = 16.dp)) {
            MetaLine(Glyph.Pin, restaurant.address)
            MetaLine(Glyph.Clock, "${restaurant.hours} · доставка ${formatVnd(restaurant.deliveryFee)} · от ${formatVnd(restaurant.minOrder)}")
        }
        BonusOfferCard(
            instance.bonus,
            restaurant.bonus,
            earn = if (mine && !cart.isEmpty) instance.deal(restaurant, cart, spending = false).earn else 0,
            onClick = instance::openBonus,
            tagPrefix = TAG,
            earnWhat = if (mine && !cart.isEmpty) "за эту корзину" else "с каждого заказа",
        )
        if (!cart.isEmpty && !mine) {
            Notice("В корзине блюда из другого ресторана. Если добавить блюдо отсюда, корзина начнётся заново.")
        }
        SectionHeader("Хиты")
        Carousel {
            restaurant.menu.take(3).forEach { dish ->
                FeatureCard(
                    title = dish.name,
                    glyph = restaurant.cuisine.glyph,
                    seed = dish.id,
                    price = formatVnd(dish.price),
                    imageHeight = 96.dp,
                    onClick = { instance.add(restaurant, dish.id) },
                    modifier = Modifier.width(140.dp).testTag("$TAG:hit-${dish.id}"),
                )
            }
        }
        SectionHeader("Меню")
        restaurant.menu.forEachIndexed { i, dish ->
            if (i > 0) Divider()
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Art(restaurant.cuisine.glyph, dish.id, Modifier.size(56.dp), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(dish.name, style = AppTheme.body.copy(fontWeight = FontWeight.SemiBold), color = Ink)
                    if (dish.description.isNotBlank()) {
                        Text(dish.description, style = AppTheme.footnote, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    Price(formatVnd(dish.price), Modifier.padding(top = 2.dp))
                    val dishBonus = restaurant.bonus?.earnFor(dish.price) ?: 0
                    if (instance.bonus != null) bonusBadge(dishBonus)?.let { BonusBadge(it, Modifier.padding(top = 3.dp)) }
                }
                Spacer(Modifier.width(8.dp))
                Stepper(
                    quantity = if (mine) cart.quantity(dish.id) else 0,
                    onPlus = { instance.add(restaurant, dish.id) },
                    onMinus = { instance.remove(dish.id) },
                    tag = "$TAG:dish-${dish.id}",
                )
            }
        }
    }
}

@Composable
private fun BasketPage(instance: FoodInstance) {
    val cart by instance.cart.collectAsState()
    val restaurant = Catalog.restaurant(cart.restaurantId)
    if (restaurant == null || cart.isEmpty) {
        PortalPage {
            EmptyState(Glyph.Cart, "Корзина пуста", "Загляните в меню и добавьте что-нибудь вкусное", Modifier.testTag("$TAG:basket-empty"))
            PrimaryButton("К ресторанам", { instance.toRoot() })
        }
        return
    }
    val short = restaurant.minOrder - cart.subtotal(restaurant)
    PortalPage(
        bottomBar = {
            BarButton("Оформить заказ", instance::checkout, Modifier.testTag("$TAG:checkout"), enabled = short <= 0)
        },
    ) {
        MiniHeader(restaurant.name, "${restaurant.cuisine.label} · ${restaurant.address}", restaurant.cuisine.glyph, restaurant.id)
        Divider()
        cart.lines(restaurant).forEach { (dish, qty) ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(dish.name, style = AppTheme.body.copy(fontWeight = FontWeight.SemiBold), color = Ink)
                    Text(formatVnd(dish.price * qty), style = AppTheme.label, color = Muted)
                }
                Stepper(qty, { instance.add(restaurant, dish.id) }, { instance.remove(dish.id) }, "$TAG:line-${dish.id}")
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            TextAction("Добавить ещё", { instance.toRoot(); instance.openRestaurant(restaurant.id) }, glyph = Glyph.Plus)
            Spacer(Modifier.weight(1f))
            TextAction("Очистить", { instance.clearCart(); instance.back() }, Modifier.testTag("$TAG:clear"), glyph = Glyph.Trash)
        }
        Divider()
        Spacer(Modifier.height(8.dp))
        Totals(cart, restaurant, instance.deal(restaurant, cart, spending = false), instance)
        if (short > 0) Notice("Минимальный заказ ${formatVnd(restaurant.minOrder)} — добавьте ещё на ${formatVnd(short)}.")
    }
}

@Composable
private fun Totals(cart: Cart, restaurant: Restaurant, deal: BonusDeal, instance: FoodInstance) {
    SummaryRow("Блюда", formatVnd(cart.subtotal(restaurant)))
    SummaryRow("Доставка", formatVnd(restaurant.deliveryFee))
    if (deal.spend > 0) {
        SummaryRow("Сумма заказа", formatVnd(cart.total(restaurant)))
    }
    BonusTotals(instance.bonus, deal, TAG)
    SummaryRow(if (deal.spend > 0) "К оплате" else "Итого", formatVnd(deal.pay), strong = true, valueModifier = Modifier.testTag("$TAG:total"))
}

@Composable
private fun CheckoutPage(instance: FoodInstance) {
    val cart by instance.cart.collectAsState()
    val delivery by instance.delivery
    val restaurant = Catalog.restaurant(cart.restaurantId) ?: return
    val ready = canSend(restaurant, cart, delivery)
    instance.bonus?.balance?.collectAsState()?.value
    val useBonus by instance.useBonus
    val deal = instance.deal(restaurant, cart, spending = useBonus)
    PortalPage {
        MiniHeader(restaurant.name, "${cart.count} поз. · ${restaurant.eta}", restaurant.cuisine.glyph, restaurant.id)
        FormSection("Адрес доставки") {
            Field(delivery.name, { v -> instance.editDelivery { it.copy(name = v) } }, "Имя", Modifier.testTag("$TAG:name"))
            Field(delivery.phone, { v -> instance.editDelivery { it.copy(phone = v) } }, "Телефон / WhatsApp", Modifier.testTag("$TAG:phone"))
            Field(delivery.address, { v -> instance.editDelivery { it.copy(address = v) } }, "Адрес, отель, номер комнаты", Modifier.testTag("$TAG:address"), singleLine = false)
            Field(delivery.comment, { v -> instance.editDelivery { it.copy(comment = v) } }, "Комментарий (необязательно)", Modifier.testTag("$TAG:comment"), singleLine = false)
        }
        SectionHeader("Сумма")
        BonusSpendRow(instance.bonus, restaurant.bonus, cart.total(restaurant), useBonus, instance::useBonus, TAG)
        Totals(cart, restaurant, deal, instance)
        SectionHeader("Отправить заказ")
        if (ready) {
            ContactButtons(
                contacts = restaurant.contacts,
                tagPrefix = TAG,
                message = orderMessage(restaurant, cart, delivery, deal),
                onOpened = { instance.sent() },
            )
        } else {
            Hint("Заполните имя, телефон и адрес", Modifier.testTag("$TAG:fill-in"))
        }
        Notice("Оплата курьеру наличными или переводом. Ресторан подтвердит заказ в мессенджере.", glyph = Glyph.Truck)
    }
}

@Composable
private fun SentPage(instance: FoodInstance) {
    PortalPage {
        Spacer(Modifier.height(24.dp))
        DoneState(
            "Спасибо за заказ!",
            "Сообщение с заказом открылось в мессенджере — отправьте его, и ресторан подтвердит время доставки.",
            Modifier.testTag("$TAG:sent"),
        )
        val (earned, spent) = instance.lastBonus
        BonusResult(instance.bonus, earned, spent, instance::openBonus, TAG)
        PrimaryButton("К ресторанам", { instance.toRoot() }, Modifier.testTag("$TAG:done"))
    }
}
