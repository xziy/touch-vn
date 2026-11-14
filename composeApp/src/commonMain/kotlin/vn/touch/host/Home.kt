package vn.touch.host

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composeunstyled.Text
import cx.m42.superizer.HomeBanner
import cx.m42.superizer.HostScreen
import cx.m42.superizer.Superizer
import cx.m42.superizer.app.SuperizerApp
import cx.m42.superizer.runtime.Clock
import cx.m42.superizer.theme.AppTheme
import vn.touch.kit.Art
import vn.touch.kit.CityBonus
import vn.touch.kit.Field
import vn.touch.kit.Glyph
import vn.touch.kit.GlyphIcon
import vn.touch.kit.Ink
import vn.touch.kit.Muted
import vn.touch.kit.formatBonus
import vn.touch.kit.formatVnd

/** The home screen's one loud colour, after the delivery-app look it is drawn from. */
internal val Mustard: Color = Color(0xFFF4B740)
internal val MustardSoft: Color = Color(0xFFFFF3D6)
private val MustardInk: Color = Color(0xFF8A5A00)
private val Shadow: Color = Color(0x33B07A10)

/** A service as Home shows it: the kit's glyph rather than the manifest's icon, so it matches the rest. */
private val serviceGlyphs: Map<String, Glyph> = mapOf(
    "food" to Glyph.Cutlery,
    "flowers" to Glyph.Flower,
    "ads" to Glyph.Tag,
    "visarun" to Glyph.Bus,
    "exchange" to Glyph.Swap,
)

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
 * which is nothing. Greeting, search, the services as a row of pills, popular picks — the same apps
 * the person has on Home (D48), in their order.
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
        HomePage(
            hello = hello,
            balance = formatBonus(balance),
            services = services,
            langTag = langTag,
            onBonus = { open(bonusScreen) },
            onOpen = openApp,
        )
        return true
    }
}

@Composable
private fun HomePage(
    hello: String,
    balance: String,
    services: List<SuperizerApp<*>>,
    langTag: String,
    onBonus: () -> Unit,
    onOpen: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    val shown = services.filter { q.isEmpty() || it.metadata.title.resolve(langTag).contains(q, ignoreCase = true) }
    val onHome = services.map { it.id.value }.toSet()
    val picks = popularPicks.filter { it.service in onHome }.filter {
        q.isEmpty() || it.title.contains(q, ignoreCase = true) || it.subtitle.contains(q, ignoreCase = true) ||
            services.first { s -> s.id.value == it.service }.metadata.title.resolve(langTag).contains(q, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .testTag("home:page")
            .padding(bottom = 28.dp),
    ) {
        // Greeting and the bonus balance, where the mockup keeps the location pill.
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(hello, style = AppTheme.body.copy(fontSize = 15.sp), color = Muted, modifier = Modifier.weight(1f).testTag("home:greeting"))
            Row(
                modifier = Modifier
                    .testTag("host:bonus")
                    .clip(CircleShape)
                    .background(MustardSoft)
                    .clickable(role = Role.Button, onClick = onBonus)
                    .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlyphIcon(Glyph.Coin, Mustard, size = 18.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    balance,
                    style = AppTheme.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = MustardInk,
                    modifier = Modifier.testTag("host:bonus-balance"),
                )
            }
        }
        Text(
            "Всё для жизни",
            style = AppTheme.body.copy(fontSize = 28.sp, lineHeight = 34.sp),
            color = Ink,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp),
        )
        Text(
            "в Нячанге!",
            style = AppTheme.body.copy(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.ExtraBold),
            color = Ink,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Box(
            Modifier
                .padding(start = 20.dp, end = 20.dp, top = 18.dp)
                .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = Shadow, spotColor = Shadow)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            Field(query, { query = it }, "Сервис, блюдо, букет…", Modifier.testTag("home:search"), leading = Glyph.Search, minHeight = 52.dp)
        }

        if (shown.isNotEmpty()) {
            HomeSection("Сервисы")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                shown.forEachIndexed { i, app ->
                    ServicePill(
                        title = app.metadata.title.resolve(langTag),
                        glyph = serviceGlyphs[app.id.value],
                        accent = i == 0 && q.isEmpty(),
                        tag = "home:service-${app.id.value}",
                        onClick = { onOpen(app.id.value) },
                    )
                }
            }
        }

        if (picks.isNotEmpty()) {
            HomeSection("Популярное")
            picks.forEach { pick -> PickCard(pick, onClick = { onOpen(pick.service) }) }
        }

        if (shown.isEmpty() && picks.isEmpty()) {
            Text(
                "Ничего не нашлось — попробуйте «еда», «розы» или «визаран»",
                style = AppTheme.body,
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp).testTag("home:nothing"),
            )
        }
    }
}

@Composable
private fun HomeSection(title: String) {
    Text(
        title,
        style = AppTheme.body.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
        color = Ink,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 12.dp),
    )
}

/** A tall pill: the glyph in a white circle, the name under it. The first one is yellow, as in the mockup. */
@Composable
private fun ServicePill(title: String, glyph: Glyph?, accent: Boolean, tag: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(40.dp)
    Column(
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 10.dp)
            .width(92.dp)
            .shadow(if (accent) 10.dp else 8.dp, shape, ambientColor = Shadow, spotColor = Shadow)
            .clip(shape)
            .background(if (accent) Mustard else Color.White)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag)
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(if (accent) Color.White else MustardSoft),
            contentAlignment = Alignment.Center,
        ) {
            if (glyph != null) {
                GlyphIcon(glyph, if (accent) MustardInk else Ink, size = 28.dp)
            } else {
                Text(title.take(1), style = AppTheme.body.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold), color = Ink)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            title,
            style = AppTheme.body.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
    }
}

/** A card of "Most popular": picture, name, where from, price; the corner tag; a round arrow. */
@Composable
private fun PickCard(pick: Pick, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(12.dp, shape, ambientColor = Shadow, spotColor = Shadow)
            .clip(shape)
            .background(Color.White)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("home:pick-${pick.id}"),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Art(pick.glyph, pick.id, Modifier.size(88.dp), shape = CircleShape, glyphScale = 0.42f)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f).padding(end = 40.dp)) {
                Text(pick.title, style = AppTheme.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold), color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(pick.subtitle, style = AppTheme.body.copy(fontSize = 13.sp), color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Text(formatVnd(pick.price), style = AppTheme.body.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold), color = Ink)
            }
        }
        pick.badge?.let { (glyph, text) ->
            Column(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp)
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .background(Mustard)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GlyphIcon(glyph, Color.White, size = 14.dp)
                Text(text, style = AppTheme.body.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold), color = Ink)
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 14.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(Mustard),
            contentAlignment = Alignment.Center,
        ) {
            GlyphIcon(Glyph.Chevron, Color.White, size = 20.dp)
        }
    }
}
