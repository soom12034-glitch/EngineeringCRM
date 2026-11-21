package com.amalaei.engineering;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/**
 * Full-screen post editor: up to ten photos, GIFs or videos, a promotional designer with a live preview, a slideshow
 * video maker and the caption that goes with them.
 */
public final class PostEditorActivity extends BaseSecureActivity {
    static final String EXTRA_POST = "postId";
    private static final int PICK_IMAGES = 91, PICK_VIDEO = 92, PICK_LOGO = 93;
    private static final String[] CATEGORIES = {"تعريف", "عرض", "معلومة", "نصيحة", "مقارنة", "تجربة استخدام"};
    private static final int INK = -15189940, MUTED = -10192758, LINE = -2299923, FIELD = -525829, DANGER = -4959479;

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final HashMap<String, Bitmap> previewPhotos = new HashMap<>();
    private final ArrayList<String> imported = new ArrayList<>();
    private Store store;
    private Typeface font;
    private JSONObject post;
    private JSONObject design;
    private ArrayList<String> media = new ArrayList<>();
    private Bitmap logo;
    private int accent;

    private LinearLayout body, mediaRow, sections;
    private HorizontalScrollView strip;
    private TextView mediaCount;
    private ImageView preview;
    private EditText title, caption, plannedDate;
    private Spinner category;
    private final ArrayList<TextView> templateChips = new ArrayList<>(), sizeChips = new ArrayList<>(), fitChips = new ArrayList<>();
    private final ArrayList<View> paletteDots = new ArrayList<>();
    private int renderToken;
    private boolean saved;

    private String tr(String arabic, String english) {
        return AppLanguage.text(this, arabic, english);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        font = UiTheme.font(this);
        store = new Store(this);
        accent = SectionColors.dark("ads");
        long id = getIntent().getLongExtra(EXTRA_POST, 0);
        post = id > 0 ? store.post(id) : new JSONObject();
        try {
            if (post.optLong("id") == 0) post.put("business", AppMode.MODE).put("category", CATEGORIES[0]);
            media = PostMedia.names(post);
            JSONObject profile = store.profile(AppMode.MODE);
            String brand = profile.optString("brand").isEmpty() ? profile.optString("name") : profile.optString("brand");
            String phone = Profiles.firstPhone(profile.optString("phones"));
            JSONObject defaults = PostContent.defaults(brand, phone, profile.optString("whatsapp"), profile.optString("website"));
            JSONObject saved = post.optString("design").isEmpty() ? new JSONObject() : new JSONObject(post.optString("design"));
            design = defaults;
            java.util.Iterator<String> keys = saved.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                design.put(key, saved.get(key));
            }
            if (post.optString("design").isEmpty() && !post.optString("title").isEmpty()) design.put("headline", post.optString("title"));
            logo = profile.optString("logo").isEmpty() ? null : QuoteBranding.bitmap(profile.optString("logo"));
        } catch (Exception e) {
            design = PostContent.defaults("", "", "", "");
        }
        build();
    }

    @Override
    public void onPostCreate(Bundle state) {
        super.onPostCreate(state);
        AppLanguage.apply(this);
        AppLanguage.watch(this);
    }

    @Override
    protected void onDestroy() {
        if (!saved) for (String name : imported) store.removeUnusedImage(name);
        worker.shutdownNow();
        store.close();
        super.onDestroy();
    }

    // ------------------------------------------------------------------ layout

    private void build() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setLayoutDirection(AppLanguage.direction(this));
        body.setPadding(dp(16), dp(16), dp(16), dp(28));
        body.setBackgroundColor(-788486);
        scroll.addView(body);
        setContentView(scroll);
        UiTheme.screen(this, scroll);
        UiTheme.header(body, post.optLong("id") > 0 ? tr("تعديل المنشور", "Edit post") : tr("منشور جديد", "New post"), "ads");

        LinearLayout mediaCard = UiTheme.card(body);
        heading(mediaCard, tr("الصور والفيديو", "Photos and video"));
        mediaCount = muted(mediaCard, "");
        strip = new HorizontalScrollView(this);
        strip.setHorizontalScrollBarEnabled(false);
        mediaRow = new LinearLayout(this);
        mediaRow.setOrientation(LinearLayout.HORIZONTAL);
        mediaRow.setLayoutDirection(AppLanguage.direction(this));
        strip.addView(mediaRow);
        mediaCard.addView(strip, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout mediaButtons = row(mediaCard);
        weighted(button(mediaButtons, tr("+ إضافة صور", "+ Add photos"), () -> pick(PICK_IMAGES), false), button(mediaButtons, tr("+ فيديو أو GIF", "+ Video or GIF"), () -> pick(PICK_VIDEO), false));
        button(mediaCard, tr("🎬 صنع فيديو عرض شرائح من الصور", "🎬 Make a slideshow video from the photos"), this::makeVideo, false);
        muted(mediaCard, tr("حتى 10 ملفات. الصورة الأولى هي غلاف المنشور. اضغط على أي ملف لترتيبه أو حذفه. الصور الكبيرة تُصغَّر تلقائيًا بجودة عالية.", "Up to 10 files. The first is the cover. Tap a file to reorder or remove it. Large photos are resized automatically."));

        LinearLayout designCard = UiTheme.card(body);
        heading(designCard, tr("التصميم الدعائي", "Promotional design"));
        FrameLayout frame = new FrameLayout(this);
        frame.setBackground(shape(0xFFE9EEF2, 18, 0));
        preview = new ImageView(this);
        preview.setAdjustViewBounds(true);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        frame.addView(preview, new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER));
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(-1, -2);
        frameParams.setMargins(0, dp(6), 0, dp(10));
        designCard.addView(frame, frameParams);

        chipCaption(designCard, tr("القالب", "Template"));
        LinearLayout templates = chipRow(designCard);
        for (int i = 0; i < PostContent.TEMPLATES.length; i++) {
            String key = PostContent.TEMPLATES[i];
            TextView chip = chip(templates, PostContent.TEMPLATE_LABELS[i], () -> set("template", key));
            chip.setTag(key);
            templateChips.add(chip);
        }
        chipCaption(designCard, tr("المقاس", "Size"));
        LinearLayout sizes = chipRow(designCard);
        for (int i = 0; i < PostContent.SIZES.length; i++) {
            String key = PostContent.SIZES[i];
            TextView chip = chip(sizes, PostContent.SIZE_LABELS[i], () -> set("size", key));
            chip.setTag(key);
            sizeChips.add(chip);
        }
        chipCaption(designCard, tr("الألوان", "Colours"));
        LinearLayout palettes = chipRow(designCard);
        for (int i = 0; i < PostDesigner.PALETTES.length; i++) {
            int index = i;
            View dot = new View(this);
            dot.setTag(i);
            dot.setContentDescription(PostDesigner.PALETTE_LABELS[i]);
            dot.setOnClickListener(v -> set("palette", index));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(44), dp(44));
            params.setMargins(dp(5), dp(4), dp(5), dp(8));
            palettes.addView(dot, params);
            paletteDots.add(dot);
        }
        chipCaption(designCard, tr("عرض الصورة", "Photo display"));
        LinearLayout fits = chipRow(designCard);
        String[][] fitOptions = {{"contain", tr("المنتج كاملًا", "Whole product")}, {"cover", tr("ملء الإطار", "Fill frame")}};
        for (String[] option : fitOptions) {
            TextView chip = chip(fits, option[1], () -> set("fit", option[0]));
            chip.setTag(option[0]);
            fitChips.add(chip);
        }

        text(designCard, "headline", tr("العنوان الرئيسي على الصورة", "Headline on the image"), false);
        text(designCard, "subtitle", tr("سطر تعريفي قصير (اختياري)", "Short line under the headline (optional)"), true);
        text(designCard, "badge", tr("شارة أعلى التصميم، مثل: جديد، عرض خاص (اختياري)", "Badge, e.g. New, Special offer (optional)"), false);

        sections = new LinearLayout(this);
        sections.setOrientation(LinearLayout.VERTICAL);
        designCard.addView(sections);
        section("showPrice", tr("السعر", "Price"), form -> {
            field(form, "price", tr("السعر الحالي", "Price"), false, true);
            field(form, "oldPrice", tr("السعر قبل الخصم (اختياري)", "Price before discount (optional)"), false, true);
            field(form, "currency", tr("العملة", "Currency"), false, false);
        });
        section("showDiscount", tr("ملصق الخصم", "Discount sticker"), form -> {
            field(form, "discount", tr("نص الخصم (اتركه فارغًا لحسابه من السعرين)", "Discount text (empty = from the two prices)"), false, false);
        });
        section("showOffer", tr("مدة العرض", "Offer period"), form -> field(form, "offer", tr("مثل: العرض ساري حتى 30 نوفمبر", "e.g. Valid until 30 November"), false, false));
        section("showFeatures", tr("المميزات أو الخدمات", "Features or services"), form -> field(form, "features", tr("ميزة في كل سطر (حتى 6)", "One per line (up to 6)"), true, false));
        section("showSpecs", tr("المواصفات / جدول المقارنة", "Specifications / comparison"), form -> {
            field(form, "specs", tr("سطر لكل بند: «الدقة: 1 ملم» — وللمقارنة: «الدقة | 1 ملم | 2 ملم»", "One per line: \"Accuracy: 1 mm\"; comparison: \"Accuracy | 1 mm | 2 mm\""), true, false);
            field(form, "nameA", tr("اسم المنتج الأول (للمقارنة)", "First product name (comparison)"), false, false);
            field(form, "nameB", tr("اسم المنتج الثاني (للمقارنة)", "Second product name (comparison)"), false, false);
        });
        section("showCta", tr("زر الدعوة", "Call to action"), form -> field(form, "cta", tr("مثل: اطلب الآن عبر واتساب", "e.g. Order now on WhatsApp"), false, false));
        section("showContact", tr("بيانات التواصل", "Contact details"), form -> {
            field(form, "phone", tr("الهاتف", "Phone"), false, false);
            field(form, "whatsapp", tr("واتساب", "WhatsApp"), false, false);
            field(form, "website", tr("الموقع الإلكتروني", "Website"), false, false);
        });
        section("showBrand", tr("اسم المنشأة", "Business name"), form -> field(form, "brand", tr("الاسم الظاهر على التصميم", "Name shown on the design"), false, false));
        section("showLogo", tr("الشعار", "Logo"), form -> {
            muted(form, logo == null ? tr("لا يوجد شعار محفوظ للمنشأة بعد.", "No logo saved for the business yet.") : tr("يُستخدم شعار المنشأة المحفوظ.", "The saved business logo is used."));
            button(form, tr("اختيار شعار من الهاتف", "Choose a logo"), () -> pick(PICK_LOGO), false);
        });

        button(designCard, tr("✓ إضافة التصميم إلى المنشور كغلاف", "✓ Add the design to the post as its cover"), this::applyDesign, true);
        button(designCard, tr("كتابة النص المصاحب من التصميم", "Write the caption from the design"), this::captionFromDesign, false);

        LinearLayout textCard = UiTheme.card(body);
        heading(textCard, tr("النص المصاحب والتنظيم", "Caption and planning"));
        title = input(textCard, tr("عنوان داخلي للمنشور", "Internal title"), post.optString("title"), false);
        caption = input(textCard, tr("النص الذي يُرسل مع الصور", "Text sent with the media"), post.optString("body"), true);
        caption.setMinLines(5);
        button(textCard, tr("إدراج بيانات المنشأة في النص", "Insert business details"), this::insertProfile, false);
        TextView categoryCaption = muted(textCard, tr("نوع المحتوى", "Content type"));
        categoryCaption.setTextSize(13);
        category = new Spinner(this);
        category.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORIES));
        for (int i = 0; i < CATEGORIES.length; i++) if (CATEGORIES[i].equals(post.optString("category"))) category.setSelection(i);
        textCard.addView(category);
        plannedDate = input(textCard, tr("يوم النشر المخطط (اختياري)", "Planned day (optional)"), post.optString("plannedDate"), false);
        plannedDate.setFocusable(false);
        plannedDate.setOnClickListener(v -> pickDate());
        LinearLayout dateRow = row(textCard);
        weighted(button(dateRow, tr("اختيار اليوم", "Choose day"), this::pickDate, false), button(dateRow, tr("إزالة اليوم", "Clear day"), () -> plannedDate.setText(""), false));

        button(body, tr("حفظ المنشور", "Save post"), this::save, true);
        if (post.optLong("id") > 0) {
            Button delete = button(body, tr("حذف المنشور", "Delete post"), this::confirmDelete, false);
            delete.setTextColor(DANGER);
        }
        button(body, tr("إلغاء", "Cancel"), this::finish, false);

        refreshMedia();
        refreshDesignControls();
        schedulePreview();
    }

    private interface FormBuilder {
        void build(LinearLayout form);
    }

    /** A switch that shows its fields only while it is on. */
    private void section(String key, String label, FormBuilder builder) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(6), dp(12), dp(6));
        box.setBackground(shape(0xFFF4F7F9, 14, LINE));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(5), 0, dp(5));
        sections.addView(box, params);
        Switch toggle = new Switch(this);
        toggle.setText(label);
        toggle.setTypeface(font, Typeface.BOLD);
        toggle.setTextSize(15);
        toggle.setTextColor(INK);
        toggle.setChecked(design.optBoolean(key));
        box.addView(toggle, new LinearLayout.LayoutParams(-1, dp(48)));
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        builder.build(form);
        form.setVisibility(toggle.isChecked() ? View.VISIBLE : View.GONE);
        box.addView(form);
        toggle.setOnCheckedChangeListener((view, checked) -> {
            form.setVisibility(checked ? View.VISIBLE : View.GONE);
            set(key, checked);
        });
    }

    private void heading(LinearLayout parent, String label) {
        TextView view = label(label, 18);
        view.setTypeface(font, Typeface.BOLD);
        parent.addView(view);
    }

    private TextView label(String value, int size) {
        TextView view = new TextView(this);
        view.setText(DisplayNumbers.latin(value));
        view.setTextSize(size);
        view.setTypeface(font);
        view.setTextColor(INK);
        view.setPadding(0, dp(4), 0, dp(4));
        return view;
    }

    private TextView muted(LinearLayout parent, String value) {
        TextView view = label(value, 13);
        view.setTextColor(MUTED);
        parent.addView(view);
        return view;
    }

    private void chipCaption(LinearLayout parent, String value) {
        TextView view = label(value, 13);
        view.setTextColor(MUTED);
        view.setPadding(0, dp(6), 0, 0);
        parent.addView(view);
    }

    private LinearLayout chipRow(LinearLayout parent) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutDirection(AppLanguage.direction(this));
        scroll.addView(row);
        parent.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
        return row;
    }

    private TextView chip(LinearLayout row, String text, Runnable action) {
        TextView chip = label(text, 13);
        chip.setGravity(Gravity.CENTER);
        chip.setSingleLine(true);
        chip.setPadding(dp(14), dp(7), dp(14), dp(7));
        chip.setOnClickListener(v -> {
            UiTheme.feedback(v);
            action.run();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMargins(dp(3), dp(3), dp(3), dp(6));
        row.addView(chip, params);
        return chip;
    }

    private void style(TextView chip, boolean selected) {
        chip.setTextColor(selected ? Color.WHITE : INK);
        chip.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22000000), shape(selected ? accent : Color.WHITE, 18, selected ? 0 : LINE), null));
    }

    private LinearLayout row(LinearLayout parent) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        parent.addView(row, new LinearLayout.LayoutParams(-1, -2));
        return row;
    }

    private void weighted(View... views) {
        for (View view : views) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1f);
            params.setMargins(dp(3), dp(4), dp(3), dp(4));
            view.setLayoutParams(params);
        }
    }

    private Button button(LinearLayout parent, String label, Runnable action, boolean primary) {
        Button button = new Button(this);
        button.setText(label);
        UiTheme.button(button, primary);
        button.setOnClickListener(v -> {
            UiTheme.feedback(v);
            action.run();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(4), 0, dp(4));
        parent.addView(button, params);
        return button;
    }

    private GradientDrawable shape(int color, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (stroke != 0) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private EditText input(LinearLayout parent, String label, String value, boolean multiline) {
        TextView caption = label(label, 13);
        caption.setTextColor(MUTED);
        parent.addView(caption);
        EditText edit = new EditText(this);
        edit.setText(value);
        edit.setBackground(shape(FIELD, 12, LINE));
        edit.setInputType(multiline ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES : InputType.TYPE_CLASS_TEXT);
        if (multiline) edit.setGravity(Gravity.TOP | Gravity.START);
        UiTheme.input(edit);
        parent.addView(edit, new LinearLayout.LayoutParams(-1, -2));
        return edit;
    }

    /** A design text field bound to {@code key}; every change refreshes the preview. */
    private void field(LinearLayout parent, String key, String label, boolean multiline, boolean numeric) {
        EditText edit = input(parent, label, design.optString(key), multiline);
        if (multiline) edit.setMinLines(3);
        if (numeric) edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edit.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                set(key, s.toString());
            }
        });
    }

    private void text(LinearLayout parent, String key, String label, boolean multiline) {
        field(parent, key, label, multiline, false);
    }

    private void set(String key, Object value) {
        try {
            design.put(key, value);
        } catch (Exception ignored) {
        }
        refreshDesignControls();
        schedulePreview();
    }

    private void refreshDesignControls() {
        for (TextView chip : templateChips) style(chip, chip.getTag().equals(design.optString("template")));
        for (TextView chip : sizeChips) style(chip, chip.getTag().equals(design.optString("size")));
        for (TextView chip : fitChips) style(chip, chip.getTag().equals(design.optString("fit", "contain")));
        for (View dot : paletteDots) {
            int index = (int) dot.getTag();
            int[] palette = PostDesigner.PALETTES[index];
            GradientDrawable circle = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{palette[0], palette[1]});
            circle.setShape(GradientDrawable.OVAL);
            boolean selected = design.optInt("palette") == index;
            circle.setStroke(dp(selected ? 4 : 2), selected ? palette[2] : LINE);
            dot.setBackground(circle);
        }
    }

    // ------------------------------------------------------------------ media

    private void refreshMedia() {
        mediaRow.removeAllViews();
        mediaCount.setText(DisplayNumbers.latin(media.size() + tr(" من 10 ملفات", " of 10 files")));
        for (int i = 0; i < media.size(); i++) {
            String name = media.get(i);
            int index = i;
            FrameLayout tile = new FrameLayout(this);
            tile.setBackground(shape(0xFFE9EEF2, 14, 0));
            tile.setClipToOutline(true);
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            tile.addView(image, new FrameLayout.LayoutParams(-1, -1));
            worker.execute(() -> {
                Bitmap thumb = PostMedia.thumbnail(this, name, 320);
                main.post(() -> image.setImageBitmap(thumb));
            });
            String tag = i == 0 ? tr("الغلاف", "Cover") : PostMedia.isVideo(name) ? "▶ " + tr("فيديو", "Video") : PostMedia.isGif(name) ? "GIF" : name.equals(design.optString("output")) ? tr("تصميم", "Design") : "";
            if (i == 0 && name.equals(design.optString("output"))) tag = tr("الغلاف • تصميم", "Cover • design");
            if (!tag.isEmpty()) {
                TextView badge = label(tag, 11);
                badge.setTextColor(Color.WHITE);
                badge.setPadding(dp(8), dp(2), dp(8), dp(2));
                badge.setBackground(shape(i == 0 ? accent : 0xCC000000, 10, 0));
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                params.bottomMargin = dp(6);
                tile.addView(badge, params);
            }
            tile.setOnClickListener(v -> mediaActions(index));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(112), dp(112));
            params.setMargins(dp(4), dp(6), dp(4), dp(6));
            mediaRow.addView(tile, params);
        }
        if (media.isEmpty()) {
            TextView empty = label(tr("لم تُضف صور بعد", "No photos yet"), 14);
            empty.setTextColor(MUTED);
            empty.setPadding(dp(8), dp(24), dp(8), dp(24));
            mediaRow.addView(empty);
        }
        // Show the cover first: in right-to-left layouts the strip starts at its right edge.
        strip.post(() -> strip.fullScroll(AppLanguage.english(this) ? View.FOCUS_LEFT : View.FOCUS_RIGHT));
    }

    private void mediaActions(int index) {
        String[] actions = {tr("اجعله الغلاف", "Make it the cover"), tr("تحريك للأمام", "Move earlier"), tr("تحريك للخلف", "Move later"), tr("حذف من المنشور", "Remove from post")};
        new FluentDialog(this).setItems(actions, (dialog, which) -> {
            String name = media.get(index);
            if (which == 0) {
                media.remove(index);
                media.add(0, name);
            } else if (which == 1 && index > 0) {
                media.remove(index);
                media.add(index - 1, name);
            } else if (which == 2 && index < media.size() - 1) {
                media.remove(index);
                media.add(index + 1, name);
            } else if (which == 3) {
                media.remove(index);
                if (name.equals(design.optString("output"))) set("output", "");
            }
            refreshMedia();
            schedulePreview();
        }).show();
    }

    private void pick(int request) {
        if (request != PICK_LOGO && media.size() >= PostMedia.MAX_ITEMS) {
            toast(tr("وصلت إلى الحد الأقصى: 10 ملفات للمنشور", "Limit reached: 10 files per post"));
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE);
        if (request == PICK_VIDEO) intent.setType("*/*").putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*", "image/gif"});
        else intent.setType("image/*");
        if (request == PICK_IMAGES) intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, request);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK || data == null) return;
        ArrayList<Uri> uris = new ArrayList<>();
        ClipData clip = data.getClipData();
        if (clip != null) for (int i = 0; i < clip.getItemCount(); i++) uris.add(clip.getItemAt(i).getUri());
        else if (data.getData() != null) uris.add(data.getData());
        if (uris.isEmpty()) return;
        if (request == PICK_LOGO) {
            importLogo(uris.get(0));
            return;
        }
        int room = PostMedia.MAX_ITEMS - media.size();
        if (uris.size() > room) toast(tr("أُضيف أول " + room + " فقط؛ الحد 10 ملفات", "Only the first " + room + " were added; the limit is 10"));
        List<Uri> chosen = uris.subList(0, Math.max(0, Math.min(room, uris.size())));
        ProgressDialog progress = progress(tr("جارٍ تجهيز الملفات…", "Preparing files…"));
        worker.execute(() -> {
            ArrayList<String> added = new ArrayList<>();
            String error = null;
            for (Uri uri : chosen) {
                try {
                    added.add(PostMedia.importUri(this, uri));
                } catch (Exception e) {
                    error = e.getMessage();
                }
            }
            String failure = error;
            main.post(() -> {
                progress.dismiss();
                for (String name : added) if (!media.contains(name)) {
                    media.add(name);
                    imported.add(name);
                }
                refreshMedia();
                schedulePreview();
                if (failure != null) alert(failure);
            });
        });
    }

    private void importLogo(Uri uri) {
        try {
            String encoded = QuoteBranding.importLogo(this, uri);
            JSONObject profile = store.profile(AppMode.MODE);
            profile.put("logo", encoded);
            if (!profile.optString("name").trim().isEmpty()) store.saveProfile(AppMode.MODE, profile);
            else toast(tr("الشعار مستخدم في هذا التصميم؛ أضف بيانات المنشأة لحفظه دائمًا", "Logo used for this design; add business details to keep it"));
            logo = QuoteBranding.bitmap(encoded);
            set("showLogo", true);
        } catch (Exception e) {
            alert(e.getMessage());
        }
    }

    // ------------------------------------------------------------------ design

    /** Photos used by the designer: stills and GIFs in post order, without an earlier design image. */
    private ArrayList<String> designSources() {
        ArrayList<String> sources = new ArrayList<>();
        for (String name : media) if (!PostMedia.isVideo(name) && !name.equals(design.optString("output"))) sources.add(name);
        return sources;
    }

    private List<Bitmap> photos(List<String> names, int edge, HashMap<String, Bitmap> cache) {
        ArrayList<Bitmap> out = new ArrayList<>();
        for (String name : names) {
            Bitmap bitmap = cache == null ? null : cache.get(name);
            if (bitmap == null) {
                try {
                    bitmap = PostMedia.decode(PostMedia.file(this, name), edge);
                } catch (Exception ignored) {
                }
                if (bitmap != null && cache != null) cache.put(name, bitmap);
            }
            if (bitmap != null) out.add(bitmap);
        }
        return out;
    }

    private Runnable previewTask;

    private void schedulePreview() {
        int token = ++renderToken;
        if (previewTask != null) main.removeCallbacks(previewTask);
        previewTask = () -> {
            JSONObject snapshot;
            try {
                snapshot = new JSONObject(design.toString());
            } catch (Exception e) {
                return;
            }
            ArrayList<String> sources = designSources();
            worker.execute(() -> {
                if (token != renderToken) return;
                try {
                    Bitmap image = PostDesigner.render(this, snapshot, photos(sources, 1100, previewPhotos), logo, 0.5f);
                    main.post(() -> {
                        if (token == renderToken) preview.setImageBitmap(image);
                    });
                } catch (Exception ignored) {
                }
            });
        };
        main.postDelayed(previewTask, 220);
    }

    private void applyDesign() {
        ProgressDialog progress = progress(tr("جارٍ إنشاء التصميم…", "Creating the design…"));
        JSONObject snapshot;
        try {
            snapshot = new JSONObject(design.toString());
        } catch (Exception e) {
            progress.dismiss();
            return;
        }
        ArrayList<String> sources = designSources();
        worker.execute(() -> {
            try {
                String name = PostDesigner.save(this, snapshot, photos(sources, 2048, null), logo);
                main.post(() -> {
                    progress.dismiss();
                    String previous = design.optString("output");
                    media.remove(previous);
                    media.remove(name);
                    if (media.size() >= PostMedia.MAX_ITEMS) media.remove(media.size() - 1);
                    media.add(0, name);
                    imported.add(name);
                    set("output", name);
                    if (caption.getText().toString().trim().isEmpty()) caption.setText(PostContent.caption(design));
                    if (title.getText().toString().trim().isEmpty()) title.setText(design.optString("headline"));
                    refreshMedia();
                    toast(tr("أُضيف التصميم كغلاف للمنشور", "Design added as the cover"));
                });
            } catch (Exception e) {
                main.post(() -> {
                    progress.dismiss();
                    alert(e.getMessage());
                });
            }
        });
    }

    private void captionFromDesign() {
        String text = PostContent.caption(design);
        if (text.isEmpty()) {
            toast(tr("اكتب عنوانًا أو فعّل بعض البنود أولًا", "Add a headline or turn on some blocks first"));
            return;
        }
        if (caption.getText().toString().trim().isEmpty()) caption.setText(text);
        else new FluentDialog(this).setMessage(tr("استبدال النص الحالي بنص مكتوب من التصميم؟", "Replace the current caption with one written from the design?"))
                .setNegativeButton(tr("إلغاء", "Cancel"), null).setPositiveButton(tr("استبدال", "Replace"), (d, w) -> caption.setText(text)).show();
    }

    // ------------------------------------------------------------------ slideshow

    private void makeVideo() {
        ArrayList<String> stills = new ArrayList<>();
        for (String name : media) if (!PostMedia.isVideo(name)) stills.add(name);
        if (stills.isEmpty()) {
            toast(tr("أضف صورًا أو تصميمًا أولًا", "Add photos or a design first"));
            return;
        }
        if (media.size() >= PostMedia.MAX_ITEMS) {
            toast(tr("احذف ملفًا لإفساح مكان للفيديو", "Remove a file to make room for the video"));
            return;
        }
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(tr("جارٍ صنع الفيديو…", "Making the video…"));
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMax(100);
        progress.setCancelable(false);
        progress.show();
        JSONObject snapshot;
        try {
            snapshot = new JSONObject(design.toString());
        } catch (Exception e) {
            progress.dismiss();
            return;
        }
        worker.execute(() -> {
            File out = null;
            try {
                out = PostVideoMaker.make(this, photos(stills, 1600, null), snapshot, logo, percent -> main.post(() -> progress.setProgress(percent)));
                String name = PostMedia.adoptVideo(this, out);
                main.post(() -> {
                    progress.dismiss();
                    if (!media.contains(name)) {
                        media.add(name);
                        imported.add(name);
                    }
                    refreshMedia();
                    toast(tr("أُضيف فيديو عرض الشرائح إلى المنشور", "Slideshow video added to the post"));
                });
            } catch (Exception e) {
                main.post(() -> {
                    progress.dismiss();
                    alert(tr("تعذر صنع الفيديو على هذا الجهاز: ", "Could not make the video on this device: ") + e.getMessage());
                });
            } finally {
                if (out != null) out.delete();
            }
        });
    }

    // ------------------------------------------------------------------ caption, date, save

    private void insertProfile() {
        JSONObject profile = store.profile(AppMode.MODE);
        StringBuilder text = new StringBuilder();
        String brand = profile.optString("brand").isEmpty() ? profile.optString("name") : profile.optString("brand");
        if (!brand.isEmpty()) text.append(brand);
        JSONObject choices = profile.optJSONObject("advertising");
        for (String key : Profiles.AD_FIELDS) {
            boolean byDefault = key.equals("address") || key.equals("phones") || key.equals("whatsapp") || key.equals("website");
            if (!(choices == null ? byDefault : choices.optBoolean(key, byDefault))) continue;
            String value = profile.optString(key).trim();
            if (value.isEmpty()) continue;
            if (text.length() > 0) text.append('\n');
            text.append(Profiles.label(key)).append(": ").append(value);
        }
        if (text.length() == 0) {
            alert(tr("أضف بيانات المنشأة من شاشة المنشورات أولًا", "Add the business details from the posts screen first"));
            return;
        }
        if (!caption.getText().toString().contains(text)) caption.append((caption.length() == 0 ? "" : "\n\n") + text);
    }

    private void pickDate() {
        LocalDate day;
        try {
            day = plannedDate.getText().toString().isEmpty() ? LocalDate.now() : LocalDate.parse(plannedDate.getText().toString());
        } catch (Exception e) {
            day = LocalDate.now();
        }
        new DatePickerDialog(this, (picker, year, month, date) -> plannedDate.setText(LocalDate.of(year, month + 1, date).toString()), day.getYear(), day.getMonthValue() - 1, day.getDayOfMonth()).show();
    }

    private void save() {
        try {
            if (title.getText().toString().trim().isEmpty() && !design.optString("headline").trim().isEmpty()) title.setText(design.optString("headline"));
            post.put("business", AppMode.MODE).put("title", title.getText().toString()).put("body", caption.getText().toString())
                    .put("category", CATEGORIES[category.getSelectedItemPosition()]).put("plannedDate", plannedDate.getText().toString())
                    .put("media", new org.json.JSONArray(media).toString()).put("image", media.isEmpty() ? "" : media.get(0)).put("design", design.toString());
            store.savePost(post);
            saved = true;
            for (String name : imported) if (!media.contains(name)) store.removeUnusedImage(name);
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            alert(e.getMessage());
        }
    }

    private void confirmDelete() {
        new FluentDialog(this).setMessage(tr("حذف هذا المنشور وملفاته وسجل مشاركته؟", "Delete this post, its files and sharing history?"))
                .setNegativeButton(tr("إلغاء", "Cancel"), null).setPositiveButton(tr("حذف", "Delete"), (d, w) -> {
                    store.removePost(post.optLong("id"));
                    saved = true;
                    for (String name : imported) store.removeUnusedImage(name);
                    finish();
                }).show();
    }

    private ProgressDialog progress(String message) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void alert(String message) {
        new FluentDialog(this).setMessage(message == null || message.isEmpty() ? tr("تعذر إتمام العملية", "Could not complete the action") : message).setPositiveButton(tr("حسنًا", "OK"), null).show();
    }
}
