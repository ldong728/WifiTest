package com.gooduo.wifitest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
//import java.security.MessageDigest;

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
    private Light[] mLightsList =new Light[COLOR_NUM];
    private byte[] mCloudCode,mFlashCode,mMoonCode;
    private byte[] mManualCode;



    public LightsController(){
        mManualCode=new byte[COLOR_NUM];
        for(int i=0;i<COLOR_NUM;i++){
            mLightsList[i]=new Light(i+1);
            mManualCode[i]=mLightsList[i].getManuelLevel();
        }
        mCloudCode=new byte[]{(byte)0xAA, 0x08, 0x0A, 0x04, 0x00, 0x00,0x00, 0x00, 0x00, 0x00, 0x00, 0x0E};
        mFlashCode=new byte[]{(byte)0xAA, 0x08, 0x0A, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0F};
        mMoonCode=new byte[]{(byte)0xAA, 0x08, 0x0A, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10};
    }
    public byte[] set(int color,int time,int level){
      byte[] temp= mLightsList[color].setPoint(time, level);
        D.i("set shortCode: "+Tool.bytesToHexString(temp));
//        return mLightsList[color].getAutoCode();
        return temp;
    }
    public byte[] unset(int color,int time){
       byte[] temp= mLightsList[color].removeKey(time);
        D.i("unset shortCode: "+Tool.bytesToHexString(temp));
        return temp;
//        return  mLightsList[color].getAutoCode();
    }
    public byte[] setManual(int color,int level){
        byte[] code= mLightsList[color].setManuelCode(level);
        mManualCode[color]=(byte)level;
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
        mCloudCode=data;
        return data;
    }
    public byte[] setFlash(int stu, int level,int probability){
        int sLevel=level,sProbability=probability;
        byte[] data=new byte[12];
        if(0==stu){
            sLevel=0;
            sProbability=0;
        }
        data[0]=(byte)0xaa;
        data[1]=(byte)0x08;
        data[2]=(byte)0x0a;
        data[3]=(byte)0x05;
        data[4]=(byte)stu;
        data[5]=(byte)sProbability;
        data[6]=(byte)sLevel;
        data[7]=(byte)0x00;
        data[8]=(byte)0x00;
        data[9]=(byte)0x00;
        data[10]=(byte)0x00;
        data[11]=(byte)(0x05+0x0a+stu+sProbability+sLevel);
        mFlashCode=data;
        return data;

    }
    public byte[] setMoon(boolean stu,int startH,int startM,int endH,int endM){
        int sStartH=startH,sStartM=startM,sEndH=endH,sEndM=endM;
        int sStu=0x01;
        byte[] data=new byte[12];
        if(!stu){
            sStu=0x00;
            sStartH=0;
            sStartM=0;
            sEndH=0;
            sEndM=0;
        }
        data[0]=(byte)0xaa;
        data[1]=(byte)0x08;
        data[2]=(byte)0x0a;
        data[3]=(byte)0x06;
        data[4]=(byte)sStu;
        data[5]=(byte)0x00;
        data[6]=(byte)sStartH;
        data[7]=(byte)sStartM;
        data[8]=(byte)sEndH;
        data[9]=(byte)sEndM;
        data[10]=(byte)0x00;
        data[11]=(byte)(0x06+0x0a+sStu+sStartH+sStartM+sEndH+sEndM);
        mMoonCode=data;
        return data;
    }
    public byte[] saveCode(){
        byte[] data=new byte[]{(byte)0xaa,
            (byte)0x08,
            (byte)0x0a,
            (byte)0x07,
            (byte)0xa5,
            (byte)0x00,
            (byte)0x00,
            (byte)0x00,
            (byte)0x00,
            (byte)0x00,
            (byte)0x00,
            (byte)0xb6};
        return data;
    }
    public byte[] runCode(){
        byte[] data=new byte[]{(byte)0xaa,
                (byte)0x08,
                (byte)0x0a,
                (byte)0x08,
                (byte)0x01,
                (byte)0x00,
                (byte)0x00,
                (byte)0x00,
                (byte)0x00,
                (byte)0x00,
                (byte)0x00,
                (byte)0x13};
        return data;
    }

    /**
     * 将自动模式的状态序列化成字节码便于储存（非控制字节码）（已过时）
     * @return 转换后的字节码
     */
    public byte[] getAutoMap(){
        ArrayList<ControllerPoint> sList= new ArrayList<ControllerPoint>(COLOR_NUM*Light.TOTAL);
        int offset=0;
        for(int i=0; i<COLOR_NUM; i++){
            ControllerPoint[] src = mLightsList[i].getControlMap();
            for(ControllerPoint c : src){
                sList.add(c);
            }
        }
        D.i(""+sList.get(245).getLevel());
        ByteArrayOutputStream obj=new ByteArrayOutputStream();
        try{
            ObjectOutputStream out=new ObjectOutputStream(obj);
            out.writeObject(sList);
            return obj.toByteArray();
//            return new byte[]{(byte)0x12,0x45};
        }catch(IOException e){
            D.e(e.getMessage());
            return null;
        }
    }
//    public byte[] getManualMap(){
//        return mManualCode;
//    }

    /**
     * 将自动化控制的状态字节码转化成控制状态
     * @param data 待转化的字节码
     */
    public void setAutoMap(byte[] data){
            ByteArrayInputStream obj= new ByteArrayInputStream(data);
            try{
                ObjectInputStream in =new ObjectInputStream(obj);
                ArrayList<ControllerPoint> sCp=(ArrayList<ControllerPoint>)in.readObject();
                for(int i=0;i<COLOR_NUM;i++){
                    for(int j=0; j<Light.TOTAL;j++){
                        ControllerPoint p=sCp.get(i*Light.TOTAL+j);
                        mLightsList[i].setControlMap(j, p.isKey(), p.getLevel());
                    }
                }
            }catch(IOException e){
                D.e(e.getMessage());
            }catch(ClassNotFoundException e){
                D.e( e.getMessage());
            }
    }
    public void setAutoMap(JSONObject data){
        Iterator<String> colorIt=data.keys();
        try{
            while(colorIt.hasNext()){
                String key=colorIt.next();
                int color = Integer.parseInt(key);
                JSONObject sub=data.getJSONObject(key);
                Iterator<String> subIt=sub.keys();
                while(subIt.hasNext()){
                    String timeString= subIt.next();
                    int time=Integer.parseInt(timeString);
                    int level=sub.getInt(timeString);
                    set(color,time,level);
                }

            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
    public void initAutoMap(){
        for(int i=0;i<COLOR_NUM;i++){
            for(int j=0; j<Light.TOTAL;j++){
                mLightsList[i].setControlMap(j,false, 0);
            }
        }
    }
    public void setManualMap(byte[] data){
        mManualCode=data;
        for(int i=0;i<data.length;i++){
            mLightsList[i].setManuelCode(data[i]&0xff);
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
                ControllerPoint[] maps= mLightsList[i].getControlMap();
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
            D.e( e.getMessage());
            return null;
        }
    }



    /**
     * 获取手动模式状态json格式字符串
     * @return
     */
    public String getJsonManual(){
        JSONObject obj=new JSONObject();
        try{
            for(int i=0;i<COLOR_NUM;i++){
                obj.accumulate(""+i,""+mLightsList[i].getManuelLevel());
            }
            return obj.toString();
        }catch(JSONException e){
            D.e(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取云遮挡模式状态Json格式字符串
     * @return json:{"stu":状态开关，"prob":概率，"mask":遮挡度}
     */
    public String getJsonCloud(){
        JSONObject obj=new JSONObject();
        try{
            obj.accumulate("stu",(int)mCloudCode[4]);
            obj.accumulate("prob",(int)mCloudCode[5]);
            obj.accumulate("mask",(int)mCloudCode[6]);
            return obj.toString();
        }catch(JSONException e){
            D.e(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public String getJsonFlash(){
        JSONObject obj=new JSONObject();
        try{
            obj.accumulate("stu",(int)mFlashCode[4]);
            obj.accumulate("prob",(int)mFlashCode[5]);
            obj.accumulate("level",(int)mFlashCode[6]);
            return obj.toString();
        }catch(JSONException e){
            D.e(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public String getJsonMoon(){
        JSONObject obj=new JSONObject();
        try{
            obj.accumulate("stu",(int)mMoonCode[4]);
            obj.accumulate("startH",(int)mMoonCode[6]);
            obj.accumulate("startM",(int)mMoonCode[7]);
            obj.accumulate("endH",(int)mMoonCode[8]);
            obj.accumulate("endM",(int)mMoonCode[9]);
            return obj.toString();
        }catch(JSONException e){
            D.e(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public void setmCloudCode(byte[] mCloudCode) {
        this.mCloudCode = mCloudCode;
    }



    public void setmFlashCode(byte[] mFlashCode) {
        this.mFlashCode = mFlashCode;
    }

    public byte[] getmMoonCode() {
        return mMoonCode;
    }

    /**
     * 获取可保存入数据库的控制指令
     * @param codeType 指令类型，包括，手动，自动及各种情景模式
     * @return byte[] byte数组，可直接保存入数据库
     */
    public byte[] getControlCode(String codeType){
        switch(codeType){
            case Db.TYPE_AUTO:{
                ArrayList<ControllerPoint> sList= new ArrayList<ControllerPoint>(COLOR_NUM*Light.TOTAL);
                int offset=0;
                for(int i=0; i<COLOR_NUM; i++){
                    ControllerPoint[] src = mLightsList[i].getControlMap();
                    for(ControllerPoint c : src){
                        sList.add(c);
                    }
                }
//                D.i(""+sList.get(245).getLevel());
                ByteArrayOutputStream obj=new ByteArrayOutputStream();
                try{
                    ObjectOutputStream out=new ObjectOutputStream(obj);
                    out.writeObject(sList);
                    return obj.toByteArray();
//            return new byte[]{(byte)0x12,0x45};
                }catch(IOException e){
                    D.e(e.getMessage());
                    return null;
                }
            }
            case Db.TYPE_MANUAL:{
                byte[] data=new byte[COLOR_NUM];
                for(int i=0; i<COLOR_NUM;i++){
                    data[i]=mLightsList[i].getManuelLevel();
                }
                return data;
            }
            case Db.TYPE_CLOUD:{
                return mCloudCode;
            }
            case Db.TYPE_FLASH:{
                return mFlashCode;
            }
            case Db.TYPE_MOON:{
                return mMoonCode;
            }
        }
        return null;
    }




    public void setmMoonCode(byte[] mMoonCode) {
        this.mMoonCode = mMoonCode;
    }

    public byte[] setmManualCode(byte[] mManualCode) {
        ByteBuffer data=ByteBuffer.allocate(Light.CODE_LENGTH*COLOR_NUM);
        this.mManualCode = mManualCode;
        for(int i=0;i<mManualCode.length;i++){
            int level=(int)mManualCode[i];
            byte[] code=mLightsList[i].setManuelCode(level);
            data.put(code);
        }
        data.flip();
        return data.array();
    }
//    public byte[] getmManualCode(){
//        byte[] data=new byte[COLOR_NUM];
//        for(int i=0; i<COLOR_NUM;i++){
//            data[i]=mLightsList[i].getManuelLevel();
//        }
//        return data;
//    }

    public void displayTemp(){
        for(Light l: mLightsList){
            l.disply();
        }
    }


}
