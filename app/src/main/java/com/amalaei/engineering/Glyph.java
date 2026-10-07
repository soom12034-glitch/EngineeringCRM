package com.amalaei.engineering;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** Resolution-independent navigation icons, drawn natively. */
final class Glyph extends View {
    private final String kind;
    private final Paint pen=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    Glyph(Context context){this(context,"clients",0xff000000);}
    Glyph(Context context,String kind,int color){super(context);this.kind=kind;pen.setColor(color);pen.setStyle(Paint.Style.STROKE);pen.setStrokeWidth(1.7f);pen.setStrokeCap(Paint.Cap.ROUND);pen.setStrokeJoin(Paint.Join.ROUND);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);canvas.save();canvas.scale(getWidth()/24f,getHeight()/24f);
        if(kind.equals("clients")){canvas.drawCircle(9,7,3,pen);canvas.drawArc(3,12,15,23,180,180,false,pen);canvas.drawArc(14,4,20,10,-80,160,false,pen);canvas.drawArc(14,13,22,23,-90,90,false,pen);}
        else if(kind.equals("today")){canvas.drawRoundRect(3,5,21,21,3,3,pen);canvas.drawLine(3,10,21,10,pen);canvas.drawLine(8,3,8,7,pen);canvas.drawLine(16,3,16,7,pen);path.reset();path.moveTo(8,15);path.lineTo(11,18);path.lineTo(16,13);canvas.drawPath(path,pen);}
        else if(kind.equals("unassigned")){path.reset();path.moveTo(4,6);path.lineTo(20,6);path.lineTo(22,19);path.lineTo(2,19);path.close();canvas.drawPath(path,pen);canvas.drawLine(3,14,8,14,pen);canvas.drawLine(16,14,21,14,pen);canvas.drawArc(8,11,16,17,0,180,false,pen);canvas.drawLine(12,2,12,10,pen);canvas.drawLine(9,7,12,10,pen);canvas.drawLine(15,7,12,10,pen);}
        else if(kind.equals("messages")){path.reset();path.moveTo(7,19);path.lineTo(3,22);path.lineTo(3,6);path.quadTo(3,3,6,3);path.lineTo(18,3);path.quadTo(21,3,21,6);path.lineTo(21,16);path.quadTo(21,19,18,19);path.close();canvas.drawPath(path,pen);canvas.drawLine(7,8,17,8,pen);canvas.drawLine(7,13,14,13,pen);}
        else if(kind.equals("ads")){path.reset();path.moveTo(3,9);path.lineTo(9,9);path.lineTo(19,4);path.lineTo(19,20);path.lineTo(9,15);path.lineTo(3,15);path.close();canvas.drawPath(path,pen);canvas.drawLine(7,15,9,21,pen);canvas.drawLine(9,21,12,21,pen);canvas.drawLine(22,9,23,8,pen);canvas.drawLine(22,15,23,16,pen);}
        else if(kind.equals("calculator")){canvas.drawRoundRect(4,2,20,22,3,3,pen);canvas.drawRoundRect(7,5,17,9,1,1,pen);for(int y=13;y<=18;y+=5)for(int x=8;x<=16;x+=4)canvas.drawCircle(x,y,0.8f,pen);}
        else {canvas.drawCircle(12,12,4,pen);canvas.drawCircle(12,12,8,pen);for(int i=0;i<8;i++){double a=i*Math.PI/4;canvas.drawLine((float)(12+8*Math.cos(a)),(float)(12+8*Math.sin(a)),(float)(12+10*Math.cos(a)),(float)(12+10*Math.sin(a)),pen);}}
        canvas.restore();
    }
}
