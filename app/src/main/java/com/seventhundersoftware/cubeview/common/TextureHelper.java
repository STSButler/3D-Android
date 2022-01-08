package com.seventhundersoftware.cubeview.common;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/***
This Java code is modified from 
https://github.com/learnopengles/Learn-OpenGLES-Tutorials
code posted on GitHub.
Their WebGL tutorials can be found at
http://www.learnopengles.com.

This Java source code loads a texture
from a resource ID.
I modified the code to allocate just one texture buffer
and delete that buffer when required.
*/
public class TextureHelper
{
	private static final String TAG = "TextureHelper";
	final static int[] textureHandle = new int[1];
	static Context mContext = null;

	/***
	 *
	 * @param context: Context of this app.
	 * @param resourceId: Drawable number of the current
	 * image to use as a texture map.
	 * @return integer: the texture handle.
	 */
	public static int loadTexture(final Context context, final int resourceId)
	{
		mContext = context;
		// Just allocate memory for one texture once.
		if(textureHandle[0] == 0){
			GLES20.glGenTextures(1, textureHandle, 0);
		}

		if (textureHandle[0] == 0)
		{
			throw new RuntimeException("Error generating texture.");
		}

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;	// No pre-scaling

		// Read in the resource
		final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

		// Bind to the texture in OpenGL
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

		// Set filtering
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

		// Load the bitmap into the bound texture.
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		// Recycle the bitmap, since its data has been loaded into OpenGL.
		bitmap.recycle();

		return textureHandle[0];
	}

	/***
	 * Free the buffer
	 * saved for an image map.
	 */
	public static void FreeTexture(){
		if(textureHandle[0] != 0) {
			GLES20.glDeleteTextures(1, textureHandle, 0);
			Log.d(TAG,"FreeTexture()");
			textureHandle[0] = 0;
		}

	}
}
