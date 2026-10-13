package com.amalaei.engineering;
import java.math.BigDecimal;
import java.util.*;
import org.json.JSONObject;

public final class LedgerMathTest {
 public static void main(String[] args)throws Exception{
  JSONObject book=new JSONObject().put("openingBalance","500");ArrayList<JSONObject> rows=new ArrayList<>();
  rows.add(new JSONObject().put("kind","in").put("amount","700").put("paymentMethod","transfer"));
  rows.add(new JSONObject().put("kind","in").put("amount","50").put("paymentMethod","cash"));
  rows.add(new JSONObject().put("kind","out").put("amount","40"));
  if(LedgerMath.totalCollections(rows).compareTo(new BigDecimal("750"))!=0)throw new AssertionError();
  if(LedgerMath.netActivity(rows).compareTo(new BigDecimal("710"))!=0)throw new AssertionError();
  if(LedgerMath.balance(book,rows).compareTo(new BigDecimal("510"))!=0)throw new AssertionError();
  if(!LedgerMath.normal("").equals("0"))throw new AssertionError();
 }
}
