package com.amalaei.engineering;

import android.content.Context;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

final class CallCsvImporter {
    static final class Result {
        final int rows, imported, created, skipped;
        Result(int rows,int imported,int created,int skipped){this.rows=rows;this.imported=imported;this.created=created;this.skipped=skipped;}
        String message(){return "اكتمل الاستيراد: "+imported+" مكالمة صالحة، "+created+" عميل جديد، و"+skipped+" صف متجاوز من أصل "+rows+".";}
    }

    static Result run(Context context,String csv) throws Exception {
        if(csv==null||csv.trim().isEmpty())throw new IllegalArgumentException("ملف سجل المكالمات فارغ");
        ArrayList<ArrayList<String>> records=parse(csv);
        if(records.isEmpty())throw new IllegalArgumentException("لم يُعثر على صفوف في الملف");
        ArrayList<String> first=records.get(0);boolean header=!hasPhone(first);
        int phone=header?column(first,"number","phone","mobile","tel","رقم","الهاتف","الجوال","رقمالهاتف","رقمالمكالمة"):inferPhone(first);
        int name=header?column(first,"name","contact","contactname","displayname","الاسم","جهةالاتصال","اسمالمتصل"):-1;
        int date=header?column(first,"date","time","timestamp","datetime","calldate","التاريخ","الوقت","تاريخالمكالمة"):-1;
        if(phone<0)throw new IllegalArgumentException("تعذر تحديد عمود رقم الهاتف. استخدم عنوان number أو phone أو رقم الهاتف");
        int imported=0,created=0,skipped=0,start=header?1:0;long fallback=System.currentTimeMillis();
        try(Store store=new Store(context)){
            for(int i=start;i<records.size();i++){
                ArrayList<String> row=records.get(i);if(empty(row))continue;
                String normalized=phone<row.size()?Analysis.phone(row.get(phone)):"";
                if(normalized.isEmpty()){skipped++;continue;}
                String contact=name>=0&&name<row.size()?safe(row.get(name),120):"";
                long at=date>=0&&date<row.size()?time(row.get(date),fallback-i):fallback-i;
                boolean fresh=store.byPhone(normalized).optLong("id")==0;
                store.recordCall(normalized,at,contact,AppMode.MODE);
                if(fresh)created++;imported++;
            }
        }
        if(imported==0)throw new IllegalArgumentException("لا يحتوي الملف أرقامًا سعودية صالحة. يدعم 05 و01 و+966");
        return new Result(records.size()-start,imported,created,skipped);
    }

    private static String safe(String value,int max){String v=value==null?"":value.trim();return v.length()>max?v.substring(0,max):v;}
    private static boolean empty(ArrayList<String> row){for(String v:row)if(v!=null&&!v.trim().isEmpty())return false;return true;}
    private static boolean hasPhone(ArrayList<String> row){return inferPhone(row)>=0;}
    private static int inferPhone(ArrayList<String> row){for(int i=0;i<row.size();i++)if(!Analysis.phone(row.get(i)).isEmpty())return i;return -1;}
    private static int column(ArrayList<String> headers,String... names){
        for(int i=0;i<headers.size();i++){String h=key(headers.get(i));for(String name:names)if(h.equals(key(name))||h.contains(key(name)))return i;}return -1;
    }
    private static String key(String s){return Analysis.digits(s==null?"":s).toLowerCase(Locale.ROOT).replace('أ','ا').replace('إ','ا').replace('آ','ا').replace('ى','ي').replaceAll("[\\s_\\-./\\\\]+","");}

    private static long time(String raw,long fallback){
        String value=Analysis.digits(raw==null?"":raw).trim();if(value.isEmpty())return fallback;
        try{long n=Long.parseLong(value);if(n>100000000000L)return n;if(n>1000000000L)return n*1000L;if(n>20000&&n<100000)return LocalDate.of(1899,12,30).plusDays(n).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();}catch(Exception ignored){}
        try{return Instant.parse(value).toEpochMilli();}catch(Exception ignored){}
        try{return OffsetDateTime.parse(value).toInstant().toEpochMilli();}catch(Exception ignored){}
        String latin=value.replace(" ص"," AM").replace(" م"," PM");
        String[] patterns={"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm","yyyy/MM/dd HH:mm:ss","yyyy/MM/dd HH:mm","dd/MM/yyyy HH:mm:ss","dd/MM/yyyy HH:mm","d/M/yyyy H:mm","M/d/yyyy h:mm a","yyyy-MM-dd"};
        for(String pattern:patterns)try{DateTimeFormatter f=DateTimeFormatter.ofPattern(pattern,Locale.US);if(pattern.equals("yyyy-MM-dd"))return LocalDate.parse(latin,f).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();return LocalDateTime.parse(latin,f).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();}catch(Exception ignored){}
        return fallback;
    }

    static ArrayList<ArrayList<String>> parse(String text){
        char separator=separator(text);ArrayList<ArrayList<String>> rows=new ArrayList<>();ArrayList<String> row=new ArrayList<>();StringBuilder cell=new StringBuilder();boolean quoted=false;
        for(int i=0;i<text.length();i++){char c=text.charAt(i);if(i==0&&c=='\ufeff')continue;if(c=='"'){if(quoted&&i+1<text.length()&&text.charAt(i+1)=='"'){cell.append('"');i++;}else quoted=!quoted;}else if(c==separator&&!quoted){row.add(cell.toString().trim());cell.setLength(0);}else if((c=='\n'||c=='\r')&&!quoted){if(c=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;row.add(cell.toString().trim());cell.setLength(0);if(!empty(row))rows.add(row);row=new ArrayList<>();}else cell.append(c);}
        row.add(cell.toString().trim());if(!empty(row))rows.add(row);return rows;
    }
    private static char separator(String text){int comma=0,semi=0,tab=0;boolean quote=false;for(int i=0;i<text.length();i++){char c=text.charAt(i);if(c=='"')quote=!quote;else if(!quote&&(c=='\n'||c=='\r'))break;else if(!quote){if(c==',')comma++;else if(c==';')semi++;else if(c=='\t')tab++;}}return tab>comma&&tab>semi?'\t':semi>comma?';':',';}
}
