package vn.touch.apps.flowers

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
public data class FlowersConfig(val schemaVersion: Int = 1)

/** Flower delivery: bouquets from Nha Trang florists, ordered for someone else, with a card. */
public class FlowersApp : SuperizerApp<FlowersConfig>() {

    override val manifest: AppManifest = AppManifest(
        id = AppId(ID),
        version = "1.0.0",
        metadata = AppMetadata(
            title = localized("ru" to "Цветы", "en" to "Flowers"),
            description = localized(
                "ru" to "Букеты с доставкой по Нячангу",
                "en" to "Bouquets delivered across Nha Trang",
            ),
            icon = AppIcon.Path(
                "M12 22c4.97 0 9-4.03 9-9-4.97 0-9 4.03-9 9z" +
                    "M5.6 10.25c0 1.38 1.12 2.5 2.5 2.5.53 0 1.01-.16 1.42-.44l-.02.19c0 1.38 1.12 2.5 2.5 2.5s2.5-1.12 2.5-2.5l-.02-.19c.4.28.89.44 1.42.44 1.38 0 2.5-1.12 2.5-2.5 0-1-.59-1.85-1.43-2.25.84-.4 1.43-1.25 1.43-2.25 0-1.38-1.12-2.5-2.5-2.5-.53 0-1.01.16-1.42.44l.02-.19C14.5 2.12 13.38 1 12 1S9.5 2.12 9.5 3.5l.02.19c-.4-.28-.89-.44-1.42-.44-1.38 0-2.5 1.12-2.5 2.5 0 1 .59 1.85 1.43 2.25-.84.4-1.43 1.25-1.43 2.25z" +
                    "M12 5.5c1.38 0 2.5 1.12 2.5 2.5s-1.12 2.5-2.5 2.5S9.5 9.38 9.5 8s1.12-2.5 2.5-2.5z" +
                    "M3 13c0 4.97 4.03 9 9 9 0-4.97-4.03-9-9-9z",
            ),
            category = "Доставка",
        ),
    )

    override val configSpec: AppConfigSpec<FlowersConfig> = AppConfigSpec(FlowersConfig.serializer(), FlowersConfig())

    override fun launch(runtime: InstanceRuntime, config: FlowersConfig): AppInstance = FlowersInstance(runtime)

    public companion object {
        public const val ID: String = "flowers"
    }
}
