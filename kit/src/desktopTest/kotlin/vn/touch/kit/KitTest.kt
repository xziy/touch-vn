package vn.touch.kit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KitTest {

    @Test fun pricesAreGroupedByThousands() {
        assertEquals("0 ₫", formatVnd(0))
        assertEquals("999 ₫", formatVnd(999))
        assertEquals("150 000 ₫", formatVnd(150_000))
        assertEquals("1 250 000 ₫", formatVnd(1_250_000))
        assertEquals("-5 000", groupDigits(-5_000))
    }

    @Test fun shortPrices() {
        assertEquals("150k ₫", formatVndShort(150_000))
        assertEquals("2M ₫", formatVndShort(2_000_000))
        assertEquals("1.5M ₫", formatVndShort(1_500_000))
        assertEquals("1 250 ₫", formatVndShort(1_250))
    }

    @Test fun messagesArePercentEncodedAsUtf8() {
        assertEquals("%D0%97%D0%B0%D0%BA%D0%B0%D0%B7%201", urlEncode("Заказ 1"))
        assertEquals("a-b_c.d~", urlEncode("a-b_c.d~"))
    }

    @Test fun contactLinks() {
        assertEquals("https://wa.me/84905123456?text=hi", Contact.whatsApp("+84 905 123 456").url("hi"))
        assertEquals("https://wa.me/84905123456", Contact.whatsApp("+84 905 123 456").url())
        assertEquals("https://t.me/nt_food?text=hi", Contact.telegram("@nt_food").url("hi"))
        assertEquals("https://zalo.me/84905123456", Contact.zalo("+84-905-123-456").url("ignored"))
        assertEquals("tel:+84905123456", Contact.phone("+84 905 123 456").url())
    }

    @Test fun phones() {
        assertTrue(looksLikePhone("+84 905 123 456"))
        assertTrue(looksLikePhone("0905123456"))
        assertFalse(looksLikePhone("12345"))
    }
}
