package com.amalaei.engineering;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

public abstract class BaseSecureActivity extends Activity {
    // The lock covers the whole app: moving between its screens never drops below one started activity.
    private static boolean sessionUnlocked = false;
    private static int startedActivities = 0;
    private boolean authenticating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSharedPreferences("settings", 0).getBoolean("secureScreen", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startedActivities++;
        if (!isUnlocked() && !authenticating) {
            authenticate();
        }
    }

    @Override
    protected void onStop() {
        startedActivities--;
        if (startedActivities == 0 && !isChangingConfigurations()) {
            sessionUnlocked = false;
        }
        super.onStop();
    }

    protected final boolean isUnlocked() {
        return sessionUnlocked || !getSharedPreferences("settings", 0).getBoolean("appLock", false);
    }

    protected final void markUnlocked() {
        sessionUnlocked = true;
    }

    private void authenticate() {
        KeyguardManager manager = getSystemService(KeyguardManager.class);
        Intent intent = manager.createConfirmDeviceCredentialIntent(AppLanguage.text(this, "فتح " + AppMode.BRAND, "Unlock " + AppMode.BRAND), AppLanguage.text(this, "أكد هويتك للوصول إلى بيانات العملاء", "Confirm your identity to access customer data"));
        if (intent == null) {
            getSharedPreferences("settings", 0).edit().putBoolean("appLock", false).apply();
            markUnlocked();
            return;
        }
        authenticating = true;
        startActivityForResult(intent, 40);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 40) {
            authenticating = false;
            if (resultCode == RESULT_OK) {
                markUnlocked();
            } else {
                finish();
            }
        }
    }
}
