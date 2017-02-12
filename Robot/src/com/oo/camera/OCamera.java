package com.oo.camera;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class OCamera implements SurfaceHolder.Callback {
	private static final String TAG = "OCamera";
	private Camera mCamera;
	private SurfaceHolder mCameraHolder;
	private SurfaceHolder mDrawHolder;
	private OFaceDetetor mFaceDetetor;

	private final int PREVIEW_ANGLE = 0;
	private final int PICTURE_ANGLE = 0;
	public static final int GET_PICTURE = 1;
	public static final int GET_FACE = 2;
	public static final int GET_POINT = 3;
	// private final int FACE_DETE_TRICK = 2;
	private int mWidth, mHeight;
	private boolean isTransfer = false;
	private byte[] mRgbByte;
	private Handler mHandle;
	//private int mFaceDeteCount;

	public OCamera(Context context, SurfaceView camera, SurfaceView draw, Handler handle) {

		// TODO Auto-generated constructor stub
		camera.setZOrderOnTop(false);
		draw.setZOrderOnTop(true);

		mCameraHolder = camera.getHolder();
		mDrawHolder = draw.getHolder();
		mDrawHolder.setFormat(PixelFormat.TRANSLUCENT);

		mCameraHolder.addCallback(this);
		mHandle = handle;

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mCamera = Camera.open(0);
		Camera.Parameters parameters = mCamera.getParameters();

		List<Size> l = parameters.getSupportedPictureSizes();
		for (Size s : l) {
			Log.i(TAG, "width:" + s.width + " height:" + s.height);
		}
		parameters.setPreviewSize(800, 600);
		mCamera.setParameters(parameters);

		mCamera.setDisplayOrientation(PREVIEW_ANGLE);
		Size s = parameters.getPreviewSize();
		mWidth = s.width;
		mHeight = s.height;
		// Log.i(TAG, "width:" + s.width + " height:" + s.height + " format:" +
		// parameters.getPreviewFormat());

		try {
			mCamera.setPreviewDisplay(mCameraHolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mCamera.startPreview();
		mCamera.setPreviewCallback(mPreviewCallBack);

	}

	private PreviewCallback mPreviewCallBack = new PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			if (isTransfer == false) {
				// Log.i(TAG, "data length" + data.length);
				isTransfer = true;

				Bitmap bitmap = yuv420sp2rgb(data);
				bitmap = adjustPhotoRotation(bitmap, PICTURE_ANGLE);

				// if (mFaceDeteCount++ > FACE_DETE_TRICK) {
				if (mFaceDetetor == null)
					mFaceDetetor = new OFaceDetetor(bitmap.getWidth(), bitmap.getHeight(), 1);

				PointF p = mFaceDetetor.findFace(bitmap);
				if (p != null)
					mHandle.obtainMessage(GET_POINT, p).sendToTarget();
				// mFaceDeteCount = 0;
				// }
				// mHandle.obtainMessage(GET_PICTURE, bitmap).sendToTarget();
				isTransfer = false;
			} else {

			}
		}
	};

	Bitmap yuv420sp2rgb(byte[] data) {
		YuvImage yuv = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		yuv.compressToJpeg(new Rect(0, 0, mWidth, mHeight), 100, out);
		mRgbByte = out.toByteArray();
		Bitmap bitmap = BitmapFactory.decodeByteArray(mRgbByte, 0, mRgbByte.length);
		return bitmap;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "sufaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// onRelease();
	}

	public void onRelease() {
		mCamera.setPreviewCallback(null);
		surfaceDestroyed(mCameraHolder);
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	private void printPoint(Point p) {
		if (p != null)
			Log.i(TAG, "point:(" + p.x + "," + p.y + ")");
	}

	Bitmap adjustPhotoRotation(Bitmap bmpSrc, final int orientationDegree) {

		Matrix m = new Matrix();

		m.postRotate(orientationDegree);
		Bitmap bmp = Bitmap.createBitmap(bmpSrc, 0, 0, bmpSrc.getWidth(), bmpSrc.getHeight(), m, true);

		return bmp;
	}
	
	public int getWidth() {
		return mWidth;
	}
	
	public int getHeight() {
		return mHeight;
	}
}
