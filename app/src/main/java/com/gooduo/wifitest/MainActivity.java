package com.gooduo.wifitest;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static WifiClass mWifiManage;
    private static WebView mWebView;
    private static UdpController mUdpController;
    private static LightControllerGroup mLightControllerGroup;
    private static JsLightBridge mLightBridge;
    private static JsWifiBridge mWifiBridge;
    private static class MyHandler extends Handler {
        WeakReference<AppCompatActivity> mActivity;
        MyHandler(AppCompatActivity activity) {
            mActivity = new WeakReference<AppCompatActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            AppCompatActivity sActivity = mActivity.get();
//            DataPack sDatagram = (DataPack) msg.obj;
//            String fromIp=sDatagram.getIp();
//            int fromPort=sDatagram.getPort();
//            byte[] data=sDatagram.getData();
            switch (msg.what) {
                case Tool.REC_DATA: {
//                    String sData = Tool.bytesToHexString(data);
//                    Log.i("godlee", "msg:" + sData);
                    break;
                }
                case Tool.ERR_DATA: {
                    break;
                }
                case Tool.WIFI_LIST_DATA: {
                    break;
                }
                case Tool.CFM_DATA: {
//                    String sData = Tool.bytesToHexString(data);
                    break;
                }
                case JsWifiBridge.JS: {
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
    private MyHandler mHander = new MyHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final WebSettings mWebSetting;
        super.onCreate(savedInstanceState);
        mWifiManage=new WifiClass(this);
        mUdpController = new UdpController(mHander) {
            @Override
            public void onReceive(Handler handler, DataPack pack) {

                byte[] data=pack.getData();
                Log.i("godlee","main Thread receive,length:"+data.length);
                if(0xaa==(data[0]&0xff)){

                    return;
                }
                if(0xff==(data[0]&0xff)){
                    mWifiBridge.getList(data,mWebView);
                    return;
                }
                String mac=mLightControllerGroup.getIp(data);
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
        mLightControllerGroup=new LightControllerGroup(mHander);
        mWifiBridge=new JsWifiBridge(mUdpController,mWifiManage,mHander);
        mLightBridge=new JsLightBridge(mLightControllerGroup);
        mLightControllerGroup.addGroupMember("C4BE8474C223");
        mLightControllerGroup.addGroupMember("F4B85E45D9F1");
        mWebView = new WebView(this);
        mWebSetting = mWebView.getSettings();
        mWebSetting.setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new mWebViewClint());
        setContentView(mWebView);
        mWebView.loadUrl("file:///android_asset/index.html");
        mWebView.addJavascriptInterface(mWifiBridge, "wifi");
        mWebView.addJavascriptInterface(mLightBridge,"light");
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
        // 退出处理
//        lock.release();
//        smt.setSend(false);
//        mTcpController.close();
        mUdpController.setReceive(false);
        mUdpController.close();
        mLightControllerGroup.finishAll();
    }

    class mWebViewClint extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            view.loadUrl(url);

            return true;

        }
    }

    private void decodeData(byte[] data) {
        Log.i("godlee", Tool.bytesToHexString(data));
    }





    /*
    jsBridge类
     */
    class JsBrg {
        private Activity mActivity;

        public JsBrg(Activity activity) {
            this.mActivity = activity;
//            initSocket();
        }

//        @JavascriptInterface
//        public void startUdpServer() {
//            Log.i("godlee", "startServer");
//            mUdpController.start();
//        }


        @JavascriptInterface
        public String test() {
            mUdpController.sendMsg(new byte[]{(byte) 0xaa, 0x08, 0x0a, 0x09, 0x2d, 0x0a, 0x11, 0x00, 0x00, 0x00, 0x00, 0x5c}, 8899);

//            Log.i("godlee", "senddata" + data);
//            mTcpController.sendData(S);
//            try {
//                JSONObject sJson = new JSONObject(data);
//            } catch (JSONException e) {
//                Log.e("godlee", e.getMessage());
//            }
            return "ok";
        }





        @JavascriptInterface
        public void greenOff() {
            Log.i("godlee","getMac");
            String code="AT+WSCAN\r";
            byte[] data=code.getBytes();
            mUdpController.sendMsg(data,48899);

//            byte[] data = new byte[]{(byte) 0xAA, (byte) 0x08, (byte) 0x0A, (byte) 0x01, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x12};
//            mUdpController.putMsg(data);
        }

        @JavascriptInterface
        public void startServer() {
            Log.i("godlee", "startServer");
//            mTcpController.start();
        }

        @JavascriptInterface
        public void stopServer() {
            Log.i("godlee", "stopServer");
//            mTcpController.stopServer();
        }

        @JavascriptInterface
        public void initSocket() {
            Log.i("godlee", "socket init");
//            mTcpController.init("192.168.1.1", 8899);
        }


    }
}
