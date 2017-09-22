package com.jf.houspitalscaner.net.entity;

import android.graphics.Bitmap;

import com.haozi.baselibrary.net.entity.BaseNetEntity;
import com.haozi.baselibrary.utils.StringUtil;
import com.routon.idr.idrinterface.readcard.BCardInfo;

/**
 * Created by Haozi on 2017/8/13.
 */

public class IDInfor extends BaseNetEntity {

    //身份证信息
    private String name;
    private String sex;
    private String nation;//民族
    private String birthday;
    private String address;
    private String num;
    private String headerImg;
    private Bitmap bmps;

    //采集图象
    private String takePic;
    private String hospital;

    public IDInfor() {
        super();
    }

    public IDInfor(BCardInfo idInfor) {
        setName(idInfor.name);
        setSex(idInfor.gender);
        setNation(idInfor.nation);
        setBirthday(idInfor.birthday);
        setAddress(idInfor.address);
        setNum(idInfor.id);
        setHeaderImg(idInfor.photo_path);
        setBmps(idInfor.photo);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        if(!StringUtil.isEmpty(birthday) && birthday.length() == 8){
            this.birthday = birthday.substring(0,4)+"年"+birthday.substring(4,6)+"月"+birthday.substring(6,8)+"日";
        }else{
            this.birthday = birthday;
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getHeaderImg() {
        return headerImg;
    }

    public void setHeaderImg(String headerImg) {
        this.headerImg = headerImg;
    }

    public Bitmap getBmps() {
        return bmps;
    }

    public void setBmps(Bitmap bmps) {
        this.bmps = bmps;
    }

    public String getTakePic() {
        return takePic;
    }

    public void setTakePic(String takePic) {
        this.takePic = takePic;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }
}
