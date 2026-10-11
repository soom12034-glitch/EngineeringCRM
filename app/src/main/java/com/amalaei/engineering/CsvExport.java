package com.amalaei.engineering;

import org.json.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

final class CsvExport {
    static String cell(String value){String v=value==null?"":value.replace("\r"," ").replace("\n"," ");if(!v.isEmpty()&&"=+-@".indexOf(v.charAt(0))>=0)v="'"+v;return "\""+v.replace("\"","\"\"")+"\"";}
    static byte[] file(String[] headers,ArrayList<String[]> rows){StringBuilder out=new StringBuilder("\uFEFF");line(out,headers);for(String[] row:rows)line(out,row);return out.toString().getBytes(StandardCharsets.UTF_8);}
    private static void line(StringBuilder out,String[] values){for(int i=0;i<values.length;i++){if(i>0)out.append(',');out.append(cell(values[i]));}out.append("\r\n");}
    static byte[] customers(ArrayList<JSONObject> leads){ArrayList<String[]> rows=new ArrayList<>();for(JSONObject l:leads)rows.add(new String[]{l.optString("name"),l.optString("company"),l.optString("phone"),l.optString("email"),l.optString("business"),l.optString("requestType"),String.valueOf(l.optInt("stage")),l.optString("followUp")});return file(new String[]{"الاسم","الشركة","الهاتف","البريد","النشاط","نوع الطلب","المرحلة","المتابعة"},rows);}
    static byte[] quotes(ArrayList<JSONObject> quotes){ArrayList<String[]> rows=new ArrayList<>();for(JSONObject q:quotes){String total="";try{total=QuoteMath.money(Quotation.subtotal(q).add(QuoteMath.tax(Quotation.subtotal(q))));}catch(Exception ignored){}rows.add(new String[]{q.optString("number"),q.optString("customer"),QuoteStatus.display(q),q.optString("date"),total});}return file(new String[]{"رقم العرض","العميل","الحالة","التاريخ","الإجمالي شامل الضريبة"},rows);}
    static byte[] ledger(ArrayList<JSONObject> entries){ArrayList<String[]> rows=new ArrayList<>();for(JSONObject e:entries)rows.add(new String[]{e.optString("title"),e.optString("kind").equals("in")?"وارد":"مصروف",e.optString("amount"),String.valueOf(e.optLong("at")),e.optString("note")});return file(new String[]{"البيان","النوع","المبلغ","التاريخ","ملاحظة"},rows);}
}
