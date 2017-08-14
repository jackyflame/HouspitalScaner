package com.jf.scanerlib;

import android.content.Context;
import android.util.Log;

import com.routon.idrconst.Shell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SystemPatch {
	private final static String TAG = "SystemPatch";
	public final static String SYSPATCH_DIR_RELATIVE_PATH = "syspatch";
	public final static String PATCH_FILE_RELATIVE_PATH = "syspatch"+File.separator+"patch.sh";
	public final static String PATCH_FILENAME = "patch.sh";
	/**
     *  执行拷贝任务
     *  @param asset 需要拷贝的assets文件路径
     *  @param asset 目标文件路径
     *  @return 拷贝成功后的目标文件句柄
     *  @throws IOException
     */
    public static boolean copyAssetToData(Context context, String asset) throws IOException { 
    	boolean is_copy = false;
        InputStream source = context.getAssets().open(new File(asset).getPath());
        if(source!=null)
        {
        	String destFileFullPath = context.getFilesDir() + File.separator + asset;
        	File dest_file = new File(destFileFullPath);
            
            FileOutputStream destination = new FileOutputStream(dest_file);
	        byte[] buffer = new byte[1024];
	        int nread;
	 
	        while ((nread = source.read(buffer)) != -1) {
	            if (nread == 0) {
	                nread = source.read();
	                if (nread < 0)
	                    break;
	                destination.write(nread);
	                continue;
	            }
	            destination.write(buffer, 0, nread);
	        }        
	        
	        destination.close();	        
	           
	        dest_file.setExecutable(true);  
	        dest_file.setReadable(true);	        
	        dest_file = null;
	        is_copy = true;
        }
        return is_copy;
    }
    
    public static boolean CheckNeedCopy(Context context) throws IOException
	{
		boolean is_need_copy = false;
			
		//判断脚本文件是否存在
		File patch_file = new File(context.getFilesDir()+ File.separator + PATCH_FILE_RELATIVE_PATH);
		//不存在才执行后续的事情
		if(!patch_file.exists()){
			
			String files[] = context.getAssets().list(SYSPATCH_DIR_RELATIVE_PATH);
			if(files!=null && files.length>0){
				for(int i = 0; i < files.length; i++){
					Log.d(TAG, "filename " + files[i]);
					if(PATCH_FILENAME.equals(files[i])){
						is_need_copy = true; //assets中存在脚本文件，认为需要copy
						break;
					}
				}
			}
		}	
		
		return is_need_copy;
	}
    
    public static boolean CopyAssetsToData(Context context) throws IOException
	{
		boolean is_copy = false;
		
		try {	
			//判断脚本文件是否存在
			File patch_file = new File(context.getFilesDir()+ File.separator + PATCH_FILE_RELATIVE_PATH);
			//不存在才执行后续的事情
			if(!patch_file.exists()){
				//copy文件
				String files[] = context.getAssets().list(SYSPATCH_DIR_RELATIVE_PATH);
				if(files!=null && files.length>0){
					//在私有目录创建文件夹
					File syspatch_dir = new File(context.getFilesDir() + File.separator +SYSPATCH_DIR_RELATIVE_PATH);
					if(!syspatch_dir.exists()){
						syspatch_dir.mkdirs();
					}
					
					for(int i = 0; i < files.length; i++){
						Log.d(TAG, "filename " + files[i]);
						String src_filepath = SYSPATCH_DIR_RELATIVE_PATH + File.separator + files[i];
						is_copy = copyAssetToData(context, src_filepath);
					}
				}				
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		return is_copy;	
	}
    	
	//执行脚本
    public static boolean execSystemPatch(String base_dir, String patch_filepath){
    	boolean is_patch = false;
    	if(patch_filepath!=null){
	    	Shell shell = new Shell();
	    	File path_file = new File(patch_filepath);
	    	if(path_file.exists() && path_file.canExecute()){
	    		String out_msg = shell.routon_client_exec(patch_filepath + " " + base_dir, -1);
	    		Log.d(TAG, out_msg);
	    		is_patch = true;
	    	}
    	}
    	return is_patch;
    }
}
