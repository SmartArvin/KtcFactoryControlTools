package com.ktc.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.util.Log;

/**
*TODO KTC执行shell命令接口
*@author Arvin
*@Time 2018-1-10 上午09:02:10
*/
public class KtcShellUtil {
	
	private static KtcShellUtil mKtcShellUtil ;
	private static final String TAG = "KtcShellUtil";
	private static final String CHMOD = "/system/bin/busybox chmod 777";
	private static final String CP = "/system/bin/busybox cp -f";
	private static final String RM = "/system/bin/busybox rm -rf";
	private static final String SYNC = "/system/bin/busybox sync";

	public static KtcShellUtil getInstance() {
    	if (mKtcShellUtil == null) {
    		mKtcShellUtil = new KtcShellUtil();
    	}
    	return mKtcShellUtil;
    }
	
	/**
	 * TODO 执行指定shell命令
	 * @param String cmdStr
	 * @return void
	 */
	public void exec(String cmdStr){
		String s;
		Process process;
		try {
			process = Runtime.getRuntime().exec(cmdStr);
			BufferedReader buff = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((s = buff.readLine()) != null) {
				Log.i(TAG , "exec:  "+s);
				process.waitFor();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO 执行chmod命令
	 * @param String path
	 * @return void
	 */
	public void chmod(String path){
		String cmd = CHMOD + " " + path;
		String s;
		Process process;
		try {
			process = Runtime.getRuntime().exec(cmd);
			BufferedReader buff = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((s = buff.readLine()) != null) {
				Log.i(TAG , "chmod:  "+s);
				process.waitFor();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * TODO 执行rm命令
	 * @param String path
	 * @return void
	 */
	public static void rm(String path){
		String cmd = RM + " " + "-r" + " " + path;
		String s;
		Process process;
		try {
			process = Runtime.getRuntime().exec(cmd);
			BufferedReader buff = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((s = buff.readLine()) != null) {
				Log.i(TAG , "rm:  "+s);
				process.waitFor();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * TODO 执行cp命令
	 * @param String sourcePath, String destPath
	 * @return void
	 */
	public static void cp(String sourcePath, String destPath){
		String cmd = CP + " " + sourcePath + " " + destPath;
		String s;
		Process process;
		try {
			process = Runtime.getRuntime().exec(cmd);
			BufferedReader buff = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((s = buff.readLine()) != null) {
				Log.i(TAG , "cp:  "+s);
				process.waitFor();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO 执行sync同步命令
	 * @param null
	 * @return void
	 */
	public static void sync(){
		String cmd = SYNC;
		String s;
		Process process;
		try {
			process = Runtime.getRuntime().exec(cmd);
			BufferedReader buff = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((s = buff.readLine()) != null) {
				Log.i(TAG , "sync:  "+s);
				process.waitFor();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
