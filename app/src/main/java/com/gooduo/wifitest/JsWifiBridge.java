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

import java.text.SimpleDateFormat;
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
    public void getLinkResult(int apStu,int pasdStu){
        JSONObject obj=new JSONObject();
        try{
            obj.accumulate("apStu",""+apStu);
            obj.accumulate("pasdStu",+pasdStu);
            postToJs("ap2staResult",obj.toString());
        }catch(JSONException e){
            Log.e("godlee",e.getMessage());
            e.printStackTrace();
        }
    }
    public void lightOk(String mac) {
        postToJs("lightStandby",mac);
    }


    @JavascriptInterface
    public void ap2sta(String data) {
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
//    public void Ap2Sta(String data){
//        JSONObject obj;
//        String ssid,pwd;
//        try{
//            obj=new JSONObject((data));
//            ssid=obj.getString("ssid");
//            pwd=obj.getString("pwd");
//            byte[] code= Tool.generate_02_data(ssid,pwd,0);
//            mUdpController.sendMsg(code,UdpController.DEFALT_IP,UdpController.CTR_PORT);
//        }catch(JSONException e){
//            Log.e("godlee",e.getMessage());
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
        postToJs("onGetWifiList", info);
    }
    @JavascriptInterface
    public void linkWifi(final String data){
        try{
            JSONObject obj=new JSONObject(data);
            String ssid=obj.getString("ssid");
//            int id=obj.getInt("id");
            Log.i("godlee","linkedTo:"+ssid);
            mWifiManager.connectBySSID(ssid);
        }catch(JSONException e){
            Log.e("godlee",e.getMessage());
            e.printStackTrace();
        }

    }
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
