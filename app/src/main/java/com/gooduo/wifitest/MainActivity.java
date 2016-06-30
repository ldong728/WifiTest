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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;


public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    private WebSettings mWebSetting;
    private SearchSSID mSearchSSID;
    private TcpController mTcpController;
    private UdpController mUdpController;
    private WifiManager mWifiManager;
    private LightsController mLightController= new LightsController();
    private Handler mHander=new Handler(){
        @Override
        public void handleMessage(Message msg){
         switch(msg.what){
             case Tool.REC_DATA:{
                byte[] data = (byte[]) msg.obj;
                 String sData=Tool.bytesToHexString(data);
//                 decodeData(data);
                  Log.i("godlee", "msg:" + sData);
//                 mWebView.loadUrl("javascript: headerTo('ap_list.html')");
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
        mTcpController =new TcpController(this,mHander);
        mSearchSSID = new SearchSSID(mHander);

//        mTcpController.start();
//        mUdpController =new UdpController("192.168.0.153",mHander);
        mWebView=new WebView(this);
        mWebSetting=mWebView.getSettings();
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
        mTcpController.close();
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
//            initSocket();
        }
        @JavascriptInterface
        public void startUdpServer(){
            Log.i("godlee","startServer");
            mSearchSSID.start();
            mSearchSSID.sendThreadStart();
        }


        @JavascriptInterface
        public String test(final String data){
            Log.i("godlee","senddata"+data);
//            mTcpController.sendData(S);

            try{
                JSONObject sJson=new JSONObject(data);
            }catch(JSONException e){
                Log.e("godlee",e.getMessage());
            }
            return data;
        }
        @JavascriptInterface
        public void sendUdp(){
            byte[] data= new byte[]{ (byte) 0xff, 0x00, 0x01,
                    0x01, 0x02 };
            mSearchSSID.putMsg(data);


        }
        @JavascriptInterface
          public void greenOn(){
//            mTcpController.init("192.168.1.1", 8899);
            byte[] data=new byte[]{(byte)0xAA,(byte)0x08,(byte)0x0A,(byte)0x01 ,(byte)0x07 ,(byte)0x64 ,(byte)0x00 ,(byte)0x00 ,(byte)0x00 ,(byte)0x00 ,(byte)0x00 ,(byte)0x76};
            mTcpController.sendData(data);
        }

        @JavascriptInterface
        public void greenOff(){

            byte[] data=new byte[]{(byte)0xAA,(byte)0x08,(byte)0x0A,(byte)0x01 ,(byte)0x07 ,(byte)0x00 ,(byte)0x00 ,(byte)0x00 ,(byte)0x00 ,(byte)0x00 ,(byte)0x00 ,(byte)0x12};
            mTcpController.sendData(data);
        }
        @JavascriptInterface
        public void sendCode(final String data){
            JSONObject sJson;
            int color,time,level;
            try{
                sJson=new JSONObject(data);
                color=Integer.parseInt(sJson.getString("color"));
                time=Integer.parseInt(sJson.getString("time"));
                level=Integer.parseInt(sJson.getString("level"));
//                Log.i
                byte[] code=mLightController.set(color,time,level);
                mTcpController.sendData(code);
                Log.i("godlee", Tool.bytesToHexString(code));

            }catch(JSONException e){
                Log.e("godlee",e.getMessage());
            }



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
            Log.i("godlee","socket init");
            mTcpController.init("192.168.1.1", 8899);
        }

    }
}
