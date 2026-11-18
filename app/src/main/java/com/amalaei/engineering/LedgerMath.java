package com.amalaei.engineering;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
    static BigDecimal value(JSONObject entry){try{return new BigDecimal(entry.optString("amount","0"));}catch(Exception ignored){return BigDecimal.ZERO;}}
    static BigDecimal total(List<JSONObject> entries,String kind){BigDecimal total=BigDecimal.ZERO;for(JSONObject item:entries)if(kind.equals(item.optString("kind")))total=total.add(value(item));return total;}
    static BigDecimal collections(List<JSONObject> entries,String method){BigDecimal total=BigDecimal.ZERO;for(JSONObject item:entries)if("in".equals(item.optString("kind"))&&method.equals(item.optString("paymentMethod","cash")))total=total.add(value(item));return total;}
    static BigDecimal totalCollections(List<JSONObject> entries){return collections(entries,"cash").add(collections(entries,"network")).add(collections(entries,"transfer"));}
    /** Cash actually held by the representative: cash collections only, less expenses. */
    static BigDecimal balance(JSONObject book,List<JSONObject> entries){return opening(book).add(collections(entries,"cash")).subtract(total(entries,"out"));}
    static BigDecimal netActivity(List<JSONObject> entries){return totalCollections(entries).subtract(total(entries,"out"));}

    /** How one entry moves the representative's cash: cash collections add, expenses subtract, card and transfer collections go straight to the company. */
    static BigDecimal cashEffect(JSONObject entry){
        if("out".equals(entry.optString("kind")))return value(entry).negate();
        return "cash".equals(entry.optString("paymentMethod","cash"))?value(entry):BigDecimal.ZERO;
    }
    /** Cash balance carried into a period that starts at {@code from}. */
    static BigDecimal balanceBefore(JSONObject book,List<JSONObject> entries,long from){
        BigDecimal balance=opening(book);
        for(JSONObject entry:entries)if(entry.optLong("at")<from)balance=balance.add(cashEffect(entry));
        return balance;
    }
    static final Comparator<JSONObject> OLDEST_FIRST=Comparator.comparingLong((JSONObject entry)->entry.optLong("at")).thenComparingLong(entry->entry.optLong("id"));
    static ArrayList<JSONObject> chronological(List<JSONObject> entries){ArrayList<JSONObject> copy=new ArrayList<>(entries);copy.sort(OLDEST_FIRST);return copy;}
    /** Running cash balance after each entry, for entries already in chronological order. */
    static BigDecimal[] running(BigDecimal start,List<JSONObject> chronological){
        BigDecimal[] balances=new BigDecimal[chronological.size()];BigDecimal balance=start;
        for(int i=0;i<balances.length;i++){balance=balance.add(cashEffect(chronological.get(i)));balances[i]=balance;}
        return balances;
    }

    static String methodLabel(String method){if("network".equals(method))return "شبكة";if("transfer".equals(method))return "تحويل";return "كاش";}
    static String money(BigDecimal value){return value.setScale(2,RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()+" ر.س";}
    static String number(BigDecimal value){return value.setScale(2,RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();}
    /** Accounting style: thousands separators and two decimals, always Latin digits. */
    static String grouped(BigDecimal value){DecimalFormat format=new DecimalFormat("#,##0.00",DecimalFormatSymbols.getInstance(Locale.US));return format.format(value.setScale(2,RoundingMode.HALF_UP));}
}
