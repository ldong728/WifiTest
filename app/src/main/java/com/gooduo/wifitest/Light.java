package com.gooduo.wifitest;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2016/6/29.
 */
public class Light {
    public static final int TOTAL=48;
    public static final int CODE_LENGTH=12;
    public static final int MAX=0x64;
    public static final int MIN=0x00;
    private final int mColor;
    private final int mMaxLevel,mMinLevel;
    private int mLevel=0;
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
        this(color, MaxLevel, MIN);
    }
    public Light(int color){
        this(color, MAX, MIN);
    }
    public byte[] setPoint(int index,int level){
        if(index>-1&&index<TOTAL&&level<MAX+1&&level>MIN-1){
            double v=(double)level/(double)100*(double)mMaxLevel;
            return setLevel(index,(int)v);
        }
        return null;

    }
    public ControllerPoint[] getControlMap(){
        return mControlMap;
    }
    public byte[] getAutoCode(){
        byte[] data=new byte[CODE_LENGTH*TOTAL];
        for(int i=0;i<TOTAL;i++){
            int h=i/2;
            int hh=i%2;
            data[CODE_LENGTH*i]=(byte)0xaa;
            data[CODE_LENGTH*i+1]=(byte)0x08;
            data[CODE_LENGTH*i+2]=(byte)0x0a;
            data[CODE_LENGTH*i+3]=(byte)0x03;
            data[CODE_LENGTH*i+4]=(byte)mColor;
            data[CODE_LENGTH*i+5]=(byte)mControlMap[i].getLevel();
            data[CODE_LENGTH*i+6]=(byte)h;
            data[CODE_LENGTH*i+7]=(byte)hh;
            data[CODE_LENGTH*i+8]=(byte)0x00;
            data[CODE_LENGTH*i+9]=(byte)0x00;
            data[CODE_LENGTH*i+10]=(byte)0x00;
            data[CODE_LENGTH*i+11]=(byte)(mColor+mControlMap[i].getLevel()+h+hh+0x0a+0x03);
        }
        return data;
    }
    public byte[] setManuelCode(int level){
        byte[] data=new byte[CODE_LENGTH];
        data[0]=(byte)0xaa;
        data[1]=(byte)0x08;
        data[2]=(byte)0x0a;
        data[3]=(byte)0x01;
        data[4]=(byte)mColor;
        data[5]=(byte)level;
        data[6]=(byte)0x00;
        data[7]=(byte)0x00;
        data[8]=(byte)0x00;
        data[9]=(byte)0x00;
        data[10]=(byte)0x00;
        data[11]=(byte)(0x01+0x0a+mColor+level);
        mLevel=level;
        return data;
    }
    public byte getManuelLevel(){
        return (byte)mLevel;
    }
    public void setControlMap(int index,boolean key,int level){
        mControlMap[index].setKey(key);
        mControlMap[index].setLevel(level);
    }
    private void initControlMap(){
        for(int i=0;i<TOTAL;i++){
            mControlMap[i]=new ControllerPoint(i);
        }
    }
    private byte[] setLevel(int index,int level){
        int length=0;
        ByteBuffer buffer=ByteBuffer.allocate(CODE_LENGTH*TOTAL);
        int leftKey,rightKey;
        int leftCount=0,rightCount=0;
//        int leftE=0,rightE=0;
//        int leftOffsetCount=0,rightOffsetCount=0;
//        int offset=0;
        mControlMap[index].setKey(true);
        mControlMap[index].setLevel(level);
        buffer.put(mControlMap[index].getCode(mColor));
        length+=CODE_LENGTH;
        if(index>0){
            for(leftKey=index-1;leftKey>0;leftKey--){
                if(mControlMap[leftKey].isKey())break;
                leftCount++;
            }
        }else{
            leftKey=0;
        }
        if(index<TOTAL-1){
            for(rightKey=index+1;rightKey<TOTAL-1;rightKey++){
                if(mControlMap[rightKey].isKey())break;
                rightCount++;
            }
        }else{
            rightKey=TOTAL-1;
        }
        if(leftCount>0){
                double levelDiffL=level-mControlMap[leftKey].getLevel();
                for(int i=1;i<leftCount+1;i++){
                    mControlMap[leftKey+i].setLevel(mControlMap[leftKey].getLevel()+(int)(i*levelDiffL/(double)leftCount));
                    buffer.put(mControlMap[leftKey+i].getCode(mColor));
                    length+=CODE_LENGTH;
                }
//            leftE=(level-mControlMap[leftKey].getLevel())/(leftCount+1);  //获取每一点的平均亮度差
//            leftOffsetCount=(level-mControlMap[leftKey].getLevel())%(leftCount+1);//获取求亮度差后的余数
//            for(int i=0;i<leftCount;i++){
//                offset=i<Math.abs(leftOffsetCount)-1? Math.abs(leftOffsetCount)/leftOffsetCount:0;//将余数放入每一点
//                mControlMap[index-i-1].setLevel(mControlMap[index-i].getLevel()-leftE-Math.abs(offset));
//                buffer.put(mControlMap[index - i - 1].getCode(mColor));
//                length+=CODE_LENGTH;
//            }
        }
        if(rightCount>0){
            double levelDiffR=mControlMap[rightKey].getLevel()-level;
            for(int j=1;j<rightCount+1;j++){
                mControlMap[index+j].setLevel(mControlMap[index].getLevel()+(int)(j*levelDiffR/(double)rightCount));
                buffer.put(mControlMap[index+j].getCode(mColor));
                length+=CODE_LENGTH;
            }
//            rightE=(level-mControlMap[rightKey].getLevel())/(rightCount+1);
//            rightOffsetCount=(level-mControlMap[rightKey].getLevel())%(rightCount+1);
//            for(int j=0;j<rightCount;j++){
//                offset=j<Math.abs(rightOffsetCount)-1? Math.abs(rightOffsetCount)/rightOffsetCount:0;
//                mControlMap[index+j+1].setLevel(mControlMap[index+j].getLevel()-rightE-Math.abs(offset));
//                buffer.put( mControlMap[index+j+1].getCode(mColor));
//                length+=CODE_LENGTH;
//            }
        }
        if(buffer.hasArray()){
            byte[] data=new byte[length];
            buffer.flip();
            buffer.get(data);
            return data;
        }else{
            return null;
        }

    }
    public void disply(){
        String inf="";
        for(int i=0;i<TOTAL;i++){
            inf+=mControlMap[i].getLevel()+",";
        }
        Log.i("godlee", inf);
    }
    public byte[] removeKey(int index){
        if(mControlMap[index].isKey()){
            mControlMap[index].setKey(false);
        }
        for(int i=index;i>-1;i--){
            if(mControlMap[i].isKey()){
                return setLevel(i,mControlMap[i].getLevel());
            }else if(i==0){
//                D.i("追朔到源头");
                byte[] data=setLevel(i,0);
                mControlMap[i].setKey(false);
                return data;
            }
        }
        return null;

    }
}
