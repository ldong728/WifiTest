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
/**
 * @author 济南有人物联�?    刘金�?
 */
public class SearchSSID extends Thread {
	private Handler handler;
	private DatagramSocket socket;

	
	private final String IP = "255.255.255.255";
	private int PORT = 26000;
	
	/**
	 * 48899端口：C32x系列的端口，用户可以用AT指令更改
     * 49000端口：除C32x系列，其他WIFI模块的端�?
     * 1902端口：有人掌控宝系列产品的端�?
	 */
	private int targetPort = 48899 ;
	
	private boolean receive = true;
	
	public SearchSSID(Handler handler) {
		this.handler = handler;
		init();
	}

	public void init(){
		try {
			socket = new DatagramSocket(null);
			socket.setBroadcast(true);
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(PORT));
			Log.i("godlee", "init ok");
		} catch (SocketException e) {
			e.printStackTrace();
			sendErrorMsg("Search Thread init fail");
			return;
		} 
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
					Message msg =handler.obtainMessage(Tool.REC_DATA,realData);
					handler.sendMessage(msg);
				}
				Log.i("godlee","receive");
			}
		} catch (Exception e) {
			Log.e("godlee",e.getMessage());
			e.printStackTrace();
			socket.close();
		}
	}

	public void close() {
		if (socket == null)
			return;
		socket.close();
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
				System.out.println("targetPort------------------->"+targetPort);
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
}
