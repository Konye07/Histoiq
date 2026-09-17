import ComposeApp
import GoogleMobileAds
import GoogleSignIn
import SwiftUI

@main
struct iOSApp: App {
    init() {
        IosNativeBridges.shared.googleSignIn = GoogleSignInBridgeImpl()
        IosNativeBridges.shared.ads = AdsBridgeImpl()
        GADMobileAds.sharedInstance().start(completionHandler: nil)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
