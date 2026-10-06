package com.amalaei.engineering;
import org.json.*;
import java.util.*;
/** Filters a legacy combined backup before any database writes. */
final class BackupPartition {
 static JSONObject select(JSONObject input)throws Exception {
  String marker=input.optString("appMode");boolean own=marker.equals(AppMode.MODE);
  if(!marker.isEmpty()&&!own)throw new IllegalArgumentException("هذه النسخة تخص التطبيق الآخر");
  JSONObject out=new JSONObject(input.toString());Set<Long> leads=new HashSet<>(),posts=new HashSet<>();Set<String> images=new HashSet<>();
  JSONArray kept=new JSONArray(),rows=input.optJSONArray("leads");if(rows==null)throw new IllegalArgumentException("نسخة غير صالحة");
  for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);String b=r.optString("business");if(b.equals(AppMode.MODE)||(own&&b.isEmpty())){kept.put(r);leads.add(r.getLong("id"));}}out.put("leads",kept);
  out.put("events",related(input,"events","leadId",leads,false));out.put("messages",related(input,"messages","leadId",leads,own));
  out.put("profiles",business(input,"profiles"));JSONArray ps=business(input,"posts");out.put("posts",ps);
  for(int i=0;i<ps.length();i++){JSONObject r=ps.getJSONObject(i);posts.add(r.getLong("id"));if(!r.optString("image").isEmpty())images.add(r.getString("image"));}
  out.put("postShares",related(input,"postShares","postId",posts,false));JSONObject src=input.optJSONObject("postImages"),dest=new JSONObject();if(src!=null)for(String image:images)if(src.has(image))dest.put(image,src.get(image));out.put("postImages",dest);
  JSONArray quotes=new JSONArray();if(AppMode.MODE.equals("survey")){JSONArray qs=input.optJSONArray("quotes");if(qs!=null)for(int i=0;i<qs.length();i++){JSONObject r=qs.getJSONObject(i),data=new JSONObject(r.getString("data"));if(data.optString("business","survey").equals("survey"))quotes.put(r);}}
  out.put("quotes",quotes);out.put("softwareRequests",AppMode.MODE.equals("software")?related(input,"softwareRequests","leadId",leads,false):new JSONArray());out.put("softwareProducts",AppMode.MODE.equals("software")?array(input,"softwareProducts"):new JSONArray());return out.put("appMode",AppMode.MODE);
 }
 static JSONArray array(JSONObject in,String key){JSONArray a=in.optJSONArray(key);return a==null?new JSONArray():a;}
 static JSONArray business(JSONObject in,String key)throws Exception {JSONArray out=new JSONArray(),rows=array(in,key);for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);if(r.optString("business").equals(AppMode.MODE))out.put(r);}return out;}
 static JSONArray related(JSONObject in,String key,String field,Set<Long> ids,boolean pending)throws Exception {JSONArray out=new JSONArray(),rows=array(in,key);for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);long id=r.optLong(field);if(ids.contains(id)||(pending&&id==0))out.put(r);}return out;}
}
