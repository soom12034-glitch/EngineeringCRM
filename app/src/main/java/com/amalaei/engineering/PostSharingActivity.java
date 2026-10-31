package com.amalaei.engineering;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public final class PostSharingActivity extends BaseSecureActivity {
    private LinearLayout body;
    private JSONObject post;
    private long postId;
    private Store store;
    private String pendingTarget = "";
    private String pendingPlan = "";

    private void button(String str, final Runnable runnable) {
        Button button = new Button(this);
        button.setText(str);
        UiTheme.button(button, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, dp(4), 0, dp(4));
        this.body.addView(button, layoutParams);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                PostSharingActivity.lambda$button$0(runnable, view);
            }
        });
    }

    private void complete(boolean z) {
        try {
            if (this.pendingPlan.isEmpty()) {
                PostSharing.record(this, this.postId, this.pendingTarget, z ? "confirmed" : "skipped");
            } else {
                PostSharing.finish(this, this.pendingPlan, z);
            }
            this.pendingTarget = "";
            this.pendingPlan = "";
            getSharedPreferences("post-sharing", 0).edit().remove("pending-target-" + this.postId).remove("pending-plan-" + this.postId).commit();
            render();
        } catch (Exception e) {
            error(e.toString());
        }
    }

    public void configure() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20), 0, dp(20), 0);
        final CheckBox checkBox = new CheckBox(this);
        final CheckBox checkBox2 = new CheckBox(this);
        checkBox.setText("Facebook");
        checkBox2.setText("WhatsApp Business");
        linearLayout.addView(checkBox);
        linearLayout.addView(checkBox2);
        final EditText editText = new EditText(this);
        final EditText editText2 = new EditText(this);
        final EditText editText3 = new EditText(this);
        editText.setHint(tr("عدد المواعيد لكل منصة (1–100)", "Reminders per platform (1–100)"));
        editText2.setHint(tr("الفاصل بالدقائق (15–43200)", "Interval in minutes (15–43200)"));
        editText3.setHint(tr("البدء بعد دقائق (0–43200)", "Start after minutes (0–43200)"));
        EditText[] editTextArr = {editText, editText2, editText3};
        for (int i = 0; i < 3; i++) {
            EditText editText4 = editTextArr[i];
            editText4.setInputType(2);
            linearLayout.addView(editText4);
        }
        editText.setText("3");
        editText2.setText("60");
        editText3.setText("0");
        final AlertDialog alertDialogCreate = new FluentDialog(this).setTitle(tr("جدولة تذكيرات المشاركة", "Schedule sharing reminders")).setView(linearLayout).setNegativeButton(tr("إلغاء", "Cancel"), (DialogInterface.OnClickListener) null).setPositiveButton(tr("حفظ", "Save"), (DialogInterface.OnClickListener) null).create();
        alertDialogCreate.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public final void onShow(DialogInterface dialogInterface) {
                PostSharingActivity.this.lambda$configure$12(alertDialogCreate, editText, editText2, editText3, checkBox, checkBox2, dialogInterface);
            }
        });
        alertDialogCreate.show();
    }

    private String date(long j) {
        return DateFormat.format("yyyy-MM-dd HH:mm", j).toString();
    }

    private int dp(int i) {
        return Math.round(i * getResources().getDisplayMetrics().density);
    }

    private void error(String str) {
        new FluentDialog(this).setMessage(str).setPositiveButton(tr("حسنًا", "OK"), (DialogInterface.OnClickListener) null).show();
    }

    private void history() {
        String str;
        String str2;
        JSONArray jSONArray;
        int i;
        long jOptLong;
        text(tr("سجل المشاركة — تأكيد المستخدم", "Sharing log — user confirmed"), 20);
        try {
            int i2 = 0;
            JSONArray jSONArray2 = new JSONArray(getSharedPreferences("post-sharing", 0).getString("history", "[]"));
            int i3 = 2;
            boolean z = true;
            String[] strArr = {"com.facebook.katana", "com.whatsapp.w4b"};
            int i4 = 0;
            while (true) {
                String str3 = "opened";
                if (i4 >= i3) {
                    break;
                }
                String str4 = strArr[i4];
                int i5 = i2;
                int i6 = i5;
                int i7 = i6;
                while (i6 < jSONArray2.length()) {
                    JSONObject jSONObject = jSONArray2.getJSONObject(i6);
                    boolean z2 = z;
                    String str5 = str3;
                    if (jSONObject.optLong("post") == this.postId && str4.equals(jSONObject.optString("target"))) {
                        str3 = str5;
                        if (str3.equals(jSONObject.optString("status"))) {
                            i7++;
                        }
                        if ("confirmed".equals(jSONObject.optString("status"))) {
                            i5++;
                        }
                    } else {
                        str3 = str5;
                    }
                    i6++;
                    z = z2;
                }
                text(PostSharing.label(str4) + " • " + tr("فتح ", "Opened ") + i7 + " • " + tr("تأكيد ", "Confirmed ") + i5, 15);
                i4++;
                z = z;
                i3 = 2;
                i2 = 0;
            }
            int length = jSONArray2.length() - 1;
            int i8 = 0;
            while (length >= 0 && i8 < 50) {
                JSONObject jSONObject2 = jSONArray2.getJSONObject(length);
                if (jSONObject2.optLong("post") != this.postId) {
                    jSONArray = jSONArray2;
                    i = length;
                } else {
                    String strOptString = jSONObject2.optString("status");
                    if ("confirmed".equals(strOptString)) {
                        str = "تم بتأكيدك";
                        str2 = "Confirmed by you";
                    } else if ("opened".equals(strOptString)) {
                        str = "فتح المشاركة فقط";
                        str2 = "Share opened only";
                    } else {
                        str = "تم التخطي";
                        str2 = "Skipped";
                    }
                    String strTr = tr(str, str2);
                    int i9 = length - 1;
                    while (true) {
                        if (i9 < 0) {
                            jSONArray = jSONArray2;
                            i = length;
                            jOptLong = 0;
                            break;
                        }
                        JSONObject jSONObject3 = jSONArray2.getJSONObject(i9);
                        jSONArray = jSONArray2;
                        i = length;
                        if (jSONObject3.optLong("post") == this.postId && jSONObject3.optString("target").equals(jSONObject2.optString("target")) && strOptString.equals(jSONObject3.optString("status"))) {
                            jOptLong = jSONObject3.optLong("at");
                            break;
                        } else {
                            i9--;
                            jSONArray2 = jSONArray;
                            length = i;
                        }
                    }
                    long j = jOptLong;
                    text(date(jSONObject2.optLong("at")) + " • " + PostSharing.label(jSONObject2.optString("target")) + " • " + strTr + (j == 0 ? "" : " • " + tr("الفاصل ", "Gap ") + ((jSONObject2.optLong("at") - j) / 60000) + tr(" دقيقة", " min")), 13);
                    i8++;
                }
                length = i - 1;
                jSONArray2 = jSONArray;
            }
            text(tr("يُحفظ أحدث 2000 حدث محليًا؛ السجل لا يثبت الإرسال ولا يعرف اسم المجموعة. لا يدخل في النسخة الاحتياطية الحالية.", "Latest 2000 events are stored locally. The log does not verify delivery or identify groups. It is not included in the current backup."), 12);
        } catch (Exception unused) {
            error(tr("تعذر قراءة السجل", "Unable to read history"));
        }
    }

    static /* synthetic */ void lambda$button$0(Runnable runnable, View view) {
        UiTheme.feedback(view);
        runnable.run();
    }

    public /* synthetic */ void lambda$configure$11(EditText editText, EditText editText2, EditText editText3, CheckBox checkBox, CheckBox checkBox2, AlertDialog alertDialog, View view) {
        final PostSharingActivity postSharingActivity = this;
        PostSharingActivity postSharingActivity2;
        try {
            int i = Integer.parseInt(editText.getText().toString());
            int i2 = Integer.parseInt(editText2.getText().toString());
            int i3 = Integer.parseInt(editText3.getText().toString());
            try {
                if ((!checkBox.isChecked() && !checkBox2.isChecked()) || i < 1 || i > 100 || i2 < 15 || i2 > 43200 || i3 < 0 || i3 > 43200) {
                    throw new IllegalArgumentException();
                }
                long jCurrentTimeMillis = System.currentTimeMillis() + (((long) i3) * 60000);
                String[] strArr = {"com.facebook.katana", "com.whatsapp.w4b"};
                int i4 = 0;
                while (i4 < 2) {
                    String str = strArr[i4];
                    if (str.equals("com.facebook.katana")) {
                        if (checkBox.isChecked()) {
                            postSharingActivity2 = this;
                            PostSharing.replace(postSharingActivity2, this.postId, str, i, i2, jCurrentTimeMillis);
                        } else {
                            postSharingActivity2 = this;
                        }
                    } else if (checkBox2.isChecked()) {
                        postSharingActivity2 = this;
                        PostSharing.replace(postSharingActivity2, this.postId, str, i, i2, jCurrentTimeMillis);
                    } else {
                        postSharingActivity2 = this;
                    }
                    i4++;
                }
                PostSharingActivity postSharingActivity3 = this;
                alertDialog.dismiss();
                PostSharing.deliver(postSharingActivity3);
                postSharingActivity3.render();
                return;
            } catch (Exception unused) {
            }
        } catch (Exception unused2) {
        }
        postSharingActivity.error(postSharingActivity.tr("اختر منصة وأدخل الأرقام ضمن الحدود الموضحة", "Select a platform and enter numbers within the shown limits"));
    }

    public /* synthetic */ void lambda$configure$12(final AlertDialog alertDialog, final EditText editText, final EditText editText2, final EditText editText3, final CheckBox checkBox, final CheckBox checkBox2, DialogInterface dialogInterface) {
        alertDialog.getButton(-1).setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                PostSharingActivity.this.lambda$configure$11(editText, editText2, editText3, checkBox, checkBox2, alertDialog, view);
            }
        });
    }

    public /* synthetic */ void lambda$render$1() {
        if (Build.VERSION.SDK_INT < 33 || checkSelfPermission("android.permission.POST_NOTIFICATIONS") == 0) {
            startActivity(new Intent("android.settings.CHANNEL_NOTIFICATION_SETTINGS").putExtra("android.provider.extra.APP_PACKAGE", getPackageName()).putExtra("android.provider.extra.CHANNEL_ID", "post-sharing"));
        } else {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 81);
        }
    }

    static /* synthetic */ WindowInsets lambda$render$10(View view, WindowInsets windowInsets) {
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

    public /* synthetic */ void lambda$render$2() {
        ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("Post", this.post.optString("body")));
        Toast.makeText(this, tr("تم نسخ النص", "Caption copied"), 0).show();
    }

    public /* synthetic */ void lambda$render$3() {
        complete(true);
    }

    public /* synthetic */ void lambda$render$4() {
        complete(false);
    }

    public /* synthetic */ void lambda$render$5(String str) {
        lambda$render$7(str, "");
    }

    public /* synthetic */ void lambda$render$6() {
        lambda$render$7("other", "");
    }

    public /* synthetic */ void lambda$render$8(String str) {
        try {
            PostSharing.finish(this, str, false);
            render();
        } catch (Exception e) {
            error(e.toString());
        }
    }

    public /* synthetic */ void lambda$render$9(String str) {
        try {
            PostSharing.stop(this, str);
            render();
        } catch (Exception e) {
            error(e.toString());
        }
    }

    public void lambda$render$7(String str, String str2) {
        if (!this.pendingTarget.isEmpty()) {
            error(tr("أكد المشاركة السابقة أو تخطّها أولًا", "Confirm or skip the previous share first"));
            return;
        }
        try {
            Intent intentPutExtra = new Intent("android.intent.action.SEND").putExtra("android.intent.extra.TEXT", this.post.optString("body")).putExtra("android.intent.extra.TITLE", this.post.optString("title"));
            if (!"other".equals(str)) {
                intentPutExtra.setPackage(str);
            }
            String strOptString = this.post.optString("image");
            if (strOptString.isEmpty()) {
                intentPutExtra.setType("text/plain");
            } else {
                if (!PostMedia.file(this, strOptString).isFile()) {
                    throw new IOException();
                }
                Uri uriBuild = new Uri.Builder().scheme("content").authority(getPackageName() + ".images").appendPath(strOptString).build();
                intentPutExtra.setType(PostMedia.mime(strOptString)).putExtra("android.intent.extra.STREAM", uriBuild);
                intentPutExtra.setClipData(ClipData.newUri(getContentResolver(), "Post", uriBuild));
                intentPutExtra.addFlags(1);
            }
            if ("other".equals(str)) {
                intentPutExtra = Intent.createChooser(intentPutExtra, tr("اختر تطبيق المشاركة", "Choose sharing app"));
            }
            startActivity(intentPutExtra);
            this.pendingTarget = str;
            this.pendingPlan = str2;
            PostSharing.record(this, this.postId, str, "opened");
            this.store.preparedPost(this.postId);
        } catch (Exception unused) {
            error(tr("تعذر فتح التطبيق. تأكد من تثبيت Facebook أو WhatsApp Business ومن توفر الصورة. قد تحتاج إلى لصق النص يدويًا في فيسبوك.", "Unable to open app. Check Facebook or WhatsApp Business is installed and the image exists. Facebook may require pasting the caption manually."));
        }
    }

    private void render() {
        JSONObject jSONObjectPost = this.store.post(this.postId);
        this.post = jSONObjectPost;
        if (jSONObjectPost.optLong("id") == 0) {
            error(tr("المنشور لم يعد موجودًا", "Post no longer exists"));
            finish();
            return;
        }
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        this.body = linearLayout;
        linearLayout.setOrientation(1);
        this.body.setLayoutDirection(AppLanguage.direction(this));
        this.body.setPadding(dp(20), dp(24), dp(20), dp(24));
        this.body.setBackgroundColor(-657414);
        scrollView.addView(this.body);
        setContentView(scrollView);
        UiTheme.header(this.body, tr("مشاركة وجدولة المنشور", "Post sharing & reminders"), "ads");
        text(this.post.optString("title"), 19);
        text(tr("التذكير لا ينشر تلقائيًا. اختر المجموعة أو المستلم داخل التطبيق وأتم الإرسال بنفسك. العدد هو عدد مواعيد المشاركة لكل منصة؛ ويُسجّل الإتمام بناءً على تأكيدك فقط. قد يتأخر التنبيه بسبب توفير البطارية.", "Reminders do not post automatically. Choose the group or recipient and send in the target app. Count means reminders per platform; completion is based only on your confirmation. Battery saving may delay notifications."), 14);
        if (!PostSharing.allowed(this)) {
            button(tr("تفعيل إشعارات المشاركة", "Enable sharing notifications"), new Runnable() {
                @Override
                public final void run() {
                    PostSharingActivity.this.lambda$render$1();
                }
            });
        }
        button(tr("جدولة جديدة", "New reminder schedule"), new Runnable() {
            @Override
            public final void run() {
                PostSharingActivity.this.configure();
            }
        });
        button(tr("نسخ نص المنشور — للصقه في فيسبوك", "Copy caption — paste it in Facebook"), new Runnable() {
            @Override
            public final void run() {
                PostSharingActivity.this.lambda$render$2();
            }
        });
        if (!this.pendingTarget.isEmpty()) {
            text(tr("هل أتممت المشاركة في التطبيق؟", "Did you complete sharing in the app?"), 17);
            button(tr("أؤكد إتمام المشاركة", "Confirm I shared"), new Runnable() {
                @Override
                public final void run() {
                    PostSharingActivity.this.lambda$render$3();
                }
            });
            button(tr("لم أرسل — تخطي", "Not sent — skip"), new Runnable() {
                @Override
                public final void run() {
                    PostSharingActivity.this.lambda$render$4();
                }
            });
        }
        String[] strArr = {"com.facebook.katana", "com.whatsapp.w4b"};
        for (int i = 0; i < 2; i++) {
            final String str = strArr[i];
            button(tr("مشاركة الآن: ", "Share now: ") + PostSharing.label(str), new Runnable() {
                @Override
                public final void run() {
                    PostSharingActivity.this.lambda$render$5(str);
                }
            });
        }
        button(tr("مشاركة عبر تطبيق آخر", "Share through another app"), new Runnable() {
            @Override
            public final void run() {
                PostSharingActivity.this.lambda$render$6();
            }
        });
        JSONArray jSONArrayLoad = PostSharing.load(this);
        for (int i2 = 0; i2 < jSONArrayLoad.length(); i2++) {
            JSONObject jSONObjectOptJSONObject = jSONArrayLoad.optJSONObject(i2);
            if (jSONObjectOptJSONObject != null && jSONObjectOptJSONObject.optLong("post") == this.postId && jSONObjectOptJSONObject.optBoolean("active")) {
                final String strOptString = jSONObjectOptJSONObject.optString("id");
                final String strOptString2 = jSONObjectOptJSONObject.optString("target");
                text(PostSharing.label(strOptString2) + " • " + tr("متبقي ", "Remaining ") + jSONObjectOptJSONObject.optInt("remaining") + " / " + jSONObjectOptJSONObject.optInt("total") + " • " + jSONObjectOptJSONObject.optInt("minutes") + tr(" دقيقة", " min"), 17);
                text(tr("الموعد: ", "Due: ") + date(jSONObjectOptJSONObject.optLong("next")), 14);
                if (jSONObjectOptJSONObject.optLong("next") <= System.currentTimeMillis()) {
                    button(tr("فتح المشاركة المستحقة", "Open due share"), new Runnable() {
                        @Override
                        public final void run() {
                            PostSharingActivity.this.lambda$render$7(strOptString2, strOptString);
                        }
                    });
                    button(tr("تخطي هذا الموعد", "Skip this reminder"), new Runnable() {
                        @Override
                        public final void run() {
                            PostSharingActivity.this.lambda$render$8(strOptString);
                        }
                    });
                }
                button(tr("إيقاف الجدولة", "Stop schedule"), new Runnable() {
                    @Override
                    public final void run() {
                        PostSharingActivity.this.lambda$render$9(strOptString);
                    }
                });
            }
        }
        scrollView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return PostSharingActivity.lambda$render$10(view, windowInsets);
            }
        });
        scrollView.requestApplyInsets();
        history();
        button(tr("رجوع", "Back"), new Runnable() {
            @Override
            public final void run() {
                PostSharingActivity.this.finish();
            }
        });
    }

    private void text(String str, int i) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Cairo.ttf"));
        textView.setTextSize(i);
        textView.setTextColor(-15717821);
        textView.setPadding(0, dp(8), 0, dp(8));
        this.body.addView(textView);
    }

    private String tr(String str, String str2) {
        return AppLanguage.text(this, str, str2);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AppLanguage.apply(this);
        this.store = new Store(this);
        this.postId = getIntent().getLongExtra("postId", 0L);
        if (bundle != null) {
            this.pendingTarget = bundle.getString("target", "");
            this.pendingPlan = bundle.getString("plan", "");
        }
    }

    @Override
    protected void onDestroy() {
        this.store.close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        getSharedPreferences("post-sharing", 0).edit().putString("pending-target-" + this.postId, this.pendingTarget).putString("pending-plan-" + this.postId, this.pendingPlan).commit();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        try {
            PostSharing.deliver(this);
            PostSharing.schedule(this);
        } catch (Exception unused) {
        }
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.pendingTarget.isEmpty()) {
            SharedPreferences sharedPreferences = getSharedPreferences("post-sharing", 0);
            this.pendingTarget = sharedPreferences.getString("pending-target-" + this.postId, "");
            this.pendingPlan = sharedPreferences.getString("pending-plan-" + this.postId, "");
        }
        try {
            PostSharing.deliver(this);
        } catch (Exception unused) {
        }
        render();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putString("target", this.pendingTarget);
        bundle.putString("plan", this.pendingPlan);
        super.onSaveInstanceState(bundle);
    }
}
