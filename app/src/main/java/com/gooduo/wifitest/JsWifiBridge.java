package com.gooduo.wifitest;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/15.
 */
public class JsWifiBridge {
    public static final int JS=0xcafe;
    private UdpController mUdpController;
    private WifiClass mWifiManager;
    private Handler mHandler;

    public JsWifiBridge(UdpController uc,WifiClass wm,Handler handler){
        mUdpController=uc;
        mWifiManager=wm;
        mHandler=handler;
    }
    public void getList(byte[] data, WebView mWebView) {
        if(0x81==(data[3] & 0xff)){
            JSONObject obj = new JSONObject();
            ArrayList<Item> ssids = Tool.decode_81_data(data);
            if (ssids.size() != 0) {
                for (Item ssid : ssids) {
                    try {
                        Log.i("godlee", ssid.getName() + ":" + ssid.getDbm() + "%");
                        obj.accumulate(ssid.getName(), ssid.getDbm());
                    } catch (JSONException e) {
                        Log.e("godlee", e.getMessage());
                        e.printStackTrace();
                    }
                }
                String json = obj.toString();
                Log.i("godlee", json);
                postToJs("getList",json);
//                mWebView.loadUrl("javascript:getList('" + json + "')");
            }
        }
    }
    public void lightOk(String mac) {
        postToJs("lightStandby",mac);
    }


    @JavascriptInterface
    public void linkSSID(String data) {
        JSONObject sJson;
        String ssid, pasd;
        int index = 0;
        byte[] code;
        try {
            sJson = new JSONObject(data);
            ssid = sJson.getString("ssid");
            pasd = sJson.getString("pasd");
            code = Tool.
                    generate_02_data(ssid, pasd, index);
            mUdpController.sendMsg(code, 48899);
        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());
        }
    }
    @JavascriptInterface
    public void usrLink() {
        byte[] data = new byte[]{(byte) 0xff, 0x00, 0x01,
                0x01, 0x02};
        mUdpController.sendMsg(data, 48899);
    }
//    @JavascriptInterface
//    public void switchMode(final String data){
//        JSONObject obj;
//        try{
//            obj=new JSONObject(data);
//            String mac=
//        }catch(JSONException e){
//            e.printStackTrace();
//        }
//    }
    @JavascriptInterface
    public void linkLights() {
        Log.i("godlee","search");
        String code="www.usr.cn";
        byte[] data=code.getBytes();
        mUdpController.sendMsg(data,48899);
    }

    @JavascriptInterface
    public void scanWifi(){
        Log.i("godlee","start scan");
        mWifiManager.startScan();
        Log.i("godlee","scan over");
        String info=mWifiManager.getResultJson();
        postToJs("onGetWifiList",info);
    }
//    @JavascriptInterface
//    public void greenOff() {
//        Log.i("godlee","getMac");
//        String code="AT+WSCAN\r";
//        byte[] data=code.getBytes();
//        mUdpController.sendMsg(data, 48899);
//
////            byte[] data = new byte[]{(byte) 0xAA, (byte) 0x08, (byte) 0x0A, (byte) 0x01, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x12};
////            mUdpController.putMsg(data);
//    }
    private void postToJs(String functionName,String value){
        JSONObject obj=new JSONObject();
        try{
            obj.accumulate("function",functionName);
            obj.accumulate("value",value);
            Message msg=mHandler.obtainMessage(JS,obj);
            mHandler.sendMessage(msg);
        }catch(JSONException e){
            Log.e("godlee",e.getMessage());
            e.printStackTrace();
        }


    }



}
