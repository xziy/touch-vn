package vn.touch.apps.visarun

import kotlinx.serialization.Serializable
import vn.touch.kit.BonusDeal
import vn.touch.kit.BonusTerms
import vn.touch.kit.Contact
import vn.touch.kit.bonusMessageLines
import vn.touch.kit.Glyph
import vn.touch.kit.looksLikePhone

internal enum class Route(val label: String, val short: String, val glyph: Glyph) {
    Cambodia("Камбоджа", "Камбоджа", Glyph.Bus),
    Laos("Лаос", "Лаос", Glyph.Bus),
    Flight("Самолётом", "Самолёт", Glyph.Plane),
    Help("Помощь с визой", "Виза", Glyph.Passport),
}

internal data class Offer(
    val id: String,
    val title: String,
    val route: Route,
    val organiser: String,
    val schedule: String,
    val duration: String,
    val price: Long,
    val currency: String,
    val includes: List<String>,
    val notIncluded: List<String>,
    val documents: List<String>,
    val contacts: List<Contact>,
    val popular: Boolean = false,
    /** The organiser's part in the city bonus system; null — not a partner. */
    val bonus: BonusTerms? = null,
) {
    /** The price for [people] in dong; null for a tour priced in another currency. */
    fun priceVnd(people: Int): Long? = if (currency == "VND") price * people.coerceAtLeast(1) else null
}

@Serializable
internal data class Booking(
    val name: String = "",
    val phone: String = "",
    val date: String = "",
    val people: String = "1",
    val hotel: String = "",
)

internal fun canSend(b: Booking): Boolean =
    b.name.isNotBlank() && looksLikePhone(b.phone) && b.date.isNotBlank() && (b.people.toIntOrNull() ?: 0) > 0

internal fun bookingMessage(offer: Offer, b: Booking, deal: BonusDeal? = null): String = buildString {
    appendLine("Заявка на визаран с портала Touch VN")
    appendLine()
    appendLine("Тур: ${offer.title} (${offer.organiser})")
    appendLine("Дата: ${b.date.trim()}")
    appendLine("Человек: ${b.people.trim()}")
    if (b.hotel.isNotBlank()) appendLine("Забрать из: ${b.hotel.trim()}")
    if (deal != null && deal.spend > 0) appendLine(bonusMessageLines(deal))
    appendLine()
    appendLine("${b.name.trim()}, ${b.phone.trim()}")
}.trimEnd()

// ------------------------------------------------------------------ the stay calculator

/** A day of the civil calendar as a count of days since 1970-01-01. Pure, no library needed. */
internal fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    val y = (if (month <= 2) year - 1 else year).toLong()
    val era = (if (y >= 0) y else y - 399) / 400
    val yoe = y - era * 400
    val m = month.toLong()
    val doy = (153 * (if (m > 2) m - 3 else m + 9) + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}

internal fun civilFromDays(days: Long): Triple<Int, Int, Int> {
    val z = days + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
    val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
    val y = (yoe + era * 400 + if (m <= 2) 1 else 0).toInt()
    return Triple(y, m, d)
}

/** "13.11.2026" → days since epoch, or null for anything that is not a real date. */
internal fun parseDate(text: String): Long? {
    val parts = text.trim().split('.', '/', '-').map { it.trim() }
    if (parts.size != 3) return null
    val d = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val y = parts[2].toIntOrNull()?.let { if (it < 100) 2000 + it else it } ?: return null
    if (m !in 1..12 || d !in 1..31 || y !in 2000..2100) return null
    val days = daysFromCivil(y, m, d)
    return if (civilFromDays(days) == Triple(y, m, d)) days else null
}

internal fun formatDate(days: Long): String {
    val (y, m, d) = civilFromDays(days)
    return "${d.toString().padStart(2, '0')}.${m.toString().padStart(2, '0')}.$y"
}

/** Today in Vietnam (UTC+7), as days since epoch. */
internal fun todayInVietnam(nowMillis: Long): Long = (nowMillis + 7 * 3_600_000L).floorDiv(86_400_000L)

internal enum class Regime(val label: String, val days: Int) {
    VisaFree45("Безвиз 45 дней", 45),
    EVisa90("E-visa 90 дней", 90),
    VisaFree30("Безвиз 30 дней", 30),
}

internal data class Stay(val lastDay: Long, val daysLeft: Long)

/** The day of entry counts as the first day: 45 days from 1 March end on 14 April. */
internal fun stay(entryDay: Long, regime: Regime, today: Long): Stay {
    val last = entryDay + regime.days - 1
    return Stay(last, last - today)
}
