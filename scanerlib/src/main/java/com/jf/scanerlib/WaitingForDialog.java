package com.jf.scanerlib;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;

public class WaitingForDialog {
	private static final String TAG="WaitingForDialog";
	private Context context;
	private String message;
	private ProgressDialog progressDialog;

	
	public WaitingForDialog(Context context,String message){
		this.context=context;
		this.message=message;		
	}	
	
	//创建一个对话框并显示
	public void showWaiting(){
		progressDialog=new ProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("提示");	
		progressDialog.setMessage(message);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}
	
	//关闭对话框
	public void dismissWaiting(){
		if(progressDialog.isShowing()&&progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	
	public void showResult(){
		AlertDialog.Builder exportDialog=new AlertDialog.Builder(context);
		exportDialog.setTitle("提示");
		exportDialog.setMessage(message);
		exportDialog.setPositiveButton("确定", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//启动进度条对话框
				
				dialog.dismiss();
			}
			
		});
		exportDialog.create().show();
		
	}
}
