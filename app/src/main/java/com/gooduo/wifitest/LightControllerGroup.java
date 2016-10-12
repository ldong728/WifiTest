package com.gooduo.wifitest;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/8.
 * 此类通过内置UDP控制器对群组灯光进行控制
 */
public class LightControllerGroup {
    public static final int SEND_OK = 0xf0c0;
    public static final int SINGLE_SEND_OK=0xcdfe;
    public static final int RECEIVE_PORT = 21195;
    private boolean mLocal = true;
    private boolean mGroupOnLine = false;

    public boolean isSendOk() {
        return sendOk;
    }

    private boolean sendOk = true;
    private boolean threadFlag = true;
    private final Handler mHandle;
    public UdpController mUdpController;
    private final HashMap<String, String> mIPMap;//组内灯具表（Mac->IP）
    private final HashMap<String, ArrayList<byte[]>> mSendBuffer;//待发指令缓存（IP地址->指令列表）
    private HashMap<String, DataPack> buffer;//接收数据包的缓存映射，用于补全断帧
    public LightsController mLightsController;

    public LightControllerGroup(Handler handler) {
        mHandle = handler;
        mIPMap = new HashMap<String, String>(16);
        mSendBuffer = new HashMap<String, ArrayList<byte[]>>(16);
        buffer = new HashMap<String, DataPack>();
        mLightsController = new LightsController();
        mUdpController = new UdpController(handler, RECEIVE_PORT) {
            @Override
            public void onReceive(Handler handler, DataPack pack) {
                DataPack fullPack = formatReceive(pack);

                if (null != fullPack) {
                    if (Light.CODE_LENGTH == fullPack.getLength()) {

                    }
                    reGroupSendQueue(fullPack);
                }
            }
        };
        mUdpController.start();
        mUdpController.sendThreadStart();
        new Thread(SendQueue).start();
    }

    public synchronized String initGroup(Db mDb) {
        mIPMap.clear();
        mSendBuffer.clear();
        buffer.clear();
        JSONObject sObj = mDb.getGroupInf();
        JSONObject[] sDeviceObj = mDb.getDeviceList();
        if (null != sObj && sDeviceObj.length > 0) {
            mLocal = true;
            JSONObject returnData = new JSONObject();
            try {
                setAutoStu(mDb.getCode(Db.TYPE_AUTO));
                setManualStu(mDb.getCode(Db.TYPE_MANUAL));
                setCloudStu(mDb.getCode(Db.TYPE_CLOUD));
                setFlashStu(mDb.getCode(Db.TYPE_FLASH));
                setMoonStu(mDb.getCode(Db.TYPE_MOON));
                String sGroupType = sObj.getString(Db.G_TYPE);
//                Log.i("godlee","GroupType:"+sGroupType);
                int index = 0;
                for (JSONObject obj : sDeviceObj) {
                    String mac = obj.getString(Db.D_MAC);
//                    Log.i("godlee","deviceId:"+mac);
                    if (sGroupType.equals(Db.GROUP_TYPE_LOCAL)) {
                        addGroupMember(mac, UdpController.DEFALT_IP);
                    } else {
                        addGroupMember(mac);
                    }
                    returnData.accumulate("" + index, obj);
                    index++;
                }
                return returnData.toString();

            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        } else {
            mLocal = false;
            return "";
        }


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

    public void manualController(int color, int level) {
        byte[] data = mLightsController.setManual(color, level);
        mUdpController.sendMsg(data, UdpController.DEFALT_IP, UdpController.DATA_PORT);
        putCodeToQueue(data);
    }

    public void setCloud(boolean stu, int probability, int mask) {
        byte[] code = mLightsController.setCloud(stu, probability, mask);
        putCodeToQueue(code);
    }

    public void setFlash(int stu, int level, int probability) {
        byte[] code = mLightsController.setFlash(stu, level, probability);
        putCodeToQueue(code);
    }

    public void setMoon(boolean stu, int start, int end) {
        byte[] code = mLightsController.setMoon(stu, start, end);
        putCodeToQueue(code);
    }

//    public String getAutoStu() {
//        return mLightsController.getJsonControlMap();
//    }
//
//    public String getManualStu() {
//        return mLightsController.getJsonManual();
//    }
//
//    public String getCloudStu() {
//        return mLightsController.getJsonCloud();
//    }
//
//    public String getFlashStu() {
//        return mLightsController.getJsonFlash();
//    }
//
//    public String getMoonStu() {
//        return mLightsController.getJsonMoon();
//    }

    public String getControlCodeJson(String controlType) {
        switch (controlType) {
            case Db.TYPE_AUTO: {
                return mLightsController.getJsonControlMap();
            }
            case Db.TYPE_MANUAL: {
                return mLightsController.getJsonManual();
            }
            case Db.TYPE_CLOUD: {
                return mLightsController.getJsonCloud();
            }
            case Db.TYPE_FLASH: {
                return mLightsController.getJsonFlash();
            }
            case Db.TYPE_MOON: {
                return mLightsController.getJsonMoon();
            }
        }


        return null;
    }

    public byte[] getControlMap(String type) {
        return mLightsController.getControlCode(type);
    }

    public void setManualStu(byte[] data) {
        byte[] sData = null != data ? data : new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        if (null != data) mLightsController.setManualMap(sData);

    }

    public void setAutoStu(byte[] data) {
        if (null != data) mLightsController.setAutoMap(data);
        else mLightsController.initAutoMap();
    }

    public void setCloudStu(byte[] data) {
        byte[] sData = (null != data) ? data : new byte[]{(byte) 0xAA, 0x08, 0x0A, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0E};
        mLightsController.setmCloudCode(sData);
    }

    public void setFlashStu(byte[] data) {
        byte[] sData = (null != data) ? data : new byte[]{(byte) 0xAA, 0x08, 0x0A, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0F};
        mLightsController.setmFlashCode(sData);
    }

    public void setMoonStu(byte[] data) {
        byte[] sData = (null != data) ? data : new byte[]{(byte) 0xAA, 0x08, 0x0A, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10};
        mLightsController.setmMoonCode(sData);
    }

    public synchronized void addGroupMember(String mac) {
        mIPMap.put(mac, "0,0,0,0");
    }

    public synchronized void addGroupMember(String mac, String ip) {
        mIPMap.put(mac, ip);
    }

    public synchronized void removeGroupMember(String mac) {
        String ip = mIPMap.remove(mac);
        if (null != ip) {
            buffer.remove(ip);
            mSendBuffer.remove(ip);
        }

    }

    public String reflushDeviceIp(byte[] data) {
        if (0xaa != (data[0] & 0xff) && data.length > Light.CODE_LENGTH) {
            String fullInf = new String(data);
            String[] inf = fullInf.split(",");
            if (inf.length > 1) {
                String key = inf[1];
                if (mIPMap.containsKey(key)) {
                    if (!mIPMap.get(key).equals(inf[0])) {
                        Log.i("godlee", "not same");
                        Log.i("godlee", mIPMap.get(key));
                        Log.i("godlee", inf[0]);
                        synchronized (mIPMap) {
                            mIPMap.put(key, inf[0]);
                            return key;
                        }
                    }
                }
                if (inf[0].equals(UdpController.DEFALT_IP)) {//AP模式连接
                    synchronized (mIPMap) {
                        Iterator it = mIPMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
                            mIPMap.put(e.getKey(), "0.0.0.0");
                        }
                        mIPMap.put(key, inf[0]);
                        return key;
                    }
                }
                return "other";
            }

        }
        return null;

    }

    public void initTime(byte[] timeData) {
        putCodeToQueue(timeData);
//        mUdpController.putMsg(timeData, ip);
    }
//    public


    /**
     * 获取灯组Json,格式为{Mac:ip,Mac:ip}
     *
     * @return
     */
    public String getLightsList() {
        JSONObject obj = new JSONObject();
        Iterator it = mIPMap.entrySet().iterator();
        try {
            while (it.hasNext()) {
                Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
                obj.accumulate(e.getKey(), e.getValue());
            }
            return obj.toString();
        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());
            e.printStackTrace();
        }
        return "{}";

    }

    public void finishAll() {
        if (null != mUdpController) mUdpController.close();
        threadFlag = false;
    }

    /**
     * 将指令放入待发送队列
     *
     * @param code
     */
    private void putCodeToQueue(byte[] code) {
        sendOk = false;
//        Log.i("godlee","put code sendStuation:"+sendOk);

        ArrayList<byte[]> list = new ArrayList<byte[]>(48);
        for (int offset = 0; offset < code.length; offset += Light.CODE_LENGTH) {
            byte[] subCode = new byte[Light.CODE_LENGTH];
            System.arraycopy(code, offset, subCode, 0, Light.CODE_LENGTH);
            list.add(subCode);
        }
        int mode = code[3] & 0xff;
        int colorOrValue = code[4] & 0xff;//获取新指令特征，用以删除待发序列中相同指令或过时指令
        synchronized (mSendBuffer) {
            Iterator it = mIPMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
                String targetIp = e.getValue();
                if (!targetIp.equals("0,0,0,0")) {
                    ArrayList<byte[]> oldList = mSendBuffer.get(targetIp);
                    if (oldList != null) {
                        synchronized (oldList) {
                            for (byte[] oldCode : oldList) {
                                if ((oldCode[3] & 0xff) == mode && (oldCode[4] & 0xff) == colorOrValue) {//删除待发序列中重复或过时指令
                                    Log.i("godlee", "delete old code " + Tool.bytesToHexString(oldCode));
                                    oldList.remove(oldList.indexOf(oldCode));
                                } else {
//                                    Log.i("godlee","mode:new: "+mode+" old: "+(oldCode[3]&0xff)+" value:new: "+colorOrValue+" old: "+(oldCode[4]&0xff));
                                }
                            }
                            oldList.addAll(list);
                        }
                        mSendBuffer.put(targetIp, oldList);
                    } else {
                        mSendBuffer.put(targetIp, new ArrayList<byte[]>(list));
                    }
                }
            }
        }
    }

    private DataPack formatReceive(DataPack revPacket) {
        if (revPacket.getLength() % Light.CODE_LENGTH != 0) {
            DataPack sBuff = buffer.get(revPacket.getIp());
            if (sBuff != null) {
                sBuff.merge(revPacket);
                if (0 == sBuff.getLength() % Light.CODE_LENGTH) {
                    revPacket = sBuff;
                    buffer.remove(sBuff.getIp());
                } else {
                    buffer.put(sBuff.getIp(), sBuff);
                    return null;
                }
            } else {
                buffer.put(revPacket.getIp(), revPacket);
                return null;
            }
        }
//        String fromIp=revPacket.reflushDeviceIp();
//        int fromPort=revPacket.getPort();
//        byte[] realData =revPacket.getData();
//        Log.i("godlee", "from:" + fromIp + ":" + fromPort + ".length(byts):"+revPacket.getLength()+"  "+ Tool.bytesToHexString(realData));
        return revPacket;

    }

    private void reGroupSendQueue(DataPack pack) {
        D.i("back:"+Tool.bytesToHexString(pack.getData())+" from IP:"+pack.getIp());
        String ip = pack.getIp();
        ArrayList<byte[]> list = mSendBuffer.get(ip);
        if (null != list) {
            byte[] packData = new byte[Light.CODE_LENGTH];
            for (int offset = 0; offset < pack.getLength(); offset += Light.CODE_LENGTH) {
                if (0x0a == (packData[2] & 0xff)) continue;
//                Log.i("godlee", "reGroupWork");
                System.arraycopy(pack.getData(), offset, packData, 0, Light.CODE_LENGTH);
                packData[2] = (byte) 0x0a;
                for (byte[] data : list) {
                    if (Arrays.equals(data, packData)) {
                        synchronized (list) {
//                            Log.i("godlee", "codeConfirmed");
                            int index = list.indexOf(data);
                            if (index > -1) {
                                list.remove(list.indexOf(data));
                            }

                        }
                        break;
                    }
                }
            }
        }
    }

    private void reGroupSendQueue(byte[] data) {

    }

    private Runnable SendQueue = new Runnable() {
        @Override
        public void run() {
            while (threadFlag) {
                if (mSendBuffer.size() > 0) {
                    Iterator it = mSendBuffer.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, ArrayList<byte[]>> e = (Map.Entry<String, ArrayList<byte[]>>) it.next();
                        ArrayList<byte[]> list = e.getValue();
                        String ip = e.getKey();
                        if (null != list && (!ip.equals("0.0.0.0"))) {
                            if (list.size() > 0) {
                                synchronized (list) {
                                    for (byte[] data : list) {
//                                    Log.i("godlee","sendQueue: "+Tool.bytesToHexString(data));
                                        mUdpController.putMsg(data, ip);
                                    }
                                }
                            } else {
                                synchronized (mSendBuffer) {
                                    mSendBuffer.remove(ip);
//                                    String mac=mIPMap.
                                    mUdpController.putMsg(new byte[]{(byte)0xAA,0x08,0x0A,0x08,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x13},ip);//执行指令,自动模式只有使用此指令才能开始运行
                                    D.i("run");
                                    Message msg=mHandle.obtainMessage(SINGLE_SEND_OK,ip);
                                    mHandle.sendMessage(msg);
                                }
                            }
                        }
                    }
                } else {
//                    Log.i("godlee","is send ok? " +sendOk);
                    if (!sendOk) {
                        sendOk = true;
                        mHandle.sendEmptyMessage(SEND_OK);
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
