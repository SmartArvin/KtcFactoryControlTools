package com.ktc.control.serialtest;

import java.io.File;
import java.text.SimpleDateFormat;

import com.ktc.control.constants.Constants;
import com.ktc.control.serialservice.SerialHelper;
import com.ktc.controltools.R;
import com.ktc.utils.KtcHexUtil;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;


/**
*
* TODO 串口模拟测试
*
* @author Arvin
* 2018-3-11
*/
public class SerialConsoleActivity extends Activity implements OnClickListener{

	private static final String TAG = "SerialConsoleActivity" ;
	
	private String mDevicePath = "dev/ttyS1";
	private int mBaudrate = 115200 ;
	
	private int flag_receive = Constants.FLAG_RECEIVE_HEX; 
	private boolean isAutoClear = false ;
	
	private TextView txt_receive ;
	private EditText edit_send , edit_time;
	private RadioGroup mRadioGroup ;
	private CheckBox mAutoClear , mAutoSend;
	private Spinner spinner_com_port , spinner_baudrate;
	private ToggleButton mToggleOpen ;
	
	private SerialControl mSerialControl ;
	private SendThread mSendThread ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serial_activity_console);
		
		mSerialControl = new SerialControl();
		mSerialControl.initSerialHelper();
		
		initViews();
	}
	
	private void initViews(){
    	txt_receive = (TextView) findViewById(R.id.txt_receive);
    	edit_send = (EditText) findViewById(R.id.edit_send);
    	edit_time = (EditText) findViewById(R.id.edit_time);
    	
    	mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
    	mAutoClear = (CheckBox) findViewById(R.id.checkbox_autoclear);
    	mAutoClear.setChecked(false);
    	
    	mAutoSend = (CheckBox) findViewById(R.id.checkbox_autotime);

    	spinner_baudrate = (Spinner)findViewById(R.id.spinner_baudRate);
    	spinner_com_port = (Spinner)findViewById(R.id.spinner_coms);
    	mToggleOpen = (ToggleButton) findViewById(R.id.toggle_open);
		
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
    	
    	mAutoSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if(isChecked){
					mSendThread = new SendThread();
					if (mSendThread != null){
						mSendThread.setResume();
					}
				}else{
					if (mSendThread != null){
						mSendThread.setSuspendFlag();
					}
				}
			}
		});
    	
		//baudrate choose
		final String[] baudrates = getResources().getStringArray(R.array.baudrates_name);
        ArrayAdapter comPortAdapter =  new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, baudrates);
        comPortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_baudrate.setAdapter(comPortAdapter);
        
        spinner_baudrate.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mBaudrate = Integer.decode(baudrates[position]) ;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
        	
		});
        
        //com choose
        final String[] entries = mSerialControl.getSerialPortFinder().getAllDevices();
        final String[] entryValues = mSerialControl.getSerialPortFinder().getAllDevicesPath();
        ArrayAdapter baudrateAdapter = new ArrayAdapter<String>(this ,android.R.layout.simple_list_item_1, entries);
        baudrateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_com_port.setAdapter(baudrateAdapter);
        
        spinner_com_port.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mDevicePath = entryValues[position] ;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
        
        mToggleOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isCheck) {
				if(isCheck){
					try {
						mSerialControl.initSerialPortConfig(new File(mDevicePath), mBaudrate, 0);
					} catch (Exception e) {
						Log.i(TAG, getResources().getString(R.string.error_security));
					}
				}else{
					if(mSerialControl != null){
						mSerialControl.closeSerialPort();
					}
				}
			}
		});
        
    }
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_clear:
			txt_receive.setText("");
			break;
		case R.id.btn_send:
			mSerialControl.sendStrCmd(KtcHexUtil.str2HexStr(edit_send.getText().toString().toUpperCase()));
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
			if(isAutoClear && sMsg !=null){
				sMsg.delete(0, sMsg.length());
			}
			String tmpStr = null ;
        	if(flag_receive == Constants.FLAG_RECEIVE_TXT){
        		tmpStr = new String(buffer,0,size).toString().toUpperCase()+"\n";
        	}else if(flag_receive == Constants.FLAG_RECEIVE_HEX){
        		tmpStr = KtcHexUtil.str2HexStr(new String(buffer,0,size).toString().toUpperCase())+"\n";
        	}
        	SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");       
			String sRecTime = sDateFormat.format(new java.util.Date());
			sMsg.append("["+sRecTime+"]:  "+tmpStr);
        	
			runOnUiThread(new Runnable(){  
	            @Override  
	            public void run() { 
	            	txt_receive.setText("\n"+sMsg);
	            	Log.i(TAG , " DataReceive:" + sMsg);
	            }  
	        }); 
		}
		
		@Override
		protected void onDataReceived(String HexMsg) {
			/*updateReceive(KtcHexUtil.hexStr2ByteArray(HexMsg), 0);
			parseCmds(KtcHexUtil.hexStr2ByteArray(HexMsg), 0);*/
		}
    }
	
	/**
	 * TODO 另起线程发送指令到PC
	 * @author Arvin
	 * 2018-3-10
	 */
	private class SendThread extends Thread{
		public boolean suspendFlag = true;
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				synchronized (this){
					while (suspendFlag){
						try{
							wait();
						} catch (InterruptedException e){
							e.printStackTrace();
						}
					}
				}
				
				try{
					if (mSerialControl != null) {
						byte[] tmp = KtcHexUtil.hexStr2ByteArray(edit_send.getText().toString().toUpperCase());//16进制转成byte[]
						mSerialControl.sendBufferCmd(tmp);
					} else {
						return;
					}
					Thread.sleep(50);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		public void setSuspendFlag() {
			this.suspendFlag = true;
		}
		
		public synchronized void setResume() {
			this.suspendFlag = false;
			notify();
		}
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
