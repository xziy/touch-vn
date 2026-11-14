package vn.touch.host

import androidx.compose.runtime.Composable
import cx.m42.superizer.HostScreen
import cx.m42.superizer.LanguageOption
import cx.m42.superizer.Superizer
import cx.m42.superizer.SuperizerContract
import cx.m42.superizer.host.SuperizerBuilder
import cx.m42.superizer.host.build
import cx.m42.superizer.app.AppId
import cx.m42.superizer.host.SystemClock
import cx.m42.superizer.host.platform.currentPlatform
import cx.m42.superizer.host.storage.PrefsStorageService
import cx.m42.superizer.runtime.HostInfo
import cx.m42.superizer.ui.shell.SuperizerShell
import kotlinx.coroutines.CoroutineScope
import vn.touch.apps.ads.AdsApp
import vn.touch.apps.exchange.ExchangeApp
import vn.touch.apps.flowers.FlowersApp
import vn.touch.apps.food.FoodApp
import vn.touch.apps.visarun.VisarunApp
import vn.touch.kit.BonusPage
import vn.touch.kit.BonusPlace
import vn.touch.kit.CITY_BONUS_TITLE
import vn.touch.kit.CityBonus
import vn.touch.kit.Glyph
import vn.touch.kit.PortalTheme
import vn.touch.kit.StoredCityBonus

public const val HOST_NAME: String = "Touch VN"
public const val SCHEME: String = "touchvn"
public const val VERSION: String = "0.1.0"

/**
 * Touch VN — the city portal of Nha Trang, in one function.
 *
 * Like Unitool, this file is the whole product: a name and the list of services. Home, the drawer,
 * Settings, deep links (`touchvn://app/<id>`) and the app frame are Superizer's. A new service is
 * one module, one line in settings.gradle.kts, one dependency and one register() below.
 */
public fun buildTouchHost(
    scope: CoroutineScope,
    debug: Boolean,
    extra: SuperizerBuilder.() -> Unit = {},
): Superizer {
    // The bonus screen opens a service by deep link, and the link goes to a host that does not
    // exist yet while the builder runs — hence the holder.
    var built: Superizer? = null
    val openApp: (String) -> Unit = { id -> built?.route?.deliver("$SCHEME://app/$id") }
    val bonus = cityBonus(scope, openApp)
    return Superizer.build(scope) {
        touch(debug, bonus, openApp)
        extra()
    }.also { built = it }
}

/**
 * The city bonus system: one account for every service, kept by the host under its own name
 * (an app's storage is that app's alone). Apps find it by [CityBonus.Key].
 */
private fun cityBonus(scope: CoroutineScope, open: (String) -> Unit): CityBonus = StoredCityBonus(
    storage = PrefsStorageService(AppId(BONUS_STORE)),
    clock = SystemClock,
    scope = scope,
    places = listOf(
        BonusPlace(FoodApp.ID, "Еда", Glyph.Cutlery, "до 10% с заказа у ресторанов-партнёров", "до 20–50% заказа"),
        BonusPlace(FlowersApp.ID, "Цветы", Glyph.Flower, "5–10% с букета", "до 20–30% букета"),
        BonusPlace(VisarunApp.ID, "Визараны", Glyph.Bus, "+150–300 за поездку, 5% за e-visa", "до 20–30% туров в донгах"),
        BonusPlace(AdsApp.ID, "Объявления", Glyph.Tag, "+50–100 за обращение к бизнесам со значком", "пока нет — частные объявления"),
        BonusPlace(ExchangeApp.ID, "Обмен валют", Glyph.Swap, "+50–100 за обмен у пунктов-партнёров", "пока нет"),
    ),
    opener = open,
)

private const val BONUS_STORE = "city-bonus"

/** The bonus screen as the host shows it — from the line on Home. */
private class BonusScreen(private val bonus: CityBonus) : HostScreen {
    override val id: String = "bonus"
    override fun title(langTag: String): String = CITY_BONUS_TITLE

    @androidx.compose.runtime.Composable
    override fun Content(superizer: Superizer, back: () -> Unit, open: (HostScreen) -> Unit) {
        PortalTheme { BonusPage(bonus, onOpenPlace = { id -> bonus.open(id) }) }
    }
}

private fun SuperizerBuilder.touch(debug: Boolean, bonus: CityBonus, openApp: (String) -> Unit) {
    host(
        HostInfo(
            name = HOST_NAME,
            version = VERSION,
            build = "dev",
            platform = currentPlatform(),
            contractVersion = SuperizerContract.VERSION,
            debug = debug,
        ),
    )

    scheme(SCHEME)

    // The services' own screens are written in Russian only, so the shell is too — otherwise an
    // English phone would show English tiles over Russian content. Add "en" with the translations.
    languages(
        LanguageOption("ru", "Русский"),
        fallback = "ru",
    )

    // Delivery — the Grab half of the portal.
    register(FoodApp())
    register(FlowersApp())
    // The city — what people in Nha Trang look for besides dinner.
    register(AdsApp())
    register(VisarunApp())
    register(ExchangeApp())

    // A fresh install shows every service on Home, in this order.
    home(FoodApp.ID, FlowersApp.ID, AdsApp.ID, VisarunApp.ID, ExchangeApp.ID)

    // The city bonus system: the account every service earns into and pays from, its screen and
    // its pill on Home.
    service(CityBonus.Key, bonus)
    val bonusScreen = BonusScreen(bonus)
    hostScreen(bonusScreen)
    // Home itself: greeting, search, services, popular picks (Home.kt). It fills the screen, so the
    // library's tile grid under it gets no height.
    homeBanner(TouchHome(bonus, bonusScreen, SystemClock, openApp))

    // The Service Menu's door in a release build.
    serviceCode("SERVICE")
}

@Composable
public fun TouchApp(superizer: Superizer): Unit = SuperizerShell(superizer)
