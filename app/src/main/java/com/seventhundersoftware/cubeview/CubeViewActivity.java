package com.seventhundersoftware.cubeview;
import static com.seventhundersoftware.cubeview.CubeViewRenderer.I_IMAGES;
import static com.seventhundersoftware.cubeview.CubeViewRenderer.I_ISLANDS;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;

import com.seventhundersoftware.cubeview.common.TextureHelper;

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
	
	private int miButtonClick = I_ISLANDS;

	private static final String SETTING_VIEWS = "setting_views";
	private static final String TAG = "CubeViewActivity";

	private  ActivityManager  activityManager = null;
	private ConfigurationInfo configurationInfo = null;
	private boolean supportsEs2 = false;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cube_view);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mGLSurfaceView = (CubeViewGLSurfaceView)findViewById(R.id.gl_surface_view);
		mContext = this;
		Log.d(TAG,"onCreate():"+ miButtonClick);

		// Check if the system supports OpenGL ES 2.0.
		activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		configurationInfo = activityManager.getDeviceConfigurationInfo();
		supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		// Views, by integer, in order follow:
		// islands 0,gallery 1,river 2,lighthouse 3,cube grid 4
		findViewById(R.id.button_views).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setViews();
			}
		});
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
		miButtonClick = sharedPrefs.getInt(SETTING_VIEWS, I_ISLANDS);
		Log.d(TAG,"onResume() retrieved view id:"+miButtonClick);
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
			mRenderer = new CubeViewRenderer(this,miButtonClick);
			Log.d(TAG,"onCreate() mRenderer:"+mRenderer);
			mGLSurfaceView.setRenderer(mRenderer, displayMetrics.density);

		}
		else 		{
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}
	}

	/***
	 * Android pauses or moves away from app.
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putInt(SETTING_VIEWS, miButtonClick);
		editor.commit();
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

	/***
	 * setViews() responds to "Change" button clicks.
	 * miButtonClick integer representing the drawable to
	 * map to a view. In otherwords, item tells
	 * the renderer which texture to display.
	 * Need to queue the event to run
	 * OpenGLES renderer on a thread.
	 */
	private void setViews()
	{
	    miButtonClick++;
		// Currently {0..4}
		// {I_ISLANDS...(I_IMAGES-1)}
		if(miButtonClick == I_IMAGES){
			miButtonClick = I_ISLANDS;
		}

		Log.d(TAG,"setViews() miButtonClick:"+ miButtonClick);
		mGLSurfaceView.queueEvent(new Runnable()
		{
			@Override
			public void run()
			{
				if(mRenderer != null){
					Log.d(TAG,"setViews() mRenderer.setTextView:");
					mRenderer.getImage(miButtonClick);
					mRenderer.setTexView();
				}
			}
		});

	}
}