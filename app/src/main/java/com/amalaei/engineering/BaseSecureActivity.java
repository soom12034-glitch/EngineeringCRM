package com.amalaei.engineering;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

public abstract class BaseSecureActivity extends Activity {
    protected boolean unlocked = true;
    protected boolean authenticating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.content.SharedPreferences security = getSharedPreferences("settings", 0);
        if (security.getBoolean("secureScreen", !BuildConfig.DEBUG)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        unlocked = !security.getBoolean("appLock", false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        android.content.SharedPreferences security = getSharedPreferences("settings", 0);
        if (security.getBoolean("appLock", false) && !unlocked && !authenticating) {
            authenticate();
        }
    }

    @Override
    protected void onStop() {
        if (!authenticating) {
            unlocked = false;
        }
        super.onStop();
    }

    protected void authenticate() {
        KeyguardManager manager = getSystemService(KeyguardManager.class);
        Intent intent = manager.createConfirmDeviceCredentialIntent("فتح " + AppMode.BRAND, "أكد هويتك للوصول إلى بيانات العملاء");
        if (intent == null) {
            getSharedPreferences("settings", 0).edit().putBoolean("appLock", false).apply();
            unlocked = true;
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
                unlocked = true;
            } else {
                finish();
            }
        }
    }
}
