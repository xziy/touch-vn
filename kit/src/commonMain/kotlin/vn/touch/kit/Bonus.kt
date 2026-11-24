package vn.touch.kit

import cx.m42.superizer.runtime.Clock
import cx.m42.superizer.runtime.ServiceKey
import cx.m42.superizer.runtime.StorageService
import cx.m42.superizer.runtime.getJson
import cx.m42.superizer.runtime.setJson
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/*
 * Городская бонусная система — one bonus account for the whole portal, the way Trip.com has its
 * coins: an order at a partner earns bonuses, and bonuses pay part of the next order at any
 * partner, in any service. Not every vendor takes part; one that does carries [BonusTerms].
 *
 * The account is a host service ([CityBonus.Key]) rather than any app's storage, because every app
 * spends from and adds to the same balance and an app cannot read another's storage.
 */

/** What one bonus is worth when it pays for an order. */
public const val VND_PER_BONUS: Long = 100

/** The display name, used on every screen that mentions the programme. */
public const val CITY_BONUS_TITLE: String = "Городские бонусы"

/**
 * A partner's deal. [earnPercent] of the price paid comes back as bonuses, plus [earnFixed] per
 * order; up to [spendPercent] of the price can be paid with bonuses (0: this partner only gives).
 */
@Serializable
public data class BonusTerms(
    val earnPercent: Int = 0,
    val earnFixed: Long = 0,
    val spendPercent: Int = 0,
) {
    init {
        require(earnPercent in 0..100 && spendPercent in 0..100 && earnFixed >= 0) { "bad terms $this" }
    }

    /** Bonuses for paying [paidVnd]; null when the price is not in dong (only [earnFixed] counts). */
    public fun earnFor(paidVnd: Long?): Long =
        earnFixed + (paidVnd?.let { it.coerceAtLeast(0) * earnPercent / 100 / VND_PER_BONUS } ?: 0)

    /** The most bonuses this partner takes on a [priceVnd] order from someone holding [balance]. */
    public fun maxSpend(priceVnd: Long, balance: Long): Long =
        minOf(balance.coerceAtLeast(0), priceVnd.coerceAtLeast(0) * spendPercent / 100 / VND_PER_BONUS)

    public val canSpend: Boolean get() = spendPercent > 0
}

/** One order's sums once bonuses are counted: how many are spent, what is left to pay, what comes back. */
public data class BonusDeal(val spend: Long, val discount: Long, val pay: Long, val earn: Long)

/**
 * The deal for an order of [priceVnd] at a vendor with [terms] (null: not a partner). With [use]
 * the most the partner allows is spent; bonuses are earned on what is actually paid.
 */
public fun bonusDeal(terms: BonusTerms?, priceVnd: Long, balance: Long, use: Boolean): BonusDeal {
    if (terms == null) return BonusDeal(0, 0, priceVnd, 0)
    val spend = if (use) terms.maxSpend(priceVnd, balance) else 0
    val discount = spend * VND_PER_BONUS
    val pay = priceVnd - discount
    return BonusDeal(spend, discount, pay, terms.earnFor(pay))
}

/** One line of the account's history. [amount] is signed: plus earned, minus spent. */
@Serializable
public data class BonusEntry(
    val id: String,
    val at: Long,
    val amount: Long,
    val title: String,
    /** The app id it happened in; empty for the programme's own (the welcome gift). */
    val service: String = "",
    /** Set for an earning that may happen only once, such as "first message to this partner". */
    val ref: String? = null,
)

/** A service of the portal as the bonus screen lists it: where bonuses come from and go to. */
public data class BonusPlace(
    val appId: String,
    val title: String,
    val glyph: Glyph,
    val earn: String,
    val spend: String,
    /** The service's colour, for its icon in the list. */
    val tone: Tone = Tone.Lagoon,
)

/** The portal-wide bonus account. Apps reach it with `runtime.service(CityBonus.Key)`. */
public interface CityBonus {
    public val balance: StateFlow<Long>
    public val history: StateFlow<List<BonusEntry>>

    /** Every service and its part in the programme, for the bonus screen. */
    public val places: List<BonusPlace>

    /**
     * Adds [amount] bonuses. With [once] set, an earning with the same ref is not repeated and
     * false comes back. Zero or less earns nothing.
     */
    public fun earn(amount: Long, title: String, service: String, once: String? = null): Boolean

    /** Takes [amount] bonuses; false — and nothing taken — when the balance is short. */
    public fun spend(amount: Long, title: String, service: String): Boolean

    /**
     * Opens the service [appId] from the bonus screen. The host's to do: an app opening another
     * is closed on the way, so it cannot be the one waiting for the answer.
     */
    public fun open(appId: String) {}

    public companion object {
        public val Key: ServiceKey<CityBonus> = ServiceKey("city-bonus")
    }
}

/**
 * The account kept in [storage]: the history is the record, the balance is its sum. A first run
 * starts with [welcome] bonuses so the programme can be tried at once.
 *
 * Changes are applied in memory at once — a checkout must know straight away whether the spend
 * went through — and written behind. The history read at start is merged with anything that
 * happened before it arrived, so an early order is never lost to a late read.
 */
public class StoredCityBonus(
    private val storage: StorageService,
    private val clock: Clock,
    private val scope: CoroutineScope,
    override val places: List<BonusPlace> = emptyList(),
    private val welcome: Long = WELCOME,
    private val opener: (appId: String) -> Unit = {},
) : CityBonus {

    override fun open(appId: String): Unit = opener(appId)

    private val _history = MutableStateFlow<List<BonusEntry>>(emptyList())
    override val history: StateFlow<List<BonusEntry>> get() = _history.asStateFlow()

    private val _balance = MutableStateFlow(0L)
    override val balance: StateFlow<Long> get() = _balance.asStateFlow()

    init {
        scope.launch {
            val stored = storage.getJson<List<BonusEntry>>(KEY_HISTORY)
            val start = stored ?: listOfNotNull(
                if (welcome > 0) entry(welcome, "Приветственные бонусы", "", ref = "welcome") else null,
            )
            change { early -> early + start.filter { s -> early.none { it.id == s.id } } }
        }
    }

    override fun earn(amount: Long, title: String, service: String, once: String?): Boolean {
        if (amount <= 0) return false
        if (once != null && _history.value.any { it.ref == once }) return false
        change { listOf(entry(amount, title, service, once)) + it }
        return true
    }

    override fun spend(amount: Long, title: String, service: String): Boolean {
        if (amount <= 0) return amount == 0L
        if (_balance.value < amount) return false
        change { listOf(entry(-amount, title, service, null)) + it }
        return true
    }

    private fun entry(amount: Long, title: String, service: String, ref: String?): BonusEntry =
        BonusEntry(Random.nextLong().toULong().toString(36), clock.now(), amount, title, service, ref)

    private fun change(transform: (List<BonusEntry>) -> List<BonusEntry>) {
        _history.update { list -> transform(list).sortedByDescending { it.at } }
        _balance.value = _history.value.sumOf { it.amount }
        scope.launch { storage.setJson(KEY_HISTORY, _history.value) }
    }

    public companion object {
        public const val KEY_HISTORY: String = "history"
        public const val WELCOME: Long = 300
    }
}

/** "1 бонус", "2 бонуса", "5 бонусов". */
public fun bonusWord(n: Long): String {
    val abs = if (n < 0) -n else n
    val tens = abs % 100
    val ones = abs % 10
    return when {
        tens in 11..14 -> "бонусов"
        ones == 1L -> "бонус"
        ones in 2..4 -> "бонуса"
        else -> "бонусов"
    }
}

/** "1 250 бонусов". */
public fun formatBonus(n: Long): String = "${groupDigits(n)} ${bonusWord(n)}"

/** "+375 бонусов" — what an offer gives; null when it gives nothing, so the badge is not drawn. */
public fun bonusBadge(amount: Long): String? = if (amount > 0) "+${formatBonus(amount)}" else null

/** A partner's rate for a list row, where the order's sum is not known yet: "+5% бонусами", "+150 бонусов". */
public fun bonusRate(terms: BonusTerms?): String? = when {
    terms == null -> null
    terms.earnPercent > 0 && terms.earnFixed > 0 -> "+${terms.earnPercent}% и +${formatBonus(terms.earnFixed)}"
    terms.earnPercent > 0 -> "+${terms.earnPercent}% бонусами"
    terms.earnFixed > 0 -> "+${formatBonus(terms.earnFixed)}"
    else -> null
}
