package vn.touch.kit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cx.m42.superizer.kit.Text
import cx.m42.superizer.runtime.LocalAppRuntime

// ------------------------------------------------------------------ surfaces

/** The gutter every block keeps from the screen's edge. */
public val Gutter: Dp = 16.dp

/** A card's corners: soft, the way the big delivery apps round theirs. */
public val CardShape: RoundedCornerShape = RoundedCornerShape(20.dp)

/**
 * A card's ground: white with a soft shadow in the light theme, a raised grey with a hairline in
 * the dark one (where a shadow would not show).
 */
@Composable
public fun Modifier.cardSurface(shape: Shape = CardShape, elevation: Dp = 10.dp): Modifier {
    val c = Touch.colors
    return this
        .then(if (c.isDark) Modifier else Modifier.shadow(elevation, shape, ambientColor = c.shadow, spotColor = c.shadow))
        .clip(shape)
        .background(c.card)
        .then(if (c.isDark) Modifier.border(1.dp, c.line, shape) else Modifier)
}

/** A tap that shrinks what was tapped a little, with no ripple: how the kit's buttons press. */
@Composable
public fun Modifier.pressable(role: Role = Role.Button, enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.97f else 1f)
    return this
        .scale(scale)
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = role, onClick = onClick)
}

// ------------------------------------------------------------------ pictures

private class ArtPalette(val light: Pair<Color, Color>, val dark: Pair<Color, Color>)

private val palettes: List<ArtPalette> = listOf(
    ArtPalette(Color(0xFFFFB38A) to Color(0xFFF2643A), Color(0xFFB4532E) to Color(0xFF7A2E16)), // papaya
    ArtPalette(Color(0xFFFF9EB5) to Color(0xFFE5486F), Color(0xFFA83A58) to Color(0xFF6E1C33)), // dragon fruit
    ArtPalette(Color(0xFFFFD66B) to Color(0xFFF5A00B), Color(0xFFAD7A12) to Color(0xFF6E4A06)), // mango
    ArtPalette(Color(0xFF7FE0B5) to Color(0xFF18A877), Color(0xFF1C7A59) to Color(0xFF0C4A35)), // pandan
    ArtPalette(Color(0xFF8FD3FF) to Color(0xFF2C8BEA), Color(0xFF225F9C) to Color(0xFF123761)), // bay
    ArtPalette(Color(0xFFC6B2FF) to Color(0xFF7B5CF0), Color(0xFF5841A8) to Color(0xFF30226B)), // orchid
    ArtPalette(Color(0xFF7FE3E0) to Color(0xFF0F9C9A), Color(0xFF1A706E) to Color(0xFF0B4241)), // lagoon
)

/**
 * A picture where a photo would go: a ripe gradient, soft light from one corner, a ring of sand
 * from the other, and the glyph large in white. [seed] picks the gradient, so the same restaurant
 * always gets the same colours.
 */
@Composable
public fun Art(
    glyph: Glyph,
    seed: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    glyphScale: Float = 0.42f,
    content: @Composable () -> Unit = {},
) {
    val palette = palettes[(seed.hashCode() and 0x7fffffff) % palettes.size]
    val (top, bottom) = if (Touch.colors.isDark) palette.dark else palette.light
    BoxWithConstraints(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(top, bottom)))
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(listOf(Color.White.copy(alpha = 0.45f), Color.Transparent), center = Offset(size.width * 0.15f, 0f), radius = size.maxDimension * 0.8f),
                    radius = size.maxDimension * 0.8f,
                    center = Offset(size.width * 0.15f, 0f),
                )
                drawCircle(Color.White.copy(alpha = 0.16f), radius = size.minDimension * 0.46f, center = Offset(size.width * 0.98f, size.height * 1.02f))
                drawCircle(Color.White.copy(alpha = 0.10f), radius = size.minDimension * 0.30f, center = Offset(size.width * 0.98f, size.height * 1.02f))
            },
        contentAlignment = Alignment.Center,
    ) {
        val side = if (maxWidth < maxHeight) maxWidth else maxHeight
        GlyphIcon(glyph, Color.White, size = side * glyphScale, modifier = Modifier.rotate(-8f))
        content()
    }
}

/** A glyph on the service's soft ground: the icon of a list row, a step, a fact. */
@Composable
public fun IconTile(
    glyph: Glyph,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    accent: Accent = Touch.accent,
    filled: Boolean = false,
) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(if (filled) accent.gradient else SolidColor(accent.soft)),
        contentAlignment = Alignment.Center,
    ) {
        GlyphIcon(glyph, if (filled) accent.onGradient else accent.fg, size = size * 0.5f)
    }
}

// ------------------------------------------------------------------ page

/**
 * A page on the canvas that scrolls as one. A [bottomBar] stays pinned under it — «Корзина»,
 * «Оформить» — on a card with a hairline, so the page reads as continuing under it.
 */
@Composable
public fun PortalPage(
    modifier: Modifier = Modifier,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = Touch.colors
    // The bar takes its own place under the scrolling part rather than floating over it, so
    // nothing scrolled into view can end up hidden — or tapped — behind it.
    Column(modifier = modifier.fillMaxSize().background(c.canvas)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
            content = content,
        )
        if (bottomBar != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.card)
                    .drawBehind { drawLine(c.line, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx()) }
                    .padding(horizontal = Gutter, vertical = 12.dp),
            ) { bottomBar() }
        }
    }
}

/**
 * The top of a service's front page: the service's gradient, an [eyebrow] over a big [title], a
 * [subtitle], the service's glyph large and faint in the corner — and under it, overlapping the
 * edge, whatever [content] puts there (a search field, a balance).
 */
@Composable
public fun ServiceHeader(
    title: String,
    glyph: Glyph,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    eyebrowGlyph: Glyph? = Glyph.Pin,
    trailing: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val accent = Touch.accent
    val c = Touch.colors
    Box(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(if (content != null) 176.dp else 150.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(accent.gradient)
                .drawBehind {
                    drawCircle(Color.White.copy(alpha = 0.10f), radius = size.height * 0.95f, center = Offset(size.width * 1.02f, size.height * -0.1f))
                    drawCircle(Color.White.copy(alpha = 0.08f), radius = size.height * 0.55f, center = Offset(size.width * 0.06f, size.height * 1.15f))
                },
        ) {
            GlyphIcon(
                glyph,
                c.onArt.copy(alpha = 0.22f),
                size = 128.dp,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 4.dp).rotate(-14f),
            )
        }
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(start = Gutter + 4.dp, end = Gutter, top = 18.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    if (eyebrow != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (eyebrowGlyph != null) {
                                GlyphIcon(eyebrowGlyph, c.onArt.copy(alpha = 0.9f), size = 14.dp)
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(eyebrow, style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold), color = c.onArt.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(title, style = Touch.type.title, color = c.onArt, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (subtitle != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(subtitle, style = Touch.type.caption, color = c.onArt.copy(alpha = 0.88f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (trailing != null) {
                    Spacer(Modifier.width(12.dp))
                    trailing()
                }
            }
            if (content != null) {
                Spacer(Modifier.height(16.dp))
                Column(Modifier.fillMaxWidth(), content = content)
            }
        }
    }
}

/** A heading between blocks, with an optional action on the right ("Все ›"). */
@Composable
public fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
    subtitle: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = Gutter + 2.dp, end = Gutter - 4.dp, top = 22.dp, bottom = 10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = Touch.type.headline, color = Touch.colors.ink)
            if (subtitle != null) Text(subtitle, style = Touch.type.caption, color = Touch.colors.muted)
        }
        if (action != null) {
            Row(
                Modifier.clip(RoundedCornerShape(10.dp)).clickable(role = Role.Button, onClick = onAction).padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(action, style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold), color = Touch.accent.fg)
                GlyphIcon(Glyph.Chevron, Touch.accent.fg, size = 16.dp)
            }
        }
    }
}

/** Muted centred text: an empty list, a reminder. */
@Composable
public fun Hint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = Touch.type.body,
        color = Touch.colors.muted,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 18.dp),
    )
}

/** A soft box in the service's colour for what the reader must not miss: a disclaimer, a rule. */
@Composable
public fun Notice(text: String, modifier: Modifier = Modifier, glyph: Glyph = Glyph.Info) {
    val accent = Touch.accent
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(accent.soft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        GlyphIcon(glyph, accent.fg, size = 18.dp, modifier = Modifier.padding(top = 1.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = Touch.type.caption, color = Touch.colors.ink, modifier = Modifier.weight(1f))
    }
}

/** A rounded card. Tappable when [onClick] is given. */
@Composable
public fun PortalCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .cardSurface()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content,
    )
}

@Composable
public fun Divider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(horizontal = Gutter).height(1.dp).background(Touch.colors.line))
}

// ------------------------------------------------------------------ small parts

/** A gold star and the score. */
@Composable
public fun Rating(value: Double, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        GlyphIcon(Glyph.Star, Touch.colors.star, size = 14.dp)
        Spacer(Modifier.width(3.dp))
        Text(value.toString(), style = Touch.type.caption.copy(fontWeight = FontWeight.Bold), color = Touch.colors.ink)
    }
}

/** A muted icon and a line of text: an address, a time. */
@Composable
public fun MetaLine(glyph: Glyph, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        GlyphIcon(glyph, Touch.colors.muted, size = 15.dp)
        Spacer(Modifier.width(7.dp))
        Text(text, style = Touch.type.caption, color = Touch.colors.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
public fun Price(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Text(text = text, style = Touch.type.price, color = if (color == Color.Unspecified) Touch.colors.ink else color, modifier = modifier)
}

/** A small pill: "Хит", "Лучший курс", "Моё". [onImage] puts it in white over a picture. */
@Composable
public fun Badge(text: String, modifier: Modifier = Modifier, onImage: Boolean = false, glyph: Glyph? = null) {
    val accent = Touch.accent
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (onImage) Touch.colors.card else accent.soft)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null) {
            GlyphIcon(glyph, accent.fg, size = 12.dp)
            Spacer(Modifier.width(4.dp))
        }
        Text(text = text, style = Touch.type.micro, color = if (onImage) Touch.colors.ink else accent.onSoft, maxLines = 1)
    }
}

/** A white pill over a picture: "15–25 мин", "4.8 ★". */
@Composable
public fun ImagePill(text: String, modifier: Modifier = Modifier, glyph: Glyph? = null, glyphTint: Color = Color.Unspecified) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(50)).background(Touch.colors.card).padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null) {
            GlyphIcon(glyph, if (glyphTint == Color.Unspecified) Touch.accent.fg else glyphTint, size = 13.dp)
            Spacer(Modifier.width(4.dp))
        }
        Text(text, style = Touch.type.micro, color = Touch.colors.ink, maxLines = 1)
    }
}

/** "Label / value" with the service's icon on its soft ground, as on a restaurant's card. */
@Composable
public fun InfoRow(label: String, value: String, modifier: Modifier = Modifier, glyph: Glyph? = null) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        if (glyph != null) {
            IconTile(glyph, size = 40.dp)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, style = Touch.type.caption, color = Touch.colors.muted)
            Text(value, style = Touch.type.body.copy(fontWeight = FontWeight.Medium), color = Touch.colors.ink)
        }
    }
}

/** "Блюда ........ 110 000 ₫"; [strong] is the total line, large. */
@Composable
public fun SummaryRow(label: String, value: String, modifier: Modifier = Modifier, strong: Boolean = false, valueModifier: Modifier = Modifier) {
    val c = Touch.colors
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = if (strong) 8.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = if (strong) Touch.type.strong else Touch.type.body,
            color = if (strong) c.ink else c.muted,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = if (strong) Touch.type.title.copy(fontSize = Touch.type.headline.fontSize) else Touch.type.body.copy(fontWeight = FontWeight.SemiBold),
            color = c.ink,
            modifier = valueModifier,
        )
    }
}

// ------------------------------------------------------------------ inputs

/**
 * A filled field with a hairline. The caller's modifier goes on the text field itself, so a
 * testTag names the node that has text-input semantics.
 */
@Composable
public fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Dp = 52.dp,
    leading: Glyph? = null,
    raised: Boolean = false,
) {
    val c = Touch.colors
    val style = Touch.type.body.copy(color = c.ink)
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // A field of several lines starts two lines tall, so it does not grow at the first wrap
            // while someone is typing — on the web that jump can throw the caret off.
            .heightIn(min = if (singleLine) minHeight else maxOf(minHeight, 76.dp))
            .then(if (raised) Modifier.cardSurface(shape, elevation = 14.dp) else Modifier.clip(shape).background(c.field))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
    ) {
        if (leading != null) {
            GlyphIcon(leading, if (raised) Touch.accent.fg else c.muted, size = 20.dp)
            Spacer(Modifier.width(10.dp))
        }
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, style = style, color = c.muted, maxLines = if (singleLine) 1 else 3, overflow = TextOverflow.Ellipsis)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = style,
                cursorBrush = SolidColor(Touch.accent.fg),
                modifier = modifier.fillMaxWidth().semantics { contentDescription = placeholder },
            )
        }
    }
}

/** The search bar at the top of a list: raised, so it floats over a header's gradient too. */
@Composable
public fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    Box(Modifier.padding(horizontal = Gutter, vertical = 6.dp)) {
        Field(value, onValueChange, placeholder, modifier, leading = Glyph.Search, minHeight = 52.dp, raised = true)
    }
}

/** A titled card of fields: "Куда доставить", "От кого". */
@Composable
public fun FormSection(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 8.dp)) {
        Text(title, style = Touch.type.strong, color = Touch.colors.ink, modifier = Modifier.padding(start = 2.dp, top = 10.dp, bottom = 10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
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
            .padding(horizontal = Gutter, vertical = 6.dp),
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
    val c = Touch.colors
    val accent = Touch.accent
    val shape = RoundedCornerShape(50)
    Text(
        text = label,
        style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold),
        color = if (active) accent.onFg else c.ink,
        modifier = Modifier
            .testTag(tag)
            .clip(shape)
            .background(if (active) accent.fg else c.card)
            .then(if (active) Modifier else Modifier.border(1.dp, c.line, shape))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    )
}

/** One tile of a [CategoryRow]. */
public data class Tile(val key: String, val label: String, val glyph: Glyph)

/**
 * The row of icon tiles under the search bar — the portal's way to filter a list.
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
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (allLabel != null) CategoryTile(allLabel, Glyph.Grid, selected == null, { onSelect(null) }, "$tagPrefix:chip-all")
        options.forEach { c ->
            CategoryTile(c.label, c.glyph, selected == c.key, { onSelect(if (selected == c.key) null else c.key) }, "$tagPrefix:chip-${c.key}")
        }
    }
}

@Composable
private fun CategoryTile(label: String, glyph: Glyph, active: Boolean, onClick: () -> Unit, tag: String) {
    val c = Touch.colors
    val accent = Touch.accent
    Column(
        modifier = Modifier
            .testTag(tag)
            .width(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .pressable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val shape = RoundedCornerShape(20.dp)
        Box(
            modifier = Modifier
                .size(58.dp)
                .then(if (active) Modifier.shadow(8.dp, shape, ambientColor = accent.fg, spotColor = accent.fg) else Modifier)
                .clip(shape)
                .background(if (active) accent.gradient else SolidColor(c.card))
                .then(if (active) Modifier else Modifier.border(1.dp, c.line, shape)),
            contentAlignment = Alignment.Center,
        ) {
            GlyphIcon(glyph, if (active) accent.onGradient else accent.fg, size = 26.dp)
        }
        Spacer(Modifier.height(7.dp))
        Text(
            label,
            style = Touch.type.micro.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.Medium),
            color = if (active) c.ink else c.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

// ------------------------------------------------------------------ buttons

private val ButtonShape = RoundedCornerShape(16.dp)

/** The page's main action: a full-width button in the service's colour. */
@Composable
public fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, glyph: Glyph? = null) {
    val accent = Touch.accent
    val c = Touch.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .heightIn(min = 54.dp)
            .clip(ButtonShape)
            .background(if (enabled) accent.gradient else SolidColor(c.field))
            .pressable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null) {
            GlyphIcon(glyph, if (enabled) accent.onGradient else c.muted, size = 18.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = Touch.type.strong, color = if (enabled) accent.onGradient else c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** The quiet alternative: the service's soft colour, or a red one for [destructive]. */
@Composable
public fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, destructive: Boolean = false, glyph: Glyph? = null) {
    val accent = Touch.accent
    val c = Touch.colors
    val ink = if (destructive) c.danger else accent.onSoft
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .heightIn(min = 54.dp)
            .clip(ButtonShape)
            .background(if (destructive) c.dangerSoft else accent.soft)
            .pressable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null) {
            GlyphIcon(glyph, ink, size = 18.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = Touch.type.strong, color = ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Text in the service's colour that acts: "Добавить ещё", "Заказать что-то ещё". */
@Composable
public fun TextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, glyph: Glyph? = null, destructive: Boolean = false) {
    val tint = if (destructive) Touch.colors.danger else Touch.accent.fg
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null) {
            GlyphIcon(glyph, tint, size = 18.dp)
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = Touch.type.caption.copy(fontWeight = FontWeight.SemiBold), color = tint)
    }
}

/**
 * The pinned bar's button: the service's gradient, a label on the left and a figure on the
 * right — "Корзина · 3 ........ 155 000 ₫".
 */
@Composable
public fun BarButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    enabled: Boolean = true,
    glyph: Glyph? = null,
    count: Int? = null,
) {
    val accent = Touch.accent
    val c = Touch.colors
    val ink = if (enabled) accent.onGradient else c.muted
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) accent.gradient else SolidColor(c.field))
            .pressable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (glyph != null || count != null) {
            Box {
                if (glyph != null) {
                    Box(Modifier.size(34.dp).clip(CircleShape).background(ink.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                        GlyphIcon(glyph, ink, size = 18.dp)
                    }
                }
                if (count != null) {
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(start = 22.dp).size(18.dp).clip(CircleShape).background(c.card),
                        contentAlignment = Alignment.Center,
                    ) { Text("$count", style = Touch.type.micro.copy(fontWeight = FontWeight.ExtraBold), color = accent.fg) }
                }
            }
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text,
            style = Touch.type.strong,
            color = ink,
            modifier = Modifier.weight(1f),
            textAlign = if (trailing == null && glyph == null) TextAlign.Center else TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailing != null) Text(trailing, style = Touch.type.price, color = ink)
    }
}

/** A round "+" / "−": big enough for a thumb, small enough for a menu row. */
@Composable
public fun RoundButton(glyph: Glyph, onClick: () -> Unit, modifier: Modifier = Modifier, filled: Boolean = true, label: String = "") {
    val accent = Touch.accent
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (filled) accent.fg else Color.Transparent)
            .pressable(onClick = onClick)
            .semantics { if (label.isNotEmpty()) contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        GlyphIcon(glyph, if (filled) accent.onFg else accent.fg, size = 18.dp)
    }
}

/** − 2 + on a soft pill; a lone + while nothing is taken. Tags `$tag-minus`, `$tag-qty`, `$tag-plus`. */
@Composable
public fun Stepper(quantity: Int, onPlus: () -> Unit, onMinus: () -> Unit, tag: String) {
    val accent = Touch.accent
    Row(
        modifier = Modifier.clip(RoundedCornerShape(50)).background(if (quantity > 0) accent.soft else Color.Transparent).padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (quantity > 0) {
            RoundButton(Glyph.Minus, onMinus, Modifier.testTag("$tag-minus"), filled = false, label = "Убрать")
            Text(
                "$quantity",
                style = Touch.type.strong,
                color = accent.onSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(24.dp).testTag("$tag-qty"),
            )
        }
        RoundButton(Glyph.Plus, onPlus, Modifier.testTag("$tag-plus"), label = "Добавить")
    }
}

/**
 * A button per contact. [message] is prefilled where the messenger allows it, which is how an
 * order reaches a vendor without a server in between. The first is the main one.
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
    val accent = Touch.accent
    val c = Touch.colors
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = Gutter, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        contacts.forEachIndexed { index, contact ->
            val main = index == 0
            val ink = if (main) accent.onGradient else c.ink
            Row(
                modifier = Modifier
                    .testTag("$tagPrefix:contact-${contact.kind.name.lowercase()}")
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .then(if (main) Modifier.clip(ButtonShape).background(accent.gradient) else Modifier.cardSurface(ButtonShape, elevation = 4.dp))
                    .pressable {
                        runtime.navigation.openUrl(contact.url(message))
                        onOpened(contact)
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(if (main) ink.copy(alpha = 0.18f) else accent.soft),
                    contentAlignment = Alignment.Center,
                ) {
                    GlyphIcon(if (contact.kind == ContactKind.Phone) Glyph.Phone else Glyph.Chat, if (main) ink else accent.fg, size = 17.dp)
                }
                Spacer(Modifier.width(12.dp))
                Text(contact.label, style = Touch.type.strong, color = ink, modifier = Modifier.weight(1f))
                GlyphIcon(Glyph.Arrow, if (main) ink else c.muted, size = 18.dp)
            }
        }
    }
}

// ------------------------------------------------------------------ cards

/**
 * The portal's list row, as a card: a picture on the left, a title, a line or two and a rating
 * under it, a price or a badge, the bonus it brings.
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
    picture: Dp = 84.dp,
    bonus: String? = null,
) {
    val c = Touch.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 6.dp)
            .cardSurface()
            .pressable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Art(glyph, seed, Modifier.size(picture), shape = RoundedCornerShape(16.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f).padding(vertical = 2.dp)) {
            Text(title, style = Touch.type.strong, color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, style = Touch.type.caption, color = c.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (rating != null || meta != null) {
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (rating != null) {
                        Rating(rating)
                        if (meta != null) {
                            Text("  ·  ", style = Touch.type.caption, color = c.muted)
                        }
                    }
                    if (meta != null) Text(meta, style = Touch.type.caption, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (price != null || badge != null) {
                Row(Modifier.padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (price != null) Price(price)
                    if (price != null && badge != null) Spacer(Modifier.width(8.dp))
                    if (badge != null) Badge(badge)
                }
            }
            if (bonus != null) BonusBadge(bonus, Modifier.padding(top = 6.dp))
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
    val c = Touch.colors
    Column(modifier = modifier.padding(vertical = 6.dp).cardSurface().pressable(onClick = onClick)) {
        Art(glyph, seed, Modifier.fillMaxWidth().height(imageHeight), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
            Box(Modifier.fillMaxSize().padding(10.dp)) {
                if (pill != null) ImagePill(pill, Modifier.align(Alignment.BottomStart), glyph = Glyph.Clock)
                if (rating != null) ImagePill(rating.toString(), Modifier.align(Alignment.TopEnd), glyph = Glyph.Star, glyphTint = c.star)
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp)) {
            Text(title, style = Touch.type.strong, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, style = Touch.type.caption, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (price != null) Price(price, Modifier.padding(top = 4.dp))
            if (bonus != null) BonusBadge(bonus, Modifier.padding(top = 6.dp))
        }
    }
}

/** A horizontal strip of [FeatureCard]s, with room for their shadows. */
@Composable
public fun Carousel(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Gutter, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

/** A promo banner in the service's gradient: a headline, a line, a white pill, the glyph large. */
@Composable
public fun HeroBanner(
    title: String,
    subtitle: String,
    glyph: Glyph,
    modifier: Modifier = Modifier,
    pill: String? = null,
    onClick: (() -> Unit)? = null,
    accent: Accent = Touch.accent,
    inset: Dp = Gutter,
) {
    val c = Touch.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = inset, vertical = if (inset > 0.dp) 8.dp else 0.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(accent.gradient)
            .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier)
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.12f), radius = size.height * 0.85f, center = Offset(size.width * 0.98f, size.height * 0.05f))
                drawCircle(Color.White.copy(alpha = 0.08f), radius = size.height * 0.45f, center = Offset(size.width * 0.7f, size.height * 1.1f))
            }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = Touch.type.headline, color = c.onArt)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = Touch.type.caption, color = c.onArt.copy(alpha = 0.9f))
            if (pill != null) {
                Spacer(Modifier.height(12.dp))
                Box(Modifier.clip(RoundedCornerShape(50)).background(Color.White).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(pill, style = Touch.type.micro, color = lerp(accent.from, accent.to, 0.6f).let { if (c.isDark) Color(0xFF111114) else it })
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(Modifier.size(76.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            GlyphIcon(glyph, c.onArt, size = 40.dp, modifier = Modifier.rotate(-10f))
        }
    }
}

/** The wide picture at the top of an opened card, with an optional pill ("25–35 мин"). */
@Composable
public fun DetailHero(glyph: Glyph, seed: String, pill: String? = null, pillGlyph: Glyph = Glyph.Clock) {
    Art(
        glyph,
        seed,
        Modifier.fillMaxWidth().padding(start = Gutter, end = Gutter, top = 8.dp).height(200.dp),
        shape = RoundedCornerShape(28.dp),
        glyphScale = 0.44f,
    ) {
        if (pill != null) {
            Box(Modifier.fillMaxSize().padding(14.dp), contentAlignment = Alignment.BottomStart) {
                ImagePill(pill, glyph = pillGlyph)
            }
        }
    }
}

/** The big title of an opened card, a line under it, a rating and an optional price. */
@Composable
public fun TitleBlock(title: String, subtitle: String? = null, price: String? = null, rating: Double? = null) {
    val c = Touch.colors
    Column(Modifier.fillMaxWidth().padding(start = Gutter + 2.dp, end = Gutter, top = 16.dp, bottom = 6.dp)) {
        Text(title, style = Touch.type.title, color = c.ink)
        if (subtitle != null || rating != null) {
            Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (rating != null) {
                    Rating(rating)
                    if (subtitle != null) Text("  ·  ", style = Touch.type.caption, color = c.muted)
                }
                if (subtitle != null) Text(subtitle, style = Touch.type.caption, color = c.muted)
            }
        }
        if (price != null) {
            Spacer(Modifier.height(8.dp))
            Text(price, style = Touch.type.title.copy(fontSize = Touch.type.headline.fontSize), color = Touch.accent.fg)
        }
    }
}

/** The header card of a basket or a form: a thumbnail, a name and a line under it. */
@Composable
public fun MiniHeader(title: String, subtitle: String, glyph: Glyph, seed: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter, vertical = 8.dp)
            .cardSurface()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Art(glyph, seed, Modifier.size(56.dp), shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = Touch.type.strong, color = Touch.colors.ink)
            Text(subtitle, style = Touch.type.caption, color = Touch.colors.muted)
        }
    }
}

// ------------------------------------------------------------------ whole states

/**
 * "Thank you for your order": a tick on the service's gradient in a soft halo, gold sparkles, a
 * big title and a line of what happens next. [modifier] carries the state's tag.
 */
@Composable
public fun DoneState(title: String, text: String, modifier: Modifier = Modifier) {
    val accent = Touch.accent
    val c = Touch.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(176.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(168.dp).clip(CircleShape).background(accent.soft.copy(alpha = 0.6f)))
            Box(Modifier.size(132.dp).clip(CircleShape).background(accent.soft))
            GlyphIcon(Glyph.Sparkle, c.coin, size = 24.dp, modifier = Modifier.align(Alignment.TopStart).padding(start = 10.dp, top = 14.dp))
            GlyphIcon(Glyph.Sparkle, accent.fg, size = 18.dp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 14.dp, top = 4.dp))
            GlyphIcon(Glyph.Coin, c.coin, size = 20.dp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 26.dp))
            Box(
                Modifier
                    .size(96.dp)
                    .shadow(16.dp, CircleShape, ambientColor = accent.fg, spotColor = accent.fg)
                    .clip(CircleShape)
                    .background(accent.gradient),
                contentAlignment = Alignment.Center,
            ) {
                GlyphIcon(Glyph.Check, accent.onGradient, size = 48.dp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = Touch.type.title, color = c.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(text, style = Touch.type.body, color = c.muted, textAlign = TextAlign.Center)
    }
}

/** "Your basket is empty": the glyph in a soft halo, a title, a muted line. */
@Composable
public fun EmptyState(glyph: Glyph, title: String, text: String, modifier: Modifier = Modifier) {
    val accent = Touch.accent
    val c = Touch.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(140.dp).clip(CircleShape).background(accent.soft), contentAlignment = Alignment.Center) {
            Box(Modifier.size(92.dp).clip(CircleShape).background(c.card), contentAlignment = Alignment.Center) {
                GlyphIcon(glyph, accent.fg, size = 44.dp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = Touch.type.title.copy(fontSize = Touch.type.headline.fontSize), color = c.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(text, style = Touch.type.body, color = c.muted, textAlign = TextAlign.Center)
    }
}

/** A style tweak shared by screens that set a figure large: tabular look without a mono font. */
public fun TextStyle.figures(): TextStyle = copy(fontWeight = FontWeight.ExtraBold)
