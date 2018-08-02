package com.kernal.passportreader.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import kernal.idcard.android.AuthParameterMessage;
import kernal.idcard.android.AuthService;
import kernal.idcard.android.RecogParameterMessage;
import kernal.idcard.android.RecogService;
import kernal.idcard.android.ResultMessage;

import com.kernal.passport.sdk.utils.ActivityRecogUtils;
import com.kernal.passport.sdk.utils.AppManager;
import com.kernal.passport.sdk.utils.CheckPermission;
import com.kernal.passport.sdk.utils.Devcode;
import com.kernal.passport.sdk.utils.PermissionActivity;
import com.kernal.passport.sdk.utils.SharedPreferencesHelper;

import android.Manifest;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
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
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
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

public class IdCardMainActivity extends Activity implements
		OnClickListener {
	private DisplayMetrics displayMetrics = new DisplayMetrics();
	private int srcWidth, srcHeight;
	private Button btn_chooserIdCardType, btn_takePicture, btn_exit,
			btn_importRecog, btn_ActivationProgram,btn_Intelligent_detecting_edges,btn_cancel_imei;
	private boolean isQuit = false;
	private Timer timer = new Timer();
	public static int DIALOG_ID = -1;
	private String[] type;
	public RecogService.recogBinder recogBinder;
	private String recogResultString = "";
	private String selectPath = "";
	private AuthService.authBinder authBinder;
	private int ReturnAuthority = -1;
	private String sn = "";
	private AlertDialog dialog;
	private EditText editText;
	private String devcode = Devcode.devcode;// project licensing development code
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
				// apm.datefile = "assets"; // PATH+"/IDCard/wtdate.lsc";//Reservation
				apm.devcode = devcode;
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
	static final String[] PERMISSION = new String[] {
			Manifest.permission.WRITE_EXTERNAL_STORAGE,// Write access
			Manifest.permission.READ_EXTERNAL_STORAGE, // read access
			Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE,
			Manifest.permission.VIBRATE, Manifest.permission.INTERNET
			 };
	private int sortOperaPermission=0;
	private Handler sortOperaHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(sortOperaPermission==0)
			{
				Intent intent = new Intent(IdCardMainActivity.this,
						CameraActivity.class);
				intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
						getApplicationContext(), "nMainId", 2));
				intent.putExtra("devcode", devcode);
				intent.putExtra("flag", 0);
				intent.putExtra("nCropType", 0);
				intent.putExtra("VehicleLicenseflag", 2);
				IdCardMainActivity.this.finish();
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
	protected void onStart() {
		super.onStart();
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
		// the came interface being released
		AppManager.getAppManager().finishAllActivity();
		SharedPreferencesHelper.putInt(getApplicationContext(), "nMainId", 2);
	}

	/**
	 * @Title: findView @Description: TODO(这里用一句话描述这个方法的作用) @param 设定文件 @return
	 *         void 返回类型 @throws
	 */
	private void findView() {
		// TODO Auto-generated method stub
		btn_chooserIdCardType = (Button) findViewById(getResources()
				.getIdentifier("btn_chooserIdCardType", "id", getPackageName()));
		btn_takePicture = (Button) findViewById(getResources().getIdentifier(
				"btn_takePicture", "id", getPackageName()));
		btn_exit = (Button) findViewById(getResources().getIdentifier(
				"btn_exit", "id", getPackageName()));
		btn_importRecog = (Button) findViewById(getResources().getIdentifier(
				"btn_importRecog", "id", getPackageName()));
		btn_ActivationProgram = (Button) this
				.findViewById(getResources().getIdentifier(
						"btn_ActivationProgram", "id", getPackageName()));
		btn_ActivationProgram.setVisibility(View.INVISIBLE);
		btn_cancel_imei= (Button) this
				.findViewById(getResources().getIdentifier(
						"btn_cancel_imei", "id", getPackageName()));
		btn_cancel_imei.setVisibility(View.INVISIBLE);
		btn_Intelligent_detecting_edges= (Button) this
				.findViewById(getResources().getIdentifier(
						"btn_Intelligent_detecting_edges", "id", getPackageName()));
		btn_ActivationProgram.setOnClickListener(this);
		btn_chooserIdCardType.setOnClickListener(this);
		btn_Intelligent_detecting_edges.setOnClickListener(this);
		btn_takePicture.setOnClickListener(this);
		btn_exit.setOnClickListener(this);
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
		btn_chooserIdCardType.setVisibility(View.GONE);
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
		if (getResources().getIdentifier("btn_ActivationProgram", "id",
				this.getPackageName()) == v.getId()) {
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

		} else if (getResources().getIdentifier("btn_takePicture", "id",
				this.getPackageName()) == v.getId()) {
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
			 */
			sortOperaPermission=0;
			if (Build.VERSION.SDK_INT >= 23) {
				CheckPermission checkPermission = new CheckPermission(this);
				if (checkPermission.permissionSet(PERMISSION)) {
					PermissionActivity.startActivityForResult(this,sortOperaHandler, 0,
							SharedPreferencesHelper.getInt(
									getApplicationContext(), "nMainId", 2),
							devcode, 0, 2,0, PERMISSION);
				} else {
					intent = new Intent(IdCardMainActivity.this,
							CameraActivity.class);
					intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
							getApplicationContext(), "nMainId", 2));
					intent.putExtra("devcode", devcode);
					intent.putExtra("flag", 0);
					intent.putExtra("nCropType", 0);
					intent.putExtra("VehicleLicenseflag", 2);
					IdCardMainActivity.this.finish();
					startActivity(intent);
				}
			} else {
				intent = new Intent(IdCardMainActivity.this,
						CameraActivity.class);
				intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
						getApplicationContext(), "nMainId", 2));
				intent.putExtra("devcode", devcode);
				intent.putExtra("flag", 0);
				intent.putExtra("nCropType", 0);
				intent.putExtra("VehicleLicenseflag", 2);
				IdCardMainActivity.this.finish();
				startActivity(intent);
			}
		}else if (getResources().getIdentifier("btn_Intelligent_detecting_edges", "id",
				this.getPackageName()) == v.getId()) {
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
			 */
			sortOperaPermission=0;
			if (Build.VERSION.SDK_INT >= 23) {
				CheckPermission checkPermission = new CheckPermission(this);
				if (checkPermission.permissionSet(PERMISSION)) {
					PermissionActivity.startActivityForResult(this,sortOperaHandler, 0,
							SharedPreferencesHelper.getInt(
									getApplicationContext(), "nMainId", 2),
							devcode, 0, 2,1, PERMISSION);
				} else {
					intent = new Intent(IdCardMainActivity.this,
							CameraActivity.class);
					intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
							getApplicationContext(), "nMainId", 2));
					intent.putExtra("devcode", devcode);
					intent.putExtra("flag", 0);
					intent.putExtra("nCropType", 1);
					intent.putExtra("VehicleLicenseflag", 2);
					IdCardMainActivity.this.finish();
					startActivity(intent);
				}
			} else {
				intent = new Intent(IdCardMainActivity.this,
						CameraActivity.class);
				intent.putExtra("nMainId", SharedPreferencesHelper.getInt(
						getApplicationContext(), "nMainId", 2));
				intent.putExtra("devcode", devcode);
				intent.putExtra("flag", 0);
				intent.putExtra("nCropType", 1);
				intent.putExtra("VehicleLicenseflag", 2);
				IdCardMainActivity.this.finish();
				startActivity(intent);
			}
		}else if (getResources().getIdentifier("btn_exit", "id",
				this.getPackageName()) == v.getId()) {
			IdCardMainActivity.this.finish();

		} else if (getResources().getIdentifier("btn_importRecog", "id",
				this.getPackageName()) == v.getId()) {
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

		}

	}

	/**
	 * @Title: activationProgramOpera @Description: TODO(这里用一句话描述这个方法的作用) @param
	 *         设定文件 @return void 返回类型 @throws
	 */
	private void activationProgramOpera() {
		// TODO Auto-generated method stub
		DIALOG_ID = 1;
		View view = getLayoutInflater().inflate(R.layout.serialdialog, null);
		editText = (EditText) view.findViewById(R.id.serialdialogEdittext);
		dialog = new Builder(IdCardMainActivity.this)
				.setView(view)
				.setPositiveButton(getString(R.string.online_activation),
						new DialogInterface.OnClickListener() {

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
								if (isNetworkConnected(IdCardMainActivity.this)) {
									if (isWifiConnected(IdCardMainActivity.this)
											|| isMobileConnected(IdCardMainActivity.this)) {
										startAuthService();
										dialog.dismiss();
									} else if (!isWifiConnected(IdCardMainActivity.this)
											&& !isMobileConnected(IdCardMainActivity.this)) {
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

							public void onClick(DialogInterface dialog,
												int which) {
								dialog.dismiss();

							}

						}).create();
		dialog.show();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 9 && resultCode == Activity.RESULT_OK) {
				Uri uri = data.getData();
				selectPath = getPath(getApplicationContext(), uri);
				RecogService.nMainID = SharedPreferencesHelper.getInt(
						getApplicationContext(), "nMainId", 2);
				new Thread(){
					@Override
					public void run() {
						RecogService.isRecogByPath = true;
						Intent recogIntent = new Intent(IdCardMainActivity.this,
								RecogService.class);
						bindService(recogIntent, recogConn, Service.BIND_AUTO_CREATE);
					}}.start();
				//ActivityRecogUtils.getRecogResult(IdCardMainActivity.this, selectPath, RecogService.nMainID, true);

		} else if (requestCode == 8 && resultCode == Activity.RESULT_OK) {
			//Activtiy recognize returned results
			int ReturnAuthority = data.getIntExtra("ReturnAuthority", -100000);// get activation status
			ResultMessage resultMessage = new ResultMessage();
			resultMessage.ReturnAuthority = data.getIntExtra("ReturnAuthority", -100000);//get activation status
			resultMessage.ReturnInitIDCard = data
					.getIntExtra("ReturnInitIDCard", -100000);// Get initialization return value
			resultMessage.ReturnLoadImageToMemory = data.getIntExtra(
					"ReturnLoadImageToMemory", -100000);//Get the image return value
			resultMessage.ReturnRecogIDCard = data.getIntExtra("ReturnRecogIDCard",
					-100000);//  Get the recogniion reurn value
			resultMessage.GetFieldName = (String[]) data
					.getSerializableExtra("GetFieldName");
			resultMessage.GetRecogResult = (String[]) data
					.getSerializableExtra("GetRecogResult");
			ActivityRecogUtils.goShowResultActivity(IdCardMainActivity.this, resultMessage,2,selectPath,selectPath.substring(0,selectPath.indexOf(".jpg"))+ "Cut.jpg");
		}
	}

	//Recognition Test
	public ServiceConnection recogConn = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
			recogBinder = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {

			recogBinder = (RecogService.recogBinder) service;
			if(recogBinder!=null)
			{

						RecogParameterMessage rpm = new RecogParameterMessage();
						rpm.nTypeLoadImageToMemory = 0;
						rpm.nMainID = 2;
						rpm.nSubID = null;
						rpm.GetSubID = true;
						rpm.GetVersionInfo = true;
						rpm.logo = "";
						rpm.userdata = "";
						rpm.sn = "";
						rpm.authfile = "";
						rpm.isCut = true;
						rpm.triggertype = 0;
						rpm.devcode = devcode;
						rpm.isAutoClassify = true;
						rpm.isOnlyClassIDCard = true;
				        //nProcessType：0- deleting all instructions 1- cropping 2- rotation  3- cropping and rotation
				        // 4- tilt correction 5- cropping+ tilt correction 6- rotation+tile correct
				        // 7- cropping+rotation+tilt correction.
						rpm.nProcessType = 7;
						rpm.nSetType = 1;// nSetType: 0－Deleting operations，1－setting operations
						rpm.lpFileName = selectPath; // // If rpm.lpFileName is null, auomatic recognition function will be executed.
						// rpm.lpHeadFileName = selectPath;//Save portrait of identity document
						rpm.isSaveCut = true;// Save cropping picture false=not saving  true=saving
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
								Intent intent = new Intent(IdCardMainActivity.this,
										ShowResultActivity.class);
								intent.putExtra("fullPagePath", selectPath);
								intent.putExtra("cutPagePath", selectPath.substring(0, selectPath.indexOf(".jpg")) + "Cut.jpg");
								intent.putExtra("recogResult", recogResultString);
								intent.putExtra("importRecog", true);
								intent.putExtra("VehicleLicenseflag", 2);
								IdCardMainActivity.this.finish();
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
								Intent intent = new Intent(IdCardMainActivity.this,
										ShowResultActivity.class);
								intent.putExtra("exception", string);
								intent.putExtra("VehicleLicenseflag", 2);
								intent.putExtra("fullPagePath", selectPath);
								intent.putExtra("importRecog", true);
								IdCardMainActivity.this.finish();
								startActivity(intent);
							}
						} catch (Exception e) {
							e.printStackTrace();
//							Toast.makeText(getApplicationContext(),
//									getString(R.string.recognized_failed),
//									Toast.LENGTH_SHORT).show();

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
		Intent authIntent = new Intent(IdCardMainActivity.this,
				AuthService.class);
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
	 *
	 * @Title: getPath
	 * @Description: TODO(这里用一句话描述这个方法的作用) 获取图片路径
	 * @param @param context @param @param uri @param @return 设定文件 @return
	 *        String 返回类型 @throws
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
}
