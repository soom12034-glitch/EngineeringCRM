package com.amalaei.engineering;
import java.util.*;
public final class CsvExportTest { public static void main(String[] args){if(!CsvExport.cell("=2+2").startsWith("\"'=2+2"))throw new AssertionError();if(!CsvExport.cell("a\"b").contains("\"\""))throw new AssertionError();byte[] b=CsvExport.file(new String[]{"أ"},new ArrayList<>());if(b.length<4||(b[0]&255)!=239)throw new AssertionError();} }
