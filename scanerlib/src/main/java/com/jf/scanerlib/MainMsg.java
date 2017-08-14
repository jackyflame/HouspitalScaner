package com.jf.scanerlib;

public class MainMsg {
	//消息
	public static final int EVT_START = 1;	
	public static final int EVT_STOP = 2;
	public static final int EVT_PAUSE = 3;
	public static final int EVT_RESUME = 4;
	public static final int EVT_SCREEN_OFF = 5;//关屏
	public static final int EVT_READER_OPENED = 6;
	public static final int EVT_FOUND_A = 7;
	public static final int EVT_FOUND_B = 8;
	public static final int EVT_READ_CARD_A_SUCCESS = 9;
	public static final int EVT_READ_CARD_B_SUCCESS = 10;
	public static final int EVT_GOT_MCU_VER = 11;
	public static final int EVT_GOT_SAM_ID = 12;
	public static final int EVT_LEAVE_A = 13;
	public static final int EVT_LEAVE_B = 14;
	public static final int EVT_STARTED = 15;
	public static final int EVT_STOPPED = 16;
	public static final int EVT_PAUSED = 17;
	public static final int EVT_STATE_CHANGE = 18;
	public static final int EVT_READ_CARD_A_FAIL = 19;
	public static final int EVT_READ_CARD_B_FAIL = 20;
	public static final int EVT_SELECT_B = 21;
		
	public static final  int EVT_TIMEOUT_ROLL = 22;   //滚动处理	
	public static final  int EVT_SERV_CONNECTED_RDCARD = 23;	
	public static final  int EVT_SERV_DISCONNECTED_RDCARD  = 24;
	public static final  int EVT_GET_SAMID = 25;
	public static final  int EVT_GET_MCU_VERSION = 26;
	public static final  int EVT_UI_PAUSED = 27;
	public static final  int EVT_UI_RESUMED = 28;
	public static final  int EVT_APP_EXIT = 29;
	public static final  int EVT_GET_VERSION = 30;
	public static final  int EVT_READMODE_CHANGED_SERV = 31;
	public static final  int EVT_READTYPE_CHANGED_SERV = 32;
	public static final  int EVT_UI_HOME=33;//Home键
	public static final  int EVT_ACTIVITY_TO_BACKGROUND=34;//界面跳转到后台
	public static final  int EVT_TIMEOUT_SCREEN_ON=35;
	
	public static final int EVT_UPDATE_START=36;
	public static final int EVT_UPDATE_SUCCESS=37;
	public static final int EVT_UPDATE_FAIL=38;
	
	public static final int EVT_READ_CARD_B_CID = 39;

}

