package com.gooduo.wifitest;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2016/6/30.
 */
public class LightsController {
    public static final int COLOR_NUM=7;
    public static final int RY=0;
    public static final int CW=1;
    public static final int DR=2;
    public static final int UV=3;
    public static final int V=4;
    public static final int B=5;
    public static final int G=6;
    Light[] mLightsGroup=new Light[COLOR_NUM];



    public LightsController(){
        for(int i=0;i<7;i++){
            mLightsGroup[i]=new Light(i+1);
        }
    }

//    public void setCode(byte[] data){
//        int singleLightLength=data.length/COLOR_NUM;
//
//        byte[] singleArray=new byte[singleLightLength];
//        for(int i=0; i<COLOR_NUM;i++){
//            System.arraycopy(data,i*singleLightLength,singleArray,0,singleLightLength);
//            for(int j=0;j<Light.TOTAL;j++){
//            }
//
//        }
//
//    }
//
//    public byte[] getCode(){
//        int sTotaLength=Light.CODE_LENGTH*Light.TOTAL*LightsController.COLOR_NUM;
//        byte[] sNew =new byte[sTotaLength];
//        int offset=0;
//        for(Light l:mLightsGroup){
//            byte[] sr=l.getAutoCode();
//            System.arraycopy(sr,0,sNew,offset,sr.length);
//            offset+=sr.length;
//        }
//        return sNew;
//    }
//    public byte[] getCode(int color){
//        return mLightsGroup[color].getAutoCode();
//    }
    public byte[] set(int color,int time,int level){
      mLightsGroup[color].setPoint(time, level);
        return mLightsGroup[color].getAutoCode();
    }

    public byte[] unset(int color,int time){
        mLightsGroup[color].removeKey(time);
        return  mLightsGroup[color].getAutoCode();
    }
    public byte[] setManual(int color,int level){
        byte[] code=mLightsGroup[color].setManuelCode(level);
        return code;
    }
    public byte[] setCloud(boolean stu,int probability,int mask){
        int sProbability=probability,sMask=mask;
        int sStu=0x01;
        byte[] data=new byte[12];
        if(!stu){
            sProbability=0;
            sMask=0;
            sStu=0x00;
        }
        data[0]=(byte)0xaa;
        data[1]=(byte)0x08;
        data[2]=(byte)0x0a;
        data[3]=(byte)0x04;
        data[4]=(byte)sStu;
        data[5]=(byte)sProbability;
        data[6]=(byte)sMask;
        data[7]=(byte)0x00;
        data[8]=(byte)0x00;
        data[9]=(byte)0x00;
        data[10]=(byte)0x00;
        data[11]=(byte)(0x04+0x0a+sStu+sProbability+sMask);
        return data;
    }
    public byte[] setFlash(int level,int probability){
        int sLevel=level,sProbability=probability;
        byte[] data=new byte[12];
        if(0==level){
            sLevel=0;
            sProbability=0;
        }
        data[0]=(byte)0xaa;
        data[1]=(byte)0x08;
        data[2]=(byte)0x0a;
        data[3]=(byte)0x05;
        data[4]=(byte)sProbability;
        data[5]=(byte)sLevel;
        data[6]=(byte)0x00;
        data[7]=(byte)0x00;
        data[8]=(byte)0x00;
        data[9]=(byte)0x00;
        data[10]=(byte)0x00;
        data[11]=(byte)(0x05+0x0a+sProbability+sLevel);
        return data;

    }
    public byte[] setMoon(boolean stu,int start,int end){
        int sStart=start,sEnd=end;
        int sStu=0x01;
        byte[] data=new byte[12];
        if(!stu){
            sStu=0x00;
            sStart=0;
            sEnd=0;
        }
        data[0]=(byte)0xaa;
        data[1]=(byte)0x08;
        data[2]=(byte)0x0a;
        data[3]=(byte)0x06;
        data[4]=(byte)sStu;
        data[5]=(byte)0x00;
        data[6]=(byte)sStart;
        data[7]=(byte)0x00;
        data[8]=(byte)sEnd;
        data[9]=(byte)0x00;
        data[10]=(byte)0x00;
        data[11]=(byte)(0x06+0x0a+sStu+sStart+sEnd);
        return data;
    }

    /**
     * 将自动模式的状态序列化成字节码便于储存（非控制字节码）
     * @return 转换后的字节码
     */
    public byte[] getControlMap(){
        ArrayList<ControllerPoint> sList= new ArrayList<ControllerPoint>(COLOR_NUM*Light.TOTAL);
        int offset=0;
        for(int i=0; i<COLOR_NUM; i++){
            ControllerPoint[] src = mLightsGroup[i].getControlMap();
            for(ControllerPoint c : src){
                sList.add(c);
            }
        }
        Log.i("godlee",""+sList.get(245).getLevel());
        ByteArrayOutputStream obj=new ByteArrayOutputStream();
        try{
            ObjectOutputStream out=new ObjectOutputStream(obj);
            out.writeObject(sList);
            return obj.toByteArray();
//            return new byte[]{(byte)0x12,0x45};
        }catch(IOException e){
            Log.e("godlee",e.getMessage());
            return null;
        }
    }

    /**
     * 将自动化控制的状态字节码转化成控制状态
     * @param data 待转化的字节码
     */
    public void setControlMap(byte[] data){
        ByteArrayInputStream obj= new ByteArrayInputStream(data);
        try{
            ObjectInputStream in =new ObjectInputStream(obj);
            ArrayList<ControllerPoint> sCp=(ArrayList<ControllerPoint>)in.readObject();
            for(int i=0;i<COLOR_NUM;i++){
                for(int j=0; j<Light.TOTAL;j++){
                    ControllerPoint p=sCp.get(i*Light.TOTAL+j);
                    mLightsGroup[i].setControlMap(j,p.isKey(),p.getLevel());
                }
            }
        }catch(IOException e){
            Log.e("godlee",e.getMessage());
        }catch(ClassNotFoundException e){
            Log.e("godlee",e.getMessage());
        }
    }

    /**
     * 获取自动控制状态的Json格式字符串
     * @return Json格式字符串
     */
    public String getJsonControlMap(){
        JSONObject obj=new JSONObject();
        try{
            for(int i=0;i<COLOR_NUM;i++){
                ControllerPoint[] maps=mLightsGroup[i].getControlMap();
                JSONObject sub=new JSONObject();
                for(int j=0;j<Light.TOTAL;j++){
                    if(maps[j].isKey()){
                        sub.accumulate(""+j,maps[j].getLevel());
                    }
                }
                obj.accumulate(""+i,sub);
            }
            return obj.toString();
        }catch(JSONException e){
            Log.e("godlee",e.getMessage());
            return null;
        }


    }

    public void displayTemp(){
        for(Light l:mLightsGroup){
            l.disply();
        }
    }


}
