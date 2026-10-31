package com.amalaei.engineering;

import android.content.Context;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

final class CallCsvImporter {
    static final class Result {
        final int rows, imported, created, skipped, named, readableContacts;
        final boolean contactsPermission;
        Result(int rows,int imported,int created,int skipped,int named,int readableContacts,boolean contactsPermission){this.rows=rows;this.imported=imported;this.created=created;this.skipped=skipped;this.named=named;this.readableContacts=readableContacts;this.contactsPermission=contactsPermission;}
        String message(){String base="اكتمل الاستيراد: "+imported+" مكالمة صالحة، "+created+" رقمًا جديدًا للمراجعة، و"+skipped+" صف متجاوز من أصل "+rows+". لا يُعتمد أي رقم كعميل تلقائيًا.";if(named>0)return base+" تمت مطابقة أسماء "+named+" رقمًا من جهات اتصال الهاتف.";if(!contactsPermission)return base+" لم تُقرأ الأسماء لأن إذن جهات الاتصال غير مسموح؛ فعّله من إعدادات الهاتف ثم اختر «تحديث الأسماء من جهات الاتصال».";if(readableContacts==0)return base+" منح الإذن موجود، لكن Android لم يُرجع جهات اتصال قابلة للقراءة. تحقق أن الأسماء محفوظة في تطبيق جهات الاتصال وليست تعريف متصل من الشبكة فقط.";return base+" قرأ التطبيق "+readableContacts+" جهة اتصال، لكن لم تتطابق أرقامها السعودية مع الأرقام المستوردة.";}
    }

    static Result run(Context context,String csv) throws Exception {
        if(csv==null||csv.trim().isEmpty())throw new IllegalArgumentException("ملف سجل المكالمات فارغ");
        ArrayList<ArrayList<String>> records=parse(csv);
        if(records.isEmpty())throw new IllegalArgumentException("لم يُعثر على صفوف في الملف");
        int headerRow=headerRow(records),start=headerRow<0?0:headerRow+1;ArrayList<String> first=records.get(headerRow<0?firstDataRow(records):headerRow);
        int phone=headerRow>=0?column(first,"number","phone","mobile","telephone","tel","caller","address","رقم","الهاتف","الجوال","هاتف","رقمالهاتف","رقمالمكالمة","رقمالمتصل"):inferPhone(first);
        phone=resolvePhoneColumn(records,start,phone);
        int name=headerRow>=0?column(first,"name","contact","contactname","displayname","callername","cachedname","cachedcallername","الاسم","جهةالاتصال","اسمالمتصل","الاسمالمخزن"):inferName(first,phone);
        int date=headerRow>=0?column(first,"date","time","timestamp","datetime","calldate","starttime","التاريخ","الوقت","تاريخالمكالمة"):inferDate(first,phone);
        if(phone<0)throw new IllegalArgumentException("لم أتعرف على أرقام سعودية داخل الملف. تأكد أن السجل يحتوي أرقامًا تبدأ بـ 05 أو 01 أو +966");
        int imported=0,created=0,skipped=0;long fallback=System.currentTimeMillis();boolean contactsPermission=PhoneData.readable(context);Map<String,String> deviceContacts=PhoneData.contacts(context);int readableContacts=deviceContacts.size();Set<String> matchedContacts=new LinkedHashSet<>(),lookedUp=new HashSet<>();
        try(Store store=new Store(context)){
            Map<String,String> phoneContacts=new LinkedHashMap<>(store.cachedContactNames());phoneContacts.putAll(deviceContacts);
            store.beginBatch();
            try{
                for(int i=start;i<records.size();i++){
                    ArrayList<String> row=records.get(i);if(empty(row))continue;
                    String normalized=phone<row.size()?phoneValue(row.get(phone)):"";
                    if(normalized.isEmpty()){skipped++;continue;}
                    String contact=name>=0&&name<row.size()?safe(row.get(name),120):"";if(missingName(contact)){contact=phoneContacts.getOrDefault(normalized,"");if(contact.isEmpty()&&contactsPermission&&lookedUp.add(normalized)){contact=PhoneData.contact(context,normalized);if(!contact.isEmpty())phoneContacts.put(normalized,contact);}if(!contact.isEmpty())matchedContacts.add(normalized);}
                    long at=date>=0&&date<row.size()?time(row.get(date)):Long.MIN_VALUE;
                    if(at==Long.MIN_VALUE)at=fallback-i;
                    boolean fresh=store.byPhone(normalized).optLong("id")==0;
                    store.recordCall(normalized,at,contact,"");
                    if(fresh)created++;imported++;
                }
                store.commitBatch();
            }catch(Exception e){store.rollbackBatch();throw e;}
        }
        if(imported==0)throw new IllegalArgumentException("لا يحتوي الملف أرقامًا سعودية صالحة. يدعم 05 و01 و+966");
        return new Result(Math.max(0,records.size()-start),imported,created,skipped,matchedContacts.size(),readableContacts,contactsPermission);
    }

    private static String safe(String value,int max){String v=value==null?"":value.trim();return v.length()>max?v.substring(0,max):v;}
    static boolean missingName(String value){String v=value==null?"":value.trim();if(v.isEmpty()||!phoneValue(v).isEmpty())return true;String k=key(v);return k.equals("unknown")||k.equals("unknowncaller")||k.equals("noname")||k.equals("غيرمعروف")||k.equals("رقمغيرمعروف")||k.equals("عميلغيرمعرف")||k.equals("عميلغيرمسمي");}
    private static boolean empty(ArrayList<String> row){for(String v:row)if(v!=null&&!v.trim().isEmpty())return false;return true;}
    static boolean hasHeader(ArrayList<String> row){if(inferPhone(row)>=0)return false;for(String v:row)if(time(v)!=Long.MIN_VALUE)return false;return column(row,"number","phone","mobile","telephone","tel","caller","address","رقم","الهاتف","الجوال","هاتف","رقمالهاتف","رقمالمكالمة","رقمالمتصل")>=0||column(row,"name","contact","contactname","displayname","callername","cachedname","cachedcallername","الاسم","جهةالاتصال","اسمالمتصل","الاسمالمخزن")>=0||column(row,"date","time","timestamp","datetime","calldate","starttime","التاريخ","الوقت","تاريخالمكالمة")>=0;}
    static int headerRow(ArrayList<ArrayList<String>> rows){for(int i=0;i<Math.min(10,rows.size());i++){if(inferPhone(rows.get(i))>=0)return -1;if(hasHeader(rows.get(i)))return i;}return -1;}
    static int firstDataRow(ArrayList<ArrayList<String>> rows){for(int i=0;i<rows.size();i++)if(inferPhone(rows.get(i))>=0)return i;return 0;}
    static String phoneValue(String value){String direct=Analysis.phone(value);if(!direct.isEmpty())return direct;ArrayList<String> found=Analysis.phones(value==null?"":value);return found.size()==1?found.get(0):"";}
    static int inferPhone(ArrayList<String> row){for(int i=0;i<row.size();i++)if(!phoneValue(row.get(i)).isEmpty())return i;return -1;}
    private static int phoneScore(ArrayList<ArrayList<String>> rows,int start,int column){int score=0;for(int i=start;i<Math.min(rows.size(),start+200);i++){ArrayList<String> row=rows.get(i);if(column<row.size()&&!phoneValue(row.get(column)).isEmpty())score++;}return score;}
    static int inferPhoneColumn(ArrayList<ArrayList<String>> rows,int start){int columns=0;for(int i=start;i<Math.min(rows.size(),start+200);i++)columns=Math.max(columns,rows.get(i).size());int best=-1,bestScore=0;for(int column=0;column<columns;column++){int score=phoneScore(rows,start,column);if(score>bestScore){best=column;bestScore=score;}}return best;}
    static int resolvePhoneColumn(ArrayList<ArrayList<String>> rows,int start,int candidate){return candidate>=0&&phoneScore(rows,start,candidate)>0?candidate:inferPhoneColumn(rows,start);}
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
                else if(n>=1000000000L){long year=Instant.ofEpochSecond(n).atZone(ZoneId.systemDefault()).getYear();if(year>=2000&&year<=2100)return n*1000L;}
                else if(n>=20000&&n<100000)return LocalDate.of(1899,12,30).plusDays(n).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }catch(Exception ignored){}
        }
        try{return Instant.parse(text).toEpochMilli();}catch(Exception ignored){}
        try{return OffsetDateTime.parse(text).toInstant().toEpochMilli();}catch(Exception ignored){}
        String marked=text.replace("ص"," AM").replace("م"," PM").replaceAll("\\s+"," ").trim();
        String[] patterns={"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm","dd/MM/yyyy HH:mm:ss","dd/MM/yyyy HH:mm","d/M/yyyy H:mm","yyyy/MM/dd HH:mm:ss","yyyy/MM/dd HH:mm","d/M/yyyy h:mm a","M/d/yyyy h:mm a","yyyy-MM-dd h:mm a","yyyy/MM/dd","yyyyMMddHHmmss","yyyyMMddHHmm","yyyyMMdd","ddMMyyyyHHmm","ddMMyyyy","yyyy-MM-dd"};
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
    private static char separator(String text){int comma=0,semi=0,tab=0,lines=0;boolean quote=false;for(int i=0;i<text.length()&&lines<10;i++){char c=text.charAt(i);if(c=='"')quote=!quote;else if(!quote&&(c=='\n'||(c=='\r'&&(i+1==text.length()||text.charAt(i+1)!='\n'))))lines++;else if(!quote){if(c==',')comma++;else if(c==';')semi++;else if(c=='\t')tab++;}}return tab>comma&&tab>semi?'\t':semi>comma?';':',';}
}
