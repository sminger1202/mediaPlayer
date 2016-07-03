package com.youku.player.grey;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by liangji on 15/8/24.
 */
public class GreyConfigParser {
    private static final String TAG = LogTag.TAG_GREY;

    public static GreyParam parseJson(String json) {

        GreyParam param=null;
        if (json == null)
            return param;

        // {"status":"success","deliver_all":[],"hit":{"feature_id":[],"config_id":"","state":0}}
        Logger.d(TAG, "json = " + json);

        try {
            JSONObject object = new JSONObject(json);

            if (object.has("status") && object.getString("status").equals("success")) {
                param=new GreyParam();

                parseHit(param,object.optJSONObject("hit"));
                parseDeliver(param,object.optJSONArray("deliver_all"));

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e(TAG, e);
        }
        return param;
    }
    // "deliver_all":[{"feature_id":2,"state":2},{"feature_id":1,"state":2}]
    protected static void parseDeliver(GreyParam param, JSONArray array) throws JSONException {
        if (array == null)
            return;
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object != null) {
                param.addDeliver(object.optInt("feature_id"), object.optInt("state"));
            }

        }
    }
    // hit":{"feature_id":[],"config_id":"","state":0}}
    protected static void parseHit(GreyParam param, JSONObject object) throws JSONException
    {
        if (object==null)
            return;
        JSONArray array = object.optJSONArray("feature_id");
        if (array!=null)
        {
            for (int i = 0; i < array.length(); i++) {
                param.addHit(array.optInt(i));
            }
        }
        param.hit_config_id=object.optInt("config_id");
        param.hit_state=object.optInt("state");

    }
}
