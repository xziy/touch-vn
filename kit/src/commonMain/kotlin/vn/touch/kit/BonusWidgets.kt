package vn.touch.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cx.m42.superizer.kit.Text
import cx.m42.superizer.runtime.LocalAppRuntime
import cx.m42.superizer.ui.platform.SystemBackHandler

// ------------------------------------------------------------------ the frame

/**
 * An app's whole screen: its pages, or the city bonus screen over them while it is open
 * ([PortalInstance.openBonus]). Without a bonus service (a host that has none, a unit test) it is
 * the pages alone.
 */
@Composable
public fun PortalScreen(instance: PortalInstance<*>, bonus: CityBonus?, content: @Composable () -> Unit) {
    val open by instance.bonusOpen.collectAsState()
    if (open && bonus != null) {
        val here = LocalAppRuntime.current.appId.value
        SystemBackHandler(enabled = true) { instance.back() }
        BonusPage(bonus, onOpenPlace = { id -> if (id == here) instance.toRoot() else bonus.open(id) }, current = here)
    } else {
        content()
    }
}

// ------------------------------------------------------------------ marks on cards

/** A gold coin in a disc: the programme's mark wherever a figure in bonuses stands. */
@Composable
public fun CoinMark(size: Dp = 28.dp, modifier: Modifier = Modifier) {
    val c = Touch.colors
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFFFFD55A), Color(0xFFF59E0B)))),
        contentAlignment = Alignment.Center,
    ) {
        GlyphIcon(Glyph.Coin, Color.White, size = size * 0.72f)
    }
}

/** "+375 бонусов" in the programme's gold, on a card or a row of a partner. */
@Composable
public fun BonusBadge(text: String, modifier: Modifier = Modifier) {
    val c = Touch.colors
    Row(
        modifier = modifier.clip(RoundedCornerShape(50)).background(c.coinSoft).padding(start = 3.dp, end = 9.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinMark(16.dp)
        Spacer(Modifier.width(5.dp))
        Text(text, style = Touch.type.micro, color = c.coinInk, maxLines = 1)
    }
}

/**
 * The wallet line at the top of a service's front page: "Городские бонусы … 1 250 ›". One tap
 * opens the bonus screen. Tag `$tagPrefix:bonus`.
 */
@Composable
public fun BonusBalanceChip(bonus: CityBonus?, onClick: () -> Unit, tagPrefix: String, modifier: Modifier = Modifier) {
    if (bonus == null) return
    val balance by bonus.balance.collectAsState()
    val c = Touch.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .testTag("$tagPrefix:bonus")
            .cardSurface(RoundedCornerShape(18.dp))
            .pressable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinMark(34.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(CITY_BONUS_TITLE, style = Touch.type.caption.copy(fontWeight = FontWeight.Bold), color = c.ink)
            Text("Один счёт на все сервисы города", style = Touch.type.micro.copy(fontWeight = FontWeight.Normal), color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Row(
            Modifier.clip(RoundedCornerShape(50)).background(c.coinSoft).padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatBonus(balance),
                style = Touch.type.caption.copy(fontWeight = FontWeight.ExtraBold),
                color = c.coinInk,
                modifier = Modifier.testTag("$tagPrefix:bonus-balance"),
            )
            GlyphIcon(Glyph.Chevron, c.coinInk, size = 16.dp)
        }
    }
}

/**
 * What this partner gives and takes, on an opened card. A vendor outside the programme ([terms]
 * null) says so in one muted line — the programme is city-wide, but not everyone is in it.
 * [earn] is what this order would bring. Tag `$tagPrefix:bonus-offer` / `$tagPrefix:bonus-none`.
 */
@Composable
public fun BonusOfferCard(
    bonus: CityBonus?,
    terms: BonusTerms?,
    earn: Long,
    onClick: () -> Unit,
    tagPrefix: String,
    modifier: Modifier = Modifier,
    earnWhat: String = "за этот заказ",
) {
    if (bonus == null) return
    val c = Touch.colors
    if (terms == null) {
        Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = Gutter + 2.dp, vertical = 8.dp).testTag("$tagPrefix:bonus-none"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlyphIcon(Glyph.Coin, c.muted, size = 16.dp)
            Spacer(Modifier.width(8.dp))
            Text("Не участвует в городской бонусной системе", style = Touch.type.caption, color = c.muted)
        }
        return
    }
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 8.dp)
            .testTag("$tagPrefix:bonus-offer")
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(c.coinSoft, c.coinSoft.copy(alpha = 0.55f))))
            .border(1.dp, c.coin.copy(alpha = 0.35f), shape)
            .pressable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinMark(42.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Партнёр городских бонусов", style = Touch.type.micro, color = c.coinInk)
            val headline = if (earn > 0) "+${formatBonus(earn)} $earnWhat" else bonusRate(terms)?.let { "$it $earnWhat" }
            if (headline != null) {
                Text(headline, style = Touch.type.strong, color = c.ink, modifier = Modifier.testTag("$tagPrefix:bonus-earn"))
            }
            Text(
                if (terms.canSpend) "Оплата бонусами до ${terms.spendPercent}% · 1 бонус = $VND_PER_BONUS ₫" else "Здесь бонусы только начисляются",
                style = Touch.type.caption,
                color = c.muted,
            )
        }
        GlyphIcon(Glyph.Chevron, c.coinInk, size = 18.dp)
    }
}

// ------------------------------------------------------------------ checkout

/**
 * "Оплатить бонусами" on a checkout, with a switch. Shows how many the partner would take from
 * this order and what that is in dong; explains when there is nothing to take. Tag
 * `$tagPrefix:bonus-use`.
 */
@Composable
public fun BonusSpendRow(
    bonus: CityBonus?,
    terms: BonusTerms?,
    priceVnd: Long,
    use: Boolean,
    onUse: (Boolean) -> Unit,
    tagPrefix: String,
) {
    if (bonus == null || terms == null) return
    val balance by bonus.balance.collectAsState()
    val c = Touch.colors
    val max = terms.maxSpend(priceVnd, balance)
    val reason = when {
        !terms.canSpend -> "Этот партнёр только начисляет бонусы"
        balance <= 0 -> "Бонусов пока нет — первые придут с этим заказом"
        max <= 0 -> "Сумма заказа слишком мала для оплаты бонусами"
        else -> null
    }
    val active = use && reason == null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .testTag("$tagPrefix:bonus-use")
            .cardSurface(RoundedCornerShape(18.dp))
            .clickable(enabled = reason == null, role = Role.Switch) { onUse(!use) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinMark(36.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Оплатить бонусами", style = Touch.type.strong, color = c.ink)
            Text(
                reason ?: "−${formatVnd(max * VND_PER_BONUS)} · спишем ${formatBonus(max)}",
                style = Touch.type.caption,
                color = c.muted,
            )
        }
        Spacer(Modifier.width(10.dp))
        Toggle(active, enabled = reason == null)
    }
}

/** A drawn switch in the coin's gold: the row around it is what is tapped. */
@Composable
private fun Toggle(on: Boolean, enabled: Boolean) {
    val c = Touch.colors
    val track = when {
        !enabled -> c.line
        on -> c.coin
        else -> c.field
    }
    Box(
        Modifier
            .width(48.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .background(track)
            .then(if (on || !enabled) Modifier else Modifier.border(1.dp, c.line, RoundedCornerShape(50)))
            .padding(3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.size(22.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(Color.White))
    }
}

/** The bonus lines under a checkout's totals: what is paid with bonuses and what comes back. */
@Composable
public fun BonusTotals(bonus: CityBonus?, deal: BonusDeal, tagPrefix: String) {
    if (bonus == null) return
    val c = Touch.colors
    if (deal.spend > 0) {
        BonusLine("Бонусами (${formatBonus(deal.spend)})", "−${formatVnd(deal.discount)}", c.success, Modifier.testTag("$tagPrefix:bonus-discount"))
    }
    if (deal.earn > 0) {
        BonusLine("Вернётся бонусами", "+${formatBonus(deal.earn)}", c.coinInk, Modifier.testTag("$tagPrefix:bonus-back"))
    }
}

@Composable
private fun BonusLine(label: String, value: String, color: Color, valueModifier: Modifier) {
    Row(Modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        GlyphIcon(Glyph.Coin, Touch.colors.coin, size = 15.dp)
        Spacer(Modifier.width(6.dp))
        Text(label, style = Touch.type.body, color = Touch.colors.muted, modifier = Modifier.weight(1f))
        Text(value, style = Touch.type.body.copy(fontWeight = FontWeight.Bold), color = color, modifier = valueModifier)
    }
}

/** The lines a vendor reads in the order message, so the discount is agreed before the courier comes. */
public fun bonusMessageLines(deal: BonusDeal): String = buildString {
    if (deal.spend > 0) {
        appendLine("Оплачено городскими бонусами Touch VN: −${formatVnd(deal.discount)} (${formatBonus(deal.spend)})")
        append("К оплате: ${formatVnd(deal.pay)}")
    }
}

/** On a "thank you" page: what came in and went out with the order. Tag `$tagPrefix:bonus-result`. */
@Composable
public fun BonusResult(bonus: CityBonus?, earned: Long, spent: Long, onClick: () -> Unit, tagPrefix: String) {
    if (bonus == null || (earned <= 0 && spent <= 0)) return
    val balance by bonus.balance.collectAsState()
    val c = Touch.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 8.dp)
            .testTag("$tagPrefix:bonus-result")
            .clip(RoundedCornerShape(22.dp))
            .background(WalletGradient)
            .pressable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinMark(48.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            if (earned > 0) Text("+${formatBonus(earned)}", style = Touch.type.title, color = Color.White)
            if (spent > 0) Text("Списано ${formatBonus(spent)}", style = Touch.type.body.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
            Text("На счёте ${formatBonus(balance)}", style = Touch.type.caption, color = Color.White.copy(alpha = 0.8f))
        }
        GlyphIcon(Glyph.Chevron, Color.White.copy(alpha = 0.8f), size = 20.dp)
    }
}

/** The wallet's own ground: night over the bay, with the coin's gold glowing from a corner. */
public val WalletGradient: Brush = Brush.linearGradient(listOf(Color(0xFF14213D), Color(0xFF0B3B4F), Color(0xFF0B6B69)))

// ------------------------------------------------------------------ the bonus screen

/**
 * The city bonus screen: the wallet card, how the programme works, where bonuses come from and go
 * to, and the history. [current] is the service it was opened from, marked in the list.
 * Tags `bonus:balance`, `bonus:place-<id>`, `bonus:entry-<n>`.
 */
@Composable
public fun BonusPage(bonus: CityBonus, onOpenPlace: (String) -> Unit, current: String? = null) {
    val balance by bonus.balance.collectAsState()
    val history by bonus.history.collectAsState()
    val c = Touch.colors
    PortalPage(Modifier.testTag("bonus:page")) {
        WalletCard(balance, Modifier.padding(horizontal = Gutter).padding(top = 12.dp))

        SectionHeader("Как это работает")
        Row(Modifier.fillMaxWidth().padding(horizontal = Gutter), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Step(Glyph.Gift, "Получайте", "за заказы у партнёров со значком «+N»", Modifier.weight(1f))
            Step(Glyph.Coin, "Тратьте", "1 бонус = $VND_PER_BONUS ₫ при оформлении", Modifier.weight(1f))
            Step(Glyph.Swap, "Везде", "один счёт на все сервисы города", Modifier.weight(1f))
        }

        if (bonus.places.isNotEmpty()) {
            SectionHeader("Где получить и потратить")
            Column(
                Modifier.fillMaxWidth().padding(horizontal = Gutter).cardSurface(),
            ) {
                bonus.places.forEachIndexed { i, place ->
                    if (i > 0) Box(Modifier.fillMaxWidth().padding(start = 72.dp).height(1.dp).background(c.line))
                    PlaceRow(place, here = place.appId == current) { onOpenPlace(place.appId) }
                }
            }
        }

        SectionHeader("История")
        if (history.isEmpty()) {
            Hint("Пока пусто — бонусы появятся после первого заказа у партнёра")
        } else {
            Column(Modifier.fillMaxWidth().padding(horizontal = Gutter).cardSurface().padding(vertical = 4.dp)) {
                history.take(HISTORY_SHOWN).forEachIndexed { i, e ->
                    if (i > 0) Box(Modifier.fillMaxWidth().padding(start = 64.dp).height(1.dp).background(c.line))
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp).testTag("bonus:entry-$i"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val plus = e.amount > 0
                        Box(
                            Modifier.size(38.dp).clip(CircleShape).background(if (plus) c.coinSoft else c.field),
                            contentAlignment = Alignment.Center,
                        ) {
                            GlyphIcon(if (plus) Glyph.Plus else Glyph.Minus, if (plus) c.coinInk else c.muted, size = 18.dp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(e.title, style = Touch.type.body.copy(fontWeight = FontWeight.Medium), color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(formatDay(e.at), style = Touch.type.caption, color = c.muted)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            (if (plus) "+" else "−") + groupDigits(kotlin.math.abs(e.amount)),
                            style = Touch.type.strong,
                            color = if (plus) c.coinInk else c.ink,
                        )
                    }
                }
            }
        }
        Notice("Демо: бонусы хранятся на этом устройстве и начисляются сразу после отправки заказа. В рабочей версии — после того как партнёр подтвердит заказ.")
    }
}

/** The balance as a card in a wallet: the figure large, what it is worth, the coin glowing. */
@Composable
public fun WalletCard(balance: Long, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x660B3B4F), spotColor = Color(0x660B3B4F))
            .clip(RoundedCornerShape(28.dp))
            .background(WalletGradient)
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(listOf(Color(0x2EFFFFFF), Color.Transparent), center = Offset(size.width * 0.95f, size.height * 0.05f), radius = size.width * 0.6f),
                    radius = size.width * 0.6f,
                    center = Offset(size.width * 0.95f, size.height * 0.05f),
                )
                drawCircle(Color.White.copy(alpha = 0.05f), radius = size.height * 0.6f, center = Offset(size.width * 0.1f, size.height * 1.1f))
            }
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TOUCH VN · ГОРОДСКИЕ БОНУСЫ", style = Touch.type.micro.copy(letterSpacing = 1.2.sp), color = Color.White.copy(alpha = 0.75f), modifier = Modifier.weight(1f))
            CoinMark(30.dp)
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                groupDigits(balance),
                style = Touch.type.display.copy(fontSize = 46.sp, lineHeight = 50.sp),
                color = Color.White,
                modifier = Modifier.testTag("bonus:balance"),
            )
            Spacer(Modifier.width(8.dp))
            Text(bonusWord(balance), style = Touch.type.strong, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(bottom = 7.dp))
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.14f)).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlyphIcon(Glyph.Wallet, Color(0xFFFCD34D), size = 14.dp)
            Spacer(Modifier.width(6.dp))
            Text("≈ ${formatVnd(balance * VND_PER_BONUS)} на оплату заказов", style = Touch.type.micro, color = Color.White)
        }
    }
}

private const val HISTORY_SHOWN = 50

@Composable
private fun Step(glyph: Glyph, title: String, text: String, modifier: Modifier) {
    val c = Touch.colors
    Column(modifier.cardSurface(RoundedCornerShape(18.dp)).padding(12.dp)) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(c.coinSoft), contentAlignment = Alignment.Center) {
            GlyphIcon(glyph, c.coinInk, size = 18.dp)
        }
        Spacer(Modifier.height(10.dp))
        Text(title, style = Touch.type.caption.copy(fontWeight = FontWeight.Bold), color = c.ink)
        Spacer(Modifier.height(2.dp))
        Text(text, style = Touch.type.micro.copy(fontWeight = FontWeight.Normal), color = c.muted)
    }
}

@Composable
private fun PlaceRow(place: BonusPlace, here: Boolean, onClick: () -> Unit) {
    val c = Touch.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bonus:place-${place.appId}")
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(place.glyph, size = 44.dp, accent = place.tone.accent())
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(place.title, style = Touch.type.strong, color = c.ink)
                if (here) {
                    Spacer(Modifier.width(8.dp))
                    Badge("Вы здесь")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlyphIcon(Glyph.Plus, c.coinInk, size = 12.dp)
                Spacer(Modifier.width(4.dp))
                Text(place.earn, style = Touch.type.caption, color = c.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlyphIcon(Glyph.Minus, c.muted, size = 12.dp)
                Spacer(Modifier.width(4.dp))
                Text(place.spend, style = Touch.type.caption, color = c.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        GlyphIcon(Glyph.Chevron, c.muted, size = 18.dp)
    }
}

/** "14.11.2026" in Vietnam's time (UTC+7), without a date library. */
public fun formatDay(epochMillis: Long): String {
    val days = (epochMillis + 7 * 3_600_000L).floorDiv(86_400_000L)
    val z = days + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
    val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
    val y = (yoe + era * 400 + if (m <= 2) 1 else 0).toInt()
    return "${d.toString().padStart(2, '0')}.${m.toString().padStart(2, '0')}.$y"
}
