package com.amalaei.engineering;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.json.JSONObject;

final class LedgerMath {
    static BigDecimal amount(String raw) {
        try {
            String value=raw==null?"":raw.trim().replace(",","");
            BigDecimal number=new BigDecimal(value.isEmpty()?"0":value);
            if(number.signum()<=0||number.scale()>2||number.compareTo(new BigDecimal("100000000"))>0)throw new NumberFormatException();
            return number.setScale(2, RoundingMode.HALF_UP);
        } catch(Exception invalid) { throw new IllegalArgumentException("أدخل مبلغًا موجبًا صحيحًا حتى خانتين عشريتين"); }
    }
    static String normal(String raw){if(raw==null||raw.trim().isEmpty())return "0";return amount(raw).stripTrailingZeros().toPlainString();}
    static BigDecimal opening(JSONObject book){try{return new BigDecimal(book.optString("openingBalance","0"));}catch(Exception ignored){return BigDecimal.ZERO;}}
    static BigDecimal total(List<JSONObject> entries,String kind){BigDecimal total=BigDecimal.ZERO;for(JSONObject item:entries)if(kind.equals(item.optString("kind")))try{total=total.add(new BigDecimal(item.optString("amount","0")));}catch(Exception ignored){}return total;}
    static BigDecimal collections(List<JSONObject> entries,String method){BigDecimal total=BigDecimal.ZERO;for(JSONObject item:entries)if("in".equals(item.optString("kind"))&&method.equals(item.optString("paymentMethod","cash")))try{total=total.add(new BigDecimal(item.optString("amount","0")));}catch(Exception ignored){}return total;}
    static BigDecimal totalCollections(List<JSONObject> entries){return collections(entries,"cash").add(collections(entries,"network")).add(collections(entries,"transfer"));}
    /** Cash actually held by the representative: cash collections only, less expenses. */
    static BigDecimal balance(JSONObject book,List<JSONObject> entries){return opening(book).add(collections(entries,"cash")).subtract(total(entries,"out"));}
    static BigDecimal netActivity(List<JSONObject> entries){return totalCollections(entries).subtract(total(entries,"out"));}
    static String methodLabel(String method){if("network".equals(method))return "شبكة";if("transfer".equals(method))return "تحويل";return "كاش";}
    static String money(BigDecimal value){return value.setScale(2,RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()+" ر.س";}
    static String number(BigDecimal value){return value.setScale(2,RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();}
}
