package vn.touch.apps.exchange

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cx.m42.superizer.runtime.InstanceRuntime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.touch.kit.CityBonus
import vn.touch.kit.PortalInstance
import vn.touch.kit.PortalScreen

internal sealed interface ExchangePage {
    data object List : ExchangePage
    data class Detail(val id: String) : ExchangePage
}

internal class ExchangeInstance(private val runtime: InstanceRuntime) : PortalInstance<ExchangePage>(ExchangePage.List) {

    /** The city bonus account; null where the host offers none. */
    val bonus: CityBonus? = runtime.service(CityBonus.Key)

    private val _currency = MutableStateFlow(Currency.USD)
    val currency: StateFlow<Currency> get() = _currency.asStateFlow()

    private val _amount = mutableStateOf("100")
    val amount: State<String> get() = _amount

    override suspend fun onLaunch() {
        runtime.storage.get(KEY_CURRENCY)?.let { saved -> Currency.entries.firstOrNull { it.name == saved }?.let { _currency.value = it } }
        runtime.storage.get(KEY_AMOUNT)?.let { _amount.value = it }
        runtime.analytics.screen(ExchangeApp.ID)
    }

    override fun titleOf(page: ExchangePage): String? = when (page) {
        ExchangePage.List -> null
        is ExchangePage.Detail -> Catalog.point(page.id)?.type?.label
    }

    fun select(c: Currency) {
        _currency.value = c
        runtime.scope.launch { runtime.storage.set(KEY_CURRENCY, c.name) }
    }

    fun setAmount(text: String) {
        val clean = text.filter { it.isDigit() || it == '.' || it == ',' || it == ' ' }.take(12)
        _amount.value = clean
        runtime.scope.launch { runtime.storage.set(KEY_AMOUNT, clean) }
    }

    fun open(id: String) = push(ExchangePage.Detail(id))

    /** Wrote to a partner point: its bonus, once a day per point (Vietnam's day). */
    fun contacted(p: Point) {
        val terms = p.bonus ?: return
        val day = (runtime.clock.now() + 7 * 3_600_000L).floorDiv(86_400_000L)
        bonus?.earn(terms.earnFor(null), "Обмен валюты · ${p.name}", ExchangeApp.ID, once = "exchange:${p.id}:$day")
    }

    @Composable
    override fun Content() { vn.touch.kit.PortalTheme(vn.touch.kit.Tone.Jade) { PortalScreen(this, bonus) { ExchangeScreen(this) } } }

    companion object {
        const val KEY_CURRENCY = "currency"
        const val KEY_AMOUNT = "amount"
    }
}
