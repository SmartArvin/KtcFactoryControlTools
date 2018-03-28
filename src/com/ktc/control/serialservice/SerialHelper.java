package com.ktc.control.serialservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.Log;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

import com.ktc.control.home.MyApplication;
import com.ktc.utils.KtcHexUtil;

/**
*
* TODO 串口操作工具类
*
* @author Arvin
* 2018-3-11
*/
public abstract class SerialHelper{
	private static final String TAG = "SerialHelper";
	private static final int MIN_INTERVAL = 50;//50ms
	private static final int MAX_INTERVAL = 300;//300ms
	
	private MyApplication mMyApplication;
	private SerialPortFinder mSerialPortFinder;
	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	
	/**
	 * TODO 初始化SerialHelper串口工具类
	 * @param null
	 * @return void
	 */
	public void initSerialHelper(){
		mMyApplication = MyApplication.getInstance();
		mSerialPortFinder = mMyApplication.getSerialPortFinder();
		try {
			mSerialPort = mMyApplication.getSerialPort();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO 获取SerialPort单例对象
	 * @param 
	 * @return SerialPort
	 */
	public SerialPort getSerialPort(){
		if(mSerialPort == null){
			try {
				if(mMyApplication == null){
					mMyApplication = MyApplication.getInstance();
				}
				mSerialPort = mMyApplication.getSerialPort();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mSerialPort;
	}
	
	/**
	 * TODO 获取SerialPortFinder单例对象
	 * @param 
	 * @return SerialPortFinder
	 */
	public SerialPortFinder getSerialPortFinder(){
		if(mSerialPortFinder == null){
			try {
				if(mMyApplication == null){
					mMyApplication = MyApplication.getInstance();
				}
				mSerialPortFinder = mMyApplication.getSerialPortFinder();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mSerialPortFinder;
	}
	
	/**
	 * TODO 初始化SerialPort端口配置
	 * @param File device, int baudrate, int flags
	 * @return void
	 */
	public void initSerialPortConfig(File device, int baudrate, int flags) throws Exception{
		if(mSerialPort != null){
			Log.i(TAG, "---initSerialPortConfig---");
			mSerialPort.initSerialPort(device, baudrate, flags);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();
			
			mReadThread = new ReadThread();
			mReadThread.start();
		}
	}
	
	/**
	 * TODO 以String形式发送指令到PC端
	 * @param String
	 * @return boolean
	 */
	public boolean sendStrCmd(String cmdHex) {
		Log.i(TAG, "---sendStrCmd---");
		String mCSHex = getCmdCS(cmdHex) ;
		Log.i(TAG, "---mCSHex---:  "+mCSHex);
		
		return sendBufferCmd(KtcHexUtil.hexStr2ByteArray(cmdHex + mCSHex));
	}
	
	/**
	 * TODO 以byte[]形式发送指令到PC端
	 * @param byte[]
	 * @return boolean
	 */
	public boolean sendBufferCmd(byte[] mBuffer) {
		Log.i(TAG, "---sendBufferCmd---");
		boolean result = true;
		try {
			if (mOutputStream != null) {
				mOutputStream.write(mBuffer);
			} else {
				result = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		}finally{
			try {
				if (mOutputStream != null) {
					mOutputStream.flush();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
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
	protected abstract void onDataReceived(final byte[] buffer, final int size);
	protected abstract void onDataReceived(final String HexMsg);
	
	/**
	 * TODO 开启子线程实时监听串口数据，以字符串方式读取串口数据
	 * @author Arvin
	 * 2018-3-10
	 */
	private class ReadThread extends Thread {

		@Override
		public void run() {
			StringBuffer dataReceived = new StringBuffer();
	        while (!isInterrupted()) {
	            int size;
	            try {
	                byte[] buffer = new byte[512];
	                if (mInputStream == null) continue;
	                size = mInputStream.read(buffer);
	                String msg = KtcHexUtil.byte2HexStr(buffer , size);
	                Log.i(TAG, "msg:  "+msg);
	                if (size > 0) {
	                    if (msg.substring(0, 2).equals("C0")) {
	                    	dataReceived = new StringBuffer();
                        	dataReceived.append(msg);
                        	Log.i(TAG, "dataReceived_0:  "+dataReceived.toString());
                        	onDataReceived(dataReceived.toString().toUpperCase());
	                    } else {
	                        if (dataReceived.length() > 0) {
	                        	dataReceived.append(msg);
	                        	Log.i(TAG, "dataReceived_1:  "+dataReceived.toString());
	                        	onDataReceived(dataReceived.toString().toUpperCase());
	                        } else {
	                            dataReceived = new StringBuffer();
	                            Log.i(TAG, "dataReceived_2:  "+dataReceived.length());
	                        }
	                    }
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
        }
	}
	
	/**
	 * TODO 开启子线程实时监听串口数据，以字节类型读取串口数据
	 * @author Arvin
	 * 2018-3-19
	 */
	private class ReadThread_byte extends Thread {

		@Override
		public void run() {
			List<Byte> dataReceived = new ArrayList<Byte>();
	        while (!isInterrupted()) {
	            int size;
	            try {
	                byte[] buffer = new byte[16];
	                if (mInputStream == null) continue;
	                size = mInputStream.read(buffer);
	                if (size > 0) {
	                    if (buffer[0] == 0xC0) {
	                        for (int i = 0; i < size; i++) {
                                 //数据还没结束，存入集合中
                                dataReceived.add(buffer[i]);
	                        }
	                        onDataReceived(listTobyte(dataReceived), dataReceived.size());
	                    } else {
	                        if (dataReceived.size() > 0) {
	                            for (int i = 0; i < size; i++) {
                                   //数据还没结束，存入集合中
                                   dataReceived.add(buffer[i]);
	                            }
	                            onDataReceived(listTobyte(dataReceived), dataReceived.size());
	                        } else {
	                            dataReceived.clear();
	                            continue;
	                        }
	                    }
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
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
	
	/**
	 * TODO 关闭串口通信
	 * @param null
	 * @return void
	 */
	public void closeSerialPort(){
		Log.i(TAG, "---closeSerialPort---");
		try {
			if (mReadThread != null){
				mReadThread.interrupt();
			}
			mMyApplication.closeSerialPort();
			mSerialPort = null;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}