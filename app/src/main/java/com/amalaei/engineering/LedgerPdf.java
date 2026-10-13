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

/** Accountant-friendly statement: corporate collections stay visible without inflating rep cash. */
final class LedgerPdf {
    private static final int PER_PAGE=6, BAR=0xff167d80, EXPENSE=0xffb45309;
    static File create(Context context,JSONObject book,List<JSONObject> entries)throws Exception{
        Typeface font=Typeface.createFromAsset(context.getAssets(),"fonts/Cairo.ttf");PdfDocument document=new PdfDocument();
        try{
            BigDecimal cash=LedgerMath.collections(entries,"cash"),network=LedgerMath.collections(entries,"network"),transfer=LedgerMath.collections(entries,"transfer"),expenses=LedgerMath.total(entries,"out"),collections=LedgerMath.totalCollections(entries),net=LedgerMath.netActivity(entries),cashHeld=LedgerMath.balance(book,entries);
            int pages=Math.max(1,(entries.size()+PER_PAGE-1)/PER_PAGE);
            for(int page=0;page<pages;page++){
                PdfDocument.Page pdf=document.startPage(new PdfDocument.PageInfo.Builder(595,842,page+1).create());Canvas c=pdf.getCanvas();c.drawColor(Quotation.WHITE);
                Quotation.rect(c,0,0,595,8,Quotation.TEAL);Quotation.rect(c,0,8,595,112,Quotation.PALE);
                Quotation.txt(c,font,"كشف حركة دفتر",34,26,527,25,Quotation.NAVY,false);Quotation.txt(c,font,book.optString("name"),34,61,527,18,Quotation.INK,false);
                Quotation.txt(c,font,"تاريخ الإصدار: "+new SimpleDateFormat("yyyy/MM/dd HH:mm",Locale.US).format(new Date()),34,91,527,10,Quotation.GRAY,false);
                summary(c,font,book,cash,network,transfer,expenses,collections,net,cashHeld);
                float y=310;Quotation.rounded(c,34,y,562,y+34,7,BAR);String[] heads={"الرصيد النقدي","المبلغ","النوع","البيان والملاحظة","التاريخ"};float[] xs={34,126,218,304,462,562};for(int i=0;i<heads.length;i++)Quotation.txt(c,font,heads[i],xs[i]+3,y+9,(int)(xs[i+1]-xs[i]-6),10,Quotation.WHITE,true);y+=34;
                BigDecimal running=LedgerMath.opening(book);for(int i=entries.size()-1;i>=0;i--){JSONObject e=entries.get(i);try{BigDecimal amount=new BigDecimal(e.optString("amount","0"));if(e.optString("kind").equals("out"))running=running.subtract(amount);else if(e.optString("paymentMethod","cash").equals("cash"))running=running.add(amount);}catch(Exception ignored){}}
                int first=page*PER_PAGE,last=Math.min(entries.size(),first+PER_PAGE);for(int i=first;i<last;i++){
                    JSONObject e=entries.get(i);BigDecimal amount=new BigDecimal(e.optString("amount","0"));boolean income=e.optString("kind").equals("in");int fill=(i-first)%2==0?0xfff8fafb:0xffeef4f5;Quotation.rect(c,34,y,562,y+67,fill);
                    String marker=e.optString("marker"),note=e.optString("note").trim(),description=e.optString("title")+(marker.isEmpty()?"":"  • "+marker)+(note.isEmpty()?"":"\nملاحظة: "+note),type=income?"تحصيل "+LedgerMath.methodLabel(e.optString("paymentMethod","cash")):"مصروف نقدي",date=new SimpleDateFormat("dd/MM/yyyy\nHH:mm",Locale.US).format(new Date(e.optLong("at")));String[] values={LedgerMath.number(running),LedgerMath.number(amount),type,description,date};
                    for(int col=0;col<values.length;col++)Quotation.txt(c,font,values[col],xs[col]+3,y+10,(int)(xs[col+1]-xs[col]-6),col==3?11:10,col==1?(income?Quotation.TEAL:EXPENSE):Quotation.INK,true,col==3?3:2);
                    if(income&&e.optString("paymentMethod","cash").equals("cash"))running=running.subtract(amount);else if(!income)running=running.add(amount);Quotation.line(c,34,y+66,562,Quotation.LINE);y+=67;
                }
                Quotation.line(c,34,777,562,Quotation.LINE);Quotation.txt(c,font,"تم إنشاؤه من تواصل • صفحة "+(page+1)+" من "+pages,34,793,528,9,Quotation.GRAY,true);document.finishPage(pdf);
            }
            File file=new File(context.getCacheDir(),"ledger-"+java.util.UUID.randomUUID().toString().replace("-","")+".pdf");try(FileOutputStream out=new FileOutputStream(file)){document.writeTo(out);}return file;
        }finally{document.close();}
    }
    private static void summary(Canvas c,Typeface font,JSONObject book,BigDecimal cash,BigDecimal network,BigDecimal transfer,BigDecimal expenses,BigDecimal collections,BigDecimal net,BigDecimal cashHeld){
        Quotation.rounded(c,34,130,562,282,10,Quotation.PALE);String[] labels={"رصيد افتتاحي","تحصيل كاش","تحصيل شبكة","تحصيل تحويل","إجمالي التحصيلات قبل المصروفات","المصروفات","صافي التحصيل بعد المصروفات","الرصيد النقدي لدى المندوب"};BigDecimal[] values={LedgerMath.opening(book),cash,network,transfer,collections,expenses,net,cashHeld};int[] colors={Quotation.NAVY,Quotation.TEAL,Quotation.TEAL,Quotation.TEAL,Quotation.NAVY,EXPENSE,Quotation.TEAL,Quotation.NAVY};for(int i=0;i<labels.length;i++){int row=i/4,col=i%4;float x=34+col*132,y=143+row*66;Quotation.txt(c,font,labels[i],x,y,128,9,Quotation.GRAY,true);Quotation.txt(c,font,LedgerMath.money(values[i]),x,y+22,128,14,colors[i],true);}Quotation.line(c,48,207,548,Quotation.LINE);Quotation.rounded(c,48,254,548,276,6,BAR);Quotation.txt(c,font,"الرصيد النقدي = الافتتاحي + تحصيل الكاش − المصروفات (الشبكة والتحويل تحصيلات للشركة ولا تدخل نقدية المندوب)",56,259,484,8,Quotation.WHITE,true);
    }
}
