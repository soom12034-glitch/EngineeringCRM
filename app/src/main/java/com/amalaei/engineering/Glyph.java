package com.amalaei.engineering;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

final class Glyph extends View {
    private final String kind;
    private final Path path;
    private final Paint pen;

    Glyph(Context context) {
        this(context, "clients", -16777216);
    }

    Glyph(Context context, String str, int i) {
        super(context);
        Paint paint = new Paint(1);
        this.pen = paint;
        this.path = new Path();
        this.kind = str;
        paint.setColor(i);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.7f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        setImportantForAccessibility(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Canvas canvas2;
        Canvas canvas3;
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(getWidth() / 24.0f, getHeight() / 24.0f);
        if (this.kind.equals("brand")) {
            this.pen.setStrokeWidth(3.8f);
            canvas.drawLine(5.0f, 5.0f, 19.0f, 5.0f, this.pen);
            canvas2 = canvas;
            canvas2.drawLine(12.0f, 5.0f, 12.0f, 20.0f, this.pen);
            this.pen.setStrokeWidth(1.7f);
        } else {
            if (this.kind.equals("home")) {
                this.path.reset();
                this.path.moveTo(3.0f, 11.0f);
                this.path.lineTo(12.0f, 3.0f);
                this.path.lineTo(21.0f, 11.0f);
                this.path.lineTo(19.0f, 11.0f);
                this.path.lineTo(19.0f, 21.0f);
                this.path.lineTo(5.0f, 21.0f);
                this.path.lineTo(5.0f, 11.0f);
                this.path.close();
                canvas.drawPath(this.path, this.pen);
                canvas.drawLine(10.0f, 21.0f, 10.0f, 15.0f, this.pen);
                canvas3 = canvas;
                canvas3.drawLine(14.0f, 21.0f, 14.0f, 15.0f, this.pen);
            } else {
                if (this.kind.equals("clients")) {
                    canvas.drawCircle(9.0f, 7.0f, 3.0f, this.pen);
                    canvas.drawArc(3.0f, 12.0f, 15.0f, 23.0f, 180.0f, 180.0f, false, this.pen);
                    canvas.drawArc(14.0f, 4.0f, 20.0f, 10.0f, -80.0f, 160.0f, false, this.pen);
                    canvas.drawArc(14.0f, 13.0f, 22.0f, 23.0f, -90.0f, 90.0f, false, this.pen);
                } else if (this.kind.equals("today")) {
                    canvas2 = canvas;
                    canvas2.drawRoundRect(3.0f, 5.0f, 21.0f, 21.0f, 3.0f, 3.0f, this.pen);
                    canvas2.drawLine(3.0f, 10.0f, 21.0f, 10.0f, this.pen);
                    canvas2.drawLine(8.0f, 3.0f, 8.0f, 7.0f, this.pen);
                    canvas2.drawLine(16.0f, 3.0f, 16.0f, 7.0f, this.pen);
                    this.path.reset();
                    this.path.moveTo(8.0f, 15.0f);
                    this.path.lineTo(11.0f, 18.0f);
                    this.path.lineTo(16.0f, 13.0f);
                    canvas2.drawPath(this.path, this.pen);
                } else if (this.kind.equals("unassigned")) {
                    this.path.reset();
                    this.path.moveTo(4.0f, 6.0f);
                    this.path.lineTo(20.0f, 6.0f);
                    this.path.lineTo(22.0f, 19.0f);
                    this.path.lineTo(2.0f, 19.0f);
                    this.path.close();
                    canvas.drawPath(this.path, this.pen);
                    canvas.drawLine(3.0f, 14.0f, 8.0f, 14.0f, this.pen);
                    canvas.drawLine(16.0f, 14.0f, 21.0f, 14.0f, this.pen);
                    canvas.drawArc(8.0f, 11.0f, 16.0f, 17.0f, 0.0f, 180.0f, false, this.pen);
                    canvas.drawLine(12.0f, 2.0f, 12.0f, 10.0f, this.pen);
                    canvas.drawLine(9.0f, 7.0f, 12.0f, 10.0f, this.pen);
                    canvas3 = canvas;
                    canvas3.drawLine(15.0f, 7.0f, 12.0f, 10.0f, this.pen);
                } else if (this.kind.equals("messages")) {
                    this.path.reset();
                    this.path.moveTo(7.0f, 19.0f);
                    this.path.lineTo(3.0f, 22.0f);
                    this.path.lineTo(3.0f, 6.0f);
                    this.path.quadTo(3.0f, 3.0f, 6.0f, 3.0f);
                    this.path.lineTo(18.0f, 3.0f);
                    this.path.quadTo(21.0f, 3.0f, 21.0f, 6.0f);
                    this.path.lineTo(21.0f, 16.0f);
                    this.path.quadTo(21.0f, 19.0f, 18.0f, 19.0f);
                    this.path.close();
                    canvas.drawPath(this.path, this.pen);
                    canvas.drawLine(7.0f, 8.0f, 17.0f, 8.0f, this.pen);
                    canvas3 = canvas;
                    canvas3.drawLine(7.0f, 13.0f, 14.0f, 13.0f, this.pen);
                } else if (this.kind.equals("ads")) {
                    this.path.reset();
                    this.path.moveTo(3.0f, 9.0f);
                    this.path.lineTo(9.0f, 9.0f);
                    this.path.lineTo(19.0f, 4.0f);
                    this.path.lineTo(19.0f, 20.0f);
                    this.path.lineTo(9.0f, 15.0f);
                    this.path.lineTo(3.0f, 15.0f);
                    this.path.close();
                    canvas.drawPath(this.path, this.pen);
                    canvas.drawLine(7.0f, 15.0f, 9.0f, 21.0f, this.pen);
                    canvas.drawLine(9.0f, 21.0f, 12.0f, 21.0f, this.pen);
                    canvas.drawLine(22.0f, 9.0f, 23.0f, 8.0f, this.pen);
                    canvas.drawLine(22.0f, 15.0f, 23.0f, 16.0f, this.pen);
                } else if (this.kind.equals("calculator")) {
                    canvas2 = canvas;
                    canvas2.drawRoundRect(4.0f, 2.0f, 20.0f, 22.0f, 3.0f, 3.0f, this.pen);
                    canvas2.drawRoundRect(7.0f, 5.0f, 17.0f, 9.0f, 1.0f, 1.0f, this.pen);
                    for (int i = 13; i <= 18; i += 5) {
                        for (int i2 = 8; i2 <= 16; i2 += 4) {
                            canvas2.drawCircle(i2, i, 0.8f, this.pen);
                        }
                    }
                } else {
                    Canvas canvas4 = canvas;
                    if (this.kind.equals("ledger") || this.kind.equals("quotes")) {
                        canvas2 = canvas;
                        canvas2.drawRoundRect(4.0f, 2.0f, 20.0f, 22.0f, 3.0f, 3.0f, this.pen);
                        canvas2.drawLine(8.0f, 7.0f, 16.0f, 7.0f, this.pen);
                        canvas2.drawLine(8.0f, 12.0f, 16.0f, 12.0f, this.pen);
                        canvas2.drawLine(8.0f, 17.0f, 13.0f, 17.0f, this.pen);
                        canvas2.drawCircle(16.0f, 17.0f, 1.2f, this.pen);
                    } else if (this.kind.equals("reports")) {
                        canvas4.drawLine(3.0f, 21.0f, 22.0f, 21.0f, this.pen);
                        canvas.drawRoundRect(4.0f, 12.0f, 8.0f, 19.0f, 1.0f, 1.0f, this.pen);
                        canvas.drawRoundRect(11.0f, 7.0f, 15.0f, 19.0f, 1.0f, 1.0f, this.pen);
                        canvas3 = canvas;
                        canvas3.drawRoundRect(18.0f, 2.0f, 22.0f, 19.0f, 1.0f, 1.0f, this.pen);
                    } else {
                        canvas4.drawCircle(12.0f, 12.0f, 4.0f, this.pen);
                        canvas4.drawCircle(12.0f, 12.0f, 8.0f, this.pen);
                        int i3 = 0;
                        while (i3 < 8) {
                            double d = (((double) i3) * 3.141592653589793d) / 4.0d;
                            canvas4.drawLine((float) ((Math.cos(d) * 8.0d) + 12.0d), (float) ((Math.sin(d) * 8.0d) + 12.0d), (float) ((Math.cos(d) * 10.0d) + 12.0d), (float) ((Math.sin(d) * 10.0d) + 12.0d), this.pen);
                            i3++;
                            canvas4 = canvas;
                        }
                    }
                }
                canvas2 = canvas;
            }
            canvas2 = canvas;
        }
        canvas2.restore();
    }
}
