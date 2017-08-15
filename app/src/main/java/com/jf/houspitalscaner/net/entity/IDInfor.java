package com.jf.houspitalscaner.net.entity;

import android.graphics.Bitmap;

import com.haozi.baselibrary.net.entity.BaseNetEntity;
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
        this.birthday = birthday;
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
}
