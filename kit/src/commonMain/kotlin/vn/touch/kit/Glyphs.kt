package vn.touch.kit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeftRight
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.BadgeCheck
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.BookUser
import com.composables.icons.lucide.Briefcase
import com.composables.icons.lucide.Bus
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Coffee
import com.composables.icons.lucide.CookingPot
import com.composables.icons.lucide.DollarSign
import com.composables.icons.lucide.Euro
import com.composables.icons.lucide.Fish
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Flower2
import com.composables.icons.lucide.Gem
import com.composables.icons.lucide.Gift
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Landmark
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Leaf
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Motorbike
import com.composables.icons.lucide.Percent
import com.composables.icons.lucide.Phone
import com.composables.icons.lucide.Pizza
import com.composables.icons.lucide.Plane
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.QrCode
import com.composables.icons.lucide.Route
import com.composables.icons.lucide.RussianRuble
import com.composables.icons.lucide.Salad
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.ShoppingBag
import com.composables.icons.lucide.ShoppingBasket
import com.composables.icons.lucide.Soup
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Tag
import com.composables.icons.lucide.Ticket
import com.composables.icons.lucide.Timer
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.Truck
import com.composables.icons.lucide.User
import com.composables.icons.lucide.UtensilsCrossed
import com.composables.icons.lucide.Wallet
import com.composables.icons.lucide.Waves
import com.composables.icons.lucide.Wrench
import com.composables.icons.lucide.Zap
import com.composables.icons.lucide.Check as LucideCheck
import com.composables.icons.lucide.Clock as LucideClock

/**
 * The portal's pictures, by meaning: the line icons of Lucide (the set ui-kit draws with), and two
 * solid marks of the portal's own — the city coin and the rating star — which read better filled.
 * They stand in for photos too: the demo has none and a web build has no emoji font.
 */
public enum class Glyph(private val lucide: () -> ImageVector) {
    // Food
    Bowl({ Lucide.Soup }),
    Fish({ Lucide.Fish }),
    Dumpling({ Lucide.CookingPot }),
    Pizza({ Lucide.Pizza }),
    Sushi({ Lucide.Salad }),
    Coffee({ Lucide.Coffee }),
    Cutlery({ Lucide.UtensilsCrossed }),

    // Flowers
    Flower({ Lucide.Flower2 }),
    Gift({ Lucide.Gift }),
    Basket({ Lucide.ShoppingBasket }),
    Leaf({ Lucide.Leaf }),

    // Classifieds
    Home({ Lucide.House }),
    Bike({ Lucide.Motorbike }),
    Tag({ Lucide.Tag }),
    Work({ Lucide.Briefcase }),
    Wrench({ Lucide.Wrench }),
    Heart({ Lucide.Heart }),

    // Travel
    Bus({ Lucide.Bus }),
    Plane({ Lucide.Plane }),
    Passport({ Lucide.BookUser }),
    Calendar({ Lucide.CalendarDays }),
    Route({ Lucide.Route }),

    // Money
    Dollar({ Lucide.DollarSign }),
    Euro({ Lucide.Euro }),
    Ruble({ Lucide.RussianRuble }),
    Coin({ CoinMark }),
    Bank({ Lucide.Landmark }),
    Gem({ Lucide.Gem }),
    Swap({ Lucide.ArrowLeftRight }),
    Wallet({ Lucide.Wallet }),
    Percent({ Lucide.Percent }),

    // Interface
    Search({ Lucide.Search }),
    Star({ StarMark }),
    Clock({ Lucide.LucideClock }),
    Timer({ Lucide.Timer }),
    Pin({ Lucide.MapPin }),
    Cart({ Lucide.ShoppingBag }),
    Truck({ Lucide.Truck }),
    Check({ Lucide.LucideCheck }),
    Plus({ Lucide.Plus }),
    Minus({ Lucide.Minus }),
    Chevron({ Lucide.ChevronRight }),
    Arrow({ Lucide.ArrowRight }),
    Trash({ Lucide.Trash2 }),
    Person({ Lucide.User }),
    Chat({ Lucide.MessageCircle }),
    Phone({ Lucide.Phone }),
    Info({ Lucide.Info }),
    Sparkle({ Lucide.Sparkles }),
    Grid({ Lucide.LayoutGrid }),
    Image({ Lucide.Image }),
    Bell({ Lucide.Bell }),
    Flame({ Lucide.Flame }),
    Zap({ Lucide.Zap }),
    Ticket({ Lucide.Ticket }),
    Waves({ Lucide.Waves }),
    Shield({ Lucide.ShieldCheck }),
    Verified({ Lucide.BadgeCheck }),
    QrCode({ Lucide.QrCode }),
    ;

    /** The vector, for a kit `Icon` or an `IconButton`. */
    public val vector: ImageVector get() = lucide()
}

/** The city coin: a solid disc with the portal's T cut out of it. */
private val CoinMark: ImageVector by lazy { solid("Coin", "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zM7 7h10v2.2h-3.9V17h-2.2V9.2H7z", holes = true) }

/** A solid star, for ratings. */
private val StarMark: ImageVector by lazy { solid("Star", "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z") }

private fun solid(name: String, path: String, holes: Boolean = false): ImageVector =
    ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .addPath(
            pathData = addPathNodes(path),
            pathFillType = if (holes) PathFillType.EvenOdd else PathFillType.NonZero,
            fill = SolidColor(Color.Black),
        )
        .build()

/** A glyph in one colour. Decorative: the text next to it says what it means. */
@Composable
public fun GlyphIcon(glyph: Glyph, tint: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Image(
        painter = rememberVectorPainter(glyph.vector),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(size),
    )
}
