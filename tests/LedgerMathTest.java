package com.amalaei.engineering;
import java.math.BigDecimal;
import java.util.*;
import org.json.JSONObject;

public final class LedgerMathTest {
 static void check(boolean ok,String what){if(!ok)throw new AssertionError(what);}
 static JSONObject entry(long id,long at,String kind,String amount,String method,String title)throws Exception{return new JSONObject().put("id",id).put("at",at).put("kind",kind).put("amount",amount).put("paymentMethod",method).put("title",title).put("note","").put("marker","");}

 public static void main(String[] args)throws Exception{
  JSONObject book=new JSONObject().put("openingBalance","500");ArrayList<JSONObject> rows=new ArrayList<>();
  rows.add(new JSONObject().put("kind","in").put("amount","700").put("paymentMethod","transfer"));
  rows.add(new JSONObject().put("kind","in").put("amount","50").put("paymentMethod","cash"));
  rows.add(new JSONObject().put("kind","out").put("amount","40"));
  check(LedgerMath.totalCollections(rows).compareTo(new BigDecimal("750"))==0,"total collections");
  check(LedgerMath.netActivity(rows).compareTo(new BigDecimal("710"))==0,"net activity");
  check(LedgerMath.balance(book,rows).compareTo(new BigDecimal("510"))==0,"balance");
  check(LedgerMath.normal("").equals("0"),"normal empty");

  // Running balance must carry across the whole statement, never restart from the closing balance.
  JSONObject shop=new JSONObject().put("openingBalance","1");ArrayList<JSONObject> history=new ArrayList<>();
  history.add(entry(1,1000,"out","15","cash","نسكافيه"));
  history.add(entry(2,2000,"out","500","cash","دفاع مدني"));
  history.add(entry(3,3000,"in","900","transfer","ميزان"));
  history.add(entry(4,4000,"in","300","cash","صيانة"));
  for(int i=0;i<20;i++)history.add(entry(10+i,5000+i,"in","10","cash","بند "+i));
  ArrayList<JSONObject> ordered=LedgerMath.chronological(history);
  BigDecimal[] running=LedgerMath.running(LedgerMath.opening(shop),ordered);
  check(running[0].compareTo(new BigDecimal("-14"))==0,"first row balance "+running[0]);
  check(running[1].compareTo(new BigDecimal("-514"))==0,"second row balance");
  check(running[2].compareTo(new BigDecimal("-514"))==0,"transfer leaves cash unchanged");
  check(running[3].compareTo(new BigDecimal("-214"))==0,"cash collection adds");
  check(running[running.length-1].compareTo(LedgerMath.balance(shop,history))==0,"last running balance equals closing cash");
  check(LedgerMath.balanceBefore(shop,history,3500).compareTo(new BigDecimal("-514"))==0,"carried balance");
  check(LedgerMath.grouped(new BigDecimal("1234567.5")).equals("1,234,567.50"),"grouped "+LedgerMath.grouped(new BigDecimal("1234567.5")));
  check(LedgerMath.grouped(new BigDecimal("-14")).equals("-14.00"),"grouped negative");

  // Filters: type, search with Arabic spelling variants, period and ordering.
  LedgerFilter filter=new LedgerFilter();
  check(filter.apply(history).size()==history.size()&&filter.apply(history).get(0).optLong("id")==29,"default shows all, newest first");
  filter.type=LedgerFilter.OUT;check(filter.apply(history).size()==2,"expenses only");
  filter.type=LedgerFilter.TRANSFER;check(filter.apply(history).size()==1,"transfers only");
  filter.type=LedgerFilter.ALL;filter.query="صيانه";check(filter.apply(history).size()==1,"ta marbuta folds");
  filter.query="دفاع   مدني";check(filter.apply(history).size()==1,"multi-word search");
  filter.query="٥٠٠";check(filter.apply(history).size()==1,"Arabic digits search amounts");
  filter.query="";filter.newestFirst=false;check(filter.apply(history).get(0).optLong("id")==1,"oldest first");
  filter.setPeriod(LedgerFilter.PERIOD_CUSTOM,0,0,0);
  Calendar day=Calendar.getInstance();day.setTimeInMillis(0);LedgerFilter.startOfDay(day);
  check(filter.from==day.getTimeInMillis()&&filter.to==day.getTimeInMillis()+86_399_999L,"custom single day spans the whole day");
  check(filter.hasPeriod()&&filter.periodEntries(history).size()==history.size(),"all sample entries fall on that day");
  filter.setPeriod(LedgerFilter.PERIOD_ALL,0,0,0);check(!filter.hasPeriod(),"all clears the period");
  Calendar march=Calendar.getInstance();march.set(2026,Calendar.MARCH,15,12,0,0);
  filter.setPeriod(LedgerFilter.PERIOD_LAST_MONTH,0,0,march.getTimeInMillis());
  Calendar feb=Calendar.getInstance();feb.setTimeInMillis(filter.to);
  check(feb.get(Calendar.MONTH)==Calendar.FEBRUARY&&feb.get(Calendar.DAY_OF_MONTH)==28,"last month ends on its last day");
  check(LedgerFilter.normalize("إدارة  آلات").equals("اداره الات"),"normalize "+LedgerFilter.normalize("إدارة  آلات"));
 }
}
