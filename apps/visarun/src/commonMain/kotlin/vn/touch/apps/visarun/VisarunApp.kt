package vn.touch.apps.visarun

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
public data class VisarunConfig(val schemaVersion: Int = 1)

/** Visa runs from Nha Trang: border trips and flights, booked with the organiser; a stay calculator. */
public class VisarunApp : SuperizerApp<VisarunConfig>() {

    override val manifest: AppManifest = AppManifest(
        id = AppId(ID),
        version = "1.0.0",
        metadata = AppMetadata(
            title = localized("ru" to "Визараны", "en" to "Visa runs"),
            description = localized(
                "ru" to "Визараны из Нячанга и калькулятор срока пребывания",
                "en" to "Visa runs from Nha Trang and a stay calculator",
            ),
            icon = AppIcon.Path(
                "M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z",
            ),
            category = "Город",
        ),
    )

    override val configSpec: AppConfigSpec<VisarunConfig> = AppConfigSpec(VisarunConfig.serializer(), VisarunConfig())

    override fun launch(runtime: InstanceRuntime, config: VisarunConfig): AppInstance = VisarunInstance(runtime)

    public companion object {
        public const val ID: String = "visarun"
    }
}
