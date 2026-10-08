# Google Play release checklist

## Build and signing

- Publish only the `play` flavor. It excludes `READ_CALL_LOG` and the call-screening service.
- Keep the package name `com.soom12034.engineeringcrm` and increment `versionCode` for every upload.
- Create and securely retain an upload keystore. Never commit the keystore or passwords. Keep at least two offline copies of the key and recovery details.
- Set `ANDROID_UPLOAD_STORE_FILE`, `ANDROID_UPLOAD_STORE_PASSWORD`, `ANDROID_UPLOAD_KEY_ALIAS`, and `ANDROID_UPLOAD_KEY_PASSWORD`, then run `./gradlew bundlePlayRelease`.
- Verify the signed result at `app/build/outputs/bundle/playRelease/app-play-release.aab` before uploading.

## Privacy and Play Console forms

- Publish `docs/privacy-policy.html` with GitHub Pages from the `/docs` directory.
- Confirm the public URL works without login: `https://soom12034-glitch.github.io/EngineeringCRM/privacy-policy.html`.
- Confirm the support email in the policy and Play Console is correct.
- Complete Data safety consistently: the Play flavor performs local/on-device processing and has no `INTERNET`, advertising, analytics, or tracking SDK permission. Disclose Contacts access and user-controlled sharing accurately.
- In the Permissions section, explain that Contacts is optional and used to match locally stored customer numbers with address-book names.
- Complete App access (no login), Ads (none), Content rating, Target audience, News, and Data deletion/account sections accurately.

## Store listing

- App name: `تواصل`.
- Provide a 512×512 PNG app icon, a 1024×500 feature graphic, at least two current phone screenshots without real customer data, a short description, and a full description.
- Do not claim that the Google Play edition reads the phone call log. Describe CSV/VCF import instead.
- Supply a support email, category, developer contact details, developer website (`https://enginex2030.com/?lang=ar`), and privacy-policy URL.

## Testing and rollout

- Upload first to Internal testing and review the automated pre-launch report.
- Test the final Play-signed build on small/large screens, RTL Arabic, increased font size, Android 10 through Android 16, notifications denied/allowed, Contacts denied/allowed, backup/restore, PDF generation, and process restart.
- If the developer account is a personal account created after November 13, 2023, complete the required closed test (currently at least 12 opted-in testers for 14 continuous days) before applying for production access; confirm the current requirement in Play Console.
- Use staged production rollout and monitor Android vitals.
