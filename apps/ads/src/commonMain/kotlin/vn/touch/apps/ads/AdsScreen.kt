package vn.touch.apps.ads

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import cx.m42.superizer.theme.AppTheme
import cx.m42.superizer.ui.platform.SystemBackHandler
import vn.touch.kit.BarButton
import vn.touch.kit.CategoryRow
import vn.touch.kit.Chip
import vn.touch.kit.Chips
import vn.touch.kit.ContactButtons
import vn.touch.kit.ContactKind
import vn.touch.kit.DetailHero
import vn.touch.kit.EmptyState
import vn.touch.kit.Field
import vn.touch.kit.FormSection
import vn.touch.kit.Glyph
import vn.touch.kit.HeroBanner
import vn.touch.kit.InfoRow
import vn.touch.kit.Ink
import vn.touch.kit.Muted
import vn.touch.kit.ListRow
import vn.touch.kit.Notice
import vn.touch.kit.PortalPage
import vn.touch.kit.SearchField
import vn.touch.kit.SecondaryButton
import vn.touch.kit.SectionHeader
import vn.touch.kit.TextAction
import vn.touch.kit.Tile
import vn.touch.kit.TitleBlock
import vn.touch.kit.BonusBalanceChip
import vn.touch.kit.BonusOfferCard
import vn.touch.kit.bonusBadge

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

@Composable
private fun FeedPage(instance: AdsInstance) {
    val mine by instance.mine.collectAsState()
    val category by instance.category.collectAsState()
    val query by instance.query
    val all = instance.all(mine)
    val list = filterAds(all, category, query)
    PortalPage(
        bottomBar = { BarButton("Подать объявление", instance::openNew, Modifier.testTag("$TAG:new")) },
    ) {
        BonusBalanceChip(instance.bonus, instance::openBonus, TAG)
        SearchField(query, instance::setQuery, "Студия, Honda, няня…", Modifier.testTag("$TAG:search"))
        if (query.isBlank() && category == null) {
            HeroBanner(
                title = "Объявления Нячанга",
                subtitle = "Жильё, байки, работа и услуги — от соседей, без посредников",
                glyph = Glyph.Tag,
                pill = "${all.size} объявлений",
            )
        }
        CategoryRow(Category.entries.map { Tile(it.name, it.short, it.glyph) }, category?.name, { k -> instance.setCategory(k?.let(Category::valueOf)) }, TAG)
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionHeader(category?.label ?: "Свежие", Modifier.weight(1f))
            if (mine.isNotEmpty()) {
                TextAction("Мои · ${mine.size}", instance::openMine, Modifier.padding(top = 10.dp).testTag("$TAG:mine"), glyph = Glyph.Person)
            }
        }
        if (list.isEmpty()) {
            EmptyState(Glyph.Search, "Ничего не найдено", "Попробуйте другое слово или категорию", Modifier.testTag("$TAG:empty"))
        }
        list.forEach { ad -> AdRow(ad, instance) }
    }
}

@Composable
private fun AdRow(ad: Ad, instance: AdsInstance) {
    ListRow(
        title = ad.title,
        glyph = ad.category.glyph,
        seed = ad.id,
        subtitle = "${ad.district.label} · ${ago(instance.now, ad.createdAt)}",
        price = ad.priceText ?: "Договорная",
        badge = if (ad.mine) "Моё" else null,
        bonus = instance.bonus?.let { bonusBadge(ad.bonus?.earnFor(null) ?: 0) },
        onClick = { instance.open(ad.id) },
        modifier = Modifier.testTag("$TAG:ad-${ad.id}"),
    )
}

@Composable
private fun DetailPage(instance: AdsInstance, id: String) {
    instance.mine.collectAsState().value
    val ad = instance.ad(id) ?: return
    PortalPage {
        DetailHero(ad.category.glyph, ad.id, pill = ad.category.label, pillGlyph = Glyph.Tag)
        TitleBlock(ad.title, "${ad.district.label} · ${ago(instance.now, ad.createdAt)}", price = ad.priceText ?: "Цена договорная")
        Text(ad.text, style = AppTheme.body, color = Ink, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp))
        InfoRow("Автор", ad.author, glyph = Glyph.Person)
        InfoRow("Район", ad.district.label, glyph = Glyph.Pin)
        if (ad.mine) {
            Notice("Объявление на модерации — пока его видите только вы.")
            SecondaryButton("Удалить объявление", { instance.delete(ad.id) }, Modifier.testTag("$TAG:delete"), destructive = true)
        } else {
            BonusOfferCard(instance.bonus, ad.bonus, earn = ad.bonus?.earnFor(null) ?: 0, onClick = instance::openBonus, tagPrefix = TAG, earnWhat = "за обращение через портал")
            SectionHeader("Связаться")
            ContactButtons(
                ad.contacts,
                TAG,
                message = "Здравствуйте! Пишу по объявлению «${ad.title}» на Touch VN." + if (ad.bonus != null) " Я участник городской бонусной системы." else "",
                onOpened = { instance.contacted(ad) },
            )
            Notice("Не переводите предоплату незнакомым людям. Встречайтесь в людных местах.")
        }
    }
}

@Composable
private fun NewPage(instance: AdsInstance) {
    val d by instance.draft
    val problems = d.problems()
    PortalPage(
        // What is missing sits right over the button it is holding back.
        bottomBar = {
            Column {
                if (problems.isNotEmpty()) {
                    Text(
                        "Укажите: " + problems.joinToString(", "),
                        style = AppTheme.footnote,
                        color = Muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("$TAG:problems"),
                    )
                }
                BarButton("Опубликовать", { instance.publish() }, Modifier.testTag("$TAG:publish"), enabled = problems.isEmpty())
            }
        },
    ) {
        SectionHeader("Категория")
        CategoryRow(
            Category.entries.map { Tile(it.name, it.short, it.glyph) },
            d.category?.name,
            { k -> instance.editDraft { it.copy(category = k?.let(Category::valueOf)) } },
            "$TAG:new-cat",
            allLabel = null,
        )
        FormSection("Объявление") {
            Field(d.title, { v -> instance.editDraft { it.copy(title = v) } }, "Заголовок", Modifier.testTag("$TAG:title"))
            Field(d.text, { v -> instance.editDraft { it.copy(text = v) } }, "Описание", Modifier.testTag("$TAG:text"), singleLine = false, minHeight = 110.dp)
            Field(d.price, { v -> instance.editDraft { it.copy(price = v) } }, "Цена (необязательно)", Modifier.testTag("$TAG:price"))
        }
        Chips(listOf(Chip("VND", "₫ донги"), Chip("USD", "$ доллары")), d.currency, { k -> if (k != null) instance.editDraft { it.copy(currency = k) } }, "$TAG:currency", allLabel = null)
        FormSection("Район") {}
        Chips(District.entries.map { Chip(it.name, it.label) }, d.district?.name, { k -> instance.editDraft { it.copy(district = k?.let(District::valueOf)) } }, "$TAG:new-district", allLabel = null)
        FormSection("Как с вами связаться") {}
        Chips(
            listOf(ContactKind.WhatsApp, ContactKind.Telegram, ContactKind.Zalo, ContactKind.Phone).map { Chip(it.name, it.name.replace("Phone", "Телефон")) },
            d.contactKind.name,
            { k -> if (k != null) instance.editDraft { it.copy(contactKind = ContactKind.valueOf(k)) } },
            "$TAG:new-contact",
            allLabel = null,
        )
        FormSection("Контакты") {
            Field(d.author, { v -> instance.editDraft { it.copy(author = v) } }, "Ваше имя", Modifier.testTag("$TAG:author"))
            Field(
                d.contact,
                { v -> instance.editDraft { it.copy(contact = v) } },
                if (d.contactKind == ContactKind.Telegram) "@username" else "+84 …",
                Modifier.testTag("$TAG:contact"),
            )
        }
    }
}

@Composable
private fun MinePage(instance: AdsInstance) {
    val mine by instance.mine.collectAsState()
    PortalPage(bottomBar = { BarButton("Подать ещё", instance::openNew) }) {
        if (mine.isEmpty()) {
            EmptyState(Glyph.Tag, "Пока пусто", "Здесь появятся ваши объявления")
        } else {
            Notice("Объявления проходят модерацию, после неё их увидят все пользователи портала.")
            Spacer(Modifier.height(4.dp))
            mine.forEach { AdRow(it, instance) }
        }
    }
}
