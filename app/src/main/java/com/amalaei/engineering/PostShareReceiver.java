package com.amalaei.engineering;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class PostShareReceiver extends BroadcastReceiver {
    static /* synthetic */ void lambda$onReceive$0(Context context, BroadcastReceiver.PendingResult pendingResult) {
        try {
            PostSharing.deliver(context);
            PostSharing.schedule(context);
        } catch (Exception unused) {
            context.getSharedPreferences("post-sharing", 0).edit().putString("error", "Unable to schedule sharing reminders").apply();
        } finally {
            pendingResult.finish();
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final BroadcastReceiver.PendingResult pendingResultGoAsync = goAsync();
        new Thread(new Runnable() {
            @Override
            public final void run() {
                PostShareReceiver.lambda$onReceive$0(context, pendingResultGoAsync);
            }
        }, "post-sharing-reminders").start();
    }
}
