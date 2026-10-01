import ComposeApp
import UserMessagingPlatform
import AppTrackingTransparency
import GoogleMobileAds

/// EEA/UK ad-consent (Google UMP) + App Tracking Transparency on iOS. Registered once at launch
/// (see iOSApp.swift) as IosNativeBridges.shared.consent — see Core/Platform/IosNativeBridges.kt
/// for why this SDK call has to live in Swift rather than being called directly from Kotlin.
class ConsentBridgeImpl: NSObject, ConsentBridge {
    func requestConsentAndInitializeAds(onComplete: @escaping () -> Void) {
        ConsentInformation.shared.requestConsentInfoUpdate(with: RequestParameters()) { [weak self] error in
            guard let self = self else { return }
            if error != nil {
                DispatchQueue.main.async { self.finishConsentFlow(onComplete: onComplete) }
                return
            }
            guard let presenter = RootViewController.current() else {
                DispatchQueue.main.async { self.finishConsentFlow(onComplete: onComplete) }
                return
            }
            ConsentForm.loadAndPresentIfRequired(from: presenter) { [weak self] _ in
                self?.finishConsentFlow(onComplete: onComplete)
            }
        }
    }

    // Always initializes ads regardless of the consent outcome — Google's EU User Consent Policy
    // still requires serving non-personalized ads to users who decline. Only the ATT prompt itself
    // is gated on canRequestAds, to avoid a redundant/contradictory-feeling second prompt for a user
    // who just declined ad personalization.
    private func finishConsentFlow(onComplete: @escaping () -> Void) {
        func startAdsAndComplete() {
            MobileAds.shared.start(completionHandler: nil)
            onComplete()
        }

        guard ConsentInformation.shared.canRequestAds else {
            startAdsAndComplete()
            return
        }
        ATTrackingManager.requestTrackingAuthorization { _ in
            DispatchQueue.main.async { startAdsAndComplete() }
        }
    }

    func showPrivacyOptionsForm(onComplete: @escaping () -> Void) {
        guard let presenter = RootViewController.current() else {
            onComplete()
            return
        }
        ConsentForm.presentPrivacyOptionsForm(from: presenter) { _ in
            onComplete()
        }
    }

    func isPrivacyOptionsRequired() -> Bool {
        ConsentInformation.shared.privacyOptionsRequirementStatus == .required
    }
}
