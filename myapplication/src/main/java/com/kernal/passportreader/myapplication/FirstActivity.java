package com.kernal.passportreader.myapplication;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.kernal.passport.sdk.utils.AppManager;
import com.kernal.passport.sdk.utils.CheckPermission;
import com.kernal.passport.sdk.utils.Devcode;
import com.kernal.passport.sdk.utils.PermissionActivity;
import com.kernal.passport.sdk.utils.SharedPreferencesHelper;
import com.kernal.passportreader.sdk.CameraActivity;
import com.kernal.passportreader.sdk.IdCardMainActivity;
import com.kernal.passportreader.sdk.MainActivity;
import com.kernal.passportreader.sdk.VehicleLicenseMainActivity;


/**
 * Created by huangzhen on 2016/12/19.
 */

public class FirstActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main01);
        Intent intent=new Intent(FirstActivity.this,MainActivity.class);
        FirstActivity.this.finish();
        startActivity(intent);
        /**
         *It will wast a lot of time if such resources like camera is released in the camera interface.
         *  To optimize user experience,
         *  it is necessary to invoke AppManager.getAppManager().finishAllActivity() which is stored in
         *  oncreate() method in the camera interface.
         *  If the displaying interface is the one to invoke and recognize,
         *  this interface can only be invoked once.
         *  If there are two displaying interfaces,
         *  it is necessary to invoke AppManager.getAppManager().finishAllActivity()
         *  which is stored in oncreate() method in the displaying interface.
         *  Otherwise, it will lead to the overflow of internal memeory.
         *
         */
//        AppManager.getAppManager().finishAllActivity();
//        Intent intent = new Intent(FirstActivity.this, CameraActivity.class);
//        if (Build.VERSION.SDK_INT >= 23) {
//            CheckPermission checkPermission = new CheckPermission(this);
//            if (checkPermission.permissionSet(MainActivity.PERMISSION)) {
//                PermissionActivity.startActivityForResult(this, 0,
//                        SharedPreferencesHelper.getInt(
//                                getApplicationContext(), "nMainId", 2),
//                        Devcode.devcode, 0,0, MainActivity.PERMISSION);
//            } else {
//                intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
//                        getApplicationContext(), "nMainId", 2));
//                intent.putExtra("devcode", Devcode.devcode);
//                intent.putExtra("flag", 0);
//                FirstActivity.this.finish();
//                startActivity(intent);
//            }
//        } else {
//            intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
//                    getApplicationContext(), "nMainId", 2));
//            intent.putExtra("devcode", Devcode.devcode);
//            intent.putExtra("flag", 0);
//            FirstActivity.this.finish();
//            startActivity(intent);
//        }
    }
}
