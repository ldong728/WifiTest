package com.gooduo.wifitest;

import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/8.
 * 此类通过内置UDP控制器对群组灯光进行控制
 */
public class LightControllerGroup {
    private boolean sendOk=true;
    private boolean threadFlag = true;
    public UdpController mUdpController;
    private final HashMap<String, String> mIPMap;//组内灯具表（心跳包数据->IP地址）
    private final HashMap<String, ArrayList<byte[]>> mSendBuffer;//待发指令缓存（IP地址->指令列表）
    private HashMap<String,DataPack> buffer;//接收数据包的缓存映射，用于补全断帧
    public LightsController mLightsController;
    public LightControllerGroup(Handler handler) {
        mIPMap = new HashMap<String, String>(16);
        mSendBuffer = new HashMap<String, ArrayList<byte[]>>(16);
        buffer=new HashMap<String,DataPack>();
        mLightsController = new LightsController();
        mUdpController=new UdpController(handler,21195) {
            @Override
            public void onReceive(Handler handler, DataPack pack) {
                DataPack fullPack=formatReceive(pack);
                if(null!=fullPack){
                    reGroupSendQueue(fullPack);
                }
            }
        };
        mUdpController.start();
        mUdpController.sendThreadStart();
        new Thread(SendQueue).start();
    }
    public synchronized void initGroup(){
        mIPMap.clear();
        mSendBuffer.clear();
        buffer.clear();
    }
    public void setAutoSituation(byte[] stu){
        mLightsController.setControlMap(stu);
    }
    public void autoController(int color, int time, int level, boolean send) {
        byte[] code;
        if (level > 100 || level < 0) {
            code = mLightsController.unset(color, time);
        } else {
            code = mLightsController.set(color, time, level);
        }
        if (send) {
            putCodeToQueue(code);
        }

    }
    public void manualController(int color,int level){
        byte[] data=mLightsController.setManual(color,level);
        putCodeToQueue(data);
    }
    public String getAutoStu(){
        return mLightsController.getJsonControlMap();
    }
    public String getManualStu(){
        return mLightsController.getJsonManual();
    }
    public void setAutoStu(byte[] data){
        mLightsController.setControlMap(data);
    }
    public void addGroupMember(byte[] member) {
        mIPMap.put(Tool.bytesToHexString(member), "0.0.0.0");
    }
    public void addGroupMember(String mac){
        mIPMap.put(mac,"0,0,0,0");
    }
    public synchronized void removeGroupMember(String mac){
        String ip=mIPMap.remove(mac);
        if(null!=ip){
            buffer.remove(ip);
            mSendBuffer.remove(ip);
        }

    }
    public String getIp(byte[] data) {
        if(0xaa!=(data[0]&0xff)&&data.length>Light.CODE_LENGTH){
            String fullInf=new String(data);
            String[] inf=fullInf.split(",");
            if(inf.length>1){
                String key=inf[1];
                if (mIPMap.containsKey(key)) {
                    if(!mIPMap.get(key).equals(inf[0])){
                        Log.i("godlee","not same");
                        Log.i("godlee",mIPMap.get(key));
                        Log.i("godlee",inf[0]);
                        synchronized (mIPMap) {
                            mIPMap.put(key, inf[0]);
                            return key;
                        }
                    }
                }
                return "other";
            }

        }
        return null;

    }

    /**
     * 获取灯组Json,格式为{Mac:ip,Mac:ip}
     * @return
     */
    public String getLightsList() {
        JSONObject obj=new JSONObject();
        Iterator it = mIPMap.entrySet().iterator();
        try{
            while (it.hasNext()) {
                Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
                obj.accumulate(e.getKey(),e.getValue());
            }
            return obj.toString();
        }catch(JSONException e){
            Log.e("godlee",e.getMessage());
            e.printStackTrace();
        }
        return "{}";

    }
    public void finishAll(){
        if(null!=mUdpController)mUdpController.close();
        threadFlag=false;
    }

    /**
     * 将指令放入待发送队列
     * @param code
     */
    private void putCodeToQueue(byte[] code) {
        sendOk=false;
        ArrayList<byte[]> list = new ArrayList<byte[]>(48);
        for (int offset = 0; offset < code.length; offset += Light.CODE_LENGTH) {
            byte[] subCode = new byte[Light.CODE_LENGTH];
            System.arraycopy(code, offset, subCode, 0, Light.CODE_LENGTH);
            list.add(subCode);
        }
        synchronized (mSendBuffer) {
            Iterator it = mIPMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
                if(!e.getValue().equals("0,0,0,0")){
                    ArrayList<byte[]> oldList = mSendBuffer.get(e.getValue());
                    if (oldList != null) {
                        synchronized (oldList) {
                            oldList.addAll(list);
                        }
                        mSendBuffer.put(e.getValue(), oldList);
                    } else {
                        mSendBuffer.put(e.getValue(), new ArrayList<byte[]>(list));
                    }
                }
            }
        }
    }
    private DataPack formatReceive( DataPack revPacket){
        if(revPacket.getLength()%Light.CODE_LENGTH!=0){
            DataPack sBuff=buffer.get(revPacket.getIp());
            if(sBuff!=null){
                sBuff.merge(revPacket);
                if(0==sBuff.getLength()%Light.CODE_LENGTH){
                    revPacket=sBuff;
                    buffer.remove(sBuff.getIp());
                }else{
                    buffer.put(sBuff.getIp(),sBuff);
                    return null;
                }
            }else{
                buffer.put(revPacket.getIp(),revPacket);
                return null;
            }
        }
//        String fromIp=revPacket.getIp();
//        int fromPort=revPacket.getPort();
//        byte[] realData =revPacket.getData();
//        Log.i("godlee", "from:" + fromIp + ":" + fromPort + ".length(byts):"+revPacket.getLength()+"  "+ Tool.bytesToHexString(realData));
        return revPacket;

    }
    private void reGroupSendQueue(DataPack pack){
        String ip=pack.getIp();
        ArrayList<byte[]> list = mSendBuffer.get(ip);
        if(null!=list) {
            byte[] packData = new byte[Light.CODE_LENGTH];
            for (int offset = 0; offset < pack.getLength(); offset += Light.CODE_LENGTH) {
                if (0x0a == (packData[2] & 0xff)) continue;
//                Log.i("godlee", "reGroupWork");
                System.arraycopy(pack.getData(), offset, packData, 0, Light.CODE_LENGTH);
                packData[2] = (byte) 0x0a;
                for (byte[] data : list) {
                    if (Arrays.equals(data, packData)) {
                        synchronized (list) {
                            Log.i("godlee", "codeConfirmed");
                            list.remove(list.indexOf(data));
                        }
                        break;
                    }
                }
            }
        }
    }
    private Runnable SendQueue = new Runnable() {
        @Override
        public void run() {
            while (threadFlag) {
                Iterator it = mSendBuffer.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String,ArrayList<byte[]>> e=(Map.Entry<String,ArrayList<byte[]>>)it.next();
                    ArrayList<byte[]> list=e.getValue();
                    String ip=e.getKey();
                    if(null!=list&&(!ip.equals("0.0.0.0"))){
                        if(list.size()>0){
                            synchronized (list){
                                for(byte[] data:list){
//                                    Log.i("godlee","sendQueue: "+Tool.bytesToHexString(data));
                                    mUdpController.putMsg(data,ip);
                                }
                            }
                        }

                    }
                    

                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {

                }
            }
        }
    };

}