package com.amalaei.engineering;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Rect;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.TransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.Map;
import java.util.WeakHashMap;

final class VisualPolish {
    private static final Map<View, Boolean> styled = new WeakHashMap();
    private static final Map<Activity, ViewTreeObserver.OnGlobalLayoutListener> observers = new WeakHashMap();

    private static final class LatinTransformation implements TransformationMethod {
        private LatinTransformation() {
        }

        @Override
        public CharSequence getTransformation(CharSequence charSequence, View view) {
            if (charSequence == null) {
                return "";
            }
            String string = charSequence.toString();
            String strLatin = DisplayNumbers.latin(string);
            if (string.equals(strLatin)) {
                return charSequence;
            }
            if (!(charSequence instanceof Spanned)) {
                return strLatin;
            }
            SpannableString spannableString = new SpannableString(strLatin);
            TextUtils.copySpansFrom((Spanned) charSequence, 0, charSequence.length(), Object.class, spannableString, 0);
            return spannableString;
        }

        @Override
        public void onFocusChanged(View view, CharSequence charSequence, boolean z, int i, Rect rect) {
        }
    }

    private VisualPolish() {
    }

    static void dialog(AlertDialog alertDialog) {
        final View decorView = alertDialog.getWindow().getDecorView();
        decorView.setLayoutDirection(AppLanguage.direction(alertDialog.getContext()));
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public final void onGlobalLayout() {
                VisualPolish.visit(decorView);
            }
        });
        alertDialog.getWindow().setBackgroundDrawable(UiTheme.surface(alertDialog.getContext(), false, 22));
    }

    static /* synthetic */ CharSequence lambda$visit$2(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
        String string = charSequence.subSequence(i, i2).toString();
        String strLatin = DisplayNumbers.latin(string);
        if (string.equals(strLatin)) {
            return null;
        }
        return strLatin;
    }

    static void remove(Activity activity) {
        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListenerRemove = observers.remove(activity);
        if (onGlobalLayoutListenerRemove == null || !activity.getWindow().getDecorView().getViewTreeObserver().isAlive()) {
            return;
        }
        activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListenerRemove);
    }

    public static void visit(View view) {
        Map<View, Boolean> map = styled;
        if (!map.containsKey(view)) {
            map.put(view, true);
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                int inputType = editText.getInputType() & 4080;
                if (inputType != 128 && inputType != 224 && inputType != 144 && ((editText.getInputType() & 15) != 2 || inputType != 16)) {
                    editText.setTransformationMethod(new LatinTransformation());
                    int inputType2 = editText.getInputType() & 15;
                    if (inputType2 == 2 || inputType2 == 3) {
                        InputFilter[] filters = editText.getFilters();
                        InputFilter[] inputFilterArr = new InputFilter[filters.length + 1];
                        System.arraycopy(filters, 0, inputFilterArr, 1, filters.length);
                        inputFilterArr[0] = new InputFilter() {
                            @Override
                            public final CharSequence filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
                                return VisualPolish.lambda$visit$2(charSequence, i, i2, spanned, i3, i4);
                            }
                        };
                        editText.setFilters(inputFilterArr);
                        String string = editText.getText().toString();
                        String strLatin = DisplayNumbers.latin(string);
                        if (!string.equals(strLatin)) {
                            editText.setText(strLatin);
                        }
                    }
                }
                UiTheme.input(editText);
            } else if (view instanceof TextView) {
                TextView textView = (TextView) view;
                if (textView.getTransformationMethod() == null) {
                    textView.setTransformationMethod(new LatinTransformation());
                }
                textView.setFontFeatureSettings("tnum");
                textView.setTypeface(UiTheme.font(view.getContext()), textView.getTypeface() == null ? 0 : textView.getTypeface().getStyle());
                if ((view instanceof Button) && !(view instanceof CompoundButton)) {
                    Button button = (Button) view;
                    if (button.getTextSize() <= button.getResources().getDisplayMetrics().scaledDensity * 19.0f) {
                        UiTheme.button(button, button.getCurrentTextColor() == -1);
                    }
                }
            }
            if (view instanceof Spinner) {
                view.setBackground(UiTheme.sectionSurface(view.getContext(), false, 13));
                view.setMinimumHeight(UiTheme.dp(view.getContext(), 48));
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                visit(viewGroup.getChildAt(i));
            }
        }
    }

    static void watch(Activity activity) {
        Map<Activity, ViewTreeObserver.OnGlobalLayoutListener> map = observers;
        if (map.containsKey(activity)) {
            return;
        }
        final View decorView = activity.getWindow().getDecorView();
        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public final void onGlobalLayout() {
                VisualPolish.visit(decorView);
            }
        };
        map.put(activity, onGlobalLayoutListener);
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        visit(decorView);
    }
}
