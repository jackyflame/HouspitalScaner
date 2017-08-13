package com.jf.houspitalscaner.net.worker;

import com.haozi.baselibrary.net.retrofit.BaseWorker;
import com.haozi.baselibrary.net.retrofit.ReqCallback;
import com.haozi.baselibrary.net.retrofit.RetrofitHelper;
import com.jf.houspitalscaner.net.entity.ImageEntity;
import com.jf.houspitalscaner.net.entity.UserEntity;
import com.jf.houspitalscaner.net.service.UserService;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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

    public void modifyUserInfo(String nickname, String mobile,String userId,ReqCallback<UserEntity> callback){
        defaultCall(userService.modifyUserInfo(nickname,mobile,userId),callback);
    }

    public void uploadUserPhoto(String filePath,String userId,ReqCallback<ImageEntity> callback){
        File file = new File(filePath);
        uploadUserPhoto(file,userId,callback);
    }

    public void uploadUserPhoto(File file,String userId,ReqCallback<ImageEntity> callback){
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"),file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploadFile",file.getName(),requestBody);
        defaultCall(userService.uploadUserPhoto(body,userId),callback);
    }
}
