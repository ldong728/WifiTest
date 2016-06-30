package com.gooduo.wifitest;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author 济南有人物联�?    刘金�?
 */
public class SearchSSID extends Thread {
	private Handler handler;
	private DatagramSocket socket;
	private SendThread mSendThread;

	
	private final String IP = "255.255.255.255";
//	private final String IP = "192.168.1.1";
	private int PORT = 26000;
	
	/**
	 * 48899端口：C32x系列的端口，用户可以用AT指令更改
     * 49000端口：除C32x系列，其他WIFI模块的端�?
     * 1902端口：有人掌控宝系列产品的端�?
	 */
	private int targetPort = 48899 ;
	
	private boolean receive = true;
	
	public SearchSSID(Handler handler) {
		mSendThread=new SendThread();
		this.handler = handler;
		init();
	}

	public void init(){
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
	public void sendThreadStart(){
		mSendThread.start();
	}
	public void putMsg(byte[] msg){
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
				Log.i("godlee","wait");
				socket.receive(revPacket);
				if(null!=handler){
					byte[] realData = new byte[revPacket.getLength()];
					System.arraycopy(data, 0, realData,0, realData.length);
//					decodeData(realData);
					Message msg =handler.obtainMessage(Tool.REC_DATA,realData);

					handler.sendMessage(msg);
					decodeData(realData);
				}
				Log.i("godlee","receive");
			}
		} catch (Exception e) {
			Log.e("godlee",e.getMessage());
			e.printStackTrace();
			socket.close();
		}
	}
	private void decodeData(byte[] data) {
		if ((data[0] & 0xff) != 0xff)// 如果接收到的数据不是0xff开头,那么丢弃
			return;
		switch (data[3] & 0xff) {
			case 0x81:// 解析返回列表指令
				ArrayList<Item> ssids = Tool.decode_81_data(data);
				if (ssids.size() != 0) {
					for(Item ssid: ssids){
						Log.i("godlee",ssid.getName()+"---"+ssid.getDbm());
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

	public void close() {
		if (socket == null)
			return;
		socket.close();
		receive=false;
		mSendThread.set(false);
	}

	
	private void sendErrorMsg(String info){
		
	}
	
	/**
	 * 发�?�数�?
	 * @param msg
	 */
	public void sendMsg(byte[] msg) {
		if (socket != null) {
			try {
				Log.i("godlee","targetPort------------------->"+targetPort);
				DatagramPacket sendPacket = new DatagramPacket(msg, msg.length,
						InetAddress.getByName(IP), targetPort);
				socket.send(sendPacket);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println("发�?�失�?");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("发�?�失�?");
			}

		}
	}
	
	public void setReceive(boolean receive) {
		this.receive = receive;
	}
	
	public void setTargetPort(int targetPort) {
		this.targetPort = targetPort;
	}
	class SendThread extends Thread{
		private Queue<byte[]> sendMsgQueue = new LinkedList<byte[]>();
		// 是否发送消息
		private boolean send = true;

		//        private SearchSSID ss;
//        public SendThread(){
//
//        }
		public synchronized void putMsg(byte[] msg){
			if(0== sendMsgQueue.size()) {
				notify();
				Log.i("godlee","put msg");
				sendMsgQueue.offer(msg);
			}
		}
		public void set(boolean flag){
			send=flag;
		}

		@Override
		public void run(){
			synchronized (this){
				while(send) {
					while (sendMsgQueue.size()>0) {
						byte[] msg = sendMsgQueue.poll();
						SearchSSID.this.sendMsg(msg);
						Log.i("godlee","sendMmsg:");
					}
					try{
						wait();
					}catch(InterruptedException e){
						Log.e("godlee",e.getMessage());
					}
				}
			}
		}

	}
}
