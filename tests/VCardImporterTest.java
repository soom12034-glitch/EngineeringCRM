package com.amalaei.engineering;

import java.util.Map;

public final class VCardImporterTest {
    public static void main(String[] args){
        String text="BEGIN:VCARD\nVERSION:2.1\nFN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=D8=A3=D8=AD=D9=85=D8=AF\nTEL;CELL:0551234567\nTEL;HOME:+966551234567\nEND:VCARD\nBEGIN:VCARD\nVERSION:3.0\nN:السعودي;علي;;;\nTEL:0507654321\nPHOTO;ENCODING=b:ignored\n ignored-fold\nEND:VCARD";
        Map<String,String> parsed=VCardImporter.parse(text);
        if(parsed.size()!=2||!"أحمد".equals(parsed.get("+966551234567"))||!"علي السعودي".equals(parsed.get("+966507654321")))throw new AssertionError("VCF names and Saudi phones");
        try{VCardImporter.parse("not a vcard");throw new AssertionError("invalid VCF must fail");}catch(IllegalArgumentException expected){}
        System.out.println("PASS: VCF 2.1/3.0, quoted-printable Arabic names, repeated phones and folded ignored fields.");
    }
}
