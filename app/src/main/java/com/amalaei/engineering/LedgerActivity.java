package com.amalaei.engineering;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONObject;

public final class LedgerActivity extends BaseSecureActivity {
    private LinearLayout body;
    private Typeface font;
    private Uri pendingPdf;
    private Store store;
    private final int INK = -15189940;
    private final int MUTED = -10192758;
    private final int TEAL = SectionColors.dark("ledger");
    private final int BG = -788486;
    private final int LINE = -2299923;

    private void alert(Exception exc) {
        new FluentDialog(this).setMessage(exc.getMessage() == null ? "تعذر إتمام العملية" : exc.getMessage()).setPositiveButton("حسنًا", (DialogInterface.OnClickListener) null).show();
    }

    private void base(String str) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout linearLayout = new LinearLayout(this);
        this.body = linearLayout;
        linearLayout.setOrientation(1);
        this.body.setLayoutDirection(AppLanguage.direction(this));
        this.body.setPadding(dp(18), dp(16), dp(18), dp(24));
        this.body.setBackgroundColor(-788486);
        scrollView.addView(this.body);
        setContentView(scrollView);
        UiTheme.screen(this, scrollView);
        UiTheme.header(this.body, str, "ledger");
        UiTheme.enter(this.body);
    }

    private Button button(LinearLayout linearLayout, String str, final Runnable runnable, boolean z) {
        Button button = new Button(this);
        button.setText(str);
        UiTheme.button(button, z);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                LedgerActivity.lambda$button$0(runnable, view);
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, dp(4), 0, dp(4));
        linearLayout.addView(button, layoutParams);
        return button;
    }

    private LinearLayout card() {
        return UiTheme.card(this.body);
    }

    private int dp(int i) {
        return Math.round(i * getResources().getDisplayMetrics().density);
    }

    public void lambda$open$7(final JSONObject jSONObject) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20), dp(8), dp(20), dp(8));
        final EditText editTextField = field(linearLayout, "اسم الدفتر", jSONObject.optString("name"), false);
        final EditText editTextField2 = field(linearLayout, "الرصيد الافتتاحي", jSONObject.optString("openingBalance"), true);
        new FluentDialog(this).setTitle("بيانات الدفتر").setView(linearLayout).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حفظ", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                LedgerActivity.this.lambda$editBook$13(jSONObject, editTextField, editTextField2, dialogInterface, i);
            }
        }).setNeutralButton("حذف الدفتر", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                LedgerActivity.this.lambda$editBook$15(jSONObject, dialogInterface, i);
            }
        }).show();
    }

    public void lambda$entryCard$8(final JSONObject jSONObject, JSONObject jSONObject2) {
        JSONObject jSONObject3;
        try {
            jSONObject3 = new JSONObject(jSONObject2.toString());
        } catch (Exception unused) {
            jSONObject3 = new JSONObject();
        }
        final JSONObject jSONObject4 = jSONObject3;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20), dp(8), dp(20), dp(8));
        final EditText editTextField = field(linearLayout, "وصف العملية", jSONObject4.optString("title"), false);
        final EditText editTextField2 = field(linearLayout, "المبلغ", jSONObject4.optString("amount"), true);
        final EditText editTextField3 = field(linearLayout, "ملاحظة مهمة للطباعة", jSONObject4.optString("note"), false);
        final Spinner spinner = new Spinner(this);
        spinner.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"تحصيل", "مصروف نقدي"}));
        spinner.setSelection(jSONObject4.optString("kind").equals("out") ? 1 : 0);
        linearLayout.addView(text("نوع العملية", 13));
        linearLayout.addView(spinner);
        final Spinner spinner2 = new Spinner(this);
        spinner2.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"كاش لدى المندوب", "دفع شبكة للشركة", "تحويل لحساب الشركة"}));
        final String[] strArr = {"cash", "network", "transfer"};
        int paymentSelected = 0;
        for (int index = 0; index < strArr.length; index++) {
            if (strArr[index].equals(jSONObject4.optString("paymentMethod", "cash"))) {
                paymentSelected = index;
            }
        }
        spinner2.setSelection(paymentSelected);
        linearLayout.addView(text("طريقة التحصيل (للوارد فقط)", 13));
        linearLayout.addView(spinner2);
        final Spinner spinner3 = new Spinner(this);
        final String[] strArr2 = {"بدون علامة", "⭐ مهم", "🔴 يحتاج مراجعة", "✓ تم", "✕ ملغي"};
        spinner3.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, strArr2));
        int i5 = 0;
        int i6 = 0;
        for (int i7 = 5; i5 < i7; i7 = 5) {
            if (strArr2[i5].equals(jSONObject4.optString("marker"))) {
                i6 = i5;
            }
            i5++;
        }
        spinner3.setSelection(i6);
        linearLayout.addView(text("علامة تذكير", 13));
        linearLayout.addView(spinner3);
        final AlertDialog alertDialogCreate = new FluentDialog(this).setTitle(jSONObject4.optLong("id") > 0 ? "تعديل بند" : "إضافة بند").setView(linearLayout).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حفظ", (DialogInterface.OnClickListener) null).create();
        alertDialogCreate.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public final void onShow(DialogInterface dialogInterface) {
                LedgerActivity.this.lambda$editEntry$10(alertDialogCreate, jSONObject4, jSONObject, editTextField, editTextField2, spinner, strArr, spinner2, spinner3, strArr2, editTextField3, dialogInterface);
            }
        });
        if (jSONObject4.optLong("id") > 0) {
            alertDialogCreate.setButton(-3, "حذف", new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialogInterface, int i8) {
                    LedgerActivity.this.lambda$editEntry$12(jSONObject4, jSONObject, dialogInterface, i8);
                }
            });
        }
        alertDialogCreate.show();
    }

    public void lambda$open$3(JSONObject jSONObject) {
        lambda$entryCard$8(jSONObject, new JSONObject());
    }

    private void entryCard(final JSONObject jSONObject, final JSONObject jSONObject2) {
        LinearLayout linearLayoutCard = card();
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayoutCard.addView(linearLayout);
        linearLayout.addView(text(jSONObject2.optString("title"), 18), new LinearLayout.LayoutParams(0, -2, 1.0f));
        boolean zEquals = jSONObject2.optString("kind").equals("in");
        TextView textViewText = text((zEquals ? "+ " : "− ") + LedgerMath.money(new BigDecimal(jSONObject2.optString("amount", "0"))), 18);
        textViewText.setTextColor(zEquals ? this.TEAL : -4959479);
        linearLayout.addView(textViewText);
        String strOptString = jSONObject2.optString("marker");
        TextView textViewText2 = text((strOptString.isEmpty() ? "" : strOptString + "  •  ") + ((Object) DateFormat.format("yyyy-MM-dd HH:mm", jSONObject2.optLong("at"))) + (zEquals ? " • " + LedgerMath.methodLabel(jSONObject2.optString("paymentMethod", "cash")) : ""), 13);
        textViewText2.setTextColor(-10192758);
        linearLayoutCard.addView(textViewText2);
        if (!jSONObject2.optString("note").isEmpty()) {
            TextView textViewText3 = text("ملاحظة: " + jSONObject2.optString("note"), 14);
            textViewText3.setTextColor(-10192758);
            linearLayoutCard.addView(textViewText3);
        }
        button(linearLayoutCard, "تعديل أو حذف", new Runnable() {
            @Override
            public final void run() {
                LedgerActivity.this.lambda$entryCard$8(jSONObject, jSONObject2);
            }
        }, false);
    }

    private EditText field(LinearLayout linearLayout, String str, String str2, boolean z) {
        TextView textViewText = text(str, 13);
        textViewText.setTextColor(-10192758);
        linearLayout.addView(textViewText);
        EditText editText = new EditText(this);
        editText.setTypeface(this.font);
        editText.setText(str2);
        editText.setTextSize(16.0f);
        editText.setPadding(dp(12), dp(6), dp(12), dp(6));
        editText.setBackground(shape(-525829, 12, -2299923));
        if (z) {
            editText.setInputType(8194);
        }
        UiTheme.input(editText);
        linearLayout.addView(editText, new LinearLayout.LayoutParams(-1, -2));
        return editText;
    }

    static /* synthetic */ void lambda$button$0(Runnable runnable, View view) {
        UiTheme.feedback(view);
        runnable.run();
    }

    public /* synthetic */ void lambda$editBook$13(JSONObject jSONObject, EditText editText, EditText editText2, DialogInterface dialogInterface, int i) {
        try {
            jSONObject.put("name", editText.getText().toString()).put("openingBalance", editText2.getText().toString());
            this.store.saveNotebook(jSONObject);
            open(jSONObject.optLong("id"));
        } catch (Exception e) {
            alert(e);
        }
    }

    public /* synthetic */ void lambda$editBook$14(JSONObject jSONObject, DialogInterface dialogInterface, int i) {
        this.store.removeNotebook(jSONObject.optLong("id"));
        list();
    }

    public /* synthetic */ void lambda$editBook$15(final JSONObject jSONObject, DialogInterface dialogInterface, int i) {
        new FluentDialog(this).setMessage("سيُحذف الدفتر وكل بنوده نهائيًا.").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حذف", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface2, int i2) {
                LedgerActivity.this.lambda$editBook$14(jSONObject, dialogInterface2, i2);
            }
        }).show();
    }

    public /* synthetic */ void lambda$editEntry$10(final AlertDialog alertDialog, final JSONObject jSONObject, final JSONObject jSONObject2, final EditText editText, final EditText editText2, final Spinner spinner, final String[] strArr, final Spinner spinner2, final Spinner spinner3, final String[] strArr2, final EditText editText3, DialogInterface dialogInterface) {
        alertDialog.getButton(-1).setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                LedgerActivity.this.lambda$editEntry$9(jSONObject, jSONObject2, editText, editText2, spinner, strArr, spinner2, spinner3, strArr2, editText3, alertDialog, view);
            }
        });
    }

    public /* synthetic */ void lambda$editEntry$11(JSONObject jSONObject, JSONObject jSONObject2, DialogInterface dialogInterface, int i) {
        this.store.removeLedgerEntry(jSONObject.optLong("id"));
        open(jSONObject2.optLong("id"));
    }

    public /* synthetic */ void lambda$editEntry$12(final JSONObject jSONObject, final JSONObject jSONObject2, DialogInterface dialogInterface, int i) {
        new FluentDialog(this).setMessage("حذف هذا البند؟").setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حذف", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface2, int i2) {
                LedgerActivity.this.lambda$editEntry$11(jSONObject, jSONObject2, dialogInterface2, i2);
            }
        }).show();
    }

    public /* synthetic */ void lambda$editEntry$9(JSONObject jSONObject, JSONObject jSONObject2, EditText editText, EditText editText2, Spinner spinner, String[] strArr, Spinner spinner2, Spinner spinner3, String[] strArr2, EditText editText3, AlertDialog alertDialog, View view) {
        try {
            jSONObject.put("notebookId", jSONObject2.optLong("id")).put("title", editText.getText().toString()).put("amount", editText2.getText().toString()).put("kind", spinner.getSelectedItemPosition() == 0 ? "in" : "out").put("paymentMethod", spinner.getSelectedItemPosition() == 0 ? strArr[spinner2.getSelectedItemPosition()] : "cash").put("marker", spinner3.getSelectedItemPosition() == 0 ? "" : strArr2[spinner3.getSelectedItemPosition()]).put("note", editText3.getText().toString()).put("at", jSONObject.optLong("at", System.currentTimeMillis()));
            this.store.saveLedgerEntry(jSONObject);
            alertDialog.dismiss();
            open(jSONObject2.optLong("id"));
        } catch (Exception e) {
            alert(e);
        }
    }

    public /* synthetic */ void lambda$list$1(JSONObject jSONObject) {
        open(jSONObject.optLong("id"));
    }

    public /* synthetic */ void lambda$newNotebook$2(EditText editText, EditText editText2, DialogInterface dialogInterface, int i) {
        try {
            open(this.store.saveNotebook(new JSONObject().put("name", editText.getText().toString()).put("openingBalance", editText2.getText().toString())));
        } catch (Exception e) {
            alert(e);
        }
    }

    private void ledgerMetrics(String str, BigDecimal bigDecimal, String str2, BigDecimal bigDecimal2) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        this.body.addView(linearLayout);
        int i = 0;
        while (i < 2) {
            LinearLayout linearLayoutCard = UiTheme.card(linearLayout);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams.setMargins(dp(3), dp(4), dp(3), dp(4));
            linearLayoutCard.setLayoutParams(layoutParams);
            TextView textViewText = text(i == 0 ? str : str2, 12);
            textViewText.setTextColor(-10192758);
            linearLayoutCard.addView(textViewText);
            TextView textViewText2 = text(LedgerMath.money(i == 0 ? bigDecimal : bigDecimal2), 18);
            textViewText2.setTextColor(this.TEAL);
            textViewText2.setTextDirection(3);
            linearLayoutCard.addView(textViewText2);
            i++;
        }
    }

    private Uri ledgerPdfUri(JSONObject jSONObject, ArrayList<JSONObject> arrayList) throws Exception {
        return new Uri.Builder().scheme("content").authority(getPackageName() + ".quotes").appendPath(LedgerPdf.create(this, jSONObject, arrayList).getName()).build();
    }

    public void list() {
        base("الدفاتر");
        muted("سجّل العهدة والتحصيلات والمصروفات فورًا؛ الرصيد يتغير مع كل بند.");
        button(this.body, "+ إنشاء دفتر جديد", new Runnable() {
            @Override
            public final void run() {
                LedgerActivity.this.newNotebook();
            }
        }, true);
        ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM notebooks ORDER BY createdAt DESC", new String[0]);
        if (arrayListQuery.isEmpty()) {
            muted("أنشئ دفترًا مثل «عهدة المندوب» أو «مصروفات السيارة».");
        }
        for (final JSONObject jSONObject : arrayListQuery) {
            ArrayList<JSONObject> arrayListQuery2 = this.store.query("SELECT * FROM ledgerEntries WHERE notebookId=? ORDER BY at DESC,id DESC", String.valueOf(jSONObject.optLong("id")));
            LinearLayout linearLayoutCard = card();
            linearLayoutCard.addView(text(jSONObject.optString("name"), 20));
            TextView textViewText = text("الرصيد الحالي  " + LedgerMath.money(LedgerMath.balance(jSONObject, arrayListQuery2)), 20);
            textViewText.setTextColor(this.TEAL);
            linearLayoutCard.addView(textViewText);
            TextView textViewText2 = text("افتتاحي " + LedgerMath.money(LedgerMath.opening(jSONObject)) + "  •  إضافات " + LedgerMath.money(LedgerMath.total(arrayListQuery2, "in")) + "  •  مصروفات " + LedgerMath.money(LedgerMath.total(arrayListQuery2, "out")), 13);
            textViewText2.setTextColor(-10192758);
            linearLayoutCard.addView(textViewText2);
            button(linearLayoutCard, "فتح الدفتر", new Runnable() {
                @Override
                public final void run() {
                    LedgerActivity.this.lambda$list$1(jSONObject);
                }
            }, false);
        }
    }

    private void muted(String str) {
        TextView textViewText = text(str, 14);
        textViewText.setTextColor(-10192758);
        this.body.addView(textViewText);
    }

    public void newNotebook() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20), dp(8), dp(20), dp(8));
        final EditText editTextField = field(linearLayout, "اسم الدفتر", "", false);
        final EditText editTextField2 = field(linearLayout, "الرصيد الافتتاحي (اختياري)", "", true);
        new FluentDialog(this).setTitle("دفتر جديد").setView(linearLayout).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("إنشاء", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                LedgerActivity.this.lambda$newNotebook$2(editTextField, editTextField2, dialogInterface, i);
            }
        }).show();
    }

    private void open(long j) {
        ArrayList<JSONObject> arrayListQuery = this.store.query("SELECT * FROM notebooks WHERE id=?", String.valueOf(j));
        if (arrayListQuery.isEmpty()) {
            list();
            return;
        }
        final JSONObject jSONObject = arrayListQuery.get(0);
        final ArrayList<JSONObject> arrayListQuery2 = this.store.query("SELECT * FROM ledgerEntries WHERE notebookId=? ORDER BY at DESC,id DESC", String.valueOf(j));
        base(jSONObject.optString("name"));
        LinearLayout linearLayoutCard = card();
        linearLayoutCard.setBackground(UiTheme.surface(this, true, 20));
        TextView textViewText = text(AppLanguage.text(this, "النقدية لدى المندوب", "Cash held by representative"), 13);
        textViewText.setTextColor(-3217685);
        linearLayoutCard.addView(textViewText);
        TextView textViewText2 = text(LedgerMath.money(LedgerMath.balance(jSONObject, arrayListQuery2)), 30);
        textViewText2.setTextColor(-1);
        textViewText2.setTextDirection(3);
        linearLayoutCard.addView(textViewText2);
        ledgerMetrics(AppLanguage.text(this, "رصيد افتتاحي", "Opening balance"), LedgerMath.opening(jSONObject), AppLanguage.text(this, "المصروفات", "Expenses"), LedgerMath.total(arrayListQuery2, "out"));
        ledgerMetrics(AppLanguage.text(this, "تحصيل كاش", "Cash collected"), LedgerMath.collections(arrayListQuery2, "cash"), AppLanguage.text(this, "تحصيل شبكة", "Card collected"), LedgerMath.collections(arrayListQuery2, "network"));
        ledgerMetrics(AppLanguage.text(this, "تحصيل تحويل", "Bank transfers"), LedgerMath.collections(arrayListQuery2, "transfer"), AppLanguage.text(this, "صافي التحصيل", "Net collections"), LedgerMath.netActivity(arrayListQuery2));
        button(this.body, "+ إضافة بند", new Runnable() {
            @Override
            public final void run() {
                LedgerActivity.this.lambda$open$3(jSONObject);
            }
        }, true);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        this.body.addView(linearLayout);
        Button[] buttonArr = {button(linearLayout, "معاينة PDF", new Runnable() {
            @Override
            public final void run() {
                LedgerActivity.this.lambda$open$4(jSONObject, arrayListQuery2);
            }
        }, false), button(linearLayout, "مشاركة PDF", new Runnable() {
            @Override
            public final void run() {
                LedgerActivity.this.lambda$open$5(jSONObject, arrayListQuery2);
            }
        }, false)};
        for (int i = 0; i < 2; i++) {
            Button button = buttonArr[i];
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams.setMargins(dp(3), dp(4), dp(3), dp(4));
            button.setLayoutParams(layoutParams);
        }
        button(this.body, "حفظ PDF في الهاتف", new Runnable() {
            @Override
            public final void run() {
                LedgerActivity.this.lambda$open$6(jSONObject, arrayListQuery2);
            }
        }, false);
        button(this.body, "تعديل اسم الدفتر أو الرصيد الافتتاحي", new Runnable() {
            @Override
            public final void run() {
                LedgerActivity.this.lambda$open$7(jSONObject);
            }
        }, false);
        this.body.addView(text(AppLanguage.text(this, "حركات الدفتر", "Transactions"), 20));
        if (arrayListQuery2.isEmpty()) {
            muted("لا توجد بنود بعد. أضف تحصيلًا أو مصروفًا وسيُحدّث الرصيد فورًا.");
        }
        Iterator<JSONObject> it = arrayListQuery2.iterator();
        while (it.hasNext()) {
            entryCard(jSONObject, it.next());
        }
        button(this.body, "العودة إلى الدفاتر", new Runnable() {
            @Override
            public final void run() {
                LedgerActivity.this.list();
            }
        }, false);
    }

    public void lambda$open$4(JSONObject jSONObject, ArrayList<JSONObject> arrayList) {
        try {
            Uri uriLedgerPdfUri = ledgerPdfUri(jSONObject, arrayList);
            Intent intentAddFlags = new Intent(this, (Class<?>) QuotePreviewActivity.class).setDataAndType(uriLedgerPdfUri, "application/pdf").putExtra("previewTitle", "معاينة كشف الدفتر").addFlags(1);
            intentAddFlags.setClipData(ClipData.newRawUri("كشف دفتر", uriLedgerPdfUri));
            startActivity(intentAddFlags);
        } catch (Exception e) {
            alert(e);
        }
    }

    public void lambda$open$6(JSONObject jSONObject, ArrayList<JSONObject> arrayList) {
        try {
            this.pendingPdf = ledgerPdfUri(jSONObject, arrayList);
            startActivityForResult(new Intent("android.intent.action.CREATE_DOCUMENT").setType("application/pdf").addCategory("android.intent.category.OPENABLE").putExtra("android.intent.extra.TITLE", new File(this.pendingPdf.getLastPathSegment()).getName()), 70);
        } catch (Exception e) {
            alert(e);
        }
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

    public void lambda$open$5(JSONObject jSONObject, ArrayList<JSONObject> arrayList) {
        try {
            Uri uriLedgerPdfUri = ledgerPdfUri(jSONObject, arrayList);
            Intent intentAddFlags = new Intent("android.intent.action.SEND").setType("application/pdf").putExtra("android.intent.extra.STREAM", uriLedgerPdfUri).putExtra("android.intent.extra.SUBJECT", "كشف " + jSONObject.optString("name")).addFlags(1);
            intentAddFlags.setClipData(ClipData.newRawUri("كشف دفتر", uriLedgerPdfUri));
            startActivity(Intent.createChooser(intentAddFlags, "مشاركة كشف الدفتر"));
        } catch (Exception e) {
            alert(e);
        }
    }

    private TextView text(String str, int i) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTypeface(this.font);
        textView.setTextColor(-15189940);
        textView.setPadding(0, dp(4), 0, dp(4));
        return textView;
    }

    @Override
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i != 70 || i2 != -1 || intent == null || intent.getData() == null || this.pendingPdf == null) {
            return;
        }
        try {
            try {
                InputStream inputStreamOpenInputStream = getContentResolver().openInputStream(this.pendingPdf);
                try {
                    OutputStream outputStreamOpenOutputStream = getContentResolver().openOutputStream(intent.getData());
                    try {
                        if (inputStreamOpenInputStream == null || outputStreamOpenOutputStream == null) {
                            throw new IOException();
                        }
                        byte[] bArr = new byte[8192];
                        while (true) {
                            int i3 = inputStreamOpenInputStream.read(bArr);
                            if (i3 == -1) {
                                break;
                            } else {
                                outputStreamOpenOutputStream.write(bArr, 0, i3);
                            }
                        }
                        Toast.makeText(this, "حُفظ كشف PDF", 0).show();
                        if (outputStreamOpenOutputStream != null) {
                            outputStreamOpenOutputStream.close();
                        }
                        if (inputStreamOpenInputStream != null) {
                            inputStreamOpenInputStream.close();
                        }
                        this.pendingPdf = null;
                    } catch (Throwable th) {
                        if (outputStreamOpenOutputStream != null) {
                            try {
                                outputStreamOpenOutputStream.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
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
            } catch (Throwable th5) {
                this.pendingPdf = null;
                throw th5;
            }
        } catch (Exception e) {
            alert(e);
            this.pendingPdf = null;
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.font = Typeface.createFromAsset(getAssets(), "fonts/Cairo.ttf");
        this.store = new Store(this);
        list();
    }

    @Override
    protected void onDestroy() {
        this.store.close();
        super.onDestroy();
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        AppLanguage.apply(this);
        AppLanguage.watch(this);
    }
}
