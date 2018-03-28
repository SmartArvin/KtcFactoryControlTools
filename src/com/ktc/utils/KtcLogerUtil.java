package com.ktc.utils;

import android.util.Log;

/**
*TODO KTC打印log日志
*@author Arvin
*@Time 2018-1-10 上午09:02:10
*/
public class KtcLogerUtil {
	
	private static KtcLogerUtil mKtcLogerUtil ;
	private static final String TAG = "KtcLogerUtil";
	public static boolean enableLog = true ;

	public static KtcLogerUtil getInstance() {
    	if (mKtcLogerUtil == null) {
    		mKtcLogerUtil = new KtcLogerUtil();
    	}
    	return mKtcLogerUtil;
    }
	
	public void setLogerEnable(boolean toEnable){
		enableLog = toEnable ;
	}
	
	/**
	 * TODO i信息
	 * @param String tag , String msg
	 * @return void
	 */
	public void I(String tag , String msg){
		if(enableLog){
			Log.i(tag, msg);
		}
	}
	
	/**
	 * TODO d信息
	 * @param String tag , String msg
	 * @return void
	 */
	public void D(String tag , String msg){
		if(enableLog){
			Log.d(tag, msg);
		}
	}
	
	/**
	 * TODO W信息
	 * @param String tag , String msg
	 * @return void
	 */
	public void W(String tag , String msg){
		if(enableLog){
			Log.w(tag, msg);
		}
	}
	
	/**
	 * TODO E信息
	 * @param String tag , String msg
	 * @return void
	 */
	public void e(String tag , String msg){
		if(enableLog){
			Log.e(tag, msg);
		}
	}
	
}
