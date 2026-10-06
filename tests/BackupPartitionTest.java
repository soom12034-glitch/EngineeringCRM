package com.amalaei.engineering;
import org.json.*;
final class BackupPartitionTest {
 static void check(boolean x,String label){if(!x)throw new AssertionError(label);}
 public static void main(String[] args)throws Exception {
 JSONObject old=new JSONObject().put("version",7).put("leads",new JSONArray("[{id:1,business:survey},{id:2,business:software},{id:3,business:''}]"))
 .put("events",new JSONArray("[{leadId:1},{leadId:2},{leadId:3}]"))
 .put("messages",new JSONArray("[{leadId:1},{leadId:2},{leadId:null}]"))
 .put("profiles",new JSONArray("[{business:survey,data:'{}'},{business:software,data:'{}'}]"))
 .put("posts",new JSONArray("[{id:10,business:survey,image:'a'},{id:20,business:software,image:'b'}]"))
 .put("postShares",new JSONArray("[{postId:10},{postId:20}]"))
 .put("postImages",new JSONObject().put("a","survey-image").put("b","software-image"))
 .put("quotes",new JSONArray().put(new JSONObject().put("data","{business:survey}")))
 .put("softwareRequests",new JSONArray("[{leadId:2},{leadId:1}]"))
 .put("softwareProducts",new JSONArray("[{}]"));
 JSONObject selected=BackupPartition.select(old);boolean survey=AppMode.MODE.equals("survey");long id=survey?1:2;
 check(selected.getJSONArray("leads").length()==1&&selected.getJSONArray("leads").getJSONObject(0).getLong("id")==id,"only own clients");
 for(String key:new String[]{"events","messages","profiles","posts","postShares"})check(selected.getJSONArray(key).length()==1,key);
 check(selected.getJSONObject("postImages").length()==1&&selected.getJSONObject("postImages").has(survey?"a":"b"),"only own media");
 check(selected.getJSONArray("quotes").length()==(survey?1:0),"quotes separated");
 check(selected.getJSONArray("softwareRequests").length()==(survey?0:1),"software work separated");
 check(selected.getJSONArray("softwareProducts").length()==(survey?0:1),"catalog separated");
 check(old.getJSONArray("leads").length()==3&&!old.has("appMode"),"original unchanged");
 old.put("appMode",AppMode.MODE);selected=BackupPartition.select(old);check(selected.getJSONArray("leads").length()==2,"own unassigned retained");check(selected.getJSONArray("messages").length()==2,"own pending retained");
 old.put("appMode",survey?"software":"survey");try{BackupPartition.select(old);throw new AssertionError("wrong backup accepted");}catch(IllegalArgumentException expected){}
 System.out.println("PASS: "+AppMode.MODE+" clients, relationships, profiles, advertising images, quotations, software records, pending data and opposite-backup rejection.");
 }
}
