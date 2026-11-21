package com.amalaei.engineering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/**
 * Draws a promotional post from a design spec (see {@link PostContent#defaults}) on a 1080-wide canvas:
 * six templates, three sizes and eight palettes, every block optional.
 */
final class PostDesigner {
    static final String[] PALETTE_LABELS = {"تواصل", "كحلي وذهبي", "أسود وبرتقالي", "أزرق ملكي", "زمردي", "عنابي", "أبيض أنيق", "رملي فاخر"};
    /** bgTop, bgBottom, accent, text on accent, text, muted text. */
    static final int[][] PALETTES = {
            {0xFF0B3A4A, 0xFF061826, 0xFFF5C451, 0xFF2A1A00, 0xFFFFFFFF, 0xFFA9C4CF},
            {0xFF0E2650, 0xFF040D1F, 0xFFD9B44A, 0xFF1A1200, 0xFFFFFFFF, 0xFFB7C3DA},
            {0xFF262626, 0xFF0A0A0A, 0xFFFF7A1A, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFBDBDBD},
            {0xFF1556D6, 0xFF082A73, 0xFFFFD23F, 0xFF231A00, 0xFFFFFFFF, 0xFFCBDAFF},
            {0xFF11805F, 0xFF053A2A, 0xFFF7E27C, 0xFF2A2400, 0xFFFFFFFF, 0xFFC6EBDD},
            {0xFF7A1E3D, 0xFF2B0814, 0xFFF4C27A, 0xFF2A1400, 0xFFFFFFFF, 0xFFE9C6D2},
            {0xFFFFFFFF, 0xFFE4ECF1, 0xFF0F766E, 0xFFFFFFFF, 0xFF0B2A3F, 0xFF56707F},
            {0xFFFBF6EC, 0xFFE9DCC4, 0xFF8A5A2B, 0xFFFFFFFF, 0xFF2E2218, 0xFF7A6A58}};
    private static final int W = 1080, M = 48;

    private static Typeface regular, bold, black;

    private final Context context;
    private final JSONObject spec;
    private final List<Bitmap> photos;
    private final Bitmap logo;
    private final int H, bgTop, bgBottom, accent, onAccent, ink, muted, card, cardLine;
    private final boolean light;
    private Canvas canvas;

    private PostDesigner(Context context, JSONObject spec, List<Bitmap> photos, Bitmap logo) {
        this.context = context;
        this.spec = spec;
        this.photos = photos == null ? new ArrayList<>() : photos;
        this.logo = logo;
        H = PostContent.height(spec.optString("size", "portrait"));
        int index = Math.max(0, Math.min(PALETTES.length - 1, spec.optInt("palette", 0)));
        int[] p = PALETTES[index];
        bgTop = p[0];
        bgBottom = p[1];
        accent = p[2];
        onAccent = p[3];
        ink = p[4];
        muted = p[5];
        light = index >= 6;
        card = light ? 0xFFFFFFFF : 0x1AFFFFFF;
        cardLine = light ? 0x1F0B2A3F : 0x33FFFFFF;
        fonts(context);
    }

    private static synchronized void fonts(Context context) {
        if (regular != null) return;
        regular = weight(context, 400);
        bold = weight(context, 700);
        black = weight(context, 900);
    }

    private static Typeface weight(Context context, int weight) {
        try {
            return new Typeface.Builder(context.getAssets(), "fonts/Cairo.ttf").setFontVariationSettings("'wght' " + weight).build();
        } catch (Exception e) {
            return Typeface.create(Typeface.createFromAsset(context.getAssets(), "fonts/Cairo.ttf"), weight >= 700 ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    /** Renders the design; {@code scale} 1 gives the full 1080px output, smaller values a quick preview. */
    static Bitmap render(Context context, JSONObject spec, List<Bitmap> photos, Bitmap logo, float scale) {
        PostDesigner designer = new PostDesigner(context, spec, photos, logo);
        Bitmap out = Bitmap.createBitmap(Math.round(W * scale), Math.round(designer.H * scale), Bitmap.Config.ARGB_8888);
        designer.canvas = new Canvas(out);
        designer.canvas.scale(scale, scale);
        designer.draw();
        return out;
    }

    /** Full-size JPEG of the design saved with the post media. */
    static String save(Context context, JSONObject spec, List<Bitmap> photos, Bitmap logo) throws Exception {
        Bitmap bitmap = render(context, spec, photos, logo, 1f);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 93, out)) throw new java.io.IOException("تعذر إنشاء التصميم");
            return PostMedia.save(context, out.toByteArray());
        } finally {
            bitmap.recycle();
        }
    }

    // ------------------------------------------------------------------ composition

    private String s(String key) {
        return DisplayNumbers.latin(spec.optString(key).trim());
    }

    private boolean on(String key) {
        return spec.optBoolean(key, false);
    }

    private void draw() {
        String template = spec.optString("template", "product");
        background();
        if (("service".equals(template) || "occasion".equals(template)) && !photos.isEmpty()) backdrop(photos.get(0), "occasion".equals(template));
        if ("occasion".equals(template)) ornaments();
        float top = header(!"occasion".equals(template));
        float bottom = footer() - 28;
        switch (template) {
            case "features": features(top, bottom); break;
            case "gallery": gallery(top, bottom); break;
            case "compare": compare(top, bottom); break;
            case "service": service(top, bottom); break;
            case "occasion": occasion(top, bottom); break;
            default: product(top, bottom); break;
        }
    }

    private void background() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(0, 0, W * 0.35f, H, bgTop, bgBottom, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, W, H, paint);
        glow(W * 0.92f, H * 0.04f, W * 0.62f, alpha(accent, light ? 0.16f : 0.22f));
        glow(W * 0.02f, H * 0.98f, W * 0.7f, alpha(light ? accent : 0xFFFFFFFF, light ? 0.10f : 0.07f));
        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        dot.setColor(alpha(ink, light ? 0.07f : 0.08f));
        for (float x = 40; x < W * 0.32f; x += 34) for (float y = 40; y < H * 0.18f; y += 34) canvas.drawCircle(x, y, 2.6f, dot);
    }

    private void glow(float x, float y, float radius, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new RadialGradient(x, y, radius, color, color & 0x00FFFFFF, Shader.TileMode.CLAMP));
        canvas.drawCircle(x, y, radius, paint);
    }

    /** Photo behind the content for the service and occasion templates, fading into the background colour. */
    private void backdrop(Bitmap photo, boolean full) {
        RectF area = new RectF(0, 0, W, full ? H : H * 0.46f);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setAlpha(full ? 70 : 255);
        canvas.drawBitmap(photo, cropRect(photo, area.width() / area.height()), area, paint);
        Paint fade = new Paint();
        int base = bgBottom & 0x00FFFFFF;
        fade.setShader(new LinearGradient(0, full ? 0 : area.top, 0, area.bottom, full ? alpha(bgTop, 0.55f) : base | 0x66000000, bgBottom, Shader.TileMode.CLAMP));
        canvas.drawRect(area, fade);
        if (!full) {
            Paint top = new Paint();
            top.setShader(new LinearGradient(0, 0, 0, 260, alpha(bgBottom, 0.75f), base, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, W, 260, top);
        }
    }

    /** Logo, business name and the badge; returns where the content may start. */
    private float header(boolean withIdentity) {
        float top = 46, size = 104, right = W - M;
        boolean showLogo = withIdentity && on("showLogo") && logo != null;
        String brand = withIdentity && on("showBrand") ? s("brand") : "";
        String badge = s("badge");
        if (!showLogo && brand.isEmpty() && badge.isEmpty()) return M + 8;
        float badgeWidth = 0;
        if (!badge.isEmpty()) {
            StaticLayout text = layout(badge, 32, onAccent, bold, 420, 1, Layout.Alignment.ALIGN_CENTER);
            badgeWidth = Math.min(460, lineWidth(text) + 64);
            RectF pill = new RectF(M, top + (size - 66) / 2, M + badgeWidth, top + (size + 66) / 2);
            shadowed(pill, 33, accent);
            StaticLayout fitted = layout(badge, 32, onAccent, bold, (int) badgeWidth - 40, 1, Layout.Alignment.ALIGN_CENTER);
            put(fitted, pill.left + 20, pill.centerY() - fitted.getHeight() / 2f);
        }
        if (showLogo) {
            RectF box = new RectF(right - size, top, right, top + size);
            shadowed(box, 26, 0xFFFFFFFF);
            contain(logo, new RectF(box.left + 12, box.top + 12, box.right - 12, box.bottom - 12));
            right -= size + 22;
        }
        if (!brand.isEmpty()) {
            float width = right - M - (badgeWidth > 0 ? badgeWidth + 24 : 0);
            StaticLayout name = fit(brand, 40, 26, bold, ink, (int) width, 1, Layout.Alignment.ALIGN_NORMAL);
            put(name, right - width, top + (size - name.getHeight()) / 2f);
        }
        return top + size + 30;
    }

    /** Call-to-action bar and contact row; returns the top of the footer. */
    private float footer() {
        String cta = on("showCta") ? s("cta") : "";
        ArrayList<String[]> contacts = new ArrayList<>();
        if (on("showContact")) {
            if (!s("phone").isEmpty()) contacts.add(new String[]{"call", s("phone")});
            if (!s("whatsapp").isEmpty() && !s("whatsapp").equals(s("phone"))) contacts.add(new String[]{"chat", s("whatsapp")});
            if (!s("website").isEmpty()) contacts.add(new String[]{"web", s("website").replaceFirst("^https?://", "").replaceFirst("/$", "")});
        }
        float contactSize = 30, iconSize = 50, gap = 30, rowHeight = 60;
        ArrayList<ArrayList<Integer>> rows = new ArrayList<>();
        float[] widths = new float[contacts.size()];
        TextPaint measure = paint(contactSize, ink, bold);
        float rowWidth = 0;
        for (int i = 0; i < contacts.size(); i++) {
            widths[i] = iconSize + 12 + Math.min(measure.measureText("‎" + contacts.get(i)[1]), W - 2 * M - iconSize - 12);
            if (rows.isEmpty() || rowWidth + gap + widths[i] > W - 2 * M) {
                rows.add(new ArrayList<>());
                rowWidth = 0;
            } else rowWidth += gap;
            rows.get(rows.size() - 1).add(i);
            rowWidth += widths[i];
        }
        float ctaHeight = cta.isEmpty() ? 0 : 100, contactsHeight = rows.isEmpty() ? 0 : rows.size() * rowHeight + (rows.size() - 1) * 10;
        float total = ctaHeight + contactsHeight + (ctaHeight > 0 && contactsHeight > 0 ? 24 : 0);
        if (total == 0) return H - M;
        float y = H - M - total;
        float footerTop = y;
        if (!cta.isEmpty()) {
            RectF bar = new RectF(M, y, W - M, y + ctaHeight);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setShadowLayer(26, 0, 10, alpha(0xFF000000, 0.28f));
            paint.setShader(new LinearGradient(bar.left, 0, bar.right, 0, lighten(accent, 0.18f), accent, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(bar, 50, 50, paint);
            StaticLayout text = fit(cta, 42, 28, black, onAccent, (int) bar.width() - 80, 1, Layout.Alignment.ALIGN_CENTER);
            put(text, bar.left + 40, bar.centerY() - text.getHeight() / 2f);
            y += ctaHeight + 24;
        }
        for (ArrayList<Integer> row : rows) {
            float width = 0;
            for (int i : row) width += widths[i];
            width += gap * (row.size() - 1);
            float x = (W + width) / 2f;
            for (int i : row) {
                String[] item = contacts.get(i);
                RectF circle = new RectF(x - iconSize, y + (rowHeight - iconSize) / 2, x, y + (rowHeight + iconSize) / 2);
                Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
                dot.setColor(accent);
                canvas.drawOval(circle, dot);
                icon(item[0], circle);
                StaticLayout text = layout("‎" + item[1], contactSize, ink, bold, (int) (widths[i] - iconSize - 12) + 2, 1, Layout.Alignment.ALIGN_NORMAL);
                put(text, circle.left - 12 - (widths[i] - iconSize - 12), y + (rowHeight - text.getHeight()) / 2f);
                x -= widths[i] + gap;
            }
            y += rowHeight + 10;
        }
        return footerTop;
    }

    private void icon(String kind, RectF circle) {
        int id = "chat".equals(kind) ? R.drawable.ic_dev_chat : "web".equals(kind) ? R.drawable.ic_dev_web : R.drawable.ic_dev_call;
        Drawable drawable = context.getDrawable(id);
        if (drawable == null) return;
        drawable = drawable.mutate();
        drawable.setTint(onAccent);
        float inset = circle.width() * 0.24f;
        drawable.setBounds(Math.round(circle.left + inset), Math.round(circle.top + inset), Math.round(circle.right - inset), Math.round(circle.bottom - inset));
        drawable.draw(canvas);
    }

    // ------------------------------------------------------------------ templates

    private void product(float top, float bottom) {
        float width = W - 2 * M;
        boolean story = H >= 1900;
        StaticLayout head = fit(s("headline"), story ? 84 : 70, 40, black, ink, (int) width, 2, Layout.Alignment.ALIGN_NORMAL);
        StaticLayout sub = s("subtitle").isEmpty() ? null : layout(s("subtitle"), story ? 40 : 35, muted, regular, (int) width, 2, Layout.Alignment.ALIGN_NORMAL);
        String offer = on("showOffer") ? s("offer") : "";
        List<String> chips = on("showFeatures") ? new ArrayList<>(PostContent.lines(spec.optString("features"), 4)) : new ArrayList<>();
        Bitmap photo = photos.isEmpty() ? null : photos.get(0);
        boolean price = on("showPrice") && !s("price").isEmpty();
        float area = bottom - top, minPhoto = area * (H <= 1080 ? 0.44f : 0.42f);
        float chipsHeight, textHeight;
        while (true) {
            // The product photo keeps its share: drop the last feature chips, then the subtitle, when the text runs long.
            chipsHeight = chips.isEmpty() ? 0 : chips(chips, M, W - M, 0, false);
            textHeight = height(head) + (sub == null ? 0 : 10 + sub.getHeight()) + (offer.isEmpty() ? 0 : 22 + 66) + (chips.isEmpty() ? 0 : 24 + chipsHeight);
            if (photo == null || textHeight <= area - minPhoto - 34) break;
            if (!chips.isEmpty()) chips.remove(chips.size() - 1);
            else if (sub != null) sub = null;
            else break;
        }
        float y;
        if (photo != null) {
            float photoHeight = Math.max(minPhoto, Math.min(area - textHeight - 34, area * 0.66f));
            RectF frame = new RectF(M, top, W - M, top + photoHeight);
            picture(photo, frame, 44);
            float sticker = H <= 1080 ? 78 : 92;
            discountSticker(frame.right - 24 - 2 * sticker, frame.top + 24, sticker);
            if (price) priceTag(frame.left + 26, frame.bottom - 26, true);
            y = frame.bottom + Math.max(30, (bottom - frame.bottom - textHeight) / 2f);
        } else {
            float priceHeight = price ? 200 : 0;
            y = top + Math.max(0, (bottom - top - textHeight - priceHeight) / 2f);
            if (price) {
                discountSticker(W - M - 150, y - 10, 80);
                priceTag(M, y + 170, false);
                y += priceHeight;
            }
        }
        y += put(head, M, y);
        if (sub != null) y += 10 + put(sub, M, y + 10);
        if (!offer.isEmpty()) {
            y += 22;
            offerPill(offer, y);
            y += 66;
        }
        if (!chips.isEmpty()) chips(chips, M, W - M, y + 24, true);
    }

    private void features(float top, float bottom) {
        float width = W - 2 * M;
        boolean story = H >= 1900;
        StaticLayout head = fit(s("headline"), story ? 78 : 64, 40, black, ink, (int) width, 2, Layout.Alignment.ALIGN_NORMAL);
        StaticLayout sub = s("subtitle").isEmpty() ? null : layout(s("subtitle"), 34, muted, regular, (int) width, 2, Layout.Alignment.ALIGN_NORMAL);
        float y = top + put(head, M, top);
        if (sub != null) y += 8 + put(sub, M, y + 8);
        y += 28;
        boolean price = on("showPrice") && !s("price").isEmpty();
        float stripTop = price ? bottom - 120 : bottom;
        if (price) priceStrip(new RectF(M, stripTop, W - M, bottom));
        float listBottom = price ? stripTop - 24 : bottom;
        Bitmap photo = photos.isEmpty() ? null : photos.get(0);
        List<String> features = on("showFeatures") ? PostContent.lines(spec.optString("features"), 6) : new ArrayList<>();
        List<String> specs = on("showSpecs") ? PostContent.lines(spec.optString("specs"), 7) : new ArrayList<>();
        if (photo != null && (story || (features.isEmpty() && specs.isEmpty()))) {
            float photoHeight = features.isEmpty() && specs.isEmpty() ? listBottom - y : (listBottom - y) * 0.40f;
            RectF frame = new RectF(M, y, W - M, y + photoHeight);
            picture(photo, frame, 36);
            discountSticker(frame.left + 22, frame.top + 22, 84);
            y = frame.bottom + 28;
            lists(features, specs, new RectF(M, y, W - M, listBottom));
        } else if (photo != null) {
            RectF frame = new RectF(M, y, W / 2f - 14, listBottom);
            picture(photo, frame, 36);
            discountSticker(frame.left + 18, frame.top + 18, 78);
            lists(features, specs, new RectF(W / 2f + 14, y, W - M, listBottom));
        } else {
            lists(features, specs, new RectF(M, y, W - M, listBottom));
        }
    }

    /** Feature checklist then a specification table, shrinking the type until both fit the box. */
    private void lists(List<String> features, List<String> specs, RectF box) {
        if (features.isEmpty() && specs.isEmpty()) return;
        for (float scale = 1f; scale >= 0.55f; scale -= 0.07f) {
            float needed = listsHeight(features, specs, box.right, box.width(), scale, false, box.top);
            if (needed <= box.height() || scale - 0.07f < 0.55f) {
                float start = box.top + Math.max(0, (box.height() - needed) / 2f);
                listsHeight(features, specs, box.right, box.width(), scale, true, start);
                return;
            }
        }
    }

    private float listsHeight(List<String> features, List<String> specs, float right, float width, float scale, boolean drawIt, float y0) {
        float y = y0;
        float x = right - width;
        float check = 44 * scale;
        for (String feature : features) {
            StaticLayout text = layout(feature, 33 * scale, ink, bold, (int) (width - check - 18), 2, Layout.Alignment.ALIGN_NORMAL);
            float rowHeight = Math.max(check, text.getHeight());
            if (drawIt) {
                RectF dot = new RectF(right - check, y + (rowHeight - check) / 2, right, y + (rowHeight + check) / 2);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(accent);
                canvas.drawOval(dot, paint);
                tick(dot);
                put(text, x, y + (rowHeight - text.getHeight()) / 2f);
            }
            y += rowHeight + 18 * scale;
        }
        if (!specs.isEmpty()) {
            if (!features.isEmpty()) y += 10 * scale;
            float tableTop = y, pad = 22 * scale;
            float[] rowHeights = new float[specs.size()];
            StaticLayout[][] cells = new StaticLayout[specs.size()][2];
            for (int i = 0; i < specs.size(); i++) {
                String[] parts = PostContent.spec(specs.get(i));
                String label = parts[0], value = parts.length > 1 ? parts[1] : "";
                float labelWidth = value.isEmpty() ? width - 2 * pad : (width - 2 * pad) * 0.46f;
                cells[i][0] = layout(label, 27 * scale, muted, regular, (int) labelWidth, 2, Layout.Alignment.ALIGN_NORMAL);
                cells[i][1] = value.isEmpty() ? null : layout(value, 29 * scale, ink, bold, (int) ((width - 2 * pad) * 0.52f), 2, Layout.Alignment.ALIGN_OPPOSITE);
                rowHeights[i] = Math.max(cells[i][0].getHeight(), cells[i][1] == null ? 0 : cells[i][1].getHeight()) + 20 * scale;
            }
            float tableHeight = pad;
            for (float h : rowHeights) tableHeight += h;
            tableHeight += pad - 20 * scale;
            if (drawIt) {
                RectF table = new RectF(x, tableTop, right, tableTop + tableHeight);
                surface(table, 28);
                float rowY = tableTop + pad - 10 * scale;
                for (int i = 0; i < specs.size(); i++) {
                    float middle = rowY + rowHeights[i] / 2f;
                    put(cells[i][0], right - pad - cells[i][0].getWidth(), middle - cells[i][0].getHeight() / 2f);
                    if (cells[i][1] != null) put(cells[i][1], x + pad, middle - cells[i][1].getHeight() / 2f);
                    if (i < specs.size() - 1) {
                        Paint line = new Paint();
                        line.setColor(cardLine);
                        canvas.drawRect(x + pad, rowY + rowHeights[i] - 1, right - pad, rowY + rowHeights[i], line);
                    }
                    rowY += rowHeights[i];
                }
            }
            y = tableTop + tableHeight;
        }
        return y - y0;
    }

    private void gallery(float top, float bottom) {
        float width = W - 2 * M;
        StaticLayout head = fit(s("headline"), H >= 1900 ? 76 : 62, 40, black, ink, (int) width, 2, Layout.Alignment.ALIGN_NORMAL);
        StaticLayout sub = s("subtitle").isEmpty() ? null : layout(s("subtitle"), 33, muted, regular, (int) width, 2, Layout.Alignment.ALIGN_NORMAL);
        float y = top + put(head, M, top);
        if (sub != null) y += 8 + put(sub, M, y + 8);
        y += 26;
        boolean price = on("showPrice") && !s("price").isEmpty();
        String offer = on("showOffer") ? s("offer") : "";
        float gridBottom = bottom;
        if (price) {
            priceStrip(new RectF(M, bottom - 120, W - M, bottom));
            gridBottom = bottom - 144;
        }
        if (!offer.isEmpty()) {
            offerPill(offer, gridBottom - 66);
            gridBottom -= 90;
        }
        RectF grid = new RectF(M, y, W - M, gridBottom);
        int count = Math.min(6, photos.size());
        if (count == 0) {
            surface(grid, 36);
            StaticLayout hint = layout("أضف صورًا للمنشور لتظهر هنا", 34, muted, bold, (int) grid.width(), 1, Layout.Alignment.ALIGN_CENTER);
            put(hint, grid.left, grid.centerY() - hint.getHeight() / 2f);
            return;
        }
        List<RectF> cells = cells(grid, count, H >= 1900);
        for (int i = 0; i < cells.size(); i++) cover(photos.get(i), cells.get(i), 28);
        discountSticker(cells.get(0).left + 18, cells.get(0).top + 18, 82);
    }

    private List<RectF> cells(RectF g, int count, boolean tall) {
        ArrayList<RectF> out = new ArrayList<>();
        float gap = 16, w = g.width(), h = g.height();
        switch (count) {
            case 1:
                out.add(g);
                break;
            case 2:
                if (tall || h > w * 1.1f) {
                    out.add(new RectF(g.left, g.top, g.right, g.top + (h - gap) / 2));
                    out.add(new RectF(g.left, g.top + (h + gap) / 2, g.right, g.bottom));
                } else {
                    out.add(new RectF(g.left + (w + gap) / 2, g.top, g.right, g.bottom));
                    out.add(new RectF(g.left, g.top, g.left + (w - gap) / 2, g.bottom));
                }
                break;
            case 3:
                out.add(new RectF(g.left, g.top, g.right, g.top + (h - gap) * 0.6f));
                out.add(new RectF(g.left + (w + gap) / 2, g.top + (h - gap) * 0.6f + gap, g.right, g.bottom));
                out.add(new RectF(g.left, g.top + (h - gap) * 0.6f + gap, g.left + (w - gap) / 2, g.bottom));
                break;
            case 4:
                for (int r = 0; r < 2; r++) for (int c = 0; c < 2; c++) out.add(cell(g, 2, 2, r, c, gap));
                break;
            case 5:
                for (int c = 0; c < 2; c++) out.add(cell(g, 2, 2, 0, c, gap));
                RectF bottomRow = new RectF(g.left, g.top + (h + gap) / 2, g.right, g.bottom);
                for (int c = 0; c < 3; c++) out.add(cell(bottomRow, 1, 3, 0, c, gap));
                break;
            default:
                int rows = tall ? 3 : 2, cols = tall ? 2 : 3;
                for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) out.add(cell(g, rows, cols, r, c, gap));
        }
        return out;
    }

    /** Cell in a grid, numbered from the right so the first photo sits where Arabic readers start. */
    private RectF cell(RectF g, int rows, int cols, int r, int c, float gap) {
        float cw = (g.width() - gap * (cols - 1)) / cols, ch = (g.height() - gap * (rows - 1)) / rows;
        float right = g.right - c * (cw + gap), top = g.top + r * (ch + gap);
        return new RectF(right - cw, top, right, top + ch);
    }

    private void compare(float top, float bottom) {
        float width = W - 2 * M;
        StaticLayout head = fit(s("headline"), H >= 1900 ? 72 : 60, 38, black, ink, (int) width, 2, Layout.Alignment.ALIGN_CENTER);
        float y = top + put(head, M, top) + 24;
        List<String> rows = on("showSpecs") ? PostContent.lines(spec.optString("specs"), 7) : new ArrayList<>();
        float area = bottom - y;
        float cardsHeight = rows.isEmpty() ? area : area * (H >= 1900 ? 0.48f : 0.52f);
        float cardWidth = (width - 40) / 2;
        String[] names = {s("nameA").isEmpty() ? "المنتج الأول" : s("nameA"), s("nameB").isEmpty() ? "المنتج الثاني" : s("nameB")};
        for (int i = 0; i < 2; i++) {
            float right = W - M - i * (cardWidth + 40);
            RectF cardBox = new RectF(right - cardWidth, y, right, y + cardsHeight);
            surface(cardBox, 32);
            StaticLayout name = fit(names[i], 36, 24, black, ink, (int) cardWidth - 32, 2, Layout.Alignment.ALIGN_CENTER);
            RectF photoBox = new RectF(cardBox.left + 16, cardBox.top + 16, cardBox.right - 16, cardBox.bottom - name.getHeight() - 34);
            if (i < photos.size()) picture(photos.get(i), photoBox, 24);
            put(name, cardBox.left + 16, cardBox.bottom - name.getHeight() - 16);
        }
        RectF vs = new RectF(W / 2f - 52, y + cardsHeight / 2 - 52, W / 2f + 52, y + cardsHeight / 2 + 52);
        shadowed(vs, 52, accent);
        StaticLayout versus = layout("VS", 40, onAccent, black, 104, 1, Layout.Alignment.ALIGN_CENTER);
        put(versus, vs.left, vs.centerY() - versus.getHeight() / 2f);
        if (rows.isEmpty()) return;
        float tableTop = y + cardsHeight + 24, tableBottom = bottom;
        for (float scale = 1f; scale >= 0.55f; scale -= 0.07f) {
            float needed = compareRows(rows, tableTop, width, scale, false, cardWidth);
            if (needed <= tableBottom - tableTop || scale - 0.07f < 0.55f) {
                compareRows(rows, tableTop, width, scale, true, cardWidth);
                return;
            }
        }
    }

    private float compareRows(List<String> rows, float top, float width, float scale, boolean drawIt, float cardWidth) {
        float y = top, pad = 18 * scale;
        float column = width * 0.30f, middle = width - 2 * column;
        if (drawIt) {
            float total = compareRows(rows, top, width, scale, false, cardWidth);
            surface(new RectF(M, top, W - M, top + total), 28);
        }
        y += pad;
        for (int i = 0; i < rows.size(); i++) {
            String[] parts = PostContent.spec(rows.get(i));
            String label = parts[0], a = parts.length > 1 ? parts[1] : "", b = parts.length > 2 ? parts[2] : "";
            StaticLayout la = layout(a, 29 * scale, ink, bold, (int) column - 16, 2, Layout.Alignment.ALIGN_CENTER);
            StaticLayout lm = layout(label, 26 * scale, muted, regular, (int) middle - 16, 2, Layout.Alignment.ALIGN_CENTER);
            StaticLayout lb = layout(b, 29 * scale, ink, bold, (int) column - 16, 2, Layout.Alignment.ALIGN_CENTER);
            float h = Math.max(lm.getHeight(), Math.max(la.getHeight(), lb.getHeight())) + 18 * scale;
            if (drawIt) {
                float mid = y + h / 2f;
                put(la, W - M - column + 8, mid - la.getHeight() / 2f);
                put(lm, M + column + 8, mid - lm.getHeight() / 2f);
                put(lb, M + 8, mid - lb.getHeight() / 2f);
                if (i < rows.size() - 1) {
                    Paint line = new Paint();
                    line.setColor(cardLine);
                    canvas.drawRect(M + pad, y + h - 1, W - M - pad, y + h, line);
                }
            }
            y += h;
        }
        return y + pad - 18 * scale - top + 18 * scale;
    }

    private void service(float top, float bottom) {
        float width = W - 2 * M;
        boolean withPhoto = !photos.isEmpty();
        float y = withPhoto ? Math.max(top, H * 0.30f) : top + 10;
        StaticLayout head = fit(s("headline"), H >= 1900 ? 90 : 76, 44, black, ink, (int) width, 2, Layout.Alignment.ALIGN_NORMAL);
        y += put(head, M, y);
        if (!s("subtitle").isEmpty()) {
            StaticLayout sub = layout(s("subtitle"), 36, muted, regular, (int) width, 3, Layout.Alignment.ALIGN_NORMAL);
            y += 10 + put(sub, M, y + 10);
        }
        y += 26;
        float pillsBottom = bottom;
        boolean price = on("showPrice") && !s("price").isEmpty();
        String offer = on("showOffer") ? s("offer") : "";
        if (price || !offer.isEmpty()) {
            pillsBottom = bottom - 86;
            float x = W - M;
            if (price) {
                String text = "يبدأ من  " + PostContent.money(s("price")) + " " + s("currency");
                x = solidPill(text, x, bottom - 70) - 18;
            }
            if (!offer.isEmpty()) offerPillAt(offer, x, bottom - 70, true);
        }
        List<String> services = on("showFeatures") ? PostContent.lines(spec.optString("features"), 6) : new ArrayList<>();
        if (services.isEmpty()) return;
        int cols = H >= 1900 || services.size() <= 3 ? 1 : 2;
        int rows = (services.size() + cols - 1) / cols;
        float gap = 18, cardHeight = Math.min(140, (pillsBottom - y - gap * (rows - 1)) / rows);
        float cardWidth = (width - gap * (cols - 1)) / cols;
        for (int i = 0; i < services.size(); i++) {
            int r = i / cols, c = i % cols;
            float right = W - M - c * (cardWidth + gap), cardTop = y + r * (cardHeight + gap);
            RectF box = new RectF(right - cardWidth, cardTop, right, cardTop + cardHeight);
            surface(box, 26);
            float badge = Math.min(64, cardHeight - 28);
            RectF number = new RectF(box.right - 18 - badge, box.centerY() - badge / 2, box.right - 18, box.centerY() + badge / 2);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(accent);
            canvas.drawOval(number, paint);
            StaticLayout digit = layout(String.valueOf(i + 1), badge * 0.5f, onAccent, black, (int) badge, 1, Layout.Alignment.ALIGN_CENTER);
            put(digit, number.left, number.centerY() - digit.getHeight() / 2f);
            StaticLayout text = fit(services.get(i), 32, 22, bold, ink, (int) (cardWidth - badge - 54), 2, Layout.Alignment.ALIGN_NORMAL);
            put(text, box.left + 18, box.centerY() - text.getHeight() / 2f);
        }
    }

    private void ornaments() {
        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(3);
        ring.setColor(alpha(accent, 0.35f));
        canvas.drawCircle(W * 0.5f, H * 0.5f, W * 0.46f, ring);
        ring.setColor(alpha(accent, 0.18f));
        canvas.drawCircle(W * 0.5f, H * 0.5f, W * 0.40f, ring);
        float[][] stars = {{0.12f, 0.16f, 22}, {0.86f, 0.22f, 16}, {0.20f, 0.78f, 14}, {0.80f, 0.70f, 24}, {0.52f, 0.10f, 12}, {0.08f, 0.52f, 10}, {0.93f, 0.48f, 12}};
        Paint star = new Paint(Paint.ANTI_ALIAS_FLAG);
        star.setColor(alpha(accent, 0.85f));
        for (float[] s : stars) canvas.drawPath(sparkle(W * s[0], H * s[1], s[2]), star);
    }

    private Path sparkle(float cx, float cy, float r) {
        Path path = new Path();
        path.moveTo(cx, cy - r);
        path.quadTo(cx, cy, cx + r, cy);
        path.quadTo(cx, cy, cx, cy + r);
        path.quadTo(cx, cy, cx - r, cy);
        path.quadTo(cx, cy, cx, cy - r);
        path.close();
        return path;
    }

    private void occasion(float top, float bottom) {
        float width = W - 2 * M - 80;
        boolean showLogo = on("showLogo") && logo != null;
        String brand = on("showBrand") ? s("brand") : "";
        StaticLayout head = fit(s("headline").isEmpty() ? "كل عام وأنتم بخير" : s("headline"), H >= 1900 ? 120 : 104, 54, black, ink, (int) width, 2, Layout.Alignment.ALIGN_CENTER);
        StaticLayout sub = s("subtitle").isEmpty() ? null : layout(s("subtitle"), 38, muted, regular, (int) width, 4, Layout.Alignment.ALIGN_CENTER);
        StaticLayout name = brand.isEmpty() ? null : fit(brand, 42, 28, bold, accent, (int) width, 1, Layout.Alignment.ALIGN_CENTER);
        float logoSize = showLogo ? (H >= 1900 ? 220 : 170) : 0;
        float total = logoSize + (showLogo ? 40 : 0) + head.getHeight() + 50 + (sub == null ? 0 : sub.getHeight() + 30) + (name == null ? 0 : name.getHeight() + 20);
        float y = top + Math.max(0, (bottom - top - total) / 2f);
        float x = (W - width) / 2f;
        if (showLogo) {
            RectF box = new RectF(W / 2f - logoSize / 2, y, W / 2f + logoSize / 2, y + logoSize);
            shadowed(box, logoSize * 0.24f, 0xFFFFFFFF);
            contain(logo, new RectF(box.left + 18, box.top + 18, box.right - 18, box.bottom - 18));
            y += logoSize + 40;
        }
        y += put(head, x, y);
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setColor(accent);
        float mid = y + 25;
        canvas.drawRect(W / 2f - 170, mid - 1.5f, W / 2f - 26, mid + 1.5f, line);
        canvas.drawRect(W / 2f + 26, mid - 1.5f, W / 2f + 170, mid + 1.5f, line);
        Path diamond = new Path();
        diamond.moveTo(W / 2f, mid - 12);
        diamond.lineTo(W / 2f + 12, mid);
        diamond.lineTo(W / 2f, mid + 12);
        diamond.lineTo(W / 2f - 12, mid);
        diamond.close();
        canvas.drawPath(diamond, line);
        y += 50;
        if (sub != null) y += put(sub, x, y) + 30;
        if (name != null) put(name, x, y + 20);
    }

    // ------------------------------------------------------------------ blocks

    private void discountSticker(float left, float top, float radius) {
        if (!on("showDiscount")) return;
        String label = PostContent.discount(spec);
        if (label.isEmpty()) return;
        RectF circle = new RectF(left, top, left + radius * 2, top + radius * 2);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShadowLayer(18, 0, 8, alpha(0xFF000000, 0.3f));
        paint.setColor(0xFFE53935);
        canvas.drawOval(circle, paint);
        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(4);
        ring.setColor(0x99FFFFFF);
        canvas.drawOval(new RectF(circle.left + 10, circle.top + 10, circle.right - 10, circle.bottom - 10), ring);
        StaticLayout text = fit(label.replace("خصم ", "خصم\n"), radius * 0.46f, radius * 0.24f, black, 0xFFFFFFFF, (int) (radius * 1.5f), 2, Layout.Alignment.ALIGN_CENTER);
        put(text, circle.centerX() - radius * 0.75f, circle.centerY() - text.getHeight() / 2f);
    }

    /** Price card; with {@code anchorBottom} the card's bottom sits at {@code y}, otherwise its top does. */
    private void priceTag(float left, float y, boolean anchorBottom) {
        String currency = s("currency").isEmpty() ? "ر.س" : s("currency");
        CharSequence now = priceText(PostContent.money(s("price")), currency, 66, false);
        StaticLayout main = layout(now, 66, onAccent, black, 640, 1, Layout.Alignment.ALIGN_CENTER);
        StaticLayout old = s("oldPrice").isEmpty() ? null : layout(priceText(PostContent.money(s("oldPrice")), currency, 32, true), 32, alpha(onAccent, 0.75f), bold, 640, 1, Layout.Alignment.ALIGN_CENTER);
        float width = Math.max(lineWidth(main), old == null ? 0 : lineWidth(old)) + 64;
        float height = main.getHeight() + (old == null ? 0 : old.getHeight() - 6) + 30;
        float top = anchorBottom ? y - height : y - height;
        RectF tag = new RectF(left, top, left + width, top + height);
        shadowed(tag, 30, accent);
        float ty = tag.top + 15;
        if (old != null) {
            put(old, tag.left, ty, tag.width());
            ty += old.getHeight() - 6;
        }
        put(main, tag.left, ty, tag.width());
    }

    private CharSequence priceText(String amount, String currency, float size, boolean struck) {
        SpannableStringBuilder text = new SpannableStringBuilder("‎" + amount + " ");
        int start = text.length();
        text.append(currency);
        text.setSpan(new RelativeSizeSpan(struck ? 0.75f : 0.5f), start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (struck) text.setSpan(new StrikethroughSpan(), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    /** Full-width price bar: label, new price, old price and discount. */
    private void priceStrip(RectF bar) {
        shadowed(bar, 30, accent);
        String currency = s("currency").isEmpty() ? "ر.س" : s("currency");
        StaticLayout label = tight("السعر", 30, alpha(onAccent, 0.85f), bold, 200);
        put(label, bar.right - 28 - label.getWidth(), bar.centerY() - label.getHeight() / 2f);
        StaticLayout main = tight(priceText(PostContent.money(s("price")), currency, 58, false), 58, onAccent, black, 520);
        float mainRight = bar.right - 28 - label.getWidth() - 22;
        put(main, mainRight - main.getWidth(), bar.centerY() - main.getHeight() / 2f);
        float left = bar.left + 26;
        String discount = on("showDiscount") ? PostContent.discount(spec) : "";
        if (!discount.isEmpty()) {
            StaticLayout d = tight(discount, 28, 0xFFFFFFFF, black, 300);
            RectF pill = new RectF(left, bar.centerY() - 30, left + d.getWidth() + 40, bar.centerY() + 30);
            Paint red = new Paint(Paint.ANTI_ALIAS_FLAG);
            red.setColor(0xFFE53935);
            canvas.drawRoundRect(pill, 30, 30, red);
            put(d, pill.left + 20, pill.centerY() - d.getHeight() / 2f);
            left = pill.right + 18;
        }
        if (!s("oldPrice").isEmpty()) {
            StaticLayout old = tight(priceText(PostContent.money(s("oldPrice")), currency, 30, true), 30, alpha(onAccent, 0.75f), bold, 400);
            if (left + old.getWidth() < mainRight - main.getWidth() - 20) put(old, left, bar.centerY() - old.getHeight() / 2f);
        }
    }

    /** Single-line layout exactly as wide as its text, so it can be placed by its edges. */
    private StaticLayout tight(CharSequence text, float size, int color, Typeface face, int maxWidth) {
        StaticLayout measured = layout(text, size, color, face, maxWidth, 1, Layout.Alignment.ALIGN_NORMAL);
        return layout(text, size, color, face, Math.min(maxWidth, (int) Math.ceil(lineWidth(measured)) + 2), 1, Layout.Alignment.ALIGN_NORMAL);
    }

    private void offerPill(String offer, float top) {
        offerPillAt(offer, W - M, top, true);
    }

    /** Outlined offer pill whose right edge is at {@code right}; returns its left edge. */
    private float offerPillAt(String offer, float right, float top, boolean outlined) {
        StaticLayout text = layout("⏰  " + offer, 30, light ? accent : lighten(accent, 0.1f), bold, (int) (W - 2 * M - 40), 1, Layout.Alignment.ALIGN_NORMAL);
        float width = Math.min(W - 2 * M, lineWidth(text) + 48);
        RectF pill = new RectF(right - width, top, right, top + 66);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(light ? alpha(accent, 0.10f) : alpha(0xFF000000, 0.35f));
        canvas.drawRoundRect(pill, 33, 33, fill);
        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(2.5f);
        stroke.setColor(accent);
        canvas.drawRoundRect(pill, 33, 33, stroke);
        StaticLayout fitted = layout("⏰  " + offer, 30, light ? accent : lighten(accent, 0.1f), bold, (int) width - 40, 1, Layout.Alignment.ALIGN_CENTER);
        put(fitted, pill.left + 20, pill.centerY() - fitted.getHeight() / 2f);
        return pill.left;
    }

    /** Solid accent pill whose right edge is at {@code right}; returns its left edge. */
    private float solidPill(String label, float right, float top) {
        StaticLayout text = layout(label, 30, onAccent, black, 700, 1, Layout.Alignment.ALIGN_CENTER);
        float width = lineWidth(text) + 52;
        RectF pill = new RectF(right - width, top, right, top + 66);
        shadowed(pill, 33, accent);
        put(text, pill.left, pill.centerY() - text.getHeight() / 2f, pill.width());
        return pill.left;
    }

    /** Wrapped feature chips from the right; returns their total height. */
    private float chips(List<String> items, float left, float right, float top, boolean drawIt) {
        float x = right, y = top, rowHeight = 62, gap = 14;
        for (String item : items) {
            StaticLayout measured = layout("✓ " + item, 28, ink, bold, (int) (right - left - 44), 1, Layout.Alignment.ALIGN_NORMAL);
            StaticLayout text = layout("✓ " + item, 28, ink, bold, (int) Math.ceil(lineWidth(measured)) + 2, 1, Layout.Alignment.ALIGN_NORMAL);
            float width = Math.min(right - left, text.getWidth() + 44);
            if (x - width < left - 1) {
                x = right;
                y += rowHeight + gap;
            }
            if (drawIt) {
                RectF chip = new RectF(x - width, y, x, y + rowHeight);
                surface(chip, 31);
                put(text, chip.left + 22, chip.centerY() - text.getHeight() / 2f);
            }
            x -= width + gap;
        }
        return y - top + rowHeight;
    }

    // ------------------------------------------------------------------ drawing helpers

    /** Photo on a card: whole product visible ("contain", default) or filling the frame ("cover"). */
    private void picture(Bitmap photo, RectF frame, float radius) {
        if ("cover".equals(spec.optString("fit", "contain"))) {
            cover(photo, frame, radius);
            return;
        }
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShadowLayer(30, 0, 14, alpha(0xFF000000, light ? 0.14f : 0.35f));
        paint.setColor(0xFFFFFFFF);
        canvas.drawRoundRect(frame, radius, radius, paint);
        canvas.save();
        Path clip = new Path();
        clip.addRoundRect(frame, radius, radius, Path.Direction.CW);
        canvas.clipPath(clip);
        float inset = Math.min(frame.width(), frame.height()) * 0.05f;
        contain(photo, new RectF(frame.left + inset, frame.top + inset, frame.right - inset, frame.bottom - inset));
        canvas.restore();
    }

    private void cover(Bitmap photo, RectF frame, float radius) {
        Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadow.setShadowLayer(30, 0, 14, alpha(0xFF000000, light ? 0.16f : 0.4f));
        shadow.setColor(bgBottom);
        canvas.drawRoundRect(frame, radius, radius, shadow);
        canvas.save();
        Path clip = new Path();
        clip.addRoundRect(frame, radius, radius, Path.Direction.CW);
        canvas.clipPath(clip);
        canvas.drawBitmap(photo, cropRect(photo, frame.width() / frame.height()), frame, new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.restore();
    }

    private Rect cropRect(Bitmap photo, float ratio) {
        int w = photo.getWidth(), h = photo.getHeight();
        if ((float) w / h > ratio) {
            int cw = Math.round(h * ratio);
            return new Rect((w - cw) / 2, 0, (w + cw) / 2, h);
        }
        int ch = Math.round(w / ratio);
        return new Rect(0, (h - ch) / 2, w, (h + ch) / 2);
    }

    private void contain(Bitmap image, RectF box) {
        float scale = Math.min(box.width() / image.getWidth(), box.height() / image.getHeight());
        float w = image.getWidth() * scale, h = image.getHeight() * scale;
        RectF dest = new RectF(box.centerX() - w / 2, box.centerY() - h / 2, box.centerX() + w / 2, box.centerY() + h / 2);
        canvas.drawBitmap(image, null, dest, new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
    }

    private void surface(RectF box, float radius) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (light) paint.setShadowLayer(22, 0, 8, 0x1F000000);
        paint.setColor(card);
        canvas.drawRoundRect(box, radius, radius, paint);
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(2);
        line.setColor(cardLine);
        canvas.drawRoundRect(box, radius, radius, line);
    }

    private void shadowed(RectF box, float radius, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShadowLayer(20, 0, 8, alpha(0xFF000000, 0.28f));
        paint.setColor(color);
        canvas.drawRoundRect(box, radius, radius, paint);
    }

    private void tick(RectF dot) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dot.width() * 0.12f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(onAccent);
        Path path = new Path();
        path.moveTo(dot.left + dot.width() * 0.28f, dot.top + dot.height() * 0.52f);
        path.lineTo(dot.left + dot.width() * 0.44f, dot.top + dot.height() * 0.67f);
        path.lineTo(dot.left + dot.width() * 0.73f, dot.top + dot.height() * 0.36f);
        canvas.drawPath(path, paint);
    }

    private TextPaint paint(float size, int color, Typeface face) {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        paint.setTypeface(face);
        paint.setTextSize(size);
        paint.setColor(color);
        return paint;
    }

    private StaticLayout layout(CharSequence text, float size, int color, Typeface face, int width, int maxLines, Layout.Alignment align) {
        // Isolate "25%" so the percent sign stays after the digits inside Arabic text.
        CharSequence value = text instanceof String ? DisplayNumbers.latin((String) text).replaceAll("(\\d+(?:\\.\\d+)?%)", "⁦$1⁩") : text;
        return StaticLayout.Builder.obtain(value, 0, value.length(), paint(size, color, face), Math.max(1, width))
                .setAlignment(align)
                .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL)
                .setIncludePad(false)
                .setLineSpacing(0, 0.98f)
                .setMaxLines(maxLines)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build();
    }

    /** Largest size between {@code max} and {@code min} at which the text fits in {@code maxLines} without being cut. */
    private StaticLayout fit(String text, float max, float min, Typeface face, int color, int width, int maxLines, Layout.Alignment align) {
        StaticLayout layout = null;
        for (float size = max; size >= min; size -= 3) {
            layout = layout(text, size, color, face, width, maxLines + 1, align);
            if (layout.getLineCount() <= maxLines) return layout(text, size, color, face, width, maxLines, align);
        }
        return layout(text, min, color, face, width, maxLines, align);
    }

    private float lineWidth(StaticLayout layout) {
        float width = 0;
        for (int i = 0; i < layout.getLineCount(); i++) width = Math.max(width, layout.getLineWidth(i));
        return width;
    }

    private float height(StaticLayout layout) {
        return layout.getText().length() == 0 ? 0 : layout.getHeight();
    }

    private float put(StaticLayout layout, float x, float y) {
        if (layout.getText().length() == 0) return 0;
        canvas.save();
        canvas.translate(x, y);
        layout.draw(canvas);
        canvas.restore();
        return layout.getHeight();
    }

    /** Draws a centered layout inside a box of the given width starting at {@code x}. */
    private void put(StaticLayout layout, float x, float y, float boxWidth) {
        put(layout, x + (boxWidth - layout.getWidth()) / 2f, y);
    }

    private static int alpha(int color, float a) {
        return (Math.round(a * 255) << 24) | (color & 0x00FFFFFF);
    }

    private static int lighten(int color, float amount) {
        int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        return Color.rgb(r + Math.round((255 - r) * amount), g + Math.round((255 - g) * amount), b + Math.round((255 - b) * amount));
    }
}
