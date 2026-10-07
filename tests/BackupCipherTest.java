package com.amalaei.engineering;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class BackupCipherTest {
    public static void main(String[] args) throws Exception {
        byte[] input="{\"lead\":\"عميل تجريبي\",\"phone\":\"+966501234567\"}".getBytes(StandardCharsets.UTF_8);char[] password="strong-pass-123".toCharArray();byte[] encrypted=BackupCipher.encrypt(input,password);String envelope=new String(encrypted,StandardCharsets.UTF_8);
        if(!BackupCipher.encrypted(envelope)||Arrays.equals(input,encrypted)||!Arrays.equals(input,BackupCipher.decrypt(envelope,password)))throw new AssertionError("encrypted backup roundtrip failed");
        try{BackupCipher.decrypt(envelope,"wrong-password".toCharArray());throw new AssertionError("wrong password accepted");}catch(IllegalArgumentException expected){}
        System.out.println("PASS: AES-GCM backup encryption roundtrip and wrong-password rejection.");
    }
}
