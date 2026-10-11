package com.amalaei.engineering;

import org.json.JSONObject;
import java.math.BigDecimal;
import java.util.ArrayList;

final class ReportMath {
    static long firstSeen(JSONObject lead,ArrayList<JSONObject> events){long first=Long.MAX_VALUE;for(JSONObject e:events)if(e.optLong("leadId")==lead.optLong("id"))first=Math.min(first,e.optLong("at"));return first==Long.MAX_VALUE?0:first;}
    static int countNew(ArrayList<JSONObject> leads,ArrayList<JSONObject> events,long from,long to){int n=0;for(JSONObject lead:leads){long at=firstSeen(lead,events);if(at>=from&&at<to)n++;}return n;}
    static BigDecimal ledgerNet(ArrayList<JSONObject> rows,long from,long to){BigDecimal total=BigDecimal.ZERO;for(JSONObject e:rows)if(e.optLong("at")>=from&&e.optLong("at")<to)try{BigDecimal amount=new BigDecimal(e.optString("amount","0"));total=total.add(e.optString("kind").equals("in")?amount:amount.negate());}catch(Exception ignored){}return total;}
    static int closed(ArrayList<JSONObject> leads,boolean completed){int n=0;for(JSONObject lead:leads)if(lead.optInt("stage")==(completed?4:5))n++;return n;}
}
