package com.amalaei.engineering;

import org.json.JSONException;
import org.json.JSONObject;

final class PostSharingRules {
    PostSharingRules() {
    }

    static boolean advance(JSONObject jSONObject, long j) throws JSONException {
        if (!jSONObject.optBoolean("active") || jSONObject.optLong("next") > j) {
            return false;
        }
        int iMax = Math.max(0, jSONObject.optInt("remaining") - 1);
        jSONObject.put("remaining", iMax).put("active", iMax > 0).put("next", j + (jSONObject.optLong("minutes") * 60000)).put("notified", false);
        return true;
    }
}
