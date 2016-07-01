package com.gooduo.wifitest;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.webkit.JavascriptInterface;

/**
 * Created by Administrator on 2016/6/21.
 */
public class WifiClass {
    private WifiManager mWifiManager;
    private Activity mActivity;

    public WifiClass(Activity mActivity){
        this.mActivity=mActivity;
        mWifiManager=(WifiManager)mActivity.getSystemService(Context.WIFI_SERVICE);
    }
    @JavascriptInterface
    public void switchWifi(boolean stu){
        if(stu){
            if(!mWifiManager.isWifiEnabled()){
                mWifiManager.setWifiEnabled(stu);
            }
        }else{
            if(mWifiManager.isWifiEnabled()){
                mWifiManager.setWifiEnabled(stu);
            }
        }
    }

    public boolean scanWifi(){

        return false;
    }

}
