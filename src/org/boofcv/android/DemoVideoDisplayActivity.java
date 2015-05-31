package org.boofcv.android;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

//import boofcv.android.gui.VideoDisplayActivity;

/**
 * Activity for displaying video results.
 *
 * @author Peter Abeles
 */
public class DemoVideoDisplayActivity extends VideoDisplayActivity {

	private static final String TAG = "DemoVideoDisplayActivity";

	public static DemoPreference preference;

	public DemoVideoDisplayActivity() {
	}

	public DemoVideoDisplayActivity(boolean hidePreview) {
		super(hidePreview);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		preference = DemoMain.preference;
		setShowFPS(preference.showFps);
	}

	@Override
	protected Camera openConfigureCamera(Camera.CameraInfo info) {

		getViewRotation();

		Camera mCamera = Camera.open(preference.cameraId);
		Camera.getCameraInfo(preference.cameraId, info);

		Camera.Parameters param = mCamera.getParameters();
		Camera.Size sizePreview = param.getSupportedPreviewSizes().get(preference.preview);
		param.setPreviewSize(sizePreview.width, sizePreview.height);

		Camera.Size sizePicture = param.getSupportedPictureSizes().get(preference.picture);
		param.setPictureSize(sizePicture.width, sizePicture.height);

		mCamera.setParameters(param);

		return mCamera;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		Log.i(TAG, "onConfigurationChanged");

		int rot = (((WindowManager)
				getSystemService(this.WINDOW_SERVICE)).getDefaultDisplay()).getRotation();

		Log.i(TAG, "rotation: " + rot);
	}



}