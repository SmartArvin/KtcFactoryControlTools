package android_serialport_api;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.util.Log;

public class SerialPort {

	private static final String TAG = "SerialPort";

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	
	private static SerialPort mSerialPort ;
	public SerialPort getInstance(){
		if(mSerialPort == null){
			mSerialPort = new SerialPort();
		}
		return mSerialPort ;
	}
	
	/**
	 * TODO 初始化串口配置并打开串口
	 * @param File device, int baudrate, int flags
	 * @return void
	 */
	public void initSerialPort(File device, int baudrate, int flags) throws Exception {
		Log.i(TAG, "----initSerialPort---:  "+"device: "+device.getAbsolutePath()+"\nbaudrate:  "+baudrate+"\nflags:  "+flags);
		if (!device.canRead() || !device.canWrite()) {
			try {
				Process su;
				su = Runtime.getRuntime().exec("/system/xbin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		Log.i(TAG, "---mFd---  "+mFd);
		if (mFd == null) {
			Log.e(TAG, "---native open returns null---");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	/**
	 * TODO 获取串口输入流
	 * @param null
	 * @return InputStream
	 */
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	/**
	 * TODO 获取串口输出流
	 * @param null
	 * @return OutputStream
	 */
	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate, int flags);
	public native void close();
	static {
		System.loadLibrary("serial_port");
	}
}
