# Touch VN — городской портал Нячанга

Супер-апп в духе Grab, но про городские услуги: хост на [Superizer](../superizer), устроенный так же, как
[Unitool](../unitool). Один `commonMain` — Android, десктоп (JVM) и веб (Wasm).

| Сервис | Что умеет |
|---|---|
| **Еда** | рестораны с фильтром по кухне, меню, корзина (одна кухня на заказ, минимальная сумма), оформление; заказ уходит ресторану готовым сообщением в WhatsApp / Telegram (у Zalo и звонка — без текста) |
| **Цветы** | букеты по категориям, заказ «кому / куда / когда / открытка / от кого» — сообщение флористу |
| **Объявления** | частные объявления: аренда, байки, продам, работа, услуги; поиск и фильтры; подача своего объявления (хранится на устройстве со статусом «на модерации») |
| **Визараны** | Камбоджа / Лаос / самолётом / помощь с e-visa, заявка организатору; калькулятор срока пребывания (безвиз 45 / 30 дней, e-visa 90; день въезда — первый день; «сегодня» по времени Вьетнама) |
| **Обмен валют** | USD / EUR / RUB / USDT: точки обмена с лучшим курсом сверху, сколько донгов выйдет за вашу сумму, связаться с обменником |

**Городская бонусная система** — один бонусный счёт на все сервисы, как монеты в Trip.com: заказ у
партнёра приносит бонусы, а бонусами можно оплатить часть следующего заказа в любом сервисе (1 бонус =
100 ₫). Участвуют не все: у партнёра на карточке значок «+N бонусов», у остальных — «Не участвует в
городской бонусной системе». Баланс — строкой на Home и вверху каждого сервиса; одно нажатие открывает
экран бонусов (баланс, как это работает, где получить и потратить, история) прямо поверх текущей страницы.
При оформлении — переключатель «Оплатить бонусами»; скидка попадает в сообщение продавцу. Первый запуск
дарит 300 бонусов.

| Сервис | Получить | Потратить |
|---|---|---|
| Еда | 5–10% с заказа (4 ресторана из 6) | до 20–50% заказа |
| Цветы | 5–10% (2 магазина из 3) | до 20–30% букета |
| Визараны | +150–300 за поездку, 5% за e-visa | до 20–30% туров в донгах |
| Объявления | +50–100 за обращение к бизнесам со значком, один раз на объявление | — |
| Обмен валют | +50–100 у пунктов-партнёров, раз в день на пункт | — |

Каталоги — **демо-данные** (вымышленные заведения, номера `+84 900 000 xxx`). Сервера нет: заказы
и заявки уходят продавцу сообщением в мессенджер.

## Запуск

```bash
./gradlew :composeApp:run                              # десктоп, окно 420×820
./gradlew :composeApp:installDebug                     # Android
./gradlew :composeApp:wasmJsBrowserDevelopmentRun      # веб, http://localhost:8083/
./gradlew desktopTest checkDependencyRules             # все тесты и правило границ
```

Deep link: `touchvn://app/<id>` (`food`, `flowers`, `ads`, `visarun`, `exchange`):
`./gradlew :composeApp:run --args="--link=touchvn://app/exchange"`, в вебе `?link=touchvn%3A%2F%2Fapp%2Ffood`.

Superizer 0.10.0 без чекаута рядом берётся из `~/.m2` (`./gradlew publishToMavenLocal` в `../superizer`).
Если рядом лежит чекаут `../superizer`, его исходники подставляются вместо пакета (композитная сборка, как
в Unitool; так собирает CI). `SUPERIZER_FROM_MAVEN=1` — взять Superizer из `~/.m2`, даже когда чекаут есть.

## iPhone

`iosApp/` — Xcode-проект, `composeApp/src/iosMain` — хост на iOS. Без Mac собираются только klib-и
(`./gradlew :composeApp:compileKotlinIosArm64`). `.github/workflows/ios.yml` (macos-26, линии как у Unitool):

- `testflight` — push в `main` (кроме правок только `*.md` и `e2e/`), запуск вручную на `main` или метка
  `testflight` на PR в `main`: Release подписывается, уходит в App Store Connect, затем `distribute`
  ждёт обработки и отдаёт сборку группе TestFlight `dev`. Номер сборки — номер запуска.
- `device` — push в ветку `ios-device` или вручную не на `main`: Release для iPhone **без подписи** —
  артефакты `ios-device-unsigned-app` (`TouchVN.app`) и `TouchVN-<run>.ipa`.
  `gh run download <run> -R xziy/touch-vn -n TouchVN-<run>.ipa`
- `simulator` — push в `ios-simulator`: Debug для симулятора на Apple silicon.

Подписать и поставить на подключённый iPhone (на Mac с сертификатом):
`IOS_SIGN_IDENTITY=<SHA-1> IOS_PROFILE=<profile>.mobileprovision scripts/ios-sign-and-install.sh TouchVN.app`.

### TestFlight

| | |
|---|---|
| Команда Apple | личная **Ivan Sizyi**, `ZK66E83UL5` (та же, что у RestoApp touch) |
| Bundle ID | `vn.touch.portal`, как у Android; Push не включён |
| App Store Connect | «Touch VN», Apple ID 6835445451, SKU `touch-vn`, основной язык — русский |
| Группа | внутренняя `dev`: получает каждую сборку сама, отзывы включены; другая — переменная `TESTFLIGHT_GROUP` |

Секреты репозитория (как у Unitool): `IOS_DIST_CERT_P12_BASE64`, `IOS_DIST_CERT_PASSWORD`,
`IOS_PROFILE_BASE64`, `ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_KEY_P8_BASE64`. Нет хотя бы одного — линия
`testflight` пишет, каких нет, и завершается зелёной. Подпись ручная: `scripts/ios-ci-signing.py`
сверяет профиль с сертификатом и приложением и пишет `Signing.xcconfig` на один запуск.

Ключи лежат на guibot2 в `~/Documents/apple-ivan-sizyi/` (только у владельца), в git их нет:
сертификат Apple Distribution (до 16.11.2027, общий с RestoApp touch) — `ios_distribution.p12` и пароль
к нему, профиль `Touch_VN_App_Store.mobileprovision` (до 16.11.2027), ключ API `AuthKey_5FBFF968UV.p8`
(роль App Manager). Когда сертификат продлён, профиль выпускается заново и оба секрета обновляются:
`base64 -w0 <файл> | gh secret set <ИМЯ> -R xziy/touch-vn`.

Шифрование: своего у приложения нет, поэтому в `Info.plist` стоит `ITSAppUsesNonExemptEncryption = NO`, и
сборка не ждёт ответа в App Store Connect. Если однажды понадобится YES, линия сама впишет его вместе
с ключом документации, когда Apple такой ключ выдаст.

Иконка — `iosApp/AppIcon.svg`, в каталоге `Assets.xcassets` лежит её PNG 1024×1024 без прозрачности.
Без иконки App Store Connect сборку не принимает.

## E2E-тесты на wasm-сборке (Playwright)

```bash
./e2e/run.sh                      # собрать wasm и прогнать все тесты
./e2e/run.sh tests/food.spec.ts   # один файл; остальные аргументы уходят в playwright
cd e2e && npx playwright show-report
```

Compose рисует в canvas, поэтому хост включает веб-доступность (`isA11YEnabled` в `wasmJsMain/main.kt`):
каждый узел семантики становится ARIA-элементом в DOM с настоящими координатами, а `testTag` становится его
`id`. Тесты ищут элементы по тем же тегам, что и JVM-тесты (`food:dish-pho-bo-plus`), прокручивают
колесом мыши и кликают мышью по месту на canvas, текст вводят с клавиатуры. `window.open` перехватывается:
тест читает ссылку WhatsApp/Telegram/Zalo, которую сформировал заказ. Помощники лежат в `e2e/tests/app.ts`.

Браузер берётся системный (`/usr/bin/chromium` или `CHROME_BIN`) с программным WebGL (SwiftShader), так что
GPU и скачивание браузеров не нужны.

## Дизайн

Интерфейс нарисован на `ui-kit` Superizer 0.10.0 (shadcn/ui для Compose, иконки Lucide) и на своей
дизайн-системе портала в `:kit` (`vn.touch.kit`): светлая и тёмная тема берутся из настройки «Оформление»
хоста, в приложениях нет ни одного hex-цвета.

| | |
|---|---|
| `Theme.kt` | `Touch.colors` (серый холст и белые карточки, золото бонусов), `Touch.type` (шкала от `display` до `micro`), `Touch.accent` — цвет сервиса; `PortalTheme(tone)` перекрашивает им `primary` kit-а |
| Тона | `Lagoon` — бренд (море Нячанга): Home, бонусы; `Chili` — Еда, `Rose` — Цветы, `Violet` — Объявления, `Sky` — Визараны, `Jade` — Обмен валют |
| `Widgets.kt` | `ServiceHeader` (градиентная шапка с поиском на краю), карточки `ListRow` / `FeatureCard`, `CategoryRow`, `BarButton`, `Stepper`, `DoneState`… |
| `BonusWidgets.kt` | кошелёк городских бонусов (`WalletCard`), монета `CoinMark`, бонусные строки оформления |
| `Glyphs.kt` | `Glyph` — иконки Lucide по смыслу, плюс сплошные монета и звезда |

Скриншоты (светлые, часть — тёмные): `./gradlew :composeApp:desktopTest --tests '*Screenshots*'` →
`composeApp/build/screenshots/`; `SHOTS=<regex>` — только нужные, `SHOTS_DARK=1` — все кадры и в тёмной теме.

## Устройство

```
composeApp/   хост: TouchHost.kt (имя, register(...) пяти сервисов, порядок на Home) + точки входа
kit/          общее для сервисов: стек страниц (PortalInstance), карточки, чипы-фильтры,
              кнопки WhatsApp/Telegram/Zalo/телефон, формат ₫; городские бонусы — Bonus.kt
              (условия партнёра BonusTerms, счёт CityBonus) и BonusWidgets.kt (значки, экран)
apps/food  apps/flowers  apps/ads  apps/visarun  apps/exchange
              по приложению на сервис: App (манифест), Instance (состояние), Screen, Model, Catalog
```

Дизайн — коралловый стиль приложений доставки, весь в `kit`: `PortalTheme` (токены Superizer,
перекрашенные в коралловый; им обёрнут `Content()` каждого сервиса), `Glyphs.kt` (векторные
иконки-иллюстрации вместо фото) и `Widgets.kt` (поиск, ряд категорий, карусель, баннер, строки
с картинкой, закреплённая нижняя кнопка, экраны «Спасибо» и «Пусто»). Верхнюю панель рисует сам
Superizer в своей монохромной теме. Home — свой, в жёлтом стиле (`composeApp/.../Home.kt`): приветствие
по времени Нячанга, баланс бонусов, поиск, сервисы рядом «пилюль», «Популярное». Слота для замены Home
у Superizer нет, поэтому он нарисован в `homeBanner` на всю высоту, а сетка плиток библиотеки под ним
получает нулевую высоту. Набор и порядок сервисов — те же, что у человека на Home.

Правила как в Unitool: приложение видит только контракт Superizer, его виджеты и `:kit` — не
`superizer:host`, не хост и не другие приложения (`./gradlew checkDependencyRules`).

### Новый сервис (такси, аренда байков, экскурсии…)

1. `apps/bikes/` — `build.gradle.kts` скопировать из `apps/flowers`, поменять `namespace`.
2. `BikesApp : SuperizerApp<BikesConfig>()` + `BikesInstance : PortalInstance<Page>` + экран на виджетах `kit`.
3. `include(":apps:bikes")` в `settings.gradle.kts`, зависимость в `composeApp/build.gradle.kts`,
   `register(BikesApp())` и id в `home(...)` в `TouchHost.kt`.

Бонусный счёт — сервис хоста (`CityBonus.Key`): приложение не может читать чужое хранилище, а баланс общий.
Хост хранит его под своим именем (`city-bonus`) и регистрирует в `TouchHost.kt` вместе со строкой на Home и
списком «где получить и потратить». Приложение берёт его через `runtime.service(CityBonus.Key)`; без него
(другой хост, юнит-тест) бонусы просто не показываются. Условия партнёра — поле `bonus: BonusTerms?` в каталоге.

## Что дальше

- Бонусы на сервере: сейчас они на устройстве и начисляются сразу после отправки заказа, в рабочей
  версии — после подтверждения партнёром.

- Сервер: каталоги, курсы и объявления по сети (`runtime.network`) вместо `Catalog.kt`, модерация объявлений.
- Английский (и вьетнамский) — строки сейчас только на русском, поэтому хост объявляет один язык.
- Иконка приложения для Android (сейчас системная по умолчанию); у iOS она есть — `iosApp/AppIcon.svg`.
