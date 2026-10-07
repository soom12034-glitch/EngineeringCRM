package com.amalaei.engineering;

import java.util.ArrayList;

public final class CallCsvParserTest {
    public static void main(String[] args) {
        ArrayList<ArrayList<String>> comma=CallCsvImporter.parse("\ufeffname,phone,date\n\"شركة، الرياض\",0501234567,2026-10-07 09:30\n");
        if(comma.size()!=2||comma.get(1).size()!=3||!comma.get(1).get(0).equals("شركة، الرياض"))throw new AssertionError("quoted comma CSV failed");
        ArrayList<ArrayList<String>> semicolon=CallCsvImporter.parse("الاسم;رقم الهاتف;التاريخ\r\nأحمد;٠٥٥١٢٣٤٥٦٧;2026/10/07 11:00");
        if(semicolon.size()!=2||semicolon.get(1).size()!=3||!Analysis.phone(semicolon.get(1).get(1)).equals("+966551234567"))throw new AssertionError("Arabic semicolon CSV failed");
        ArrayList<ArrayList<String>> tab=CallCsvImporter.parse("contact\tnumber\ttimestamp\nACME\t+966501234567\t1760000000000");
        if(tab.size()!=2||tab.get(1).size()!=3)throw new AssertionError("tab-separated CSV failed");
        System.out.println("PASS: call CSV parser supports quoted comma, Arabic semicolon, tab and Arabic digits.");
    }
}
