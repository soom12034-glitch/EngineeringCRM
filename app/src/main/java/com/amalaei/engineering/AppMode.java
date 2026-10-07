package com.amalaei.engineering;
final class AppMode { static final String MODE="survey"; static final String BRAND="تواصل"; static final String LABEL="إدارة العملاء والمبيعات"; static boolean accepts(String activity){return activity.isEmpty()||activity.equals(MODE);} }
