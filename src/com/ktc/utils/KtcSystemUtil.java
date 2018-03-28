package com.ktc.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mstar.android.storage.MStorageManager;
import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.factory.FactoryManager;
import com.mstar.android.tvapi.factory.vo.EnumAcOnPowerOnMode;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.media.AudioManager;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;

/**
*TODO KTC系统信息获取接口
*@author Arvin
*@Time 2018-1-10 上午11:20:33
*/
public class KtcSystemUtil {
	
	private static final String TAG = "KtcSystemUtil";
	private static KtcSystemUtil mKtcSystemUtil;
	private static KtcLogerUtil  mKtcLogerUtil;
	private static Context mContext = null;
	
	public static KtcSystemUtil getInstance(Context context) {
		mContext = context;
    	if (mKtcSystemUtil == null) {
    		mKtcSystemUtil = new KtcSystemUtil();
    	}
    	return mKtcSystemUtil;
    }
	
	/**
	 * TODO 获取KtcLogerUtil实例对象
	 * @param null
	 * @return KtcLogerUtil
	 */
	private static KtcLogerUtil getKtcLogerUtil() {
    	if (mKtcLogerUtil == null) {
    		mKtcLogerUtil = KtcLogerUtil.getInstance();
    	}
    	return mKtcLogerUtil;
    }
	
	/**
	 * TODO 获取设备型号
	 * @param null
	 * @return String
	 */
	public String getProductsModel() {
		try {
			String productMode = getProp("/system/build.prop" , "ro.product.model");
			return productMode;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * TODO 获取设备软件版本号
	 * @param null
	 * @return String
	 */
	public String getProductsVersion() {
		String softVersionx = "";
		softVersionx = getProp("/system/build.prop", "ro.build.version.incremental");
		return softVersionx;
	}
	
	/**
	 * TODO 获取设备支持的SDK版本
	 * @param null
	 * @return String
	 */
	public String getSdkVersion() {
		String mSdkVersion = "";
		mSdkVersion = getProp("/system/build.prop", "ro.build.version.sdk");
		return mSdkVersion;
	}
	
	/**
	 * TODO 获取设备Android版本
	 * @param null
	 * @return String
	 */
	public String getAndroidVersion() {
		String mAndroidVersion = "";
		mAndroidVersion = getProp("/system/build.prop", "ro.build.version.release");
		return mAndroidVersion;
	}
	
	/**
	 * TODO 获取SerialNumber(适用于638Infocus)
	 * @param null
	 * @return String
	 */
	public String getSerialNumber() {
        return getEnvironment("ProductSerialNumber");
    }
	
	/**
	 * TODO 写入SerialNumber(适用于638Infocus)
	 * @param null
	 * @return String
	 */
	public void setSerialNumber(String serial) {
        try {
            TvManager.getInstance().setEnvironment("ProductSerialNumber", serial);
        } catch (TvCommonException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	/**
	 * TODO 重启设备
	 * @param null
	 * @return void
	 */
	public void Reboot() {
		try {
			TvCommonManager.getInstance().rebootSystem("reboot");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO 关机
	 * @param null
	 * @return void
	 */
	public void ShutDown() {
		new Thread() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	/**
	 * TODO 判断是否存在SDcard
	 * @param null
	 * @return boolean
	 */
	public boolean hasSDFile(){
		File mFile = new File("/mnt/sdcard/");
		if(mFile != null && mFile.list().length > 0){
			return true;
		}
		
		return false;
	}
	
   
   /**
	 * TODO 判断是否有usb设备
	 * @param null
	 * @return boolean
	 */
   public boolean hasUsbDisk(){
	   return getFirstUsbPath() == null ? false : true;
   }
   
   /**
	 * TODO 判断是否有usb文件
	 * @param null
	 * @return boolean
	 */
	public boolean hasUsbFiles(){
	    String usbPath = getFirstUsbPath() ;
	    if(usbPath != null && !usbPath.equals("")){
	    	File mFile = new File(usbPath);
			if(mFile != null && mFile.list().length > 0){
				return true;
			}
	    }
	   return false;
    }
	
	/**
	 * TODO 获取设备Memory信息
	 * @param null
	 * @return String(MB)
	 */
	public long getSystemMemory() {
		ActivityManager mActivityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo() ;
		mActivityManager.getMemoryInfo(memoryInfo) ;
		long memSize = memoryInfo.totalMem  ; 

		String availMemStr = Formatter.formatFileSize(mContext , memSize); 
		return memSize/1024/1024 ; 
	}
	
	/**
	 * TODO 获取系统EMMC分区总大小
	 * @param null
	 * @return String(MB)
	 */
	public int getEmmcSize() {
		final String FILENAME_PROC_EMMCINFO = "/proc/partitions";
        try {
            FileReader fr = new FileReader(FILENAME_PROC_EMMCINFO);
            BufferedReader br = new BufferedReader(fr, 2048);
            String Line = "";
            String EmmcSize = "";
            while ((Line = br.readLine()) != null){
                if (Line.length() >7 && Line.lastIndexOf(" ") > 0) {
                    if (Line.substring(Line.lastIndexOf(" ") + 1 , Line.length()).equals("mmcblk0")) {
                        EmmcSize = removeAllSpace(Line);
                        break;
                    }
                }
            }
            br.close();
            if (EmmcSize.length() >7 ){
                EmmcSize = (String) EmmcSize.subSequence(4, EmmcSize.length()-7);
 
                Matcher mer = Pattern.compile("^[0-9]+$").matcher(EmmcSize);
                //如果为正整数就说明数据正确的，确保在Double.parseDouble中不会异常
                if (mer.find()) {
                	Double mem = (Double.parseDouble(EmmcSize)/1024);
                    NumberFormat nf = new DecimalFormat("0.0");
                    mem = Double.parseDouble(nf.format(mem));
                    return (int) Math.ceil(mem);//去掉小数点
                }
            }
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
	
	private String removeAllSpace(String s) {
	  StringTokenizer st = new StringTokenizer(s," ",false);
	  String t="";
	  while (st.hasMoreElements()){
		  t += st.nextElement();
	  }
	  return t;
	}
	
	/**
	 * TODO 获取DDRAM大小
	 * @param 
	 * @return String(MB)
	 */
	public int getDDRAMSize(){//MB
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0 ;
        try{
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader,8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(firstLine != null){
            totalRam = (int)Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return (int)(Float.valueOf(firstLine) / 1024);
    }
	
	/**
	 * TODO 设置系统音量
	 * @param int volume
	 * @return boolean
	 */
	public boolean setStreamVolume(int volume) {
		getKtcLogerUtil().I(TAG, "setStreamVolume:  "+volume);
		if ((volume >= 0) && (volume <= 100)) {
			try {
				AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume,
						AudioManager.FLAG_PLAY_SOUND);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

		} else {
			return false;
		}
	}
	
	/**
	 * TODO 获取当前系统音量
	 * @param null
	 * @return int
	 */
	public int getStreamVolume() {
		try {
			AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
			return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * TODO 设置开机模式
	 * @param int value
	 * @return boolean
	 */
	public boolean setPowerMode(int value) {
		getKtcLogerUtil().I(TAG, "setPowerMode:  "+value);
		
		boolean ret = false;
		EnumAcOnPowerOnMode poweronmode = null;
		poweronmode = EnumAcOnPowerOnMode.values()[value];
		try {
			FactoryManager fm = TvManager.getInstance().getFactoryManager();
			ret = fm.setEnvironmentPowerMode(poweronmode);
		} catch (TvCommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * TODO 获取当前开机启动模式
	 * @param null
	 * @return int
	 */
	public int getPowerMode() {
		int ret = 0;
		try {
			FactoryManager fm = TvManager.getInstance().getFactoryManager();
			ret = fm.getEnvironmentPowerMode().ordinal();
		} catch (TvCommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getKtcLogerUtil().I(TAG, "getPowerMode:  "+ret);
		return ret;
	}
	
	/**
	 * TODO 获取已挂载的第一个U盘路径(eg:/mnt/usb/sda1/)
	 * @param null
	 * @return String
	 */
   public String getFirstUsbPath(){
   	MStorageManager storageManager = MStorageManager.getInstance(mContext);
		String[] volumes = storageManager.getVolumePaths();
		boolean isUsbAsSd = SystemProperties.getBoolean("mstar.usb.as.sdcard", false);
		for(String mVolume : volumes){
			//filter sdcard
			if(!isUsbAsSd && mVolume.equals("/mnt/sdcard")){
				continue ;
			}
			//for usb plug
			File mFile = new File(mVolume);
			if(mFile != null && mFile.list().length > 0){
				getKtcLogerUtil().I(TAG, "getFirstUsbPath:  "+(mVolume.endsWith("/") ? mVolume : mVolume+"/"));
				return mVolume.endsWith("/") ? mVolume : mVolume+"/";
			}
		}
		return null;
   }
	
	/**
	 * TODO 开/关adb调试
	 * @param boolean toEnable
	 * @return void
	 */
	public void setAdbEnable(boolean toEnable) {
		getKtcLogerUtil().I(TAG, "setAdbEnable:  "+toEnable);
		
        SystemProperties.set("persist.service.adb.enable", String.valueOf(toEnable ? 1 : 0));
        SystemProperties.set("persist.sys.ktc.adb.enable", String.valueOf(toEnable ? 1 : 0));
        String cmd = "setprop service.adb.tcp.port 5555";
        String stop = "stop adbd";
        String start = "start adbd";
        try {
            Runtime.getRuntime().exec(cmd);
            if(toEnable){
                Runtime.getRuntime().exec(start);
            }else {
                Runtime.getRuntime().exec(stop);
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
	
	/**
	 * TODO 判断是否已打开adb调试
	 * @param null
	 * @return boolean
	 */
	public boolean isAdbEnable() {
        int mAdbEnable = SystemProperties.getInt("persist.service.adb.enable", 0);
        int mAdbKtcEnable = SystemProperties.getInt("persist.sys.ktc.adb.enable", 0);
        
        getKtcLogerUtil().I(TAG, "isAdbEnable:  "+(mAdbEnable == 1 && mAdbKtcEnable == 1 ? true : false));
        return mAdbEnable == 1 && mAdbKtcEnable == 1 ? true : false ;
    }
	
	private String getProp(String file, String key) {
		String value = "";
		Properties props = new Properties();
		InputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			props.load(in);
			value = props.getProperty(key);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (value != null) {
			String[] array = value.split(";");
			if (array[0].length() > 0) {
				value = array[0];
			}
			value = value.replace("\"", "");
			value = value.trim();
			return value;
		} else {
			return "";
		}
	}
	
	/**
	 * TODO 判断是否支持DTMB
	 * @param null
	 * @return boolean
	 */
	public boolean hasDTMB() {
        Properties props = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream("/system/build.prop"));
            props.load(in);
            String value = props.getProperty("ktc.dtmb");
            boolean tag = false;
            if(value != null && value.equals("true"))
                    tag = true;
            return tag;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
	
	/**
	 * TODO 判断是否为T4C1
	 * @param null
	 * @return boolean
	 */
	public boolean isT4C1(){
		try {
		Properties props = new Properties();
		InputStream in = new BufferedInputStream(new FileInputStream("/system/build.prop"));
		props.load(in);
		String value = props.getProperty("ktc.board.t4c1");
		return (value != null && value.equals("true"));
		}
		catch (Exception e) {
		}
		return false;
	}
	
	/**
	 * TODO 判断是否为T5C1
	 * @param null
	 * @return boolean
	 */
	public boolean isT5c1(){
		try {
		Properties props = new Properties();
		InputStream in = new BufferedInputStream(new FileInputStream("/system/build.prop"));
		props.load(in);
		String value = props.getProperty("ktc.board.t5c1");
		return (value != null && value.equals("true"));
		}
		catch (Exception e) {
		}
		return false;
	}
	
	/**
     * TODO 获取EMMC ID
     * @param null
     * @return String
     */
    public String getEMMC_Id() {
        try {
        	BufferedReader reader = new BufferedReader(new FileReader("/sys/block/mmcblk0/device/cid"), 256);
            return reader.readLine().toUpperCase();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * TODO 读取环境变量
     * @param String
     * @return String
     */
    private String getEnvironment(String name) {
        // TODO Auto-generated method stub
        String str = "";
        try {
            return TvManager.getInstance().getEnvironment(name);
        } catch (TvCommonException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if (name.equals("ProductSerialNumber")) {
                str = "not input product number";
            }
        }
        return str;
    }

    /**
     * TODO 获取工模串口开关状态
     * @param 
     * @return boolean
     */
    public boolean isSerialPortOpen(){
		return SystemProperties.getBoolean("persist.sys.port.open", false);
    }
}
