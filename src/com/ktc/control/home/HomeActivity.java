package com.ktc.control.home;

import com.ktc.control.serialtest.SerialAutoConsoleActivity;
import com.ktc.control.serialtest.SerialConsoleActivity;
import com.ktc.controltools.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
*
* TODO KTC串行通信/WLAN通信集成工具
*
* @author Arvin
* 2018-3-11
*/
public class HomeActivity extends Activity implements OnClickListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }
    
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_serial_console:
			startActivity(new Intent(HomeActivity.this , SerialConsoleActivity.class));
			break;
		case R.id.btn_serial_console_manual:
			startActivity(new Intent(HomeActivity.this , SerialAutoConsoleActivity.class));
			break;
		case R.id.btn_manual:
			break;
		case R.id.btn_wlan:
			break;
			
		case R.id.btn_about:
			AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
			builder.setTitle("About");
			builder.setMessage(R.string.about_msg);
			builder.show();
			break;
		case R.id.btn_quit:
			finish();
			break;

		default:
			break;
		}
	}
}
