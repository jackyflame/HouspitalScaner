package com.jf.houspitalscaner.net.service;

import com.haozi.baselibrary.net.entity.RespEntity;
import com.jf.houspitalscaner.net.entity.ImageEntity;
import com.jf.houspitalscaner.net.entity.UserEntity;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by Android Studio.
 * ProjectName: ChongQingHaoLi
 * Author: Haozi
 * Date: 2017/6/10
 * Time: 18:21
 */

public interface UserService {

    /**
     * 注册登录
     * @param username 用户名
     * @param password 密码
     * */
    @GET("userController.do?registerOrLogin")
    Observable<Response<RespEntity<UserEntity>>> registerOrLogin(@Query(value = "username") String username, @Query(value = "password") String password);

    /**
     * 注册登录
     * @param nickname 昵称
     * @param mobile 手机号码
     * @param userId 用户id
     * */
    @GET("userController.do?modifyUserInfo")
    Observable<Response<RespEntity<UserEntity>>> modifyUserInfo(@Query(value = "nickname") String nickname, @Query(value = "mobile") String mobile, @Query(value = "userId") String userId);

    /**
     * 上传头像
     * @param file 文件信息
     * */
    @Multipart
    @POST("userController.do?uploadUserPhoto")
    Observable<Response<RespEntity<ImageEntity>>> uploadUserPhoto(@Part MultipartBody.Part file, @Query(value = "userId") String userId);

}
