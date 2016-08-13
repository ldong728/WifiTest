package com.gooduo.wifitest;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2016/6/21.
 */
public class WifiClass {
    public static String ssid="";
    private boolean stu;
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private Handler mHandler;
    private List<ScanResult> mWifiList;
    private List<WifiConfiguration> mWifiConfigurations;
    private WifiManager.WifiLock mWifiLock;

    public WifiClass(Activity mActivity,Handler mHandler) {
        this.mHandler=mHandler;
        mWifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    public void switchWifi(boolean stu) {
        if (stu) {
            if (!mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(stu);
            }
        } else {
            if (mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(stu);
            }
        }
    }

    public int checkState() {
        return mWifiManager.getWifiState();
    }

    /**
     * 锁定wifiLock
     */
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    /**
     * 解锁wifiLock
     */
    public void releaseWifiLock() {
        //判断是否锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    /**
     * 创建一个wifiLock
     */
    public void createWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("test");
    }

    /**
     * 得到配置好的网络
     *
     * @return
     */
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfigurations;
    }

    /**
     * 指定配置好的网络进行连接
     *
     * @param index
     */
    public void connetionConfiguration(int index) {
        if (index > mWifiConfigurations.size()) {
            return;
        }
        //连接配置好指定ID的网络
        mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId, true);
    }

    public void connectBySSID(String SSID) {
        disConnectionWifi(getNetWorkId());
        WifiConfiguration config = isExsits(SSID);
        final int id;
        boolean inlist=false;
        if (null != config) {
            mWifiManager.removeNetwork(config.networkId);
//            Log.i("godlee","removeNetwork");
//            id=config.networkId;
        }
            config = new WifiConfiguration();
            config.allowedAuthAlgorithms.clear();
            config.allowedGroupCiphers.clear();
            config.allowedKeyManagement.clear();
            config.allowedPairwiseCiphers.clear();
            config.allowedProtocols.clear();
            config.SSID = "\"" + SSID + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.priority = 999999;
            Log.i("godlee", "wifiConfiguration ok");
            id = mWifiManager.addNetwork(config);
            Log.i("godlee", "not in list" + id);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean stu = mWifiManager.enableNetwork(id, true);
                    mWifiManager.saveConfiguration();
                    mWifiManager.reconnect();
                    setStu(stu);
                    Log.i("godlee", "linked" + stu);
                }
            }).start();

    }

    private void setStu(boolean stu) {
        this.stu = stu;
    }

    public void startScan() {
        switchWifi(true);
        boolean stu = mWifiManager.startScan();

        //得到扫描结果
        mWifiList = mWifiManager.getScanResults();
        //得到配置好的网络连接
        mWifiConfigurations = mWifiManager.getConfiguredNetworks();
        Log.i("godlee", "scan:" + stu);
    }

    //得到网络列表
    public List<ScanResult> getWifiList() {
        return mWifiList;
    }

    //查看扫描结果
    public StringBuffer lookUpScan() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mWifiList.size(); i++) {
            sb.append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包;
            // 其中把包括：BSSID、SSID、capabilities、frequency、level;
            sb.append((mWifiList.get(i)).toString()).append("\n");
        }
        return sb;
    }

    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    public int getIpAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    //得到连接的ID
    public int getNetWorkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    //得到wifiInfo的所有信息
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    //添加一个网络并连接
    public void addNetWork(WifiConfiguration configuration) {
        int wcgId = mWifiManager.addNetwork(configuration);
        boolean stu = mWifiManager.enableNetwork(wcgId, true);
        Log.i("godlee", "linked OK");
    }

    //断开指定ID的网络
    public void disConnectionWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    public String getResultJson() {
        JSONObject obj = new JSONObject();
        int i = 0;
        for (ScanResult r : mWifiList) {
            try {
                obj.accumulate("" + i, r.SSID);
                i++;
            } catch (JSONException e) {
                Log.e("godlee", e.getMessage());
            }
        }
        Log.i("godlee", "getting result");
        return obj.toString();
    }

    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }
}


