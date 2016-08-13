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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;



public class MainActivity extends AppCompatActivity {
    private static WifiClass mWifiManage;
    private static Db mDb;
    private static WebView mWebView;
    private static UdpController mUdpController;
    private static LightControllerGroup mLightControllerGroup;
    private static JsLightBridge mLightBridge;
    private static JsWifiBridge mWifiBridge;
    private static JsWebBridge mWebBridge;
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
//                    mWifiManage.connectBySSID(ssid);
                    break;
                }

                case JsBridge.JS: {
                    JSONObject obj=(JSONObject)msg.obj;
                    try{
                        String function=obj.getString("function");
                        String value=obj.getString("value");
                        mWebView.loadUrl("javascript:"+function+"('"+value+"')");
                    }catch(JSONException e){
                        Log.e("godlee",e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                }

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
                Log.i("godlee",new String(pack.getData()));
                return;
            }
        };
        mUdpController.start();
        mDb=new Db(this,Db.DB_NAME,null,2);
        mLightControllerGroup=new LightControllerGroup(mHander);
        mWifiBridge=new JsWifiBridge(mUdpController,mWifiManage,mHander,mDb);
        mLightBridge=new JsLightBridge(mHander,mLightControllerGroup,mDb);
        mWebBridge =new JsWebBridge(mHander,mDb,mRequestQueue);
        mLightControllerGroup.addGroupMember("C4BE8474C223");
        mLightControllerGroup.addGroupMember("F4B85E45D9F1");
        mWebView = new WebView(this);
//        mDb.onCreate(mDb.getWritableDatabase());
        mWebSetting = mWebView.getSettings();
        mWebSetting.setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new mWebViewClint());
        setContentView(mWebView);
        mWebView.loadUrl("file:///android_asset/index.html");
        mWebView.addJavascriptInterface(mWifiBridge, "wifi");
        mWebView.addJavascriptInterface(mLightBridge, "light");
        mWebView.addJavascriptInterface(mWebBridge,"web");
        mReceiver=new WifiReceiver();
        IntentFilter filter=new IntentFilter();
//        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver,filter);
        Log.i("godlee", "wifiTest started");
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
                            Log.i("godlee","Ap Name:"+info.getSSID());
                            Log.i("godlee", "Ap IP:" + gateIp);
                            if(gateIp.equals(UdpController.DEFALT_IP)){
//                        mLightBridge.initTime(UdpController.DEFALT_IP);
//                        Log.i("godlee","inittimeOK");
                                String ssid= mWifiBridge.linkedOk(info.getSSID());
//                        Log.i("godlee","recallToUi");
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
