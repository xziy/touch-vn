package vn.touch.apps.food

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
public data class FoodConfig(val schemaVersion: Int = 1)

/**
 * Food delivery in Nha Trang: restaurants, their menus, one basket, and an order that goes to the
 * restaurant's own WhatsApp or Telegram as a ready-written message — no server in between.
 */
public class FoodApp : SuperizerApp<FoodConfig>() {

    override val manifest: AppManifest = AppManifest(
        id = AppId(ID),
        version = "1.0.0",
        metadata = AppMetadata(
            title = localized("ru" to "Еда", "en" to "Food"),
            description = localized(
                "ru" to "Доставка еды из ресторанов Нячанга",
                "en" to "Food delivery from Nha Trang restaurants",
            ),
            // Fork and knife.
            icon = AppIcon.Path(
                "M11 9H9V2H7v7H5V2H3v7c0 2.12 1.66 3.84 3.75 3.97V22h2.5v-9.03C11.34 12.84 13 11.12 13 9V2h-2v7z" +
                    "M16 6v8h2.5v8H21V2c-2.76 0-5 2.24-5 4z",
            ),
            category = "Доставка",
        ),
    )

    override val configSpec: AppConfigSpec<FoodConfig> = AppConfigSpec(FoodConfig.serializer(), FoodConfig())

    override fun launch(runtime: InstanceRuntime, config: FoodConfig): AppInstance = FoodInstance(runtime)

    public companion object {
        public const val ID: String = "food"
    }
}
