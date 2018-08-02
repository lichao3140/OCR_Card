package com.kernal.passport.sdk.utils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
/**
 *
 *Instruments to insepct permissions.
 */
public class CheckPermission {
	private final Context context;
	//Constructor
	public CheckPermission(Context context) {
		this.context = context.getApplicationContext();
	}
	//When inspecting permissions, system permission set can be judged.
	public boolean permissionSet(String... permissions) {
		for (String permission : permissions) {
			if (isLackPermission(permission)) {//To see if all permission sets can be added.
				return true;
			}
		}
		return false;
	}
	//To insepct the system perssion is to judge if the current situation has enough permission( PERMISSION_DENIED:the permission is sufficient or not).
	private boolean isLackPermission(String permission) {
		return context.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED;
	}
}
