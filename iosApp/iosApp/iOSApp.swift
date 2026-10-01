import ComposeApp
import GoogleMobileAds
import GoogleSignIn
import SwiftUI

@main
struct iOSApp: App {
    init() {
        IosNativeBridges.shared.googleSignIn = GoogleSignInBridgeImpl()
        IosNativeBridges.shared.ads = AdsBridgeImpl()
        MobileAds.shared.start(completionHandler: nil)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                    // "Continue with Facebook" (Core/Auth/FacebookSignIn.ios.kt) OAuth-redirect
                    // callback — a URL that doesn't match the configured deep-link scheme/host is
                    // a no-op, so it's safe to always forward every incoming URL here.
                    FacebookOAuthBridge.shared.handleDeeplink(url: url)
                }
        }
    }
}
