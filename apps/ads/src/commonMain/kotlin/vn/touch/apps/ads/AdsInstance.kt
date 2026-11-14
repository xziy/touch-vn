package vn.touch.apps.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cx.m42.superizer.runtime.InstanceRuntime
import cx.m42.superizer.runtime.getJson
import cx.m42.superizer.runtime.setJson
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.touch.kit.CityBonus
import vn.touch.kit.PortalInstance
import vn.touch.kit.PortalScreen

internal sealed interface AdsPage {
    data object Feed : AdsPage
    data class Detail(val id: String) : AdsPage
    data object New : AdsPage
    data object Mine : AdsPage
}

/**
 * The feed is the demo catalogue plus the person's own ads. Without a server the own ads live in
 * `runtime.storage` and are shown as "на модерации": the screen is honest that nobody else sees
 * them yet.
 */
internal class AdsInstance(private val runtime: InstanceRuntime) : PortalInstance<AdsPage>(AdsPage.Feed) {

    /** The city bonus account; null where the host offers none. */
    val bonus: CityBonus? = runtime.service(CityBonus.Key)

    private var demo: List<Ad> = emptyList()

    private val _mine = MutableStateFlow<List<Ad>>(emptyList())
    val mine: StateFlow<List<Ad>> get() = _mine.asStateFlow()

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> get() = _category.asStateFlow()

    private val _query = mutableStateOf("")
    val query: State<String> get() = _query

    private val _draft = mutableStateOf(Draft())
    val draft: State<Draft> get() = _draft

    val now: Long get() = runtime.clock.now()

    override suspend fun onLaunch() {
        demo = Catalog.demo(now)
        _mine.value = runtime.storage.getJson<List<Ad>>(KEY_MINE).orEmpty()
        runtime.analytics.screen(AdsApp.ID)
    }

    override fun titleOf(page: AdsPage): String? = when (page) {
        AdsPage.Feed -> null
        is AdsPage.Detail -> ad(page.id)?.category?.label
        AdsPage.New -> "Новое объявление"
        AdsPage.Mine -> "Мои объявления"
    }

    fun all(mine: List<Ad>): List<Ad> = mine + demo
    fun ad(id: String): Ad? = all(_mine.value).firstOrNull { it.id == id }

    fun setCategory(c: Category?) { _category.value = c }
    fun setQuery(q: String) { _query.value = q }
    fun open(id: String) = push(AdsPage.Detail(id))
    fun openNew() = push(AdsPage.New)
    fun openMine() = push(AdsPage.Mine)
    fun editDraft(transform: (Draft) -> Draft) { _draft.value = transform(_draft.value) }

    fun publish(): Boolean {
        val d = _draft.value
        if (d.problems().isNotEmpty()) return false
        val ad = d.toAd("my-" + Random.nextLong().toULong().toString(36), now)
        save(listOf(ad) + _mine.value)
        // Name and contact stay for the next ad.
        _draft.value = Draft(author = d.author, contactKind = d.contactKind, contact = d.contact)
        runtime.analytics.event("ads_publish")
        replace(AdsPage.Mine)
        return true
    }

    /**
     * Wrote to a business that is a bonus partner. Without a checkout there is nothing to spend
     * here; the bonus comes once per ad, so tapping the button twice earns nothing more.
     */
    fun contacted(ad: Ad) {
        val terms = ad.bonus ?: return
        bonus?.earn(terms.earnFor(null), "Объявление · ${ad.author}", AdsApp.ID, once = "ads:${ad.id}")
    }

    fun delete(id: String) {
        save(_mine.value.filterNot { it.id == id })
        if (page is AdsPage.Detail) back()
    }

    private fun save(list: List<Ad>) {
        _mine.value = list
        runtime.scope.launch { runtime.storage.setJson(KEY_MINE, list) }
    }

    @Composable
    override fun Content() { vn.touch.kit.PortalTheme { PortalScreen(this, bonus) { AdsScreen(this) } } }

    companion object { const val KEY_MINE = "mine" }
}
