package vn.touch.apps.visarun

import vn.touch.kit.BonusTerms
import vn.touch.kit.Contact

/**
 * Demo offers. Routes are the usual ones from Nha Trang; organisers, prices and numbers are
 * placeholders for the portal's own partners.
 */
internal object Catalog {

    val offers: List<Offer> = listOf(
        Offer(
            id = "mocbai-bus",
            title = "Мокбай — Камбоджа, автобус",
            route = Route.Cambodia,
            organiser = "Visarun NT",
            schedule = "Вт, пт, вс — выезд в 18:00",
            duration = "≈ 1,5 суток",
            price = 2_900_000,
            currency = "VND",
            includes = listOf("Спальный автобус туда и обратно", "Трансфер от отеля", "Сопровождение на границе"),
            notIncluded = listOf("Сборы на границе", "Питание"),
            documents = listOf("Загранпаспорт, срок ≥ 6 месяцев", "2 фото 4×6", "Наличные USD"),
            contacts = listOf(Contact.telegram("@demo_visarun_nt"), Contact.whatsApp("+84 900 000 401")),
            popular = true,
            bonus = BonusTerms(earnFixed = 150, spendPercent = 20),
        ),
        Offer(
            id = "moc-bai-fast",
            title = "Мокбай через Хошимин, минивэн",
            route = Route.Cambodia,
            organiser = "Saigon Border Trips",
            schedule = "Ежедневно, выезд в 05:00 из Хошимина",
            duration = "1 день + перелёт/поезд до Хошимина",
            price = 1_600_000,
            currency = "VND",
            includes = listOf("Минивэн Хошимин — граница — Хошимин", "Сопровождение"),
            notIncluded = listOf("Дорога Нячанг — Хошимин", "Сборы на границе"),
            documents = listOf("Загранпаспорт", "Наличные USD"),
            contacts = listOf(Contact.whatsApp("+84 900 000 402")),
        ),
        Offer(
            id = "laos-boy",
            title = "Бо И — Лаос, автобус",
            route = Route.Laos,
            organiser = "Central Visa Tour",
            schedule = "Ср, сб — выезд в 17:00",
            duration = "≈ 2 суток",
            price = 3_500_000,
            currency = "VND",
            includes = listOf("Автобус туда и обратно", "Ночёвка в Контуме", "Сопровождение"),
            notIncluded = listOf("Сборы на границе", "Питание"),
            documents = listOf("Загранпаспорт, срок ≥ 6 месяцев", "Наличные USD"),
            contacts = listOf(Contact.telegram("@demo_laos_run"), Contact.zalo("+84 900 000 403")),
            bonus = BonusTerms(earnFixed = 150, spendPercent = 20),
        ),
        Offer(
            id = "bangkok",
            title = "Бангкок — самолёт, 2 дня",
            route = Route.Flight,
            organiser = "Sky Visarun",
            schedule = "Любая дата, по наличию билетов",
            duration = "2 дня / 1 ночь",
            price = 290,
            currency = "USD",
            includes = listOf("Билеты из Камрани туда и обратно", "Отель 1 ночь", "Трансфер в аэропорт"),
            notIncluded = listOf("Питание", "Багаж сверх 7 кг"),
            documents = listOf("Загранпаспорт", "Обратный билет", "E-visa или безвиз на въезд"),
            contacts = listOf(Contact.whatsApp("+84 900 000 404"), Contact.telegram("@demo_sky_visarun")),
            popular = true,
            bonus = BonusTerms(earnFixed = 300),
        ),
        Offer(
            id = "kl",
            title = "Куала-Лумпур — самолёт, 1 день",
            route = Route.Flight,
            organiser = "Sky Visarun",
            schedule = "Пн, чт",
            duration = "1 день, в тот же день обратно",
            price = 240,
            currency = "USD",
            includes = listOf("Билеты из Камрани / Хошимина", "Помощь с e-visa на обратный въезд"),
            notIncluded = listOf("Отель", "Питание"),
            documents = listOf("Загранпаспорт", "Обратный билет"),
            contacts = listOf(Contact.whatsApp("+84 900 000 404")),
            bonus = BonusTerms(earnFixed = 200),
        ),
        Offer(
            id = "evisa-help",
            title = "Оформление e-visa на 90 дней",
            route = Route.Help,
            organiser = "Visa Help NT",
            schedule = "Онлайн, 3–5 рабочих дней",
            duration = "Без поездки — визу оформляют до выезда",
            price = 1_200_000,
            currency = "VND",
            includes = listOf("Заполнение анкеты", "Проверка фото и паспорта", "Оплата госпошлины"),
            notIncluded = listOf("Сама поездка на визаран"),
            documents = listOf("Скан паспорта", "Фото на белом фоне"),
            contacts = listOf(Contact.telegram("@demo_visa_help"), Contact.whatsApp("+84 900 000 405")),
            bonus = BonusTerms(earnPercent = 5, spendPercent = 30),
        ),
    )

    fun offer(id: String): Offer? = offers.firstOrNull { it.id == id }
}
