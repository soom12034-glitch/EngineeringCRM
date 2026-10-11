package com.amalaei.engineering;

import org.json.JSONObject;

public final class QuoteCustomerTest {
    public static void main(String[] args) throws Exception {
        JSONObject named = new JSONObject()
                .put("name", "+966551234567")
                .put("contactName", "أحمد العميل")
                .put("company", "")
                .put("phone", "+966551234567");
        if (!QuoteActivity.customerName(named).equals("أحمد العميل"))
            throw new AssertionError("contact name must replace a phone placeholder");
        String label = QuoteActivity.customerLabel(named);
        if (!label.startsWith("أحمد العميل\n") || !label.contains("\u2066+966551234567\u2069") || label.contains("•"))
            throw new AssertionError("customer label name/phone direction");

        JSONObject company = new JSONObject(named.toString()).put("company", "شركة المساحة");
        if (!QuoteActivity.customerLabel(company).startsWith("أحمد العميل — شركة المساحة\n"))
            throw new AssertionError("company label");

        JSONObject unnamed = new JSONObject().put("name", "+966501234567").put("phone", "+966501234567");
        if (!QuoteActivity.customerName(unnamed).equals("عميل غير مسمى"))
            throw new AssertionError("unnamed customer fallback");
        if (!Quotation.ltr("+966551234567").equals("\u2066+966551234567\u2069"))
            throw new AssertionError("PDF LTR phone isolation");
        if (Quotation.ITEMS_PER_PAGE != 4)
            throw new AssertionError("PDF page density must leave room for totals and footer");
        JSONObject validity = new JSONObject().put("validDays", "30");
        if (Quotation.validDays(validity) != 30 || Quotation.validDays(validity.put("validDays", "0")) != -1)
            throw new AssertionError("quotation validity range");
        System.out.println("PASS: quote customer names and LTR phone labels.");
    }
}
