package vn.touch.apps.exchange

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import cx.m42.superizer.ui.platform.SystemBackHandler
import vn.touch.kit.CategoryRow
import vn.touch.kit.ContactButtons
import vn.touch.kit.DetailHero
import vn.touch.kit.Divider
import vn.touch.kit.EmptyState
import vn.touch.kit.Field
import vn.touch.kit.FormSection
import vn.touch.kit.Glyph
import vn.touch.kit.InfoRow
import vn.touch.kit.ListRow
import vn.touch.kit.Notice
import vn.touch.kit.PortalPage
import vn.touch.kit.SectionHeader
import vn.touch.kit.SummaryRow
import vn.touch.kit.Tile
import vn.touch.kit.TitleBlock
import vn.touch.kit.formatVnd
import vn.touch.kit.BonusBalanceChip
import vn.touch.kit.BonusOfferCard
import vn.touch.kit.bonusBadge

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

@Composable
private fun ListPage(instance: ExchangeInstance) {
    val currency by instance.currency.collectAsState()
    val amountText by instance.amount
    val amount = parseAmount(amountText)
    val list = quotes(Catalog.points, currency, amount ?: 1.0)
    PortalPage {
        BonusBalanceChip(instance.bonus, instance::openBonus, TAG)
        CategoryRow(
            Currency.entries.map { Tile(it.name, it.code, it.glyph) },
            currency.name,
            { k -> if (k != null) instance.select(Currency.valueOf(k)) },
            TAG,
            allLabel = null,
        )
        FormSection("Сколько меняете, ${currency.code}") {
            Field(amountText, instance::setAmount, "100", Modifier.testTag("$TAG:amount"), leading = currency.glyph)
        }
        SectionHeader("Лучший курс — сверху")
        if (list.isEmpty()) EmptyState(currency.glyph, "Никто не покупает ${currency.code}", "Попробуйте другую валюту")
        list.forEachIndexed { i, q ->
            ListRow(
                title = q.point.name,
                glyph = q.point.type.glyph,
                seed = q.point.id,
                subtitle = "1 ${currency.code} = ${formatRate(q.rate)} ₫" + if (q.point.delivery) " · доставка" else "",
                meta = "${q.point.type.label} · ${q.point.hours}",
                price = if (amount != null) formatVnd(q.vnd) else null,
                badge = if (i == 0) "Лучший" else null,
                bonus = instance.bonus?.let { bonusBadge(q.point.bonus?.earnFor(null) ?: 0) },
                onClick = { instance.open(q.point.id) },
                modifier = Modifier.testTag("$TAG:point-${q.point.id}"),
            )
        }
        Notice("Курсы обновлены: ${Catalog.UPDATED}. Уточняйте курс перед обменом; пересчитывайте деньги на месте.")
    }
}

@Composable
private fun DetailPage(instance: ExchangeInstance, id: String) {
    val p = Catalog.point(id) ?: return
    val currency by instance.currency.collectAsState()
    val amountText by instance.amount
    PortalPage {
        DetailHero(p.type.glyph, p.id, pill = p.hours)
        TitleBlock(p.name, p.type.label)
        Spacer(Modifier.height(4.dp))
        InfoRow("Адрес", p.address, glyph = Glyph.Pin)
        InfoRow("Часы работы", p.hours, glyph = Glyph.Clock)
        InfoRow("Доставка денег", if (p.delivery) "Есть" else "Нет", glyph = Glyph.Truck)
        SectionHeader("Курсы покупки")
        Currency.entries.forEachIndexed { i, c ->
            if (i > 0) Divider()
            val rate = p.rates[c]
            SummaryRow("1 ${c.code}", if (rate == null) "не покупают" else "${formatRate(rate)} ₫")
        }
        Notice(p.note)
        BonusOfferCard(instance.bonus, p.bonus, earn = p.bonus?.earnFor(null) ?: 0, onClick = instance::openBonus, tagPrefix = TAG, earnWhat = "за обмен через портал")
        SectionHeader("Связаться")
        ContactButtons(p.contacts, TAG, message = exchangeMessage(p, currency, parseAmount(amountText)), onOpened = { instance.contacted(p) })
    }
}
