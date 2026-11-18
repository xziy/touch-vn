package vn.touch.apps.exchange

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cx.m42.superizer.kit.Text
import cx.m42.superizer.ui.platform.SystemBackHandler
import vn.touch.kit.Badge
import vn.touch.kit.BonusBadge
import vn.touch.kit.BonusBalanceChip
import vn.touch.kit.BonusOfferCard
import vn.touch.kit.ContactButtons
import vn.touch.kit.EmptyState
import vn.touch.kit.Glyph
import vn.touch.kit.GlyphIcon
import vn.touch.kit.Gutter
import vn.touch.kit.IconTile
import vn.touch.kit.InfoRow
import vn.touch.kit.Notice
import vn.touch.kit.PortalPage
import vn.touch.kit.SectionHeader
import vn.touch.kit.ServiceHeader
import vn.touch.kit.Touch
import vn.touch.kit.bonusBadge
import vn.touch.kit.cardSurface
import vn.touch.kit.formatVnd
import vn.touch.kit.pressable

private const val TAG = ExchangeApp.ID

@Composable
internal fun ExchangeScreen(instance: ExchangeInstance) {
    val pages by instance.pages.collectAsState()
    SystemBackHandler(enabled = pages.size > 1) { instance.back() }
    when (val page = pages.last()) {
        ExchangePage.List -> ListPage(instance)
        is ExchangePage.Detail -> DetailPage(instance, page.id)
    }
}

/** The most dong any point pays for one [currency]; null when nobody buys it. */
private fun bestRate(currency: Currency): Double? = Catalog.points.mapNotNull { it.rates[currency] }.maxOrNull()

// ------------------------------------------------------------------ converter and points

@Composable
private fun ListPage(instance: ExchangeInstance) {
    val currency by instance.currency.collectAsState()
    val amountText by instance.amount
    val amount = parseAmount(amountText)
    val list = quotes(Catalog.points, currency, amount ?: 1.0)
    PortalPage {
        ServiceHeader(
            title = "Курс лучше банка",
            glyph = Glyph.Swap,
            eyebrow = "Обмен валюты · Нячанг",
        ) {
            Converter(
                currency = currency,
                amountText = amountText,
                amount = amount,
                best = list.firstOrNull(),
                onAmount = instance::setAmount,
                onCurrency = instance::select,
            )
        }
        BonusBalanceChip(instance.bonus, instance::openBonus, TAG, Modifier.padding(top = 8.dp))
        SectionHeader("Где поменять", subtitle = "Сначала лучший курс · ${list.size} ${pointWord(list.size)}")
        if (list.isEmpty()) EmptyState(currency.glyph, "Никто не покупает ${currency.code}", "Попробуйте другую валюту")
        val top = list.firstOrNull()
        list.forEachIndexed { i, q ->
            PointCard(
                q = q,
                currency = currency,
                amount = amount,
                best = i == 0,
                behind = if (i > 0 && amount != null && top != null) top.vnd - q.vnd else 0,
                earn = instance.bonus?.let { bonusBadge(q.point.bonus?.earnFor(null) ?: 0) },
                onClick = { instance.open(q.point.id) },
                modifier = Modifier.testTag("$TAG:point-${q.point.id}"),
            )
        }
        Spacer(Modifier.height(6.dp))
        Notice("Курсы обновлены: ${Catalog.UPDATED}. Уточняйте курс перед обменом; пересчитывайте деньги на месте.", glyph = Glyph.Clock)
    }
}

private fun pointWord(n: Int): String {
    val tens = n % 100
    return when {
        tens in 11..14 -> "пунктов"
        n % 10 == 1 -> "пункт"
        n % 10 in 2..4 -> "пункта"
        else -> "пунктов"
    }
}

/**
 * The converter, as the money apps draw it: what you give, large, with the currency beside it; the
 * currencies as a segmented switch; and what you get, in dong, at the best rate in town.
 * Tags `exchange:amount` (the editable node) and `exchange:chip-<CUR>`.
 */
@Composable
private fun Converter(
    currency: Currency,
    amountText: String,
    amount: Double?,
    best: Quote?,
    onAmount: (String) -> Unit,
    onCurrency: (Currency) -> Unit,
) {
    val c = Touch.colors
    val accent = Touch.accent
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .cardSurface(RoundedCornerShape(24.dp), elevation = 16.dp)
            .padding(18.dp),
    ) {
        Text("Вы отдаёте", style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold), color = c.muted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            val style = Touch.type.display.copy(fontSize = 34.sp, lineHeight = 42.sp, color = c.ink)
            Box(Modifier.weight(1f).padding(vertical = 4.dp)) {
                if (amountText.isEmpty()) Text("0", style = style, color = c.muted)
                BasicTextField(
                    value = amountText,
                    onValueChange = onAmount,
                    singleLine = true,
                    textStyle = style,
                    cursorBrush = SolidColor(accent.fg),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("$TAG:amount").semantics { contentDescription = "Сумма, ${currency.code}" },
                )
            }
            Spacer(Modifier.width(10.dp))
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(c.field).padding(start = 5.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(accent.gradient), contentAlignment = Alignment.Center) {
                    GlyphIcon(currency.glyph, accent.onGradient, size = 16.dp)
                }
                Spacer(Modifier.width(7.dp))
                Text(currency.code, style = Touch.type.strong, color = c.ink)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.field).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Currency.entries.forEach { cur ->
                val on = cur == currency
                val shape = RoundedCornerShape(11.dp)
                Row(
                    Modifier
                        .weight(1f)
                        .testTag("$TAG:chip-${cur.name}")
                        .then(if (on) Modifier.cardSurface(shape, elevation = 3.dp) else Modifier.clip(shape))
                        .pressable(role = Role.Tab) { onCurrency(cur) }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlyphIcon(cur.glyph, if (on) accent.fg else c.muted, size = 15.dp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        cur.code,
                        style = Touch.type.caption.copy(fontWeight = if (on) FontWeight.ExtraBold else FontWeight.SemiBold),
                        color = if (on) c.ink else c.muted,
                        maxLines = 1,
                    )
                }
            }
        }

        // The seam between "give" and "get", with the swap mark on it.
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).height(1.dp).background(c.line))
            Box(Modifier.padding(horizontal = 10.dp).size(32.dp).clip(CircleShape).background(accent.soft), contentAlignment = Alignment.Center) {
                GlyphIcon(Glyph.Swap, accent.fg, size = 16.dp)
            }
            Box(Modifier.weight(1f).height(1.dp).background(c.line))
        }

        Text("Вы получите", style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold), color = c.muted)
        Spacer(Modifier.height(2.dp))
        when {
            best == null -> Text("Никто не покупает ${currency.code}", style = Touch.type.headline, color = c.muted)
            amount == null -> Text("— ₫", style = Touch.type.display, color = c.muted)
            else -> Text(formatVnd(best.vnd), style = Touch.type.display, color = accent.fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (best != null) {
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(accent.soft).padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlyphIcon(Glyph.Sparkle, accent.fg, size = 16.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "1 ${currency.code} = ${formatRate(best.rate)} ₫",
                    style = Touch.type.caption.copy(fontWeight = FontWeight.Bold),
                    color = accent.onSoft,
                )
                Text(
                    " · ${best.point.name}",
                    style = Touch.type.caption,
                    color = accent.onSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * A point in the list: who it is, what you get for the amount, its rate. The best one wears the
 * service's colour; the others say how much less they give.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PointCard(
    q: Quote,
    currency: Currency,
    amount: Double?,
    best: Boolean,
    behind: Long,
    earn: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Touch.colors
    val accent = Touch.accent
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .cardSurface(shape)
            .then(if (best) Modifier.border(2.dp, accent.fg, shape) else Modifier)
            .pressable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(q.point.type.glyph, size = 44.dp, filled = best)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(q.point.name, style = Touch.type.strong, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${q.point.type.label} · ${q.point.hours}", style = Touch.type.caption, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                if (amount != null) {
                    Text("Вы получите", style = Touch.type.micro.copy(fontWeight = FontWeight.Normal), color = c.muted)
                    Text(formatVnd(q.vnd), style = Touch.type.headline.copy(fontWeight = FontWeight.ExtraBold), color = if (best) accent.fg else c.ink)
                } else {
                    Text("Курс за 1 ${currency.code}", style = Touch.type.micro.copy(fontWeight = FontWeight.Normal), color = c.muted)
                    Text("${formatRate(q.rate)} ₫", style = Touch.type.headline.copy(fontWeight = FontWeight.ExtraBold), color = c.ink)
                }
            }
            if (amount != null) Column(horizontalAlignment = Alignment.End) {
                Text("1 ${currency.code}", style = Touch.type.micro.copy(fontWeight = FontWeight.Normal), color = c.muted)
                Text("${formatRate(q.rate)} ₫", style = Touch.type.body.copy(fontWeight = FontWeight.Bold), color = c.ink)
            }
        }
        val behindLine = behind > 0
        if (best || behindLine || q.point.delivery || earn != null) {
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (best) Badge("Лучший курс", glyph = Glyph.Sparkle)
                if (behindLine) Tag("на ${formatVnd(behind)} меньше", c.danger, c.dangerSoft)
                if (q.point.delivery) Tag("Доставка", c.ink, c.field, Glyph.Truck)
                if (earn != null) BonusBadge(earn)
            }
        }
    }
}

/** A small pill in a given ink on a given ground: "Доставка", "на 3 000 ₫ меньше". */
@Composable
private fun Tag(text: String, ink: androidx.compose.ui.graphics.Color, ground: androidx.compose.ui.graphics.Color, glyph: Glyph? = null) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(ground).padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null) {
            GlyphIcon(glyph, ink, size = 13.dp)
            Spacer(Modifier.width(4.dp))
        }
        Text(text, style = Touch.type.micro, color = ink, maxLines = 1)
    }
}

// ------------------------------------------------------------------ point

@Composable
private fun DetailPage(instance: ExchangeInstance, id: String) {
    val p = Catalog.point(id) ?: return
    val currency by instance.currency.collectAsState()
    val amountText by instance.amount
    val amount = parseAmount(amountText)
    val c = Touch.colors
    PortalPage {
        PointHero(p, currency, amount)
        BonusOfferCard(instance.bonus, p.bonus, earn = p.bonus?.earnFor(null) ?: 0, onClick = instance::openBonus, tagPrefix = TAG, earnWhat = "за обмен через портал", modifier = Modifier.padding(top = 4.dp))

        SectionHeader("Курсы покупки", subtitle = "Сколько донгов дают за 1 единицу")
        Column(Modifier.fillMaxWidth().padding(horizontal = Gutter).cardSurface().padding(vertical = 6.dp)) {
            Currency.entries.forEachIndexed { i, cur ->
                if (i > 0) Box(Modifier.fillMaxWidth().padding(start = 70.dp, end = 16.dp).height(1.dp).background(c.line))
                RateRow(cur, p.rates[cur], selected = cur == currency, topInTown = p.rates[cur] != null && p.rates[cur] == bestRate(cur))
            }
        }

        SectionHeader("О пункте")
        Column(Modifier.fillMaxWidth().padding(horizontal = Gutter).cardSurface().padding(vertical = 8.dp)) {
            InfoRow("Адрес", p.address, glyph = Glyph.Pin)
            InfoRow("Часы работы", p.hours, glyph = Glyph.Clock)
            InfoRow("Доставка денег", if (p.delivery) "Есть, курьер привезёт донги" else "Нет, только на месте", glyph = Glyph.Truck)
        }
        Spacer(Modifier.height(6.dp))
        Notice(p.note)
        SectionHeader("Связаться", subtitle = "Сообщение с суммой уже написано")
        ContactButtons(p.contacts, TAG, message = exchangeMessage(p, currency, amount), onOpened = { instance.contacted(p) })
    }
}

/** The point's card, in the service's gradient: who it is, and what it gives for your amount. */
@Composable
private fun PointHero(p: Point, currency: Currency, amount: Double?) {
    val c = Touch.colors
    val accent = Touch.accent
    val rate = p.rates[currency]
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = Gutter, end = Gutter, top = 10.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(accent.gradient)
            .drawBehind {
                drawCircle(c.onArt.copy(alpha = 0.10f), radius = size.height * 0.75f, center = Offset(size.width * 1.0f, 0f))
                drawCircle(c.onArt.copy(alpha = 0.07f), radius = size.height * 0.4f, center = Offset(size.width * 0.8f, size.height * 1.1f))
            }
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(c.onArt.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                GlyphIcon(p.type.glyph, c.onArt, size = 24.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, style = Touch.type.headline, color = c.onArt, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(p.address, style = Touch.type.caption, color = c.onArt.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(20.dp))
        if (rate != null) {
            Text(
                if (amount != null) "За ${formatAmount(amount)} ${currency.code} вы получите" else "1 ${currency.code}",
                style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold),
                color = c.onArt.copy(alpha = 0.85f),
            )
            Text(formatVnd(if (amount != null) kotlin.math.round(rate * amount).toLong() else rate.toLong()), style = Touch.type.display, color = c.onArt, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text("Курс: 1 ${currency.code} = ${formatRate(rate)} ₫", style = Touch.type.caption, color = c.onArt.copy(alpha = 0.85f))
        } else {
            Text("${currency.code} здесь не покупают", style = Touch.type.title, color = c.onArt)
            Text("Смотрите курсы других валют ниже", style = Touch.type.caption, color = c.onArt.copy(alpha = 0.85f))
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeroPill(Glyph.Clock, p.hours)
            if (p.delivery) HeroPill(Glyph.Truck, "Доставка")
        }
    }
}

@Composable
private fun HeroPill(glyph: Glyph, text: String) {
    val c = Touch.colors
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(c.onArt.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphIcon(glyph, c.onArt, size = 13.dp)
        Spacer(Modifier.width(5.dp))
        Text(text, style = Touch.type.micro, color = c.onArt, maxLines = 1)
    }
}

/** A line of the rates table: the currency, its rate here, and a mark where it is the town's best. */
@Composable
private fun RateRow(cur: Currency, rate: Double?, selected: Boolean, topInTown: Boolean) {
    val c = Touch.colors
    val accent = Touch.accent
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(cur.glyph, size = 42.dp, filled = selected)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(cur.code, style = Touch.type.strong, color = c.ink)
            Text(cur.label, style = Touch.type.caption, color = c.muted)
        }
        Column(horizontalAlignment = Alignment.End) {
            if (rate != null) {
                Text("${formatRate(rate)} ₫", style = Touch.type.price, color = if (topInTown) accent.fg else c.ink)
                if (topInTown) Text("лучший в городе", style = Touch.type.micro, color = accent.fg)
            } else {
                Text("не покупают", style = Touch.type.caption, color = c.muted)
            }
        }
    }
}
