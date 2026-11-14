package vn.touch.apps.food

import vn.touch.kit.BonusTerms
import vn.touch.kit.Contact

/**
 * Demo catalogue. The names and numbers are placeholders: a real portal loads this from its server
 * (or from runtime.network) — the screens do not care where the list came from.
 */
internal object Catalog {

    val restaurants: List<Restaurant> = listOf(
        Restaurant(
            id = "pho-hung",
            name = "Phở Hưng",
            cuisine = Cuisine.Vietnamese,
            address = "ул. Хунг Выонг, 12",
            hours = "06:00–22:00",
            deliveryFee = 15_000,
            minOrder = 50_000,
            etaMinutes = 25..35,
            bonus = BonusTerms(earnPercent = 5, spendPercent = 30),
            rating = 4.8,
            contacts = listOf(Contact.whatsApp("+84 900 000 101"), Contact.zalo("+84 900 000 101")),
            menu = listOf(
                Dish("pho-bo", "Фо бо", "Суп с говядиной и рисовой лапшой", 55_000),
                Dish("pho-ga", "Фо га", "Суп с курицей", 50_000),
                Dish("bun-cha", "Бун ча", "Свинина на гриле, лапша, зелень", 60_000),
                Dish("nem", "Нем ран", "Жареные спринг-роллы, 5 шт.", 45_000),
                Dish("tra-da", "Ча да", "Холодный зелёный чай", 10_000),
            ),
        ),
        Restaurant(
            id = "lac-canh",
            name = "Морской двор",
            cuisine = Cuisine.Seafood,
            address = "ул. Нгуен Бинь Кхием, 44",
            hours = "10:00–23:00",
            deliveryFee = 25_000,
            minOrder = 200_000,
            etaMinutes = 40..60,
            rating = 4.6,
            contacts = listOf(Contact.whatsApp("+84 900 000 102"), Contact.phone("+84 900 000 102")),
            menu = listOf(
                Dish("shrimp", "Креветки на гриле", "500 г, соус из соли, перца и лайма", 250_000),
                Dish("squid", "Кальмар в соусе", "Кальмар с овощами", 180_000),
                Dish("scallop", "Гребешки с луком", "6 шт., жареный лук и арахис", 120_000),
                Dish("fried-rice", "Рис с морепродуктами", "Жареный рис", 90_000),
            ),
        ),
        Restaurant(
            id = "pelmeni",
            name = "Пельменная «Сибирь»",
            cuisine = Cuisine.Russian,
            address = "ул. Бьет Ты, 21",
            hours = "09:00–23:00",
            deliveryFee = 20_000,
            minOrder = 100_000,
            etaMinutes = 30..45,
            rating = 4.7,
            contacts = listOf(Contact.telegram("@demo_pelmeni_nt"), Contact.whatsApp("+84 900 000 103")),
            menu = listOf(
                Dish("pelmeni", "Пельмени домашние", "20 шт., сметана", 120_000),
                Dish("borsch", "Борщ", "С пампушкой и салом", 95_000),
                Dish("olivier", "Оливье", "Классический", 70_000),
                Dish("syrniki", "Сырники", "Со сметаной и вареньем", 80_000),
                Dish("kvas", "Квас", "0,5 л", 40_000),
            ),
        ),
        Restaurant(
            id = "pizza-sea",
            name = "Pizza by the Sea",
            cuisine = Cuisine.Pizza,
            address = "ул. Чан Фу, 78",
            hours = "11:00–00:00",
            deliveryFee = 20_000,
            minOrder = 150_000,
            etaMinutes = 35..50,
            bonus = BonusTerms(earnPercent = 7, spendPercent = 30),
            rating = 4.5,
            contacts = listOf(Contact.whatsApp("+84 900 000 104"), Contact.telegram("@demo_pizza_nt")),
            menu = listOf(
                Dish("margherita", "Маргарита", "30 см", 160_000),
                Dish("pepperoni", "Пепперони", "30 см", 190_000),
                Dish("seafood-pizza", "С морепродуктами", "30 см, креветки, кальмар", 230_000),
                Dish("carbonara", "Паста карбонара", "", 150_000),
            ),
        ),
        Restaurant(
            id = "sakura",
            name = "Sakura Rolls",
            cuisine = Cuisine.Asian,
            address = "ул. Ле Тхань Тон, 5",
            hours = "10:00–22:00",
            deliveryFee = 20_000,
            minOrder = 150_000,
            etaMinutes = 40..55,
            bonus = BonusTerms(earnPercent = 5, spendPercent = 20),
            rating = 4.4,
            contacts = listOf(Contact.whatsApp("+84 900 000 105")),
            menu = listOf(
                Dish("philadelphia", "Филадельфия", "8 шт., лосось, сливочный сыр", 180_000),
                Dish("california", "Калифорния", "8 шт., краб, тобико", 150_000),
                Dish("set-big", "Сет «Нячанг»", "32 шт. на компанию", 520_000),
            ),
        ),
        Restaurant(
            id = "cafe-sua-da",
            name = "Cà Phê Sữa Đá",
            cuisine = Cuisine.Coffee,
            address = "ул. Нгуен Тхьен Тхуат, 30",
            hours = "06:30–21:00",
            deliveryFee = 10_000,
            minOrder = 40_000,
            etaMinutes = 15..25,
            bonus = BonusTerms(earnPercent = 10, spendPercent = 50),
            rating = 4.9,
            contacts = listOf(Contact.zalo("+84 900 000 106"), Contact.whatsApp("+84 900 000 106")),
            menu = listOf(
                Dish("sua-da", "Ка фе суа да", "Кофе со сгущёнкой и льдом", 30_000),
                Dish("coconut", "Кокосовый кофе", "", 45_000),
                Dish("egg", "Яичный кофе", "", 45_000),
                Dish("banh-flan", "Флан", "Карамельный пудинг", 25_000),
            ),
        ),
    )

    fun restaurant(id: String?): Restaurant? = restaurants.firstOrNull { it.id == id }
}
