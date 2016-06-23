package com.gooduo.wifitest;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.net.SocketFactory;

/**
 * Created by Administrator on 2016/6/21.
 */
public class TcpController extends Thread{
    private Socket mSocket;
    private Activity mActivity;
    private boolean mRevFlag=true;
    private Handler mHandler;

    public TcpController(Activity mActivity,Handler handler){
        this.mHandler = handler;
        this.mActivity=mActivity;
    }
    public void init(String ip,int port){
        try{
            InetSocketAddress sAddress=new InetSocketAddress(ip,port);

            mSocket= new Socket();
            mSocket.bind(sAddress);
//            mSocket=new Socket(sAddress.getHostString(),port);
        }catch(SocketException e){
            Log.e("godlee",e.getMessage());

        }catch (IOException e){
            Log.e("godlee",e.getMessage());
        }


    }
    public void sendData(byte[] data){
        try{
            OutputStream sOut=mSocket.getOutputStream();
            sOut.write(data);
        }catch(IOException e){
            Log.e("godlee",e.getMessage());
        }
    }
    public void temp(){
        byte[] array=new byte[]{0x12,0x13,(byte)0xFF,(byte)0x5a};
        Log.i("godlee", Tool.bytesToHexString(array));
//        sendData(array);
    }
    public void stopServer(){
        mRevFlag=false;
    }


    @Override
    public void run() {
//        ServerSocket sServerSocket=null;
        Socket sSocket;
        InputStream sInputStream;
        byte buffer[] =new byte[1024];
        try{
            ServerSocket sServerSocket=new ServerSocket(4900);
            while(mRevFlag){
                Log.i("godlee","startServer");
                sSocket=sServerSocket.accept();
                sInputStream=sSocket.getInputStream();
                sInputStream.read(buffer);
                Log.i("godlee",Tool.bytesToHexString(buffer));

            }
            sServerSocket.close();
        }catch(IOException e){
            Log.e("godlee", e.getMessage());
//            sServerSocket.close();
            mRevFlag=false;
//            sSocket.close();
        }
        catch(Exception e){
            Log.e("godlee",e.getMessage());
        }



    }
}
