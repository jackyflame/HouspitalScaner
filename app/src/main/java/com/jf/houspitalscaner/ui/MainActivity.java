package com.jf.houspitalscaner.ui;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.haozi.baselibrary.utils.StringUtil;
import com.haozi.baselibrary.utils.ViewUtils;
import com.jf.houspitalscaner.R;
import com.jf.houspitalscaner.databinding.ActivityMainBinding;
import com.jf.houspitalscaner.vm.MainVM;
import com.routon.idr.idrinterface.readcard.ReadType;

public class MainActivity extends BaseReadCardActivity<ActivityMainBinding,MainVM> {

    private ImageView headerImg;
    private Handler mHanlder;
    public static final int HANDLER_DISMISS = 4000;

    @Override
    protected void initView() {
        //绑定布局值
        bindLayout(R.layout.activity_main,new MainVM(this));
        mButtonPause = findViewById(R.id.mButtonPause);
        mButtonStop = findViewById(R.id.mButtonStop);
        mButtonStart = findViewById(R.id.mButtonStart);
        headerImg = findViewById(R.id.img_header);
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
    }

    public void cleanPic() {
        //headerImg.setImageDrawable(null);
    }

    public void postDelayDialogDissmiss() {
        if(mHanlder != null){
            mHanlder.sendEmptyMessageDelayed(HANDLER_DISMISS,5000);
        }
    }
}
