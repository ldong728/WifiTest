package com.gooduo.wifitest;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
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
    private static WebView mWebView;
    private static WebSettings mWebSetting;
    private static UdpController mUdpController;
    //    private TcpController mTcpController;
    private WifiManager mWifiManager;
    private LightsController mLightController = new LightsController();

    static class MyHandler extends Handler {
        WeakReference<AppCompatActivity> mActivity;

        MyHandler(AppCompatActivity activity) {
            mActivity = new WeakReference<AppCompatActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            AppCompatActivity sActivity = mActivity.get();
            switch (msg.what) {
                case Tool.REC_DATA: {
                    byte[] data = (byte[]) msg.obj;
                    String sData = Tool.bytesToHexString(data);
//                 decodeData(data);
                    Log.i("godlee", "msg:" + sData);
//                 mWebView.loadUrl("javascript: headerTo('ap_list.html')");
                    break;

                }
                case Tool.ERR_DATA: {
                    byte[] data = (byte[]) msg.obj;
                    String sData = Tool.bytesToHexString(data);
                    Log.i("godlee", " other massage:" + sData);
                    break;
                }
                case Tool.WIFI_LIST_DATA: {
                    getList(msg, mWebView);
                    break;
                }
                case Tool.CFM_DATA: {
                    byte[] data = (byte[]) msg.obj;
                    String sData = Tool.bytesToHexString(data);
                    Log.i("godlee", "cfm massage:" + sData);
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
        super.onCreate(savedInstanceState);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        byte[] ip = Tool.int2byte(dhcpInfo.serverAddress);
        Log.i("godlee", Tool.bytesToHexString(ip));
        Log.i("godlee", "" + (int) (ip[0] & 0xff));
//        mTcpController =new TcpController(this,mHander);
        mUdpController = new UdpController(mHander);

//        mTcpController.start();
//        mUdpController =new UdpBackUp("192.168.0.153",mHander);
        mWebView = new WebView(this);
        mWebSetting = mWebView.getSettings();
        mWebSetting.setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new mWebViewClint());
        setContentView(mWebView);
        mWebView.loadUrl("file:///android_asset/index.html");
        mWebView.addJavascriptInterface(new JsBrg(this), "wifi");
        Log.i("godlee", "wifiTest started");

//        mTcpController.init("192.168.1.1", 8899);


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


    private static void getList(Message msg, WebView mWebView) {
        byte[] data = (byte[]) msg.obj;
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
            mWebView.loadUrl("javascript:getList('" + json + "')");

        }
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

        @JavascriptInterface
        public void startUdpServer() {
            Log.i("godlee", "startServer");
            mUdpController.start();
            mUdpController.sendThreadStart();
        }


        @JavascriptInterface
        public String test(final String data) {
            Log.i("godlee", "senddata" + data);
//            mTcpController.sendData(S);

            try {
                JSONObject sJson = new JSONObject(data);
            } catch (JSONException e) {
                Log.e("godlee", e.getMessage());
            }
            return data;
        }

        @JavascriptInterface
        public void sendUdp() {
            byte[] data = new byte[]{(byte) 0xff, 0x00, 0x01,
                    0x01, 0x02};
            mUdpController.sendMsg(data, 48899);
        }

        @JavascriptInterface
        public void greenOn() {
//            mTcpController.init("192.168.1.1", 8899);
            byte[] data = new byte[]{(byte) 0xAA, (byte) 0x08, (byte) 0x0A, (byte) 0x01, (byte) 0x07, (byte) 0x64, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x76};
            mUdpController.putMsg(data);
        }

        @JavascriptInterface
        public void greenOff() {

            byte[] data = new byte[]{(byte) 0xAA, (byte) 0x08, (byte) 0x0A, (byte) 0x01, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x12};
            mUdpController.putMsg(data);
        }

        @JavascriptInterface
        public void sendCode(final String data) {
            JSONObject sJson;
            int color, time, level;
            byte[] code;
            try {
                sJson = new JSONObject(data);
                color = Integer.parseInt(sJson.getString("color"));
                time = Integer.parseInt(sJson.getString("time"));
                level = Integer.parseInt(sJson.getString("level"));
                if (level > 100 || level < 0) {
                    code = mLightController.unset(color, time);
                } else {
                    code = mLightController.set(color, time, level);
                }

//                mTcpController.sendData(code);
                mUdpController.putMsg(code);
//                Log.i("godlee", Tool.bytesToHexString(code));
//                mLightController.displayTemp();
//                byte[] points=mLightController.getControlMap();
//                Log.i("godlee",Tool.bytesToHexString(points));
//                mLightController.setControlMap(points);
//                Log.i("godlee","formated");
                mLightController.displayTemp();

                Log.i("godlee", mLightController.getJsonControlMap());

            } catch (JSONException e) {
                Log.e("godlee", e.getMessage());
            }


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
