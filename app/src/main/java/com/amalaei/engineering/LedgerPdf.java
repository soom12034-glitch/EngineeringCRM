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
    private static final int BAR = -15434365;
    private static final int EXPENSE = -4959479;
    private static final int PER_PAGE = 7;

    LedgerPdf() {
    }

    static File create(Context context, JSONObject jSONObject, List<JSONObject> list) throws Exception {
        float[] fArr;
        String str;
        BigDecimal bigDecimal;
        int i;
        BigDecimal bigDecimal2;
        String str2 = "دفتر";
        Typeface typefaceCreateFromAsset = Typeface.createFromAsset(context.getAssets(), "fonts/Cairo.ttf");
        PdfDocument pdfDocument = new PdfDocument();
        try {
            BigDecimal bigDecimalCollections = LedgerMath.collections(list, "cash");
            BigDecimal bigDecimalCollections2 = LedgerMath.collections(list, "network");
            BigDecimal bigDecimalCollections3 = LedgerMath.collections(list, "transfer");
            BigDecimal bigDecimal3 = LedgerMath.total(list, "out");
            BigDecimal bigDecimal4 = LedgerMath.totalCollections(list);
            BigDecimal bigDecimalNetActivity = LedgerMath.netActivity(list);
            BigDecimal bigDecimalBalance = LedgerMath.balance(jSONObject, list);
            int iMax = Math.max(1, (list.size() + 6) / PER_PAGE);
            int i2 = 1;
            BigDecimal bigDecimal5 = bigDecimalCollections;
            int i3 = 0;
            while (i3 < iMax) {
                int i4 = i3;
                i3 = i4 + 1;
                int i5 = iMax;
                BigDecimal bigDecimal6 = bigDecimalCollections2;
                PdfDocument.Page pageStartPage = pdfDocument.startPage(new PdfDocument.PageInfo.Builder(595, 842, i3).create());
                Canvas canvas = pageStartPage.getCanvas();
                canvas.drawColor(-1);
                Quotation.rect(canvas, 0.0f, 0.0f, 595.0f, 8.0f, BAR);
                Quotation.rect(canvas, 0.0f, 8.0f, 595.0f, 112.0f, -788488);
                Quotation.txt(canvas, typefaceCreateFromAsset, "كشف حركة دفتر", 34.0f, 26.0f, 527, 25.0f, -15717821, false);
                Quotation.txt(canvas, typefaceCreateFromAsset, jSONObject.optString("name"), 34.0f, 61.0f, 527, 18.0f, -14996678, false);
                    Quotation.txt(canvas, typefaceCreateFromAsset, "تاريخ الإصدار: " + new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(new Date()), 34.0f, 91.0f, 527, 10.0f, -10062718, false);
                    Typeface typeface = typefaceCreateFromAsset;
                    BigDecimal bigDecimal7 = bigDecimal5;
                    BigDecimal bigDecimal8 = bigDecimal6;
                    String str3 = str2;
                        summary(canvas, typeface, jSONObject, bigDecimal7, bigDecimal8, bigDecimalCollections3, bigDecimal3, bigDecimal4, bigDecimalNetActivity, bigDecimalBalance);
                        Canvas canvas2 = canvas;
                        typefaceCreateFromAsset = typeface;
                        Quotation.rounded(canvas2, 34.0f, 302.0f, 562.0f, 334.0f, 7.0f, BAR);
                        String[] strArr = new String[5];
                        strArr[0] = "الرصيد النقدي";
                        strArr[i2] = "المبلغ";
                        strArr[2] = "النوع";
                        strArr[3] = "البيان والملاحظة";
                        strArr[4] = "التاريخ";
                        int i6 = 3;
                        float[] fArr2 = {34.0f, 126.0f, 218.0f, 304.0f, 462.0f, 562.0f};
                        int i7 = 0;
                        while (true) {
                            fArr = fArr2;
                            if (i7 >= 5) {
                                break;
                            }
                            String str4 = strArr[i7];
                            float f = fArr[i7];
                            i7++;
                            String[] strArr2 = strArr;
                            Canvas canvas3 = canvas2;
                            Quotation.txt(canvas3, typefaceCreateFromAsset, str4, f + 3.0f, 310.0f, (int) ((fArr[i7] - f) - 6.0f), 10.0f, -1, true);
                            canvas2 = canvas3;
                            fArr2 = fArr;
                            strArr = strArr2;
                        }
                        Canvas canvas4 = canvas2;
                        BigDecimal bigDecimalOpening = LedgerMath.opening(jSONObject);
                        int size = list.size() - 1;
                        while (true) {
                            str = "amount";
                            bigDecimal = bigDecimal8;
                            if (size < 0) {
                                break;
                            }
                            BigDecimal bigDecimal9 = bigDecimalCollections3;
                            JSONObject jSONObject2 = list.get(size);
                            BigDecimal bigDecimal10 = bigDecimal3;
                            try {
                                bigDecimal2 = bigDecimal4;
                                try {
                                    BigDecimal bigDecimal11 = new BigDecimal(jSONObject2.optString("amount", "0"));
                                    if (jSONObject2.optString("kind").equals("out")) {
                                        bigDecimalOpening = bigDecimalOpening.subtract(bigDecimal11);
                                    } else if (jSONObject2.optString("paymentMethod", "cash").equals("cash")) {
                                        bigDecimalOpening = bigDecimalOpening.add(bigDecimal11);
                                    }
                                } catch (Exception unused) {
                                }
                            } catch (Exception unused2) {
                                bigDecimal2 = bigDecimal4;
                            }
                            size--;
                            bigDecimal8 = bigDecimal;
                            bigDecimalCollections3 = bigDecimal9;
                            bigDecimal3 = bigDecimal10;
                            bigDecimal4 = bigDecimal2;
                        }
                        BigDecimal bigDecimal12 = bigDecimalCollections3;
                        BigDecimal bigDecimal13 = bigDecimal3;
                        BigDecimal bigDecimal14 = bigDecimal4;
                        int i8 = i4 * PER_PAGE;
                        int iMin = Math.min(list.size(), i8 + PER_PAGE);
                        int i9 = i8;
                        float f2 = 334.0f;
                        while (i9 < iMin) {
                            JSONObject jSONObject3 = list.get(i9);
                            int i10 = i8;
                            int i11 = iMin;
                            BigDecimal bigDecimal15 = new BigDecimal(jSONObject3.optString(str, "0"));
                            String str5 = str;
                            boolean zEquals = jSONObject3.optString("kind").equals("in");
                            String strTrim = jSONObject3.optString("note").trim();
                            float f3 = f2 + (strTrim.isEmpty() ? 54 : 58);
                            Quotation.rect(canvas4, 34.0f, f2, 562.0f, f3, (i9 - i10) % 2 == 0 ? -460037 : -1116939);
                            float f4 = f2;
                            String strOptString = jSONObject3.optString("marker");
                            int i12 = i9;
                            BigDecimal bigDecimal16 = bigDecimalNetActivity;
                            String str6 = jSONObject3.optString("title") + (strOptString.isEmpty() ? "" : "  • " + strOptString) + (strTrim.isEmpty() ? "" : "\nملاحظة: " + strTrim);
                            String str7 = zEquals ? "تحصيل " + LedgerMath.methodLabel(jSONObject3.optString("paymentMethod", "cash")) : "مصروف نقدي";
                            String str8 = new SimpleDateFormat("dd/MM/yyyy\nHH:mm", Locale.US).format(new Date(jSONObject3.optLong("at")));
                            String[] strArr3 = new String[5];
                            strArr3[0] = LedgerMath.number(bigDecimalOpening);
                            strArr3[i2] = LedgerMath.number(bigDecimal15);
                            strArr3[2] = str7;
                            strArr3[i6] = str6;
                            strArr3[4] = str8;
                            int i13 = 0;
                            while (i13 < 5) {
                                String str9 = strArr3[i13];
                                float f5 = fArr[i13];
                                float f6 = f5 + 3.0f;
                                float f7 = f4 + 8.0f;
                                int i14 = i13 + 1;
                                int i15 = (int) ((fArr[i14] - f5) - 6.0f);
                                if (i13 == i2) {
                                    i = zEquals ? BAR : EXPENSE;
                                } else {
                                    i = -14996678;
                                }
                                int i16 = i6;
                                Quotation.txt(canvas4, typefaceCreateFromAsset, str9, f6, f7, i15, 10.0f, i, true, (i13 != i16 || strTrim.isEmpty()) ? 2 : i16);
                                i6 = i16;
                                i13 = i14;
                                i2 = 1;
                            }
                            int i17 = i6;
                            if (zEquals && jSONObject3.optString("paymentMethod", "cash").equals("cash")) {
                                bigDecimalOpening = bigDecimalOpening.subtract(bigDecimal15);
                            } else if (!zEquals) {
                                bigDecimalOpening = bigDecimalOpening.add(bigDecimal15);
                            }
                            Quotation.line(canvas4, 34.0f, f3 - 1.0f, 562.0f, -2300440);
                            i9 = i12 + 1;
                            i6 = i17;
                            i8 = i10;
                            iMin = i11;
                            str = str5;
                            f2 = f3;
                            bigDecimalNetActivity = bigDecimal16;
                            bigDecimalBalance = bigDecimalBalance;
                            i2 = 1;
                        }
                        Quotation.line(canvas4, 34.0f, 777.0f, 562.0f, -2300440);
                        Quotation.txt(canvas4, typefaceCreateFromAsset, "تم إنشاؤه من تواصل • صفحة " + i3 + " من " + i5, 34.0f, 793.0f, 528, 9.0f, -10062718, true);
                        pdfDocument.finishPage(pageStartPage);
                        iMax = i5;
                        str2 = str3;
                        bigDecimal5 = bigDecimal7;
                        bigDecimalCollections2 = bigDecimal;
                        bigDecimalCollections3 = bigDecimal12;
                        bigDecimal3 = bigDecimal13;
                        bigDecimal4 = bigDecimal14;
                        bigDecimalNetActivity = bigDecimalNetActivity;
                        bigDecimalBalance = bigDecimalBalance;
                        i2 = 1;
            }
            String str10 = str2;
            String strTrim2 = DisplayNumbers.latin(jSONObject.optString("name", str10)).replaceAll("[\\\\/:*?\"<>|\\r\\n]+", " ").trim();
            String strSubstring = strTrim2.isEmpty() ? str10 : strTrim2;
            if (strSubstring.length() > 70) {
                strSubstring = strSubstring.substring(0, 70);
            }
            File file = new File(context.getCacheDir(), "كشف " + strSubstring + " " + new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(new Date()) + ".pdf");
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                pdfDocument.writeTo(fileOutputStream);
            }
            return file;
        } finally {
            pdfDocument.close();
        }
    }

    private static void summary(Canvas canvas, Typeface typeface, JSONObject jSONObject, BigDecimal bigDecimal, BigDecimal bigDecimal2, BigDecimal bigDecimal3, BigDecimal bigDecimal4, BigDecimal bigDecimal5, BigDecimal bigDecimal6, BigDecimal bigDecimal7) {
        Quotation.rounded(canvas, 34.0f, 130.0f, 562.0f, 274.0f, 10.0f, -788488);
        String[] strArr = {"رصيد افتتاحي", "تحصيل كاش", "تحصيل شبكة", "تحصيل تحويل", "إجمالي قبل المصروفات", "المصروفات", "الصافي بعد المصروفات", "نقدية المندوب"};
        BigDecimal[] bigDecimalArr = {LedgerMath.opening(jSONObject), bigDecimal, bigDecimal2, bigDecimal3, bigDecimal5, bigDecimal4, bigDecimal6, bigDecimal7};
        int[] iArr = {-15717821, BAR, BAR, BAR, -15717821, EXPENSE, BAR, -15717821};
        for (int i = 0; i < 8; i++) {
            float f = ((i % 4) * 132) + 34;
            float f2 = ((i / 4) * 62) + 141;
            Quotation.txt(canvas, typeface, strArr[i], f, f2, 128, 9.0f, -10062718, true, 2);
            Quotation.txt(canvas, typeface, LedgerMath.money(bigDecimalArr[i]), f, f2 + 27.0f, 128, 14.0f, iArr[i], true);
        }
        Quotation.line(canvas, 48.0f, 201.0f, 548.0f, -2300440);
        Quotation.rounded(canvas, 48.0f, 248.0f, 548.0f, 269.0f, 6.0f, BAR);
        Quotation.txt(canvas, typeface, "نقدية المندوب = الافتتاحي + تحصيل كاش − المصروفات", 56.0f, 253.0f, 484, 9.0f, -1, true);
    }
}
