package com.amalaei.engineering;
import org.json.JSONObject;
public final class QuoteStatusTest { public static void main(String[] args)throws Exception{JSONObject q=new JSONObject().put("status","accepted");if(!QuoteStatus.value(q).equals("accepted"))throw new AssertionError();if(!QuoteStatus.value(new JSONObject()).equals("draft"))throw new AssertionError();if(QuoteStatus.valid("broken"))throw new AssertionError();} }
