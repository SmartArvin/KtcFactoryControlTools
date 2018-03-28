package com.ktc.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.mstar.android.ethernet.EthernetDevInfo;
import com.mstar.android.ethernet.EthernetManager;

/**
*TODO KTC网络相关接口
*@author Arvin
*@Time 2018-1-9 下午15:44:12
*/
/**
 *
 * TODO 
 *
 * @author Arvin
 * 2018-3-5
 */
public class KtcNetworkUtil {
	
	private final String TAG = "KtcNetworkUtil";
	private static KtcNetworkUtil mKtcNetworkUtil;
	private static Context mContext = null;
	
	private WifiManager mWifiManager ;
	private EthernetManager mEthernetManager ;
	
	public static final int SECURE_OPEN = 0;
    public static final int SECURE_WEP = 1;
    public static final int SECURE_PSK = 2;
    public static final int SECURE_EAP = 3;
	
	public static KtcNetworkUtil getInstance(Context context) {
		mContext = context;
    	if (mKtcNetworkUtil == null) {
    		mKtcNetworkUtil = new KtcNetworkUtil();
    	}
    	return mKtcNetworkUtil;
    }
	
	/**
	 * TODO 判断网络是否已连接
	 * @param null
	 * @return boolean
	 */
	public boolean isNetWorkConnected(){
		ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable()) {
            	return true ;
        }else {
        	return false ;
        }
	}
	
	/**
	 * TODO 获取当前已连接的网络类型
	 * @param null
	 * @return int
	 */
	public int getNetConnectType() {
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable()) {
           return networkInfo.getType() ;
        }else {
          return ConnectivityManager.TYPE_NONE ;
        }
    }
	
	private EthernetManager getEthernetManager() {
        if (mEthernetManager == null) {
            mEthernetManager = EthernetManager.getInstance();
        }

        return mEthernetManager;
    }
	
	/**
	 * TODO 判断有线网络是否为自动连接（HDCP）
	 * @param null
	 * @return boolean
	 */
	public boolean isEthernetAutoIP() {
		EthernetDevInfo mEthInfo = getEthernetManager().getSavedConfig();
		if (null != mEthInfo
				&& mEthInfo.getConnectMode().equals(
						EthernetDevInfo.ETHERNET_CONN_MODE_DHCP)) {
			return true;
		}
		return false;
	}
	
	/**
	 * TODO 判断有线网络是否为静态IP连接
	 * @param null
	 * @return boolean
	 */
	public boolean isEthernetStaticIP() {
		EthernetDevInfo mEthInfo = getEthernetManager().getSavedConfig();
		if (null != mEthInfo
				&& mEthInfo.getConnectMode().equals(EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL)) {
			return true;
		}

		return false;
	}
	
    /**
     * TODO 开/关有线网络
     * @param boolean toEnable
     * @return void
     */
    public void setEthernetEnable(boolean toEnable) {
    	if(toEnable){
    		if(!isEthernetEnable()){
    			getEthernetManager().setEnabled(toEnable);
    		}
    	}else{
    		if (isEthernetEnable()) {
            	getEthernetManager().setEnabled(toEnable);
            }
    	}
    }
    
    
    /**
     * TODO 判断有线网络是否打开
     * @param null
     * @return boolean
     */
    public boolean isEthernetEnable() {
    	 EthernetManager ethernet = getEthernetManager();
         if (EthernetManager.ETHERNET_STATE_ENABLED == ethernet.getState()) {
             return true;
         }
         return false;
    }

    /**
     * TODO 判断有线网络是否已连接
     * @param null
     * @return boolean
     */
    public boolean isEthernetConnected(){
    	if(isEthernetEnable() && isNetInterfaceAvailable("eth0")){
    		return true ;
    	}
    	return false ;
    }
    
    private boolean isNetInterfaceAvailable(String ifName) {
        String netInterfaceStatusFile = "/sys/class/net/" + ifName + "/carrier";
        return isStatusAvailable(netInterfaceStatusFile);
    }

    private boolean isStatusAvailable(String statusFile) {
        char st = readStatus(statusFile);
        if (st == '1') {
            return true;
        }
        return false;
    }

    private synchronized char readStatus(String filePath) {
        int tempChar = 0;
        File file = new File(filePath);
        if (file.exists()) {
            Reader reader = null;
            try {
                reader = new InputStreamReader(new FileInputStream(file));
                tempChar = reader.read();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return (char) tempChar;
    }
    
	//#####################################################################################################//
    /**
     * TODO 获得WifiManager实例
     * @param null
     * @return WifiManager
     */
    private WifiManager getWifiManager() {
        if (mWifiManager == null) {
        	mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        }
        return mWifiManager;
    }
	
    /**
     * TODO 开/关无线网络
     * @param boolean toEnable
     * @return void
     */
    public void setWifiEnable(boolean toEnable){
    	if(toEnable){
    		if(!getWifiManager().isWifiEnabled()){
    	    	getWifiManager().setWifiEnabled(toEnable);
    	    }
    	}else{
    		if(getWifiManager().isWifiEnabled()){
    	    	getWifiManager().setWifiEnabled(toEnable);
    	    }
    	}
    }
    
    /**
     * TODO 判断无线网络是否打开
     * @param null
     * @return boolean
     */
    public boolean isWifiEnable() {
        return getWifiManager().isWifiEnabled();
    }
    
    /**
     * TODO 判断无线网络是否已连接
     * @param null
     * @return boolean
     */
    public boolean isWifiConnected() {
        // wifi is disabled
        if (!getWifiManager().isWifiEnabled()) {
            return false;
        }

        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        // wifi have not connected
        WifiInfo info = getWifiManager().getConnectionInfo();
        if (info == null || info.getSSID() == null
                || info.getNetworkId() == WifiConfiguration.INVALID_NETWORK_ID
                || wifi != State.CONNECTED) {
            return false;
        }
        return true;
    }
    
    
    /**
     * TODO 开/关无线热点
     * @param boolean enable
     * @return void
     */
    public void setWifiHotSpotEnable(boolean enable) {
    	final ContentResolver cr = mContext.getContentResolver();
        /**
         * Disable Wifi if enabling tethering
         */
        int wifiState = getWifiManager().getWifiState();
        if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
                    (wifiState == WifiManager.WIFI_STATE_ENABLED))) {
            getWifiManager().setWifiEnabled(false);
            Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
        }

        getWifiManager().setWifiApEnabled(null, enable);

        /**
         *  If needed, restore Wifi on tether disable
         */
        if (!enable) {
            int wifiSavedState = 0;
            try {
                wifiSavedState = Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            if (wifiSavedState == 1) {
            	getWifiManager().setWifiEnabled(true);
                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
            }
        }
    }
    
    /**
     * TODO 判断无线热点是否打开
     * @param null
     * @return boolean
     */
    public boolean isWifiHotspotEnable(){
    	//WIFI_AP_STATE_DISABLING = 10; WIFI_AP_STATE_DISABLED = 11; 
    	//WIFI_AP_STATE_ENABLING = 12; WIFI_AP_STATE_ENABLED = 13;
    	//WIFI_AP_STATE_FAILED = 14;
    	int mWifiApState = getWifiManager().getWifiApState();
    	return mWifiApState == WifiManager.WIFI_AP_STATE_ENABLED || mWifiApState == WifiManager.WIFI_AP_STATE_ENABLING;
    }
    
    /**
     * TODO 创建无线热点（支持无密和加密方式）
     * @param int mKeyMgmt , String mSSID , String mPassword
     * @return boolean
     */
    public boolean createWifiHotSpot(int mKeyMgmt , String mSSID , String mPassword){
    	//NONE = 0;WPA_PSK = 1;WPA_EAP = 2;IEEE8021X = 3;WPA2_PSK = 4;
    	if(mKeyMgmt == KeyMgmt.NONE || mKeyMgmt == KeyMgmt.WPA_EAP 
    			|| mKeyMgmt == KeyMgmt.IEEE8021X || mKeyMgmt == KeyMgmt.WPA2_PSK
    			|| mKeyMgmt == KeyMgmt.WPA_PSK){
    		WifiConfiguration mWifiConfig = getConfig(mKeyMgmt , mSSID , mPassword);
        	if (mWifiConfig != null) {
        		if (getWifiManager().getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
        			getWifiManager().setWifiApEnabled(null, false);
        			return getWifiManager().setWifiApEnabled(mWifiConfig, true);
        		} else {
        			return getWifiManager().setWifiApConfiguration(mWifiConfig);
        		}
        	}
    	}else{
    		Log.i(TAG, "Hotspot Security level illegal!");
    	}
    	return false ;
    }
    	
    private WifiConfiguration getConfig(int mKeyMgmt , String mSSID , String mPassword) {

    	WifiConfiguration config = new WifiConfiguration();
    	config.SSID = mSSID;

    	switch (mKeyMgmt) {
    		case KeyMgmt.NONE:
    			config.allowedKeyManagement.set(KeyMgmt.NONE);
    			return config;

    		case KeyMgmt.WPA_PSK:
    			config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
    			config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
    			if (mPassword.length() >= 8) {
    				String password = mPassword;
    				config.preSharedKey = password;
    			}
    			return config;
    			
    		case KeyMgmt.WPA2_PSK:
    			config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPassword.length() != 0) {
                    String password = mPassword;
                    config.preSharedKey = password;
                }
                return config;
    			
    	}
    	return null;
    }
    
    /**
     * TODO 获取设备固定MAC地址
     * @param null
     * @return String
     */
    public String getDeviceMacAddress() {
        return getEthernetMacAddress() ;
    }

    /**
     * TODO 获取有线网络MAC地址
     * @param null
     * @return String
     */
    public String getEthernetMacAddress() {
        try {
            return readLine("/sys/class/net/eth0/address").toUpperCase();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * TODO 获取WLAN无线MAC地址
     * @param null
     * @return String
     */
    public String getWlanMacAddress() {
        try {
            return readLine("/sys/class/net/wlan0/address").toUpperCase();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }
    
    
    /**
	 * TODO 获取WIFI模组的vid_pid(适用于MTK模组)
	 * @param null
	 * @return int[]
	 */
	public int[] getWifiModule_VidAndPid() {
		final int STD_USB_REQUEST_GET_DESCRIPTOR = 0x06;
	    final int LIBUSB_DT_STRING = 0x03;
		final String PRODUCT_MTK = "MediaTek Inc.";
		final String PRODUCT_Realtek = "Realtek";
	    
	    String usbName = null;
	    int vid , pid;
	    String manufacturer = "";//制造商
        String product = "";//产品
        String serial = "";
        int[] vid_pid = new int[2];
        
        UsbManager mUsbManager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
	    Log.i(TAG, "deviceList == null:  "+(deviceList == null));
	    Log.i(TAG, "deviceList:  "+deviceList.size());
	    if (deviceList.size() == 0) {
	        return vid_pid;
	    }else{
	    	Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
	    	while (deviceIterator.hasNext()) {
				UsbDevice device = (UsbDevice) deviceIterator.next();
		        usbName = device.getDeviceName();
		        vid = device.getVendorId() ;
		        pid = device.getProductId() ;
		        
		        KtcLogerUtil.getInstance().I(TAG, "Name: " + usbName+"\n");
		        KtcLogerUtil.getInstance().I(TAG, "VID: " + Integer.toHexString(vid)+" : "+"PID: " + Integer.toHexString(pid));
	        	vid_pid[0] = vid ;
	        	vid_pid[1] = pid ;
		        break ;//只取第一个设备即wifi模组
			}
	    }
	    return vid_pid;
	}
	/*public int[] getWifiModule_VidAndPid() {
		Log.i(TAG, "XXXXXXXXXXXX  getWifiModule_VidAndPid  XXXXXXXXXXX");
		final int STD_USB_REQUEST_GET_DESCRIPTOR = 0x06;
	    final int LIBUSB_DT_STRING = 0x03;
		final String PRODUCT_MTK = "MediaTek Inc.";
		final String PRODUCT_Realtek = "Realtek";
	    
	    String usbName = null;
	    int vid , pid;
	    String manufacturer = "";//制造商
        String product = "";//产品
        String serial = "";
        int[] vid_pid = new int[2];
        
        UsbManager mUsbManager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
	    Log.i(TAG, "deviceList == null:  "+(deviceList == null));
	    Log.i(TAG, "deviceList:  "+deviceList.size());
	    if (deviceList.size() == 0) {
	        return vid_pid;
	    }else{
	    	Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
	    	while (deviceIterator.hasNext()) {
				UsbDevice device = (UsbDevice) deviceIterator.next();
		        usbName = device.getDeviceName();
		        vid = device.getVendorId() ;
		        pid = device.getProductId() ;
		        
		        KtcLogerUtil.getInstance().I(TAG, "Name: " + usbName+"\n");
		        KtcLogerUtil.getInstance().I(TAG, "VID: " + Integer.toHexString(vid)+" : "+"PID: " + Integer.toHexString(pid));
		        
			    UsbInterface intf = device.getInterface(0);
			    UsbDeviceConnection connection = mUsbManager.openDevice(device);
			    
			    if(null==connection){
			    	KtcLogerUtil.getInstance().I(TAG, "unable to establish connection");
			    } else {
			        connection.claimInterface(intf, true);
			        byte[] rawDescs = connection.getRawDescriptors();
			        try {
			            byte[] buffer = new byte[255];
			            int idxMan = rawDescs[14];
			            int idxPrd = rawDescs[15];
			
			            int rdo = connection.controlTransfer(UsbConstants.USB_DIR_IN
			                    | UsbConstants.USB_TYPE_STANDARD, STD_USB_REQUEST_GET_DESCRIPTOR,
			                    (LIBUSB_DT_STRING << 8) | idxMan, 0, buffer, 0xFF, 0);
			            manufacturer = new String(buffer, 2, rdo - 2, "UTF-16LE");
			            product = new String(buffer, 2, rdo - 2, "UTF-16LE");
			            serial = connection.getSerial() ;
			        } catch (UnsupportedEncodingException e){
			        	e.printStackTrace();
			        }
			
			        KtcLogerUtil.getInstance().I(TAG, "Manufacturer:" + manufacturer + "\n");
			        KtcLogerUtil.getInstance().I(TAG, "Product:" + product + "\n");
			        KtcLogerUtil.getInstance().I(TAG, "Serial:" + serial + "\n");
			        if(PRODUCT_MTK.equals(manufacturer)){
			        	vid_pid[0] = vid ;
			        	vid_pid[1] = pid ;
			        	break;
			        }
			    }
			}
	    }
	    return vid_pid;
	}*/
	
	
	/**
	 * TODO 根据SSID连接开放网络
	 * @param ScanResult mScanResult
	 * @return void
	 */
	public boolean connectOpenWifi(String mSSID , String mIp){
		ScanResult mScanResult = getScanResultBySSID(mSSID, mIp);
		if(mScanResult == null){
			return false ;
		}
		WifiConfiguration config = getOpenWifiConfig(mScanResult);
        if (config == null) {
        	return false ;
        } else {
            LinkProperties linkProperties = new LinkProperties();
            // init linkProperties
            config.linkProperties = new LinkProperties(linkProperties);

            // connect to ssid
            getWifiManager().connect(config, null);
            
            return true ;
        }
	}
	
	/**
	 * TODO 根据SSID和IP获取ScanResult
	 * @param String mSSID , String mIp
	 * @return ScanResult
	 */
	private ScanResult getScanResultBySSID(String mSSID , String mIp){
        final List<ScanResult> results = getWifiManager().getScanResults();
        if (results != null) {
            for (ScanResult tmpResult : results) {
                if (TextUtils.isEmpty(tmpResult.SSID) || tmpResult.capabilities.contains("[IBSS]")) {
                    continue;
                }
                if(mSSID.equals(tmpResult.SSID)){
                	return tmpResult ;
                }
            }
        }
        return null;
	}
	
	/**
	 * TODO 根据ScanResult封装指定的WifiConfiguration
	 * @param ScanResult
	 * @return WifiConfiguration
	 */
	private WifiConfiguration getOpenWifiConfig(ScanResult mScanResult) {
        WifiConfiguration mConfig = new WifiConfiguration();
        mConfig.SSID = convertToQuotedString(mScanResult.SSID);
        if(getSecurity(mScanResult) != SECURE_OPEN){
        	return null;
        }else{
        	mConfig.ipAssignment = IpAssignment.DHCP;
        	mConfig.allowedKeyManagement.set(KeyMgmt.NONE);
        }
        return mConfig;
    }
	
	/**
	 * TODO 获取ScanResult安全类型
	 * @param ScanResult
	 * @return int
	 */
	private int getSecurity(ScanResult result) {
        Log.d(TAG, "ScanResult.capabilities, " + result.capabilities);
        if (result.capabilities.contains("WEP")) {
            return SECURE_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURE_PSK;
        }

        return SECURE_OPEN;
    }
	
	private String convertToQuotedString(String string) {
	   return "\"" + string + "\"";
	}
	
}
