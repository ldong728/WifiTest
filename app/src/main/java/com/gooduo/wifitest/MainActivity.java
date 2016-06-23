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
import android.text.format.Formatter;

import java.net.InetAddress;


public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    private WebSettings mWebSetting;
    private SearchSSID mSearchSSID;
    private TcpController mTcpController;
    private WifiManager mWifiManager;
    private Handler mHander=new Handler(){
        @Override
        public void handleMessage(Message msg){
         switch(msg.what){
             case Tool.REC_DATA:{
                byte[] data = (byte[]) msg.obj;
                 Tool.bytesToHexString(data);
                 decodeData(data);
                 break;
             }
             default:break;
         }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiManager= (WifiManager)getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo=mWifiManager.getDhcpInfo();
        byte[] ip=Tool.int2byte(dhcpInfo.serverAddress);
        Log.i("godlee",Tool.bytesToHexString(ip));
        Log.i("godlee",""+(int)(ip[0]&0xff));
//        Log.i("godlee"," "+ InetAddress.getHostAdress());
//        Log.i("godlee"," "+Formatter.formatIpAddress(dhcpInfo.serverAddress));
        mTcpController =new TcpController(this,mHander);

        mWebView=new WebView(this);
        mWebSetting=mWebView.getSettings();
        mWebSetting.setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new mWebViewClint());
        setContentView(mWebView);
        mWebView.loadUrl("file:///android_asset/temp.html");
        mWebView.addJavascriptInterface(new JsBrg(this), "wifi");
        Log.i("godlee","hello logcat");

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
        mSearchSSID.setReceive(false);
        mSearchSSID.close();
    }
    class mWebViewClint extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            view.loadUrl(url);

            return true;

        }
    }
    private void decodeData(byte[] data){
            Log.i("godlee", Tool.bytesToHexString(data));
    }


    /*
    jsBridge类
     */
    class JsBrg {
        private Activity mActivity;
        public JsBrg(Activity activity){
            this.mActivity=activity;
        }
        @JavascriptInterface
        public void searchSSID(){
            mTcpController.temp();
        }


        @JavascriptInterface
        public void sendData(byte[] data){
            Log.i("godlee","senddata"+Tool.bytesToHexString(data));
            mTcpController.sendData(data);
        }
        @JavascriptInterface
        public void sendData(){
            mTcpController.init("192.168.0.153",4900);
            byte[] data=new byte[]{(byte)0xAA,0x08,0x0A,0x01 ,0x07 ,0x64 ,0x00 ,0x00 ,0x00 ,0x00 ,0x00 ,0x76};
            mTcpController.sendData(data);
            Log.i("godlee", "send sucessful");
        }

        @JavascriptInterface
        public void startServer(){
            Log.i("godlee", "startServer");
            mTcpController.start();
        }

        @JavascriptInterface
        public void stopServer(){
            Log.i("godlee","stopServer");
            mTcpController.stopServer();
        }
        @JavascriptInterface
        public void initSocket(){
            mTcpController.init("192.168.0.153",4900);
        }

    }
}
