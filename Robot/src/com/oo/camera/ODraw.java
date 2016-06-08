package com.oo.camera;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ODraw implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private static final String TAG = "ODraw";
	private int mWidth, mHeight,  mX,  mY;
	private float mWidthScale, mHeightScale;
	
	public void getViewSize(final SurfaceView v)
	{
		Runnable r = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mWidth = v.getWidth();
				mHeight = v.getHeight();
				mWidthScale = (float) (mWidth / 2000.0);
				mHeightScale = (float) (mHeight / 2000.0);
				int[] location = new  int[2] ;
			    v.getLocationInWindow(location); //获取在当前窗口内的绝对坐标
			    v.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
			    mX = location[0];
			    mY = location[1];
				Log.i(TAG, "X:" + mX + " Y:" + mY + " width:" + mWidth + " mHeight:"+ mHeight);
			}
		};
		new Thread(r).start();
	}
	public ODraw(SurfaceView v) {
		// TODO Auto-generated constructor stub
		mHolder = v.getHolder();

		mHolder.addCallback(this);
		getViewSize(v);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

		Log.i(TAG, "surface created");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "surface changed");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	public void draw(Point leftEye, Point rightEye, Point mouth) {
		new MDrawThread(leftEye, rightEye, mouth).start();
	}

	private void drawBlock(Canvas canvas, Paint paint, Point leftEye, Point rightEye, Point mouth) {
		// TODO Auto-generated method stub
		if (leftEye != null && rightEye != null && mouth != null) {
			
			canvas.drawCircle((leftEye.x + 1000)* mWidthScale, (leftEye.y + 1000 ) * mHeightScale, 20, paint);
			//canvas.drawCircle(rightEye.x, rightEye.y, 20, paint);
			//canvas.drawCircle(mouth.x, mouth.y, 20, paint);
		}
	}

	class MDrawThread extends Thread {
		Point mLeftEye, mRightEye, mMouth;

		public MDrawThread(Point l, Point r, Point m) {
			// TODO Auto-generated constructor stub
			mLeftEye = l;
			mRightEye = r;
			mMouth = m;
		}

		@Override
		public void run() {
			Log.i(TAG, "Work");

			Canvas canvas = mHolder.lockCanvas();
			if (canvas == null) {
				Log.i(TAG, "canvas is null ptr");
				return;
			}
			canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
			mHolder.unlockCanvasAndPost(canvas);

			canvas = mHolder.lockCanvas();
			if (canvas == null) {
				Log.i(TAG, "canvas is null ptr");
				return;
			}

			Paint paint = new Paint();
			paint.setColor(Color.RED);
			drawBlock(canvas, paint, mLeftEye, mRightEye, mMouth);
			mHolder.unlockCanvasAndPost(canvas);
		}

	}

}
