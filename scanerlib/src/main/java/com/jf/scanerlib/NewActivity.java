package com.jf.scanerlib;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewActivity extends BaseActivity implements OnClickListener {
	private final static String TAG="NewActivity";
    private Button buttonBack;
    private Button buttonSIM;
//    private Button buttonIMEI;
    private EditText editTextSim;
    private EditText editTextImei;
    private TelephonyManager telephonyManager;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "NewActivity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_activity);
		buttonBack=(Button)findViewById(R.id.buttonBack);
		buttonSIM=(Button)findViewById(R.id.buttonSIM);
//		buttonIMEI=(Button)findViewById(R.id.buttonIMEI);
		editTextSim=(EditText)findViewById(R.id.editTextSIM);
		editTextImei=(EditText)findViewById(R.id.editTextIMEI);
		
		
		buttonBack.setOnClickListener(this);
		buttonSIM.setOnClickListener(this);
//		buttonIMEI.setOnClickListener(this);
		
		telephonyManager=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==buttonBack){
			finish();
		}/*else if(v==buttonIMEI){
			String imei=telephonyManager.getDeviceId();
			if(!imei.isEmpty()&&imei!=null){
				editTextImei.setText(imei);
			}else{
				editTextImei.setText("获取失败");
			}
			
		}*/else if(v==buttonSIM){
			String sim=telephonyManager.getSimSerialNumber();
			if(sim!=null&&!sim.isEmpty()){
				editTextSim.setText(sim);
			}else{
				editTextSim.setText("获取失败");
			}
			
		}
	}

}
