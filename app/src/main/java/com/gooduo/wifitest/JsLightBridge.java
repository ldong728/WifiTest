package com.gooduo.wifitest;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

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
    public void setCloudCode(final String data){
        JSONObject sJson;
        boolean stu;
        int prob,mask;

        try{
            sJson = new JSONObject(data);
            stu = Integer.parseInt(sJson.getString("stu"))==0? false:true;
            prob = Integer.parseInt(sJson.getString("prob"));
            mask= Integer.parseInt(sJson.getString("mask"));
            mLightControllerGroup.setCloud(stu, prob, mask);
        }catch(JSONException e){
            Log.e("godlee",e.getMessage());
            e.printStackTrace();
        }
    }
    @JavascriptInterface
    public void setFlashCode(final String data){
        JSONObject sJson;
        int level,prob;
        try{
            sJson = new JSONObject(data);
            prob = Integer.parseInt(sJson.getString("prob"));
            level= Integer.parseInt(sJson.getString("level"));
            mLightControllerGroup.setFlash(prob, level);
        }catch(JSONException e){
            Log.e("godlee",e.getMessage());
            e.printStackTrace();
        }
    }
    public void setMoonCode(final String data){
        JSONObject sJson;
        boolean stu;
        int start,end;

        try{
            sJson = new JSONObject(data);
            stu = Integer.parseInt(sJson.getString("stu"))==0? false:true;
            start = Integer.parseInt(sJson.getString("start"));
            end= Integer.parseInt(sJson.getString("end"));
            mLightControllerGroup.setMoon(stu, start, end);
        }catch(JSONException e){
            Log.e("godlee",e.getMessage());
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public byte[] initTime(String ip){
        SimpleDateFormat data=new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
        String sTime=data.format(new java.util.Date());
        String[] times=sTime.split(",");
        int y=Integer.parseInt(times[0]);
        int M=Integer.parseInt(times[1]);
        int d=Integer.parseInt(times[2]);
        int h=Integer.parseInt(times[3]);
        int m=Integer.parseInt(times[4]);
        int s=Integer.parseInt(times[5]);
        byte[] timeCode=new byte[]{
                (byte)0xaa,
                (byte)0x08,
                (byte)0x0a,
                (byte)0x09,
                (byte)(y-1970),
                (byte)M,
                (byte)d,
                (byte)h,
                (byte)m,
                (byte)s,
                (byte)0,
                (byte)(0x0a+0x09+(y-1970)+M+d+h+m+s)
        };
        mLightControllerGroup.initTime(timeCode,ip);
        Log.i("godlee",data.toPattern());
        Log.i("godlee",data.toLocalizedPattern());
        Log.i("godlee",data.format(new java.util.Date()));
        return timeCode;
    }

    @JavascriptInterface
    public String getAutoCode(){
        return mLightControllerGroup.getAutoStu();
    }

    @JavascriptInterface
    public String getManualCode(){
        return mLightControllerGroup.getManualStu();
    }
    @JavascriptInterface
    public String getCloudCode(){
        return mLightControllerGroup.getCloudStu();
    }
    @JavascriptInterface
    public String getFlashCode(){
        return mLightControllerGroup.getFlashStu();
    }
    @JavascriptInterface
    public String getMoonCode(){
        return mLightControllerGroup.getMoonStu();
    }

    @JavascriptInterface
    public String getLightList(){
        return mLightControllerGroup.getLightsList();
    }



}
