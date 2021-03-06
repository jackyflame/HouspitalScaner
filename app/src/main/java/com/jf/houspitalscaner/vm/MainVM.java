package com.jf.houspitalscaner.vm;

import android.app.Activity;
import android.content.Intent;
import android.databinding.Bindable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.haozi.baselibrary.constants.SPKeys;
import com.haozi.baselibrary.db.MyShareDbHelper;
import com.haozi.baselibrary.event.HttpEvent;
import com.haozi.baselibrary.net.retrofit.ReqCallback;
import com.haozi.baselibrary.utils.BitmapUtils;
import com.haozi.baselibrary.utils.FileUtil;
import com.haozi.baselibrary.utils.StringUtil;
import com.haozi.baselibrary.utils.SystemUtil;
import com.haozi.baselibrary.utils.ViewUtils;
import com.jf.houspitalscaner.BR;
import com.jf.houspitalscaner.R;
import com.jf.houspitalscaner.base.vm.BaseVM;
import com.jf.houspitalscaner.db.UserPresent;
import com.jf.houspitalscaner.net.entity.IDInfor;
import com.jf.houspitalscaner.net.entity.ImageEntity;
import com.jf.houspitalscaner.ui.MainActivity;
import com.jf.scanerlib.ClientReadCardActivity;
import com.routon.idr.idrinterface.readcard.BCardInfo;

import java.io.File;


/**
 * Created by admin on 2017/8/9.
 */

public class MainVM extends BaseVM<UserPresent> {

    private MainActivity activity;
    private IDInfor idInfor;
    private AlertDialog msgDialog;

    public MainVM(MainActivity activity) {
        super(new UserPresent());
        this.activity = activity;
    }

    @Bindable
    public IDInfor getIdInfor() {
        return idInfor;
    }

    public void setIdInfor(BCardInfo idInforNew) {
        if(idInforNew == null){
            this.idInfor = null;
        }else{
            this.idInfor = new IDInfor(idInforNew);
            uploadIdHeader();
        }
        notifyPropertyChanged(BR.idInfor);
    }

    public String getNowHospital() {
        return MyShareDbHelper.getString(SPKeys.SPKEY_HOSPITAL,"还没有设置当前医院");
    }

    public void setHospital(String hospital){
        MyShareDbHelper.putString(SPKeys.SPKEY_HOSPITAL,hospital);
    }

    public void onScanClick(View view){
        uploadScanInfo();
    }

    public void onImageTakeClick(View view){
        SystemUtil.takePicture(activity,activity.INPUT_CONTENT_TACKPIC);
    }

    public void uploadIdHeader(){
        if(idInfor == null){
            ViewUtils.Toast(activity,"请重新扫描身份证");
            return;
        }
        boolean isSuccess = BitmapUtils.writeBmpToSDCard(idInfor.getBmps(), FileUtil.PROJECT_IMAGE_HEADER_CACHE,100);
        if(isSuccess){
            mPrensent.uploadPhoto(FileUtil.PROJECT_IMAGE_HEADER_CACHE, new ReqCallback<ImageEntity>() {
                @Override
                public void onReqStart() {
                    activity.showProgressDialog(false,"上传照片中");
                }
                @Override
                public void onNetResp(ImageEntity response) {
                    if(response != null && !StringUtil.isEmpty(response.getId()) && idInfor != null){
                        idInfor.setHeaderImg(response.getId());
                        //uploadScanInfo();
                    }else{
                        activity.dismissProgressDialog();
                        ViewUtils.Toast(activity,"上传身份证头像失败，请重新扫描身份证上传");
                    }
                }
                @Override
                public void onReqError(HttpEvent httpEvent) {
                    activity.dismissProgressDialog();
                    ViewUtils.Toast(activity,"上传身份证头像失败，请重新扫描身份证上传");
                }
            });
        }
    }

    private void uploadScanInfo(){
        if(idInfor == null){
            ViewUtils.Toast(activity,"请重新扫描身份证");
            return;
        }
        if(StringUtil.isEmpty(idInfor.getHeaderImg())){
            ViewUtils.Toast(activity,"上传身份证头像失败，请重新扫描身份证上传");
            return;
        }
        mPrensent.record(idInfor, new ReqCallback<String>() {
            @Override
            public void onReqStart() {
                activity.showProgressDialog(false,"上传信息中");
            }
            @Override
            public void onNetResp(String response) {
                activity.dismissProgressDialog();
                msgDialog = ViewUtils.showMsgDialog(activity,"上传身份证信息成功");
                activity.postDelayDialogDissmiss();
                //清空记录
                setIdInfor(null);
                activity.cleanPic();
            }
            @Override
            public void onReqError(HttpEvent httpEvent) {
                activity.dismissProgressDialog();
                ViewUtils.Toast(activity,"上传身份证信息失败，请重新扫描");
            }
        });
    }

    public void uploadImage(File pic) {
        mPrensent.uploadPhoto(pic, new ReqCallback<ImageEntity>() {
            @Override
            public void onReqStart() {
                activity.showProgressDialog();
            }
            @Override
            public void onNetResp(ImageEntity response) {
                activity.dismissProgressDialog();
                if(idInfor != null && response != null){
                    idInfor.setTakePic(response.getId());
                }
            }
            @Override
            public void onReqError(HttpEvent httpEvent) {
                activity.dismissProgressDialog();
                ViewUtils.Toast(activity,"上传失败："+httpEvent.getMessage());
                activity.cleanPic();
            }
        });
    }

    public void dismissDialog(){
        if(msgDialog != null && msgDialog.isShowing()){
            msgDialog.dismiss();
        }
    }
}
