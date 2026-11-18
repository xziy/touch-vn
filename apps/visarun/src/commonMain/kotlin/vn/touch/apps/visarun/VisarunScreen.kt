package vn.touch.apps.visarun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cx.m42.superizer.kit.Text
import cx.m42.superizer.ui.platform.SystemBackHandler
import kotlin.math.abs
import vn.touch.kit.Art
import vn.touch.kit.BarButton
import vn.touch.kit.Badge
import vn.touch.kit.BonusBadge
import vn.touch.kit.BonusBalanceChip
import vn.touch.kit.BonusOfferCard
import vn.touch.kit.BonusResult
import vn.touch.kit.BonusSpendRow
import vn.touch.kit.BonusTotals
import vn.touch.kit.CategoryRow
import vn.touch.kit.ContactButtons
import vn.touch.kit.DetailHero
import vn.touch.kit.DoneState
import vn.touch.kit.Field
import vn.touch.kit.Glyph
import vn.touch.kit.GlyphIcon
import vn.touch.kit.Gutter
import vn.touch.kit.Hint
import vn.touch.kit.IconTile
import vn.touch.kit.ImagePill
import vn.touch.kit.MiniHeader
import vn.touch.kit.Notice
import vn.touch.kit.PortalPage
import vn.touch.kit.PrimaryButton
import vn.touch.kit.SectionHeader
import vn.touch.kit.ServiceHeader
import vn.touch.kit.SummaryRow
import vn.touch.kit.Tile
import vn.touch.kit.Touch
import vn.touch.kit.bonusBadge
import vn.touch.kit.cardSurface
import vn.touch.kit.formatMoney
import vn.touch.kit.formatVnd
import vn.touch.kit.pressable

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

// ------------------------------------------------------------------ the list

@Composable
private fun ListPage(instance: VisarunInstance) {
    val route by instance.route.collectAsState()
    val stayInput by instance.stayInput
    val offers = Catalog.offers.filter { route == null || it.route == route }
    PortalPage {
        ServiceHeader(
            title = "Продлим пребывание",
            glyph = Glyph.Passport,
            eyebrow = "Визаран · из Нячанга",
            subtitle = "Камбоджа, Лаос, перелёт или e-visa",
        ) {
            StaySummary(instance, stayInput)
        }
        Spacer(Modifier.height(6.dp))
        BonusBalanceChip(instance.bonus, instance::openBonus, TAG)
        CategoryRow(Route.entries.map { Tile(it.name, it.short, it.glyph) }, route?.name, { k -> instance.filter(k?.let(Route::valueOf)) }, TAG)
        SectionHeader(route?.label ?: "Визараны из Нячанга", subtitle = offersCount(offers.size))
        offers.forEach { o -> OfferCard(o, instance) }
        Spacer(Modifier.height(4.dp))
        Notice("Правила въезда меняются. Перед поездкой проверьте условия для своего гражданства на evisa.gov.vn и у организатора.")
    }
}

/**
 * The calculator's answer, on the header's edge — the first thing a person on a visa wants to
 * know: the days left, the last day, and how much of the stay is behind.
 */
@Composable
private fun StaySummary(instance: VisarunInstance, input: StayInput) {
    val c = Touch.colors
    val accent = Touch.accent
    val entry = parseDate(input.entry)
    val s = entry?.let { stay(it, input.regime, instance.today) }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .testTag("$TAG:calculator")
            .cardSurface(elevation = 14.dp)
            .pressable(onClick = instance::openCalculator)
            .padding(16.dp),
    ) {
        if (s == null || entry == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Glyph.Calendar, size = 48.dp, filled = true)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Сколько мне ещё можно?", style = Touch.type.strong, color = c.ink)
                    Text("Укажите дату въезда — посчитаем последний день и подскажем визаран", style = Touch.type.caption, color = c.muted)
                }
                Spacer(Modifier.width(8.dp))
                GlyphIcon(Glyph.Chevron, c.muted, size = 20.dp)
            }
        } else {
            val tint = stayTint(s)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Срок пребывания · ${input.regime.label}", style = Touch.type.micro, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${abs(s.daysLeft)}", style = Touch.type.display.copy(fontSize = 36.sp, lineHeight = 40.sp), color = tint)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (s.daysLeft < 0) "дн. просрочки" else "дн. осталось",
                            style = Touch.type.strong,
                            color = c.ink,
                            modifier = Modifier.padding(bottom = 5.dp),
                        )
                    }
                }
                Row(
                    Modifier.clip(RoundedCornerShape(50)).background(accent.soft).padding(start = 12.dp, end = 6.dp, top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Пересчитать", style = Touch.type.micro, color = accent.onSoft)
                    GlyphIcon(Glyph.Chevron, accent.onSoft, size = 14.dp)
                }
            }
            Spacer(Modifier.height(10.dp))
            StayBar(entry, s, input.regime, instance.today, height = 8.dp)
            Spacer(Modifier.height(8.dp))
            Text(stayLine(s), style = Touch.type.caption, color = c.muted)
        }
    }
}

/** An offer as a rich card: the picture with its tags, the route, the dates, the price large. */
@Composable
private fun OfferCard(o: Offer, instance: VisarunInstance) {
    val c = Touch.colors
    val accent = Touch.accent
    val earn = instance.bonus?.let { bonusBadge(instance.deal(o, 1, spending = false).earn) }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 7.dp)
            .testTag("$TAG:offer-${o.id}")
            .cardSurface()
            .pressable { instance.open(o.id) },
    ) {
        Art(o.route.glyph, o.id, Modifier.fillMaxWidth().height(136.dp), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), glyphScale = 0.4f) {
            Box(Modifier.fillMaxSize().padding(10.dp)) {
                ImagePill(o.route.label, Modifier.align(Alignment.BottomStart), glyph = o.route.glyph)
                if (o.popular) ImagePill("Популярно", Modifier.align(Alignment.TopStart), glyph = Glyph.Flame, glyphTint = c.danger)
                if (earn != null) BonusBadge(earn, Modifier.align(Alignment.TopEnd))
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp)) {
            Text(o.title, style = Touch.type.strong.copy(fontSize = 17.sp), color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(o.organiser, style = Touch.type.caption, color = c.muted, maxLines = 1)
            Spacer(Modifier.height(10.dp))
            Meta(Glyph.Calendar, o.schedule)
            Spacer(Modifier.height(4.dp))
            Meta(Glyph.Timer, o.duration)
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(formatMoney(o.price, o.currency), style = Touch.type.title.copy(fontSize = 21.sp, lineHeight = 26.sp), color = c.ink)
                    Text(perWhat(o), style = Touch.type.caption, color = c.muted)
                }
                Row(
                    Modifier.clip(RoundedCornerShape(50)).background(accent.gradient).padding(start = 14.dp, end = 10.dp, top = 9.dp, bottom = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Подробнее", style = Touch.type.caption.copy(fontWeight = FontWeight.Bold), color = accent.onGradient)
                    Spacer(Modifier.width(2.dp))
                    GlyphIcon(Glyph.Chevron, accent.onGradient, size = 16.dp)
                }
            }
        }
    }
}

// ------------------------------------------------------------------ an offer

@Composable
private fun DetailPage(instance: VisarunInstance, id: String) {
    val o = Catalog.offer(id) ?: return
    val c = Touch.colors
    val accent = Touch.accent
    PortalPage(
        bottomBar = {
            BarButton("Оставить заявку", { instance.book(o.id) }, Modifier.testTag("$TAG:book"), trailing = formatMoney(o.price, o.currency))
        },
    ) {
        DetailHero(o.route.glyph, o.id, pill = o.route.label, pillGlyph = o.route.glyph)
        Column(Modifier.fillMaxWidth().padding(start = Gutter + 2.dp, end = Gutter, top = 16.dp, bottom = 4.dp)) {
            if (o.popular) {
                Badge("Популярно", glyph = Glyph.Flame)
                Spacer(Modifier.height(8.dp))
            }
            Text(o.title, style = Touch.type.title, color = c.ink)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlyphIcon(Glyph.Verified, accent.fg, size = 16.dp)
                Spacer(Modifier.width(5.dp))
                Text(o.organiser, style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold), color = c.muted)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatMoney(o.price, o.currency), style = Touch.type.title.copy(fontSize = 28.sp, lineHeight = 34.sp), color = accent.fg)
                Spacer(Modifier.width(6.dp))
                Text(perWhat(o), style = Touch.type.body, color = c.muted, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
        Card {
            Fact(Glyph.Calendar, "Расписание", o.schedule)
            Hairline()
            Fact(Glyph.Timer, "Длительность", o.duration)
            Hairline()
            Fact(o.route.glyph, "Направление", o.route.label)
        }
        BonusOfferCard(instance.bonus, o.bonus, earn = instance.deal(o, 1, spending = false).earn, onClick = instance::openBonus, tagPrefix = TAG, earnWhat = "за поездку")
        Bullets("Включено", o.includes, Glyph.Check, c.success, c.successSoft)
        Bullets("Не включено", o.notIncluded, Glyph.Minus, c.muted, c.field)
        Bullets("Документы", o.documents, Glyph.Passport, accent.fg, accent.soft)
        Spacer(Modifier.height(4.dp))
        Notice("Заявка уходит организатору в мессенджер — он подтвердит место и время выезда.", glyph = Glyph.Chat)
    }
}

@Composable
private fun Bullets(title: String, items: List<String>, glyph: Glyph, tint: Color, ground: Color) {
    if (items.isEmpty()) return
    Card(title) {
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(26.dp).clip(CircleShape).background(ground), contentAlignment = Alignment.Center) {
                        GlyphIcon(glyph, tint, size = 15.dp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(it, style = Touch.type.body, color = Touch.colors.ink, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ------------------------------------------------------------------ the request

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
        Card("Кто едет", step = 1, done = b.name.isNotBlank() && b.phone.count(Char::isDigit) >= 9) {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Field(b.name, { v -> instance.editBooking { it.copy(name = v) } }, "Имя и фамилия (как в паспорте)", Modifier.testTag("$TAG:name"), leading = Glyph.Person)
                Field(b.phone, { v -> instance.editBooking { it.copy(phone = v) } }, "Телефон / WhatsApp", Modifier.testTag("$TAG:phone"), leading = Glyph.Phone)
            }
        }
        Card("Поездка", step = 2, done = b.date.isNotBlank() && (b.people.toIntOrNull() ?: 0) > 0) {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Field(b.date, { v -> instance.editBooking { it.copy(date = v) } }, "Желаемая дата, например 20.11", Modifier.testTag("$TAG:date"), leading = Glyph.Calendar)
                Field(b.people, { v -> instance.editBooking { it.copy(people = v.filter(Char::isDigit).take(2)) } }, "Сколько человек", Modifier.testTag("$TAG:people"), leading = Glyph.Person)
                Field(b.hotel, { v -> instance.editBooking { it.copy(hotel = v) } }, "Откуда забрать (необязательно)", Modifier.testTag("$TAG:hotel"), leading = Glyph.Pin)
            }
        }
        val vnd = o.priceVnd(people)
        if (instance.bonus != null && o.bonus != null) {
            SectionHeader("Сумма")
            if (vnd != null) BonusSpendRow(instance.bonus, o.bonus, vnd, useBonus, instance::useBonus, TAG)
            Card {
                if (vnd != null) {
                    SummaryRow("Тур × $people", formatVnd(vnd))
                } else {
                    SummaryRow("Тур × $people", formatMoney(o.price * people.coerceAtLeast(1), o.currency))
                }
                BonusTotals(instance.bonus, deal, TAG)
                if (vnd != null) SummaryRow("К оплате", formatVnd(deal.pay), strong = true, valueModifier = Modifier.testTag("$TAG:total"))
            }
        } else {
            Card {
                SummaryRow("Тур × $people", formatMoney(o.price * people.coerceAtLeast(1), o.currency), strong = true)
            }
        }
        SectionHeader("Отправить организатору", subtitle = "Откроем мессенджер с готовой заявкой")
        if (ready) {
            ContactButtons(o.contacts, TAG, message = bookingMessage(o, b, deal), onOpened = { instance.sent(o.id) })
        } else {
            Hint("Заполните имя, телефон, дату и число людей", Modifier.testTag("$TAG:fill-in"))
        }
    }
}

// ------------------------------------------------------------------ the calculator

@Composable
private fun CalculatorPage(instance: VisarunInstance) {
    val input by instance.stayInput
    val entry = parseDate(input.entry)
    val c = Touch.colors
    PortalPage {
        Spacer(Modifier.height(6.dp))
        Card("Дата въезда во Вьетнам") {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Field(input.entry, { v -> instance.editStay { it.copy(entry = v) } }, "дд.мм.гггг", Modifier.testTag("$TAG:entry"), leading = Glyph.Calendar)
                if (input.entry.isNotBlank() && entry == null) {
                    Text("Не похоже на дату — нужен формат 01.11.2026", style = Touch.type.caption, color = c.danger, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Режим въезда", style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold), color = c.muted, modifier = Modifier.padding(horizontal = 18.dp))
            Spacer(Modifier.height(2.dp))
            Spacer(Modifier.height(6.dp))
            RegimePicker(input.regime) { r -> instance.editStay { it.copy(regime = r) } }
        }
        if (entry != null) {
            val s = stay(entry, input.regime, instance.today)
            StayResult(entry, s, input.regime, instance.today)
            if (s.daysLeft in 0..10) {
                PrimaryButton("Подобрать визаран", { instance.toRoot() }, Modifier.testTag("$TAG:pick"), glyph = Glyph.Bus)
            }
        }
        SectionHeader("Как считается срок")
        Rule(Glyph.Calendar, "День въезда — первый день", "45 дней с 1 марта заканчиваются 14 апреля.")
        Rule(Glyph.Passport, "Точная дата — в штампе", "Сверьтесь со штампом о въезде или с вашей e-visa.")
        Rule(Glyph.Bus, "Выезжайте с запасом", "Визаран лучше планировать за 3–5 дней до конца срока.")
        Rule(Glyph.Shield, "Просрочка — это штраф", "Overstay оформляют на выезде и могут не пустить снова.")
        Spacer(Modifier.height(4.dp))
        Notice("Это подсказка, а не юридическая консультация.")
    }
}

/**
 * The regimes side by side — the days large, the kind under them — none hidden off the edge.
 * Tags as a chip row's: `visarun:regime:chip-<name>`.
 */
@Composable
private fun RegimePicker(selected: Regime, onSelect: (Regime) -> Unit) {
    val accent = Touch.accent
    val c = Touch.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Regime.entries.forEach { r ->
            val active = r == selected
            val shape = RoundedCornerShape(16.dp)
            Box(Modifier.weight(1f).testTag("$TAG:regime:chip-${r.name}").pressable { onSelect(r) }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (active) accent.gradient else SolidColor(c.field))
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("${r.days} дней", style = Touch.type.strong, color = if (active) accent.onGradient else c.ink, maxLines = 1)
                Text(
                    if (r == Regime.EVisa90) "e-visa" else "безвиз",
                    style = Touch.type.micro.copy(fontWeight = FontWeight.Medium),
                    color = if (active) accent.onGradient.copy(alpha = 0.85f) else c.muted,
                )
            }
            }
        }
    }
}

/** The answer, large: the days, the sentence, and the stay as a bar from entry to the last day. */
@Composable
private fun StayResult(entry: Long, s: Stay, regime: Regime, today: Long) {
    val c = Touch.colors
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .testTag("$TAG:result")
            .cardSurface()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(if (s.daysLeft < 0) "Просрочено на" else "Осталось", style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold), color = c.muted)
        Text(
            "${abs(s.daysLeft)} дн.",
            style = Touch.type.display.copy(fontSize = 56.sp, lineHeight = 62.sp, letterSpacing = (-1.5).sp),
            color = stayTint(s),
        )
        Text(
            stayLine(s),
            style = Touch.type.body.copy(fontWeight = FontWeight.Medium),
            color = c.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("$TAG:left"),
        )
        Spacer(Modifier.height(18.dp))
        StayBar(entry, s, regime, today, height = 10.dp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Въезд", style = Touch.type.micro, color = c.muted)
                Text(formatDate(entry), style = Touch.type.caption.copy(fontWeight = FontWeight.Bold), color = c.ink)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Сегодня", style = Touch.type.micro, color = c.muted)
                Text(
                    "день ${(today - entry + 1).coerceAtLeast(0)} из ${regime.days}",
                    style = Touch.type.caption.copy(fontWeight = FontWeight.Bold),
                    color = c.ink,
                )
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Последний день", style = Touch.type.micro, color = c.muted)
                Text(formatDate(s.lastDay), style = Touch.type.caption.copy(fontWeight = FontWeight.Bold), color = stayTint(s))
            }
        }
    }
}

/** How much of the stay is behind: the service's gradient, turning red in the last week. */
@Composable
private fun StayBar(entry: Long, s: Stay, regime: Regime, today: Long, height: androidx.compose.ui.unit.Dp) {
    val c = Touch.colors
    val accent = Touch.accent
    val used = ((today - entry + 1).toFloat() / regime.days).coerceIn(0.03f, 1f)
    val fill: Brush = if (s.daysLeft < 7) SolidColor(c.danger) else accent.gradient
    Box(Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(50)).background(c.field)) {
        Box(Modifier.fillMaxWidth(used).fillMaxHeight().clip(RoundedCornerShape(50)).background(fill))
    }
}

@Composable
private fun Rule(glyph: Glyph, title: String, text: String) {
    val c = Touch.colors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 5.dp).cardSurface(elevation = 6.dp).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(glyph, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = Touch.type.strong.copy(fontSize = 15.sp), color = c.ink)
            Text(text, style = Touch.type.caption, color = c.muted)
        }
    }
}

// ------------------------------------------------------------------ sent

@Composable
private fun SentPage(instance: VisarunInstance) {
    PortalPage {
        Spacer(Modifier.height(12.dp))
        DoneState(
            "Заявка у организатора!",
            "Отправьте сообщение в мессенджере — организатор подтвердит место и время выезда.",
            Modifier.testTag("$TAG:sent"),
        )
        val (earned, spent) = instance.lastBonus
        BonusResult(instance.bonus, earned, spent, instance::openBonus, TAG)
        Card("Что дальше") {
            Step(1, "Организатор ответит в мессенджере")
            Step(2, "Подтвердит дату, место и время выезда")
            Step(3, "Возьмите паспорт и наличные USD на сборы", last = true)
        }
        Spacer(Modifier.height(6.dp))
        PrimaryButton("К визаранам", { instance.toRoot() }, Modifier.testTag("$TAG:done"))
    }
}

@Composable
private fun Step(n: Int, text: String, last: Boolean = false) {
    val accent = Touch.accent
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = if (last) 0.dp else 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp).clip(CircleShape).background(accent.soft), contentAlignment = Alignment.Center) {
            Text("$n", style = Touch.type.micro.copy(fontWeight = FontWeight.ExtraBold), color = accent.onSoft)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = Touch.type.body, color = Touch.colors.ink, modifier = Modifier.weight(1f))
    }
}

// ------------------------------------------------------------------ small parts

private fun stayLine(s: Stay): String = when {
    s.daysLeft < 0 -> "Срок истёк ${formatDate(s.lastDay)} — это просрочка (overstay)!"
    s.daysLeft == 0L -> "Сегодня последний день пребывания"
    else -> "Последний день — ${formatDate(s.lastDay)}, осталось ${s.daysLeft} дн."
}

@Composable
private fun stayTint(s: Stay): Color = if (s.daysLeft < 7) Touch.colors.danger else Touch.accent.fg

private fun perWhat(o: Offer): String = if (o.route == Route.Help) "за оформление" else "за человека"

private fun offersCount(n: Int): String {
    val word = when {
        n % 100 in 11..14 -> "предложений"
        n % 10 == 1 -> "предложение"
        n % 10 in 2..4 -> "предложения"
        else -> "предложений"
    }
    return "$n $word"
}

@Composable
private fun Meta(glyph: Glyph, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GlyphIcon(glyph, Touch.accent.fg, size = 15.dp)
        Spacer(Modifier.width(8.dp))
        Text(text, style = Touch.type.caption, color = Touch.colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** A white card on the canvas with a heading; a numbered step of a form when [step] is given. */
@Composable
private fun Card(
    title: String? = null,
    step: Int? = null,
    done: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val accent = Touch.accent
    val c = Touch.colors
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .cardSurface()
            .padding(top = if (title != null) 14.dp else 8.dp, bottom = if (title != null) 16.dp else 8.dp),
    ) {
        if (title != null) {
            Row(Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (step != null) {
                    Box(
                        Modifier.size(26.dp).clip(CircleShape).background(if (done) accent.gradient else SolidColor(accent.soft)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (done) {
                            GlyphIcon(Glyph.Check, accent.onGradient, size = 15.dp)
                        } else {
                            Text("$step", style = Touch.type.micro.copy(fontWeight = FontWeight.ExtraBold), color = accent.onSoft)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                }
                Text(title, style = Touch.type.strong, color = c.ink)
            }
        }
        content()
    }
}

/** A fact of an offer: the glyph on its soft ground, the label over the value. */
@Composable
private fun Fact(glyph: Glyph, label: String, value: String) {
    val c = Touch.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(glyph, size = 38.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = Touch.type.caption, color = c.muted)
            Text(value, style = Touch.type.body.copy(fontWeight = FontWeight.SemiBold), color = c.ink)
        }
    }
}

@Composable
private fun Hairline() {
    Box(Modifier.fillMaxWidth().padding(start = 64.dp, end = 14.dp).height(1.dp).background(Touch.colors.line))
}
