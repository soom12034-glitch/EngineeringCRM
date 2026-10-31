package com.amalaei.engineering;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONObject;

public final class ReportsActivity extends BaseSecureActivity {
    LinearLayout body;
    Typeface font;
    int period = 0;
    Store store;

    static /* synthetic */ void lambda$button$0(Runnable runnable, View view) {
        UiTheme.feedback(view);
        runnable.run();
    }

    void button(String str, final Runnable runnable) {
        Button button = new Button(this);
        button.setText(str);
        UiTheme.button(button, false);
        this.body.addView(button, new LinearLayout.LayoutParams(-1, -2));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                ReportsActivity.lambda$button$0(runnable, view);
            }
        });
    }

    void card(String str, String str2) {
        statText(UiTheme.card(this.body), str, str2);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AppLanguage.apply(this);
        this.font = Typeface.createFromAsset(getAssets(), "fonts/Cairo.ttf");
        this.store = new Store(this);
        show();
    }

    @Override
    protected void onDestroy() {
        this.store.close();
        super.onDestroy();
    }

    long[] range() {
        LocalDate localDateMinusDays;
        LocalDate localDatePlusDays;
        ZoneId zoneIdSystemDefault = ZoneId.systemDefault();
        LocalDate localDateNow = LocalDate.now();
        int i = this.period;
        if (i == 0) {
            localDateMinusDays = localDateNow.withDayOfMonth(1);
            localDatePlusDays = localDateMinusDays.plusMonths(1L);
        } else if (i == 1) {
            localDatePlusDays = localDateNow.withDayOfMonth(1);
            localDateMinusDays = localDatePlusDays.minusMonths(1L);
        } else {
            if (i != 2) {
                return new long[]{0, Long.MAX_VALUE};
            }
            localDateMinusDays = localDateNow.minusDays(89L);
            localDatePlusDays = localDateNow.plusDays(1L);
        }
        return new long[]{localDateMinusDays.atStartOfDay(zoneIdSystemDefault).toInstant().toEpochMilli(), localDatePlusDays.atStartOfDay(zoneIdSystemDefault).toInstant().toEpochMilli()};
    }

    void show() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        this.body = linearLayout;
        linearLayout.setOrientation(1);
        this.body.setLayoutDirection(AppLanguage.direction(this));
        int iDp = UiTheme.dp(this, 18);
        this.body.setPadding(iDp, iDp, iDp, iDp);
        this.body.setBackgroundColor(-788486);
        scrollView.addView(this.body);
        setContentView(scrollView);
        UiTheme.screen(this, scrollView);
        UiTheme.header(this.body, tr("التقارير والإحصاءات", "Reports & insights"), "reports");
        Spinner spinner = new Spinner(this);
        spinner.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, new String[]{tr("هذا الشهر", "This month"), tr("الشهر الماضي", "Last month"), tr("آخر 90 يومًا", "Last 90 days"), tr("كل البيانات", "All time")}));
        spinner.setSelection(this.period);
        this.body.addView(spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                if (ReportsActivity.this.period != i) {
                    ReportsActivity.this.period = i;
                    ReportsActivity.this.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        long[] jArrRange = range();
        ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM leads WHERE business=?", "survey");
        ArrayList<JSONObject> arrayListQuery2 = this.store.query("SELECT * FROM events", new String[0]);
        ArrayList<JSONObject> arrayListQuery3 = this.store.query("SELECT * FROM ledgerEntries", new String[0]);
        ArrayList<JSONObject> arrayListQuery4 = this.store.query("SELECT * FROM quotes", new String[0]);
        int iCountNew = ReportMath.countNew(arrayListQuery, arrayListQuery2, jArrRange[0], jArrRange[1]);
        int iClosed = ReportMath.closed(arrayListQuery, true);
        int iClosed2 = ReportMath.closed(arrayListQuery, false);
        BigDecimal bigDecimalLedgerNet = ReportMath.ledgerNet(arrayListQuery3, jArrRange[0], jArrRange[1]);
        BigDecimal bigDecimalAdd = BigDecimal.ZERO;
        Iterator<JSONObject> it = arrayListQuery4.iterator();
        while (it.hasNext()) {
            try {
                JSONObject jSONObject = new JSONObject(it.next().getString("data"));
                if ("accepted".equals(QuoteStatus.value(jSONObject))) {
                    bigDecimalAdd = bigDecimalAdd.add(Quotation.subtotal(jSONObject).add(QuoteMath.tax(Quotation.subtotal(jSONObject))));
                }
            } catch (Exception unused) {
            }
        }
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        for (JSONObject jSONObject2 : arrayListQuery) {
            boolean z = !jSONObject2.optString("business").equals("survey") || jSONObject2.optString("requestType", "sales").equals("sales");
            BigDecimal bigDecimal = bigDecimalLedgerNet;
            BigDecimal bigDecimal2 = bigDecimalAdd;
            if (jSONObject2.optInt("stage") == 4) {
                if (z) {
                    i++;
                } else {
                    i3++;
                }
            }
            if (jSONObject2.optInt("stage") == 5) {
                if (z) {
                    i2++;
                } else {
                    i4++;
                }
            }
            bigDecimalLedgerNet = bigDecimal;
            bigDecimalAdd = bigDecimal2;
        }
        BigDecimal bigDecimal3 = bigDecimalLedgerNet;
        LinearLayout linearLayout2 = new LinearLayout(this);
        this.body.addView(linearLayout2);
        stat(linearLayout2, tr("عملاء جدد — للفترة", "New customers — period"), String.valueOf(iCountNew));
        stat(linearLayout2, tr("صافي الدفتر — للفترة", "Ledger net — period"), LedgerMath.money(bigDecimal3));
        text(tr("حالة الأعمال الحالية — جميع البيانات", "Current business status — all data"), 17);
        LinearLayout linearLayout3 = new LinearLayout(this);
        this.body.addView(linearLayout3);
        stat(linearLayout3, tr("التحويل للمبيعات", "Sales conversion"), Math.round((((double) i) * 100.0d) / ((double) Math.max(1, i + i2))) + "%");
        stat(linearLayout3, tr("الإنجاز للخدمات", "Service completion"), Math.round((((double) i3) * 100.0d) / ((double) Math.max(1, i3 + i4))) + "%");
        card(tr("مكتمل / مغلق", "Completed / closed"), iClosed + " / " + iClosed2);
        card(tr("قيمة العروض المقبولة — جميع البيانات", "Accepted quotations — all data"), QuoteMath.money(bigDecimalAdd) + " SAR");
        stagesChart(arrayListQuery);
        button(tr("رجوع", "Back"), new Runnable() {
            @Override
            public final void run() {
                ReportsActivity.this.finish();
            }
        });
        UiTheme.enter(this.body);
    }

    void stagesChart(ArrayList<JSONObject> arrayList) {
        LinearLayout linearLayoutCard = UiTheme.card(this.body);
        TextView textView = new TextView(this);
        textView.setText(tr("العملاء حسب المرحلة", "Customers by stage"));
        textView.setTextSize(18.0f);
        textView.setTypeface(this.font);
        linearLayoutCard.addView(textView);
        int iMax = 1;
        String[] strArr = {tr("جديد", "New"), tr("تواصل أولي", "Contacted"), tr("قيد المتابعة", "In progress"), tr("مرحلة متقدمة", "Advanced"), tr("مكتمل", "Completed"), tr("مغلق دون إتمام", "Closed without completion")};
        int[] iArr = new int[6];
        Iterator<JSONObject> it = arrayList.iterator();
        while (it.hasNext()) {
            int iOptInt = it.next().optInt("stage");
            if (iOptInt >= 0 && iOptInt < 6) {
                iArr[iOptInt] = iArr[iOptInt] + 1;
            }
        }
        for (int i = 0; i < 6; i++) {
            iMax = Math.max(iMax, iArr[i]);
        }
        for (int i2 = 0; i2 < 6; i2++) {
            TextView textView2 = new TextView(this);
            textView2.setText(strArr[i2] + "  •  " + iArr[i2]);
            textView2.setTextColor(-15189940);
            textView2.setTypeface(this.font);
            textView2.setPadding(0, UiTheme.dp(this, 8), 0, 0);
            linearLayoutCard.addView(textView2);
            ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMax(iMax);
            progressBar.setProgress(iArr[i2]);
            progressBar.setProgressTintList(ColorStateList.valueOf(UiTheme.accent(this)));
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(SectionColors.tint("reports")));
            progressBar.setContentDescription(strArr[i2] + ": " + iArr[i2]);
            linearLayoutCard.addView(progressBar, new LinearLayout.LayoutParams(-1, UiTheme.dp(this, 12)));
        }
    }

    void stat(LinearLayout linearLayout, String str, String str2) {
        LinearLayout linearLayoutCard = UiTheme.card(linearLayout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams.setMargins(UiTheme.dp(this, 3), UiTheme.dp(this, 6), UiTheme.dp(this, 3), UiTheme.dp(this, 6));
        linearLayoutCard.setLayoutParams(layoutParams);
        statText(linearLayoutCard, str, str2);
    }

    void statText(LinearLayout linearLayout, String str, String str2) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTypeface(this.font);
        textView.setTextSize(12.0f);
        textView.setTextColor(-10192758);
        TextView textView2 = new TextView(this);
        textView2.setText(str2);
        textView2.setTextSize(23.0f);
        textView2.setTypeface(this.font);
        textView2.setTextColor(UiTheme.accent(this));
        textView2.setTextDirection(3);
        linearLayout.addView(textView);
        linearLayout.addView(textView2);
    }

    void text(String str, int i) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTypeface(this.font);
        textView.setTextColor(-15189940);
        textView.setPadding(0, UiTheme.dp(this, 8), 0, UiTheme.dp(this, 8));
        this.body.addView(textView);
    }

    String tr(String str, String str2) {
        return AppLanguage.text(this, str, str2);
    }
}
