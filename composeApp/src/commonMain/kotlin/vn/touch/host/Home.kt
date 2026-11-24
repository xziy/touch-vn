package vn.touch.host

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cx.m42.superizer.HomeBanner
import cx.m42.superizer.HostScreen
import cx.m42.superizer.Superizer
import cx.m42.superizer.app.SuperizerApp
import cx.m42.superizer.kit.Text
import cx.m42.superizer.runtime.Clock
import vn.touch.kit.Art
import vn.touch.kit.BonusBadge
import vn.touch.kit.CityBonus
import vn.touch.kit.CoinMark
import vn.touch.kit.EmptyState
import vn.touch.kit.Field
import vn.touch.kit.Glyph
import vn.touch.kit.GlyphIcon
import vn.touch.kit.Gutter
import vn.touch.kit.HeroBanner
import vn.touch.kit.ImagePill
import vn.touch.kit.PortalTheme
import vn.touch.kit.Price
import vn.touch.kit.SectionHeader
import vn.touch.kit.Tone
import vn.touch.kit.Touch
import vn.touch.kit.VND_PER_BONUS
import vn.touch.kit.WalletGradient
import vn.touch.kit.cardSurface
import vn.touch.kit.formatBonus
import vn.touch.kit.formatVnd
import vn.touch.kit.pressable

/** How Home dresses a service: its glyph, its colour, a line of what it is for. */
internal data class ServiceLook(val glyph: Glyph, val tone: Tone, val tagline: String)

internal val serviceLooks: Map<String, ServiceLook> = mapOf(
    "food" to ServiceLook(Glyph.Cutlery, Tone.Chili, "Доставка за 30 минут"),
    "flowers" to ServiceLook(Glyph.Flower, Tone.Rose, "Букеты из Далата"),
    "ads" to ServiceLook(Glyph.Tag, Tone.Violet, "Аренда, байки, работа"),
    "visarun" to ServiceLook(Glyph.Bus, Tone.Sky, "Камбоджа, Лаос, e-visa"),
    "exchange" to ServiceLook(Glyph.Swap, Tone.Jade, "Лучший курс рядом"),
)

private val fallbackLook = ServiceLook(Glyph.Sparkle, Tone.Lagoon, "")

/** One card under "Популярное". Demo picks from the services' catalogues; a tap opens the service. */
internal data class Pick(
    val id: String,
    val service: String,
    val title: String,
    val subtitle: String,
    val price: Long,
    val glyph: Glyph,
    /** The corner tag: a star with the rating, or a coin with what it earns. */
    val badge: Pair<Glyph, String>? = null,
)

internal val popularPicks: List<Pick> = listOf(
    Pick("pho-bo", "food", "Фо бо", "Phở Hưng · суп с говядиной", 55_000, Glyph.Bowl, Glyph.Star to "4.8"),
    Pick("red-25", "flowers", "25 красных роз", "Роза ветров · розы из Далата", 750_000, Glyph.Flower, Glyph.Coin to "+10%"),
    Pick("shrimp", "food", "Креветки на гриле", "Морской двор · 500 г", 250_000, Glyph.Fish, Glyph.Star to "4.6"),
    Pick("mocbai-bus", "visarun", "Мокбай — Камбоджа", "Visarun NT · автобус", 2_900_000, Glyph.Bus, Glyph.Coin to "+150"),
    Pick("spring", "flowers", "Весеннее утро", "Lotus Flowers · хризантемы", 550_000, Glyph.Leaf, Glyph.Coin to "+5%"),
)

/** A banner of the promo strip: what it says and which service it opens. */
private data class Promo(val id: String, val service: String, val title: String, val text: String, val pill: String, val glyph: Glyph, val tone: Tone)

private val promos: List<Promo> = listOf(
    Promo("visarun", "visarun", "Визаран без забот", "Мокбай и Лаос: трансфер, обед, помощь на границе", "+150 бонусов за поездку", Glyph.Route, Tone.Sky),
    Promo("flowers", "flowers", "Розы из Далата", "Соберём букет и привезём сегодня", "до 10% бонусами", Glyph.Flower, Tone.Rose),
    Promo("exchange", "exchange", "Курс лучше банка", "Пункты обмена рядом, от лучшего курса", "USD · EUR · RUB · USDT", Glyph.Swap, Tone.Jade),
)

/** "Доброе утро" by the hour in Nha Trang (UTC+7), whatever zone the device is in. */
internal fun greeting(nowMillis: Long): String {
    val hour = ((nowMillis / 3_600_000L + 7) % 24 + 24) % 24
    return when (hour) {
        in 5..11 -> "Доброе утро!"
        in 12..17 -> "Добрый день!"
        in 18..22 -> "Добрый вечер!"
        else -> "Доброй ночи!"
    }
}

/**
 * Touch VN's Home, drawn in the host's slot above the library's tiles.
 *
 * Superizer's Home is a tile grid in its own monochrome theme and offers no slot to replace it, so
 * this banner takes the whole height: the shell's Column gives its weighted tile grid what is left,
 * which is nothing. The sea header with the search, the wallet, the services as a bento of tiles,
 * the offers, the popular picks — the same apps the person has on Home (D48), in their order.
 */
internal class TouchHome(
    private val bonus: CityBonus,
    private val bonusScreen: HostScreen,
    private val clock: Clock,
    private val openApp: (String) -> Unit,
) : HomeBanner {

    @Composable
    override fun Content(superizer: Superizer, open: (HostScreen) -> Unit): Boolean {
        val home by superizer.home.collectAsState()
        val unlocked by superizer.unlocked.collectAsState()
        val registered by superizer.registry.apps.collectAsState()
        val langTag by superizer.settings.langTag.collectAsState()
        val balance by bonus.balance.collectAsState()
        val visible = remember(registered, unlocked) { superizer.registry.visible(unlocked) }
        val services = home.mapNotNull { id -> visible.firstOrNull { it.id == id } }
        val hello = remember { greeting(clock.now()) }
        PortalTheme {
            HomePage(
                hello = hello,
                balance = balance,
                services = services,
                langTag = langTag,
                onBonus = { open(bonusScreen) },
                onOpen = openApp,
            )
        }
        return true
    }
}

@Composable
private fun HomePage(
    hello: String,
    balance: Long,
    services: List<SuperizerApp<*>>,
    langTag: String,
    onBonus: () -> Unit,
    onOpen: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    val titleOf = { app: SuperizerApp<*> -> app.metadata.title.resolve(langTag) }
    val shown = services.filter { q.isEmpty() || titleOf(it).contains(q, ignoreCase = true) }
    val onHome = services.map { it.id.value }.toSet()
    val picks = popularPicks.filter { it.service in onHome }.filter {
        q.isEmpty() || it.title.contains(q, ignoreCase = true) || it.subtitle.contains(q, ignoreCase = true) ||
            services.first { s -> s.id.value == it.service }.let(titleOf).contains(q, ignoreCase = true)
    }
    val c = Touch.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.canvas)
            .verticalScroll(rememberScrollState())
            .testTag("home:page")
            .padding(bottom = 32.dp),
    ) {
        Header(hello, query, onQuery = { query = it })

        Wallet(balance, onBonus, Modifier.padding(horizontal = Gutter).padding(top = 14.dp))

        if (shown.isNotEmpty()) {
            SectionHeader("Сервисы", subtitle = "Всё, что нужно в Нячанге — в одном месте")
            Services(shown, titleOf, onOpen)
        }

        if (q.isEmpty()) {
            val offers = promos.filter { it.service in onHome }
            if (offers.isNotEmpty()) {
                SectionHeader("Спецпредложения")
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Gutter, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    offers.forEach { promo ->
                        PortalTheme(promo.tone) {
                            HeroBanner(
                                title = promo.title,
                                subtitle = promo.text,
                                glyph = promo.glyph,
                                pill = promo.pill,
                                onClick = { onOpen(promo.service) },
                                modifier = Modifier.width(316.dp).testTag("home:promo-${promo.id}"),
                                inset = 0.dp,
                            )
                        }
                    }
                }
            }
        }

        if (picks.isNotEmpty()) {
            SectionHeader("Популярное в городе")
            Column(Modifier.fillMaxWidth().padding(horizontal = Gutter), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                picks.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { pick ->
                            PortalTheme((serviceLooks[pick.service] ?: fallbackLook).tone) {
                                PickCard(pick, Modifier.weight(1f), onClick = { onOpen(pick.service) })
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        if (shown.isEmpty() && picks.isEmpty()) {
            EmptyState(
                Glyph.Search,
                "Ничего не нашлось",
                "Попробуйте «еда», «розы» или «визаран»",
                Modifier.testTag("home:nothing"),
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 28.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlyphIcon(Glyph.Waves, c.muted, size = 14.dp)
            Spacer(Modifier.width(6.dp))
            Text("Touch VN · сделано в Нячанге", style = Touch.type.micro.copy(fontWeight = FontWeight.Medium), color = c.muted)
        }
    }
}

/** The sea: the greeting, the promise, and the search floating on the edge of the water. */
@Composable
private fun Header(hello: String, query: String, onQuery: (String) -> Unit) {
    val accent = Touch.accent
    val c = Touch.colors
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(188.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(accent.gradient)
                .drawBehind {
                    // The sun over the bay, and two swells.
                    drawCircle(
                        Brush.radialGradient(listOf(Color(0x80FDE68A), Color.Transparent), center = Offset(size.width * 0.92f, size.height * 0.12f), radius = size.width * 0.42f),
                        radius = size.width * 0.42f,
                        center = Offset(size.width * 0.92f, size.height * 0.12f),
                    )
                    drawCircle(Color.White.copy(alpha = 0.07f), radius = size.width * 0.75f, center = Offset(size.width * 0.2f, size.height * 1.75f))
                    drawCircle(Color.White.copy(alpha = 0.06f), radius = size.width * 0.6f, center = Offset(size.width * 0.95f, size.height * 1.65f))
                },
        ) {
            GlyphIcon(
                Glyph.Waves,
                c.onArt.copy(alpha = 0.16f),
                size = 112.dp,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 10.dp, top = 52.dp).rotate(-6f),
            )
        }
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(start = Gutter + 4.dp, end = Gutter, top = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.18f)).padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlyphIcon(Glyph.Pin, c.onArt, size = 14.dp)
                    Spacer(Modifier.width(5.dp))
                    Text("Нячанг, центр", style = Touch.type.micro, color = c.onArt)
                }
                Spacer(Modifier.weight(1f))
                Text(hello, style = Touch.type.caption.copy(fontWeight = FontWeight.Medium), color = c.onArt.copy(alpha = 0.88f), modifier = Modifier.testTag("home:greeting"))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Всё для жизни\nв Нячанге",
                style = Touch.type.display,
                color = c.onArt,
                modifier = Modifier.padding(horizontal = Gutter + 4.dp),
            )
            Spacer(Modifier.height(22.dp))
            Box(Modifier.padding(horizontal = Gutter)) {
                Field(query, onQuery, "Еда, букет, визаран, курс…", Modifier.testTag("home:search"), leading = Glyph.Search, minHeight = 56.dp, raised = true)
            }
        }
    }
}

/** The balance as a slim card from the wallet: the coin, the figure, what it buys, and a way in. */
@Composable
private fun Wallet(balance: Long, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("host:bonus")
            .shadow(14.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x550B3B4F), spotColor = Color(0x550B3B4F))
            .clip(RoundedCornerShape(22.dp))
            .background(WalletGradient)
            .pressable(onClick = onClick)
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(listOf(Color(0x24FFFFFF), Color.Transparent), center = Offset(size.width * 0.1f, size.height * 0.5f), radius = size.height * 1.4f),
                    radius = size.height * 1.4f,
                    center = Offset(size.width * 0.1f, size.height * 0.5f),
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinMark(42.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                formatBonus(balance),
                style = Touch.type.headline.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                modifier = Modifier.testTag("host:bonus-balance"),
            )
            Text(
                "≈ ${formatVnd(balance * VND_PER_BONUS)} на заказы",
                style = Touch.type.caption,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        Row(
            Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFFBBF24)).padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Кошелёк", style = Touch.type.micro.copy(fontWeight = FontWeight.Bold), color = Color(0xFF3B2600))
            GlyphIcon(Glyph.Chevron, Color(0xFF3B2600), size = 14.dp)
        }
    }
}

/**
 * The services as a bento: the first two as large tiles with a line each, the rest three to a row.
 * Each tile wears its service's colour.
 */
@Composable
private fun Services(apps: List<SuperizerApp<*>>, titleOf: (SuperizerApp<*>) -> String, onOpen: (String) -> Unit) {
    val big = apps.take(2)
    val rest = apps.drop(2)
    Column(Modifier.fillMaxWidth().padding(horizontal = Gutter), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            big.forEach { app ->
                val look = serviceLooks[app.id.value] ?: fallbackLook
                PortalTheme(look.tone) { BigTile(titleOf(app), look, "home:service-${app.id.value}", Modifier.weight(1f)) { onOpen(app.id.value) } }
            }
            if (big.size == 1) Spacer(Modifier.weight(1f))
        }
        rest.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { app ->
                    val look = serviceLooks[app.id.value] ?: fallbackLook
                    PortalTheme(look.tone) { SmallTile(titleOf(app), look, "home:service-${app.id.value}", Modifier.weight(1f)) { onOpen(app.id.value) } }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BigTile(title: String, look: ServiceLook, tag: String, modifier: Modifier, onClick: () -> Unit) {
    val accent = Touch.accent
    val c = Touch.colors
    Box(
        modifier
            .height(150.dp)
            .testTag(tag)
            .cardSurface(RoundedCornerShape(24.dp))
            .pressable(onClick = onClick)
            .background(Brush.linearGradient(listOf(accent.soft, c.card))),
    ) {
        GlyphIcon(
            look.glyph,
            accent.fg.copy(alpha = 0.12f),
            size = 110.dp,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 0.dp, bottom = 0.dp).rotate(-12f),
        )
        Column(Modifier.fillMaxSize().padding(14.dp)) {
            Box(
                Modifier
                    .size(48.dp)
                    .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = accent.fg, spotColor = accent.fg)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.gradient),
                contentAlignment = Alignment.Center,
            ) {
                GlyphIcon(look.glyph, accent.onGradient, size = 24.dp)
            }
            Spacer(Modifier.weight(1f))
            Text(title, style = Touch.type.headline, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(look.tagline, style = Touch.type.caption, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SmallTile(title: String, look: ServiceLook, tag: String, modifier: Modifier, onClick: () -> Unit) {
    val accent = Touch.accent
    val c = Touch.colors
    Column(
        modifier
            .height(112.dp)
            .testTag(tag)
            .cardSurface(RoundedCornerShape(22.dp))
            .pressable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(accent.soft), contentAlignment = Alignment.Center) {
            GlyphIcon(look.glyph, accent.fg, size = 24.dp)
        }
        Spacer(Modifier.weight(1f))
        Text(title, style = Touch.type.caption.copy(fontWeight = FontWeight.Bold), color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** A card of "Популярное": the picture with its tag, the name, where from, the price. */
@Composable
private fun PickCard(pick: Pick, modifier: Modifier, onClick: () -> Unit) {
    val c = Touch.colors
    Column(modifier.testTag("home:pick-${pick.id}").cardSurface().pressable(onClick = onClick)) {
        Art(pick.glyph, pick.id, Modifier.fillMaxWidth().height(118.dp), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
            Box(Modifier.fillMaxSize().padding(10.dp)) {
                val serviceGlyph = (serviceLooks[pick.service] ?: fallbackLook).glyph
                Box(
                    Modifier.align(Alignment.TopStart).size(28.dp).clip(CircleShape).background(c.card),
                    contentAlignment = Alignment.Center,
                ) { GlyphIcon(serviceGlyph, Touch.accent.fg, size = 15.dp) }
                pick.badge?.let { (glyph, text) ->
                    if (glyph == Glyph.Star) {
                        ImagePill(text, Modifier.align(Alignment.BottomStart), glyph = Glyph.Star, glyphTint = c.star)
                    } else {
                        BonusBadge(text, Modifier.align(Alignment.BottomStart))
                    }
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp)) {
            Text(pick.title, style = Touch.type.strong, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(pick.subtitle, style = Touch.type.caption, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Price(formatVnd(pick.price), Modifier.weight(1f))
                Box(
                    Modifier.size(30.dp).clip(CircleShape).background(Touch.accent.gradient),
                    contentAlignment = Alignment.Center,
                ) { GlyphIcon(Glyph.Plus, Touch.accent.onGradient, size = 16.dp) }
            }
        }
    }
}
