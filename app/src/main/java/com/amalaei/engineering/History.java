package com.amalaei.engineering;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.CallLog;
import android.database.Cursor;

final class History {
    static void run(Context context) {
        int matched=0,created=0;
        try {
            if(context.checkSelfPermission(Manifest.permission.READ_CALL_LOG)!=PackageManager.PERMISSION_GRANTED)throw new SecurityException();
            long checkpoint=context.getSharedPreferences("settings",0).getLong("historyId",0);
            boolean more=true;
            try(Store store=new Store(context)) {
                if(context.checkSelfPermission(Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED){
                    for(org.json.JSONObject existing:store.query("SELECT phone,lastCallAt FROM leads WHERE lastCallAt>0 AND phone IS NOT NULL AND (contactName='' OR name=phone)")){
                        String name=PhoneData.contact(context,existing.optString("phone"));if(!name.isEmpty())store.recordCall(existing.optString("phone"),existing.optLong("lastCallAt"),name);
                    }
                }
                while(more){more=false;
                    android.net.Uri uri=CallLog.Calls.CONTENT_URI.buildUpon().appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY,"200").build();
                    try(Cursor c=context.getContentResolver().query(uri,new String[]{CallLog.Calls._ID,CallLog.Calls.NUMBER,CallLog.Calls.DATE,CallLog.Calls.CACHED_NAME},"_id>?",new String[]{String.valueOf(checkpoint)},"_id ASC")) {
                        if(c==null)throw new IllegalStateException();int count=0;
                        while(c.moveToNext()) {String phone=Analysis.phone(c.getString(1));if(!phone.isEmpty()){if(store.byPhone(phone).optLong("id")==0)created++;String name=c.getString(3);if(CallCsvImporter.missingName(name))name=PhoneData.contact(context,phone);store.recordCall(phone,c.getLong(2),name==null?"":name.trim(),"");matched++;}checkpoint=c.getLong(0);count++;}
                        context.getSharedPreferences("settings",0).edit().putLong("historyId",checkpoint).apply();more=count>=200;
                    }
                }
            }
            status(context,"اكتمل الاستيراد: "+created+" رقمًا جديدًا للمراجعة، و"+matched+" مكالمة عولجت. لا يصبح الرقم عميلًا إلا بعد اعتماده.");
        } catch(SecurityException e){status(context,"إذن سجل المكالمات غير متاح. أندرويد قد يقيّده حسب المثبّت. لا يتم تجاوز حماية الهاتف.");}
        catch(Exception e){status(context,"توقف استيراد السجل؛ أعد المحاولة لاستكمال آخر موضع محفوظ.");}
    }
    static void status(Context context,String text){context.getSharedPreferences("settings",0).edit().putString("status",text).apply();}
}
