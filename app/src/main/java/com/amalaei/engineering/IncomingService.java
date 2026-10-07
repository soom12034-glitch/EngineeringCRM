package com.amalaei.engineering;

import android.telecom.Call;
import android.telecom.CallScreeningService;

public final class IncomingService extends CallScreeningService {
    @Override public void onScreenCall(Call.Details call) {
        if(call.getCallDirection()!=Call.Details.DIRECTION_INCOMING)return;
        respondToCall(call,new CallResponse.Builder().setDisallowCall(false).setRejectCall(false).setSilenceCall(false).build());
        final android.content.SharedPreferences prefs=getSharedPreferences("settings",0);
        if(!prefs.getBoolean("calls",false)||call.getHandle()==null)return;
        final String phone=Analysis.phone(call.getHandle().getSchemeSpecificPart());if(phone.isEmpty())return;
        final long now=System.currentTimeMillis();final String newBusiness=prefs.getString("newBusiness","");
        new Thread(() -> {try(Store store=new Store(this)){store.recordCall(phone,now,PhoneData.contact(this,phone),newBusiness);}catch(Exception e){prefs.edit().putString("status","تعذر حفظ رقم الاتصال؛ تحقق من مساحة الهاتف.").apply();}},"incoming-call").start();
    }
}
