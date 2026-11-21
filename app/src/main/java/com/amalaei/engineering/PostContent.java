package com.amalaei.engineering;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/** Text side of a promotional design: optional blocks, list parsing, discount and the caption that goes with the image. */
final class PostContent {
    static final String[] TEMPLATES = {"product", "features", "gallery", "compare", "service", "occasion"};
    static final String[] TEMPLATE_LABELS = {"عرض منتج وسعر", "مميزات ومواصفات", "معرض صور", "مقارنة", "خدمة", "مناسبة وتهنئة"};
    static final String[] SIZES = {"square", "portrait", "story"};
    static final String[] SIZE_LABELS = {"مربع 1:1", "طولي 4:5", "حالة 9:16"};
    static final int[] HEIGHTS = {1080, 1350, 1920};

    private PostContent() {
    }

    static int height(String size) {
        for (int i = 0; i < SIZES.length; i++) if (SIZES[i].equals(size)) return HEIGHTS[i];
        return 1350;
    }

    static boolean on(JSONObject spec, String key) {
        return spec.optBoolean(key, false);
    }

    /** Non-empty lines without leading bullets, at most {@code max}. */
    static List<String> lines(String raw, int max) {
        ArrayList<String> out = new ArrayList<>();
        if (raw == null) return out;
        for (String line : DisplayNumbers.latin(raw).split("\\r?\\n")) {
            String value = line.trim().replaceFirst("^[-•*✓✔·]+\\s*", "").trim();
            if (!value.isEmpty()) out.add(value);
            if (out.size() == max) break;
        }
        return out;
    }

    /** "label: value" → [label, value]; "label | a | b" → [label, a, b]; a line without a separator is [line]. */
    static String[] spec(String line) {
        if (line.contains("|")) {
            String[] parts = line.split("\\|", -1);
            for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
            return parts;
        }
        int colon = line.indexOf(':');
        if (colon < 0) colon = line.indexOf('：');
        if (colon > 0) return new String[]{line.substring(0, colon).trim(), line.substring(colon + 1).trim()};
        return new String[]{line.trim()};
    }

    static BigDecimal number(String raw) {
        try {
            String value = DisplayNumbers.latin(raw == null ? "" : raw).replaceAll("[^0-9.]", "");
            return value.isEmpty() ? null : new BigDecimal(value);
        } catch (Exception e) {
            return null;
        }
    }

    /** Discount label: the text the user typed, otherwise the percentage between the old and new price. */
    static String discount(JSONObject spec) {
        String typed = spec.optString("discount").trim();
        if (!typed.isEmpty()) return DisplayNumbers.latin(typed);
        BigDecimal price = number(spec.optString("price")), old = number(spec.optString("oldPrice"));
        if (price == null || old == null || old.signum() <= 0 || price.compareTo(old) >= 0) return "";
        int percent = old.subtract(price).multiply(new BigDecimal(100)).divide(old, 0, RoundingMode.HALF_UP).intValue();
        return percent <= 0 ? "" : "خصم " + percent + "%";
    }

    /** Price as typed with thousands separators when it is a plain number. */
    static String money(String raw) {
        BigDecimal value = number(raw);
        if (value == null) return DisplayNumbers.latin(raw == null ? "" : raw.trim());
        java.text.DecimalFormat format = new java.text.DecimalFormat(value.stripTrailingZeros().scale() > 0 ? "#,##0.00" : "#,##0", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US));
        return format.format(value);
    }

    /** A ready caption built from the design: headline, offer, price, features and how to reach the business. */
    static String caption(JSONObject spec) {
        StringBuilder text = new StringBuilder();
        String headline = spec.optString("headline").trim(), subtitle = spec.optString("subtitle").trim();
        if (!headline.isEmpty()) text.append("✨ ").append(headline).append('\n');
        if (!subtitle.isEmpty()) text.append(subtitle).append('\n');
        if (on(spec, "showOffer") && !spec.optString("offer").trim().isEmpty()) text.append("\n⏰ ").append(spec.optString("offer").trim()).append('\n');
        if (on(spec, "showPrice") && !spec.optString("price").trim().isEmpty()) {
            String currency = spec.optString("currency", "ر.س").trim();
            text.append("\n💰 السعر: ").append(money(spec.optString("price"))).append(' ').append(currency);
            if (!spec.optString("oldPrice").trim().isEmpty()) text.append(" بدلًا من ").append(money(spec.optString("oldPrice"))).append(' ').append(currency);
            text.append('\n');
            if (on(spec, "showDiscount") && !discount(spec).isEmpty()) text.append("🔥 ").append(discount(spec)).append('\n');
        }
        if (on(spec, "showFeatures")) {
            List<String> features = lines(spec.optString("features"), 8);
            if (!features.isEmpty()) {
                text.append('\n');
                for (String feature : features) text.append("✅ ").append(feature).append('\n');
            }
        }
        if (on(spec, "showSpecs")) {
            List<String> specs = lines(spec.optString("specs"), 8);
            if (!specs.isEmpty()) {
                text.append("\n📋 المواصفات:\n");
                for (String row : specs) {
                    String[] parts = spec(row);
                    text.append("• ").append(parts.length == 1 ? parts[0] : String.join(" — ", parts)).append('\n');
                }
            }
        }
        if (on(spec, "showCta") && !spec.optString("cta").trim().isEmpty()) text.append("\n👈 ").append(spec.optString("cta").trim()).append('\n');
        if (on(spec, "showContact")) {
            String phone = spec.optString("phone").trim(), whatsapp = spec.optString("whatsapp").trim(), website = spec.optString("website").trim();
            if (!phone.isEmpty() || !whatsapp.isEmpty() || !website.isEmpty()) text.append('\n');
            if (!phone.isEmpty()) text.append("📞 ").append(phone).append('\n');
            if (!whatsapp.isEmpty()) text.append("💬 واتساب: ").append(whatsapp).append('\n');
            if (!website.isEmpty()) text.append("🌐 ").append(website).append('\n');
        }
        return DisplayNumbers.latin(text.toString().trim());
    }

    /** Sensible starting design for a new post. */
    static JSONObject defaults(String brand, String phone, String whatsapp, String website) {
        try {
            return new JSONObject().put("template", "product").put("size", "portrait").put("palette", 0).put("fit", "contain")
                    .put("headline", "").put("subtitle", "").put("badge", "")
                    .put("showPrice", false).put("price", "").put("oldPrice", "").put("currency", "ر.س")
                    .put("showDiscount", false).put("discount", "")
                    .put("showOffer", false).put("offer", "")
                    .put("showFeatures", false).put("features", "")
                    .put("showSpecs", false).put("specs", "").put("nameA", "").put("nameB", "")
                    .put("showCta", true).put("cta", "تواصل معنا الآن")
                    .put("showContact", true).put("phone", phone).put("whatsapp", whatsapp).put("website", website)
                    .put("showBrand", true).put("brand", brand).put("showLogo", true).put("output", "");
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
