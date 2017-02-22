package com.gooduo.wifitest;

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
    public boolean isHead(){
        for(int i=0;i<data.length;i++){
            if(0==i&&(byte)0xaa!=data[i])return false;
            if(1==i&&(byte)0x08!=data[i])return false;
            if(2==i&&((byte)0x0b!=data[i]||(byte)0x0a!=data[i]))return false;
            if(i>2)return true;
        }
        return false;

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
    public byte[] merge(byte[] otherData){
        byte[] newData=new byte[this.data.length+otherData.length];
        System.arraycopy(data,0,newData,0,data.length);
        System.arraycopy(otherData,0,newData,data.length,otherData.length);
        this.data=newData;
        return newData;
    }
    public byte[] merge(DataPack otherPack){
        if(ip.equals(otherPack.getIp())&&port==otherPack.getPort()){
            merge(otherPack.getData());
        }
        return data;
    }
}
