package com.jf.houspitalscaner.net.entity;

import android.graphics.Bitmap;

import com.haozi.baselibrary.net.entity.BaseNetEntity;

/**
 * Created by Haozi on 2017/8/13.
 */

public class IDInfor extends BaseNetEntity {

    //身份证信息
    private String name;
    private String sex;
    private String nation;//民族
    private String year;
    private String month;
    private String day;
    private String address;
    private String num;
    private String headerImg;
    private Bitmap bmps;

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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
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
