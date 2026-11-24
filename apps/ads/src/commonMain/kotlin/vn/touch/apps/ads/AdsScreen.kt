package vn.touch.apps.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.shadow
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
import vn.touch.kit.Art
import vn.touch.kit.BarButton
import vn.touch.kit.BonusBadge
import vn.touch.kit.BonusBalanceChip
import vn.touch.kit.BonusOfferCard
import vn.touch.kit.CategoryRow
import vn.touch.kit.Chip
import vn.touch.kit.ContactButtons
import vn.touch.kit.ContactKind
import vn.touch.kit.DetailHero
import vn.touch.kit.EmptyState
import vn.touch.kit.Field
import vn.touch.kit.Glyph
import vn.touch.kit.GlyphIcon
import vn.touch.kit.Gutter
import vn.touch.kit.IconTile
import vn.touch.kit.ImagePill
import vn.touch.kit.Notice
import vn.touch.kit.PortalPage
import vn.touch.kit.SearchField
import vn.touch.kit.SecondaryButton
import vn.touch.kit.SectionHeader
import vn.touch.kit.ServiceHeader
import vn.touch.kit.Tile
import vn.touch.kit.Touch
import vn.touch.kit.bonusBadge
import vn.touch.kit.cardSurface
import vn.touch.kit.formatMoney
import vn.touch.kit.pressable

private const val TAG = AdsApp.ID

@Composable
internal fun AdsScreen(instance: AdsInstance) {
    val pages by instance.pages.collectAsState()
    SystemBackHandler(enabled = pages.size > 1) { instance.back() }
    when (val page = pages.last()) {
        AdsPage.Feed -> FeedPage(instance)
        is AdsPage.Detail -> DetailPage(instance, page.id)
        AdsPage.New -> NewPage(instance)
        AdsPage.Mine -> MinePage(instance)
    }
}

// ------------------------------------------------------------------ the feed

@Composable
private fun FeedPage(instance: AdsInstance) {
    val mine by instance.mine.collectAsState()
    val category by instance.category.collectAsState()
    val query by instance.query
    val all = instance.all(mine)
    val list = filterAds(all, category, query)
    PortalPage {
        ServiceHeader(
            title = "Найдётся всё",
            glyph = Glyph.Tag,
            eyebrow = "Объявления · Нячанг",
            subtitle = "Жильё, байки, работа — от соседей",
        ) {
            SearchField(query, instance::setQuery, "Студия, Honda, няня…", Modifier.testTag("$TAG:search"))
        }
        Spacer(Modifier.height(6.dp))
        Actions(mine.size, instance)
        BonusBalanceChip(instance.bonus, instance::openBonus, TAG)
        CategoryRow(Category.entries.map { Tile(it.name, it.short, it.glyph) }, category?.name, { k -> instance.setCategory(k?.let(Category::valueOf)) }, TAG)
        SectionHeader(
            title = when {
                category != null -> category!!.label
                query.isNotBlank() -> "Найдено"
                else -> "Свежие объявления"
            },
            subtitle = if (list.isEmpty()) null else adsCount(list.size),
        )
        if (list.isEmpty()) {
            EmptyState(Glyph.Search, "Ничего не найдено", "Попробуйте другое слово или категорию", Modifier.testTag("$TAG:empty"))
        }
        AdGrid(list, instance, showCategory = category == null)
    }
}

/** "Подать объявление" as a big card in the service's colour, and the person's own ads beside it. */
@Composable
private fun Actions(mine: Int, instance: AdsInstance) {
    val accent = Touch.accent
    val c = Touch.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier
                .weight(1f)
                .height(68.dp)
                .testTag("$TAG:new")
                .shadow(if (c.isDark) 0.dp else 10.dp, RoundedCornerShape(20.dp), ambientColor = accent.fg, spotColor = accent.fg)
                .clip(RoundedCornerShape(20.dp))
                .background(accent.gradient)
                .pressable(onClick = instance::openNew)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(c.onArt.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
                GlyphIcon(Glyph.Plus, accent.onGradient, size = 22.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Подать объявление", style = Touch.type.strong, color = accent.onGradient, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Бесплатно, за пару минут", style = Touch.type.caption, color = accent.onGradient.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (mine > 0) {
            Column(
                Modifier
                    .width(92.dp)
                    .height(68.dp)
                    .testTag("$TAG:mine")
                    .cardSurface()
                    .pressable(onClick = instance::openMine)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlyphIcon(Glyph.Person, accent.fg, size = 16.dp)
                    Spacer(Modifier.width(5.dp))
                    Text("$mine", style = Touch.type.headline.copy(fontWeight = FontWeight.ExtraBold), color = c.ink)
                }
                Text("Мои", style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold), color = c.muted)
            }
        }
    }
}

/** Two cards to a row, the picture on top and the price first — the way a marketplace lists. */
@Composable
private fun AdGrid(list: List<Ad>, instance: AdsInstance, showCategory: Boolean = true) {
    Column(Modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        list.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { ad -> AdCard(ad, instance, Modifier.weight(1f), showCategory) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AdCard(ad: Ad, instance: AdsInstance, modifier: Modifier, showCategory: Boolean) {
    val c = Touch.colors
    val earn = instance.bonus?.let { bonusBadge(ad.bonus?.earnFor(null) ?: 0) }
    Column(modifier.testTag("$TAG:ad-${ad.id}").cardSurface().pressable { instance.open(ad.id) }) {
        Art(ad.category.glyph, ad.id, Modifier.fillMaxWidth().height(128.dp), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), glyphScale = 0.36f) {
            Box(Modifier.fillMaxSize().padding(8.dp)) {
                if (ad.mine) {
                    ImagePill("На модерации", Modifier.align(Alignment.TopStart), glyph = Glyph.Clock)
                } else if (showCategory) {
                    ImagePill(ad.category.short, Modifier.align(Alignment.TopStart), glyph = ad.category.glyph)
                }
                if (earn != null) BonusBadge(earn, Modifier.align(Alignment.BottomStart))
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(ad.priceMain, style = Touch.type.price.copy(fontSize = 17.sp), color = c.ink, maxLines = 1)
                if (ad.price != null && ad.per.isNotBlank()) {
                    Text(
                        " ${shortPer(ad.per)}",
                        style = Touch.type.caption,
                        color = c.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Box(Modifier.height(42.dp)) {
                Text(ad.title, style = Touch.type.body.copy(fontWeight = FontWeight.Medium), color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${ad.district.label} · ${ago(instance.now, ad.createdAt)}",
                style = Touch.type.caption,
                color = c.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ------------------------------------------------------------------ one ad

@Composable
private fun DetailPage(instance: AdsInstance, id: String) {
    instance.mine.collectAsState().value
    val ad = instance.ad(id) ?: return
    val c = Touch.colors
    PortalPage {
        DetailHero(ad.category.glyph, ad.id, pill = if (ad.mine) "На модерации" else null)
        Column(Modifier.fillMaxWidth().padding(start = Gutter + 2.dp, end = Gutter, top = 18.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(ad.priceMain, style = Touch.type.title.copy(fontSize = 28.sp, lineHeight = 34.sp), color = c.ink)
                if (ad.price != null && ad.per.isNotBlank()) {
                    Text(" ${ad.per}", style = Touch.type.body, color = c.muted, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(ad.title, style = Touch.type.headline, color = c.ink)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlyphIcon(Glyph.Pin, c.muted, size = 15.dp)
                Spacer(Modifier.width(4.dp))
                Text(ad.district.label, style = Touch.type.caption, color = c.muted)
                Spacer(Modifier.width(14.dp))
                GlyphIcon(Glyph.Clock, c.muted, size = 15.dp)
                Spacer(Modifier.width(4.dp))
                Text(ago(instance.now, ad.createdAt), style = Touch.type.caption, color = c.muted)
            }
        }
        if (ad.mine) {
            Notice("Объявление на модерации — пока его видите только вы.", glyph = Glyph.Clock)
        }
        Card("Описание") {
            Text(ad.text, style = Touch.type.body, color = c.ink, modifier = Modifier.padding(horizontal = 16.dp))
        }
        Card {
            Fact(ad.category.glyph, "Категория", ad.category.label)
            Hairline()
            Fact(Glyph.Pin, "Район", ad.district.label)
            Hairline()
            Fact(Glyph.Clock, "Размещено", ago(instance.now, ad.createdAt))
        }
        Seller(ad)
        if (ad.mine) {
            Spacer(Modifier.height(6.dp))
            SecondaryButton("Удалить объявление", { instance.delete(ad.id) }, Modifier.testTag("$TAG:delete"), destructive = true, glyph = Glyph.Trash)
        } else {
            BonusOfferCard(instance.bonus, ad.bonus, earn = ad.bonus?.earnFor(null) ?: 0, onClick = instance::openBonus, tagPrefix = TAG, earnWhat = "за обращение через портал")
            SectionHeader("Связаться", subtitle = "Напишем от вас — текст уже готов")
            ContactButtons(
                ad.contacts,
                TAG,
                message = "Здравствуйте! Пишу по объявлению «${ad.title}» на Touch VN." + if (ad.bonus != null) " Я участник городской бонусной системы." else "",
                onOpened = { instance.contacted(ad) },
            )
            Spacer(Modifier.height(6.dp))
            Notice("Не переводите предоплату незнакомым людям. Встречайтесь в людных местах.", glyph = Glyph.Shield)
        }
    }
}

/** Who placed the ad: the initial on the service's gradient, the name, how they can be reached. */
@Composable
private fun Seller(ad: Ad) {
    val accent = Touch.accent
    val c = Touch.colors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 6.dp).cardSurface().padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(accent.gradient), contentAlignment = Alignment.Center) {
            Text(ad.author.take(1).uppercase(), style = Touch.type.headline.copy(fontWeight = FontWeight.ExtraBold), color = accent.onGradient)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(if (ad.mine) "Вы" else "Автор", style = Touch.type.micro, color = c.muted)
            Text(ad.author, style = Touch.type.strong, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                ad.contacts.joinToString(" · ") { it.kind.name.replace("Phone", "Телефон") },
                style = Touch.type.caption,
                color = c.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ------------------------------------------------------------------ a new ad

@Composable
private fun NewPage(instance: AdsInstance) {
    val d by instance.draft
    val problems = d.problems()
    val c = Touch.colors
    PortalPage(
        // What is missing sits right over the button it is holding back.
        bottomBar = {
            Column {
                if (problems.isNotEmpty()) {
                    Text(
                        "Укажите: " + problems.joinToString(", "),
                        style = Touch.type.caption,
                        color = c.muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("$TAG:problems"),
                    )
                }
                BarButton("Опубликовать", { instance.publish() }, Modifier.testTag("$TAG:publish"), enabled = problems.isEmpty(), glyph = Glyph.Check)
            }
        },
    ) {
        Spacer(Modifier.height(6.dp))
        Card("Категория", step = 1, done = d.category != null) {
            CategoryGrid(d.category) { k -> instance.editDraft { it.copy(category = k) } }
        }
        Card("Объявление", step = 2, done = d.title.trim().length >= 5 && d.text.trim().length >= 10) {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Field(d.title, { v -> instance.editDraft { it.copy(title = v) } }, "Заголовок, например «Продам шлем»", Modifier.testTag("$TAG:title"))
                Field(d.text, { v -> instance.editDraft { it.copy(text = v) } }, "Описание: состояние, размеры, условия", Modifier.testTag("$TAG:text"), singleLine = false, minHeight = 110.dp)
            }
        }
        Card("Цена", step = 3, done = d.price.any(Char::isDigit), hint = "Можно не указывать — будет «Договорная»") {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Field(d.price, { v -> instance.editDraft { it.copy(price = v) } }, "Цена", Modifier.testTag("$TAG:price"))
            }
            Spacer(Modifier.height(10.dp))
            Segments(listOf(Chip("VND", "₫ донги"), Chip("USD", "$ доллары")), d.currency, { k -> if (k != null) instance.editDraft { it.copy(currency = k) } }, "$TAG:currency")
        }
        Card("Район", step = 4, done = d.district != null) {
            Segments(District.entries.map { Chip(it.name, it.label) }, d.district?.name, { k -> instance.editDraft { it.copy(district = k?.let(District::valueOf)) } }, "$TAG:new-district")
        }
        Card("Как с вами связаться", step = 5, done = "имя" !in problems && "контакт" !in problems) {
            Segments(
                listOf(ContactKind.WhatsApp, ContactKind.Telegram, ContactKind.Zalo, ContactKind.Phone).map { Chip(it.name, it.name.replace("Phone", "Телефон")) },
                d.contactKind.name,
                { k -> if (k != null) instance.editDraft { it.copy(contactKind = ContactKind.valueOf(k)) } },
                "$TAG:new-contact",
            )
            Spacer(Modifier.height(10.dp))
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Field(d.author, { v -> instance.editDraft { it.copy(author = v) } }, "Ваше имя", Modifier.testTag("$TAG:author"), leading = Glyph.Person)
                Field(
                    d.contact,
                    { v -> instance.editDraft { it.copy(contact = v) } },
                    if (d.contactKind == ContactKind.Telegram) "@username" else "+84 …",
                    Modifier.testTag("$TAG:contact"),
                    leading = if (d.contactKind == ContactKind.Phone) Glyph.Phone else Glyph.Chat,
                )
            }
        }
        Notice("После публикации объявление пройдёт модерацию — обычно это быстро.", glyph = Glyph.Shield)
    }
}

/** The categories as a 3×2 grid of tiles, so all six are seen at once. Tags as a [CategoryRow]'s. */
@Composable
private fun CategoryGrid(selected: Category?, onSelect: (Category?) -> Unit) {
    val accent = Touch.accent
    val c = Touch.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Category.entries.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { cat ->
                    val active = cat == selected
                    val shape = RoundedCornerShape(16.dp)
                    Box(
                        Modifier
                            .weight(1f)
                            .testTag("$TAG:new-cat:chip-${cat.name}")
                            .pressable { onSelect(if (active) null else cat) },
                    ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(if (active) accent.soft else c.field)
                            .border(1.5.dp, if (active) accent.fg else Color.Transparent, shape)
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(if (active) accent.gradient else SolidColor(c.card)),
                            contentAlignment = Alignment.Center,
                        ) {
                            GlyphIcon(cat.glyph, if (active) accent.onGradient else accent.fg, size = 20.dp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            cat.short,
                            style = Touch.type.micro.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold),
                            color = if (active) accent.onSoft else c.ink,
                            maxLines = 1,
                        )
                    }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ my ads

@Composable
private fun MinePage(instance: AdsInstance) {
    val mine by instance.mine.collectAsState()
    PortalPage(bottomBar = { BarButton("Подать ещё", instance::openNew, glyph = Glyph.Plus) }) {
        if (mine.isEmpty()) {
            EmptyState(Glyph.Tag, "Пока пусто", "Здесь появятся ваши объявления")
        } else {
            Spacer(Modifier.height(6.dp))
            Notice("Объявления проходят модерацию, после неё их увидят все пользователи портала.", glyph = Glyph.Clock)
            SectionHeader("На модерации", subtitle = adsCount(mine.size))
            AdGrid(mine, instance)
        }
    }
}

// ------------------------------------------------------------------ small parts

/** A white card on the canvas with a heading; a numbered step of a form when [step] is given. */
@Composable
private fun Card(
    title: String? = null,
    step: Int? = null,
    done: Boolean = false,
    hint: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val accent = Touch.accent
    val c = Touch.colors
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .cardSurface()
            .padding(top = if (title != null) 14.dp else 6.dp, bottom = if (title != null) 16.dp else 6.dp),
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
                Column(Modifier.weight(1f)) {
                    Text(title, style = Touch.type.strong, color = c.ink)
                    if (hint != null) Text(hint, style = Touch.type.caption, color = c.muted)
                }
            }
        }
        content()
    }
}

/**
 * The options side by side in equal parts, none hidden off the edge — a [Chips] row that does not
 * scroll, with the same contract: tags `$tagPrefix:chip-<key>`, tapping the chosen one clears it.
 */
@Composable
private fun Segments(options: List<Chip>, selected: String?, onSelect: (String?) -> Unit, tagPrefix: String) {
    val accent = Touch.accent
    val c = Touch.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { o ->
            val active = o.key == selected
            val shape = RoundedCornerShape(14.dp)
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("$tagPrefix:chip-${o.key}")
                    .pressable { onSelect(if (active) null else o.key) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .background(if (active) accent.soft else c.field)
                        .border(1.5.dp, if (active) accent.fg else Color.Transparent, shape),
                )
                Text(
                    o.label,
                    style = Touch.type.caption.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold),
                    color = if (active) accent.onSoft else c.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** A line of the facts card: the glyph on its soft ground, the label, the value on the right. */
@Composable
private fun Fact(glyph: Glyph, label: String, value: String) {
    val c = Touch.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(glyph, size = 34.dp)
        Spacer(Modifier.width(12.dp))
        Text(label, style = Touch.type.body, color = c.muted, modifier = Modifier.weight(1f))
        Text(value, style = Touch.type.body.copy(fontWeight = FontWeight.SemiBold), color = c.ink, textAlign = TextAlign.End)
    }
}

@Composable
private fun Hairline() {
    Box(Modifier.fillMaxWidth().padding(start = 60.dp, end = 14.dp).height(1.dp).background(Touch.colors.line))
}

/** The price as it leads a card: the figure, or what stands in for one. */
private val Ad.priceMain: String
    get() = price?.let { formatMoney(it, currency) } ?: if (category == Category.Free) "Бесплатно" else "Договорная"

private fun shortPer(per: String): String = when (per) {
    "в месяц" -> "/ мес"
    "в сутки" -> "/ сутки"
    else -> per
}

private fun adsCount(n: Int): String {
    val word = when {
        n % 100 in 11..14 -> "объявлений"
        n % 10 == 1 -> "объявление"
        n % 10 in 2..4 -> "объявления"
        else -> "объявлений"
    }
    return "$n $word"
}
