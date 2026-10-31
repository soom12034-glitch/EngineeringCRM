package com.amalaei.engineering;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.View;

final class GlassIcon extends View {
    private final Shader base;
    private final Paint paint;
    private final Shader sheen;
    private final Glyph symbol;

    GlassIcon(Context context) {
        this(context, "clients");
    }

    GlassIcon(Context context, String str) {
        super(context);
        this.paint = new Paint(1);
        this.sheen = new LinearGradient(5.0f, 4.0f, 42.0f, 43.0f, -1711276033, 16777215, Shader.TileMode.CLAMP);
        this.base = new LinearGradient(6.0f, 4.0f, 58.0f, 58.0f, SectionColors.light(str), SectionColors.dark(str), Shader.TileMode.CLAMP);
        this.symbol = new Glyph(context, str, -1);
        setImportantForAccessibility(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(getWidth() / 64.0f, getHeight() / 64.0f);
        this.paint.setShader(null);
        this.paint.setColor(521616721);
        canvas.drawRoundRect(7.0f, 10.0f, 59.0f, 61.0f, 17.0f, 17.0f, this.paint);
        this.paint.setColor(-1);
        this.paint.setShader(this.base);
        canvas.drawRoundRect(5.0f, 4.0f, 57.0f, 56.0f, 16.0f, 16.0f, this.paint);
        this.paint.setShader(this.sheen);
        canvas.drawRoundRect(7.0f, 6.0f, 55.0f, 32.0f, 14.0f, 14.0f, this.paint);
        this.paint.setShader(null);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(0.8f);
        this.paint.setColor(-1996488705);
        canvas.drawRoundRect(5.5f, 4.5f, 56.5f, 55.5f, 16.0f, 16.0f, this.paint);
        this.paint.setStyle(Paint.Style.FILL);
        canvas.save();
        canvas.translate(16.0f, 15.0f);
        this.symbol.layout(0, 0, 30, 30);
        this.symbol.draw(canvas);
        canvas.restore();
        canvas.restore();
    }
}
