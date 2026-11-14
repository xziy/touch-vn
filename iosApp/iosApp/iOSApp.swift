import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                // touchvn://app/<id>, as --link= on the desktop and ?link= on the web.
                .onOpenURL { url in MainViewControllerKt.deliverLink(url: url.absoluteString) }
        }
    }
}
