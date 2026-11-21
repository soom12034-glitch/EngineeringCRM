package com.amalaei.engineering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Surface;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import org.json.JSONObject;

/**
 * Turns the post photos into a short MP4 slideshow (H.264, no sound): each photo gently zooms for 2.5 seconds and
 * cross-fades into the next, with the headline and business name on a band at the bottom.
 */
final class PostVideoMaker {
    interface Progress {
        void update(int percent);
    }

    private static final int FPS = 30, HOLD = 75, FADE = 15;

    private PostVideoMaker() {
    }

    static File make(Context context, List<Bitmap> slides, JSONObject spec, Bitmap logo, Progress progress) throws Exception {
        if (slides.isEmpty()) throw new IllegalArgumentException("لا توجد صور");
        int width = 720, height = sizeHeight(spec.optString("size", "portrait"));
        File out = File.createTempFile("slideshow", ".mp4", context.getCacheDir());
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 5_000_000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FPS);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        MediaCodec codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        MediaMuxer muxer = null;
        Surface surface = null;
        boolean started = false;
        try {
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            surface = codec.createInputSurface();
            codec.start();
            muxer = new MediaMuxer(out.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            Drain drain = new Drain(codec, muxer);
            int[] palette = PostDesigner.PALETTES[Math.max(0, Math.min(PostDesigner.PALETTES.length - 1, spec.optInt("palette", 0)))];
            Overlay overlay = new Overlay(context, spec, width, height, palette, logo);
            int count = Math.min(slides.size(), 10);
            int total = count * HOLD + FADE;
            for (int frame = 0; frame < total; frame++) {
                Canvas canvas = surface.lockHardwareCanvas();
                try {
                    canvas.drawColor(palette[1]);
                    int slide = Math.min(count - 1, frame / HOLD);
                    int local = frame - slide * HOLD;
                    drawSlide(canvas, slides.get(slide), width, height, local, 255);
                    if (local >= HOLD - FADE && slide + 1 < count) {
                        int alpha = Math.round(255f * (local - (HOLD - FADE)) / FADE);
                        drawSlide(canvas, slides.get(slide + 1), width, height, local - HOLD, alpha);
                    }
                    if (frame >= count * HOLD) {
                        Paint fade = new Paint();
                        fade.setColor(palette[1]);
                        fade.setAlpha(Math.round(255f * (frame - count * HOLD) / FADE));
                        canvas.drawRect(0, 0, width, height, fade);
                    } else overlay.draw(canvas);
                } finally {
                    surface.unlockCanvasAndPost(canvas);
                }
                drain.run(false);
                if (progress != null && frame % 10 == 0) progress.update(frame * 100 / total);
            }
            codec.signalEndOfInputStream();
            drain.run(true);
            started = drain.started;
            if (progress != null) progress.update(100);
        } finally {
            try {
                codec.stop();
            } catch (Exception ignored) {
            }
            codec.release();
            if (surface != null) surface.release();
            if (muxer != null) {
                try {
                    if (started) muxer.stop();
                } catch (Exception ignored) {
                }
                muxer.release();
            }
        }
        if (!started || out.length() == 0) throw new IllegalStateException("لم يُنتج المرمّز أي إطار");
        return out;
    }

    /** Even-height frames for the encoder: 720×720, 720×896 and 720×1280. */
    static int sizeHeight(String size) {
        if ("square".equals(size)) return 720;
        if ("story".equals(size)) return 1280;
        return 896;
    }

    /** The photo shown whole over a blurred-looking, darkened fill of itself, zooming in slowly. */
    private static void drawSlide(Canvas canvas, Bitmap photo, int width, int height, int local, int alpha) {
        float progress = Math.max(0, Math.min(1, (local + FADE) / (float) (HOLD + FADE)));
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setAlpha(alpha);
        RectF full = new RectF(0, 0, width, height);
        canvas.drawBitmap(photo, crop(photo, (float) width / height), full, paint);
        Paint shade = new Paint();
        shade.setColor(0x99000000);
        shade.setAlpha(Math.round(0.6f * alpha));
        canvas.drawRect(full, shade);
        float zoom = 1f + 0.06f * progress;
        float fit = Math.min(width * 0.9f / photo.getWidth(), height * 0.74f / photo.getHeight()) * zoom;
        float w = photo.getWidth() * fit, h = photo.getHeight() * fit;
        float cy = height * 0.44f;
        RectF dest = new RectF(width / 2f - w / 2, cy - h / 2, width / 2f + w / 2, cy + h / 2);
        canvas.drawBitmap(photo, null, dest, paint);
    }

    private static Rect crop(Bitmap photo, float ratio) {
        int w = photo.getWidth(), h = photo.getHeight();
        if ((float) w / h > ratio) {
            int cw = Math.round(h * ratio);
            return new Rect((w - cw) / 2, 0, (w + cw) / 2, h);
        }
        int ch = Math.round(w / ratio);
        return new Rect(0, (h - ch) / 2, w, (h + ch) / 2);
    }

    /** Bottom band with the headline, business name and logo, prepared once and drawn on every frame. */
    private static final class Overlay {
        private final StaticLayout headline, brand;
        private final Bitmap logo;
        private final int width, height, accent;
        private final float band;

        Overlay(Context context, JSONObject spec, int width, int height, int[] palette, Bitmap logo) {
            this.width = width;
            this.height = height;
            this.accent = palette[2];
            this.logo = spec.optBoolean("showLogo") ? logo : null;
            Typeface face;
            try {
                face = new Typeface.Builder(context.getAssets(), "fonts/Cairo.ttf").setFontVariationSettings("'wght' 800").build();
            } catch (Exception e) {
                face = Typeface.DEFAULT_BOLD;
            }
            String head = DisplayNumbers.latin(spec.optString("headline").trim());
            String name = spec.optBoolean("showBrand") ? DisplayNumbers.latin(spec.optString("brand").trim()) : "";
            int textWidth = width - 64 - (this.logo == null ? 0 : 96);
            headline = head.isEmpty() ? null : layout(head, 34, face, textWidth, 2);
            brand = name.isEmpty() ? null : layout(name, 24, face, textWidth, 1);
            band = (headline == null && brand == null && this.logo == null) ? 0 : Math.max(this.logo == null ? 0 : 112, (headline == null ? 0 : headline.getHeight()) + (brand == null ? 0 : brand.getHeight() + 6) + 44);
        }

        private StaticLayout layout(String text, float size, Typeface face, int width, int lines) {
            TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paint.setTypeface(face);
            paint.setTextSize(size);
            paint.setColor(0xFFFFFFFF);
            return StaticLayout.Builder.obtain(text, 0, text.length(), paint, width).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL).setIncludePad(false).setMaxLines(lines).setEllipsize(TextUtils.TruncateAt.END).build();
        }

        void draw(Canvas canvas) {
            if (band == 0) return;
            float top = height - band;
            Paint shade = new Paint();
            shade.setShader(new LinearGradient(0, top - 60, 0, height, 0x00000000, 0xE6000000, Shader.TileMode.CLAMP));
            canvas.drawRect(0, top - 60, width, height, shade);
            Paint line = new Paint();
            line.setColor(accent);
            canvas.drawRect(width - 32 - 70, top + 8, width - 32, top + 13, line);
            float right = width - 32;
            if (logo != null) {
                RectF box = new RectF(32, top + 18, 32 + 80, top + 98);
                Paint white = new Paint(Paint.ANTI_ALIAS_FLAG);
                white.setColor(0xFFFFFFFF);
                canvas.drawRoundRect(box, 18, 18, white);
                float scale = Math.min(64f / logo.getWidth(), 64f / logo.getHeight());
                float w = logo.getWidth() * scale, h = logo.getHeight() * scale;
                canvas.drawBitmap(logo, null, new RectF(box.centerX() - w / 2, box.centerY() - h / 2, box.centerX() + w / 2, box.centerY() + h / 2), new Paint(Paint.FILTER_BITMAP_FLAG));
            }
            float y = top + 22;
            if (headline != null) {
                canvas.save();
                canvas.translate(right - headline.getWidth(), y);
                headline.draw(canvas);
                canvas.restore();
                y += headline.getHeight() + 6;
            }
            if (brand != null) {
                brand.getPaint().setColor(accent);
                canvas.save();
                canvas.translate(right - brand.getWidth(), y);
                brand.draw(canvas);
                canvas.restore();
            }
        }
    }

    /** Moves encoded frames into the MP4, stamping them at a steady 30 fps regardless of how fast they were drawn. */
    private static final class Drain {
        private final MediaCodec codec;
        private final MediaMuxer muxer;
        private final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        private int track = -1;
        private long written;
        boolean started;

        Drain(MediaCodec codec, MediaMuxer muxer) {
            this.codec = codec;
            this.muxer = muxer;
        }

        void run(boolean endOfStream) {
            long deadline = System.currentTimeMillis() + 20_000;
            while (true) {
                int index = codec.dequeueOutputBuffer(info, endOfStream ? 10_000 : 0);
                if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (!endOfStream || System.currentTimeMillis() > deadline) return;
                } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    track = muxer.addTrack(codec.getOutputFormat());
                    muxer.start();
                    started = true;
                } else if (index >= 0) {
                    ByteBuffer data = codec.getOutputBuffer(index);
                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) info.size = 0;
                    if (info.size > 0 && started && data != null) {
                        info.presentationTimeUs = written++ * 1_000_000L / FPS;
                        data.position(info.offset);
                        data.limit(info.offset + info.size);
                        muxer.writeSampleData(track, data, info);
                    }
                    boolean end = (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                    codec.releaseOutputBuffer(index, false);
                    if (end) return;
                }
            }
        }
    }
}
