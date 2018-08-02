package com.kernal.passport.sdk.utils;
import com.kernal.passportreader.sdk.CameraActivity;
import com.kernal.passportreader.sdk.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Created by huangzhen on 2016/5/18. The page for users to get license, licenses processing
 */
public class PermissionActivity extends Activity {
	public static final int PERMISSION_GRANTED = 0;//  Identity permission licensing.
	public static final int PERMISSION_DENIEG = 1;// When there is insufficent licensing, the license will be rejected.
	private static final int PERMISSION_REQUEST_CODE = 0;// There will be a result paramater when system licensing manages pages.
	private static final String EXTRA_PERMISSION = "com.kernal.permissiondemo";// licensing paramater
	private static final String PACKAGE_URL_SCHEME = "package:";//  licensing proposal
	private CheckPermission checkPermission;//  licensing inspector to inspect licenses
	private boolean isrequestCheck;//  To judge if system licensing inspction is needed to prevent it from overlapping with system prompt frame.
	private static int is_nMainId;
	private static String is_devcode;
	private static int is_flag;
	private static int VehicleLicenseflag;
	private static Handler  sortOperaHandler;
	private static  int is_nCropType;
	// Open interface to start current licensing page
	public static void startActivityForResult(Activity activity,Handler  sortOperaHandler1,
											  int requestCode, int nMainId, String devcode, int flag,
											  int VehicleLicenseflag,int nCropType, String... permission) {
		is_nMainId = nMainId;
		is_devcode = devcode;
		is_flag = flag;
		sortOperaHandler=sortOperaHandler1;
		is_nCropType=nCropType;
		VehicleLicenseflag = VehicleLicenseflag;
		Intent intent = new Intent(activity, PermissionActivity.class);
		intent.putExtra(EXTRA_PERMISSION, permission);
		intent.putExtra("nMainId", nMainId);
		intent.putExtra("devcode", devcode);
		intent.putExtra("nCropType", nCropType);
		intent.putExtra("VehicleLicenseflag", VehicleLicenseflag);
		intent.putExtra("flag", 0);
		//ActivityCompat.startActivityForResult(activity, intent, requestCode,
		//		null);
		if (Build.VERSION.SDK_INT >= 16) {
			activity.startActivityForResult(intent, requestCode, null);
		} else
			activity.startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.permission_layout);
		if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSION))// If paramater is not equal to installed licensing paramaters.
		{
			throw new RuntimeException(
					"The current Activity needs static StartActivityForResult to start up");// Abnormality prompt.
		}
		checkPermission = new CheckPermission(this);
		isrequestCheck = true;// change inspection status
	}

	// Ask for user licensing after inspection
	@Override
	protected void onResume() {
		super.onResume();
		if (isrequestCheck) {
			String[] permission = getPermissions();
			if (checkPermission.permissionSet(permission)) {
				requestPermissions(permission); // 去请求权限
			} else {
				allPermissionGranted();//  Get all the licensing
			}
		} else {
			isrequestCheck = true;
		}
	}

	//  Get all the licensing
	private void allPermissionGranted() {
		setResult(PERMISSION_GRANTED);
//		Intent intent = new Intent(this, CameraActivity.class);
//		intent.putExtra("nMainId", is_nMainId);
//		intent.putExtra("devcode", is_devcode);
//		intent.putExtra("flag", is_flag);
//		intent.putExtra("VehicleLicenseflag", VehicleLicenseflag);
//		startActivity(intent);
		Message message=new Message();
		message.what=is_nCropType;
		sortOperaHandler.sendMessage(message);
		finish();
	}

	//   Ask for permission to be compatible with versions.
	private void requestPermissions(String... permission) {
		PermissionActivity.this.requestPermissions(permission,
				PERMISSION_REQUEST_CODE);
	}

	//  To Return the delivered permission paramater
	private String[] getPermissions() {
		return getIntent().getStringArrayExtra(EXTRA_PERMISSION);
	}

	/**
	 *  It is used for permission management. If all the licenses are issued, the user can pass directly.
	 *  If the permission is partially missing, dialog prompt will be used.
	 *
	 * @param requestCode
	 *           Ask for codes
	 * @param permissions
	 *            Permission paramater
	 * @param grantResults
	 *            Result
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (PERMISSION_REQUEST_CODE == requestCode
				&& hasAllPermissionGranted(grantResults)) //  To judge if the request code is consisent with request result.
		{
			isrequestCheck = true;// The licenses can be inspected to pass directly. Otherwise, it can be set up by alert dialog.
			allPermissionGranted(); // Enter
		} else { //  Setting up alert dialog
			isrequestCheck = false;
			// showMissingPermissionDialog();//dialog
			Toast.makeText(this, getString(R.string.openPermission), Toast.LENGTH_SHORT).show();
			finish();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sortOperaHandler=null;
	}

	//  Get all licenisng
	private boolean hasAllPermissionGranted(int[] grantResults) {
		for (int grantResult : grantResults) {
			if (grantResult == PackageManager.PERMISSION_DENIED) {
				return false;
			}
		}
		return true;
	}

	// open up ACTION_APPLICATION_DETAILS_SETTINGS
	private void startAppSettings() {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
		startActivity(intent);
	}

}
