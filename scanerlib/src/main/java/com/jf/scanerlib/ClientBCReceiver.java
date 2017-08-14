package com.jf.scanerlib;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.routon.idr.idrinterface.readcard.BCardInfo;
import com.routon.idr.idrinterface.readcard.ReadMode;
import com.routon.idr.idrinterface.readcard.ReadState;
import com.routon.idr.idrinterface.readcard.ReadType;
import com.routon.idrconst.Action;
import com.routon.idrconst.ActionKey;
import com.routon.idrconst.iDRConst;

public class ClientBCReceiver extends BroadcastReceiver {
	private final String TAG="ClientBCReceiver";
	private Handler mHandler = null;
	
	public ClientBCReceiver(Handler mHandler) {
		super();
		this.mHandler = mHandler;
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		String action=intent.getAction();		
		Log.d(TAG,"onReceive = " + action);
		Message send_msg = new Message();
		if(action.equals(Action.ACTION_READER_STATE_CHANGED))
		{
			if(mHandler!=null){
				ReadState rdState = intent.getParcelableExtra(ActionKey.SERV_READCARD_NEW_READ_STATE);
				send_msg.what=MainMsg.EVT_STATE_CHANGE;    				
				send_msg.obj=rdState;
				mHandler.sendMessage(send_msg);
			}
			
		}else if(action.equals(Action.ACTION_TYPEA_STATUS_CHANGED)){
			int status = intent.getIntExtra(ActionKey.SERV_READCARD_STATUS, -1);
			switch(status)
			{
				case iDRConst.MSG_TYPEA_FOUND:
					send_msg.what=MainMsg.EVT_FOUND_A;    					
					break;
					
				case iDRConst.MSG_TYPEA_OK:		    					
					send_msg.what=MainMsg.EVT_READ_CARD_A_SUCCESS;
					//modify by pingping 2016.8.2 增加卡序列号长度
					byte data[]=new byte[32];
					data=intent.getByteArrayExtra(ActionKey.SERV_READCARD_A_CARDNO);
					int datalen = intent.getIntExtra(ActionKey.SERV_READCARD_A_CARDNO_LEN, 0);
					send_msg.obj=data;
					send_msg.arg1 = datalen;
					//end
					break;
				
				case iDRConst.MSG_TYPEA_REMOVED:	    					
					send_msg.what=MainMsg.EVT_LEAVE_A;
					break;
					
				case iDRConst.MSG_TYPEA_FAIL:	    					
					send_msg.what=MainMsg.EVT_READ_CARD_A_FAIL;
					break;
					
				default:	    					
					send_msg.what=MainMsg.EVT_READ_CARD_A_FAIL;
					break;		    					
			}
			if(mHandler!=null){
				mHandler.sendMessage(send_msg);
			}
			
		}else if(action.equals(Action.ACTION_TYPEB_STATUS_CHANGED)){
			int status = intent.getIntExtra(ActionKey.SERV_READCARD_STATUS, -1);
			switch(status)
			{
				case iDRConst.MSG_TYPEB_FOUND:
					send_msg.what=MainMsg.EVT_FOUND_B;
					break;
					
				case iDRConst.MSG_TYPEB_SELECTED:
					send_msg.what=MainMsg.EVT_SELECT_B;
					break;
					
				case iDRConst.MSG_TYPEB_OK: 
					BCardInfo cardInfo=new BCardInfo();
					cardInfo.name=intent.getStringExtra(ActionKey.SERV_READCARD_B_NAME);
					cardInfo.gender=intent.getStringExtra(ActionKey.SERV_READCARD_B_GENDER);
					cardInfo.nation=intent.getStringExtra(ActionKey.SERV_READCARD_B_NATION);
					cardInfo.birthday=intent.getStringExtra(ActionKey.SERV_READCARD_B_BIRTHDAY);
					cardInfo.address=intent.getStringExtra(ActionKey.SERV_READCARD_B_ADDRESS);
					cardInfo.id=intent.getStringExtra(ActionKey.SERV_READCARD_B_ID);
					cardInfo.agency=intent.getStringExtra(ActionKey.SERV_READCARD_B_AGENCY);
					cardInfo.expireStart=intent.getStringExtra(ActionKey.SERV_READCARD_B_EXPIRESTART);
					cardInfo.expireEnd=intent.getStringExtra(ActionKey.SERV_READCARD_B_EXPIREEND);
					cardInfo.photo=intent.getParcelableExtra(ActionKey.SERV_READCARD_B_PHOTO);
					cardInfo.hasFpInfo=intent.getBooleanExtra(ActionKey.SERV_READCARD_B_FLAG_FINGER,false);
					if(cardInfo.hasFpInfo)
					{
						cardInfo.fingerPrint=intent.getByteArrayExtra(ActionKey.SERV_READCARD_B_FINGERCHAR);
					}
					cardInfo.baseData = intent.getByteArrayExtra(ActionKey.SERV_READCARD_B_BASEDATA);
					cardInfo.wltData = intent.getByteArrayExtra(ActionKey.SERV_READCARD_B_WLTDATA);
					send_msg.what=MainMsg.EVT_READ_CARD_B_SUCCESS;
					send_msg.obj=cardInfo;
					break;
					
				case iDRConst.MSG_TYPEB_REMOVED:	   
					send_msg.what=MainMsg.EVT_LEAVE_B;
					break;
					
				case iDRConst.MSG_TYPEB_FAIL:
					send_msg.what=MainMsg.EVT_READ_CARD_B_FAIL;
					break;
				case iDRConst.MSG_TYPEB_CARDCID:
				{
					send_msg.what=MainMsg.EVT_READ_CARD_B_CID;					
					byte data[]=new byte[32];
					data=intent.getByteArrayExtra(ActionKey.SERV_READCARD_B_CARDCID);
					int datalen = intent.getIntExtra(ActionKey.SERV_READCARD_B_CARDCID_LEN, 0);
					send_msg.obj=data;
					send_msg.arg1 = datalen;
				}
					break;
				default:
					send_msg.what=MainMsg.EVT_READ_CARD_B_FAIL;
					break;	
					
			}
			if(mHandler!=null){
				mHandler.sendMessage(send_msg);
			}
			
		}else if(action.equals(Action.ACTION_READER_PAUSED)){
			if(mHandler!=null){
				send_msg.what=MainMsg.EVT_PAUSED;  				
				mHandler.sendMessage(send_msg);
			}
			
		}else if(action.equals(Action.ACTION_READER_STOPPED)){
			if(mHandler!=null){
				send_msg.what=MainMsg.EVT_STOPPED;  				
				mHandler.sendMessage(send_msg);
			}
			
		}else if(action.equals(Action.ACTION_READER_STARTED)){
			if(mHandler!=null){
				send_msg.what=MainMsg.EVT_STARTED;
				mHandler.sendMessage(send_msg);
			}
		}else if(action.equals(Action.ACTION_READER_READMODE_CHANGED))
		{
			if(mHandler!=null){
				ReadMode rdMode = intent.getParcelableExtra(ActionKey.SERV_READCARD_NEW_READ_MODE);
				send_msg.what=MainMsg.EVT_READMODE_CHANGED_SERV;    				
				send_msg.obj=rdMode;
				mHandler.sendMessage(send_msg);
			}			
		}else if(action.equals(Action.ACTION_READER_READTYPE_CHANGED))
		{
			if(mHandler!=null){
				ReadType rdType = intent.getParcelableExtra(ActionKey.SERV_READCARD_NEW_READ_TYPE);
				send_msg.what=MainMsg.EVT_READTYPE_CHANGED_SERV;    				
				send_msg.obj=rdType;
				mHandler.sendMessage(send_msg);
			}			
		}else if(action.equals(Action.ACTION_READER_OPENED)){
			if(mHandler!=null){
				send_msg.what=MainMsg.EVT_READER_OPENED;
				mHandler.sendMessage(send_msg);
			}
		}
		else if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
			if(mHandler!=null){
				String reason = intent.getStringExtra("reason");
				Log.d(TAG,"****"+reason+"****");
				if("homekey".equals(reason)){
						send_msg.what=MainMsg.EVT_UI_HOME;  				
						mHandler.sendMessage(send_msg);
				}
			}
		}else if(action.equals(Intent.ACTION_SCREEN_OFF)){
			if(mHandler!=null){
		
				send_msg.what=MainMsg.EVT_SCREEN_OFF;  				
				mHandler.sendMessage(send_msg);
				
			}
		}
	}
}
