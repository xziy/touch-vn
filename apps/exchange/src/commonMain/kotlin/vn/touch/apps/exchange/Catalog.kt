package vn.touch.apps.exchange

import vn.touch.kit.BonusTerms
import vn.touch.kit.Contact

/**
 * Demo rates: realistic in size, invented in detail. A live portal pulls them from the points
 * themselves several times a day; [UPDATED] says how old these are.
 */
internal object Catalog {

    const val UPDATED: String = "сегодня, 09:00 (демо-данные)"

    val points: List<Point> = listOf(
        Point(
            "gold-dam", "Ювелирный «Ким Тхань»", PointType.Gold, "ул. Фан Бой Тяу, рядом с рынком Чо Дам", "07:30–20:00",
            mapOf(Currency.USD to 26_310.0, Currency.EUR to 30_250.0, Currency.RUB to 305.0),
            delivery = false, note = "Лучший курс за купюры 100 USD нового образца.",
            contacts = listOf(Contact.phone("+84 900 000 501"), Contact.zalo("+84 900 000 501")),
            bonus = BonusTerms(earnFixed = 50),
        ),
        Point(
            "gold-center", "Ювелирный «Хоанг Ань»", PointType.Gold, "ул. Хунг Выонг, 18", "08:00–21:00",
            mapOf(Currency.USD to 26_280.0, Currency.EUR to 30_300.0, Currency.RUB to 300.0),
            delivery = false, note = "Евро меняют по хорошему курсу, рубли — только купюры 5000.",
            contacts = listOf(Contact.zalo("+84 900 000 502")),
        ),
        Point(
            "bank-vcb", "Банк (отделение в центре)", PointType.Bank, "ул. Ко Бак", "07:30–16:30, пн–пт",
            mapOf(Currency.USD to 26_150.0, Currency.EUR to 29_900.0),
            delivery = false, note = "Нужен паспорт. Курс ниже, зато официально и с чеком.",
            contacts = listOf(Contact.phone("+84 900 000 503")),
        ),
        Point(
            "private-rub", "Обмен рублей — перевод на карту", PointType.Private, "Доставка наличных по городу", "09:00–22:00",
            mapOf(Currency.RUB to 312.0, Currency.USDT to 26_200.0),
            delivery = true, note = "Вы переводите рубли на карту, курьер привозит донги. От 10 000 RUB.",
            contacts = listOf(Contact.telegram("@demo_rub_vnd_nt"), Contact.whatsApp("+84 900 000 504")),
            bonus = BonusTerms(earnFixed = 100),
        ),
        Point(
            "private-usdt", "Крипто-обмен USDT", PointType.Private, "Офис на севере / доставка", "10:00–21:00",
            mapOf(Currency.USDT to 26_250.0, Currency.USD to 26_200.0, Currency.RUB to 309.0),
            delivery = true, note = "USDT TRC-20. Встреча в офисе или курьер.",
            contacts = listOf(Contact.telegram("@demo_usdt_nt")),
            bonus = BonusTerms(earnFixed = 50),
        ),
        Point(
            "hotel-desk", "Обменник на набережной", PointType.Private, "ул. Чан Фу, у отелей", "08:00–23:00",
            mapOf(Currency.USD to 25_900.0, Currency.EUR to 29_500.0, Currency.RUB to 290.0),
            delivery = false, note = "Удобно ночью, но курс заметно ниже.",
            contacts = listOf(Contact.whatsApp("+84 900 000 506")),
        ),
    )

    fun point(id: String): Point? = points.firstOrNull { it.id == id }
}
