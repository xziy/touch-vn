package vn.touch.apps.visarun

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cx.m42.superizer.runtime.InstanceRuntime
import cx.m42.superizer.runtime.getJson
import cx.m42.superizer.runtime.setJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import vn.touch.kit.BonusDeal
import vn.touch.kit.CityBonus
import vn.touch.kit.PortalInstance
import vn.touch.kit.PortalScreen
import vn.touch.kit.bonusDeal

internal sealed interface VisarunPage {
    data object List : VisarunPage
    data class Detail(val id: String) : VisarunPage
    data class Book(val id: String) : VisarunPage
    data object Calculator : VisarunPage
    data object Sent : VisarunPage
}

/** What the calculator remembers between visits: the entry date and the regime. */
@Serializable
internal data class StayInput(val entry: String = "", val regime: Regime = Regime.VisaFree45)

internal class VisarunInstance(private val runtime: InstanceRuntime) : PortalInstance<VisarunPage>(VisarunPage.List) {

    private val _route = MutableStateFlow<Route?>(null)
    val route: StateFlow<Route?> get() = _route.asStateFlow()

    /** The city bonus account; null where the host offers none. */
    val bonus: CityBonus? = runtime.service(CityBonus.Key)

    private val _useBonus = mutableStateOf(true)
    val useBonus: State<Boolean> get() = _useBonus

    /** What the last booking earned and spent, for the "thank you" page. */
    var lastBonus: Pair<Long, Long> = 0L to 0L
        private set

    private val _booking = mutableStateOf(Booking())
    val booking: State<Booking> get() = _booking

    private val _stay = mutableStateOf(StayInput())
    val stayInput: State<StayInput> get() = _stay

    val today: Long get() = todayInVietnam(runtime.clock.now())

    override suspend fun onLaunch() {
        _stay.value = runtime.storage.getJson<StayInput>(KEY_STAY) ?: StayInput()
        runtime.storage.getJson<Booking>(KEY_CONTACT)?.let { _booking.value = Booking(name = it.name, phone = it.phone, hotel = it.hotel) }
        runtime.analytics.screen(VisarunApp.ID)
    }

    override fun titleOf(page: VisarunPage): String? = when (page) {
        VisarunPage.List -> null
        is VisarunPage.Detail -> Catalog.offer(page.id)?.route?.label
        is VisarunPage.Book -> "Заявка"
        VisarunPage.Calculator -> "Срок пребывания"
        VisarunPage.Sent -> "Заявка отправлена"
    }

    fun filter(r: Route?) { _route.value = r }
    fun open(id: String) = push(VisarunPage.Detail(id))
    fun book(id: String) = push(VisarunPage.Book(id))
    fun openCalculator() = push(VisarunPage.Calculator)

    fun editBooking(t: (Booking) -> Booking) { _booking.value = t(_booking.value) }
    fun useBonus(on: Boolean) { _useBonus.value = on }

    /** A tour in another currency takes no bonuses (the price is not in dong) but still gives the fixed part. */
    fun deal(o: Offer, people: Int, spending: Boolean): BonusDeal {
        val vnd = o.priceVnd(people)
        val d = bonusDeal(o.bonus, vnd ?: 0, bonus?.balance?.value ?: 0, spending && _useBonus.value && vnd != null)
        return if (vnd == null) d.copy(pay = 0, earn = o.bonus?.earnFor(null) ?: 0) else d
    }

    fun editStay(t: (StayInput) -> StayInput) {
        _stay.value = t(_stay.value)
        val v = _stay.value
        runtime.scope.launch { runtime.storage.setJson(KEY_STAY, v) }
    }

    fun sent(offerId: String) {
        runtime.analytics.event("visarun_booking")
        val b = _booking.value
        lastBonus = 0L to 0L
        val o = Catalog.offer(offerId)
        if (bonus != null && o != null) {
            val people = b.people.toIntOrNull() ?: 1
            val deal = deal(o, people, spending = true)
            val spent = if (bonus.spend(deal.spend, "Оплата тура · ${o.title}", VisarunApp.ID)) deal.spend else 0
            val earn = if (spent == deal.spend) deal.earn else o.bonus?.earnFor(o.priceVnd(people)) ?: 0
            bonus.earn(earn, "Визаран · ${o.title}", VisarunApp.ID)
            lastBonus = earn to spent
        }
        runtime.scope.launch { runtime.storage.setJson(KEY_CONTACT, b) }
        toRoot()
        push(VisarunPage.Sent)
    }

    @Composable
    override fun Content() { vn.touch.kit.PortalTheme { PortalScreen(this, bonus) { VisarunScreen(this) } } }

    companion object {
        const val KEY_STAY = "stay"
        const val KEY_CONTACT = "contact"
    }
}
