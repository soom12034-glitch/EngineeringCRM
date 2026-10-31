package com.amalaei.engineering;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class PostSharing {
    static final String BUSINESS = "com.whatsapp.w4b";
    static final String FACEBOOK = "com.facebook.katana";

    PostSharing() {
    }

    static PendingIntent alarm(Context context) {
        return PendingIntent.getBroadcast(context, 0, new Intent(context, (Class<?>) PostShareReceiver.class).setAction("post-sharing.remind"), 201326592);
    }

    static boolean allowed(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        channel(context);
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel("post-sharing");
        if (!notificationManager.areNotificationsEnabled() || notificationChannel.getImportance() == 0) {
            return false;
        }
        return Build.VERSION.SDK_INT < 33 || context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == 0;
    }

    static void channel(Context context) {
        ((NotificationManager) context.getSystemService(NotificationManager.class)).createNotificationChannel(new NotificationChannel("post-sharing", AppLanguage.text(context, "تذكيرات مشاركة المنشورات", "Post sharing reminders"), 3));
    }

    static synchronized void deliver(Context context) throws JSONException {
        if (allowed(context)) {
            JSONArray jSONArrayLoad = load(context);
            Store store = new Store(context);
            for (int i = 0; i < jSONArrayLoad.length(); i++) {
                try {
                    JSONObject jSONObject = jSONArrayLoad.getJSONObject(i);
                    if (jSONObject.optBoolean("active")) {
                        JSONObject jSONObjectPost = store.post(jSONObject.optLong("post"));
                        if (jSONObjectPost.optLong("id") == 0) {
                            jSONObject.put("active", false);
                        } else if (!jSONObject.optBoolean("notified") && jSONObject.optLong("next") <= System.currentTimeMillis()) {
                            ((NotificationManager) context.getSystemService(NotificationManager.class)).notify(jSONObject.optString("id"), 0, new Notification.Builder(context, "post-sharing").setSmallIcon(R.drawable.ic_notification).setContentTitle(jSONObjectPost.optString("title")).setContentText(AppLanguage.text(context, "موعد مشاركة يدوية على ", "Manual sharing reminder: ") + label(jSONObject.optString("target"))).setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, (Class<?>) PostSharingActivity.class).putExtra("postId", jSONObject.optLong("post")).setAction(jSONObject.optString("id")), 201326592)).setAutoCancel(true).setVisibility(0).build());
                            jSONObject.put("notified", true);
                        }
                    }
                } catch (Throwable th) {
                    try {
                        store.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            }
            store.close();
            save(context, jSONArrayLoad);
            schedule(context);
        }
    }

    static synchronized void finish(Context context, String str, boolean z) throws JSONException {
        JSONArray jSONArrayLoad = load(context);
        for (int i = 0; i < jSONArrayLoad.length(); i++) {
            JSONObject jSONObject = jSONArrayLoad.getJSONObject(i);
            if (str.equals(jSONObject.optString("id")) && jSONObject.optBoolean("active") && jSONObject.optLong("next") <= System.currentTimeMillis()) {
                record(context, jSONObject.optLong("post"), jSONObject.optString("target"), z ? "confirmed" : "skipped");
                PostSharingRules.advance(jSONObject, System.currentTimeMillis());
            }
        }
        save(context, jSONArrayLoad);
        ((NotificationManager) context.getSystemService(NotificationManager.class)).cancel(str, 0);
        schedule(context);
    }

    static String label(String str) {
        if (BUSINESS.equals(str)) {
            return "WhatsApp Business";
        }
        return FACEBOOK.equals(str) ? "Facebook" : "Other apps";
    }

    static JSONArray load(Context context) {
        try {
            return new JSONArray(context.getSharedPreferences("post-sharing", 0).getString("plans", "[]"));
        } catch (JSONException unused) {
            return new JSONArray();
        }
    }

    static synchronized void record(Context context, long j, String str, String str2) throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences("post-sharing", 0);
        JSONArray jSONArray = new JSONArray(sharedPreferences.getString("history", "[]"));
        jSONArray.put(new JSONObject().put("post", j).put("target", str).put("status", str2).put("at", System.currentTimeMillis()));
        JSONArray jSONArray2 = new JSONArray();
        for (int iMax = Math.max(0, jSONArray.length() - 2000); iMax < jSONArray.length(); iMax++) {
            jSONArray2.put(jSONArray.get(iMax));
        }
        sharedPreferences.edit().putString("history", jSONArray2.toString()).commit();
    }

    static synchronized void replace(Context context, long j, String str, int i, int i2, long j2) throws JSONException {
        if (j > 0) {
            if ((FACEBOOK.equals(str) || BUSINESS.equals(str)) && i >= 1 && i <= 100 && i2 >= 15 && i2 <= 43200) {
                JSONArray jSONArrayLoad = load(context);
                for (int i3 = 0; i3 < jSONArrayLoad.length(); i3++) {
                    JSONObject jSONObject = jSONArrayLoad.getJSONObject(i3);
                    if (jSONObject.optLong("post") == j && str.equals(jSONObject.optString("target"))) {
                        jSONObject.put("active", false);
                    }
                }
                jSONArrayLoad.put(new JSONObject().put("id", UUID.randomUUID().toString()).put("post", j).put("target", str).put("total", i).put("remaining", i).put("minutes", i2).put("next", j2).put("active", true).put("notified", false));
                save(context, jSONArrayLoad);
                schedule(context);
            }
        }
        throw new IllegalArgumentException();
    }

    static void save(Context context, JSONArray jSONArray) {
        context.getSharedPreferences("post-sharing", 0).edit().putString("plans", jSONArray.toString()).commit();
    }

    static synchronized void schedule(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
        alarmManager.cancel(alarm(context));
        if (allowed(context)) {
            JSONArray jSONArrayLoad = load(context);
            long jMin = Long.MAX_VALUE;
            for (int i = 0; i < jSONArrayLoad.length(); i++) {
                JSONObject jSONObjectOptJSONObject = jSONArrayLoad.optJSONObject(i);
                if (jSONObjectOptJSONObject != null && jSONObjectOptJSONObject.optBoolean("active") && !jSONObjectOptJSONObject.optBoolean("notified")) {
                    jMin = Math.min(jMin, jSONObjectOptJSONObject.optLong("next"));
                }
            }
            if (jMin != Long.MAX_VALUE) {
                alarmManager.setAndAllowWhileIdle(0, Math.max(jMin, System.currentTimeMillis() + 1000), alarm(context));
            }
        }
    }

    static synchronized void stop(Context context, String str) throws JSONException {
        JSONArray jSONArrayLoad = load(context);
        for (int i = 0; i < jSONArrayLoad.length(); i++) {
            JSONObject jSONObject = jSONArrayLoad.getJSONObject(i);
            if (str.equals(jSONObject.optString("id"))) {
                jSONObject.put("active", false);
            }
        }
        save(context, jSONArrayLoad);
        ((NotificationManager) context.getSystemService(NotificationManager.class)).cancel(str, 0);
        schedule(context);
    }
}
