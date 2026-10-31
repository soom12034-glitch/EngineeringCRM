package com.amalaei.engineering;

import java.util.Locale;

final class DisplayNumbers {
    private DisplayNumbers() {
    }

    static String latin(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder sb = null;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            char mapped = c;
            if (c >= 1632 && c <= 1641) {
                mapped = (char) (c - 1584);
            } else if (c >= 1776 && c <= 1785) {
                mapped = (char) (c - 1728);
            }
            if (mapped != c && sb == null) {
                sb = new StringBuilder(str);
            }
            if (sb != null) {
                sb.setCharAt(i, mapped);
            }
        }
        return sb == null ? str : sb.toString();
    }

    static Locale locale(boolean z) {
        return z ? Locale.US : Locale.forLanguageTag("ar-u-nu-latn");
    }
}
