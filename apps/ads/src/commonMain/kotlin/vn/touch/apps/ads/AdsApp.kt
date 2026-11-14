package vn.touch.apps.ads

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
public data class AdsConfig(val schemaVersion: Int = 1)

/** Private classifieds of Nha Trang: rent, bikes, things, jobs, services. */
public class AdsApp : SuperizerApp<AdsConfig>() {

    override val manifest: AppManifest = AppManifest(
        id = AppId(ID),
        version = "1.0.0",
        metadata = AppMetadata(
            title = localized("ru" to "Объявления", "en" to "Classifieds"),
            description = localized(
                "ru" to "Частные объявления: аренда, байки, продажа, работа",
                "en" to "Private ads: rent, bikes, for sale, jobs",
            ),
            // A speech bubble with an exclamation mark.
            icon = AppIcon.Path(
                "M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" +
                    "M13 11h-2V5h2v6zm0 4h-2v-2h2v2z",
            ),
            category = "Город",
        ),
    )

    override val configSpec: AppConfigSpec<AdsConfig> = AppConfigSpec(AdsConfig.serializer(), AdsConfig())

    override fun launch(runtime: InstanceRuntime, config: AdsConfig): AppInstance = AdsInstance(runtime)

    public companion object {
        public const val ID: String = "ads"
    }
}
