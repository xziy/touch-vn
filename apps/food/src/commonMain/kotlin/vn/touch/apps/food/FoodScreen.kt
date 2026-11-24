package vn.touch.apps.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cx.m42.superizer.kit.Text
import cx.m42.superizer.ui.platform.SystemBackHandler
import vn.touch.kit.Art
import vn.touch.kit.BarButton
import vn.touch.kit.BonusBadge
import vn.touch.kit.BonusDeal
import vn.touch.kit.BonusOfferCard
import vn.touch.kit.BonusResult
import vn.touch.kit.BonusSpendRow
import vn.touch.kit.BonusTotals
import vn.touch.kit.Carousel
import vn.touch.kit.CategoryRow
import vn.touch.kit.CoinMark
import vn.touch.kit.ContactButtons
import vn.touch.kit.DetailHero
import vn.touch.kit.Divider
import vn.touch.kit.DoneState
import vn.touch.kit.EmptyState
import vn.touch.kit.FeatureCard
import vn.touch.kit.Field
import vn.touch.kit.Glyph
import vn.touch.kit.GlyphIcon
import vn.touch.kit.Gutter
import vn.touch.kit.IconTile
import vn.touch.kit.ImagePill
import vn.touch.kit.MetaLine
import vn.touch.kit.MiniHeader
import vn.touch.kit.Notice
import vn.touch.kit.PortalPage
import vn.touch.kit.Price
import vn.touch.kit.PrimaryButton
import vn.touch.kit.SearchField
import vn.touch.kit.SectionHeader
import vn.touch.kit.ServiceHeader
import vn.touch.kit.Stepper
import vn.touch.kit.SummaryRow
import vn.touch.kit.TextAction
import vn.touch.kit.Tile
import vn.touch.kit.TitleBlock
import vn.touch.kit.Touch
import vn.touch.kit.bonusBadge
import vn.touch.kit.bonusRate
import vn.touch.kit.cardSurface
import vn.touch.kit.formatBonus
import vn.touch.kit.formatVnd
import vn.touch.kit.pressable

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

/** "1 ресторан", "3 ресторана", "6 ресторанов". */
private fun restaurantsWord(n: Int): String {
    val tens = n % 100
    val ones = n % 10
    return when {
        tens in 11..14 -> "ресторанов"
        ones == 1 -> "ресторан"
        ones in 2..4 -> "ресторана"
        else -> "ресторанов"
    }
}

// ------------------------------------------------------------------ the list

@Composable
private fun RestaurantList(instance: FoodInstance) {
    val cuisine by instance.cuisine.collectAsState()
    val cart by instance.cart.collectAsState()
    val query by instance.query
    val list = filterRestaurants(Catalog.restaurants, cuisine, query)
    val browsing = cuisine == null && query.isBlank()
    val all = Catalog.restaurants
    PortalPage(bottomBar = basketBar(instance, cart)) {
        ServiceHeader(
            title = "Что закажем сегодня?",
            glyph = Glyph.Cutlery,
            eyebrow = "Доставка · Нячанг, центр",
            subtitle = "${all.size} ${restaurantsWord(all.size)} · от ${all.minOf { it.etaMinutes.first }} мин",
            trailing = { WalletPill(instance) },
        ) {
            SearchField(query, instance::search, "Ресторан или блюдо", Modifier.testTag("$TAG:search"))
        }
        Spacer(Modifier.height(6.dp))
        CategoryRow(
            options = Cuisine.entries.map { Tile(it.name, it.short, it.glyph) },
            selected = cuisine?.name,
            onSelect = { key -> instance.filter(key?.let(Cuisine::valueOf)) },
            tagPrefix = TAG,
        )
        if (browsing) {
            SectionHeader("Популярное", subtitle = "Самые высокие оценки в городе")
            Carousel {
                all.sortedByDescending { it.rating }.take(3).forEach { r ->
                    FeatureCard(
                        title = r.name,
                        glyph = r.cuisine.glyph,
                        seed = r.id,
                        subtitle = r.cuisine.label,
                        rating = r.rating,
                        pill = r.eta,
                        bonus = instance.bonus?.let { bonusRate(r.bonus) },
                        onClick = { instance.openRestaurant(r.id) },
                        modifier = Modifier.width(248.dp).testTag("$TAG:popular-${r.id}"),
                    )
                }
            }
        }
        SectionHeader(if (browsing) "Все рестораны" else "Найдено: ${list.size}")
        if (list.isEmpty()) {
            EmptyState(Glyph.Search, "Ничего не нашлось", "Попробуйте другое блюдо или кухню", Modifier.testTag("$TAG:empty"))
        }
        list.forEach { r ->
            RestaurantRow(
                r,
                bonus = instance.bonus?.let { bonusRate(r.bonus) },
                onClick = { instance.openRestaurant(r.id) },
                modifier = Modifier.testTag("$TAG:restaurant-${r.id}"),
            )
        }
        Notice("Демо-каталог: заказ уходит ресторану сообщением в WhatsApp / Telegram / Zalo.", Modifier.padding(top = 10.dp))
    }
}

/** The city wallet as a glass pill on the header: the coin and the balance. Tags `food:bonus`, `food:bonus-balance`. */
@Composable
private fun WalletPill(instance: FoodInstance) {
    val bonus = instance.bonus ?: return
    val balance by bonus.balance.collectAsState()
    val c = Touch.colors
    Row(
        Modifier
            .testTag("$TAG:bonus")
            .clip(RoundedCornerShape(50))
            .background(c.onArt.copy(alpha = 0.22f))
            .pressable(onClick = instance::openBonus)
            .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinMark(24.dp)
        Spacer(Modifier.width(6.dp))
        Text(
            formatBonus(balance),
            style = Touch.type.micro.copy(fontWeight = FontWeight.ExtraBold),
            color = c.onArt,
            maxLines = 1,
            modifier = Modifier.testTag("$TAG:bonus-balance"),
        )
        GlyphIcon(Glyph.Chevron, c.onArt.copy(alpha = 0.85f), size = 14.dp)
    }
}

/** A restaurant in the list: the picture, the name, the cuisine, and its facts with their icons. */
@Composable
private fun RestaurantRow(r: Restaurant, bonus: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Touch.colors
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .cardSurface()
            .pressable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Art(r.cuisine.glyph, r.id, Modifier.size(92.dp), shape = RoundedCornerShape(16.dp)) {
            Box(Modifier.fillMaxSize().padding(6.dp), contentAlignment = Alignment.BottomStart) {
                ImagePill(r.rating.toString(), glyph = Glyph.Star, glyphTint = c.star)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(r.name, style = Touch.type.strong, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(r.cuisine.label, style = Touch.type.caption, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Fact(Glyph.Clock, r.eta)
                Spacer(Modifier.width(12.dp))
                Fact(Glyph.Bike, formatVnd(r.deliveryFee))
            }
            if (bonus != null) BonusBadge(bonus, Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun Fact(glyph: Glyph, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GlyphIcon(glyph, Touch.accent.fg, size = 14.dp)
        Spacer(Modifier.width(4.dp))
        Text(text, style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold), color = Touch.colors.ink, maxLines = 1)
    }
}

/** The pinned "Корзина" bar, or nothing while the basket is empty. */
private fun basketBar(instance: FoodInstance, cart: Cart): (@Composable () -> Unit)? {
    val restaurant = Catalog.restaurant(cart.restaurantId) ?: return null
    if (cart.isEmpty) return null
    return {
        BarButton(
            text = "Корзина",
            trailing = formatVnd(cart.subtotal(restaurant)),
            onClick = instance::openBasket,
            modifier = Modifier.testTag("$TAG:basket"),
            glyph = Glyph.Cart,
            count = cart.count,
        )
    }
}

// ------------------------------------------------------------------ a restaurant's menu

@Composable
private fun MenuPage(instance: FoodInstance, restaurantId: String) {
    val restaurant = Catalog.restaurant(restaurantId) ?: return
    val cart by instance.cart.collectAsState()
    val mine = cart.restaurantId == restaurant.id
    val qty = { dishId: String -> if (mine) cart.quantity(dishId) else 0 }
    PortalPage(bottomBar = basketBar(instance, cart)) {
        DetailHero(restaurant.cuisine.glyph, restaurant.id, pill = "Открыто · ${restaurant.hours}")
        TitleBlock(restaurant.name, restaurant.cuisine.label, rating = restaurant.rating)
        Column(Modifier.padding(start = Gutter + 2.dp, end = Gutter, bottom = 4.dp)) {
            MetaLine(Glyph.Pin, restaurant.address)
        }
        FactsCard(restaurant)
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
        SectionHeader("Хиты", subtitle = "Заказывают чаще всего")
        Carousel {
            restaurant.menu.take(3).forEach { dish ->
                HitCard(
                    dish,
                    restaurant,
                    quantity = qty(dish.id),
                    onClick = { instance.add(restaurant, dish.id) },
                    modifier = Modifier.width(156.dp).testTag("$TAG:hit-${dish.id}"),
                )
            }
        }
        SectionHeader("Меню", subtitle = "${restaurant.menu.size} блюд")
        CardColumn(Modifier.padding(vertical = 6.dp)) {
            restaurant.menu.forEachIndexed { i, dish ->
                if (i > 0) Divider()
                DishRow(dish, restaurant, instance, quantity = qty(dish.id))
            }
        }
    }
}

/** Time, delivery and the minimum as three figures side by side, each with its icon. */
@Composable
private fun FactsCard(restaurant: Restaurant) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 8.dp)
            .cardSurface()
            .padding(vertical = 14.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FactCell(Glyph.Timer, restaurant.eta, "в пути", Modifier.weight(1f))
        VerticalLine()
        FactCell(Glyph.Bike, formatVnd(restaurant.deliveryFee), "доставка", Modifier.weight(1f))
        VerticalLine()
        FactCell(Glyph.Basket, formatVnd(restaurant.minOrder), "мин. заказ", Modifier.weight(1f))
    }
}

@Composable
private fun FactCell(glyph: Glyph, value: String, label: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        IconTile(glyph, size = 34.dp)
        Spacer(Modifier.height(6.dp))
        Text(value, style = Touch.type.caption.copy(fontWeight = FontWeight.ExtraBold), color = Touch.colors.ink, maxLines = 1)
        Text(label, style = Touch.type.micro.copy(fontWeight = FontWeight.Normal), color = Touch.colors.muted, maxLines = 1)
    }
}

@Composable
private fun VerticalLine() {
    Box(Modifier.width(1.dp).fillMaxHeight().padding(vertical = 6.dp).background(Touch.colors.line))
}

/** A hit: the picture, the name, the price and a "+" — one tap puts it in the basket. */
@Composable
private fun HitCard(dish: Dish, restaurant: Restaurant, quantity: Int, onClick: () -> Unit, modifier: Modifier) {
    val c = Touch.colors
    val accent = Touch.accent
    Column(modifier.padding(vertical = 6.dp).cardSurface().pressable(onClick = onClick)) {
        Art(restaurant.cuisine.glyph, dish.id, Modifier.fillMaxWidth().height(104.dp), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
            if (quantity > 0) {
                Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.TopStart) {
                    ImagePill("× $quantity", glyph = Glyph.Cart)
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 12.dp)) {
            Text(dish.name, style = Touch.type.strong, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Price(formatVnd(dish.price), Modifier.weight(1f))
                Box(Modifier.size(30.dp).clip(CircleShape).background(accent.gradient), contentAlignment = Alignment.Center) {
                    GlyphIcon(Glyph.Plus, accent.onGradient, size = 16.dp)
                }
            }
        }
    }
}

@Composable
private fun DishRow(dish: Dish, restaurant: Restaurant, instance: FoodInstance, quantity: Int) {
    val c = Touch.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Art(restaurant.cuisine.glyph, dish.id, Modifier.size(80.dp), shape = RoundedCornerShape(16.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(dish.name, style = Touch.type.strong, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (dish.description.isNotBlank()) {
                Text(dish.description, style = Touch.type.caption, color = c.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Price(formatVnd(dish.price))
                    val dishBonus = restaurant.bonus?.earnFor(dish.price) ?: 0
                    if (instance.bonus != null) bonusBadge(dishBonus)?.let { BonusBadge(it, Modifier.padding(top = 4.dp)) }
                }
                Spacer(Modifier.width(8.dp))
                Stepper(
                    quantity = quantity,
                    onPlus = { instance.add(restaurant, dish.id) },
                    onMinus = { instance.remove(dish.id) },
                    tag = "$TAG:dish-${dish.id}",
                )
            }
        }
    }
}

// ------------------------------------------------------------------ the basket

@Composable
private fun BasketPage(instance: FoodInstance) {
    val cart by instance.cart.collectAsState()
    val restaurant = Catalog.restaurant(cart.restaurantId)
    if (restaurant == null || cart.isEmpty) {
        PortalPage {
            Spacer(Modifier.height(24.dp))
            EmptyState(Glyph.Cart, "Корзина пуста", "Загляните в меню и добавьте что-нибудь вкусное", Modifier.testTag("$TAG:basket-empty"))
            PrimaryButton("К ресторанам", { instance.toRoot() }, glyph = Glyph.Cutlery)
        }
        return
    }
    val short = restaurant.minOrder - cart.subtotal(restaurant)
    val deal = instance.deal(restaurant, cart, spending = false)
    PortalPage(
        bottomBar = {
            BarButton(
                if (short > 0) "Ещё ${formatVnd(short)} до минимума" else "Оформить заказ",
                instance::checkout,
                Modifier.testTag("$TAG:checkout"),
                trailing = if (short > 0) null else formatVnd(deal.pay),
                enabled = short <= 0,
            )
        },
    ) {
        MiniHeader(restaurant.name, "${restaurant.cuisine.label} · ${restaurant.eta}", restaurant.cuisine.glyph, restaurant.id, Modifier.padding(top = 4.dp))
        SectionHeader(
            "Ваш заказ",
            subtitle = "${cart.count} поз. на ${formatVnd(cart.subtotal(restaurant))}",
            action = "Добавить ещё",
            onAction = { instance.toRoot(); instance.openRestaurant(restaurant.id) },
        )
        CardColumn(Modifier.padding(top = 6.dp, bottom = 2.dp)) {
            cart.lines(restaurant).forEachIndexed { i, (dish, qty) ->
                if (i > 0) Divider()
                BasketLine(dish, qty, restaurant, instance)
            }
            Divider()
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)) {
                TextAction("Очистить корзину", { instance.clearCart(); instance.back() }, Modifier.testTag("$TAG:clear"), glyph = Glyph.Trash, destructive = true)
            }
        }
        if (short > 0) MinOrderCard(restaurant, cart.subtotal(restaurant), short)
        Spacer(Modifier.height(10.dp))
        Totals(cart, restaurant, deal, instance)
    }
}

@Composable
private fun BasketLine(dish: Dish, qty: Int, restaurant: Restaurant, instance: FoodInstance) {
    val c = Touch.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Art(restaurant.cuisine.glyph, dish.id, Modifier.size(56.dp), shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(dish.name, style = Touch.type.strong, color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Price(formatVnd(dish.price * qty))
                if (qty > 1) Text("  ${qty} × ${formatVnd(dish.price)}", style = Touch.type.micro.copy(fontWeight = FontWeight.Normal), color = c.muted, maxLines = 1)
            }
        }
        Spacer(Modifier.width(8.dp))
        Stepper(qty, { instance.add(restaurant, dish.id) }, { instance.remove(dish.id) }, "$TAG:line-${dish.id}")
    }
}

/** Below the restaurant's minimum: how much is left, as words and as a bar filling up. */
@Composable
private fun MinOrderCard(restaurant: Restaurant, subtotal: Long, short: Long) {
    val accent = Touch.accent
    val c = Touch.colors
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(accent.soft)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlyphIcon(Glyph.Basket, accent.fg, size = 18.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                "Минимальный заказ ${formatVnd(restaurant.minOrder)} — добавьте ещё на ${formatVnd(short)}.",
                style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold),
                color = c.ink,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        val part = (subtotal.toFloat() / restaurant.minOrder).coerceIn(0.04f, 1f)
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(c.card)) {
            Box(Modifier.fillMaxWidth(part).fillMaxHeight().clip(RoundedCornerShape(50)).background(accent.gradient))
        }
    }
}

/** The sums on a card: dishes, delivery, the bonus lines and the total, large. */
@Composable
private fun Totals(cart: Cart, restaurant: Restaurant, deal: BonusDeal, instance: FoodInstance) {
    CardColumn(Modifier.padding(vertical = 10.dp)) {
        SummaryRow("Блюда", formatVnd(cart.subtotal(restaurant)))
        SummaryRow("Доставка", formatVnd(restaurant.deliveryFee))
        if (deal.spend > 0) {
            SummaryRow("Сумма заказа", formatVnd(cart.total(restaurant)))
        }
        BonusTotals(instance.bonus, deal, TAG)
        Spacer(Modifier.height(6.dp))
        Divider()
        Spacer(Modifier.height(4.dp))
        SummaryRow(if (deal.spend > 0) "К оплате" else "Итого", formatVnd(deal.pay), strong = true, valueModifier = Modifier.testTag("$TAG:total"))
    }
}

// ------------------------------------------------------------------ checkout

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
        MiniHeader(restaurant.name, "${cart.count} поз. · ${restaurant.eta}", restaurant.cuisine.glyph, restaurant.id, Modifier.padding(top = 4.dp))
        SectionHeader("Куда доставить", subtitle = "Адрес запомним для следующих заказов")
        CardColumn(Modifier.padding(14.dp), spaced = true) {
            Field(delivery.name, { v -> instance.editDelivery { it.copy(name = v) } }, "Имя", Modifier.testTag("$TAG:name"), leading = Glyph.Person)
            Field(delivery.phone, { v -> instance.editDelivery { it.copy(phone = v) } }, "Телефон / WhatsApp", Modifier.testTag("$TAG:phone"), leading = Glyph.Phone)
            Field(delivery.address, { v -> instance.editDelivery { it.copy(address = v) } }, "Адрес, отель, номер комнаты", Modifier.testTag("$TAG:address"), singleLine = false, leading = Glyph.Pin)
            Field(delivery.comment, { v -> instance.editDelivery { it.copy(comment = v) } }, "Комментарий (необязательно)", Modifier.testTag("$TAG:comment"), leading = Glyph.Chat)
        }
        SectionHeader("Оплата", subtitle = "Курьеру наличными или переводом")
        BonusSpendRow(instance.bonus, restaurant.bonus, cart.total(restaurant), useBonus, instance::useBonus, TAG)
        Totals(cart, restaurant, deal, instance)
        SectionHeader("Отправить заказ", subtitle = "Ресторан получит его готовым сообщением")
        if (ready) {
            ContactButtons(
                contacts = restaurant.contacts,
                tagPrefix = TAG,
                message = orderMessage(restaurant, cart, delivery, deal),
                onOpened = { instance.sent() },
            )
        } else {
            FillIn(Modifier.testTag("$TAG:fill-in"))
        }
        Notice("Ресторан подтвердит заказ и время доставки в мессенджере.", Modifier.padding(top = 6.dp), glyph = Glyph.Truck)
    }
}

/** What is still missing before the order can go: a dashed-looking placeholder where the buttons will be. */
@Composable
private fun FillIn(modifier: Modifier = Modifier) {
    val c = Touch.colors
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(c.field)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphIcon(Glyph.Info, c.muted, size = 18.dp)
        Spacer(Modifier.width(10.dp))
        Text("Заполните имя, телефон и адрес", style = Touch.type.body.copy(fontWeight = FontWeight.Medium), color = c.muted)
    }
}

// ------------------------------------------------------------------ sent

@Composable
private fun SentPage(instance: FoodInstance) {
    PortalPage {
        Spacer(Modifier.height(12.dp))
        DoneState(
            "Спасибо за заказ!",
            "Сообщение с заказом открылось в мессенджере — отправьте его, и ресторан подтвердит время доставки.",
            Modifier.testTag("$TAG:sent"),
        )
        val (earned, spent) = instance.lastBonus
        BonusResult(instance.bonus, earned, spent, instance::openBonus, TAG)
        SectionHeader("Что дальше")
        CardColumn(Modifier.padding(vertical = 6.dp)) {
            NextStep(1, Glyph.Chat, "Отправьте сообщение", "Оно уже написано — осталось нажать «Отправить»")
            Divider()
            NextStep(2, Glyph.Check, "Ресторан подтвердит", "Ответит в мессенджере и назовёт время")
            Divider()
            NextStep(3, Glyph.Bike, "Курьер привезёт", "Оплата при получении — наличными или переводом")
        }
        Spacer(Modifier.height(10.dp))
        PrimaryButton("К ресторанам", { instance.toRoot() }, Modifier.testTag("$TAG:done"))
    }
}

@Composable
private fun NextStep(n: Int, glyph: Glyph, title: String, text: String) {
    val c = Touch.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(glyph, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("$n. $title", style = Touch.type.strong, color = c.ink)
            Text(text, style = Touch.type.caption, color = c.muted)
        }
    }
}

// ------------------------------------------------------------------ parts

/** A card on the canvas holding a group: lines of a basket, the menu, the sums, a form. */
@Composable
private fun CardColumn(inner: Modifier = Modifier, spaced: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .cardSurface()
            .then(inner),
        verticalArrangement = if (spaced) Arrangement.spacedBy(10.dp) else Arrangement.Top,
        content = content,
    )
}
