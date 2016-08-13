package com.gooduo.wifitest;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2016/7/14.
 */

public class JsLightBridge extends JsBridge {
    private LightControllerGroup mLightControllerGroup;
    private Db mDb;

    public JsLightBridge(Handler mHandler, LightControllerGroup lightControllerGroup, Db mDb) {
        super(mHandler);
        mLightControllerGroup = lightControllerGroup;
        this.mDb = mDb;
    }

    @JavascriptInterface
    public void sendAutoCode(final String data) {

        byte[] map = mLightControllerGroup.getAutoMap();
        Log.i("godlee", "put code" + Tool.bytesToHexString(map));
//        mDb.temp();
//        mDb.saveCode(Db.TYPE_AUTO, map);
        Log.i("godlee", "put ok");
        JSONObject sJson;
        int color, time, level;
        boolean send;
        try {
            sJson = new JSONObject(data);
            color = Integer.parseInt(sJson.getString("color"));
            time = Integer.parseInt(sJson.getString("time"));
            level = Integer.parseInt(sJson.getString("level"));
            send = sJson.getString("mode").equals("confirm") ? true : false;
            mLightControllerGroup.autoController(color, time, level, send);
        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());
            e.printStackTrace();
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
            mLightControllerGroup.manualController(color, level);
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
        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void setFlashCode(final String data) {
        JSONObject sJson;
        int level, prob;
        try {
            sJson = new JSONObject(data);
            prob = Integer.parseInt(sJson.getString("prob"));
            level = Integer.parseInt(sJson.getString("level"));
            mLightControllerGroup.setFlash(prob, level);
        } catch (JSONException e) {
            Log.e("godlee", e.getMessage());
            e.printStackTrace();
        }
    }

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
    public String getAutoCode() {
        return mLightControllerGroup.getAutoStu();
    }

    @JavascriptInterface
    public String getManualCode() {
        return mLightControllerGroup.getManualStu();
    }

    @JavascriptInterface
    public String getCloudCode() {
        return mLightControllerGroup.getCloudStu();
    }

    @JavascriptInterface
    public String getFlashCode() {
        return mLightControllerGroup.getFlashStu();
    }

    @JavascriptInterface
    public String getMoonCode() {
        return mLightControllerGroup.getMoonStu();
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
            boolean stu = mDb.signIn(mail, pasd);
            if (stu) return "1";
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
//        Log.i("godlee","getInf");
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
//        String list="[";
//        if(null!=objs){
//            int count=0;
//            for(JSONObject obj:objs){
//                list+=obj.toString();
//                count++;
//                if(count<objs.length)list+=",";
//            }
//            list+="]";
//            Log.i("godlee","get GroupList:"+list);
//            return list;
//        }
//        Log.i("godlee","there have no GroupList");
//        return "[]";

    }

    @JavascriptInterface
    public int addGroup(final String inf) {
        try {
            JSONObject obj = new JSONObject(inf);
            String groupName = obj.getString("name");
            String groupInf = obj.getString("inf");
            int groupId = mDb.addGroup(groupName, groupInf);
            if (groupId > -1) mDb.setGroupId(groupId);
            return groupId;

        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

//    @JavascriptInterface
//    public void changeGroupType(final String inf){
//        try{
//            JSONObject obj=new JSONObject(inf);
//
//        }catch(JSONException e){
//            e.printStackTrace();
//        }
//    }

    @JavascriptInterface
    public void addDevice(final String inf) {
        try {
            JSONObject obj = new JSONObject(inf);
            String mac = obj.getString("mac");
            mDb.addDevice(mac, WifiClass.ssid, "light", "light");

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
        JSONObject groupInf = mDb.getGroupInf();
        JSONObject returnInf = new JSONObject();
        String deviceList=mLightControllerGroup.initGroup(mDb);;
        try {
            String sGroupType = groupInf.getString(Db.G_TYPE);
            returnInf.accumulate("device",deviceList);
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
//                    mLightControllerGroup.initGroup(mDb);
                    return "";

            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
        return "";

    }


//    public String addGroup()


}
