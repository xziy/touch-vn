package vn.touch.apps.flowers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cx.m42.superizer.kit.Text
import cx.m42.superizer.ui.platform.SystemBackHandler
import vn.touch.kit.Art
import vn.touch.kit.BarButton
import vn.touch.kit.BonusBadge
import vn.touch.kit.BonusBalanceChip
import vn.touch.kit.BonusOfferCard
import vn.touch.kit.BonusResult
import vn.touch.kit.BonusSpendRow
import vn.touch.kit.BonusTotals
import vn.touch.kit.Carousel
import vn.touch.kit.CategoryRow
import vn.touch.kit.ContactButtons
import vn.touch.kit.DoneState
import vn.touch.kit.EmptyState
import vn.touch.kit.Field
import vn.touch.kit.Glyph
import vn.touch.kit.GlyphIcon
import vn.touch.kit.Gutter
import vn.touch.kit.IconTile
import vn.touch.kit.ImagePill
import vn.touch.kit.MiniHeader
import vn.touch.kit.Notice
import vn.touch.kit.PortalPage
import vn.touch.kit.PrimaryButton
import vn.touch.kit.SearchField
import vn.touch.kit.SectionHeader
import vn.touch.kit.ServiceHeader
import vn.touch.kit.SummaryRow
import vn.touch.kit.Tile
import vn.touch.kit.Touch
import vn.touch.kit.bonusBadge
import vn.touch.kit.cardSurface
import vn.touch.kit.formatVnd
import vn.touch.kit.pressable

private const val TAG = FlowersApp.ID

@Composable
internal fun FlowersScreen(instance: FlowersInstance) {
    val pages by instance.pages.collectAsState()
    SystemBackHandler(enabled = pages.size > 1) { instance.back() }
    when (val page = pages.last()) {
        FlowersPage.List -> ListPage(instance)
        is FlowersPage.Detail -> DetailPage(instance, page.id)
        is FlowersPage.Order -> OrderPage(instance, page.id)
        FlowersPage.Sent -> SentPage(instance)
    }
}

// ------------------------------------------------------------------ front page

@Composable
private fun ListPage(instance: FlowersInstance) {
    val kind by instance.kind.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    val list = Catalog.bouquets.filter { b ->
        (kind == null || b.kind == kind) &&
            (q.isEmpty() || listOf(b.name, b.composition, b.shop.name, b.kind.label).any { it.contains(q, ignoreCase = true) })
    }
    val browsing = kind == null && q.isEmpty()
    // While browsing, the hits stand in their own strip and the window below holds the rest;
    // each bouquet is drawn — and tagged `flowers:bouquet-<id>` — once.
    val hits = if (browsing) Catalog.bouquets.filter { it.hit } else emptyList()
    val window = list - hits.toSet()
    PortalPage {
        ServiceHeader(
            title = "Букет, который запомнят",
            glyph = Glyph.Flower,
            eyebrow = "Доставка цветов · Нячанг",
            subtitle = "Привезём сегодня, если заказать до 18:00",
        ) {
            SearchField(query, { query = it }, "Розы, пионы, корзина…", Modifier.testTag("$TAG:search"))
        }
        BonusBalanceChip(instance.bonus, instance::openBonus, TAG, Modifier.padding(top = 8.dp))
        CategoryRow(Kind.entries.map { Tile(it.name, it.short, it.glyph) }, kind?.name, { k -> instance.filter(k?.let(Kind::valueOf)) }, TAG)

        if (browsing) {
            SectionHeader("Хиты недели", subtitle = "Их чаще всего дарят в Нячанге")
            Carousel {
                hits.forEach { b ->
                    HitCard(b, earnOf(instance, b), onClick = { instance.open(b.id) }, modifier = Modifier.testTag("$TAG:bouquet-${b.id}"))
                }
            }
        }

        SectionHeader(
            title = kind?.label ?: if (browsing) "Ещё букеты" else "Найдено",
            subtitle = "${window.size} ${bouquetWord(window.size)} · доставка сегодня",
        )
        if (list.isEmpty()) {
            EmptyState(Glyph.Flower, "Ничего не нашлось", "Попробуйте «розы», «коробка» или «лотос»")
        }
        // Two columns, as a shop window: the picture sells a bouquet, not its row.
        Column(Modifier.fillMaxWidth().padding(horizontal = Gutter), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            window.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { b ->
                        BouquetCard(
                            b,
                            earnOf(instance, b),
                            onClick = { instance.open(b.id) },
                            modifier = Modifier.weight(1f).testTag("$TAG:bouquet-${b.id}"),
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Notice("Демо-каталог: заказ уходит магазину сообщением в мессенджер, магазин присылает фото букета.")
    }
}

private fun earnOf(instance: FlowersInstance, b: Bouquet): String? =
    instance.bonus?.let { bonusBadge(b.shop.bonus?.earnFor(b.price) ?: 0) }

private fun bouquetWord(n: Int): String {
    val tens = n % 100
    return when {
        tens in 11..14 -> "букетов"
        n % 10 == 1 -> "букет"
        n % 10 in 2..4 -> "букета"
        else -> "букетов"
    }
}

/**
 * The kit's [Art] picks its gradient from the seed's hash, one of seven in the kit's order
 * (papaya, dragon fruit, mango, pandan, bay, orchid, lagoon). A florist's window wants the warm,
 * flowery ones, so each bouquet names its gradient and we find a seed that lands on it.
 */
private val bouquetPalette: Map<String, Int> = mapOf(
    "red-25" to 1, "white-15" to 5, "mix-101" to 0, "spring" to 3, "sunny" to 2,
    "box-pink" to 1, "box-macaron" to 5, "basket-fruit" to 0, "orchid" to 5, "lotus-bunch" to 1,
)

@Composable
private fun bouquetSeed(b: Bouquet): String = remember(b.id) {
    val palette = bouquetPalette[b.id] ?: return@remember b.id
    generateSequence(0) { it + 1 }.map { "${b.id}#$it" }.first { (it.hashCode() and 0x7fffffff) % 7 == palette }
}

/** "Хит" on a picture: the flame in the service's colour. */
@Composable
private fun HitPill(modifier: Modifier = Modifier, text: String = "Хит") {
    ImagePill(text, modifier, glyph = Glyph.Flame)
}

/** A wide card of the "hits" strip: big picture, name, what is in it, the price. */
@Composable
private fun HitCard(b: Bouquet, earn: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Touch.colors
    Column(modifier.width(272.dp).padding(vertical = 6.dp).cardSurface().pressable(onClick = onClick)) {
        Art(
            b.kind.art,
            bouquetSeed(b),
            Modifier.fillMaxWidth().padding(6.dp).height(150.dp),
            shape = RoundedCornerShape(16.dp),
            glyphScale = 0.5f,
        ) {
            Box(Modifier.fillMaxSize().padding(10.dp)) {
                HitPill(Modifier.align(Alignment.TopStart))
                if (earn != null) BonusBadge(earn, Modifier.align(Alignment.BottomStart))
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 14.dp)) {
            Text(b.name, style = Touch.type.strong, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(b.composition, style = Touch.type.caption, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatVnd(b.price), style = Touch.type.price, color = c.ink, modifier = Modifier.weight(1f))
                Text(b.shop.name, style = Touch.type.micro, color = c.muted, maxLines = 1)
            }
        }
    }
}

/**
 * A tile of the shop window. Everything that is not on every bouquet — the hit, the bonus — sits
 * on the picture, so the two cards of a row always end at the same height.
 */
@Composable
private fun BouquetCard(b: Bouquet, earn: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Touch.colors
    val accent = Touch.accent
    Column(modifier.cardSurface().pressable(onClick = onClick)) {
        Art(
            b.kind.art,
            bouquetSeed(b),
            Modifier.fillMaxWidth().padding(6.dp).height(150.dp),
            shape = RoundedCornerShape(16.dp),
            glyphScale = 0.46f,
        ) {
            Box(Modifier.fillMaxSize().padding(8.dp)) {
                if (b.hit) HitPill(Modifier.align(Alignment.TopStart))
                if (earn != null) BonusBadge(earn, Modifier.align(Alignment.BottomStart))
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 10.dp, top = 4.dp, bottom = 12.dp)) {
            Text(b.shop.name, style = Touch.type.micro.copy(fontWeight = FontWeight.Medium), color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Box(Modifier.heightIn(min = 44.dp)) {
                Text(b.name, style = Touch.type.strong, color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatVnd(b.price), style = Touch.type.price, color = c.ink, modifier = Modifier.weight(1f), maxLines = 1)
                Box(Modifier.size(32.dp).clip(CircleShape).background(accent.soft), contentAlignment = Alignment.Center) {
                    GlyphIcon(Glyph.Plus, accent.fg, size = 18.dp)
                }
            }
        }
    }
}

// ------------------------------------------------------------------ bouquet

@Composable
private fun DetailPage(instance: FlowersInstance, id: String) {
    val b = Catalog.bouquet(id) ?: return
    val c = Touch.colors
    val accent = Touch.accent
    PortalPage(
        bottomBar = {
            BarButton("Заказать доставку", { instance.startOrder(b.id) }, Modifier.testTag("$TAG:order"), trailing = formatVnd(b.price), glyph = Glyph.Truck)
        },
    ) {
        Art(
            b.kind.art,
            bouquetSeed(b),
            Modifier.fillMaxWidth().padding(start = Gutter, end = Gutter, top = 10.dp).height(260.dp),
            shape = RoundedCornerShape(28.dp),
            glyphScale = 0.46f,
        ) {
            Box(Modifier.fillMaxSize().padding(14.dp)) {
                if (b.hit) HitPill(Modifier.align(Alignment.TopStart), "Хит продаж")
                ImagePill(b.shop.name, Modifier.align(Alignment.BottomStart), glyph = Glyph.Verified)
            }
        }

        Column(Modifier.fillMaxWidth().padding(start = Gutter + 4.dp, end = Gutter, top = 18.dp, bottom = 4.dp)) {
            Text(b.kind.label.uppercase(), style = Touch.type.micro.copy(fontWeight = FontWeight.Bold), color = accent.fg)
            Spacer(Modifier.height(4.dp))
            Text(b.name, style = Touch.type.title, color = c.ink)
            Spacer(Modifier.height(4.dp))
            Text(b.composition, style = Touch.type.body, color = c.muted)
            Spacer(Modifier.height(12.dp))
            Text(formatVnd(b.price), style = Touch.type.title, color = accent.fg)
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 10.dp).cardSurface().padding(vertical = 14.dp),
        ) {
            Fact(Glyph.Grid, b.size, "Размер", Modifier.weight(1f))
            Fact(Glyph.Truck, "Сегодня", "Доставка", Modifier.weight(1f))
            Fact(Glyph.Gift, "С текстом", "Открытка", Modifier.weight(1f))
        }

        BonusOfferCard(instance.bonus, b.shop.bonus, earn = b.shop.bonus?.earnFor(b.price) ?: 0, onClick = instance::openBonus, tagPrefix = TAG, earnWhat = "за этот букет")

        SectionHeader("Флорист")
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Gutter).cardSurface().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(Glyph.Flower, size = 48.dp, filled = true)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(b.shop.name, style = Touch.type.strong, color = c.ink)
                Text(b.shop.address, style = Touch.type.caption, color = c.muted)
            }
        }
        Spacer(Modifier.height(6.dp))
        Notice("Флорист соберёт букет в день доставки и пришлёт его фото перед отправкой.", glyph = Glyph.Image)
    }
}

/** One of the three facts under a bouquet's price: an icon, the value, what it is. */
@Composable
private fun Fact(glyph: Glyph, value: String, label: String, modifier: Modifier = Modifier) {
    val c = Touch.colors
    Column(modifier.padding(horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        IconTile(glyph, size = 40.dp)
        Spacer(Modifier.height(8.dp))
        Text(value, style = Touch.type.strong, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Text(label, style = Touch.type.micro.copy(fontWeight = FontWeight.Normal), color = c.muted, maxLines = 1, textAlign = TextAlign.Center)
    }
}

// ------------------------------------------------------------------ order

@Composable
private fun OrderPage(instance: FlowersInstance, id: String) {
    val b = Catalog.bouquet(id) ?: return
    val order by instance.order
    val ready = canSend(order)
    instance.bonus?.balance?.collectAsState()?.value
    val useBonus by instance.useBonus
    val deal = instance.deal(b, spending = useBonus)
    val partner = instance.bonus != null && b.shop.bonus != null
    // The way out stays in reach: the messengers once the form is complete, what is missing until then.
    PortalPage(
        bottomBar = {
            if (ready) {
                Column {
                    Text("Отправить заказ магазину", style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold), color = Touch.colors.muted, modifier = Modifier.padding(start = 2.dp, bottom = 4.dp))
                    ContactButtons(b.shop.contacts, TAG, Modifier.bleed(Gutter), message = orderMessage(b, order, deal), onOpened = { instance.sent(b.id) })
                }
            } else {
                FillIn(Modifier.testTag("$TAG:fill-in"))
            }
        },
    ) {
        MiniHeader(b.name, "${b.shop.name} · ${formatVnd(b.price)}", b.kind.art, bouquetSeed(b), Modifier.padding(top = 4.dp))

        FormCard("Кому", Glyph.Gift, "Получатель и адрес доставки") {
            Field(order.recipient, { v -> instance.edit { it.copy(recipient = v) } }, "Имя получателя", Modifier.testTag("$TAG:recipient"), leading = Glyph.Person)
            Field(order.recipientPhone, { v -> instance.edit { it.copy(recipientPhone = v) } }, "Телефон получателя (необязательно)", Modifier.testTag("$TAG:recipient-phone"), leading = Glyph.Phone)
            Field(order.address, { v -> instance.edit { it.copy(address = v) } }, "Адрес, отель, номер", Modifier.testTag("$TAG:address"), singleLine = false, leading = Glyph.Pin)
            Field(order.whenText, { v -> instance.edit { it.copy(whenText = v) } }, "Когда: например, 14 февраля к 10:00", Modifier.testTag("$TAG:when"), leading = Glyph.Calendar)
        }
        FormCard("Открытка", Glyph.Heart, "Напишем от руки и вложим в букет") {
            Field(order.card, { v -> instance.edit { it.copy(card = v) } }, "Текст открытки (необязательно)", Modifier.testTag("$TAG:card"), singleLine = false, minHeight = 84.dp)
        }
        FormCard("От кого", Glyph.Person, "Магазин свяжется с вами, если что-то уточнить") {
            Field(order.sender, { v -> instance.edit { it.copy(sender = v) } }, "Ваше имя", Modifier.testTag("$TAG:sender"), leading = Glyph.Person)
            Field(order.senderPhone, { v -> instance.edit { it.copy(senderPhone = v) } }, "Ваш телефон / WhatsApp", Modifier.testTag("$TAG:sender-phone"), leading = Glyph.Phone)
        }

        SectionHeader("Сумма")
        if (partner) BonusSpendRow(instance.bonus, b.shop.bonus, b.price, useBonus, instance::useBonus, TAG)
        Column(Modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 6.dp).cardSurface().padding(vertical = 10.dp)) {
            SummaryRow("Букет", formatVnd(b.price))
            if (partner) BonusTotals(instance.bonus, deal, TAG)
            Box(Modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 6.dp).height(1.dp).background(Touch.colors.line))
            SummaryRow("К оплате", formatVnd(if (partner) deal.pay else b.price), strong = true, valueModifier = Modifier.testTag("$TAG:total"))
        }

        Spacer(Modifier.height(6.dp))
        Notice("Заказ уходит магазину сообщением в мессенджере — флорист подтвердит его и пришлёт фото букета.", glyph = Glyph.Chat)
    }
}

/** Lets [ContactButtons], which keep the page's gutter, span the pinned bar that already keeps it. */
private fun Modifier.bleed(dx: Dp): Modifier = layout { measurable, constraints ->
    val extra = dx.roundToPx() * 2
    val placeable = measurable.measure(
        constraints.copy(minWidth = constraints.minWidth + extra, maxWidth = constraints.maxWidth + extra),
    )
    layout(placeable.width - extra, placeable.height) { placeable.place(-dx.roundToPx(), 0) }
}

/** A titled card of fields, with the section's icon on the service's soft ground. */
@Composable
private fun FormCard(title: String, glyph: Glyph, hint: String, content: @Composable ColumnScope.() -> Unit) {
    val c = Touch.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 6.dp).cardSurface().padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(glyph, size = 36.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = Touch.type.strong, color = c.ink)
                Text(hint, style = Touch.type.caption, color = c.muted)
            }
        }
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

/** What is still missing before the order can go: in place of the messenger buttons. */
@Composable
private fun FillIn(modifier: Modifier = Modifier) {
    val c = Touch.colors
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(c.field)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphIcon(Glyph.Info, c.muted, size = 18.dp)
        Spacer(Modifier.width(10.dp))
        Text("Заполните получателя, адрес, время и свои контакты", style = Touch.type.caption.copy(fontWeight = FontWeight.Medium), color = c.muted)
    }
}

// ------------------------------------------------------------------ sent

@Composable
private fun SentPage(instance: FlowersInstance) {
    PortalPage {
        Spacer(Modifier.height(12.dp))
        DoneState(
            "Заказ у флориста!",
            "Отправьте сообщение в мессенджере — магазин подтвердит доставку и пришлёт фото букета.",
            Modifier.testTag("$TAG:sent"),
        )
        val (earned, spent) = instance.lastBonus
        BonusResult(instance.bonus, earned, spent, instance::openBonus, TAG)
        SectionHeader("Что дальше")
        Column(Modifier.fillMaxWidth().padding(horizontal = Gutter).cardSurface().padding(vertical = 8.dp)) {
            Step(1, "Магазин подтвердит заказ", "Ответит в том же мессенджере")
            Step(2, "Пришлёт фото букета", "Перед тем как отдать курьеру")
            Step(3, "Курьер привезёт цветы", "В указанное время, с открыткой")
        }
        Spacer(Modifier.height(12.dp))
        PrimaryButton("К букетам", { instance.toRoot() }, Modifier.testTag("$TAG:done"), glyph = Glyph.Flower)
    }
}

@Composable
private fun Step(n: Int, title: String, text: String) {
    val c = Touch.colors
    val accent = Touch.accent
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(accent.soft), contentAlignment = Alignment.Center) {
            Text("$n", style = Touch.type.strong, color = accent.onSoft)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = Touch.type.body.copy(fontWeight = FontWeight.SemiBold), color = c.ink)
            Text(text, style = Touch.type.caption, color = c.muted)
        }
    }
}
