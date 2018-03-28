package com.ktc.control.home;

import com.ktc.utils.KtcSystemUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 *
 * TODO 开机广播
 *
 * @author Arvin
 * 2018-3-11
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "SerialBootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
        	Log.i(TAG, "---Intent.ACTION_BOOT_COMPLETED---");
        	if(KtcSystemUtil.getInstance(context).isSerialPortOpen()){
        		Log.i(TAG, "---startService---SerialConsoleService---");
        		context.startService(new Intent("com.ktc.action.SerialConsoleService"));
        	}
        }

    }

}
