package com.amalaei.engineering;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.math.BigDecimal;

public final class CalculatorActivity extends BaseSecureActivity {
    private Button addMode;
    private Typeface arabic;
    private LinearLayout body;
    private TextView expression;
    private Button includedMode;
    private Typeface numbers;
    private TextView outputCaption;
    private Button rateButton;
    private TextView result;
    private TextView screenCaption;
    private TextView screenTaxLine;
    private TextView status;
    private TextView subtitle;
    private final Calculator calculator = new Calculator();
    private BigDecimal taxPercent = new BigDecimal("15");
    private boolean includedTax = false;
    private final int INK = -15717821;
    private final int ACCENT = SectionColors.dark("calculator");
    private final int MUTED = -10192758;
    private final int BG = -788486;

    private void act(String str) {
        try {
            UiTheme.feedback(this.body);
            this.calculator.press(str);
            display();
        } catch (IllegalArgumentException e) {
            this.status.setTextColor(-4371881);
            this.status.setText(e.getMessage());
        }
    }

    private void chooseRate() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20), dp(8), dp(20), 0);
        TextView textViewText = text("أدخل النسبة من 0 إلى 100، ويمكن استخدام نسبة عشرية مثل 14.5", 13, -15717821, false);
        textViewText.setSingleLine(false);
        linearLayout.addView(textViewText);
        final EditText editText = new EditText(this);
        editText.setTypeface(this.numbers);
        editText.setText(Calculator.plain(this.taxPercent));
        editText.setSelectAllOnFocus(true);
        editText.setTextDirection(3);
        editText.setGravity(8388611);
        editText.setInputType(8194);
        linearLayout.addView(editText, new LinearLayout.LayoutParams(-1, dp(55)));
        new FluentDialog(this).setTitle("نسبة الضريبة").setView(linearLayout).setNegativeButton("إلغاء", (DialogInterface.OnClickListener) null).setPositiveButton("حفظ", new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface dialogInterface, int i) {
                CalculatorActivity.this.lambda$chooseRate$6(editText, dialogInterface, i);
            }
        }).show();
    }

    private void display() {
        BigDecimal bigDecimalEvaluate;
        boolean z;
        String str;
        String strPercent = percent();
        this.subtitle.setText((this.includedTax ? "استخراج الضريبة من الإجمالي" : "إضافة الضريبة إلى الصافي") + " • " + strPercent);
        this.screenCaption.setText("المبلغ المدخل / العملية  ·  SAR");
        this.outputCaption.setText(this.includedTax ? "الصافي بعد استخراج الضريبة" : "الإجمالي شامل الضريبة");
        this.rateButton.setText(AppLanguage.text(this, "نسبة الضريبة • ", "Tax rate • ") + strPercent);
        styleMode(this.addMode, !this.includedTax);
        styleMode(this.includedMode, this.includedTax);
        String strExpression = this.calculator.expression();
        String str2 = "0";
        this.expression.setText(strExpression.isEmpty() ? "0" : strExpression.replace('*', (char) 215).replace('/', (char) 247));
        try {
            try {
                bigDecimalEvaluate = this.calculator.value();
                z = false;
            } catch (IllegalArgumentException e) {
                if (!"أكمل العملية أولًا".equals(e.getMessage())) {
                    throw e;
                }
                while (!strExpression.isEmpty() && "+*/-".indexOf(strExpression.charAt(strExpression.length() - 1)) >= 0) {
                    strExpression = strExpression.substring(0, strExpression.length() - 1);
                }
                if (!strExpression.isEmpty()) {
                    str2 = strExpression;
                }
                bigDecimalEvaluate = Calculator.evaluate(str2);
                z = true;
            }
            BigDecimal[] bigDecimalArrVat = Calculator.vat(bigDecimalEvaluate, this.taxPercent, this.includedTax);
            this.result.setText(Calculator.money(this.includedTax ? bigDecimalArrVat[0] : bigDecimalArrVat[2]));
            this.screenTaxLine.setText("قيمة الضريبة " + strPercent + "  •  " + Calculator.money(bigDecimalArrVat[1]) + " SAR");
            this.status.setTextColor(-7761244);
            TextView textView = this.status;
            if (z) {
                str = "أدخل العدد التالي • النتيجة لآخر تعبير مكتمل";
            } else {
                str = this.includedTax ? "الصافي بعد خصم الضريبة من الإجمالي" : "الإجمالي بعد إضافة الضريبة";
            }
            textView.setText(str);
        } catch (IllegalArgumentException e2) {
            this.result.setText("—");
            this.screenTaxLine.setText("تعذر حساب الضريبة");
            this.status.setTextColor(-4371881);
            this.status.setText(e2.getMessage());
        }
    }

    private int dp(int i) {
        return Math.round(i * getResources().getDisplayMetrics().density);
    }

    private Button key(String str, String str2) {
        float f;
        int i;
        boolean zMatches = str2.matches("[0-9.]");
        boolean zEquals = str2.equals("=");
        boolean zEquals2 = str2.equals("tax");
        Button button = new Button(this);
        button.setText(str);
        if (zEquals2) {
            f = 15.0f;
        } else if (str2.equals("backscreen")) {
            f = 13.0f;
        } else {
            f = str2.equals("C") ? 20.0f : 29.0f;
        }
        button.setTextSize(f);
        button.setTypeface((zEquals2 || str2.equals("backscreen")) ? this.arabic : this.numbers);
        if (zEquals) {
            i = -1;
        } else {
            i = (!zEquals2 && zMatches) ? -15717821 : this.ACCENT;
        }
        button.setTextColor(i);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(dp(4), 0, dp(4), 0);
        button.setStateListAnimator(null);
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(571438702), UiTheme.surface(this, zEquals, 14), null));
        if (str2.equals("back")) {
            str = "حذف آخر رقم";
        } else if (str2.equals("C")) {
            str = "مسح العملية";
        }
        button.setContentDescription(str);
        UiTheme.motion(button);
        button.setSoundEffectsEnabled(false);
        return button;
    }

    private void keypadRow(String[] strArr, String[] strArr2) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutDirection(0);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(58));
        layoutParams.setMargins(0, dp(3), 0, dp(3));
        this.body.addView(linearLayout, layoutParams);
        for (int i = 0; i < strArr.length; i++) {
            final String str = strArr2[i];
            Button buttonKey = key(strArr[i], str);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, -1, 1.0f);
            layoutParams2.setMargins(dp(3), 0, dp(3), 0);
            linearLayout.addView(buttonKey, layoutParams2);
            buttonKey.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(View view) {
                    CalculatorActivity.this.lambda$keypadRow$5(str, view);
                }
            });
        }
    }

    public /* synthetic */ void lambda$chooseRate$6(EditText editText, DialogInterface dialogInterface, int i) {
        try {
            this.taxPercent = Calculator.taxRate(editText.getText().toString());
            getPreferences(0).edit().putString("taxPercent", Calculator.plain(this.taxPercent)).apply();
            display();
        } catch (IllegalArgumentException e) {
            this.status.setTextColor(-4371881);
            this.status.setText(e.getMessage());
        }
    }

    public /* synthetic */ void lambda$keypadRow$5(String str, View view) {
        act(str);
    }

    public /* synthetic */ WindowInsets lambda$onCreate$0(View view, WindowInsets windowInsets) {
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
        view.setPadding(dp(18), systemWindowInsetTop + dp(12), dp(18), systemWindowInsetBottom + dp(96));
        return windowInsets;
    }

    public /* synthetic */ void lambda$onCreate$1(View view) {
        finish();
    }

    public /* synthetic */ void lambda$onCreate$2(View view) {
        this.includedTax = false;
        display();
    }

    public /* synthetic */ void lambda$onCreate$3(View view) {
        this.includedTax = true;
        display();
    }

    public /* synthetic */ void lambda$onCreate$4(View view) {
        chooseRate();
    }

    private Button modeButton(String str) {
        Button buttonKey = key(str, "tax");
        buttonKey.setTextSize(13.0f);
        return buttonKey;
    }

    private String percent() {
        return Calculator.plain(this.taxPercent) + "%";
    }

    private GradientDrawable shape(int i, int i2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(i2));
        return gradientDrawable;
    }

    private void styleMode(Button button, boolean z) {
        button.setTextColor(z ? -1 : this.ACCENT);
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(571438702), shape(z ? this.ACCENT : SectionColors.tint("calculator"), 16), null));
    }

    private TextView text(String str, int i, int i2, boolean z) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTextColor(i2);
        textView.setTypeface(z ? this.numbers : this.arabic);
        textView.setGravity(8388629);
        textView.setTextDirection(z ? 3 : 4);
        if (z) {
            textView.setSingleLine(true);
            textView.setFontFeatureSettings("tnum");
        }
        return textView;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.arabic = Typeface.createFromAsset(getAssets(), "fonts/Cairo.ttf");
        this.numbers = Typeface.create("sans-serif-medium", 0);
        try {
            this.taxPercent = Calculator.taxRate(bundle == null ? getPreferences(0).getString("taxPercent", "15") : bundle.getString("taxPercent", "15"));
        } catch (Exception unused) {
            this.taxPercent = new BigDecimal("15");
        }
        this.includedTax = bundle != null && bundle.getBoolean("includedTax", false);
        if (bundle != null) {
            this.calculator.restore(bundle.getString("expression", ""), bundle.getBoolean("finished", false));
        }
        getWindow().setStatusBarColor(-788486);
        getWindow().setNavigationBarColor(-788486);
        getWindow().getDecorView().setSystemUiVisibility(8208);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);
        scrollView.setVerticalScrollBarEnabled(true);
        scrollView.setScrollbarFadingEnabled(false);
        scrollView.setSmoothScrollingEnabled(true);
        LinearLayout linearLayout = new LinearLayout(this);
        this.body = linearLayout;
        linearLayout.setOrientation(1);
        this.body.setBackgroundColor(-788486);
        scrollView.addView(this.body, new FrameLayout.LayoutParams(-1, -2));
        setContentView(scrollView);
        this.body.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return CalculatorActivity.this.lambda$onCreate$0(view, windowInsets);
            }
        });
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setLayoutDirection(AppLanguage.direction(this));
        linearLayout2.setGravity(16);
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(1);
        linearLayout3.addView(text("حاسبة الأسعار", 23, -15717821, false));
        TextView textViewText = text("", 12, -10192758, false);
        this.subtitle = textViewText;
        linearLayout3.addView(textViewText);
        linearLayout2.addView(linearLayout3, new LinearLayout.LayoutParams(0, -2, 1.0f));
        Button buttonKey = key("رجوع", "backscreen");
        linearLayout2.addView(buttonKey, new LinearLayout.LayoutParams(dp(68), dp(44)));
        buttonKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                CalculatorActivity.this.lambda$onCreate$1(view);
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, 0, 0, dp(16));
        this.body.addView(linearLayout2, layoutParams);
        UiTheme.header(this.body, AppLanguage.text(this, "الحاسبة", "Calculator"), "calculator");
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(1);
        linearLayout4.setPadding(dp(20), dp(14), dp(20), dp(14));
        linearLayout4.setBackground(UiTheme.surface(this, true, 24));
        linearLayout4.setElevation(dp(3));
        this.body.addView(linearLayout4, new LinearLayout.LayoutParams(-1, -2));
        TextView textViewText2 = text("المبلغ المدخل / العملية  ·  SAR", 11, -3343375, false);
        this.screenCaption = textViewText2;
        linearLayout4.addView(textViewText2);
        TextView textViewText3 = text("0", 18, -6687004, true);
        this.expression = textViewText3;
        textViewText3.setHorizontalScrollBarEnabled(false);
        this.expression.setAutoSizeTextTypeUniformWithConfiguration(13, 18, 1, 2);
        linearLayout4.addView(this.expression, new LinearLayout.LayoutParams(-1, dp(30)));
        View view = new View(this);
        view.setBackgroundColor(861858516);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, dp(1));
        layoutParams2.setMargins(0, dp(3), 0, dp(5));
        linearLayout4.addView(view, layoutParams2);
        TextView textViewText4 = text("", 11, -3343375, false);
        this.outputCaption = textViewText4;
        linearLayout4.addView(textViewText4);
        TextView textViewText5 = text("0.00", 38, -1, true);
        this.result = textViewText5;
        textViewText5.setAutoSizeTextTypeUniformWithConfiguration(21, 38, 1, 2);
        this.result.setTextIsSelectable(true);
        linearLayout4.addView(this.result, new LinearLayout.LayoutParams(-1, dp(54)));
        TextView textViewText6 = text("", 11, -6687004, false);
        this.screenTaxLine = textViewText6;
        textViewText6.setSingleLine(true);
        this.screenTaxLine.setAutoSizeTextTypeUniformWithConfiguration(9, 11, 1, 2);
        linearLayout4.addView(this.screenTaxLine, new LinearLayout.LayoutParams(-1, dp(24)));
        keypadRow(new String[]{"7", "8", "9", "÷"}, new String[]{"7", "8", "9", "/"});
        keypadRow(new String[]{"4", "5", "6", "×"}, new String[]{"4", "5", "6", "*"});
        keypadRow(new String[]{"1", "2", "3", "−"}, new String[]{"1", "2", "3", "-"});
        keypadRow(new String[]{"0", ".", "=", "+"}, new String[]{"0", ".", "=", "+"});
        keypadRow(new String[]{"AC", "⌫", "±"}, new String[]{"C", "back", "sign"});
        UiTheme.enter(this.body);
        TextView textViewText7 = text("إعدادات الضريبة", 12, -10192758, false);
        textViewText7.setPadding(0, dp(14), 0, dp(5));
        this.body.addView(textViewText7);
        LinearLayout linearLayout5 = new LinearLayout(this);
        linearLayout5.setLayoutDirection(1);
        this.body.addView(linearLayout5, new LinearLayout.LayoutParams(-1, dp(58)));
        this.addMode = modeButton("إضافة الضريبة");
        this.includedMode = modeButton("المبلغ شامل الضريبة");
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(0, -1, 1.0f);
        layoutParams3.setMargins(dp(3), 0, dp(3), 0);
        linearLayout5.addView(this.addMode, layoutParams3);
        linearLayout5.addView(this.includedMode, layoutParams3);
        this.addMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view2) {
                CalculatorActivity.this.lambda$onCreate$2(view2);
            }
        });
        this.includedMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view2) {
                CalculatorActivity.this.lambda$onCreate$3(view2);
            }
        });
        this.rateButton = key("", "tax");
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, dp(50));
        layoutParams4.setMargins(dp(3), dp(8), dp(3), dp(8));
        this.body.addView(this.rateButton, layoutParams4);
        this.rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view2) {
                CalculatorActivity.this.lambda$onCreate$4(view2);
            }
        });
        TextView textViewText8 = text("", 12, -7761244, false);
        this.status = textViewText8;
        textViewText8.setMinHeight(dp(27));
        this.body.addView(this.status);
        TextView textViewText9 = text("في وضع «المبلغ شامل الضريبة» يستخرج التطبيق الصافي والضريبة الصحيحة من الإجمالي؛ مثال 1000 شامل 15% = صافي 869.57 وضريبة 130.43.", 11, -10192758, false);
        textViewText9.setSingleLine(false);
        textViewText9.setPadding(0, dp(10), 0, dp(18));
        this.body.addView(textViewText9);
        display();
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        AppLanguage.apply(this);
        AppLanguage.watch(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putString("expression", this.calculator.expression());
        bundle.putBoolean("finished", this.calculator.finished());
        bundle.putString("taxPercent", Calculator.plain(this.taxPercent));
        bundle.putBoolean("includedTax", this.includedTax);
        super.onSaveInstanceState(bundle);
    }
}
