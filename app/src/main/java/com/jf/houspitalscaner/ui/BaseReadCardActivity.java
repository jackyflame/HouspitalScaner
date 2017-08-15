package com.jf.houspitalscaner.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.jf.houspitalscaner.base.BaseDBActivity;
import com.jf.houspitalscaner.base.vm.BaseVM;
import com.jf.scanerlib.ClientBCReceiver;
import com.jf.scanerlib.MainMsg;
import com.jf.scanerlib.NewActivity;
import com.jf.scanerlib.ReadCardSound;
import com.jf.scanerlib.SystemPatch;
import com.jf.scanerlib.WaitingForDialog;
import com.routon.idr.device.sdk.Storage;
import com.routon.idr.idrinterface.readcard.BCardInfo;
import com.routon.idr.idrinterface.readcard.IReadCardService;
import com.routon.idr.idrinterface.readcard.ReadMode;
import com.routon.idr.idrinterface.readcard.ReadState;
import com.routon.idr.idrinterface.readcard.ReadType;
import com.routon.idr.idrinterface.readcard.ReaderBean;
import com.routon.idr.path.iDRPath;
import com.routon.idr.version.iDRVersion;
import com.routon.idrconst.Action;
import com.routon.idrconst.LogWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

public class BaseReadCardActivity<X extends ViewDataBinding, T extends BaseVM> extends BaseDBActivity<X, T> implements OnClickListener {

    private Button mButtonPause;
    private Button mButtonStop;
    private Button mButtonStart;

    private final String TAG = "BaseReadCardActivity";
    private String mTextStatus;
    private BCardInfo mBCardInfo;
    private String cardANo;
    private ArrayAdapter spinnerModeAdapter;
    private ArrayAdapter spinnerTypeAdapter;


    private ReadState mClientState = ReadState.st_unknown;
    private static final int ROLL_INTERVAL = 20;  //滚动执行间隔时间 20ms
    private static final int SCREEN_ON_INTERVAL = 60000;//60s

    public IReadCardService mReadCardService;
    public ClientBCReceiver mReceiver;
    public boolean isServRDcardConned = false;
    public boolean roll_timer_running = false;
    ReaderBean mRdrBean = null;
    private ReadCardSound soundPlayer;//读卡提示音
    public boolean isNeedStart = false;
    private final static String CLIENT_LOG_FILENAME = "readcard.txt";
    ReadState m_lastServRdSt = ReadState.st_unknown;

    private WaitingForDialog dialog;
    private final static String TERM_INFO_FILENAME = "iDR410.txt";
    private final static String CARDA_FILENAME = "employee.txt";
    private final static String IEMI_FILEPATH = "/cache/meid.txt";

    private void setClientState(ReadState clientState) {
        if (mClientState != clientState) {
            Log.d(TAG, mClientState + " to " + clientState);
        }
        mClientState = clientState;
    }

    private Boolean procOnAny(Message msg) {
        Boolean is_processed = false;
        switch (msg.what) {
            case MainMsg.EVT_APP_EXIT://程序退出
                startRollingTimer(ROLL_INTERVAL, false);
                if (mReadCardService != null) {
                    try {
                        //停止读卡
                        mReadCardService.stop();
                        unbindReadCardService();
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    is_processed = true;
                }
                finish();
                break;
            case MainMsg.EVT_UI_HOME://home键
            case MainMsg.EVT_SCREEN_OFF://关屏
                startRollingTimer(ROLL_INTERVAL, false);
                if (mReadCardService != null) {
                    try {
                        //停止读卡
                        mReadCardService.stop();

                        //跳转到st_idle状态
                        setClientState(ReadState.st_idle);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mButtonPause.setEnabled(false);
                    mButtonStop.setEnabled(false);
                    mButtonStart.setEnabled(true);
                    is_processed = true;
                }

                break;
            case MainMsg.EVT_ACTIVITY_TO_BACKGROUND:
                startRollingTimer(ROLL_INTERVAL, false);
                //状态读卡停止会转入st_idle，无需再暂停
                if (mClientState != ReadState.st_idle) {
                    if (mReadCardService != null) {
                        try {
                            //暂停读卡
                            mReadCardService.pause();

                            //跳转到st_idle状态
                            setClientState(ReadState.st_opened);
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        mTextStatus = "已暂停读卡";
                        updateTextCardInfo(false, ReadType.B);
                        updateTextStatus(mTextStatus);
                        mButtonPause.setEnabled(false);
                        mButtonStop.setEnabled(true);
                        mButtonStart.setEnabled(true);
                    }
                }
                Intent intent = new Intent(this, NewActivity.class);
                startActivity(intent);
                is_processed = true;
                break;
            case MainMsg.EVT_GET_VERSION: {
                if (mReadCardService != null) {
                    try {
                        String ver = mReadCardService.getVersion();
                        if (ver != null) {
                            showToast(ver);
                        } else {
                            showToast("读服务版本号失败!");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
            case MainMsg.EVT_SERV_DISCONNECTED_RDCARD: {
                mTextStatus = "读卡服务程序已退出";

                mButtonPause.setEnabled(false);
                mButtonStop.setEnabled(false);
                mButtonStart.setEnabled(false);

                bindReadCardService();
                mTextStatus = "正在初始化";

                //跳转到st_init状态
                setClientState(ReadState.st_init);
                //启动轮询定时器
                startRollingTimer(ROLL_INTERVAL, true);

                is_processed = true;
            }
            break;
            case MainMsg.EVT_READMODE_CHANGED_SERV: {
                //修改读卡模式
                ReadMode rdMode = (ReadMode) msg.obj;
            }
            break;
            case MainMsg.EVT_READTYPE_CHANGED_SERV: {
                //修改读卡类型
                ReadType rdType = (ReadType) msg.obj;
            }
            break;

            case MainMsg.EVT_UPDATE_START:
                dialog = new WaitingForDialog(this, "正在更新系统......");
                dialog.showWaiting();
                break;
            case MainMsg.EVT_UPDATE_SUCCESS: {
                dialog.dismissWaiting();
                WaitingForDialog dialogResult = new WaitingForDialog(this, "更新系统成功，重启设备生效！");
                dialogResult.showResult();
            }
            break;
            case MainMsg.EVT_UPDATE_FAIL: {
                dialog.dismissWaiting();
                WaitingForDialog dialogResult = new WaitingForDialog(this, "更新系统失败!");
                dialogResult.showResult();
            }
            break;
            default:
                break;
        }
        return is_processed;
    }

    private int procOnInit(Message msg) {
        switch (msg.what) {
            case MainMsg.EVT_SERV_CONNECTED_RDCARD: {
                //停止轮询定时器
                startRollingTimer(ROLL_INTERVAL, false);
                //读卡服务连接成功后,等待用户的界面操作
                if (mReadCardService != null) {
                    isServRDcardConned = true;

                    mTextStatus = "初始化成功";

                    mButtonPause.setEnabled(false);
                    mButtonStop.setEnabled(false);
                    mButtonStart.setEnabled(true);
                    setClientState(ReadState.st_idle);
                }
            }
            break;
            case MainMsg.EVT_READER_OPENED:
                if (mReadCardService != null) {
                    mTextStatus = "初始化成功";

                    mButtonPause.setEnabled(false);
                    mButtonStop.setEnabled(false);
                    mButtonStart.setEnabled(true);
                    setClientState(ReadState.st_idle);
                }
                break;
            case MainMsg.EVT_TIMEOUT_ROLL: {
                bindReadCardService();

                //启动轮询定时器
                startRollingTimer(ROLL_INTERVAL, true);
            }
            break;
            default:
                break;
        }
        return 0;
    }

    private int procOnIdle(Message msg) {
        switch (msg.what) {
            case MainMsg.EVT_START: {
                //接收到启动读卡请求
                mTextStatus = "正在启动读卡...";
                mButtonPause.setEnabled(false);
                mButtonStop.setEnabled(true);
                mButtonStart.setEnabled(false);

                mRdrBean = (ReaderBean) msg.obj;
                if (mReadCardService != null) {
                    if (mRdrBean != null) {
                        try {
                            isNeedStart = true;
                            ReadState servRdSt = mReadCardService.getState();
                            //查看服务状态，若为opened直接启动，
                            //若为其他状态先发送停止读卡请求,然后等待事件EVT_STOPPED再启动
                            if (servRdSt.equals(ReadState.st_opened)) {
                                mReadCardService.start(mRdrBean);
                            } else {
                                mReadCardService.stop();
                            }
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            break;
            case MainMsg.EVT_STOPPED: {
                //停止成功
                if (isNeedStart) {
                    isNeedStart = false;
                    if ((mReadCardService != null) && (mRdrBean != null)) {
                        try {
                            mReadCardService.start(mRdrBean);
                            startRollingTimer(ROLL_INTERVAL, true);
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } else {
                    mTextStatus = "已停止读卡";
                    updateTextStatus(mTextStatus);
                    mButtonPause.setEnabled(false);
                    mButtonStop.setEnabled(false);
                    mButtonStart.setEnabled(true);
                }
            }
            break;
            case MainMsg.EVT_TIMEOUT_ROLL: {
                //查询读卡服务的状态
                if (mReadCardService != null) {
                    ReadState servRdSt;
                    try {
                        servRdSt = mReadCardService.getState();
                        switch (servRdSt) {
                            case st_idle_auto:
                            case st_idle_online: {
                                if (!m_lastServRdSt.equals(servRdSt)) {
                                    mTextStatus = "请放卡";
                                    updateTextStatus(mTextStatus);
                                    mButtonPause.setEnabled(true);
                                    mButtonStop.setEnabled(true);
                                    mButtonStart.setEnabled(false);
                                }
                                //重启启动查询状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);

                                //状态跳转到st_work
                                setClientState(ReadState.st_work);
                            }
                            case st_cardon_a:
                            case st_cardon_b: {
                                //启动成功

                                //启动查状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);

                                //状态跳转到st_work
                                setClientState(ReadState.st_work);
                            }
                            break;
                            case st_fault: {
                                //启动失败
                                mTextStatus = "设备通信故障";
                                updateTextStatus(mTextStatus);

                                //启动查状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;
                            case st_init: {
                                mTextStatus = "正在启动读卡...";
                                updateTextStatus(mTextStatus);

                                //启动查状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;
                            case st_idle:
                            case st_opened:
                            default: {
                                //启动查状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;
                        }
                        m_lastServRdSt = servRdSt;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
            case MainMsg.EVT_STARTED: {
                //启动成功

                //停定时器
                startRollingTimer(ROLL_INTERVAL, false);

                mTextStatus = "请放卡";
                updateTextStatus(mTextStatus);
                mButtonPause.setEnabled(true);
                mButtonStop.setEnabled(true);
                mButtonStart.setEnabled(false);

                //启动查状态定时器
                startRollingTimer(ROLL_INTERVAL, true);

                //状态跳转到st_work
                setClientState(ReadState.st_work);
            }
            break;
            case MainMsg.EVT_STOP: {
                if (mReadCardService != null) {
                    try {
                        isNeedStart = false;
                        mTextStatus = "正在停止读卡";
                        //停止读卡,且等待EVT_STOPPED事件
                        mReadCardService.stop();
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            break;
            case MainMsg.EVT_TIMEOUT_SCREEN_ON:
                //读卡成功重启亮屏定时器
                keepScreenOff();//关屏
                //end
                break;
            default:
                break;
        }
        return 0;
    }

    private int procOnWork(Message msg) {
        switch (msg.what) {
            case MainMsg.EVT_TIMEOUT_ROLL: {
                if (mReadCardService != null) {
                    //查询读卡服务的状态
                    ReadState servRdSt;
                    try {
                        servRdSt = mReadCardService.getState();
                        switch (servRdSt) {
                            case st_idle_auto:
                            case st_idle_online: {
                                if (!m_lastServRdSt.equals(servRdSt)) {
                                    mTextStatus = "请放卡";
                                    updateTextStatus(mTextStatus);
                                    mButtonPause.setEnabled(true);
                                    mButtonStop.setEnabled(true);
                                    mButtonStart.setEnabled(false);
                                }
                                //重启启动查询状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;
                            case st_cardon_a:
                            case st_cardon_b: {
                                //启动成功
                                //重启启动查询状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;
                            case st_idle:
                            case st_opened: {
                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;
                            case st_init: {
                                mTextStatus = "正在启动读卡...";
                                updateTextCardInfo(false, ReadType.B);
                                updateTextStatus(mTextStatus);
                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;
                            case st_fault: {
                                mTextStatus = "设备故障";
                                updateTextCardInfo(false, ReadType.B);
                                updateTextStatus(mTextStatus);
                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;
                            default:
                                break;
                        }
                        m_lastServRdSt = servRdSt;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
            case MainMsg.EVT_FOUND_A: {
                mTextStatus = "正在读TypeA卡...";
                updateTextStatus(mTextStatus);
            }
            break;
            case MainMsg.EVT_READ_CARD_A_SUCCESS: {
                //播放读卡提示音
                if (soundPlayer != null) {
                    soundPlayer.playSound(ReadCardSound.NORMAL);
                }
                //更新界面显示
                mTextStatus = "读TypeA卡成功";
                byte data[] = (byte[]) msg.obj;
                //modify by pingping 2016.8.2 增加卡序列号长度
                //cardANo=String.format("%02X%02X%02X%02X", data[0], data[1], data[2], data[3]);
                int datalen = msg.arg1;
                cardANo = "";
                //cardANo=String.format("%02X%02X%02X%02X", data[0], data[1], data[2], data[3]);
                for (int i = 0; i < datalen; i++) {
                    cardANo += String.format("%02X", data[i]);
                }
                //end
                updateTextStatus(mTextStatus);
                updateTextCardInfo(true, ReadType.A);

                //读卡成功重启亮屏定时器
                keepScreenOn();//亮屏
                startScreenOnTimer(SCREEN_ON_INTERVAL, false);
                startScreenOnTimer(SCREEN_ON_INTERVAL, true);
                //end
            }
            break;
            case MainMsg.EVT_LEAVE_A: {
                mTextStatus = "TypeA卡已离开";
                updateTextStatus(mTextStatus);
                updateTextCardInfo(false, ReadType.A);
            }
            break;
            case MainMsg.EVT_READ_CARD_A_FAIL: {
                mTextStatus = "读TypeA卡失败";
                updateTextStatus(mTextStatus);
                updateTextCardInfo(false, ReadType.A);

                //读卡成功重启亮屏定时器
                keepScreenOn();//亮屏
                startScreenOnTimer(SCREEN_ON_INTERVAL, false);
                startScreenOnTimer(SCREEN_ON_INTERVAL, true);
                //end
            }
            break;
            case MainMsg.EVT_SELECT_B: {
                mTextStatus = "TypeB卡选卡成功...";
                updateTextStatus(mTextStatus);
            }
            break;
            case MainMsg.EVT_FOUND_B: {
                mTextStatus = "TypeB卡寻卡成功...";
                updateTextStatus(mTextStatus);
            }
            break;
            case MainMsg.EVT_READ_CARD_B_SUCCESS: {
                //播放读卡提示音
                if (soundPlayer != null) {
                    soundPlayer.playSound(ReadCardSound.NORMAL);
                }
                //更新界面显示
                mTextStatus = "读TypeB卡成功";

                mBCardInfo = (BCardInfo) msg.obj;
                updateTextStatus(mTextStatus);
                updateTextCardInfo(true, ReadType.B);

                //读卡成功重启亮屏定时器
                keepScreenOn();//亮屏
                startScreenOnTimer(SCREEN_ON_INTERVAL, false);
                startScreenOnTimer(SCREEN_ON_INTERVAL, true);
                //end
            }
            break;
            case MainMsg.EVT_LEAVE_B: {
                mTextStatus = "TypeB卡已离开";
                updateTextStatus(mTextStatus);
                updateTextCardInfo(false, ReadType.B);
            }
            break;
            case MainMsg.EVT_READ_CARD_B_FAIL: {
                mTextStatus = "读TypeB卡失败";
                updateTextStatus(mTextStatus);
                updateTextCardInfo(false, ReadType.B);

                //读卡成功重启亮屏定时器
                keepScreenOn();//亮屏
                startScreenOnTimer(SCREEN_ON_INTERVAL, false);
                startScreenOnTimer(SCREEN_ON_INTERVAL, true);
                //end
            }
            break;
            case MainMsg.EVT_GET_SAMID: {
                if (mReadCardService != null) {
                    try {
                        String samId = mReadCardService.getSamId();
                        if (samId != null) {
                            //mEditTextSam.setText(samId);
                            //lihuili 2016-06-04 此函数调用仅用于手动读出imei和meid，并存储到SD卡，一般不要放开
                            //write_SAMID_IMEI_MEID_ToFile(samId);
                        } else {
                            //mEditTextSam.setText("读安全模块号失败!");
                        }
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            break;
            case MainMsg.EVT_GET_MCU_VERSION: {
                if (mReadCardService != null) {
                    try {
                        String mcuVer = mReadCardService.getChipVersion();
                        if (mcuVer != null) {
                            //mEditTextMcuVersion.setText(mcuVer);
                        } else {
                            //mEditTextMcuVersion.setText("读单片机版本号失败!");
                        }
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            break;
            case MainMsg.EVT_PAUSE:
                if (mReadCardService != null) {
                    try {
                        mTextStatus = "正在暂停读卡";
                        //暂停读卡,且等待EVT_PAUSED事件
                        mReadCardService.pause();
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;
            case MainMsg.EVT_PAUSED: {
                mTextStatus = "已暂停读卡";
                updateTextCardInfo(false, ReadType.B);
                updateTextStatus(mTextStatus);
                mButtonPause.setEnabled(false);
                mButtonStop.setEnabled(true);
                mButtonStart.setEnabled(true);

                //状态跳转到st_opened
                setClientState(ReadState.st_opened);
            }
            break;
            case MainMsg.EVT_STOP:
                if (mReadCardService != null) {
                    try {
                        mTextStatus = "正在停止读卡";
                        //停止读卡,且等待EVT_STOPPED事件
                        mReadCardService.stop();
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;
            case MainMsg.EVT_STOPPED: {
                mTextStatus = "已停止读卡";
                updateTextCardInfo(false, ReadType.B);
                updateTextStatus(mTextStatus);
                mButtonPause.setEnabled(false);
                mButtonStop.setEnabled(false);
                mButtonStart.setEnabled(true);

                //状态跳转到st_idle
                setClientState(ReadState.st_idle);
            }
            break;
            case MainMsg.EVT_TIMEOUT_SCREEN_ON:
                //读卡成功重启亮屏定时器
                keepScreenOff();//关屏
                //end
                break;
            default:
                break;
        }
        return 0;
    }

    private int procOnOpened(Message msg) {
        switch (msg.what) {
            case MainMsg.EVT_START: {
                //接收到启动读卡请求
                mTextStatus = "正在启动读卡...";
                showProgressDialog(false,mTextStatus);
                mRdrBean = (ReaderBean) msg.obj;
                if ((mReadCardService != null) && (mRdrBean != null)) {
                    //先发送启动读卡请求,然后等待事件EVT_STARTED 或者 查状态定时器到事件EVT_TIMEOUT_ROLL
                    try {
                        mReadCardService.start(mRdrBean);
                        startRollingTimer(ROLL_INTERVAL, true);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            break;
            case MainMsg.EVT_STARTED: {
                //启动成功
                hideProgressDialog();
                //停定时器
                startRollingTimer(ROLL_INTERVAL, false);

                mTextStatus = "请放卡";
                updateTextStatus(mTextStatus);
                mButtonPause.setEnabled(true);
                mButtonStop.setEnabled(true);
                mButtonStart.setEnabled(false);

                //启动查状态定时器
                startRollingTimer(ROLL_INTERVAL, true);

                //状态跳转到st_work
                setClientState(ReadState.st_work);
            }
            break;
            case MainMsg.EVT_TIMEOUT_ROLL: {
                //查询读卡服务的状态
                if (mReadCardService != null) {
                    ReadState servRdSt;
                    try {
                        servRdSt = mReadCardService.getState();
                        switch (servRdSt) {
                            case st_idle_auto:
                            case st_idle_online: {
                                if (!m_lastServRdSt.equals(servRdSt)) {
                                    mTextStatus = "请放卡";
                                    updateTextStatus(mTextStatus);
                                    mButtonPause.setEnabled(true);
                                    mButtonStop.setEnabled(true);
                                    mButtonStart.setEnabled(false);
                                }
                                //重启启动查询状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);
                                //状态跳转到st_work
                                setClientState(ReadState.st_work);
                            }
                            break;
                            case st_cardon_a:
                            case st_cardon_b: {
                                //启动成功
                                //启动查状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);
                                //状态跳转到st_work
                                setClientState(ReadState.st_work);
                            }
                            break;
                            case st_fault: {
                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;
                            case st_init: {
                                mTextStatus = "正在启动读卡...";
                                updateTextStatus(mTextStatus);
                                //启动查状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;
                            case st_idle:
                            case st_opened:
                            default: {

                            }
                            break;
                        }
                        m_lastServRdSt = servRdSt;
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            break;

            case MainMsg.EVT_STOP: {
                //接收到停止读卡请求
                if ((mReadCardService != null)
                        ) {
                    try {
                        mTextStatus = "正在停止读卡";
                        //先发送停止读卡请求,然后等待事件EVT_STOPPED
                        mReadCardService.stop();
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            break;
            case MainMsg.EVT_STOPPED: {
                if ((mReadCardService != null) && (mRdrBean != null)) {
                    mTextStatus = "已停止读卡";

                    mButtonPause.setEnabled(false);
                    mButtonStop.setEnabled(false);
                    mButtonStart.setEnabled(true);
                    //状态跳转到st_idle
                    setClientState(ReadState.st_idle);
                }
            }
            break;
            case MainMsg.EVT_TIMEOUT_SCREEN_ON:
                //读卡成功重启亮屏定时器
                keepScreenOff();//关屏
                //end
                break;
            default:
                break;
        }
        return 0;
    }

    //lihuili 2016-01-14 st_fault是多余, 程序不会调用procOnFault
    private int procOnFault(Message msg) {
        switch (msg.what) {
            case MainMsg.EVT_TIMEOUT_ROLL: {
                if (mReadCardService != null) {
                    //查询读卡服务的状态
                    try {
                        ReadState servRdSt = mReadCardService.getState();
                        switch (servRdSt) {
                            case st_idle_auto:
                            case st_idle_online: {
                                if (!m_lastServRdSt.equals(servRdSt)) {
                                    mTextStatus = "请放卡";
                                    updateTextStatus(mTextStatus);
                                    mButtonPause.setEnabled(true);
                                    mButtonStop.setEnabled(true);
                                    mButtonStart.setEnabled(false);
                                }
                                //重启启动查询状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);

                                //状态跳转到st_work
                                setClientState(ReadState.st_work);
                            }
                            case st_cardon_a:
                            case st_cardon_b: {
                                //状态恢复正常

                                //启动查状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);
                                //状态跳转到st_work
                                setClientState(ReadState.st_work);
                            }
                            break;
                            case st_fault: {
                                mTextStatus = "设备通信故障";
                                updateTextCardInfo(false, ReadType.B);
                                updateTextStatus(mTextStatus);

                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;

                            case st_idle:
                            case st_opened:
                            case st_init:
                            default: {
                                //启动查状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);
                            }
                            break;
                        }
                        m_lastServRdSt = servRdSt;
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            break;
            case MainMsg.EVT_STATE_CHANGE: {
                //停定时器
                startRollingTimer(ROLL_INTERVAL, false);

                //读卡服务状态改变
                ReadState servRdSt = (ReadState) msg.obj;
                if (servRdSt != null) {
                    switch (servRdSt) {
                        case st_idle_auto:
                        case st_idle_online: {
                            if (!m_lastServRdSt.equals(servRdSt)) {
                                mTextStatus = "请放卡";
                                updateTextStatus(mTextStatus);
                                mButtonPause.setEnabled(true);
                                mButtonStop.setEnabled(true);
                                mButtonStart.setEnabled(false);

                                //启动查状态定时器
                                startRollingTimer(ROLL_INTERVAL, true);
                                //状态跳转到st_work
                                setClientState(ReadState.st_work);
                            }
                        }
                        break;
                        case st_cardon_a:
                        case st_cardon_b: {
                            //状态恢复正常

                            //启动查状态定时器
                            startRollingTimer(ROLL_INTERVAL, true);
                            //状态跳转到st_work
                            setClientState(ReadState.st_work);
                        }
                        break;
                        case st_fault: {
                            mTextStatus = "设备通信故障";
                            updateTextCardInfo(false, ReadType.B);
                            updateTextStatus(mTextStatus);

                            //启动查状态定时器
                            startRollingTimer(ROLL_INTERVAL, true);
                        }
                        break;
                        case st_idle:
                        case st_opened:
                        case st_init:
                        default: {
                            //do nothing
                        }
                        break;
                    }
                    m_lastServRdSt = servRdSt;
                }
            }
            break;
            default:
                break;
        }
        return 0;
    }

    private int startRollingTimer(int timeout, boolean enable) {
        if (mHandler != null) {
            if (enable) {
                mHandler.removeMessages(MainMsg.EVT_TIMEOUT_ROLL);
                mHandler.sendEmptyMessageDelayed(MainMsg.EVT_TIMEOUT_ROLL, timeout);
            } else {
                mHandler.removeMessages(MainMsg.EVT_TIMEOUT_ROLL);
            }
        }
        return 0;
    }

    //定时发送保存屏亮消息机制
    private int startScreenOnTimer(int timeout, boolean enable) {
        if (mHandler != null) {
            if (enable) {
                mHandler.removeMessages(MainMsg.EVT_TIMEOUT_SCREEN_ON);
                mHandler.sendEmptyMessageDelayed(MainMsg.EVT_TIMEOUT_SCREEN_ON, timeout);
            } else {
                mHandler.removeMessages(MainMsg.EVT_TIMEOUT_SCREEN_ON);
            }
        }
        return 0;
    }

    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void keepScreenOff() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            //Log.d(TAG,"msg " +msg.what);
            boolean isProcessed = procOnAny(msg);
            if (!isProcessed) {
                switch (mClientState) {
                    case st_init: {
                        procOnInit(msg);
                    }
                    break;
                    case st_idle: {
                        procOnIdle(msg);
                    }
                    break;

                    case st_work: {
                        procOnWork(msg);
                    }
                    break;
                    case st_opened: {
                        procOnOpened(msg);
                    }
                    break;
                    case st_fault: {
                        procOnFault(msg);
                    }
                    break;
                    default:
                        break;
                }
            }
        }
    };

    public void updateTextStatus(String statusStr) {
        //mTextViewStatus.setText(statusStr);
    }

    public void updateTextCardInfo(boolean flag, ReadType rdType) {
        if (flag) {
            if (rdType == ReadType.B) {
                //mTextViewName.setText(mBCardInfo.name);
                //mTextViewGender.setText(mBCardInfo.gender);
                //mTextViewNation.setText(mBCardInfo.nation);
                //mTextViewYear.setText(mBCardInfo.birthday.substring(0, 4));
                //mTextViewMonth.setText(mBCardInfo.birthday.substring(4, 6));
                //mTextViewDay.setText(mBCardInfo.birthday.substring(6, 8));
                //mTextViewAddress.setText(mBCardInfo.address);
                //mTextViewIDNo.setText(mBCardInfo.id);
                //mTextViewAgency.setText(mBCardInfo.agency);
                //mTextViewExpire.setText(mBCardInfo.expireStart + " - " + mBCardInfo.expireEnd);
                //mImageViewPortrait.setImageBitmap(mBCardInfo.photo);
            } else {
                //mTextViewIDNo.setText(cardANo);
                //lihuili add 20170515 for heying
                //write_cardsn_ToFile(cardANo);
            }
        } else {
            //mTextViewName.setText("");
            //mTextViewGender.setText("");
            //mTextViewNation.setText("");
            //mTextViewYear.setText("");
            //mTextViewMonth.setText("");
            //mTextViewDay.setText("");
            //mTextViewAddress.setText("");
            //mTextViewIDNo.setText("");
            //mTextViewAgency.setText("");
            //mTextViewExpire.setText("");
            //mImageViewPortrait.setImageBitmap(null);
        }
    }

    private void register() {
        mReceiver = new ClientBCReceiver(mHandler);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Action.ACTION_READER_STATE_CHANGED);
        filter.addAction(Action.ACTION_TYPEA_STATUS_CHANGED);
        filter.addAction(Action.ACTION_TYPEB_STATUS_CHANGED);
        filter.addAction(Action.ACTION_READER_PAUSED);
        filter.addAction(Action.ACTION_READER_STOPPED);
        filter.addAction(Action.ACTION_READER_STARTED);
        filter.addAction(Action.ACTION_READER_READMODE_CHANGED);
        filter.addAction(Action.ACTION_READER_READTYPE_CHANGED);
        filter.addAction(Action.ACTION_READER_OPENED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mReceiver, filter);
    }

    private void unRegister() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Log.d(TAG, "Build.SERIAL is " + Build.SERIAL);

        register();

        Log.d(TAG, "try to open LogWriter");
        LogWriter mLogWriter = new LogWriter(CLIENT_LOG_FILENAME, this);
        Log.d(TAG, "mLogWriter is " + mLogWriter);

        PackageManager pm = getPackageManager();//context为当前Activity上下文
        String majorVer = null;
        try {
            PackageInfo pi;
            pi = pm.getPackageInfo(getPackageName(), 0);
            majorVer = pi.versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        iDRVersion version = new iDRVersion();
        setTitle(getResources().getString(com.jf.scanerlib.R.string.app_name) + "V" + majorVer + "(" + version.getSvnRevision() + ")");


        soundPlayer = new ReadCardSound(this);
        //初始化界面
        setContentView(com.jf.scanerlib.R.layout.readcard);

        ////默认读卡模式为自动
        //mSpinnerMode.setSelection(ReadMode.AUTO.getValue());
        ////读卡类型为A卡/B卡
        //mSpinnerType.setSelection(ReadType.A_AND_B.getValue());

        mButtonPause.setEnabled(false);
        mButtonStop.setEnabled(false);
        mButtonStart.setEnabled(false);

        //绑定读卡服务
        bindReadCardService();
        mTextStatus = "正在初始化";

        mBCardInfo = new BCardInfo();

        setClientState(ReadState.st_init);
        
        UpdateSystemThread updateThread = new UpdateSystemThread(this);
        updateThread.start();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        unRegister();

        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(TAG, "onResume");
    }

    private ServiceConnection conn_readcard = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName comp, IBinder binder) {
            Log.d(TAG, "ReadCardService Connected");
            mReadCardService = IReadCardService.Stub.asInterface(binder);
            if (mReadCardService != null) {
                Message send_msg = new Message();
                send_msg.what = MainMsg.EVT_SERV_CONNECTED_RDCARD;
                if (mHandler != null) {
                    mHandler.sendMessage(send_msg);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName comp) {
            Log.d(TAG, "ReadCardService Disconnected");
            mReadCardService = null;
            Message send_msg = new Message();
            send_msg.what = MainMsg.EVT_SERV_DISCONNECTED_RDCARD;
            if (mHandler != null) {
                mHandler.sendMessage(send_msg);
            }
        }
    };

    public void bindReadCardService() {
        Intent intentReadCard = new Intent(IReadCardService.class.getName());
        Log.d(TAG, "bindReadCardService " + IReadCardService.class.getName());
        bindService(intentReadCard, conn_readcard, BIND_AUTO_CREATE);
        startService(intentReadCard);
    }

    public void unbindReadCardService() {
        Log.d(TAG, "unbindReadCardService " + IReadCardService.class.getName());
        if (mReadCardService != null) {
            unbindService(conn_readcard);
        }
        mReadCardService = null;
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        Message send_msg = new Message();
        if (arg0 == mButtonPause) {
            send_msg.what = MainMsg.EVT_PAUSE;
        } else if (arg0 == mButtonStop) {
            send_msg.what = MainMsg.EVT_STOP;
        } else if (arg0 == mButtonStart) {
            ReaderBean rdrBean = new ReaderBean();
            send_msg.what = MainMsg.EVT_START;
            send_msg.obj = rdrBean;
        }
        if (mHandler != null) {
            mHandler.sendMessage(send_msg);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Message send_msg = new Message();
            send_msg.what = MainMsg.EVT_APP_EXIT;
            if (mHandler != null) {
                mHandler.sendMessage(send_msg);
            }
        }
        return false;
    }

    /**
     * 系统更新
     * */
    public class UpdateSystemThread extends Thread {
        private Context context;

        public UpdateSystemThread(Context context) {
            this.context = context;
        }

        public void run() {
            //lihuili add 2016-05-26 增加系统补丁更新操作：如果patch.sh文件不不存在，则执行copy和更新。
            try {
                boolean is_need_copy = SystemPatch.CheckNeedCopy(context);
                Log.d(TAG, "is_need_copy?" + is_need_copy);
                if (is_need_copy) {
                    //提示"正在更新系统......"
                    Message send_msg = new Message();
                    send_msg.what = MainMsg.EVT_UPDATE_START;
                    if (mHandler != null) {
                        mHandler.sendMessage(send_msg);
                    }
                    boolean is_copy = SystemPatch.CopyAssetsToData(context);
                    Log.d(TAG, "is_copy?" + is_copy);
                    if (is_copy) {
                        String base_dirpath = getFilesDir() + File.separator + SystemPatch.SYSPATCH_DIR_RELATIVE_PATH;
                        String patch_filepath = getFilesDir() + File.separator + SystemPatch.PATCH_FILE_RELATIVE_PATH;
                        boolean is_patched = SystemPatch.execSystemPatch(base_dirpath, patch_filepath);
                        Log.d(TAG, "is_patched?" + is_patched);
                        //提示"正在更新系统成功!"
                        Message send_msg_suc = new Message();
                        send_msg_suc.what = MainMsg.EVT_UPDATE_SUCCESS;
                        if (mHandler != null) {
                            mHandler.sendMessage(send_msg_suc);
                        }
                    } else {
                        //提示"正在更新系统失败!"
                        Message send_msg_fail = new Message();
                        send_msg_fail.what = MainMsg.EVT_UPDATE_FAIL;
                        if (mHandler != null) {
                            mHandler.sendMessage(send_msg_fail);
                        }
                    }
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            //end---------------------------------------------------------------------
        }
    }

}
