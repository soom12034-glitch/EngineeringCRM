package com.amalaei.engineering;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import org.json.JSONObject;

final class Reminders {
    static boolean enabled(Context c){return c.getSharedPreferences("settings",0).getBoolean("reminders",true);}
    static void channel(Context c){NotificationChannel channel=new NotificationChannel("followups","مواعيد متابعة العملاء والخدمات",NotificationManager.IMPORTANCE_DEFAULT);channel.setDescription("تذكيرات المبيعات والدعم الفني والصيانة والمعايرة حسب موعد المتابعة");c.getSystemService(NotificationManager.class).createNotificationChannel(channel);}
    static boolean allowed(Context c){channel(c);NotificationManager manager=c.getSystemService(NotificationManager.class);NotificationChannel channel=manager.getNotificationChannel("followups");return (Build.VERSION.SDK_INT<33||c.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)&&manager.areNotificationsEnabled()&&channel!=null&&channel.getImportance()!=NotificationManager.IMPORTANCE_NONE;}
    static Intent settings(Context c){NotificationManager manager=c.getSystemService(NotificationManager.class);if(manager.areNotificationsEnabled())return new Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,c.getPackageName()).putExtra(android.provider.Settings.EXTRA_CHANNEL_ID,"followups");return new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,c.getPackageName());}
    static void test(Context c){channel(c);if(!allowed(c))return;PendingIntent click=PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class).setAction("com.amalaei.engineering.NOTIFICATION_TEST"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);c.getSystemService(NotificationManager.class).notify("test",0,new Notification.Builder(c,"followups").setSmallIcon(R.drawable.ic_notification).setContentTitle("عملائي • الإشعارات تعمل").setContentText("إشعار تجريبي. ستصلك تذكيرات الطلبات عند مواعيد المتابعة المحددة.").setContentIntent(click).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).build());}
    static PendingIntent alarm(Context c){return PendingIntent.getBroadcast(c,1,new Intent(c,ReminderReceiver.class).setAction("com.amalaei.engineering.REMIND"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    static void schedule(Context c){
        AlarmManager manager=c.getSystemService(AlarmManager.class);manager.cancel(alarm(c));
        if(!enabled(c)||!allowed(c))return;
        try(Store store=new Store(c)){
            long next=Long.MAX_VALUE;
            for(JSONObject lead:store.query("SELECT followUp,followTime,lastNotified,stage FROM leads WHERE stage<4 AND followUp<>''")){
                String date=lead.optString("followUp"),time=lead.optString("followTime","09:00");
                if(FollowUps.pending(lead.optInt("stage"),date,time,lead.optString("lastNotified")))try{next=Math.min(next,FollowUps.at(date,time));}catch(Exception invalid){}
            }
            for(JSONObject row:store.query("SELECT * FROM softwareRequests"))try{JSONObject q=new JSONObject(row.getString("data")),notified=new JSONObject(row.optString("notified","{}"));for(String kind:SoftwareWork.REMINDERS){String date=SoftwareWork.due(q,kind),time=SoftwareWork.time(q,kind);if(!date.isEmpty()&&!FollowUps.key(date,time).equals(notified.optString(kind)))next=Math.min(next,FollowUps.at(date,time));}}catch(Exception invalid){}
            if(next!=Long.MAX_VALUE)manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,Math.max(next,System.currentTimeMillis()+60000),alarm(c));
        }catch(Exception e){c.getSharedPreferences("settings",0).edit().putString("status","تعذر جدولة التنبيهات. افتح قائمة المتابعات وتحقق من الصلاحيات.").apply();}
    }
    static void deliver(Context c){
        if(!enabled(c)||!allowed(c))return;
        NotificationManager manager=c.getSystemService(NotificationManager.class);
        channel(c);
        try(Store store=new Store(c)){
            int count=0;
            for(JSONObject lead:store.query("SELECT * FROM leads WHERE stage<4 AND followUp<>'' ORDER BY followUp,followTime")){
                String date=lead.optString("followUp"),time=lead.optString("followTime","09:00"),key=FollowUps.key(date,time);
                if(!FollowUps.pending(lead.optInt("stage"),date,time,lead.optString("lastNotified")))continue;
                try{if(FollowUps.at(date,time)>System.currentTimeMillis())continue;}catch(Exception invalid){continue;}
                long id=lead.optLong("id");Intent open=new Intent(c,MainActivity.class).putExtra("openLead",id).setAction("com.amalaei.engineering.OPEN."+id);
                PendingIntent click=PendingIntent.getActivity(c,(int)id,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
                Notification notice=new Notification.Builder(c,"followups").setSmallIcon(R.drawable.ic_notification).setContentTitle("متابعة: "+lead.optString("name")).setContentText((lead.optString("business").equals("software")?"البرامج والتطبيقات":SurveyRequests.label(lead.optString("requestType","sales")))+" • "+date+" "+time).setContentIntent(click).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).build();
                manager.notify("lead",(int)id,notice);
                android.content.ContentValues values=new android.content.ContentValues();values.put("lastNotified",key);store.getWritableDatabase().update("leads",values,"id=?",new String[]{String.valueOf(id)});
                if(++count>=30)break;
            }
            for(JSONObject row:store.query("SELECT * FROM softwareRequests ORDER BY id")){if(count>=30)break;try{JSONObject q=new JSONObject(row.getString("data")),notified=new JSONObject(row.optString("notified","{}"));StringBuilder text=new StringBuilder();for(String kind:SoftwareWork.REMINDERS){String date=SoftwareWork.due(q,kind),time=SoftwareWork.time(q,kind),key=FollowUps.key(date,time);if(!date.isEmpty()&&!key.equals(notified.optString(kind))&&FollowUps.at(date,time)<=System.currentTimeMillis()){if(text.length()>0)text.append(" • ");text.append(SoftwareWork.reminderLabel(kind)).append(": ").append(date);notified.put(kind,key);}}if(text.length()==0)continue;long id=row.getLong("id");Intent open=new Intent(c,SoftwareActivity.class).putExtra("requestId",id).setAction("com.amalaei.engineering.SOFTWARE."+id);PendingIntent click=PendingIntent.getActivity(c,(int)id,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);manager.notify("software",(int)id,new Notification.Builder(c,"followups").setSmallIcon(R.drawable.ic_notification).setContentTitle(q.optString("title")+" • "+store.lead(row.optLong("leadId")).optString("name")).setContentText(text.toString()).setStyle(new Notification.BigTextStyle().bigText(text.toString())).setContentIntent(click).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).build());android.content.ContentValues v=new android.content.ContentValues();v.put("notified",notified.toString());store.getWritableDatabase().update("softwareRequests",v,"id=?",new String[]{String.valueOf(id)});count++;}catch(Exception invalid){}}
        }
    }
}
