package com.amalaei.engineering;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.TimePicker;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;

public final class SoftwareActivity extends BaseSecureActivity {
    LinearLayout body;
    JSONObject draft;
    EditText follow;
    Typeface font;
    Spinner priority;
    Spinner state;
    Store store;
    EditText time;
    EditText title;
    Map<String, EditText> inputs = new LinkedHashMap();
    long owner = 0;
    int filter = 0;
    boolean today = false;
    String snapshot = "";

    public /* synthetic */ void lambda$back$0(DialogInterface dialogInterface, int i) {
        if (lambda$editor$11()) {
            list();
        }
    }

    public /* synthetic */ void lambda$back$1(DialogInterface dialogInterface, int i) {
        list();
    }

    static /* synthetic */ void lambda$btn$2(Runnable runnable, View view) {
        UiTheme.feedback(view);
        runnable.run();
    }

    public /* synthetic */ void lambda$catalog$17() {
        product(new JSONObject(), 0L);
    }

    public /* synthetic */ void lambda$catalog$18(JSONObject jSONObject, JSONObject jSONObject2) {
        product(jSONObject, jSONObject2.optLong("id"));
    }

    public /* synthetic */ void lambda$dateButton$15(final EditText editText) {
        LocalDate localDateNow = LocalDate.now();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public final void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                editText.setText(LocalDate.of(i, i2 + 1, i3).toString());
            }
        }, localDateNow.getYear(), localDateNow.getMonthValue() - 1, localDateNow.getDayOfMonth()).show();
    }

    public /* synthetic */ void lambda$editor$10() {
        try {
            capture();
            JSONObject jSONObject = this.draft.getJSONObject("details");
            label("المتبقي: " + QuoteMath.money(SoftwareWork.value(jSONObject.optString("amount")).subtract(SoftwareWork.value(jSONObject.optString("paid")))) + " ريال", 18);
        } catch (Exception e) {
            err(e);
        }
    }

    public /* synthetic */ void lambda$editor$12(DialogInterface dialogInterface, int i) {
        this.store.getWritableDatabase().delete("softwareRequests", "id=?", new String[]{String.valueOf(this.draft.optLong("id"))});
        ((NotificationManager) getSystemService(NotificationManager.class)).cancel("software", (int) this.draft.optLong("id"));
        list();
        Reminders.schedule(this);
    }

    public /* synthetic */ void lambda$editor$13() {
        new FluentDialog(this).setMessage("حذف هذا الطلب نهائيًا؟").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حذف", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                SoftwareActivity.this.lambda$editor$12(dialogInterface, i);
            }
        }).show();
    }

    public /* synthetic */ void lambda$editor$8(TimePicker timePicker, int i, int i2) {
        this.time.setText(String.format(Locale.US, "%02d:%02d", Integer.valueOf(i), Integer.valueOf(i2)));
    }

    public /* synthetic */ void lambda$editor$9() {
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public final void onTimeSet(TimePicker timePicker, int i, int i2) {
                SoftwareActivity.this.lambda$editor$8(timePicker, i, i2);
            }
        }, 9, 0, true).show();
    }

    public /* synthetic */ void lambda$list$3() {
        this.today = !this.today;
        list();
    }

    public /* synthetic */ void lambda$list$4(JSONObject jSONObject) {
        try {
            this.draft = this.store.softwareRequest(jSONObject.optLong("id"));
            editor();
        } catch (Exception e) {
            err(e);
        }
    }

    public /* synthetic */ void lambda$newWork$5(ArrayList arrayList, DialogInterface dialogInterface, int i) {
        startNew(((JSONObject) arrayList.get(i)).optLong("id"));
    }

    public /* synthetic */ void lambda$pickProduct$16(ArrayList arrayList, DialogInterface dialogInterface, int i) {
        try {
            JSONObject jSONObject = new JSONObject(((JSONObject) arrayList.get(i)).getString("data"));
            if (this.inputs.containsKey("program")) {
                this.inputs.get("program").setText(jSONObject.optString("name"));
            }
            if (this.inputs.containsKey("plan")) {
                this.inputs.get("plan").setText(jSONObject.optString("plan"));
            }
        } catch (Exception e) {
            err(e);
        }
    }

    public /* synthetic */ void lambda$product$19(EditText editText, EditText editText2, EditText editText3, EditText editText4, EditText editText5, long j, AlertDialog alertDialog, View view) {
        try {
            if (editText.getText().toString().trim().isEmpty()) {
                throw new IllegalArgumentException("أدخل اسم البرنامج");
            }
            SoftwareWork.value(editText2.getText().toString());
            JSONObject jSONObjectPut = new JSONObject().put("name", editText.getText().toString()).put("plan", editText3.getText().toString()).put("price", editText2.getText().toString()).put("period", editText4.getText().toString()).put("notes", editText5.getText().toString());
            ContentValues contentValues = new ContentValues();
            contentValues.put("data", jSONObjectPut.toString());
            if (j == 0) {
                this.store.getWritableDatabase().insertOrThrow("softwareProducts", null, contentValues);
            } else {
                this.store.getWritableDatabase().update("softwareProducts", contentValues, "id=?", new String[]{String.valueOf(j)});
            }
            alertDialog.dismiss();
            catalog();
        } catch (Exception e) {
            err(e);
        }
    }

    public /* synthetic */ void lambda$product$20(final AlertDialog alertDialog, final EditText editText, final EditText editText2, final EditText editText3, final EditText editText4, final EditText editText5, final long j, DialogInterface dialogInterface) {
        alertDialog.getButton(-1).setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                SoftwareActivity.this.lambda$product$19(editText, editText2, editText3, editText4, editText5, j, alertDialog, view);
            }
        });
    }

    public /* synthetic */ void lambda$startNew$6(long j, DialogInterface dialogInterface, int i) {
        try {
            JSONObject jSONObject = new JSONObject();
            try {
                JSONObject jSONObjectOptJSONObject = new JSONObject(this.store.lead(j).optString("requestDetails", "{}")).optJSONObject("software");
                if (jSONObjectOptJSONObject != null) {
                    String[] strArr = {"program", "plan", "platform", "needs"};
                    for (int i2 = 0; i2 < 4; i2++) {
                        String str = strArr[i2];
                        jSONObject.put(str, jSONObjectOptJSONObject.optString(str));
                    }
                    if (i == 0) {
                        jSONObject.put("endDate", jSONObjectOptJSONObject.optString("subscriptionEnd"));
                    }
                    if (i == 3) {
                        jSONObject.put("sessionDate", jSONObjectOptJSONObject.optString("demoDate"));
                    }
                }
            } catch (Exception unused) {
            }
            this.draft = new JSONObject().put("leadId", j).put("type", SoftwareWork.TYPES[i]).put("status", 0).put("priority", 1).put("details", jSONObject).put("followTime", "09:00");
            editor();
        } catch (Exception e) {
            err(e);
        }
    }

    void back() {
        if (this.draft == null) {
            finish();
            return;
        }
        try {
            capture();
            if (this.draft.toString().equals(this.snapshot)) {
                list();
                return;
            }
        } catch (Exception unused) {
        }
        new FluentDialog(this).setMessage("حفظ تعديلات الطلب؟").setPositiveButton("حفظ والعودة", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                SoftwareActivity.this.lambda$back$0(dialogInterface, i);
            }
        }).setNegativeButton("تجاهل", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                SoftwareActivity.this.lambda$back$1(dialogInterface, i);
            }
        }).setNeutralButton("متابعة التحرير", (DialogInterface.OnClickListener) null).show();
    }

    void base(String str) {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        this.body = linearLayout;
        linearLayout.setOrientation(1);
        this.body.setLayoutDirection(AppLanguage.direction(this));
        this.body.setPadding(dp(18), dp(16), dp(18), dp(24));
        this.body.setBackgroundColor(-788486);
        scrollView.addView(this.body);
        setContentView(scrollView);
        UiTheme.screen(this, scrollView);
        UiTheme.header(this.body, str, "clients");
        UiTheme.enter(this.body);
    }

    void btn(String str, final Runnable runnable) {
        Button button = new Button(this);
        button.setTypeface(this.font);
        button.setText(str);
        UiTheme.button(button, str.startsWith("+") || str.equals("حفظ الطلب"));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, dp(4), 0, dp(4));
        this.body.addView(button, layoutParams);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                SoftwareActivity.lambda$btn$2(runnable, view);
            }
        });
    }

    void capture() throws Exception {
        this.draft.put("title", this.title.getText().toString().trim()).put("status", this.state.getSelectedItemPosition()).put("priority", this.priority.getSelectedItemPosition()).put("followUp", this.follow.getText().toString().trim()).put("followTime", this.time.getText().toString().trim());
        JSONObject jSONObject = this.draft.getJSONObject("details");
        for (String str : this.inputs.keySet()) {
            jSONObject.put(str, this.inputs.get(str).getText().toString().trim());
        }
    }

    void catalog() {
        base("دليل البرامج والباقات");
        btn("+ إضافة برنامج أو باقة", new Runnable() {
            @Override
            public final void run() {
                SoftwareActivity.this.lambda$catalog$17();
            }
        });
        for (final JSONObject jSONObject : this.store.query("SELECT * FROM softwareProducts ORDER BY id DESC", new String[0])) {
            try {
                final JSONObject jSONObject2 = new JSONObject(jSONObject.getString("data"));
                label(jSONObject2.optString("name") + " • " + jSONObject2.optString("plan"), 19);
                label(jSONObject2.optString("price") + " ريال • " + jSONObject2.optString("period"), 14);
                btn("تعديل", new Runnable() {
                    @Override
                    public final void run() {
                        SoftwareActivity.this.lambda$catalog$18(jSONObject2, jSONObject);
                    }
                });
            } catch (Exception e) {
                err(e);
            }
        }
        btn("عودة إلى الطلبات", new Runnable() {
            @Override
            public final void run() {
                SoftwareActivity.this.list();
            }
        });
    }

    void dateButton(final EditText editText) {
        btn("اختيار التاريخ", new Runnable() {
            @Override
            public final void run() {
                SoftwareActivity.this.lambda$dateButton$15(editText);
            }
        });
    }

    int dp(int i) {
        return UiTheme.dp(this, i);
    }

    void editor() {
        try {
            this.inputs.clear();
            long j = 0;
            this.snapshot = this.draft.optLong("id") > 0 ? this.store.softwareRequest(this.draft.optLong("id")).toString() : "";
            base(SoftwareWork.label(this.draft.optString("type")));
            label("العميل: " + this.store.lead(this.draft.optLong("leadId")).optString("name"), 17);
            this.title = field("عنوان الطلب *", this.draft.optString("title"));
            this.state = select("الحالة", SoftwareWork.statuses(this.draft.optString("type")), this.draft.optInt("status"));
            this.priority = select("الأولوية", new String[]{"منخفضة", "عادية", "عالية", "عاجلة"}, this.draft.optInt("priority", 1));
            JSONObject jSONObject = this.draft.getJSONObject("details");
            String[][] strArrFields = SoftwareWork.fields(this.draft.optString("type"));
            int length = strArrFields.length;
            int i = 0;
            while (i < length) {
                String[] strArr = strArrFields[i];
                String str = strArr[1];
                long j2 = j;
                String str2 = strArr[0];
                EditText editTextField = field(str, jSONObject.optString(str2, str2.equals("renewDays") ? "7" : ""));
                this.inputs.put(strArr[0], editTextField);
                if (strArr[0].equals("amount") || strArr[0].equals("paid")) {
                    editTextField.setInputType(8194);
                }
                if (strArr[0].equals("renewDays")) {
                    editTextField.setInputType(2);
                }
                if (strArr[0].endsWith("Date")) {
                    dateButton(editTextField);
                }
                i++;
                j = j2;
            }
            long j3 = j;
            btn("اختيار برنامج من الدليل", new Runnable() {
                @Override
                public final void run() {
                    SoftwareActivity.this.lambda$editor$7();
                }
            });
            EditText editTextField2 = field("موعد متابعة مستقل YYYY-MM-DD", this.draft.optString("followUp"));
            this.follow = editTextField2;
            dateButton(editTextField2);
            this.time = field("وقت التنبيه HH:mm", this.draft.optString("followTime", "09:00"));
            btn("اختيار وقت التنبيه", new Runnable() {
                @Override
                public final void run() {
                    SoftwareActivity.this.lambda$editor$9();
                }
            });
            label("تنبيهات تلقائية للتجديد والدفعات والتسليم والجلسات عند تحديد تواريخها. الطلب المكتمل أو الملغى لا يرسل تنبيهات.", 13);
            if (this.inputs.containsKey("amount")) {
                btn("حساب المتبقي", new Runnable() {
                    @Override
                    public final void run() {
                        SoftwareActivity.this.lambda$editor$10();
                    }
                });
            }
            btn("حفظ الطلب", new Runnable() {
                @Override
                public final void run() {
                    SoftwareActivity.this.lambda$editor$11();
                }
            });
            if (this.draft.optLong("id") > j3) {
                btn("حذف الطلب", new Runnable() {
                    @Override
                    public final void run() {
                        SoftwareActivity.this.lambda$editor$13();
                    }
                });
            }
            btn("العودة إلى القائمة", new Runnable() {
                @Override
                public final void run() {
                    SoftwareActivity.this.back();
                }
            });
        } catch (Exception e) {
            err(e);
        }
    }

    void err(Exception exc) {
        new FluentDialog(this).setMessage(exc.getMessage() == null ? "تعذر إتمام العملية" : exc.getMessage()).setPositiveButton("حسنًا", (DialogInterface.OnClickListener) null).show();
    }

    EditText field(String str, String str2) {
        label(str, 13);
        EditText editText = new EditText(this);
        editText.setTypeface(this.font);
        editText.setTextSize(15.0f);
        editText.setInputType(131073);
        editText.setText(str2);
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
        textView.setPadding(0, dp(7), 0, dp(7));
        this.body.addView(textView);
        return textView;
    }

    void list() {
        char c;
        this.draft = null;
        int i = 0;
        getPreferences(0).edit().remove("draft").apply();
        base("إدارة نشاط البرامج");
        if (this.owner > 0) {
            label("العميل: " + this.store.lead(this.owner).optString("name"), 18);
        }
        char c2 = 14;
        label("اشتراكات وتطوير ودعم وجلسات مستقلة لكل عميل", 14);
        btn("+ طلب أو اشتراك جديد", new Runnable() {
            @Override
            public final void run() {
                SoftwareActivity.this.newWork();
            }
        });
        btn("دليل البرامج والباقات", new Runnable() {
            @Override
            public final void run() {
                SoftwareActivity.this.catalog();
            }
        });
        btn(this.today ? "عرض كل الطلبات" : "المستحق اليوم والمتأخر", new Runnable() {
            @Override
            public final void run() {
                SoftwareActivity.this.lambda$list$3();
            }
        });
        int i2 = 1;
        select("نوع الطلب", new String[]{"كل الأنواع", "الاشتراكات", "التطوير", "الدعم الفني", "التجارب والتدريب"}, this.filter).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i3, long j) {
                if (i3 != SoftwareActivity.this.filter) {
                    SoftwareActivity.this.filter = i3;
                    SoftwareActivity.this.list();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        int i3 = 0;
        for (final JSONObject jSONObject : this.store.query("SELECT * FROM softwareRequests " + (this.owner > 0 ? "WHERE leadId=" + this.owner : "") + " ORDER BY id DESC", new String[0])) {
            LinearLayout linearLayout = this.body;
            try {
                JSONObject jSONObject2 = new JSONObject(jSONObject.getString("data"));
                if (this.filter > 0 && !jSONObject2.optString("type").equals(SoftwareWork.TYPES[this.filter - 1])) {
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                boolean due = false;
                for (String str : SoftwareWork.REMINDERS) {
                    String strDue = SoftwareWork.due(jSONObject2, str);
                    if (!strDue.isEmpty() && strDue.compareTo(LocalDate.now().toString()) <= 0) {
                        sb.append(SoftwareWork.reminderLabel(str)).append(": ").append(strDue).append("\n");
                        due = true;
                    }
                }
                if (this.today && !due) {
                    continue;
                }
                this.body = UiTheme.card(linearLayout);
                label(jSONObject2.optString("title"), 20);
                label(this.store.lead(jSONObject.optLong("leadId")).optString("name") + " • " + SoftwareWork.label(jSONObject2.optString("type")) + " • " + SoftwareWork.statuses(jSONObject2.optString("type"))[jSONObject2.optInt("status")], 13);
                if (sb.length() > 0) {
                    label(sb.toString(), 14);
                }
                JSONObject jSONObjectOptJSONObject = jSONObject2.optJSONObject("details");
                if (jSONObjectOptJSONObject != null && SoftwareWork.value(jSONObjectOptJSONObject.optString("amount")).signum() > 0) {
                    label("المتبقي: " + QuoteMath.money(SoftwareWork.value(jSONObjectOptJSONObject.optString("amount")).subtract(SoftwareWork.value(jSONObjectOptJSONObject.optString("paid")))) + " ريال", 14);
                }
                btn("فتح التفاصيل", new Runnable() {
                    @Override
                    public final void run() {
                        SoftwareActivity.this.lambda$list$4(jSONObject);
                    }
                });
                this.body = linearLayout;
                i3++;
            } catch (Exception e) {
                this.body = linearLayout;
                err(e);
            }
        }
        if (i3 == 0) {
            label("لا توجد طلبات في هذا العرض", 17);
        }
        btn("رجوع", new Runnable() {
            @Override
            public final void run() {
                SoftwareActivity.this.finish();
            }
        });
    }

    void newWork() {
        final ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM leads WHERE business='software' ORDER BY name", new String[0]);
        long j = this.owner;
        if (j > 0) {
            startNew(j);
            return;
        }
        if (arrayListQuery.isEmpty()) {
            err(new IllegalArgumentException("أضف عميلًا إلى نشاط البرامج أولًا"));
            return;
        }
        int size = arrayListQuery.size();
        String[] strArr = new String[size];
        for (int i = 0; i < size; i++) {
            strArr[i] = arrayListQuery.get(i).optString("name");
        }
        new FluentDialog(this).setTitle("اختر العميل").setItems(strArr, new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i2) {
                SoftwareActivity.this.lambda$newWork$5(arrayListQuery, dialogInterface, i2);
            }
        }).show();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public void onCreate(Bundle bundle) {
        String string;
        long longExtra;
        JSONObject jSONObjectSoftwareRequest;
        long jOptLong;
        long jOptLong2;
        super.onCreate(bundle);
        this.font = Typeface.createFromAsset(getAssets(), "fonts/Cairo.ttf");
        this.store = new Store(this);
        this.owner = getIntent().getLongExtra("leadId", 0L);
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, new OnBackInvokedCallback() {
                @Override
                public final void onBackInvoked() {
                    SoftwareActivity.this.back();
                }
            });
        }
        try {
            long longExtra2 = getIntent().getLongExtra("requestId", 0L);
            if (bundle != null || longExtra2 <= 0) {
                if (bundle == null) {
                    string = getPreferences(0).getString("draft", "");
                } else {
                    string = bundle.getString("draft", "");
                }
                if (!string.isEmpty()) {
                    JSONObject jSONObject = new JSONObject(string);
                    this.draft = jSONObject;
                    jOptLong2 = jSONObject.optLong("leadId");
                    this.owner = jOptLong2;
                    if (!this.store.lead(jOptLong2).optString("business").equals("software")) {
                        this.draft = null;
                        getPreferences(0).edit().remove("draft").apply();
                    }
                }
                longExtra = getIntent().getLongExtra("requestId", 0L);
                if (longExtra > 0) {
                    jSONObjectSoftwareRequest = this.store.softwareRequest(longExtra);
                    this.draft = jSONObjectSoftwareRequest;
                    if (jSONObjectSoftwareRequest.optLong("id") > 0) {
                        jOptLong = this.draft.optLong("leadId");
                        this.owner = jOptLong;
                    }
                }
                list();
                return;
            }
            JSONObject jSONObjectSoftwareRequest2 = this.store.softwareRequest(longExtra2);
            this.draft = jSONObjectSoftwareRequest2;
            if (jSONObjectSoftwareRequest2.optLong("id") <= 0) {
                if (bundle == null) {
                    string = getPreferences(0).getString("draft", "");
                } else {
                    string = bundle.getString("draft", "");
                }
                if (!string.isEmpty()) {
                    JSONObject jSONObject2 = new JSONObject(string);
                    this.draft = jSONObject2;
                    jOptLong2 = jSONObject2.optLong("leadId");
                    this.owner = jOptLong2;
                    if (!this.store.lead(jOptLong2).optString("business").equals("software")) {
                        this.draft = null;
                        getPreferences(0).edit().remove("draft").apply();
                    }
                }
                longExtra = getIntent().getLongExtra("requestId", 0L);
                if (longExtra > 0) {
                    jSONObjectSoftwareRequest = this.store.softwareRequest(longExtra);
                    this.draft = jSONObjectSoftwareRequest;
                    if (jSONObjectSoftwareRequest.optLong("id") > 0) {
                        jOptLong = this.draft.optLong("leadId");
                    }
                }
                list();
                return;
            }
            jOptLong = this.draft.optLong("leadId");
            this.owner = jOptLong;
            editor();
        } catch (Exception unused) {
            this.draft = null;
        }
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
        Reminders.schedule(this);
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

    public void lambda$editor$7() {
        final ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM softwareProducts ORDER BY id DESC", new String[0]);
        String[] strArr = new String[arrayListQuery.size()];
        for (int i = 0; i < arrayListQuery.size(); i++) {
            try {
                JSONObject jSONObject = new JSONObject(arrayListQuery.get(i).getString("data"));
                strArr[i] = jSONObject.optString("name") + " • " + jSONObject.optString("plan");
            } catch (Exception unused) {
            }
        }
        new FluentDialog(this).setTitle("البرامج والباقات").setItems(strArr, new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i2) {
                SoftwareActivity.this.lambda$pickProduct$16(arrayListQuery, dialogInterface, i2);
            }
        }).show();
    }

    void product(JSONObject jSONObject, final long j) {
        LinearLayout linearLayout = this.body;
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        this.body = linearLayout2;
        final EditText editTextField = field("اسم البرنامج *", jSONObject.optString("name"));
        final EditText editTextField2 = field("الباقة", jSONObject.optString("plan"));
        final EditText editTextField3 = field("السعر المرجعي (ريال)", jSONObject.optString("price"));
        final EditText editTextField4 = field("الدورة: شهري / سنوي / مرة واحدة", jSONObject.optString("period"));
        final EditText editTextField5 = field("وصف المميزات", jSONObject.optString("notes"));
        this.body = linearLayout;
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(linearLayout2);
        final AlertDialog alertDialogCreate = new FluentDialog(this).setTitle("برنامج / باقة").setView(scrollView).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حفظ", (DialogInterface.OnClickListener) null).create();
        alertDialogCreate.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public final void onShow(DialogInterface dialogInterface) {
                SoftwareActivity.this.lambda$product$20(alertDialogCreate, editTextField, editTextField3, editTextField2, editTextField4, editTextField5, j, dialogInterface);
            }
        });
        alertDialogCreate.show();
    }

    public boolean lambda$editor$11() {
        try {
            capture();
            this.store.saveSoftware(this.draft);
            this.snapshot = this.draft.toString();
            getPreferences(0).edit().putString("draft", this.snapshot).apply();
            ((NotificationManager) getSystemService(NotificationManager.class)).cancel("software", (int) this.draft.optLong("id"));
            Reminders.schedule(this);
            Toast.makeText(this, "حُفظ الطلب", 0).show();
            return true;
        } catch (Exception e) {
            err(e);
            return false;
        }
    }

    Spinner select(String str, String[] strArr, int i) {
        label(str, 13);
        Spinner spinner = new Spinner(this);
        spinner.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, strArr));
        spinner.setSelection(i);
        this.body.addView(spinner);
        return spinner;
    }

    void startNew(final long j) {
        if (this.store.lead(j).optString("business").equals("software")) {
            new FluentDialog(this).setTitle("نوع الطلب الجديد").setItems(SoftwareWork.LABELS, new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialogInterface, int i) {
                    SoftwareActivity.this.lambda$startNew$6(j, dialogInterface, i);
                }
            }).show();
        } else {
            err(new IllegalArgumentException("العميل لا ينتمي لنشاط البرامج"));
        }
    }
}
