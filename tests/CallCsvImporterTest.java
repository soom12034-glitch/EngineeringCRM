package com.amalaei.engineering;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;

public final class CallCsvImporterTest {
    public static void main(String[] args) {
        if(!validYear(CallCsvImporter.time("1760000000000"),2025))throw new AssertionError("epoch millis parse");
        if(!validYear(CallCsvImporter.time("202610070930"),2026))throw new AssertionError("compact yyyyMMddHHmm must not be treated as epoch millis");
        if(!validYear(CallCsvImporter.time("2026-10-07 09:30"),2026))throw new AssertionError("ISO datetime");
        if(!validYear(CallCsvImporter.time("2026-10-07"),2026))throw new AssertionError("ISO date only");
        if(!validYear(CallCsvImporter.time("07/10/2026 09:30 ص"),2026))throw new AssertionError("Arabic AM marker");
        if(!validYear(CallCsvImporter.time("07/10/2026 09:30 م"),2026))throw new AssertionError("Arabic PM marker");
        if(CallCsvImporter.time("")!=Long.MIN_VALUE)throw new AssertionError("empty date must be invalid");
        if(CallCsvImporter.time("لا يوجد تاريخ")!=Long.MIN_VALUE)throw new AssertionError("non-date text must be invalid");
        if(!CallCsvImporter.hasHeader(list("name","phone","date")))throw new AssertionError("header row detected");
        if(CallCsvImporter.hasHeader(list("أحمد","0551234567","07/10/2026 11:00")))throw new AssertionError("data row must not be a header");
        if(CallCsvImporter.column(list("رقم","الهاتف"),"رقم","الهاتف")!=0)throw new AssertionError("exact column match must beat contains match");
        if(CallCsvImporter.column(list("cached_name","number","date"),"name","cachedname")!=0)throw new AssertionError("Infinix cached name header");
        ArrayList<String> rec=list("أحمد محمد","0551234567","07/10/2026 11:00");
        int phone=CallCsvImporter.inferPhone(rec);
        if(phone!=1)throw new AssertionError("inferPhone");
        if(CallCsvImporter.inferName(rec,phone)!=0)throw new AssertionError("inferName");
        if(CallCsvImporter.inferDate(rec,phone)!=2)throw new AssertionError("inferDate");
        if(!CallCsvImporter.missingName("عميل غير معرف")||!CallCsvImporter.missingName("Unknown")||CallCsvImporter.missingName("المهندس فادي"))throw new AssertionError("placeholder contact names");
        String denied=new CallCsvImporter.Result(2,2,1,0,0,0,false).message();
        if(!denied.contains("إذن جهات الاتصال غير مسموح"))throw new AssertionError("permission diagnostic");
        String unmatched=new CallCsvImporter.Result(2,2,1,0,0,12,true).message();
        if(!unmatched.contains("12 جهة اتصال")||!unmatched.contains("لم تتطابق"))throw new AssertionError("unmatched contact diagnostic");
        String matched=new CallCsvImporter.Result(2,2,1,0,2,12,true).message();
        if(!matched.contains("مطابقة أسماء 2"))throw new AssertionError("matched contact diagnostic");
        try{CallCsvImporter.parse("name,phone\n\"x,0551234567");throw new AssertionError("unbalanced quotes must throw");}catch(IllegalArgumentException expected){}
        ArrayList<ArrayList<String>> serial=rows(list("رقم","الاسم","الجوال"),list("1","أحمد","0551234567"));
        if(CallCsvImporter.resolvePhoneColumn(serial,1,0)!=2)throw new AssertionError("a header column without phones must fall back to the data");
        if(CallCsvImporter.resolvePhoneColumn(serial,1,2)!=2)throw new AssertionError("a header column with phones must be kept");
        if(CallCsvImporter.hasHeader(list("Hotel Riyadh","0551234567","2026-10-07")))throw new AssertionError("a row with a phone is data even if a cell contains a header word");
        if(CallCsvImporter.hasHeader(list("Report date","2026-10-07")))throw new AssertionError("a row with a date value is not a header");
        if(CallCsvImporter.headerRow(rows(list("أحمد","0551234567"),list("Hotel Riyadh",""),list("سعد","0507654321")))!=-1)throw new AssertionError("no header may follow a data row");
        if(CallCsvImporter.firstDataRow(rows(list("Report date","2026-10-07"),list("أحمد","0551234567")))!=1)throw new AssertionError("column inference must skip preamble rows");
        if(CallCsvImporter.time("07/10/2026 09:30 ص")!=at(2026,10,7,9,30))throw new AssertionError("AM/PM dates must be day first");
        if(CallCsvImporter.time("7/10/2026 09:30 م")!=at(2026,10,7,21,30))throw new AssertionError("single digit day must be day first");
        if(CallCsvImporter.time("13/10/2026 09:30 ص")!=at(2026,10,13,9,30))throw new AssertionError("day above 12");
        if(CallCsvImporter.time("10/13/2026 09:30 AM")!=at(2026,10,13,9,30))throw new AssertionError("month first stays supported when unambiguous");
        System.out.println("PASS: call CSV time parsing, header detection, column inference and unbalanced quotes.");
    }
    private static long at(int year,int month,int day,int hour,int minute){return LocalDateTime.of(year,month,day,hour,minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();}
    @SafeVarargs private static ArrayList<ArrayList<String>> rows(ArrayList<String>... values){return new ArrayList<>(Arrays.asList(values));}
    private static boolean validYear(long epochMillis,int expected){if(epochMillis==Long.MIN_VALUE)return false;int year=Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).getYear();return year==expected;}
    private static ArrayList<String> list(String... values){return new ArrayList<>(Arrays.asList(values));}
}
