package com.amalaei.engineering;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;

/** What the ledger screen is showing: search text, entry type, period and order. */
final class LedgerFilter {
    static final String ALL="all",IN="in",OUT="out",CASH="cash",NETWORK="network",TRANSFER="transfer",MARKED="marked";
    static final String PERIOD_ALL="all",PERIOD_TODAY="today",PERIOD_MONTH="month",PERIOD_LAST_MONTH="lastMonth",PERIOD_CUSTOM="custom";

    String query="";
    String type=ALL;
    String period=PERIOD_ALL;
    long from=Long.MIN_VALUE;
    long to=Long.MAX_VALUE;
    boolean newestFirst=true;

    boolean hasPeriod(){return from!=Long.MIN_VALUE||to!=Long.MAX_VALUE;}
    boolean narrowsEntries(){return !query.trim().isEmpty()||!ALL.equals(type);}
    boolean active(){return hasPeriod()||narrowsEntries();}
    void reset(){query="";type=ALL;setPeriod(PERIOD_ALL,0,0,System.currentTimeMillis());newestFirst=true;}

    boolean inPeriod(JSONObject entry){long at=entry.optLong("at");return at>=from&&at<=to;}

    boolean matches(JSONObject entry){
        if(!inPeriod(entry))return false;
        boolean in="in".equals(entry.optString("kind"));String method=entry.optString("paymentMethod","cash");
        switch(type){
            case IN: if(!in)return false; break;
            case OUT: if(in)return false; break;
            case CASH: case NETWORK: case TRANSFER: if(!in||!type.equals(method))return false; break;
            case MARKED: if(entry.optString("marker").trim().isEmpty())return false; break;
            default: break;
        }
        String needle=normalize(query);
        if(needle.isEmpty())return true;
        String haystack=normalize(entry.optString("title")+" "+entry.optString("note")+" "+entry.optString("marker")+" "+entry.optString("amount"));
        for(String word:needle.split(" "))if(!word.isEmpty()&&!haystack.contains(word))return false;
        return true;
    }

    ArrayList<JSONObject> apply(List<JSONObject> entries){
        ArrayList<JSONObject> result=new ArrayList<>();
        for(JSONObject entry:entries)if(matches(entry))result.add(entry);
        result.sort(newestFirst?LedgerMath.OLDEST_FIRST.reversed():LedgerMath.OLDEST_FIRST);
        return result;
    }
    /** Entries of the selected period only; statements ignore search and type so the running balance stays complete. */
    ArrayList<JSONObject> periodEntries(List<JSONObject> entries){
        ArrayList<JSONObject> result=new ArrayList<>();
        for(JSONObject entry:entries)if(inPeriod(entry))result.add(entry);
        result.sort(LedgerMath.OLDEST_FIRST);
        return result;
    }

    /** Sets a named period relative to {@code now}; for a custom period {@code customFrom}/{@code customTo} are any moments on the first and last day. */
    void setPeriod(String name,long customFrom,long customTo,long now){
        period=name;
        Calendar start=Calendar.getInstance(),end=Calendar.getInstance();
        start.setTimeInMillis(now);end.setTimeInMillis(now);
        switch(name){
            case PERIOD_TODAY: break;
            case PERIOD_MONTH: start.set(Calendar.DAY_OF_MONTH,1);end.set(Calendar.DAY_OF_MONTH,end.getActualMaximum(Calendar.DAY_OF_MONTH));break;
            case PERIOD_LAST_MONTH: start.add(Calendar.MONTH,-1);start.set(Calendar.DAY_OF_MONTH,1);end.setTimeInMillis(start.getTimeInMillis());end.set(Calendar.DAY_OF_MONTH,end.getActualMaximum(Calendar.DAY_OF_MONTH));break;
            case PERIOD_CUSTOM: start.setTimeInMillis(Math.min(customFrom,customTo));end.setTimeInMillis(Math.max(customFrom,customTo));break;
            default: period=PERIOD_ALL;from=Long.MIN_VALUE;to=Long.MAX_VALUE;return;
        }
        startOfDay(start);endOfDay(end);from=start.getTimeInMillis();to=end.getTimeInMillis();
    }

    static void startOfDay(Calendar day){day.set(Calendar.HOUR_OF_DAY,0);day.set(Calendar.MINUTE,0);day.set(Calendar.SECOND,0);day.set(Calendar.MILLISECOND,0);}
    static void endOfDay(Calendar day){day.set(Calendar.HOUR_OF_DAY,23);day.set(Calendar.MINUTE,59);day.set(Calendar.SECOND,59);day.set(Calendar.MILLISECOND,999);}

    /** Lower-cases, uses Latin digits and folds common Arabic spelling variants so "اداره" finds "إدارة". */
    static String normalize(String text){
        String value=DisplayNumbers.latin(text==null?"":text).toLowerCase(Locale.ROOT);
        StringBuilder out=new StringBuilder(value.length());
        for(int i=0;i<value.length();i++){
            char c=value.charAt(i);
            if(c=='أ'||c=='إ'||c=='آ'||c=='ٱ')c='ا';
            else if(c=='ة')c='ه';
            else if(c=='ى')c='ي';
            else if(c=='ؤ')c='و';
            else if(c=='ئ')c='ي';
            else if(c=='ـ'||(c>='ً'&&c<='ْ'))continue;
            else if(Character.isWhitespace(c))c=' ';
            out.append(c);
        }
        return out.toString().trim().replaceAll(" +"," ");
    }
}
