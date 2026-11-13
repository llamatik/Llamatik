import SwiftUI
import shared

@main
struct iOSApp: App {

    init() {
        HelperKt.doInitKoin()
        ReviewEntryPoint.shared.notifyAppLaunched()
    }

    var body: some Scene {
        WindowGroup {
            // Compose draws edge to edge and applies the safe-area / keyboard insets itself
            // (status bar, home indicator). If SwiftUI also insets the view, the theme stops short of
            // the screen edges and Compose sees zero insets, which skews the bars' padding.
            ContentView()
                .ignoresSafeArea(.all)
        }
    }
}
