package com.gooduo.wifitest;

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
public class JsWifiBridge extends JsBridge{
    
    private UdpController mUdpController;
    private WifiClass mWifiClass;
//    private Handler mHandler;
    private Db mDb;

    public JsWifiBridge(UdpController uc,WifiClass wm,Handler handler,Db mDb){
        super(handler);
        mUdpController=uc;
        mWifiClass =wm;
//        mHandler=handler;
        this.mDb=mDb;
    }
    public static String trimSSid(String ssid){
        String sData;
        if(ssid.startsWith("\"")){
            sData=ssid.replace("\"","");
        }else{
            sData=ssid;
        }
        return sData;
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
    public String linkedOk(String data){
        if(null!=data){
            String sData=trimSSid(data);
//            if(data.startsWith("\"")){
//                sData=data.replace("\"","");
//            }else{
//                sData=data;
//            }

            postToJs("onLinked",sData);
            return sData;
        }
        return null;

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
            Log.i("godlee","order:"+Tool.bytesToHexString(code));
        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());
        }
    }
    @JavascriptInterface
    public void usrLink() {
        byte[] data = new byte[]{(byte) 0xff, 0x00, 0x01,
                0x01, 0x02};
        mUdpController.sendMsg(data,UdpController.DEFALT_IP,48899);
    }

    @JavascriptInterface
    public void linkLights() {
        Log.i("godlee","search");
        String code="www.usr.cn";
        byte[] data=code.getBytes();
        mUdpController.sendMsg(data,48899);
    }

    @JavascriptInterface
    public void scanWifi(){
        Log.i("godlee", "start scan");
        mWifiClass.startScan();
        Log.i("godlee", "scan over");
        String info= mWifiClass.getResultJson();
        postToJs("onGetWifiList", info);
    }
    @JavascriptInterface
    public void linkWifi(final String data){
        try{
            JSONObject obj=new JSONObject(data);
            String ssid=obj.getString("ssid");
//            int id=obj.getInt("id");
            Log.i("godlee","linkedTo:"+ssid);
            mWifiClass.connectBySSID(ssid);
        }catch(JSONException e){
            Log.e("godlee",e.getMessage());
            e.printStackTrace();
        }

    }
    @JavascriptInterface
    public void unlinkWifi(){
        mWifiClass.disConnectionWifi(mWifiClass.getNetWorkId());
    }

    @JavascriptInterface
    public void wifiOpt(){
//        Log.i("godlee","jump");
       mHandler.sendEmptyMessage(TO_OPT);
    }

    @JavascriptInterface

    public String getCurrentSSID(){
        return trimSSid(mWifiClass.getSSID());
    }


}
