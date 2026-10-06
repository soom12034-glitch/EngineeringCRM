package com.amalaei.engineering;
import android.content.*;
public final class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){final PendingResult result=goAsync();new Thread(()->{try{if("com.amalaei.engineering.REMIND".equals(intent.getAction()))Reminders.deliver(context);Reminders.schedule(context);}catch(Exception e){context.getSharedPreferences("settings",0).edit().putString("status","تعذر إظهار تذكير المتابعة؛ راجع قائمة اليوم.").apply();}finally{result.finish();}},"followup-reminder").start();}
}
