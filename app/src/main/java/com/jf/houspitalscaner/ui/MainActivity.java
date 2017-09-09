package com.jf.houspitalscaner.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.haozi.baselibrary.utils.BitmapUtils;
import com.haozi.baselibrary.utils.StringUtil;
import com.haozi.baselibrary.utils.SystemUtil;
import com.haozi.baselibrary.utils.ViewUtils;
import com.jf.houspitalscaner.R;
import com.jf.houspitalscaner.databinding.ActivityMainBinding;
import com.jf.houspitalscaner.vm.MainVM;
import com.routon.idr.idrinterface.readcard.ReadType;

import java.io.File;

public class MainActivity extends BaseReadCardActivity<ActivityMainBinding,MainVM> {

    private ImageView headerImg;
    private Handler mHanlder;
    private ImageView img_takepic;
    public static final int HANDLER_DISMISS = 4000;
    /**拍摄图片*/
    public static final int INPUT_CONTENT_TACKPIC = 1002;
    /**添加图片*/
    public static final int INPUT_CONTENT_ADDPIC = 1003;

    @Override
    protected void initView() {
        //绑定布局值
        bindLayout(R.layout.activity_main,new MainVM(this));
        mButtonPause = findViewById(R.id.mButtonPause);
        mButtonStop = findViewById(R.id.mButtonStop);
        mButtonStart = findViewById(R.id.mButtonStart);
        headerImg = findViewById(R.id.img_header);
        img_takepic = mBinding.imgTakepic;
        //初始化按钮
        super.initView();

        mHanlder = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == HANDLER_DISMISS){
                    viewModel.dismissDialog();
                }
            }
        };
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set, menu);
        //MenuItem searchItem = menu.findItem(R.id.action_set);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_now:
                showNowHospital();
                return true;
            case R.id.action_set:
                setHospital();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showNowHospital(){
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(viewModel.getNowHospital())
                .setPositiveButton("确定",null)
                .create()
                .show();
    }

    private void setHospital(){
        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("请设置医院")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newHospital = ViewUtils.getTextFromTextView(editText);
                        if(StringUtil.isEmpty(newHospital)){
                            showToast("医院名称不能为空");
                        }else{
                            viewModel.setHospital(newHospital);
                            dialogInterface.dismiss();;
                            showToast("设置成功");
                        }
                    }
                })
                .setNegativeButton("取消",null)
                .create()
                .show();
    }

    public void updateTextCardInfo(boolean flag, ReadType rdType){
        if(flag == false){
            viewModel.setIdInfor(null);
        }else{
            viewModel.setIdInfor(mBCardInfo);
        }
        cleanPic();
    }

    public void cleanPic() {
        //headerImg.setImageDrawable(null);
        img_takepic.setImageResource(R.mipmap.ic_takephoto);
    }

    public void postDelayDialogDissmiss() {
        if(mHanlder != null){
            mHanlder.sendEmptyMessageDelayed(HANDLER_DISMISS,5000);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //检查返回结果状态
        if(requestCode == INPUT_CONTENT_TACKPIC || requestCode == INPUT_CONTENT_ADDPIC ){
            //返回图片处理方式
            if(resultCode == RESULT_OK){
                switch (requestCode) {
                    case INPUT_CONTENT_TACKPIC:
                        //得到图片
                        File pic = SystemUtil.getTakePicFile();
                        //viewModel.uploadHeaderImage(pic);
                        Bitmap bitmap = BitmapUtils.getScaleBitmap(pic.getPath(),800,600);
                        img_takepic.setImageBitmap(bitmap);
                        viewModel.uploadImage(pic);
                        break;
                    case INPUT_CONTENT_ADDPIC:
                        //得到图片
                        pic = SystemUtil.getAddPicFile(data);
                        //viewModel.uploadHeaderImage(pic);
                        bitmap = BitmapFactory.decodeFile(pic.getPath());
                        img_takepic.setImageBitmap(bitmap);
                        viewModel.uploadImage(pic);
                        break;
                }
            }
        }
    }
}
