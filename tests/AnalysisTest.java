package com.amalaei.engineering;

public final class AnalysisTest {
    private static void check(boolean value,String description){if(!value)throw new AssertionError(description);}
    public static void main(String[] args){
        long at=java.time.LocalDate.of(2026,10,5).atTime(12,0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        Analysis equipment=Analysis.inspect("اريد جهاز توتال استيشن ورقم التواصل 0541234567 غدا",at);
        check(equipment.business.equals("survey"),"survey suggestion");
        check(equipment.phone.equals("+966541234567"),"phone in message");
        check(equipment.followUp.equals("2026-10-06"),"tomorrow uses message date");
        Analysis software=Analysis.inspect("اريد اشتراك برنامج سحابي تواصل sales@example.com اليوم",at);
        check(software.business.equals("software"),"software suggestion");
        check(software.email.equals("sales@example.com"),"email extraction");
        check(software.followUp.equals("2026-10-05"),"today");
        check(Analysis.inspect("اريد برنامج وجهاز GPS",at).business.isEmpty(),"ambiguous business requires review");
        check(Analysis.inspect("مرحبا",at).phone.isEmpty(),"no invented phone");
        check(Analysis.phone("٠٥٤١٢٣٤٥٦٧").equals("+966541234567"),"Arabic digits");
        check(Analysis.phone("0112345678").equals("+966112345678"),"landline");
        check(Analysis.phone("+12025550123").isEmpty(),"Saudi import boundary");
        check(Analysis.inspect("المتابعة بتاريخ 2026-10-12",at).followUp.equals("2026-10-12"),"explicit date");
        check(Analysis.inspect("المتابعة بتاريخ 2026-99-12",at).followUp.isEmpty(),"invalid date not accepted");
        System.out.println("PASS: message suggestions, date anchoring, ambiguous business, no invented phone, normalization and invalid-date guard.");
    }
}
