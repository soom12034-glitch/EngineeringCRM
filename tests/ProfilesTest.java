package com.amalaei.engineering;
public final class ProfilesTest {
    static void check(boolean value){if(!value)throw new AssertionError();}
    static void reject(String url){try{Profiles.url(url);throw new AssertionError("accepted: "+url);}catch(IllegalArgumentException expected){}}
    public static void main(String[] args){
        check(Profiles.url("example.com/company").equals("https://example.com/company"));
        check(Profiles.url("https://instagram.com/company").equals("https://instagram.com/company"));
        check(Profiles.url("").isEmpty());reject("javascript:alert(1)");reject("file:///etc/passwd");reject("https://user:pass@example.com");reject("intent://app");reject("not a link");
        check(Profiles.firstPhone("0500000000\n").equals("0500000000"));
        check(!java.util.Arrays.asList(Profiles.AD_FIELDS).contains("registration"));check(!java.util.Arrays.asList(Profiles.AD_FIELDS).contains("vat"));
        System.out.println("PASS: links normalized, unsupported schemes rejected, first image contact separated, tax/registration not auto included in advertising.");
    }
}
