package com.amalaei.engineering;

import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.util.Base64;
import java.io.*;

final class QuoteBranding {
    private static final int MAX_SOURCE_BYTES=4*1024*1024,MAX_PIXELS=50_000_000,MAX_LOGO_BYTES=650*1024;

    static String importLogo(Context context,Uri uri)throws Exception{
        if(uri==null||!"content".equals(uri.getScheme()))throw new IOException("اختر الشعار من منتقي الصور");
        byte[] source;
        try(InputStream input=context.getContentResolver().openInputStream(uri);ByteArrayOutputStream output=new ByteArrayOutputStream()){
            if(input==null)throw new IOException("تعذر قراءة الشعار");byte[] buffer=new byte[8192];int read;
            while((read=input.read(buffer))!=-1){output.write(buffer,0,read);if(output.size()>MAX_SOURCE_BYTES)throw new IOException("اختر شعارًا لا يتجاوز 4 ميغابايت");}source=output.toByteArray();
        }
        BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(source,0,source.length,bounds);
        if(bounds.outWidth<=0||bounds.outHeight<=0||(long)bounds.outWidth*bounds.outHeight>MAX_PIXELS)throw new IOException("ملف الشعار غير صالح أو أبعاده كبيرة جدًا");
        Bitmap original=BitmapFactory.decodeByteArray(source,0,source.length);if(original==null)throw new IOException("تعذر فتح صورة الشعار");
        try{
            float scale=Math.min(1f,Math.min(900f/original.getWidth(),360f/original.getHeight()));int width=Math.max(1,Math.round(original.getWidth()*scale)),height=Math.max(1,Math.round(original.getHeight()*scale));
            Bitmap resized=Bitmap.createScaledBitmap(original,width,height,true);
            try{
                ByteArrayOutputStream encoded=new ByteArrayOutputStream();resized.compress(Bitmap.CompressFormat.PNG,100,encoded);
                if(encoded.size()>MAX_LOGO_BYTES){encoded.reset();Bitmap white=Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);Canvas canvas=new Canvas(white);canvas.drawColor(Color.WHITE);canvas.drawBitmap(resized,0,0,null);white.compress(Bitmap.CompressFormat.JPEG,88,encoded);white.recycle();}
                if(encoded.size()>MAX_LOGO_BYTES)throw new IOException("بسّط صورة الشعار أو استخدم أبعادًا أصغر");
                return Base64.encodeToString(encoded.toByteArray(),Base64.NO_WRAP);
            }finally{if(resized!=original)resized.recycle();}
        }finally{original.recycle();}
    }

    static Bitmap bitmap(String encoded){
        try{if(encoded==null||encoded.isEmpty()||encoded.length()>MAX_LOGO_BYTES*4/3+16)return null;byte[] bytes=Base64.decode(encoded,Base64.DEFAULT);if(bytes.length>MAX_LOGO_BYTES)return null;return BitmapFactory.decodeByteArray(bytes,0,bytes.length);}catch(Exception invalid){return null;}
    }
}
