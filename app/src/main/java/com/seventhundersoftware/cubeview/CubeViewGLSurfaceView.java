package com.seventhundersoftware.cubeview;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.util.Log;

import com.seventhundersoftware.cubeview.common.TextureHelper;

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
    private float mPreviousX;
    private float mPreviousY;
    private final int I_MOVEMENT = 4;
    private static final String TAG="CubeViewGLSurfaceView()";
    
    private float mDensity;
        	
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

	@param MotionEvent: event with X and Y properties.
	@return boolean
	*/
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		if (event != null)
		{			
			float x = event.getX();
			float y = event.getY();

			if (event.getAction() == MotionEvent.ACTION_MOVE)
			{
				if (mRenderer != null) {

					float deltaX = (x - mPreviousX) / mDensity / 2f;
					float deltaY = (y - mPreviousY) / mDensity / 2f;

					float absY = Math.abs(y);
					mRenderer.mFloatDeltY = deltaY;
				}

			}	
			
			mPreviousX = x;
			mPreviousY = Math.abs(y);
			
			return true;
		}
		else
		{
			return super.onTouchEvent(event);
		}		
	}


	/***
	Assign our CubeViewRenderer
	@param CubeViewRenderer: Derived from Renderer
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
}
