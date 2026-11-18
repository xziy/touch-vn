package vn.touch.apps.exchange

import vn.touch.kit.BonusTerms
import vn.touch.kit.Contact
import vn.touch.kit.Glyph
import kotlin.math.roundToLong

internal enum class Currency(val code: String, val label: String, val glyph: Glyph) {
    USD("USD", "Доллар", Glyph.Dollar),
    EUR("EUR", "Евро", Glyph.Euro),
    RUB("RUB", "Рубль", Glyph.Ruble),
    USDT("USDT", "Tether", Glyph.Wallet),
}

internal enum class PointType(val label: String, val glyph: Glyph) {
    Bank("Банк", Glyph.Bank),
    Gold("Ювелирный", Glyph.Gem),
    Private("Частный обмен", Glyph.Swap),
}

internal data class Point(
    val id: String,
    val name: String,
    val type: PointType,
    val address: String,
    val hours: String,
    /** VND paid for one unit of the currency. Absent: the point does not buy it. */
    val rates: Map<Currency, Double>,
    val delivery: Boolean,
    val note: String,
    val contacts: List<Contact>,
    /** The point's part in the city bonus system; null — not a partner. */
    val bonus: BonusTerms? = null,
)

internal data class Quote(val point: Point, val rate: Double, val vnd: Long)

/** Every point that buys [currency], the one paying the most for [amount] first. */
internal fun quotes(points: List<Point>, currency: Currency, amount: Double): List<Quote> =
    points.mapNotNull { p -> p.rates[currency]?.let { Quote(p, it, (it * amount).roundToLong()) } }
        .sortedByDescending { it.rate }

/** "1 500", "1500.5", "1 500,5" → 1500.5; anything else → null. */
internal fun parseAmount(text: String): Double? {
    val clean = text.filter { !it.isWhitespace() }.replace(',', '.')
    if (clean.isEmpty()) return null
    return clean.toDoubleOrNull()?.takeIf { it > 0 }
}

/** "26 310" for whole rates, "305,5" for the rouble. */
internal fun formatRate(rate: Double): String {
    val whole = rate.toLong()
    val fraction = ((rate - whole) * 10).roundToLong()
    val grouped = vn.touch.kit.groupDigits(whole)
    return if (fraction == 0L) grouped else "$grouped,$fraction"
}

internal fun exchangeMessage(point: Point, currency: Currency, amount: Double?): String =
    "Здравствуйте! Хочу обменять " + (amount?.let { formatAmount(it) + " " } ?: "") + currency.code +
        " на донги. Нашёл вас на портале Touch VN." +
        if (point.delivery) " Возможна доставка?" else ""

internal fun formatAmount(value: Double): String =
    if (value == value.toLong().toDouble()) vn.touch.kit.groupDigits(value.toLong()) else value.toString()
