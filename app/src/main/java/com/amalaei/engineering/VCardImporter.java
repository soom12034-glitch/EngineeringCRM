package com.amalaei.engineering;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

final class VCardImporter {
    static Map<String,String> parse(String text) {
        if(text==null||text.trim().isEmpty())throw new IllegalArgumentException("ملف جهات الاتصال فارغ");
        LinkedHashMap<String,String> contacts=new LinkedHashMap<>();Card card=new Card();String logical=null;boolean soft=false;int cards=0;
        for(String raw:text.split("\\r?\\n",-1)){
            if(soft){logical+=stripFold(raw);}else if((raw.startsWith(" ")||raw.startsWith("\t"))&&logical!=null){logical+=stripFold(raw);}else{if(logical!=null){cards+=consume(logical,card,contacts);}logical=relevant(raw)?raw:null;}
            soft=logical!=null&&logical.toUpperCase(Locale.ROOT).contains("ENCODING=QUOTED-PRINTABLE")&&logical.endsWith("=");if(soft)logical=logical.substring(0,logical.length()-1);
        }
        if(logical!=null)cards+=consume(logical,card,contacts);
        if(cards==0)throw new IllegalArgumentException("الملف ليس بصيغة VCF مدعومة");
        if(contacts.isEmpty())throw new IllegalArgumentException("لا يحتوي الملف أسماءً مرتبطة بأرقام سعودية صالحة");
        return contacts;
    }
    private static final class Card {String fn="",structured="";final LinkedHashSet<String> phones=new LinkedHashSet<>();void clear(){fn="";structured="";phones.clear();}}
    private static int consume(String line,Card card,Map<String,String> contacts){int colon=line.indexOf(':');String header=(colon<0?line:line.substring(0,colon)).toUpperCase(Locale.ROOT),key=header.split(";",2)[0];String value=colon<0?"":line.substring(colon+1);if(key.equals("BEGIN")){card.clear();return 0;}if(key.equals("FN")){card.fn=decode(header,value);return 0;}if(key.equals("N")){card.structured=decode(header,value);return 0;}if(key.equals("TEL")){String phone=Analysis.phone(decode(header,value));if(!phone.isEmpty())card.phones.add(phone);return 0;}if(!key.equals("END"))return 0;String name=card.fn.trim();if(name.isEmpty())name=structuredName(card.structured);if(name.length()>120)name=name.substring(0,120);if(!name.isEmpty()&&!CallCsvImporter.missingName(name))for(String phone:card.phones)contacts.putIfAbsent(phone,name);card.clear();return 1;}
    private static String structuredName(String value){String[] p=value.split(";",-1);StringBuilder b=new StringBuilder();int[] order={3,1,2,0,4};for(int i:order)if(i<p.length&&!p[i].trim().isEmpty()){if(b.length()>0)b.append(' ');b.append(p[i].trim());}return b.toString();}
    private static String decode(String header,String value){String decoded=header.contains("ENCODING=QUOTED-PRINTABLE")?quotedPrintable(value):value;return decoded.replace("\\n"," ").replace("\\N"," ").replace("\\,",",").replace("\\;",";").replace("\\\\","\\").trim();}
    private static String quotedPrintable(String value){ByteArrayOutputStream out=new ByteArrayOutputStream();for(int i=0;i<value.length();){char c=value.charAt(i);if(c=='='&&i+2<value.length()){int hi=Character.digit(value.charAt(i+1),16),lo=Character.digit(value.charAt(i+2),16);if(hi>=0&&lo>=0){out.write((hi<<4)|lo);i+=3;continue;}}byte[] bytes=String.valueOf(c).getBytes(StandardCharsets.UTF_8);out.write(bytes,0,bytes.length);i++;}return new String(out.toByteArray(),StandardCharsets.UTF_8);}
    private static String stripFold(String value){return value.startsWith(" ")||value.startsWith("\t")?value.substring(1):value;}
    private static boolean relevant(String line){String upper=line.toUpperCase(Locale.ROOT);return upper.startsWith("BEGIN:")||upper.startsWith("END:")||upper.startsWith("FN:")||upper.startsWith("FN;")||upper.startsWith("N:")||upper.startsWith("N;")||upper.startsWith("TEL:")||upper.startsWith("TEL;");}
}
