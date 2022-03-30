package com.seventhundersoftware.cubeview;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.util.Log;
import static com.seventhundersoftware.cubeview.CubeViewConstants.*;

/***
 * Copyright (c) 2021 Amy Washburn Butler
 * GNU General Public License v3.0

 The CubeViewGLSurfaceView class derives from Android's GLSurfaceView,
 which provides an OpenGL ES surface for rendering.
 This code creates the surface, implements a method that responds 
 to touch events and a method capable of assigning a renderer to the view.
*/
public class CubeViewGLSurfaceView extends GLSurfaceView
{	
    private CubeViewRenderer mRenderer;
	
    // Offsets for touch events	 
    private float mPriorSwipeX = 0;
    private float mPriorSwipeY = 0;
    private float mPriorTouchDownX = 0;
    private float mPriorTouchDownY = 0;

    private static final String TAG="CubeViewGLSurfaceView()";
    private Context mContext = null;
    private float mDensity;
    private int iBtnClick = I_ISLANDS;
        	
	/***
	Constructor
	@param context: this Context.
	*/	
	public CubeViewGLSurfaceView(Context context)
	{
		super(context);
	}
	
	/***
	Constructor 
	@param context: This Context
	@param attrs: Set of attributes for canvas.
	*/	
	public CubeViewGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.mContext = context;
	}

	/***
	OpenGL ES canvas responds
	to touch events.
	Here we can rotate the view based on the user's
	touch position along the Y axis. However
	we're also the X axis touch point. 
	X and Y values might prove useful
	if we want to see the top and bottom, not
	just left and right sides of a view.
	@return boolean
	*/
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		if (event != null)
		{			
			float x = event.getX();
			float y = event.getY();
			int iEvent = event.getAction();
			Log.d(TAG,"onTouchEvent event action:"+iEvent);
			if (iEvent == MotionEvent.ACTION_MOVE)
			{
				if (mRenderer != null) {

					float deltaX = (x - mPriorSwipeX) / mDensity / 2f;
					float deltaY = (y - mPriorSwipeY) / mDensity / 2f;

					float absY = Math.abs(y);
					mRenderer.mFloatDeltY = deltaY;
				}

			}
			// Change views if player
			// tapped down and up in same location.
			else if (iEvent == MotionEvent.ACTION_UP){
				Log.d(TAG,"onTouch Action up: "+iEvent);
				Log.d(TAG,"current x:"+x+",y:"+y+",priorX:"+this.mPriorTouchDownX+",priorY:"+this.mPriorTouchDownY);
				if(x == this.mPriorTouchDownX && y == this.mPriorTouchDownY){
					this.assignView();
				}
			}
			else if(iEvent == MotionEvent.ACTION_DOWN) {
				this.mPriorTouchDownX = x;
				this.mPriorTouchDownY = y;
			}
			
			mPriorSwipeX = x;
			mPriorSwipeY = Math.abs(y);
			
			return true;
		}
		else
		{
			return super.onTouchEvent(event);
		}		
	}

	/***
	Assign our CubeViewRenderer
	@param density: float
	*/
	public void setRenderer(CubeViewRenderer renderer, float density)
	{
		mRenderer = renderer;
		mDensity = density;
		super.setRenderer(renderer);
	}

	@Override
	public void onPause(){
		super.onPause();
	}

	public void assignView(){
		Log.d(TAG,"assignView() surfaceView.setTextView:");
		iBtnClick++;
		if(iBtnClick >= I_IMAGES){
			iBtnClick = I_ISLANDS;
		}
		mRenderer.getImage(iBtnClick);
		mRenderer.setTexView();
		this.queueEvent(new Runnable()
		{
			@Override
			public void run()
			{
				if(mRenderer != null){
					Log.d(TAG,"surfaceview run():");
					mRenderer.getImage(iBtnClick);
					mRenderer.setTexView();
				}
			}
		});
	}
	// Getter
	public int getView() {
		return this.iBtnClick;
	}
}
