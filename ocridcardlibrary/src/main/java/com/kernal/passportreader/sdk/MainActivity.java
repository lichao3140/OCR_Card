package com.kernal.passportreader.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.kernal.lisence.DeviceFP;
import com.kernal.lisence.ProcedureAuthOperate;
import com.kernal.passport.sdk.utils.ActivityRecogUtils;
import com.kernal.passport.sdk.utils.PermissionActivity;
import com.kernal.passport.sdk.utils.AppManager;
import com.kernal.passport.sdk.utils.CheckPermission;
import com.kernal.passport.sdk.utils.Devcode;
import com.kernal.passport.sdk.utils.SharedPreferencesHelper;
import com.kernal.passportreader.sdk.R;

import android.Manifest;
import android.hardware.usb.UsbInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import kernal.idcard.android.AuthParameterMessage;
import kernal.idcard.android.AuthService;
import kernal.idcard.android.IDCardAPI;
import kernal.idcard.android.RecogParameterMessage;
import kernal.idcard.android.RecogService;
import kernal.idcard.android.ResultMessage;

public class MainActivity extends Activity implements OnClickListener {
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private int srcWidth, srcHeight;
    private Button btn_chooserIdCardType, btn_takePicture, btn_exit,
            btn_importRecog, btn_ActivationProgram,btn_Intelligent_detecting_edges,btn_cancel_imei;
    private boolean isQuit = false;
    private Timer timer = new Timer();
    private String[][] type2 = {{"机读码", "3000"}, {"护照", "13"},
            {"居民身份证", "2"}, {"港澳通行证", "9"}, {"大陆居民往来台湾通行证", "11"},
            {"签证", "12"}, {"新版港澳通行证", "22"}, {"中国驾照", "5"},
            {"中国行驶证", "6"},{"厦门社会保障卡","1039"},{"福建社会保障卡","1041"}, {"香港身份证", "1001"}, {"回乡证(正面)", "14"},
            {"回乡证(背面)", "15"}, {"澳门身份证", "1005"}, {"新版澳门身份证", "1012"},
            {"台胞证", "10"}, {"新版台胞证(正面)", "25"}, {"新版台胞证(背面)", "26"},
            {"台湾身份证(正面)", "1031"}, {"台湾身份证(背面)", "1032"},{"中国军官证", "7"},
            {"全民健康保险卡", "1030"}, {"马来西亚身份证", "2001"}, {"新加坡身份证", "2004"},
            {"新西兰驾照", "2003"}, {"加利福尼亚驾照", "2002"}, {"印度尼西亚身份证", "2010"}, {"泰国身份证", "2011"}};
    private int nMainID = 0;
    public static int DIALOG_ID = -1;
    private String[] type;
    public RecogService.recogBinder recogBinder;
    private String recogResultString = "";
    private String selectPath = "";
    private int nMainId = 2;
    private String[] recogTypes = {"机读码(2*44)", "机读码(2*36)", "机读码(3*30)"};
    private String[] IDCardTypes = {"身份证（正面）", "身份证（背面）"};
    private AuthService.authBinder authBinder;
    private int ReturnAuthority = -1;
    private String sn = "";
    private AlertDialog dialog;
    private EditText editText;
    private int nSubID=0;
    private String devcode = Devcode.devcode;//project licensing development code
    public ServiceConnection authConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            authBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            authBinder = (AuthService.authBinder) service;
            try {

                AuthParameterMessage apm = new AuthParameterMessage();
                // apm.datefile = "assets"; // PATH+"/IDCard/wtdate.lsc";// Reservation
                apm.devcode = devcode;//  Reservation
                apm.sn = sn;
                ReturnAuthority = authBinder.getIDCardAuth(apm);
                if (ReturnAuthority != 0) {
                    Toast.makeText(getApplicationContext(),
                            "ReturnAuthority:" + ReturnAuthority,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.activation_success),
                            Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        getString(R.string.license_verification_failed),
                        Toast.LENGTH_LONG).show();

            } finally {
                if (authBinder != null) {
                    unbindService(authConn);
                }
            }
        }
    };
    public static final String[] PERMISSION = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//  Write access
            Manifest.permission.READ_EXTERNAL_STORAGE, //  read access
             Manifest.permission.READ_PHONE_STATE,Manifest.permission.CAMERA,
            Manifest.permission.VIBRATE, Manifest.permission.INTERNET,

    };
    private int sortOperaPermission=0;
    private Handler sortOperaHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
              if(sortOperaPermission==0)
              {
                  Intent  intent = new Intent(MainActivity.this, CameraActivity.class);
                  intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
                          getApplicationContext(), "nMainId", 2));
                  intent.putExtra("devcode", devcode);
                  intent.putExtra("flag", 0);
                  intent.putExtra("nCropType", msg.what);
                  MainActivity.this.finish();
                  startActivity(intent);
              }else if(sortOperaPermission==1){
                  activationProgramOpera();
              }else if(sortOperaPermission==2){
                  Intent intent = new Intent(
                          Intent.ACTION_PICK,
                          MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                  try {
                      startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)),
                              9);
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
              }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//  hiding titles
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//  setting up the full screen
        //  Screen always on
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        srcWidth = displayMetrics.widthPixels;
        srcHeight = displayMetrics.heightPixels;
        setContentView(getResources().getIdentifier("activity_main", "layout",
                getPackageName()));
        findView();
        //  the came interface being released
        AppManager.getAppManager().finishAllActivity();

    }


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        type2 = intiList();
        type = new String[type2.length];

        for (int i = 0; i < type2.length; i++) {
            type[i] = type2[i][0];
        }
        if (getResources().getConfiguration().locale.getLanguage().equals("zh")
                && getResources().getConfiguration().locale.getCountry()
                .equals("CN")) {
            RecogService.nTypeInitIDCard = 3;
        } else if (getResources().getConfiguration().locale.getLanguage()
                .equals("zh")
                && getResources().getConfiguration().locale.getCountry()
                .equals("TW")) {
            RecogService.nTypeInitIDCard = 3;
        } else {
            RecogService.nTypeInitIDCard = 4;
        }
    }

    /**
     * @Title: findView @Description: TODO(这里用一句话描述这个方法的作用) @param 设定文件 @return
     * void 返回类型 @throws
     */
    private void findView() {
        // TODO Auto-generated method stub

        btn_chooserIdCardType = (Button) findViewById(getResources().getIdentifier("btn_chooserIdCardType", "id",
                getPackageName()));
        btn_takePicture = (Button) findViewById(getResources().getIdentifier("btn_takePicture", "id",
                getPackageName()));
        btn_cancel_imei = (Button) findViewById(getResources().getIdentifier("btn_cancel_imei", "id",
                getPackageName()));
        btn_exit = (Button) findViewById(getResources().getIdentifier("btn_exit", "id",
                getPackageName()));
        btn_importRecog = (Button) findViewById(getResources().getIdentifier("btn_importRecog", "id",
                getPackageName()));
        btn_ActivationProgram = (Button) findViewById(getResources().getIdentifier("btn_ActivationProgram", "id",
                getPackageName()));
        btn_Intelligent_detecting_edges= (Button) findViewById(getResources().getIdentifier("btn_Intelligent_detecting_edges", "id",
                getPackageName()));
        btn_ActivationProgram.setOnClickListener(this);
        btn_chooserIdCardType.setOnClickListener(this);
        btn_takePicture.setOnClickListener(this);
        btn_Intelligent_detecting_edges.setOnClickListener(this);
        btn_exit.setOnClickListener(this);
        btn_cancel_imei.setOnClickListener(this);
        btn_importRecog.setOnClickListener(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                srcWidth / 2, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) (srcHeight * 0.25);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_ActivationProgram.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_ActivationProgram);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_chooserIdCardType.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_chooserIdCardType);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_takePicture.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_takePicture);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_Intelligent_detecting_edges.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_Intelligent_detecting_edges);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_importRecog.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_importRecog);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_cancel_imei.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(srcWidth / 2,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.btn_cancel_imei);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btn_exit.setLayoutParams(params);

    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent;
        if (getResources()
                .getIdentifier("btn_ActivationProgram", "id", this.getPackageName()) == v.getId()) {
            sortOperaPermission=1;
            if (Build.VERSION.SDK_INT >= 23) {
                CheckPermission checkPermission = new CheckPermission(this);
                if (checkPermission.permissionSet(PERMISSION)) {

                    PermissionActivity.startActivityForResult(this,sortOperaHandler, 0,
                            SharedPreferencesHelper.getInt(
                                    getApplicationContext(), "nMainId", 2),
                            devcode, 0, 0,0, PERMISSION);
                } else {
                    activationProgramOpera();
                }
            } else {
                activationProgramOpera();
            }

        } else if (getResources()
                .getIdentifier("btn_chooserIdCardType", "id", this.getPackageName()) == v.getId()) {

                dialog();


        } else if (getResources()
                .getIdentifier("btn_takePicture", "id", this.getPackageName()) == v.getId()) {
/**
 * It will wast a lot of time if such resources like camera is released in the camera interface.
 * To optimize user experience, it is necessary to invoke AppManager.getAppManager().finishAllActivity() which
 * is stored in oncreate() method in the camera interface.
 * If the displaying interface is the one to invoke and recognize,
 * this interface can only be invoked once.
 * If there are two displaying interfaces,
 * it is necessary to invoke AppManager.getAppManager().finishAllActivity() which
 * is stored in oncreate() method in the displaying interface.
 * Otherwise, it will lead to the overflow of internal memeory.
 */         sortOperaPermission=0;
            intent = new Intent(MainActivity.this, CameraActivity.class);
            if (Build.VERSION.SDK_INT >= 23) {
                CheckPermission checkPermission = new CheckPermission(this);
                if (checkPermission.permissionSet(PERMISSION)) {

                    PermissionActivity.startActivityForResult(this,sortOperaHandler, 0,
                            SharedPreferencesHelper.getInt(
                                    getApplicationContext(), "nMainId", 2),
                            devcode, 0, 0,0, PERMISSION);
                } else {
                    intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
                            getApplicationContext(), "nMainId", 2));
                    intent.putExtra("nSubID", SharedPreferencesHelper.getInt(
                            getApplicationContext(), "nSubID", 0));
                    intent.putExtra("devcode", devcode);
                    intent.putExtra("flag", 0);
                    intent.putExtra("nCropType", 0);
                    MainActivity.this.finish();
                    startActivity(intent);
                }
            } else {
                intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
                        getApplicationContext(), "nMainId", 2));
                intent.putExtra("nSubID", SharedPreferencesHelper.getInt(
                        getApplicationContext(), "nSubID", 0));
                intent.putExtra("devcode", devcode);
                intent.putExtra("flag", 0);
                intent.putExtra("nCropType", 0);
                MainActivity.this.finish();
                startActivity(intent);
            }
        }else if (getResources()
                .getIdentifier("btn_Intelligent_detecting_edges", "id", this.getPackageName()) == v.getId()) {
/**
 * It will wast a lot of time if such resources like camera is released in the camera interface.
 * To optimize user experience, it is necessary to invoke AppManager.getAppManager().finishAllActivity() which
 * is stored in oncreate() method in the camera interface.
 * If the displaying interface is the one to invoke and recognize,
 * this interface can only be invoked once.
 * If there are two displaying interfaces,
 * it is necessary to invoke AppManager.getAppManager().finishAllActivity() which
 * is stored in oncreate() method in the displaying interface.
 * Otherwise, it will lead to the overflow of internal memeory.
 */         sortOperaPermission=0;
            intent = new Intent(MainActivity.this, CameraActivity.class);
            if (Build.VERSION.SDK_INT >= 23) {
                CheckPermission checkPermission = new CheckPermission(this);
                if (checkPermission.permissionSet(PERMISSION)) {

                    PermissionActivity.startActivityForResult(this,sortOperaHandler, 0,
                            SharedPreferencesHelper.getInt(
                                    getApplicationContext(), "nMainId", 2),
                            devcode, 0, 0,1, PERMISSION);
                } else {
                    intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
                            getApplicationContext(), "nMainId", 2));
                    intent.putExtra("devcode", devcode);
                    intent.putExtra("nSubID", SharedPreferencesHelper.getInt(
                            getApplicationContext(), "nSubID", 0));
                    intent.putExtra("flag", 0);
                    intent.putExtra("nCropType", 1);
                    MainActivity.this.finish();
                    startActivity(intent);
                }
            } else {
                intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
                        getApplicationContext(), "nMainId", 2));
                intent.putExtra("devcode", devcode);
                intent.putExtra("nSubID", SharedPreferencesHelper.getInt(
                        getApplicationContext(), "nSubID", 0));
                intent.putExtra("flag", 0);
                intent.putExtra("nCropType", 1);
                MainActivity.this.finish();
                startActivity(intent);
            }
        } else if (getResources()
                .getIdentifier("btn_importRecog", "id", this.getPackageName()) == v.getId()) {
// Phote Album
            sortOperaPermission=2;
            if (Build.VERSION.SDK_INT >= 23) {
                CheckPermission checkPermission = new CheckPermission(this);
                if (checkPermission.permissionSet(PERMISSION)) {
                    PermissionActivity.startActivityForResult(this,sortOperaHandler, 0,
                            SharedPreferencesHelper.getInt(
                                    getApplicationContext(), "nMainId", 2),
                            devcode, 0, 0,0, PERMISSION);
                } else {
                    intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    try {
                        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)),
                                9);
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.install_fillManager), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                try {
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_picture)),
                            9);
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.install_fillManager), Toast.LENGTH_SHORT).show();
                }
            }

        } else if (getResources()
                .getIdentifier("btn_exit", "id", this.getPackageName()) == v.getId()) {
            MainActivity.this.finish();

        }else if(getResources()
                .getIdentifier("btn_cancel_imei", "id", this.getPackageName()) == v.getId()){
            deleteAuthInfosOpera();
        }


    }

    /**
     * @Title: activationProgramOpera @Description: TODO(这里用一句话描述这个方法的作用) @param
     * 设定文件 @return void 返回类型 @throws
     */
    private void activationProgramOpera() {
        // TODO Auto-generated method stub
        DIALOG_ID = 1;
        View view = getLayoutInflater().inflate(R.layout.serialdialog, null);
        editText = (EditText) view.findViewById(R.id.serialdialogEdittext);
        dialog = new Builder(MainActivity.this)
                .setView(view)
                .setPositiveButton(getString(R.string.online_activation),
                        new DialogInterface.OnClickListener() {
                           @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm.isActive()) {
                                    imm.toggleSoftInput(
                                            InputMethodManager.SHOW_IMPLICIT,
                                            InputMethodManager.HIDE_NOT_ALWAYS);
                                }
                                String editsString = editText.getText()
                                        .toString().toUpperCase();
                                if (editsString != null) {
                                    sn = editsString;
                                }
                                if (isNetworkConnected(MainActivity.this)) {
                                    if (isWifiConnected(MainActivity.this)
                                            || isMobileConnected(MainActivity.this)) {
                                        startAuthService();
                                        dialog.dismiss();
                                    } else if (!isWifiConnected(MainActivity.this)
                                            && !isMobileConnected(MainActivity.this)) {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                getString(R.string.network_unused),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            getString(R.string.please_connect_network),
                                            Toast.LENGTH_SHORT).show();
                                }

                            }

                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();

                            }

                        }).create();
        dialog.show();
    }
    /**
     * @Title: deleteAuthInfosOpera @Description: 清除序列号绑定的IMEI信息
     * 设定文件 @return void 返回类型 @throws
     */
    private void deleteAuthInfosOpera() {
        // TODO Auto-generated method stub
        DIALOG_ID = 1;
        View view = getLayoutInflater().inflate(R.layout.serialdialog, null);
        editText = (EditText) view.findViewById(R.id.serialdialogEdittext);
        TextView  textView = (TextView) view.findViewById(R.id.serialdialogTextview);
        textView.setText(getString(R.string.cancel_title));
        dialog = new Builder(MainActivity.this)
                .setView(view)
                .setPositiveButton(getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm.isActive()) {
                                    imm.toggleSoftInput(
                                            InputMethodManager.SHOW_IMPLICIT,
                                            InputMethodManager.HIDE_NOT_ALWAYS);
                                }
                                String editsString = editText.getText()
                                        .toString().toUpperCase();
                                if (editsString != null) {
                                    sn = editsString;
                                }
                                if (isNetworkConnected(MainActivity.this)) {
                                    if (isWifiConnected(MainActivity.this)
                                            || isMobileConnected(MainActivity.this)) {
                                        try {
                                            ProcedureAuthOperate pao = new ProcedureAuthOperate(MainActivity.this);
                                            pao.DeleteAuthInfosTask.execute(sn,"","11");
                                           int isSuccessDeleteAuthInfos=(Integer) pao.DeleteAuthInfosTask.get();
                                           if(isSuccessDeleteAuthInfos==0){
                                               Toast.makeText(
                                                       getApplicationContext(),
                                                       getString(R.string.cancel_success),
                                                       Toast.LENGTH_SHORT).show();
                                           }else {
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        getString(R.string.exception10)+":"+isSuccessDeleteAuthInfos,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    } else if (!isWifiConnected(MainActivity.this)
                                            && !isMobileConnected(MainActivity.this)) {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                getString(R.string.network_unused),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            getString(R.string.please_connect_network),
                                            Toast.LENGTH_SHORT).show();
                                }

                            }

                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();

                            }

                        }).create();
        dialog.show();
    }

    /**
     * @Title: dialog @Description: TODO(这里用一句话描述这个方法的作用) @param 设定文件 @return
     * void 返回类型 @throws
     */
    private void dialog() {
        // TODO Auto-generated method stub

        int checkedItem = -1;
        /*
         * if
		 * (getResources().getConfiguration().locale.getLanguage().equals("zh")
		 * && getResources().getConfiguration().locale.getCountry()
		 * .equals("CN")) {
		 */
        for (int i = 0; i < type2.length; i++) {

            if (Integer.valueOf(type2[i][1]) == SharedPreferencesHelper.getInt(
                    getApplicationContext(), "nMainId", 2)) {
                if(type2[i].length>2) {
                    if (Integer.valueOf(type2[i][2]) == SharedPreferencesHelper.getInt(
                            getApplicationContext(), "nSubID", 0)) {
                        checkedItem = i;
                        break;
                    }
                }else {
                    checkedItem = i;
                    break;
                }


            }
        }

        // }
        // The selected results in the type column do not disapper
        DIALOG_ID = 1;
        Builder dialog = createAlertDialog(
                getString(R.string.chooseRecogType), null);
        dialog.setPositiveButton(getString(R.string.confirm), dialogListener);
        dialog.setNegativeButton(getString(R.string.cancel), dialogListener);
        dialog.setSingleChoiceItems(type, checkedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

						/*
						 * if (getResources().getConfiguration().locale
						 * .getLanguage().equals("zh") &&
						 * getResources().getConfiguration().locale
						 * .getCountry().equals("CN")) {
						 */
                        for (int i = 0; i < type2.length; i++) {
                            if (which == i) {
                                nMainID = Integer.valueOf(type2[i][1]);
                                if(type2[i].length>2){
                                    nSubID=Integer.valueOf(type2[i][2]);
                                }
                                break;
                            }
                        }

                        // }
                    }
                });
        dialog.show();
    }

    /**
     * @Title: createAlertDialog @Description: TODO(这里用一句话描述这个方法的作用) @param @param
     * string @param @param object @param @return 设定文件 @return Builder
     * 返回类型 @throws
     */
    private Builder createAlertDialog(String title, String message) {
        // TODO Auto-generated method stub
        Builder dialog = new Builder(this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.create();
        return dialog;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (isQuit == false) {
                isQuit = true;
                Toast.makeText(getBaseContext(), R.string.back_confirm, 2000)
                        .show();
                TimerTask task = null;
                task = new TimerTask() {
                    @Override
                    public void run() {
                        isQuit = false;
                    }
                };
                timer.schedule(task, 2000);
            } else {
                finish();
            }
        }
        return true;
    }

    public DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {

            switch (DIALOG_ID) {

                case 1:
                    if (dialog.BUTTON_POSITIVE == which) {
                        if (nMainID == 0) {
                            if (SharedPreferencesHelper.getInt(
                                    getApplicationContext(), "nMainId", 2) != 2) {
                                nMainID = SharedPreferencesHelper.getInt(
                                        getApplicationContext(), "nMainId", 2);
                            } else {
                                nMainID = 2;
                            }
                        }

                        SharedPreferencesHelper.putInt(getApplicationContext(),
                                "nMainId", nMainID);
                        SharedPreferencesHelper.putInt(getApplicationContext(),
                                "nSubID", nSubID);
                        dialog.dismiss();
                    } else if (dialog.BUTTON_NEGATIVE == which) {
                        dialog.dismiss();
                    }
                    break;

            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == 9 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            selectPath = getPath(getApplicationContext(), uri);
            RecogService.nMainID = SharedPreferencesHelper.getInt(
                    getApplicationContext(), "nMainId", 2);

            new Thread() {
                @Override
                public void run() {
                    RecogService.isRecogByPath = true;
                    Intent recogIntent = new Intent(MainActivity.this,
                            RecogService.class);
                    bindService(recogIntent, recogConn, Service.BIND_AUTO_CREATE);
                }
            }.start();

                //ActivityRecogUtils.getRecogResult(MainActivity.this, selectPath, RecogService.nMainID, true);



        } else if (requestCode == 8 && resultCode == Activity.RESULT_OK) {
            //Activtiy recognize returned results
            int ReturnAuthority = data.getIntExtra("ReturnAuthority", -100000);//  get activation status
            ResultMessage resultMessage = new ResultMessage();
            resultMessage.ReturnAuthority = data.getIntExtra("ReturnAuthority", -100000);//  get activation status
            resultMessage.ReturnInitIDCard = data
                    .getIntExtra("ReturnInitIDCard", -100000);// Get initialization return value
            resultMessage.ReturnLoadImageToMemory = data.getIntExtra(
                    "ReturnLoadImageToMemory", -100000);// Get the image return value
            resultMessage.ReturnRecogIDCard = data.getIntExtra("ReturnRecogIDCard",
                    -100000);// Get the recogniion reurn value
            resultMessage.GetFieldName = (String[]) data
                    .getSerializableExtra("GetFieldName");
            resultMessage.GetRecogResult = (String[]) data
                    .getSerializableExtra("GetRecogResult");
            ActivityRecogUtils.goShowResultActivity(MainActivity.this, resultMessage,0,selectPath,selectPath.substring(0,selectPath.indexOf(".jpg"))+ "Cut.jpg");
        }
    }

    //  Recognition Test
    public ServiceConnection recogConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            recogBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            recogBinder = (RecogService.recogBinder) service;
            if(recogBinder!=null) {
                        RecogParameterMessage rpm = new RecogParameterMessage();
                        rpm.nTypeLoadImageToMemory = 0;
                        rpm.nMainID = SharedPreferencesHelper.getInt(
                                getApplicationContext(), "nMainId", 2);
                        rpm.nSubID[0] = SharedPreferencesHelper.getInt(getApplicationContext(), "nSubID", 0);
                        rpm.GetSubID = true;
                        rpm.GetVersionInfo = true;
                        rpm.logo = "";
                        rpm.userdata = "";
                        rpm.sn = "";
                        rpm.authfile = "";
                        rpm.isCut = true;
                        rpm.triggertype = 0;
                        rpm.devcode = devcode;
                        //nProcessType：0- deleting all instructions 1- cropping 2- rotation  3- cropping and rotation
                        // 4- tilt correction 5- cropping+ tilt correction 6- rotation+tile correct
                        // 7- cropping+rotation+tilt correction.
                        rpm.nProcessType = 7;
                        rpm.nSetType = 1;// nSetType: 0－Deleting operations，1－setting operations
                        rpm.lpFileName = selectPath; // If rpm.lpFileName is null, auomatic recognition function will be executed.
                        // rpm.lpHeadFileName = selectPath;//Save portrait of identity document
                        rpm.isSaveCut = true;// Save cropping picture false=not saving  true=saving
                        rpm.cutSavePath="";
                        if (SharedPreferencesHelper.getInt(getApplicationContext(),
                                "nMainId", 2) == 2) {
                            rpm.isAutoClassify = true;
                            rpm.isOnlyClassIDCard = true;
                        } else if (SharedPreferencesHelper.getInt(getApplicationContext(),
                                "nMainId", 2) == 3000) {
                            rpm.nMainID = 1034;
                        }
                        // end
                        try {

                            ResultMessage resultMessage;
                            resultMessage = recogBinder.getRecogResult(rpm);
                            if (resultMessage.ReturnAuthority == 0
                                    && resultMessage.ReturnInitIDCard == 0
                                    && resultMessage.ReturnLoadImageToMemory == 0
                                    && resultMessage.ReturnRecogIDCard > 0) {
                                String iDResultString = "";
                                String[] GetFieldName = resultMessage.GetFieldName;
                                String[] GetRecogResult = resultMessage.GetRecogResult;

                                for (int i = 1; i < GetFieldName.length; i++) {
                                    if (GetRecogResult[i] != null) {
                                        if (!recogResultString.equals(""))
                                            recogResultString = recogResultString
                                                    + GetFieldName[i] + ":"
                                                    + GetRecogResult[i] + ",";
                                        else {
                                            recogResultString = GetFieldName[i] + ":"
                                                    + GetRecogResult[i] + ",";
                                        }
                                    }
                                }
                                Intent intent = new Intent(MainActivity.this,
                                        ShowResultActivity.class);
                                intent.putExtra("recogResult", recogResultString);
                                intent.putExtra("fullPagePath", selectPath);
                                intent.putExtra("importRecog", true);
                                intent.putExtra("cutPagePath", selectPath.substring(0, selectPath.indexOf(".jpg")) + "Cut.jpg");
                                MainActivity.this.finish();
                                startActivity(intent);
                            } else {
                                String string = "";
                                if (resultMessage.ReturnAuthority == -100000) {
                                    string = getString(R.string.exception)
                                            + resultMessage.ReturnAuthority;
                                } else if (resultMessage.ReturnAuthority != 0) {
                                    string = getString(R.string.exception1)
                                            + resultMessage.ReturnAuthority;
                                } else if (resultMessage.ReturnInitIDCard != 0) {
                                    string = getString(R.string.exception2)
                                            + resultMessage.ReturnInitIDCard;
                                } else if (resultMessage.ReturnLoadImageToMemory != 0) {
                                    if (resultMessage.ReturnLoadImageToMemory == 3) {
                                        string = getString(R.string.exception3)
                                                + resultMessage.ReturnLoadImageToMemory;
                                    } else if (resultMessage.ReturnLoadImageToMemory == 1) {
                                        string = getString(R.string.exception4)
                                                + resultMessage.ReturnLoadImageToMemory;
                                    } else {
                                        string = getString(R.string.exception5)
                                                + resultMessage.ReturnLoadImageToMemory;
                                    }
                                } else if (resultMessage.ReturnRecogIDCard <= 0) {
                                    if (resultMessage.ReturnRecogIDCard == -6) {
                                        string = getString(R.string.exception9);
                                    } else {
                                        string = getString(R.string.exception6)
                                                + resultMessage.ReturnRecogIDCard;
                                    }
                                }
                                Intent intent = new Intent(MainActivity.this,
                                        ShowResultActivity.class);
                                intent.putExtra("exception", string);
                                intent.putExtra("fullPagePath", selectPath);
                                intent.putExtra("importRecog", true);
                                MainActivity.this.finish();
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            Looper.prepare();
//                            Toast.makeText(getApplicationContext(),
//                                    getString(R.string.recognized_failed),
//                                    Toast.LENGTH_SHORT).show();
//                            Looper.loop();
                        } finally {
                            if (recogBinder != null) {
                                unbindService(recogConn);
                            }
                        }

            }

        }
    };

    private void startAuthService() {
        RecogService.isOnlyReadSDAuthmodeLSC = false;
        Intent authIntent = new Intent(MainActivity.this, AuthService.class);
        bindService(authIntent, authConn, Service.BIND_AUTO_CREATE);
    }

    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /***
     * @param @param context @param @param uri @param @return 设定文件 @return
     *               String 返回类型 @throws
     * @Title: getPath
     * @Description: TODO(这里用一句话描述这个方法的作用) 获取图片路径
     */

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) { // 忽略大小写
            // String[] projection = { "_data" };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null,
                        null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private String[][] intiList() {
        String[][] list = null;
        if (getResources().getConfiguration().locale.getLanguage().equals("zh")
                && getResources().getConfiguration().locale.getCountry()
                .equals("CN")) {
            String[][] temp = {{"机读码", "3000"}, {"护照", "13"},
                    {"居民身份证", "2"}, {"临时身份证", "4"}, {"往来港澳通行证2005版", "9"},
                    {"大陆居民往来台湾通行证1992版", "11"}, {"往来台湾通行证2017版-照片页", "29"},{"签证", "12"},
                    {"往来港澳通行证2014版", "22"}, {"机动车驾驶证（中国）", "5"},{ "机动车驾驶证副页（中国）", "28" }, {"机动车行驶证（中国）", "6"},
                    {"厦门社会保障卡","1039"},{"福建社会保障卡","1041"},{"香港居民身份证2003版（照片页）", "1001"},{ "户口本", "16" }, {"港澳居民来往内地通行证(照片页)", "14"},
                    {"港澳居民来往内地通行证(机读码页)", "15"}, {"澳门居民身份证（照片页）", "1005"},
                    {"深圳居住证", "1013"},{"台湾居民往来大陆通行证1992版（照片页）", "10"},
                    {"台湾居民往来大陆通行证2015版(照片页)", "25"}, {"台湾居民往来大陆通行证2015版(机读码页)", "26"},
                    {"台湾国民身份证(照片页)", "1031"}, {"台湾国民身份证(条码页)", "1032"},{"中国军官证1998版", "7"},
                    {"台湾全民健康保险卡", "1030"}, {"马来西亚身份证（照片页）", "2001"},
                    {"新加坡身份证", "2004"}, {"新西兰驾驶证", "2003"},
                    {"加利福尼亚驾驶证", "2002"}, {"印度尼西亚居民身份证-KEPT", "2010","1"},{"印度尼西亚居民身份证-KPT", "2010","2"}, {"泰国国民身份证", "2011"},{ "北京社保卡", "1021" }
                    ,{ "泰国驾驶证", "2012","1" },{ "泰国驾驶证-私家车", "2012","2" },{ "墨西哥选民证AB", "2013","1" },{ "墨西哥选民证C", "2013","2" },{ "墨西哥选民证DE", "2013","3" },{ "墨西哥选民证背面ABC", "2014" },{ "马来西亚驾照", "2021" },{ "新加坡驾驶证", "2031" },{ "印度尼西亚驾驶证", "2041" },{ "日本驾照", "2051" }};
            list = temp;
        } else if (getResources().getConfiguration().locale.getLanguage()
                .equals("zh")
                && getResources().getConfiguration().locale.getCountry()
                .equals("TW")) {
            String[][] temp = {{"機讀碼", "3000"}, {"護照", "13"},
                    {"居民身份證", "2"}, {"臨時身份證", "4"}, {"往来港澳通行證2005版", "9"},
                    {"大陸居民往來台灣通行證1992版", "11"},{"往來台灣通行證2017版-照片頁", "29"}, {"簽證", "12"},
                    {"往來港澳通行證2014版", "22"}, {"機動車駕駛證（中國）", "5"},{ "機動車駕駛證副頁（中國）", "28" }, {"機動車行駛證（中國）", "6"},
                    {"廈門社會保障卡","1039"},{"福建社會保障卡","1041"},{"香港居民身份證2003版（照片頁）", "1001"}, { "戶口本", "16" },{"港澳居民來往內地通行證(照片頁)", "14"},
                    {"港澳居民來往內地通行證(機讀碼頁)", "15"}, {"澳門居民身份證（照片頁）", "1005"},
                    {"深圳居住證", "1013"},{"台灣居民往來大陸通行證1992版（照片頁）", "10"},
                    {"台灣國民身份證(照片頁)", "1031"}, {"台灣國民身份證(條碼頁)", "1032"},
                    {"台灣居民往來大陸通行證2015版(照片頁)", "25"}, {"台灣居民往來大陸通行證2015版(機讀碼頁)", "26"},{"中國軍官証1998版", "7"},
                    {"台灣全民健康保險卡", "1030"}, {"馬來西亞身份證（照片頁）", "2001"},
                    {"新加坡身份證", "2004"}, {"新西蘭駕照", "2003"},
                    {"加利福尼亞駕照", "2002"}, {"印度尼西亞身份證-KEPT", "2010","1"},{"印度尼西亞身份證-KPT", "2010","2"}, {"泰國國民身份證", "2011"},{ "北京社保卡", "1021" }
                    ,{ "泰國駕駛證", "2012", "1"},{ "泰國駕駛證-私家車", "2012","2" },{ "墨西哥選民證AB", "2013","1" },{ "墨西哥選民證C", "2013","2" },{ "墨西哥選民證DE", "2013","3" },{ "墨西哥選民證背面ABC", "2014" },{ "馬來西亞駕照", "2021" },{ "新加坡駕駛證", "2031" },{ "印度尼西亞駕駛證", "2041" },{ "日本駕照", "2051" }};
            list = temp;
        } else {
            String[][] temp = {{"Machine readable zone", "3000"},
                    {"Passport", "13"}, {"Chinese ID card", "2"}, {"Interim ID card", "4"},
                    {"Exit-Entry Permit to HK/Macau", "9"},
                    {"Taiwan pass", "11"},{"Taiwan pass(2017 Obverse)", "29"},  {"Visa", "12"},
                    {"e-EEP to HK/Macau", "22"},
                    {"Chinese Driving license", "5"},
                    { "Chinese Driving license(second)", "28" },
                    {"Chinese vehicle license", "6"},
                    {"Xiamen Social Security Card","1039"},{"Fujian Social Security Card","1041"},{"HK ID card", "1001"},{ "Household Register", "16" },
                    {"Home return permit (Obverse)", "14"},
                    {"Home return permit (Reverse)", "15"},
                    {"Macau ID card", "1005"},
                    {"Shenzhen Resident Permit", "1013"},
                    {"To the Mainland Travel Permit", "10"},
                    {"Taiwan ID card (Obverse)", "1031"},
                    {"Taiwan ID card (Reverse)", "1032"},
                    {"To the New Mainland Travel Permit(Obverse)", "25"},
                    {"To the New Mainland Travel Permit(Reverse)", "26"},{"Chinese certificate of officers", "7"},
                    {"National health care card", "1030"},
                    {"MyKad", "2001"}, {"Singapore ID card", "2004"},
                    {"New Zealand Driving license", "2003"},
                    {"California driving license", "2002"}, {"Indonesia Resident Identity Card (EKPT)", "2010","1"},{"Indonesia Resident Identity Card (KPT)", "2010","2"}, {"Thailand's Identity Card", "2011"} ,
                    {"Beijing social security card","1021"}
                    ,{ "Thailand Driving License", "2012","1" },{ "Thailand Private Car Driving License", "2012","2" },{ "Mexico INE&IFE ID Card Type AB", "2013","1" },{ "Mexico INE&IFE ID Card Type C", "2013","2" },{ "Mexico INE&IFE ID Card Type DE", "2013","3" },{ "Mexico INE&IFE ID Card Reverse Page", "2014" },{ "Malaysia Driving license", "2021" },{ "Singapore Driving License", "2031" },{ "Indonesia Driving License", "2041" },{ "Japan Driving License", "2051" }};
            list = temp;
        }
        return list;
    }
}
