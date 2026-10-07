package com.amalaei.engineering;
final class AppMode { static final String MODE="survey"; static final String BRAND="Engineering CRM"; static final String LABEL="أجهزة المساحة"; static boolean accepts(String activity){return activity.isEmpty()||activity.equals(MODE);} }
