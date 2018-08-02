package com.kernal.passport.sdk.utils;

import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class CameraParametersUtils {
	Camera.Parameters parameters;
	public int srcWidth, srcHeight;
	public int preWidth, preHeight;
	public int picWidth, picHeight;
	public int surfaceWidth, surfaceHeight;
	List<Size> list;
	private boolean isShowBorder = false;
	private Context context;
	public CameraParametersUtils(Context context) {
		this.context = context;
		setScreenSize(context);
	}

	/**
	 *  Get the width and height of device shooting resolution
	 * 
	 * @param camera
	 */
	public void getCameraPicParameters(Camera camera) {
		isShowBorder = false;
		parameters = camera.getParameters();
		list = parameters.getSupportedPictureSizes();
		float ratioScreen = (float) srcWidth / srcHeight;
		for (int i = 0; i < list.size(); i++) {
			float ratioPicture = (float) list.get(i).width / list.get(i).height;
			if (ratioScreen == ratioPicture) {// To judge if the aspect ratio is the same with shooting aspect ratio. If they are the same, the follwing codes need to be exectuted.
				if (list.get(i).width >= 1600 || list.get(i).height >= 1200) {// The default preview is standardized by 1600*1200.
					if (picWidth == 0 && picHeight == 0) {// init value
						picWidth = list.get(i).width;
						picHeight = list.get(i).height;
					}
					if (list.get(0).width > list.get(list.size() - 1).width) {
						// If the first value is greater than the last one.
						if (picWidth > list.get(i).width
								|| picHeight > list.get(i).height) {
							//  If there are resolutions greater than 1600*1200 and less than the above recorded one, we will use the middle one.
							picWidth = list.get(i).width;
							picHeight = list.get(i).height;
						}
					} else {
						//  If the first value is less than the last one.
						if (picWidth < list.get(i).width
								|| picHeight < list.get(i).height) {
							// If the above mentioned wideth and height is greater than or equal to 1600*1200, there will be no more selection.
							if (picWidth >= 1600 || picHeight >= 1200) {

							} else {
								// To find appropriate resolution, if picWidth and picHeight is not greater than 1600*1200, the selection can continue.
								picWidth = list.get(i).width;
								picHeight = list.get(i).height;
							}
						}
					}
				}
			}
		}
		//  It means that the suitable resolution is not found.
		if (picWidth == 0 || picHeight == 0) {
			isShowBorder = true;
			picWidth = list.get(0).width;
			picHeight = list.get(0).height;
			for (int i = 0; i < list.size(); i++) {

				if (list.get(0).width > list.get(list.size() - 1).width) {
					// If the first value is greater than the last one.
					if (picWidth >= list.get(i).width
							|| picHeight >= list.get(i).height) {
						//  If the resolution width or height in last shooting is greater than width and height in this time, the following codes can be executed.
						if (list.get(i).width >= 1600) {
							//  If the preview width and height this time is greater than 1280*720,the following codes can be executed.
							picWidth = list.get(i).width;
							picHeight = list.get(i).height;

						}
					}
				} else {
					if (picWidth <= list.get(i).width
							|| picHeight <= list.get(i).height) {
						if (picWidth >= 1600 || picHeight >= 1200) {

						} else {
							//  The width or height in last preview resoution is greater than wideth and height this time, the following codes can be executed.
							if (list.get(i).width >= 1600) {
								// the preview width and height this time is greter than 1280*720, the following codes can be executed.
								picWidth = list.get(i).width;
								picHeight = list.get(i).height;

							}
						}

					}
				}
			}
		}
		// If we do not find a resolution greater than 1280*720, the maximum value in the sets can be used.
		if (picWidth == 0 || picHeight == 0) {
			isShowBorder = true;
			if (list.get(0).width > list.get(list.size() - 1).width) {
				picWidth = list.get(0).width;
				picHeight = list.get(0).height;
			} else {
				picWidth = list.get(list.size() - 1).width;
				picHeight = list.get(list.size() - 1).height;
			}
		}
		if (isShowBorder) {
			if (ratioScreen > (float) picWidth / picHeight) {
				float rp = ratioScreen - ((float) preWidth / preHeight);
				// If the error is between 0.02, it can be ignored.
				if (rp <= 0.02) {
					surfaceWidth = srcWidth;
					surfaceHeight = srcHeight;
				} else {
					surfaceWidth = (int) (((float) preWidth / preHeight) * srcHeight);
					surfaceHeight = srcHeight;
				}
			} else {
				surfaceWidth = srcWidth;
				surfaceHeight = (int) (((float) picWidth / picHeight) * srcHeight);
			}
		} else {
			surfaceWidth = srcWidth;
			surfaceHeight = srcHeight;
		}
		System.out.println("surfaceWidth:" + surfaceWidth + "--surfaceHeight:"
				+ surfaceHeight);
	}

	/**
	 * Width and height to get the device preview resolution
	 * 
	 * @param camera
	 */
	public void getCameraPreParameters(Camera camera, int rotation,List<Size> list)

	{
		isShowBorder = false;
		// Honor 7
		if ("PLK-TL01H".equals(Build.MODEL)) {
			preWidth = 1920;
			preHeight = 1080;
			surfaceWidth = srcWidth;
			surfaceHeight = srcHeight;
			return;
		}
		preWidth = 0;
		preHeight = 0;
		// other devices
		float ratioScreen = 0;
		if (rotation == 0 || rotation == 180) {
			ratioScreen = (float) srcWidth / srcHeight;
		} else if (rotation == 90 || rotation == 270) {
			ratioScreen = (float) srcHeight / srcWidth;
		}
		System.out.println("srcWidth:"+srcWidth+"--srcHeight:"+srcHeight);
		for (int i = 0; i < list.size(); i++) {
			float ratioPreview = (float) list.get(i).width / list.get(i).height;
			if (ratioScreen == ratioPreview) {// To judge if the aspect ratio of a screen is the same with preview aspect ratio. If they are the same, execute the following codes.
				if (list.get(i).width >= 1280 || list.get(i).height >= 720) {// The default preview is standardlized by 1280*720.
					if (preWidth == 0 && preHeight == 0) {//  Initialization value.
						preWidth = list.get(i).width;
						preHeight = list.get(i).height;
					}
					if (list.get(0).width > list.get(list.size() - 1).width) {
						//  If the first value is greater than the last one.
						if (preWidth > list.get(i).width
								|| preHeight > list.get(i).height) {
							// If there are resolutions greater than 1280*720 and less than the above recorded one, we will use the middle one.
							preWidth = list.get(i).width;
							preHeight = list.get(i).height;
						}
					} else {
						//  If the first value is greater than the last one.
						if (preWidth < list.get(i).width
								|| preHeight < list.get(i).height) {
							//If the above mentioned wideth and height is greater than or equal to 1280*720, there will be no more selection.
							if (preWidth >= 1280 || preHeight >= 720) {

							} else {
								//To find an appropriate resolution, if picWidth and picHeight is not greater than 1280*720, the selection can continue.
								preWidth = list.get(i).width;
								preHeight = list.get(i).height;
							}
						}
					}
				}
			}
		}
		//  It demonstrates that we did not find the suitable resolution for the program.
		if (preWidth == 0 || preHeight == 0) {
			isShowBorder = true;
			preWidth = list.get(0).width;
			preHeight = list.get(0).height;
			for (int i = 0; i < list.size(); i++) {

				if (list.get(0).width > list.get(list.size() - 1).width) {
					//  If the first value is greater than the last one.
					if (preWidth >= list.get(i).width
							|| preHeight >= list.get(i).height) {
						// The width or height in last preview resoution is greater than wideth and height this time, the following codes can be executed.
						if (list.get(i).width >= 1280) {
							//  The preview width and height this time is greter than 1280*720, the following codes can be executed.
							preWidth = list.get(i).width;
							preHeight = list.get(i).height;

						}
					}
				} else {
					if (preWidth <= list.get(i).width
							|| preHeight <= list.get(i).height) {
						if (preWidth >= 1280 || preHeight >= 720) {

						} else {
							// The width or height in last preview resoution is greater than wideth and height this time, the following codes can be executed.
							if (list.get(i).width >= 1280) {
								// The preview width and height this time is greter than 1280*720, the following codes can be executed.
								preWidth = list.get(i).width;
								preHeight = list.get(i).height;

							}
						}

					}
				}
			}
		}

		//  If we do not find a resolution greater than 1280*720, the maximum value in the sets can be used.
		if (preWidth <= 640 || preHeight <= 480) {
			isShowBorder = true;
			if (list.get(0).width > list.get(list.size() - 1).width) {
				preWidth = list.get(0).width;
				preHeight = list.get(0).height;
			} else {
				preWidth = list.get(list.size() - 1).width;
				preHeight = list.get(list.size() - 1).height;
			}
		}
		if (isShowBorder) {
			if (ratioScreen > (float) preWidth / preHeight) {
				if (rotation == 0 || rotation == 180) {
					surfaceWidth = (int) (((float) preWidth / preHeight) * srcHeight);
					surfaceHeight = srcHeight;
					if(surfaceWidth>srcWidth){
						surfaceWidth = srcWidth;
						surfaceHeight = (int) (((float) preHeight /preWidth ) * srcWidth);
					}
				} else if (rotation == 90 || rotation == 270) {
					surfaceWidth = (int) (((float) preHeight / preWidth) * srcHeight);
					surfaceHeight = srcHeight;
					if(surfaceWidth>srcWidth){
						surfaceWidth = srcWidth;
						surfaceHeight = (int) (((float)  preWidth/preHeight ) * srcWidth);
					}
				}

			} else {
				if (rotation == 0 || rotation == 180) {
					surfaceWidth = srcWidth;
					surfaceHeight = (int) (((float) preHeight / preWidth) * srcWidth);
					if(surfaceHeight>srcHeight){
						surfaceWidth = (int) (((float)  preWidth/preHeight ) * srcHeight);
						surfaceHeight = srcHeight;
					}
				} else if (rotation == 90 || rotation == 270) {
					surfaceWidth = srcWidth;
					surfaceHeight = (int) (((float) preWidth / preHeight) * srcWidth);
					if(surfaceHeight>srcHeight){
						surfaceWidth = (int) (((float)  preHeight/preWidth ) * srcHeight);
						surfaceHeight = srcHeight;
					}
				}

			}
		} else {
			surfaceWidth = srcWidth;
			surfaceHeight = srcHeight;
		}

//		System.out.println("surfaceWidth1:" + surfaceWidth
//				+ "--surfaceHeight1:" + surfaceHeight+"---"+preWidth+"*"+preHeight);
	}

	@SuppressLint("NewApi")
	public void setScreenSize(Context context) {
		int x, y;
		WindowManager wm = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Point screenSize = new Point();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				display.getRealSize(screenSize);
				x = screenSize.x;
				y = screenSize.y;
			} else {
				display.getSize(screenSize);
				x = screenSize.x;
				y = screenSize.y;
			}
		} else {
			x = display.getWidth();
			y = display.getHeight();
		}

		srcWidth = x;
		srcHeight = y;
	}

	/**
	 * @param mDecorView
	 *            {tags} Format the documents.
	 * @return ${return_type} Return types
	 * @throws
	 * @Title: Immersive mode.
	 * @Description: Hiding virtual buttons
	 */
	@TargetApi(19)
	public void hiddenVirtualButtons(View mDecorView) {
		if (Build.VERSION.SDK_INT >= 19) {
			mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE);
		}
	}

	public static int setRotation(int width, int height, int uiRot, int rotation) {
		if (width >= height) {
			if (uiRot == 1 || uiRot == 3) {
				switch (uiRot) {
				case 1:
					rotation = 0;
                    //Preview rotation degree for special devicesf
					if("Nexus 5X".equals(Build.MODEL)){
						rotation=180;
					}
					break;
				case 3:
					rotation = 180;
                    //Preview rotation degree for special devicesf
					if("Nexus 5X".equals(Build.MODEL)){
						rotation=0;
					}
					break;
				}

			} else {
				switch (uiRot) {
				case 0:
					rotation = 0;
                    //Preview rotation degree for special devicesf
					if("Nexus 5X".equals(Build.MODEL)){
						rotation=180;
					}
					break;

				case 2:
					rotation = 180;
                    //Preview rotation degree for special devicesf
					if("Nexus 5X".equals(Build.MODEL)){
						rotation=0;
					}
					break;

				}
			}
		} else if (height >= width) {
			if (uiRot == 0 || uiRot == 2) {

				switch (uiRot) {
				case 0:
					rotation = 90;
                    //Preview rotation degree for special devicesf
					if("Nexus 5X".equals(Build.MODEL)){
						rotation=270;
					}
					break;

				case 2:
					rotation = 270;
                    //Preview rotation degree for special devicesf
					if("Nexus 5X".equals(Build.MODEL)){
						rotation=90;
					}
					break;

				}
			} else {
				switch (uiRot) {
				case 1:
					rotation = 270;
					//Preview rotation degree for special devicesf
					if("Nexus 5X".equals(Build.MODEL)){
						rotation=90;
					}
					break;
				case 3:
 					rotation = 90;
                    //Preview rotation degree for special devicesf
					if("Nexus 5X".equals(Build.MODEL)){
						rotation=270;
					}
					break;
				}
			}
		}
		return rotation;
	}
}
