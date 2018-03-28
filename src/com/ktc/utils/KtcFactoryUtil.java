package com.ktc.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.ktc.utils.factory.FactoryDB;
import com.ktc.utils.factory.FactoryDeskImpl;
import com.ktc.utils.factory.IFactoryDesk;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.factory.FactoryManager;


/**
*TODO KTC工厂相关功能实现
*@author Arvin
*@Time 2018-1-10 上午09:02:10
*/
public class KtcFactoryUtil {
	
	private static final String TAG = "KtcFactoryUtil";
	private static KtcFactoryUtil mKtcFactoryUtil ;
	private static KtcLogerUtil mKtcLogerUtil ;
	private static KtcDataUtil mKtcDataUtil ;
	private static Context mContext = null;
	
	private final int gainDisplayDivideWB=8;//display:256=2048/8,step=8
	private final int offsetDisplayDivideWB=2;//display:1024=2048/2,step=4
	
	private static IFactoryDesk factoryManager;
	
	public static KtcFactoryUtil getInstance(Context context) {
		mContext = context;
    	if (mKtcFactoryUtil == null) {
    		mKtcFactoryUtil = new KtcFactoryUtil();
    	}
    	
    	if(factoryManager == null){
    		factoryManager = FactoryDeskImpl.getInstance(mContext);
    	}
    	
    	FactoryDB.getInstance(mContext).openDB();
    	factoryManager.loadEssentialDataFromDB();
    	return mKtcFactoryUtil;
    }
	
	/**
	 * TODO 获取KtcDataUtil实例对象
	 * @param null
	 * @return KtcDataUtil
	 */
	private static KtcDataUtil getKtcDataUtil() {
    	if (mKtcDataUtil == null) {
    		mKtcDataUtil = KtcDataUtil.getInstance(mContext);
    	}
    	return mKtcDataUtil;
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
	
	public void factoryReset(){
		new factoryResetTask().execute();
	}

	class factoryResetTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			FactoryRestordefalut();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}
	}
	
	 private void FactoryRestordefalut() {
        restoreFactoryAtvProgramTable((short) 0);
    	restoreToDefault();
    }
	 
	 private void restoreFactoryAtvProgramTable(short cityIndex) {
		getKtcLogerUtil().I(TAG, "----restoreFactoryAtvProgramTable----");
		try {
			FactoryManager fm = TvManager.getInstance().getFactoryManager();
			fm.restoreFactoryAtvProgramTable(cityIndex);
		} catch (TvCommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 
	 private boolean restoreToDefault() {

			boolean ret = true;
			boolean result = false;
			try {
				TvManager.getInstance().setTvosCommonCommand(
						"SetFactoryResetStatus");
			} catch (TvCommonException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			File srcFile = new File("/tvdatabase/DatabaseBackup/",
					"user_setting.db");
			File destFile = new File("/tvdatabase/Database/", "user_setting.db");
			result = copyFile(srcFile, destFile);
			result = false;
			if (result == false) {
				ret = false;
			}

			srcFile = new File("/tvdatabase/DatabaseBackup/", "factory.db");
			destFile = new File("/tvdatabase/Database/", "factory.db");
			result = copyFile(srcFile, destFile);
			result = false;

			try {
				Runtime.getRuntime().exec("sync");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				TvManager.getInstance().setTvosCommonCommand(
						"RestoreFactoryResetStatus");
			} catch (TvCommonException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (result == false) {
				ret = false;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mContext.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
			getKtcLogerUtil().I(TAG, "restoreToDefault:  "+ret);
			return ret;

		}
	 
	private boolean hasDtmb(){
		boolean hasDtmb = false;
		Properties props = new Properties();
		InputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream("/system/build.prop"));
			props.load(in);
			String value = props.getProperty("ktc.dtmb");
			hasDtmb = value != null && value.equals("true");
		} catch (Exception e) {
			hasDtmb = false;		
		}
		getKtcLogerUtil().I(TAG, "hasDtmb:  "+hasDtmb);
		return hasDtmb;
	}
	 

    /**
     * Copy data from a source stream to destFile.
     * Return true if succeed, return false if failed.
     */
    private  boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    Log.d(" out.write(buffer, 0, bytesRead);", " out.write(buffer, 0, bytesRead);");
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (IOException e) {
                }
                out.close();
            }
            return true;
        } catch (IOException e) {
            Log.d("copyToFile(InputStream inputStream, File destFile)", e.getMessage());
            return false;
        }
    }
    
    // copy a file from srcFile to destFile, return true if succeed, return
    // false if fail
    private  boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally  {
                in.close();
            }
        } catch (IOException e) {
            Log.d("copyFile(File srcFile, File destFile)", e.getMessage());
            result = false;
        }
        chmodFile(destFile);
        return result;
    }
    
    private void chmodFile(File destFile){
        try {
            String command = "chmod 666 " + destFile.getAbsolutePath();
            getKtcLogerUtil().I(TAG, "chmodFile:  "+command);
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(command);
           } catch (IOException e) {
            getKtcLogerUtil().I(TAG, "chmodFile:  chmod fail!!!!");
            e.printStackTrace();
           }
    }
    
    /***********for W/B Adjust***********/
    /**
     * TODO 设置R_Gain
     * @param int 
     * @return void
     */
    public void setWB_R_Gain(int rgainvalWB){
		if(rgainvalWB > 2047){
			rgainvalWB=2047;
		}else if(rgainvalWB < 0){
			rgainvalWB = 0 ;
		}
		factoryManager.setWbRedGain((short) rgainvalWB);
    }
    public int getWB_R_Gain(){
		return factoryManager.getWbRedGain();
    }
    
    
    /**
     * TODO 设置G_Gain
     * @param int 
     * @return void
     */
    public void setWB_G_Gain(int ggainvalWB){
		if(ggainvalWB > 2047){
			ggainvalWB = 2047;
		}else if(ggainvalWB < 0){
			ggainvalWB = 0 ;
		}
		factoryManager.setWbGreenGain((short) ggainvalWB);
    }
    public int getWB_G_Gain(){
		return factoryManager.getWbGreenGain();
    }
    
    
    /**
     * TODO 设置B_Gain
     * @param int 
     * @return void
     */
    public void setWB_B_Gain(int bgainvalWB){
		if(bgainvalWB > 2047){
			bgainvalWB = 2047;
		}else if(bgainvalWB < 0){
			bgainvalWB = 0 ;
		}
		factoryManager.setWbBlueGain((short) bgainvalWB);
    }
    public int getWB_B_Gain(){
		return factoryManager.getWbBlueGain();
    }
    
    
    /**
     * TODO 设置R_Offset
     * @param int 
     * @return void
     */
    public void setWB_R_Offset(int roffsetvalWB){
    	if(roffsetvalWB > 2047){
			roffsetvalWB = 2047;
		}else if(roffsetvalWB < 0){
			roffsetvalWB = 0 ;
		}
		factoryManager.setWbRedOffset((short) roffsetvalWB);
    }
    public int getWB_R_Offset(){
		return factoryManager.getWbRedOffset();
    }
    
    
    /**
     * TODO 设置G_Offset
     * @param int 
     * @return void
     */
    public void setWB_G_Offset(int goffsetvalWB){
    	if(goffsetvalWB > 2047){
    		goffsetvalWB = 2047;
		}else if(goffsetvalWB < 0){
			goffsetvalWB = 0 ;
		}
		factoryManager.setWbGreenOffset((short) goffsetvalWB);
    }
    public int getWB_G_Offset(){
		return factoryManager.getWbGreenOffset();
    }
    
    
    /**
     * TODO 设置B_Offset
     * @param int 
     * @return void
     */
    public void setWB_B_Offset(int boffsetvalWB){
    	if(boffsetvalWB > 2047){
    		boffsetvalWB = 2047;
		}else if(boffsetvalWB < 0){
			boffsetvalWB = 0 ;
		}
		factoryManager.setWbBlueOffset((short) boffsetvalWB);
    }
    public int getWB_B_Offset(){
		return factoryManager.getWbBlueOffset();
    }
    
    
    /**
     * TODO 频道预设
     * @param void
     * @return void
     */
    public void presetFactoryChannels() {
		try {
			if (hasDtmb()) {
				TvManager.getInstance().setTvosCommonCommand("SetResetATVDTVChannel");
			} else {
				TvManager.getInstance().setTvosCommonCommand("SetResetATVChannel");
			}
		} catch (TvCommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    /**
	 * TODO 切换老化模式开关
	 * @param 
	 * @return short
	 */
    public void setFactoryAgeMode(boolean toEnable) {
    	int agemodeindex ;
    	int poweronmodeindex ;
		if (toEnable) {
			agemodeindex = 1;
			poweronmodeindex = 2;
			factoryManager.setPowerOnMode(poweronmodeindex);
		} else{
			agemodeindex = 0;
		}
		factoryManager.setAgemode((short) (agemodeindex));
	}	
	
	/**
	 * TODO 获取当前老化模式状态
	 * @param 
	 * @return short
	 */
	public short getAgeMode() {//0:Off ; 1:On
		return factoryManager.getagemode();
	}
	
	/**
	 * TODO 进入工厂调试模式
	 * @param 
	 * @return void
	 */
	public void enterFactoryMode(){
		final short T_SystemSetting_IDX = 0x19;
		int ret = -1;
		ContentValues vals = new ContentValues();
		vals.put("FactoryDebugMode", 1);
		vals.put("bAgeModeFlag", 0);
		vals.put("bEnergyEfficiencyImprove", 0);
		vals.put("u8EnergyEfficiencyBacklight", 0);
		vals.put("u8EnergyEfficiencyBacklight_Max", 100);
		try {
			ret = mContext.getContentResolver().update(
					Uri.parse("content://mstar.tv.usersetting/systemsetting"),
					vals, null, null);
		} catch (SQLException e) {
		}
		if (ret == -1) {
			System.out.println("update tbl_SystemSetting ignored");
		}
		
        try {
			TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(T_SystemSetting_IDX);
		} catch (TvCommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO 设置Pattern
	 * @param short wbPattern
	 * @return void
	 */
	public void setWbPattern(short wbPattern){
		/*String[] testpatternarray =
			{ "Off", "100IRE", "90IRE", "80IRE", "70IRE", "60IRE",
			  "50IRE", "40IRE", "30IRE", "20IRE", "10IRE", "0IRE" };*/
		factoryManager.setWbPattern(wbPattern);
	}
}
