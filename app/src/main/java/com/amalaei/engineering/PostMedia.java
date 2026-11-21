package com.amalaei.engineering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Files attached to posts: photos (optimized on import), GIFs and videos, stored once by content hash in
 * {@code files/post-images}. A post keeps an ordered list of names in its {@code media} column; the first one is the cover.
 */
final class PostMedia {
    static final int MAX_ITEMS = 10;
    /** Largest stored still image (designs and optimized photos are far below this). */
    static final int MAX_IMAGE = 6 * 1024 * 1024;
    static final int MAX_SOURCE_IMAGE = 30 * 1024 * 1024, MAX_GIF = 15 * 1024 * 1024;
    static final long MAX_VIDEO = 200L * 1024 * 1024;
    /** Photos and GIFs carried inside an encrypted backup; videos are never included (too large). */
    static final int MAX_BACKUP_IMAGES = 40 * 1024 * 1024;
    private static final int LONG_EDGE = 2048;
    private static final String NAME = "[a-f0-9]{64}\\.(jpg|png|webp|gif|mp4|webm|3gp)";

    static final class RestorePlan {
        final LinkedHashMap<String, byte[]> images = new LinkedHashMap<>();
    }

    private PostMedia() {
    }

    static boolean valid(String name) {
        return name != null && name.matches(NAME);
    }

    static boolean isVideo(String name) {
        return name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".3gp");
    }

    static boolean isGif(String name) {
        return name.endsWith(".gif");
    }

    static boolean isStill(String name) {
        return !isVideo(name) && !isGif(name);
    }

    static File dir(Context c) throws IOException {
        File dir = new File(c.getFilesDir(), "post-images");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("تعذر إنشاء مجلد الصور");
        return dir;
    }

    static File file(Context c, String name) throws IOException {
        if (!valid(name)) throw new IOException("اسم ملف غير صالح");
        return new File(dir(c), name);
    }

    /** Ordered media names of a post; older posts only have the single {@code image} column. */
    static ArrayList<String> names(JSONObject post) {
        ArrayList<String> names = new ArrayList<>();
        try {
            JSONArray media = new JSONArray(post.optString("media", "[]").isEmpty() ? "[]" : post.optString("media", "[]"));
            for (int i = 0; i < media.length(); i++) {
                String name = media.optString(i);
                if (valid(name) && !names.contains(name)) names.add(name);
            }
        } catch (Exception ignored) {
        }
        String image = post.optString("image");
        if (names.isEmpty() && !post.has("media") && valid(image)) names.add(image);
        return names;
    }

    static String hash(byte[] bytes) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static String hex(byte[] digest) {
        StringBuilder b = new StringBuilder();
        for (byte value : digest) b.append(String.format(Locale.US, "%02x", value & 255));
        return b.toString();
    }

    static String extension(byte[] data) throws Exception {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, o);
        if (o.outWidth <= 0 || o.outHeight <= 0 || (long) o.outWidth * o.outHeight > 50_000_000) throw new IOException("اختر صورة صالحة وبأبعاد أصغر");
        if ("image/jpeg".equals(o.outMimeType)) return "jpg";
        if ("image/png".equals(o.outMimeType)) return "png";
        if ("image/webp".equals(o.outMimeType)) return "webp";
        if ("image/gif".equals(o.outMimeType)) return "gif";
        throw new IOException("الصيغ المدعومة: JPG وPNG وWEBP وGIF");
    }

    /** Stores encoded image bytes as they are (used for designs and GIFs). */
    static String save(Context c, byte[] data) throws Exception {
        String ext = extension(data);
        if (data.length > ("gif".equals(ext) ? MAX_GIF : MAX_IMAGE)) throw new IOException("الصورة كبيرة جدًا");
        String name = hash(data) + "." + ext;
        File dest = file(c, name);
        if (!dest.exists()) {
            android.util.AtomicFile atomic = new android.util.AtomicFile(dest);
            FileOutputStream out = null;
            try {
                out = atomic.startWrite();
                out.write(data);
                atomic.finishWrite(out);
            } catch (Exception e) {
                if (out != null) atomic.failWrite(out);
                throw e;
            }
        }
        return name;
    }

    /** Imports one picked file: photos are rotated upright and resized, GIFs are kept, videos are copied as they are. */
    static String importUri(Context c, Uri uri) throws Exception {
        if (uri == null || !"content".equals(uri.getScheme())) throw new IOException("اختر الملف من منتقي الملفات");
        String type = c.getContentResolver().getType(uri);
        if (type != null && type.startsWith("video/")) return importVideo(c, uri, type);
        byte[] bytes = read(c, uri, MAX_SOURCE_IMAGE, "اختر صورة لا تتجاوز 30 ميغابايت");
        if ("gif".equals(extension(bytes))) {
            if (bytes.length > MAX_GIF) throw new IOException("اختر صورة GIF لا تتجاوز 15 ميغابايت");
            return save(c, bytes);
        }
        return save(c, optimize(bytes));
    }

    private static byte[] read(Context c, Uri uri, int limit, String tooLarge) throws Exception {
        try (InputStream in = c.getContentResolver().openInputStream(uri); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            if (in == null) throw new IOException("تعذر قراءة الملف");
            byte[] buffer = new byte[16384];
            int n;
            while ((n = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, n);
                if (bytes.size() > limit) throw new IOException(tooLarge);
            }
            return bytes.toByteArray();
        }
    }

    /** Upright, at most 2048px on the long edge, JPEG (PNG when the photo has transparency). */
    static byte[] optimize(byte[] source) throws Exception {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(source, 0, source.length, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0 || (long) bounds.outWidth * bounds.outHeight > 100_000_000) throw new IOException("اختر صورة صالحة");
        int rotation = 0;
        try (InputStream in = new java.io.ByteArrayInputStream(source)) {
            int orientation = new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            rotation = orientation == ExifInterface.ORIENTATION_ROTATE_90 ? 90 : orientation == ExifInterface.ORIENTATION_ROTATE_180 ? 180 : orientation == ExifInterface.ORIENTATION_ROTATE_270 ? 270 : 0;
        } catch (Exception ignored) {
        }
        int longest = Math.max(bounds.outWidth, bounds.outHeight);
        if (rotation == 0 && longest <= LONG_EDGE && source.length <= 1_500_000 && ("image/jpeg".equals(bounds.outMimeType) || "image/webp".equals(bounds.outMimeType))) return source;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = 1;
        while (longest / (o.inSampleSize * 2) >= LONG_EDGE) o.inSampleSize *= 2;
        Bitmap decoded = BitmapFactory.decodeByteArray(source, 0, source.length, o);
        if (decoded == null) throw new IOException("تعذر فتح الصورة");
        float scale = Math.min(1f, (float) LONG_EDGE / Math.max(decoded.getWidth(), decoded.getHeight()));
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        matrix.postRotate(rotation);
        Bitmap upright = Bitmap.createBitmap(decoded, 0, 0, decoded.getWidth(), decoded.getHeight(), matrix, true);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            boolean alpha = upright.hasAlpha() && "image/png".equals(bounds.outMimeType);
            upright.compress(alpha ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 88, out);
            if (out.size() > MAX_IMAGE) {
                out.reset();
                upright.compress(Bitmap.CompressFormat.JPEG, 80, out);
            }
            return out.toByteArray();
        } finally {
            if (upright != decoded) upright.recycle();
            decoded.recycle();
        }
    }

    private static String importVideo(Context c, Uri uri, String type) throws Exception {
        String ext = type.contains("webm") ? "webm" : type.contains("3gp") ? "3gp" : "mp4";
        File temp = File.createTempFile("video", ".part", dir(c));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long total = 0;
            try (InputStream in = c.getContentResolver().openInputStream(uri); OutputStream out = new FileOutputStream(temp)) {
                if (in == null) throw new IOException("تعذر قراءة الفيديو");
                byte[] buffer = new byte[65536];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    total += n;
                    if (total > MAX_VIDEO) throw new IOException("اختر فيديو لا يتجاوز 200 ميغابايت");
                    digest.update(buffer, 0, n);
                    out.write(buffer, 0, n);
                }
            }
            return adopt(c, temp, hex(digest.digest()) + "." + ext);
        } finally {
            if (temp.exists()) temp.delete();
        }
    }

    /** Moves a finished file (an imported or generated video) into the media folder under its content hash. */
    static String adoptVideo(Context c, File source) throws Exception {
        if (source.length() > MAX_VIDEO) throw new IOException("الفيديو كبير جدًا");
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new FileInputStream(source)) {
            byte[] buffer = new byte[65536];
            int n;
            while ((n = in.read(buffer)) != -1) digest.update(buffer, 0, n);
        }
        File temp = File.createTempFile("video", ".part", dir(c));
        try {
            try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(temp)) {
                byte[] buffer = new byte[65536];
                int n;
                while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            }
            return adopt(c, temp, hex(digest.digest()) + ".mp4");
        } finally {
            if (temp.exists()) temp.delete();
        }
    }

    private static String adopt(Context c, File temp, String name) throws Exception {
        File dest = file(c, name);
        if (dest.exists()) return name;
        if (!temp.renameTo(dest)) throw new IOException("تعذر حفظ الفيديو");
        return name;
    }

    /** Small preview bitmap: the photo itself, the first GIF frame or a frame from the video. */
    static Bitmap thumbnail(Context c, String name) {
        return thumbnail(c, name, 480);
    }

    static Bitmap thumbnail(Context c, String name, int edge) {
        try {
            File f = file(c, name);
            if (isVideo(name)) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                try {
                    retriever.setDataSource(f.getPath());
                    Bitmap frame = retriever.getFrameAtTime(500_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if (frame == null) return null;
                    float scale = Math.min(1f, (float) edge / Math.max(frame.getWidth(), frame.getHeight()));
                    if (scale >= 1f) return frame;
                    Bitmap small = Bitmap.createScaledBitmap(frame, Math.round(frame.getWidth() * scale), Math.round(frame.getHeight() * scale), true);
                    frame.recycle();
                    return small;
                } finally {
                    retriever.release();
                }
            }
            return decode(f, edge);
        } catch (Exception e) {
            return null;
        }
    }

    /** Decodes a stored still image with its long edge reduced to about {@code edge} pixels. */
    static Bitmap decode(File f, int edge) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getPath(), o);
        o.inSampleSize = 1;
        while (Math.max(o.outWidth, o.outHeight) / (o.inSampleSize * 2) >= edge) o.inSampleSize *= 2;
        o.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(f.getPath(), o);
    }

    static String mime(String name) {
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".mp4")) return "video/mp4";
        if (name.endsWith(".webm")) return "video/webm";
        if (name.endsWith(".3gp")) return "video/3gpp";
        return "image/jpeg";
    }

    /**
     * Base64 copies of post photos and GIFs for the encrypted backup. Every cover is required; further photos are added
     * while they fit, so a large library never blocks the backup. Videos are left out.
     */
    static JSONObject backup(Context c, ArrayList<JSONObject> posts) throws Exception {
        JSONObject images = new JSONObject();
        int total = 0;
        ArrayList<String> extras = new ArrayList<>();
        for (JSONObject p : posts) {
            List<String> names = names(p);
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                if (isVideo(name) || images.has(name)) continue;
                if (i > 0) {
                    extras.add(name);
                    continue;
                }
                File f = file(c, name);
                if (!f.exists()) throw new IOException("إحدى صور الدعاية مفقودة؛ استبدلها أو أزلها قبل النسخ الاحتياطي");
                byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
                total += bytes.length;
                if (total > MAX_BACKUP_IMAGES) throw new IOException("صور أغلفة المنشورات أكبر من 40 ميغابايت؛ احذف بعض المنشورات القديمة قبل إنشاء النسخة");
                images.put(name, android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP));
            }
        }
        for (String name : extras) {
            if (images.has(name)) continue;
            File f = file(c, name);
            if (!f.isFile() || total + f.length() > MAX_BACKUP_IMAGES) continue;
            byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
            total += bytes.length;
            images.put(name, android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP));
        }
        return images;
    }

    static RestorePlan validateRestore(JSONArray posts, JSONObject images) throws Exception {
        RestorePlan plan = new RestorePlan();
        int total = 0;
        Iterator<String> keys = images.keys();
        while (keys.hasNext()) {
            String name = keys.next();
            if (!valid(name) || isVideo(name)) throw new IOException("اسم صورة غير صالح");
            String encoded = images.getString(name);
            if (encoded.length() > MAX_GIF * 4 / 3 + 8) throw new IOException("صورة النسخة كبيرة جدًا");
            byte[] data = android.util.Base64.decode(encoded, android.util.Base64.DEFAULT);
            if (data.length > (isGif(name) ? MAX_GIF : MAX_IMAGE)) throw new IOException("صورة النسخة كبيرة جدًا");
            total += data.length;
            if (total > MAX_BACKUP_IMAGES) throw new IOException("صور النسخة كبيرة جدًا");
            if (!name.equals(hash(data) + "." + extension(data))) throw new IOException("صورة النسخة غير متطابقة");
            plan.images.put(name, data);
        }
        for (int i = 0; i < posts.length(); i++) {
            String name = posts.getJSONObject(i).optString("image");
            if (!name.isEmpty() && !plan.images.containsKey(name)) throw new IOException("صورة غير موجودة في النسخة الاحتياطية");
        }
        return plan;
    }

    /** Drops media names whose files are neither in the backup nor already on this phone (videos are never backed up). */
    static void keepAvailable(Context c, JSONObject post, RestorePlan plan) throws Exception {
        JSONArray kept = new JSONArray();
        for (String name : names(post)) {
            boolean present = plan.images.containsKey(name);
            if (!present) try {
                present = file(c, name).isFile();
            } catch (IOException ignored) {
            }
            if (present) kept.put(name);
        }
        post.put("media", kept.toString());
        post.put("image", kept.length() == 0 ? "" : kept.getString(0));
    }

    static ArrayList<File> install(Context c, RestorePlan plan) throws Exception {
        ArrayList<File> created = new ArrayList<>();
        try {
            for (Map.Entry<String, byte[]> item : plan.images.entrySet()) {
                File dest = file(c, item.getKey());
                if (dest.exists()) {
                    if (dest.length() > MAX_GIF || !java.util.Arrays.equals(java.nio.file.Files.readAllBytes(dest.toPath()), item.getValue()))
                        throw new IOException("صورة محفوظة تالفة؛ أزل المنشور المرتبط بها ثم أعد المحاولة");
                    continue;
                }
                String stored = save(c, item.getValue());
                if (!stored.equals(item.getKey())) throw new IOException("صورة غير صالحة");
                created.add(dest);
            }
            return created;
        } catch (Exception e) {
            remove(created);
            throw e;
        }
    }

    static void remove(Collection<File> files) {
        for (File f : files)
            try {
                if (f.isFile()) f.delete();
            } catch (Exception ignored) {
            }
    }

    /** Deletes stored media that no post refers to any more. */
    static void prune(Context c, JSONArray posts) {
        try {
            HashSet<String> used = new HashSet<>();
            for (int i = 0; i < posts.length(); i++) used.addAll(names(posts.getJSONObject(i)));
            File[] files = dir(c).listFiles();
            if (files != null) for (File f : files) if (f.isFile() && valid(f.getName()) && !used.contains(f.getName())) f.delete();
        } catch (Exception ignored) {
        }
    }
}
