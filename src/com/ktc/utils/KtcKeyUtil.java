package com.ktc.utils;

import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;

/**
*TODO KTC按键工具类
*@author Arvin
*@Time 2018-1-10 上午11:20:33
*/
public class KtcKeyUtil {
	
	private static final String TAG = "KtcKtcKeyUtil";
	private static KtcKeyUtil mKtcKeyUtil;
	
	public static KtcKeyUtil getInstance() {
    	if (mKtcKeyUtil == null) {
    		mKtcKeyUtil = new KtcKeyUtil();
    	}
    	return mKtcKeyUtil;
    }
	
	/**
	 * TODO 模拟按键发送
	 * @param int
	 * @return void
	 */
	public void sendKeyEvent(final int keyCode) {
		new Thread() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(keyCode);
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}
		}.start();
	}
	
}
