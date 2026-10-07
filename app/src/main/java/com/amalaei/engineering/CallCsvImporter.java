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
        ArrayList<String> first=records.get(0);boolean header=hasHeader(first);
        int phone=header?column(first,"number","phone","mobile","tel","رقم","الهاتف","الجوال","هاتف","رقمالهاتف","رقمالمكالمة"):inferPhone(first);
        int name=header?column(first,"name","contact","contactname","displayname","الاسم","جهةالاتصال","اسمالمتصل"):inferName(first,phone);
        int date=header?column(first,"date","time","timestamp","datetime","calldate","التاريخ","الوقت","تاريخالمكالمة"):inferDate(first,phone);
        if(phone<0)throw new IllegalArgumentException("تعذر تحديد عمود رقم الهاتف. استخدم عنوان number أو phone أو رقم الهاتف");
        int imported=0,created=0,skipped=0,start=header?1:0;long fallback=System.currentTimeMillis();
        try(Store store=new Store(context)){
            store.beginBatch();
            try{
                for(int i=start;i<records.size();i++){
                    ArrayList<String> row=records.get(i);if(empty(row))continue;
                    String normalized=phone<row.size()?Analysis.phone(row.get(phone)):"";
                    if(normalized.isEmpty()){skipped++;continue;}
                    String contact=name>=0&&name<row.size()?safe(row.get(name),120):"";
                    long at=date>=0&&date<row.size()?time(row.get(date)):Long.MIN_VALUE;
                    if(at==Long.MIN_VALUE)at=fallback-i;
                    boolean fresh=store.byPhone(normalized).optLong("id")==0;
                    store.recordCall(normalized,at,contact,AppMode.MODE);
                    if(fresh)created++;imported++;
                }
                store.commitBatch();
            }catch(Exception e){store.rollbackBatch();throw e;}
        }
        if(imported==0)throw new IllegalArgumentException("لا يحتوي الملف أرقامًا سعودية صالحة. يدعم 05 و01 و+966");
        return new Result(records.size()-start,imported,created,skipped);
    }

    private static String safe(String value,int max){String v=value==null?"":value.trim();return v.length()>max?v.substring(0,max):v;}
    private static boolean empty(ArrayList<String> row){for(String v:row)if(v!=null&&!v.trim().isEmpty())return false;return true;}
    static boolean hasHeader(ArrayList<String> row){return column(row,"number","phone","mobile","tel","رقم","الهاتف","الجوال","هاتف","رقمالهاتف","رقمالمكالمة")>=0||column(row,"name","contact","contactname","displayname","الاسم","جهةالاتصال","اسمالمتصل")>=0||column(row,"date","time","timestamp","datetime","calldate","التاريخ","الوقت","تاريخالمكالمة")>=0;}
    static int inferPhone(ArrayList<String> row){for(int i=0;i<row.size();i++)if(!Analysis.phone(row.get(i)).isEmpty())return i;return -1;}
    static int inferName(ArrayList<String> row,int phone){for(int i=0;i<row.size();i++){if(i==phone)continue;String v=row.get(i)==null?"":row.get(i).trim();if(!v.isEmpty()&&Analysis.phone(v).isEmpty()&&time(v)==Long.MIN_VALUE)return i;}return -1;}
    static int inferDate(ArrayList<String> row,int phone){for(int i=0;i<row.size();i++){if(i==phone)continue;String v=row.get(i)==null?"":row.get(i).trim();if(v.isEmpty()||!Analysis.phone(v).isEmpty())continue;if(time(v)!=Long.MIN_VALUE)return i;}return -1;}
    static int column(ArrayList<String> headers,String... names){
        String[] keys=new String[headers.size()];for(int i=0;i<keys.length;i++)keys[i]=key(headers.get(i));
        for(String name:names){String k=key(name);for(int i=0;i<keys.length;i++)if(keys[i].equals(k))return i;}
        for(String name:names){String k=key(name);for(int i=0;i<keys.length;i++)if(keys[i].contains(k))return i;}
        return -1;
    }
    private static String key(String s){return Analysis.digits(s==null?"":s).toLowerCase(Locale.ROOT).replace('أ','ا').replace('إ','ا').replace('آ','ا').replace('ى','ي').replace('ة','ه').replaceAll("[\\s_\\-./\\\\]+","");}

static long time(String raw){
        String text=latin(raw==null?"":raw).trim();if(text.isEmpty())return Long.MIN_VALUE;
        if(text.matches("[0-9]+")){
            try{long n=Long.parseLong(text);
                if(n>=100000000000L){long year=Instant.ofEpochMilli(n).atZone(ZoneId.systemDefault()).getYear();if(year>=2000&&year<=2100)return n;}
                else if(n>=1000000000L)return n*1000L;
                else if(n>=20000&&n<100000)return LocalDate.of(1899,12,30).plusDays(n).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }catch(Exception ignored){}
        }
        try{return Instant.parse(text).toEpochMilli();}catch(Exception ignored){}
        try{return OffsetDateTime.parse(text).toInstant().toEpochMilli();}catch(Exception ignored){}
        String marked=text.replace("ص"," AM").replace("م"," PM").replaceAll("\\s+"," ").trim();
        String[] patterns={"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm","dd/MM/yyyy HH:mm:ss","dd/MM/yyyy HH:mm","d/M/yyyy H:mm","yyyy/MM/dd HH:mm:ss","yyyy/MM/dd HH:mm","M/d/yyyy h:mm a","yyyy-MM-dd h:mm a","dd/MM/yyyy h:mm a","yyyy/MM/dd","yyyyMMddHHmmss","yyyyMMddHHmm","yyyyMMdd","ddMMyyyyHHmm","ddMMyyyy","yyyy-MM-dd"};
        for(String pattern:patterns)try{boolean dateOnly=!pattern.matches(".*[Hh:a].*");DateTimeFormatter f=DateTimeFormatter.ofPattern(pattern,Locale.US);if(dateOnly)return LocalDate.parse(marked,f).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();return LocalDateTime.parse(marked,f).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();}catch(Exception ignored){}
        return Long.MIN_VALUE;
    }
    private static String latin(String raw){StringBuilder b=new StringBuilder(raw.length());for(int i=0;i<raw.length();i++){char c=raw.charAt(i);if(c>='٠'&&c<='٩')b.append((char)('0'+c-'٠'));else if(c>='۰'&&c<='۹')b.append((char)('0'+c-'۰'));else b.append(c);}return b.toString();}

    static ArrayList<ArrayList<String>> parse(String text){
        char separator=separator(text);ArrayList<ArrayList<String>> rows=new ArrayList<>();ArrayList<String> row=new ArrayList<>();StringBuilder cell=new StringBuilder();boolean quoted=false;
        for(int i=0;i<text.length();i++){char c=text.charAt(i);if(i==0&&c=='\ufeff')continue;if(c=='"'){if(quoted&&i+1<text.length()&&text.charAt(i+1)=='"'){cell.append('"');i++;}else quoted=!quoted;}else if(c==separator&&!quoted){row.add(cell.toString().trim());cell.setLength(0);}else if((c=='\n'||c=='\r')&&!quoted){if(c=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;row.add(cell.toString().trim());cell.setLength(0);if(!empty(row))rows.add(row);row=new ArrayList<>();}else cell.append(c);}
        if(quoted)throw new IllegalArgumentException("علامات الاقتباس غير متوازنة في الملف؛ صحّح الملف وأعد المحاولة");
        row.add(cell.toString().trim());if(!empty(row))rows.add(row);return rows;
    }
    private static char separator(String text){int comma=0,semi=0,tab=0;boolean quote=false;for(int i=0;i<text.length();i++){char c=text.charAt(i);if(c=='"')quote=!quote;else if(!quote&&(c=='\n'||c=='\r'))break;else if(!quote){if(c==',')comma++;else if(c==';')semi++;else if(c=='\t')tab++;}}return tab>comma&&tab>semi?'\t':semi>comma?';':',';}
}
