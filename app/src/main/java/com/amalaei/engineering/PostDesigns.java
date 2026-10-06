package com.amalaei.engineering;
import android.content.Context;
import android.graphics.*;
import android.text.*;
import java.io.ByteArrayOutputStream;
final class PostDesigns {
    static String create(Context c,String image,String title,String summary,String brand,String contact,int variant)throws Exception {
        int[] accents={0xFF6550C9,0xFF287F71,0xFFB87027},tints={0xFFF1EEFC,0xFFE9F5EF,0xFFFCF2E5};int ink=0xFF253047,accent=accents[variant],tint=tints[variant];
        Bitmap bitmap=Bitmap.createBitmap(1080,1350,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bitmap);canvas.drawColor(0xFFF8FAFC);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setColor(tint);canvas.drawRoundRect(24,24,1056,1326,48,48,paint);
        Typeface cairo=Typeface.createFromAsset(c.getAssets(),"fonts/Cairo.ttf");
        drawText(canvas,brand.isEmpty()?"":brand,64,48,952,48,1,accent,cairo);drawText(canvas,title,64,147,952,58,2,ink,Typeface.create(cairo,Typeface.BOLD));
        paint.setColor(Color.WHITE);canvas.drawRoundRect(64,390,1016,900,32,32,paint);
        if(!image.isEmpty()) {BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;String path=PostMedia.file(c,image).getPath();BitmapFactory.decodeFile(path,o);o.inSampleSize=1;while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>1200)o.inSampleSize*=2;o.inJustDecodeBounds=false;Bitmap photo=BitmapFactory.decodeFile(path,o);if(photo==null)throw new java.io.IOException("تعذر قراءة صورة المنتج");float scale=Math.min(880f/photo.getWidth(),438f/photo.getHeight());float w=photo.getWidth()*scale,h=photo.getHeight()*scale;canvas.drawBitmap(photo,null,new RectF(540-w/2,645-h/2,540+w/2,645+h/2),new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));photo.recycle();}
        else{drawText(canvas,title,110,540,860,65,3,accent,cairo);}
        drawText(canvas,summary,64,938,952,35,4,ink,cairo);paint.setColor(accent);canvas.drawRoundRect(64,1204,1016,1290,24,24,paint);TextPaint footer=new TextPaint(Paint.ANTI_ALIAS_FLAG);footer.setTypeface(cairo);footer.setTextSize(31);footer.setColor(Color.WHITE);String call=contact.isEmpty()?"راسلنا لمعرفة التفاصيل":contact;StaticLayout footerLayout=StaticLayout.Builder.obtain(call,0,call.length(),footer,912).setAlignment(Layout.Alignment.ALIGN_CENTER).setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL).setIncludePad(false).setMaxLines(1).setEllipsize(TextUtils.TruncateAt.END).build();canvas.save();canvas.translate(84,1215);footerLayout.draw(canvas);canvas.restore();
        try(ByteArrayOutputStream out=new ByteArrayOutputStream()){if(!bitmap.compress(Bitmap.CompressFormat.JPEG,90,out))throw new java.io.IOException("تعذر إنشاء التصميم");return PostMedia.save(c,out.toByteArray());}finally{bitmap.recycle();}
    }
    private static void drawText(Canvas canvas,String value,int x,int y,int width,int size,int lines,int color,Typeface typeface){if(value.isEmpty())return;TextPaint paint=new TextPaint(Paint.ANTI_ALIAS_FLAG);paint.setColor(color);paint.setTextSize(size);paint.setTypeface(typeface);StaticLayout layout=StaticLayout.Builder.obtain(value,0,value.length(),paint,width).setAlignment(Layout.Alignment.ALIGN_NORMAL).setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL).setIncludePad(false).setMaxLines(lines).setEllipsize(TextUtils.TruncateAt.END).setLineSpacing(3,1).build();canvas.save();canvas.translate(x,y);layout.draw(canvas);canvas.restore();}
}
