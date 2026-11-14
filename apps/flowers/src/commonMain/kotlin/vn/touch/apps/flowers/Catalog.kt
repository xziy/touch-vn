package vn.touch.apps.flowers

import vn.touch.kit.BonusTerms
import vn.touch.kit.Contact

/** Demo catalogue: placeholder shops and numbers, to be replaced by the portal's own feed. */
internal object Catalog {

    private val lotus = Shop("lotus", "Lotus Flowers", "ул. Нгуен Тхьен Тхуат, 12", listOf(Contact.whatsApp("+84 900 000 201"), Contact.zalo("+84 900 000 201")), bonus = BonusTerms(earnPercent = 5, spendPercent = 30))
    private val rose = Shop("rose", "Роза ветров", "ул. Хунг Выонг, 40", listOf(Contact.telegram("@demo_flowers_nt"), Contact.whatsApp("+84 900 000 202")), bonus = BonusTerms(earnPercent = 10, spendPercent = 20))
    private val dalat = Shop("dalat", "Dalat Garden", "ул. Тханг Нгуен, 3", listOf(Contact.zalo("+84 900 000 203"), Contact.phone("+84 900 000 203")))

    val bouquets: List<Bouquet> = listOf(
        Bouquet("red-25", "25 красных роз", Kind.Roses, 750_000, "Розы из Далата, 50 см, лента", "45 см", rose, hit = true),
        Bouquet("white-15", "15 белых роз", Kind.Roses, 480_000, "Белые розы, эвкалипт", "40 см", lotus),
        Bouquet("mix-101", "101 роза", Kind.Roses, 2_900_000, "Микс красных и розовых роз", "80 см", rose),
        Bouquet("spring", "Весеннее утро", Kind.Mixed, 550_000, "Хризантемы, гвоздики, альстромерии", "45 см", lotus, hit = true),
        Bouquet("sunny", "Подсолнухи", Kind.Mixed, 420_000, "5 подсолнухов, ромашки, зелень", "50 см", dalat),
        Bouquet("box-pink", "Розовая коробка", Kind.Box, 690_000, "Пионовидные розы и гортензия в шляпной коробке", "Ø 25 см", rose),
        Bouquet("box-macaron", "Коробка с макарунами", Kind.Box, 850_000, "Розы, кустовые розы, 6 макарунов", "Ø 30 см", lotus),
        Bouquet("basket-fruit", "Корзина с фруктами", Kind.Basket, 950_000, "Сезонные фрукты, розы, лилии", "50×35 см", dalat),
        Bouquet("orchid", "Орхидея в горшке", Kind.Tropical, 600_000, "Фаленопсис, 2 ветки", "60 см", dalat),
        Bouquet("lotus-bunch", "Лотосы", Kind.Tropical, 350_000, "10 бутонов лотоса", "60 см", lotus),
    )

    fun bouquet(id: String): Bouquet? = bouquets.firstOrNull { it.id == id }
}
