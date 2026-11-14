package vn.touch.apps.flowers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import cx.m42.superizer.ui.platform.SystemBackHandler
import vn.touch.kit.BarButton
import vn.touch.kit.CategoryRow
import vn.touch.kit.ContactButtons
import vn.touch.kit.DetailHero
import vn.touch.kit.DoneState
import vn.touch.kit.FeatureCard
import vn.touch.kit.Field
import vn.touch.kit.FormSection
import vn.touch.kit.Glyph
import vn.touch.kit.HeroBanner
import vn.touch.kit.Hint
import vn.touch.kit.InfoRow
import vn.touch.kit.MiniHeader
import vn.touch.kit.Notice
import vn.touch.kit.PortalPage
import vn.touch.kit.PrimaryButton
import vn.touch.kit.SectionHeader
import vn.touch.kit.Tile
import vn.touch.kit.TitleBlock
import vn.touch.kit.formatVnd
import vn.touch.kit.BonusBalanceChip
import vn.touch.kit.BonusOfferCard
import vn.touch.kit.BonusResult
import vn.touch.kit.BonusSpendRow
import vn.touch.kit.BonusTotals
import vn.touch.kit.SummaryRow
import vn.touch.kit.bonusBadge

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

@Composable
private fun ListPage(instance: FlowersInstance) {
    val kind by instance.kind.collectAsState()
    val list = Catalog.bouquets.filter { kind == null || it.kind == kind }
    PortalPage {
        HeroBanner(
            title = "Цветы с доставкой",
            subtitle = "Привезём сегодня, если заказать до 18:00",
            glyph = Glyph.Flower,
            pill = "${Catalog.bouquets.size} букетов",
        )
        BonusBalanceChip(instance.bonus, instance::openBonus, TAG)
        CategoryRow(Kind.entries.map { Tile(it.name, it.short, it.glyph) }, kind?.name, { k -> instance.filter(k?.let(Kind::valueOf)) }, TAG)
        SectionHeader(kind?.label ?: "Все букеты")
        // Two columns, as a shop window: the picture sells a bouquet, not its row.
        list.chunked(2).forEach { pair ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                pair.forEach { b ->
                    FeatureCard(
                        title = b.name,
                        glyph = b.kind.glyph,
                        seed = b.id,
                        subtitle = b.shop.name,
                        price = formatVnd(b.price),
                        pill = if (b.hit) "Хит" else null,
                        imageHeight = 140.dp,
                        bonus = instance.bonus?.let { bonusBadge(b.shop.bonus?.earnFor(b.price) ?: 0) },
                        onClick = { instance.open(b.id) },
                        modifier = Modifier.weight(1f).testTag("$TAG:bouquet-${b.id}"),
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Notice("Демо-каталог: заказ уходит магазину сообщением в мессенджер, магазин присылает фото букета.")
    }
}

@Composable
private fun DetailPage(instance: FlowersInstance, id: String) {
    val b = Catalog.bouquet(id) ?: return
    PortalPage(
        bottomBar = {
            BarButton("Заказать доставку", { instance.startOrder(b.id) }, Modifier.testTag("$TAG:order"), trailing = formatVnd(b.price))
        },
    ) {
        DetailHero(b.kind.glyph, b.id, pill = if (b.hit) "Хит продаж" else null, pillGlyph = Glyph.Star)
        TitleBlock(b.name, b.kind.label, price = formatVnd(b.price))
        BonusOfferCard(instance.bonus, b.shop.bonus, earn = b.shop.bonus?.earnFor(b.price) ?: 0, onClick = instance::openBonus, tagPrefix = TAG, earnWhat = "за этот букет")
        Spacer(Modifier.height(6.dp))
        InfoRow("Состав", b.composition, glyph = Glyph.Flower)
        InfoRow("Размер", b.size, glyph = Glyph.Grid)
        InfoRow(b.shop.name, b.shop.address, glyph = Glyph.Pin)
        InfoRow("Доставка", "По Нячангу, в день заказа", glyph = Glyph.Truck)
    }
}

@Composable
private fun OrderPage(instance: FlowersInstance, id: String) {
    val b = Catalog.bouquet(id) ?: return
    val order by instance.order
    val ready = canSend(order)
    instance.bonus?.balance?.collectAsState()?.value
    val useBonus by instance.useBonus
    val deal = instance.deal(b, spending = useBonus)
    PortalPage {
        MiniHeader(b.name, "${b.shop.name} · ${formatVnd(b.price)}", b.kind.glyph, b.id)
        FormSection("Кому") {
            Field(order.recipient, { v -> instance.edit { it.copy(recipient = v) } }, "Имя получателя", Modifier.testTag("$TAG:recipient"))
            Field(order.recipientPhone, { v -> instance.edit { it.copy(recipientPhone = v) } }, "Телефон получателя (необязательно)", Modifier.testTag("$TAG:recipient-phone"))
            Field(order.address, { v -> instance.edit { it.copy(address = v) } }, "Адрес, отель, номер", Modifier.testTag("$TAG:address"), singleLine = false)
            Field(order.whenText, { v -> instance.edit { it.copy(whenText = v) } }, "Когда: например, 14 февраля к 10:00", Modifier.testTag("$TAG:when"))
            Field(order.card, { v -> instance.edit { it.copy(card = v) } }, "Текст открытки (необязательно)", Modifier.testTag("$TAG:card"), singleLine = false)
        }
        FormSection("От кого") {
            Field(order.sender, { v -> instance.edit { it.copy(sender = v) } }, "Ваше имя", Modifier.testTag("$TAG:sender"))
            Field(order.senderPhone, { v -> instance.edit { it.copy(senderPhone = v) } }, "Ваш телефон / WhatsApp", Modifier.testTag("$TAG:sender-phone"))
        }
        if (instance.bonus != null && b.shop.bonus != null) {
            SectionHeader("Сумма")
            BonusSpendRow(instance.bonus, b.shop.bonus, b.price, useBonus, instance::useBonus, TAG)
            SummaryRow("Букет", formatVnd(b.price))
            BonusTotals(instance.bonus, deal, TAG)
            SummaryRow("К оплате", formatVnd(deal.pay), strong = true, valueModifier = Modifier.testTag("$TAG:total"))
        }
        SectionHeader("Отправить заказ магазину")
        if (ready) {
            ContactButtons(b.shop.contacts, TAG, message = orderMessage(b, order, deal), onOpened = { instance.sent(b.id) })
        } else {
            Hint("Заполните получателя, адрес, время и свои контакты", Modifier.testTag("$TAG:fill-in"))
        }
    }
}

@Composable
private fun SentPage(instance: FlowersInstance) {
    PortalPage {
        Spacer(Modifier.height(24.dp))
        DoneState(
            "Заказ у флориста!",
            "Отправьте сообщение в мессенджере — магазин подтвердит доставку и пришлёт фото букета.",
            Modifier.testTag("$TAG:sent"),
        )
        val (earned, spent) = instance.lastBonus
        BonusResult(instance.bonus, earned, spent, instance::openBonus, TAG)
        PrimaryButton("К букетам", { instance.toRoot() }, Modifier.testTag("$TAG:done"))
    }
}
