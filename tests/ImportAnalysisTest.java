package com.amalaei.engineering;
import java.util.*;
public final class ImportAnalysisTest {
    static void check(boolean value,String msg){if(!value)throw new AssertionError(msg);}
    public static void main(String[] args){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Riyadh"));long at=java.time.Instant.parse("2026-10-05T06:00:00Z").toEpochMilli();
        Analysis a=Analysis.inspect("الاسم: عميل تجريبي\nالشركة: منشأة تجريبية\nرقمي ٠٥٤١٢٣٤٥٦٧\nاريد توتال M25 غدا الساعة 03:30 مساء\nالميزانية: 5,000\nالكمية: ٢",at);
        check(a.phone.equals("+966541234567"),"Saudi Arabic phone");check(a.company.equals("منشأة تجريبية")&&a.name.equals("عميل تجريبي"),"explicit identity fields");check(a.details.get("budget").equals("5000"),"thousands amount");check(a.details.get("quantity").equals("2")&&a.details.get("model").equals("M25"),"request details");check(a.followTime.equals("15:30")&&a.followUp.equals("2026-10-06"),"time and date");
        Analysis ambiguous=Analysis.inspect("0541234567 و 0557654321",at);check(ambiguous.phones.size()==2&&ambiguous.phone.isEmpty(),"multiple numbers require choice");
        check(Analysis.inspect("انتهاء الاشتراك: 2026-11-01",at).followUp.isEmpty(),"subscription date not followup");check(Analysis.inspect("الكمية: 0",at).details.get("quantity")==null,"invalid quantity omitted");
        String export="05/10/2026, 09:00 - أنا: برنامج ERP متاح\n05/10/2026, 09:10 - +966541234567: اريد جهاز GPS\nكلمني غدا\n05/10/2026, 09:11 - تغير رمز الامان\n05/10/2026, 09:12 - أنا: حاضر";
        LinkedHashMap<String,ArrayList<ChatText.Message>> parsed=ChatText.parse(export,at,false);check(parsed.size()==2,"participants parsed");check(parsed.get("+966541234567").size()==1,"system line omitted");ChatText.Message customer=parsed.get("+966541234567").get(0);check(customer.body.contains("كلمني غدا")&&customer.dated,"multiline and date");check(Analysis.inspect(customer.body,customer.at).business.equals("survey"),"own ERP message excluded");
        String ios="[10/05/26, 9:10:00 AM] Ahmed: برنامج سحابي";ChatText.Message m=ChatText.parse(ios,at,true).get("Ahmed").get(0);check(m.at==java.time.Instant.parse("2026-10-05T06:10:00Z").toEpochMilli(),"iOS month-first export");
        check(ChatText.parse("نص بلا تاريخ",at,false).isEmpty(),"plain text fallback");check(!ChatText.parse("99/99/26, 9:10 - Ahmed: غدا",at,false).get("Ahmed").get(0).dated,"invalid timestamp flagged");
        System.out.println("PASS: structured suggestions, Arabic digits, request values, phone ambiguity, Android/iOS exports, sender isolation, multiline and timestamp validity.");
    }
}
