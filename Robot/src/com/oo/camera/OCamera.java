package com.oo.camera;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class OCamera extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "OCamera";
	private Camera mCamera;
	private SurfaceHolder mHolder;

	public OCamera(Context context, SurfaceView v) {
		super(context);
		// TODO Auto-generated constructor stub
		mHolder = v.getHolder();
		mHolder.addCallback(this);
		// mHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mCamera = Camera.open(0);
		Camera.Parameters parameters = mCamera.getParameters();
		
		parameters.setPreviewSize(800, 480);
		mCamera.setParameters(parameters);
		Size s = parameters.getPreviewSize();
		Log.i(TAG, "width:" + s.width + " height:" + s.height);
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(mHolder);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		if (mCamera != null)
			mCamera.startPreview();
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
		surfaceDestroyed(mHolder);
	}
}
