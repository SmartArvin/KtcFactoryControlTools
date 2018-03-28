package com.ktc.control.wlan;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ktc.control.home.MyApplication;
import com.ktc.utils.KtcHexUtil;
import com.mstar.android.ethernet.EthernetDevInfo;
import com.mstar.android.ethernet.EthernetManager;

/**
*
* TODO 串口操作工具类
*
* @author Arvin
* 2018-3-11
*/
public abstract class WlanHelper{
	private static final String TAG = "WlanHelper";
	
	private MyApplication mMyApplication;
	private Context mContext ;
	private UdpReceiveThread mUdpReceiveThread ;
	private UdpSendThread mUdpSendThread;
	private TcpSocketThread mTcpSocketThread ;
	
	private NetTool mNetTool;
	private EthernetManager ethernet;
	private WifiManager wifiMgr;
	private EthernetDevInfo info;
	private WifiInfo wifiInfo;
	private DhcpInfo dhcpInfo;
	
	/**
	 * TODO 初始化WlanHelper局域网通信工具类
	 * @param null
	 * @return void
	 */
	public void initWlanHelper(Context mContext){
		this.mContext = mContext;
		mMyApplication = MyApplication.getInstance();
		mUdpReceiveThread = new UdpReceiveThread();
		mUdpReceiveThread.start();
		mUdpSendThread = new UdpSendThread();
		mUdpSendThread.start();
		mTcpSocketThread = new TcpSocketThread(mContext);
		mTcpSocketThread.start();
		
		initMainInfo();
	}
	
	/**
	 * TODO 获取命令的校验位CheckSum(rule:Checksum(CS) :  0xFF – (amount of all Data exclude checksum byte) & 0xFF)
	 * @param String cmdHex
	 * @return String
	 */
	public String getCmdCS(String cmdHex) {
		if (cmdHex == null || cmdHex.equals("")) {
			return ""; 
		}
		int total = 0;
		int len = cmdHex.length();
		int num = 0;
		while (num < len) {
			String s = cmdHex.substring(num, num + 2);
			total += Integer.parseInt(s, 16);
			num = num + 2; 
		}
		//用256求余最大是255，即16进制的FF
		int mod = total % 256;
		String hex = Integer.toHexString(mod);
		// 如果不够两位校验位的长度，补0
		if(hex.length() < 2){
			hex = "0"+hex ;
		}
		return Integer.toHexString(Integer.parseInt("FF", 16) - Integer.parseInt(hex, 16)).toUpperCase();
	}
	
	/**
	 * TODO 串口接收数据处理
	 * @param final byte[] buffer, final int size
	 * @return void
	 */
	protected abstract void onDataLoopBack(final String HexMsg);
	protected abstract void onDataReceived(final String HexMsg);
	
	/**
	 * TODO 信息回传
	 * @param 
	 * @return void
	 */
	public static void SendMessage(OutputStream out, String re1, String cmd){
		if(re1!=null){
			try {
				out.write(KtcHexUtil.hexStr2ByteArray(re1));
				out.flush();
				Log.i(TAG, "response:" + cmd);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class TcpSocketThread extends Thread{
		private Context context; 
		private ServerSocket serSocket = null;
		private Socket socket = null;
		public TcpSocketThread(Context context) {  
	        this.context = context; 
	        if(context == null){
	        }
	    }
		
		@Override
		public void run() {
			try {
				serSocket = new ServerSocket(Constant.TCPPort);
				//serSocket.setSoTimeout(1000*10);
			} catch (IOException e) {
				e.printStackTrace();
			}

			Log.v("MDA", "TCP Server started, port:" + serSocket.getLocalPort());

			while (!interrupted()) {
				// wait for client
				try {
					socket = serSocket.accept();
					new SocketThread(context, socket).start();
					Log.i(TAG, "Socket建立成功！");
					Log.i(TAG,  "Socket connected");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 *
	 * TODO TV-->PC发送回传数据
	 *
	 * @author Arvin
	 * 2018-3-23
	 */
	private class UdpSendThread extends Thread{

		private static final String TAG = "MDA-UDPsend";
		private DatagramPacket udpPacket;
		private DatagramSocket udpSocket;
		private byte[] data;

		@Override
		public void run() {
			
			data = KtcHexUtil.hexStr2ByteArray(UdpMsg.getMSG());
			while(true){
			try {	
			    //lixq 20160624 start
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//lixq 20160624 end
				if(Constant.Reportflag==true){
					udpSocket = new DatagramSocket();
					udpPacket = new DatagramPacket(data,data.length , InetAddress.getByName(Constant.quest_ip),Constant.Port);
					udpSocket.send(udpPacket);
					Log.i(TAG, "send UDP response, dest port:"+Constant.Port);
				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			}
		}

	}
	
	private class UdpReceiveThread extends Thread{
		private static final String TAG = "MDA-UDPReceive";
		private DatagramSocket udpSocket=null;
		private DatagramPacket udpPacket=null;
		private String codeString;
		private String ip=null;

		@Override
		public void run() {

			byte[] data = new byte[300];
			try {
				udpSocket = new DatagramSocket(Constant.Port);
				udpPacket = new DatagramPacket(data,data.length);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			while (true) {
				try {
					udpSocket.receive(udpPacket);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				ip = udpPacket.getAddress().toString();
				Constant.quest_ip = ip.substring(1,ip.length());
				codeString = KtcHexUtil.byte2HexStr(data);
				Log.i(TAG , "receive:" + Constant.quest_ip + "UDP request...\n");
				Log.i(TAG , "request content:" + codeString + "\n\n");
			}
		}
	}
	
	private class SocketThread extends Thread{
		private Context mContext; 
		private static final String TAG = "MDA-SocketThread";
		private Socket tcpsSocket;
		private OutputStream out;
		private DataInputStream ds;

		public SocketThread(Context context, Socket socket) {
			this.tcpsSocket = socket;
			this.mContext = context;
			if(context == null){
				Log.i(TAG,  "SocketThread>>context is null");
	        }
		}

		public void run() {
			try {
				ds = new DataInputStream(tcpsSocket.getInputStream());
				out = tcpsSocket.getOutputStream();
				byte[] data = new byte[100];
				int i = 0;
				while ((i = ds.read(data)) != -1) {
					String msg = KtcHexUtil.byte2HexStr(data, i);
					if (msg.substring(6, 8).equals(Constant.GETCMD)) {
					Log.i(TAG , 
							"Server Host port:  " + tcpsSocket.getPort() 
							+ "receive MDA Host TCP HEX String:  "+ KtcHexUtil.byte2HexStr(data, i));

						// deal get command, need to return data
						onDataReceived(msg);
					}else if (msg.substring(6, 8).equals(Constant.SETCMD)) {
						// deal set command
						onDataLoopBack(msg);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	
	
    /**
     * TODO 将List<Byte>转换为byte[]
     * @param 
     * @return byte[]
     */
    private static byte[] listTobyte(List<Byte> list) {
        if (list == null || list.size() < 0)
            return null;
        byte[] bytes = new byte[list.size()];
        int i = 0;
        Iterator<Byte> iterator = list.iterator();
        while (iterator.hasNext()) {
            bytes[i] = iterator.next();
            i++;
        }
        return bytes;
    }
    
    public void initMainInfo() {
		mNetTool = new NetTool(mMyApplication);
		String netState = mNetTool.getNetType();
		if (netState != null && netState.equals("Ethernet")) {
			ethernet = mNetTool.getEthernetManager();
			info = ethernet.getSavedConfig();
			
			UdpMsg.setMAC(info.getMacAddress("eth0").replace(":", ""));
			if(info.getIpAddress()!=null){
				UdpMsg.setIp(HexAdress(info.getIpAddress()));
			}else{
				UdpMsg.setIp(HexAdress("0.0.0.0"));
			}
			UdpMsg.setGateway_ip(HexAdress(info.getRouteAddr()));
			UdpMsg.setSubnet_mac(HexAdress(info.getNetMask()));

			if(info.getDnsAddr()==null){
				UdpMsg.setDNS1(HexAdress("0.0.0.0"));
			}else{
				UdpMsg.setDNS1(HexAdress(info.getDnsAddr()));
			}

			if(info.getDns2Addr()==null){
				UdpMsg.setDNS2(HexAdress("0.0.0.0"));
			}else{
				UdpMsg.setDNS2(HexAdress(info.getDns2Addr()));
			}
		}else if (netState != null && netState.equals("Wifi")) {
			wifiMgr = mNetTool.getWifiManager();
			wifiInfo = wifiMgr.getConnectionInfo();
			dhcpInfo = wifiMgr.getDhcpInfo();
			
			UdpMsg.setMAC(wifiInfo.getMacAddress().replace(":", ""));
			UdpMsg.setIp(HexAdress(intToIp(dhcpInfo.ipAddress)));
			UdpMsg.setGateway_ip(HexAdress(intToIp(dhcpInfo.gateway)));
			UdpMsg.setSubnet_mac(HexAdress(intToIp(dhcpInfo.netmask)));
			if(dhcpInfo.dns1 == 0){
				UdpMsg.setDNS1(HexAdress("0.0.0.0"));
			}else{
				UdpMsg.setDNS1(HexAdress(intToIp(dhcpInfo.dns1)));
			}
			if(dhcpInfo.dns2 == 0){
				UdpMsg.setDNS2(HexAdress("0.0.0.0"));
			}else{
				UdpMsg.setDNS2(HexAdress(intToIp(dhcpInfo.dns2)));
			}
		}

	}
	
	private String intToIp(int paramInt) {
		return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
				+ (0xFF & paramInt >> 24);
	}

	private String HexAdress(String adress) {
		String IPadress = KtcHexUtil.byte2HexStr(KtcHexUtil.ipToBytesByReg(adress));
		return IPadress;
	}
	
}