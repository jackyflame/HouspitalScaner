package com.jf.scanerlib;

import com.routon.idr.idrinterface.readcard.IReadCardService;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class BaseActivity extends Activity {
	   private static final String TAG = "BaseActivity";
		private IReadCardService mReadCardService = null;
		private BroadcastReceiver mReceiver = null;
		
		private ServiceConnection conn_readcard = new ServiceConnection(){

			@Override
			public void onServiceConnected(ComponentName comp, IBinder binder) {
				Log.d(TAG, "ReadCardService Connected");
				mReadCardService = IReadCardService.Stub.asInterface(binder);
			}

			@Override
			public void onServiceDisconnected(ComponentName comp) {
				Log.d(TAG, "ReadCardService Disconnected");
				mReadCardService = null;
			}    	
	    };
	    	
	    
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Log.d(TAG, "baseActivity onCreate");
			//绑定服务
			bindReadcardService();
			//注册广播
			mReceiver = new Receiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			registerReceiver(mReceiver, filter);
		}
		
	    private class Receiver extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)){
					String reason = intent.getStringExtra("reason");
					if("homekey".equals(reason)){
						Log.i(TAG, "home key pressed, stop readcard and fingerprint");
						if(mReadCardService != null){
							try {
								mReadCardService.stop();
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
					}
				}
				else if(Intent.ACTION_SCREEN_OFF.equals(action)){
					if(mReadCardService != null){
						try {
							mReadCardService.stop();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			}
	    }		
		
		@Override
		protected void onResume() {
			super.onResume();
			Log.d(TAG, "baseActivity onResume");
			if(mReadCardService != null){
				try {
					mReadCardService.open();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onPause() {
			super.onPause();
			
		}

		@Override
		protected void onDestroy() {
			super.onDestroy();
			unregisterReceiver(mReceiver);
			unbindService(conn_readcard);
		}
		
		private void bindReadcardService(){
			Intent intentReadCard = new Intent(IReadCardService.class.getName());
			bindService(intentReadCard, conn_readcard, BIND_AUTO_CREATE);
			startService(intentReadCard);		
		}
}
