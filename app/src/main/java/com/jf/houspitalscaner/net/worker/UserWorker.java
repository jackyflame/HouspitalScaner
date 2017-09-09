package com.jf.houspitalscaner.net.worker;

import com.haozi.baselibrary.net.retrofit.BaseWorker;
import com.haozi.baselibrary.net.retrofit.ReqCallback;
import com.haozi.baselibrary.net.retrofit.RetrofitHelper;
import com.jf.houspitalscaner.net.entity.IDInfor;
import com.jf.houspitalscaner.net.entity.ImageEntity;
import com.jf.houspitalscaner.net.entity.UserEntity;
import com.jf.houspitalscaner.net.service.UserService;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Query;

/**
 * Created by Android Studio.
 * ProjectName: ChongQingHaoLi
 * Author: Haozi
 * Date: 2017/6/10
 * Time: 18:24
 */

public class UserWorker extends BaseWorker {

    private final UserService userService;

    public UserWorker() {
        userService = RetrofitHelper.getInstance().callAPI(UserService.class);
    }

    public void registerOrLogin(String username, String password,ReqCallback<UserEntity> callback){
        defaultCall(userService.registerOrLogin(username,password),callback);
    }

    public void record(IDInfor idInfor, ReqCallback<String> callback){
        record(idInfor.getName(),idInfor.getNum(),idInfor.getSex(),idInfor.getBirthday(),
                idInfor.getNation(),idInfor.getAddress(),idInfor.getHospital(),idInfor.getHeaderImg(),
                idInfor.getTakePic(),callback);
    }

    public void record(String name,String idcard,String sex,String birthday,String nation,
                       String address,String hospital,String idcardPhotoId,String scenePhotoId,ReqCallback<String> callback){
        defaultCall(userService.record(name,idcard,sex,birthday,nation,address,hospital,idcardPhotoId,scenePhotoId),callback);
    }

    public void uploadPhoto(String filePath,ReqCallback<ImageEntity> callback){
        File file = new File(filePath);
        uploadPhoto(file,callback);
    }

    public void uploadPhoto(File file,ReqCallback<ImageEntity> callback){
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"),file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploadFile",file.getName(),requestBody);
        defaultCall(userService.uploadPhoto(body),callback);
    }
}
