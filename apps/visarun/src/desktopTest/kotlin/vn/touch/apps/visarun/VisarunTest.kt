package vn.touch.apps.visarun

import androidx.compose.ui.test.performScrollTo
import cx.m42.superizer.testing.AppContractTest
import cx.m42.superizer.testing.AppTestScope
import cx.m42.superizer.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VisarunContractTest : AppContractTest(VisarunApp())

class StayTest {
    @Test fun civilDatesRoundTrip() {
        assertEquals(0L, daysFromCivil(1970, 1, 1))
        for (d in listOf(-1000L, 0L, 19_000L, 20_405L, 30_000L)) {
            val (y, m, day) = civilFromDays(d)
            assertEquals(d, daysFromCivil(y, m, day))
        }
        assertEquals("29.02.2028", formatDate(parseDate("29.02.2028")!!))
    }

    @Test fun badDatesAreRefused() {
        assertNull(parseDate("31.02.2026"))
        assertNull(parseDate("hello"))
        assertNull(parseDate("1.13.2026"))
        assertEquals(parseDate("01.03.2026"), parseDate("1/3/26"))
    }

    @Test fun theEntryDayIsDayOne() {
        val entry = parseDate("01.03.2026")!!
        val s = stay(entry, Regime.VisaFree45, today = entry)
        assertEquals("14.04.2026", formatDate(s.lastDay))
        assertEquals(44L, s.daysLeft)
    }

    @Test fun todayIsVietnamese() {
        // 2026-11-13 18:00 UTC is already the 14th in Nha Trang.
        val utc = daysFromCivil(2026, 11, 13) * 86_400_000L + 18 * 3_600_000L
        assertEquals("14.11.2026", formatDate(todayInVietnam(utc)))
    }
}

class VisarunFlowTest {
    private fun AppTestScope<*>.tap(tag: String) { runCatching { node(tag).performScrollTo() }; click(tag) }

    @Test fun bookABorderRun() = runAppTest(VisarunApp()) {
        tap("visarun:chip-Cambodia")
        tap("visarun:offer-mocbai-bus")
        tap("visarun:book")
        type("visarun:name", "Ivan Petrov")
        type("visarun:phone", "+84 900 000 000")
        type("visarun:date", "20.11")
        tap("visarun:contact-telegram")
        assertShown("visarun:sent")
        assertTrue(runtime.navigation.urls.single().startsWith("https://t.me/demo_visarun_nt?text="))
    }

    @Test fun theCalculatorAnswers() = runAppTest(VisarunApp()) {
        tap("visarun:calculator")
        val inst = instance as VisarunInstance
        type("visarun:entry", formatDate(inst.today - 40))
        assertShown("visarun:result")
        assertShown("visarun:pick")
    }
}
