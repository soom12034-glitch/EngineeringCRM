package com.amalaei.engineering;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import java.util.LinkedHashMap;
import java.util.Map;

final class PhoneData {
    static String saudi(String raw) {
        if (raw == null) return "";
        StringBuilder digits = new StringBuilder();
        for (char c : raw.toCharArray()) {
            int n = Character.digit(c, 10);
            if (n >= 0) digits.append(n);
            else if (c == '+') digits.append(c);
        }
        String s = digits.toString();
        if (s.matches("0[15][0-9]{8}")) s = "+966" + s.substring(1);
        else if (s.matches("00966[0-9]{9}")) s = "+" + s.substring(2);
        else if (s.matches("966[0-9]{9}")) s = "+" + s;
        return s.matches("\\+966[0-9]{9}") ? s : "";
    }
    static Boolean exists(Context context,String phone){if(context.checkSelfPermission(Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED)return null;Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phone));try(Cursor c=context.getContentResolver().query(uri,new String[]{ContactsContract.PhoneLookup._ID},null,null,null)){return c==null?null:c.moveToFirst();}catch(Exception unavailable){return null;}}
    static String contact(Context context, String phone) {
        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return "";
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        try (Cursor c = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) return c.getString(0) == null ? "" : c.getString(0);
        } catch (Exception ignored) { }
        return "";
    }
    static Map<String,String> contacts(Context context) {
        LinkedHashMap<String,String> result=new LinkedHashMap<>();
        if(context.checkSelfPermission(Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED)return result;
        String[] projection={ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        try(Cursor c=context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,projection,null,null,null)){
            if(c!=null)while(c.moveToNext()){
                String phone=saudi(c.getString(0)),name=c.getString(1)==null?"":c.getString(1).trim();
                if(!phone.isEmpty()&&!name.isEmpty()&&!result.containsKey(phone))result.put(phone,name);
            }
        }catch(Exception ignored){}
        return result;
    }
}
