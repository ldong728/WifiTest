package com.gooduo.wifitest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;


public class MainActivity extends AppCompatActivity {
    public static final boolean IS_DEBUG=true;
    private static WifiClass mWifiManage;
    private static Db mDb;
    private static WebView mWebView;
    private static UdpController mUdpController;
    private static LightControllerGroup mLightControllerGroup;
    private static JsLightBridge mLightBridge;
    private static JsWifiBridge mWifiBridge;
    private static WifiReceiver mReceiver;
    private MyHandler mHander = new MyHandler(this);
    private RequestQueue mRequestQueue;
    private static class MyHandler extends Handler {
        WeakReference<AppCompatActivity> mActivity;
        MyHandler(AppCompatActivity activity) {
            mActivity = new WeakReference<AppCompatActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            AppCompatActivity sActivity = mActivity.get();
            switch (msg.what) {
                case JsBridge.LOCAL_LINK:{
                    String ssid=(String)msg.obj;
                    break;
                }

                case JsBridge.JS: {
                    JSONObject obj=(JSONObject)msg.obj;
                    try{
                        String function=obj.getString("function");
                        String value=obj.getString("value");
                        mWebView.loadUrl("javascript:"+function+"('"+value+"')");
                    }catch(JSONException e){
                        D.e(e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                }
                case JsBridge.TO_OPT:
                    Intent sIntent=new Intent(Settings.ACTION_WIFI_SETTINGS);
                    sActivity.startActivity(sIntent);
                    break;

                case LightControllerGroup.SEND_OK:
                    Log.i("godlee", "send all over");

                    break;
                case LightControllerGroup.SINGLE_SEND_OK:
                    String ip=(String)msg.obj;
                    Log.i("godlee","send To "+ip+" ok!");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final WebSettings mWebSetting;
        super.onCreate(savedInstanceState);
        mRequestQueue= Volley.newRequestQueue(this);
        mWifiManage=new WifiClass(this,mHander);

        mUdpController = new UdpController(mHander) {
            @Override
            public void onReceive(Handler handler, DataPack pack) {
                String fromIp=pack.getIp();
//                int fromPort=pack.getPort();
                byte[] data=pack.getData();
                Log.i("godlee","main Thread receive,length:"+data.length);
                if(fromIp.equals(UdpController.DEFALT_IP)){
                    if(0xff==(data[0]&0xff)){
                        if(0x81==(data[3]&0xff)){
                            mWifiBridge.getList(data,mWebView);
                            return;
                        }
                        if(0x82==(data[3]&0xff)){
                            int ssidStu=data[4]&0xff;
                            int pasdStu=data[5]&0xff;
                            mWifiBridge.getLinkResult(ssidStu,pasdStu);
                        }

                    }
                    if(0xaa==(data[0]&0xff)){
                        Log.i("godlee",Tool.bytesToHexString(data));
                        return;
                    }
                }


                String mac=mLightControllerGroup.reflushDeviceIp(data);
                if(null!=mac){
                    sendATMsg("AT+ENTM\r\n",pack.getIp(),pack.getPort());
                    if(!mac.equals("other")){
                        mWifiBridge.lightOk(mac);
                    }
                }

                Log.i("godlee",Tool.bytesToHexString(pack.getData()));
//                Log.i("godlee",new String(pack.getData()));
                return;
            }
        };
        mUdpController.start();
        mDb=new Db(this,Db.DB_NAME,null,2);
        mLightControllerGroup=new LightControllerGroup(mHander);
//        mWebController = new WebSocketController(mHander);
        mWifiBridge=new JsWifiBridge(mUdpController,mWifiManage,mHander);
        mLightBridge=JsLightBridge.initJsLightBridge(mHander,mLightControllerGroup,mDb);
//        mWebBridge =new JsWebBridge(mHander,mDb,mRequestQueue);

//        mLightControllerGroup.addGroupMember("C4BE8474C223");
//        mLightControllerGroup.addGroupMember("F4B85E45D9F1");
        mWebView = new WebView(this);
        mWebSetting = mWebView.getSettings();
        mWebSetting.setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new mWebViewClint());
        setContentView(mWebView);
        mWebView.loadUrl("file:///android_asset/index.html");
        mWebView.addJavascriptInterface(mWifiBridge, "wifi");
        mWebView.addJavascriptInterface(mLightBridge, "light");
//        mWebView.addJavascriptInterface(mWebBridge,"web");
//        mWebView.addJavascriptInterface(mWebController,"web");
        mReceiver=new WifiReceiver();
        IntentFilter filter=new IntentFilter();
//        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver,filter);
//        mWebController.connect();
        Log.i("godlee", "wifiTest started");
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                D.i("canGoBack? "+mWebView.canGoBack());
//                D.i("title:"+mWebView.getTitle());
//                D.i("title:"+mWebView.getUrl());
                if(keyCode == KeyEvent.KEYCODE_BACK && !mWebView.getUrl().equals("file:///android_asset/equip_index.html")){
//                    mWebView.goBack();
                    mWebView.loadUrl("javascript:"+"goBack()");
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        // 退出处理
        mUdpController.setReceive(false);
        mUdpController.close();
        mLightControllerGroup.finishAll();
        System.exit(0);
    }

    /**
     * 保持在页面内跳转
     */
    class mWebViewClint extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            view.loadUrl(url);

            return true;

        }
    }

    class WifiReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

//            Log.i("godlee",intent.getAction());
            switch(intent.getAction()){
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo ntwInf=(NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if(null!=ntwInf){
                        if(NetworkInfo.State.CONNECTED==ntwInf.getState()){
                            WifiManager wm=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                            WifiInfo info=wm.getConnectionInfo();
                            DhcpInfo dhcpInfo=wm.getDhcpInfo();
                            String gateIp=Tool.intIpToString(dhcpInfo.serverAddress);
                            Log.i("godlee","Ap NameChang:"+info.getSSID());
                            Log.i("godlee", "Ap IP:" + gateIp);
                            String ssid= mWifiBridge.linkedOk(info.getSSID());
                            WifiClass.ssid=ssid;
                            if(gateIp.equals(UdpController.DEFALT_IP)){//如果路由器IP为默认IP，即连接的设备为AP模式
                                if(null!=ssid){
                                    WifiClass.ssid=ssid;
                                    Log.i("godlee","currentSSid:"+WifiClass.ssid);
                                }
                            }else{

                            }
                        }
                    }

                    break;
//                case WifiManager.RSSI_CHANGED_ACTION:
//
//                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:

                    break;
            }

        }
    }



}
