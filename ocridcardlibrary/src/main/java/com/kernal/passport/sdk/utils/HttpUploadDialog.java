package com.kernal.passport.sdk.utils;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kernal.passportreader.sdk.ShowResultActivity;

import java.io.File;

import kernal.idcard.android.WriteToPCTask;

/**
 * Created by huangzhen on 2017/3/28.
 */

public class HttpUploadDialog extends DialogFragment implements
        View.OnClickListener {
    private int width, height;
    public static Context context;
    private TextView tv_httpupload_title,tv_httpupload_ok,tv_httpupload_cancel,tv_httpupload_null;
    private ImageView iv_httpupload_picture;
    private EditText et_httpupload_content;
    @Override
    public void onClick(View v) {
        if(NetworkProber.isNetworkAvailable(context)){
            if (getResources().getIdentifier("tv_httpupload_ok", "id",
                    context.getPackageName()) == v.getId()) {
                System.out.println("确定");
                if(et_httpupload_content.getText().toString()!=null&&!et_httpupload_content.getText().toString().equals(""))
                {
                    new WriteToPCTask(context
                            ).execute(
                            ShowResultActivity.fullPagePath, et_httpupload_content.getText().toString());
                }else{
                    new WriteToPCTask(context
                    ).execute(
                            ShowResultActivity.fullPagePath, "");
                }
                dismiss();
            }else if(getResources().getIdentifier("tv_httpupload_cancel", "id",
                    context.getPackageName()) == v.getId()){
                dismiss();
            }
        }else{
            Toast.makeText(context,context.getString(getResources().getIdentifier("networkunused", "string",
                    context.getPackageName())),Toast.LENGTH_SHORT).show();
            dismiss();
        }

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // TODO Auto-generated method stub
        View view = inflater.inflate(getResources().getIdentifier("dialog_httpupload",
                "layout", context.getPackageName()), null);
        tv_httpupload_title = (TextView) view.findViewById(getResources().getIdentifier("tv_httpupload_title",
                "id", context.getPackageName()));
        tv_httpupload_ok= (TextView) view.findViewById(getResources().getIdentifier("tv_httpupload_ok",
                "id", context.getPackageName()));
        tv_httpupload_cancel= (TextView) view.findViewById(getResources().getIdentifier("tv_httpupload_cancel",
                "id", context.getPackageName()));
        tv_httpupload_null= (TextView) view.findViewById(getResources().getIdentifier("tv_httpupload_null",
                "id", context.getPackageName()));
        iv_httpupload_picture = (ImageView) view.findViewById(getResources().getIdentifier("iv_httpupload_picture",
                "id", context.getPackageName()));
        et_httpupload_content = (EditText) view.findViewById(getResources().getIdentifier("et_httpupload_content",
                "id", context.getPackageName()));
        RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(
                (int) (width * 0.9), RelativeLayout.LayoutParams.WRAP_CONTENT);
        lparams.leftMargin=(int) (width * 0.02);
        lparams.topMargin=(int) (width * 0.02);
        tv_httpupload_title.setLayoutParams(lparams);
        lparams = new RelativeLayout.LayoutParams(
                (int) (width * 0.65), (int) (width * 0.7*0.75));
        lparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lparams.topMargin=(int) (width * 0.03);
        iv_httpupload_picture.setLayoutParams(lparams);
        iv_httpupload_picture.setImageBitmap(BitmapFactory.decodeFile(ShowResultActivity.fullPagePath));
        lparams = new RelativeLayout.LayoutParams(
                (int) (width * 0.7), RelativeLayout.LayoutParams.WRAP_CONTENT);
        lparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lparams.topMargin=(int) (width * 0.7*0.7);
        et_httpupload_content.setLayoutParams(lparams);
        lparams = new RelativeLayout.LayoutParams(
                (int) (width * 0.45),RelativeLayout.LayoutParams.WRAP_CONTENT);
        lparams.leftMargin=(int) (width * 0.38);
        lparams.topMargin=(int) (width * 0.7*0.94);
        tv_httpupload_ok.setLayoutParams(lparams);
        tv_httpupload_ok.setOnClickListener(this);
        lparams = new RelativeLayout.LayoutParams(
                (int) (width * 0.45), RelativeLayout.LayoutParams.WRAP_CONTENT);
        lparams.leftMargin=0;
        lparams.topMargin=(int) (width * 0.7*0.94);
        tv_httpupload_cancel.setLayoutParams(lparams);
        tv_httpupload_cancel.setOnClickListener(this);
        lparams = new RelativeLayout.LayoutParams(
                (int) (width * 0.9), (int) (width * 0.1));
        lparams.topMargin=(int) (width * 0.66);
        tv_httpupload_null.setLayoutParams(lparams);



        return view;
    };
    @Override
    public void show(FragmentManager manager, String tag) {
        // TODO Auto-generated method stub

        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commit();
    }
}
