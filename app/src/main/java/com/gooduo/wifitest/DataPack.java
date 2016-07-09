package com.gooduo.wifitest;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/7/9.
 */
public class DataPack {
    private String ip;
    private int port;
    private byte[] data;
    public DataPack(String ip,int port,byte[] data){
        this.ip=ip;
        this.port=port;
        this.data=data;
    }
    public byte[] getData() {
        return data;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
    public int getLength(){
        return data.length;
    }
}
