package com.ktc.control.home;

import com.ktc.utils.KtcOpenSdk;

import android.util.Log;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

/**
*
* TODO KTC串行通信/WLAN通信集成工具
*
* @author Arvin
* 2018-3-11
*/
public class MyApplication extends android.app.Application {

	private static MyApplication instance;
	private static SerialPortFinder mSerialPortFinder = null;
	private static SerialPort mSerialPort = null;
	
	public static KtcOpenSdk mKtcOpenSdk ;
	
	@Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        /*CatchCrashHandler mCrashHandler = CatchCrashHandler.getInstance();
        mCrashHandler.init(getApplicationContext());*/
    }
	
	 /**
     * @return singleton instance.
     */
    public static MyApplication getInstance() {
    	if(instance == null){
    		instance = new MyApplication();
    	}
        return instance;
    }
    

	/**
	 * TODO 获取SerialPort单例对象
	 * @param String mDevicePath , int mBaudrate
	 * @return SerialPort
	 */
	public SerialPort getSerialPort() throws Exception {
		if (mSerialPort == null) {
			/* Open the serial port */
			mSerialPort = new SerialPort();
		}
		return mSerialPort;
	}
	
	/**
	 * TODO 获取SerialPortFinder单例对象
	 * @param 
	 * @return SerialPortFinder
	 */
	public SerialPortFinder getSerialPortFinder() {
		if (mSerialPortFinder == null) {
			mSerialPortFinder = new SerialPortFinder();
		}
		return mSerialPortFinder;
	}
	
	/**
     * TODO 获取KtcOpenSdk实例对象
     * @param null
     * @return KtcOpenSdk
     */
    public KtcOpenSdk getKtcOpenSdk(){
    	if(mKtcOpenSdk == null){
    		mKtcOpenSdk = KtcOpenSdk.getInstance(this);
    	}
		return mKtcOpenSdk;
    }
	
	/**
	 * TODO 关闭串口通信
	 * @param null
	 * @return void
	 */
	public void closeSerialPort() {
		if (mSerialPort != null) {
			Log.i("Serial__", "------mSerialPort.close()------");
			mSerialPort.close();
			mSerialPort = null;
		}
	}
}
