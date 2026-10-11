package com.amalaei.engineering;

import org.json.JSONObject;
import java.time.LocalDate;

/** Status is deliberately part of quote JSON so existing databases need no migration. */
final class QuoteStatus {
    static final String DRAFT="draft", SENT="sent", ACCEPTED="accepted", REJECTED="rejected";
    static final String[] KEYS={DRAFT,SENT,ACCEPTED,REJECTED};
    static final String[] LABELS={"مسودة","مرسل","مقبول","مرفوض"};
    static boolean valid(String value){for(String key:KEYS)if(key.equals(value))return true;return false;}
    static String value(JSONObject quote){String value=quote.optString("status",DRAFT);return valid(value)?value:DRAFT;}
    static String label(JSONObject quote){return LABELS[index(value(quote))];}
    static int index(String value){for(int i=0;i<KEYS.length;i++)if(KEYS[i].equals(value))return i;return 0;}
    static boolean expired(JSONObject quote){try{return LocalDate.parse(quote.getString("date")).plusDays(Quotation.validDays(quote)).isBefore(LocalDate.now());}catch(Exception e){return false;}}
    static String display(JSONObject quote){return expired(quote)?"منتهي":label(quote);}
}
