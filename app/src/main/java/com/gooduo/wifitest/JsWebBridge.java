package com.gooduo.wifitest;


import com.android.volley.RequestQueue;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Administrator on 2016/7/26.
 */
public class JsWebBridge extends JsBridge {
    private Db mDb;
    private RequestQueue mRequestQueue;

    public JsWebBridge(Handler handler, Db mDb, RequestQueue rq){
        super(handler);
        this.mDb=mDb;
        mRequestQueue=rq;
    }
    public void syncUserInf(){

    }
    @JavascriptInterface
    private void sendRequest(JsonObjectRequest jor){
        mRequestQueue.add(jor);
    }
    private JsonObjectRequest createRequest(String url,String postParmJson, final String callBackName,final String callFailName){
        JSONObject postParm;
        try{
            postParm=new JSONObject(postParmJson);
            JsonObjectRequest jor=new JsonObjectRequest(url,postParm,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject){
                            String sParm=jsonObject.toString();
                            postToJs(callBackName, sParm);
                        }
                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            postToJs(callFailName,volleyError.getMessage());
                        }
                    }
            );
            return jor;
        }catch(JSONException e){
            Log.i("godlee", e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

}
