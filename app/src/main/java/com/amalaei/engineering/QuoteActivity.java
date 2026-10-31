package com.amalaei.engineering;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public final class QuoteActivity extends BaseSecureActivity {
    LinearLayout body;
    JSONObject draft;
    Typeface font;
    LinearLayout itemsBody;
    Store store;
    TextView totals;
    ArrayList<EditText[]> rows = new ArrayList<>();
    Map<String, EditText> fields = new LinkedHashMap();
    String savedSnapshot = "";
    ArrayList<TextView> rowTitles = new ArrayList<>();
    int listFilter = 0;

    static String customerLabel(JSONObject jSONObject) {
        String strCustomerName = customerName(jSONObject);
        String strTrim = jSONObject.optString("company").trim();
        String strTrim2 = jSONObject.optString("phone").trim();
        StringBuilder sb = new StringBuilder(strCustomerName);
        if (!strTrim.isEmpty() && !strTrim.equals(strCustomerName)) {
            sb.append(" — ").append(strTrim);
        }
        if (!strTrim2.isEmpty()) {
            sb.append("\n\u2066").append(strTrim2).append("\u2069");
        }
        return sb.toString();
    }

    static String customerName(JSONObject jSONObject) {
        String strTrim = jSONObject.optString("phone").trim();
        String strTrim2 = jSONObject.optString("name").trim();
        String strTrim3 = jSONObject.optString("contactName").trim();
        if (strTrim2.isEmpty() || (!strTrim.isEmpty() && Analysis.phone(strTrim2).equals(strTrim))) {
            strTrim2 = strTrim3;
        }
        return strTrim2.isEmpty() ? "عميل غير مسمى" : strTrim2;
    }

    public /* synthetic */ void lambda$add$18(EditText[] editTextArr, LinearLayout linearLayout) {
        int iIndexOf = this.rows.indexOf(editTextArr);
        if (iIndexOf < 0) {
            return;
        }
        this.rows.remove(iIndexOf);
        this.rowTitles.remove(iIndexOf);
        this.itemsBody.removeView(linearLayout);
        int i = 0;
        while (i < this.rowTitles.size()) {
            TextView textView = this.rowTitles.get(i);
            i++;
            textView.setText("الصنف " + i);
        }
        try {
            capture();
            summary();
        } catch (Exception unused) {
        }
    }

    static /* synthetic */ void lambda$btn$3(Runnable runnable, View view) {
        UiTheme.feedback(view);
        runnable.run();
    }

    public /* synthetic */ void lambda$changeStatus$15(DialogInterface dialogInterface, DialogInterface dialogInterface2, int i) {
        dialogInterface.dismiss();
        editor();
    }

    public /* synthetic */ void lambda$changeStatus$16(DialogInterface dialogInterface, DialogInterface dialogInterface2, int i) {
        try {
            JSONObject jSONObjectLead = this.store.lead(this.draft.optLong("leadId"));
            jSONObjectLead.put("stage", 4);
            this.store.save(jSONObjectLead);
            dialogInterface.dismiss();
            editor();
        } catch (Exception e) {
            error(e);
        }
    }

    public /* synthetic */ void lambda$changeStatus$17(final DialogInterface dialogInterface, int i) {
        try {
            String str = QuoteStatus.KEYS[i];
            this.draft.put("status", str).put("statusAt", System.currentTimeMillis());
            if (str.equals("accepted") && this.draft.optLong("leadId") > 0) {
                new FluentDialog(this).setMessage("هل تريد نقل العميل إلى مرحلة مكتمل؟").setNegativeButton("ليس الآن", new DialogInterface.OnClickListener() {
                    @Override
                    public final void onClick(DialogInterface dialogInterface2, int i2) {
                        QuoteActivity.this.lambda$changeStatus$15(dialogInterface, dialogInterface2, i2);
                    }
                }).setPositiveButton("نعم", new DialogInterface.OnClickListener() {
                    @Override
                    public final void onClick(DialogInterface dialogInterface2, int i2) {
                        QuoteActivity.this.lambda$changeStatus$16(dialogInterface, dialogInterface2, i2);
                    }
                }).show();
            } else {
                dialogInterface.dismiss();
                editor();
            }
        } catch (Exception e) {
            error(e);
        }
    }

    public /* synthetic */ void lambda$chooseCustomer$14(ArrayList arrayList, DialogInterface dialogInterface, int i) {
        JSONObject jSONObject = (JSONObject) arrayList.get(i);
        String strCustomerName = customerName(jSONObject);
        String strTrim = jSONObject.optString("company").trim();
        EditText editText = this.fields.get("customer");
        if (!strTrim.isEmpty()) {
            strCustomerName = strTrim;
        }
        editText.setText(strCustomerName);
        this.fields.get("customerPhone").setText(jSONObject.optString("phone"));
        try {
            JSONObject jSONObject2 = new JSONObject(jSONObject.optString("requestDetails", "{}"));
            this.fields.get("customerVat").setText(jSONObject2.optString("customerVat"));
            this.fields.get("customerAddress").setText(jSONObject2.optString("customerAddress"));
            this.draft.put("leadId", jSONObject.optLong("id"));
        } catch (Exception unused) {
        }
    }

    public /* synthetic */ void lambda$company$19(Map map, JSONObject jSONObject, DialogInterface dialogInterface, int i) {
        try {
            for (Object key : map.keySet()) {
                String str = (String) key;
                jSONObject.put(str, ((EditText) map.get(str)).getText().toString().trim());
            }
            JSONObject jSONObjectProfile = this.store.profile("survey");
            jSONObjectProfile.put("quotationCompany", new JSONObject(jSONObject.toString()));
            if (jSONObjectProfile.optString("name").isEmpty()) {
                jSONObjectProfile.put("name", jSONObject.optString("name"));
            }
            this.store.saveProfile("survey", jSONObjectProfile);
        } catch (Exception e) {
            error(e);
        }
    }

    public /* synthetic */ void lambda$editor$10() {
        save(false);
    }

    public /* synthetic */ void lambda$editor$11() {
        export(false);
    }

    public /* synthetic */ void lambda$editor$12() {
        export(true);
    }

    public /* synthetic */ void lambda$editor$6(DatePicker datePicker, int i, int i2, int i3) {
        this.fields.get("date").setText(LocalDate.of(i, i2 + 1, i3).toString());
    }

    public /* synthetic */ void lambda$editor$7() {
        LocalDate localDateNow = LocalDate.now();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public final void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                QuoteActivity.this.lambda$editor$6(datePicker, i, i2, i3);
            }
        }, localDateNow.getYear(), localDateNow.getMonthValue() - 1, localDateNow.getDayOfMonth()).show();
    }

    public /* synthetic */ void lambda$editor$8() {
        add(new JSONObject());
    }

    public /* synthetic */ void lambda$editor$9() {
        try {
            capture();
            summary();
        } catch (Exception e) {
            error(e);
        }
    }

    public /* synthetic */ void lambda$leave$1(DialogInterface dialogInterface, int i) {
        try {
            capture();
            this.store.saveQuote(this.draft);
            list();
        } catch (Exception e) {
            error(e);
        }
    }

    public /* synthetic */ void lambda$leave$2(DialogInterface dialogInterface, int i) {
        list();
    }

    public /* synthetic */ void lambda$list$5(JSONObject jSONObject) {
        try {
            this.draft = this.store.quote(jSONObject.optLong("id"));
            editor();
        } catch (Exception e) {
            error(e);
        }
    }

    public /* synthetic */ void lambda$onCreate$0() {
        if (this.draft == null) {
            finish();
        } else {
            leave();
        }
    }

    void add(JSONObject jSONObject) {
        if (this.rows.size() >= 100) {
            Toast.makeText(this, "الحد الأقصى 100 صنف", 0).show();
            return;
        }
        LinearLayout linearLayout = this.body;
        final LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(12, 12, 12, 12);
        this.itemsBody.addView(linearLayout2);
        this.body = linearLayout2;
        this.rowTitles.add(label("الصنف " + (this.rows.size() + 1), 19));
        EditText editTextInput = input("الوصف (حتى 80 حرفًا)", jSONObject.optString("description"), false);
        editTextInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(80)});
        EditText editTextInput2 = input("الوحدة", jSONObject.optString("unit", "قطعة"), false);
        EditText editTextInput3 = input("الكمية", jSONObject.optString("qty", "1"), true);
        EditText editTextInput4 = input("سعر الوحدة قبل الضريبة", jSONObject.optString("price", ""), true);
        editTextInput2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        final EditText[] editTextArr = {editTextInput, editTextInput2, editTextInput3, editTextInput4};
        this.rows.add(editTextArr);
        btn("حذف هذا الصنف", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.lambda$add$18(editTextArr, linearLayout2);
            }
        });
        this.body = linearLayout;
        for (int i = 0; i < 4; i++) {
            editTextArr[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
                    QuoteActivity.this.refreshTotals();
                }
            });
        }
        refreshTotals();
    }

    void base(String str) {
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
        UiTheme.header(this.body, str, "quotes");
        UiTheme.enter(this.body);
    }

    void btn(String str, final Runnable runnable) {
        Button button = new Button(this);
        button.setText(str);
        button.setTypeface(this.font);
        UiTheme.button(button, str.startsWith("+") || str.equals("حفظ العرض"));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, UiTheme.dp(this, 4), 0, UiTheme.dp(this, 4));
        this.body.addView(button, layoutParams);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                QuoteActivity.lambda$btn$3(runnable, view);
            }
        });
    }

    void capture() throws Exception {
        for (String str : this.fields.keySet()) {
            this.draft.put(str, this.fields.get(str).getText().toString().trim());
        }
        JSONArray jSONArray = new JSONArray();
        for (EditText[] editTextArr : this.rows) {
            jSONArray.put(new JSONObject().put("description", editTextArr[0].getText().toString()).put("unit", editTextArr[1].getText().toString()).put("qty", editTextArr[2].getText().toString()).put("price", editTextArr[3].getText().toString()));
        }
        this.draft.put("items", jSONArray);
    }

    void changeStatus() {
        new FluentDialog(this).setTitle("حالة العرض").setSingleChoiceItems(QuoteStatus.LABELS, QuoteStatus.index(QuoteStatus.value(this.draft)), new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                QuoteActivity.this.lambda$changeStatus$17(dialogInterface, i);
            }
        }).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).show();
    }

    void chooseCustomer() {
        if (checkSelfPermission("android.permission.READ_CONTACTS") == 0) {
            try {
                this.store.refreshContactNames(PhoneData.contacts(this));
            } catch (Exception unused) {
            }
        }
        final ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM leads WHERE business='survey' ORDER BY CASE WHEN lastCallAt=0 THEN 1 ELSE 0 END, lastCallAt DESC, name COLLATE NOCASE", new String[0]);
        if (arrayListQuery.isEmpty()) {
            new FluentDialog(this).setMessage("لا يوجد عملاء محفوظون في نشاط المساحة").setPositiveButton("حسنًا", (DialogInterface.OnClickListener) null).show();
            return;
        }
        int size = arrayListQuery.size();
        String[] strArr = new String[size];
        for (int i = 0; i < size; i++) {
            strArr[i] = customerLabel(arrayListQuery.get(i));
        }
        new FluentDialog(this).setTitle("اختيار العميل").setItems(strArr, new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i2) {
                QuoteActivity.this.lambda$chooseCustomer$14(arrayListQuery, dialogInterface, i2);
            }
        }).show();
    }

    void chooseLogo() {
        try {
            capture();
            startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT").setType("image/*").addCategory("android.intent.category.OPENABLE"), 51);
        } catch (Exception e) {
            error(e);
        }
    }

    void company() {
        try {
            capture();
            final JSONObject jSONObject = this.draft.getJSONObject("company");
            LinearLayout linearLayout = this.body;
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setOrientation(1);
            linearLayout2.setPadding(20, 10, 20, 10);
            this.body = linearLayout2;
            final LinkedHashMap linkedHashMap = new LinkedHashMap();
            String[][] strArr = {new String[]{"name", "اسم المؤسسة"}, new String[]{"registration", "السجل التجاري"}, new String[]{"vat", "الرقم الضريبي للمؤسسة"}, new String[]{"address", "العنوان"}, new String[]{"phone", "الهاتف"}, new String[]{"email", "البريد"}};
            for (int i = 0; i < 6; i++) {
                String[] strArr2 = strArr[i];
                String str = strArr2[0];
                linkedHashMap.put(str, input(strArr2[1], jSONObject.optString(str), false));
            }
            this.body = linearLayout;
            ScrollView scrollView = new ScrollView(this);
            scrollView.addView(linearLayout2);
            new FluentDialog(this).setTitle("بيانات المؤسسة • المساحة فقط").setView(scrollView).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حفظ", new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialogInterface, int i2) {
                    QuoteActivity.this.lambda$company$19(linkedHashMap, jSONObject, dialogInterface, i2);
                }
            }).show();
        } catch (Exception e) {
            error(e);
        }
    }

    void duplicate() {
        try {
            capture();
            JSONObject jSONObject = new JSONObject(this.draft.toString());
            jSONObject.remove("id");
            jSONObject.put("number", "QT-" + LocalDate.now().getYear() + "-" + String.format(Locale.US, "%04d", Long.valueOf(this.store.query("SELECT COALESCE(MAX(id),0)+1 AS next FROM quotes", new String[0]).get(0).optLong("next")))).put("date", LocalDate.now().toString()).put("status", "draft").remove("statusAt");
            this.draft = jSONObject;
            editor();
            Toast.makeText(this, "أُنشئت نسخة مسودة جديدة", 0).show();
        } catch (Exception e) {
            error(e);
        }
    }

    void editor() {
        this.savedSnapshot = "";
        long j = 0;
        if (this.draft.optLong("id") > 0) {
            try {
                this.savedSnapshot = this.store.quote(this.draft.optLong("id")).toString();
            } catch (Exception unused) {
            }
        }
        this.fields.clear();
        this.rows.clear();
        this.rowTitles.clear();
        base("عرض سعر المساحة");
        label("الحالة: " + QuoteStatus.display(this.draft) + ". اسم العميل مطلوب؛ أسعار الوحدات قبل الضريبة وتُحسب 15% تلقائيًا.", 14);
        btn("اختيار عميل محفوظ للمساحة", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.chooseCustomer();
            }
        });
        String[][] strArr = {new String[]{"number", "رقم العرض"}, new String[]{"date", "تاريخ العرض YYYY-MM-DD"}, new String[]{"validDays", "صلاحية العرض بالأيام"}, new String[]{"customer", "اسم العميل / الشركة أو المنشأة *"}, new String[]{"customerVat", "الرقم الضريبي للعميل (اختياري)"}, new String[]{"customerAddress", "العنوان الوطني للعميل (اختياري)"}, new String[]{"customerPhone", "رقم هاتف العميل (اختياري)"}, new String[]{"notes", "ملاحظات وشروط العرض (اختياري)"}};
        int i = 0;
        for (int i2 = 8; i < i2; i2 = 8) {
            String[] strArr2 = strArr[i];
            String str = strArr2[1];
            long j2 = j;
            JSONObject jSONObject = this.draft;
            String str2 = strArr2[0];
            EditText editTextInput = input(str, jSONObject.optString(str2, str2.equals("validDays") ? "15" : ""), false);
            editTextInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(strArr2[0].equals("notes") ? 240 : strArr2[0].equals("customerAddress") ? 110 : 80)});
            if (strArr2[0].equals("customerPhone")) {
                editTextInput.setInputType(3);
                editTextInput.setTextDirection(3);
                editTextInput.setGravity(8388611);
            }
            if (strArr2[0].equals("validDays")) {
                editTextInput.setInputType(2);
            }
            this.fields.put(strArr2[0], editTextInput);
            i++;
            j = j2;
        }
        long j3 = j;
        btn("اختيار تاريخ العرض", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.lambda$editor$7();
            }
        });
        btn("بيانات المؤسسة في هذا النموذج", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.company();
            }
        });
        JSONObject jSONObjectOptJSONObject = this.draft.optJSONObject("company");
        boolean z = (jSONObjectOptJSONObject == null || jSONObjectOptJSONObject.optString("logo").isEmpty()) ? false : true;
        btn(z ? "تغيير شعار الشركة" : "رفع شعار الشركة", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.chooseLogo();
            }
        });
        if (z) {
            btn("حذف شعار الشركة", new Runnable() {
                @Override
                public final void run() {
                    QuoteActivity.this.removeLogo();
                }
            });
        }
        label("الأصناف", 20);
        LinearLayout linearLayout = new LinearLayout(this);
        this.itemsBody = linearLayout;
        linearLayout.setOrientation(1);
        this.body.addView(this.itemsBody);
        this.totals = label("", 18);
        btn("إضافة صنف", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.lambda$editor$8();
            }
        });
        btn("حساب الإجماليات", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.lambda$editor$9();
            }
        });
        btn("تغيير حالة العرض", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.changeStatus();
            }
        });
        if (this.draft.optLong("id") > j3) {
            btn("تكرار هذا العرض", new Runnable() {
                @Override
                public final void run() {
                    QuoteActivity.this.duplicate();
                }
            });
        }
        btn("حفظ العرض", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.lambda$editor$10();
            }
        });
        btn("معاينة PDF", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.lambda$editor$11();
            }
        });
        btn("مشاركة PDF عبر واتساب أو البريد", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.lambda$editor$12();
            }
        });
        btn("حفظ PDF في الهاتف", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.lambda$editor$13();
            }
        });
        btn("العروض المحفوظة", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.leave();
            }
        });
        JSONArray jSONArrayOptJSONArray = this.draft.optJSONArray("items");
        if (jSONArrayOptJSONArray != null) {
            for (int i3 = 0; i3 < jSONArrayOptJSONArray.length(); i3++) {
                add(jSONArrayOptJSONArray.optJSONObject(i3));
            }
        }
        if (this.rows.isEmpty()) {
            add(new JSONObject());
        }
        try {
            summary();
        } catch (Exception unused2) {
        }
    }

    void error(Exception exc) {
        new FluentDialog(this).setMessage(exc.getMessage() == null ? "تعذر إتمام العملية" : exc.getMessage()).setPositiveButton("حسنًا", (DialogInterface.OnClickListener) null).show();
    }

    void export(boolean z) {
        try {
            Uri uriMake = make();
            Intent intentPutExtra = z ? new Intent("android.intent.action.SEND").setType("application/pdf").putExtra("android.intent.extra.STREAM", uriMake).putExtra("android.intent.extra.SUBJECT", "عرض سعر " + this.draft.optString("number")) : new Intent(this, (Class<?>) QuotePreviewActivity.class).setDataAndType(uriMake, "application/pdf");
            intentPutExtra.addFlags(1);
            intentPutExtra.setClipData(ClipData.newRawUri("عرض سعر", uriMake));
            if (z) {
                startActivity(Intent.createChooser(intentPutExtra, "مشاركة عرض السعر"));
            } else {
                startActivity(intentPutExtra);
            }
        } catch (Exception e) {
            error(e);
        }
    }

    public void lambda$editor$13() {
        try {
            getPreferences(0).edit().putString("pendingPdf", make().toString()).apply();
            startActivityForResult(new Intent("android.intent.action.CREATE_DOCUMENT").setType("application/pdf").addCategory("android.intent.category.OPENABLE").putExtra("android.intent.extra.TITLE", this.draft.optString("number") + ".pdf"), 50);
        } catch (Exception e) {
            error(e);
        }
    }

    EditText input(String str, String str2, boolean z) {
        label(str, 13);
        EditText editText = new EditText(this);
        editText.setTypeface(this.font);
        editText.setText(str2);
        editText.setTextSize(16.0f);
        editText.setInputType(z ? 8194 : 131073);
        UiTheme.input(editText);
        this.body.addView(editText, new LinearLayout.LayoutParams(-1, -2));
        return editText;
    }

    TextView label(String str, int i) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTypeface(this.font);
        textView.setTextColor(-15189940);
        textView.setPadding(0, 10, 0, 10);
        this.body.addView(textView);
        return textView;
    }

    void leave() {
        try {
            capture();
            if (this.draft.toString().equals(this.savedSnapshot)) {
                list();
                return;
            }
        } catch (Exception unused) {
        }
        new FluentDialog(this).setMessage("لديك تغييرات في عرض السعر").setPositiveButton("حفظ والعودة", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                QuoteActivity.this.lambda$leave$1(dialogInterface, i);
            }
        }).setNeutralButton("متابعة التحرير", (DialogInterface.OnClickListener) null).setNegativeButton("تجاهل التغييرات", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                QuoteActivity.this.lambda$leave$2(dialogInterface, i);
            }
        }).show();
    }

    void list() {
        getPreferences(0).edit().remove("draft").apply();
        this.draft = null;
        this.fields.clear();
        this.rows.clear();
        base("عروض أسعار أجهزة المساحة");
        label("نموذج احترافي بهوية المنشأة • ضريبة 15% • ريال سعودي", 14);
        btn("+ إنشاء عرض سعر", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.lambda$list$4();
            }
        });
        String[] strArr = {"كل الحالات", "مسودة", "مرسل", "مقبول", "مرفوض", "منتهي"};
        Spinner spinner = new Spinner(this);
        spinner.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, strArr));
        spinner.setSelection(this.listFilter);
        this.body.addView(spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                if (QuoteActivity.this.listFilter != i) {
                    QuoteActivity.this.listFilter = i;
                    QuoteActivity.this.list();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        for (final JSONObject jSONObject : this.store.query("SELECT * FROM quotes ORDER BY id DESC", new String[0])) {
            try {
                JSONObject jSONObject2 = new JSONObject(jSONObject.getString("data"));
                String strDisplay = QuoteStatus.display(jSONObject2);
                int i = this.listFilter;
                if (i <= 0 || strDisplay.equals(strArr[i])) {
                    LinearLayout linearLayout = this.body;
                    this.body = UiTheme.card(linearLayout);
                    label(jSONObject2.optString("number") + " • " + strDisplay, 13);
                    label(jSONObject2.optString("customer"), 19);
                    BigDecimal bigDecimalSubtotal = Quotation.subtotal(jSONObject2);
                    label(QuoteMath.money(bigDecimalSubtotal.add(QuoteMath.tax(bigDecimalSubtotal))) + " SAR", 18);
                    btn(AppLanguage.text(this, "فتح عرض السعر", "Open quotation"), new Runnable() {
                        @Override
                        public final void run() {
                            QuoteActivity.this.lambda$list$5(jSONObject);
                        }
                    });
                    this.body = linearLayout;
                }
            } catch (Exception e) {
                error(e);
            }
        }
        btn("رجوع إلى العملاء", new Runnable() {
            @Override
            public final void run() {
                QuoteActivity.this.finish();
            }
        });
    }

    Uri make() throws Exception {
        capture();
        this.store.saveQuote(this.draft);
        this.savedSnapshot = this.draft.toString();
        return Uri.parse("content://" + getPackageName() + ".quotes/" + Quotation.pdf(this, this.draft).getName());
    }

    public void lambda$list$4() {
        try {
            this.draft = new JSONObject().put("business", "survey").put("number", "QT-" + LocalDate.now().getYear() + "-" + String.format(Locale.US, "%04d", Long.valueOf(this.store.query("SELECT COALESCE(MAX(id),0)+1 AS next FROM quotes", new String[0]).get(0).optLong("next")))).put("date", LocalDate.now().toString()).put("validDays", "15").put("status", "draft").put("notes", "الأسعار تشمل ضريبة القيمة المضافة، ويخضع التوريد لتوفر المنتجات عند اعتماد العرض.").put("customer", "").put("items", new JSONArray());
            JSONObject jSONObjectProfile = this.store.profile("survey");
            this.draft.put("company", jSONObjectProfile.optJSONObject("quotationCompany") == null ? Quotation.company() : new JSONObject(jSONObjectProfile.getJSONObject("quotationCompany").toString()));
            editor();
        } catch (Exception e) {
            error(e);
        }
    }

    @Override
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 != -1 || intent == null) {
            return;
        }
        try {
            if (i == 51) {
                this.draft.getJSONObject("company").put("logo", QuoteBranding.importLogo(this, intent.getData()));
                saveBrand();
                editor();
                Toast.makeText(this, "تم حفظ شعار الشركة", 0).show();
                return;
            }
            if (i == 50) {
                try (InputStream in = getContentResolver().openInputStream(Uri.parse(getPreferences(0).getString("pendingPdf", "")));
                     OutputStream out = getContentResolver().openOutputStream(intent.getData())) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    Toast.makeText(this, "حُفظ ملف PDF", 0).show();
                }
            }
        } catch (Exception e) {
            error(e);
        }
    }

    @Override
    public void onBackPressed() {
        if (this.draft == null) {
            finish();
        } else {
            leave();
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, new OnBackInvokedCallback() {
                @Override
                public final void onBackInvoked() {
                    QuoteActivity.this.lambda$onCreate$0();
                }
            });
        }
        this.font = Typeface.createFromAsset(getAssets(), "fonts/Cairo.ttf");
        this.store = new Store(this);
        if (bundle != null && bundle.containsKey("draft")) {
            try {
                this.draft = new JSONObject(bundle.getString("draft"));
                editor();
                return;
            } catch (Exception unused) {
            }
        }
        String string = getPreferences(0).getString("draft", "");
        if (!string.isEmpty()) {
            try {
                this.draft = new JSONObject(string);
                editor();
                return;
            } catch (Exception unused2) {
            }
        }
        list();
    }

    @Override
    protected void onDestroy() {
        this.store.close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (this.draft != null) {
            try {
                capture();
                getPreferences(0).edit().putString("draft", this.draft.toString()).apply();
            } catch (Exception unused) {
            }
        }
        super.onPause();
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        AppLanguage.apply(this);
        AppLanguage.watch(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        if (this.draft != null) {
            try {
                capture();
                bundle.putString("draft", this.draft.toString());
            } catch (Exception unused) {
            }
        }
        super.onSaveInstanceState(bundle);
    }

    void refreshTotals() {
        if (this.totals == null || this.draft == null) {
            return;
        }
        try {
            summary();
        } catch (Exception unused) {
            this.totals.setText("أكمل الكمية وسعر الوحدة لكل صنف لحساب الإجمالي");
        }
    }

    void removeLogo() {
        try {
            capture();
            this.draft.getJSONObject("company").remove("logo");
            saveBrand();
            editor();
            Toast.makeText(this, "حُذف الشعار من عروض الأسعار الجديدة", 0).show();
        } catch (Exception e) {
            error(e);
        }
    }

    void save(boolean z) {
        try {
            capture();
            this.store.saveQuote(this.draft);
            this.savedSnapshot = this.draft.toString();
            getPreferences(0).edit().putString("draft", this.savedSnapshot).apply();
            summary();
            if (z) {
                return;
            }
            Toast.makeText(this, "حُفظ العرض", 0).show();
        } catch (Exception e) {
            error(e);
        }
    }

    void saveBrand() throws Exception {
        JSONObject jSONObject = this.draft.getJSONObject("company");
        JSONObject jSONObjectProfile = this.store.profile("survey");
        jSONObjectProfile.put("quotationCompany", new JSONObject(jSONObject.toString()));
        if (jSONObjectProfile.optString("name").isEmpty()) {
            jSONObjectProfile.put("name", jSONObject.optString("name"));
        }
        this.store.saveProfile("survey", jSONObjectProfile);
    }

    void summary() throws Exception {
        capture();
        BigDecimal bigDecimalSubtotal = Quotation.subtotal(this.draft);
        BigDecimal bigDecimalTax = QuoteMath.tax(bigDecimalSubtotal);
        this.totals.setText("المجموع الفرعي: " + QuoteMath.money(bigDecimalSubtotal) + "\nالضريبة 15%: " + QuoteMath.money(bigDecimalTax) + "\nالإجمالي: " + QuoteMath.money(bigDecimalSubtotal.add(bigDecimalTax)) + " ر.س");
    }
}
