package com.gooduo.wifitest;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/7/4.
 */
class ControllerPoint implements Serializable {
    private int mIndex;
    private boolean mKey=false;
    private int mLevel=0;
    public ControllerPoint(int index,boolean key,int level){
        this.mIndex=index;
        this.mKey=key;
        this.mLevel=level;
    }
    public ControllerPoint (int index,boolean key){
        this(index,key,0x00);
    }
    public ControllerPoint(int index){
        this(index,false,0x00);
    }
    public boolean isKey(){
        return mKey;
    }
    public void setKey(boolean key){
        this.mKey=key;
    }
    public void setLevel(int level){

        this.mLevel=level>-1?level:0;
    }
    public int getLevel(){
        return mLevel;
    }
    public byte[] getCode(int color){
        int h=mIndex/2;
        int hh=mIndex%2;
        byte[] data = new byte[Light.CODE_LENGTH];
        data[0]=(byte)0xaa;
        data[1]=(byte)0x08;
        data[2]=(byte)0x0a;
        data[3]=(byte)0x03;
        data[4]=(byte)color;
        data[5]=(byte)mLevel;
        data[6]=(byte)h;
        data[7]=(byte)hh;
        data[8]=(byte)0x00;
        data[9]=(byte)0x00;
        data[10]=(byte)0x00;
        data[11]=(byte)(color+mLevel+h+hh+0x0a+0x03);
        return data;

    }
}