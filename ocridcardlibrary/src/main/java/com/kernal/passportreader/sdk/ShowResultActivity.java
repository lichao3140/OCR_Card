/**
 *
 */
package com.kernal.passportreader.sdk;

import java.io.File;

import com.kernal.passport.sdk.utils.AppManager;
import com.kernal.passport.sdk.utils.HttpUploadDialog;
import com.kernal.passport.sdk.utils.NetworkProber;
import com.kernal.passport.sdk.utils.SharedPreferencesHelper;
import com.kernal.passportreader.sdk.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageVolume;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * Project Name: PassportReader_Sample_Sdk Category name:ShowResultActivity
 * Category description  Creator: yujin  Creating time: 12th, June 2015.
 * Reviser:huangzhen Revising time: 12th, June 2015. Modifying remarks.
 *
 * @version
 *
 */
public class ShowResultActivity extends Activity implements OnClickListener {
	private DisplayMetrics displayMetrics = new DisplayMetrics();
	private int srcWidth, srcHeight;
	private EditText et_recogPicture;
	private ImageView iv_recogPicture;
	private String recogResult = "";
	private String exception;
	private String[] splite_Result;
	private String result = "";
	private Button btn_ok;
	private TextView btn_back;
	private String devcode = "";
	public static String cutPagePath = "";
	public static String fullPagePath = "";
	private boolean isSaveFullPic = false;
	private int VehicleLicenseflag = 0;//  To judge if it comes from Mainactivity. 0 is for yes, 1 is for no.
	private RelativeLayout re_et_recogPicture;
	private FrameLayout FrameLayout_activity_show_result,FrameLayout_toolbar_show_result;
	private TextView tv_set;
	private boolean importRecog=false;
	private int nCropType=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//  hiding titles
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//		WindowManager.LayoutParams.FLAG_FULLSCREEN);//setting up the full screen
		//  Screen always on
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		srcWidth = displayMetrics.widthPixels;
		srcHeight = displayMetrics.heightPixels;
		setContentView(getResources().getIdentifier("activity_show_result",
				"layout", getPackageName()));
		Intent intent = getIntent();
		recogResult = intent.getStringExtra("recogResult");
		exception = intent.getStringExtra("exception");
		devcode = intent.getStringExtra("devcode");
		cutPagePath = intent.getStringExtra("cutPagePath");
		fullPagePath= intent.getStringExtra("fullPagePath");
		VehicleLicenseflag = intent.getIntExtra("VehicleLicenseflag", 0);
		importRecog=intent.getBooleanExtra("importRecog", false);
		nCropType=intent.getIntExtra("nCropType", 0);
		findView();
		//  the came interface being released
		AppManager.getAppManager().finishAllActivity();
	}

	/**
	 * @Title: findView
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return void 返回类型
	 * @throws
	 */
	private void findView() {
		// TODO Auto-generated method stub
		et_recogPicture = (EditText) findViewById(getResources().getIdentifier(
				"et_recogPicture", "id", getPackageName()));
		iv_recogPicture= (ImageView) findViewById(getResources().getIdentifier(
				"iv_recogPicture", "id", getPackageName()));
		re_et_recogPicture=(RelativeLayout) findViewById(getResources().getIdentifier(
				"re_et_recogPicture", "id", getPackageName()));
		FrameLayout_activity_show_result=(FrameLayout) findViewById(getResources().getIdentifier(
				"FrameLayout_activity_show_result", "id", getPackageName()));
		FrameLayout_toolbar_show_result=(FrameLayout) findViewById(getResources().getIdentifier(
				"FrameLayout_toolbar_show_result", "id", getPackageName()));
		btn_back = (TextView) findViewById(getResources()
				.getIdentifier("btn_back", "id", getPackageName()));
		btn_back.setOnClickListener(this);
		btn_ok = (Button) findViewById(getResources()
				.getIdentifier("btn_ok", "id", getPackageName()));
		btn_ok.setOnClickListener(this);
		tv_set =(TextView) findViewById(getResources()
				.getIdentifier("tv_set", "id", getPackageName()));
		tv_set.setOnClickListener(this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				(int)(srcWidth*0.9), (int)(srcWidth*0.9*0.75));
		params.topMargin=(int)(srcHeight*0.06);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		iv_recogPicture.setLayoutParams(params);
		if(cutPagePath!=null&&!cutPagePath.equals("")){
			iv_recogPicture.setImageBitmap(BitmapFactory.decodeFile(cutPagePath));
		}
		params = new RelativeLayout.LayoutParams(
				(int)(srcWidth*0.9), RelativeLayout.LayoutParams.WRAP_CONTENT);

		et_recogPicture.setLayoutParams(params);
		params = new RelativeLayout.LayoutParams(
				(int)(srcWidth*0.9), (int)(srcHeight*0.9-srcWidth*0.9*0.75));
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.topMargin= (int)(srcWidth*0.9*0.75)+(int)(srcHeight*0.06);
		re_et_recogPicture.setLayoutParams(params);
		params = new RelativeLayout.LayoutParams((int) (srcWidth * 0.5),
				(int) (srcWidth * 0.13));
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.BELOW,R.id.et_recogPicture);
		btn_ok.setLayoutParams(params);
		params = new RelativeLayout.LayoutParams(
				srcWidth, (int)(srcHeight*0.06));
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		FrameLayout_toolbar_show_result.setLayoutParams(params);
		params = new RelativeLayout.LayoutParams((int) (srcWidth * 0.145),
				(int) (srcWidth * 0.05));
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.leftMargin=(int)(srcWidth*0.02);
		btn_back.setLayoutParams(params);
		params = new RelativeLayout.LayoutParams((int) (srcWidth * 0.14),
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.leftMargin=(int)(srcWidth*0.84);
		tv_set.setLayoutParams(params);

		if (exception != null && !exception.equals("")) {
			et_recogPicture.setText(exception);
		} else {
			splite_Result = recogResult.split(",");
			for (int i = 0; i < splite_Result.length; i++) {
				if (result.equals("")) {
					result = splite_Result[i] + "\n";
				} else {
					result = result + splite_Result[i] + "\n";
				}

			}
			et_recogPicture.setText(result);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent=null;
		if (getResources().getIdentifier("btn_back", "id",
				this.getPackageName()) == v.getId()) {

			if (importRecog) {
				if (VehicleLicenseflag == 0) {
					intent = new Intent(ShowResultActivity.this, MainActivity.class);
				} else if (VehicleLicenseflag == 1) {
					intent = new Intent(ShowResultActivity.this,
							VehicleLicenseMainActivity.class);
				}else if (VehicleLicenseflag == 2) {
					intent = new Intent(ShowResultActivity.this,
							IdCardMainActivity.class);
				}
				ShowResultActivity.this.finish();

			} else {
				if (!isSaveFullPic) {
					if (fullPagePath != null && !fullPagePath.equals("")) {
						File file = new File(fullPagePath);
						if (file.exists()) {
							file.delete();
						}
					}
				}
				intent = new Intent(ShowResultActivity.this,
						CameraActivity.class);
			intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
					getApplicationContext(), "nMainId", 2));
			intent.putExtra("devcode", devcode);
				intent.putExtra("nCropType", nCropType);
			intent.putExtra("VehicleLicenseflag", VehicleLicenseflag);
			ShowResultActivity.this.finish();

		}
			startActivity(intent);
		} else if (getResources().getIdentifier("btn_ok", "id",
				this.getPackageName()) == v.getId()) {

			if ((!isSaveFullPic)&&(!importRecog)) {
				if (fullPagePath != null && !fullPagePath.equals("")) {
					File file = new File(fullPagePath);
					if (file.exists()) {
						file.delete();
					}
				}
			}else if ((!isSaveFullPic)&&importRecog) {
				if (cutPagePath != null && !cutPagePath.equals("")) {
					File file = new File(cutPagePath);
					if (file.exists()) {
						file.delete();
						sendBroadcast(new Intent(
								Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
								Uri.parse("file://" + cutPagePath)));
					}
				}
			}
			if (VehicleLicenseflag == 0) {
				intent = new Intent(ShowResultActivity.this, MainActivity.class);
				ShowResultActivity.this.finish();
				startActivity(intent);
			} else if (VehicleLicenseflag == 1) {
				intent = new Intent(ShowResultActivity.this,
						VehicleLicenseMainActivity.class);
				ShowResultActivity.this.finish();
				startActivity(intent);
			}else if (VehicleLicenseflag == 2) {
				intent = new Intent(ShowResultActivity.this,
						IdCardMainActivity.class);
				ShowResultActivity.this.finish();
				startActivity(intent);
			}
		} else if (getResources().getIdentifier("tv_set", "id",
				this.getPackageName()) == v.getId()) {
			if(NetworkProber.isNetworkAvailable(ShowResultActivity.this)) {
				File file = new File(ShowResultActivity.fullPagePath);
				if (file.exists() && !file.isDirectory()) {
					HttpUploadDialog.context = ShowResultActivity.this;
					HttpUploadDialog myHttpUploadDialog = new HttpUploadDialog();
					myHttpUploadDialog.show(getFragmentManager(), "HttpUploadDialog");

				} else {
					Toast.makeText(ShowResultActivity.this, getString(getResources().getIdentifier("filenoexists", "string",
							getPackageName())), Toast.LENGTH_SHORT).show();
				}
			}else{
				Toast.makeText(ShowResultActivity.this, getString(getResources().getIdentifier("network_unused", "string",
						getPackageName())), Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			Intent intent = null;
			if (importRecog) {
				if (VehicleLicenseflag == 0) {
					intent = new Intent(ShowResultActivity.this, MainActivity.class);
				} else if (VehicleLicenseflag == 1) {
					intent = new Intent(ShowResultActivity.this,
							VehicleLicenseMainActivity.class);
				}else if (VehicleLicenseflag == 2) {
					intent = new Intent(ShowResultActivity.this,
							IdCardMainActivity.class);
				}
				ShowResultActivity.this.finish();

			} else {
				if (!isSaveFullPic) {
					if (fullPagePath != null && !fullPagePath.equals("")) {
						File file = new File(fullPagePath);
						if (file.exists()) {
							file.delete();
						}
					}
				}
				intent = new Intent(ShowResultActivity.this,
						CameraActivity.class);
				intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
						getApplicationContext(), "nMainId", 2));
				intent.putExtra("devcode", devcode);
				intent.putExtra("VehicleLicenseflag", VehicleLicenseflag);
				ShowResultActivity.this.finish();

			}
			startActivity(intent);
		}
		return true;
	}
}
