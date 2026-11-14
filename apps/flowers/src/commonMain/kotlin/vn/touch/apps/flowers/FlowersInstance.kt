package vn.touch.apps.flowers

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
import vn.touch.kit.BonusDeal
import vn.touch.kit.CityBonus
import vn.touch.kit.PortalInstance
import vn.touch.kit.PortalScreen
import vn.touch.kit.bonusDeal

internal sealed interface FlowersPage {
    data object List : FlowersPage
    data class Detail(val id: String) : FlowersPage
    data class Order(val id: String) : FlowersPage
    data object Sent : FlowersPage
}

internal class FlowersInstance(private val runtime: InstanceRuntime) : PortalInstance<FlowersPage>(FlowersPage.List) {

    private val _kind = MutableStateFlow<Kind?>(null)
    val kind: StateFlow<Kind?> get() = _kind.asStateFlow()

    /** The city bonus account; null where the host offers none. */
    val bonus: CityBonus? = runtime.service(CityBonus.Key)

    private val _useBonus = mutableStateOf(true)
    val useBonus: State<Boolean> get() = _useBonus

    /** What the last order earned and spent, for the "thank you" page. */
    var lastBonus: Pair<Long, Long> = 0L to 0L
        private set

    private val _order = mutableStateOf(FlowerOrder())
    val order: State<FlowerOrder> get() = _order

    override suspend fun onLaunch() {
        // Who orders is remembered; who receives is not.
        val last = runtime.storage.getJson<FlowerOrder>(KEY_SENDER)
        if (last != null) _order.value = FlowerOrder(sender = last.sender, senderPhone = last.senderPhone)
        runtime.analytics.screen(FlowersApp.ID)
    }

    override fun titleOf(page: FlowersPage): String? = when (page) {
        FlowersPage.List -> null
        is FlowersPage.Detail -> Catalog.bouquet(page.id)?.name
        is FlowersPage.Order -> "Оформление"
        FlowersPage.Sent -> "Заказ отправлен"
    }

    fun filter(kind: Kind?) { _kind.value = kind }
    fun open(id: String) = push(FlowersPage.Detail(id))
    fun startOrder(id: String) = push(FlowersPage.Order(id))
    fun edit(transform: (FlowerOrder) -> FlowerOrder) { _order.value = transform(_order.value) }
    fun useBonus(on: Boolean) { _useBonus.value = on }

    fun deal(b: Bouquet, spending: Boolean): BonusDeal =
        bonusDeal(b.shop.bonus, b.price, bonus?.balance?.value ?: 0, spending && _useBonus.value)

    /** Called once the messenger has been opened with the order for [bouquetId]. */
    fun sent(bouquetId: String) {
        lastBonus = 0L to 0L
        val b = Catalog.bouquet(bouquetId)
        if (bonus != null && b != null) {
            val deal = deal(b, spending = true)
            val spent = if (bonus.spend(deal.spend, "Оплата букета · ${b.shop.name}", FlowersApp.ID)) deal.spend else 0
            val earn = if (spent == deal.spend) deal.earn else b.shop.bonus?.earnFor(b.price) ?: 0
            bonus.earn(earn, "Букет «${b.name}» · ${b.shop.name}", FlowersApp.ID)
            lastBonus = earn to spent
        }
        sent()
    }

    fun sent() {
        runtime.analytics.event("flowers_order")
        val o = _order.value
        runtime.scope.launch { runtime.storage.setJson(KEY_SENDER, FlowerOrder(sender = o.sender, senderPhone = o.senderPhone)) }
        _order.value = FlowerOrder(sender = o.sender, senderPhone = o.senderPhone)
        toRoot()
        push(FlowersPage.Sent)
    }

    @Composable
    override fun Content() { vn.touch.kit.PortalTheme { PortalScreen(this, bonus) { FlowersScreen(this) } } }

    companion object { const val KEY_SENDER = "sender" }
}
