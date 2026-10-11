package com.amalaei.engineering;

import org.json.JSONObject;
import java.math.BigDecimal;

final class WhatsAppTemplates {
    static String fill(String template,JSONObject lead,JSONObject quote){
        String company=lead.optString("company").trim(); if(company.isEmpty())company=lead.optString("name");
        String amount="";String number="";
        try{if(quote!=null){number=quote.optString("number");amount=QuoteMath.money(Quotation.subtotal(quote).add(QuoteMath.tax(Quotation.subtotal(quote))));}}catch(Exception ignored){}
        return template.replace("{الاسم}",lead.optString("name")).replace("{الشركة}",company).replace("{رقم_العرض}",number).replace("{المبلغ}",amount);
    }
}
