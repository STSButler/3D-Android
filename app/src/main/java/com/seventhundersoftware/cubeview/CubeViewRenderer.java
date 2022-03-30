package com.seventhundersoftware.cubeview;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.seventhundersoftware.cubeview.common.RawResourceReader;
import com.seventhundersoftware.cubeview.common.ShaderHelper;
import com.seventhundersoftware.cubeview.common.TextureHelper;
import static com.seventhundersoftware.cubeview.CubeViewConstants.*;

/**
 * Copyright (c) 2021 Amy Washburn Butler
 * GNU General Public License v3.0

 The CubeViewRenderer class includes most of the
 OpenGL ES method calls and buffers, to display a view.
 It creates and uploads vertex, texel and element buffers,
 uploads textures, depending on the view the user selects, and
 renders the view.
 Java, with OpenGL ES, uploads matrices for
 the model-view-projection matrix used to rotate the view.
*/
public class CubeViewRenderer implements GLSurfaceView.Renderer {

    // Debugging output begins with:
    private static final String TAG = "CubeViewRenderer";
    private final Context mActivityContext;
    private boolean bScale = false;

    // Matrix for the scene's model.
    private float[] mMatrixModel = new float[16];

    // Matrix for camera view.
    private float[] mMatrixView = new float[16];

    // Matrix for projection.
    private float[] mMatrixProject = new float[16];

    // Matrix for model view projection.
    private float[] mMatrixMVP = new float[16];

    // Matrices for combined rotations.
    private final float[] mMatrixAccumulateX = new float[16];
    private final float[] mMatrixAccumulateY = new float[16];

    // Matrix for rotations.
    private final float[] mMatrixRotate = new float[16];

    // Matrix for temporary storage.
    private float[] mMatrixTemp = new float[16];

    // Vertices and texels to be assigned to floating
    // arrays for upload to vertex buffer objects (VBO).
    private final FloatBuffer mFloatBufferCubeVertices;
    private final FloatBuffer mFloatBufferCubeTexels;
    // Shader location of model view projection matrix.
    private int mIntHandleMVP;

    // Shader location of texture uniform.
    private int mIntUniformTextureHandle;

    // Shader location of vertex attributes.
    private int mIntVertexAttribs;

    // Shader location of texture attributes.
    private int mIntAttributeTexture;

    // Bytes for each floating point and each integer:
    private final int I_BYTES_PER_FLOAT = 4;
    private final int I_BYTES_PER_ELEMENT = 2;

    // Number of vertices:
    private final int I_SIZE_VERTEX = 3;

    // Number of texels:
    private final int I_SIZE_TEXEL = 2;

    /**
     * This is a handle to our cube shading program.
     */
    private int mIntHandleProgram;

    // Handle to texture in shader:
    public int mITexture;

    // Rotation around Y axis:
    public float mFloatDeltY;
    protected float mFloatDeltYPrev;

    protected final float F_SCALE_DOWN = 0.125f;
    protected final float F_SCALE_UP = 8f;
    protected boolean bScalePrevious = false;

    protected float mfScale = 1f;

    // Prepared for element array:
    private final short mShortArrayElements[] = {
    0, 1, 2, 0, 2, 3,  // front
    4, 5, 6, 4, 6, 7,  // back
    8, 9, 10, 8, 10, 11, // top
    12, 13, 14, 12, 14, 15, // bottom
    16, 17, 18, 16, 18, 19, // right
    20, 21, 22, 20, 22, 23  // left
    };

    private final ShortBuffer mShortElements;
    private int img = 0;
    private int imgV = 0;

    /***
     * Initialize the renderer.
     * @param activityContext: Context
     * @param imgView: integer; image number for drawable
     * to texture map the first view.
     */
    public CubeViewRenderer(final Context activityContext, int imgView) {
        mActivityContext = activityContext;
        imgV = imgView;

    // Declare cube vertices:X,Y,Z
    // Every four rows represent vertices
    // for one side of the cube.
    final float[] FloatArrayCubeVertices =
    {
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        -1.0f, 1.0f, 1.0f,

        -1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, -1.0f,
        1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,

        -1.0f, 1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,

        -1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,

        1.0f, -1.0f, -1.0f,
        1.0f, 1.0f, -1.0f,
        1.0f, 1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,

        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        -1.0f, 1.0f, 1.0f,
        -1.0f, 1.0f, -1.0f

    };

    // Declare cube texels:S,T.
    // Every four rows represent texels
    // for one side of the cube.
    // These map one cross formatted
    // texture to the cube.
    final float[] floatArrayCubeTexels =
    {
        0.4999f, 0.2499f,
        0.74999f, 0.24999f,
        0.74999f, 0.498f,
        0.4999f, 0.498f,

        0.249f, 0.251f,
        0.249f, 0.498f,
        0.0f, 0.498f,
        0.0f, 0.251f,

        0.251f, 0.498f,
        0.499f, 0.498f,
        0.499f, 0.749f,
        0.251f, 0.749f,

        0.251f, 0.249f,
        0.251f, 0.0f,
        0.499f, 0.0f,
        0.499f, 0.249f,

        1.0f, 0.2511f,
        1.0f, 0.499f,
        0.74999f, 0.499f,
        0.74999f, 0.2511f,

        0.249990f, 0.24999f,
        0.4999f, 0.24999f,
        0.4999f, 0.4999f,
        0.249990f, 0.4999f
    };

        // Initialize the buffers.
        mFloatBufferCubeVertices = ByteBuffer.allocateDirect(FloatArrayCubeVertices.length * I_BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mFloatBufferCubeVertices.put(FloatArrayCubeVertices).position(0);

        mFloatBufferCubeTexels = ByteBuffer.allocateDirect(floatArrayCubeTexels.length * I_BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mFloatBufferCubeTexels.put(floatArrayCubeTexels).position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(
                mShortArrayElements.length * I_BYTES_PER_ELEMENT);
        dlb.order(ByteOrder.nativeOrder());
        mShortElements = dlb.asShortBuffer();
        mShortElements.put(mShortArrayElements);
        mShortElements.position(0);
    }

    /***
     * Create our OpenGL ES 2.0 surface
     * including view, model and rotation matrices,
     * plus vertex and fragment shaders,
     * the program for this app, and texture
     * for the first view.
     *
     * @param glUnused: Not using OpenGL ES 1.0
     * @param config: Configuration for OpenGL ES.
     */
    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // Set the background clear color to black.
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -0.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // View matrix = direction we're viewing.
        Matrix.setLookAtM(mMatrixView, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        final String vertexShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_vertex_shader_tex);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_fragment_shader_tex);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        mIntHandleProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[]{"a_Position", "a_TexCoordinate"});
        getIntHandles();
        GLES20.glUseProgram(mIntHandleProgram);
        getImage(imgV);
        setTexView();
        // Initialize the accumulated rotation matrix
        Matrix.setIdentityM(mMatrixAccumulateX, 0);
        Matrix.setIdentityM(mMatrixAccumulateY, 0);

        // Settings for first render.
        Matrix.setIdentityM(mMatrixModel, 0);
        Matrix.rotateM(mMatrixModel, 0, 90, 0, 0, 1);
        Matrix.translateM(mMatrixModel, 0, 0.0f, 0f, -1f);
        mFloatDeltY = 1f;
        mFloatDeltYPrev = 0.0f;
    }

    /***
     * Assign the drawable ID for this view
     * to private integer property, img.
     * @param item: Integer representing the
     * image we want to use for this view.
     */
    public void getImage(int item){
        bScale = false;
        Log.d(TAG,"renderer.getImage():"+item);
        switch(item){
            case I_LIGHTHOUSE:
                img =  R.drawable.scene_cube_lighthouse;
                break;
            case I_RIVER:
                img =  R.drawable.scene_cube_river;
                break;
            case I_GALLERY:
                img =  R.drawable.scene_cube_gallery;
                bScale = true;
                break;
            case I_GRID:
                img =  R.drawable.scene_cube_grid;
                break;
            case I_ISLANDS:
            default:
                img =  R.drawable.scene_cube_islands;
                break;
        }
    }

    /***
     * Prepares the projection matrix when
     * the view changes.
     * Called for new image maps.
     * Called for every rotation, if this app rotated.
     * @param glUnused
     * @param width: Width of our surface.
     * @param height: Height of our surface.
     */
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {   //  OpenGL ES 2.0 top left corner, width and height:
        GLES20.glViewport(0, 0, width, height);
        Log.d(TAG, "onSurfaceChanged() w:" + width + ",h:" + height);
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 0.5f;
        final float far = 1000f;
        // Projection matrix left and right, top and bottom, near and far:
        Matrix.frustumM(mMatrixProject, 0, left, right, bottom, top, near, far);
    }

    /***
     * Prepare a texture from a drawable, along
     * with mipmaps. Assign the shader's texture
     * uniform to our new texture. Possibly
     * scale the texture up or down.
     *

     * apply for the next view.
     */
    public void setTexView() {
        Log.d(TAG,"renderer.setTexview()");
        if (bScalePrevious == false && bScale == true) {
            // scale up the cube:
            Matrix.scaleM(mMatrixModel, 0, F_SCALE_UP, F_SCALE_UP, F_SCALE_UP);
            bScalePrevious = bScale;
        } else if (bScalePrevious == true && bScale == false) {
            Matrix.scaleM(mMatrixModel, 0, F_SCALE_DOWN, F_SCALE_DOWN, F_SCALE_DOWN);
            bScalePrevious = bScale;
        }
        mITexture = TextureHelper.loadTexture(mActivityContext, img);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mITexture);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glUniform1i(mIntUniformTextureHandle, 0);
    }

    /***
     * Obtain the integer handles to
     * the shader's model view matrix, texture sampler,
     * texture attributes and vertex attributes.
     */
    private void getIntHandles() {
        mIntHandleMVP = GLES20.glGetUniformLocation(mIntHandleProgram, "u_MVPMatrix");
        mIntUniformTextureHandle = GLES20.glGetUniformLocation(mIntHandleProgram, "u_Texture");
        mIntAttributeTexture = GLES20.glGetAttribLocation(mIntHandleProgram, "a_TexCoordinate");
        mIntVertexAttribs = GLES20.glGetAttribLocation(mIntHandleProgram, "a_Position");

        mFloatBufferCubeVertices.position(0);
        GLES20.glVertexAttribPointer(mIntVertexAttribs, I_SIZE_VERTEX, GLES20.GL_FLOAT, false,
                0, mFloatBufferCubeVertices);
        GLES20.glEnableVertexAttribArray(mIntVertexAttribs);

        mFloatBufferCubeTexels.position(0);
        GLES20.glVertexAttribPointer(mIntAttributeTexture, I_SIZE_TEXEL, GLES20.GL_FLOAT, false,
                0, mFloatBufferCubeTexels);
        GLES20.glEnableVertexAttribArray(mIntAttributeTexture);
    }

    /***
     * Display one frame of our view.
     * @param glUnused: OpenGL ES 1.0 not used.
     */
    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Start with identity model, moved just a little back from Z center:
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mFloatDeltYPrev != mFloatDeltY) {
            drawDeltaRotationsY();
            // Accumulated rotations:
            mFloatDeltYPrev = mFloatDeltY;
        }
        drawCube();
    }

    /***
     * Rotate a model matrix then
     * upload the matrix to the shader.
     */
    private void drawDeltaRotationsY() {
        Matrix.setIdentityM(mMatrixModel, 0);
        Matrix.rotateM(mMatrixModel, 0, 90, 0, 0, 1);
        Matrix.translateM(mMatrixModel, 0, 0.0f, 0f, -1f);
        if (bScalePrevious == true) {
            Matrix.scaleM(mMatrixModel, 0, F_SCALE_UP, F_SCALE_UP, F_SCALE_UP);
        }

        // Current rotation = identity matrix:
        Matrix.setIdentityM(mMatrixRotate, 0);
        // Rotate around the Y axis:
        Matrix.rotateM(mMatrixRotate, 0, mFloatDeltY, 0.0f, 1.0f, 0.0f);
        // mMatrixRotate * mMatrixAccumulateY = mMatrixTemp. Save mMatrixAccumulateY with all rotations:
        Matrix.multiplyMM(mMatrixTemp, 0, mMatrixRotate, 0, mMatrixAccumulateY, 0);
        // mMatrixTemp => mMatrixAccumulateY
        System.arraycopy(mMatrixTemp, 0, mMatrixAccumulateY, 0, 16);

        // mMatrixModel * mMatrixAccumulateX = mMatrixTemp. Multiply model by all rotations, then save to model:
        Matrix.multiplyMM(mMatrixTemp, 0, mMatrixModel, 0, mMatrixAccumulateY, 0);
        System.arraycopy(mMatrixTemp, 0, mMatrixModel, 0, 16);
    }

    /**
     * Draw the cube.
     * Multiply the current rotated model matrix
     * by the view and projection matrix. Copy to mMatrixMVP.
     * Upload mMatrixMVP matrix to the shader.
     * Draw each vertex and texel to fragments on the screen.
     */
    private void drawCube() {
        // mMatrixView * mMatrixModel = mMatrixMVP. Multiply model by view and copy to MVP.
        Matrix.multiplyMM(mMatrixMVP, 0, mMatrixView, 0, mMatrixModel, 0);
        // mMatrixProject * mMatrixMVP = mMatrixTemp. Multiply MVP by Projection and copy to temp.
        Matrix.multiplyMM(mMatrixTemp, 0, mMatrixProject, 0, mMatrixMVP, 0);
        // mMatrixTemp => mMatrixMVP. Save temp to MVP for upload.
        System.arraycopy(mMatrixTemp, 0, mMatrixMVP, 0, 16);

        // Upload MVP matrix to vertex shader.
        GLES20.glUniformMatrix4fv(mIntHandleMVP, 1, false, mMatrixMVP, 0);

        // Draw each element's vertex and texel attribute:
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, mShortArrayElements.length,
                GLES20.GL_UNSIGNED_SHORT, mShortElements);
    }

}

