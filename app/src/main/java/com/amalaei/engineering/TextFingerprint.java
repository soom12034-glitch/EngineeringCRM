package com.amalaei.engineering;
import java.security.MessageDigest;
final class TextFingerprint {
    static String of(String value) {
        try {byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"));StringBuilder b=new StringBuilder();for(byte v:bytes)b.append(String.format("%02x",v&255));return b.toString();}
        catch(Exception e){throw new IllegalStateException(e);}
    }
}
