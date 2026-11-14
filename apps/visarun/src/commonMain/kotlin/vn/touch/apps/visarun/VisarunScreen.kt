package vn.touch.apps.visarun

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composeunstyled.Text
import cx.m42.superizer.theme.AppTheme
import cx.m42.superizer.ui.platform.SystemBackHandler
import vn.touch.kit.BarButton
import vn.touch.kit.CategoryRow
import vn.touch.kit.Chip
import vn.touch.kit.Chips
import vn.touch.kit.ContactButtons
import vn.touch.kit.Coral
import vn.touch.kit.DetailHero
import vn.touch.kit.DoneState
import vn.touch.kit.FeatureCard
import vn.touch.kit.Field
import vn.touch.kit.FormSection
import vn.touch.kit.Glyph
import vn.touch.kit.GlyphIcon
import vn.touch.kit.HeroBanner
import vn.touch.kit.Hint
import vn.touch.kit.InfoRow
import vn.touch.kit.Ink
import vn.touch.kit.Line
import vn.touch.kit.MiniHeader
import vn.touch.kit.Muted
import vn.touch.kit.Notice
import vn.touch.kit.PortalPage
import vn.touch.kit.PrimaryButton
import vn.touch.kit.SectionHeader
import vn.touch.kit.Tile
import vn.touch.kit.TitleBlock
import vn.touch.kit.formatMoney
import vn.touch.kit.formatVnd
import vn.touch.kit.BonusBalanceChip
import vn.touch.kit.BonusOfferCard
import vn.touch.kit.BonusResult
import vn.touch.kit.BonusSpendRow
import vn.touch.kit.BonusTotals
import vn.touch.kit.SummaryRow
import vn.touch.kit.bonusBadge

private const val TAG = VisarunApp.ID

@Composable
internal fun VisarunScreen(instance: VisarunInstance) {
    val pages by instance.pages.collectAsState()
    SystemBackHandler(enabled = pages.size > 1) { instance.back() }
    when (val page = pages.last()) {
        VisarunPage.List -> ListPage(instance)
        is VisarunPage.Detail -> DetailPage(instance, page.id)
        is VisarunPage.Book -> BookPage(instance, page.id)
        VisarunPage.Calculator -> CalculatorPage(instance)
        VisarunPage.Sent -> SentPage(instance)
    }
}

@Composable
private fun ListPage(instance: VisarunInstance) {
    val route by instance.route.collectAsState()
    val stayInput by instance.stayInput
    PortalPage {
        StaySummary(instance, stayInput)
        BonusBalanceChip(instance.bonus, instance::openBonus, TAG)
        CategoryRow(Route.entries.map { Tile(it.name, it.short, it.glyph) }, route?.name, { k -> instance.filter(k?.let(Route::valueOf)) }, TAG)
        SectionHeader(route?.label ?: "Визараны из Нячанга")
        Catalog.offers.filter { route == null || it.route == route }.forEach { o ->
            FeatureCard(
                title = o.title,
                glyph = o.route.glyph,
                seed = o.id,
                subtitle = "${o.organiser} · ${o.schedule}",
                price = formatMoney(o.price, o.currency),
                pill = if (o.popular) "Популярно" else o.duration,
                imageHeight = 150.dp,
                bonus = instance.bonus?.let { bonusBadge(instance.deal(o, 1, spending = false).earn) },
                onClick = { instance.open(o.id) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).testTag("$TAG:offer-${o.id}"),
            )
        }
        Notice("Правила въезда меняются. Перед поездкой проверьте условия для своего гражданства на evisa.gov.vn и у организатора.")
    }
}

/** The calculator's answer, at the top of the list — the first thing a person on a visa wants to know. */
@Composable
private fun StaySummary(instance: VisarunInstance, input: StayInput) {
    val entry = parseDate(input.entry)
    val s = entry?.let { stay(it, input.regime, instance.today) }
    HeroBanner(
        title = "Сколько мне ещё можно?",
        subtitle = s?.let(::stayLine) ?: "Укажите дату въезда — посчитаем последний день и подскажем визаран",
        glyph = Glyph.Calendar,
        pill = if (s == null) "Посчитать" else input.regime.label,
        onClick = instance::openCalculator,
        modifier = Modifier.testTag("$TAG:calculator"),
    )
}

private fun stayLine(s: Stay): String = when {
    s.daysLeft < 0 -> "Срок истёк ${formatDate(s.lastDay)} — это просрочка (overstay)!"
    s.daysLeft == 0L -> "Сегодня последний день пребывания"
    else -> "Последний день — ${formatDate(s.lastDay)}, осталось ${s.daysLeft} дн."
}

@Composable
private fun DetailPage(instance: VisarunInstance, id: String) {
    val o = Catalog.offer(id) ?: return
    PortalPage(
        bottomBar = {
            BarButton("Оставить заявку", { instance.book(o.id) }, Modifier.testTag("$TAG:book"), trailing = formatMoney(o.price, o.currency))
        },
    ) {
        DetailHero(o.route.glyph, o.id, pill = o.duration)
        TitleBlock(o.title, o.organiser, price = formatMoney(o.price, o.currency))
        BonusOfferCard(instance.bonus, o.bonus, earn = instance.deal(o, 1, spending = false).earn, onClick = instance::openBonus, tagPrefix = TAG, earnWhat = "за поездку")
        Spacer(Modifier.height(4.dp))
        InfoRow("Расписание", o.schedule, glyph = Glyph.Calendar)
        InfoRow("Длительность", o.duration, glyph = Glyph.Clock)
        Bullets("Включено", o.includes, Glyph.Check, Coral)
        Bullets("Не включено", o.notIncluded, Glyph.Minus, Muted)
        Bullets("Документы", o.documents, Glyph.Passport, Coral)
    }
}

@Composable
private fun Bullets(title: String, items: List<String>, glyph: Glyph, tint: Color) {
    if (items.isEmpty()) return
    SectionHeader(title)
    items.forEach {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            GlyphIcon(glyph, tint, size = 18.dp, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.width(10.dp))
            Text(it, style = AppTheme.body, color = Ink)
        }
    }
}

@Composable
private fun BookPage(instance: VisarunInstance, id: String) {
    val o = Catalog.offer(id) ?: return
    val b by instance.booking
    val ready = canSend(b)
    instance.bonus?.balance?.collectAsState()?.value
    val useBonus by instance.useBonus
    val people = b.people.toIntOrNull() ?: 1
    val deal = instance.deal(o, people, spending = useBonus)
    PortalPage {
        MiniHeader(o.title, "${o.organiser} · ${formatMoney(o.price, o.currency)}", o.route.glyph, o.id)
        FormSection("Заявка") {
            Field(b.name, { v -> instance.editBooking { it.copy(name = v) } }, "Имя и фамилия (как в паспорте)", Modifier.testTag("$TAG:name"))
            Field(b.phone, { v -> instance.editBooking { it.copy(phone = v) } }, "Телефон / WhatsApp", Modifier.testTag("$TAG:phone"))
            Field(b.date, { v -> instance.editBooking { it.copy(date = v) } }, "Желаемая дата, например 20.11", Modifier.testTag("$TAG:date"))
            Field(b.people, { v -> instance.editBooking { it.copy(people = v.filter(Char::isDigit).take(2)) } }, "Сколько человек", Modifier.testTag("$TAG:people"))
            Field(b.hotel, { v -> instance.editBooking { it.copy(hotel = v) } }, "Откуда забрать (необязательно)", Modifier.testTag("$TAG:hotel"))
        }
        val vnd = o.priceVnd(people)
        if (instance.bonus != null && o.bonus != null) {
            SectionHeader("Сумма")
            if (vnd != null) {
                BonusSpendRow(instance.bonus, o.bonus, vnd, useBonus, instance::useBonus, TAG)
                SummaryRow("Тур × $people", formatVnd(vnd))
            } else {
                SummaryRow("Тур × $people", formatMoney(o.price * people.coerceAtLeast(1), o.currency))
            }
            BonusTotals(instance.bonus, deal, TAG)
            if (vnd != null) SummaryRow("К оплате", formatVnd(deal.pay), strong = true, valueModifier = Modifier.testTag("$TAG:total"))
        }
        SectionHeader("Отправить организатору")
        if (ready) {
            ContactButtons(o.contacts, TAG, message = bookingMessage(o, b, deal), onOpened = { instance.sent(o.id) })
        } else {
            Hint("Заполните имя, телефон, дату и число людей", Modifier.testTag("$TAG:fill-in"))
        }
    }
}

@Composable
private fun CalculatorPage(instance: VisarunInstance) {
    val input by instance.stayInput
    val entry = parseDate(input.entry)
    PortalPage {
        FormSection("Дата въезда во Вьетнам") {
            Field(input.entry, { v -> instance.editStay { it.copy(entry = v) } }, "дд.мм.гггг", Modifier.testTag("$TAG:entry"), leading = Glyph.Calendar)
            if (input.entry.isNotBlank() && entry == null) {
                Text("Не похоже на дату — нужен формат 01.11.2026", style = AppTheme.label, color = AppTheme.danger)
            }
        }
        FormSection("Режим въезда") {}
        Chips(Regime.entries.map { Chip(it.name, it.label) }, input.regime.name, { k -> if (k != null) instance.editStay { it.copy(regime = Regime.valueOf(k)) } }, "$TAG:regime", allLabel = null)
        if (entry != null) {
            val s = stay(entry, input.regime, instance.today)
            Column(Modifier.fillMaxWidth().padding(top = 18.dp).testTag("$TAG:result"), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (s.daysLeft < 0) "Просрочено на" else "Осталось", style = AppTheme.label, color = Muted)
                Text(
                    "${kotlin.math.abs(s.daysLeft)} дн.",
                    style = AppTheme.title.copy(fontSize = 48.sp, lineHeight = 56.sp, fontWeight = FontWeight.Bold),
                    color = if (s.daysLeft < 7) AppTheme.danger else Coral,
                )
                Text(
                    stayLine(s),
                    style = AppTheme.label,
                    color = Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp).testTag("$TAG:left"),
                )
            }
            Spacer(Modifier.height(12.dp))
            Timeline(
                listOf(
                    Step("Въезд во Вьетнам", formatDate(entry), done = true),
                    Step("Сегодня", "день ${(instance.today - entry + 1).coerceAtLeast(0)} из ${input.regime.days}", done = s.daysLeft >= 0, current = true),
                    Step("Последний день", formatDate(s.lastDay), done = false),
                ),
            )
            if (s.daysLeft in 0..10) {
                PrimaryButton("Подобрать визаран", { instance.toRoot() }, Modifier.testTag("$TAG:pick"))
            }
        }
        Notice("День въезда считается первым днём. Это подсказка, а не юридическая консультация — точный срок указан в штампе.")
    }
}

private data class Step(val title: String, val detail: String, val done: Boolean, val current: Boolean = false)

/** "Track your order", turned to a stay: coral dots joined by a line. */
@Composable
private fun Timeline(steps: List<Step>) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp)) {
        steps.forEachIndexed { i, step ->
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(if (step.current) 20.dp else 16.dp)
                            .clip(CircleShape)
                            .background(if (step.done || step.current) Coral else Color.White)
                            .border(2.dp, if (step.done || step.current) Coral else Line, CircleShape),
                    )
                    if (i < steps.lastIndex) Box(Modifier.width(2.dp).height(34.dp).background(Line))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(step.title, style = AppTheme.body.copy(fontWeight = if (step.current) FontWeight.Bold else FontWeight.Medium), color = if (step.current) Coral else Ink)
                    Text(step.detail, style = AppTheme.footnote, color = Muted)
                }
            }
        }
    }
}

@Composable
private fun SentPage(instance: VisarunInstance) {
    PortalPage {
        Spacer(Modifier.height(24.dp))
        DoneState(
            "Заявка у организатора!",
            "Отправьте сообщение в мессенджере — организатор подтвердит место и время выезда.",
            Modifier.testTag("$TAG:sent"),
        )
        val (earned, spent) = instance.lastBonus
        BonusResult(instance.bonus, earned, spent, instance::openBonus, TAG)
        PrimaryButton("К визаранам", { instance.toRoot() }, Modifier.testTag("$TAG:done"))
    }
}
