package com.amalaei.engineering;

import java.util.List;
import org.json.JSONObject;

public final class PostContentTest {
    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    public static void main(String[] args) throws Exception {
        List<String> lines = PostContent.lines("• دقة عالية\n\n- ضمان سنتين\n✓ ٣ سنوات صيانة\n", 6);
        check(lines.size() == 3 && lines.get(0).equals("دقة عالية") && lines.get(2).equals("3 سنوات صيانة"), "bullets stripped, digits latin: " + lines);
        check(PostContent.lines("a\nb\nc", 2).size() == 2, "max lines");

        String[] pair = PostContent.spec("الدقة: 1 ملم");
        check(pair.length == 2 && pair[0].equals("الدقة") && pair[1].equals("1 ملم"), "label: value");
        String[] row = PostContent.spec("المدى | 300 م | 500 م");
        check(row.length == 3 && row[2].equals("500 م"), "comparison row");
        check(PostContent.spec("ضمان").length == 1, "plain line");

        JSONObject spec = new JSONObject().put("price", "750").put("oldPrice", "1,000");
        check(PostContent.discount(spec).equals("خصم 25%"), "auto discount " + PostContent.discount(spec));
        spec.put("discount", "وفّر ٢٠٠");
        check(PostContent.discount(spec).equals("وفّر 200"), "typed discount wins");
        check(PostContent.discount(new JSONObject().put("price", "900").put("oldPrice", "800")).isEmpty(), "no discount when the price went up");
        check(PostContent.money("12500").equals("12,500") && PostContent.money("99.5").equals("99.50") && PostContent.money("حسب الطلب").equals("حسب الطلب"), "money");

        JSONObject design = PostContent.defaults("إنجنكس", "0549349245", "0549349245", "https://enginex2030.com");
        design.put("headline", "ميزان ليكا").put("showPrice", true).put("price", "750").put("oldPrice", "1000").put("showDiscount", true)
                .put("showFeatures", true).put("features", "دقة عالية\nضمان سنتين").put("showOffer", true).put("offer", "حتى نهاية الشهر");
        String caption = PostContent.caption(design);
        check(caption.startsWith("✨ ميزان ليكا"), "caption headline");
        check(caption.contains("750 ر.س بدلًا من 1,000 ر.س") && caption.contains("خصم 25%"), "caption price " + caption);
        check(caption.contains("✅ ضمان سنتين") && caption.contains("⏰ حتى نهاية الشهر") && caption.contains("🌐 https://enginex2030.com"), "caption blocks");
        check(PostContent.height("story") == 1920 && PostContent.height("square") == 1080 && PostContent.height("x") == 1350, "sizes");

        String a = "a".repeat(64) + ".jpg", b = "b".repeat(64) + ".mp4";
        JSONObject post = new JSONObject().put("image", a).put("media", "[\"" + a + "\",\"" + b + "\",\"bad.exe\",\"" + a + "\"]");
        List<String> names = PostMedia.names(post);
        check(names.size() == 2 && names.get(1).equals(b), "media list keeps order, drops invalid and duplicates: " + names);
        check(PostMedia.names(new JSONObject().put("image", a)).size() == 1, "old posts fall back to the image column");
        check(PostMedia.names(new JSONObject().put("image", a).put("media", "[]")).isEmpty(), "an emptied media list stays empty");
        check(PostMedia.isVideo(b) && !PostMedia.isVideo(a) && PostMedia.mime(b).equals("video/mp4"), "kinds");
    }
}
