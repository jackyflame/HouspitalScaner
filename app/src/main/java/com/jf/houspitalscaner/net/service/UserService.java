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
     * @param name:姓名
     * @param idcard：身份证号
     * @param sex：性别
     * @param birthday：出生日期
     * @param nation：民族
     * @param address：地址
     * @param hospital：医院（字典）
     * @param idcardPhotoId：现场照片id
     * */
    @GET("hospitalRecordController.do?record")
    Observable<Response<RespEntity<String>>> record(@Query(value = "name") String name, @Query(value = "idcard") String idcard
                                                    ,@Query(value = "sex") String sex, @Query(value = "birthday") String birthday
                                                    ,@Query(value = "nation") String nation, @Query(value = "address") String address
                                                    ,@Query(value = "hospital") String hospital
                                                    ,@Query(value = "idcardPhotoId") String idcardPhotoId
                                                    ,@Query(value = "scenePhotoId") String scenePhotoId);

    /**
     * 上传头像
     * @param file 文件信息
     * */
    @Multipart
    @POST("fuelingRecordController.do?uploadPhoto")
    Observable<Response<RespEntity<ImageEntity>>> uploadPhoto(@Part MultipartBody.Part file);
}
