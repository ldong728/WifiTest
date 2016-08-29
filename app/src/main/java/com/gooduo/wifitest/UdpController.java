package com.gooduo.wifitest;

import android.os.Handler;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;


/**
 * @author godlee
 */
public abstract class UdpController extends Thread {
    public static final int DATA_PORT =8899;
    public static final int CTR_PORT=48899;
    public static final String BROADCAST_IP="255.255.255.255";
    public static final String DEFALT_IP= "192.168.1.1";
    private static int count;
    public  final int id;
    private boolean broadcast=false;
    private  String ip;
    public int localPort =26000;
    private Handler handler;
    private DatagramSocket socket;
    private SendThread mSendThread;
    private HashMap<String,DataPack> buffer;



    //	private final String ip = "192.168.1.1";
//    private int localPort = 9090;
//    private int localPort=8899;

    /**
     * 48899端口：C32x系列的端口，用户可以用AT指令更改
     * 49000端口：除C32x系列，其他WIFI模块的端�?
     * 1902端口：有人掌控宝系列产品的端�?
     */
//    private int DATA_PORT = 48899;

//    private int DATA_PORT = 8899;

    private boolean receive = true;
    public UdpController(Handler handler,String ip,int port){
        id=count++;
        Log.i("godlee","new UDPController id:"+id);
        this.ip=ip;
        localPort=port;
        mSendThread = new SendThread();
        this.handler = handler;
        init();
    }
    public UdpController(Handler handler) {
        this(handler,BROADCAST_IP,26000);
        broadcast=true;
//        mSendThread = new SendThread();
//        this.handler = handler;
//        init();
    }
    public UdpController(Handler handler,int port){

        this(handler,BROADCAST_IP,port);
        broadcast=true;
//        init();
    }



    public abstract void onReceive(Handler handler,DataPack pack);



    public void init() {
        buffer=new HashMap<String,DataPack>();
        try {
            socket = new DatagramSocket(null);
            socket.setBroadcast(true);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(localPort));
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
        mSendThread.putMsg(msg,ip, DATA_PORT);
    }
    public void putMsg(byte[] msg,int port){
       mSendThread.putMsg(msg, ip, port);
    }

    public void putMsg(byte[] msg,String ip){
        mSendThread.putMsg(msg, ip, DATA_PORT);
    }


    public void run() {
        if (socket == null) {
            return;
        }

        try {
            byte[] data = new byte[1024];
            DatagramPacket revPacket = new DatagramPacket(data, data.length);
            while (receive&&!socket.isClosed()) {
                socket.receive(revPacket);
                String fromIp=revPacket.getAddress().getHostAddress();
                int fromPort=revPacket.getPort();
                Log.i("godlee","receiveLength:"+revPacket.getLength());
                if(broadcast||ip.equals(fromIp)){
                    if (null != handler) {
                        byte[] realData = new byte[revPacket.getLength()];
                        System.arraycopy(data, 0, realData, 0, revPacket.getLength());
                        Log.i("godlee",Tool.bytesToHexString(realData));
                        onReceive(handler,new DataPack(fromIp,fromPort,realData));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("godlee", e.getMessage());
//            e.printStackTrace();
            socket.close();
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
//        String fromIp=revPacket.reflushDeviceIp();
//        int fromPort=revPacket.getPort();
//        byte[] realData =revPacket.getData();
//        Log.i("godlee", "from:" + fromIp + ":" + fromPort + ".length(byts):"+revPacket.getLength()+"  "+ Tool.bytesToHexString(realData));
        return revPacket;

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
                Log.i("godlee", "DATA_PORT------------------->" + DATA_PORT);
                DatagramPacket sendPacket = new DatagramPacket(msg, msg.length,
                        InetAddress.getByName(ip), DATA_PORT);
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
                Log.i("godlee", "DATA_PORT------------------->" + DATA_PORT);
                Log.i("godlee","sendContent: "+Tool.bytesToHexString(pkg.getData()));
//                Log.i("godlee","sendToip":pkg.)
//                DatagramPacket sendPacket = new DatagramPacket(msg, msg.length,
//                        InetAddress.getByName(ip), DATA_PORT);
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
                Log.i("godlee","sendContent: "+msg );
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
                Log.i("godlee", "DATA_PORT------------------->" + port);
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
    public void sendATMsg(String str,String ip,int port){
        byte[] data=str.getBytes();
        sendMsg(data,ip,port);
    }

    public void setReceive(boolean receive) {
        this.receive = receive;
    }


    class SendThread extends Thread {
        private final Queue<DatagramPacket> sendMsgQueue = new LinkedList<DatagramPacket>();
        // 是否发送消息
        private boolean send = true;

       public synchronized  void  putMsg(byte[] msg,String ip,int port){
            try{
                DatagramPacket sDatagramPacket=new DatagramPacket(msg,msg.length,InetAddress.getByName(ip),port);
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

                while (send) {
//                    if (sendMsgQueue.size() > 0) {
                        synchronized (sendMsgQueue) {
                            DatagramPacket msg = sendMsgQueue.poll();
                            if(null!=msg){
                                UdpController.this.sendMsg(msg);
                            }
//                        Log.i("godlee", "sendMmsg:");
                            try {
                                sleep(25);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
//                    }
//                    try {
//                        wait();
//                    } catch (InterruptedException e) {
//                        Log.e("godlee", e.getMessage());
//                    }
                }

        }

    }
}
