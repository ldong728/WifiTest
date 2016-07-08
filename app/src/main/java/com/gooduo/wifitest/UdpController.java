package com.gooduo.wifitest;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


/**
 * @author godlee
 */
public class UdpController extends Thread {
    private static final String IP = "255.255.255.255";
    public static final int PORT=26000;
    private Handler handler;
    private DatagramSocket socket;
    private SendThread mSendThread;



    //	private final String IP = "192.168.1.1";
//    private int PORT = 9090;
//    private int PORT=8899;

    /**
     * 48899端口：C32x系列的端口，用户可以用AT指令更改
     * 49000端口：除C32x系列，其他WIFI模块的端�?
     * 1902端口：有人掌控宝系列产品的端�?
     */
//    private int usrPort = 48899;

    private int usrPort = 8899;

    private boolean receive = true;

    public UdpController(Handler handler) {
        mSendThread = new SendThread();
        this.handler = handler;
        init();
    }
    public UdpController(Handler handler,int port){
        usrPort=port;
        mSendThread = new SendThread();
        this.handler = handler;
        init();
    }

    public void init() {
        try {
            socket = new DatagramSocket(null);
            socket.setBroadcast(true);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(PORT));
            Log.i("godlee", "udp init ok");
        } catch (SocketException e) {
            e.printStackTrace();
            sendErrorMsg("Search Thread init fail");
            return;
        }
    }

    public void sendThreadStart() {
        mSendThread.start();
    }

    public void putMsg(byte[] msg) {
        mSendThread.putMsg(msg);
    }


    public void run() {
        if (socket == null) {
            return;
        }

        try {
            byte[] data = new byte[1024];
            DatagramPacket revPacket = new DatagramPacket(data, data.length);
            while (receive) {
//                Log.i("godlee", "wait");
                socket.receive(revPacket);
                Log.i("godlee", "from ip:" + revPacket.getAddress().getHostAddress());
                Log.i("godlee","from port:"+revPacket.getPort());
//                sendMsg(new byte[]{(byte)0xaa,0x08,0x0a,0x09,0x20,0x07,0x05,0x09,0x20,0x05,0x00},revPacket.getAddress().getHostAddress(),revPacket.getPort());
                if (null != handler) {
                    byte[] realData = new byte[revPacket.getLength()];
                    System.arraycopy(data, 0, realData, 0, realData.length);
                    Message msg = handler.obtainMessage(filteData(realData), realData);
                    handler.sendMessage(msg);
//                    decodeData(realData);
                }
                Log.i("godlee", "receive");
            }
        } catch (Exception e) {
            Log.e("godlee", e.getMessage());
            e.printStackTrace();
            socket.close();
        }
    }

    private int filteData(byte[] data) {
        if ((data[0] & 0xff) == 0xff) {//usr mode
            switch (data[3] & 0xff) {
                case 0x81:
                    return Tool.WIFI_LIST_DATA;
                default:
                    return Tool.ERR_DATA;
            }
        }
        if((data[0]&0xff)==0xaa&&(data[1]&0xff)==0x08) {//指令
            return Tool.CFM_DATA;
//            switch (data[3]&0xff){
//
//            }
        }
        return Tool.ERR_DATA;
    }

    private void decodeData(byte[] data) {
//		if ((data[0] & 0xff) != 0xff)// 如果接收到的数据不是0xff开头,那么丢弃
//			return;
        if ((data[0] & 0xff) == 0xff) {
            switch (data[3] & 0xff) {
                case 0x81:// 解析返回列表指令
                    ArrayList<Item> ssids = Tool.decode_81_data(data);
                    if (ssids.size() != 0) {
                        for (Item ssid : ssids) {
                            Log.i("godlee", ssid.getName() + "---" + ssid.getDbm());
                        }
                    }
                    break;
                case 0x82:// 返回校验结果
                    int[] values = Tool.decode_82_data(data);
//				if (values[0] == 0)
//					UIUtil.toastShow(this, R.string.no_ssid);
//				else if (values[1] == 0)
//					UIUtil.toastShow(this, R.string.error_pasd_length);
//				else if (values[0] == 1 && values[1] == 1)
//					UIUtil.toastShow(this, R.string.confing_end);
                    break;
            }
        }

    }

    public void close() {
        receive = false;
        mSendThread.set(false);
        handler=null;
        if (socket == null)
            return;
        socket.close();

    }


    private void sendErrorMsg(String info) {

    }

    private void sendMsg(byte[] msg) {
        if (socket != null) {
            try {
                Log.i("godlee", "usrPort------------------->" + usrPort);
                DatagramPacket sendPacket = new DatagramPacket(msg, msg.length,
                        InetAddress.getByName(IP), usrPort);
                socket.send(sendPacket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.out.println("发送失败（未知主机）");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("发送失败（IO错误）");
            } catch (IllegalBlockingModeException e){
                e.printStackTrace();
            }

        }
    }
    private void sendMsg(DatagramPacket pkg) {
        if (socket != null) {
            try {
                Log.i("godlee", "usrPort------------------->" + usrPort);
//                DatagramPacket sendPacket = new DatagramPacket(msg, msg.length,
//                        InetAddress.getByName(IP), usrPort);
                socket.send(pkg);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.out.println("发送失败（未知主机）");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("发送失败（IO错误）");
            } catch (IllegalBlockingModeException e){
                e.printStackTrace();
            }

        }
    }
    public void sendMsg(byte[] msg,String ip,int port) {
        if (socket != null) {
            try {
                Log.i("godlee", "sendtoPort------------------->" + port);
                DatagramPacket sendPacket = new DatagramPacket(msg, msg.length,
                        InetAddress.getByName(ip), port);
                socket.send(sendPacket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.out.println("发送失败（未知主机）");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("发送失败（IO错误）");
            } catch (IllegalBlockingModeException e){
                e.printStackTrace();
            }

        }
    }
    public void sendMsg(byte[] msg,int port) {
        if (socket != null) {
            try {
                Log.i("godlee", "usrPort------------------->" + port);
                DatagramPacket sendPacket = new DatagramPacket(msg, msg.length,
                        InetAddress.getByName(IP), port);
                socket.send(sendPacket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.out.println("发送失败（未知主机）");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("发送失败（IO错误）");
            } catch (IllegalBlockingModeException e){
                e.printStackTrace();
            }

        }
    }

    public void setReceive(boolean receive) {
        this.receive = receive;
    }

    public void setUsrPort(int usrPort) {
        this.usrPort = usrPort;
    }

    class SendThread extends Thread {
        private Queue<DatagramPacket> sendMsgQueue = new LinkedList<DatagramPacket>();
        // 是否发送消息
        private boolean send = true;

        //        private UdpController ss;
//        public SendThread(){
//
//        }
        public void putMsg(byte[] msg) {
            putMsg(msg,IP,usrPort);
        }
        public void putMsg(byte[] msg,int port){
            putMsg(msg,IP,usrPort);
        }

        public synchronized  void  putMsg(byte[] msg,String ip,int port){
            try{
                DatagramPacket sDatagramPacket=new DatagramPacket(msg,msg.length,InetAddress.getByName(ip),port);
                if (0 == sendMsgQueue.size()) {
                    notify();
                    Log.i("godlee", "put msg");
                }
                sendMsgQueue.offer(sDatagramPacket);
            }catch(IOException e){
                Log.e("godlee",e.getMessage());
                e.printStackTrace();
            }

        }

        public void set(boolean flag) {
            send = flag;
        }

        @Override
        public void run() {
            synchronized (this) {
                while (send) {
                    while (sendMsgQueue.size() > 0) {
                        DatagramPacket msg = sendMsgQueue.poll();
                        UdpController.this.sendMsg(msg);
                        Log.i("godlee", "sendMmsg:");
                    }
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Log.e("godlee", e.getMessage());
                    }
                }
            }
        }

    }
}
