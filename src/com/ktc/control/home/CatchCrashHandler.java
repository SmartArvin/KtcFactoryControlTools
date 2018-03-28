package com.ktc.control.home;

import java.lang.Thread.UncaughtExceptionHandler;
import android.content.Context;
import android.util.Log;

/**
 *
 * TODO 捕获应用crash异常
 *
 * @author Arvin
 * 2018-3-15
 */
public class CatchCrashHandler implements UncaughtExceptionHandler{
    private static String TAG = "SerialCatchCrashHandler";
    private static CatchCrashHandler mCrashHandler;
    //系统默认的UncaughtException处理类
    private UncaughtExceptionHandler mDefaultHandler;
    //程序的Context对象
    private Context mContext;

    /**
     * TODO 获取CatchCrashHandler单例对象
     * @param 
     * @return CatchCrashHandler
     */
    public static CatchCrashHandler getInstance() {
        if (mCrashHandler == null) {
            mCrashHandler = new CatchCrashHandler();
        }
        return mCrashHandler;
    }
    
    public void init(Context context) {
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
    	Log.i(TAG, "uncaughtException");
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * TODO 异常处理，比如收集日志等
     * @param 
     * @return boolean
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        Throwable cause = ex.getCause(); 
        if(cause != null){
        	Log.e(TAG , cause.toString());
        	cause.printStackTrace();
        }
        return true;
    }
    
}