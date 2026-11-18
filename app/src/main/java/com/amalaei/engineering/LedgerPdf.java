package com.amalaei.engineering;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;

/**
 * A4 ledger statement: entries in date order with a running cash balance that carries across pages,
 * separate in/out columns, a totals row and signature boxes on the last page.
 */
final class LedgerPdf {
    private static final int TEAL = -15434365, NAVY = -15717821, INK = -14996678, GRAY = -10062718, EXPENSE = -4959479;
    private static final int PALE = -788488, ROW_A = -460037, ROW_B = -1116939, RULE = -2300440, TOTAL_BG = -2034959, WHITE = -1;
    private static final float LEFT = 34, RIGHT = 562, WIDTH = RIGHT - LEFT, TABLE_BOTTOM = 772, HEAD_H = 24, PAD = 6;
    private static final float FIRST_TABLE_TOP = 284, NEXT_TABLE_TOP = 70, CLOSING_H = 168;
    /** Column edges from left to right: balance, out, in, type, description, date. */
    private static final float[] EDGES = {34, 104, 174, 244, 314, 490, 562};
    private static final String[] HEADS = {"الرصيد النقدي", "منصرف", "وارد", "النوع", "البيان والملاحظة", "التاريخ"};
    private static final String CURRENCY = " ر.س";

    private static final class Row {
        String date = "", time = "", type = "", in = "", out = "", balance = "";
        StaticLayout description;
        float height;
        boolean opening, negative;
    }

    private LedgerPdf() {
    }

    static File create(Context context, JSONObject book, List<JSONObject> allEntries, LedgerFilter filter) throws Exception {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Cairo.ttf");
        Typeface bold = Typeface.create(font, Typeface.BOLD);
        ArrayList<JSONObject> entries = filter.periodEntries(allEntries);
        boolean period = filter.hasPeriod();
        BigDecimal carried = period ? LedgerMath.balanceBefore(book, allEntries, filter.from) : LedgerMath.opening(book);
        BigDecimal[] running = LedgerMath.running(carried, entries);
        BigDecimal closing = running.length == 0 ? carried : running[running.length - 1];
        SimpleDateFormat day = new SimpleDateFormat("dd/MM/yyyy", Locale.US), clock = new SimpleDateFormat("HH:mm", Locale.US);
        String carriedLabel = period ? "رصيد أول المدة" : "رصيد افتتاحي";

        ArrayList<Row> rows = new ArrayList<>();
        Row opening = new Row();
        opening.opening = true;
        long openingAt = period ? filter.from : book.optLong("createdAt", entries.isEmpty() ? System.currentTimeMillis() : entries.get(0).optLong("at"));
        opening.date = day.format(new Date(openingAt));
        opening.type = "—";
        opening.balance = amount(carried);
        opening.negative = carried.signum() < 0;
        opening.description = layout(bold, period ? "رصيد مرحّل من الفترة السابقة" : "الرصيد الافتتاحي للدفتر", 9.5f, NAVY, descriptionWidth(), 2);
        opening.height = rowHeight(opening.description, false);
        rows.add(opening);
        for (int i = 0; i < entries.size(); i++) {
            JSONObject entry = entries.get(i);
            Row row = new Row();
            boolean in = "in".equals(entry.optString("kind"));
            Date at = new Date(entry.optLong("at"));
            row.date = day.format(at);
            row.time = clock.format(at);
            row.type = in ? "تحصيل " + LedgerMath.methodLabel(entry.optString("paymentMethod", "cash")) : "مصروف";
            if (in) row.in = amount(LedgerMath.value(entry)); else row.out = amount(LedgerMath.value(entry));
            row.balance = amount(running[i]);
            row.negative = running[i].signum() < 0;
            String marker = entry.optString("marker").trim(), note = entry.optString("note").trim();
            String text = entry.optString("title") + (marker.isEmpty() ? "" : "  • " + marker) + (note.isEmpty() ? "" : "\nملاحظة: " + note);
            row.description = layout(font, text, 9.5f, INK, descriptionWidth(), note.isEmpty() ? 2 : 4);
            row.height = rowHeight(row.description, true);
            rows.add(row);
        }

        ArrayList<ArrayList<Row>> pages = new ArrayList<>();
        ArrayList<Row> page = new ArrayList<>();
        pages.add(page);
        float y = FIRST_TABLE_TOP + HEAD_H;
        for (Row row : rows) {
            if (y + row.height > TABLE_BOTTOM && !page.isEmpty()) {
                page = new ArrayList<>();
                pages.add(page);
                y = NEXT_TABLE_TOP + HEAD_H;
            }
            page.add(row);
            y += row.height;
        }
        if (y + CLOSING_H > TABLE_BOTTOM + 8) {
            // Carry the last few rows over so the totals and signatures never sit alone on a page.
            ArrayList<Row> last = new ArrayList<>();
            while (page.size() > 1 && last.size() < 3) last.add(0, page.remove(page.size() - 1));
            pages.add(last);
        }

        String issued = new SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.US).format(new Date());
        String range = period ? "من " + day.format(new Date(filter.from)) + " إلى " + day.format(new Date(filter.to)) : "جميع الحركات";
        PdfDocument document = new PdfDocument();
        try {
            for (int p = 0; p < pages.size(); p++) {
                PdfDocument.Page sheet = document.startPage(new PdfDocument.PageInfo.Builder(595, 842, p + 1).create());
                Canvas canvas = sheet.getCanvas();
                canvas.drawColor(WHITE);
                float top;
                if (p == 0) {
                    Quotation.rect(canvas, 0, 0, 595, 8, TEAL);
                    Quotation.rect(canvas, 0, 8, 595, 112, PALE);
                    Quotation.txt(canvas, bold, "كشف حركة دفتر", LEFT, 20, (int) WIDTH, 21, NAVY, false);
                    Quotation.txt(canvas, font, book.optString("name"), LEFT, 52, (int) WIDTH, 15, INK, false);
                    Quotation.txt(canvas, font, "الفترة: " + range + "     •     عدد الحركات: " + entries.size() + "     •     تاريخ الإصدار: " + issued, LEFT, 84, (int) WIDTH, 8.5f, GRAY, false);
                    summary(canvas, font, bold, carriedLabel, carried, entries, closing);
                    top = FIRST_TABLE_TOP;
                } else {
                    Quotation.rect(canvas, 0, 0, 595, 6, TEAL);
                    Quotation.rect(canvas, 0, 6, 595, 54, PALE);
                    Quotation.txt(canvas, bold, "كشف حركة دفتر — " + book.optString("name"), LEFT, 16, (int) WIDTH, 12, NAVY, false);
                    Quotation.txt(canvas, font, "الفترة: " + range + "  •  تابع", LEFT, 34, (int) WIDTH, 8, GRAY, false);
                    top = NEXT_TABLE_TOP;
                }
                ArrayList<Row> pageRows = pages.get(p);
                float cursor = top;
                if (!pageRows.isEmpty() || p == pages.size() - 1) {
                    tableHead(canvas, bold, cursor);
                    cursor += HEAD_H;
                }
                for (int r = 0; r < pageRows.size(); r++) {
                    Row row = pageRows.get(r);
                    drawRow(canvas, font, bold, row, cursor, row.opening ? TOTAL_BG : (r % 2 == 0 ? ROW_A : ROW_B));
                    cursor += row.height;
                }
                if (p == pages.size() - 1) closing(canvas, font, bold, cursor, entries, closing);
                Quotation.line(canvas, LEFT, 786, RIGHT, RULE);
                Quotation.txt(canvas, font, "تم إنشاؤه بتطبيق تواصل  •  صفحة " + (p + 1) + " من " + pages.size(), LEFT, 792, (int) WIDTH, 8.5f, GRAY, true);
                document.finishPage(sheet);
            }
            String safe = DisplayNumbers.latin(book.optString("name", "دفتر")).replaceAll("[\\\\/:*?\"<>|\\r\\n]+", " ").trim();
            if (safe.isEmpty()) safe = "دفتر";
            if (safe.length() > 70) safe = safe.substring(0, 70);
            String span = period ? " " + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(filter.from)) + " - " + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(filter.to)) : "";
            File file = new File(context.getCacheDir(), "كشف " + safe + span + " " + new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(new Date()) + ".pdf");
            try (FileOutputStream stream = new FileOutputStream(file)) {
                document.writeTo(stream);
            }
            return file;
        } finally {
            document.close();
        }
    }

    /** Numbers are forced left-to-right so a minus sign stays in front of the digits inside Arabic text. */
    private static String amount(BigDecimal value) {
        return "‎" + LedgerMath.grouped(value);
    }

    private static int descriptionWidth() {
        return (int) (EDGES[5] - EDGES[4] - 2 * PAD);
    }

    private static float rowHeight(StaticLayout description, boolean withTime) {
        return Math.max(withTime ? 34 : 22, description.getHeight()) + 12;
    }

    private static StaticLayout layout(Typeface font, String text, float size, int color, int width, int maxLines) {
        String value = DisplayNumbers.latin(text);
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        paint.setTypeface(font);
        paint.setTextSize(size);
        paint.setColor(color);
        return StaticLayout.Builder.obtain(value, 0, value.length(), paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL)
                .setIncludePad(false)
                .setMaxLines(maxLines)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build();
    }

    private static void tableHead(Canvas canvas, Typeface bold, float top) {
        Quotation.rounded(canvas, LEFT, top, RIGHT, top + HEAD_H, 6, TEAL);
        Quotation.rect(canvas, LEFT, top + HEAD_H - 6, RIGHT, top + HEAD_H, TEAL);
        for (int i = 0; i < HEADS.length; i++) {
            Quotation.txt(canvas, bold, HEADS[i], EDGES[i] + 2, top + 5, (int) (EDGES[i + 1] - EDGES[i] - 4), 9, WHITE, true);
        }
    }

    private static void drawRow(Canvas canvas, Typeface font, Typeface bold, Row row, float top, int background) {
        Quotation.rect(canvas, LEFT, top, RIGHT, top + row.height, background);
        float middle = top + row.height / 2;
        float single = middle - 9;
        if (row.time.isEmpty()) {
            Quotation.txt(canvas, font, row.date, EDGES[5] + 2, single, (int) (EDGES[6] - EDGES[5] - 4), 9, INK, true);
        } else {
            Quotation.txt(canvas, font, row.date, EDGES[5] + 2, middle - 17, (int) (EDGES[6] - EDGES[5] - 4), 9, INK, true);
            Quotation.txt(canvas, font, row.time, EDGES[5] + 2, middle + 1, (int) (EDGES[6] - EDGES[5] - 4), 8, GRAY, true);
        }
        canvas.save();
        canvas.translate(EDGES[4] + PAD, middle - row.description.getHeight() / 2f);
        row.description.draw(canvas);
        canvas.restore();
        Quotation.txt(canvas, font, row.type, EDGES[3] + 2, single, (int) (EDGES[4] - EDGES[3] - 4), 9, row.out.isEmpty() ? INK : EXPENSE, true);
        Quotation.txt(canvas, font, row.in, EDGES[2] + 2, single, (int) (EDGES[3] - EDGES[2] - 4), 9, TEAL, true);
        Quotation.txt(canvas, font, row.out, EDGES[1] + 2, single, (int) (EDGES[2] - EDGES[1] - 4), 9, EXPENSE, true);
        Quotation.txt(canvas, row.opening ? bold : font, row.balance, EDGES[0] + 2, single, (int) (EDGES[1] - EDGES[0] - 4), 9, row.negative ? EXPENSE : NAVY, true);
        Quotation.line(canvas, LEFT, top + row.height - 0.8f, RIGHT, RULE);
        for (int i = 1; i < EDGES.length - 1; i++) Quotation.rect(canvas, EDGES[i] - 0.3f, top + 4, EDGES[i] + 0.3f, top + row.height - 4, RULE);
    }

    private static void summary(Canvas canvas, Typeface font, Typeface bold, String carriedLabel, BigDecimal carried, List<JSONObject> entries, BigDecimal closing) {
        Quotation.rounded(canvas, LEFT, 124, RIGHT, 270, 10, PALE);
        String[] labels = {carriedLabel, "تحصيل كاش", "تحصيل شبكة", "تحصيل تحويل", "إجمالي التحصيل", "المصروفات", "صافي الحركة", "النقدية لدى المندوب"};
        BigDecimal[] values = {carried, LedgerMath.collections(entries, "cash"), LedgerMath.collections(entries, "network"), LedgerMath.collections(entries, "transfer"),
                LedgerMath.totalCollections(entries), LedgerMath.total(entries, "out"), LedgerMath.netActivity(entries), closing};
        int[] colors = {NAVY, TEAL, TEAL, TEAL, NAVY, EXPENSE, TEAL, WHITE};
        for (int i = 0; i < 8; i++) {
            float x = RIGHT - ((i % 4) + 1) * 132f;
            float y = i < 4 ? 134 : 196;
            if (i == 7) Quotation.rounded(canvas, x + 6, y - 6, x + 126, y + 46, 8, TEAL);
            Quotation.txt(canvas, font, labels[i], x + 4, y, 124, 8.5f, i == 7 ? -2493457 : GRAY, true, 1);
            Quotation.txt(canvas, i == 7 ? bold : font, amount(values[i]) + CURRENCY, x + 4, y + 17, 124, 12.5f, colors[i], true);
        }
        Quotation.line(canvas, 48, 184, 548, RULE);
        Quotation.rounded(canvas, 48, 248, 548, 264, 5, -1576205);
        Quotation.txt(canvas, font, "النقدية لدى المندوب = " + carriedLabel + " + تحصيل كاش − المصروفات", 56, 249, 484, 8, NAVY, true);
    }

    private static void closing(Canvas canvas, Typeface font, Typeface bold, float top, List<JSONObject> entries, BigDecimal closing) {
        Quotation.rect(canvas, LEFT, top, RIGHT, top + 28, TOTAL_BG);
        Quotation.rect(canvas, LEFT, top, RIGHT, top + 1.2f, TEAL);
        Quotation.txt(canvas, bold, "الإجمالي", EDGES[3] + 2, top + 6, (int) (EDGES[6] - EDGES[3] - 4), 10, NAVY, true);
        Quotation.txt(canvas, bold, amount(LedgerMath.totalCollections(entries)), EDGES[2] + 2, top + 6, (int) (EDGES[3] - EDGES[2] - 4), 9.5f, TEAL, true);
        Quotation.txt(canvas, bold, amount(LedgerMath.total(entries, "out")), EDGES[1] + 2, top + 6, (int) (EDGES[2] - EDGES[1] - 4), 9.5f, EXPENSE, true);
        Quotation.txt(canvas, bold, amount(closing), EDGES[0] + 2, top + 6, (int) (EDGES[1] - EDGES[0] - 4), 9.5f, NAVY, true);
        Quotation.rect(canvas, LEFT, top + 28, RIGHT, top + 29.2f, TEAL);
        Quotation.txt(canvas, font, "* الرصيد النقدي يتغير بالتحصيل الكاش والمصروفات فقط؛ أما التحصيل بالشبكة أو التحويل فيُودَع في حساب الشركة مباشرة ولا يغيّر نقدية المندوب.",
                LEFT, top + 36, (int) WIDTH, 7.5f, GRAY, false, 2);
        float sign = top + 80;
        String[] titles = {"توقيع المندوب", "اعتماد المراجع / المستلم"};
        float[] lefts = {RIGHT - 236, LEFT};
        for (int i = 0; i < 2; i++) {
            float x = lefts[i];
            Quotation.txt(canvas, bold, titles[i], x, sign, 236, 9.5f, NAVY, false);
            Quotation.txt(canvas, font, "الاسم:", x, sign + 22, 236, 8.5f, GRAY, false);
            Quotation.line(canvas, x, sign + 38, x + 236 - 40, RULE);
            Quotation.txt(canvas, font, "التوقيع:", x, sign + 46, 236, 8.5f, GRAY, false);
            Quotation.line(canvas, x, sign + 62, x + 236 - 44, RULE);
        }
    }
}
