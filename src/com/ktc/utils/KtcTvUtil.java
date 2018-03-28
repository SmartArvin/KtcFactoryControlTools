package com.ktc.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mstar.android.tv.TvChannelManager;
import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tv.TvPictureManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.common.vo.ProgramInfo;

/**
*TODO KTC频道设置等
*@author Arvin
*@Time 2018-1-9 上午11:00:10
*/
/**
 *
 * TODO 
 *
 * @author Arvin
 * 2018-3-5
 */
public class KtcTvUtil {
	
	private static final String TAG = "KtcChannelUtil";
	private static KtcTvUtil mKtcTvUtil;
	private static KtcShellUtil mKtcShellUtil ;
	private static KtcSystemUtil mKtcSystemUtil ;
	private static KtcLogerUtil mKtcLogerUtil ;
	private static KtcDataUtil mKtcDataUtil;
	private static Context mContext = null;
	
	/**
	 * TODO 获取KtcChannelUtil实例对象
	 * @param Context
	 * @return KtcChannelUtil
	 */
	public static KtcTvUtil getInstance(Context context) {
		mContext = context;
    	if (mKtcTvUtil == null) {
    		mKtcTvUtil = new KtcTvUtil();
    	}
    	return mKtcTvUtil;
    }
	
	
	/**
	 * TODO 获取KtcShellUtil实例对象
	 * @param null
	 * @return KtcShellUtil
	 */
	private static KtcShellUtil getKtcShellUtil() {
    	if (mKtcShellUtil == null) {
    		mKtcShellUtil =  KtcShellUtil.getInstance();
    	}
    	return mKtcShellUtil;
    }
	
	/**
	 * TODO 获取KtcSystemUtil实例对象
	 * @param null
	 * @return KtcSystemUtil
	 */
	private static KtcSystemUtil getKtcSystemUtil() {
    	if (mKtcSystemUtil == null) {
    		mKtcSystemUtil = KtcSystemUtil.getInstance(mContext);
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
	
	public static KtcDataUtil getKtcDataUtil() {
    	if (mKtcDataUtil == null) {
    		mKtcDataUtil = KtcDataUtil.getInstance(mContext);
    	}
    	return mKtcDataUtil;
    }
	
	/**
	 * TODO 根据频道号获取对应ProgramInfo
	 * @param int proNum , ArrayList<ProgramInfo>
	 * @return ProgramInfo
	 */
	public ProgramInfo getProgramByNumber(int proNum , ArrayList<ProgramInfo> proLists) {
		if(proLists != null && proLists.size() > 0){
			for(ProgramInfo mProgramInfo : proLists){
				if(mProgramInfo.number == proNum){
					getKtcLogerUtil().I(TAG, "getProgramByNumber:  "+mProgramInfo.number);
					return mProgramInfo ;
				}
			}
		}
		return null ;
	}
	
	/**
	 * TODO 获取对应信源的频道列表
	 * @param int sourceType
	 * @return ArrayList<ProgramInfo>
	 */
	public ArrayList<ProgramInfo> getProgramListByType(int sourceType){
	    int mServiceNum = 0;
		ProgramInfo pgi = null;
		ArrayList<ProgramInfo> mProInfoList_DTV = new ArrayList<ProgramInfo>();
		ArrayList<ProgramInfo> mProInfoList_ATV = new ArrayList<ProgramInfo>();
		
		
		mServiceNum = TvChannelManager.getInstance().getProgramCount(TvChannelManager.PROGRAM_COUNT_ATV_DTV);
		for (int k = 0; k < mServiceNum; k++) {
            pgi = TvChannelManager.getInstance().getProgramInfoByIndex(k);
            if (pgi != null) {
                // Show All Programs
                if ((pgi.isDelete == true) || (pgi.isVisible == false)) {
                    continue;
                } else {
                	ProgramInfoObject pfo = new ProgramInfoObject();
                    if (pgi.serviceType == TvChannelManager.SERVICE_TYPE_ATV) {
                        pfo.setChannelId(String.valueOf(getATVDisplayChNum(pgi.number)));
                    } else if (pgi.serviceType == TvChannelManager.SERVICE_TYPE_DTV){
                        pfo.setChannelId(String.valueOf(pgi.number));
                    }
                    
                    pfo.setChannelName(pgi.serviceName);
                    pfo.setServiceType(pgi.serviceType);
                    pfo.setSkipImg(pgi.isSkip);
                    pfo.setSslImg(pgi.isScramble);
                    pfo.setFrequenry(pgi.frequency);
                    
                    if (pgi.serviceType == TvChannelManager.SERVICE_TYPE_ATV) {
                    	getKtcLogerUtil().I(TAG, "getProgramListByType——ATV:  "+pgi.serviceName);
                    	mProInfoList_ATV.add(pgi);
                    } else if (pgi.serviceType == TvChannelManager.SERVICE_TYPE_DTV){
                    	getKtcLogerUtil().I(TAG, "getProgramListByType——DTV:  "+pgi.serviceName);
                    	mProInfoList_DTV.add(pgi);
                    }
                }
            }
        }
		
		if (sourceType == TvCommonManager.INPUT_SOURCE_ATV) {
			return mProInfoList_ATV;
		} else if(sourceType == TvCommonManager.INPUT_SOURCE_DTV){
			return mProInfoList_DTV;
		}
		
		return null ;
	}
	
	private class ProgramInfoObject {
        private String channelId = null;
        private String channelName = null;
        private short serviceType;
        private boolean skipImg = false;
        private boolean sslImg = false;
        private int frequenry = 0;
        private int indexOfProgInfoList = 0;

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        public String getChannelName() {
            return channelName;
        }

        public void setChannelName(String channelName) {
            this.channelName = channelName;
        }

        public short getServiceType() {
            return serviceType;
        }

        public void setServiceType(short type) {
            this.serviceType = type;
        }

        public boolean isSkipImg() {
            return skipImg;
        }

        public void setSkipImg(boolean skipImg) {
            this.skipImg = skipImg;
        }

        public boolean isSslImg() {
            return sslImg;
        }

        public void setSslImg(boolean sslImg) {
            this.sslImg = sslImg;
        }

        public void setFrequenry(int f) {
            frequenry = f;
        }

        public int getFrequenry() {
            return frequenry;
        }

        public void setProgInfoListIdx(int idx) {
            indexOfProgInfoList = idx;
        }

        public int getProgInfoListIdx() {
            return indexOfProgInfoList;
        }
    }
	
	
	/**
	 * TODO 复制TV频道表至USB
	 * @param null
	 * @return boolean
	 */
	public boolean cloneProgramsTvToUSB() {
		int i = 0;
		String usbPath = getKtcSystemUtil().getFirstUsbPath();
		
		File USBFile_DTMB[] = new File[2];
		File TVFile_DTMB[] = new File[2];
		
		USBFile_DTMB[0] = new File(usbPath+"atv_cmdb.bin");
		USBFile_DTMB[1] = new File(usbPath+"dtv_cmdb_0.bin");
		
		TVFile_DTMB[0] = new File("/tvdatabase/Database/atv_cmdb.bin");
		TVFile_DTMB[1] = new File("/tvdatabase/Database/dtv_cmdb_0.bin");

		for (i = 0; i < 2; i++) {
			getKtcShellUtil().cp(TVFile_DTMB[i].getAbsolutePath() , USBFile_DTMB[i].getAbsolutePath());
			getKtcShellUtil().sync();
		}

		for (i = 0; i < 2; i++) {
			if (!USBFile_DTMB[i].exists()) {
				getKtcLogerUtil().I(TAG, "cloneProgramsTvToUSB:  file clone fail!!!");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * TODO 克隆USB中频道表至TV
	 * @param null
	 * @return boolean
	 */
	public boolean cloneProgramsUSBToTv() {
		int i = 0;
		String usbPath = getKtcSystemUtil().getFirstUsbPath();
		
		File USBFile_DTMB[] = new File[2];
		File TVFile_DTMB[] = new File[2];
		
		USBFile_DTMB[0] = new File(usbPath+"atv_cmdb.bin");
		USBFile_DTMB[1] = new File(usbPath+"dtv_cmdb_0.bin");
		
		TVFile_DTMB[0] = new File("/tvdatabase/Database/atv_cmdb.bin");
		TVFile_DTMB[1] = new File("/tvdatabase/Database/dtv_cmdb_0.bin");


		for (i = 0; i < 2; i++) {
			if (!USBFile_DTMB[i].exists()) {
				getKtcLogerUtil().I(TAG, "cloneProgramsUSBToTv:  file not exit!!!");
				return false;
			}
		}
		try {
			for (i = 0; i < 2; i++) {
				FileInputStream fis = new FileInputStream(USBFile_DTMB[i]);
				FileOutputStream fos = new FileOutputStream(TVFile_DTMB[i]);
				byte[] bt = new byte[fis.available()];
				fis.read(bt);
				fos.write(bt);
				fos.close();
				fis.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			getKtcLogerUtil().I(TAG, "cloneProgramsUSBToTv:  file clone fail!!!");
			return false;
		}
		TvCommonManager.getInstance().rebootSystem("reboot");
	
		return true;
	}
	
	/**
	 * TODO 根据频道号及信源类型切换频道
	 * @param int proNum , int sourceType
	 * @return boolean
	 */
	public boolean selectProgram(int proNum , int sourceType){
		if(sourceType == TvCommonManager.INPUT_SOURCE_ATV){
			TvCommonManager.getInstance().setInputSource(TvCommonManager.INPUT_SOURCE_ATV);
			return selectProgram(proNum);
		}else if(sourceType == TvCommonManager.INPUT_SOURCE_DTV){
			TvCommonManager.getInstance().setInputSource(TvCommonManager.INPUT_SOURCE_DTV);
			return selectProgram(proNum);
		}else{
			getKtcLogerUtil().I(TAG, "selectProgram:  Program Type Illegal!");
			return false;
		}
	}
	
	private boolean selectProgram(int proNum) {
		
		TvCommonManager mTvCommonManager = TvCommonManager.getInstance();
		TvChannelManager mTvChannelManager = TvChannelManager.getInstance();
		
		boolean isSuccess = false;
		int mProgramType = getProgramType(proNum);
		int mInputSource = mTvCommonManager.getCurrentTvInputSource();
		
		if (mProgramType >= 0) {
			if (mInputSource == TvCommonManager.INPUT_SOURCE_ATV) {
				isSuccess = mTvChannelManager.selectProgram((proNum - 1) , TvChannelManager.SERVICE_TYPE_ATV);
			} else if (mInputSource == TvCommonManager.INPUT_SOURCE_DTV) {
				switch (mProgramType) {
				case TvChannelManager.SERVICE_TYPE_DTV:
					isSuccess = mTvChannelManager.selectProgram(proNum , TvChannelManager.SERVICE_TYPE_DTV);
					break;
				case TvChannelManager.SERVICE_TYPE_RADIO:
					isSuccess = mTvChannelManager.selectProgram(proNum , TvChannelManager.SERVICE_TYPE_RADIO);
					break;
				case TvChannelManager.SERVICE_TYPE_DATA:
					isSuccess = mTvChannelManager.selectProgram(proNum , TvChannelManager.SERVICE_TYPE_DATA);
					break;
				}
			}
		}
		getKtcLogerUtil().I(TAG, "selectProgram:  "+isSuccess);
		return isSuccess;
	}
	
	private int getProgramType(int proNum) {
		
		TvCommonManager mTvCommonManager = TvCommonManager.getInstance();
		
		int mProgramType = -1;
		ArrayList<ProgramInfo> mProgramList = getAllProgramList();
		int curInputSrc = mTvCommonManager.getCurrentTvInputSource();
		
		if (TvCommonManager.INPUT_SOURCE_ATV == curInputSrc) {
			for (ProgramInfo mAtvPro : mProgramList) {
				if ((proNum - 1) == mAtvPro.number && mAtvPro.serviceType == TvChannelManager.SERVICE_TYPE_ATV) {
					mProgramType = TvChannelManager.SERVICE_TYPE_ATV;
					break;
				}
			}
		} else if (TvCommonManager.INPUT_SOURCE_DTV == curInputSrc) {
			for (ProgramInfo mDtvPro : mProgramList) {
				if (proNum == mDtvPro.number) {
					mProgramType = TvChannelManager.SERVICE_TYPE_DTV;
					break;
				}
			}
			
			for (ProgramInfo mRadioPro : mProgramList) {
				if (proNum == mRadioPro.number) {
					if (mRadioPro.serviceType == TvChannelManager.SERVICE_TYPE_RADIO) {
						mProgramType = TvChannelManager.SERVICE_TYPE_RADIO;
						break;
					}
				}
			}

			for (ProgramInfo mDataPro : mProgramList) {
				if (proNum == mDataPro.number) {
					if (mDataPro.serviceType == TvChannelManager.SERVICE_TYPE_DATA) {
						mProgramType = TvChannelManager.SERVICE_TYPE_DATA;
						break;
					}
				}
			}
		}
		getKtcLogerUtil().I(TAG, "getProgramType:  "+mProgramType);
		return mProgramType;
	}
	
	private ArrayList<ProgramInfo> getAllProgramList() {
		
		TvChannelManager mTvChannelManager = TvChannelManager.getInstance();

		ArrayList<ProgramInfo> mProgramNumbers = new ArrayList<ProgramInfo>();
		int m_nServiceNum = mTvChannelManager.getProgramCount(TvChannelManager.PROGRAM_COUNT_ATV_DTV);
		
		for (int i = 0; i < m_nServiceNum; i++) {
			ProgramInfo mProgramInfo = (ProgramInfo) mTvChannelManager.getProgramInfoByIndex(i);
			if (mProgramInfo != null) {
				if (mProgramInfo.isDelete == true) {
					continue;
				} else {
					getKtcLogerUtil().I(TAG, "getAllProgramList:  "+mProgramInfo.serviceName);
					mProgramNumbers.add(mProgramInfo);
				}
			}
		}
		
		return mProgramNumbers;
	}
	
	
	/**
	 * TODO 切换至指定信源
	 * @param final int sourceType
	 * @return void
	 */
	public void changeTvSource(final int sourceType){
		final TvCommonManager mTvCommonManager = TvCommonManager.getInstance() ;
		final TvChannelManager tvChannelManager = TvChannelManager.getInstance();
		switch (sourceType) {
		case TvCommonManager.INPUT_SOURCE_ATV:
		case TvCommonManager.INPUT_SOURCE_DTV:
		case TvCommonManager.INPUT_SOURCE_CVBS:
		case TvCommonManager.INPUT_SOURCE_YPBPR:
		case TvCommonManager.INPUT_SOURCE_HDMI:
		case TvCommonManager.INPUT_SOURCE_HDMI2:
		case TvCommonManager.INPUT_SOURCE_HDMI3:
		case TvCommonManager.INPUT_SOURCE_VGA:
			if(sourceType == TvCommonManager.INPUT_SOURCE_DTV && !getKtcSystemUtil().hasDTMB()){
				return ;
			}
			
	        if (mTvCommonManager.getCurrentTvInputSource() >= TvCommonManager.INPUT_SOURCE_STORAGE) {
	            Intent source_switch_from_storage = new Intent("source.switch.from.storage");
	            mContext.sendBroadcast(source_switch_from_storage);
	            executePreviousTask(sourceType);
	        } else {
	        	new Thread(new Runnable() {

		            @SuppressWarnings("deprecation")
					@Override
		            public void run() {
		                try {
		                    Thread.sleep(1000);
		                } catch (InterruptedException e) {
		                    e.printStackTrace();
		                }
		                if (sourceType == TvCommonManager.INPUT_SOURCE_ATV){
		                	mTvCommonManager.setInputSource(sourceType);
		                    int curChannelNumber = tvChannelManager.getCurrentChannelNumber();
		                    if(curChannelNumber > 0xFF){
		                        curChannelNumber = 0;
		                    }
		                    tvChannelManager.setAtvChannel(curChannelNumber);
		                }else if (sourceType == TvCommonManager.INPUT_SOURCE_DTV){
		                	mTvCommonManager.setInputSource(sourceType);
		                	tvChannelManager.playDtvCurrentProgram();
		                }else{
		                	mTvCommonManager.setInputSource(sourceType);
		                }
		                Intent intent = new Intent("com.mstar.tv.tvplayer.ui.intent.action.SOURCE_INFO");
						intent.putExtra("task_tag", "input_source_changed");
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						mContext.startActivity(intent);
						Log.i(TAG, "getCurrentTvInputSource:   "+mTvCommonManager.getCurrentTvInputSource());
		            }
		        }).start();
	        }
	        
			break;
		case TvCommonManager.INPUT_SOURCE_STORAGE:
			TvCommonManager.getInstance().setInputSource(TvCommonManager.INPUT_SOURCE_STORAGE);
			break;

		default:
			break;
		}
	}
	
    private void executePreviousTask(final int inputSource) {
    	getKtcLogerUtil().I(TAG, "executePreviousTask:  "+inputSource);
    	
    	final Intent mIntent = new Intent("com.mstar.android.intent.action.START_TV_PLAYER");
    	mIntent.putExtra("task_tag", "input_source_changed");
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (inputSource == TvCommonManager.INPUT_SOURCE_ATV){
                	TvCommonManager.getInstance().setInputSource(inputSource);
                    int curChannelNumber = TvChannelManager.getInstance().getCurrentChannelNumber();
                    if (curChannelNumber > 0xFF) {
                        curChannelNumber = 0;
                    }
                    TvChannelManager.getInstance().setAtvChannel(curChannelNumber);
                }else if (inputSource == TvCommonManager.INPUT_SOURCE_DTV){
                	TvCommonManager.getInstance().setInputSource(inputSource);
                	TvChannelManager.getInstance().playDtvCurrentProgram();
                }else{
                	TvCommonManager.getInstance().setInputSource(inputSource);
                }

                try {
                    if (mIntent != null)
                        mContext.startActivity(mIntent);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }).start();
    }
    
	private int getATVDisplayChNum(int chNo) {
        int num = chNo;
        if (TvCommonManager.getInstance().isSupportModule(TvCommonManager.MODULE_ATV_PAL_ENABLE)) {
            num += 1;
        } else if (TvCommonManager.getInstance().isSupportModule(TvCommonManager.MODULE_ATV_NTSC_ENABLE)) {
            if (TvCommonManager.getInstance().getCurrentTvSystem() == TvCommonManager.TV_SYSTEM_ISDB) {
                num += 1;
            }
        }
        getKtcLogerUtil().I(TAG, ""+num);
        return num;
    }

	
	/**
	 * TODO 获取当前系统信源通道类型
	 * @param null
	 * @return int
	 */
	public int getCurrentInputSource(){
		return TvCommonManager.getInstance().getCurrentTvInputSource();
	}
	
	/**
	 * TODO 获取当前信源的Edid版本
	 * @param null
	 * @return int
	 */
	public int getHdmiEdidVersion(){
		int mCurrentVersion = TvCommonManager.getInstance().getHdmiEdidVersion();
        return mCurrentVersion;
	}
	
	
	/**
	 * TODO 设置当前信源的Edid版本
	 * @param int
	 * @return void
	 */
	public void setHdmiEdidVersion(int version){
		TvCommonManager.getInstance().setHdmiEdidVersion(version);
	}
	
	/**
     * TODO 获取HDMI1对应EDID
     * @param null
     * @return byte[]
     */
    public byte[] getHDMI1_Edid_byte() throws IOException {  
    	String filePath = "/tvconfig/config/EDID_BIN/HDMI1_EDID.txt" ;
    	return readFile2ByteByPath(filePath); 
    } 
    
    public String getHDMI1_Edid_str() throws IOException {  
    	String filePath = "/tvconfig/config/EDID_BIN/HDMI1_EDID.txt" ;
    	return readFile2StrByPath(filePath); 
    } 
    
    /**
     * TODO 获取HDMI2对应EDID
     * @param null
     * @return byte[]
     */
    public byte[] getHDMI2_Edid_byte() throws IOException {  
    	String filePath = "/tvconfig/config/EDID_BIN/HDMI2_EDID.txt" ;
    	return readFile2ByteByPath(filePath); 
    }
    
    public String getHDMI2_Edid_str() throws IOException {  
    	String filePath = "/tvconfig/config/EDID_BIN/HDMI2_EDID.txt" ;
    	return readFile2StrByPath(filePath); 
    }
	
	
	/**
     * TODO 获取1.4对应HDCP key
     * @param null
     * @return byte[]
     */
    public byte[] getHDCPKey_1_4() throws IOException {  
    	String filePath = "/tvconfig/config/certificate/hdcp_key.bin" ;
    	return readFile2ByteByPath(filePath);   
    }  
    
    /**
     * TODO 获取2.0对应HDCP key
     * @param null
     * @return String
     */
    public byte[] getHDCPKey_2_0() throws IOException {  
    	String filePath = "/tvconfig/config/certificate/hdcp2_key.bin" ;
    	return readFile2ByteByPath(filePath);  
    }  
    
    /**
     * TODO 设置文件权限
     * @param 
     * @return void
     */
    private static void chmodFile(File destFile) {
	    try {
	         String command = "chmod 644 " + destFile.getAbsolutePath();
	         Runtime runtime = Runtime.getRuntime();
	          Process proc = runtime.exec(command);
	      } catch (IOException e) {
	             e.printStackTrace();
        }
    }
    
    /**
     * TODO 根据路径读取文件内容以byte[]显示
     * @param String filePath
     * @return byte[]
     */
    private byte[] readFile2ByteByPath(String filePath) throws IOException {  
		chmodFile(new File(filePath));
        InputStream in = null;  
        BufferedInputStream buffer = null;  
        DataInputStream dataIn = null;  
        ByteArrayOutputStream bos = null;  
        DataOutputStream dos = null;  
        byte[] bArray = null;  
        try {  
            in = new FileInputStream(filePath);  
            buffer = new BufferedInputStream(in);  
            dataIn = new DataInputStream(buffer);  
            bos = new ByteArrayOutputStream();  
            dos = new DataOutputStream(bos);  
            byte[] buf = new byte[1024];  
            while (true) {  
                int len = dataIn.read(buf);  
                if (len < 0)  
                    break;  
                dos.write(buf, 0, len);  
            }  
            bArray = bos.toByteArray();  
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
            return null;  
  
        } finally {  
  
            if (in != null)  
                in.close();  
            if (dataIn != null)  
                dataIn.close();  
            if (buffer != null)  
                buffer.close();  
            if (bos != null)  
                bos.close();  
            if (dos != null)  
                dos.close();  
        }  
        return bArray;  
    }
    
    /**
     * TODO 根据路径读取文件内容以String显示
     * @param String filePath
     * @return String
     */
    private String readFile2StrByPath(String filePath) throws IOException {  
		chmodFile(new File(filePath));
        InputStream in = null;  
        BufferedInputStream buffer = null;  
        DataInputStream dataIn = null;  
        ByteArrayOutputStream bos = null;  
        DataOutputStream dos = null;  
        String bStr = null;  
        try {  
            in = new FileInputStream(filePath);  
            buffer = new BufferedInputStream(in);  
            dataIn = new DataInputStream(buffer);  
            bos = new ByteArrayOutputStream();  
            dos = new DataOutputStream(bos);  
            byte[] buf = new byte[1024];  
            while (true) {  
                int len = dataIn.read(buf);  
                if (len < 0)  
                    break;  
                dos.write(buf, 0, len);  
            }  
            bStr = bos.toString().replace(" ", "");  
  
            Log.i("Serial", "bos:  "+bStr);
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
            return null;  
  
        } finally {  
  
            if (in != null)  
                in.close();  
            if (dataIn != null)  
                dataIn.close();  
            if (buffer != null)  
                buffer.close();  
            if (bos != null)  
                bos.close();  
            if (dos != null)  
                dos.close();  
        }  
        return bStr;  
    }
    
    /**
     * TODO 切换图像模式
     * @param int
     * @return boolean
     */
    public boolean changePictureMode(int mPictureMode){
    	//Standard/Lightness/Soft/User
        /*picModes = new int[]{
	    		TvPictureManager.PICTURE_MODE_NORMAL , TvPictureManager.PICTURE_MODE_DYNAMIC ,
	    		TvPictureManager.PICTURE_MODE_SOFT , TvPictureManager.PICTURE_MODE_USER
	        };*/
    	int mArcType = TvPictureManager.VIDEO_ARC_MAX;
    	boolean isSuccess = false ;
        if (TvPictureManager.getInstance() != null) {
        	isSuccess = TvPictureManager.getInstance().setPictureMode(mPictureMode);
            mArcType = TvPictureManager.getInstance().getVideoArcType();
            if (mArcType >= TvPictureManager.VIDEO_ARC_MAX) {
                return isSuccess;
            }

            int mCurrentInputSrc = TvCommonManager.getInstance().getCurrentTvInputSource();
            if ((mPictureMode == TvPictureManager.PICTURE_MODE_GAME
                    || mPictureMode == TvPictureManager.PICTURE_MODE_AUTO
                    || mPictureMode == TvPictureManager.PICTURE_MODE_PC
                    || mPictureMode == TvPictureManager.PICTURE_MODE_GAME
                    || mPictureMode == TvPictureManager.PICTURE_MODE_AUTO
                    || mPictureMode == TvPictureManager.PICTURE_MODE_PC)
                    && (mCurrentInputSrc != TvCommonManager.INPUT_SOURCE_VGA)
                    && (mCurrentInputSrc != TvCommonManager.INPUT_SOURCE_VGA2)
                    && (mCurrentInputSrc != TvCommonManager.INPUT_SOURCE_VGA3)
                    && (mCurrentInputSrc != TvCommonManager.INPUT_SOURCE_HDMI)
                    && (mCurrentInputSrc != TvCommonManager.INPUT_SOURCE_HDMI2)
                    && (mCurrentInputSrc != TvCommonManager.INPUT_SOURCE_HDMI3)
                    && (mCurrentInputSrc != TvCommonManager.INPUT_SOURCE_HDMI4)) {
                TvPictureManager.getInstance().setVideoArcType(mArcType);
            }
        }
        return isSuccess;
    }
	
    /**
     * TODO 获取当前图像模式
     * @param null
     * @return int
     */
    public int getPictureMode(){
    	return TvPictureManager.getInstance().getPictureMode();
    }
    
    /**
     * TODO 设置图像色温
     * @param int
     * @return void
     */
    public void setPictureColorTemperature(int mColorTemp){
    	int[] colorModes = new int[]{//Cold/Nature/Warm
    			TvPictureManager.COLOR_TEMP_COOL ,
    			TvPictureManager.COLOR_TEMP_NATURE ,
    			TvPictureManager.COLOR_TEMP_WARM
    	};
    	int mPicColor = -1 ;
    	for(int i = 0 ; i < colorModes.length ; i++){
    		if(mColorTemp == colorModes[i]){
    			mPicColor = mColorTemp ;
    		}
    	}
    	if(mPicColor != -1){
    		TvPictureManager.getInstance().setColorTempratureIdx(mColorTemp);	
    	}
    }
    
    /**
	 * TODO 设置背光
	 * @param short
	 * @return void
	 */
	public void setBackLight(int u8EnergyEfficiencyBacklightmax) {
		getKtcDataUtil().updateDatabase_systemsetting("u8EnergyEfficiencyBacklight_Max", u8EnergyEfficiencyBacklightmax);
		try {
			TvManager.getInstance().setTvosCommonCommand("SetEnergyEfficiencyImprove");
		} catch (TvCommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO 设置Contrast
	 * @param int
	 * @return void
	 */
	public void setPicContrast(int picContrast){
        TvPictureManager.getInstance().setVideoItem(
        		TvPictureManager.PICTURE_CONTRAST,
        		picContrast);
	}
	
	/**
	 * TODO 设置Brightness
	 * @param int
	 * @return void
	 */
	public void setPicBrightness(int picBrightness){
		TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_BRIGHTNESS,
                picBrightness);
	}
}

