package vn.touch.kit

/** "1 250 000" — thousands split by a thin space, the way prices are written in Nha Trang shops. */
public fun groupDigits(value: Long): String {
    val negative = value < 0
    val digits = (if (negative) -value else value).toString()
    val grouped = digits.reversed().chunked(3).joinToString(" ").reversed()
    return if (negative) "-$grouped" else grouped
}

/** "150 000 ₫". */
public fun formatVnd(amount: Long): String = "${groupDigits(amount)} ₫"

/** "150k ₫" — short form for chips and tight rows. */
public fun formatVndShort(amount: Long): String = when {
    amount >= 1_000_000 && amount % 100_000 == 0L -> {
        val millions = amount / 1_000_000
        val tenth = (amount % 1_000_000) / 100_000
        if (tenth == 0L) "${millions}M ₫" else "$millions.${tenth}M ₫"
    }
    amount >= 1_000 && amount % 1_000 == 0L -> "${amount / 1_000}k ₫"
    else -> formatVnd(amount)
}

/** "$45" / "45 $" style is ambiguous across audiences; the portal writes the code. */
public fun formatMoney(amount: Long, currency: String): String =
    if (currency == "VND") formatVnd(amount) else "${groupDigits(amount)} $currency"

/** A phone number a vendor can call back: 9 to 15 digits, whatever the spelling around them. */
public fun looksLikePhone(value: String): Boolean = value.count { it.isDigit() } in 9..15
