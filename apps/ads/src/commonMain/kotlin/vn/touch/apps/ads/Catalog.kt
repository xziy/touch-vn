package vn.touch.apps.ads

import vn.touch.kit.BonusTerms
import vn.touch.kit.Contact

/** Demo ads, dated relative to now so the feed always looks alive. Placeholder contacts. */
internal object Catalog {

    private const val HOUR = 3_600_000L

    fun demo(now: Long): List<Ad> = listOf(
        Ad("a1", Category.Rent, "Студия у моря, 5 мин до пляжа", "Студия 35 м², ул. Чан Фу. Кондиционер, кухня, бассейн на крыше. От 3 месяцев.", 9_000_000, per = "в месяц", district = District.Center, author = "Мария", contacts = listOf(Contact.telegram("@demo_rent_nt"), Contact.whatsApp("+84 900 000 301")), createdAt = now - 2 * HOUR),
        Ad("a2", Category.Rent, "Дом 3 спальни на Севере", "Винь Хоа, двор, парковка для байков, тихий район. Можно с животными.", 18_000_000, per = "в месяц", district = District.North, author = "Нгуен", contacts = listOf(Contact.zalo("+84 900 000 302")), createdAt = now - 20 * HOUR),
        Ad("a3", Category.Rent, "Квартира посуточно в Панораме", "Вид на море, 1 спальня, 2 гостя. Заселение 24/7.", 800_000, per = "в сутки", district = District.Center, author = "Алексей", contacts = listOf(Contact.whatsApp("+84 900 000 303")), bonus = BonusTerms(earnFixed = 100), createdAt = now - 3 * 24 * HOUR),
        Ad("b1", Category.Bikes, "Аренда Honda Air Blade", "125 cc, 2023 г. Шлемы в подарок, доставка к отелю.", 150_000, per = "в сутки", district = District.Center, author = "Байк-прокат Дмитрия", contacts = listOf(Contact.telegram("@demo_bike_nt"), Contact.whatsApp("+84 900 000 304")), bonus = BonusTerms(earnFixed = 100), createdAt = now - 5 * HOUR),
        Ad("b2", Category.Bikes, "Продам Yamaha NVX", "2021 г., голубая карточка, пробег 18 000 км, обслужен.", 28_000_000, district = District.South, author = "Игорь", contacts = listOf(Contact.whatsApp("+84 900 000 305")), createdAt = now - 2 * 24 * HOUR),
        Ad("s1", Category.Sale, "Детская коляска", "Прогулочная, складная, отличное состояние. Самовывоз.", 1_200_000, district = District.North, author = "Ольга", contacts = listOf(Contact.telegram("@demo_olga")), createdAt = now - 8 * HOUR),
        Ad("s2", Category.Sale, "Сапборд с веслом", "Надувной, 320 см, насос и рюкзак в комплекте.", 4_500_000, district = District.Center, author = "Сергей", contacts = listOf(Contact.whatsApp("+84 900 000 306")), createdAt = now - 4 * 24 * HOUR),
        Ad("j1", Category.Jobs, "Ищем администратора в кафе", "Русский и английский, график 2/2, зарплата + чаевые.", 12_000_000, per = "в месяц", district = District.Center, author = "Кафе на Бьет Ты", contacts = listOf(Contact.telegram("@demo_jobs_nt")), createdAt = now - 26 * HOUR),
        Ad("j2", Category.Jobs, "Гид-переводчик на экскурсии", "Острова, Далат, Ян Бэй. Нужен опыт и свой байк.", null, district = District.Center, author = "Турагентство", contacts = listOf(Contact.whatsApp("+84 900 000 307")), createdAt = now - 6 * 24 * HOUR),
        Ad("v1", Category.Services, "Маникюр и педикюр на дому", "Выезд по центру и северу. Покрытие гель-лак.", 350_000, district = District.North, author = "Екатерина", contacts = listOf(Contact.telegram("@demo_nails_nt")), createdAt = now - 3 * HOUR),
        Ad("v2", Category.Services, "Ремонт кондиционеров", "Чистка, заправка, ремонт. Приезд в день обращения.", 250_000, per = "за чистку", district = District.South, author = "Мастер Тхань", contacts = listOf(Contact.zalo("+84 900 000 308"), Contact.phone("+84 900 000 308")), bonus = BonusTerms(earnFixed = 50), createdAt = now - 30 * HOUR),
        Ad("f1", Category.Free, "Отдам котёнка в добрые руки", "2 месяца, к лотку приучен.", null, district = District.CamRanh, author = "Анна", contacts = listOf(Contact.whatsApp("+84 900 000 309")), createdAt = now - 10 * HOUR),
    )
}
