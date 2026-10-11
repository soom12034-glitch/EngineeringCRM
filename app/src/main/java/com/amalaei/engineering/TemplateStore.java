package com.amalaei.engineering;

import org.json.*;

final class TemplateStore {
    static JSONArray defaults(){try{return new JSONArray().put(new JSONObject().put("title","متابعة بعد عرض السعر").put("body","السلام عليكم {الاسم}،\nنود متابعة عرض السعر رقم {رقم_العرض} بقيمة {المبلغ}. هل ترغب في توضيح أي بند؟")).put(new JSONObject().put("title","تذكير بموعد").put("body","السلام عليكم {الاسم}،\nنذكرك بموعد المتابعة المتفق عليه. يسعدنا خدمتك.")).put(new JSONObject().put("title","شكر بعد إتمام الطلب").put("body","شكرًا لك {الاسم} على ثقتك في {الشركة}. يسعدنا خدمتك دائمًا."));}catch(Exception e){return new JSONArray();}}
    static JSONArray custom(JSONObject profile){JSONArray array=profile.optJSONArray("whatsappTemplates");return array==null?new JSONArray():array;}
}
