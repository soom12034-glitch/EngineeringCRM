package com.amalaei.engineering;
import java.net.URI;
final class Profiles {
    static final String[][] FIELDS={
        {"name","الاسم الرسمي للمنشأة"},{"brand","الاسم المختصر للدعاية (اختياري)"},{"address","العنوان"},{"nationalAddress","العنوان الوطني (اختياري)"},{"phones","أرقام التواصل — رقم في كل سطر"},{"whatsapp","رقم واتساب المنشأة"},{"email","البريد الإلكتروني"},{"website","الموقع الإلكتروني"},{"maps","رابط الموقع على الخرائط"},{"facebook","صفحة فيسبوك"},{"instagram","صفحة إنستغرام"},{"snapchat","سناب شات"},{"tiktok","تيك توك"},{"x","صفحة X"},{"linkedin","لينكدإن"},{"youtube","يوتيوب"},{"registration","السجل التجاري (اختياري)"},{"vat","الرقم الضريبي (اختياري)"}
    };
    static final String[] LINKS={"website","maps","facebook","instagram","snapchat","tiktok","x","linkedin","youtube"};
    static final String[] AD_FIELDS={"address","phones","whatsapp","email","website","maps","facebook","instagram","snapchat","tiktok","x","linkedin","youtube"};
    static String label(String key){for(String[] pair:FIELDS)if(pair[0].equals(key))return pair[1].replace(" — رقم في كل سطر","");return key;}
    static String url(String value){value=value.trim();if(value.isEmpty())return "";try{URI u=new URI(value.contains("://")?value:"https://"+value);if(!("https".equalsIgnoreCase(u.getScheme())||"http".equalsIgnoreCase(u.getScheme()))||u.getHost()==null||u.getUserInfo()!=null)throw new IllegalArgumentException();return u.toASCIIString();}catch(Exception e){throw new IllegalArgumentException("أدخل رابطًا صحيحًا يبدأ بـ https://");}}
    static String firstPhone(String value){String[] lines=value.trim().split("\\r?\\n");return lines.length==0?"":lines[0].trim();}
}
