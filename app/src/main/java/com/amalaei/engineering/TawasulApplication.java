package com.amalaei.engineering;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public final class TawasulApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                AppLanguage.apply(activity);
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                VisualPolish.remove(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                VisualPolish.watch(activity);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }
        });
    }
}
