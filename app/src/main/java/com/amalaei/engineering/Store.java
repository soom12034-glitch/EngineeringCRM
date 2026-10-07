package com.amalaei.engineering;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.File;
import java.util.ArrayList;

final class Store extends SQLiteOpenHelper {
    private final Context context;
    Store(Context context) { super(context, "clients.db", null, 7);this.context=context.getApplicationContext(); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE leads (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, phone TEXT UNIQUE, business TEXT NOT NULL DEFAULT '', stage INTEGER NOT NULL DEFAULT 0, company TEXT NOT NULL DEFAULT '', email TEXT NOT NULL DEFAULT '', notes TEXT NOT NULL DEFAULT '', followUp TEXT NOT NULL DEFAULT '', interest TEXT NOT NULL DEFAULT '', contactName TEXT NOT NULL DEFAULT '', lastCallAt INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE INDEX lead_business ON leads(business, stage)");
        db.execSQL("CREATE INDEX lead_due ON leads(followUp)");
        db.execSQL("CREATE TABLE messages (id INTEGER PRIMARY KEY AUTOINCREMENT, fingerprint TEXT NOT NULL UNIQUE, sender TEXT NOT NULL, body TEXT NOT NULL, at INTEGER NOT NULL, source TEXT NOT NULL, state TEXT NOT NULL DEFAULT 'pending', leadId INTEGER, isGroup INTEGER NOT NULL DEFAULT 0)");
        extend(db);advertisingSchema(db);profileSchema(db);quoteSchema(db);serviceSchema(db);softwareSchema(db);
    }
    private void extend(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE leads ADD COLUMN followTime TEXT NOT NULL DEFAULT '09:00'");
        db.execSQL("ALTER TABLE leads ADD COLUMN lastNotified TEXT NOT NULL DEFAULT ''");
        db.execSQL("ALTER TABLE leads ADD COLUMN requestDetails TEXT NOT NULL DEFAULT '{}'");
        db.execSQL("CREATE TABLE events (id INTEGER PRIMARY KEY AUTOINCREMENT, leadId INTEGER NOT NULL, kind TEXT NOT NULL, body TEXT NOT NULL, at INTEGER NOT NULL, externalKey TEXT UNIQUE)");
        db.execSQL("CREATE INDEX event_lead_time ON events(leadId, at)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) { if(oldV<2)extend(db);if(oldV<3)advertisingSchema(db);if(oldV<4)profileSchema(db);if(oldV<5)quoteSchema(db);if(oldV<6)serviceSchema(db);if(oldV<7)softwareSchema(db); }
    private void softwareSchema(SQLiteDatabase db){
        db.execSQL("CREATE TABLE softwareRequests(id INTEGER PRIMARY KEY AUTOINCREMENT, leadId INTEGER NOT NULL, data TEXT NOT NULL, notified TEXT NOT NULL DEFAULT '{}')");
        db.execSQL("CREATE INDEX software_owner ON softwareRequests(leadId)");
        db.execSQL("CREATE TABLE softwareProducts(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT NOT NULL)");
        db.execSQL("CREATE TRIGGER software_owner_insert BEFORE INSERT ON softwareRequests WHEN NOT EXISTS(SELECT 1 FROM leads WHERE id=NEW.leadId AND business='software') BEGIN SELECT RAISE(ABORT,'software customer required'); END");
        db.execSQL("CREATE TRIGGER software_owner_update BEFORE UPDATE ON softwareRequests WHEN NOT EXISTS(SELECT 1 FROM leads WHERE id=NEW.leadId AND business='software') BEGIN SELECT RAISE(ABORT,'software customer required'); END");
    }
    JSONObject softwareRequest(long id)throws Exception{ArrayList<JSONObject> found=query("SELECT data FROM softwareRequests WHERE id=?",String.valueOf(id));return found.isEmpty()?new JSONObject():new JSONObject(found.get(0).getString("data"));}
    long saveSoftware(JSONObject q)throws Exception{SoftwareWork.validate(q);JSONObject owner=lead(q.optLong("leadId"));if(!owner.optString("business").equals("software"))throw new IllegalArgumentException("اختر عميلًا من نشاط البرامج");android.database.sqlite.SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{ContentValues v=new ContentValues();v.put("leadId",q.optLong("leadId"));long id=q.optLong("id");JSONObject prior=id>0?softwareRequest(id):new JSONObject();v.put("data",q.toString());if(id==0){id=db.insertOrThrow("softwareRequests",null,v);q.put("id",id);}v.put("data",q.toString());if(prior.optInt("status")>=4&&q.optInt("status")<4)v.put("notified","{}");if(db.update("softwareRequests",v,"id=?",new String[]{String.valueOf(id)})!=1)throw new IllegalStateException("الطلب غير موجود");event(q.optLong("leadId"),"software",SoftwareWork.label(q.optString("type"))+": "+q.optString("title")+" • "+SoftwareWork.statuses(q.optString("type"))[q.optInt("status")],System.currentTimeMillis(),null);db.setTransactionSuccessful();return id;}finally{db.endTransaction();}}
    private void serviceSchema(SQLiteDatabase db){db.execSQL("ALTER TABLE leads ADD COLUMN requestType TEXT NOT NULL DEFAULT 'sales' CHECK(requestType IN ('sales','support','maintenance','calibration'))");}
    private void quoteSchema(SQLiteDatabase db){db.execSQL("CREATE TABLE quotes (id INTEGER PRIMARY KEY AUTOINCREMENT, business TEXT NOT NULL DEFAULT 'survey' CHECK(business='survey'), data TEXT NOT NULL)");}
    long saveQuote(JSONObject q)throws Exception{Quotation.validate(q);SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{ContentValues v=new ContentValues();v.put("business","survey");v.put("data",q.toString());long id=q.optLong("id");if(id==0){id=db.insertOrThrow("quotes",null,v);q.put("id",id);v.put("data",q.toString());}if(db.update("quotes",v,"id=?",new String[]{String.valueOf(id)})!=1)throw new IllegalStateException("تعذر حفظ العرض");db.setTransactionSuccessful();return id;}finally{db.endTransaction();}}
    JSONObject quote(long id)throws Exception{return new JSONObject(query("SELECT data FROM quotes WHERE id=?",String.valueOf(id)).get(0).getString("data"));}
    private void advertisingSchema(SQLiteDatabase db){
        db.execSQL("CREATE TABLE posts (id INTEGER PRIMARY KEY AUTOINCREMENT, business TEXT NOT NULL CHECK(business IN ('survey','software')), title TEXT NOT NULL, body TEXT NOT NULL DEFAULT '', image TEXT NOT NULL DEFAULT '', category TEXT NOT NULL DEFAULT 'تعريف', plannedDate TEXT NOT NULL DEFAULT '', lastPreparedAt INTEGER NOT NULL DEFAULT 0, createdAt INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX post_business ON posts(business,plannedDate)");
        db.execSQL("CREATE TABLE postShares (id INTEGER PRIMARY KEY AUTOINCREMENT, postId INTEGER NOT NULL, at INTEGER NOT NULL)");
    }
    JSONObject post(long id){ArrayList<JSONObject> rows=query("SELECT * FROM posts WHERE id=?",String.valueOf(id));return rows.isEmpty()?new JSONObject():rows.get(0);}
    long savePost(JSONObject post)throws Exception {String business=post.optString("business"),title=post.optString("title").trim(),date=post.optString("plannedDate");if(!business.equals(AppMode.MODE))throw new IllegalArgumentException("حدد نشاط المنشور");if(title.isEmpty())throw new IllegalArgumentException("أدخل عنوانًا للمنشور");if(!date.isEmpty())java.time.LocalDate.parse(date);String image=post.optString("image");if(!image.isEmpty()&&!PostMedia.file(context,image).isFile())throw new IllegalArgumentException("الصورة غير موجودة");if(post.optString("body").trim().isEmpty()&&image.isEmpty())throw new IllegalArgumentException("أضف نصًا أو صورة للمنشور");ContentValues v=new ContentValues();v.put("business",business);v.put("title",title);v.put("body",post.optString("body"));v.put("category",post.optString("category","تعريف"));v.put("plannedDate",date);v.put("image",image);long id=post.optLong("id");if(id>0){String previous=post(id).optString("image");SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{if(db.update("posts",v,"id=?",new String[]{String.valueOf(id)})!=1)throw new IllegalStateException("المنشور لم يعد موجودًا");db.setTransactionSuccessful();}finally{db.endTransaction();}if(!previous.equals(image))removeUnusedImage(previous);return id;}v.put("createdAt",System.currentTimeMillis());return getWritableDatabase().insertOrThrow("posts",null,v);}
    void preparedPost(long id){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{long at=System.currentTimeMillis();ContentValues log=new ContentValues();log.put("postId",id);log.put("at",at);db.insertOrThrow("postShares",null,log);ContentValues v=new ContentValues();v.put("lastPreparedAt",at);db.update("posts",v,"id=?",new String[]{String.valueOf(id)});db.setTransactionSuccessful();}finally{db.endTransaction();}}
    void removePost(long id){String image=post(id).optString("image");SQLiteDatabase db=getWritableDatabase();db.beginTransaction();boolean success=false;try{db.delete("postShares","postId=?",new String[]{String.valueOf(id)});db.delete("posts","id=?",new String[]{String.valueOf(id)});db.setTransactionSuccessful();success=true;}finally{db.endTransaction();}if(success)removeUnusedImage(image);}
    void removeUnusedImage(String image){if(image==null||image.isEmpty()||!query("SELECT id FROM posts WHERE image=? LIMIT 1",image).isEmpty())return;try{File target=PostMedia.file(context,image);if(target.isFile())target.delete();}catch(Exception ignored){}}
    private void profileSchema(SQLiteDatabase db){
        db.execSQL("CREATE TABLE profiles (business TEXT PRIMARY KEY CHECK(business IN ('survey','software')), data TEXT NOT NULL)");
        for(String activity:new String[]{AppMode.MODE})try{ContentValues row=new ContentValues();row.put("business",activity);row.put("data",defaultProfile(activity).toString());db.insertOrThrow("profiles",null,row);}catch(Exception e){throw new IllegalStateException("تعذر ترقية ملفات المنشآت",e);}
    }
    private JSONObject defaultProfile(String activity)throws Exception{return new JSONObject().put("name","").put("phones","");}


    JSONObject profile(String activity){if(!activity.equals(AppMode.MODE))throw new IllegalArgumentException("نشاط غير صالح");ArrayList<JSONObject> found=query("SELECT data FROM profiles WHERE business=?",activity);try{if(!found.isEmpty())return new JSONObject(found.get(0).getString("data"));return defaultProfile(activity);}catch(Exception e){throw new IllegalStateException("تعذر قراءة بيانات المنشأة",e);}}
    void saveProfile(String activity,JSONObject data)throws Exception{if(!activity.equals(AppMode.MODE))throw new IllegalArgumentException("نشاط غير صالح");if(data.optString("name").trim().isEmpty())throw new IllegalArgumentException("أدخل اسم المنشأة");for(String key:Profiles.LINKS)data.put(key,Profiles.url(data.optString(key)));String email=data.optString("email");if(!email.isEmpty()&&!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))throw new IllegalArgumentException("البريد الإلكتروني غير صالح");ContentValues v=new ContentValues();v.put("business",activity);v.put("data",data.toString());if(getWritableDatabase().insertWithOnConflict("profiles",null,v,SQLiteDatabase.CONFLICT_REPLACE)<0)throw new IllegalStateException("تعذر حفظ بيانات المنشأة");}
    void event(long leadId,String kind,String body,long at,String externalKey) {
        ContentValues v=new ContentValues();v.put("leadId",leadId);v.put("kind",kind);v.put("body",body);v.put("at",at);if(externalKey==null)v.putNull("externalKey");else v.put("externalKey",externalKey);
        getWritableDatabase().insertWithOnConflict("events",null,v,SQLiteDatabase.CONFLICT_IGNORE);
    }
    void request(long id,JSONObject details) throws Exception {SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{ContentValues v=new ContentValues();v.put("requestDetails",details.toString());db.update("leads",v,"id=?",new String[]{String.valueOf(id)});event(id,"request","تحديث تفاصيل طلب العميل",System.currentTimeMillis(),null);db.setTransactionSuccessful();}finally{db.endTransaction();}}
    void finishFollowUp(long id,boolean tomorrow) throws Exception {SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{JSONObject item=lead(id);if(item.optLong("id")==0)return;item.put("followUp",tomorrow?java.time.LocalDate.now().plusDays(1).toString():"");save(item);event(id,"followup",tomorrow?"تأجيل المتابعة إلى الغد":"إتمام المتابعة",System.currentTimeMillis(),null);db.setTransactionSuccessful();}finally{db.endTransaction();}}

    static JSONObject row(Cursor c) throws Exception {
        JSONObject o = new JSONObject();
        for (int i=0; i<c.getColumnCount(); i++) {
            if (c.isNull(i)) o.put(c.getColumnName(i), JSONObject.NULL);
            else if (c.getType(i) == Cursor.FIELD_TYPE_INTEGER) o.put(c.getColumnName(i), c.getLong(i));
            else o.put(c.getColumnName(i), c.getString(i));
        }
        return o;
    }
    ArrayList<JSONObject> query(String sql, String... args) {
        ArrayList<JSONObject> result = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) { while (c.moveToNext()) result.add(row(c)); }
        catch (Exception e) { throw new IllegalStateException("تعذر قراءة قاعدة البيانات", e); }
        return result;
    }
    JSONObject lead(long id) { ArrayList<JSONObject> data = query("SELECT * FROM leads WHERE id=?", String.valueOf(id)); return data.isEmpty() ? new JSONObject() : data.get(0); }
    JSONObject byPhone(String phone) { ArrayList<JSONObject> data = query("SELECT * FROM leads WHERE phone=?", phone); return data.isEmpty() ? new JSONObject() : data.get(0); }
    long save(JSONObject o) throws Exception {
        if(!AppMode.accepts(o.optString("business")))throw new IllegalArgumentException("العميل ينتمي إلى التطبيق الآخر");String name=o.optString("name").trim(); if (name.isEmpty()) throw new IllegalArgumentException("أدخل اسم العميل");
        String raw=o.optString("phone").trim(), phone=raw.isEmpty()?"":Analysis.phone(raw);
        if (!raw.isEmpty() && phone.isEmpty()) throw new IllegalArgumentException("أدخل رقمًا سعوديًا صحيحًا أو اترك الرقم فارغًا");
        String due=o.optString("followUp").trim(); if (!due.isEmpty()) { try { java.time.LocalDate.parse(due); } catch (Exception e) { throw new IllegalArgumentException("موعد المتابعة بصيغة سنة-شهر-يوم"); } }
        String business=o.optString("business"); if (!business.isEmpty() && !business.equals("survey") && !business.equals("software")) throw new IllegalArgumentException("نشاط غير صالح");
        String requestType=business.equals("survey")?o.optString("requestType","sales"):"sales";if(!SurveyRequests.valid(requestType))throw new IllegalArgumentException("نوع طلب غير صالح");
        int stage=o.optInt("stage"); if(stage<0 || stage>5) throw new IllegalArgumentException("مرحلة غير صالحة");
        ContentValues v=new ContentValues(); v.put("name",name); if(phone.isEmpty())v.putNull("phone");else v.put("phone",phone);
        for(String key:new String[]{"business","company","email","notes","followUp","interest"}) v.put(key,o.optString(key));
        String time=o.optString("followTime","09:00");try{time=java.time.LocalTime.parse(time).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));}catch(Exception e){throw new IllegalArgumentException("وقت المتابعة بصيغة ساعة:دقيقة");}
        v.put("followUp",due);v.put("followTime",time);v.put("stage",stage);v.put("requestType",requestType);
        long id=o.optLong("id");JSONObject before=id>0?lead(id):new JSONObject();if(id>0&&!business.equals("software")&&!query("SELECT id FROM softwareRequests WHERE leadId=? LIMIT 1",String.valueOf(id)).isEmpty())throw new IllegalArgumentException("للعميل طلبات برامج مستقلة؛ احتفظ بنشاطه أو احذف طلباته أولًا");
        if(!before.optString("followUp").equals(due)||!before.optString("followTime","09:00").equals(time)||before.optInt("stage")!=stage||!before.optString("requestType","sales").equals(requestType))v.put("lastNotified","");
        SQLiteDatabase write=getWritableDatabase();write.beginTransaction();
        try{
            if(id>0) { if(write.update("leads",v,"id=?",new String[]{String.valueOf(id)})!=1)throw new IllegalStateException("العميل لم يعد موجودًا");if(!before.optString("requestType","sales").equals(requestType))event(id,"requestType","تغيير نوع الطلب إلى "+SurveyRequests.label(requestType),System.currentTimeMillis(),null);event(id,"update",before.optInt("stage")!=stage?"تغيير مرحلة العميل وحفظ بياناته":"تحديث بيانات العميل",System.currentTimeMillis(),null);write.setTransactionSuccessful();return id; }
            long created=write.insertOrThrow("leads",null,v);event(created,"created","إضافة العميل",System.currentTimeMillis(),null);write.setTransactionSuccessful();return created;
        } finally { write.endTransaction(); }
    }
    void recordCall(String phone,long at,String contact){recordCall(phone,at,contact,"");}
    void recordCall(String phone, long at, String contact,String activity) {
        SQLiteDatabase db=getWritableDatabase(); db.beginTransaction();
        try {
            JSONObject lead=byPhone(phone); ContentValues values=new ContentValues();
            values.put("lastCallAt",Math.max(at,lead.optLong("lastCallAt")));
            if(!contact.isEmpty()) values.put("contactName",contact);
            if(lead.optLong("id")>0) {
                if (!contact.isEmpty() && (lead.optString("name").equals(phone) || lead.optString("name").isEmpty())) values.put("name",contact);
                db.update("leads",values,"id=?",new String[]{String.valueOf(lead.optLong("id"))});
            } else { values.put("business",AppMode.accepts(activity)?activity:"");values.put("name",contact.isEmpty()?phone:contact); values.put("phone",phone); db.insertOrThrow("leads",null,values); }
            long leadId=byPhone(phone).optLong("id");event(leadId,"call","اتصال مسجل من الهاتف",at,"call|"+phone+"|"+at);
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }
    void beginBatch(){getWritableDatabase().beginTransaction();}
    void commitBatch(){SQLiteDatabase db=getWritableDatabase();db.setTransactionSuccessful();db.endTransaction();}
    void rollbackBatch(){getWritableDatabase().endTransaction();}
    int assign(java.util.Collection<Long> ids,String activity){if(!activity.equals(AppMode.MODE))throw new IllegalArgumentException();SQLiteDatabase db=getWritableDatabase();db.beginTransaction();int changed=0;try{for(Long id:ids){ContentValues v=new ContentValues();v.put("business",activity);int n=db.update("leads",v,"id=? AND business=''",new String[]{String.valueOf(id)});if(n>0){changed++;event(id,"assign","تعيين نشاط العميل",System.currentTimeMillis(),null);}}db.setTransactionSuccessful();return changed;}finally{db.endTransaction();}}
    void message(String fingerprint,String sender,String body,long at,String source,boolean group) {
        if(body==null || body.trim().isEmpty())return;
        ContentValues v=new ContentValues(); v.put("fingerprint",fingerprint); v.put("sender",sender==null?"":sender); v.put("body",body.length()>100000?body.substring(0,100000):body); v.put("at",at); v.put("source",source); v.put("isGroup",group?1:0);
        getWritableDatabase().insertWithOnConflict("messages",null,v,SQLiteDatabase.CONFLICT_IGNORE);
    }
    void reviewed(long id,long leadId) { ArrayList<JSONObject> messages=query("SELECT body FROM messages WHERE id=?",String.valueOf(id));String body=messages.isEmpty()?"":messages.get(0).optString("body");event(leadId,"message","رسالة تمت مراجعتها: "+(body.length()>3000?body.substring(0,3000):body),System.currentTimeMillis(),"message|"+id); ContentValues v=new ContentValues();v.put("state","reviewed");v.put("leadId",leadId);getWritableDatabase().update("messages",v,"id=?",new String[]{String.valueOf(id)}); }
    void dismiss(long id) { getWritableDatabase().delete("messages","id=?",new String[]{String.valueOf(id)}); }
    void remove(long id) { java.util.ArrayList<Integer> pendingNotifications=new java.util.ArrayList<>();for(JSONObject row:query("SELECT id FROM softwareRequests WHERE leadId=?",String.valueOf(id)))pendingNotifications.add((int)row.optLong("id"));SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{db.delete("softwareRequests","leadId=?",new String[]{String.valueOf(id)});db.delete("events","leadId=?",new String[]{String.valueOf(id)});db.delete("leads","id=?",new String[]{String.valueOf(id)});db.setTransactionSuccessful();}finally{db.endTransaction();}android.app.NotificationManager notifications=context.getSystemService(android.app.NotificationManager.class);for(int softwareId:pendingNotifications)notifications.cancel("software",softwareId); }
    JSONObject backup() throws Exception {SQLiteDatabase db=getReadableDatabase();db.beginTransaction();try{ArrayList<JSONObject> posts=query("SELECT * FROM posts");JSONObject snapshot=new JSONObject().put("appMode",AppMode.MODE).put("version",7).put("softwareRequests",new JSONArray(query("SELECT * FROM softwareRequests"))).put("softwareProducts",new JSONArray(query("SELECT * FROM softwareProducts"))).put("quotes",new JSONArray(query("SELECT * FROM quotes"))).put("leads",new JSONArray(query("SELECT * FROM leads"))).put("messages",new JSONArray(query("SELECT * FROM messages"))).put("events",new JSONArray(query("SELECT * FROM events"))).put("profiles",new JSONArray(query("SELECT * FROM profiles"))).put("posts",new JSONArray(posts)).put("postShares",new JSONArray(query("SELECT * FROM postShares"))).put("postImages",PostMedia.backup(context,posts));db.setTransactionSuccessful();return snapshot;}finally{db.endTransaction();}}
    void restore(JSONObject backup) throws Exception {
        backup=BackupPartition.select(backup);
        if(backup.optInt("version")<1 || backup.optInt("version")>7)throw new IllegalArgumentException("نسخة غير مدعومة");
        JSONArray leads=backup.getJSONArray("leads"),messages=backup.getJSONArray("messages"),events=backup.optInt("version")>=2?backup.getJSONArray("events"):new JSONArray();
        if(leads.length()>50000 || messages.length()>50000 || events.length()>200000)throw new IllegalArgumentException("النسخة كبيرة جدًا");
        JSONArray posts=backup.optInt("version")>=3?backup.getJSONArray("posts"):new JSONArray(),shares=backup.optInt("version")>=3?backup.getJSONArray("postShares"):new JSONArray();if(posts.length()>10000||shares.length()>100000)throw new IllegalArgumentException("مكتبة المنشورات كبيرة جدًا");PostMedia.RestorePlan imagePlan=PostMedia.validateRestore(posts,backup.optInt("version")>=3?backup.getJSONObject("postImages"):new JSONObject());
        JSONArray profiles=backup.optInt("version")>=4?backup.getJSONArray("profiles"):new JSONArray();if(profiles.length()>2)throw new IllegalArgumentException("عدد ملفات المنشآت غير صالح");for(int i=0;i<profiles.length();i++)new JSONObject(profiles.getJSONObject(i).getString("data"));
        JSONArray quotes=backup.optInt("version")>=5?backup.getJSONArray("quotes"):new JSONArray();if(quotes.length()>10000)throw new IllegalArgumentException("عروض كثيرة جدًا");for(int i=0;i<quotes.length();i++)Quotation.validate(new JSONObject(quotes.getJSONObject(i).getString("data")));
        JSONArray work=backup.optInt("version")>=7?backup.getJSONArray("softwareRequests"):new JSONArray(),products=backup.optInt("version")>=7?backup.getJSONArray("softwareProducts"):new JSONArray();if(work.length()>20000||products.length()>5000)throw new IllegalArgumentException("بيانات البرامج كبيرة جدًا");for(int i=0;i<work.length();i++)SoftwareWork.validate(new JSONObject(work.getJSONObject(i).getString("data")));
        SQLiteDatabase db=getWritableDatabase();java.util.ArrayList<java.io.File> installed=new java.util.ArrayList<>();boolean restored=false;db.beginTransaction();
        try {
            for(int i=0;i<leads.length();i++)validateLead(leads.getJSONObject(i));
            if(backup.optInt("version")>=7){db.delete("softwareRequests",null,null);db.delete("softwareProducts",null,null);}
            if(backup.optInt("version")>=5)db.delete("quotes",null,null);
            if(backup.optInt("version")>=4)db.delete("profiles",null,null);
            db.delete("postShares",null,null);db.delete("posts",null,null);db.delete("events",null,null);db.delete("messages",null,null);db.delete("leads",null,null);
            for(int i=0;i<leads.length();i++)insertJson(db,"leads",leads.getJSONObject(i));
            for(int i=0;i<messages.length();i++)insertJson(db,"messages",messages.getJSONObject(i));
            for(int i=0;i<events.length();i++)insertJson(db,"events",events.getJSONObject(i));
            for(int i=0;i<posts.length();i++)insertJson(db,"posts",posts.getJSONObject(i));
            for(int i=0;i<shares.length();i++)insertJson(db,"postShares",shares.getJSONObject(i));
            for(int i=0;i<profiles.length();i++)insertJson(db,"profiles",profiles.getJSONObject(i));
            for(int i=0;i<quotes.length();i++)insertJson(db,"quotes",quotes.getJSONObject(i));
            for(int i=0;i<work.length();i++){JSONObject row=work.getJSONObject(i);JSONObject q=new JSONObject(row.getString("data"));q.put("id",row.getLong("id")).put("leadId",row.getLong("leadId"));row.put("data",q.toString()).put("notified","{}");insertJson(db,"softwareRequests",row);}
            for(int i=0;i<products.length();i++)insertJson(db,"softwareProducts",products.getJSONObject(i));
            db.execSQL("UPDATE leads SET lastNotified=''");
            installed=PostMedia.install(context,imagePlan);db.setTransactionSuccessful();restored=true;
        } finally { db.endTransaction();if(!restored)PostMedia.remove(installed); }
        PostMedia.prune(context,posts);
    }
    private void validateLead(JSONObject lead)throws Exception{
        int stage=lead.optInt("stage");if(stage<0||stage>5)throw new IllegalArgumentException("تُرفض النسخة: مرحلة عميل غير صالحة في بياناتها");
        String business=lead.optString("business");if(!business.isEmpty()&&!business.equals("survey")&&!business.equals("software"))throw new IllegalArgumentException("تُرفض النسخة: نشاط عميل غير صالح في بياناتها");
        String requestType=lead.optString("requestType","sales");if(!SurveyRequests.valid(requestType))throw new IllegalArgumentException("تُرفض النسخة: نوع طلب غير صالح في بياناتها");
        String due=lead.optString("followUp");if(!due.isEmpty()){try{java.time.LocalDate.parse(due);}catch(Exception e){throw new IllegalArgumentException("تُرفض النسخة: موعد متابعة غير صالح في بياناتها");}}
        String time=lead.optString("followTime","09:00");try{java.time.LocalTime.parse(time);}catch(Exception e){throw new IllegalArgumentException("تُرفض النسخة: وقت متابعة غير صالح في بياناتها");}
    }
    private void insertJson(SQLiteDatabase db,String table,JSONObject o) throws Exception {
        ContentValues v=new ContentValues(); try(Cursor columns=db.rawQuery("SELECT * FROM "+table+" LIMIT 0",null)){
            for(String key:columns.getColumnNames()) if(o.has(key)){if(o.isNull(key))v.putNull(key);else if(o.get(key) instanceof Number)v.put(key,o.getLong(key));else v.put(key,o.getString(key));}
        } db.insertOrThrow(table,null,v);
    }
}
