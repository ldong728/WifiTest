package com.gooduo.wifitest;

import android.os.Handler;

import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by Administrator on 2016/9/26.
 */

public class WebSocketController{
    private static final String URL="ws://192.168.0.53:7272";
    private WebSocketConnection mWsc;
    private Handler mHandler;
    private ReceiveMessage mReceive;





    public WebSocketController(Handler mHandler) {
        mWsc = new WebSocketConnection();
        this.mHandler=mHandler;
    }
    public void setReceiver(ReceiveMessage l){
        mReceive=l;
    }

    public void connect() {
        D.i("connecting");
        if(!isConnect()){
            try {
                mWsc.connect(URL, mWscHandler);

            } catch (WebSocketException e) {
                e.printStackTrace();
                D.e("can't connect");
            }
        }


    }
    public void disConnected(){
        if(isConnect()){
            mWsc.disconnect();
        }
    }

    public boolean isConnect(){
        return mWsc.isConnected();
    }
    public void sendData(String data){
        D.i("data sended"+data);
        mWsc.sendTextMessage(data);
    }
    public void sendData(JSONObject data){
        String sData=data.toString();
        sendData(sData);
    }



    private WebSocketHandler mWscHandler = new WebSocketHandler() {
        @Override
        public void onBinaryMessage(byte[] payload) {
            super.onBinaryMessage(payload);
        }

        @Override
        public void onClose(int code, String reason) {
            D.i("close code:"+code+" reason:"+reason);

            super.onClose(code, reason);
        }

        @Override
        public void onOpen() {
            D.i("webSocket connected ok");
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
            D.i(payload);
            if(mReceive!=null)mReceive.onTextMessage(payload);
            super.onTextMessage(payload);
        }
    };
    public interface ReceiveMessage{
        public void onTextMessage(String payload);
    }

}
