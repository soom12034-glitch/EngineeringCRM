package com.amalaei.engineering;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;

final class LedgerPdf {
    private static final int PER_PAGE=7;
    private static final int BAR=0xff167d80;
    static File create(Context context,JSONObject book,List<JSONObject> entries)throws Exception{
        Typeface font=Typeface.createFromAsset(context.getAssets(),"fonts/Cairo.ttf");PdfDocument document=new PdfDocument();
        try{int pages=Math.max(1,(entries.size()+PER_PAGE-1)/PER_PAGE);BigDecimal in=LedgerMath.total(entries,"in"),out=LedgerMath.total(entries,"out"),balance=LedgerMath.balance(book,entries);
            for(int page=0;page<pages;page++){PdfDocument.Page pdf=document.startPage(new PdfDocument.PageInfo.Builder(595,842,page+1).create());Canvas c=pdf.getCanvas();c.drawColor(Quotation.WHITE);
                Quotation.rect(c,0,0,595,8,Quotation.TEAL);Quotation.rect(c,0,8,595,118,Quotation.PALE);
                Quotation.txt(c,font,"كشف دفتر",34,27,527,25,Quotation.NAVY,false);Quotation.txt(c,font,book.optString("name"),34,62,527,18,Quotation.INK,false);
                Quotation.txt(c,font,"تاريخ الإصدار: "+new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(new Date()),34,94,527,10,Quotation.GRAY,false);
                Quotation.rounded(c,34,136,562,246,10,Quotation.PALE);
                Quotation.txt(c,font,"الرصيد الافتتاحي",397,151,140,11,Quotation.GRAY,true);Quotation.txt(c,font,LedgerMath.money(LedgerMath.opening(book)),397,174,140,16,Quotation.NAVY,true);
                Quotation.txt(c,font,"إجمالي الإضافات",228,151,140,11,Quotation.GRAY,true);Quotation.txt(c,font,LedgerMath.money(in),228,174,140,16,Quotation.TEAL,true);
                Quotation.txt(c,font,"إجمالي المصروفات",59,151,140,11,Quotation.GRAY,true);Quotation.txt(c,font,LedgerMath.money(out),59,174,140,16,0xffb45309,true);
                Quotation.line(c,54,204,542,Quotation.LINE);Quotation.rounded(c,54,214,542,238,7,BAR);Quotation.txt(c,font,"الرصيد الحالي",363,219,150,11,Quotation.WHITE,true);Quotation.txt(c,font,LedgerMath.money(balance),72,217,270,15,Quotation.WHITE,true);
                float y=270;Quotation.rounded(c,34,y,562,y+34,7,BAR);String[] heads={"الرصيد","المبلغ","النوع","البيان","التاريخ"};float[] xs={34,126,224,302,452,562};for(int i=0;i<heads.length;i++)Quotation.txt(c,font,heads[i],xs[i]+3,y+9,(int)(xs[i+1]-xs[i]-6),10,Quotation.WHITE,true);y+=34;
                BigDecimal running=LedgerMath.opening(book);for(int i=entries.size()-1;i>=0;i--){JSONObject e=entries.get(i);try{BigDecimal a=new BigDecimal(e.optString("amount","0"));running=running.add(e.optString("kind").equals("in")?a:a.negate());}catch(Exception ignored){}}
                int first=page*PER_PAGE,last=Math.min(entries.size(),first+PER_PAGE);for(int i=first;i<last;i++){JSONObject e=entries.get(i);BigDecimal amount=new BigDecimal(e.optString("amount","0"));String kind=e.optString("kind");int fill=(i-first)%2==0?0xfff8fafb:0xffeef4f5;Quotation.rect(c,34,y,562,y+60,fill);String marker=e.optString("marker");String title=e.optString("title")+(marker.isEmpty()?"":"  • "+marker);String date=new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.US).format(new Date(e.optLong("at")));String[] values={LedgerMath.number(running),LedgerMath.number(amount),kind.equals("in")?"إضافة":"مصروف",title,date};for(int col=0;col<values.length;col++)Quotation.txt(c,font,values[col],xs[col]+3,y+16,(int)(xs[col+1]-xs[col]-6),col==3?12:11,col==1?(kind.equals("in")?Quotation.TEAL:0xffb45309):Quotation.INK,true,col==3?2:1);if(kind.equals("in"))running=running.subtract(amount);else running=running.add(amount);Quotation.line(c,34,y+59,562,Quotation.LINE);y+=60;}
                Quotation.line(c,34,777,562,Quotation.LINE);Quotation.txt(c,font,"تم إنشاؤه من تواصل • صفحة "+(page+1)+" من "+pages,34,793,528,9,Quotation.GRAY,true);document.finishPage(pdf);
            }
            File file=new File(context.getCacheDir(),"quote-"+java.util.UUID.randomUUID().toString().replace("-","")+".pdf");try(FileOutputStream outFile=new FileOutputStream(file)){document.writeTo(outFile);}return file;
        } finally {document.close();}
    }
}
