import ComposeApp
import GoogleSignIn

/// Real "Continue with Google" on iOS. Registered once at launch (see iOSApp.swift) as
/// IosNativeBridges.shared.googleSignIn — see Core/Platform/IosNativeBridges.kt for why this SDK
/// call has to live in Swift rather than being called directly from Kotlin.
class GoogleSignInBridgeImpl: GoogleSignInBridge {
    func signIn(hashedNonce: String, onResult: @escaping (String?, String?, String?, String?, String?, String?) -> Void) {
        guard let presenter = RootViewController.current() else {
            onResult(nil, nil, nil, nil, nil, nil)
            return
        }
        GIDSignIn.sharedInstance.signIn(
            withPresenting: presenter,
            hint: nil,
            additionalScopes: nil,
            nonce: hashedNonce
        ) { result, error in
            guard let user = result?.user, error == nil else {
                onResult(nil, nil, nil, nil, nil, nil)
                return
            }
            let avatarUrl = (user.profile?.hasImage == true)
                ? user.profile?.imageURL(withDimension: 200)?.absoluteString
                : nil
            onResult(
                user.idToken?.tokenString,
                user.accessToken.tokenString,
                user.userID,
                user.profile?.email,
                user.profile?.name,
                avatarUrl
            )
        }
    }
}
