package com.seventhundersoftware.cubeview;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;

import com.seventhundersoftware.cubeview.common.TextureHelper;
import static com.seventhundersoftware.cubeview.CubeViewConstants.*;

/***
 * Copyright (c) 2021 Amy Washburn Butler
 * GNU General Public License v3.0

 * The CubeViewActivity class loads the application's main
 * Activity, creates an OpenGL ES SurfaceView,
 * changes views, when the user taps a translucent, overlay button,
 * saves and restores the view.
 *
 * The app's lifecycle pairs:
 * onCreate() onDestroy()
 * onPause(), onResume(),
 * onSaveInstanceState(), onRestoreInstanceState(),
 * onStart(),onStop()
 */
public class CubeViewActivity extends Activity
{
	public static final String PREFERENCE_FILE_NAME = "CubeView.xml";

	/** Hold a reference to our GLSurfaceView */
	private CubeViewGLSurfaceView mGLSurfaceView = null;
	private CubeViewRenderer mRenderer = null;
	private Context mContext = null;

	private static final String SETTING_VIEWS = "setting_views";
	private static final String TAG = "CubeViewActivity";

	private  ActivityManager  activityManager = null;
	private ConfigurationInfo configurationInfo = null;
	private boolean supportsEs2 = false;
	private int currentApiVersion;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cube_view);

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		currentApiVersion = android.os.Build.VERSION.SDK_INT;

		final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
				View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
				View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
				View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

		if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
			getWindow().getDecorView().setSystemUiVisibility(flags);
			final View decorView = getWindow().getDecorView();

			decorView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
				@Override
				public void onSystemUiVisibilityChange(int visibility) {

					if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
						decorView.setSystemUiVisibility(flags);
					}
				}
			});
		}

		mGLSurfaceView = (CubeViewGLSurfaceView)findViewById(R.id.gl_surface_view);
		mContext = this;

		// Check if the system supports OpenGL ES 2.0.
		activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		configurationInfo = activityManager.getDeviceConfigurationInfo();
		supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
	}

	/***
	 * OnDestroy() and onCreate() often pair.
	 * Meaning if onDestroy() is called, then
	 * onCreate() will be called at start up.
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
		TextureHelper.FreeTexture();
		Log.d(TAG,"onDestroy()");
	}

	/***
	 * onResume() and onPause() often pair.
	 * Meaning if onPause() is called, then
	 * onResume() will be called at start up.
	 */
	@Override
	protected void onResume() 
	{
		super.onResume();
		Log.d(TAG,"onResume()");
		SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
		int iLastView = sharedPrefs.getInt(SETTING_VIEWS, CubeViewConstants.I_ISLANDS);
		Log.d(TAG,"onResume() retrieved view id:"+iLastView);
		if (supportsEs2)
		{
			// Request an OpenGL ES 2.0 compatible context.
			mGLSurfaceView.setEGLContextClientVersion(2);

			final DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			// Activity creates SurfaceView.
			// Activity creates Renderer.
			// SurfaceView assigns Renderer.
			// The last parameter is the image number.
			mRenderer = new CubeViewRenderer(this,iLastView);
			Log.d(TAG,"onCreate() mRenderer:"+mRenderer);
			mGLSurfaceView.setRenderer(mRenderer, displayMetrics.density);
		}
	}

	/***
	 * Android pauses or moves away from app.
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		int iSaveView = I_ISLANDS;
		SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		if(mGLSurfaceView != null){
			iSaveView = mGLSurfaceView.getView();
		}
		editor.putInt(SETTING_VIEWS, iSaveView);
		editor.apply();
		Log.d(TAG,"onPause()");
	}

	/***
	 * onStart() and onStart() often pair.
	 * Left here for output to show the
	 * Android life cycle.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG,"onStart()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG,"onStop()");
	}

	/***
	 * onSaveInstanceState() and onRestoreInstanceState()
	 * often pair. However when view orientation is
	 * restricted to either landscape or portrait,
	 * then onRestoreInstanceState() is not called.
	 * This app only displays in landscape mode.
	 * Therefore these two methods are just left
	 * for reference.
	 * @param savedInstanceState: Bundle to save state.
	 */
	@Override
	protected void onSaveInstanceState (Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}
}
