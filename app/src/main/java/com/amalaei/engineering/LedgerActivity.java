package com.amalaei.engineering;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;

public final class LedgerActivity extends BaseSecureActivity {
    private static final int INK = -15189940, MUTED = -10192758, LINE = -2299923, EXPENSE = -4959479, FIELD = -525829;
    private final int TEAL = SectionColors.dark("ledger");

    private LinearLayout body;
    private Typeface font;
    private Uri pendingPdf;
    private Store store;

    private final LedgerFilter filter = new LedgerFilter();
    private long filterBook = -1;
    private JSONObject book;
    private ArrayList<JSONObject> entries = new ArrayList<>();
    private final HashMap<Long, BigDecimal> balanceAfter = new HashMap<>();
    private LinearLayout entriesBox;
    private TextView resultLine;
    private final ArrayList<TextView> typeChips = new ArrayList<>(), periodChips = new ArrayList<>();
    private TextView sortChip, clearChip;

    private static final String[][] TYPES = {
            {LedgerFilter.ALL, "الكل", "All"},
            {LedgerFilter.IN, "التحصيلات", "Collections"},
            {LedgerFilter.OUT, "المصروفات", "Expenses"},
            {LedgerFilter.CASH, "كاش", "Cash"},
            {LedgerFilter.NETWORK, "شبكة", "Card"},
            {LedgerFilter.TRANSFER, "تحويل", "Transfer"},
            {LedgerFilter.MARKED, "المعلَّمة", "Flagged"}};
    private static final String[][] PERIODS = {
            {LedgerFilter.PERIOD_ALL, "كل الفترات", "All time"},
            {LedgerFilter.PERIOD_TODAY, "اليوم", "Today"},
            {LedgerFilter.PERIOD_MONTH, "هذا الشهر", "This month"},
            {LedgerFilter.PERIOD_LAST_MONTH, "الشهر الماضي", "Last month"},
            {LedgerFilter.PERIOD_CUSTOM, "فترة مخصصة", "Custom range"}};

    private String tr(String arabic, String english) {
        return AppLanguage.text(this, arabic, english);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void alert(Exception error) {
        new FluentDialog(this).setMessage(error.getMessage() == null ? tr("تعذر إتمام العملية", "Could not complete the action") : error.getMessage()).setPositiveButton(tr("حسنًا", "OK"), null).show();
    }

    private void base(String title) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setLayoutDirection(AppLanguage.direction(this));
        body.setPadding(dp(18), dp(16), dp(18), dp(24));
        body.setBackgroundColor(-788486);
        scroll.addView(body);
        setContentView(scroll);
        UiTheme.screen(this, scroll);
        UiTheme.header(body, title, "ledger");
        UiTheme.enter(body);
    }

    private Button button(LinearLayout parent, String label, Runnable action, boolean primary) {
        Button button = new Button(this);
        button.setText(label);
        UiTheme.button(button, primary);
        button.setOnClickListener(view -> {
            UiTheme.feedback(view);
            action.run();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(4), 0, dp(4));
        parent.addView(button, params);
        return button;
    }

    private void weighted(View... views) {
        for (View view : views) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1f);
            params.setMargins(dp(3), dp(4), dp(3), dp(4));
            view.setLayoutParams(params);
        }
    }

    private TextView text(String value, int size) {
        TextView view = new TextView(this);
        view.setText(DisplayNumbers.latin(value));
        view.setTextSize(size);
        view.setTypeface(font);
        view.setTextColor(INK);
        view.setPadding(0, dp(4), 0, dp(4));
        return view;
    }

    private void muted(LinearLayout parent, String value) {
        TextView view = text(value, 14);
        view.setTextColor(MUTED);
        parent.addView(view);
    }

    private GradientDrawable shape(int color, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (stroke != 0) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private EditText field(LinearLayout parent, String label, String value, boolean numeric) {
        TextView caption = text(label, 13);
        caption.setTextColor(MUTED);
        parent.addView(caption);
        EditText edit = new EditText(this);
        edit.setTypeface(font);
        edit.setText(value);
        edit.setTextSize(16);
        edit.setPadding(dp(12), dp(6), dp(12), dp(6));
        edit.setBackground(shape(FIELD, 12, LINE));
        if (numeric) edit.setInputType(8194);
        UiTheme.input(edit);
        parent.addView(edit, new LinearLayout.LayoutParams(-1, -2));
        return edit;
    }

    // ---------------------------------------------------------------- notebooks list

    public void list() {
        base(tr("الدفاتر", "Ledgers"));
        muted(body, tr("سجّل العهدة والتحصيلات والمصروفات فورًا؛ الرصيد يتغير مع كل بند.", "Record custody, collections and expenses as they happen; the balance updates with every entry."));
        button(body, "+ إنشاء دفتر جديد", this::newNotebook, true);
        ArrayList<JSONObject> notebooks = store.query("SELECT * FROM notebooks ORDER BY createdAt DESC");
        if (notebooks.isEmpty()) muted(body, tr("أنشئ دفترًا مثل «عهدة المندوب» أو «مصروفات السيارة».", "Create a ledger such as \"Rep custody\" or \"Car expenses\"."));
        for (JSONObject notebook : notebooks) {
            ArrayList<JSONObject> rows = store.query("SELECT * FROM ledgerEntries WHERE notebookId=? ORDER BY at DESC,id DESC", String.valueOf(notebook.optLong("id")));
            LinearLayout card = UiTheme.card(body);
            card.addView(text(notebook.optString("name"), 20));
            TextView balance = text(tr("الرصيد الحالي  ", "Current balance  ") + money(LedgerMath.balance(notebook, rows)), 20);
            balance.setTextColor(TEAL);
            card.addView(balance);
            TextView details = text(tr("افتتاحي ", "Opening ") + money(LedgerMath.opening(notebook)) + "  •  " + tr("تحصيلات ", "Collections ") + money(LedgerMath.totalCollections(rows))
                    + "  •  " + tr("مصروفات ", "Expenses ") + money(LedgerMath.total(rows, "out")) + "  •  " + rows.size() + tr(" حركة", " entries"), 13);
            details.setTextColor(MUTED);
            card.addView(details);
            button(card, "فتح الدفتر", () -> open(notebook.optLong("id")), false);
        }
    }

    private String money(BigDecimal value) {
        return LedgerMath.grouped(value) + tr(" ر.س", " SAR");
    }

    public void newNotebook() {
        LinearLayout form = dialogForm();
        EditText name = field(form, tr("اسم الدفتر", "Ledger name"), "", false);
        EditText opening = field(form, tr("الرصيد الافتتاحي (اختياري)", "Opening balance (optional)"), "", true);
        new FluentDialog(this).setTitle(tr("دفتر جديد", "New ledger")).setView(form).setNegativeButton(tr("إلغاء", "Cancel"), null).setPositiveButton(tr("إنشاء", "Create"), (dialog, which) -> {
            try {
                open(store.saveNotebook(new JSONObject().put("name", name.getText().toString()).put("openingBalance", opening.getText().toString())));
            } catch (Exception e) {
                alert(e);
            }
        }).show();
    }

    private LinearLayout dialogForm() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), dp(8));
        return form;
    }

    // ---------------------------------------------------------------- one notebook

    private void open(long id) {
        ArrayList<JSONObject> found = store.query("SELECT * FROM notebooks WHERE id=?", String.valueOf(id));
        if (found.isEmpty()) {
            list();
            return;
        }
        if (filterBook != id) {
            filter.reset();
            filterBook = id;
        }
        book = found.get(0);
        entries = store.query("SELECT * FROM ledgerEntries WHERE notebookId=? ORDER BY at DESC,id DESC", String.valueOf(id));
        balanceAfter.clear();
        ArrayList<JSONObject> ordered = LedgerMath.chronological(entries);
        BigDecimal[] running = LedgerMath.running(LedgerMath.opening(book), ordered);
        for (int i = 0; i < ordered.size(); i++) balanceAfter.put(ordered.get(i).optLong("id"), running[i]);

        base(book.optString("name"));
        LinearLayout cash = UiTheme.card(body);
        cash.setBackground(UiTheme.surface(this, true, 20));
        TextView caption = text(tr("النقدية لدى المندوب", "Cash held by representative"), 13);
        caption.setTextColor(-3217685);
        cash.addView(caption);
        TextView amount = text(money(LedgerMath.balance(book, entries)), 30);
        amount.setTextColor(-1);
        amount.setTextDirection(View.TEXT_DIRECTION_LTR);
        cash.addView(amount);
        metrics(tr("رصيد افتتاحي", "Opening balance"), LedgerMath.opening(book), tr("المصروفات", "Expenses"), LedgerMath.total(entries, "out"));
        metrics(tr("تحصيل كاش", "Cash collected"), LedgerMath.collections(entries, "cash"), tr("تحصيل شبكة", "Card collected"), LedgerMath.collections(entries, "network"));
        metrics(tr("تحصيل تحويل", "Bank transfers"), LedgerMath.collections(entries, "transfer"), tr("صافي التحصيل", "Net collections"), LedgerMath.netActivity(entries));

        button(body, "+ إضافة بند", () -> editEntry(new JSONObject(), false), true);
        controls();

        LinearLayout statement = UiTheme.card(body);
        TextView statementTitle = text(tr("كشف الحساب PDF", "PDF statement"), 17);
        statement.addView(statementTitle);
        muted(statement, tr("يشمل الكشف الفترة المختارة كاملة مع رصيد أول المدة والرصيد بعد كل حركة.", "The statement covers the selected period with the carried-in balance and the balance after each entry."));
        LinearLayout pdfRow = new LinearLayout(this);
        pdfRow.setOrientation(LinearLayout.HORIZONTAL);
        statement.addView(pdfRow);
        weighted(button(pdfRow, "معاينة PDF", this::previewPdf, false), button(pdfRow, "مشاركة PDF", this::sharePdf, false), button(pdfRow, "حفظ PDF", this::savePdf, false));

        TextView heading = text(tr("حركات الدفتر", "Transactions"), 20);
        body.addView(heading);
        resultLine = text("", 13);
        resultLine.setTextColor(MUTED);
        body.addView(resultLine);
        entriesBox = new LinearLayout(this);
        entriesBox.setOrientation(LinearLayout.VERTICAL);
        body.addView(entriesBox, new LinearLayout.LayoutParams(-1, -2));
        refresh();

        button(body, "إعدادات الدفتر", this::editBook, false);
        button(body, "العودة إلى الدفاتر", this::list, false);
    }

    private void metrics(String firstLabel, BigDecimal first, String secondLabel, BigDecimal second) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        body.addView(row);
        for (int i = 0; i < 2; i++) {
            LinearLayout card = UiTheme.card(row);
            weighted(card);
            TextView label = text(i == 0 ? firstLabel : secondLabel, 12);
            label.setTextColor(MUTED);
            card.addView(label);
            TextView value = text(money(i == 0 ? first : second), 17);
            value.setTextColor(TEAL);
            value.setTextDirection(View.TEXT_DIRECTION_LTR);
            card.addView(value);
        }
    }

    /** Search box plus type, period and order chips; changes only rebuild the entry list so typing keeps focus. */
    private void controls() {
        LinearLayout card = UiTheme.card(body);
        card.addView(text(tr("أدوات الدفتر", "Ledger tools"), 17));
        EditText search = new EditText(this);
        search.setHint(tr("ابحث في البيان أو الملاحظة أو المبلغ", "Search description, note or amount"));
        search.setSingleLine(true);
        search.setText(filter.query);
        search.setBackground(shape(FIELD, 12, LINE));
        UiTheme.input(search);
        card.addView(search, new LinearLayout.LayoutParams(-1, -2));
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                filter.query = s.toString();
                refresh();
            }
        });

        typeChips.clear();
        LinearLayout types = chipRow(card, tr("النوع", "Type"));
        for (String[] type : TYPES) {
            TextView chip = chip(types, tr(type[1], type[2]), () -> {
                filter.type = type[0];
                refresh();
            });
            chip.setTag(type[0]);
            typeChips.add(chip);
        }

        periodChips.clear();
        LinearLayout periods = chipRow(card, tr("الفترة", "Period"));
        for (String[] period : PERIODS) {
            TextView chip = chip(periods, tr(period[1], period[2]), () -> {
                if (LedgerFilter.PERIOD_CUSTOM.equals(period[0])) pickRange();
                else {
                    filter.setPeriod(period[0], 0, 0, System.currentTimeMillis());
                    refresh();
                }
            });
            chip.setTag(period[0]);
            periodChips.add(chip);
        }

        LinearLayout order = chipRow(card, tr("الترتيب", "Order"));
        sortChip = chip(order, "", () -> {
            filter.newestFirst = !filter.newestFirst;
            refresh();
        });
        clearChip = chip(order, tr("✕ مسح الفلاتر", "✕ Clear filters"), () -> {
            filter.reset();
            search.setText("");
            refresh();
        });
    }

    private LinearLayout chipRow(LinearLayout parent, String label) {
        TextView caption = text(label, 12);
        caption.setTextColor(MUTED);
        parent.addView(caption);
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutDirection(AppLanguage.direction(this));
        scroll.addView(row);
        parent.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
        return row;
    }

    private TextView chip(LinearLayout row, String label, Runnable action) {
        TextView chip = text(label, 13);
        chip.setGravity(Gravity.CENTER);
        chip.setSingleLine(true);
        chip.setPadding(dp(14), dp(7), dp(14), dp(7));
        chip.setOnClickListener(view -> {
            UiTheme.feedback(view);
            action.run();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMargins(dp(3), dp(3), dp(3), dp(6));
        row.addView(chip, params);
        return chip;
    }

    private void style(TextView chip, boolean selected) {
        chip.setTextColor(selected ? -1 : INK);
        chip.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22000000), shape(selected ? TEAL : -1, 18, selected ? 0 : LINE), null));
    }

    private void pickRange() {
        Calendar start = Calendar.getInstance();
        if (LedgerFilter.PERIOD_CUSTOM.equals(filter.period)) start.setTimeInMillis(filter.from);
        else start.set(Calendar.DAY_OF_MONTH, 1);
        DatePickerDialog first = new DatePickerDialog(this, (picker, year, month, day) -> {
            Calendar from = Calendar.getInstance();
            from.set(year, month, day);
            Calendar end = Calendar.getInstance();
            if (LedgerFilter.PERIOD_CUSTOM.equals(filter.period)) end.setTimeInMillis(filter.to);
            if (end.before(from)) end.setTimeInMillis(from.getTimeInMillis());
            DatePickerDialog second = new DatePickerDialog(this, (picker2, year2, month2, day2) -> {
                Calendar to = Calendar.getInstance();
                to.set(year2, month2, day2);
                filter.setPeriod(LedgerFilter.PERIOD_CUSTOM, from.getTimeInMillis(), to.getTimeInMillis(), System.currentTimeMillis());
                refresh();
            }, end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH));
            second.setTitle(tr("إلى تاريخ", "To date"));
            second.show();
        }, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
        first.setTitle(tr("من تاريخ", "From date"));
        first.show();
    }

    private void refresh() {
        if (entriesBox == null) return;
        for (TextView chip : typeChips) style(chip, chip.getTag().equals(filter.type));
        SimpleDateFormat day = new SimpleDateFormat("dd/MM", Locale.US);
        for (TextView chip : periodChips) {
            boolean selected = chip.getTag().equals(filter.period);
            style(chip, selected);
            if (LedgerFilter.PERIOD_CUSTOM.equals(chip.getTag()))
                chip.setText(selected ? day.format(new Date(filter.from)) + " – " + day.format(new Date(filter.to)) : tr("فترة مخصصة", "Custom range"));
        }
        sortChip.setText(filter.newestFirst ? tr("↓ الأحدث أولًا", "↓ Newest first") : tr("↑ الأقدم أولًا", "↑ Oldest first"));
        style(sortChip, false);
        clearChip.setVisibility(filter.active() || !filter.newestFirst ? View.VISIBLE : View.GONE);
        style(clearChip, false);
        clearChip.setTextColor(EXPENSE);

        ArrayList<JSONObject> shown = filter.apply(entries);
        resultLine.setText(DisplayNumbers.latin(shown.size() + tr(" حركة", " entries") + "  •  " + tr("تحصيل ", "In ") + money(LedgerMath.totalCollections(shown))
                + "  •  " + tr("مصروف ", "Out ") + money(LedgerMath.total(shown, "out"))));
        entriesBox.removeAllViews();
        if (entries.isEmpty()) {
            muted(entriesBox, tr("لا توجد بنود بعد. أضف تحصيلًا أو مصروفًا وسيُحدّث الرصيد فورًا.", "No entries yet. Add a collection or an expense and the balance updates instantly."));
            return;
        }
        if (shown.isEmpty()) {
            muted(entriesBox, tr("لا توجد حركات تطابق الفلاتر الحالية.", "No entries match the current filters."));
            return;
        }
        muted(entriesBox, tr("اضغط على الحركة لتعديلها، واضغط مطولًا لنسخها أو حذفها.", "Tap an entry to edit it; long-press to duplicate or delete it."));
        int limit = Math.min(shown.size(), 300);
        for (int i = 0; i < limit; i++) entryCard(shown.get(i));
        if (shown.size() > limit) muted(entriesBox, tr("تُعرض أول 300 حركة؛ استخدم البحث أو الفترة لتضييق النتائج.", "Showing the first 300 entries; use search or a period to narrow the results."));
    }

    private void entryCard(JSONObject entry) {
        LinearLayout card = UiTheme.card(entriesBox);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top);
        TextView title = text(entry.optString("title"), 17);
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));
        boolean in = "in".equals(entry.optString("kind"));
        TextView amount = text((in ? "+ " : "− ") + money(LedgerMath.value(entry)), 17);
        amount.setTextColor(in ? TEAL : EXPENSE);
        top.addView(amount);
        String marker = entry.optString("marker");
        String when = new SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.US).format(new Date(entry.optLong("at")));
        TextView meta = text((marker.isEmpty() ? "" : marker + "  •  ") + when + "  •  " + (in ? tr("تحصيل ", "Collection · ") + LedgerMath.methodLabel(entry.optString("paymentMethod", "cash")) : tr("مصروف نقدي", "Cash expense")), 13);
        meta.setTextColor(MUTED);
        card.addView(meta);
        if (!entry.optString("note").isEmpty()) {
            TextView note = text(tr("ملاحظة: ", "Note: ") + entry.optString("note"), 14);
            note.setTextColor(MUTED);
            card.addView(note);
        }
        BigDecimal after = balanceAfter.get(entry.optLong("id"));
        if (after != null) {
            TextView balance = text(tr("النقدية بعد الحركة: ", "Cash after entry: ") + money(after), 12);
            balance.setTextColor(after.signum() < 0 ? EXPENSE : TEAL);
            card.addView(balance);
        }
        card.setForeground(new RippleDrawable(ColorStateList.valueOf(0x1A000000), null, shape(-1, 18, 0)));
        card.setOnClickListener(view -> editEntry(entry, false));
        card.setOnLongClickListener(view -> {
            entryActions(entry);
            return true;
        });
    }

    private void entryActions(JSONObject entry) {
        String[] actions = {tr("تعديل الحركة", "Edit entry"), tr("نسخ كحركة جديدة", "Duplicate as new entry"), tr("حذف الحركة", "Delete entry")};
        new FluentDialog(this).setTitle(entry.optString("title")).setItems(actions, (dialog, which) -> {
            if (which == 0) editEntry(entry, false);
            else if (which == 1) editEntry(entry, true);
            else confirmDelete(entry);
        }).show();
    }

    private void confirmDelete(JSONObject entry) {
        new FluentDialog(this).setMessage(tr("حذف هذا البند؟", "Delete this entry?")).setNegativeButton(tr("إلغاء", "Cancel"), null).setPositiveButton(tr("حذف", "Delete"), (dialog, which) -> {
            store.removeLedgerEntry(entry.optLong("id"));
            open(book.optLong("id"));
        }).show();
    }

    /** Add, edit or duplicate an entry; duplicates keep the details but get a new id and the current time. */
    private void editEntry(JSONObject source, boolean duplicate) {
        JSONObject entry;
        try {
            entry = new JSONObject(source.toString());
        } catch (Exception unused) {
            entry = new JSONObject();
        }
        if (duplicate) {
            entry.remove("id");
            entry.remove("at");
        }
        final JSONObject draft = entry;
        boolean existing = draft.optLong("id") > 0;
        final long[] when = {draft.optLong("at", System.currentTimeMillis())};
        LinearLayout form = dialogForm();
        EditText title = field(form, tr("وصف العملية", "Description"), draft.optString("title"), false);
        EditText amount = field(form, tr("المبلغ", "Amount"), draft.optString("amount"), true);
        EditText note = field(form, tr("ملاحظة مهمة للطباعة", "Note shown on the statement"), draft.optString("note"), false);

        TextView dateCaption = text(tr("تاريخ ووقت الحركة", "Date and time"), 13);
        dateCaption.setTextColor(MUTED);
        form.addView(dateCaption);
        Button date = new Button(this);
        SimpleDateFormat stamp = new SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.US);
        date.setText(stamp.format(new Date(when[0])));
        UiTheme.button(date, false);
        form.addView(date, new LinearLayout.LayoutParams(-1, -2));
        date.setOnClickListener(view -> {
            Calendar current = Calendar.getInstance();
            current.setTimeInMillis(when[0]);
            new DatePickerDialog(this, (picker, year, month, day) -> new TimePickerDialog(this, (clock, hour, minute) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, day, hour, minute, 0);
                chosen.set(Calendar.MILLISECOND, 0);
                when[0] = chosen.getTimeInMillis();
                date.setText(stamp.format(new Date(when[0])));
            }, current.get(Calendar.HOUR_OF_DAY), current.get(Calendar.MINUTE), true).show(),
                    current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH)).show();
        });

        Spinner kind = spinner(form, tr("نوع العملية", "Type"), new String[]{tr("تحصيل", "Collection"), tr("مصروف نقدي", "Cash expense")}, "out".equals(draft.optString("kind")) ? 1 : 0);
        String[] methods = {"cash", "network", "transfer"};
        int methodIndex = 0;
        for (int i = 0; i < methods.length; i++) if (methods[i].equals(draft.optString("paymentMethod", "cash"))) methodIndex = i;
        TextView methodCaption = text(tr("طريقة التحصيل", "Collection method"), 13);
        Spinner method = spinner(form, null, new String[]{tr("كاش لدى المندوب", "Cash with representative"), tr("دفع شبكة للشركة", "Card payment to company"), tr("تحويل لحساب الشركة", "Transfer to company account")}, methodIndex);
        methodCaption.setTextColor(MUTED);
        form.addView(methodCaption, form.indexOfChild(method));
        kind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int visibility = position == 0 ? View.VISIBLE : View.GONE;
                methodCaption.setVisibility(visibility);
                method.setVisibility(visibility);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        String[] markers = {"بدون علامة", "⭐ مهم", "🔴 يحتاج مراجعة", "✓ تم", "✕ ملغي"};
        int markerIndex = 0;
        for (int i = 0; i < markers.length; i++) if (markers[i].equals(draft.optString("marker"))) markerIndex = i;
        Spinner marker = spinner(form, tr("علامة تذكير", "Flag"), new String[]{tr(markers[0], "No flag"), tr(markers[1], "⭐ Important"), tr(markers[2], "🔴 Needs review"), tr(markers[3], "✓ Done"), tr(markers[4], "✕ Cancelled")}, markerIndex);

        String heading = duplicate ? tr("نسخ حركة", "Duplicate entry") : existing ? tr("تعديل بند", "Edit entry") : tr("إضافة بند", "Add entry");
        AlertDialog dialog = new FluentDialog(this).setTitle(heading).setView(form).setNegativeButton(tr("إلغاء", "Cancel"), null).setPositiveButton(tr("حفظ", "Save"), null).create();
        if (existing) dialog.setButton(AlertDialog.BUTTON_NEUTRAL, tr("حذف", "Delete"), (d, which) -> confirmDelete(draft));
        dialog.setOnShowListener(shown -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            try {
                boolean collection = kind.getSelectedItemPosition() == 0;
                draft.put("notebookId", book.optLong("id")).put("title", title.getText().toString()).put("amount", amount.getText().toString())
                        .put("kind", collection ? "in" : "out").put("paymentMethod", collection ? methods[method.getSelectedItemPosition()] : "cash")
                        .put("marker", marker.getSelectedItemPosition() == 0 ? "" : markers[marker.getSelectedItemPosition()])
                        .put("note", note.getText().toString()).put("at", when[0]);
                store.saveLedgerEntry(draft);
                dialog.dismiss();
                open(book.optLong("id"));
            } catch (Exception e) {
                alert(e);
            }
        }));
        dialog.show();
    }

    private Spinner spinner(LinearLayout form, String label, String[] items, int selected) {
        if (label != null) {
            TextView caption = text(label, 13);
            caption.setTextColor(MUTED);
            form.addView(caption);
        }
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items));
        spinner.setSelection(selected);
        form.addView(spinner);
        return spinner;
    }

    private void editBook() {
        LinearLayout form = dialogForm();
        EditText name = field(form, tr("اسم الدفتر", "Ledger name"), book.optString("name"), false);
        EditText opening = field(form, tr("الرصيد الافتتاحي", "Opening balance"), book.optString("openingBalance"), true);
        JSONObject current = book;
        new FluentDialog(this).setTitle(tr("إعدادات الدفتر", "Ledger settings")).setView(form).setNegativeButton(tr("إلغاء", "Cancel"), null).setPositiveButton(tr("حفظ", "Save"), (dialog, which) -> {
            try {
                current.put("name", name.getText().toString()).put("openingBalance", opening.getText().toString());
                store.saveNotebook(current);
                open(current.optLong("id"));
            } catch (Exception e) {
                alert(e);
            }
        }).setNeutralButton(tr("حذف الدفتر", "Delete ledger"), (dialog, which) -> new FluentDialog(this).setMessage(tr("سيُحذف الدفتر وكل بنوده نهائيًا.", "The ledger and all its entries will be deleted permanently."))
                .setNegativeButton(tr("إلغاء", "Cancel"), null).setPositiveButton(tr("حذف", "Delete"), (d, w) -> {
                    store.removeNotebook(current.optLong("id"));
                    filterBook = -1;
                    list();
                }).show()).show();
    }

    // ---------------------------------------------------------------- PDF statement

    private Uri statementUri() throws Exception {
        File file = LedgerPdf.create(this, book, entries, filter);
        return new Uri.Builder().scheme("content").authority(getPackageName() + ".quotes").appendPath(file.getName()).build();
    }

    private void previewPdf() {
        try {
            Uri uri = statementUri();
            Intent intent = new Intent(this, QuotePreviewActivity.class).setDataAndType(uri, "application/pdf").putExtra("previewTitle", tr("معاينة كشف الدفتر", "Statement preview")).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setClipData(ClipData.newRawUri("كشف دفتر", uri));
            startActivity(intent);
        } catch (Exception e) {
            alert(e);
        }
    }

    private void sharePdf() {
        try {
            Uri uri = statementUri();
            Intent intent = new Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM, uri).putExtra(Intent.EXTRA_SUBJECT, "كشف " + book.optString("name")).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setClipData(ClipData.newRawUri("كشف دفتر", uri));
            startActivity(Intent.createChooser(intent, tr("مشاركة كشف الدفتر", "Share statement")));
        } catch (Exception e) {
            alert(e);
        }
    }

    private void savePdf() {
        try {
            pendingPdf = statementUri();
            startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/pdf").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE, new File(pendingPdf.getLastPathSegment()).getName()), 70);
        } catch (Exception e) {
            alert(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 70 || resultCode != RESULT_OK || data == null || data.getData() == null || pendingPdf == null) return;
        try (InputStream in = getContentResolver().openInputStream(pendingPdf); OutputStream out = getContentResolver().openOutputStream(data.getData())) {
            if (in == null || out == null) throw new IOException();
            byte[] buffer = new byte[8192];
            for (int read; (read = in.read(buffer)) != -1; ) out.write(buffer, 0, read);
            Toast.makeText(this, tr("حُفظ كشف PDF", "PDF statement saved"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            alert(e);
        } finally {
            pendingPdf = null;
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        font = Typeface.createFromAsset(getAssets(), "fonts/Cairo.ttf");
        store = new Store(this);
        list();
    }

    @Override
    protected void onDestroy() {
        store.close();
        super.onDestroy();
    }

    @Override
    public void onPostCreate(Bundle state) {
        super.onPostCreate(state);
        AppLanguage.apply(this);
        AppLanguage.watch(this);
    }
}
