package vn.touch.kit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cx.m42.superizer.kit.Kit
import cx.m42.superizer.kit.KitShapes
import cx.m42.superizer.kit.KitTheme

/**
 * Touch VN's look: the sea of Nha Trang for the brand, a colour per service, gold for the city
 * coins, and a cool grey canvas the white cards stand on. Everything comes in a light and a dark
 * set, picked by the host's theme (Settings → Appearance), so nothing in an app names a hex.
 *
 * Built on Superizer's ui-kit: [PortalTheme] keeps the kit's greys, type and spacing, and gives
 * its `primary` the colour of the service being drawn — a kit `Button` in Food is orange, in
 * Flowers rose, on Home the sea.
 */
@Immutable
public data class TouchColors(
    /** The page behind the cards. */
    val canvas: Color,
    val card: Color,
    /** A field, a stepper's ground, a chip at rest. */
    val field: Color,
    val ink: Color,
    val muted: Color,
    /** Hairlines and card borders. */
    val line: Color,
    /** What a light card casts; transparent in the dark, where a border does the job. */
    val shadow: Color,
    /** The city coins: the coin, its soft ground and the text on that ground. */
    val coin: Color,
    val coinSoft: Color,
    val coinInk: Color,
    val star: Color,
    val success: Color,
    val successSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    /** White over a picture or a gradient, in both themes. */
    val onArt: Color,
    val isDark: Boolean,
)

public val LightTouchColors: TouchColors = TouchColors(
    canvas = Color(0xFFF3F4F6),
    card = Color(0xFFFFFFFF),
    field = Color(0xFFF1F2F4),
    ink = Color(0xFF0B0B0F),
    muted = Color(0xFF6B6B76),
    line = Color(0xFFE6E7EB),
    shadow = Color(0x1F0F172A),
    coin = Color(0xFFF5A60B),
    coinSoft = Color(0xFFFFF4D2),
    coinInk = Color(0xFF8A4B00),
    star = Color(0xFFF5A60B),
    success = Color(0xFF15A34A),
    successSoft = Color(0xFFDCFCE7),
    danger = Color(0xFFDC2626),
    dangerSoft = Color(0xFFFEE2E2),
    onArt = Color(0xFFFFFFFF),
    isDark = false,
)

public val DarkTouchColors: TouchColors = TouchColors(
    canvas = Color(0xFF09090B),
    card = Color(0xFF17171A),
    field = Color(0xFF232327),
    ink = Color(0xFFF7F7F8),
    muted = Color(0xFFA1A1AA),
    line = Color(0xFF2A2A2F),
    shadow = Color(0x00000000),
    coin = Color(0xFFFBBF24),
    coinSoft = Color(0xFF2E2408),
    coinInk = Color(0xFFFCD34D),
    star = Color(0xFFFBBF24),
    success = Color(0xFF22C55E),
    successSoft = Color(0xFF0B2A17),
    danger = Color(0xFFF87171),
    dangerSoft = Color(0xFF3A1212),
    onArt = Color(0xFFFFFFFF),
    isDark = true,
)

/**
 * A service's colour: [fg] for its buttons, active chips and prices that matter, [soft] for the
 * ground of its icons, [from]→[to] for its header and banners. [onFg] is what is written on [fg].
 */
@Immutable
public data class Accent(
    val fg: Color,
    val onFg: Color,
    val soft: Color,
    val onSoft: Color,
    val from: Color,
    val to: Color,
) {
    /** What is written on [gradient]: white in both themes, the gradients being deep enough for it. */
    val onGradient: Color get() = Color.White

    /** The header and banner gradient, top-left to bottom-right. */
    public val gradient: Brush get() = Brush.linearGradient(listOf(from, to))
}

/** The named accents, each a light and a dark set. An app picks one; the kit knows no app. */
public enum class Tone(internal val light: Accent, internal val dark: Accent) {
    /** The brand: the sea off Tran Phu. Home, the bonus screen, anything the host draws. */
    Lagoon(
        Accent(Color(0xFF0B8C87), Color.White, Color(0xFFD5F5F0), Color(0xFF075E5A), Color(0xFF0B8C87), Color(0xFF1173B8)),
        Accent(Color(0xFF2DD4BF), Color(0xFF032B28), Color(0xFF0D2E2B), Color(0xFF5EEAD4), Color(0xFF0F9E92), Color(0xFF0E6597)),
    ),
    /** Food: chili and fire. */
    Chili(
        Accent(Color(0xFFEA580C), Color.White, Color(0xFFFFEDD5), Color(0xFF9A3412), Color(0xFFFB8B3C), Color(0xFFE2410B)),
        Accent(Color(0xFFFB923C), Color(0xFF2B1204), Color(0xFF3A1C0A), Color(0xFFFDBA74), Color(0xFFF26B1D), Color(0xFFC2410C)),
    ),
    /** Flowers. */
    Rose(
        Accent(Color(0xFFE11D58), Color.White, Color(0xFFFFE4EA), Color(0xFF9F1239), Color(0xFFFB7194), Color(0xFFDB2766)),
        Accent(Color(0xFFFB7194), Color(0xFF2E0613), Color(0xFF3A1020), Color(0xFFFDA4AF), Color(0xFFE8456F), Color(0xFFB5174A)),
    ),
    /** Classifieds. */
    Violet(
        Accent(Color(0xFF7C3AED), Color.White, Color(0xFFEDE9FE), Color(0xFF5B21B6), Color(0xFFA27BFA), Color(0xFF6D28D9)),
        Accent(Color(0xFFA78BFA), Color(0xFF1C0B3D), Color(0xFF261845), Color(0xFFC4B5FD), Color(0xFF8B5CF6), Color(0xFF6326D0)),
    ),
    /** Visa runs: the sky over the border. */
    Sky(
        Accent(Color(0xFF0272C2), Color.White, Color(0xFFE0F0FE), Color(0xFF075985), Color(0xFF38A9F8), Color(0xFF0262B0)),
        Accent(Color(0xFF38BDF8), Color(0xFF04223A), Color(0xFF0B2638), Color(0xFF7DD3FC), Color(0xFF1094D8), Color(0xFF0563A6)),
    ),
    /** Money exchange. */
    Jade(
        Accent(Color(0xFF059669), Color.White, Color(0xFFD5F7E8), Color(0xFF065F46), Color(0xFF2FC98F), Color(0xFF047857)),
        Accent(Color(0xFF34D399), Color(0xFF032619), Color(0xFF0A2C20), Color(0xFF6EE7B7), Color(0xFF13A574), Color(0xFF057556)),
    ),
    ;

    @Composable
    @ReadOnlyComposable
    public fun accent(): Accent = if (Touch.colors.isDark) dark else light
}

public val LocalTouchColors: androidx.compose.runtime.ProvidableCompositionLocal<TouchColors> =
    staticCompositionLocalOf { LightTouchColors }
public val LocalAccent: androidx.compose.runtime.ProvidableCompositionLocal<Accent> =
    staticCompositionLocalOf { Tone.Lagoon.light }

/**
 * The portal's type scale, in the kit's family: tighter and bolder than a document's, the way a
 * delivery app sets a price or a shop's name.
 */
@Immutable
public data class TouchType(
    /** A page's own headline: "Всё для жизни в Нячанге". */
    val display: TextStyle,
    /** An opened card's name, a big total. */
    val title: TextStyle,
    /** A section's heading. */
    val headline: TextStyle,
    /** A card's name. */
    val strong: TextStyle,
    val body: TextStyle,
    /** Secondary lines: a cuisine, an address, a time. */
    val caption: TextStyle,
    /** Badges and the labels under icons. */
    val micro: TextStyle,
    /** Money, wherever it is the point. */
    val price: TextStyle,
)

private fun touchType(family: FontFamily?): TouchType {
    val base = TextStyle(fontFamily = family)
    return TouchType(
        display = base.copy(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.6).sp),
        title = base.copy(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
        headline = base.copy(fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
        strong = base.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
        body = base.copy(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal),
        caption = base.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
        micro = base.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
        price = base.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold),
    )
}

private val DefaultTouchType: TouchType = touchType(FontFamily.Default)

/** Read inside any composable: `Touch.colors.canvas`, `Touch.accent.fg`, `Touch.type.title`. */
public object Touch {
    public val colors: TouchColors
        @Composable @ReadOnlyComposable get() = LocalTouchColors.current

    public val type: TouchType
        @Composable @ReadOnlyComposable get() = Kit.typography.p.fontFamily.let { family ->
            if (family == null || family == FontFamily.Default) DefaultTouchType else touchType(family)
        }

    /** The colour of the service being drawn — the sea outside any service. */
    public val accent: Accent
        @Composable @ReadOnlyComposable get() = LocalAccent.current
}

/**
 * The portal's theme around a service's screen (or Home, or the bonus screen). It follows the host's
 * light or dark choice, which the kit already carries, and recolours the kit's `primary` with
 * [tone] so the kit's own buttons, switches and tabs speak the service's colour.
 */
@Composable
public fun PortalTheme(tone: Tone = Tone.Lagoon, content: @Composable () -> Unit) {
    val dark = Kit.colors.isDark
    val colors = if (dark) DarkTouchColors else LightTouchColors
    val accent = if (dark) tone.dark else tone.light
    CompositionLocalProvider(LocalTouchColors provides colors, LocalAccent provides accent) {
        KitTheme(
            dark = dark,
            colors = Kit.colors.copy(
                primary = accent.fg,
                onPrimary = accent.onFg,
                ring = accent.fg,
                background = colors.card,
                card = colors.card,
                popover = colors.card,
            ),
            typography = Kit.typography,
            spacing = Kit.spacing,
            shapes = KitShapes(radius = 12.dp),
            content = content,
        )
    }
}

/** Accents for the colours apps still read directly: theme-aware, so dark mode needs no `if`. */
public val Ink: Color @Composable @ReadOnlyComposable get() = Touch.colors.ink
public val Muted: Color @Composable @ReadOnlyComposable get() = Touch.colors.muted
public val Line: Color @Composable @ReadOnlyComposable get() = Touch.colors.line
public val FieldFill: Color @Composable @ReadOnlyComposable get() = Touch.colors.field
/** The service's colour (the old coral, now whichever colour the service wears). */
public val Coral: Color @Composable @ReadOnlyComposable get() = Touch.accent.fg
public val CoralSoft: Color @Composable @ReadOnlyComposable get() = Touch.accent.soft
public val Sun: Color @Composable @ReadOnlyComposable get() = Touch.colors.coin
