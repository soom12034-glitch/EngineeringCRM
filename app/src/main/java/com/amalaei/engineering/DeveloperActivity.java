package com.amalaei.engineering;

import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** About-the-developer page: who built Tawasul and how to reach them. */
public final class DeveloperActivity extends BaseSecureActivity {
    private static final String NAME_AR = "هشام صقر", NAME_EN = "Hisham Saqr";
    private static final String PHONE_SA = "+966549349245", PHONE_EG = "+201147372720";
    private static final String EMAIL = "enginex@enginex2030.com", WEBSITE = "https://enginex2030.com/";
    private static final int DEEP = 0xFF071A2B, OCEAN = 0xFF0B3A4F, TEAL = 0xFF0F766E, MINT = 0xFF9FE1CB, GOLD = 0xFFE9C46A, WHATSAPP = 0xFF25D366;
    private Typeface font;
    private LinearLayout body;
    private int order;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        font = UiTheme.font(this);
        getWindow().setStatusBarColor(DEEP);
        getWindow().setNavigationBarColor(DEEP);
        getWindow().getDecorView().setSystemUiVisibility(0);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{DEEP, OCEAN, 0xFF0D5C63}));
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setLayoutDirection(AppLanguage.direction(this));
        body.setPadding(dp(20), dp(12), dp(20), dp(28));
        scroll.addView(body, new FrameLayout.LayoutParams(-1, -2));
        scroll.setOnApplyWindowInsetsListener((v, insets) -> {
            int top = Build.VERSION.SDK_INT >= 30 ? insets.getInsets(android.view.WindowInsets.Type.systemBars()).top : insets.getSystemWindowInsetTop();
            int bottom = Build.VERSION.SDK_INT >= 30 ? insets.getInsets(android.view.WindowInsets.Type.systemBars()).bottom : insets.getSystemWindowInsetBottom();
            v.setPadding(0, top, 0, bottom);
            return insets;
        });
        setContentView(scroll);
        build();
    }

    private void build() {
        ImageView back = new ImageView(this);
        back.setImageResource(R.drawable.ic_dev_back);
        back.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        back.setPadding(dp(8), dp(8), dp(8), dp(8));
        back.setBackground(round(0x1FFFFFFF, 0x33FFFFFF, 22));
        back.setContentDescription(tr("رجوع", "Back"));
        back.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        backParams.gravity = Gravity.START;
        body.addView(back, backParams);

        FrameLayout monogram = new FrameLayout(this);
        GradientDrawable ring = new GradientDrawable();
        ring.setShape(GradientDrawable.OVAL);
        ring.setGradientType(GradientDrawable.SWEEP_GRADIENT);
        ring.setColors(new int[]{GOLD, MINT, TEAL, GOLD});
        monogram.setBackground(ring);
        monogram.setElevation(dp(10));
        monogram.setOutlineSpotShadowColor(GOLD);
        TextView initials = text(tr("تواصل", "Tawasul"), AppLanguage.english(this) ? 22 : 28, Color.WHITE, true);
        initials.setGravity(Gravity.CENTER);
        GradientDrawable core = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{0xFF12324A, DEEP});
        core.setShape(GradientDrawable.OVAL);
        initials.setBackground(core);
        FrameLayout.LayoutParams coreParams = new FrameLayout.LayoutParams(-1, -1);
        coreParams.setMargins(dp(4), dp(4), dp(4), dp(4));
        monogram.addView(initials, coreParams);
        LinearLayout.LayoutParams monoParams = new LinearLayout.LayoutParams(dp(120), dp(120));
        monoParams.gravity = Gravity.CENTER_HORIZONTAL;
        monoParams.setMargins(0, dp(8), 0, dp(18));
        add(monogram, monoParams);

        TextView name = text(tr(NAME_AR, NAME_EN), 30, Color.WHITE, true);
        name.setGravity(Gravity.CENTER);
        add(name, wide());
        TextView role = text(tr("مطوّر تطبيق تواصل", "Developer of Tawasul"), 15, MINT, false);
        role.setGravity(Gravity.CENTER);
        add(role, wide());

        TextView brand = text("ENGINEX 2030", 12, GOLD, true);
        brand.setLetterSpacing(0.25f);
        brand.setPadding(dp(16), dp(6), dp(16), dp(6));
        brand.setBackground(round(0x1AE9C46A, 0x66E9C46A, 18));
        LinearLayout.LayoutParams brandParams = new LinearLayout.LayoutParams(-2, -2);
        brandParams.gravity = Gravity.CENTER_HORIZONTAL;
        brandParams.setMargins(0, dp(12), 0, dp(26));
        add(brand, brandParams);

        TextView whatsapp = text(tr("ابدأ محادثة على واتساب", "Chat on WhatsApp"), 17, Color.WHITE, true);
        whatsapp.setGravity(Gravity.CENTER);
        whatsapp.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_dev_chat, 0, 0, 0);
        whatsapp.setCompoundDrawablePadding(dp(10));
        whatsapp.setPadding(dp(18), dp(16), dp(18), dp(16));
        GradientDrawable green = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0xFF1FAF5A, WHATSAPP});
        green.setCornerRadius(dp(18));
        whatsapp.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33FFFFFF), green, null));
        whatsapp.setElevation(dp(6));
        whatsapp.setOnClickListener(v -> openWhatsApp(PHONE_SA));
        UiTheme.motion(whatsapp);
        add(whatsapp, spaced(0, 14));

        phoneCard("🇸🇦", tr("السعودية", "Saudi Arabia"), PHONE_SA, "+966 54 934 9245");
        phoneCard("🇪🇬", tr("مصر", "Egypt"), PHONE_EG, "+20 114 737 2720");
        linkCard(R.drawable.ic_dev_mail, 0xFFE0A03A, tr("البريد الإلكتروني", "Email"), EMAIL, () -> open(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + EMAIL))));
        linkCard(R.drawable.ic_dev_web, 0xFF3B82C4, tr("الموقع الرسمي", "Official website"), "enginex2030.com", () -> open(new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE))));

        LinearLayout chips = new LinearLayout(this);
        chips.setGravity(Gravity.CENTER);
        for (String chip : new String[]{tr("مجاني", "Free"), tr("بدون إعلانات", "No ads"), tr("بياناتك على جهازك", "Your data stays on your phone")}) {
            TextView label = text(chip, 12, MINT, false);
            label.setPadding(dp(10), dp(5), dp(10), dp(5));
            label.setBackground(round(0x149FE1CB, 0x339FE1CB, 14));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
            params.setMargins(dp(3), 0, dp(3), 0);
            chips.addView(label, params);
        }
        add(chips, spaced(14, 18));

        TextView footer = text(tr("تواصل", "Tawasul") + " • " + tr("الإصدار ", "Version ") + BuildConfig.VERSION_NAME, 12, 0x99FFFFFF, false);
        footer.setGravity(Gravity.CENTER);
        add(footer, wide());
        TextView hint = text(tr("اضغط مطولًا على أي وسيلة لنسخها", "Long-press any contact to copy it"), 11, 0x80FFFFFF, false);
        hint.setGravity(Gravity.CENTER);
        add(hint, wide());
    }

    private void phoneCard(String flag, String country, String number, String display) {
        LinearLayout card = glassCard();
        TextView badge = text(flag, 22, Color.WHITE, false);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(round(0x1FFFFFFF, 0x33FFFFFF, 14));
        badge.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        card.addView(badge, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout labels = labels(country, display);
        card.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        ImageView chat = actionButton(R.drawable.ic_dev_chat, WHATSAPP, tr("واتساب ", "WhatsApp ") + country);
        chat.setOnClickListener(v -> openWhatsApp(number));
        card.addView(chat, actionParams());
        ImageView call = actionButton(R.drawable.ic_dev_call, TEAL, tr("اتصال ", "Call ") + country);
        call.setOnClickListener(v -> open(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number))));
        card.addView(call, actionParams());
        card.setOnLongClickListener(v -> copy(number));
        add(card, spaced(0, 10));
    }

    private void linkCard(int icon, int color, String title, String value, Runnable action) {
        LinearLayout card = glassCard();
        card.addView(iconBubble(icon, color), new LinearLayout.LayoutParams(dp(44), dp(44)));
        card.addView(labels(title, value), new LinearLayout.LayoutParams(0, -2, 1));
        card.setClickable(true);
        card.setOnClickListener(v -> action.run());
        card.setOnLongClickListener(v -> copy(title.equals(tr("الموقع الرسمي", "Official website")) ? WEBSITE : value));
        UiTheme.motion(card);
        add(card, spaced(0, 10));
    }

    private LinearLayout glassCard() {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22FFFFFF), round(0x14FFFFFF, 0x2EFFFFFF, 20), null));
        return card;
    }

    private LinearLayout labels(String title, String value) {
        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(12), 0, dp(12), 0);
        labels.addView(text(title, 12, MINT, false));
        TextView number = text(value, 16, Color.WHITE, true);
        number.setTextDirection(View.TEXT_DIRECTION_LTR);
        number.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        labels.addView(number);
        return labels;
    }

    private ImageView iconBubble(int icon, int color) {
        ImageView view = new ImageView(this);
        view.setImageResource(icon);
        view.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        view.setPadding(dp(11), dp(11), dp(11), dp(11));
        GradientDrawable bubble = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{color, darker(color)});
        bubble.setCornerRadius(dp(14));
        view.setBackground(bubble);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return view;
    }

    private ImageView actionButton(int icon, int color, String description) {
        ImageView view = new ImageView(this);
        view.setImageResource(icon);
        view.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        view.setPadding(dp(10), dp(10), dp(10), dp(10));
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(color);
        view.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44FFFFFF), circle, null));
        view.setContentDescription(description);
        UiTheme.motion(view);
        return view;
    }

    private LinearLayout.LayoutParams actionParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(42));
        params.setMarginStart(dp(8));
        return params;
    }

    private void openWhatsApp(String number) {
        open(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + number.replace("+", ""))));
    }

    private void open(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, tr("لا يوجد تطبيق مناسب لفتح هذا الرابط", "No app is available to open this"), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean copy(String value) {
        getSystemService(ClipboardManager.class).setPrimaryClip(ClipData.newPlainText(tr(NAME_AR, NAME_EN), value));
        Toast.makeText(this, tr("تم النسخ: ", "Copied: ") + value, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void add(View view, LinearLayout.LayoutParams params) {
        body.addView(view, params);
        if (ValueAnimator.areAnimatorsEnabled()) {
            view.setAlpha(0f);
            view.setTranslationY(dp(18));
            view.animate().alpha(1f).translationY(0).setStartDelay(60L * order++).setDuration(420).start();
        }
    }

    private LinearLayout.LayoutParams wide() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams spaced(int top, int bottom) {
        LinearLayout.LayoutParams params = wide();
        params.setMargins(0, dp(top), 0, dp(bottom));
        return params;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(font, bold ? Typeface.BOLD : Typeface.NORMAL);
        return view;
    }

    private GradientDrawable round(int fill, int stroke, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(dp(radius));
        shape.setStroke(dp(1), stroke);
        return shape;
    }

    private static int darker(int color) {
        return Color.rgb(Color.red(color) * 3 / 4, Color.green(color) * 3 / 4, Color.blue(color) * 3 / 4);
    }

    private String tr(String arabic, String english) {
        return AppLanguage.text(this, arabic, english);
    }

    private int dp(int value) {
        return UiTheme.dp(this, value);
    }
}
