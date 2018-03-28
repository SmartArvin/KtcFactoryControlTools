package com.ktc.control.serialtest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.ktc.control.constants.CMD_TYPE_VALUE2;
import com.ktc.control.constants.Constants;
import com.ktc.control.serialservice.SerialHelper;
import com.ktc.controltools.R;
import com.ktc.utils.KtcFactoryUtil;
import com.ktc.utils.KtcHexUtil;
import com.ktc.utils.KtcKeyUtil;
import com.ktc.utils.KtcNetworkUtil;
import com.ktc.utils.KtcOpenSdk;
import com.ktc.utils.KtcSystemUtil;
import com.ktc.utils.KtcTvUtil;
import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tv.TvPictureManager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * TODO 串口通信自动测试
 *
 * @author Arvin
 * 2018-3-11
 */
public class SerialAutoConsoleActivity extends Activity implements OnClickListener{

	private static final String TAG = "SerialAutoConsoleActivity" ;
	
	private String mDevicePath = "dev/ttyS1";
	private int mBaudrate = 115200 ;
	
	private int flag_receive = Constants.FLAG_RECEIVE_HEX; 
	private boolean isAutoClear = false ;
	
	private TextView txt_receive , txt_status ,txt_tv_back;
	private RadioGroup mRadioGroup ;
	private CheckBox mAutoClear;
	
	private SerialControl mSerialControl ;
	
	private KtcSystemUtil mKtcSystemUtil ;
	private KtcFactoryUtil mKtcFactoryUtil ;
	private KtcTvUtil mKtcTvUtil ;
	private KtcKeyUtil mKtcKeyUtil ;
	private KtcNetworkUtil mKtcNetworkUtil ;
	private KtcOpenSdk mKtcOpenSdk ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serial_activity_auto_console);
		
		mSerialControl = new SerialControl();
		mSerialControl.initSerialHelper();
		
		initViews();
		
		try {
			mSerialControl.initSerialPortConfig(new File(mDevicePath), mBaudrate, 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initViews(){
    	txt_receive = (TextView) findViewById(R.id.txt_receive);
    	txt_status = (TextView) findViewById(R.id.txt_status);
    	txt_tv_back = (TextView) findViewById(R.id.txt_tv_back);
    	mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
    	mAutoClear = (CheckBox) findViewById(R.id.checkbox_autoclear);
    	mAutoClear.setChecked(false);
    	mRadioGroup.check(R.id.radio_hex);
    	mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int arg1) {
				switch (group.getCheckedRadioButtonId()) {
				case R.id.radio_txt://show txt
					flag_receive = Constants.FLAG_RECEIVE_TXT ;
					break;
				case R.id.radio_hex://show hex
					flag_receive = Constants.FLAG_RECEIVE_HEX ;
					break;	
				default:
					break;
			}
			}
		});
    	
    	mAutoClear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){   
            @Override   
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	//auto clear receive data
            	isAutoClear = isChecked ;
            }   
        }); 
    	
    	//init utils
		mKtcOpenSdk = KtcOpenSdk.getInstance(this);
		mKtcSystemUtil = mKtcOpenSdk.getKtcSystemUtil();
		mKtcFactoryUtil = mKtcOpenSdk.getKtcFactoryUtil();
		mKtcTvUtil = mKtcOpenSdk.getKtcTvUtil();
		mKtcKeyUtil = mKtcOpenSdk.getKtcKeyUtil();
		mKtcNetworkUtil = mKtcOpenSdk.getKtcNetworkUtil();
		
    }
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_clear:
			txt_receive.setText("");
			break;

		default:
			break;
		}
	}
	
	/**
	 *
	 * TODO 串口控制工具类
	 * @author Arvin
	 * 2018-3-11
	 */
	StringBuilder sMsg = new StringBuilder();
	private class SerialControl extends SerialHelper{

		@Override
		protected void onDataReceived(byte[] buffer, int size) {
			updateReceive(buffer, size);
			parseCmds(buffer, size);
		}
		
		@Override
		protected void onDataReceived(String HexMsg) {
			int len = KtcHexUtil.hexStr2ByteArray(HexMsg).length ;
			updateReceive(KtcHexUtil.hexStr2ByteArray(HexMsg), len);
			parseCmds(KtcHexUtil.hexStr2ByteArray(HexMsg), len);
		}
    }
	
	/**
	 * TODO 解析PC->TV命令(Command Format:  0xC0(Vender) + ITEM (2bytes)+ DL(Data LEN)(2bytes) + Data + CS)
	 * @param byte[] buffer, int size
	 * @return void
	 */
	private boolean parseCmds(byte[] buffer, int size){
		String hexStr = KtcHexUtil.byte2HexStr(buffer, size).replace(" ", "").toUpperCase();
		Log.i(TAG , "==parseCmd_1:  "+hexStr);
		//check empty/lenth
		Log.i(TAG , "==!isStrNotEmpty(hexStr):  "+(!isStrNotEmpty(hexStr)));
		Log.i(TAG , "==hexStr.length():  "+(hexStr.length()));
		if(!isStrNotEmpty(hexStr) || hexStr.length() < 10){
			return updateLoopBack(CMD_TYPE_VALUE2.TV_TV_NACK) ;
		}
		
		String noCsHexStr = getSubString(hexStr, 0, hexStr.length() - 2);
		Log.i(TAG , "==getCmdCS:  "+(mSerialControl.getCmdCS(noCsHexStr)));
		Log.i(TAG , "==hexStr_end:  "+(getSubString(hexStr, hexStr.length() -2 , 2)));
		//check checksum
		if(!mSerialControl.getCmdCS(noCsHexStr).equals(getSubString(hexStr, hexStr.length() -2 , 2))){
			return updateLoopBack(CMD_TYPE_VALUE2.TV_TV_NACK) ;
		}

		Log.i(TAG , "==parseCmd_2:  "+hexStr);
		String PC_CMD_HEAD = getSubString(hexStr , 0 , 2);
		String PC_CMD_ITEM = getSubString(hexStr , 2 , 4);
		String PC_CMD_DL = getSubString(hexStr , 6 , 4);
		Log.i(TAG, "PC_CMD_ITEM   "+PC_CMD_ITEM);
		Log.i(TAG, "PC_CMD_DL   "+PC_CMD_DL);
		String PC_CMD_DATA = getSubString(hexStr , 10 , Integer.parseInt(PC_CMD_DL, 16) * 2);
		Log.i(TAG, "PC_CMD_DATA   "+PC_CMD_DATA);
		
		String TV_CMD_ITEM = "";
		String TV_CMD_DL = "";
		String TV_CMD_DATA = "";
		
		if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_ENTER_FACTORY_MODE)){
			mKtcFactoryUtil.enterFactoryMode();
			TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_SERIAL_NUMBER)){
			if(isStrNotEmpty(PC_CMD_DATA)){
				mKtcSystemUtil.setSerialNumber(KtcHexUtil.hexStrToString(PC_CMD_DATA));
				updateStatus("PC_SET_SERIAL_NUMBER:\n	"+mKtcSystemUtil.getSerialNumber());
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK ;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_SERIAL_NUMBER)){
			String mSerialNumber = mKtcSystemUtil.getSerialNumber() ;
			if(isStrNotEmpty(mSerialNumber)){
				updateStatus("PC_GET_SERIAL_NUMBER:\n"+mSerialNumber);
				TV_CMD_ITEM = "0001";
				TV_CMD_DL = String.format("%04x" , mSerialNumber.length());
				TV_CMD_DATA = KtcHexUtil.str2HexStr(mSerialNumber) ;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_FW_VERSION)){
			String mFwVersion = mKtcSystemUtil.getProductsVersion();
			if(isStrNotEmpty(mFwVersion)){
				updateStatus("PC_GET_FW_VERSION:\n"+mFwVersion);
				TV_CMD_ITEM = "0001";
				TV_CMD_DL = String.format("%04x" , mFwVersion.length());
				TV_CMD_DATA = KtcHexUtil.str2HexStr(mFwVersion) ;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_MODEL_NAME)){
			String mModelName = mKtcSystemUtil.getProductsModel();
			if(isStrNotEmpty(mModelName)){
				updateStatus("PC_GET_FW_VERSION:\n"+mModelName); 
				TV_CMD_ITEM = "0001";
				TV_CMD_DATA = KtcHexUtil.str2HexStr(mModelName) ;
				TV_CMD_DL = String.format("%04x" , TV_CMD_DATA.length()/2);
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_HDMI_EDID)){
			if(PC_CMD_DATA.equals("00")){//HDMI1
				byte[] mHDMI1_Edid;
				try {
					mHDMI1_Edid = mKtcTvUtil.getHDMI1_Edid_byte();
					Log.i(TAG, "mHDMI1_Edid:  "+mHDMI1_Edid.toString());
					if(mHDMI1_Edid != null){
						String byte2HexStr = KtcHexUtil.byte2HexStr(mHDMI1_Edid) ;
						updateStatus("PC_GET_HDMI2_EDID:\n"+byte2HexStr); 
						TV_CMD_ITEM = "0001";
						TV_CMD_DL = "0100";
						TV_CMD_DATA = KtcHexUtil.hexStrToString(byte2HexStr);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
			}else if(PC_CMD_DATA.equals("01")){//HDMI2
				byte[] mHDMI2_Edid;
				try {
					mHDMI2_Edid = mKtcTvUtil.getHDMI2_Edid_byte();
					if(mHDMI2_Edid != null){
						String byte2HexStr = KtcHexUtil.byte2HexStr(mHDMI2_Edid);
						updateStatus("PC_GET_HDMI2_EDID:\n"+byte2HexStr); 
						TV_CMD_ITEM = "0001";
						TV_CMD_DL = "0100";
						TV_CMD_DATA = KtcHexUtil.hexStrToString(byte2HexStr);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(PC_CMD_DATA.equals("02")){//HDMI3
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("03")){//HDMI4
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("04")){//HDMI5
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_HDCP_KEY)){
			byte[] mHDCP_KEY_1_4;
			try {
				mHDCP_KEY_1_4 = mKtcTvUtil.getHDCPKey_1_4();
				if(mHDCP_KEY_1_4 != null){
					updateStatus("PC_GET_HDCP_KEY:\n"+KtcHexUtil.byte2HexStr(mHDCP_KEY_1_4)); 
					int len = Integer.parseInt("0130", 16);
					Log.i(TAG, "len:  "+len);
					TV_CMD_ITEM = "00010130";
					TV_CMD_DL = "0130";
					TV_CMD_DATA = KtcHexUtil.byte2HexStr(mHDCP_KEY_1_4) ;
					Log.i(TAG, "BACK_CMD_DATA:  "+TV_CMD_DATA.length());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_CHECK_SD_FILE)){
			boolean hasSdFile = mKtcSystemUtil.hasSDFile();
			
			updateStatus("PC_CHECK_SD_FILE:\n"+hasSdFile); 
			TV_CMD_ITEM = "0001";
			TV_CMD_DL = "0001";
			TV_CMD_DATA = (hasSdFile ? "01" : "00") ;
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_PIC_MODE)){
			if(PC_CMD_DATA.equals("01")){
				mKtcTvUtil.changePictureMode(TvPictureManager.PICTURE_MODE_DYNAMIC);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("02")){
				mKtcTvUtil.changePictureMode(TvPictureManager.PICTURE_MODE_NORMAL);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("03")){
				mKtcTvUtil.changePictureMode(TvPictureManager.PICTURE_MODE_USER);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("04")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("05")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("06")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("07")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("08")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("09")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("0A")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("0B")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("0C")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			
			updateStatus("PC_SET_PIC_MODE_VIVID:  "+mKtcTvUtil.getPictureMode()); 
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_MEMORY_SIZE)){
			if(PC_CMD_DATA.equals("01")){
				String data = mKtcSystemUtil.getEmmcSize()+"" ;
				updateStatus("PC_GET_EMMC_SIZE:  "+data); 
				if(isStrNotEmpty(data)){
					TV_CMD_ITEM = "0001" ;
					TV_CMD_DL = String.format("%04x" , data.length()) ;
					TV_CMD_DATA = KtcHexUtil.str2HexStr(data) ;
				}else{
					TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
				}
			}else if(PC_CMD_DATA.equals("02")){
				String data = mKtcSystemUtil.getDDRAMSize()+"" ;
				updateStatus("PC_GET_DDRAM_SIZE:  "+data); 
				if(isStrNotEmpty(data)){
					TV_CMD_ITEM = "0001" ;
					TV_CMD_DL = String.format("%04x" , data.length());
					TV_CMD_DATA = KtcHexUtil.str2HexStr(data);
				}else{
					TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
				}
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_EMMC_ID)){
			updateStatus("PC_GET_EMMC_ID:  "+mKtcSystemUtil.getEMMC_Id() ); 
			TV_CMD_ITEM = "0001" ;
			TV_CMD_DL= "0010" ;
			TV_CMD_DATA = mKtcSystemUtil.getEMMC_Id();
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_ETHERNET_MAC_ADDR)){
			String data = mKtcNetworkUtil.getEthernetMacAddress();
			if(isStrNotEmpty(data)){
				String mEthMac = data.replaceAll(":", "");
				updateStatus("PC_GET_ETHERNET_MAC_ADDR:  "+mEthMac); 
				TV_CMD_ITEM = "0001" ;
				TV_CMD_DL = "0006" ;
				TV_CMD_DATA = mEthMac;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_CHECK_USB_FILE)){
			boolean hasSdFile = mKtcSystemUtil.hasUsbFiles() ;
			updateStatus("PC_CHECK_USB_FILE:  "+hasSdFile );
			TV_CMD_ITEM = "0001" ;
			TV_CMD_DL = "0001" ;
			TV_CMD_DATA = hasSdFile ? "01" : "00";
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_VOLUME)){
			String data = getSubString(hexStr , 10, 2);
			if(isStrNotEmpty(data)){
				mKtcSystemUtil.setStreamVolume(Integer.parseInt(data , 16));
				
				updateStatus("PC_SET_VOLUME:\n	"+mKtcSystemUtil.getStreamVolume());
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK ;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_FAC_PRESET_CHANNEL)){
			if(PC_CMD_DATA.equals("00")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("01")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("02")){
				mKtcFactoryUtil.presetFactoryChannels();
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("03")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("04")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("05")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("06")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("07")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("08")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_MAIN_INPUT_SRC)){
			if(PC_CMD_DATA.equals("01")){//DTV
				mKtcTvUtil.changeTvSource(TvCommonManager.INPUT_SOURCE_DTV);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("02")){//ATV
				mKtcTvUtil.changeTvSource(TvCommonManager.INPUT_SOURCE_ATV);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("03")){//AV
				mKtcTvUtil.changeTvSource(TvCommonManager.INPUT_SOURCE_CVBS);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("11")){//HDMI1
				mKtcTvUtil.changeTvSource(TvCommonManager.INPUT_SOURCE_HDMI);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("12")){//HDMI2
				mKtcTvUtil.changeTvSource(TvCommonManager.INPUT_SOURCE_HDMI2);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("13")){//HDMI3
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("14")){//HDMI4
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("15")){//HDMI5
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("21")){//Video1
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("22")){//Video2
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("31")){//Component
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("32")){//Component2
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("41")){//PC
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("42")){//PC2
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("51")){//USB
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("61")){//DLNA
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			
			updateStatus(null); 
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_IR_KEY)){
			if(PC_CMD_DATA.equals("01")){//DPAD_UP
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
				showTips("--PC_IR_KEY_UP--");
			}else if(PC_CMD_DATA.equals("02")){//DPAD_DOWN
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
				showTips("--PC_IR_KEY_DOWN--");
			}else if(PC_CMD_DATA.equals("03")){//DPAD_LEFT
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
				showTips("--PC_IR_KEY_LEFT--");
			}else if(PC_CMD_DATA.equals("04")){//DPAD_RIGHT
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
				showTips("--PC_IR_KEY_RIGHT--");
			}else if(PC_CMD_DATA.equals("05")){//PRG UP
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_PAGE_UP);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
				return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
			}else if(PC_CMD_DATA.equals("06")){//PRG DOWN
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_PAGE_DOWN);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
				return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
			}else if(PC_CMD_DATA.equals("07")){//MTS
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("08")){//CC
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("09")){//Wide_model
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("0A")){//Tools
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("0B")){//0
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_0);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("0C")){//1
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_1);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("0D")){//2
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_2);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("0E")){//3
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_3);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("0F")){//4
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_4);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("10")){//5
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_5);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("11")){//6
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_6);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("12")){//7
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_7);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("13")){//8
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_8);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("14")){//9
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_9);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("15")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("16")){//ENT
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("17")){//POWER
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_POWER);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("18")){//VOLUME_UP
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_VOLUME_UP);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("19")){//VOLUME_DOWN
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("20")){//MENU
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_MENU);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("21")){//EXIT
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("22")){//BACK
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_BACK);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("23")){//INFO
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_INFO);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("24")){//TV_INPUT
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_TV_INPUT);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("25")){//MUTE
				mKtcKeyUtil.sendKeyEvent(KeyEvent.KEYCODE_VOLUME_MUTE);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("26")){//TELETEXT
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_WHITE_PATTERN)){
			if(PC_CMD_DATA.equals("00")){//EXIT
				mKtcFactoryUtil.setWbPattern((short)0);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("01")){//70%
				mKtcFactoryUtil.setWbPattern((short)4);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("02")){//100%
				mKtcFactoryUtil.setWbPattern((short)1);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("03")){//20%
				mKtcFactoryUtil.setWbPattern((short)9);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("04")){//80%
				mKtcFactoryUtil.setWbPattern((short)3);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_LED_STATUS)){
			if(PC_CMD_DATA.equals("01")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("02")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("03")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_LIGHT_SENSOR_LEVEL)){
			TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_KEY_LOCK_IR_LOCK)){
			if(PC_CMD_DATA.equals("01")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("02")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("03")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("04")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_KEYPAD_ADC)){
			TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_FACTORY_RESET)){
			mKtcFactoryUtil.factoryReset();
			TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_BRGHTNSS)){
			if(isStrNotEmpty(PC_CMD_DATA)){
				mKtcTvUtil.setPicBrightness(Integer.parseInt(PC_CMD_DATA , 16));
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_CONTRAST)){
			if(isStrNotEmpty(PC_CMD_DATA)){
				mKtcTvUtil.setPicContrast(Integer.parseInt(PC_CMD_DATA , 16));
				updateStatus(null); 
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_BACKLIGHT)){
			if(isStrNotEmpty(PC_CMD_DATA)){
				mKtcTvUtil.setBackLight(Integer.parseInt(PC_CMD_DATA , 16));
				updateStatus(null); 
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_AGING_MODE)){
			if(PC_CMD_DATA.equals("01")){//OFF
				mKtcFactoryUtil.setFactoryAgeMode(false);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("02")){//ON
				mKtcFactoryUtil.setFactoryAgeMode(true);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("03")){//ON_WHITE
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_COLORTEMP)){
			if(PC_CMD_DATA.equals("01")){//cool
				mKtcTvUtil.setPictureColorTemperature(TvPictureManager.COLOR_TEMP_COOL);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("02")){//Neutral
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("03")){//Warm 1
				mKtcTvUtil.setPictureColorTemperature(TvPictureManager.COLOR_TEMP_WARM);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("04")){//Warm 2
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("05")){//STANDARD
				mKtcTvUtil.setPictureColorTemperature(TvPictureManager.COLOR_TEMP_NATURE);
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
			}else if(PC_CMD_DATA.equals("06")){//Computer
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("07")){//Normal
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("08")){//User
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_CT_DATA)){
			if(PC_CMD_DL.equals("03")){//double
				String type = getSubString(hexStr , 10 , 2);
				if(type.equals("01")){//R gain
					TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
				}else if(type.equals("02")){//G gain
					TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
				}else if(type.equals("03")){//B gain
					TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
				}else if(type.equals("04")){//R Offset
					String data = getSubString(hexStr , 12, 4);
					if(isStrNotEmpty(data)){
						int roffsetvalWB = Integer.parseInt(data , 16) ;
						mKtcFactoryUtil.setWB_R_Offset(roffsetvalWB);
						
						updateStatus("roffsetvalWB:  "+roffsetvalWB);
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
					}else{
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
					}
				}else if(type.equals("05")){//G Offset
					String data = getSubString(hexStr , 12, 4);
					if(isStrNotEmpty(data)){
						int goffsetvalWB = Integer.parseInt(data , 16) ;
						mKtcFactoryUtil.setWB_G_Offset(goffsetvalWB);
						
						updateStatus("goffsetvalWB:  "+goffsetvalWB);
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
					}else{
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
					}
				}else if(type.equals("06")){//B Offset
					String data = getSubString(hexStr , 12, 4);
					if(isStrNotEmpty(data)){
						int boffsetvalWB = Integer.parseInt(data , 16) ;
						mKtcFactoryUtil.setWB_B_Offset(boffsetvalWB);
						
						updateStatus("boffsetvalWB:  "+boffsetvalWB);
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
					}else{
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
					}
				}else{
					TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
				}
			}else if(PC_CMD_DL.equals("02")){//single
				String type = getSubString(hexStr , 10 , 2);
				if(type.equals("01")){//R gain
					String data = getSubString(hexStr , 12, 2);
					if(isStrNotEmpty(data)){
						int roffsetvalWB = Integer.parseInt(data , 16) ;
						mKtcFactoryUtil.setWB_R_Gain(roffsetvalWB);
						
						updateStatus("roffsetvalWB:  "+roffsetvalWB);
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
					}else{
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
					}
				}else if(type.equals("02")){//G gain
					String data = getSubString(hexStr , 12, 2);
					if(isStrNotEmpty(data)){
						int goffsetvalWB = Integer.parseInt(data , 16) ;
						mKtcFactoryUtil.setWB_G_Gain(goffsetvalWB);
						
						updateStatus("goffsetvalWB:  "+goffsetvalWB);
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
					}else{
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
					}
				}else if(type.equals("03")){//B gain
					String data = getSubString(hexStr , 12, 2);
					if(isStrNotEmpty(data)){
						int boffsetvalWB = Integer.parseInt(data , 16) ;
						mKtcFactoryUtil.setWB_B_Gain(boffsetvalWB);
						
						updateStatus("boffsetvalWB:  "+boffsetvalWB);
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_ACK;
					}else{
						TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
					}
				}else if(type.equals("04")){//R Offset
					TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
				}else if(type.equals("05")){//G Offset
					TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
				}else if(type.equals("06")){//B Offset
					TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
				}else{
					TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
				}
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_CT_DATA)){
			if(PC_CMD_DATA.equals("01")){
				int r_gain = mKtcFactoryUtil.getWB_R_Gain();
				
				updateStatus("PC_GET_CT_DATA_R_GAIN:  "+r_gain); 
				TV_CMD_ITEM = "0001" ;
				TV_CMD_DL = "0002" ;
				TV_CMD_DATA = String.format("%04x" , r_gain) ;
			}else if(PC_CMD_DATA.equals("02")){
				int g_gain = mKtcFactoryUtil.getWB_G_Gain();
				
				updateStatus("PC_GET_CT_DATA_G_GAIN:  "+g_gain);
				TV_CMD_ITEM = "0001" ;
				TV_CMD_DL = "0002" ;
				TV_CMD_DATA = String.format("%04x" , g_gain) ;
			}else if(PC_CMD_DATA.equals("03")){
				int b_gain = mKtcFactoryUtil.getWB_B_Gain();
				
				updateStatus("PC_GET_CT_DATA_B_GAIN:  "+b_gain ); 
				TV_CMD_ITEM = "0001" ;
				TV_CMD_DL = "0002" ;
				TV_CMD_DATA = String.format("%04x" , b_gain) ;
			}else if(PC_CMD_DATA.equals("04")){
				int r_offset = mKtcFactoryUtil.getWB_R_Offset();
				
				updateStatus("PC_GET_CT_DATA_R_OFFSET:  "+r_offset); 
				TV_CMD_ITEM = "0001" ;
				TV_CMD_DL = "0002" ;
				TV_CMD_DATA = String.format("%04x" , r_offset) ;
			}else if(PC_CMD_DATA.equals("05")){
				int g_offset = mKtcFactoryUtil.getWB_G_Offset();
				
				updateStatus("PC_GET_CT_DATA_G_OFFSET:  "+g_offset); 
				TV_CMD_ITEM = "0001" ;
				TV_CMD_DL = "0002" ;
				TV_CMD_DATA = String.format("%04x" , g_offset) ;
			}else if(PC_CMD_DATA.equals("06")){
				int b_offset = mKtcFactoryUtil.getWB_B_Offset();
				
				updateStatus("PC_GET_CT_DATA_B_OFFSET:  "+b_offset); 
				TV_CMD_ITEM = "0001" ;
				TV_CMD_DL = "0002" ;
				TV_CMD_DATA = String.format("%04x" , b_offset) ;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_CONNECT_WIFI)){
			if(isStrNotEmpty(PC_CMD_DATA)){
				boolean isSuccess = mKtcNetworkUtil.connectOpenWifi(KtcHexUtil.hexStrToString(PC_CMD_DATA), null);
				if(!isSuccess){
					TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
				}
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
		
		else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_WIFI_MAC_ADDR)){
			String data = mKtcNetworkUtil.getWlanMacAddress();
			if(isStrNotEmpty(data)){
				String mWlanMac = data.replaceAll(":", "");
				updateStatus("PC_GET_WIFI_MAC_ADDR:  "+mWlanMac); 
				TV_CMD_ITEM = "00010006" ;
				TV_CMD_DL = "0006" ;
				TV_CMD_DATA = mWlanMac ;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_WIFI_PID)){
			int[] vid_pid = mKtcNetworkUtil.getWifiModule_VidAndPid();
			if(vid_pid.length > 1){
				String mHexPid = String.format("%04x" , vid_pid[1]);
				
				updateStatus("PC_GET_WIFI_PID:  "+vid_pid[1]); 
				TV_CMD_ITEM = "0001" ;
				TV_CMD_DL = "0002" ;
				TV_CMD_DATA = mHexPid ;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_WIFI_VID)){
			int[] vid_pid = mKtcNetworkUtil.getWifiModule_VidAndPid();
			if(vid_pid.length > 1){
				String mHexVid = String.format("%04x" , vid_pid[0]);
				
				updateStatus("PC_GET_WIFI_PID:  "+mHexVid); 
				TV_CMD_ITEM = "0001" ;
				TV_CMD_DL = "0002" ;
				TV_CMD_DATA = mHexVid ;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK ;
			}
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_CHECK_WIFI_STATUS)){
			boolean isWifiConnected = mKtcNetworkUtil.isWifiConnected() ;

			updateStatus("PC_CHECK_WIFI_STATUS:   "+isWifiConnected); 
			TV_CMD_ITEM = "0001" ;
			TV_CMD_DL = "0001" ;
			TV_CMD_DATA = isWifiConnected ? "01" : "00" ;
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_GET_ETHERENT_STATUS)){
			boolean isEthEnable = mKtcNetworkUtil.isEthernetEnable() ;
			
			updateStatus("PC_GET_ETHERENT_STATUS:   "+isEthEnable); 
			TV_CMD_ITEM = "0001" ;
			TV_CMD_DL = "0001" ;
			TV_CMD_DATA = isEthEnable ? "01" : "00" ;
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}else if(PC_CMD_ITEM.equals(CMD_TYPE_VALUE2.ITEM_PC_SET_LIGHT_SENSOR_STATUS)){
			if(PC_CMD_DATA.equals("00")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else if(PC_CMD_DATA.equals("01")){
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}else{
				TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NOT_SUPPORT;
			}
			
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}else{
			TV_CMD_ITEM = CMD_TYPE_VALUE2.TV_TV_NACK;
			return updateLoopBack(TV_CMD_ITEM + TV_CMD_DL + TV_CMD_DATA);
		}
	}
	
	/**
	 * TODO 截取指定字符串
	 * @param String hexStr , int startIndex , int offsetLen
	 * @return String
	 */
	private String getSubString(String hexStr , int startIndex , int offsetLen){
		if(hexStr == null || hexStr.equals(""))return "";
		if((startIndex + offsetLen) <= hexStr.length()){
			return hexStr.substring(startIndex, startIndex + offsetLen).toUpperCase();
		}
		return "";
	}
	
	/*private String getData(String hexString , String dataLenHex){
		return String.format("%04x" , hexString.length());
	}*/
	
	/**
	 * TODO 判断返回数据是否为空
	 * @param String
	 * @return boolean
	 */
	private boolean isStrNotEmpty(String str){
		if(str != null && !str.equals("")){
			return true ;
		}
		return false ;
	}
	
	/**
	 * TODO 更新数据接收区
	 * @param byte[] buffer, int size
	 * @return void
	 */
	private void updateReceive(byte[] buffer, int size){
		if(isAutoClear && sMsg !=null){
			sMsg.delete(0, sMsg.length());
		}
		String tmpStr = null ;
    	if(flag_receive == Constants.FLAG_RECEIVE_TXT){
    		tmpStr = new String(buffer,0,size).toString().toUpperCase()+"\n";
    	}else if(flag_receive == Constants.FLAG_RECEIVE_HEX){
    		tmpStr = KtcHexUtil.byte2HexStr(buffer, size)+"\n";
    	}
    	SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");       
		String sRecTime = sDateFormat.format(new java.util.Date());
		sMsg.append("["+sRecTime+"]:  "+tmpStr);
    	
		runOnUiThread(new Runnable(){  
            @Override  
            public void run() { 
            	txt_receive.setText("\n"+sMsg);
            	Log.i(TAG , "DataReceive:" + sMsg);
            }  
        }); 
	}
	
	private void updateStatus(final String hexStatus){
		Log.i(TAG, "---updateStatus---");
		runOnUiThread(new Runnable(){  
            @Override  
            public void run() { 
            	txt_status.setText("");
        		txt_status.setText(hexStatus == null ? "" : hexStatus);
            }  
        }); 
	}
	
	private boolean updateLoopBack(final String hexStatus){
		runOnUiThread(new Runnable(){  
            @Override  
            public void run() { 
            	txt_tv_back.setText("");
        		txt_tv_back.setText(hexStatus == null ? "" : hexStatus);
            }  
        }); 
		if(hexStatus != null){
			return mSerialControl.sendStrCmd(hexStatus);
		}
		return false ;
	}
	
	/**
	 * TODO 显示提示信息
	 * @param 
	 * @return void
	 */
	private void showTips(final String hexStatus){
		runOnUiThread(new Runnable(){  
            @Override  
            public void run() { 
            	txt_tv_back.setText("");
            	Toast.makeText(SerialAutoConsoleActivity.this, hexStatus == null ? "" : hexStatus, Toast.LENGTH_SHORT).show();
            }  
        }); 
	}
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if(mSerialControl != null){
			mSerialControl.closeSerialPort();
		}
		super.onDestroy();
	}
	
}
