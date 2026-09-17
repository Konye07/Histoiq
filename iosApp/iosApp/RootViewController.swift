import UIKit

/// Shared by GoogleSignInBridge and AdsBridge — both need a view controller to present native UI
/// (the Google account picker, a full-screen ad) from, and neither has one handed to them directly
/// since they're called from Kotlin, not from a SwiftUI view's own context.
enum RootViewController {
    static func current() -> UIViewController? {
        let base = UIApplication.shared.connectedScenes
            .compactMap { ($0 as? UIWindowScene)?.keyWindow }
            .first?.rootViewController
        return topMost(of: base)
    }

    private static func topMost(of base: UIViewController?) -> UIViewController? {
        if let nav = base as? UINavigationController {
            return topMost(of: nav.visibleViewController)
        }
        if let tab = base as? UITabBarController {
            return topMost(of: tab.selectedViewController)
        }
        if let presented = base?.presentedViewController {
            return topMost(of: presented)
        }
        return base
    }
}
