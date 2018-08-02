package com.kernal.passportreader.sdk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import com.kernal.lisence.DeviceFP;
import com.kernal.passport.sdk.utils.ActivityRecogUtils;
import com.kernal.passport.sdk.view.ViewfinderView;
import com.kernal.passport.sdk.utils.AppManager;
import com.kernal.passport.sdk.utils.CameraParametersUtils;
import com.kernal.passport.sdk.utils.Devcode;
import com.kernal.passport.sdk.utils.FrameCapture;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import kernal.idcard.android.Frame;
import kernal.idcard.android.RecogParameterMessage;
import kernal.idcard.android.RecogService;
import kernal.idcard.android.ResultMessage;
/**
 *
 *
 * Projec Name: ssportReader_Sample_Sdk
 * Category Name: CameraActivity
 *  Category description: manual shooting to process passport MRZ. Creator: Huangzhen  Creating time: 10th, July 2014. Reviser: Huangzhen
 *Notes: modfiy the camere interface to make the program designing more beautiful.
 *
 * @version
 *
 */
@SuppressLint("NewApi")
public class CameraActivity extends Activity implements SurfaceHolder.Callback,
		Camera.PreviewCallback, OnClickListener {
	public String PATH = Environment.getExternalStorageDirectory().toString()
			+ "/wtimage/";
	private int width, height, WIDTH, HEIGHT;
	private Camera camera;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private RelativeLayout rightlyaout, bg_camera_doctype;
	private ToneGenerator tone;
	public RecogService.recogBinder recogBinder;
	private DisplayMetrics displayMetrics = new DisplayMetrics();
	private boolean istakePic = false;// To judge if it has taken a picture. If having taken a pciture, a reminder will occur that the processing is on the way, please wait.
	private long time1, recogTime;
	private ViewfinderView viewfinder_view;
	private int uiRot = Surface.ROTATION_0, tempUiRot = 0, rotation = 0;
	private Bitmap rotate_bitmap;
	private int rotationWidth, rotationHeight;
	private Camera.Parameters parameters;
	private boolean isOpenFlash = false;
	private ImageButton imbtn_flash, imbtn_camera_back, imbtn_takepic,
			imbtn_eject;
	private byte[] data1;
	private int regWidth, regHeight, left, right, top, bottom, nRotateType;
	private TextView tv_camera_doctype;
	private int lastPosition = 0;
	// end
	private int quality = 100;
	private final String IDCardPath = Environment.getExternalStorageDirectory()
			.toString() + "/AndroidWT/IdCapture/";
	private String picPathString = PATH + "WintoneIDCard.jpg";
	private String HeadJpgPath = PATH + "head.jpg";
	private String recogResultPath = PATH + "idcapture.txt",
			recogResultString = "";
	private double screenInches;
	private int[] nflag = new int[4];
	private boolean isTakePic = false;
	private String devcode = "";
	public static int nMainIDX;
	private Vibrator mVibrator;
	private int Format = ImageFormat.NV21;// .YUY2
	private String name = "";
	public boolean isTouched = false;
	private boolean isFirstGetSize = true;
	private Size size;
	private Message msg;
	private Handler mAutoFocusHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 100) {
				autoFocus();
			}
		}
	};
	private Runnable touchTimeOut = new Runnable() {
		@Override
		public void run() {

			isTouched = false;
		}
	};
	public static Handler resetIsTouchedhandler = new Handler();
	private int DelayedFrames = -1;
	// private boolean isConfirmSideLine = true;
	private int ConfirmSideSuccess = -1;
	private int LoadBufferImage = -1;
	private CameraParametersUtils cameraParametersUtils;
	private List<Size> list;//  Store sets of preview resolution
	private boolean isSetCameraParamter = false;
	public static boolean isOpendetectLightspot = false;
	private ImageView imbtn_spot_dection;
	private Intent recogIntent;
	private Frame frame = new Frame();
	private float scale;
	private int nSubID=0;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			cameraParametersUtils.setScreenSize(CameraActivity.this);
			width = cameraParametersUtils.srcWidth;
			height = cameraParametersUtils.srcHeight;
			rotation = CameraParametersUtils.setRotation(width, height, uiRot,
					rotation);
			if (msg.what == 100) {
				if (rotation == 0 || rotation == 180) {
					findLandscapeView();
				} else if (rotation == 90 || rotation == 270) {
					findPortraitView();
				}
			} else {

				if (rotation == 0 || rotation == 180) {
					changeCameraParammter(uiRot);
					findLandscapeView();
				} else if (rotation == 90 || rotation == 270) {
					changeCameraParammter(uiRot);
					findPortraitView();
				}
			}

			isTouched = false;
			isSetCameraParamter = false;
		}
	};
	private Toast mtoast;
	private Handler rejectRecogHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (resultMessage != null && resultMessage.ReturnRecogIDCard == -6) {
				/**
				 *To judge conditions of recognition rejection except driving license and identity card.
				 */
				viewfinder_view.FRAMECOLOR = Color.rgb(238, 65, 86);
				if(nCropType==1&&frame.ltStartX == 0
						&& frame.ltStartY == 0
						&& frame.rtStartX == 0
						&& frame.rtStartY == 0
						&& frame.lbStartX == 0
						&& frame.lbStartY == 0
						&& frame.rbStartX == 0 && frame.rbStartY == 0){
					viewfinder_view.setFourLines(frame,"");
				}else{
					viewfinder_view.setFourLines(frame,getString(getResources().getIdentifier("please_place",
							"string", getPackageName())) + tv_camera_doctype.getText().toString());
				}
				rejectRecogHandler.sendEmptyMessageDelayed(100, 600);

			} else if (ConfirmSideSuccess == -139) {
				/**
				 * To judge conditions of recognition rejection of driving license and identity card.
				 */
				viewfinder_view.FRAMECOLOR = Color.rgb(238, 65, 86);
				/**
				 * It is required that a reminder should occur to infrom the synchronic changes of words and frames
				 */
				if(nCropType==1&&frame.ltStartX == 0
						&& frame.ltStartY == 0
						&& frame.rtStartX == 0
						&& frame.rtStartY == 0
						&& frame.lbStartX == 0
						&& frame.lbStartY == 0
						&& frame.rbStartX == 0 && frame.rbStartY == 0){
                    viewfinder_view.setFourLines(frame,"");
				}else{
                    viewfinder_view.setFourLines(frame,getString(getResources().getIdentifier("please_place",
                            "string", getPackageName()))+ tv_camera_doctype.getText().toString());
                }
				rejectRecogHandler.sendEmptyMessageDelayed(100, 600);
			} else if (ConfirmSideSuccess == -145) {
				/**
				 * to judge if all the documents are far away from camera
				 */
				viewfinder_view.FRAMECOLOR = Color.rgb(238, 65, 86);
				/**
				 * It is required that a reminder should occur to infrom the synchronic changes of words and frames
				 */
				if(nCropType==1&&frame.ltStartX == 0
						&& frame.ltStartY == 0
						&& frame.rtStartX == 0
						&& frame.rtStartY == 0
						&& frame.lbStartX == 0
						&& frame.lbStartY == 0
						&& frame.rbStartX == 0 && frame.rbStartY == 0){
                    viewfinder_view.setFourLines(frame,"");
				}else{
                    viewfinder_view.setFourLines(frame,getString(getResources().getIdentifier("too_far_away",
                            "string", getPackageName())));
                }
				rejectRecogHandler.sendEmptyMessageDelayed(100, 600);
			} else {
				viewfinder_view.FRAMECOLOR = Color.rgb(77, 223, 68);
				viewfinder_view.setFourLines(frame,"");
				rejectRecogHandler.sendEmptyMessageDelayed(101, 600);
			}
			if (resultMessage != null) {
				if (msg.what == 100) {
					/**
					 * After displaying the reminder, the classfication value should return to zero, preventing it from displaying all the time.
					 */
					if (resultMessage != null&&resultMessage.ReturnRecogIDCard<0) {
						resultMessage.ReturnRecogIDCard = -1;
					}
				}

			}
		}
	};
	private Handler detectLightspotHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				if (mtoast != null) {
					mtoast.setText(R.string.detectLightspot);
				} else {
					/*
					 * The first paramater: the context decides to use “this” or getApplicationContext()
					 * The second paramater: the displaying character string can be symbolized by R. string.
					 * The third paramater: the displaying time length can be symbolized by LENGTH_LONG or LENGTH_SHORT or millisecond.
					 */
					mtoast = Toast.makeText(getApplicationContext(),
							getString(R.string.detectLightspot),
							Toast.LENGTH_SHORT);
				}

			} else if (msg.what == 2) {
				// imbtn_spot_dection
				// .setBackgroundResource(R.drawable.spot_dection_on);

				if (mtoast != null) {
					mtoast.setText(R.string.opendetectLightspot);
				} else {
					/*
					 * The first paramater: the context decides to use “this” or getApplicationContext()
					 * The second paramater: the displaying character string can be symbolized by R. string.
					 * The third paramater: the displaying time length can be symbolized by LENGTH_LONG or LENGTH_SHORT or millisecond.
					 */
					mtoast = Toast.makeText(getApplicationContext(),
							getString(R.string.opendetectLightspot),
							Toast.LENGTH_SHORT);
				}
			} else if (msg.what == 3) {
				// imbtn_spot_dection
				// .setBackgroundResource(R.drawable.spot_dection_off);
				if (mtoast != null) {
					mtoast.setText(R.string.closeddetectLightspot);
				} else {
					/*
					 * The first paramater: the context decides to use “this” or getApplicationContext()
					 * The second paramater: the displaying character string can be symbolized by R. string.
					 * The third paramater: the displaying time length can be symbolized by LENGTH_LONG or LENGTH_SHORT or millisecond.
					 */
					mtoast = Toast.makeText(getApplicationContext(),
							getString(R.string.closeddetectLightspot),
							Toast.LENGTH_SHORT);
				}
			}
			//  After displaying 500 millisecond, toast disappears.
			if (Build.VERSION.SDK_INT < 24) {
				showMyToast(mtoast, 300);
			} else {
				mtoast.setDuration(0);
				mtoast.show();
			}


		}
	};
	private Runnable updateRejectRecog=new Runnable() {
		@Override
		public void run() {
			ViewfinderView.FRAMECOLOR = Color.rgb(77, 223, 68);
			rejectRecogHandler.removeCallbacksAndMessages(null);
			viewfinder_view.setFourLines(frame,"");
		}
	};
	private Runnable updateUI = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			cameraParametersUtils.setScreenSize(CameraActivity.this);
			width = cameraParametersUtils.srcWidth;
			height = cameraParametersUtils.srcHeight;
			rotation = CameraParametersUtils.setRotation(width, height, uiRot,
					rotation);
			if (rotation == 0 || rotation == 180) {
				cameraParametersUtils.getCameraPreParameters(camera, rotation,
						list);
				setCameraParamters();
				findLandscapeView();
			} else if (rotation == 90 || rotation == 270) {
				cameraParametersUtils.getCameraPreParameters(camera, rotation,
						list);
				setCameraParamters();
				findPortraitView();
			}
			isTouched = false;
			isSetCameraParamter = false;
		}

	};
	// Recognition test

	public ServiceConnection recogConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			recogBinder = null;
		}
        @Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			recogBinder = (RecogService.recogBinder) service;
			if(recogBinder!=null){
				if(recogBinder.getReturnAuthority()!=0){
					Toast.makeText(getApplicationContext(),getString(getResources()
							.getIdentifier("exception2", "string", getPackageName()))+":"+recogBinder.getReturnAuthority(),Toast.LENGTH_SHORT).show();
				};
			}

		}
		;

	};
	private int flag = 0;// To recognize the marks on obverse and reverse sides 0- obverse and reverse sides  1-obverse side  2- reverse side
	private boolean isTakePicRecog = false;//  To judge if a compulsive recognition should be executed.
	public static boolean isTakePicRecogFrame = false;
	private String picPathString1 = "";
	private int detectLightspot = 0;
	private int VehicleLicenseflag = 0;//  frow which interface to skip to camera interface. “0” means from the MainActivity interface, “1” means from VehicleLicenseMainActivity interface, “2” means from identity card interface.
	private int nCropType = 0;
	private ResultMessage resultMessage;
	private TextView tv_reject_recog;

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (resetIsTouchedhandler == null) {
			resetIsTouchedhandler = new Handler();
		}
		sum = 0;
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
		RecogService.isRecogByPath = false;
		RecogService.isOnlyReadSDAuthmodeLSC = false;// If it is true, the programs will no longer copy the files in assets into SD card, including licensing files. Such paramater setting should be prior to invoking.
		bg_camera_doctype = (RelativeLayout) findViewById(getResources()
				.getIdentifier("bg_camera_doctype", "id", getPackageName()));
		viewfinder_view = (ViewfinderView) findViewById(getResources()
				.getIdentifier("viewfinder_view", "id", getPackageName()));
		surfaceView = (SurfaceView) findViewById(getResources().getIdentifier(
				"surfaceViwe", "id", getPackageName()));
		imbtn_flash = (ImageButton) findViewById(getResources().getIdentifier(
				"imbtn_flash", "id", getPackageName()));
		imbtn_camera_back = (ImageButton) this.findViewById(getResources()
				.getIdentifier("imbtn_camera_back", "id", getPackageName()));
		imbtn_takepic = (ImageButton) findViewById(getResources()
				.getIdentifier("imbtn_takepic", "id", getPackageName()));
		imbtn_takepic.setOnClickListener(this);
		imbtn_eject = (ImageButton) findViewById(getResources().getIdentifier(
				"imbtn_eject", "id", getPackageName()));
		imbtn_spot_dection = (ImageView) findViewById(getResources()
				.getIdentifier("imbtn_spot_dection", "id", getPackageName()));
		imbtn_eject.setOnClickListener(this);
		imbtn_eject.setVisibility(View.VISIBLE);
		tv_camera_doctype = (TextView) this.findViewById(getResources()
				.getIdentifier("tv_camera_doctype", "id", getPackageName()));
		tv_reject_recog = (TextView) this.findViewById(getResources()
				.getIdentifier("tv_reject_recog", "id", getPackageName()));
		tv_reject_recog.setText("");
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(CameraActivity.this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		Intent intent = getIntent();
		nMainIDX = intent.getIntExtra("nMainId", 2);
		devcode = intent.getStringExtra("devcode");
		flag = intent.getIntExtra("flag", 0);
		VehicleLicenseflag = intent.getIntExtra("VehicleLicenseflag", 0);
		nCropType = intent.getIntExtra("nCropType", 0);
		nSubID= intent.getIntExtra("nSubID", 0);
		viewfinder_view.setIdcardType(nMainIDX);
		viewfinder_view.setnCropType(nCropType);
		viewfinder_view.setTvRejectRecog(tv_reject_recog);
		tv_camera_doctype.setTextColor(Color.rgb(238, 65, 86));
		switch (nMainIDX) {
			case 3000:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("mrz","string",getPackageName())));
				break;
			case 13:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("passport","string",getPackageName())));
				break;
			case 2:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("ID_card","string",getPackageName())));
				break;
			case 4:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("Interim_ID_card","string",getPackageName())));
				break;

			case 9:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("EPT_HK_Macau","string",getPackageName())));
				break;
			case 11:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("MRTTTP","string",getPackageName())));
				break;
			case 12:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("visa","string",getPackageName())));
				break;
			case 22:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("NEEPT_HK_Macau","string",getPackageName())));
				break;
			case 5:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("china_driver","string",getPackageName())));
				break;
			case 28:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("china_driver01","string",getPackageName())));
				break;
			case 6:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("china_driving_license","string",getPackageName())));
				break;
			case 1001:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("HK_IDcard","string",getPackageName())));
				break;
			case 16:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("Household_Register","string",getPackageName())));
				break;
			case 14:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("HRPO","string",getPackageName())));
				break;
			case 15:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("HRPR","string",getPackageName())));
				break;
			case 1005:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("IDCard_Macau","string",getPackageName())));
				break;
			case 1012:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("New_IDCard_Macau","string",getPackageName())));
				break;
			case 1013:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("Shenzhen_Resident_Permit","string",getPackageName())));
				break;

			case 10:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("TRTTTMTP","string",getPackageName())));
				break;
			case 25:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("NTRTTTMTP","string",getPackageName())));
				break;
			case 26:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("NTRTTTMTP_01","string",getPackageName())));
				break;
			case 1031:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("Taiwan_IDcard_front","string",getPackageName())));
				break;
			case 1032:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("Taiwan_IDcard_reverse","string",getPackageName())));
				break;
			case 1030:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("National_health_insurance_card","string",getPackageName())));
				break;
			case 2001:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("MyKad","string",getPackageName())));
				break;
			case 2004:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("Singapore_IDcard","string",getPackageName())));
				break;
			case 2003:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("Driver_license","string",getPackageName())));
				break;
			case 2002:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("California_driver_license","string",getPackageName())));
				break;
			case 2010:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("Indonesia_Id_Card","string",getPackageName())));
				break;
			case 2011:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("Thailand_id_card","string",getPackageName())));
				break;
			case 1021:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("Beijingsscard","string",getPackageName())));
				break;
			case 7:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("ChineseOfficer","string",getPackageName())));
				break;
			case 1039:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("CXSS_Card_Portrait_Page","string",getPackageName())));
				break;
			case 1041:
				tv_camera_doctype.setText(getString(getResources().getIdentifier("CFSS_Card_Portrait_Page","string",getPackageName())));
				break;
			case 2012:
				if(nSubID==1) {
					tv_camera_doctype
							.setText(getString(getResources().getIdentifier("Thailand_Driving_License","string",getPackageName())));
				}else if(nSubID==2){
					tv_camera_doctype
							.setText(getString(getResources().getIdentifier("Thailand_Driving_License_private","string",getPackageName())));
				}
				break;
			case 2013:
				if(nSubID==1) {
					tv_camera_doctype
							.setText(getString(getResources().getIdentifier("Mexico_I_Portrait_Page_AB","string",getPackageName())));
				}else if(nSubID==2){
					tv_camera_doctype
							.setText(getString(getResources().getIdentifier("Mexico_I_Portrait_Page_C","string",getPackageName())));
				}else if(nSubID==3){
					tv_camera_doctype
							.setText(getString(getResources().getIdentifier("Mexico_I_Portrait_Page_DE","string",getPackageName())));
				}
				break;
			case 2014:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("Mexico_I_Reverse_Page","string",getPackageName())));
				break;
			case 2021:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("Malaysia_Driving_license","string",getPackageName())));
				break;
			case 2031:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("Singapore_Driving_License","string",getPackageName())));
				break;
			case 2041:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("Indonesia_Driving_License","string",getPackageName())));
				break;
			case 2051:
				tv_camera_doctype
						.setText(getString(getResources().getIdentifier("Japan_Driving_License","string",getPackageName())));
				break;
			default:
				break;
		}

	}

	// private long natiantime=0;
	@Override
	@SuppressLint("NewApi")
	@TargetApi(19)
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (Build.VERSION.SDK_INT > 18) {
			Window window = getWindow();
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		setContentView(getResources().getIdentifier("demo_camera",
				"layout", getPackageName()));
		cameraParametersUtils = new CameraParametersUtils(CameraActivity.this);
		width = cameraParametersUtils.srcWidth;
		height = cameraParametersUtils.srcHeight;
		// physical size of Andriod device
		double x = Math
				.pow(displayMetrics.widthPixels / displayMetrics.xdpi, 2);
		double y = Math.pow(displayMetrics.heightPixels / displayMetrics.ydpi,
				2);
		screenInches = Math.sqrt(x + y);
		// physical size of Andriod device end
		cameraParametersUtils.hiddenVirtualButtons(getWindow().getDecorView());
		rotationWidth = displayMetrics.widthPixels;
		rotationHeight = displayMetrics.heightPixels;
		AppManager.getAppManager().addActivity(CameraActivity.this);
	}

	/**
	 * @Title: findPortraitView @Description: 界面竖屏布局 @param 设定文件 @return void
	 * 返回类型 @throws
	 */
	public void findPortraitView() {
		isTakePicRecog = false;
		isTakePicRecogFrame = false;
		imbtn_takepic.setVisibility(View.GONE);
		imbtn_camera_back.setOnClickListener(this);
		imbtn_flash.setOnClickListener(this);
		// TODO Auto-generated method stub
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		uiRot = getWindowManager().getDefaultDisplay().getRotation();
		viewfinder_view.setDirecttion(uiRot);
		// UI layout of strobe light
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				(int) (height * 0.05), (int) (height * 0.05));
		layoutParams.leftMargin = (int) (width * 0.89);
		layoutParams.topMargin = (int) (height * 0.08);
		imbtn_flash.setLayoutParams(layoutParams);
		// UI layout of return push-button
		layoutParams = new RelativeLayout.LayoutParams((int) (height * 0.05),
				(int) (height * 0.05));
		layoutParams.leftMargin = (int) (width * 0.02);
		layoutParams.topMargin = (int) (height * 0.08);
		imbtn_camera_back.setLayoutParams(layoutParams);
		//  UI layout of spot detection
		layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.15),
				(int) (width * 0.12));
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		layoutParams.topMargin = (int) (height * 0.07);
		imbtn_spot_dection.setLayoutParams(layoutParams);
		imbtn_spot_dection.setOnClickListener(this);
		if (isOpendetectLightspot) {
			imbtn_spot_dection
					.setBackgroundResource(R.drawable.spot_dection_on);

		} else {
			imbtn_spot_dection
					.setBackgroundResource(R.drawable.spot_dection_off);

		}
		int surfaceWidth = cameraParametersUtils.surfaceWidth;
		int surfaceHeight = cameraParametersUtils.surfaceHeight;
		if (height == surfaceView.getHeight() || surfaceView.getHeight() == 0) {
			layoutParams = new RelativeLayout.LayoutParams(width, height);
			surfaceView.setLayoutParams(layoutParams);
			// UI layout of document type backgroud
//			layoutParams = new RelativeLayout.LayoutParams(
//					(int) (height * 0.65), (int) (height * 0.05));
//			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//			layoutParams.topMargin = (int) (height * 0.46);
//			bg_camera_doctype.setLayoutParams(layoutParams);
			//Layout of recognition rejection reminder
			layoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			layoutParams.topMargin = (int) (height * 0.8);
			tv_reject_recog.setLayoutParams(layoutParams);
			// layout of shooting button
			layoutParams = new RelativeLayout.LayoutParams(
					(int) (height * 0.1), (int) (height * 0.1));
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			layoutParams.topMargin = (int) (height * 0.75);
			imbtn_takepic.setLayoutParams(layoutParams);
		}

		// layout of displaying shooting button
		layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.6),
				(int) (height * 0.03));
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		imbtn_eject.setLayoutParams(layoutParams);
		imbtn_eject.setBackgroundResource(R.drawable.locker_btn_def01);
		if (surfaceWidth < width || surfaceHeight < height) {

			layoutParams = new RelativeLayout.LayoutParams(surfaceWidth,
					surfaceHeight);
			layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			surfaceView.setLayoutParams(layoutParams);
			//  layout of shooting button
			layoutParams = new RelativeLayout.LayoutParams(
					(int) (height * 0.1), (int) (height * 0.1));
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			layoutParams.leftMargin = (int) (width * 0.83);
			imbtn_takepic.setLayoutParams(layoutParams);


		}

		if (screenInches >= 8) {
			tv_camera_doctype.setTextSize(25);
			tv_reject_recog.setTextSize(25);
		} else {
			tv_camera_doctype.setTextSize(20);
			tv_reject_recog.setTextSize(20);
		}
		if (nMainIDX == 3000) {
			//  It is still unclear about the classification of automatic MRZ recognition, the compulsive shooting functions of MRZ can be hidden temporarily
			imbtn_eject.setVisibility(View.GONE);
		} else {
			imbtn_eject.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * @Title: findLandscapeView @Description: 界面横屏布局 @param 设定文件 @return void
	 * 返回类型 @throws
	 */
	public void findLandscapeView() {
		isTakePicRecog = false;
		isTakePicRecogFrame = false;
		imbtn_takepic.setVisibility(View.GONE);
		imbtn_camera_back.setOnClickListener(this);
		imbtn_flash.setOnClickListener(this);
		// TODO Auto-generated method stub
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		uiRot = getWindowManager().getDefaultDisplay().getRotation();
		viewfinder_view.setDirecttion(uiRot);
		//  UI layout of strobe light
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				(int) (width * 0.05), (int) (width * 0.05));
		layoutParams.leftMargin = (int) (width * 0.06);
		layoutParams.topMargin = (int) (height * 0.08);
		imbtn_flash.setLayoutParams(layoutParams);
		// UI layout of return push-button
		layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.05),
				(int) (width * 0.05));
		layoutParams.leftMargin = (int) (width * 0.06);
		layoutParams.topMargin = (int) (height * 0.97) - (int) (width * 0.08);
		imbtn_camera_back.setLayoutParams(layoutParams);
		// UI layout of spot detection
		layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.08),
				(int) (width * 0.07));
		layoutParams.leftMargin = (int) (width * 0.05);
		layoutParams.topMargin = (int) (height * 0.57) - (int) (width * 0.08);
		imbtn_spot_dection.setLayoutParams(layoutParams);
		imbtn_spot_dection.setOnClickListener(this);
		// Layout of recognition rejection reminder
		layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		layoutParams.topMargin = (int) (height * 0.85);
		tv_reject_recog.setLayoutParams(layoutParams);
		if (isOpendetectLightspot) {
			imbtn_spot_dection
					.setBackgroundResource(R.drawable.spot_dection_on);
		} else {
			imbtn_spot_dection
					.setBackgroundResource(R.drawable.spot_dection_off);
		}

		int surfaceWidth = cameraParametersUtils.surfaceWidth;
		int surfaceHeight = cameraParametersUtils.surfaceHeight;
		if (width == surfaceView.getWidth() || surfaceView.getWidth() == 0) {

			layoutParams = new RelativeLayout.LayoutParams(width, height);
			surfaceView.setLayoutParams(layoutParams);

//			// UI layout of document type backgroud
//			layoutParams = new RelativeLayout.LayoutParams(
//					(int) (width * 0.65), (int) (width * 0.05));
//			layoutParams.leftMargin = (int) (width * 0.2);
//			layoutParams.topMargin = (int) (height * 0.46);
//			bg_camera_doctype.setLayoutParams(layoutParams);

			// layout of shooting button
			layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.1),
					(int) (width * 0.1));
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			layoutParams.leftMargin = (int) (width * 0.885);
			imbtn_takepic.setLayoutParams(layoutParams);
		}

		// layout of displaying shooting button
		layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.03),
				(int) (height * 0.4));
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		imbtn_eject.setLayoutParams(layoutParams);
		imbtn_eject.setBackgroundResource(R.drawable.locker_btn);
		if (surfaceWidth < width || surfaceHeight < height) {

			layoutParams = new RelativeLayout.LayoutParams(surfaceWidth,
					surfaceHeight);
			layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			surfaceView.setLayoutParams(layoutParams);

			layoutParams = new RelativeLayout.LayoutParams(
					(int) (width * 0.05), (int) (width * 0.05));
			layoutParams.leftMargin = (int) (width * 0.1);
			layoutParams.topMargin = (int) (height * 0.08);
			imbtn_flash.setLayoutParams(layoutParams);
			// UI layout of return push-button
			layoutParams = new RelativeLayout.LayoutParams(
					(int) (width * 0.05), (int) (width * 0.05));
			layoutParams.leftMargin = (int) (width * 0.1);
			layoutParams.topMargin = (int) (height * 0.92)
					- (int) (width * 0.04);
			imbtn_camera_back.setLayoutParams(layoutParams);
			//  layout of shooting button
			layoutParams = new RelativeLayout.LayoutParams((int) (width * 0.1),
					(int) (width * 0.1));
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			layoutParams.leftMargin = (int) (width * 0.885);
			imbtn_takepic.setLayoutParams(layoutParams);
		}

		if (screenInches >= 8) {
			tv_camera_doctype.setTextSize(25);
			tv_reject_recog.setTextSize(20);
		} else {
			tv_camera_doctype.setTextSize(20);
			tv_reject_recog.setTextSize(15);
		}
		if (nMainIDX == 3000) {
			//  It is still unclear about the classification of automatic MRZ recognition, the compulsive shooting functions of MRZ can be hidden temporarily.
			imbtn_eject.setVisibility(View.GONE);
		} else {
			imbtn_eject.setVisibility(View.VISIBLE);
		}
	}

	@SuppressLint("NewApi")
	@TargetApi(14)
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

		if (camera != null) {
			// setCameraParamters();
			try {
				if (isFirstGetSize) {
					// Avoid layout confusion in camera interface
					CameraActivity.this.runOnUiThread(updateUI);
				} else {
					msg = new Message();
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
			}
			Message msg = new Message();
			msg.what = 100;
			mAutoFocusHandler.sendMessage(msg);
		}


	}

	/**
	 * Setting camera paramater
	 */
	public void setCameraParamters() {
		try {
			if (null == camera) {
				camera = Camera.open();
			}

			WIDTH = cameraParametersUtils.preWidth;
			HEIGHT = cameraParametersUtils.preHeight;
			parameters = camera.getParameters();
			// if (parameters.getSupportedFocusModes().contains(
			// parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			// if (time != null) {
			// time.cancel();
			// time = null;
			// }
			// isFocusSuccess = true;
			// parameters
			// .setFocusMode(parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			// } else
//			if (parameters.getSupportedFocusModes().contains(
//					parameters.FOCUS_MODE_AUTO)) {
//				parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
//			}

			parameters.setPictureFormat(PixelFormat.JPEG);
			parameters.setExposureCompensation(0);
			if(WIDTH!=0&&HEIGHT!=0) {
				parameters.setPreviewSize(WIDTH/* 1920 */, HEIGHT/* 1080 */);
			}
			//System.out.println("WIDTH:" + WIDTH + "---HEIGHT：" + HEIGHT);
			try {
				camera.setPreviewDisplay(surfaceHolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (isOpenFlash && parameters.getSupportedFlashModes().contains(
					Camera.Parameters.FLASH_MODE_TORCH)) {
				parameters
						.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			}
			/**
			 * samsung s6 do not support FOCUS_MODE_MACRO
			 */
			/*if(parameters.getSupportedFocusModes().contains(parameters.FOCUS_MODE_MACRO)){
				parameters.setFocusMode(parameters.FOCUS_MODE_MACRO);
			}else*/
				if(parameters.getSupportedFocusModes().contains(parameters.FOCUS_MODE_AUTO))
			{
				parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
			}
			camera.setPreviewCallback(CameraActivity.this);
			camera.setParameters(parameters);
			camera.setDisplayOrientation(rotation);
			camera.startPreview();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		synchronized (this) {
			try {
				if (camera != null) {
					camera.setPreviewCallback(null);
					camera.stopPreview();
					camera.release();
					camera = null;
				}
			} catch (Exception e) {
				Log.i("TAG", e.getMessage());
			}
		}
	}

	public void closeCamera() {
		synchronized (this) {
			try {
				if (camera != null) {
					camera.setPreviewCallback(null);
					camera.stopPreview();
					camera.release();
					camera = null;
				}
			} catch (Exception e) {
				Log.i("TAG", e.getMessage());
			}
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mAutoFocusHandler.removeMessages(100);
		closeCamera();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		rotation = CameraParametersUtils.setRotation(width, height, uiRot,
				rotation);
		startCamera();
		/*
		Modifying MI Pad when it got stuck in the process ofpower button pressing to restart
		 */
		setCameraParamters();
		isSetCameraParamter = true;
		Message msg = new Message();
		rejectRecogHandler.sendMessage(msg);
	}

	/**
	 * change the camera paramater
	 */
	public void changeCameraParammter(int uiRot) {
		startCamera();
        //If we do not add the following codes,
        // the program will exit by Home button.
        // When re-enterring the program, it will become blank screen.
        // If the variables are not added, the transformation from landscape mode to portrait one wil be installed each time,
        // leading to bad user experience.
		if (isSetCameraParamter) {
			setCameraParamters();
		} else {
			//To prevent large rotation from 1 to 3, fuction surfaceChanged will not be executed, which will enable us not preview image in a rotaryway.
			try {
				camera.setDisplayOrientation(rotation);
			} catch (Exception e) {
			}
		}
		// end
	}

	/**
	 *  To solve that the MI Pad turns off screen for 15 seconds and gets stuck if restarting.
	 */
	private void startCamera() {
		// TODO Auto-generated method stub
		// Get camera object
		try {
			try {
				if (null == camera) {
					camera = Camera.open();
				}
			} catch (Exception e) {
				//  To prevent the layout confusion after forbidding using camera
				Toast.makeText(getApplicationContext(),
						getString(R.string.openCameraPermission),
						Toast.LENGTH_SHORT).show();
			}

			parameters = camera.getParameters();
			list = parameters.getSupportedPreviewSizes();
			cameraParametersUtils
					.getCameraPreParameters(camera, rotation, list);
		} catch (Exception e) {
			msg = new Message();
			msg.what = 100;
			handler.sendMessage(msg);
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		rejectRecogHandler.removeMessages(100);
		rejectRecogHandler.removeMessages(101);
		mAutoFocusHandler.removeMessages(100);
		if (resetIsTouchedhandler != null) {
			resetIsTouchedhandler.removeCallbacks(touchTimeOut);
			resetIsTouchedhandler = null;
		}
		if (recogBinder != null) {
			unbindService(recogConn);
			recogBinder = null;
		}
		closeCamera();

		super.onDestroy();
	}

	public void autoFocus() {

		if (camera != null) {
			try {
				if (camera.getParameters().getSupportedFocusModes() != null
						&& camera
						.getParameters()
						.getSupportedFocusModes()
						.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
					try {
						camera.autoFocus(null);
					} catch (Exception e) {
						e.printStackTrace();
					}
					mAutoFocusHandler.sendEmptyMessageDelayed(
							100, 2500);
				} else {
					Toast.makeText(getBaseContext(),
							getString(R.string.unsupport_auto_focus),
							Toast.LENGTH_LONG).show();

				}

			} catch (Exception e) {
				e.printStackTrace();
				//camera.stopPreview();
				//camera.startPreview();

			}

		}
	}

	// When the shutter is pressed, onShutter() calles back the shooting voice.
	private ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			if (tone == null)
				// Make voices to inform users
				tone = new ToneGenerator(1,// AudioManager.AUDIOFOCUS_REQUEST_GRANTED
						ToneGenerator.MIN_VOLUME);
			tone.startTone(ToneGenerator.TONE_PROP_BEEP);
		}
	};

	/* Calling back camera interface.  */
	private PictureCallback PictureCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			createPreviewPicture(data1, "Android_WintoneIDCard_" + name + "_full.jpg",
					PATH, size.width, size.height, 0, 0, size.width,
					size.height);
			getRecogResult();
		}
	};

	//  Monitor return button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			isTouched = true;
			sum = -1;
			mAutoFocusHandler.removeMessages(100);
			if (resetIsTouchedhandler != null) {
				resetIsTouchedhandler.removeCallbacks(touchTimeOut);
				resetIsTouchedhandler = null;
			}
			Intent intent = new Intent("kernal.idcard.MainActivity");
			//  to set up animation switch entering from right and exit from left
			// CameraActivity.this.finish();
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private int sum = 0;// To make sure thread synchronized

	@Override
	public void onPreviewFrame(final byte[] data, final Camera camera) {
		if (isTouched) {
			return;
		} else {
			uiRot = getWindowManager().getDefaultDisplay().getRotation();
			if (sum == 0) {

				data1 = data;
				MyThread thread = new MyThread();
				thread.start();

			}
		}
	}

	/**
	 * @author huangzhen core recognition thread
	 */
	class MyThread extends Thread {
		public MyThread() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			RecogOpera();
		}
	}

	public synchronized void RecogOpera() {
		sum = sum + 1;
		if (isTouched) {
			return;
		}
		if (uiRot != tempUiRot) {
			isTouched = true;
			Message mesg = new Message();
			handler.sendMessage(mesg);
			tempUiRot = uiRot;
		}
		if (isFirstGetSize) {
			isFirstGetSize = false;
			if(camera!=null&&camera.getParameters()!=null) {
				size = camera.getParameters().getPreviewSize();
			}
			if (nMainIDX == 3000) {
				RecogService.nMainID = 1034;
			} else {
				RecogService.nMainID = nMainIDX;
			}
			RecogService.isRecogByPath = false;
			recogIntent = new Intent(CameraActivity.this, RecogService.class);
			bindService(recogIntent, recogConn, Service.BIND_AUTO_CREATE);
			if (rotation == 90 || rotation == 270) {
				scale = ((float) cameraParametersUtils.surfaceWidth / size.height);
			} else {
				scale = ((float) cameraParametersUtils.surfaceWidth / size.width);
			}
			viewfinder_view.barWidth=(int)((width-cameraParametersUtils.surfaceWidth)*0.5);
			viewfinder_view.barHeight=(int)((height-cameraParametersUtils.surfaceHeight)*0.5);
		}
		if (isTakePicRecog && recogBinder != null) {
			RecogService.isRecogByPath = true;
			name = pictureName();
			picPathString1 = PATH + "Android_WintoneIDCard_" + name + "_full.jpg";
			picPathString = PATH + "Android_WintoneIDCard_" + name + "Cut.jpg";
			isTouched = true;
			mAutoFocusHandler.removeMessages(100);
			camera.takePicture(shutterCallback, null, PictureCallback);
			return;
		} else if (recogBinder != null && !isTakePicRecog) {
			if (nMainIDX != 3000) {
				// not MRZ recognition
				if (!isTakePic) {
					int CheckPicIsClear = -1;
					if (nMainIDX == 2 ||nMainIDX ==1013 || nMainIDX == 22 || nMainIDX == 1030
							|| nMainIDX == 1031 || nMainIDX == 1032
							|| nMainIDX == 1005 || nMainIDX == 1001
							|| nMainIDX == 2001 || nMainIDX == 2004
							|| nMainIDX == 2002 || nMainIDX == 2003
							|| nMainIDX == 14 || nMainIDX == 15
							|| nMainIDX == 25 || nMainIDX == 26 || nMainIDX == 2010 || nMainIDX == 2011) {

						if (rotation == 90 || rotation == 270) {
							// Portrait mode
							recogBinder
									.SetROI((int) (size.height * 0.025),
											(int) (size.width - 0.59375 * size.height) / 2,
											(int) (size.height * 0.975),
											(int) (size.width + 0.59375 * size.height) / 2);// Paramater reservation
							left = (int) (size.height * 0.025);
							top = (int) (size.width - 0.59375 * size.height) / 2;
							right = (int) (size.height * 0.975);
							bottom = (int) (size.width + 0.59375 * size.height) / 2;
						} else if (rotation == 0 || rotation == 180) {
							//  landscape mode
							left = (int) ((0.2 * width - ((width - cameraParametersUtils.surfaceWidth) * 0.5)) / scale);
							top = (int) ((((float) (height - 0.41004673 * width) / 2) - (height-cameraParametersUtils.surfaceHeight) * 0.5) / scale);
							right = (int) ((0.85 * width - ((width - cameraParametersUtils.surfaceWidth) * 0.5)) / scale);
							bottom = (int) ((((float) (height + 0.41004673 * width) / 2) - (height-cameraParametersUtils.surfaceHeight) * 0.5) / scale);
							// System.out.println("templeft:"+templeft+"--tempTop:"+tempTop+"--tempright:"+tempright+"--tempBottom:"+tempBottom);
							recogBinder.SetROI(left, top, right, bottom);// Paramater reservation
						}

					} else if (nMainIDX == 5 || nMainIDX == 6) {
						if (rotation == 90 || rotation == 270) {

							// Portrait mode
							recogBinder
									.SetROI((int) (size.height * 0.025),
											(int) (size.width - 0.64 * size.height) / 2,
											(int) (size.height * 0.975),
											(int) (size.width + 0.64 * size.height) / 2);// Paramater reservation
							left = (int) (size.height * 0.025);
							top = (int) (size.width - 0.64 * size.height) / 2;
							right = (int) (size.height * 0.975);
							bottom = (int) (size.width + 0.64 * size.height) / 2;
						} else if (rotation == 0 || rotation == 180) {
							// landscape mode
							left = (int) ((0.24 * width - ((width - cameraParametersUtils.surfaceWidth) * 0.5)) / scale);
							top = (int) ((((float) (height - 0.41004673 * width) / 2) - (height-cameraParametersUtils.surfaceHeight) * 0.5) / scale);
							right = (int) ((0.81 * width - ((width - cameraParametersUtils.surfaceWidth) * 0.5)) / scale);
							bottom = (int) ((((float) (height + 0.41004673 * width) / 2) - (height-cameraParametersUtils.surfaceHeight) * 0.5) / scale);
							recogBinder.SetROI(left, top, right, bottom);// Paramater reservation
						}
					} else {
						if (rotation == 90 || rotation == 270) {
							// Portrait mode
							recogBinder
									.SetROI((int) (size.height * 0.025),
											(int) (size.width - 0.659 * size.height) / 2,
											(int) (size.height * 0.975),
											(int) (size.width + 0.659 * size.height) / 2);// Paramater reservation
							left = (int) (size.height * 0.025);
							top = (int) (size.width - 0.659 * size.height) / 2;
							right = (int) (size.height * 0.975);
							bottom = (int) (size.width + 0.659 * size.height) / 2;

						} else if (rotation == 0 || rotation == 180) {
							// landscape mode
							left = (int) ((0.2 * width - ((width - cameraParametersUtils.surfaceWidth) * 0.5)) / scale);
							top = (int) ((((float) (height - 0.45 * width) / 2) - (height-cameraParametersUtils.surfaceHeight) * 0.5) / scale);
							right = (int) ((0.85 * width - ((width - cameraParametersUtils.surfaceWidth) * 0.5)) / scale);
							bottom = (int) ((((float) (height + 0.45 * width) / 2) - (height-cameraParametersUtils.surfaceHeight) * 0.5) / scale);
							recogBinder.SetROI(left, top, right, bottom);// Paramater reservation

						}
					}
					if (nCropType == 0) {
						if (rotation == 0) {
							recogBinder.SetRotateType(0);
						} else if (rotation == 180) {
							recogBinder.SetRotateType(2);
						} else if (rotation == 90) {
							recogBinder.SetRotateType(1);
						} else if (rotation == 270) {
							recogBinder.SetRotateType(3);
						}
					} else {
						/**
						 *  Intelligent inspection
						 */
						recogBinder.SetRotateType(0);
					}
					/**
					 * SetDetectIDCardType
					 * To set to differeciate intelligent edge detection and traditional edge detection.
					 * @param nCropType  “0” stands for traditional edge detection, “1” for intelligent edge detection.
					 * Put before “recogBinder.ConfirmSideLineEx(0)“ to invoke.
					 */
					recogBinder.SetVideoStreamCropTypeEx(nCropType);
					LoadBufferImage = recogBinder.LoadBufferImageEx(data1,
							size.width, size.height, 24, 0);
					/**
					 *To set up threshold value to test the image resolution
					 * the default value is 80
					 */
					if (nCropType == 1) {
						recogBinder.SetPixClearEx(40);
					}
					if (isOpendetectLightspot) {
						detectLightspot = recogBinder.DetectLightspot();
						if (detectLightspot == 0) {
							Message mesg = new Message();
							mesg.what = 1;
							detectLightspotHandler.sendMessage(mesg);
						}
					} else {
						detectLightspot = -2;
					}
					// 0 means 2nd generation of Identity Card of P.R., health insurance card, 1 for driving license, vehicle license; 2 for passport and etc.
//						 if (nMainIDX == 5 || nMainIDX == 6) {
//						 recogBinder.SetConfirmSideMethod(1);
//						 } else if (nMainIDX == 13 || nMainIDX ==
//						 9
//						 || nMainIDX == 10 || nMainIDX == 11
//						 || nMainIDX == 12) {
//						 recogBinder.SetConfirmSideMethod(2);
//						 recogBinder.IsDetectRegionValid(1);
//						 } else {
//						 if (nMainIDX == 3 || nMainIDX == 2) {
//					    recogBinder.SetConfirmSideMethod(0);
//						 recogBinder.IsDetectRegionValid(1);
//						 recogBinder.IsDetect180Rotate(1);
//					    recogBinder.SetDetectIDCardType(flag);
//						 } else {
//						 recogBinder.SetConfirmSideMethod(4);
//						 }
//						 }
					if (LoadBufferImage == 0) {
						if (detectLightspot != 0) {
							ConfirmSideSuccess = recogBinder
									.ConfirmSideLineEx(0);
								if(nCropType==1)
								{   recogBinder.GetFourSideLines(frame);
									if(rotation==0||rotation==180) {
										frame.ltStartX = (int) (frame.ltStartX * scale) + (int) ((width - cameraParametersUtils.surfaceWidth) * 0.5);
										frame.ltStartY = (int) (frame.ltStartY * scale) + (int) ((height - cameraParametersUtils.surfaceHeight) * 0.5);
										frame.lbStartX = (int) (frame.lbStartX * scale) + (int) ((width - cameraParametersUtils.surfaceWidth) * 0.5);
										frame.lbStartY = (int) (frame.lbStartY * scale) + (int) ((height - cameraParametersUtils.surfaceHeight) * 0.5);
										frame.rtStartX = (int) (frame.rtStartX * scale) + (int) ((width - cameraParametersUtils.surfaceWidth) * 0.5);
										frame.rtStartY = (int) (frame.rtStartY * scale) + (int) ((height - cameraParametersUtils.surfaceHeight) * 0.5);
										frame.rbStartX = (int) (frame.rbStartX * scale) + (int) ((width - cameraParametersUtils.surfaceWidth) * 0.5);
										frame.rbStartY = (int) (frame.rbStartY * scale) + (int) ((height - cameraParametersUtils.surfaceHeight) * 0.5);
									}else{
										frame.ltStartX = (int) (frame.ltStartX * scale) + (int) ((height - cameraParametersUtils.surfaceHeight) * 0.5);
										frame.ltStartY = (int) (frame.ltStartY * scale) +(int) ((width - cameraParametersUtils.surfaceWidth) * 0.5) ;
										frame.lbStartX = (int) (frame.lbStartX * scale) + (int) ((height - cameraParametersUtils.surfaceHeight) * 0.5);
										frame.lbStartY = (int) (frame.lbStartY * scale) + (int) ((width - cameraParametersUtils.surfaceWidth) * 0.5);
										frame.rtStartX = (int) (frame.rtStartX * scale) + (int) ((height - cameraParametersUtils.surfaceHeight) * 0.5);
										frame.rtStartY = (int) (frame.rtStartY * scale) +(int) ((width - cameraParametersUtils.surfaceWidth) * 0.5) ;
										frame.rbStartX = (int) (frame.rbStartX * scale) + (int) ((height - cameraParametersUtils.surfaceHeight) * 0.5);
										frame.rbStartY = (int) (frame.rbStartY * scale) + (int) ((width - cameraParametersUtils.surfaceWidth) * 0.5);
									}

								}
							if (ConfirmSideSuccess >= 0) {
								CheckPicIsClear = recogBinder
										.CheckPicIsClearEx();

							}
						}
					}
//					 System.out.println("ConfirmSideSuccess:"
//						 + ConfirmSideSuccess + "--"
//						 + "CheckPicIsClear:" + CheckPicIsClear+"---LoadBufferImage:"+LoadBufferImage);
//						 createPreviewPicture(data1,
//						 "test_ConfirmSideSuccess:"+ConfirmSideSuccess+"_CheckPicIsClear:"+CheckPicIsClear+"_"+pictureName()+".jpg",
//						 PATH, size.width, size.height, 0, 0,
//								 size.width, size.height);
					viewfinder_view.setFourLines(frame,null);
					if (LoadBufferImage == 0 && ConfirmSideSuccess > 0
							&& CheckPicIsClear == 0) {
						name = pictureName();
						picPathString = PATH + "Android_WintoneIDCard_" + name
								+ ".jpg";
						recogResultPath = PATH + "idcapture_" + name
								+ ".txt";
						HeadJpgPath = PATH + "head_" + name + ".jpg";
						// store the full picture start
						picPathString1 = PATH + "Android_WintoneIDCard_" + name
								+ "_full.jpg";
						// store the full picture end
						isTakePic = true;
						new FrameCapture(data1, WIDTH, HEIGHT, left, top,
								right, bottom, "11");
						mAutoFocusHandler.removeMessages(100);
						time1 = System.currentTimeMillis();
						RecogService.isRecogByPath = false;
						getRecogResult();
					}
				}

			} else {
				// MRZ recognition
				int returnType = 0;
				// data1 = data;
				regWidth = size.width;
				regHeight = size.height;
				if(width<height){
					left = (int) (0.05* size.height);
					right = (int) (size.height * 0.95);
					top = (int)(size.width*0.4);
					bottom = (int)(size.width*0.6);
				}else {
					left = (int) (0.15 * size.width);
					right = (int) (size.width * 0.85);
					top = size.height / 3;
					bottom = 2 * size.height / 3;
				}

				recogBinder.SetROI(left, top, right, bottom);
				if(width<height) {
					recogBinder.SetRotateType(1);
				}else {
					recogBinder.SetRotateType(0);
				}
				LoadBufferImage = recogBinder.LoadBufferImageEx(data1,
						size.width, size.height, 24, 0);
				int CheckPicIsClear = -1;
				if (LoadBufferImage == 0) {
					detectLightspot = -2;
					if (detectLightspot != 0) {
						ConfirmSideSuccess = recogBinder.ConfirmSideLineEx(0);
						System.out.println("ConfirmSideSuccess："
								+ ConfirmSideSuccess);
						if (ConfirmSideSuccess == 1034
								|| ConfirmSideSuccess == 1033
								|| ConfirmSideSuccess == 1036) {
							CheckPicIsClear = recogBinder.CheckPicIsClearEx();
							if (CheckPicIsClear == 0) {
								viewfinder_view.setCheckLeftFrame(1);
								viewfinder_view.setCheckTopFrame(1);
								viewfinder_view.setCheckRightFrame(1);
								viewfinder_view.setCheckBottomFrame(1);
							}
						}
					}
				}
				if ((ConfirmSideSuccess == 1034 || ConfirmSideSuccess == 1033 || ConfirmSideSuccess == 1036)
						&& CheckPicIsClear == 0) {
					// new FrameCapture(data1,WIDTH,HEIGHT,"11");
					// Message msg=new Message();
					// testHandler.sendMessage(msg);
					nMainIDX = ConfirmSideSuccess;
					name = pictureName();
					picPathString1 = PATH + "Android_WintoneIDCard_" + name
							+ "_full.jpg";
					saveFullPic(picPathString1);
					switch (ConfirmSideSuccess) {
						case 1034:
							if (!istakePic) {

								istakePic = true;
								time1 = System.currentTimeMillis();

								name = pictureName();
								picPathString = PATH + "Android_WintoneIDCard_" + name
										+ ".jpg";
								recogResultPath = PATH + "idcapture_" + name
										+ ".txt";
								HeadJpgPath = PATH + "head_" + name + ".jpg";
								// createPreviewPicture(data1,
								// "WintoneIDCard_" +
								// name
								// + ".jpg", PATH, regWidth, regHeight,
								// left,
								// top, right, bottom);

								getRecogResult();
								new FrameCapture(data1, regWidth, regHeight, left,
										top, right, bottom, "11");
							}
							break;
						case 1036:
							if (!istakePic) {

								istakePic = true;
								time1 = System.currentTimeMillis();

								name = pictureName();
								picPathString = PATH + "Android_WintoneIDCard_" + name
										+ ".jpg";
								recogResultPath = PATH + "idcapture_" + name
										+ ".txt";
								HeadJpgPath = PATH + "head_" + name + ".jpg";
								// createPreviewPicture(data1,
								// "WintoneIDCard_" +
								// name
								// + ".jpg", PATH, regWidth, regHeight,
								// left,
								// top, right, bottom);

								getRecogResult();
								new FrameCapture(data1, regWidth, regHeight, left,
										top, right, bottom, "11");
							}
							break;
						case 1033:
							if (!istakePic) {

								istakePic = true;
								time1 = System.currentTimeMillis();
								name = pictureName();
								picPathString = PATH + "Android_WintoneIDCard_" + name
										+ ".jpg";
								recogResultPath = PATH + "idcapture_" + name
										+ ".txt";
								HeadJpgPath = PATH + "head_" + name + ".jpg";
								// createPreviewPicture(data1,
								// "WintoneIDCard_" +
								// name
								// + ".jpg", PATH, regWidth, regHeight,
								// left,
								// top, right, bottom);

								getRecogResult();

								new FrameCapture(data1, regWidth, regHeight, left,
										top, right, bottom, "11");
							}
							break;
						default:
							break;
					}
				}

			}
		}
		sum = sum - 1;
	}

	private Handler testHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Toast.makeText(getApplicationContext(),
					"Document type:" + ConfirmSideSuccess, Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * store the full picture
	 *
	 * @param picPathString1
	 */
	private void saveFullPic(String picPathString1) {
		// TODO Auto-generated method stub
		//store the full picture start
		File file = new File(PATH);
		if (!file.exists())
			file.mkdirs();
		YuvImage yuvimage = new YuvImage(data1, Format, size.width,
				size.height, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		yuvimage.compressToJpeg(new Rect(0, 0, size.width, size.height),
				quality, baos);

		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(picPathString1);
			outStream.write(baos.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch
			// block
			e.printStackTrace();
		} finally {
			try {
				outStream.close();
				baos.close();
			} catch (Exception e) {
			}
		}
		//store the full picture end

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (getResources().getIdentifier("imbtn_camera_back", "id",
				this.getPackageName()) == v.getId()) {
			//  Return button clicking event
			isTouched = true;
			sum = -1;
			mAutoFocusHandler.removeMessages(100);
			if (resetIsTouchedhandler != null) {
				resetIsTouchedhandler.removeCallbacks(touchTimeOut);
				resetIsTouchedhandler = null;
			}

			Intent intent = new Intent("kernal.idcard.MainActivity");
			// to set up animation switch entering from right and exit from left
			// CameraActivity.this.finish();
			startActivity(intent);
		} else if (getResources().getIdentifier("imbtn_flash", "id",
				this.getPackageName()) == v.getId()) {
			// Strobe light clicking event
			try {
				if (camera == null)
					camera = Camera.open();
				parameters = camera.getParameters();
				//Meizu 4 device
				if ("MX4".equals(Build.MODEL)) {
					camera.stopPreview();
				}
				if (parameters.getSupportedFlashModes() != null
						&& parameters.getSupportedFlashModes().contains(
						Camera.Parameters.FLASH_MODE_TORCH)
						&& parameters.getSupportedFlashModes().contains(
						Camera.Parameters.FLASH_MODE_OFF)) {
					if (!isOpenFlash) {
						isOpenFlash = true;

						parameters
								.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
						if (parameters.getSupportedFocusModes().contains(
								parameters.FOCUS_MODE_AUTO)) {
							parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
						}
						parameters.setPictureFormat(PixelFormat.JPEG);
						parameters.setExposureCompensation(0);
						parameters.setPreviewSize(WIDTH, HEIGHT);
						try {
							camera.setPreviewDisplay(surfaceHolder);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						camera.setPreviewCallback(CameraActivity.this);
						camera.setParameters(parameters);
						imbtn_flash.setBackgroundResource(R.drawable.flash_off);
					} else {
						isOpenFlash = false;
						parameters
								.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
						if (parameters.getSupportedFocusModes().contains(
								parameters.FOCUS_MODE_AUTO)) {
							parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
						}
						parameters.setPictureFormat(PixelFormat.JPEG);
						parameters.setExposureCompensation(0);
						parameters.setPreviewSize(WIDTH, HEIGHT);
						try {
							camera.setPreviewDisplay(surfaceHolder);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						camera.setPreviewCallback(CameraActivity.this);
						camera.setParameters(parameters);
						imbtn_flash.setBackgroundResource(R.drawable.flash_on);
					}
				} else {
					Toast.makeText(getApplicationContext(),
							getString(R.string.unsupportflash),
							Toast.LENGTH_SHORT).show();
				}
				// Meizu 4 device
				if ("MX4".equals(Build.MODEL)) {
					camera.startPreview();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		} else if (getResources().getIdentifier("imbtn_takepic", "id",
				this.getPackageName()) == v.getId()) {
			// shooting buttion trigger event
			isTakePicRecog = true;
		} else if (getResources().getIdentifier("imbtn_eject", "id",
				this.getPackageName()) == v.getId()) {
			isTakePicRecogFrame = true;
			imbtn_takepic.setVisibility(View.VISIBLE);
			imbtn_eject.setVisibility(View.GONE);
		} else if (getResources().getIdentifier("imbtn_spot_dection", "id",
				this.getPackageName()) == v.getId()) {

			if (isOpendetectLightspot) {
				isOpendetectLightspot = false;
				imbtn_spot_dection
						.setBackgroundResource(R.drawable.spot_dection_off);
				Message mesg = new Message();
				mesg.what = 3;
				detectLightspotHandler.sendMessage(mesg);
			} else {
				isOpendetectLightspot = true;
				imbtn_spot_dection
						.setBackgroundResource(R.drawable.spot_dection_on);
				Message mesg = new Message();
				mesg.what = 2;
				detectLightspotHandler.sendMessage(mesg);
			}
		}

	}

	//Create files
	public void createFile(String path, String content, boolean iscreate) {
		if (iscreate) {
			System.out.println("path:" + path);
			File file = new File(path.substring(0, path.lastIndexOf("/")));
			if (!file.exists()) {
				file.mkdirs();
			}
			File newfile = new File(path);
			if (!newfile.exists()) {

				try {
					newfile.createNewFile();
					OutputStream out = new FileOutputStream(path);
					byte[] buffer = content.toString().getBytes();
					out.write(buffer, 0, buffer.length);
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				newfile.delete();
				try {
					newfile.createNewFile();
					OutputStream out = new FileOutputStream(path);
					byte[] buffer = content.toString().getBytes();
					out.write(buffer, 0, buffer.length);
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		;
	}

	private ArrayList<Size> splitSize(String str, Camera camera) {
		if (str == null)
			return null;
		StringTokenizer tokenizer = new StringTokenizer(str, ",");
		ArrayList<Size> sizeList = new ArrayList<Size>();
		while (tokenizer.hasMoreElements()) {
			Size size = strToSize(tokenizer.nextToken(), camera);
			if (size != null){
				sizeList.add(size);
			}

		}
		if (sizeList.size() == 0)
			return null;
		return sizeList;
	}

	private Size strToSize(String str, Camera camera) {
		if (str == null)
			return null;
		int pos = str.indexOf('x');
		if (pos != -1) {
			String width = str.substring(0, pos);
			String height = str.substring(pos + 1);
			return camera.new Size(Integer.parseInt(width),
					Integer.parseInt(height));
		}
		return null;
	}

	public void createPreviewPicture(byte[] reconData, String pictureName,
									 String path, int preWidth, int preHeight, int left, int top,
									 int right, int bottom) {
		if ("Nexus 5X".equals(Build.MODEL)) {
			reconData = rotateYUV420Degree180(reconData, preWidth, preHeight);
		}
		File file = new File(path);
		if (!file.exists())
			file.mkdirs();
		// if there are documents, we can save nv 21 group as JPG huangzhen
		YuvImage yuvimage = new YuvImage(reconData, Format, preWidth,
				preHeight, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(new Rect(left, top, right, bottom), quality,
				baos);
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(path + pictureName);
			outStream.write(baos.toByteArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				outStream.close();
				baos.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		//  if there are documents, we can save nv 21 group as JPG huangzhen
	}

	private static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
		byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
		int i = 0;
		int count = 0;
		for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
			yuv[count] = data[i];
			count++;
		}
		i = imageWidth * imageHeight * 3 / 2 - 1;
		for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
				* imageHeight; i -= 2) {
			yuv[count++] = data[i - 1];
			yuv[count++] = data[i];
		}
		return yuv;
	}

	/**
	 * @Title: pictureName @Description: 将文件命名 @param @return 设定文件 @return
	 * String 文件以时间命的名字 @throws
	 */
	public String pictureName() {
		String str = "";
		Time t = new Time();
		t.setToNow(); //  Get system time。
		int year = t.year;
		int month = t.month + 1;
		int date = t.monthDay;
		int hour = t.hour; // 0-23
		int minute = t.minute;
		int second = t.second;
		if (month < 10)
			str = String.valueOf(year) + "0" + String.valueOf(month);
		else {
			str = String.valueOf(year) + String.valueOf(month);
		}
		if (date < 10) {
			str = str + "0" + String.valueOf(date);
		}
		else {
			str = str + String.valueOf(date);
		}
		if (hour < 10)
			str = str + "0" + String.valueOf(hour);
		else {
			str = str + String.valueOf(hour);
		}
		if (minute < 10)
			str = str + "0" + String.valueOf(minute);
		else {
			str = str + String.valueOf(minute);
		}
		if (second < 10)
			str = str + "0" + String.valueOf(second);
		else {
			str = str + String.valueOf(second);
		}
		return str;
	}

	public void getRecogResult() {
		RecogParameterMessage rpm = new RecogParameterMessage();
		rpm.nTypeLoadImageToMemory = 0;
		rpm.nMainID = nMainIDX;
		rpm.nSubID[0] = nSubID;
		rpm.GetSubID = true;
		rpm.GetVersionInfo = true;
		rpm.logo = "";
		rpm.userdata = "";
		rpm.sn = "";
		rpm.authfile = "";
		rpm.isSaveCut = true;
		rpm.triggertype = 0;
		rpm.devcode = Devcode.devcode;
		rpm.isOnlyClassIDCard = true;
		rpm.Scale=1;
		rpm.isOpenGetThaiFeatureFuction=true;
		//rpm.isSetIDCardRejectType=false;
		// rpm.idcardRotateDegree=3;
		if (nMainIDX == 2) {
			rpm.isAutoClassify = true;
			rpm.nv21bytes = data1;
			rpm.nv21_width = WIDTH;
			rpm.nv21_height = HEIGHT;
			rpm.lpHeadFileName = "";//  save document protrait
			rpm.lpFileName = picPathString1; //  If rpm.lpFileName is null, automatic recognition fuction will be executed
			rpm.cutSavePath = picPathString;
		} else {
			rpm.nv21bytes = data1;
			rpm.nv21_width = WIDTH;
			rpm.nv21_height = HEIGHT;
			rpm.lpHeadFileName = HeadJpgPath;// save document protrait
			rpm.lpFileName = picPathString1; //  If rpm.lpFileName is null, automatic recognition fuction will be executed.
			rpm.cutSavePath = picPathString;
		}
		if (isTakePicRecog) {
			rpm.isCut = true;
			rpm.nProcessType = 7;
			rpm.nSetType = 1;
			//rpm.cutSavePath = picPathString1;
		} else {
			rpm.isCut = false;
		}

		// end
		try {
			// camera.stopPreview();
			resultMessage = recogBinder.getRecogResult(rpm);
			if ((!isTakePicRecog) && resultMessage.ReturnRecogIDCard != -6) {
				/**
				 * When recognition is rejected, a full picture should not be saved.
				 */
				saveFullPic(picPathString1);
			}
			if (resultMessage.ReturnAuthority == 0
					&& resultMessage.ReturnInitIDCard == 0
					&& resultMessage.ReturnLoadImageToMemory == 0
					&& resultMessage.ReturnRecogIDCard > 0) {
				//System.out.println("是否是複印件:"+resultMessage.IsIDCopy);
				CameraActivity.this.runOnUiThread(updateRejectRecog);
				String iDResultString = "";
				String[] GetFieldName = resultMessage.GetFieldName;
				String[] GetRecogResult = resultMessage.GetRecogResult;
				//  function which can get the location and coordination of field
				// List<int[]>listdata=
				// resultMessage.textNamePosition;
				istakePic = false;
				for (int i = 1; i < GetFieldName.length; i++) {
					if (GetRecogResult[i] != null) {
						if (!recogResultString.equals("")) {
							recogResultString = recogResultString
									+ GetFieldName[i] + ":" + GetRecogResult[i]
									+ ",";
						}
						else {
							recogResultString = GetFieldName[i] + ":"
									+ GetRecogResult[i] + ",";
						}
					}
				}
				recogResultString=recogResultString+"是否是复印件:"+resultMessage.IsIDCopy;
				if(nMainIDX==2011) {
					recogResultString=recogResultString+"\n"+"特征点位置:";
					for (int i = 0; i < 6; i++) {
						if (i == 5) {
							recogResultString = recogResultString + resultMessage.xpos[i] + ":" + resultMessage.ypos[i];
						} else {
							recogResultString = recogResultString + resultMessage.xpos[i] + ":" + resultMessage.ypos[i] + "---";
						}

					}
				}
				// camera.setPreviewCallback(null);
				mVibrator = (Vibrator) getApplication().getSystemService(
						Service.VIBRATOR_SERVICE);
				mVibrator.vibrate(200);
				Intent intent = new Intent("kernal.idcard.ShowResultActivity");
				intent.putExtra("recogResult", recogResultString);
				if (devcode != null) {
					intent.putExtra("devcode", devcode);
				}
				intent.putExtra("fullPagePath", picPathString1);
				intent.putExtra("cutPagePath", picPathString);
				intent.putExtra("nCropType", nCropType);
				intent.putExtra("VehicleLicenseflag", VehicleLicenseflag);
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
				if (resultMessage.ReturnRecogIDCard != -6 || isTakePicRecog) {

					if(!new File(picPathString1).exists()) {
						//To prevent clicking and taking photos, the code is executed,
						// but it is not executed until the picture code is identified
						// to store the picture code (synchronization problem)
						saveFullPic(picPathString1);
						CameraActivity.this.runOnUiThread(updateRejectRecog);
						Intent intent = new Intent("kernal.idcard.ShowResultActivity");
						intent.putExtra("exception", string);
						intent.putExtra("nCropType", nCropType);
						intent.putExtra("fullPagePath", picPathString1);
						intent.putExtra("VehicleLicenseflag", VehicleLicenseflag);
						startActivity(intent);
					}else{
						CameraActivity.this.runOnUiThread(updateRejectRecog);
						Intent intent = new Intent("kernal.idcard.ShowResultActivity");
						intent.putExtra("exception", string);
						intent.putExtra("nCropType", nCropType);
						intent.putExtra("fullPagePath", picPathString1);
						intent.putExtra("VehicleLicenseflag", VehicleLicenseflag);
						startActivity(intent);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
//			Looper.prepare();
//			Toast.makeText(getApplicationContext(),
//					getString(R.string.recognized_failed), Toast.LENGTH_SHORT)
//					.show();
//			Looper.loop();

		} finally {
			if (resultMessage.ReturnRecogIDCard == -6) {
				isTakePic = false;
				ConfirmSideSuccess = -1;
				LoadBufferImage = -1;
				mAutoFocusHandler.sendEmptyMessageDelayed(
						100, 0);
				if (isTakePicRecog) {
					isTakePicRecog = false;
					if (recogBinder != null) {
						unbindService(recogConn);
						recogBinder = null;
					}
				}

			} else {
				if (recogBinder != null) {
					unbindService(recogConn);
					recogBinder = null;
				}
			}
		}

	}

	/**
	 * The disappearing time for custom Toast
	 *
	 * @param toast
	 * @param cnt
	 */
	public static void showMyToast(final Toast toast, final int cnt) {
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				toast.show();
			}
		}, 0);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				toast.cancel();
				timer.cancel();
			}
		}, cnt);
	}

}
