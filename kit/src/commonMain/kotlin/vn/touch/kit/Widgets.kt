package vn.touch.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composeunstyled.Text
import cx.m42.superizer.runtime.LocalAppRuntime
import cx.m42.superizer.theme.AppTheme
import cx.m42.superizer.theme.LightTokens
import cx.m42.superizer.theme.SuperizerTheme
import cx.m42.superizer.theme.Tokens
import cx.m42.superizer.ui.components.AppButton
import cx.m42.superizer.ui.components.ButtonSize
import cx.m42.superizer.ui.components.ButtonVariant

// ------------------------------------------------------------------ palette and theme

/** The portal's one loud colour: buttons, prices that matter, the selected tab. */
public val Coral: Color = Color(0xFFF25C54)
public val CoralSoft: Color = Color(0xFFFFEEEC)
/** The illustrations' second colour — the ring around a "done" tick, a sparkle. */
public val Sun: Color = Color(0xFFFFC542)
public val Ink: Color = Color(0xFF1C1C1E)
public val Muted: Color = Color(0xFF8A8A8E)
public val Line: Color = Color(0xFFEFEFF2)
public val FieldFill: Color = Color(0xFFF5F5F7)

/**
 * The host's monochrome tokens, recoloured: white pages, near-black text, coral where a person
 * should look. Every service wraps its content in [PortalTheme], so AppButton and friends from
 * the Superizer kit come out coral too.
 */
public val PortalTokens: Tokens = LightTokens.copy(
    foreground = Ink,
    primary = Coral,
    primaryForeground = Color.White,
    muted = Muted,
    border = Line,
    surface = FieldFill,
    pageBackground = Color.White,
    accent = Coral,
    accentSubtle = CoralSoft,
    radius = 14.dp,
    header = LightTokens.header.copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    subtitle = LightTokens.subtitle.copy(fontWeight = FontWeight.Bold),
    buttonLabel = LightTokens.buttonLabel.copy(fontWeight = FontWeight.SemiBold),
)

@Composable
public fun PortalTheme(content: @Composable () -> Unit) {
    SuperizerTheme(PortalTokens, content)
}

// ------------------------------------------------------------------ pictures

private val palettes: List<Pair<Color, Color>> = listOf(
    Color(0xFFFFD6C2) to Color(0xFFFF9A7A), // peach
    Color(0xFFFFCFCB) to Color(0xFFF2766E), // coral
    Color(0xFFFFEBB0) to Color(0xFFFFBE3B), // sun
    Color(0xFFCDEFD9) to Color(0xFF5FC48C), // mint
    Color(0xFFD3E7FF) to Color(0xFF6FA8EC), // sky
    Color(0xFFE6DCFF) to Color(0xFFA48BEF), // lilac
)

/**
 * A picture where a photo would go: a warm gradient, two soft circles and a big white glyph.
 * [seed] picks the gradient, so the same restaurant always gets the same colours.
 */
@Composable
public fun Art(
    glyph: Glyph,
    seed: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    glyphScale: Float = 0.46f,
    content: @Composable () -> Unit = {},
) {
    val (top, bottom) = palettes[(seed.hashCode() and 0x7fffffff) % palettes.size]
    BoxWithConstraints(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(top, bottom)))
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.22f), radius = size.minDimension * 0.42f, center = Offset(size.width * 0.9f, size.height * 0.1f))
                drawCircle(Color.White.copy(alpha = 0.14f), radius = size.minDimension * 0.3f, center = Offset(size.width * 0.08f, size.height * 0.95f))
            },
        contentAlignment = Alignment.Center,
    ) {
        val side = if (maxWidth < maxHeight) maxWidth else maxHeight
        GlyphIcon(glyph, Color.White.copy(alpha = 0.95f), size = side * glyphScale)
        content()
    }
}

// ------------------------------------------------------------------ page

/**
 * A white page that scrolls as one. A [bottomBar] stays pinned under it — "В корзину", "Оформить".
 */
@Composable
public fun PortalPage(
    modifier: Modifier = Modifier,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    // The bar takes its own place under the scrolling part rather than floating over it, so
    // nothing scrolled into view can end up hidden — or tapped — behind it.
    Column(modifier = modifier.fillMaxSize().background(AppTheme.pageBackground)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 4.dp, bottom = 24.dp),
            content = content,
        )
        if (bottomBar != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .drawBehind { drawLine(Line, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx()) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) { bottomBar() }
        }
    }
}

/** A heading between blocks, with an optional coral action on the right ("Все"). */
@Composable
public fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = AppTheme.header.copy(fontSize = 18.sp), color = Ink, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(
                action,
                style = AppTheme.label.copy(fontWeight = FontWeight.SemiBold),
                color = Coral,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onAction).padding(4.dp),
            )
        }
    }
}

/** Muted centred text: an empty list, a reminder. */
@Composable
public fun Hint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = AppTheme.body,
        color = Muted,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
    )
}

/** A soft coral box for what the reader must not miss: a disclaimer, a rule. */
@Composable
public fun Notice(text: String, modifier: Modifier = Modifier, glyph: Glyph = Glyph.Info) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CoralSoft)
            .padding(12.dp),
    ) {
        GlyphIcon(glyph, Coral, size = 18.dp, modifier = Modifier.padding(top = 1.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = AppTheme.label, color = Ink, modifier = Modifier.weight(1f))
    }
}

/** A white rounded block with a hairline border. Tappable when [onClick] is given. */
@Composable
public fun PortalCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Line, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content,
    )
}

@Composable
public fun Divider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(Line))
}

// ------------------------------------------------------------------ small parts

/** A coral star and the score. */
@Composable
public fun Rating(value: Double, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        GlyphIcon(Glyph.Star, Coral, size = 14.dp)
        Spacer(Modifier.width(3.dp))
        Text(value.toString(), style = AppTheme.footnote.copy(fontWeight = FontWeight.SemiBold), color = Ink)
    }
}

/** A muted icon and a line of text: an address, a time. */
@Composable
public fun MetaLine(glyph: Glyph, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        GlyphIcon(glyph, Muted, size = 15.dp)
        Spacer(Modifier.width(6.dp))
        Text(text, style = AppTheme.footnote, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
public fun Price(text: String, modifier: Modifier = Modifier, color: Color = Ink) {
    Text(text = text, style = AppTheme.body.copy(fontWeight = FontWeight.Bold), color = color, modifier = modifier)
}

/** A little coral label: "Хит", "Лучший", "Моё". */
@Composable
public fun Badge(text: String, modifier: Modifier = Modifier, onImage: Boolean = false) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (onImage) Color.White else CoralSoft)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text = text, style = AppTheme.footnote.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp), color = Coral)
    }
}

/** "Label / value" with a coral icon in a soft circle, as on a restaurant's card. */
@Composable
public fun InfoRow(label: String, value: String, modifier: Modifier = Modifier, glyph: Glyph? = null) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (glyph != null) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(CoralSoft), contentAlignment = Alignment.Center) {
                GlyphIcon(glyph, Coral, size = 18.dp)
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, style = AppTheme.footnote, color = Muted)
            Text(value, style = AppTheme.body, color = Ink)
        }
    }
}

/** "Блюда ........ 110 000 ₫"; [strong] is the total line, in coral. */
@Composable
public fun SummaryRow(label: String, value: String, modifier: Modifier = Modifier, strong: Boolean = false, valueModifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = if (strong) AppTheme.body.copy(fontWeight = FontWeight.Bold) else AppTheme.body,
            color = if (strong) Ink else Muted,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = if (strong) AppTheme.subtitle.copy(fontWeight = FontWeight.Bold) else AppTheme.body.copy(fontWeight = FontWeight.SemiBold),
            color = if (strong) Coral else Ink,
            modifier = valueModifier,
        )
    }
}

// ------------------------------------------------------------------ inputs

/**
 * A grey filled field, the reference's look. The caller's modifier goes on the text field itself,
 * so a testTag names the node that has text-input semantics.
 */
@Composable
public fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Dp = 50.dp,
    leading: Glyph? = null,
) {
    val style = AppTheme.body.copy(color = Ink)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(FieldFill)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
    ) {
        if (leading != null) {
            GlyphIcon(leading, Muted, size = 20.dp)
            Spacer(Modifier.width(10.dp))
        }
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, style = style, color = Muted, maxLines = if (singleLine) 1 else 3, overflow = TextOverflow.Ellipsis)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = style,
                cursorBrush = SolidColor(Coral),
                modifier = modifier.fillMaxWidth().semantics { contentDescription = placeholder },
            )
        }
    }
}

/** The search bar at the top of a list. */
@Composable
public fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Field(value, onValueChange, placeholder, modifier, leading = Glyph.Search, minHeight = 46.dp)
    }
}

/** A titled group of fields: "Куда доставить", "От кого". */
@Composable
public fun FormSection(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = AppTheme.label.copy(fontWeight = FontWeight.SemiBold), color = Muted, modifier = Modifier.padding(top = 8.dp))
        content()
    }
}

/** One option of a [Chips] row. */
public data class Chip(val key: String, val label: String)

/**
 * A horizontal row of pills. [selected] null is "all"; tapping the selected chip clears it.
 * Tags are `$tagPrefix:chip-<key>` and `$tagPrefix:chip-all`.
 */
@Composable
public fun Chips(
    options: List<Chip>,
    selected: String?,
    onSelect: (String?) -> Unit,
    tagPrefix: String,
    allLabel: String? = "Все",
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (allLabel != null) ChipView(allLabel, selected == null, { onSelect(null) }, "$tagPrefix:chip-all")
        options.forEach { chip ->
            ChipView(chip.label, selected == chip.key, { onSelect(if (selected == chip.key) null else chip.key) }, "$tagPrefix:chip-${chip.key}")
        }
    }
}

@Composable
private fun ChipView(label: String, active: Boolean, onClick: () -> Unit, tag: String) {
    val shape = RoundedCornerShape(50)
    Text(
        text = label,
        style = AppTheme.label.copy(fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium),
        color = if (active) Color.White else Ink,
        modifier = Modifier
            .testTag(tag)
            .clip(shape)
            .background(if (active) Coral else FieldFill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    )
}

/** One tile of a [CategoryRow]. */
public data class Tile(val key: String, val label: String, val glyph: Glyph)

/**
 * The row of square icon tiles under the search bar — the portal's way to filter a list.
 * Same contract as [Chips]: null is "all", tags `$tagPrefix:chip-<key>` and `$tagPrefix:chip-all`.
 */
@Composable
public fun CategoryRow(
    options: List<Tile>,
    selected: String?,
    onSelect: (String?) -> Unit,
    tagPrefix: String,
    allLabel: String? = "Все",
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (allLabel != null) CategoryTile(allLabel, Glyph.Grid, selected == null, { onSelect(null) }, "$tagPrefix:chip-all")
        options.forEach { c ->
            CategoryTile(c.label, c.glyph, selected == c.key, { onSelect(if (selected == c.key) null else c.key) }, "$tagPrefix:chip-${c.key}")
        }
    }
}

@Composable
private fun CategoryTile(label: String, glyph: Glyph, active: Boolean, onClick: () -> Unit, tag: String) {
    Column(
        modifier = Modifier
            .testTag(tag)
            .width(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(if (active) Coral else CoralSoft),
            contentAlignment = Alignment.Center,
        ) {
            GlyphIcon(glyph, if (active) Color.White else Coral, size = 28.dp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = AppTheme.footnote.copy(fontSize = 12.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal),
            color = if (active) Coral else Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

// ------------------------------------------------------------------ buttons

/** The page's main action: a coral full-width button. */
@Composable
public fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    AppButton(text, onClick, modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), enabled = enabled, size = ButtonSize.Lg)
}

/** The quiet alternative: a light grey button. */
@Composable
public fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, destructive: Boolean = false) {
    AppButton(
        text,
        onClick,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        variant = if (destructive) ButtonVariant.Destructive else ButtonVariant.Secondary,
        size = ButtonSize.Lg,
    )
}

/** Coral text that acts: "Добавить ещё", "Заказать что-то ещё". */
@Composable
public fun TextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, glyph: Glyph? = null) {
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null) {
            GlyphIcon(glyph, Coral, size = 18.dp)
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = AppTheme.label.copy(fontWeight = FontWeight.SemiBold), color = Coral)
    }
}

/**
 * The pinned bar's button: coral, a label on the left and a figure on the right —
 * "Корзина · 3 ........ 155 000 ₫".
 */
@Composable
public fun BarButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, trailing: String? = null, enabled: Boolean = true) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) Coral else Color(0xFFD4D4D8))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            style = AppTheme.buttonLabel,
            color = Color.White,
            modifier = Modifier.weight(1f),
            textAlign = if (trailing == null) TextAlign.Center else TextAlign.Start,
        )
        if (trailing != null) Text(trailing, style = AppTheme.buttonLabel.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

/** A round "+" / "−", 34.dp: big enough for a thumb, small enough for a menu row. */
@Composable
public fun RoundButton(glyph: Glyph, onClick: () -> Unit, modifier: Modifier = Modifier, filled: Boolean = true, label: String = "") {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (filled) Coral else Color.White)
            .border(1.5.dp, Coral, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { if (label.isNotEmpty()) contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        GlyphIcon(glyph, if (filled) Color.White else Coral, size = 18.dp)
    }
}

/** − 2 +; just + while nothing is taken. Tags `$tag-minus`, `$tag-qty`, `$tag-plus`. */
@Composable
public fun Stepper(quantity: Int, onPlus: () -> Unit, onMinus: () -> Unit, tag: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (quantity > 0) {
            RoundButton(Glyph.Minus, onMinus, Modifier.testTag("$tag-minus"), filled = false, label = "Убрать")
            Text("$quantity", style = AppTheme.body.copy(fontWeight = FontWeight.Bold), color = Ink, modifier = Modifier.testTag("$tag-qty"))
        }
        RoundButton(Glyph.Plus, onPlus, Modifier.testTag("$tag-plus"), label = "Добавить")
    }
}

/**
 * A button per contact. [message] is prefilled where the messenger allows it, which is how an
 * order reaches a vendor without a server in between.
 */
@Composable
public fun ContactButtons(
    contacts: List<Contact>,
    tagPrefix: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    onOpened: (Contact) -> Unit = {},
) {
    val runtime = LocalAppRuntime.current
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        contacts.forEachIndexed { index, contact ->
            val main = index == 0
            Row(
                modifier = Modifier
                    .testTag("$tagPrefix:contact-${contact.kind.name.lowercase()}")
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (main) Coral else FieldFill)
                    .clickable(role = Role.Button) {
                        runtime.navigation.openUrl(contact.url(message))
                        onOpened(contact)
                    }
                    .padding(horizontal = 18.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                GlyphIcon(if (contact.kind == ContactKind.Phone) Glyph.Phone else Glyph.Chat, if (main) Color.White else Coral, size = 18.dp)
                Spacer(Modifier.width(8.dp))
                Text(contact.label, style = AppTheme.buttonLabel, color = if (main) Color.White else Ink)
            }
        }
    }
}

// ------------------------------------------------------------------ cards

/**
 * The portal's list row: a picture on the left, a title, a line or two and a rating under it, a
 * price or a badge on the right.
 */
@Composable
public fun ListRow(
    title: String,
    glyph: Glyph,
    seed: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    meta: String? = null,
    rating: Double? = null,
    price: String? = null,
    badge: String? = null,
    picture: Dp = 76.dp,
    bonus: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Art(glyph, seed, Modifier.size(picture), shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.body.copy(fontWeight = FontWeight.Bold), color = Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, style = AppTheme.footnote, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (rating != null || meta != null) {
                Row(Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (rating != null) {
                        Rating(rating)
                        if (meta != null) Spacer(Modifier.width(8.dp))
                    }
                    if (meta != null) Text(meta, style = AppTheme.footnote, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (price != null || badge != null) {
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (price != null) Price(price, color = Coral)
                    if (price != null && badge != null) Spacer(Modifier.width(8.dp))
                    if (badge != null) Badge(badge)
                }
            }
            if (bonus != null) BonusBadge(bonus, Modifier.padding(top = 4.dp))
        }
    }
}

/** A big picture card for a carousel or a featured list: picture on top, text under it. */
@Composable
public fun FeatureCard(
    title: String,
    glyph: Glyph,
    seed: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    rating: Double? = null,
    price: String? = null,
    pill: String? = null,
    imageHeight: Dp = 128.dp,
    bonus: String? = null,
) {
    Column(modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(bottom = 6.dp)) {
        Art(glyph, seed, Modifier.fillMaxWidth().height(imageHeight)) {
            if (pill != null) {
                Box(Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.BottomEnd) { Badge(pill, onImage = true) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = AppTheme.body.copy(fontWeight = FontWeight.Bold), color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (subtitle != null) {
                Text(subtitle, style = AppTheme.footnote, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            }
            if (rating != null) {
                Spacer(Modifier.width(8.dp))
                Rating(rating)
            }
        }
        if (price != null) Price(price, Modifier.padding(top = 2.dp))
        if (bonus != null) BonusBadge(bonus, Modifier.padding(top = 4.dp))
    }
}

/** A horizontal strip of [FeatureCard]s. */
@Composable
public fun Carousel(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

/** The coral banner at the top of a section's front page — "Latest Offers" in the reference. */
@Composable
public fun HeroBanner(
    title: String,
    subtitle: String,
    glyph: Glyph,
    modifier: Modifier = Modifier,
    pill: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Coral)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.10f), radius = size.height * 0.75f, center = Offset(size.width * 0.95f, size.height * 0.1f))
            }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.header.copy(fontSize = 21.sp), color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = AppTheme.label, color = Color.White.copy(alpha = 0.92f))
            if (pill != null) {
                Spacer(Modifier.height(10.dp))
                Box(Modifier.clip(RoundedCornerShape(50)).background(Color.White).padding(horizontal = 12.dp, vertical = 5.dp)) {
                    Text(pill, style = AppTheme.footnote.copy(fontWeight = FontWeight.SemiBold), color = Coral)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(Modifier.size(84.dp).clip(CircleShape).background(Sun), contentAlignment = Alignment.Center) {
            GlyphIcon(glyph, Coral, size = 46.dp)
        }
    }
}

/** The wide picture at the top of an opened card, with an optional pill ("25–35 мин"). */
@Composable
public fun DetailHero(glyph: Glyph, seed: String, pill: String? = null, pillGlyph: Glyph = Glyph.Clock) {
    Art(
        glyph,
        seed,
        Modifier.fillMaxWidth().height(190.dp).padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        glyphScale = 0.5f,
    ) {
        if (pill != null) {
            Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.BottomEnd) {
                Row(
                    Modifier.clip(RoundedCornerShape(50)).background(Color.White).padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlyphIcon(pillGlyph, Coral, size = 14.dp)
                    Spacer(Modifier.width(4.dp))
                    Text(pill, style = AppTheme.footnote.copy(fontWeight = FontWeight.SemiBold), color = Ink)
                }
            }
        }
    }
}

/** The big coral title of an opened card, a muted line under it and an optional price. */
@Composable
public fun TitleBlock(title: String, subtitle: String? = null, price: String? = null, rating: Double? = null) {
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)) {
        Text(title, style = AppTheme.title.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold), color = Coral)
        if (subtitle != null || rating != null) {
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (rating != null) {
                    Rating(rating)
                    Spacer(Modifier.width(8.dp))
                }
                if (subtitle != null) Text(subtitle, style = AppTheme.label, color = Muted)
            }
        }
        if (price != null) {
            Spacer(Modifier.height(6.dp))
            Text(price, style = AppTheme.subtitle.copy(fontSize = 22.sp), color = Ink)
        }
    }
}

/** The order-row header of a basket or a form: a thumbnail, a name and a line under it. */
@Composable
public fun MiniHeader(title: String, subtitle: String, glyph: Glyph, seed: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Art(glyph, seed, Modifier.size(56.dp), shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.body.copy(fontWeight = FontWeight.Bold), color = Ink)
            Text(subtitle, style = AppTheme.footnote, color = Muted)
        }
    }
}

// ------------------------------------------------------------------ whole states

/**
 * "Thank you for your order": a coral tick in a sun-yellow ring with sparkles, a coral title and
 * a line of what happens next. [modifier] carries the state's tag.
 */
@Composable
public fun DoneState(title: String, text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
            GlyphIcon(Glyph.Sparkle, Sun, size = 22.dp, modifier = Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 18.dp))
            GlyphIcon(Glyph.Sparkle, Sun, size = 16.dp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 18.dp, top = 6.dp))
            GlyphIcon(Glyph.Sparkle, Coral, size = 14.dp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 30.dp))
            GlyphIcon(Glyph.Sparkle, Sun, size = 12.dp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 22.dp, bottom = 16.dp))
            Box(
                Modifier.size(112.dp).clip(CircleShape).background(Color.White).border(9.dp, Sun, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                GlyphIcon(Glyph.Check, Coral, size = 60.dp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = AppTheme.header.copy(fontSize = 22.sp), color = Coral, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(text, style = AppTheme.body, color = Muted, textAlign = TextAlign.Center)
    }
}

/** "Your basket is empty": a big coral glyph, a coral title, a muted line. */
@Composable
public fun EmptyState(glyph: Glyph, title: String, text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(150.dp).clip(CircleShape).background(CoralSoft), contentAlignment = Alignment.Center) {
            GlyphIcon(glyph, Coral, size = 76.dp)
        }
        Spacer(Modifier.height(18.dp))
        Text(title, style = AppTheme.header, color = Coral, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(text, style = AppTheme.body, color = Muted, textAlign = TextAlign.Center)
    }
}
