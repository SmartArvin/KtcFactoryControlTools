package com.ktc.utils;


import android.content.Context;

/**
*TODO KTC第三方开放sdk
*@author Arvin
*@Time 2018-1-9 上午11:00:10
*/
public class KtcOpenSdk {
	
	private static KtcOpenSdk mKtcHotelSdk;
	private static Context mContext = null;
	
	private static KtcShellUtil mKtcShellUtil ;
	private static KtcSystemUtil mKtcSystemUtil ;
	private static KtcNetworkUtil mKtcNetworkUtil ;
	private static KtcTvUtil mKtcTvUtil ;
	private static KtcKeyUtil mKtcKeyUtil ;
	private static KtcFactoryUtil mKtcFactoryUtil ;
	
	public static KtcOpenSdk getInstance(Context context) {
		mContext = context;
    	if (mKtcHotelSdk == null) {
    		mKtcHotelSdk = new KtcOpenSdk();
    	}
    	return mKtcHotelSdk;
    }
	
	/**
	 * TODO 获取KtcShellUtil实例对象
	 * @param null
	 * @return KtcShellUtil
	 */
	public KtcShellUtil getKtcShellUtil(){
		if(mKtcShellUtil == null){
			mKtcShellUtil = KtcShellUtil.getInstance();
		}
		return mKtcShellUtil ;
	}
	
	/**
	 * TODO 获取KtcSystemUtil实例对象
	 * @param null
	 * @return KtcSystemUtil
	 */
	public KtcSystemUtil getKtcSystemUtil(){
		if(mKtcSystemUtil == null){
			mKtcSystemUtil = KtcSystemUtil.getInstance(mContext);
		}
		return mKtcSystemUtil ;
	}
	
	/**
	 * TODO 获取KtcNetworkUtil实例对象
	 * @param null
	 * @return KtcNetworkUtil
	 */
	public KtcNetworkUtil getKtcNetworkUtil(){
		if(mKtcNetworkUtil == null){
			mKtcNetworkUtil = KtcNetworkUtil.getInstance(mContext);
		}
		return mKtcNetworkUtil ;
	}
	
	/**
	 * TODO 获取KtcChannelUtil实例对象
	 * @param null
	 * @return KtcChannelUtil
	 */
	public KtcTvUtil getKtcTvUtil(){
		if(mKtcTvUtil == null){
			mKtcTvUtil = KtcTvUtil.getInstance(mContext);
		}
		return mKtcTvUtil ;
	}
	
	/**
	 * TODO 获取KtcFactoryUtil实例对象
	 * @param null
	 * @return KtcFactoryUtil
	 */
	public KtcFactoryUtil getKtcFactoryUtil(){
		if(mKtcFactoryUtil == null){
			mKtcFactoryUtil = KtcFactoryUtil.getInstance(mContext);
		}
		return mKtcFactoryUtil ;
	}
	
	/**
	 * TODO 获取KtcKeyUtil实例对象
	 * @param null
	 * @return KtcKeyUtil
	 */
	public KtcKeyUtil getKtcKeyUtil(){
		if(mKtcKeyUtil == null){
			mKtcKeyUtil = KtcKeyUtil.getInstance();
		}
		return mKtcKeyUtil ;
	}
	
}
