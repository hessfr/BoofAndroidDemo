package org.boofcv.android;

import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
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

		preference = DemoMain.preference;
		setShowFPS(preference.showFps);
	}

	@Override
	protected Camera openConfigureCamera(Camera.CameraInfo info) {

		Log.i(TAG, "openConfigureCamera");
		getViewRotation();

		Camera mCamera = Camera.open(preference.cameraId);
		Camera.getCameraInfo(preference.cameraId, info);

		Camera.Parameters param = mCamera.getParameters();
		Camera.Size sizePreview = param.getSupportedPreviewSizes().get(preference.preview);
		param.setPreviewSize(sizePreview.width, sizePreview.height);

		Camera.Size sizePicture = param.getSupportedPictureSizes().get(preference.picture);
		param.setPictureSize(sizePicture.width, sizePicture.height);

		mCamera.setParameters(param);

		// Rotate the views depending on the orientation of the screen:
		int rot = (((WindowManager)
				getSystemService(this.WINDOW_SERVICE)).getDefaultDisplay()).getRotation();

		if (rot == Surface.ROTATION_0) {
			rotateView(90f);
		} else if (rot == Surface.ROTATION_90) {
			// default, don't do anything
//		} else if (rot == Surface.ROTATION_180) {
//			rotateView(180f);
		} else if (rot == Surface.ROTATION_270) {
			rotateView(180f);
		} else {
			Log.e(TAG,"Invalid roation state");
		}


		return mCamera;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// TODO: When using display.getRotation() we can't detect 180 degree at once changes!

		getViewRotation();

		int newRotation = (((WindowManager)
				getSystemService(this.WINDOW_SERVICE)).getDefaultDisplay()).getRotation();

		if (newRotation == Surface.ROTATION_90) {
			rotateView(0f);
		} else if (newRotation == Surface.ROTATION_0) {
			rotateView(90f);
		} else if (newRotation == Surface.ROTATION_270) {
			rotateView(180f);
		} else {
			Log.e(TAG, "Invalid roation state");
		}

		getViewRotation();

	}



}