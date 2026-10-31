package com.amalaei.engineering;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.app.role.RoleManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public final class MainActivity extends BaseSecureActivity {
    private static final int MAX_BACKUP_BYTES = 26214400;
    private static final int MAX_ENCRYPTED_BACKUP_BYTES = 37748736;
    private int backgroundOps;
    private Typeface cairo;
    private LinearLayout content;
    private char[] pendingBackupPassword;
    private String pendingCsv;
    private Spinner postCategory;
    private AlertDialog postDialog;
    private JSONObject postDraft;
    private LinearLayout root;
    private Store store;
    private static final String[] POST_CATEGORIES = {"تعريف", "عرض", "معلومة", "نصيحة", "مقارنة", "تجربة استخدام"};
    private static final String[] ORGANIZATION_ACTIVITIES = {"خدمات", "تجارة عامة", "دعم فني", "صيانة", "تسويق", "تسويق رقمي", "بيع مباشر", "بيع تجزئة", "بيع جملة", "خدمات لوجستية", "نقل وتوصيل", "مقاولات", "أجهزة مساحة", "تقنية وبرمجيات", "استشارات", "تدريب وتعليم", "نشاط حرفي", "تصنيع", "عقارات", "مطاعم وأغذية", "صحة وتجميل", "سيارات", "سياحة وسفر", "نشاط آخر — كتابة يدوية"};
    private final int BLUE = -15434365;
    private final int NAVY = -15717821;
    private final int INK = -15189940;
    private final int MUTED = -10192758;
    private final int BG = -788486;
    private final int LINE = -2299923;
    private String business = "survey";
    private String tab = "home";
    private String search = "";
    private int limit = 100;
    private int requestFilter = 0;
    private int customerStageFilter = 0;
    private String followDate = "";
    private boolean ready = false;
    private boolean importing = false;
    private final Map<String, EditText> postInputs = new LinkedHashMap();
    private int adFilter = 0;
    private int adLimit = 20;
    private final LinkedHashSet<Long> selectedCalls = new LinkedHashSet<>();

    public void adCopy() {
        final String str = this.business;
        LinearLayout linearLayoutForm = form();
        muted(linearLayoutForm, "قوالب نصية محلية مجانية؛ لا تولّد صورًا ولا تضيف خصمًا أو وعدًا من تلقاء نفسها.");
        final EditText editTextField = field(linearLayoutForm, str.equals("survey") ? "الجهاز أو الخدمة" : "البرنامج أو الميزة", "", 1);
        final EditText editTextField2 = field(linearLayoutForm, "الفائدة أو المعلومات التي تريد إبرازها", "", 131073);
        final EditText editTextField3 = field(linearLayoutForm, "اسم النشاط (اختياري)", profileBrand(str), 1);
        final EditText editTextField4 = field(linearLayoutForm, "بيانات المنشأة المصاحبة للنص — يمكنك تعديلها لهذا المنشور", profileFooter(str, false), 131073);
        new FluentDialog(this).setTitle("ثلاث أفكار للنص").setView(linearLayoutForm).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("تجهيز الصيغ", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$adCopy$132(editTextField, editTextField2, editTextField3, editTextField4, str, dialogInterface, i);
            }
        }).show();
    }

    private void advertising() {
        UiTheme.header(this.content, tr("المنشورات", "Posts"), "ads");
        JSONObject jSONObjectProfile = this.store.profile(this.business);
        muted(this.content, jSONObjectProfile.optString("name").isEmpty() ? "أضف بيانات منشأة هذا النشاط" : jSONObjectProfile.optString("name"));
        button(this.content, "بيانات المنشأة ونوع نشاطها", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$advertising$111();
            }
        });
        muted(this.content, "مكتبة مستقلة لهذا النشاط • صور ونصوص جاهزة للمشاركة");
        JSONObject jSONObject = this.store.query("SELECT COUNT(*) AS total,SUM(CASE WHEN lastPreparedAt=0 THEN 1 ELSE 0 END) AS unused,SUM(CASE WHEN plannedDate<>'' AND plannedDate<=? THEN 1 ELSE 0 END) AS due FROM posts WHERE business=?", LocalDate.now().toString(), this.business).get(0);
        LinearLayout linearLayoutRow = row();
        this.content.addView(linearLayoutRow);
        metric(linearLayoutRow, "منشورات", jSONObject.optInt("total"), -15434365);
        metric(linearLayoutRow, "لم تُفتح", jSONObject.optInt("unused"), -13333386);
        metric(linearLayoutRow, "مخطط لها", jSONObject.optInt("due"), -2714312);
        primary(button(this.content, "+  إضافة صورة أو منشور", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$advertising$112();
            }
        }));
        button(this.content, "تجهيز ثلاث صيغ للنص", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.adCopy();
            }
        });
        spinner(this.content, "عرض المكتبة", new String[]{"كل المنشورات", "المخطط لليوم والمتأخر", "لم يُفتح للمشاركة بعد"}, this.adFilter).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                if (MainActivity.this.adFilter != i) {
                    MainActivity.this.adFilter = i;
                    MainActivity.this.adLimit = 20;
                    MainActivity.this.render();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        muted(this.content, tr("اضغط مشاركة الصورة والنص لفتح خيارات Facebook وWhatsApp Business وجدولة التذكيرات وسجل التأكيدات. الإرسال داخل المنصة يدوي.", "Tap Share image and text for Facebook, WhatsApp Business, reminders and confirmation history. Sending inside each platform is manual."));
        int i = this.adFilter;
        String str = i == 1 ? " AND plannedDate<>'' AND plannedDate<=?" : i == 2 ? " AND lastPreparedAt=0" : "";
        ArrayList<JSONObject> arrayListQuery = i == 1 ? this.store.query("SELECT * FROM posts WHERE business=?" + str + " ORDER BY plannedDate,createdAt DESC LIMIT 21 OFFSET " + (this.adLimit - 20), this.business, LocalDate.now().toString()) : this.store.query("SELECT * FROM posts WHERE business=?" + str + " ORDER BY createdAt DESC LIMIT 21 OFFSET " + (this.adLimit - 20), this.business);
        if (arrayListQuery.isEmpty()) {
            empty("جهّز أول منشور لهذا النشاط", "أضف صورة من هاتفك ونصًا قصيرًا؛ ستجده هنا عند الحاجة");
        }
        int i2 = 0;
        for (final JSONObject jSONObject2 : arrayListQuery) {
            int i3 = i2 + 1;
            if (i2 >= 20) {
                break;
            }
            LinearLayout linearLayoutCard = card(this.content);
            linearLayoutCard.addView(text(jSONObject2.optString("title"), 20));
            linearLayoutCard.addView(pill(jSONObject2.optString("category"), -15434365, -1575950));
            showPostImage(linearLayoutCard, jSONObject2.optString("image"));
            String strOptString = jSONObject2.optString("body");
            if (!strOptString.isEmpty()) {
                if (strOptString.length() > 650) {
                    strOptString = strOptString.substring(0, 650) + "…";
                }
                TextView textViewText = text(strOptString, 14);
                textViewText.setTextIsSelectable(true);
                linearLayoutCard.addView(textViewText);
            }
            if (!jSONObject2.optString("plannedDate").isEmpty()) {
                muted(linearLayoutCard, "يوم النشر المخطط: " + jSONObject2.optString("plannedDate"));
            }
            muted(linearLayoutCard, jSONObject2.optLong("lastPreparedAt") == 0 ? "لم تُفتح المشاركة بعد" : "آخر فتح للمشاركة: " + ((Object) DateFormat.format("yyyy-MM-dd HH:mm", jSONObject2.optLong("lastPreparedAt"))));
            primary(button(linearLayoutCard, "مشاركة الصورة والنص", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$advertising$113(jSONObject2);
                }
            }));
            LinearLayout linearLayoutRow2 = row();
            linearLayoutCard.addView(linearLayoutRow2);
            button(linearLayoutRow2, "تعديل", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$advertising$114(jSONObject2);
                }
            }).setLayoutParams(new LinearLayout.LayoutParams(0, dp(44), 1.0f));
            Button button = button(linearLayoutRow2, "نسخة جديدة", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$advertising$115(jSONObject2);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(44), 1.0f);
            layoutParams.setMargins(dp(6), 0, dp(6), 0);
            button.setLayoutParams(layoutParams);
            button(linearLayoutCard, "سجل فتح المشاركة", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$advertising$116(jSONObject2);
                }
            });
            i2 = i3;
        }
        if (this.adLimit > 20) {
            button(this.content, "الصفحة السابقة", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$advertising$117();
                }
            });
        }
        if (arrayListQuery.size() > 20) {
            button(this.content, "الصفحة التالية", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$advertising$118();
                }
            });
        }
    }

    private void alert(String str) {
        new FluentDialog(this).setMessage(str).setPositiveButton("حسنًا", (DialogInterface.OnClickListener) null).show();
    }

    private boolean allowedDetail(String str, String str2) {
        if (str.isEmpty()) {
            return false;
        }
        return (str.equals("survey") ? "|device|model|service|condition|quantity|budget|purchaseDate|calibrationDate|" : "|program|platform|needs|plan|budget|demoDate|subscriptionEnd|").contains("|" + str2 + "|");
    }

    private void askRestorePassword(final String str, boolean z) {
        AlertDialog.Builder negativeButton;
        DialogInterface.OnClickListener onClickListener;
        String str2;
        if (z) {
            LinearLayout linearLayoutForm = form();
            final EditText editTextField = field(linearLayoutForm, "كلمة مرور النسخة", "", 129);
            negativeButton = new FluentDialog(this).setTitle("فك تشفير النسخة").setView(linearLayoutForm).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null);
            onClickListener = new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.lambda$askRestorePassword$99(str, editTextField, dialogInterface, i);
                }
            };
            str2 = "استعادة";
        } else {
            negativeButton = new FluentDialog(this).setTitle("نسخة قديمة غير مشفرة").setMessage("هذا الملف غير مشفر وقد يحتوي بيانات حساسة بنص مقروء. هل تريد استيراده ثم إنشاء نسخة مشفرة جديدة؟").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null);
            onClickListener = new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.lambda$askRestorePassword$98(str, dialogInterface, i);
                }
            };
            str2 = "استيراد";
        }
        negativeButton.setPositiveButton(str2, onClickListener).show();
    }

    public void assignCalls() {
        String str;
        if (this.selectedCalls.isEmpty()) {
            str = "حدد الأرقام التي تريد اعتمادها أولًا";
        } else {
            int iAssign = this.store.assign(new ArrayList(this.selectedCalls), "survey");
            this.selectedCalls.clear();
            render();
            str = "تم اعتماد " + iAssign + " رقمًا كعملاء";
        }
        alert(str);
    }

    private byte[] backupBytes() throws Exception {
        try (Store snapshot = new Store(getApplicationContext())) {
            byte[] bytes = snapshot.backup().toString(2).getBytes(StandardCharsets.UTF_8);
            if (bytes.length <= MAX_BACKUP_BYTES) {
                return bytes;
            }
            throw new IllegalArgumentException("حجم النسخة يتجاوز 25 ميغابايت؛ احذف الرسائل أو الصور غير اللازمة ثم أعد المحاولة");
        } catch (OutOfMemoryError unused) {
            throw new IllegalArgumentException("النسخة أكبر من ذاكرة الهاتف؛ احذف الرسائل أو الصور غير اللازمة ثم أعد المحاولة");
        }
    }

    private Button button(LinearLayout linearLayout, String str, final Runnable runnable) {
        Button button = new Button(this);
        button.setText(str);
        UiTheme.button(button, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, dp(5), 0, dp(5));
        linearLayout.addView(button, layoutParams);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                MainActivity.lambda$button$0(runnable, view);
            }
        });
        return button;
    }

    private void calculatorCard() {
        LinearLayout linearLayoutRow = row();
        linearLayoutRow.setGravity(16);
        linearLayoutRow.setPadding(dp(18), dp(18), dp(18), dp(18));
        linearLayoutRow.setBackground(gradient(-15717821, -15315357, 20));
        linearLayoutRow.setElevation(dp(3));
        linearLayoutRow.setMinimumHeight(dp(112));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, dp(16), 0, dp(10));
        this.content.addView(linearLayoutRow, layoutParams);
        LinearLayout linearLayoutColumn = column();
        linearLayoutColumn.setGravity(16);
        linearLayoutRow.addView(linearLayoutColumn, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textViewText = text("الحاسبة الذكية", 19);
        textViewText.setTextColor(-1);
        linearLayoutColumn.addView(textViewText, new LinearLayout.LayoutParams(-1, -2));
        TextView textViewText2 = text("حساب سريع وضريبة القيمة المضافة 15%", 12);
        textViewText2.setTextColor(-3343375);
        textViewText2.setSingleLine(false);
        textViewText2.setPadding(0, dp(3), 0, dp(3));
        linearLayoutColumn.addView(textViewText2, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout linearLayoutColumn2 = column();
        linearLayoutColumn2.setGravity(17);
        linearLayoutColumn2.setBackground(shape(654311423, 16, 0));
        linearLayoutColumn2.addView(new Glyph(this, "calculator", -1), new LinearLayout.LayoutParams(dp(30), dp(30)));
        linearLayoutRow.addView(linearLayoutColumn2, new LinearLayout.LayoutParams(dp(54), dp(54)));
        linearLayoutRow.setClickable(true);
        linearLayoutRow.setForeground(new RippleDrawable(ColorStateList.valueOf(872415231), null, null));
        linearLayoutRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                MainActivity.this.lambda$calculatorCard$27(view);
            }
        });
        linearLayoutRow.setContentDescription("فتح الحاسبة الذكية");
    }

    private void callSetup() {
        int i;
        LinearLayout linearLayoutForm = form();
        muted(linearLayoutForm, "يُنشأ العميل تلقائيًا من الرقم والاسم المتاح. لن تُطلب منك بقية البيانات. سيُحدّث السجل عند فتح التطبيق إذا سمح الهاتف بالوصول إليه.");
        String[] strArr = {"اتركه غير محدد للمراجعة الجماعية", "أجهزة المساحة", "البرامج والتطبيقات"};
        if (getSharedPreferences("settings", 0).getString("newBusiness", "").equals("survey")) {
            i = 1;
        } else {
            i = getSharedPreferences("settings", 0).getString("newBusiness", "").equals("software") ? 2 : 0;
        }
        final Spinner spinner = spinner(linearLayoutForm, "النشاط الافتراضي للأرقام الجديدة", strArr, i);
        spinner.setSelection("survey".equals("survey") ? 1 : 2);
        spinner.setEnabled(false);
        new FluentDialog(this).setTitle("إعداد المكالمات").setView(linearLayoutForm).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("تفعيل واستيراد", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i2) {
                MainActivity.this.lambda$callSetup$87(spinner, dialogInterface, i2);
            }
        }).show();
    }

    private void capturePost() {
        if (this.postDraft == null) {
            return;
        }
        try {
            for (Map.Entry<String, EditText> entry : this.postInputs.entrySet()) {
                this.postDraft.put(entry.getKey(), entry.getValue().getText().toString());
            }
            Spinner spinner = this.postCategory;
            if (spinner != null) {
                this.postDraft.put("category", POST_CATEGORIES[spinner.getSelectedItemPosition()]);
            }
        } catch (Exception unused) {
        }
    }

    private LinearLayout card(LinearLayout linearLayout) {
        return UiTheme.card(linearLayout);
    }

    public void chooseBackupPassword() {
        LinearLayout linearLayoutForm = form();
        final EditText editTextField = field(linearLayoutForm, "كلمة المرور — 8 أحرف على الأقل", "", 129);
        final EditText editTextField2 = field(linearLayoutForm, "تأكيد كلمة المرور", "", 129);
        new FluentDialog(this).setTitle("حماية النسخة الاحتياطية").setView(linearLayoutForm).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("اختيار مكان الحفظ", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$chooseBackupPassword$88(editTextField, editTextField2, dialogInterface, i);
            }
        }).show();
    }

    public void chooseCallCsv() {
        if (checkSelfPermission("android.permission.READ_CONTACTS") != 0) {
            contactDisclosure(14, this::openCallCsv);
        } else {
            openCallCsv();
        }
    }

    public void chooseCsvExport() {
        new FluentDialog(this).setTitle("تصدير CSV").setMessage("يحتوي الملف أرقام العملاء وبياناتهم، وهو غير مشفر. اختر مكان حفظ آمنًا ولا تشاركه إلا عند الحاجة.").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("متابعة", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$chooseCsvExport$89(dialogInterface, i);
            }
        }).show();
    }

    private void chooseCsvKind() {
        new FluentDialog(this).setTitle("اختر البيانات المراد تصديرها").setItems(new String[]{"العملاء", "عروض الأسعار", "بنود الدفاتر"}, new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$chooseCsvKind$90(dialogInterface, i);
            }
        }).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).show();
    }

    public void chooseLanguage() {
        final boolean zEnglish = AppLanguage.english(this);
        new FluentDialog(this).setTitle(tr("لغة التطبيق", "App language")).setSingleChoiceItems(new String[]{"العربية", "English"}, zEnglish ? 1 : 0, new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$chooseLanguage$76(zEnglish, dialogInterface, i);
            }
        }).setNegativeButton(tr("إلغاء", "Cancel"), (DialogInterface.OnClickListener) null).show();
    }

    private void chooseQuick(final JSONObject jSONObject, final Analysis analysis, final String str) {
        int i = 0;
        final ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM leads ORDER BY name COLLATE NOCASE LIMIT 1000", new String[0]);
        JSONObject jSONObject2 = analysis.phone.isEmpty() ? new JSONObject() : this.store.byPhone(analysis.phone);
        if (jSONObject2.optLong("id") > 0) {
            Iterator<JSONObject> it = arrayListQuery.iterator();
            boolean z = false;
            while (it.hasNext()) {
                if (it.next().optLong("id") == jSONObject2.optLong("id")) {
                    z = true;
                }
            }
            if (!z) {
                arrayListQuery.add(0, jSONObject2);
            }
        }
        String[] strArr = new String[arrayListQuery.size() + 1];
        strArr[0] = "إنشاء عميل من المعلومات المستخرجة";
        int i2 = 0;
        while (i < arrayListQuery.size()) {
            int i3 = i + 1;
            strArr[i3] = arrayListQuery.get(i).optString("name") + " • " + arrayListQuery.get(i).optString("phone", "");
            if (arrayListQuery.get(i).optLong("id") == jSONObject2.optLong("id") && jSONObject2.optLong("id") > 0) {
                i2 = i3;
            }
            i = i3;
        }
        LinearLayout linearLayoutForm = form();
        if (i2 > 0) {
            muted(linearLayoutForm, "وجدنا عميلًا بنفس الرقم؛ تم اقتراحه لتجنب التكرار.");
        }
        if (analysis.phones.size() > 1) {
            muted(linearLayoutForm, "ورد أكثر من رقم في النص. راجع رقم العميل؛ لن نختار أحدها من محتوى الرسالة تلقائيًا.");
        }
        final Spinner spinner = spinner(linearLayoutForm, "العميل", strArr, i2);
        new FluentDialog(this).setTitle("ربط المعلومات بالعميل").setView(linearLayoutForm).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("مراجعة وحفظ سريع", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i4) {
                MainActivity.this.lambda$chooseQuick$57(jSONObject, spinner, arrayListQuery, analysis, str, dialogInterface, i4);
            }
        }).show();
    }

    private void clients() {
        String string;
        String str;
        int i = 0;
        JSONObject jSONObject = ((!this.business.equals("survey") || this.tab.equals("unassigned") || this.requestFilter <= 0) ? this.store.query("SELECT COUNT(*) AS total, SUM(CASE WHEN stage<4 THEN 1 ELSE 0 END) AS active, SUM(CASE WHEN stage=4 THEN 1 ELSE 0 END) AS won FROM leads WHERE business=?", this.business) : this.store.query("SELECT COUNT(*) AS total, SUM(CASE WHEN stage<4 THEN 1 ELSE 0 END) AS active, SUM(CASE WHEN stage=4 THEN 1 ELSE 0 END) AS won FROM leads WHERE business=? AND requestType=?", this.business, SurveyRequests.KEYS[this.requestFilter - 1])).get(0);
        UiTheme.header(this.content, tr(this.tab.equals("today") ? "المتابعات" : this.tab.equals("unassigned") ? "الوارد للمراجعة" : "العملاء", this.tab.equals("today") ? "Follow-ups" : this.tab.equals("unassigned") ? "Review inbox" : "Customers"), this.tab.equals("today") ? "today" : this.tab.equals("unassigned") ? "unassigned" : "clients");
        muted(this.content, this.tab.equals("today") ? "المواعيد المستحقة والمتأخرة في مكان واحد" : this.tab.equals("unassigned") ? "هذه أرقام من سجل المكالمات وليست عملاء حتى تعتمدها بنفسك" : "ملفات العملاء المعتمدة وفرص المتابعة الحالية");
        if (!this.tab.equals("unassigned")) {
            LinearLayout linearLayoutRow = row();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            layoutParams.setMargins(0, dp(14), 0, dp(12));
            this.content.addView(linearLayoutRow, layoutParams);
            metric(linearLayoutRow, "إجمالي العملاء", jSONObject.optInt("total"), -15434365);
            metric(linearLayoutRow, this.business.equals("survey") ? "طلبات نشطة" : "فرص نشطة", jSONObject.optInt("active"), -2714312);
            metric(linearLayoutRow, "مكتملون", jSONObject.optInt("won"), -13333386);
        }
        if (this.business.equals("software")) {
            button(this.content, "اشتراكات ومشاريع ودعم البرامج", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$clients$13();
                }
            });
        }
        if (this.business.equals("survey") && this.tab.equals("clients")) {
            button(this.content, "عروض أسعار المساحة • إنشاء PDF", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$clients$14();
                }
            });
        }
        if (!this.tab.equals("unassigned")) {
            primary(button(this.content, "إضافة عميل", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$clients$15();
                }
            }));
        }
        if (this.tab.equals("today") && (!Reminders.enabled(this) || !Reminders.allowed(this))) {
            primary(button(this.content, "تفعيل إشعارات المتابعة", this::enableNotifications));
        }
        if (this.business.equals("survey") && !this.tab.equals("unassigned")) {
            spinner(this.content, "نوع الطلب", new String[]{"جميع الطلبات", "المبيعات", "الدعم الفني", "الصيانة", "المعايرة"}, this.requestFilter).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i2, long j) {
                    if (i2 != MainActivity.this.requestFilter) {
                        MainActivity.this.requestFilter = i2;
                        MainActivity.this.limit = 100;
                        MainActivity.this.render();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
        if (this.tab.equals("unassigned")) {
            LinearLayout linearLayoutCard = card(this.content);
            linearLayoutCard.addView(text("راجع قبل الاعتماد", 19));
            muted(linearLayoutCard, "الاستيراد لا ينشئ عملاء تلقائيًا. اختر فقط الأرقام التي تتأكد أنها عملاء ثم اعتمدها.");
            primary(button(linearLayoutCard, "استيراد ملف سجل المكالمات CSV", this::chooseCallCsv));
            LinearLayout linearLayoutRow2 = row();
            linearLayoutCard.addView(linearLayoutRow2);
            button(linearLayoutRow2, "اعتماد المحدد", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.assignCalls();
                }
            }).setLayoutParams(new LinearLayout.LayoutParams(0, dp(44), 1.0f));
            Button button = button(linearLayoutRow2, "حذف غير المعتمد", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$clients$17();
                }
            });
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, dp(44), 1.0f);
            layoutParams2.setMargins(dp(6), 0, 0, 0);
            button.setLayoutParams(layoutParams2);
        }
        if (this.tab.equals("clients")) {
            customerFilters();
            LinearLayout linearLayoutRow3 = row();
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams3.setMargins(0, dp(10), 0, dp(10));
            this.content.addView(linearLayoutRow3, layoutParams3);
            final EditText editText = new EditText(this);
            editText.setTextSize(14.0f);
            editText.setTypeface(this.cairo);
            editText.setSingleLine(true);
            editText.setHint("ابحث بالاسم أو الرقم أو الشركة");
            editText.setHintTextColor(-10192758);
            editText.setPadding(dp(14), 0, dp(14), 0);
            editText.setBackground(shape(-1, 14, -2299923));
            editText.setText(this.search);
            linearLayoutRow3.addView(editText, new LinearLayout.LayoutParams(0, dp(48), 1.0f));
            final Button button2 = button(linearLayoutRow3, "بحث", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$clients$18(editText);
                }
            });
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(dp(64), dp(48));
            layoutParams4.setMargins(0, 0, dp(8), 0);
            button2.setLayoutParams(layoutParams4);
            editText.setImeOptions(3);
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public final boolean onEditorAction(TextView textView, int i2, KeyEvent keyEvent) {
                    return MainActivity.lambda$clients$19(button2, textView, i2, keyEvent);
                }
            });
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(this.business);
        if (this.tab.equals("clients")) {
            muted(this.content, "مرتبة حسب آخر تواصل؛ استخدم البحث للوصول السريع إلى ملف العميل.");
        }
        if (this.tab.equals("today")) {
            followUpDates();
            if (this.followDate.isEmpty()) {
                arrayList.add(LocalDate.now().toString());
                string = "business=? AND stage<4 AND (followUp='' OR followUp<=?)";
            } else {
                arrayList.add(this.followDate);
                string = "business=? AND stage<4 AND followUp=?";
            }
            str = "CASE WHEN followUp='' THEN 1 ELSE 0 END, followUp ASC, followTime ASC, name COLLATE NOCASE";
        } else {
            string = "business=?";
            str = "CASE WHEN lastCallAt=0 THEN 1 ELSE 0 END, lastCallAt DESC, name COLLATE NOCASE";
        }
        if (this.tab.equals("clients") && this.customerStageFilter > 0) {
            StringBuilder sbAppend = new StringBuilder().append(string);
            int i2 = this.customerStageFilter;
            string = sbAppend.append(i2 == 1 ? " AND stage<4" : i2 == 2 ? " AND stage=4" : " AND stage=5").toString();
        }
        if (this.tab.equals("unassigned")) {
            arrayList.clear();
            string = "business=''";
            str = "lastCallAt DESC";
        }
        if (this.business.equals("survey") && !this.tab.equals("unassigned") && this.requestFilter > 0) {
            string = string + " AND requestType=?";
            arrayList.add(SurveyRequests.KEYS[this.requestFilter - 1]);
        }
        if (!this.search.isEmpty() && this.tab.equals("clients")) {
            string = string + " AND (name LIKE ? OR phone LIKE ? OR company LIKE ?)";
            for (int i3 = 0; i3 < 3; i3++) {
                arrayList.add("%" + this.search + "%");
            }
        }
        final ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM leads WHERE " + string + " ORDER BY " + str + " LIMIT " + (this.limit + 1), (String[]) arrayList.toArray(new String[0]));
        if (arrayListQuery.isEmpty()) {
            empty(this.tab.equals("today") ? "متابعاتك تحت السيطرة" : "ابدأ بأول عميل", this.tab.equals("today") ? "لا توجد متابعات مستحقة في هذا النشاط اليوم" : "أضف عميلًا أو استورد أرقام المكالمات من الإعدادات");
        }
        if (this.tab.equals("unassigned") && !arrayListQuery.isEmpty()) {
            button(this.content, "تحديد المعروض بالكامل", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$clients$20(arrayListQuery);
                }
            });
        }
        for (JSONObject jSONObject2 : arrayListQuery) {
            int i4 = i + 1;
            if (i >= this.limit) {
                break;
            }
            leadCard(jSONObject2);
            i = i4;
        }
        if (arrayListQuery.size() > this.limit) {
            button(this.content, "عرض المزيد", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$clients$21();
                }
            });
        }
    }

    private LinearLayout column() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        return linearLayout;
    }

    private void contactDisclosure(final int i, final Runnable runnable) {
        new FluentDialog(this).setTitle("استخدام جهات الاتصال").setMessage("سيقرأ تواصل أسماء وأرقام جهات الاتصال من هاتفك لمطابقة أرقام العملاء بالأسماء محليًا. لا تُرسل جهات الاتصال إلى المطور أو إلى خادم، ويمكنك متابعة استخدام التطبيق دون هذا الإذن.").setNegativeButton(runnable == null ? "ليس الآن" : "المتابعة دون أسماء", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i2) {
                MainActivity.lambda$contactDisclosure$77(runnable, dialogInterface, i2);
            }
        }).setPositiveButton("متابعة", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i2) {
                MainActivity.this.lambda$contactDisclosure$78(i, dialogInterface, i2);
            }
        }).show();
    }

    private void customerFilters() {
        LinearLayout linearLayoutRow = row();
        this.content.addView(linearLayoutRow);
        String[] strArr = {tr("الكل", "All"), tr("نشط", "Active"), tr("مكتمل", "Done"), tr("مغلق", "Closed")};
        for (int index = 0; index < 4; index++) {
            final int i = index;
            Button button = button(linearLayoutRow, strArr[i], new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$customerFilters$22(i);
                }
            });
            UiTheme.button(button, i == this.customerStageFilter);
            button.setTextSize(12.0f);
            button.setPadding(dp(5), dp(8), dp(5), dp(8));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams.setMargins(dp(2), dp(4), dp(2), dp(4));
            button.setLayoutParams(layoutParams);
        }
    }

    private void designPost() {
        if (this.postDraft == null) {
            return;
        }
        LinearLayout linearLayoutForm = form();
        muted(linearLayoutForm, "قالب محلي 1080×1350. الصورة الحالية تُوضع داخل القالب دون إزالة الخلفية. راجع المعاينة ثم احفظ المنشور.");
        final EditText editTextField = field(linearLayoutForm, "عنوان الصورة", this.postDraft.optString("title"), 1);
        final EditText editTextField2 = field(linearLayoutForm, "نص قصير على الصورة (حتى 180 حرفًا)", "", 131073);
        String strOptString = this.postDraft.optString("business");
        final EditText editTextField3 = field(linearLayoutForm, "اسم النشاط", profileBrand(strOptString), 1);
        final EditText editTextField4 = field(linearLayoutForm, "التواصل أو الدعوة للإجراء", profileContact(strOptString), 1);
        final Spinner spinner = spinner(linearLayoutForm, "ألوان التصميم", new String[]{"بنفسجي فاتح", "أخضر فاتح", "برتقالي فاتح"}, strOptString.equals("survey") ? 2 : 0);
        new FluentDialog(this).setTitle("تصميم صورة للمنشور").setView(linearLayoutForm).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("إنشاء المعاينة", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$designPost$133(editTextField, editTextField2, editTextField3, editTextField4, spinner, dialogInterface, i);
            }
        }).show();
    }

    private int dp(int i) {
        return (int) (i * getResources().getDisplayMetrics().density);
    }

    private void edit(JSONObject jSONObject, final long j) {
        String str;
        try {
            final JSONObject jSONObject2 = new JSONObject(jSONObject.toString());
            if (!jSONObject2.has("id") && !jSONObject2.has("business")) {
                jSONObject2.put("business", "survey");
            }
            LinearLayout linearLayoutColumn = column();
            linearLayoutColumn.setLayoutDirection(AppLanguage.direction(this));
            linearLayoutColumn.setPadding(dp(18), dp(8), dp(18), dp(8));
            ScrollView scrollView = new ScrollView(this);
            scrollView.addView(linearLayoutColumn);
            if (j > 0) {
                linearLayoutColumn.addView(text("اقتراحات من النص — راجع الاسم والرقم والنشاط والموعد قبل الحفظ. التحليل محلي بالكلمات والصيغ، وقد لا يفهم السياق.", 16));
            }
            UiTheme.header(linearLayoutColumn, tr("ملف العميل", "Customer profile"), "clients");
            final EditText editTextField = field(linearLayoutColumn, "اسم العميل *", jSONObject2.optString("name"), 1);
            final EditText editTextField2 = field(linearLayoutColumn, "الرقم السعودي", jSONObject2.optString("phone"), 3);
            editTextField2.setTextDirection(3);
            final Spinner spinner = new Spinner(this);
            spinner.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"", "survey", "software"}));
            spinner.setSelection("survey".equals("survey") ? 1 : 2);
            spinner.setVisibility(8);
            linearLayoutColumn.addView(spinner, new LinearLayout.LayoutParams(1, 1));
            muted(linearLayoutColumn, "نوع الطلب التالي خاص بالمساحة. عند تغيير النوع تبدأ المرحلة من جديد؛ اختر المرحلة المناسبة قبل الحفظ.");
            final Spinner spinner2 = spinner(linearLayoutColumn, "نوع الطلب الحالي — نشاط المساحة", SurveyRequests.LABELS, SurveyRequests.index(jSONObject2.optString("requestType", "sales")));
            final Spinner spinner3 = spinner(linearLayoutColumn, "المرحلة", stages(jSONObject2.optString("business"), jSONObject2.optString("requestType", "sales")), jSONObject2.optInt("stage"));
            final String[] strArr = {jSONObject2.optString("business") + ":" + jSONObject2.optString("requestType", "sales")};
            final Runnable runnable = new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$edit$41(spinner, spinner2, strArr, spinner3);
                }
            };
            AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j2) {
                    runnable.run();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            };
            spinner.setOnItemSelectedListener(onItemSelectedListener);
            spinner2.setOnItemSelectedListener(onItemSelectedListener);
            final EditText editTextField3 = field(linearLayoutColumn, "الشركة", jSONObject2.optString("company"), 1);
            final EditText editTextField4 = field(linearLayoutColumn, "البريد الإلكتروني للعميل", jSONObject2.optString("email"), 33);
            final EditText editTextField5 = field(linearLayoutColumn, "الجهاز / البرنامج / الخدمة المطلوبة", jSONObject2.optString("interest"), 1);
            final EditText editTextField6 = field(linearLayoutColumn, "موعد المتابعة — اضغط لفتح التقويم", jSONObject2.optString("followUp"), 1);
            editTextField6.setTextDirection(3);
            editTextField6.setFocusable(false);
            editTextField6.setClickable(true);
            editTextField6.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(View view) {
                    MainActivity.this.lambda$edit$42(editTextField6, view);
                }
            });
            final EditText editTextField7 = field(linearLayoutColumn, "وقت التنبيه (HH:mm)", jSONObject2.optString("followTime", "09:00"), 1);
            editTextField7.setTextDirection(3);
            button(linearLayoutColumn, "اختيار الوقت", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$edit$44(editTextField7);
                }
            });
            button(linearLayoutColumn, "اختيار موعد من التقويم", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$edit$45(editTextField6);
                }
            });
            button(linearLayoutColumn, "إزالة الموعد", new Runnable() {
                @Override
                public final void run() {
                    editTextField6.setText("");
                }
            });
            final EditText editTextField8 = field(linearLayoutColumn, "ملاحظات", jSONObject2.optString("notes"), 131073);
            editTextField8.setMinLines(3);
            final CheckBox checkBox = new CheckBox(this);
            checkBox.setTypeface(this.cairo);
            checkBox.setText("فتح جهات اتصال الهاتف لحفظ العميل بعد حفظ بياناته");
            linearLayoutColumn.addView(checkBox);
            FluentDialog fluentDialog = new FluentDialog(this);
            if (j > 0) {
                str = "مراجعة وحفظ معلومات الرسالة";
            } else {
                str = jSONObject2.optLong("id") > 0 ? "تعديل العميل" : "عميل جديد";
            }
            final AlertDialog alertDialogCreate = fluentDialog.setTitle(str).setView(scrollView).setPositiveButton("حفظ", (DialogInterface.OnClickListener) null).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).create();
            if (jSONObject2.optLong("id") > 0) {
                tools(linearLayoutColumn, jSONObject2, alertDialogCreate);
            }
            if (jSONObject2.optLong("id") > 0) {
                button(linearLayoutColumn, "حذف العميل", new Runnable() {
                    @Override
                    public final void run() {
                        MainActivity.this.lambda$edit$48(jSONObject2, alertDialogCreate);
                    }
                });
            }
            alertDialogCreate.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public final void onShow(DialogInterface dialogInterface) {
                    MainActivity.this.lambda$edit$50(alertDialogCreate, jSONObject2, editTextField, editTextField2, spinner, spinner3, spinner2, editTextField3, editTextField4, editTextField5, editTextField6, editTextField8, editTextField7, j, checkBox, dialogInterface);
                }
            });
            alertDialogCreate.show();
            alertDialogCreate.getWindow().setBackgroundDrawable(shape(-1, 24, 0));
            alertDialogCreate.getWindow().setLayout(-1, -1);
            alertDialogCreate.getButton(-1).setTextColor(-15434365);
            alertDialogCreate.getButton(-2).setTextColor(-10192758);
        } catch (Exception unused) {
            alert("تعذر فتح بيانات العميل");
        }
    }

    public void lambda$advertising$114(JSONObject jSONObject) {
        String[] strArr;
        AlertDialog alertDialog = this.postDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            this.postDialog.dismiss();
        }
        try {
            JSONObject jSONObject2 = new JSONObject(jSONObject.toString());
            this.postDraft = jSONObject2;
            if (!jSONObject2.has("business")) {
                this.postDraft.put("business", this.business);
            }
            this.postInputs.clear();
            LinearLayout linearLayoutForm = form();
            linearLayoutForm.addView(text(this.postDraft.optString("business").equals("survey") ? "منشور أجهزة المساحة" : "منشور البرامج والتطبيقات", 22));
            muted(linearLayoutForm, "يُحفظ في مكتبة هذا النشاط فقط");
            button(linearLayoutForm, "إدراج بيانات منشأة هذا النشاط في النص", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$editPost$119();
                }
            });
            this.postInputs.put("title", field(linearLayoutForm, "عنوان داخلي للمنشور", this.postDraft.optString("title"), 1));
            EditText editTextField = field(linearLayoutForm, "النص المصاحب للصورة", this.postDraft.optString("body"), 131073);
            editTextField.setMinLines(5);
            this.postInputs.put("body", editTextField);
            int i = 0;
            int i2 = 0;
            while (true) {
                strArr = POST_CATEGORIES;
                if (i >= strArr.length) {
                    break;
                }
                if (strArr[i].equals(this.postDraft.optString("category"))) {
                    i2 = i;
                }
                i++;
            }
            this.postCategory = spinner(linearLayoutForm, "نوع المحتوى", strArr, i2);
            showPostImage(linearLayoutForm, this.postDraft.optString("image"));
            button(linearLayoutForm, "اختيار صورة من الهاتف", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$editPost$120();
                }
            });
            if (!this.postDraft.optString("image").isEmpty()) {
                button(linearLayoutForm, "إزالة الصورة من المنشور", new Runnable() {
                    @Override
                    public final void run() {
                        MainActivity.this.lambda$editPost$121();
                    }
                });
            }
            muted(linearLayoutForm, "صور JPG / PNG / WEBP / GIF حتى 3 ميغابايت. تُحفظ الصورة الأصلية دون تعديل.");
            button(linearLayoutForm, "تجهيز صورة بقالب جاهز", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$editPost$122();
                }
            });
            final EditText editTextField2 = field(linearLayoutForm, "يوم النشر المخطط (اختياري)", this.postDraft.optString("plannedDate"), 1);
            this.postInputs.put("plannedDate", editTextField2);
            button(linearLayoutForm, "اختيار يوم النشر", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$editPost$124(editTextField2);
                }
            });
            button(linearLayoutForm, "إزالة يوم النشر", new Runnable() {
                @Override
                public final void run() {
                    editTextField2.setText("");
                }
            });
            muted(linearLayoutForm, "هذا تنظيم للمحتوى فقط؛ لا توجد جدولة إرسال تلقائي إلى الجروبات.");
            primary(button(linearLayoutForm, "حفظ المنشور", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$editPost$126();
                }
            }));
            if (this.postDraft.optLong("id") > 0) {
                final long jOptLong = this.postDraft.optLong("id");
                button(linearLayoutForm, "حذف المنشور", new Runnable() {
                    @Override
                    public final void run() {
                        MainActivity.this.lambda$editPost$128(jOptLong);
                    }
                });
            }
            ScrollView scrollView = new ScrollView(this);
            scrollView.addView(linearLayoutForm);
            AlertDialog alertDialogCreate = new FluentDialog(this).setTitle("محرر الدعاية").setView(scrollView).setNegativeButton("إلغاء", new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialogInterface, int i3) {
                    MainActivity.this.lambda$editPost$129(dialogInterface, i3);
                }
            }).create();
            this.postDialog = alertDialogCreate;
            alertDialogCreate.setCancelable(false);
            this.postDialog.show();
            this.postDialog.getWindow().setBackgroundDrawable(shape(-1, 20, 0));
            this.postDialog.getWindow().setLayout(-1, -1);
        } catch (Exception unused) {
            alert("تعذر فتح المنشور");
        }
    }

    private void editProfile(final String str) {
        MainActivity mainActivity = this;
        final JSONObject jSONObjectProfile = mainActivity.store.profile(str);
        LinearLayout linearLayoutForm = mainActivity.form();
        linearLayoutForm.addView(mainActivity.text("بيانات المنشأة", 23));
        mainActivity.muted(linearLayoutForm, "حدّد نشاط منشأتك من القائمة. هذا إعداد للمنشأة وليس قسمًا ثابتًا للتطبيق أو للعميل.");
        String strOptString = jSONObjectProfile.optString("organizationActivityName");
        if (strOptString.isEmpty()) {
            strOptString = CustomerActivity.label(jSONObjectProfile.optString("organizationActivityType"));
        }
        String[] strArr = ORGANIZATION_ACTIVITIES;
        Spinner spinner = mainActivity.spinner(linearLayoutForm, "نشاط المنشأة", strArr, mainActivity.organizationActivityIndex(strOptString));
        int i = 1;
        final EditText editTextField = mainActivity.field(linearLayoutForm, "نشاط آخر (اكتبه عند عدم وجوده في القائمة)", jSONObjectProfile.optString("organizationActivityCustom"), 1);
        editTextField.setVisibility(spinner.getSelectedItemPosition() == strArr.length - 1 ? 0 : 8);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i2, long j) {
                editTextField.setVisibility(i2 == MainActivity.ORGANIZATION_ACTIVITIES.length + (-1) ? 0 : 8);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        final LinkedHashMap linkedHashMap = new LinkedHashMap();
        String[][] strArr2 = Profiles.FIELDS;
        int length = strArr2.length;
        int i2 = 0;
        while (i2 < length) {
            String[] strArr3 = strArr2[i2];
            EditText editTextField2 = mainActivity.field(linearLayoutForm, strArr3[i], jSONObjectProfile.optString(strArr3[0]), (strArr3[0].equals("address") || strArr3[0].equals("nationalAddress") || strArr3[0].equals("phones")) ? 131073 : strArr3[0].equals("email") ? 33 : i);
            linkedHashMap.put(strArr3[0], editTextField2);
            for (String link : Profiles.LINKS) {
                if (link.equals(strArr3[0])) {
                    editTextField2.setTextDirection(3);
                    editTextField2.setHint("https://...");
                }
            }
            i2++;
            i = 1;
        }
        final Spinner spinner3 = spinner;
        linearLayoutForm.addView(mainActivity.text("ما يُدرج في نصوص الدعاية", 20));
        mainActivity.muted(linearLayoutForm, "اختر البيانات التي تريد إضافتها تلقائيًا. السجل التجاري والرقم الضريبي يُحفظان في الملف ولا يُدرجان في الدعاية.");
        JSONObject jSONObjectOptJSONObject = jSONObjectProfile.optJSONObject("advertising");
        final LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        String[] strArr6 = Profiles.AD_FIELDS;
        int length3 = strArr6.length;
        int i4 = 0;
        while (i4 < length3) {
            String str2 = strArr6[i4];
            boolean zOptBoolean = str2.equals("address") || str2.equals("phones") || str2.equals("whatsapp") || str2.equals("website");
            String[] strArr7 = strArr6;
            CheckBox checkBox = new CheckBox(mainActivity);
            checkBox.setText(Profiles.label(str2));
            if (jSONObjectOptJSONObject != null) {
                zOptBoolean = jSONObjectOptJSONObject.optBoolean(str2, zOptBoolean);
            }
            checkBox.setChecked(zOptBoolean);
            linearLayoutForm.addView(checkBox);
            linkedHashMap2.put(str2, checkBox);
            i4++;
            mainActivity = this;
            strArr6 = strArr7;
        }
        final AlertDialog[] alertDialogArr = new AlertDialog[1];
        primary(button(linearLayoutForm, "حفظ بيانات هذه المنشأة", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$editProfile$134(spinner3, editTextField, jSONObjectProfile, linkedHashMap, linkedHashMap2, str, alertDialogArr);
            }
        }));
        button(linearLayoutForm, "نسخ بيانات المنشأة المحفوظة", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$editProfile$135(str);
            }
        });
        for (String str3 : Profiles.LINKS) {
            final String strOptString2 = jSONObjectProfile.optString(str3);
            if (!strOptString2.isEmpty()) {
                button(linearLayoutForm, "فتح " + Profiles.label(str3), new Runnable() {
                    @Override
                    public final void run() {
                        MainActivity.this.lambda$editProfile$136(strOptString2);
                    }
                });
            }
        }
        alertDialogArr[0] = panel("بيانات المنشأة", linearLayoutForm);
    }

    private void editTemplates() {
        LinearLayout linearLayoutForm = form();
        final EditText editTextField = field(linearLayoutForm, "اسم القالب", "", 1);
        final EditText editTextField2 = field(linearLayoutForm, "نص القالب — استخدم {الاسم} و{الشركة} و{رقم_العرض} و{المبلغ}", "", 131073);
        editTextField2.setMinLines(6);
        new FluentDialog(this).setTitle("إضافة قالب واتساب مخصص").setView(linearLayoutForm).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حفظ", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$editTemplates$110(editTextField, editTextField2, dialogInterface, i);
            }
        }).show();
    }

    private void empty(String str, String str2) {
        LinearLayout linearLayoutCard = card(this.content);
        linearLayoutCard.setGravity(17);
        linearLayoutCard.setPadding(dp(24), dp(35), dp(24), dp(35));
        linearLayoutCard.addView(new Glyph(this, "clients", -15434365), new LinearLayout.LayoutParams(dp(42), dp(42)));
        TextView textViewText = text(str, 19);
        textViewText.setGravity(17);
        linearLayoutCard.addView(textViewText);
        TextView textViewText2 = text(str2, 14);
        textViewText2.setTextColor(-10192758);
        textViewText2.setGravity(17);
        linearLayoutCard.addView(textViewText2);
    }

    public void enableNotifications() {
        getSharedPreferences("settings", 0).edit().putBoolean("reminders", true).apply();
        Reminders.channel(this);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 13);
        } else {
            if (!Reminders.allowed(this)) {
                startActivity(Reminders.settings(this));
                return;
            }
            Reminders.deliver(this);
            Reminders.schedule(this);
            render();
        }
    }

    private EditText field(LinearLayout linearLayout, String str, String str2, int i) {
        muted(linearLayout, str);
        EditText editText = new EditText(this);
        editText.setTextSize(15.0f);
        editText.setTypeface(this.cairo);
        editText.setTextColor(-15189940);
        editText.setInputType(i);
        if (str2.equals("null")) {
            str2 = "";
        }
        editText.setText(str2);
        editText.setPadding(dp(14), dp(10), dp(14), dp(10));
        editText.setBackground(shape(-788486, 12, -2299923));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, 0, 0, dp(14));
        editText.setMinHeight(dp(50));
        linearLayout.addView(editText, layoutParams);
        UiTheme.input(editText);
        return editText;
    }

    private void followAction(final long j, final boolean z) {
        new FluentDialog(this).setMessage(z ? "تأجيل موعد المتابعة المحفوظ إلى الغد بنفس الوقت؟" : "تسجيل إتمام المتابعة وإزالة الموعد الحالي؟").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("تأكيد", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$followAction$104(j, z, dialogInterface, i);
            }
        }).show();
    }

    private void followUpDates() {
        LinearLayout linearLayoutRow = row();
        this.content.addView(linearLayoutRow);
        LocalDate localDateNow = this.followDate.isEmpty() ? LocalDate.now() : LocalDate.parse(this.followDate);
        for (int i = 0; i < 5; i++) {
            final LocalDate localDatePlusDays = localDateNow.plusDays(i);
            Button button = button(linearLayoutRow, localDatePlusDays.format(DateTimeFormatter.ofPattern("EEE", AppLanguage.english(this) ? Locale.US : Locale.forLanguageTag("ar-u-nu-latn"))) + "\n" + localDatePlusDays.getDayOfMonth(), new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$followUpDates$23(localDatePlusDays);
                }
            });
            UiTheme.button(button, localDatePlusDays.toString().equals(this.followDate));
            button.setTextSize(12.0f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams.setMargins(dp(2), 0, dp(2), 0);
            button.setLayoutParams(layoutParams);
        }
        muted(this.content, this.followDate.isEmpty() ? tr("المستحق والمتأخر والعملاء دون موعد", "Due, overdue and unscheduled customers") : this.followDate);
        button(this.content, tr("اختيار تاريخ من التقويم", "Choose date from calendar"), new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$followUpDates$25();
            }
        });
        if (this.followDate.isEmpty()) {
            return;
        }
        button(this.content, tr("عرض المستحق والمتأخر", "Show due and overdue"), new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$followUpDates$26();
            }
        });
    }

    private LinearLayout form() {
        LinearLayout linearLayoutColumn = column();
        linearLayoutColumn.setLayoutDirection(AppLanguage.direction(this));
        linearLayoutColumn.setBackgroundColor(-788486);
        linearLayoutColumn.setPadding(dp(20), dp(12), dp(20), dp(20));
        return linearLayoutColumn;
    }

    private GradientDrawable glass(int i, int i2) {
        GradientDrawable gradientDrawableGradient = gradient(i, i2, 18);
        gradientDrawableGradient.setStroke(dp(1), -1971984);
        return gradientDrawableGradient;
    }

    private GradientDrawable gradient(int i, int i2, int i3) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{i, i2});
        gradientDrawable.setCornerRadius(dp(i3));
        return gradientDrawable;
    }

    private void heroMetric(LinearLayout linearLayout, String str, int i) {
        LinearLayout linearLayoutColumn = column();
        linearLayoutColumn.setGravity(17);
        linearLayout.addView(linearLayoutColumn, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textViewText = text(String.valueOf(i), 27);
        textViewText.setGravity(17);
        textViewText.setTextColor(-1);
        linearLayoutColumn.addView(textViewText);
        TextView textViewText2 = text(str, 11);
        textViewText2.setGravity(17);
        textViewText2.setTextColor(-2955027);
        linearLayoutColumn.addView(textViewText2);
    }

    private void home() {
        JSONObject jSONObject = this.store.query("SELECT COUNT(*) AS customers, SUM(CASE WHEN stage<4 THEN 1 ELSE 0 END) AS active, SUM(CASE WHEN stage<4 AND followUp<>'' AND followUp<=? THEN 1 ELSE 0 END) AS due FROM leads WHERE business=?", LocalDate.now().toString(), this.business).get(0);
        LinearLayout linearLayoutColumn = column();
        linearLayoutColumn.setPadding(dp(18), dp(12), dp(18), dp(14));
        linearLayoutColumn.setBackground(UiTheme.surface(this, true, 22));
        linearLayoutColumn.setElevation(dp(3));
        this.content.addView(linearLayoutColumn, new LinearLayout.LayoutParams(-1, -2));
        TextView textViewText = text(tr("أعمالك اليوم", "Your work today"), 19);
        textViewText.setTextColor(-1);
        linearLayoutColumn.addView(textViewText);
        LinearLayout linearLayoutRow = row();
        linearLayoutColumn.addView(linearLayoutRow);
        heroMetric(linearLayoutRow, tr("العملاء", "Customers"), jSONObject.optInt("customers"));
        heroMetric(linearLayoutRow, tr("فرص نشطة", "Active"), jSONObject.optInt("active"));
        heroMetric(linearLayoutRow, tr("متابعات اليوم", "Due today"), jSONObject.optInt("due"));
        TextView textViewText2 = text(tr("الوصول السريع", "Quick access"), 19);
        textViewText2.setPadding(0, dp(12), 0, dp(4));
        this.content.addView(textViewText2);
        LinearLayout linearLayoutRow2 = row();
        this.content.addView(linearLayoutRow2);
        homeAction(linearLayoutRow2, tr("العملاء", "Customers"), "clients", 0, 0, -15434365, new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$home$4();
            }
        });
        homeAction(linearLayoutRow2, tr("المتابعات", "Follow-ups"), "today", 0, 0, -15434365, new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$home$5();
            }
        });
        LinearLayout linearLayoutRow3 = row();
        this.content.addView(linearLayoutRow3);
        homeAction(linearLayoutRow3, tr("الدفاتر", "Ledgers"), "ledger", 0, 0, -15434365, new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$home$6();
            }
        });
        homeAction(linearLayoutRow3, tr("المنشورات", "Posts"), "ads", 0, 0, -15434365, new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$home$7();
            }
        });
        LinearLayout linearLayoutRow4 = row();
        this.content.addView(linearLayoutRow4);
        homeAction(linearLayoutRow4, tr("الحاسبة", "Calculator"), "calculator", 0, 0, -15434365, new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$home$8();
            }
        });
        homeAction(linearLayoutRow4, tr("التقارير", "Reports"), "reports", 0, 0, -15434365, new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$home$9();
            }
        });
        ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM leads WHERE business=? AND stage<4 AND followUp<>'' ORDER BY followUp,followTime LIMIT 1", this.business);
        if (arrayListQuery.isEmpty()) {
            return;
        }
        this.content.addView(text(tr("أقرب متابعة", "Next follow-up"), 17));
        homeFollowUp(arrayListQuery.get(0));
    }

    private void homeAction(LinearLayout linearLayout, String str, String str2, int i, int i2, int i3, final Runnable runnable) {
        LinearLayout linearLayoutColumn = column();
        linearLayoutColumn.setGravity(17);
        linearLayoutColumn.setPadding(dp(8), dp(8), dp(8), dp(8));
        UiTheme.sectionCard(linearLayoutColumn, str2);
        linearLayoutColumn.setClickable(true);
        linearLayoutColumn.setForeground(new RippleDrawable(ColorStateList.valueOf(403996035), null, null));
        UiTheme.motion(linearLayoutColumn);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams.setMargins(dp(3), dp(4), dp(3), dp(4));
        linearLayoutColumn.setMinimumHeight(dp(88));
        linearLayout.addView(linearLayoutColumn, layoutParams);
        linearLayoutColumn.addView(new GlassIcon(this, str2), new LinearLayout.LayoutParams(dp(40), dp(40)));
        TextView textViewText = text(str, 14);
        textViewText.setTypeface(this.cairo, 1);
        textViewText.setTextColor(-15717821);
        textViewText.setGravity(17);
        textViewText.setSingleLine(true);
        textViewText.setIncludeFontPadding(false);
        textViewText.setAutoSizeTextTypeUniformWithConfiguration(12, 14, 1, 2);
        textViewText.setPadding(0, dp(4), 0, 0);
        linearLayoutColumn.addView(textViewText, new LinearLayout.LayoutParams(-1, -2));
        linearLayoutColumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                MainActivity.lambda$homeAction$10(runnable, view);
            }
        });
    }

    private void homeFollowUp(final JSONObject jSONObject) {
        LinearLayout linearLayoutCard = card(this.content);
        linearLayoutCard.setOrientation(0);
        linearLayoutCard.setGravity(16);
        linearLayoutCard.setPadding(dp(14), dp(10), dp(14), dp(10));
        linearLayoutCard.addView(new Glyph(this, "today", -15434365), new LinearLayout.LayoutParams(dp(23), dp(23)));
        LinearLayout linearLayoutColumn = column();
        linearLayoutColumn.setPadding(dp(10), 0, dp(10), 0);
        linearLayoutCard.addView(linearLayoutColumn, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayoutColumn.addView(text(jSONObject.optString("name").isEmpty() ? "عميل غير مسمى" : jSONObject.optString("name"), 16));
        muted(linearLayoutColumn, jSONObject.optString("interest").isEmpty() ? "متابعة عميل" : jSONObject.optString("interest"));
        TextView textViewText = text(jSONObject.optString("followTime", "09:00"), 14);
        textViewText.setTextColor(-10192758);
        linearLayoutCard.addView(textViewText);
        linearLayoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                MainActivity.this.lambda$homeFollowUp$12(jSONObject, view);
            }
        });
    }

    private void homeSummary(LinearLayout linearLayout, String str, int i, String str2, int i2) {
        LinearLayout linearLayoutRow = row();
        linearLayoutRow.setPadding(0, dp(8), 0, dp(8));
        linearLayout.addView(linearLayoutRow, new LinearLayout.LayoutParams(-1, -2));
        linearLayoutRow.addView(new Glyph(this, str2, i2), new LinearLayout.LayoutParams(dp(24), dp(24)));
        TextView textViewText = text(str, 15);
        textViewText.setPadding(dp(12), 0, 0, 0);
        linearLayoutRow.addView(textViewText, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textViewText2 = text(String.valueOf(i), 20);
        textViewText2.setTextColor(i2);
        linearLayoutRow.addView(textViewText2);
    }

    private void homeWideAction(String str, String str2, int i, int i2, int i3, final Runnable runnable) {
        LinearLayout linearLayoutRow = row();
        linearLayoutRow.setGravity(16);
        linearLayoutRow.setPadding(dp(14), dp(7), dp(14), dp(7));
        linearLayoutRow.setBackground(glass(-1, -525828));
        linearLayoutRow.setElevation(dp(3));
        linearLayoutRow.setClickable(true);
        linearLayoutRow.setForeground(new RippleDrawable(ColorStateList.valueOf(303003246), null, null));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(66));
        layoutParams.setMargins(0, dp(4), 0, 0);
        this.content.addView(linearLayoutRow, layoutParams);
        LinearLayout linearLayoutColumn = column();
        linearLayoutColumn.setGravity(17);
        linearLayoutColumn.setBackground(shape(i3, 14, 0));
        linearLayoutColumn.addView(new Glyph(this, str2, -1), new LinearLayout.LayoutParams(dp(26), dp(26)));
        linearLayoutRow.addView(linearLayoutColumn, new LinearLayout.LayoutParams(dp(44), dp(44)));
        TextView textViewText = text(str, 15);
        textViewText.setTextColor(-15717821);
        textViewText.setSingleLine(true);
        textViewText.setPadding(dp(12), 0, dp(6), 0);
        linearLayoutRow.addView(textViewText, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textViewText2 = text("‹", 26);
        textViewText2.setTextColor(i3);
        linearLayoutRow.addView(textViewText2);
        linearLayoutRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                runnable.run();
            }
        });
    }

    private void importCallCsv(final Uri uri) {
        if (this.importing) {
            return;
        }
        this.importing = true;
        History.status(this, "جارٍ فحص ملف سجل المكالمات…");
        this.backgroundOps++;
        new Thread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$importCallCsv$82(uri);
            }
        }, "call-csv-import").start();
    }

    private void importContactVcf(final Uri uri) {
        this.backgroundOps++;
        new Thread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$importContactVcf$84(uri);
            }
        }, "contact-vcf-import").start();
    }

    private void importHistory() {
        getSharedPreferences("settings", 0).edit().putLong("historyId", 0L).apply();
        syncHistory(true);
    }

    public /* synthetic */ void lambda$adCopy$130(AlertDialog[] alertDialogArr, String str, String str2, String[] strArr, int i, String[] strArr2) {
        try {
            alertDialogArr[0].dismiss();
            lambda$advertising$114(new JSONObject().put("business", str).put("title", str2 + " — " + strArr[i]).put("body", strArr2[i]).put("category", i == 2 ? "معلومة" : "تعريف"));
        } catch (Exception unused) {
            alert("تعذر تجهيز المنشور");
        }
    }

    public /* synthetic */ void lambda$adCopy$131(String[] strArr, int i) {
        ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("نص الدعاية", strArr[i]));
    }

    public /* synthetic */ void lambda$adCopy$132(EditText editText, EditText editText2, EditText editText3, EditText editText4, final String str, DialogInterface dialogInterface, int i) {
        final String strTrim = editText.getText().toString().trim();
        String strTrim2 = editText2.getText().toString().trim();
        if (strTrim.isEmpty() || strTrim2.isEmpty()) {
            alert("أدخل المنتج أو الميزة والفائدة أولًا");
            return;
        }
        String strTrim3 = editText3.getText().toString().trim();
        String strTrim4 = editText4.getText().toString().trim();
        String str2 = (strTrim3.isEmpty() ? "" : "\n\n" + strTrim3) + "\nراسلنا لمعرفة التفاصيل" + (strTrim4.isEmpty() ? "" : "\n" + strTrim4);
        final String[] strArr = {"تعريف مباشر", "سؤال يجذب الاهتمام", "منشور معلوماتي"};
        final String[] strArr2 = {strTrim + "\n\n" + strTrim2 + str2, "هل تبحث عن " + strTrim + "؟\n\n" + strTrim2 + str2, "قبل اختيار " + strTrim + "، تعرّف على ما يفيد احتياجك:\n\n" + strTrim2 + str2};
        LinearLayout linearLayoutForm = form();
        final AlertDialog[] alertDialogArr = new AlertDialog[1];
        for (int index = 0; index < 3; index++) {
            final int i2 = index;
            LinearLayout linearLayoutCard = this.card(linearLayoutForm);
            linearLayoutCard.addView(this.text(strArr[i2], 19));
            TextView textViewText = this.text(strArr2[i2], 15);
            textViewText.setTextIsSelectable(true);
            linearLayoutCard.addView(textViewText);
            final MainActivity mainActivity = this;
            mainActivity.button(linearLayoutCard, "استخدام هذه الصيغة", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$adCopy$130(alertDialogArr, str, strTrim, strArr, i2, strArr2);
                }
            });
            mainActivity.button(linearLayoutCard, "نسخ النص", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$adCopy$131(strArr2, i2);
                }
            });
        }
        alertDialogArr[0] = this.panel("اختر الصيغة وعدّلها", linearLayoutForm);
    }

    public /* synthetic */ void lambda$advertising$111() {
        editProfile(this.business);
    }

    public /* synthetic */ void lambda$advertising$112() {
        try {
            lambda$advertising$114(new JSONObject().put("business", this.business));
        } catch (Exception unused) {
            alert("تعذر فتح المحرر");
        }
    }

    public /* synthetic */ void lambda$advertising$115(JSONObject jSONObject) {
        try {
            JSONObject jSONObject2 = new JSONObject(jSONObject.toString());
            jSONObject2.remove("id");
            jSONObject2.put("title", jSONObject.optString("title") + " — نسخة").put("plannedDate", "");
            this.store.savePost(jSONObject2);
            render();
        } catch (Exception unused) {
            alert("تعذر إنشاء نسخة");
        }
    }

    public /* synthetic */ void lambda$advertising$116(JSONObject jSONObject) {
        StringBuilder sb = new StringBuilder();
        Iterator<JSONObject> it = this.store.query("SELECT at FROM postShares WHERE postId=? ORDER BY at DESC LIMIT 50", String.valueOf(jSONObject.optLong("id"))).iterator();
        while (it.hasNext()) {
            sb.append(DateFormat.format("yyyy-MM-dd HH:mm", it.next().optLong("at"))).append("\n");
        }
        alert(sb.length() == 0 ? "لم يُفتح للمشاركة بعد" : sb.toString());
    }

    public /* synthetic */ void lambda$advertising$117() {
        this.adLimit -= 20;
        render();
    }

    public /* synthetic */ void lambda$advertising$118() {
        this.adLimit += 20;
        render();
    }

    public /* synthetic */ void lambda$askRestorePassword$98(String str, DialogInterface dialogInterface, int i) {
        restoreDocument(str, null);
    }

    public /* synthetic */ void lambda$askRestorePassword$99(String str, EditText editText, DialogInterface dialogInterface, int i) {
        restoreDocument(str, editText.getText().toString().toCharArray());
    }

    static /* synthetic */ void lambda$button$0(Runnable runnable, View view) {
        UiTheme.feedback(view);
        runnable.run();
    }

    public /* synthetic */ void lambda$calculatorCard$27(View view) {
        startActivity(new Intent(this, (Class<?>) CalculatorActivity.class));
    }

    public /* synthetic */ void lambda$callSetup$87(Spinner spinner, DialogInterface dialogInterface, int i) {
        String str;
        SharedPreferences.Editor editorPutBoolean = getSharedPreferences("settings", 0).edit().putBoolean("calls", true).putBoolean("autoSync", true);
        if (spinner.getSelectedItemPosition() == 1) {
            str = "survey";
        } else {
            str = spinner.getSelectedItemPosition() == 2 ? "software" : "";
        }
        editorPutBoolean.putString("newBusiness", str).apply();
        RoleManager roleManager = (RoleManager) getSystemService(RoleManager.class);
        if (!roleManager.isRoleAvailable("android.app.role.CALL_SCREENING") || roleManager.isRoleHeld("android.app.role.CALL_SCREENING")) {
            requestCallPermissions();
        } else {
            getSharedPreferences("settings", 0).edit().putBoolean("setupPending", true).apply();
            startActivityForResult(roleManager.createRequestRoleIntent("android.app.role.CALL_SCREENING"), 11);
        }
    }

    public /* synthetic */ void lambda$chooseBackupPassword$88(EditText editText, EditText editText2, DialogInterface dialogInterface, int i) {
        String string = editText.getText().toString();
        String string2 = editText2.getText().toString();
        if (string.length() < 8 || !string.equals(string2)) {
            alert("كلمتا المرور غير متطابقتين أو أقصر من 8 أحرف");
        } else {
            this.pendingBackupPassword = string.toCharArray();
            startActivityForResult(new Intent("android.intent.action.CREATE_DOCUMENT").setType("application/json").putExtra("android.intent.extra.TITLE", "engineering-crm-backup.ecrm").addCategory("android.intent.category.OPENABLE"), 20);
        }
    }

    public /* synthetic */ void lambda$chooseCsvExport$89(DialogInterface dialogInterface, int i) {
        chooseCsvKind();
    }

    public /* synthetic */ void lambda$chooseCsvKind$90(DialogInterface dialogInterface, int i) {
        String str;
        String str2;
        if (i == 0) {
            str = "customers";
        } else {
            str = i == 1 ? "quotes" : "ledger";
        }
        this.pendingCsv = str;
        if (str.equals("customers")) {
            str2 = "customers.csv";
        } else {
            str2 = this.pendingCsv.equals("quotes") ? "quotes.csv" : "ledger.csv";
        }
        startActivityForResult(new Intent("android.intent.action.CREATE_DOCUMENT").setType("text/csv").addCategory("android.intent.category.OPENABLE").putExtra("android.intent.extra.TITLE", str2), 41);
    }

    public /* synthetic */ void lambda$chooseLanguage$76(boolean z, DialogInterface dialogInterface, int i) {
        boolean z2 = i == 1;
        if (z2 == z) {
            dialogInterface.dismiss();
            return;
        }
        AppLanguage.set(this, z2);
        dialogInterface.dismiss();
        Intent intent = new Intent(this, (Class<?>) MainActivity.class);
        intent.addFlags(268468224);
        startActivity(intent);
        finish();
    }

    public /* synthetic */ void lambda$chooseQuick$57(JSONObject jSONObject, Spinner spinner, ArrayList arrayList, Analysis analysis, String str, DialogInterface dialogInterface, int i) {
        quickPanel(jSONObject, spinner.getSelectedItemPosition() == 0 ? new JSONObject() : (JSONObject) arrayList.get(spinner.getSelectedItemPosition() - 1), analysis, str);
    }

    public /* synthetic */ void lambda$clients$13() {
        startActivity(new Intent(this, (Class<?>) SoftwareActivity.class));
    }

    public /* synthetic */ void lambda$clients$14() {
        startActivity(new Intent(this, (Class<?>) QuoteActivity.class));
    }

    public /* synthetic */ void lambda$clients$15() {
        edit(new JSONObject(), 0L);
    }

    public /* synthetic */ void lambda$clients$16(DialogInterface dialogInterface, int i) {
        int iRemoveUnassigned = this.store.removeUnassigned();
        this.selectedCalls.clear();
        render();
        alert("تم حذف " + iRemoveUnassigned + " رقمًا غير معتمد");
    }

    public /* synthetic */ void lambda$clients$17() {
        new FluentDialog(this).setTitle("حذف الأرقام للمراجعة").setMessage("سيُحذف فقط ما لم تعتمدْه كعميل. لا يمكن التراجع إلا من نسخة احتياطية.").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حذف", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$clients$16(dialogInterface, i);
            }
        }).show();
    }

    public /* synthetic */ void lambda$clients$18(EditText editText) {
        this.search = editText.getText().toString().trim();
        this.limit = 100;
        render();
    }

    static /* synthetic */ boolean lambda$clients$19(Button button, TextView textView, int i, KeyEvent keyEvent) {
        if (i != 3) {
            return false;
        }
        button.performClick();
        return true;
    }

    public /* synthetic */ void lambda$clients$20(ArrayList arrayList) {
        for (int i = 0; i < Math.min(this.limit, arrayList.size()); i++) {
            this.selectedCalls.add(Long.valueOf(((JSONObject) arrayList.get(i)).optLong("id")));
        }
        render();
    }

    public /* synthetic */ void lambda$clients$21() {
        this.limit += 100;
        render();
    }

    static /* synthetic */ void lambda$contactDisclosure$77(Runnable runnable, DialogInterface dialogInterface, int i) {
        if (runnable != null) {
            runnable.run();
        }
    }

    public /* synthetic */ void lambda$contactDisclosure$78(int i, DialogInterface dialogInterface, int i2) {
        requestPermissions(new String[]{"android.permission.READ_CONTACTS"}, i);
    }

    public /* synthetic */ void lambda$customerFilters$22(int i) {
        this.customerStageFilter = i;
        this.limit = 100;
        render();
    }

    public /* synthetic */ void lambda$designPost$133(EditText editText, EditText editText2, EditText editText3, EditText editText4, Spinner spinner, DialogInterface dialogInterface, int i) {
        final MainActivity mainActivity = this;
        String strTrim = editText.getText().toString().trim();
        String strTrim2 = editText2.getText().toString().trim();
        String strTrim3 = editText3.getText().toString().trim();
        String strTrim4 = editText4.getText().toString().trim();
        if (strTrim.isEmpty() || strTrim.length() > 80 || strTrim2.length() > 180 || strTrim3.length() > 60 || strTrim4.length() > 60) {
            alert("اختصر العنوان إلى 80 حرفًا والنص إلى 180 واسم النشاط والتواصل إلى 60 حرفًا");
            return;
        }
        try {
                mainActivity.postDraft.put("image", PostDesigns.create(mainActivity, this.postDraft.optString("image"), strTrim, strTrim2, strTrim3, strTrim4, spinner.getSelectedItemPosition()));
                mainActivity.lambda$advertising$114(mainActivity.postDraft);
        } catch (Exception e) {
                mainActivity.alert(e.getMessage() == null ? "تعذر إنشاء التصميم" : e.getMessage());
        }
    }

    public /* synthetic */ void lambda$edit$41(Spinner spinner, Spinner spinner2, String[] strArr, Spinner spinner3) {
        String str;
        boolean z = spinner.getSelectedItemPosition() == 1;
        spinner2.setEnabled(z);
        if (spinner.getSelectedItemPosition() == 2) {
            str = "software";
        } else {
            str = z ? "survey" : "";
        }
        String str2 = z ? SurveyRequests.KEYS[spinner2.getSelectedItemPosition()] : "sales";
        String str3 = str + ":" + str2;
        if (str3.equals(strArr[0])) {
            return;
        }
        spinner3.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, stages(str, str2)));
        spinner3.setSelection(0);
        strArr[0] = str3;
    }

    public /* synthetic */ void lambda$edit$42(EditText editText, View view) {
        lambda$edit$45(editText);
    }

    public /* synthetic */ void lambda$edit$44(final EditText editText) {
        LocalTime localTimeOf;
        try {
            localTimeOf = LocalTime.parse(editText.getText());
        } catch (Exception unused) {
            localTimeOf = LocalTime.of(9, 0);
        }
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public final void onTimeSet(TimePicker timePicker, int i, int i2) {
                editText.setText(String.format(Locale.US, "%02d:%02d", Integer.valueOf(i), Integer.valueOf(i2)));
            }
        }, localTimeOf.getHour(), localTimeOf.getMinute(), true).show();
    }

    public /* synthetic */ void lambda$edit$47(JSONObject jSONObject, AlertDialog alertDialog, DialogInterface dialogInterface, int i) {
        this.store.remove(jSONObject.optLong("id"));
        alertDialog.dismiss();
        render();
        Reminders.schedule(this);
    }

    public /* synthetic */ void lambda$edit$48(final JSONObject jSONObject, final AlertDialog alertDialog) {
        new FluentDialog(this).setMessage("حذف العميل نهائيًا؟ ستبقى الرسائل الأصلية في أرشيف الرسائل.").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حذف", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$edit$47(jSONObject, alertDialog, dialogInterface, i);
            }
        }).show();
    }

    public /* synthetic */ void lambda$edit$49(JSONObject jSONObject, EditText editText, EditText editText2, Spinner spinner, Spinner spinner2, Spinner spinner3, EditText editText3, EditText editText4, EditText editText5, EditText editText6, EditText editText7, EditText editText8, long j, AlertDialog alertDialog, CheckBox checkBox, View view) {
        String message;
        String str;
        try {
            JSONObject jSONObjectPut = jSONObject.put("name", editText.getText().toString()).put("phone", editText2.getText().toString());
            if (spinner.getSelectedItemPosition() == 1) {
                str = "survey";
            } else {
                str = spinner.getSelectedItemPosition() == 2 ? "software" : "";
            }
            jSONObjectPut.put("business", str).put("stage", spinner2.getSelectedItemPosition()).put("requestType", spinner.getSelectedItemPosition() == 1 ? SurveyRequests.KEYS[spinner3.getSelectedItemPosition()] : "sales").put("company", editText3.getText().toString()).put("email", editText4.getText().toString()).put("interest", editText5.getText().toString()).put("followUp", editText6.getText().toString()).put("notes", editText7.getText().toString()).put("followTime", editText8.getText().toString());
            SQLiteDatabase writableDatabase = this.store.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                long jSave = this.store.save(jSONObject);
                if (j > 0) {
                    this.store.reviewed(j, jSave);
                }
                ((NotificationManager) getSystemService(NotificationManager.class)).cancel("lead", (int) jSave);
                writableDatabase.setTransactionSuccessful();
                writableDatabase.endTransaction();
                alertDialog.dismiss();
                render();
                Reminders.schedule(this);
                if (checkBox.isChecked()) {
                    lambda$tools$33(jSONObject);
                }
            } catch (Throwable th) {
                writableDatabase.endTransaction();
                throw th;
            }
        } catch (SQLiteConstraintException unused) {
            message = "هذا الرقم موجود بالفعل. اربط الرسالة بالعميل الموجود أو عدّل سجله.";
            alert(message);
        } catch (Exception e) {
            message = e.getMessage() == null ? "تعذر الحفظ" : e.getMessage();
            alert(message);
        }
    }

    public /* synthetic */ void lambda$edit$50(final AlertDialog alertDialog, final JSONObject jSONObject, final EditText editText, final EditText editText2, final Spinner spinner, final Spinner spinner2, final Spinner spinner3, final EditText editText3, final EditText editText4, final EditText editText5, final EditText editText6, final EditText editText7, final EditText editText8, final long j, final CheckBox checkBox, DialogInterface dialogInterface) {
        alertDialog.getButton(-1).setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                MainActivity.this.lambda$edit$49(jSONObject, editText, editText2, spinner, spinner2, spinner3, editText3, editText4, editText5, editText6, editText7, editText8, j, alertDialog, checkBox, view);
            }
        });
    }

    public /* synthetic */ void lambda$editPost$119() {
        EditText editText = this.postInputs.get("body");
        if (editText == null) {
            return;
        }
        String strProfileFooter = profileFooter(this.postDraft.optString("business"), true);
        if (strProfileFooter.isEmpty()) {
            alert("أضف بيانات المنشأة من الإعدادات أولًا");
        } else {
            if (editText.getText().toString().contains(strProfileFooter)) {
                return;
            }
            editText.append("\n\n" + strProfileFooter);
        }
    }

    public /* synthetic */ void lambda$editPost$120() {
        capturePost();
        this.postDialog.dismiss();
        startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT").setType("image/*").addCategory("android.intent.category.OPENABLE"), 30);
    }

    public /* synthetic */ void lambda$editPost$121() {
        capturePost();
        try {
            String strOptString = this.postDraft.optString("image");
            this.postDraft.put("image", "");
            this.store.removeUnusedImage(strOptString);
            lambda$advertising$114(this.postDraft);
        } catch (Exception unused) {
        }
    }

    public /* synthetic */ void lambda$editPost$122() {
        capturePost();
        designPost();
    }

    public /* synthetic */ void lambda$editPost$124(final EditText editText) {
        LocalDate localDateNow = LocalDate.now();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public final void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                editText.setText(LocalDate.of(i, i2 + 1, i3).toString());
            }
        }, localDateNow.getYear(), localDateNow.getMonthValue() - 1, localDateNow.getDayOfMonth()).show();
    }

    public /* synthetic */ void lambda$editPost$126() {
        capturePost();
        try {
            this.store.savePost(this.postDraft);
            this.postDialog.dismiss();
            this.postDraft = null;
            this.postInputs.clear();
            this.tab = "ads";
            render();
        } catch (Exception e) {
            alert(e.getMessage() == null ? "تعذر حفظ المنشور" : e.getMessage());
        }
    }

    public /* synthetic */ void lambda$editPost$127(long j, DialogInterface dialogInterface, int i) {
        this.store.removePost(j);
        this.postDialog.dismiss();
        this.postDraft = null;
        this.postInputs.clear();
        render();
    }

    public /* synthetic */ void lambda$editPost$128(final long j) {
        new FluentDialog(this).setMessage("حذف هذا المنشور وسجل فتح مشاركته؟").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حذف", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$editPost$127(j, dialogInterface, i);
            }
        }).show();
    }

    public /* synthetic */ void lambda$editPost$129(DialogInterface dialogInterface, int i) {
        this.postDraft = null;
        this.postInputs.clear();
    }

    public /* synthetic */ void lambda$editProfile$134(Spinner spinner, EditText editText, JSONObject jSONObject, Map map, Map map2, String str, AlertDialog[] alertDialogArr) {
        try {
            int selectedItemPosition = spinner.getSelectedItemPosition();
            String[] strArr = ORGANIZATION_ACTIVITIES;
            boolean z = true;
            if (selectedItemPosition != strArr.length - 1) {
                z = false;
            }
            String strTrim = z ? editText.getText().toString().trim() : strArr[spinner.getSelectedItemPosition()];
            if (strTrim.isEmpty()) {
                throw new IllegalArgumentException("اكتب نشاط المنشأة أولًا");
            }
            JSONObject jSONObjectPut = jSONObject.put("organizationActivityType", z ? "other" : "").put("organizationActivityName", strTrim);
            if (!z) {
                strTrim = "";
            }
            jSONObjectPut.put("organizationActivityCustom", strTrim);
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) map).entrySet()) {
                jSONObject.put((String) entry.getKey(), ((EditText) entry.getValue()).getText().toString().trim());
            }
            JSONObject jSONObject2 = new JSONObject();
            for (Map.Entry<?, ?> entry2 : ((Map<?, ?>) map2).entrySet()) {
                jSONObject2.put((String) entry2.getKey(), ((CheckBox) entry2.getValue()).isChecked());
            }
            jSONObject.put("advertising", jSONObject2);
            this.store.saveProfile(str, jSONObject);
            alertDialogArr[0].dismiss();
            render();
            alert("حُفظت بيانات المنشأة ونوع نشاطها");
        } catch (Exception e) {
            alert(e.getMessage() == null ? "تحقق من البيانات والروابط" : e.getMessage());
        }
    }

    public /* synthetic */ void lambda$editProfile$135(String str) {
        StringBuilder sb = new StringBuilder();
        JSONObject jSONObjectProfile = this.store.profile(str);
        for (String[] strArr : Profiles.FIELDS) {
            String strOptString = jSONObjectProfile.optString(strArr[0]);
            if (!strOptString.isEmpty()) {
                sb.append(strArr[1]).append(": ").append(strOptString).append("\n");
            }
        }
        ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("بيانات المنشأة", sb.toString()));
    }

    public /* synthetic */ void lambda$editProfile$136(String str) {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse(Profiles.url(str))));
        } catch (Exception unused) {
            alert("تعذر فتح الرابط");
        }
    }

    public /* synthetic */ void lambda$editTemplates$110(EditText editText, EditText editText2, DialogInterface dialogInterface, int i) {
        try {
            if (editText.getText().toString().trim().isEmpty() || editText2.getText().toString().trim().isEmpty()) {
                throw new IllegalArgumentException("أدخل الاسم والنص");
            }
            JSONObject jSONObjectProfile = this.store.profile("survey");
            JSONArray jSONArrayCustom = TemplateStore.custom(jSONObjectProfile);
            jSONArrayCustom.put(new JSONObject().put("title", editText.getText().toString().trim()).put("body", editText2.getText().toString().trim()));
            jSONObjectProfile.put("whatsappTemplates", jSONArrayCustom);
            this.store.saveProfile("survey", jSONObjectProfile);
            alert("حُفظ القالب المخصص");
        } catch (Exception e) {
            alert(e.getMessage() == null ? "تعذر حفظ القالب" : e.getMessage());
        }
    }

    public /* synthetic */ void lambda$followAction$104(long j, boolean z, DialogInterface dialogInterface, int i) {
        try {
            this.store.finishFollowUp(j, z);
            ((NotificationManager) getSystemService(NotificationManager.class)).cancel("lead", (int) j);
            Reminders.schedule(this);
            render();
            alert(z ? "تم تأجيل المتابعة. أعد فتح العميل لرؤية الموعد الجديد." : "تم إتمام المتابعة. أعد فتح العميل لرؤية التحديث.");
        } catch (Exception unused) {
            alert("تعذر تحديث الموعد");
        }
    }

    public /* synthetic */ void lambda$followUpDates$23(LocalDate localDate) {
        this.followDate = localDate.toString();
        render();
    }

    public /* synthetic */ void lambda$followUpDates$24(DatePicker datePicker, int i, int i2, int i3) {
        this.followDate = LocalDate.of(i, i2 + 1, i3).toString();
        render();
    }

    public /* synthetic */ void lambda$followUpDates$25() {
        LocalDate localDateNow = this.followDate.isEmpty() ? LocalDate.now() : LocalDate.parse(this.followDate);
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public final void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                MainActivity.this.lambda$followUpDates$24(datePicker, i, i2, i3);
            }
        }, localDateNow.getYear(), localDateNow.getMonthValue() - 1, localDateNow.getDayOfMonth()).show();
    }

    public /* synthetic */ void lambda$followUpDates$26() {
        this.followDate = "";
        render();
    }

    public /* synthetic */ void lambda$home$4() {
        this.tab = "clients";
        render();
    }

    public /* synthetic */ void lambda$home$5() {
        this.tab = "today";
        render();
    }

    public /* synthetic */ void lambda$home$6() {
        startActivity(new Intent(this, (Class<?>) LedgerActivity.class));
    }

    public /* synthetic */ void lambda$home$7() {
        this.tab = "ads";
        render();
    }

    public /* synthetic */ void lambda$home$8() {
        startActivity(new Intent(this, (Class<?>) CalculatorActivity.class));
    }

    public /* synthetic */ void lambda$home$9() {
        startActivity(new Intent(this, (Class<?>) ReportsActivity.class));
    }

    static /* synthetic */ void lambda$homeAction$10(Runnable runnable, View view) {
        UiTheme.feedback(view);
        runnable.run();
    }

    public /* synthetic */ void lambda$homeFollowUp$12(JSONObject jSONObject, View view) {
        edit(jSONObject, 0L);
    }

    public /* synthetic */ void lambda$importCallCsv$81(String str) {
        this.backgroundOps--;
        this.importing = false;
        if (isFinishing() || isDestroyed()) {
            return;
        }
        History.status(this, str);
        this.tab = "unassigned";
        render();
        alert(str);
    }

    public /* synthetic */ void lambda$importCallCsv$82(Uri uri) {
        String message;
        try {
            message = CallCsvImporter.run(getApplicationContext(), read(uri, 5242880)).message();
        } catch (Exception e) {
            message = "تعذر استيراد الملف: " + (e.getMessage() == null ? "تحقق من صيغة CSV" : e.getMessage());
        }
        final String strMessage = message;
        runOnUiThread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$importCallCsv$81(strMessage);
            }
        });
    }

    public /* synthetic */ void lambda$importContactVcf$83(String str) {
        this.backgroundOps--;
        if (isFinishing() || isDestroyed()) {
            return;
        }
        render();
        alert(str);
    }

    public /* synthetic */ void lambda$importContactVcf$84(Uri uri) {
        String message;
        try {
            Map<String, String> map = VCardImporter.parse(read(uri, 15728640));
            this.store.cacheContactNames(map);
            message = "تم حفظ " + map.size() + " اسمًا ورقمًا سعوديًا في دليل جهات الاتصال المحلي، وتحديث أسماء " + this.store.refreshContactNames(map) + " سجلًا موجودًا. لا تُضاف جهات الاتصال كعملاء تلقائيًا؛ ستظهر أسماؤها عند استيراد سجل مكالمات مطابق.";
        } catch (Exception e) {
            message = "تعذر استيراد جهات الاتصال: " + (e.getMessage() == null ? "تحقق من ملف VCF" : e.getMessage());
        }
        final String str = message;
        runOnUiThread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$importContactVcf$83(str);
            }
        });
    }

    public /* synthetic */ void lambda$leadCard$28(JSONObject jSONObject, CompoundButton compoundButton, boolean z) {
        LinkedHashSet<Long> linkedHashSet = this.selectedCalls;
        Long lValueOf = Long.valueOf(jSONObject.optLong("id"));
        if (z) {
            linkedHashSet.add(lValueOf);
        } else {
            linkedHashSet.remove(lValueOf);
        }
    }

    public /* synthetic */ void lambda$leadCard$29(JSONObject jSONObject) {
        edit(jSONObject, 0L);
    }

    public /* synthetic */ void lambda$leadCard$30(JSONObject jSONObject) {
        this.selectedCalls.add(Long.valueOf(jSONObject.optLong("id")));
        assignCalls();
    }

    public /* synthetic */ void lambda$leadCard$31(JSONObject jSONObject, String str) {
        this.store.event(jSONObject.optLong("id"), "dial", "فتح شاشة الاتصال — لا يعني إتمام المكالمة", System.currentTimeMillis(), null);
        startActivity(new Intent("android.intent.action.DIAL", Uri.parse("tel:" + str)));
    }

    public /* synthetic */ void lambda$leadCard$32(String str) {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://wa.me/" + str.replace("+", ""))));
        } catch (Exception unused) {
            alert(tr("لا يوجد تطبيق يستطيع فتح واتساب.", "No app can open WhatsApp."));
        }
    }

    public /* synthetic */ void lambda$messages$52(JSONObject jSONObject, DialogInterface dialogInterface, int i) {
        this.store.dismiss(jSONObject.optLong("id"));
        render();
    }

    public /* synthetic */ void lambda$messages$53(final JSONObject jSONObject) {
        new FluentDialog(this).setMessage("حذف هذه الرسالة من قائمة المراجعة؟").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حذف", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$messages$52(jSONObject, dialogInterface, i);
            }
        }).show();
    }

    public /* synthetic */ void lambda$messages$54() {
        this.limit += 100;
        render();
    }

    public /* synthetic */ void lambda$messages$55() {
        ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT sender, body FROM messages WHERE state='reviewed' ORDER BY at DESC LIMIT 50", new String[0]);
        StringBuilder sb = new StringBuilder();
        for (JSONObject jSONObject : arrayListQuery) {
            sb.append(jSONObject.optString("sender")).append(": ").append(jSONObject.optString("body")).append("\n\n");
        }
        alert(sb.length() == 0 ? "لا يوجد أرشيف بعد" : sb.toString());
    }

    public /* synthetic */ void lambda$pasteText$59(EditText editText, DialogInterface dialogInterface, int i) {
        String string = editText.getText().toString();
        if (string.trim().isEmpty() || string.length() > 100000) {
            alert("أدخل نصًا حتى 100 ألف حرف");
        } else {
            saveSharedText(string, "");
        }
    }

    public /* synthetic */ void lambda$quickPanel$58(EditText editText, EditText editText2, JSONObject jSONObject, Spinner spinner, Map map, LinkedHashMap linkedHashMap, JSONObject jSONObject2, Map map2, Analysis analysis, JSONObject jSONObject3, AlertDialog[] alertDialogArr) {
        String message;
        try {
            String strTrim = editText.getText().toString().trim();
            String strTrim2 = editText2.getText().toString().trim();
            if (strTrim2.isEmpty()) {
                strTrim2 = Analysis.phone(strTrim);
            }
            jSONObject.put("name", strTrim2).put("phone", strTrim).put("business", spinner.getSelectedItemPosition() == 1 ? "survey" : spinner.getSelectedItemPosition() == 2 ? "software" : "");
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) map).entrySet()) {
                if (((CheckBox) entry.getValue()).isChecked()) {
                    jSONObject.put((String) entry.getKey(), linkedHashMap.get(entry.getKey()));
                }
            }
            SQLiteDatabase writableDatabase = this.store.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                long jSave = this.store.save(jSONObject);
                String strOptString = jSONObject.optString("business");
                JSONObject jSONObjectOptJSONObject = jSONObject2.optJSONObject(strOptString);
                if (jSONObjectOptJSONObject == null) {
                    jSONObjectOptJSONObject = new JSONObject();
                }
                boolean z = false;
                for (Map.Entry<?, ?> entry2 : ((Map<?, ?>) map2).entrySet()) {
                    if (((CheckBox) entry2.getValue()).isChecked() && allowedDetail(strOptString, (String) entry2.getKey())) {
                        jSONObjectOptJSONObject.put((String) entry2.getKey(), analysis.details.get(entry2.getKey()));
                        z = true;
                    }
                }
                if (z) {
                    jSONObject2.put(strOptString, jSONObjectOptJSONObject);
                    this.store.request(jSave, jSONObject2);
                }
                this.store.reviewed(jSONObject3.optLong("id"), jSave);
                writableDatabase.setTransactionSuccessful();
                writableDatabase.endTransaction();
                alertDialogArr[0].dismiss();
                render();
                Reminders.schedule(this);
                alert("حُفظ العميل والمعلومات المختارة");
            } catch (Throwable th) {
                writableDatabase.endTransaction();
                throw th;
            }
        } catch (SQLiteConstraintException unused) {
            message = "هذا الرقم موجود بالفعل. اختر العميل الموجود للربط.";
            alert(message);
        } catch (Exception e) {
            message = e.getMessage() == null ? "تعذر الحفظ" : e.getMessage();
            alert(message);
        }
    }

    public /* synthetic */ void lambda$receive$102(String str, String str2, String str3) {
        this.backgroundOps--;
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (str != null) {
            alert("تعذر قراءة المشاركة؛ جرّب مشاركة نص الرسالة فقط");
        } else {
            saveShared(str2, str3);
        }
    }

    public /* synthetic */ void lambda$receive$103(Uri uri, final String str) {
        String body = null;
        String failure = null;
        try {
            body = read(uri, 1048576);
        } catch (Exception e) {
            failure = e.getMessage();
        }
        final String str2 = body;
        final String message = failure;
        runOnUiThread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$receive$102(message, str2, str);
            }
        });
    }

    static /* synthetic */ WindowInsets lambda$render$1(View view, WindowInsets windowInsets) {
        int systemWindowInsetTop;
        int systemWindowInsetBottom;
        if (Build.VERSION.SDK_INT >= 30) {
            Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars());
            systemWindowInsetTop = insets.top;
            systemWindowInsetBottom = insets.bottom;
        } else {
            systemWindowInsetTop = windowInsets.getSystemWindowInsetTop();
            systemWindowInsetBottom = windowInsets.getSystemWindowInsetBottom();
        }
        view.setPadding(0, systemWindowInsetTop, 0, systemWindowInsetBottom);
        return windowInsets;
    }

    public /* synthetic */ void lambda$render$2(View view) {
        switchLanguage();
    }

    public /* synthetic */ void lambda$render$3(String[] strArr, View view) {
        this.tab = strArr[0];
        this.limit = 100;
        render();
    }

    public /* synthetic */ void lambda$requestDetails$106(JSONObject jSONObject, String[][] strArr, Map map, JSONObject jSONObject2, String str, long j, AlertDialog[] alertDialogArr) {
        try {
            JSONObject jSONObject3 = new JSONObject(jSONObject.toString());
            for (String[] strArr2 : strArr) {
                String strTrim = ((EditText) map.get(strArr2[0])).getText().toString().trim();
                if (!strTrim.isEmpty() && (strArr2[0].endsWith("Date") || strArr2[0].equals("subscriptionEnd"))) {
                    LocalDate.parse(strTrim);
                }
                if (!strTrim.isEmpty() && strArr2[0].equals("quantity") && Integer.parseInt(strTrim) < 1) {
                    throw new IllegalArgumentException("الكمية يجب أن تكون أكبر من صفر");
                }
                if (!strTrim.isEmpty() && strArr2[0].equals("budget") && new BigDecimal(strTrim).signum() < 0) {
                    throw new IllegalArgumentException("الميزانية غير صالحة");
                }
                jSONObject3.put(strArr2[0], strTrim);
            }
            jSONObject2.put(str, jSONObject3);
            this.store.request(j, jSONObject2);
            alertDialogArr[0].dismiss();
            alert("حُفظت تفاصيل الطلب");
        } catch (Exception unused) {
            alert("تحقق من الكمية والميزانية والتواريخ بصيغة YYYY-MM-DD");
        }
    }

    public /* synthetic */ void lambda$restoreDocument$100(boolean z, String str) {
        this.backgroundOps--;
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (z) {
            ((NotificationManager) getSystemService(NotificationManager.class)).cancelAll();
            render();
            Reminders.schedule(this);
        }
        alert(str);
    }

    public /* synthetic */ void lambda$restoreDocument$101(char[] cArr, String str) {
        String result;
        boolean restored = false;
        try {
            this.store.restore(new JSONObject(new String(cArr == null ? str.getBytes(StandardCharsets.UTF_8) : BackupCipher.decrypt(str, cArr), StandardCharsets.UTF_8)));
            getSharedPreferences("settings", 0).edit().putLong("historyId", 0L).apply();
            result = "اكتملت الاستعادة";
            restored = true;
        } catch (Exception e) {
            result = "تعذر استعادة النسخة: " + (e.getMessage() == null ? "تحقق من كلمة المرور والملف" : e.getMessage());
        } finally {
            if (cArr != null) {
                Arrays.fill(cArr, (char) 0);
            }
        }
        final String str2 = result;
        final boolean z = restored;
        runOnUiThread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$restoreDocument$100(z, str2);
            }
        });
    }

    public /* synthetic */ void lambda$restoreSelected$95(String str) {
        this.backgroundOps--;
        if (isFinishing() || isDestroyed()) {
            return;
        }
        alert("تعذر قراءة النسخة: " + str);
    }

    public /* synthetic */ void lambda$restoreSelected$96(String str, boolean z) {
        this.backgroundOps--;
        if (isFinishing() || isDestroyed()) {
            return;
        }
        askRestorePassword(str, z);
    }

    public /* synthetic */ void lambda$restoreSelected$97(Uri uri) {
        try {
            final String str = read(uri, MAX_ENCRYPTED_BACKUP_BYTES);
            final boolean zEncrypted = BackupCipher.encrypted(str);
            runOnUiThread(new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$restoreSelected$96(str, zEncrypted);
                }
            });
        } catch (Exception e) {
            final String message = e.getMessage() == null ? "تحقق من الملف" : e.getMessage();
            runOnUiThread(new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$restoreSelected$95(message);
                }
            });
        }
    }

    public /* synthetic */ void lambda$review$56(String str, JSONObject jSONObject, Spinner spinner, String[] strArr, Spinner spinner2, DialogInterface dialogInterface, int i) {
        LinkedHashMap<String, ArrayList<ChatText.Message>> linkedHashMap = ChatText.parse(str, jSONObject.optLong("at"), spinner.getSelectedItemPosition() == 1);
        String str2 = strArr[spinner2.getSelectedItemPosition()];
        ArrayList<ChatText.Message> arrayList = linkedHashMap.get(str2);
        if (arrayList == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        Iterator<ChatText.Message> it = arrayList.iterator();
        while (it.hasNext()) {
            sb.append(it.next().body).append("\n");
        }
        Analysis analysisInspect = Analysis.inspect(sb.toString(), jSONObject.optLong("at"));
        analysisInspect.followUp = "";
        analysisInspect.followTime = "";
        for (ChatText.Message message : arrayList) {
            Analysis analysisInspect2 = Analysis.inspect(message.body, message.at);
            if (!analysisInspect2.followUp.isEmpty() && (message.dated || message.body.matches("(?s).*20[0-9]{2}-[0-9]{2}-[0-9]{2}.*"))) {
                analysisInspect.followUp = analysisInspect2.followUp;
            }
            if (!analysisInspect2.followTime.isEmpty()) {
                analysisInspect.followTime = analysisInspect2.followTime;
            }
        }
        String strPhone = Analysis.phone(str2);
        if (!strPhone.isEmpty()) {
            analysisInspect.phone = strPhone;
        }
        chooseQuick(jSONObject, analysisInspect, str2);
    }

    public /* synthetic */ void lambda$settings$60() {
        startActivity(new Intent(this, (Class<?>) LedgerActivity.class));
    }

    public /* synthetic */ void lambda$settings$61() {
        startActivity(new Intent(this, (Class<?>) ReportsActivity.class));
    }

    public /* synthetic */ void lambda$settings$62() {
        this.tab = "ads";
        render();
    }

    public /* synthetic */ void lambda$settings$63() {
        editProfile(this.business);
    }

    public /* synthetic */ void lambda$settings$65() {
        ShortcutManager shortcutManager = (ShortcutManager) getSystemService(ShortcutManager.class);
        if (shortcutManager.isRequestPinShortcutSupported()) {
            shortcutManager.requestPinShortcut(new ShortcutInfo.Builder(this, "tawasul-crm").setShortLabel("تواصل").setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher)).setIntent(new Intent(this, (Class<?>) MainActivity.class).setAction("android.intent.action.VIEW")).build(), null);
        } else {
            alert("اسحب أيقونة تواصل من قائمة التطبيقات إلى الشاشة الرئيسية.");
        }
    }

    public /* synthetic */ void lambda$settings$66(SharedPreferences sharedPreferences, CompoundButton compoundButton, boolean z) {
        sharedPreferences.edit().putBoolean("reminders", z).apply();
        if (z) {
            enableNotifications();
        } else {
            ((NotificationManager) getSystemService(NotificationManager.class)).cancelAll();
            Reminders.schedule(this);
        }
    }

    public /* synthetic */ void lambda$settings$67() {
        String str;
        if (Reminders.allowed(this)) {
            Reminders.test(this);
            str = "أُرسل الإشعار التجريبي؛ افحص شريط الإشعارات.";
        } else {
            str = "فعّل إذن الإشعارات أولًا من زر التفعيل";
        }
        alert(str);
    }

    public /* synthetic */ void lambda$settings$68() {
        startActivity(new Intent("android.settings.APP_NOTIFICATION_SETTINGS").putExtra("android.provider.extra.APP_PACKAGE", getPackageName()));
    }

    private /* synthetic */ void lambda$settings$71() {
        RoleManager roleManager = (RoleManager) getSystemService(RoleManager.class);
        if (roleManager.isRoleAvailable("android.app.role.CALL_SCREENING")) {
            startActivityForResult(roleManager.createRequestRoleIntent("android.app.role.CALL_SCREENING"), 11);
        } else {
            alert("هذا الدور غير متاح على الهاتف");
        }
    }

    public /* synthetic */ void lambda$settings$72(DialogInterface dialogInterface, int i) {
        if (checkSelfPermission("android.permission.READ_CALL_LOG") == 0) {
            importHistory();
        } else {
            requestPermissions(new String[]{"android.permission.READ_CALL_LOG"}, 12);
        }
    }

    private /* synthetic */ void lambda$settings$73() {
        new FluentDialog(this).setMessage("سيُقرأ سجل المكالمات وتُحفظ الأرقام السعودية وأسماؤها محليًا. هل تريد البدء؟").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("استيراد", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$settings$72(dialogInterface, i);
            }
        }).show();
    }

    public /* synthetic */ void lambda$settings$74(DialogInterface dialogInterface, int i) {
        startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT").setType("application/json").addCategory("android.intent.category.OPENABLE"), 21);
    }

    public /* synthetic */ void lambda$settings$75() {
        new FluentDialog(this).setMessage("الاستعادة تستبدل العملاء والرسائل والمنشورات وملفات المنشآت في النسخ الجديدة. أنشئ نسخة احتياطية أولًا.").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("اختيار النسخة", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$settings$74(dialogInterface, i);
            }
        }).show();
    }

    public /* synthetic */ void lambda$syncContactNames$79(boolean z, Map map, int i, Runnable runnable) {
        String str;
        this.backgroundOps--;
        if (isFinishing() || isDestroyed()) {
            return;
        }
        render();
        if (z) {
            if (i < 0) {
                str = "تعذر تحديث الأسماء الآن؛ أعد المحاولة بعد قليل";
            } else if (map.isEmpty()) {
                str = "لم يعثر التطبيق على جهات اتصال سعودية قابلة للقراءة";
            } else {
                str = i > 0 ? "تم حفظ دليل جهات الاتصال وتحديث أسماء " + i + " سجلًا موجودًا" : "تم حفظ دليل جهات الاتصال. لا توجد أرقام مطابقة في الوارد أو العملاء الحاليين بعد.";
            }
            alert(str);
        }
        if (runnable != null) {
            runnable.run();
        }
    }

    public /* synthetic */ void lambda$syncContactNames$80(final boolean z, final Runnable runnable) {
        final Map<String, String> mapContacts = PhoneData.contacts(getApplicationContext());
        int updated;
        try {
            this.store.cacheContactNames(mapContacts);
            updated = this.store.refreshContactNames(mapContacts);
        } catch (Exception unused) {
            updated = -1;
        }
        final int iRefreshContactNames = updated;
        runOnUiThread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$syncContactNames$79(z, mapContacts, iRefreshContactNames, runnable);
            }
        });
    }

    public /* synthetic */ void lambda$syncHistory$85(boolean z) {
        this.backgroundOps--;
        this.importing = false;
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (z) {
            this.tab = "unassigned";
        }
        render();
        if (z) {
            alert(getSharedPreferences("settings", 0).getString("status", ""));
        }
    }

    public /* synthetic */ void lambda$syncHistory$86(final boolean z) {
        History.run(getApplicationContext());
        runOnUiThread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$syncHistory$85(z);
            }
        });
    }

    public /* synthetic */ void lambda$templates$107(EditText editText, DialogInterface dialogInterface, int i) {
        ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("رسالة العميل", editText.getText().toString()));
    }

    public /* synthetic */ void lambda$templates$108(JSONObject jSONObject, EditText editText, long j, String str, DialogInterface dialogInterface, int i) {
        String strOptString = jSONObject.optString("phone");
        if (strOptString.isEmpty() || strOptString.equals("null")) {
            alert("أضف رقم العميل أو استخدم نسخ النص");
            return;
        }
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://wa.me/" + strOptString.replace("+", "") + "?text=" + Uri.encode(editText.getText().toString()))));
            this.store.event(j, "template", "تجهيز قالب واتساب: " + str + " — لا يؤكد إرسال الرسالة", System.currentTimeMillis(), null);
        } catch (Exception unused) {
            alert("تعذر فتح واتساب؛ استخدم نسخ النص");
        }
    }

    public /* synthetic */ void lambda$templates$109(String[] strArr, ArrayList arrayList, final JSONObject jSONObject, final long j, DialogInterface dialogInterface, int i) {
        if (i == strArr.length - 1) {
            editTemplates();
            return;
        }
        LinearLayout linearLayoutForm = form();
        final EditText editTextField = field(linearLayoutForm, "راجع النص وعدّله قبل الإرسال", WhatsAppTemplates.fill((String) arrayList.get(i), jSONObject, null), 131073);
        editTextField.setMinLines(7);
        muted(linearLayoutForm, "فتح واتساب يجهز النص فقط. تضغط إرسال داخل واتساب بنفسك.");
        final String str = strArr[i];
        new FluentDialog(this).setTitle(str).setView(linearLayoutForm).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setNeutralButton("نسخ النص", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface2, int i2) {
                MainActivity.this.lambda$templates$107(editTextField, dialogInterface2, i2);
            }
        }).setPositiveButton("فتح واتساب", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface2, int i2) {
                MainActivity.this.lambda$templates$108(jSONObject, editTextField, j, str, dialogInterface2, i2);
            }
        }).show();
    }

    public /* synthetic */ void lambda$timeline$105(EditText editText, long j, AlertDialog[] alertDialogArr) {
        String strTrim = editText.getText().toString().trim();
        if (strTrim.isEmpty()) {
            alert("اكتب الملاحظة أولًا");
            return;
        }
        this.store.event(j, "note", strTrim, System.currentTimeMillis(), null);
        alertDialogArr[0].dismiss();
        timeline(j);
    }

    public /* synthetic */ void lambda$tools$34(JSONObject jSONObject) {
        startActivity(new Intent(this, (Class<?>) SoftwareActivity.class).putExtra("leadId", jSONObject.optLong("id")));
    }

    public /* synthetic */ void lambda$tools$35(JSONObject jSONObject) {
        timeline(jSONObject.optLong("id"));
    }

    public /* synthetic */ void lambda$tools$36(JSONObject jSONObject) {
        requestDetails(jSONObject.optLong("id"));
    }

    public /* synthetic */ void lambda$tools$37(JSONObject jSONObject) {
        templates(jSONObject.optLong("id"));
    }

    public /* synthetic */ void lambda$tools$38(AlertDialog alertDialog, JSONObject jSONObject) {
        alertDialog.dismiss();
        followAction(jSONObject.optLong("id"), false);
    }

    public /* synthetic */ void lambda$tools$39(AlertDialog alertDialog, JSONObject jSONObject) {
        alertDialog.dismiss();
        followAction(jSONObject.optLong("id"), true);
    }

    public /* synthetic */ void lambda$writeCsv$91(String str) {
        this.backgroundOps--;
        if (isFinishing()) {
            return;
        }
        alert(str);
    }

    public /* synthetic */ void lambda$writeCsv$92(String str, Uri uri) {
        String message;
        byte[] bArrLedger;
        try {
            if (str.equals("customers")) {
                bArrLedger = CsvExport.customers(this.store.query("SELECT * FROM leads WHERE business=? ORDER BY id DESC", "survey"));
            } else if (str.equals("quotes")) {
                ArrayList arrayList = new ArrayList();
                Iterator<JSONObject> it = this.store.query("SELECT * FROM quotes ORDER BY id DESC", new String[0]).iterator();
                while (it.hasNext()) {
                    arrayList.add(new JSONObject(it.next().getString("data")));
                }
                bArrLedger = CsvExport.quotes(arrayList);
            } else {
                bArrLedger = CsvExport.ledger(this.store.query("SELECT * FROM ledgerEntries ORDER BY at DESC,id DESC", new String[0]));
            }
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                if (outputStream == null) {
                    throw new IOException();
                }
                outputStream.write(bArrLedger);
            }
            message = "تم تصدير CSV بنجاح";
        } catch (Exception unused) {
            message = "تعذر تصدير CSV؛ تحقق من المساحة ومكان الحفظ";
        }
        final String str2 = message;
        runOnUiThread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$writeCsv$91(str2);
            }
        });
    }

    public /* synthetic */ void lambda$writeEncryptedBackup$93(String str) {
        this.backgroundOps--;
        if (isFinishing() || isDestroyed()) {
            return;
        }
        alert(str);
    }

    public /* synthetic */ void lambda$writeEncryptedBackup$94(char[] cArr, Uri uri) {
        String message;
        try {
            byte[] bArrEncrypt = BackupCipher.encrypt(backupBytes(), cArr);
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                if (outputStream == null) {
                    throw new IOException();
                }
                outputStream.write(bArrEncrypt);
            }
            message = "حُفظت النسخة الاحتياطية المشفرة";
        } catch (Exception e) {
            message = "تعذر حفظ النسخة المشفرة: " + (e.getMessage() == null ? "تحقق من الملف والمساحة" : e.getMessage());
        } finally {
            Arrays.fill(cArr, (char) 0);
        }
        final String str = message;
        runOnUiThread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$writeEncryptedBackup$93(str);
            }
        });
    }

    private void leadCard(final JSONObject jSONObject) {
        String str;
        String str2;
        int iAccent;
        int iTint;
        boolean zEquals = this.tab.equals("unassigned");
        LinearLayout linearLayoutCard = card(this.content);
        LinearLayout linearLayoutRow = row();
        linearLayoutCard.addView(linearLayoutRow);
        String strOptString = jSONObject.optString("name");
        final String strOptString2 = jSONObject.optString("phone", "");
        boolean z = strOptString.isEmpty() || (!strOptString2.isEmpty() && Analysis.phone(strOptString).equals(strOptString2));
        if (zEquals) {
            if (z) {
                str = "رقم وارد";
                str2 = "Incoming number";
                strOptString = tr(str, str2);
            }
        } else if (z) {
            str = "عميل غير مسمى";
            str2 = "Unnamed customer";
            strOptString = tr(str, str2);
        }
        TextView textViewText = text(z ? "?" : strOptString.substring(0, 1), 20);
        textViewText.setTextColor(-1);
        textViewText.setGravity(17);
        textViewText.setPadding(0, 0, 0, 0);
        textViewText.setBackground(UiTheme.sectionSurface(this, true, 16));
        linearLayoutRow.addView(textViewText, new LinearLayout.LayoutParams(dp(46), dp(46)));
        LinearLayout linearLayoutColumn = column();
        linearLayoutColumn.setPadding(dp(10), 0, dp(10), 0);
        linearLayoutRow.addView(linearLayoutColumn, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayoutColumn.addView(text(strOptString, 17));
        if (!strOptString2.isEmpty() && !strOptString2.equals("null")) {
            TextView textViewText2 = text(DisplayNumbers.latin(strOptString2), 12);
            textViewText2.setTextDirection(3);
            textViewText2.setTextColor(-10192758);
            linearLayoutColumn.addView(textViewText2);
        }
        if (zEquals) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setContentDescription(tr("تحديد الرقم للاعتماد", "Select number for approval"));
            checkBox.setChecked(this.selectedCalls.contains(Long.valueOf(jSONObject.optLong("id"))));
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z2) {
                    MainActivity.this.lambda$leadCard$28(jSONObject, compoundButton, z2);
                }
            });
            linearLayoutRow.addView(checkBox, new LinearLayout.LayoutParams(dp(48), dp(48)));
        }
        String[] strArrStages = stages(jSONObject.optString("business"), jSONObject.optString("requestType", "sales"));
        int iMax = Math.max(0, Math.min(5, jSONObject.optInt("stage")));
        String strTr = zEquals ? tr("بانتظار المراجعة", "Pending review") : strArrStages[iMax];
        if (iMax == 4) {
            iAccent = -14125988;
        } else {
            iAccent = iMax == 5 ? -6076858 : UiTheme.accent(this);
        }
        if (iMax == 4) {
            iTint = -1575955;
        } else {
            iTint = iMax == 5 ? -397586 : SectionColors.tint(UiTheme.section(this));
        }
        View viewPill = pill(strTr, iAccent, iTint);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(0, dp(9), 0, dp(4));
        linearLayoutCard.addView(viewPill, layoutParams);
        if (!jSONObject.optString("company").isEmpty()) {
            muted(linearLayoutCard, jSONObject.optString("company"));
        }
        if (!jSONObject.optString("interest").isEmpty()) {
            muted(linearLayoutCard, jSONObject.optString("interest"));
        }
        String strOptString3 = jSONObject.optString("followUp");
        if (!strOptString3.isEmpty()) {
            TextView textViewText3 = text(strOptString3 + "  •  " + jSONObject.optString("followTime", "09:00"), 12);
            textViewText3.setTextDirection(3);
            textViewText3.setTextColor(strOptString3.compareTo(LocalDate.now().toString()) < 0 ? -6070492 : -10192758);
            linearLayoutCard.addView(textViewText3);
        }
        LinearLayout linearLayoutRow2 = row();
        linearLayoutCard.addView(linearLayoutRow2);
        button(linearLayoutRow2, tr("التفاصيل", "Details"), new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$leadCard$29(jSONObject);
            }
        }).setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
        if (zEquals) {
            button(linearLayoutRow2, tr("اعتماد عميل", "Approve customer"), new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$leadCard$30(jSONObject);
                }
            }).setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
            return;
        }
        if (strOptString2.isEmpty() || strOptString2.equals("null")) {
            return;
        }
        Button button = button(linearLayoutRow2, tr("اتصال", "Call"), new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$leadCard$31(jSONObject, strOptString2);
            }
        });
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams2.setMargins(dp(5), 0, dp(5), 0);
        button.setLayoutParams(layoutParams2);
        button(linearLayoutRow2, tr("واتساب", "WhatsApp"), new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$leadCard$32(strOptString2);
            }
        }).setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
    }

    private void messages() {
        UiTheme.header(this.content, tr("الرسائل والوارد", "Messages & inbox"), "messages");
        muted(this.content, tr("راجع النصوص التي شاركتها مع التطبيق قبل اعتماد بياناتها.", "Review shared messages before approving their details."));
        primary(button(this.content, "لصق نص أو محادثة للتحليل", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.pasteText();
            }
        }));
        ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM messages WHERE state='pending' ORDER BY at DESC LIMIT " + (this.limit + 1), new String[0]);
        if (arrayListQuery.isEmpty()) {
            empty("لا توجد رسائل معلّقة", "شارك نص رسالة أو ملف TXT من واتساب إلى عملائي، ثم راجع المعلومات قبل حفظها.");
        }
        int i = 0;
        for (final JSONObject jSONObject : arrayListQuery) {
            int i2 = i + 1;
            if (i >= this.limit) {
                break;
            }
            LinearLayout linearLayoutCard = card(this.content);
            linearLayoutCard.addView(text(jSONObject.optString("sender").isEmpty() ? "نص مشارك" : jSONObject.optString("sender"), 19));
            linearLayoutCard.addView(text(jSONObject.optString("source") + " • " + ((Object) DateFormat.format("yyyy-MM-dd HH:mm", jSONObject.optLong("at"))), 14));
            String strOptString = jSONObject.optString("body");
            if (strOptString.length() > 4000) {
                strOptString = strOptString.substring(0, 4000) + "\n… نص طويل؛ محفوظ في الأرشيف المحلي";
            }
            linearLayoutCard.addView(text(strOptString, 16));
            button(linearLayoutCard, "تحليل ومراجعة المعلومات", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$messages$51(jSONObject);
                }
            });
            button(linearLayoutCard, "تجاهل هذه الرسالة", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$messages$53(jSONObject);
                }
            });
            i = i2;
        }
        if (arrayListQuery.size() > this.limit) {
            button(this.content, "عرض المزيد", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$messages$54();
                }
            });
        }
        button(this.content, "عرض أرشيف الرسائل المراجعة", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$messages$55();
            }
        });
    }

    private void metric(LinearLayout linearLayout, String str, int i, int i2) {
        LinearLayout linearLayoutColumn = column();
        linearLayoutColumn.setPadding(dp(12), dp(12), dp(12), dp(12));
        UiTheme.styleCard(linearLayoutColumn);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams.setMargins(dp(3), 0, dp(3), 0);
        linearLayout.addView(linearLayoutColumn, layoutParams);
        TextView textViewText = text(String.valueOf(i), 24);
        textViewText.setTextColor(UiTheme.accent(this));
        linearLayoutColumn.addView(textViewText);
        muted(linearLayoutColumn, str);
    }

    private void muted(LinearLayout linearLayout, String str) {
        TextView textViewText = text(str, 13);
        textViewText.setTextColor(-10192758);
        linearLayout.addView(textViewText);
    }

    public void openCallCsv() {
        startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT").setType("text/*").addCategory("android.intent.category.OPENABLE"), 31);
    }

    public void openContactVcf() {
        startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT").setType("text/x-vcard").putExtra("android.intent.extra.MIME_TYPES", new String[]{"text/x-vcard", "text/vcard", "text/directory", "text/plain"}).addCategory("android.intent.category.OPENABLE"), 32);
    }

    public void openPrivacyPolicy() {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse(BuildConfig.PRIVACY_POLICY_URL)));
        } catch (Exception unused) {
            alert("تعذر فتح المتصفح. الرابط: https://soom12034-glitch.github.io/EngineeringCRM/privacy-policy.html");
        }
    }

    private int organizationActivityIndex(String str) {
        int i = 0;
        while (true) {
            String[] strArr = ORGANIZATION_ACTIVITIES;
            if (i >= strArr.length) {
                return strArr.length - 1;
            }
            if (strArr[i].equals(str)) {
                return i;
            }
            i++;
        }
    }

    private AlertDialog panel(String str, LinearLayout linearLayout) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(linearLayout);
        AlertDialog alertDialogCreate = new FluentDialog(this).setTitle(str).setView(scrollView).setNegativeButton("إغلاق", (DialogInterface.OnClickListener) null).create();
        alertDialogCreate.show();
        alertDialogCreate.getWindow().setBackgroundDrawable(shape(-1, 20, 0));
        alertDialogCreate.getWindow().setLayout(-1, -1);
        return alertDialogCreate;
    }

    public void pasteText() {
        LinearLayout linearLayoutForm = form();
        final EditText editTextField = field(linearLayoutForm, "الصق نص الرسالة أو المحادثة هنا", "", 131073);
        editTextField.setMinLines(8);
        new FluentDialog(this).setTitle("تحليل نص واتساب").setView(linearLayoutForm).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("تحليل", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$pasteText$59(editTextField, dialogInterface, i);
            }
        }).show();
    }

    public void lambda$edit$45(final EditText editText) {
        LocalDate localDateNow;
        try {
            localDateNow = editText.getText().toString().trim().isEmpty() ? LocalDate.now() : LocalDate.parse(editText.getText().toString().trim());
        } catch (Exception unused) {
            localDateNow = LocalDate.now();
        }
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public final void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                editText.setText(LocalDate.of(i, i2 + 1, i3).toString());
            }
        }, localDateNow.getYear(), localDateNow.getMonthValue() - 1, localDateNow.getDayOfMonth()).show();
    }

    private TextView pill(String str, int i, int i2) {
        TextView textViewText = text(str, 11);
        textViewText.setTextColor(i);
        textViewText.setPadding(dp(10), dp(5), dp(10), dp(5));
        textViewText.setBackground(shape(i2, 20, 0));
        return textViewText;
    }

    private void primary(Button button) {
        UiTheme.button(button, true);
    }

    public void privacyPolicy() {
        LinearLayout linearLayoutForm = form();
        linearLayoutForm.addView(text("سياسة الخصوصية", 24));
        linearLayoutForm.addView(text("يعمل تواصل محليًا على جهازك ولا يطلب إذن الإنترنت. لا يرسل بيانات العملاء أو جهات الاتصال أو الرسائل أو النسخ الاحتياطية إلى خوادم المطور.", 15));
        muted(linearLayoutForm, "الأذونات واستخدامها");
        linearLayoutForm.addView(text("• جهات الاتصال: اختيارية، وتُستخدم محليًا لمطابقة رقم العميل بالاسم أو تجهيز حفظه في الهاتف.\n• الإشعارات: لتنبيهات المتابعة التي تفعّلها.\n• سجل المكالمات: لا تطلبه نسخة Google Play؛ يوجد فقط في الإصدار المباشر المنفصل خارج المتجر.", 14));
        muted(linearLayoutForm, "الحفظ والحذف");
        linearLayoutForm.addView(text("تُحفظ البيانات داخل مساحة التطبيق المعزولة. يمكنك حذف السجلات، أو مسح بيانات التطبيق من إعدادات Android، أو إزالة التطبيق. النسخ الاحتياطية مشفرة بكلمة مرور يحددها المستخدم ولا يستطيع المطور استعادتها.", 14));
        muted(linearLayoutForm, "الحماية والمشاركة");
        linearLayoutForm.addView(text("لا توجد حسابات أو إعلانات أو أدوات تحليل أو تتبع. لا تنتقل البيانات إلى تطبيق خارجي إلا عندما تختار أنت المشاركة عبر واتساب أو البريد أو غيرهما.", 14));
        button(linearLayoutForm, "عرض السياسة الكاملة على الويب", this::openPrivacyPolicy);
        muted(linearLayoutForm, "آخر تحديث: 8 أكتوبر 2026 • للتواصل: enginex@enginex2030.com");
        panel("الخصوصية والأمان", linearLayoutForm);
    }

    private String profileBrand(String str) {
        JSONObject jSONObjectProfile = this.store.profile(str);
        return jSONObjectProfile.optString("brand").isEmpty() ? jSONObjectProfile.optString("name") : jSONObjectProfile.optString("brand");
    }

    private String profileContact(String str) {
        JSONObject jSONObjectProfile = this.store.profile(str);
        String strFirstPhone = Profiles.firstPhone(jSONObjectProfile.optString("phones"));
        return strFirstPhone.isEmpty() ? jSONObjectProfile.optString("whatsapp") : strFirstPhone;
    }

    private String profileFooter(String str, boolean z) {
        String strTrim;
        JSONObject jSONObjectProfile = this.store.profile(str);
        StringBuilder sb = new StringBuilder();
        if (z && !profileBrand(str).isEmpty()) {
            sb.append(profileBrand(str));
        }
        JSONObject jSONObjectOptJSONObject = jSONObjectProfile.optJSONObject("advertising");
        for (String str2 : Profiles.AD_FIELDS) {
            boolean z2 = str2.equals("address") || str2.equals("phones") || str2.equals("whatsapp") || str2.equals("website");
            if (jSONObjectOptJSONObject != null) {
                if (jSONObjectOptJSONObject.optBoolean(str2, z2)) {
                    strTrim = jSONObjectProfile.optString(str2).trim();
                    if (!strTrim.isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append(Profiles.label(str2)).append(": ").append(strTrim);
                    }
                }
            } else if (z2) {
                strTrim = jSONObjectProfile.optString(str2).trim();
                if (!strTrim.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(Profiles.label(str2)).append(": ").append(strTrim);
                }
            }
        }
        return sb.toString();
    }

    private void quickPanel(final JSONObject jSONObject, JSONObject jSONObject2, final Analysis analysis, String str) {
        JSONObject jSONObject3;
        try {
            final JSONObject jSONObject4 = new JSONObject(jSONObject2.toString());
            LinearLayout linearLayoutForm = form();
            linearLayoutForm.addView(text("المعلومات المستخرجة", 22));
            muted(linearLayoutForm, "مراجعة واحدة بدل تعبئة نموذج العميل. المعلومات الموجودة لا تُستبدل إلا إذا اخترت ذلك.");
            boolean z = jSONObject4.optLong("id") == 0;
            String strOptString = analysis.name;
            if (strOptString.isEmpty() && !analysis.phone.isEmpty()) {
                strOptString = PhoneData.contact(this, analysis.phone);
            }
            if (strOptString.isEmpty() && !str.isEmpty() && Analysis.phone(str).isEmpty()) {
                strOptString = str;
            }
            if (strOptString.isEmpty()) {
                strOptString = analysis.phone;
            }
            if (!z) {
                strOptString = jSONObject4.optString("name");
            }
            EditText editTextField = field(linearLayoutForm, "اسم العميل أو رقمه", strOptString, 1);
            EditText editTextField2 = field(linearLayoutForm, "الرقم السعودي", z ? analysis.phone : jSONObject4.optString("phone"), 3);
            editTextField2.setTextDirection(3);
            int i = 2;
            String[] strArr = {"غير محدد", "أجهزة المساحة", "البرامج والتطبيقات"};
            if ("survey".equals("survey")) {
                i = 1;
            } else if (!"survey".equals("software")) {
                i = 0;
            }
            final Spinner spinner = spinner(linearLayoutForm, "النشاط المقترح", strArr, i);
            spinner.setEnabled(false);
            final LinkedHashMap linkedHashMap = new LinkedHashMap();
            linkedHashMap.put("company", analysis.company);
            linkedHashMap.put("email", analysis.email);
            linkedHashMap.put("interest", analysis.interest);
            linkedHashMap.put("followUp", analysis.followUp);
            linkedHashMap.put("followTime", analysis.followTime);
            HashMap map = new HashMap();
            boolean z2 = z;
            map.put("company", "الشركة");
            map.put("email", "البريد");
            map.put("interest", "الاهتمام");
            map.put("followUp", "المتابعة");
            map.put("followTime", "الوقت");
            EditText editText = editTextField2;
            final LinkedHashMap linkedHashMap2 = new LinkedHashMap();
            Iterator it = linkedHashMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                if (!((String) entry.getValue()).isEmpty()) {
                    CheckBox checkBox = new CheckBox(this);
                    Iterator it2 = it;
                    String strOptString2 = jSONObject4.optString((String) entry.getKey());
                    EditText editText2 = editText;
                    EditText editText3 = editTextField;
                    checkBox.setText(((String) map.get(entry.getKey())) + ": " + ((String) entry.getValue()) + (strOptString2.isEmpty() ? "" : "\nالمحفوظ: " + strOptString2));
                    checkBox.setChecked((z2 || strOptString2.isEmpty()) && (!((String) entry.getKey()).equals("followUp") || ((String) entry.getValue()).compareTo(LocalDate.now().toString()) >= 0));
                    linearLayoutForm.addView(checkBox);
                    linkedHashMap2.put((String) entry.getKey(), checkBox);
                    it = it2;
                    editText = editText2;
                    editTextField = editText3;
                }
            }
            final EditText editText4 = editText;
            final EditText editText5 = editTextField;
            try {
                jSONObject3 = new JSONObject(jSONObject4.optString("requestDetails", "{}"));
            } catch (Exception unused) {
                jSONObject3 = new JSONObject();
            }
            final LinkedHashMap linkedHashMap3 = new LinkedHashMap();
            if (!analysis.details.isEmpty()) {
                linearLayoutForm.addView(text("اقتراحات تفاصيل الطلب", 19));
                for (Map.Entry<String, String> entry2 : analysis.details.entrySet()) {
                    CheckBox checkBox2 = new CheckBox(this);
                    checkBox2.setText(requestLabel(entry2.getKey()) + ": " + entry2.getValue());
                    JSONObject jSONObjectOptJSONObject = jSONObject3.optJSONObject("survey");
                    checkBox2.setChecked(jSONObjectOptJSONObject == null || jSONObjectOptJSONObject.optString(entry2.getKey()).isEmpty());
                    linearLayoutForm.addView(checkBox2);
                    linkedHashMap3.put(entry2.getKey(), checkBox2);
                }
            }
            final JSONObject jSONObject5 = jSONObject3;
            final AlertDialog[] alertDialogArr = new AlertDialog[1];
            primary(button(linearLayoutForm, "اعتماد المعلومات وحفظ العميل", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$quickPanel$58(editText4, editText5, jSONObject4, spinner, linkedHashMap2, linkedHashMap, jSONObject5, linkedHashMap3, analysis, jSONObject, alertDialogArr);
                }
            }));
            alertDialogArr[0] = panel("اعتماد المعلومات", linearLayoutForm);
        } catch (Exception unused2) {
            alert("تعذر قراءة العميل");
        }
    }

    private String read(Uri uri, int i) throws Exception {
        if (!"content".equals(uri.getScheme())) {
            throw new IllegalArgumentException("اختر ملفًا من مدير المستندات");
        }
        InputStream inputStreamOpenInputStream = getContentResolver().openInputStream(uri);
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                if (inputStreamOpenInputStream == null) {
                    throw new IOException();
                }
                byte[] bArr = new byte[4096];
                do {
                    int i2 = inputStreamOpenInputStream.read(bArr);
                    if (i2 == -1) {
                        String strDecode = TextDecoder.decode(byteArrayOutputStream.toByteArray());
                        byteArrayOutputStream.close();
                        if (inputStreamOpenInputStream != null) {
                            inputStreamOpenInputStream.close();
                        }
                        return strDecode;
                    }
                    byteArrayOutputStream.write(bArr, 0, i2);
                } while (byteArrayOutputStream.size() <= i);
                throw new IllegalArgumentException("الملف أكبر من الحد المسموح");
            } catch (Throwable th) {
                try {
                    byteArrayOutputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (Throwable th3) {
            if (inputStreamOpenInputStream != null) {
                try {
                    inputStreamOpenInputStream.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    private void receive(Intent intent) {
        long longExtra = intent.getLongExtra("openLead", 0L);
        if (longExtra > 0) {
            JSONObject jSONObjectLead = this.store.lead(longExtra);
            if (jSONObjectLead.optLong("id") > 0) {
                this.business = "survey";
                this.requestFilter = 0;
                this.tab = "today";
                render();
                edit(jSONObjectLead, 0L);
            }
            intent.removeExtra("openLead");
        }
        if ("android.intent.action.SEND".equals(intent.getAction())) {
            intent.setAction("android.intent.action.VIEW");
            String stringExtra = intent.getStringExtra("android.intent.extra.TEXT");
            final String stringExtra2 = intent.getStringExtra("android.intent.extra.SUBJECT");
            final Uri uri = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
            if (uri == null) {
                saveShared(stringExtra, stringExtra2);
            } else {
                this.backgroundOps++;
                new Thread(new Runnable() {
                    @Override
                    public final void run() {
                        MainActivity.this.lambda$receive$103(uri, stringExtra2);
                    }
                }, "shared-read").start();
            }
        }
    }

    public void render() {
        getWindow().setStatusBarColor(-788486);
        getWindow().setNavigationBarColor(-1);
        getWindow().getDecorView().setSystemUiVisibility(8208);
        LinearLayout linearLayoutColumn = column();
        this.root = linearLayoutColumn;
        linearLayoutColumn.setLayoutDirection(AppLanguage.direction(this));
        this.root.setBackgroundColor(-788486);
        this.root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return MainActivity.lambda$render$1(view, windowInsets);
            }
        });
        setContentView(this.root);
        LinearLayout linearLayoutRow = row();
        linearLayoutRow.setPadding(dp(20), dp(10), dp(20), dp(8));
        this.root.addView(linearLayoutRow);
        linearLayoutRow.addView(new GlassIcon(this, "brand"), new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout linearLayoutColumn2 = column();
        linearLayoutColumn2.setPadding(dp(10), 0, dp(8), 0);
        linearLayoutRow.addView(linearLayoutColumn2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textViewText = text(tr("تواصل", "Tawasul"), 22);
        textViewText.setTextColor(-15717821);
        textViewText.setSingleLine(true);
        linearLayoutColumn2.addView(textViewText);
        TextView textViewText2 = text(tr("إدارة العملاء", "CRM workspace"), 11);
        textViewText2.setTextColor(-10192758);
        textViewText2.setSingleLine(true);
        linearLayoutColumn2.addView(textViewText2);
        linearLayoutRow.addView(pill(tr("خاص وآمن", "Secure"), -15763858, -1574667));
        TextView textViewPill = pill("AR | EN", -15717821, -1577224);
        textViewPill.setContentDescription(tr("تغيير اللغة", "Change language"));
        textViewPill.setPadding(dp(7), dp(5), dp(7), dp(5));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(dp(5), 0, 0, 0);
        linearLayoutRow.addView(textViewPill, layoutParams);
        textViewPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                MainActivity.this.lambda$render$2(view);
            }
        });
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);
        scrollView.setVerticalScrollBarEnabled(false);
        LinearLayout linearLayoutColumn3 = column();
        this.content = linearLayoutColumn3;
        linearLayoutColumn3.setPadding(dp(20), dp(4), dp(20), dp(20));
        scrollView.addView(this.content);
        this.root.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        try {
            if (this.tab.equals("home")) {
                home();
            } else if (this.tab.equals("messages")) {
                messages();
            } else if (this.tab.equals("settings")) {
                settings();
            } else if (this.tab.equals("ads")) {
                advertising();
            } else {
                clients();
            }
        } catch (Exception unused) {
            empty("تعذر عرض البيانات", "أعد فتح التطبيق وتحقق من مساحة الهاتف.");
        }
        LinearLayout linearLayoutRow2 = row();
        linearLayoutRow2.setPadding(dp(6), dp(8), dp(6), dp(4));
        linearLayoutRow2.setBackground(UiTheme.surface(this, false, 22));
        this.root.addView(linearLayoutRow2);
        String[][] strArr = {new String[]{"home", tr("الرئيسية", "Home")}, new String[]{"clients", tr("العملاء", "Customers")}, new String[]{"today", tr("المتابعات", "Follow-ups")}, new String[]{"unassigned", tr("الوارد", "Inbox")}, new String[]{"messages", tr("الرسائل", "Messages")}, new String[]{"settings", tr("الإعدادات", "Settings")}};
        int i = 0;
        for (int i2 = 6; i < i2; i2 = 6) {
            final String[] strArr2 = strArr[i];
            boolean zEquals = this.tab.equals(strArr2[0]);
            LinearLayout linearLayoutColumn4 = column();
            linearLayoutColumn4.setGravity(17);
            linearLayoutColumn4.setPadding(0, dp(5), 0, dp(5));
            if (zEquals) {
                linearLayoutColumn4.setBackground(shape(-1575950, 14, 0));
            }
            linearLayoutRow2.addView(linearLayoutColumn4, new LinearLayout.LayoutParams(0, dp(64), 1.0f));
            linearLayoutColumn4.addView(new Glyph(this, strArr2[0], zEquals ? -15434365 : -10192758), new LinearLayout.LayoutParams(dp(23), dp(23)));
            TextView textViewText3 = text(strArr2[1], 10);
            textViewText3.setTextColor(zEquals ? -15434365 : -10192758);
            textViewText3.setGravity(17);
            linearLayoutColumn4.addView(textViewText3);
            linearLayoutColumn4.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(View view) {
                    MainActivity.this.lambda$render$3(strArr2, view);
                }
            });
            i++;
        }
    }

    private void requestCallPermissions() {
        openCallCsv();
    }

    public void requestContactNames() {
        if (checkSelfPermission("android.permission.READ_CONTACTS") != 0) {
            contactDisclosure(10, null);
        } else {
            syncContactNames(true, null);
        }
    }

    private void requestDetails(final long j) {
        JSONObject jSONObject;
        int i;
        JSONObject jSONObjectLead = this.store.lead(j);
        final String strOptString = jSONObjectLead.optString("business");
        if (strOptString.isEmpty()) {
            alert("حدد نشاط العميل واحفظه أولًا");
            return;
        }
        try {
            jSONObject = new JSONObject(jSONObjectLead.optString("requestDetails", "{}"));
        } catch (Exception unused) {
            jSONObject = new JSONObject();
        }
        final JSONObject jSONObject2 = jSONObject;
        JSONObject existingDetails = jSONObject2.optJSONObject(strOptString);
        final JSONObject jSONObjectOptJSONObject = existingDetails == null ? new JSONObject() : existingDetails;
        int i2 = 1;
        String[][] strArrFields = strOptString.equals("survey") ? SurveyRequests.fields(jSONObjectLead.optString("requestType", "sales")) : new String[][]{new String[]{"program", "البرنامج المطلوب"}, new String[]{"platform", "سحابي / مكتبي / تطبيق"}, new String[]{"needs", "احتياجات العميل"}, new String[]{"demoDate", "موعد العرض التجريبي (YYYY-MM-DD)"}, new String[]{"plan", "الباقة المطلوبة"}, new String[]{"budget", "الميزانية المتوقعة (ريال)"}, new String[]{"subscriptionEnd", "انتهاء الاشتراك (YYYY-MM-DD)"}};
        LinearLayout linearLayoutForm = form();
        linearLayoutForm.addView(text(strOptString.equals("survey") ? "طلب أجهزة المساحة" : "طلب البرامج والتطبيقات", 22));
        muted(linearLayoutForm, "تفاصيل خاصة بالنشاط المحفوظ لهذا العميل");
        if (strOptString.equals("survey")) {
            muted(linearLayoutForm, "نوع الطلب الحالي: " + SurveyRequests.label(jSONObjectLead.optString("requestType", "sales")) + " • عدله من بيانات العميل");
        }
        final LinkedHashMap linkedHashMap = new LinkedHashMap();
        int length = strArrFields.length;
        int i3 = 0;
        while (i3 < length) {
            String[] strArr = strArrFields[i3];
            if (strArr[0].equals("quantity")) {
                i = 2;
            } else if (strArr[0].equals("budget")) {
                i = 8194;
            } else {
                i = (strArr[0].equals("issue") || strArr[0].equals("workDone")) ? 131073 : i2;
            }
            String str = strArr[0];
            linkedHashMap.put(str, field(linearLayoutForm, strArr[i2], jSONObjectOptJSONObject.optString(str), i));
            i3++;
            i2 = 1;
        }
        final AlertDialog[] alertDialogArr = new AlertDialog[i2];
        final String[][] strArr2 = strArrFields;
        primary(button(linearLayoutForm, "حفظ تفاصيل الطلب", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$requestDetails$106(jSONObjectOptJSONObject, strArr2, linkedHashMap, jSONObject2, strOptString, j, alertDialogArr);
            }
        }));
        alertDialogArr[0] = panel("تفاصيل الطلب", linearLayoutForm);
    }

    private String requestLabel(String str) {
        String[] strArr = {"device", "model", "service", "condition", "quantity", "budget", "program", "platform", "needs", "plan", "purchaseDate", "calibrationDate", "demoDate", "subscriptionEnd"};
        String[] strArr2 = {"الجهاز", "الموديل", "الخدمة", "الحالة", "الكمية", "الميزانية", "البرنامج", "المنصة", "الاحتياجات", "الباقة", "موعد الشراء", "تجديد المعايرة", "العرض التجريبي", "انتهاء الاشتراك"};
        for (int i = 0; i < 14; i++) {
            if (strArr[i].equals(str)) {
                return strArr2[i];
            }
        }
        return str;
    }

    private void restoreDocument(final String str, final char[] cArr) {
        this.backgroundOps++;
        new Thread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$restoreDocument$101(cArr, str);
            }
        }, "encrypted-restore").start();
    }

    private void restoreSelected(final Uri uri) {
        this.backgroundOps++;
        new Thread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$restoreSelected$97(uri);
            }
        }, "restore-read").start();
    }

    public void lambda$messages$51(final JSONObject jSONObject) {
        final String strOptString = jSONObject.optString("body");
        LinkedHashMap<String, ArrayList<ChatText.Message>> linkedHashMap = ChatText.parse(strOptString, jSONObject.optLong("at"), false);
        if (linkedHashMap.isEmpty()) {
            chooseQuick(jSONObject, Analysis.inspect(strOptString, jSONObject.optLong("at")), "");
            return;
        }
        LinearLayout linearLayoutForm = form();
        muted(linearLayoutForm, "اختر مرسل رسائل العميل حتى لا تُخلط رسائلك أو رسائل المجموعة بمعلوماته. لا تعتمد على عنوان الملف لتحديد الهوية.");
        final String[] strArr = (String[]) linkedHashMap.keySet().toArray(new String[0]);
        final Spinner spinner = spinner(linearLayoutForm, "صاحب الرسائل المطلوب تحليلها", strArr, 0);
        final Spinner spinner2 = spinner(linearLayoutForm, "صيغة تاريخ ملف واتساب", new String[]{"يوم / شهر / سنة", "شهر / يوم / سنة"}, 0);
        new FluentDialog(this).setTitle("تحليل محادثة مصدّرة").setView(linearLayoutForm).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("استخراج المعلومات", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$review$56(strOptString, jSONObject, spinner2, strArr, spinner, dialogInterface, i);
            }
        }).show();
    }

    private LinearLayout row() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        return linearLayout;
    }

    public void lambda$tools$33(JSONObject jSONObject) {
        String str;
        String strPhone = Analysis.phone(jSONObject.optString("phone"));
        if (strPhone.isEmpty()) {
            str = "أضف رقم هاتف صحيحًا للعميل أولًا";
        } else {
            Boolean boolExists = PhoneData.exists(this, strPhone);
            if (!Boolean.TRUE.equals(boolExists)) {
                try {
                    startActivity(new Intent(boolExists == null ? "android.intent.action.INSERT_OR_EDIT" : "android.intent.action.INSERT").setType(boolExists == null ? "vnd.android.cursor.item/contact" : "vnd.android.cursor.dir/contact").putExtra("name", jSONObject.optString("name")).putExtra("phone", strPhone).putExtra("phone_type", strPhone.startsWith("+9665") ? 2 : 3).putExtra("company", jSONObject.optString("company")).putExtra("email", jSONObject.optString("email")));
                    return;
                } catch (Exception unused) {
                    alert("تعذر فتح شاشة جهات الاتصال على الهاتف");
                    return;
                }
            }
            str = "هذا الرقم موجود بالفعل في جهات اتصال الهاتف";
        }
        alert(str);
    }

    private void saveShared(String str, String str2) {
        String str3;
        if (str == null || str.trim().isEmpty()) {
            str3 = "شارك نص رسالة أو ملف محادثة نصيًا دون الوسائط";
        } else {
            if (str.length() <= 100000) {
                if (str2 == null) {
                    str2 = "";
                }
                saveSharedText(str, str2);
                return;
            }
            str3 = "المحادثة طويلة جدًا؛ شارك جزءًا أصغر (حتى 100 ألف حرف)";
        }
        alert(str3);
    }

    private void saveSharedText(String str, String str2) {
        String strOf = TextFingerprint.of("share|" + str2 + "|" + str.trim());
        this.store.message(strOf, str2, str, System.currentTimeMillis(), "نص واتساب مشترك", true);
        ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM messages WHERE fingerprint=?", strOf);
        this.tab = "messages";
        render();
        if (arrayListQuery.isEmpty() || !arrayListQuery.get(0).optString("state").equals("pending")) {
            alert("هذا النص سبق حفظه ومراجعته");
        } else {
            lambda$messages$51(arrayListQuery.get(0));
        }
    }

    private void settings() {
        UiTheme.header(this.content, tr("الإعدادات", "Settings"), "settings");
        button(this.content, tr("اللغة: العربية", "Language: English"), new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.chooseLanguage();
            }
        });
        primary(button(this.content, "الدفاتر والعهد والمصروفات", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$settings$60();
            }
        }));
        button(this.content, "التقارير والإحصاءات", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$settings$61();
            }
        });
        button(this.content, "تصدير البيانات CSV", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.chooseCsvExport();
            }
        });
        button(this.content, "إدارة المحتوى التسويقي", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$settings$62();
            }
        });
        primary(button(this.content, "بيانات المنشأة ونوع نشاطها", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$settings$63();
            }
        }));
        LinearLayout linearLayout = this.content;
        LinearLayout linearLayoutCard = card(linearLayout);
        this.content = linearLayoutCard;
        muted(linearLayoutCard, "عام وخصوصية");
        this.content.addView(text("بياناتك محفوظة على الهاتف. هذا التطبيق مستقل؛ لا يحتاج موقعًا أو اشتراك API، ولا يرسل العملاء أو الرسائل إلى الإنترنت.", 16));
        final SharedPreferences sharedPreferences = getSharedPreferences("settings", 0);
        Switch r5 = new Switch(this);
        r5.setText(tr("أصوات تفاعل خفيفة (اختيارية)", "Subtle interaction sounds (optional)"));
        r5.setChecked(sharedPreferences.getBoolean("interactionSounds", false));
        r5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                sharedPreferences.edit().putBoolean("interactionSounds", z).apply();
            }
        });
        this.content.addView(r5);
        Switch secure = new Switch(this);
        secure.setText("منع لقطات الشاشة والمعاينة في التطبيقات الأخيرة");
        secure.setChecked(sharedPreferences.getBoolean("secureScreen", false));
        secure.setOnCheckedChangeListener((compoundButton, on) -> {
            sharedPreferences.edit().putBoolean("secureScreen", on).apply();
            if (on) {
                getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
            } else {
                getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
            }
        });
        this.content.addView(secure);
        Switch lock = new Switch(this);
        lock.setText("طلب قفل الهاتف عند فتح التطبيق");
        lock.setChecked(sharedPreferences.getBoolean("appLock", false));
        lock.setOnCheckedChangeListener((compoundButton, on) -> {
            if (on && !getSystemService(android.app.KeyguardManager.class).isDeviceSecure()) {
                compoundButton.setChecked(false);
                alert("فعّل رمز قفل أو بصمة للهاتف أولًا، ثم أعد المحاولة.");
                return;
            }
            sharedPreferences.edit().putBoolean("appLock", on).apply();
            markUnlocked();
        });
        this.content.addView(lock);
        button(this.content, "إضافة اختصار للشاشة الرئيسية", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$settings$65();
            }
        });
        button(this.content, "تحديث الأسماء من جهات الاتصال", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.requestContactNames();
            }
        });
        button(this.content, "استيراد دليل جهات الاتصال من ملف VCF", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.openContactVcf();
            }
        });
        button(this.content, "سياسة الخصوصية داخل التطبيق", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.privacyPolicy();
            }
        });
        button(this.content, "فتح سياسة الخصوصية على الويب", this::openPrivacyPolicy);
        LinearLayout linearLayoutCard2 = card(linearLayout);
        this.content = linearLayoutCard2;
        linearLayoutCard2.addView(text("تنبيهات المتابعة", 18));
        final SharedPreferences sharedPreferences2 = getSharedPreferences("settings", 0);
        Switch r2 = new Switch(this);
        r2.setText("تفعيل تذكيرات المتابعة");
        r2.setChecked(sharedPreferences2.getBoolean("reminders", true));
        r2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MainActivity.this.lambda$settings$66(sharedPreferences2, compoundButton, z);
            }
        });
        this.content.addView(r2);
        primary(button(this.content, "تفعيل إشعارات المتابعة", this::enableNotifications));
        button(this.content, "إرسال إشعار تجريبي", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$settings$67();
            }
        });
        button(this.content, "إعدادات إشعارات التطبيق", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$settings$68();
            }
        });
        muted(this.content, Reminders.allowed(this) ? "إشعارات التطبيق مسموحة" : "الإشعارات غير مسموحة حاليًا — امنح الإذن لإظهار التذكير");
        muted(this.content, "حدد تاريخ المتابعة ووقت التنبيه في بيانات العميل لأي نوع طلب. التذكير تقريبي وقد يتأخر مع توفير الطاقة؛ لا تُنبه الطلبات المكتملة أو المغلقة.");
        LinearLayout linearLayoutCard3 = card(linearLayout);
        this.content = linearLayoutCard3;
        linearLayoutCard3.addView(text("المكالمات وجهات الاتصال", 18));
        muted(this.content, "استورد ملف CSV صادرًا من تطبيق الهاتف أو أداة النسخ الاحتياطي. تُدمج الأرقام المتكررة وتُقبل العناوين العربية والإنجليزية والفواصل المختلفة.");
        primary(button(this.content, "اختيار ملف سجل المكالمات CSV", this::chooseCallCsv));
        muted(this.content, "هذه نسخة المتجر: لا تطلب إذن سجل المكالمات المقيد، بما يتوافق مع سياسة Google Play.");
        LinearLayout linearLayoutCard4 = card(linearLayout);
        this.content = linearLayoutCard4;
        linearLayoutCard4.addView(text("رسائل واتساب", 18));
        this.content.addView(text("واتساب: المشاركة اليدوية فقط. لا يطلب هذا الإصدار الوصول إلى الإشعارات ولا يقرأها في الخلفية. شارك نص الرسالة أو ملف TXT إلى عملائي ثم راجع المعلومات قبل الحفظ.", 16));
        LinearLayout linearLayoutCard5 = card(linearLayout);
        this.content = linearLayoutCard5;
        linearLayoutCard5.addView(text("النسخ الاحتياطي", 18));
        primary(button(this.content, "إنشاء نسخة احتياطية مشفرة", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.chooseBackupPassword();
            }
        }));
        button(this.content, "استعادة نسخة احتياطية", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$settings$75();
            }
        });
        this.content.addView(text("النسخ الجديدة محمية بكلمة مرور وتشفير AES‑256‑GCM. لا يمكن استعادة كلمة المرور إذا فُقدت.", 14));
        this.content = linearLayout;
        linearLayout.addView(text(sharedPreferences.getString("status", ""), 16));
        this.content.addView(text("التحليل محلي بالكلمات والصيغ: اقتراح اهتمام ونشاط وموعد وبريد ورقم. ليس تحليلًا سحابيًا بالذكاء الاصطناعي. البحث الخارجي عن معلومات الأرقام غير مفعل.", 14));
    }

    private GradientDrawable shape(int i, int i2, int i3) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(i2));
        if (i3 != 0) {
            gradientDrawable.setStroke(dp(1), i3);
        }
        return gradientDrawable;
    }

    public void lambda$advertising$113(JSONObject jSONObject) {
        startActivity(new Intent(this, (Class<?>) PostSharingActivity.class).putExtra("postId", jSONObject.optLong("id")));
    }

    private void showPostImage(LinearLayout linearLayout, String str) {
        if (str.isEmpty()) {
            return;
        }
        Bitmap bitmapThumbnail = PostMedia.thumbnail(this, str);
        if (bitmapThumbnail == null) {
            muted(linearLayout, "الصورة غير متاحة؛ أعد إضافتها");
            return;
        }
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmapThumbnail);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setContentDescription("صورة المنشور");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(180));
        layoutParams.setMargins(0, dp(12), 0, dp(8));
        imageView.setBackground(UiTheme.surface(this, false, 16));
        linearLayout.addView(imageView, layoutParams);
    }

    private Spinner spinner(LinearLayout linearLayout, String str, String[] strArr, int i) {
        muted(linearLayout, str);
        Spinner spinner = new Spinner(this);
        spinner.setPadding(dp(12), dp(6), dp(12), dp(6));
        spinner.setBackground(shape(-788486, 12, -2299923));
        spinner.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, strArr));
        spinner.setSelection(i);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(52));
        layoutParams.setMargins(0, 0, 0, dp(12));
        linearLayout.addView(spinner, layoutParams);
        return spinner;
    }

    private String[] stages(String str) {
        return SurveyRequests.stages(str, "sales");
    }

    private String[] stages(String str, String str2) {
        return SurveyRequests.stages(str, str2);
    }

    private GradientDrawable surface(int i) {
        return shape(i, 20, 0);
    }

    private void switchLanguage() {
        AppLanguage.set(this, !AppLanguage.english(this));
        Intent intent = new Intent(this, (Class<?>) MainActivity.class);
        intent.addFlags(268468224);
        startActivity(intent);
        finish();
    }

    private void syncContactNames(final boolean z, final Runnable runnable) {
        this.backgroundOps++;
        new Thread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$syncContactNames$80(z, runnable);
            }
        }, "contact-name-sync").start();
    }

    private void syncHistory(final boolean z) {
        if (this.importing) {
            return;
        }
        this.importing = true;
        getSharedPreferences("settings", 0).edit().putLong("lastSync", System.currentTimeMillis()).apply();
        History.status(this, "جارٍ إنشاء العملاء من سجل المكالمات…");
        this.backgroundOps++;
        new Thread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$syncHistory$86(z);
            }
        }, "history").start();
    }

    private void templates(final long j) {
        final JSONObject jSONObjectLead = this.store.lead(j);
        String strOptString = jSONObjectLead.optString("interest");
        if (strOptString.isEmpty()) {
            strOptString = jSONObjectLead.optString("business").equals("software") ? "البرنامج المطلوب" : "الجهاز أو الخدمة المطلوبة";
        }
        String str = "السلام عليكم " + jSONObjectLead.optString("name") + "،\n";
        String[] strArr = (!jSONObjectLead.optString("business").equals("survey") || jSONObjectLead.optString("requestType", "sales").equals("sales")) ? new String[]{"متابعة الاهتمام", "متابعة عرض السعر", "تأكيد موعد", "متابعة بعد البيع"} : new String[]{"متابعة طلب الخدمة", "متابعة تفاصيل الطلب", "تأكيد موعد", "متابعة بعد إتمام الخدمة"};
        String[] strArr2 = new String[4];
        strArr2[0] = str + "يسعدنا متابعة طلبك بخصوص " + strOptString + ". هل توجد تفاصيل إضافية أو استفسارات يمكننا مساعدتك بها؟";
        strArr2[1] = str + "نود متابعة عرض السعر الخاص بـ " + strOptString + ". هل اطلعت عليه، وهل ترغب في توضيح أي بند؟";
        strArr2[2] = str + "نود تأكيد موعد المتابعة " + (jSONObjectLead.optString("followUp").isEmpty() ? "[اكتب التاريخ والوقت]" : jSONObjectLead.optString("followUp") + " " + jSONObjectLead.optString("followTime", "09:00")) + " بخصوص " + strOptString + ". هل الموعد مناسب لك؟";
        strArr2[3] = str + "نتمنى أن تكون تجربتك مع " + strOptString + " جيدة. هل تحتاج أي مساعدة أو متابعة إضافية؟";
        if (jSONObjectLead.optString("business").equals("survey") && !jSONObjectLead.optString("requestType", "sales").equals("sales")) {
            strArr2[1] = str + "نتابع طلب " + SurveyRequests.label(jSONObjectLead.optString("requestType")) + " بخصوص " + strOptString + ". هل توجد تفاصيل إضافية عن الجهاز أو المشكلة؟";
        }
        ArrayList arrayList = new ArrayList();
        final ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < strArr.length; i++) {
            arrayList.add(strArr[i]);
            arrayList2.add(strArr2[i]);
        }
        JSONArray jSONArrayCustom = TemplateStore.custom(this.store.profile("survey"));
        for (int i2 = 0; i2 < jSONArrayCustom.length(); i2++) {
            JSONObject jSONObjectOptJSONObject = jSONArrayCustom.optJSONObject(i2);
            if (jSONObjectOptJSONObject != null) {
                arrayList.add(jSONObjectOptJSONObject.optString("title", "قالب مخصص"));
                arrayList2.add(jSONObjectOptJSONObject.optString("body"));
            }
        }
        int size = arrayList.size();
        final String[] strArr3 = new String[size + 1];
        for (int i3 = 0; i3 < arrayList.size(); i3++) {
            strArr3[i3] = (String) arrayList.get(i3);
        }
        strArr3[size] = "إدارة القوالب المخصصة";
        new FluentDialog(this).setTitle("اختر قالب رسالة").setItems(strArr3, new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i4) {
                MainActivity.this.lambda$templates$109(strArr3, arrayList2, jSONObjectLead, j, dialogInterface, i4);
            }
        }).show();
    }

    private TextView text(String str, int i) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTextColor(-15189940);
        textView.setTypeface(this.cairo);
        textView.setFontVariationSettings("'wght' " + (i >= 19 ? 650 : 450));
        textView.setFontFeatureSettings("kern");
        textView.setGravity(8388611);
        textView.setPadding(0, dp(4), 0, dp(4));
        return textView;
    }

    private void timeline(final long j) {
        JSONObject jSONObjectLead = this.store.lead(j);
        LinearLayout linearLayoutForm = form();
        linearLayoutForm.addView(text(jSONObjectLead.optString("name"), 22));
        muted(linearLayoutForm, "المكالمات المسجلة والملاحظات والتغييرات — أحدث 200 تعامل");
        final EditText editTextField = field(linearLayoutForm, "أضف ملاحظة للتعامل", "", 131073);
        final AlertDialog[] alertDialogArr = new AlertDialog[1];
        primary(button(linearLayoutForm, "حفظ الملاحظة", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$timeline$105(editTextField, j, alertDialogArr);
            }
        }));
        ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM events WHERE leadId=? ORDER BY at DESC,id DESC LIMIT 200", String.valueOf(j));
        if (arrayListQuery.isEmpty()) {
            muted(linearLayoutForm, "لا يوجد سجل تعاملات بعد. التحديث لا يستنتج أحداثًا قديمة لم تُسجّل.");
        }
        for (JSONObject jSONObject : arrayListQuery) {
            LinearLayout linearLayoutCard = card(linearLayoutForm);
            muted(linearLayoutCard, DateFormat.format("yyyy-MM-dd HH:mm", jSONObject.optLong("at")).toString());
            TextView textViewText = text(jSONObject.optString("body"), 15);
            textViewText.setTextIsSelectable(true);
            linearLayoutCard.addView(textViewText);
        }
        alertDialogArr[0] = panel("سجل التعاملات", linearLayoutForm);
    }

    private void tools(LinearLayout linearLayout, final JSONObject jSONObject, final AlertDialog alertDialog) {
        linearLayout.addView(text("ملف العميل", 20));
        if (!jSONObject.optString("phone").isEmpty() && !jSONObject.optString("phone").equals("null")) {
            button(linearLayout, "حفظ في جهات اتصال الهاتف", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$tools$33(jSONObject);
                }
            });
        }
        if (jSONObject.optString("business").equals("software")) {
            button(linearLayout, "اشتراكات وطلبات هذا العميل", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$tools$34(jSONObject);
                }
            });
        }
        button(linearLayout, "سجل التعاملات والملاحظات", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$tools$35(jSONObject);
            }
        });
        button(linearLayout, "تفاصيل طلب العميل", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$tools$36(jSONObject);
            }
        });
        button(linearLayout, "قوالب رسائل واتساب", new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$tools$37(jSONObject);
            }
        });
        if (!jSONObject.optString("followUp").isEmpty()) {
            button(linearLayout, "تمت المتابعة", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$tools$38(alertDialog, jSONObject);
                }
            });
            button(linearLayout, "تأجيل المتابعة إلى الغد", new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$tools$39(alertDialog, jSONObject);
                }
            });
        }
        muted(linearLayout, "هذه الإجراءات تخص البيانات المحفوظة؛ احفظ تعديلات النموذج أولًا.");
    }

    private String tr(String str, String str2) {
        return AppLanguage.text(this, str, str2);
    }

    private void writeCsv(final Uri uri) {
        final String str = this.pendingCsv;
        this.pendingCsv = null;
        if (str == null) {
            return;
        }
        this.backgroundOps++;
        new Thread(new Runnable() {
            @Override
            public final void run() {
                MainActivity.this.lambda$writeCsv$92(str, uri);
            }
        }, "csv-export").start();
    }

    private void writeEncryptedBackup(final Uri uri) {
        final char[] cArr = this.pendingBackupPassword;
        this.pendingBackupPassword = null;
        if (cArr == null) {
            alert("أعد إنشاء النسخة وحدد كلمة مرور");
        } else {
            this.backgroundOps++;
            new Thread(new Runnable() {
                @Override
                public final void run() {
                    MainActivity.this.lambda$writeEncryptedBackup$94(cArr, uri);
                }
            }, "encrypted-backup").start();
        }
    }

    @Override
    protected void onActivityResult(int i, int i2, Intent intent) {
        char[] cArr;
        super.onActivityResult(i, i2, intent);
        if (i == 40) {
            if (i2 == -1) {
                render();
            }
            return;
        }
        if (i == 20 && i2 != -1 && (cArr = this.pendingBackupPassword) != null) {
            Arrays.fill(cArr, (char) 0);
            this.pendingBackupPassword = null;
            return;
        }
        if (i == 30) {
            if (this.postDraft == null) {
                return;
            }
            if (i2 == -1 && intent != null && intent.getData() != null) {
                try {
                    this.postDraft.put("image", PostMedia.importUri(this, intent.getData()));
                } catch (Exception e) {
                    alert(e.getMessage() == null ? "تعذر إضافة الصورة" : e.getMessage());
                }
            }
            lambda$advertising$114(this.postDraft);
            return;
        }
        if (i == 11 && getSharedPreferences("settings", 0).getBoolean("setupPending", false)) {
            getSharedPreferences("settings", 0).edit().putBoolean("setupPending", false).apply();
            requestCallPermissions();
            return;
        }
        if (i2 != -1 || intent == null || intent.getData() == null) {
            return;
        }
        if (i == 31) {
            importCallCsv(intent.getData());
            return;
        }
        if (i == 32) {
            importContactVcf(intent.getData());
            return;
        }
        try {
            if (i == 20) {
                writeEncryptedBackup(intent.getData());
            } else if (i == 41) {
                writeCsv(intent.getData());
            } else if (i == 21) {
                restoreSelected(intent.getData());
            }
        } catch (Exception e2) {
            alert("تعذر حفظ أو استعادة النسخة: " + (e2.getMessage() == null ? "تحقق من الملف والمساحة" : e2.getMessage()) + ". عند فشل الاستعادة لا تُستبدل قاعدة البيانات جزئيًا.");
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AppLanguage.apply(this);
        this.cairo = Typeface.createFromAsset(getAssets(), "fonts/Cairo.ttf");
        this.store = new Store(this);
        if (bundle != null) {
            this.business = "survey";
            this.tab = bundle.getString("tab", "home");
            this.followDate = bundle.getString("followDate", "");
            this.customerStageFilter = bundle.getInt("customerStageFilter", 0);
        }
        this.ready = true;
        render();
        receive(getIntent());
        if (!getSharedPreferences("settings", 0).getBoolean("notificationsSetup19", false)) {
            getSharedPreferences("settings", 0).edit().putBoolean("notificationsSetup19", true).apply();
            enableNotifications();
        }
        if (bundle == null || !bundle.containsKey("postDraft")) {
            return;
        }
        try {
            lambda$advertising$114(new JSONObject(bundle.getString("postDraft")));
        } catch (Exception unused) {
        }
    }

    @Override
    protected void onDestroy() {
        char[] cArr = this.pendingBackupPassword;
        if (cArr != null) {
            Arrays.fill(cArr, (char) 0);
        }
        if (this.backgroundOps == 0) {
            this.store.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        receive(intent);
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        AppLanguage.watch(this);
    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 13) {
            Reminders.channel(this);
            Reminders.deliver(this);
            Reminders.schedule(this);
            render();
            if (iArr.length == 0 || iArr[0] != 0) {
                alert("لم يُمنح إذن الإشعارات. تبقى المتابعات ظاهرة في قائمة اليوم.");
            }
        }
        if (i == 12) {
            if (checkSelfPermission("android.permission.READ_CALL_LOG") == 0) {
                importHistory();
            } else {
                alert("لم يُمنح إذن السجل. يمكنك إضافة العملاء يدويًا واستقبال المكالمات الجديدة عند تفعيل دور تحديد المتصل.");
            }
        }
        if (i == 10) {
            if (checkSelfPermission("android.permission.READ_CONTACTS") == 0) {
                syncContactNames(true, null);
            } else {
                alert("لم يُمنح إذن جهات الاتصال؛ لن تظهر الأسماء المسجلة على الهاتف.");
            }
        }
        if (i == 14) {
            if (checkSelfPermission("android.permission.READ_CONTACTS") == 0) {
                syncContactNames(false, this::openCallCsv);
            } else {
                alert("سيُستورد الملف دون أسماء الهاتف لأن إذن جهات الاتصال لم يُمنح.");
                openCallCsv();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.ready && isUnlocked()) {
            render();
            Reminders.schedule(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        if (this.postDraft != null) {
            capturePost();
            bundle.putString("postDraft", this.postDraft.toString());
        }
        bundle.putString("followDate", this.followDate);
        bundle.putInt("customerStageFilter", this.customerStageFilter);
        bundle.putString("business", this.business);
        bundle.putString("tab", this.tab);
        super.onSaveInstanceState(bundle);
    }

    String visualSection() {
        return this.tab.equals("unassigned") ? "messages" : this.tab;
    }
}
