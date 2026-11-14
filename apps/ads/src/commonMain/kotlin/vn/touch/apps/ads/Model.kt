package vn.touch.apps.ads

import kotlinx.serialization.Serializable
import vn.touch.kit.BonusTerms
import vn.touch.kit.Contact
import vn.touch.kit.ContactKind
import vn.touch.kit.Glyph
import vn.touch.kit.formatMoney
import vn.touch.kit.looksLikePhone

@Serializable
internal enum class Category(val label: String, val short: String, val glyph: Glyph) {
    Rent("Аренда жилья", "Жильё", Glyph.Home),
    Bikes("Байки и авто", "Байки", Glyph.Bike),
    Sale("Продам", "Продам", Glyph.Tag),
    Jobs("Работа", "Работа", Glyph.Work),
    Services("Услуги", "Услуги", Glyph.Wrench),
    Free("Отдам / ищу", "Отдам", Glyph.Heart),
}

@Serializable
internal enum class District(val label: String) {
    Center("Центр"),
    North("Север"),
    South("Юг"),
    CamRanh("Камрань"),
}

@Serializable
internal data class Ad(
    val id: String,
    val category: Category,
    val title: String,
    val text: String,
    val price: Long? = null,
    val currency: String = "VND",
    /** "в месяц", "в сутки", "за час" — what the price is for, if not the whole thing. */
    val per: String = "",
    val district: District,
    val author: String,
    val contacts: List<Contact>,
    val createdAt: Long,
    val mine: Boolean = false,
    /** A business in the city bonus system; private ads have none. */
    val bonus: BonusTerms? = null,
) {
    val priceText: String?
        get() = price?.let { formatMoney(it, currency) + if (per.isBlank()) "" else " $per" }
}

/** Newest first; a query matches the title or the text, case-insensitively, every word of it. */
internal fun filterAds(ads: List<Ad>, category: Category?, query: String): List<Ad> {
    val words = query.lowercase().split(' ').filter { it.isNotBlank() }
    return ads
        .filter { category == null || it.category == category }
        .filter { ad ->
            val haystack = (ad.title + " " + ad.text).lowercase()
            words.all { it in haystack }
        }
        .sortedByDescending { it.createdAt }
}

internal fun ago(now: Long, then: Long): String {
    val hours = ((now - then) / 3_600_000).coerceAtLeast(0)
    return when {
        hours < 1 -> "только что"
        hours < 24 -> "$hours ч назад"
        hours < 48 -> "вчера"
        else -> "${hours / 24} дн. назад"
    }
}

/** What the "new ad" form holds while it is being filled in. */
@Serializable
internal data class Draft(
    val category: Category? = null,
    val title: String = "",
    val text: String = "",
    val price: String = "",
    val currency: String = "VND",
    val district: District? = null,
    val author: String = "",
    val contactKind: ContactKind = ContactKind.WhatsApp,
    val contact: String = "",
) {
    fun problems(): List<String> = buildList {
        if (category == null) add("категорию")
        if (title.trim().length < 5) add("заголовок")
        if (text.trim().length < 10) add("описание")
        if (district == null) add("район")
        if (author.isBlank()) add("имя")
        val contactOk = if (contactKind == ContactKind.Telegram) contact.trim().length >= 4 else looksLikePhone(contact)
        if (!contactOk) add("контакт")
        if (price.isNotBlank() && price.filter { it.isDigit() }.isEmpty()) add("цену")
    }

    fun toAd(id: String, now: Long): Ad = Ad(
        id = id,
        category = category!!,
        title = title.trim(),
        text = text.trim(),
        price = price.filter { it.isDigit() }.toLongOrNull(),
        currency = currency,
        district = district!!,
        author = author.trim(),
        contacts = listOf(Contact(contactKind, contact.trim())),
        createdAt = now,
        mine = true,
    )
}
