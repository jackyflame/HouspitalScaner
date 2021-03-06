package com.jf.houspitalscaner.db;

import com.alibaba.fastjson.JSON;
import com.haozi.baselibrary.constants.SPKeys;
import com.haozi.baselibrary.db.BasePresent;
import com.haozi.baselibrary.db.MyShareDbHelper;
import com.haozi.baselibrary.event.HttpEvent;
import com.haozi.baselibrary.net.config.ErrorType;
import com.haozi.baselibrary.net.retrofit.ReqCallback;
import com.haozi.baselibrary.utils.StringUtil;
import com.jf.houspitalscaner.net.entity.IDInfor;
import com.jf.houspitalscaner.net.entity.ImageEntity;
import com.jf.houspitalscaner.net.entity.UserEntity;
import com.jf.houspitalscaner.net.worker.UserWorker;

import java.io.File;

/**
 * Created by Haozi on 2017/5/27.
 */
public class UserPresent extends BasePresent {

    /**静态单例初始化*/
    private static class SingletonHolder{
        /** 静态初始化器，由JVM来保证线程安全*/
        private static UserPresent instance = new UserPresent();
    }
    /**单例静态引用*/
    public static UserPresent getInstance(){
        return SingletonHolder.instance;
    }

    private final UserWorker userWorker;

    public UserPresent() {
        this.userWorker = new UserWorker();
    }

    public UserEntity getUser(){
        String userInfoStr = MyShareDbHelper.getString(SPKeys.SPKEY_USERINFO,"");
        if(StringUtil.isEmpty(userInfoStr)){
            return null;
        }
        UserEntity user = JSON.parseObject(userInfoStr,UserEntity.class);
        return user;
    }

    public void saveUser(UserEntity user){
        if(user == null){
            MyShareDbHelper.putString(SPKeys.SPKEY_USERINFO,"");
        }else{
            MyShareDbHelper.putString(SPKeys.SPKEY_USERINFO,JSON.toJSONString(user));
        }
    }

    public void cleanUserData() {
        MyShareDbHelper.putString(SPKeys.SPKEY_USERINFO,"");
    }

    public String getNowUserId(){
        UserEntity user = getUser();
        if(user == null){
            return null;
        }
        return user.getId();
    }

    public void registerOrLogin(String username, String password,ReqCallback<UserEntity> callback){
        userWorker.registerOrLogin(username,password,callback);
    }

    public void record(IDInfor idInfor, ReqCallback<String> callback){
        idInfor.setHospital(MyShareDbHelper.getString(SPKeys.SPKEY_HOSPITAL,"未设置"));
        userWorker.record(idInfor,callback);
    }

    public void uploadPhoto(String filePath,ReqCallback<ImageEntity> callback){
        userWorker.uploadPhoto(filePath,callback);
    }

    public void uploadPhoto(File file, ReqCallback<ImageEntity> callback){
        userWorker.uploadPhoto(file,callback);
    }
}
