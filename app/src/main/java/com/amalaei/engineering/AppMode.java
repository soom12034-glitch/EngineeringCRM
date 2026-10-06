package com.amalaei.engineering;
final class AppMode { static final String MODE="survey"; static final String LABEL="أعمال المساحة"; static boolean accepts(String activity){return activity.isEmpty()||activity.equals(MODE);} }
