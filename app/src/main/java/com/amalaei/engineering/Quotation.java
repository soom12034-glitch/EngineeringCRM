package com.amalaei.engineering;

import android.content.Context;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.text.*;
import org.json.*;
import java.math.*;
import java.io.*;

final class Quotation {
    static final int NAVY=0xff12324a,TEAL=0xff13867b,GOLD=0xffd5a64a,INK=0xff1b2b3a,GRAY=0xff667482,PALE=0xfff3f7f8,LINE=0xffdce5e8,WHITE=Color.WHITE;
    static final float[] X={34,112,190,242,302,528,562};
    static final int ITEMS_PER_PAGE=4;

    static JSONObject company()throws Exception{return new JSONObject().put("name","").put("registration","").put("vat","").put("address","").put("phone","").put("email","");}

    static BigDecimal subtotal(JSONObject quote)throws Exception{
        BigDecimal sum=BigDecimal.ZERO;JSONArray items=quote.getJSONArray("items");
        for(int i=0;i<items.length();i++){JSONObject item=items.getJSONObject(i);sum=sum.add(QuoteMath.line(item.getString("qty"),item.getString("price")));}
        return sum;
    }

    static int validDays(JSONObject quote){try{int days=Integer.parseInt(quote.optString("validDays","15"));return days>=1&&days<=365?days:-1;}catch(Exception invalid){return -1;}}

    static void validate(JSONObject quote)throws Exception{
        if(!quote.optString("business","survey").equals("survey"))throw new IllegalArgumentException("النموذج خاص بالمساحة");
        if(quote.optString("customer").trim().isEmpty()||quote.optString("number").trim().isEmpty())throw new IllegalArgumentException("أدخل العميل ورقم العرض");
        for(String key:new String[]{"customer","number","customerVat","customerPhone","customerAddress"})if(quote.optString(key).length()>(key.equals("customerAddress")?110:80))throw new IllegalArgumentException("اختصر بيانات العميل لتناسب النموذج");
        if(quote.optString("notes").length()>240)throw new IllegalArgumentException("اختصر ملاحظات العرض إلى 240 حرفًا");
        if(validDays(quote)<1)throw new IllegalArgumentException("صلاحية العرض يجب أن تكون من يوم إلى 365 يومًا");
        java.time.LocalDate.parse(quote.getString("date"));JSONArray items=quote.getJSONArray("items");
        if(items.length()<1||items.length()>100)throw new IllegalArgumentException("أضف من صنف إلى 100 صنف");
        for(int i=0;i<items.length();i++)if(items.getJSONObject(i).optString("description").trim().isEmpty()||items.getJSONObject(i).optString("description").length()>80||items.getJSONObject(i).optString("unit").length()>15)throw new IllegalArgumentException("أدخل وصف كل صنف");
        subtotal(quote);JSONObject company=quote.getJSONObject("company");if(company.optString("name").trim().isEmpty())throw new IllegalArgumentException("أدخل اسم المنشأة");
        if(!company.optString("logo").isEmpty()){Bitmap logo=QuoteBranding.bitmap(company.optString("logo"));if(logo==null)throw new IllegalArgumentException("شعار الشركة غير صالح؛ ارفعه مرة أخرى");logo.recycle();}
    }

    static String ltr(String value){String clean=value==null?"":value.trim();return clean.isEmpty()?"":"\u2066"+clean+"\u2069";}

    static float fit(Typeface font,String value,float maximum,float minimum,int width){
        TextPaint paint=new TextPaint(3);paint.setTypeface(font);float size=maximum;paint.setTextSize(size);
        while(size>minimum&&paint.measureText(value)>width){size-=0.5f;paint.setTextSize(size);}return size;
    }

    static void txt(Canvas canvas,Typeface font,String text,float x,float y,int width,float size,int color,boolean center){txt(canvas,font,text,x,y,width,size,color,center,1);}

    static void txt(Canvas canvas,Typeface font,String text,float x,float y,int width,float size,int color,boolean center,int maxLines){
        String value=text==null?"":text;TextPaint paint=new TextPaint(3);paint.setTypeface(font);paint.setTextSize(size);paint.setColor(color);
        StaticLayout.Builder builder=StaticLayout.Builder.obtain(value,0,value.length(),paint,width).setAlignment(center?Layout.Alignment.ALIGN_CENTER:Layout.Alignment.ALIGN_NORMAL).setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL).setIncludePad(false).setLineSpacing(1,1.08f).setMaxLines(maxLines);
        if(maxLines==1)builder.setEllipsize(TextUtils.TruncateAt.END);StaticLayout layout=builder.build();canvas.save();canvas.translate(x,y);layout.draw(canvas);canvas.restore();
    }

    static void rect(Canvas canvas,float left,float top,float right,float bottom,int color){Paint paint=new Paint(3);paint.setColor(color);canvas.drawRect(left,top,right,bottom,paint);}
    static void rounded(Canvas canvas,float left,float top,float right,float bottom,float radius,int color){Paint paint=new Paint(3);paint.setColor(color);canvas.drawRoundRect(left,top,right,bottom,radius,radius,paint);}
    static void line(Canvas canvas,float x,float y,float right,int color){rect(canvas,x,y,right,y+0.8f,color);}

    static void drawLogo(Canvas canvas,Typeface font,Bitmap logo,String companyName){
        rounded(canvas,34,28,145,105,9,WHITE);
        if(logo==null){rounded(canvas,51,37,128,96,11,TEAL);String initial=companyName.trim().isEmpty()?"T":companyName.trim().substring(0,1);txt(canvas,font,initial,51,50,77,24,WHITE,true);return;}
        float availableWidth=95,availableHeight=61,scale=Math.min(availableWidth/logo.getWidth(),availableHeight/logo.getHeight());float width=logo.getWidth()*scale,height=logo.getHeight()*scale,left=34+(111-width)/2,top=28+(77-height)/2;
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);canvas.drawBitmap(logo,null,new RectF(left,top,left+width,top+height),paint);
    }

    static void header(Canvas canvas,Typeface font,JSONObject company,JSONObject quote,Bitmap logo)throws Exception{
        rect(canvas,0,0,595,120,PALE);rect(canvas,0,0,595,8,TEAL);drawLogo(canvas,font,logo,company.optString("name"));
        String name=company.optString("name");txt(canvas,font,name,166,27,390,fit(font,name,19,12,390),NAVY,false);
        txt(canvas,font,"سجل تجاري  "+ltr(company.optString("registration")),166,61,390,9,GRAY,false);
        txt(canvas,font,"الرقم الضريبي  "+ltr(company.optString("vat")),166,81,390,9,GRAY,false);

        rect(canvas,0,120,595,76+120,NAVY);rect(canvas,0,190,595,196,GOLD);
        txt(canvas,font,"عرض سعر",316,137,240,23,WHITE,false);txt(canvas,font,"QUOTATION",43,140,220,15,0xffb8d7d3,false);
        String date=java.time.LocalDate.parse(quote.getString("date")).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        txt(canvas,font,"رقم العرض  "+ltr(quote.getString("number")),316,171,240,9,WHITE,false);
        txt(canvas,font,ltr(date)+"  •  صالح لمدة "+validDays(quote)+" يومًا",43,171,250,9,WHITE,false);
    }

    static void customerCard(Canvas canvas,Typeface font,JSONObject quote){
        rounded(canvas,34,214,562,302,10,PALE);rect(canvas,552,214,562,302,TEAL);
        txt(canvas,font,"مقدم إلى",319,227,220,10,TEAL,false);txt(canvas,font,quote.optString("customer"),319,248,220,14,INK,false);
        txt(canvas,font,quote.optString("customerAddress"),319,276,220,8,GRAY,false);
        txt(canvas,font,"بيانات التواصل",55,227,220,10,TEAL,false);txt(canvas,font,"الهاتف  "+ltr(quote.optString("customerPhone")),55,249,220,9,INK,false);
        txt(canvas,font,"الرقم الضريبي  "+ltr(quote.optString("customerVat")),55,274,220,8,GRAY,false);
        rect(canvas,297.5f,229,298.5f,288,LINE);
    }

    static float itemsTable(Canvas canvas,Typeface font,JSONArray items,int page){
        float y=320;rounded(canvas,34,y,562,y+31,7,NAVY);String[] headers={"الإجمالي","سعر الوحدة","الكمية","الوحدة","الوصف","#"};
        for(int column=0;column<6;column++)txt(canvas,font,headers[column],X[column]+2,y+8,(int)(X[column+1]-X[column]-4),9,WHITE,true);y+=31;
        int first=page*ITEMS_PER_PAGE,last=Math.min(items.length(),first+ITEMS_PER_PAGE);
        for(int index=first;index<last;index++){
            JSONObject item=items.optJSONObject(index);int fill=(index-first)%2==0?0xfff8fafb:0xffeef4f5;rect(canvas,34,y,562,y+48,fill);
            String[] values={QuoteMath.money(QuoteMath.line(item.optString("qty"),item.optString("price"))),QuoteMath.money(QuoteMath.number(item.optString("price"))),QuoteMath.money(QuoteMath.number(item.optString("qty"))),item.optString("unit","—"),item.optString("description"),String.valueOf(index+1)};
            for(int column=0;column<6;column++){if(column>0)rect(canvas,X[column]-.4f,y+7,X[column]+.4f,y+41,LINE);txt(canvas,font,values[column],X[column]+4,y+10,(int)(X[column+1]-X[column]-8),column==4?9:8,column==0?NAVY:INK,true,column==4?2:1);}line(canvas,34,y+47,562,LINE);y+=48;
        }
        return y;
    }

    static void totalsAndTerms(Canvas canvas,Typeface font,JSONObject quote)throws Exception{
        float top=558;rounded(canvas,305,top,562,top+91,9,PALE);BigDecimal sub=subtotal(quote),tax=QuoteMath.tax(sub),total=sub.add(tax);
        txt(canvas,font,"المجموع الفرعي",436,top+10,112,9,GRAY,false);txt(canvas,font,QuoteMath.money(sub)+" SAR",320,top+10,105,9,INK,true);
        txt(canvas,font,"الضريبة 15%",436,top+34,112,9,GRAY,false);txt(canvas,font,QuoteMath.money(tax)+" SAR",320,top+34,105,9,INK,true);
        rounded(canvas,305,top+59,562,top+91,7,TEAL);txt(canvas,font,"الإجمالي",436,top+66,112,11,WHITE,false);txt(canvas,font,QuoteMath.money(total)+" SAR",316,top+66,112,11,WHITE,true);

        rounded(canvas,34,top,287,top+91,9,0xfffffbf2);rect(canvas,34,top,42,top+91,GOLD);txt(canvas,font,"ملاحظات وشروط",55,top+9,215,10,GOLD,false);
        String notes=quote.optString("notes").trim();if(notes.isEmpty())notes="يسري هذا العرض لمدة "+validDays(quote)+" يومًا من تاريخ الإصدار.";
        txt(canvas,font,notes,55,top+31,215,8,GRAY,false,3);
    }

    static void footer(Canvas canvas,Typeface font,JSONObject company,int page,int pages){
        line(canvas,34,735,562,LINE);rect(canvas,34,735,122,739,TEAL);
        txt(canvas,font,company.optString("address"),34,749,528,9,INK,true);
        String contact="هاتف  "+ltr(company.optString("phone"));if(!company.optString("email").trim().isEmpty())contact+="     •     "+company.optString("email");
        txt(canvas,font,contact,34,771,528,8,TEAL,true);txt(canvas,font,"صفحة "+(page+1)+" من "+pages,34,805,528,8,GRAY,true);
    }

    static File pdf(Context context,JSONObject quote)throws Exception{
        validate(quote);PdfDocument document=new PdfDocument();Typeface font=Typeface.createFromAsset(context.getAssets(),"fonts/Cairo.ttf");JSONArray items=quote.getJSONArray("items");int pages=(items.length()+ITEMS_PER_PAGE-1)/ITEMS_PER_PAGE;JSONObject company=quote.getJSONObject("company");Bitmap logo=QuoteBranding.bitmap(company.optString("logo"));
        try{
            for(int page=0;page<pages;page++){
                PdfDocument.Page pdfPage=document.startPage(new PdfDocument.PageInfo.Builder(595,842,page+1).create());Canvas canvas=pdfPage.getCanvas();canvas.drawColor(WHITE);
                header(canvas,font,company,quote,logo);customerCard(canvas,font,quote);float bottom=itemsTable(canvas,font,items,page);
                if(page==pages-1)totalsAndTerms(canvas,font,quote);else txt(canvas,font,"يتبع في الصفحة التالية ←",34,bottom+18,528,9,TEAL,true);
                footer(canvas,font,company,page,pages);document.finishPage(pdfPage);
            }
            File file=new File(context.getCacheDir(),"quote-"+java.util.UUID.randomUUID().toString().replace("-","")+".pdf");try(OutputStream output=new FileOutputStream(file)){document.writeTo(output);}return file;
        }finally{if(logo!=null)logo.recycle();document.close();}
    }
}
