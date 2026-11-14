package vn.touch.kit

import cx.m42.superizer.testing.InMemoryStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class BonusTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @Test fun russianPlurals() {
        assertEquals("1 бонус", formatBonus(1))
        assertEquals("2 бонуса", formatBonus(2))
        assertEquals("5 бонусов", formatBonus(5))
        assertEquals("11 бонусов", formatBonus(11))
        assertEquals("21 бонус", formatBonus(21))
        assertEquals("1 250 бонусов", formatBonus(1_250))
        assertEquals(null, bonusBadge(0))
        assertEquals("+150 бонусов", bonusBadge(150))
    }

    @Test fun termsEarnAndCapTheSpend() {
        val terms = BonusTerms(earnPercent = 5, spendPercent = 30)
        // 5% of 125 000 ₫ = 6 250 ₫ = 62 bonuses (whole ones only).
        assertEquals(62, terms.earnFor(125_000))
        // 30% of 125 000 ₫ is 375 bonuses; someone holding 300 spends 300.
        assertEquals(300, terms.maxSpend(125_000, 300))
        assertEquals(375, terms.maxSpend(125_000, 10_000))
        assertEquals(150, BonusTerms(earnFixed = 150).earnFor(null))
        assertFalse(BonusTerms(earnFixed = 150).canSpend)
    }

    @Test fun aDealEarnsOnWhatIsPaid() {
        val terms = BonusTerms(earnPercent = 5, spendPercent = 30)
        val d = bonusDeal(terms, 125_000, balance = 300, use = true)
        assertEquals(BonusDeal(spend = 300, discount = 30_000, pay = 95_000, earn = 47), d)
        assertEquals(BonusDeal(0, 0, 125_000, 62), bonusDeal(terms, 125_000, 300, use = false))
        assertEquals(BonusDeal(0, 0, 125_000, 0), bonusDeal(null, 125_000, 300, use = true))
    }

    @Test fun aFirstRunStartsWithTheWelcomeGift() {
        val wallet = StoredCityBonus(InMemoryStorage(), { 1_000L }, scope)
        assertEquals(StoredCityBonus.WELCOME, wallet.balance.value)
        assertEquals("welcome", wallet.history.value.single().ref)
    }

    @Test fun spendingNeverGoesBelowZero() {
        val wallet = StoredCityBonus(InMemoryStorage(), { 1_000L }, scope, welcome = 100)
        assertFalse(wallet.spend(101, "too much", "food"))
        assertEquals(100, wallet.balance.value)
        assertTrue(wallet.spend(40, "ok", "food"))
        assertEquals(60, wallet.balance.value)
    }

    @Test fun aOnceEarningIsNotRepeated() {
        val wallet = StoredCityBonus(InMemoryStorage(), { 1_000L }, scope, welcome = 0)
        assertTrue(wallet.earn(50, "Обмен", "exchange", once = "exchange:gold:1"))
        assertFalse(wallet.earn(50, "Обмен", "exchange", once = "exchange:gold:1"))
        assertTrue(wallet.earn(50, "Обмен", "exchange", once = "exchange:gold:2"))
        assertEquals(100, wallet.balance.value)
    }

    @Test fun theAccountSurvivesARestart() {
        val storage = InMemoryStorage()
        StoredCityBonus(storage, { 1_000L }, scope).earn(62, "Заказ", "food")
        val again = StoredCityBonus(storage, { 2_000L }, scope)
        // Welcome once, not twice.
        assertEquals(StoredCityBonus.WELCOME + 62, again.balance.value)
        assertEquals(2, again.history.value.size)
    }

    @Test fun vietnameseDays() {
        // 2026-11-13 18:00 UTC is already the 14th in Nha Trang.
        assertEquals("14.11.2026", formatDay(1_794_592_800_000L))
    }
}
