package com.amalaei.engineering;

public final class TextDecoderTest {
    public static void main(String[] args) throws Exception {
        if(!TextDecoder.decode(null).isEmpty())throw new AssertionError("null input");
        if(!TextDecoder.decode(new byte[0]).isEmpty())throw new AssertionError("empty input");
        String arabic="اسم العميل، 0541234567، الرياض، بريد alo@x.sa، نص عربي دال على البناء";
        byte[] utf8=arabic.getBytes("UTF-8");
        if(!TextDecoder.decode(utf8).equals(arabic))throw new AssertionError("plain UTF-8 roundtrip");
        byte[] bom=new byte[utf8.length+3];bom[0]=(byte)0xEF;bom[1]=(byte)0xBB;bom[2]=(byte)0xBF;System.arraycopy(utf8,0,bom,3,utf8.length);
        if(!TextDecoder.decode(bom).equals(arabic))throw new AssertionError("UTF-8 BOM roundtrip");
        byte[] rawLe=arabic.getBytes("UTF-16LE");byte[] le=new byte[rawLe.length+2];le[0]=(byte)0xFF;le[1]=(byte)0xFE;System.arraycopy(rawLe,0,le,2,rawLe.length);
        if(!TextDecoder.decode(le).equals(arabic))throw new AssertionError("UTF-16LE BOM roundtrip");
        byte[] rawBe=arabic.getBytes("UTF-16BE");byte[] be=new byte[rawBe.length+2];be[0]=(byte)0xFE;be[1]=(byte)0xFF;System.arraycopy(rawBe,0,be,2,rawBe.length);
        if(!TextDecoder.decode(be).equals(arabic))throw new AssertionError("UTF-16BE BOM roundtrip");
        byte[] cp1256=arabic.getBytes("windows-1256");
        if(!TextDecoder.decode(cp1256).equals(arabic))throw new AssertionError("windows-1256 Arabic roundtrip");
        byte[] hole=new byte[utf8.length+1];System.arraycopy(utf8,0,hole,0,utf8.length);hole[utf8.length]=(byte)0xD8;
        if(!TextDecoder.decode(hole).contains("اسم العميل"))throw new AssertionError("single truncated UTF-8 char must not force cp1256 re-encoding of the whole file");
        if(TextDecoder.scoreArabic("مرحبا وتحية")<=TextDecoder.scoreArabic("no arabic here at all"))throw new AssertionError("Arabic score must exceed latin score");
        System.out.println("PASS: text decoder handles UTF-8 BOM, UTF-16 LE/BE, windows-1256 and protects whole-file corruption.");
    }
}