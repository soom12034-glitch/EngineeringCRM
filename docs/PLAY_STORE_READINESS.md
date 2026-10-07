# Google Play readiness

## Implemented

- Package: `com.soom12034.engineeringcrm`
- Target SDK 36; minimum SDK 29.
- Dedicated `play` flavor without `READ_CALL_LOG` or `CallScreeningService`.
- Dedicated `direct` flavor for controlled distribution outside Google Play.
- User-selected CSV call-history import in the Play flavor.
- No `INTERNET` permission, advertising SDK, analytics SDK, or tracking SDK.
- Android backup and device-transfer extraction disabled for CRM data.
- Optional device-credential app lock and secure-screen protection.
- AES-256-GCM password-encrypted user backups.
- In-app privacy disclosure and repository privacy-policy draft.

## Required before production submission

1. Choose and legally clear the final product name and brand assets.
2. Add the legal developer/company name, support email, and a permanent public privacy-policy URL.
3. Create and securely archive a private Play upload key; never publish an APK signed by the Android debug key.
4. Build a signed `playRelease` Android App Bundle (`.aab`) and enroll in Play App Signing.
5. Complete Play Console declarations: Data safety, permissions, ads, target audience, content rating, and app access.
6. Prepare store listing copy, phone screenshots, 512×512 icon, and 1024×500 feature graphic.
7. Run closed testing on representative Samsung, Xiaomi, Oppo, and Google devices before production rollout.

The Play build must not add `READ_CALL_LOG` unless the product becomes an eligible default Phone or Assistant handler and passes Google Play's restricted-permission review.
