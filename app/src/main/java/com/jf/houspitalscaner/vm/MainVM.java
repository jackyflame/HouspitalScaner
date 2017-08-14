package com.jf.houspitalscaner.vm;

import android.app.Activity;
import android.content.Intent;
import android.databinding.Bindable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.haozi.baselibrary.constants.SPKeys;
import com.haozi.baselibrary.db.MyShareDbHelper;
import com.jf.houspitalscaner.BR;
import com.jf.houspitalscaner.R;
import com.jf.houspitalscaner.base.vm.BaseVM;
import com.jf.houspitalscaner.net.entity.IDInfor;
import com.jf.scanerlib.ClientReadCardActivity;


/**
 * Created by admin on 2017/8/9.
 */

public class MainVM extends BaseVM {

    private Activity activity;
    private IDInfor idInfor;

    public MainVM(FragmentActivity activity) {
        this.activity = activity;

        idInfor = new IDInfor();
        idInfor.setName("张三");
        idInfor.setNum("510622198805052211");
        idInfor.setSex("男");
        idInfor.setNation("汉族");
        idInfor.setAddress("四川省成都市成华区将军路223号");
        idInfor.setYear("1988");
        idInfor.setMonth("05");
        idInfor.setDay("05");
        Bitmap bmp= BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);
        idInfor.setBmps(bmp);
    }

    @Bindable
    public IDInfor getIdInfor() {
        return idInfor;
    }

    public void setIdInfor(IDInfor idInfor) {
        this.idInfor = idInfor;
        notifyPropertyChanged(BR.idInfor);
    }

    public String getNowHospital() {
        return MyShareDbHelper.getString(SPKeys.SPKEY_HOSPITAL,"还没有设置当前医院");
    }

    public void setHospital(String hospital){
        MyShareDbHelper.putString(SPKeys.SPKEY_HOSPITAL,hospital);
    }

    public void onScanClick(View view){
        Intent intent = new Intent(activity,ClientReadCardActivity.class);
        activity.startActivity(intent);
    }
}
