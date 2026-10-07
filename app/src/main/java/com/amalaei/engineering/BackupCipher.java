package com.amalaei.engineering;

import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

final class BackupCipher {
    private static final String FORMAT="engineeringcrm-encrypted-backup";
    private static final int ITERATIONS=210000;
    private static final byte[] AAD="Engineering CRM backup v1".getBytes(StandardCharsets.UTF_8);

    static boolean encrypted(String text){try{return FORMAT.equals(new JSONObject(text).optString("format"));}catch(Exception e){return false;}}
    static byte[] encrypt(byte[] plain,char[] password) throws Exception {
        validate(password);byte[] salt=new byte[16],iv=new byte[12];SecureRandom random=new SecureRandom();random.nextBytes(salt);random.nextBytes(iv);byte[] key=derive(password,salt,ITERATIONS);
        try{Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));cipher.updateAAD(AAD);byte[] encrypted=cipher.doFinal(plain);JSONObject envelope=new JSONObject().put("format",FORMAT).put("version",1).put("cipher","AES-256-GCM").put("kdf","PBKDF2-HMAC-SHA256").put("iterations",ITERATIONS).put("salt",Base64.getEncoder().encodeToString(salt)).put("iv",Base64.getEncoder().encodeToString(iv)).put("ciphertext",Base64.getEncoder().encodeToString(encrypted));return envelope.toString().getBytes(StandardCharsets.UTF_8);}finally{Arrays.fill(key,(byte)0);}
    }
    static byte[] decrypt(String envelope,char[] password) throws Exception {
        validate(password);JSONObject json=new JSONObject(envelope);if(!FORMAT.equals(json.optString("format"))||json.optInt("version")!=1)throw new IllegalArgumentException("صيغة النسخة المشفرة غير مدعومة");int iterations=json.getInt("iterations");if(iterations<100000||iterations>1000000)throw new IllegalArgumentException("إعدادات تشفير النسخة غير صالحة");byte[] salt=Base64.getDecoder().decode(json.getString("salt")),iv=Base64.getDecoder().decode(json.getString("iv")),data=Base64.getDecoder().decode(json.getString("ciphertext"));if(salt.length!=16||iv.length!=12)throw new IllegalArgumentException("بيانات تشفير النسخة غير صالحة");byte[] key=derive(password,salt,iterations);
        try{Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));cipher.updateAAD(AAD);return cipher.doFinal(data);}catch(javax.crypto.AEADBadTagException wrong){throw new IllegalArgumentException("كلمة المرور غير صحيحة أو الملف تالف");}finally{Arrays.fill(key,(byte)0);}
    }
    private static byte[] derive(char[] password,byte[] salt,int iterations)throws Exception{PBEKeySpec spec=new PBEKeySpec(password,salt,iterations,256);try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}finally{spec.clearPassword();}}
    private static void validate(char[] password){if(password==null||password.length<8)throw new IllegalArgumentException("استخدم كلمة مرور من 8 أحرف على الأقل");}
}
