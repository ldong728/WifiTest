package com.gooduo.wifitest;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2016/6/21.
 */
public class WifiClass {
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private Activity mActivity;
    private List<ScanResult> mWifiList;
    private List<WifiConfiguration> mWifiConfigurations;
    private WifiManager.WifiLock mWifiLock;

    public WifiClass(Activity mActivity){
        this.mActivity=mActivity;
        mWifiManager=(WifiManager)mActivity.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo=mWifiManager.getConnectionInfo();
    }
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

    public int checkState() {
        return mWifiManager.getWifiState();
    }

    /**锁定wifiLock
     *
     */
    public void acquireWifiLock(){
        mWifiLock.acquire();
    }

    /**解锁wifiLock
     *
     */
    public void releaseWifiLock(){
        //判断是否锁定
        if(mWifiLock.isHeld()){
            mWifiLock.acquire();
        }
    }

    /**创建一个wifiLock
     *
     */
    public void createWifiLock(){
        mWifiLock=mWifiManager.createWifiLock("test");
    }

    /**得到配置好的网络
     *
     * @return
     */
    public List<WifiConfiguration> getConfiguration(){
        return mWifiConfigurations;
    }

    /**指定配置好的网络进行连接
     *
     * @param index
     */
    public void connetionConfiguration(int index){
        if(index>mWifiConfigurations.size()){
            return ;
        }
        //连接配置好指定ID的网络
        mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId, true);
    }
    public void startScan(){
        boolean stu=mWifiManager.startScan();
        Log.i("godlee","scan:"+stu);
        //得到扫描结果
        mWifiList=mWifiManager.getScanResults();
        //得到配置好的网络连接
        mWifiConfigurations=mWifiManager.getConfiguredNetworks();
    }
    //得到网络列表
    public List<ScanResult> getWifiList(){
        return mWifiList;
    }
    //查看扫描结果
    public StringBuffer lookUpScan(){
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<mWifiList.size();i++){
            sb.append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            sb.append((mWifiList.get(i)).toString()).append("\n");
        }
        return sb;
    }
    public String getMacAddress(){
        return (mWifiInfo==null)?"NULL":mWifiInfo.getMacAddress();
    }
    public String getBSSID(){
        return (mWifiInfo==null)?"NULL":mWifiInfo.getBSSID();
    }
    public int getIpAddress(){
        return (mWifiInfo==null)?0:mWifiInfo.getIpAddress();
    }
    //得到连接的ID
    public int getNetWordId(){
        return (mWifiInfo==null)?0:mWifiInfo.getNetworkId();
    }
    //得到wifiInfo的所有信息
    public String getWifiInfo(){
        return (mWifiInfo==null)?"NULL":mWifiInfo.toString();
    }
    //添加一个网络并连接
    public void addNetWork(WifiConfiguration configuration){
        int wcgId=mWifiManager.addNetwork(configuration);
        mWifiManager.enableNetwork(wcgId, true);
    }
    //断开指定ID的网络
    public void disConnectionWifi(int netId){
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }
    public String getResultJson(){
        JSONObject obj=new JSONObject();
        int i=0;
        for(ScanResult r :mWifiList){
            try{
                obj.accumulate(""+i,r.SSID);
                i++;
            }catch(JSONException e){
                Log.e("godlee", e.getMessage());
            }
        }
        Log.i("godlee","getting result");
        return obj.toString();
    }
}


