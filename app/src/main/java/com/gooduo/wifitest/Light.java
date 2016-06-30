package com.gooduo.wifitest;

import android.util.Log;

/**
 * Created by Administrator on 2016/6/29.
 */
public class Light {
    public static int TOTAL=48;
    public static int MAX=0x64;
    public static int MIN=0x00;
    private int mColor,mMaxLevel,mMinLevel;
    private ControllerPoint[] mControlMap =new ControllerPoint[TOTAL];


    public Light(int color,int maxLevel,int minLevel){
        this.mColor=color;
        if(maxLevel>MAX)maxLevel=MAX;
        if(minLevel<MIN)minLevel=MIN;
        this.mMaxLevel=maxLevel;
        this.mMinLevel=minLevel;
        initControlMap();
    }
    public Light(int color,int MaxLevel){
        this(color, MaxLevel,MIN);
    }
    public Light(int color){
        this(color, MAX, MIN);
    }
    public void setPoint(int index,int level){
        if(index>-1&&index<TOTAL&&level<MAX+1&&level>MIN-1){
            double v=(double)level/(double)100*(double)mMaxLevel;
            setLevel(index,(int)v);
        }

    }

    public byte[] getAutoCode(){
        byte[] data=new byte[12*TOTAL];
        for(int i=0;i<TOTAL;i++){
            int h=i/2;
            int hh=i%2;
            data[12*i]=(byte)0xaa;
            data[12*i+1]=(byte)0x08;
            data[12*i+2]=(byte)0x0a;
            data[12*i+3]=(byte)0x03;
            data[12*i+4]=(byte)mColor;
            data[12*i+5]=(byte)mControlMap[i].getLevel();
            data[12*i+6]=(byte)h;
            data[12*i+7]=(byte)hh;
            data[12*i+8]=(byte)0x00;
            data[12*i+9]=(byte)0x00;
            data[12*i+10]=(byte)0x00;
            data[12*i+11]=(byte)(mColor+mControlMap[i].getLevel()+h+hh+0x0a+0x03);
        }
        return data;
    }

    private void initControlMap(){
        for(int i=0;i<TOTAL;i++){
            mControlMap[i]=new ControllerPoint(i);
        }
    }
    private void setLevel(int index,int level){
//        Log.i("godlee","setLevel");
        int leftKey,rightKey;
        int leftCount=0,rightCount=0;
        int leftE=0,rightE=0;
        int leftOffsetCount=0,rightOffsetCount=0;
        int offset=0;
        mControlMap[index].setKey(true);
        mControlMap[index].setLevel(level);
        for(leftKey=index-1;leftKey>0;leftKey--){
            if(mControlMap[leftKey].isKey()==true)break;
            leftCount++;
        }
        for(rightKey=index+1;rightKey<TOTAL-1;rightKey++){
            if(mControlMap[rightKey].isKey()==true)break;
//            Log.i("godlee",""+rightKey);
            rightCount++;
        }
        if(leftCount>0){

            leftE=(level-mControlMap[leftKey].getLevel())/(leftCount+1);
            leftOffsetCount=(level-mControlMap[leftKey].getLevel())%(leftCount+1);
            for(int i=0;i<leftCount;i++){
                offset=i<leftOffsetCount-1? 1:0;
                mControlMap[index-i-1].setLevel(mControlMap[index-i].getLevel()-leftE-offset);
            }
//            Log.i("godlee","leftE="+leftE+"; leftCount="+leftCount);
        }
//        Log.i("godlee","rightKey="+rightKey+";   leftKey="+leftKey);
        if(rightCount>0){
            rightE=(level-mControlMap[rightKey].getLevel())/(rightCount+1);
            rightOffsetCount=(level-mControlMap[rightKey].getLevel())%(rightCount+1);
            for(int j=0;j<rightCount;j++){
                offset=j<rightOffsetCount-1? 1:0;
                mControlMap[index+j+1].setLevel(mControlMap[index+j].getLevel()-rightE-offset);
            }
//            Log.i("godlee","rightE="+rightE+";  rightCount="+rightCount);
        }
    }
    public void disply(){
        String inf="";
        for(int i=0;i<TOTAL;i++){
            inf+=mControlMap[i].getLevel()+",";
        }
        Log.i("godlee", inf);
    }






    class ControllerPoint {
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
}
