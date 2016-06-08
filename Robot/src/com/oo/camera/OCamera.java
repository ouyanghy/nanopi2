package com.oo.camera;

import java.io.IOException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class OCamera  implements SurfaceHolder.Callback, FaceDetectionListener {
	private static final String TAG = "OCamera";
	private Camera mCamera;
	private ODraw mDraw;
	private SurfaceHolder mCameraHolder;
	private SurfaceHolder mDrawHolder;
	private final int MOUNT_ANGLE = 270;
	public static final int GET_FACE = 1;
	
	public OCamera(Context context, SurfaceView camera, SurfaceView draw) {
		
		// TODO Auto-generated constructor stub
		camera.setZOrderOnTop(false);
		draw.setZOrderOnTop(true);
	
		mCameraHolder = camera.getHolder();
		mDrawHolder = draw.getHolder();
		mDrawHolder.setFormat(PixelFormat.TRANSLUCENT);
		
		
		mCameraHolder.addCallback(this);
		mDraw = new ODraw(draw);
		// mCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mCamera = Camera.open(0);
		Camera.Parameters parameters = mCamera.getParameters();

		parameters.setPreviewSize(800, 480);
		mCamera.setParameters(parameters);
		mCamera.setFaceDetectionListener(this);
		
		mCamera.setDisplayOrientation(MOUNT_ANGLE);
		Size s = parameters.getPreviewSize();
		Log.i(TAG, "width:" + s.width + " height:" + s.height);
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(mCameraHolder);
				mCamera.startFaceDetection();

				mCamera.startPreview();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "sufaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
		}
	}

	public void onRelease() {
		surfaceDestroyed(mCameraHolder);
	}

	private void printPoint(Point p) {
		if (p != null)
			Log.i(TAG, "point:(" + p.x + "," + p.y + ")");
	}

	@Override
	public void onFaceDetection(final Face[] faces, Camera camera) {
		// TODO Auto-generated method stub

		if (faces.length > 0 && faces != null && faces[0] != null) {
			printPoint(faces[0].leftEye);
			printPoint(faces[0].rightEye);
			printPoint(faces[0].mouth);
			Log.i(TAG, "score:" + faces[0].score);
			mDraw.draw(faces[0].leftEye, faces[0].rightEye, faces[0].mouth);
		}
	}
}
