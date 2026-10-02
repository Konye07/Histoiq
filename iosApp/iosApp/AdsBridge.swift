import ComposeApp
import Foundation
import GoogleMobileAds

/// TestFlight and App Review installs carry a "sandboxReceipt" where the App Store ships "receipt".
/// Testers must never see or tap live ads, so those installs get Google's public test units in place
/// of the configured ones. The simulator keeps the configured units: the Mobile Ads SDK already
/// treats simulators as test devices, so real unit IDs can be exercised there safely.
enum AdEnvironment {
    private static let usesTestUnits: Bool = {
        #if targetEnvironment(simulator)
        return false
        #else
        return Bundle.main.appStoreReceiptURL?.lastPathComponent == "sandboxReceipt"
        #endif
    }()

    static func interstitialUnit(_ configured: String) -> String {
        usesTestUnits ? "ca-app-pub-3940256099942544/4411468910" : configured
    }

    static func rewardedUnit(_ configured: String) -> String {
        usesTestUnits ? "ca-app-pub-3940256099942544/1712485313" : configured
    }
}

/// Real AdMob interstitial/rewarded ads on iOS. Registered once at launch (see iOSApp.swift) as
/// IosNativeBridges.shared.ads — see Core/Platform/IosNativeBridges.kt for why this SDK call has
/// to live in Swift rather than being called directly from Kotlin.
class AdsBridgeImpl: NSObject, AdsBridge {
    private var interstitial: InterstitialAd?
    private var rewarded: RewardedAd?
    private var onInterstitialDismissed: (() -> Void)?

    func showInterstitial(adUnitId: String, onDismissed: @escaping () -> Void) {
        InterstitialAd.load(with: AdEnvironment.interstitialUnit(adUnitId), request: Request()) { [weak self] ad, error in
            guard let self = self, let ad = ad, error == nil, let presenter = RootViewController.current() else {
                onDismissed()
                return
            }
            self.interstitial = ad
            self.onInterstitialDismissed = onDismissed
            ad.fullScreenContentDelegate = self
            ad.present(from: presenter)
        }
    }

    func showRewarded(adUnitId: String, onRewarded: @escaping () -> Void) {
        RewardedAd.load(with: AdEnvironment.rewardedUnit(adUnitId), request: Request()) { [weak self] ad, error in
            guard let self = self, let ad = ad, error == nil, let presenter = RootViewController.current() else {
                return
            }
            self.rewarded = ad
            ad.fullScreenContentDelegate = self
            ad.present(from: presenter) {
                onRewarded()
            }
        }
    }
}

extension AdsBridgeImpl: FullScreenContentDelegate {
    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        // Only the interstitial needs an explicit dismissal callback — the rewarded ad's own
        // present(from:userDidEarnRewardHandler:) already tells the caller whether the reward was
        // earned; nothing else needs to happen when it closes.
        if ad === interstitial {
            onInterstitialDismissed?()
            onInterstitialDismissed = nil
            interstitial = nil
        } else if ad === rewarded {
            rewarded = nil
        }
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        if ad === interstitial {
            onInterstitialDismissed?()
            onInterstitialDismissed = nil
            interstitial = nil
        } else if ad === rewarded {
            rewarded = nil
        }
    }
}
