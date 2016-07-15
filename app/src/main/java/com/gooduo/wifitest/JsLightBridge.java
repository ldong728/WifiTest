package com.gooduo.wifitest;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/7/14.
 */

public class JsLightBridge {
    private LightControllerGroup mLightControllerGroup;
    public JsLightBridge(LightControllerGroup lightControllerGroup){
        mLightControllerGroup=lightControllerGroup;
    }

    @JavascriptInterface
    public void sendAutoCode(final String data) {
        JSONObject sJson;
        int color, time, level;
        boolean send;
        try {
            sJson = new JSONObject(data);
            color = Integer.parseInt(sJson.getString("color"));
            time = Integer.parseInt(sJson.getString("time"));
            level = Integer.parseInt(sJson.getString("level"));
            send=sJson.getString("mode").equals("confirm")? true:false;
            mLightControllerGroup.autoController(color,time,level,send);
        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void setManualCode(final String data){
        JSONObject sJson;
        int color,level;
        try{
            sJson = new JSONObject(data);
            color = Integer.parseInt(sJson.getString("color"));
            level = Integer.parseInt(sJson.getString("level"));
            mLightControllerGroup.manualController(color, level);
        }catch(JSONException e){
            Log.e("godlee",e.getMessage());
            e.printStackTrace();
        }
    }
    @JavascriptInterface
    public String getAutoCode(){
        return mLightControllerGroup.getAutoStu();
    }
    @JavascriptInterface
    public String getLightList(){
        return mLightControllerGroup.getLightsList();

    }



}
