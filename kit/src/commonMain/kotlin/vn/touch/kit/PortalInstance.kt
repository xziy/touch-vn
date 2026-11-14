package vn.touch.kit

import cx.m42.superizer.app.AppChrome
import cx.m42.superizer.app.AppInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * An app made of pages stacked on each other: a list, a card opened from it, a form opened from
 * the card. The stack drives the host's top bar ([chrome]) and the back gesture.
 *
 * Every portal app has that shape, which is why it lives here once rather than in each of them.
 *
 * Over the stack, any page can open the city bonus screen ([openBonus]): it shows in place, on
 * the first tap, and back returns to the page it was opened from — the order being filled in is
 * still there. [PortalScreen] draws it.
 */
public abstract class PortalInstance<P : Any>(root: P) : AppInstance() {

    private val _pages = MutableStateFlow(listOf(root))
    public val pages: StateFlow<List<P>> get() = _pages.asStateFlow()

    public val page: P get() = _pages.value.last()

    private val _bonusOpen = MutableStateFlow(false)
    public val bonusOpen: StateFlow<Boolean> get() = _bonusOpen.asStateFlow()

    private val _chrome = MutableStateFlow(AppChrome())
    override val chrome: StateFlow<AppChrome> get() = _chrome.asStateFlow()

    /** The bar's title for [page]; null keeps the app's own name. */
    protected abstract fun titleOf(page: P): String?

    public fun push(page: P) {
        _bonusOpen.value = false
        _pages.value = _pages.value + page
        refreshChrome()
    }

    /** Replaces the top page — "order sent" in place of the form, so back does not reopen it. */
    public fun replace(page: P) {
        _bonusOpen.value = false
        _pages.value = _pages.value.dropLast(1) + page
        refreshChrome()
    }

    /** Back inside the app. False at the root, where the host takes over and closes it. */
    public fun back(): Boolean {
        if (_bonusOpen.value) {
            closeBonus()
            return true
        }
        if (_pages.value.size <= 1) return false
        _pages.value = _pages.value.dropLast(1)
        refreshChrome()
        return true
    }

    public fun toRoot() {
        _bonusOpen.value = false
        _pages.value = _pages.value.take(1)
        refreshChrome()
    }

    /** The city bonus screen over whatever page is open. */
    public fun openBonus() {
        _bonusOpen.value = true
        refreshChrome()
    }

    public fun closeBonus() {
        _bonusOpen.value = false
        refreshChrome()
    }

    protected fun refreshChrome() {
        _chrome.value = if (_bonusOpen.value) {
            AppChrome(title = CITY_BONUS_TITLE, canGoBack = true)
        } else {
            AppChrome(title = titleOf(page), canGoBack = _pages.value.size > 1)
        }
    }
}
