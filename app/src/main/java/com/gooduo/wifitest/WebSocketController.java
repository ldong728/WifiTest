package com.gooduo.wifitest;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by Administrator on 2016/9/26.
 */

public class WebSocketController{
//    public static final int WEB_SOCKET_CONNECT_OK=0xefabc;
    private static final String URL="ws://192.168.0.78:7272";
    private WebSocketConnection mWsConnection;
    private Handler mHandler;
    private ReceiveMessage mReceive;





    public WebSocketController(Handler mHandler) {
        mWsConnection = new WebSocketConnection();
        this.mHandler=mHandler;
    }
    public void setReceiver(ReceiveMessage l){
        mReceive=l;
    }

    public void connect() {
        D.i("connecting");
        if(!isConnect()){
            try {
                mWsConnection.connect(URL, mWscHandler);

            } catch (WebSocketException e) {
                e.printStackTrace();
                D.e("can't connect");
            }
        }


    }
    public void disConnected(){
        if(isConnect()){
            mWsConnection.disconnect();
        }
    }

    public boolean isConnect(){
        return mWsConnection.isConnected();
    }
    public void sendData(String data){
        D.i("data sended"+data);
        mWsConnection.sendTextMessage(data);
    }
    private JSONObject createCodeData(String type,String code,Db db) throws JSONException{
        if(null!=db.getmCurrentUsn()){
            JSONObject sCodeMap=new JSONObject(code);
            JSONObject sCodeContent=new JSONObject();
            sCodeContent.accumulate("C_TYPE",type);
            sCodeContent.accumulate("code",sCodeMap);
            JSONObject obj=new JSONObject();
            obj.accumulate("mode","codeSet");
            obj.accumulate("G_ID",db.getGroupId());
            obj.accumulate("code",sCodeContent);
            obj.accumulate("U_SN",db.getmCurrentUsn());
            return obj;
        }
        return null;
    }
    private JSONObject createOtherData(String data) throws JSONException{
        return new JSONObject(data);
//        return null;
    }

    public void sendData(JSONObject data){
        if(null!=data){
            String sData=data.toString();
            sendData(sData);
        }
    }
//    public void syncData()
    public void sendData(String type,String code,Db db){
        if(isConnect()){
            try{
                JSONObject obj;
                if(Db.TYPE_OTHER==type){
                    obj=createOtherData(code);
                }else{
                    obj=createCodeData(type,code,db);
                }

                sendData(obj);
            }catch(JSONException e){
                D.e(e.getMessage());
                e.printStackTrace();
            }
        }else{
            D.i("websocked conect fail,save to Db");
//            db.putDataToOffline(type,code);
        }
    }
//    public void sendOtherData(String code,Db db){
//        if(isConnect()){
//
//        }else{
//
//        }
//    }


//    public void syncData(JSONObject data,)


    private WebSocketHandler mWscHandler = new WebSocketHandler() {
        @Override
        public void onBinaryMessage(byte[] payload) {
            super.onBinaryMessage(payload);
        }

        @Override
        public void onClose(int code, String reason) {
            D.i("close code:"+code+" reason:"+reason);
            if(mReceive!=null)mReceive.onClose(code,reason);
            super.onClose(code, reason);
        }

        @Override
        public void onOpen() {
            D.i("webSocket connected ok");
            if(mReceive!=null)mReceive.onOpen();
//            mHandler.sendEmptyMessage(WEB_SOCKET_CONNECT_OK);
            super.onOpen();
        }

        @Override
        public void onRawTextMessage(byte[] payload) {
            D.i("onRawTextMessage");
            D.i(Tool.bytesToHexString(payload));
            super.onRawTextMessage(payload);
        }

        @Override
        public void onTextMessage(String payload) {
            D.i("onTextMessage");
//            D.i(payload);
            if(mReceive!=null)mReceive.onTextMessage(payload);
            super.onTextMessage(payload);
        }
    };
    public interface ReceiveMessage{
        public void onTextMessage(String payload);
        public void onClose(int code,String reason);
        public void onOpen();

    }

}
