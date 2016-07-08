package com.gooduo.wifitest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/8.
 */
public class LightGroup {
    public UdpController mUdpController;
    private HashMap<byte[],String> mIPMap;
    public LightsController mLightsController;

    public LightGroup(UdpController udpController){

    }

}
