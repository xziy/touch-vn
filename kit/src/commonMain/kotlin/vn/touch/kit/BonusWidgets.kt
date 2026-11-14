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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composeunstyled.Text
import cx.m42.superizer.runtime.LocalAppRuntime
import cx.m42.superizer.theme.AppTheme
import cx.m42.superizer.ui.platform.SystemBackHandler

/** The programme's colour pair: sun-yellow ground, dark-amber ink — distinct from coral prices. */
private val BonusFill: Color = Color(0xFFFFF4D6)
private val BonusInk: Color = Color(0xFF9A6200)

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

/** "+375 бонусов" in the programme's colours, on a card or a row of a partner. */
@Composable
public fun BonusBadge(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(8.dp)).background(BonusFill).padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphIcon(Glyph.Coin, BonusInk, size = 13.dp)
        Spacer(Modifier.width(4.dp))
        Text(text, style = AppTheme.footnote.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp), color = BonusInk, maxLines = 1)
    }
}

/**
 * The line at the top of a service's front page: "Городские бонусы ...... 1 250 бонусов ›".
 * One tap opens the bonus screen. Tag `$tagPrefix:bonus`.
 */
@Composable
public fun BonusBalanceChip(bonus: CityBonus?, onClick: () -> Unit, tagPrefix: String, modifier: Modifier = Modifier) {
    if (bonus == null) return
    val balance by bonus.balance.collectAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("$tagPrefix:bonus")
            .clip(RoundedCornerShape(14.dp))
            .background(BonusFill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(Sun), contentAlignment = Alignment.Center) {
            GlyphIcon(Glyph.Coin, Color.White, size = 18.dp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(CITY_BONUS_TITLE, style = AppTheme.label.copy(fontWeight = FontWeight.SemiBold), color = Ink)
            Text("Копите и тратите во всех сервисах города", style = AppTheme.footnote.copy(fontSize = 12.sp), color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            formatBonus(balance),
            style = AppTheme.body.copy(fontWeight = FontWeight.Bold),
            color = BonusInk,
            modifier = Modifier.testTag("$tagPrefix:bonus-balance"),
        )
        GlyphIcon(Glyph.Chevron, BonusInk, size = 18.dp)
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
    if (terms == null) {
        Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).testTag("$tagPrefix:bonus-none"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlyphIcon(Glyph.Coin, Muted, size = 16.dp)
            Spacer(Modifier.width(8.dp))
            Text("Не участвует в городской бонусной системе", style = AppTheme.footnote, color = Muted)
        }
        return
    }
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("$tagPrefix:bonus-offer")
            .clip(shape)
            .background(BonusFill)
            .border(1.dp, Sun.copy(alpha = 0.6f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(Sun), contentAlignment = Alignment.Center) {
            GlyphIcon(Glyph.Coin, Color.White, size = 22.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Партнёр городской бонусной системы", style = AppTheme.footnote, color = BonusInk)
            val headline = if (earn > 0) "+${formatBonus(earn)} $earnWhat" else bonusRate(terms)?.let { "$it $earnWhat" }
            if (headline != null) {
                Text(headline, style = AppTheme.body.copy(fontWeight = FontWeight.Bold), color = Ink, modifier = Modifier.testTag("$tagPrefix:bonus-earn"))
            }
            Text(
                if (terms.canSpend) "Можно оплатить бонусами до ${terms.spendPercent}% · 1 бонус = $VND_PER_BONUS ₫" else "Здесь бонусы только начисляются",
                style = AppTheme.footnote,
                color = Muted,
            )
        }
        GlyphIcon(Glyph.Chevron, BonusInk, size = 18.dp)
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
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("$tagPrefix:bonus-use")
            .clip(RoundedCornerShape(14.dp))
            .background(BonusFill)
            .clickable(enabled = reason == null, role = Role.Switch) { onUse(!use) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphIcon(Glyph.Coin, BonusInk, size = 20.dp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Оплатить бонусами", style = AppTheme.body.copy(fontWeight = FontWeight.SemiBold), color = Ink)
            Text(
                reason ?: "Спишем ${formatBonus(max)} = −${formatVnd(max * VND_PER_BONUS)} · на счёте ${formatBonus(balance)}",
                style = AppTheme.footnote,
                color = Muted,
            )
        }
        Spacer(Modifier.width(10.dp))
        Switch(active, enabled = reason == null)
    }
}

/** A drawn switch: the row around it is what is tapped. */
@Composable
private fun Switch(on: Boolean, enabled: Boolean) {
    val track = when {
        !enabled -> Line
        on -> Coral
        else -> Color(0xFFD4D4D8)
    }
    Box(
        Modifier.width(44.dp).height(26.dp).clip(RoundedCornerShape(50)).background(track).padding(3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.size(20.dp).clip(CircleShape).background(Color.White))
    }
}

/** The bonus lines under a checkout's totals: what is paid with bonuses and what comes back. */
@Composable
public fun BonusTotals(bonus: CityBonus?, deal: BonusDeal, tagPrefix: String) {
    if (bonus == null) return
    if (deal.spend > 0) {
        SummaryRow("Бонусами (${formatBonus(deal.spend)})", "−${formatVnd(deal.discount)}", valueModifier = Modifier.testTag("$tagPrefix:bonus-discount"))
    }
    if (deal.earn > 0) {
        SummaryRow("Вернётся бонусами", "+${formatBonus(deal.earn)}", valueModifier = Modifier.testTag("$tagPrefix:bonus-back"))
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("$tagPrefix:bonus-result")
            .clip(RoundedCornerShape(16.dp))
            .background(BonusFill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (earned > 0) Text("+${formatBonus(earned)}", style = AppTheme.header, color = BonusInk)
        if (spent > 0) Text("Списано ${formatBonus(spent)}", style = AppTheme.label, color = Ink)
        Text("На счёте ${formatBonus(balance)} · $CITY_BONUS_TITLE", style = AppTheme.footnote, color = Muted)
    }
}

// ------------------------------------------------------------------ the bonus screen

/**
 * The city bonus screen: the balance, how the programme works, where bonuses come from and go to,
 * and the history. [current] is the service it was opened from, marked in the list.
 * Tags `bonus:balance`, `bonus:place-<id>`, `bonus:entry-<n>`.
 */
@Composable
public fun BonusPage(bonus: CityBonus, onOpenPlace: (String) -> Unit, current: String? = null) {
    val balance by bonus.balance.collectAsState()
    val history by bonus.history.collectAsState()
    PortalPage(Modifier.testTag("bonus:page")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Coral)
                .drawBehind {
                    drawCircle(Color.White.copy(alpha = 0.10f), radius = size.height * 0.7f, center = Offset(size.width * 0.95f, 0f))
                    drawCircle(Sun.copy(alpha = 0.35f), radius = size.height * 0.35f, center = Offset(size.width * 0.85f, size.height))
                }
                .padding(20.dp),
        ) {
            Text("Городская бонусная система", style = AppTheme.label, color = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.height(6.dp))
            Text(
                groupDigits(balance),
                style = AppTheme.title.copy(fontSize = 44.sp, lineHeight = 50.sp, fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.testTag("bonus:balance"),
            )
            Text(bonusWord(balance) + " на счёте", style = AppTheme.body, color = Color.White)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.clip(RoundedCornerShape(50)).background(Color.White).padding(horizontal = 12.dp, vertical = 5.dp)) {
                Text(
                    "≈ ${formatVnd(balance * VND_PER_BONUS)} на оплату заказов",
                    style = AppTheme.footnote.copy(fontWeight = FontWeight.SemiBold),
                    color = Coral,
                )
            }
        }

        SectionHeader("Как это работает")
        Step(1, "Получайте", "За заказы, брони и обмен у партнёров — у них на карточке значок «+N бонусов».")
        Step(2, "Тратьте", "При оформлении включите «Оплатить бонусами»: 1 бонус = $VND_PER_BONUS ₫, каждый партнёр принимает бонусами свою долю цены.")
        Step(3, "По всему городу", "Один счёт на все сервисы: заработали на еде — потратили на цветах или визаране.")

        if (bonus.places.isNotEmpty()) {
            SectionHeader("Где получить и потратить")
            bonus.places.forEach { place -> PlaceRow(place, here = place.appId == current) { onOpenPlace(place.appId) } }
        }

        SectionHeader("История")
        if (history.isEmpty()) {
            Hint("Пока пусто — бонусы появятся после первого заказа у партнёра")
        }
        history.take(HISTORY_SHOWN).forEachIndexed { i, e ->
            if (i > 0) Divider()
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).testTag("bonus:entry-$i"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(e.title, style = AppTheme.body, color = Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(formatDay(e.at), style = AppTheme.footnote, color = Muted)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    (if (e.amount > 0) "+" else "−") + groupDigits(kotlin.math.abs(e.amount)),
                    style = AppTheme.body.copy(fontWeight = FontWeight.Bold),
                    color = if (e.amount > 0) BonusInk else Ink,
                )
            }
        }
        Notice("Демо: бонусы хранятся на этом устройстве и начисляются сразу после отправки заказа. В рабочей версии — после того как партнёр подтвердит заказ.")
    }
}

private const val HISTORY_SHOWN = 50

@Composable
private fun Step(n: Int, title: String, text: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(BonusFill), contentAlignment = Alignment.Center) {
            Text("$n", style = AppTheme.label.copy(fontWeight = FontWeight.Bold), color = BonusInk)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.body.copy(fontWeight = FontWeight.SemiBold), color = Ink)
            Text(text, style = AppTheme.footnote, color = Muted)
        }
    }
}

@Composable
private fun PlaceRow(place: BonusPlace, here: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bonus:place-${place.appId}")
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(CoralSoft), contentAlignment = Alignment.Center) {
            GlyphIcon(place.glyph, Coral, size = 24.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(place.title, style = AppTheme.body.copy(fontWeight = FontWeight.Bold), color = Ink)
                if (here) {
                    Spacer(Modifier.width(8.dp))
                    Badge("Вы здесь")
                }
            }
            Text("Получить: ${place.earn}", style = AppTheme.footnote, color = Muted)
            Text("Потратить: ${place.spend}", style = AppTheme.footnote, color = Muted)
        }
        GlyphIcon(Glyph.Chevron, Muted, size = 18.dp)
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
