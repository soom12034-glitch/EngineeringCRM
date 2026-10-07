package com.amalaei.engineering;

import java.time.Instant;
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
        ArrayList<String> rec=list("أحمد محمد","0551234567","07/10/2026 11:00");
        int phone=CallCsvImporter.inferPhone(rec);
        if(phone!=1)throw new AssertionError("inferPhone");
        if(CallCsvImporter.inferName(rec,phone)!=0)throw new AssertionError("inferName");
        if(CallCsvImporter.inferDate(rec,phone)!=2)throw new AssertionError("inferDate");
        try{CallCsvImporter.parse("name,phone\n\"x,0551234567");throw new AssertionError("unbalanced quotes must throw");}catch(IllegalArgumentException expected){}
        System.out.println("PASS: call CSV time parsing, header detection, column inference and unbalanced quotes.");
    }
    private static boolean validYear(long epochMillis,int expected){if(epochMillis==Long.MIN_VALUE)return false;int year=Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).getYear();return year==expected;}
    private static ArrayList<String> list(String... values){return new ArrayList<>(Arrays.asList(values));}
}