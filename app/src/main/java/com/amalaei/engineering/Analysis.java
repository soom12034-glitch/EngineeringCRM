package com.amalaei.engineering;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.*;
final class Analysis {
    String phone="",interest="",business="",followUp="",followTime="",email="",name="",company="";
    final LinkedHashMap<String,String> details=new LinkedHashMap<>();
    final ArrayList<String> phones=new ArrayList<>();
    static String digits(String raw){StringBuilder b=new StringBuilder();for(char c:raw.toCharArray()){int d=Character.digit(c,10);b.append(d>=0?(char)('0'+d):c);}return b.toString();}
    static String phone(String raw){if(raw==null)return "";String p=digits(raw).replaceAll("[^0-9+]","");if(p.matches("0[15][0-9]{8}"))p="+966"+p.substring(1);else if(p.matches("00966[0-9]{9}"))p="+"+p.substring(2);else if(p.matches("966[0-9]{9}"))p="+"+p;return p.matches("\\+966[0-9]{9}")?p:"";}
    static ArrayList<String> phones(String body){LinkedHashSet<String> set=new LinkedHashSet<>();String d=digits(body);Matcher m=Pattern.compile("(?<![0-9])(?:\\+966|00966|966)[ ()-]*[0-9](?:[ ()-]*[0-9]){8}(?![ ()-]*[0-9])|(?<![0-9])0[15](?:[ ()-]*[0-9]){8}(?![ ()-]*[0-9])").matcher(d);while(m.find()){String p=phone(m.group());if(!p.isEmpty())set.add(p);}return new ArrayList<>(set);}
    static String labeled(String body,String alternatives){Matcher m=Pattern.compile("(?im)^\\s*(?:"+alternatives+")\\s*[:：]\\s*([^\\n]{1,180})").matcher(body);return m.find()?m.group(1).trim():"";}
    static void put(Analysis a,String key,String value){if(!value.isEmpty())a.details.put(key,value);}
    static Analysis inspect(String body,long at){
        Analysis r=new Analysis();String d=digits(body);String n=d.replace('أ','ا').replace('إ','ا').replace('آ','ا').replace('ى','ي').toLowerCase(Locale.ROOT);
        r.phones.addAll(phones(body));if(r.phones.size()==1)r.phone=r.phones.get(0);
        Matcher e=Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}").matcher(body);if(e.find())r.email=e.group();
        r.name=labeled(body,"اسم العميل|الاسم|اسمي|انا|أنا|name");r.company=labeled(body,"اسم الشركة|الشركة|المؤسسة|company");
        String[] survey={"توتال","total station","gps","gnss","جي بي اس","اوتوليفل","مساحة","معايرة","ميريديان","meridian"};String[] software={"برنامج","برامج","تطبيق","اشتراك","erp","منصة","سحابي","تطوير","enginex","bluemax"};boolean eq=false,app=false;
        for(String w:survey)if(n.contains(w)){eq=true;r.interest+=(r.interest.isEmpty()?"":"، ")+w;}for(String w:software)if(n.contains(w)){app=true;r.interest+=(r.interest.isEmpty()?"":"، ")+w;}if(eq!=app)r.business=eq?"survey":"software";
        String explicit=labeled(body,"الاهتمام|المطلوب|الطلب|احتياج العميل");if(!explicit.isEmpty())r.interest=explicit;
        LocalDate date=java.time.Instant.ofEpochMilli(at).atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        if(n.contains("غدا")||n.contains("بكره")||n.contains("بكرة"))r.followUp=date.plusDays(1).toString();else if(n.contains("اليوم"))r.followUp=date.toString();
        Matcher dates=Pattern.compile("\\b20[0-9]{2}-[0-9]{2}-[0-9]{2}\\b").matcher(d);while(dates.find()){int start=d.lastIndexOf('\n',dates.start())+1;String prefix=d.substring(start,dates.start());if(prefix.matches(".*(?:موعد الشراء|تجديد المعايرة|موعد العرض التجريبي|انتهاء الاشتراك).*"))continue;try{r.followUp=LocalDate.parse(dates.group()).toString();break;}catch(Exception ignored){}}
        Matcher time=Pattern.compile("(?:الساعة|الساعه|وقت المتابعة|الوقت)\\s*[:：]?\\s*([01]?[0-9]|2[0-3]):([0-5][0-9])(?:\\s*(صباحا|صباحًا|مساء|مساءً|ص|م))?(?![0-9])").matcher(n);if(time.find()){int h=Integer.parseInt(time.group(1));String suffix=time.group(3);if(suffix!=null&&(suffix.startsWith("م"))&&h<12)h+=12;if(suffix!=null&&suffix.startsWith("ص")&&h==12)h=0;r.followTime=String.format(Locale.US,"%02d:%s",h,time.group(2));}
        put(r,"model",labeled(body,"الموديل|موديل|model"));if(!r.details.containsKey("model")){Matcher model=Pattern.compile("(?i)\\b(?:M25|MS70|M35|TCR[0-9]{3,5}|TS[0-9]{2}|RD8200)\\b").matcher(d);if(model.find())put(r,"model",model.group().toUpperCase(Locale.ROOT));}
        put(r,"device",labeled(body,"نوع الجهاز|الجهاز"));put(r,"service",labeled(body,"الخدمة|نوع الخدمة"));put(r,"condition",labeled(body,"الحالة|حالة الجهاز"));
        put(r,"program",labeled(body,"البرنامج|اسم البرنامج"));put(r,"platform",labeled(body,"المنصة|نوع البرنامج"));put(r,"needs",labeled(body,"الاحتياجات|احتياجات العميل"));put(r,"plan",labeled(body,"الباقة|الخطة"));
        Matcher budget=Pattern.compile("(?:الميزانية|ميزانيتي|ميزانية|budget)\\s*[:：]?\\s*([0-9]+(?:[.,][0-9]+)?)").matcher(n);if(budget.find())put(r,"budget",budget.group(1).matches("[0-9]+(?:,[0-9]{3})+")?budget.group(1).replace(",",""):budget.group(1).replace(',','.'));
        Matcher quantity=Pattern.compile("(?:الكمية|كمية|عدد الاجهزة|عدد أجهزة)\\s*[:：]?\\s*([0-9]+)").matcher(n);if(quantity.find())try{if(Integer.parseInt(quantity.group(1))>0)put(r,"quantity",quantity.group(1));}catch(Exception ignored){}
        for(String[] pair:new String[][]{{"purchaseDate","موعد الشراء"},{"calibrationDate","تجديد المعايرة"},{"demoDate","موعد العرض التجريبي"},{"subscriptionEnd","انتهاء الاشتراك"}}){String value=labeled(d,pair[1]);if(!value.isEmpty())try{put(r,pair[0],LocalDate.parse(value).toString());}catch(Exception ignored){}}
        return r;
    }
}
