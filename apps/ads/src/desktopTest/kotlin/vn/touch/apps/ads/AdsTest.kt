package vn.touch.apps.ads

import androidx.compose.ui.test.performScrollTo
import cx.m42.superizer.testing.AppContractTest
import cx.m42.superizer.testing.AppTestScope
import cx.m42.superizer.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdsContractTest : AppContractTest(AdsApp())

class FilterTest {
    private val ads = Catalog.demo(now = 100L * 24 * 3_600_000)

    @Test fun categoryAndWordsNarrowTheFeed() {
        assertEquals(listOf("b1", "b2"), filterAds(ads, Category.Bikes, "").map { it.id })
        assertEquals(listOf("b2"), filterAds(ads, null, "yamaha").map { it.id })
        assertEquals(listOf("a1"), filterAds(ads, null, "Студия пляжа").map { it.id })
        assertTrue(filterAds(ads, Category.Jobs, "Honda").isEmpty())
    }

    @Test fun ago() {
        val h = 3_600_000L
        assertEquals("только что", ago(10 * h, 10 * h))
        assertEquals("5 ч назад", ago(10 * h, 5 * h))
        assertEquals("вчера", ago(30 * h, 0))
        assertEquals("3 дн. назад", ago(80 * h, 0))
    }
}

class AdsFlowTest {
    private fun AppTestScope<*>.tap(tag: String) { runCatching { node(tag).performScrollTo() }; click(tag) }

    @Test fun searchAndContact() = runAppTest(AdsApp()) {
        type("ads:search", "honda")
        assertShown("ads:ad-b1")
        assertAbsent("ads:ad-a1")
        tap("ads:ad-b1")
        tap("ads:contact-telegram")
        assertTrue(runtime.navigation.urls.single().startsWith("https://t.me/demo_bike_nt?text="))
    }

    @Test fun anOwnAdIsKeptAndCanBeDeleted() = runAppTest(AdsApp()) {
        tap("ads:new")
        assertShown("ads:problems")
        tap("ads:new-cat:chip-Sale")
        type("ads:title", "Продам шлем")
        type("ads:text", "Шлем, размер M, почти новый")
        type("ads:price", "300000")
        tap("ads:new-district:chip-North")
        type("ads:author", "Ваня")
        type("ads:contact", "+84 900 111 222")
        tap("ads:publish")
        val inst = instance as AdsInstance
        val mine = inst.mine.value.single()
        assertEquals(300_000L, mine.price)
        assertEquals(Category.Sale, mine.category)
        tap("ads:ad-${mine.id}")
        tap("ads:delete")
        assertTrue(inst.mine.value.isEmpty())
    }
}
