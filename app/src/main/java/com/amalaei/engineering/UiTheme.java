package com.amalaei.engineering;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

final class UiTheme {
    static final int BG = -788486;
    static final int INK = -15189940;
    static final int LINE = -2299923;
    static final int MUTED = -10192758;
    static final int NAVY = -15717821;
    static final int TEAL = -15434365;
    static final int TINT = -1576205;
    static final int WHITE = -1;
    private static Typeface cachedFont;

    private UiTheme() {
    }

    static int accent(Context context) {
        return SectionColors.dark(section(context));
    }

    static void button(Button button, boolean z) {
        Context context = button.getContext();
        String strText = ActionLabels.text(button.getText().toString(), AppLanguage.english(context));
        if (!strText.contentEquals(button.getText())) {
            button.setText(strText);
        }
        button.setAllCaps(false);
        button.setTypeface(font(context));
        button.setTextSize(14.0f);
        button.setTextColor(z ? WHITE : accent(context));
        button.setMinHeight(dp(context, 48));
        button.setMinimumHeight(dp(context, 48));
        button.setPadding(dp(context, 14), dp(context, 8), dp(context, 14), dp(context, 8));
        button.setStateListAnimator(null);
        GradientDrawable gradientDrawableSectionSurface = sectionSurface(context, z, 14);
        button.setBackgroundTintList(null);
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(z ? 872415231 : 403996035), gradientDrawableSectionSurface, null));
        button.setElevation(z ? dp(context, 2) : 0.0f);
        motion(button);
        button.setSoundEffectsEnabled(false);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setHyphenationFrequency(0);
        if ((button.getParent() instanceof LinearLayout) && ((LinearLayout) button.getParent()).getOrientation() == 0) {
            compactButton(button);
        }
    }

    static LinearLayout card(LinearLayout linearLayout) {
        Context context = linearLayout.getContext();
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(dp(context, 16), dp(context, 14), dp(context, 16), dp(context, 14));
        sectionCard(linearLayout2, section(context));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WHITE, -2);
        layoutParams.setMargins(0, dp(context, 7), 0, dp(context, 7));
        linearLayout.addView(linearLayout2, layoutParams);
        return linearLayout2;
    }

    static void compactButton(Button button) {
        Context context = button.getContext();
        boolean zContains = button.getText().toString().contains("\n");
        button.setSingleLine(!zContains);
        if (zContains) {
            button.setMaxLines(2);
        }
        button.setPadding(dp(context, 6), dp(context, 8), dp(context, 6), dp(context, 8));
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setAutoSizeTextTypeUniformWithConfiguration(11, 14, 1, 2);
    }

    static int dp(Context context, int i) {
        return Math.round(i * context.getResources().getDisplayMetrics().density);
    }

    static void enter(View view) {
        if (ValueAnimator.areAnimatorsEnabled()) {
            view.setAlpha(0.0f);
            view.animate().alpha(1.0f).setDuration(180L).start();
        }
    }

    static void feedback(View view) {
        Context context = view.getContext();
        AudioManager audioManager = (AudioManager) context.getSystemService(AudioManager.class);
        if (context.getSharedPreferences("settings", 0).getBoolean("interactionSounds", false) && audioManager.getRingerMode() == 2) {
            audioManager.playSoundEffect(0);
        }
    }

    static Typeface font(Context context) {
        if (cachedFont == null) {
            cachedFont = Typeface.createFromAsset(context.getAssets(), "fonts/Cairo.ttf");
        }
        return cachedFont;
    }

    static void header(LinearLayout linearLayout, String str, String str2) {
        Context context = linearLayout.getContext();
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setGravity(16);
        linearLayout2.setPadding(dp(context, 16), dp(context, 14), dp(context, 16), dp(context, 14));
        linearLayout2.setBackground(sectionSurface(context, true, 20));
        linearLayout2.addView(new GlassIcon(context, str2), new LinearLayout.LayoutParams(dp(context, 48), dp(context, 48)));
        TextView textView = new TextView(context);
        textView.setText(DisplayNumbers.latin(str));
        textView.setTypeface(font(context), 1);
        textView.setTextColor(WHITE);
        textView.setTextSize(22.0f);
        textView.setPadding(dp(context, 12), 0, dp(context, 12), 0);
        linearLayout2.addView(textView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WHITE, -2);
        layoutParams.setMargins(0, 0, 0, dp(context, 14));
        linearLayout.addView(linearLayout2, layoutParams);
    }

    static void input(EditText editText) {
        Context context = editText.getContext();
        editText.setTypeface(font(context));
        editText.setTextSize(15.0f);
        editText.setTextColor(INK);
        editText.setHintTextColor(MUTED);
        editText.setPadding(dp(context, 14), dp(context, 10), dp(context, 14), dp(context, 10));
        editText.setMinHeight(dp(context, 50));
        editText.setBackground(sectionSurface(context, false, 13));
    }

    static /* synthetic */ boolean lambda$motion$1(View view, MotionEvent motionEvent) {
        boolean z = true;
        boolean z2 = motionEvent.getActionMasked() == 0;
        if (motionEvent.getActionMasked() != 1 && motionEvent.getActionMasked() != 3) {
            z = false;
        }
        if ((z2 || z) && ValueAnimator.areAnimatorsEnabled()) {
            view.animate().scaleX(z2 ? 0.975f : 1.0f).scaleY(z2 ? 0.975f : 1.0f).setDuration(z2 ? 80L : 150L).start();
        }
        return false;
    }

    static /* synthetic */ WindowInsets lambda$screen$0(View view, WindowInsets windowInsets) {
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

    static void motion(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public final boolean onTouch(View view2, MotionEvent motionEvent) {
                return UiTheme.lambda$motion$1(view2, motionEvent);
            }
        });
    }

    static void screen(Activity activity, View view) {
        activity.getWindow().setStatusBarColor(BG);
        activity.getWindow().setNavigationBarColor(BG);
        activity.getWindow().getDecorView().setSystemUiVisibility(8208);
        view.setBackgroundColor(BG);
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public final WindowInsets onApplyWindowInsets(View view2, WindowInsets windowInsets) {
                return UiTheme.lambda$screen$0(view2, windowInsets);
            }
        });
        view.requestApplyInsets();
    }

    static String section(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof MainActivity) {
                return ((MainActivity) context).visualSection();
            }
            if (context instanceof LedgerActivity) {
                return "ledger";
            }
            if (context instanceof ReportsActivity) {
                return "reports";
            }
            if ((context instanceof QuoteActivity) || (context instanceof QuotePreviewActivity)) {
                return "quotes";
            }
            if (context instanceof CalculatorActivity) {
                return "calculator";
            }
            if (context instanceof PostSharingActivity) {
                return "ads";
            }
            if (context instanceof SoftwareActivity) {
                return "clients";
            }
            Context baseContext = ((ContextWrapper) context).getBaseContext();
            if (baseContext == context) {
                return "brand";
            }
            context = baseContext;
        }
        return "brand";
    }

    static void sectionCard(View view, String str) {
        styleCard(view);
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{WHITE, SectionColors.tint(str)});
        gradientDrawable.setCornerRadius(dp(view.getContext(), 20));
        gradientDrawable.setStroke(dp(view.getContext(), 1), -2628885);
        view.setBackground(gradientDrawable);
    }

    static GradientDrawable sectionSurface(Context context, boolean z, int i) {
        String strSection = section(context);
        GradientDrawable.Orientation orientation = GradientDrawable.Orientation.TL_BR;
        int[] iArr = new int[2];
        if (z) {
            iArr[0] = SectionColors.dark(strSection);
            iArr[1] = NAVY;
        } else {
            iArr[0] = WHITE;
            iArr[1] = SectionColors.tint(strSection);
        }
        GradientDrawable gradientDrawable = new GradientDrawable(orientation, iArr);
        gradientDrawable.setCornerRadius(dp(context, i));
        gradientDrawable.setStroke(dp(context, 1), z ? SectionColors.light(strSection) : LINE);
        return gradientDrawable;
    }

    static void styleCard(View view) {
        view.setBackground(surface(view.getContext(), false, 20));
        view.setElevation(dp(view.getContext(), 2));
        view.setOutlineAmbientShadowColor(-5718581);
        view.setOutlineSpotShadowColor(-5718581);
    }

    static GradientDrawable surface(Context context, boolean z, int i) {
        GradientDrawable.Orientation orientation = GradientDrawable.Orientation.TL_BR;
        int[] iArr = new int[2];
        if (z) {
            iArr[0] = NAVY;
            iArr[1] = -15315360;
        } else {
            iArr[0] = WHITE;
            iArr[1] = -591108;
        }
        GradientDrawable gradientDrawable = new GradientDrawable(orientation, iArr);
        gradientDrawable.setCornerRadius(dp(context, i));
        gradientDrawable.setStroke(dp(context, 1), z ? -13273209 : LINE);
        return gradientDrawable;
    }
}
