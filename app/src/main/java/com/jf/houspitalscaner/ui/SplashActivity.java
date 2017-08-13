package com.jf.houspitalscaner.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.haozi.baselibrary.event.HttpEvent;
import com.haozi.baselibrary.log.LogUtil;
import com.haozi.baselibrary.net.config.ErrorType;
import com.haozi.baselibrary.net.retrofit.ReqCallback;
import com.jf.houspitalscaner.R;
import com.jf.houspitalscaner.base.BaseDBActivity;
import com.jf.houspitalscaner.databinding.ActivitySplashBinding;
import com.jf.houspitalscaner.net.entity.UserEntity;
import com.jf.houspitalscaner.vm.SplashVM;

/**
 * Created by Android Studio.
 * ProjectName: ChongQingHaoLi
 * Author: Haozi
 * Date: 2017/6/4
 * Time: 16:28
 */

public class SplashActivity extends BaseDBActivity<ActivitySplashBinding,SplashVM> {

    private Handler mHandler;
    private Runnable mRunner;
    private long startTime;
    private static final long SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //绑定布局值
        bindLayout(R.layout.activity_splash,new SplashVM(this));
        //自动登录
        if(viewModel.isLogin()){
            viewModel.requestLoginToken(new ReqCallback<UserEntity>() {
                @Override
                public void onReqStart() {
                    startTime = System.currentTimeMillis();
                }
                @Override
                public void onNetResp(UserEntity response) {
                    if (response != null) {
                        viewModel.saveUserData(response);
                        resultCheck(viewModel.isLogin());
                    }
                }
                @Override
                public void onReqError(HttpEvent baseEvent) {
                    if (baseEvent.getCode() == ErrorType.ERROR_NETWORK) {
                        resultCheck(viewModel.isLogin());
                    } else {
                        viewModel.cleanUserData();
                        resultCheck(false);
                    }
                }
            });
        }else{
            startTime = System.currentTimeMillis();
            resultCheck(false);
        }
    }

    private void resultCheck(final boolean isLogin) {
        final long duration = System.currentTimeMillis() - startTime;
        if (duration > SPLASH_DURATION) {
            enterNextActivity(isLogin);
        } else {
            postRunnable(isLogin, SPLASH_DURATION - duration);
        }
    }

    private void enterNextActivity(boolean isLogin) {
        LogUtil.i("SplashActivity","enterNextActivity MainTmpActivity or LoginActivity----");
        if(isLogin){
            startActivity(new Intent(this,MainActivity.class));
        }else{

        }
        finish();
    }

    private void postRunnable(final boolean isLogin, final long delay) {
        if(mHandler == null){
            mHandler = new Handler();
        }
        removeRunnable();
        mRunner = new Runnable() {
            @Override
            public void run() {
                enterNextActivity(isLogin);
            }
        };
        mHandler.postDelayed(mRunner, delay);
    }

    private void removeRunnable() {
        if (mRunner != null && mHandler != null) mHandler.removeCallbacks(mRunner);
    }

    @Override
    public void onBackPressed() {
        removeRunnable();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        removeRunnable();
        super.onDestroy();
    }
}
