package com.gooduo.wifitest;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

/**
 * Created by Administrator on 2016/7/14.
 */

public class JsLightBridge extends JsBridge {
    public static final String STATUS="status";
    public static final String INF="inf";
    public static final String STATUS_DEL_DEVICE="delDevice";
    public static final String STATUS_USER_ADD="SN";
    public  boolean ismReadyToSend() {
        return mReadyToSend;
    }
    public void setmReadyToSend(boolean mReadyToSend) {
        this.mReadyToSend = mReadyToSend;
    }
    private boolean isGroupOnLine=false;
    private String mCurrentType;
    private String mCurrentScene;
    private String mSn;
    private boolean mReadyToSend=false;
    private LightControllerGroup mLightControllerGroup;
    private WebSocketController mWsc;
    private Db mDb;
    private LinkedList<String> mOfflineList;
    //websocket 连接控制
    private WebSocketController.ReceiveMessage mReceive=new WebSocketController.ReceiveMessage(){
        @Override
        public void onClose(int code, String reason) {
            mOfflineList=null;
        }
        @Override
        public void onOpen() {
            mOfflineList=mDb.getOfflineQueue();
            D.i("webSocket connect ok");
        }
        @Override
        public void onTextMessage(String payload) {
            D.i(payload);
            try{
                JSONObject sJpayload=new JSONObject(payload);
                String sStatus=sJpayload.getString(STATUS);
                String sInf=sJpayload.getString(INF);
                if(sInf.equals("ok")){
                    switch (sStatus){
                        case STATUS_USER_ADD:
                            mSn=sJpayload.getString("content");
                            mDb.synUser(mSn);
                            break;
                        default:
                            break;
                    }
                }
            }catch(JSONException e){
                D.i(e.getMessage());
                D.i(payload);
            }
            //处理因断线无法同步的数据
            if(null!=mOfflineList){
                String data=mOfflineList.poll();
                D.i("get data from offline_tbl: "+data);
                if(null!=data){
                    mWsc.sendData(data);
                    mDb.deleteDataFromOffline(data);
                }
            }


        }
    };

    public JsLightBridge(Handler mHandler, LightControllerGroup lightControllerGroup, Db mDb,WebSocketController mWsc) {
        super(mHandler);
        mLightControllerGroup = lightControllerGroup;
        this.mDb = mDb;
        mSn=mDb.getmCurrentUsn();
//        D.i("sn="+mSn);
        this.mWsc=mWsc;

        mWsc.setReceiver(mReceive);
    }
    private void syncCode(String type, String data){
        if(isGroupOnLine){
            mWsc.sendData(type,data,mDb);
        }
    }


    @JavascriptInterface
    public String sendAutoCode(final String data) {

//        byte[] map = mLightControllerGroup.getAutoMap();
//        Log.i("godlee", "put code" + Tool.bytesToHexString(map));
//        mDb.temp();
//        mDb.saveCode(Db.TYPE_AUTO, map);
//        Log.i("godlee", "put ok");
        JSONObject sJson;
        int color, time, level;
        boolean send;
        try {
            sJson = new JSONObject(data);
            color = Integer.parseInt(sJson.getString("color"));
            time = Integer.parseInt(sJson.getString("time"));
            level = Integer.parseInt(sJson.getString("level"));
            send = sJson.getString("mode").equals("confirm") ? true : false;
            Log.i("godlee", "color: " + color + " time: " + time + " level: " + level);
            mLightControllerGroup.autoController(color, time, level, send);
            mCurrentType=Db.TYPE_AUTO;
//            JSONObject obj=mWsc.createCodeData(Db.TYPE_AUTO,mDb.getGroupId(),mLightControllerGroup.getControlCodeJson(Db.TYPE_AUTO),mSn);
//            mWsc.sendData(obj);
            return "1";
        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());
            e.printStackTrace();
            return "0";
        }
    }



    @JavascriptInterface
    public void setManualCode(final String data) {
        JSONObject sJson;
        int color, level;
        Log.i("godlee","manualCode");
        try {
            sJson = new JSONObject(data);
            color = Integer.parseInt(sJson.getString("color"));
            level = Integer.parseInt(sJson.getString("level"));
            Log.i("godlee","color:"+color+" level:"+level);
            mLightControllerGroup.manualController(color, level);
            mCurrentType=Db.TYPE_MANUAL;
//            JSONObject obj=mWsc.createCodeData(Db.TYPE_MANUAL,mDb.getGroupId(),mLightControllerGroup.getControlCodeJson(Db.TYPE_MANUAL),mSn);
//            mWsc.sendData(obj);

        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void setCloudCode(final String data) {
        JSONObject sJson;
        boolean stu;
        int prob, mask;

        try {
            sJson = new JSONObject(data);
            stu = Integer.parseInt(sJson.getString("stu")) == 0 ? false : true;
            prob = Integer.parseInt(sJson.getString("prob"));
            mask = Integer.parseInt(sJson.getString("mask"));
            mLightControllerGroup.setCloud(stu, prob, mask);
            mCurrentScene=Db.TYPE_CLOUD;
        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void setFlashCode(final String data) {
        JSONObject sJson;
        int level, prob,stu;
        try {
            sJson = new JSONObject(data);
            stu = Integer.parseInt(sJson.getString("stu"));
            prob = Integer.parseInt(sJson.getString("prob"));
            level = Integer.parseInt(sJson.getString("level"));
            mLightControllerGroup.setFlash(stu, prob, level);
            mCurrentScene=Db.TYPE_FLASH;
        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());

            e.printStackTrace();
        }
    }
    @JavascriptInterface
    public void setMoonCode(final String data) {
        JSONObject sJson;
        boolean stu;
        int start, end;
        try {
            sJson = new JSONObject(data);
            stu = Integer.parseInt(sJson.getString("stu")) == 0 ? false : true;
            start = Integer.parseInt(sJson.getString("start"));
            end = Integer.parseInt(sJson.getString("end"));
            mLightControllerGroup.setMoon(stu, start, end);
        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void initTime() {
        Log.i("godlee","initTime");
        SimpleDateFormat data = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
        String sTime = data.format(new java.util.Date());
        String[] times = sTime.split(",");
        int y = Integer.parseInt(times[0]);
        int M = Integer.parseInt(times[1]);
        int d = Integer.parseInt(times[2]);
        int h = Integer.parseInt(times[3]);
        int m = Integer.parseInt(times[4]);
        int s = Integer.parseInt(times[5]);
        byte[] timeCode = new byte[]{
                (byte) 0xaa,
                (byte) 0x08,
                (byte) 0x0a,
                (byte) 0x09,
                (byte) (y - 1970),
                (byte) M,
                (byte) d,
                (byte) h,
                (byte) m,
                (byte) s,
                (byte) 0,
                (byte) (0x0a + 0x09 + (y - 1970) + M + d + h + m + s)
        };
        mLightControllerGroup.initTime(timeCode);
        Log.i("godlee", data.toPattern());
        Log.i("godlee", data.toLocalizedPattern());
        Log.i("godlee", data.format(new java.util.Date()));
    }
    @JavascriptInterface
        public String tempMethod(){
        Log.i("godlee","temp");
        return "ok";
    }
    @JavascriptInterface
    public String getControlCode(final String type){
        return mLightControllerGroup.getControlCodeJson(type);
    }
    @JavascriptInterface
    public void saveCodeToDb(final String type){
        byte[] code=mLightControllerGroup.getControlMap(type);
        mDb.setCode(type,code);
    }

    @JavascriptInterface
    public String getLightList() {
        return mLightControllerGroup.getLightsList();
    }

    @JavascriptInterface
    public int addUser(final String userInf) {
        int id = -1;
        try {
            JSONObject obj = new JSONObject(userInf);
            String name = obj.getString("name");
            String email = obj.getString("email");
            String phone = obj.getString("phone");
            String pasd = obj.getString("pasd");
            id = mDb.addUser(name, email, phone, pasd);
//            if(mWsc.isConnect()){
                String str="{\"mode\":\"reg\",\"U_EMAIL\":\""+email+"\",\"U_PHONE\":\""+phone+"\",\"U_NAME\":\""+name+"\",\"U_PASD\":\""+pasd+"\",\"signature\":\"\"}";
                mWsc.sendData(Db.TYPE_OTHER,str,mDb);
                D.i(str);
//                mWsc.sendData(str);
//            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    @JavascriptInterface
    public String signin(final String inf) {
        try {
            JSONObject obj = new JSONObject(inf);
            String mail = obj.getString("email");
            String pasd = obj.getString("pasd");
            String stu = mDb.signIn(mail, pasd);
//            mDb=obj.getString(Db.)
            if (stu!=null){
                mSn=stu;
                return "1";
            }
            return "0";
        } catch (JSONException e) {
            e.printStackTrace();
            return "-1";
        }

    }

    @JavascriptInterface
    public String getUserList() {
        JSONObject[] objs = mDb.getUserList();
        return Tool.jsonArray2String(objs);
    }

    @JavascriptInterface
    public String getUserInf() {
        Log.i("godlee","getInf");
        JSONObject obj = mDb.getUserInf();
        if (null != obj) {
            return obj.toString();
        } else {
            return "";
        }
    }

    @JavascriptInterface
    public String getGroupList(String type) {
        JSONObject[] objs = mDb.getGroupList(type);
        return Tool.jsonArray2String(objs);
    }

    @JavascriptInterface
    public String getGroupInf(){
        JSONObject obj=mDb.getGroupInf();
        return obj.toString();
    }

    @JavascriptInterface
    public int addGroup(final String inf) {
        try {
            JSONObject obj = new JSONObject(inf);
            String groupName = obj.getString("name");
            String groupInf = obj.getString("inf");
            int groupId = mDb.addGroup(groupName, groupInf);
            if (groupId > -1){
                mDb.setGroupId(groupId);

            }
            return groupId;

        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @JavascriptInterface
    public void changeGroupType(final String inf){
        try{
            JSONObject obj=new JSONObject(inf);
            String ssid=obj.getString("ssid");
            String pasd=obj.getString("pasd");
            mDb.changeGroupType(ssid,pasd);
            JSONObject gInf=mDb.getGroupInf();
            JSONObject data=new JSONObject();
            data.accumulate("mode","createGroup");
            data.accumulate("G_ID",mDb.getGroupId());
            data.accumulate(Db.G_NAME,gInf.getString(Db.G_NAME));
            data.accumulate(Db.G_INF,gInf.getString(Db.G_INF));
            data.accumulate(Db.G_TYPE,"online");
            data.accumulate(Db.G_SSID,ssid);
            data.accumulate(Db.G_SSID_PASD,pasd);
            data.accumulate("device",mDb.getDeviceList()[0]);
            data.accumulate("U_SN",mDb.getmCurrentUsn());
            mWsc.sendData(Db.TYPE_OTHER,data.toString(),mDb);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
    @JavascriptInterface
    public void mergeGroup(final String inf){
        try{
            JSONObject obj=new JSONObject(inf);
            String ssid=obj.getString("ssid");
            String pasd=obj.getString("pasd");
            mDb.changeGroupType(ssid,pasd);

        }catch(JSONException e){
            e.printStackTrace();
        }
    }


    @JavascriptInterface
    public void addDevice(final String inf) {
        try {
            JSONObject obj = new JSONObject(inf);
            String mac = obj.getString("mac");
            mDb.addDevice(mac, WifiClass.ssid, "light", "light");
            JSONObject data=new JSONObject();
            data.accumulate("mode","addDevice");
            data.accumulate(Db.G_ID,mDb.getGroupId());
            data.accumulate(Db.D_MAC,mac);
            data.accumulate(Db.D_TYPE,"light");
            data.accumulate(Db.D_NAME,"light");
            data.accumulate(Db.U_SN,mDb.getmCurrentUsn());
            mWsc.sendData(Db.TYPE_OTHER,data.toString(),mDb);



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void chooseGroup(final String inf) {
        Log.i("godlee", "choose Group");
        int groupId = Integer.parseInt(inf);
        mDb.setGroupId(groupId);

    }

    @JavascriptInterface
    public String initGroup() {
//        D.i("initGroup");
        JSONObject groupInf = mDb.getGroupInf();
        JSONObject returnInf = new JSONObject();
        String deviceList=mLightControllerGroup.initGroup(mDb);
//        Log.i("godlee","deviceList: "+deviceList);
        try {
            String sGroupType = groupInf.getString(Db.G_TYPE);
            JSONObject deviceListJson=new JSONObject(deviceList);
            returnInf.accumulate("device",deviceListJson);
            returnInf.accumulate("inf",groupInf);
            switch (sGroupType) {
                case Db.GROUP_TYPE_LOCAL:
                    returnInf.accumulate("type", Db.GROUP_TYPE_LOCAL);
//                    mLightControllerGroup.initGroup(mDb);
                    String[] deviceInf = mDb.getValue(Db.DEVICE_TBL, new String[]{Db.D_SSID}, Db.G_ID + "=?", new String[]{"" + mDb.getGroupId()});
                    String ssid = deviceInf[0];
                    Log.i("godlee", "get device SSID from DB:" + ssid);
                    returnInf.accumulate("ssid", ssid);
                    Message msg = mHandler.obtainMessage(JsBridge.LOCAL_LINK, ssid);
                    mHandler.sendMessage(msg);
                    return returnInf.toString();

                case Db.GROUP_TYPE_ONLINE:
                    returnInf.accumulate("type", Db.GROUP_TYPE_ONLINE);
                    returnInf.accumulate("ssid",groupInf.getString(Db.G_SSID));
                    D.i(returnInf.toString());
                    isGroupOnLine=true;
                    return returnInf.toString();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        D.i("nothing");
        return "";
    }
    @JavascriptInterface
    public void saveCode(){
        mReadyToSend=true;
        if(mLightControllerGroup.isSendOk()){
            mHandler.sendEmptyMessage(LightControllerGroup.SEND_OK);

            D.i("save");
        }
        mWsc.sendData(mCurrentType,mLightControllerGroup.getControlCodeJson(mCurrentType),mDb);
        if(null!=mCurrentScene){
            mWsc.sendData(mCurrentScene,mLightControllerGroup.getControlCodeJson(mCurrentScene),mDb);
        }
    }

    @JavascriptInterface
    public void runCode(){

    }







//    public String addGroup()


}
