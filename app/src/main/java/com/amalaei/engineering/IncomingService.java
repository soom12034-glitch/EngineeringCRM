package com.amalaei.engineering;

import android.telecom.Call;
import android.telecom.CallScreeningService;

public final class IncomingService extends CallScreeningService {
    @Override public void onScreenCall(Call.Details call) {
        if(call.getCallDirection()!=Call.Details.DIRECTION_INCOMING)return;
        respondToCall(call,new CallResponse.Builder().setDisallowCall(false).setRejectCall(false).setSilenceCall(false).build());
        if(!getSharedPreferences("settings",0).getBoolean("calls",false)||call.getHandle()==null)return;
        String phone=Analysis.phone(call.getHandle().getSchemeSpecificPart());if(phone.isEmpty())return;
        try(Store store=new Store(this)){store.recordCall(phone,System.currentTimeMillis(),PhoneData.contact(this,phone),getSharedPreferences("settings",0).getString("newBusiness",""));}
        catch(Exception e){getSharedPreferences("settings",0).edit().putString("status","تعذر حفظ رقم الاتصال؛ تحقق من مساحة الهاتف.").apply();}
    }
}
