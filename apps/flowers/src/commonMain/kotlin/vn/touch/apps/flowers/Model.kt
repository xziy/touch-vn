package vn.touch.apps.flowers

import kotlinx.serialization.Serializable
import vn.touch.kit.BonusDeal
import vn.touch.kit.BonusTerms
import vn.touch.kit.Contact
import vn.touch.kit.bonusMessageLines
import vn.touch.kit.Glyph
import vn.touch.kit.formatVnd
import vn.touch.kit.looksLikePhone

internal enum class Kind(val label: String, val short: String, val glyph: Glyph) {
    Roses("Розы", "Розы", Glyph.Flower),
    Mixed("Сборные", "Сборные", Glyph.Flower),
    Box("В коробке", "Коробки", Glyph.Gift),
    Basket("Корзины", "Корзины", Glyph.Basket),
    Tropical("Тропические", "Тропики", Glyph.Leaf),
}

/** [bonus]: the shop's part in the city bonus system; null — not a partner. */
internal data class Shop(val id: String, val name: String, val address: String, val contacts: List<Contact>, val bonus: BonusTerms? = null)

internal data class Bouquet(
    val id: String,
    val name: String,
    val kind: Kind,
    val price: Long,
    val composition: String,
    val size: String,
    val shop: Shop,
    val hit: Boolean = false,
)

@Serializable
internal data class FlowerOrder(
    val recipient: String = "",
    val recipientPhone: String = "",
    val address: String = "",
    val whenText: String = "",
    val card: String = "",
    val sender: String = "",
    val senderPhone: String = "",
)

internal fun canSend(order: FlowerOrder): Boolean =
    order.recipient.isNotBlank() && order.address.isNotBlank() && order.whenText.isNotBlank() &&
        order.sender.isNotBlank() && looksLikePhone(order.senderPhone)

internal fun orderMessage(bouquet: Bouquet, order: FlowerOrder, deal: BonusDeal? = null): String = buildString {
    appendLine("Заказ цветов с портала Touch VN — ${bouquet.shop.name}")
    appendLine()
    appendLine("Букет: ${bouquet.name} — ${formatVnd(bouquet.price)}")
    if (deal != null && deal.spend > 0) appendLine(bonusMessageLines(deal))
    appendLine("Когда: ${order.whenText.trim()}")
    appendLine("Получатель: ${order.recipient.trim()}" + order.recipientPhone.trim().let { if (it.isEmpty()) "" else ", $it" })
    appendLine("Адрес: ${order.address.trim()}")
    if (order.card.isNotBlank()) appendLine("Открытка: «${order.card.trim()}»")
    appendLine()
    appendLine("Заказчик: ${order.sender.trim()}, ${order.senderPhone.trim()}")
}.trimEnd()
