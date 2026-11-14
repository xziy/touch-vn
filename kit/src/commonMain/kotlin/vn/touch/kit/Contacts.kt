package vn.touch.kit

import kotlinx.serialization.Serializable

/** How the people of Nha Trang actually reach a business: a call, or one of three messengers. */
@Serializable
public enum class ContactKind { Phone, WhatsApp, Telegram, Zalo }

/**
 * One way to reach a vendor. [value] is a phone number in any spelling ("+84 905 123 456") or, for
 * Telegram, a username ("@nhatrang_flowers").
 */
@Serializable
public data class Contact(val kind: ContactKind, val value: String) {

    public val label: String
        get() = when (kind) {
            ContactKind.Phone -> "Позвонить"
            ContactKind.WhatsApp -> "WhatsApp"
            ContactKind.Telegram -> "Telegram"
            ContactKind.Zalo -> "Zalo"
        }

    /**
     * The link that opens this contact. A [message] is prefilled where the messenger allows it
     * (WhatsApp and Telegram); Zalo and a phone call take none.
     */
    public fun url(message: String? = null): String {
        val digits = value.filter { it.isDigit() }
        val text = message?.takeIf { it.isNotBlank() }?.let(::urlEncode)
        return when (kind) {
            ContactKind.Phone -> "tel:+$digits"
            ContactKind.WhatsApp -> "https://wa.me/$digits" + (text?.let { "?text=$it" } ?: "")
            ContactKind.Telegram -> "https://t.me/${value.trim().removePrefix("@")}" + (text?.let { "?text=$it" } ?: "")
            ContactKind.Zalo -> "https://zalo.me/$digits"
        }
    }

    public companion object {
        public fun phone(value: String): Contact = Contact(ContactKind.Phone, value)
        public fun whatsApp(value: String): Contact = Contact(ContactKind.WhatsApp, value)
        public fun telegram(value: String): Contact = Contact(ContactKind.Telegram, value)
        public fun zalo(value: String): Contact = Contact(ContactKind.Zalo, value)
    }
}

/** RFC 3986 percent-encoding of UTF-8; common code has no URLEncoder. */
public fun urlEncode(value: String): String {
    val unreserved = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~"
    val out = StringBuilder()
    for (byte in value.encodeToByteArray()) {
        val c = byte.toInt() and 0xFF
        if (c < 0x80 && unreserved.indexOf(c.toChar()) >= 0) {
            out.append(c.toChar())
        } else {
            out.append('%').append("0123456789ABCDEF"[c shr 4]).append("0123456789ABCDEF"[c and 0xF])
        }
    }
    return out.toString()
}
