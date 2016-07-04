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
        this.mLevel=level;
    }
    public int getLevel(){
        return mLevel;
    }
}