@file:OptIn(ExperimentalTestApi::class)

package vn.touch.host

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import cx.m42.superizer.ThemeMode
import cx.m42.superizer.host.storage.PrefsStorage
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Phone screenshots, rendered headless: 412×915 dp at 2× into build/screenshots/.
 * Each frame starts from a fresh install and taps its way to the state it shows. A few frames are
 * also drawn in the dark theme (`-dark`); `SHOTS_DARK=1` draws every frame in both, and `SHOTS=<regex>`
 * draws only the frames whose names match.
 *
 *     ./gradlew :composeApp:desktopTest --tests '*Screenshots*'
 *     SHOTS='food' SHOTS_DARK=1 ./gradlew :composeApp:desktopTest --tests '*Screenshots*'
 */
class Screenshots {

    private class Drive(
        val tap: (String) -> Unit,
        val type: (String, String) -> Unit,
        val replace: (String, String) -> Unit,
    )

    private val widthDp = 412
    private val heightDp = 915
    private val scale = 2f
    private val out = File("build/screenshots")

    private val only: Regex? = System.getenv("SHOTS")?.takeIf { it.isNotBlank() }?.let(::Regex)
    private val allDark: Boolean = System.getenv("SHOTS_DARK") == "1"

    /** The frames drawn in the dark theme too, without SHOTS_DARK. */
    private val dark = setOf("01-home", "02-food", "03-food-menu", "06-flowers", "09-ads", "13-visarun", "16-exchange", "19-bonus")

    private fun shot(name: String, drive: Drive.() -> Unit) {
        if (only != null && !only.containsMatchIn(name)) return
        shot(name, darkTheme = false, drive)
        if (allDark || name in dark) shot("$name-dark", darkTheme = true, drive)
    }

    private fun shot(name: String, darkTheme: Boolean, drive: Drive.() -> Unit) {
        PrefsStorage.useDirectory(Files.createTempDirectory("touchvn-shot").toFile())
        runDesktopComposeUiTest(width = (widthDp * scale).toInt(), height = (heightDp * scale).toInt()) {
            val superizer = buildTouchHost(CoroutineScope(SupervisorJob() + Dispatchers.Unconfined), debug = false)
            superizer.settings.chooseLanguage("ru")
            superizer.settings.chooseTheme(if (darkTheme) ThemeMode.Dark else ThemeMode.Light)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(scale)) { TouchApp(superizer) }
            }
            waitForIdle()
            Drive(
                tap = { tag ->
                    runCatching { onNodeWithTag(tag).performScrollTo() }
                    onNodeWithTag(tag).performClick()
                    waitForIdle()
                },
                type = { tag, text ->
                    runCatching { onNodeWithTag(tag).performScrollTo() }
                    onNodeWithTag(tag).performTextInput(text)
                    waitForIdle()
                },
                replace = { tag, text ->
                    onNodeWithTag(tag).performTextReplacement(text)
                    waitForIdle()
                },
            ).drive()
            // No hover tint where the last tap was: a finger leaves none.
            runCatching { onRoot().performMouseInput { moveTo(Offset(1f, 1f)) } }
            waitForIdle()
            out.mkdirs()
            ImageIO.write(captureToImage().toAwtImage().opaque(), "png", File(out, "$name.png"))
        }
    }

    private fun BufferedImage.opaque(): BufferedImage =
        BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).also { img ->
            img.createGraphics().apply {
                color = java.awt.Color.WHITE
                fillRect(0, 0, width, height)
                drawImage(this@opaque, 0, 0, null)
                dispose()
            }
        }

    private fun Drive.foodBasket() {
        tap("home:service-food")
        tap("food:restaurant-pho-hung")
        tap("food:dish-pho-bo-plus")
        tap("food:dish-pho-bo-plus")
        tap("food:dish-nem-plus")
    }

    @Test
    fun capture() {
        shot("01-home") {}
        shot("02-food") { tap("home:service-food") }
        shot("03-food-menu") { foodBasket() }
        shot("04-food-basket") { foodBasket(); tap("food:basket") }
        shot("05-food-checkout") {
            foodBasket(); tap("food:basket"); tap("food:checkout")
            type("food:name", "Иван")
            type("food:phone", "+84 912 345 678")
            type("food:address", "Отель Panorama, номер 2105")
        }
        shot("06-flowers") { tap("home:service-flowers") }
        shot("07-flowers-bouquet") { tap("home:service-flowers"); tap("flowers:bouquet-box-pink") }
        shot("08-flowers-order") {
            tap("home:service-flowers"); tap("flowers:bouquet-box-pink"); tap("flowers:order")
            type("flowers:recipient", "Анна")
            type("flowers:address", "ул. Чан Фу, 50, кв. 12")
            type("flowers:when", "завтра к 10:00")
            type("flowers:card", "С днём рождения!")
        }
        shot("09-ads") { tap("home:service-ads") }
        shot("10-ads-rent") { tap("home:service-ads"); tap("ads:chip-Rent") }
        shot("11-ads-detail") { tap("home:service-ads"); tap("ads:ad-b1") }
        shot("12-ads-new") {
            tap("home:service-ads"); tap("ads:new")
            tap("ads:new-cat:chip-Sale")
            type("ads:title", "Продам шлем")
            type("ads:text", "Шлем, размер M, почти новый")
        }
        shot("13-visarun") { tap("home:service-visarun") }
        shot("14-visarun-offer") { tap("home:service-visarun"); tap("visarun:offer-mocbai-bus") }
        shot("15-visarun-calculator") {
            tap("home:service-visarun"); tap("visarun:calculator")
            val entry = LocalDate.now(ZoneOffset.ofHours(7)).minusDays(38)
            type("visarun:entry", entry.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        }
        shot("16-exchange") { tap("home:service-exchange") }
        shot("17-exchange-rub") { tap("home:service-exchange"); tap("exchange:chip-RUB"); replace("exchange:amount", "50000") }
        shot("18-exchange-point") { tap("home:service-exchange"); tap("exchange:point-gold-dam") }
        shot("19-bonus") { tap("host:bonus") }
        shot("20-food-bonus-checkout") {
            foodBasket(); tap("food:basket"); tap("food:checkout")
            tap("food:bonus-use"); tap("food:bonus-use")
        }
        shot("21-food-bonus-sent") {
            foodBasket(); tap("food:basket"); tap("food:checkout")
            type("food:name", "Иван")
            type("food:phone", "+84 912 345 678")
            type("food:address", "Отель Panorama, номер 2105")
            tap("food:contact-whatsapp")
        }
    }
}
