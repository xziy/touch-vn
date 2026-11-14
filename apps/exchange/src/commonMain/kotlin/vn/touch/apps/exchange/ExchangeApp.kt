package vn.touch.apps.exchange

import cx.m42.superizer.app.AppConfigSpec
import cx.m42.superizer.app.AppIcon
import cx.m42.superizer.app.AppId
import cx.m42.superizer.app.AppInstance
import cx.m42.superizer.app.AppManifest
import cx.m42.superizer.app.AppMetadata
import cx.m42.superizer.app.SuperizerApp
import cx.m42.superizer.app.localized
import cx.m42.superizer.runtime.InstanceRuntime
import kotlinx.serialization.Serializable

@Serializable
public data class ExchangeConfig(val schemaVersion: Int = 1)

/** Where to change money in Nha Trang: banks, gold shops and private exchangers, best rate first. */
public class ExchangeApp : SuperizerApp<ExchangeConfig>() {

    override val manifest: AppManifest = AppManifest(
        id = AppId(ID),
        version = "1.0.0",
        metadata = AppMetadata(
            title = localized("ru" to "Обмен валют", "en" to "Exchange"),
            description = localized(
                "ru" to "Курсы обменников Нячанга: доллары, евро, рубли, USDT",
                "en" to "Nha Trang exchange rates: USD, EUR, RUB, USDT",
            ),
            icon = AppIcon.Named("currency"),
            category = "Город",
        ),
    )

    override val configSpec: AppConfigSpec<ExchangeConfig> = AppConfigSpec(ExchangeConfig.serializer(), ExchangeConfig())

    override fun launch(runtime: InstanceRuntime, config: ExchangeConfig): AppInstance = ExchangeInstance(runtime)

    public companion object {
        public const val ID: String = "exchange"
    }
}
