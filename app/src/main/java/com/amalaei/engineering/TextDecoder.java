package com.amalaei.engineering;

final class TextDecoder {
    private TextDecoder(){}
    static String decode(byte[] data){
        if(data==null||data.length==0)return "";
        if(data.length>=3&&data[0]==(byte)0xEF&&data[1]==(byte)0xBB&&data[2]==(byte)0xBF)return new String(data,3,data.length-3,java.nio.charset.StandardCharsets.UTF_8);
        if(data.length>=2&&data[0]==(byte)0xFF&&data[1]==(byte)0xFE)return new String(data,2,data.length-2,java.nio.charset.StandardCharsets.UTF_16LE);
        if(data.length>=2&&data[0]==(byte)0xFE&&data[1]==(byte)0xFF)return new String(data,2,data.length-2,java.nio.charset.StandardCharsets.UTF_16BE);
        String utf8=new String(data,java.nio.charset.StandardCharsets.UTF_8);
        if(!utf8.contains("\ufffd"))return utf8;
        try{
            String cp1256=new String(data,"windows-1256");
            String cp1252=new String(data,"windows-1252");
            int arabicUtf8=scoreArabic(utf8),cp1256Score=scoreArabic(cp1256),cp1252Score=scoreArabic(cp1252);
            if(arabicUtf8>=6)return utf8;
            if(cp1256Score>0&&cp1256Score>=cp1252Score&&cp1256Score>=arabicUtf8)return cp1256;
            if(cp1252Score>0&&cp1252Score>cp1256Score&&cp1252Score>=arabicUtf8)return cp1252;
        }catch(Exception ignored){}
        return utf8;
    }
    static int scoreArabic(String s){
        int score=0;for(int i=0;i<s.length();i++){char c=s.charAt(i);if((c>=0x0600&&c<=0x06FF)||(c>=0x0621&&c<=0x064A)||(c>=0x0660&&c<=0x0669)||(c>=0x064B&&c<=0x065F))score+=2;if(c=='\uFEFF')score+=1;}return score;
    }
}