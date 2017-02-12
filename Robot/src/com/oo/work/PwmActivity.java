package com.oo.work;



import com.oo.camera.OCamera;
import com.oo.pwm.Servo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PwmActivity extends Activity {
	private static final String TAG = "WORK";
	private Servo mServo;
	private SurfaceView mCameraSurfaceView;
	private SurfaceView mSurfaceDrawView;
	private OCamera mCamera;
	private MHandle mHandle;
	private ImageView mPictureView;
	private boolean isDraw =false;
	private TextView mTv;
	private int iHorDegree = 0 ,iVerDegree = 0;
	private boolean bWork = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pwm);
		//mSurfaceHolder = findViewById(id)
		mTv = (TextView) findViewById(R.id.tv);
		mHandle = new MHandle();
		mCameraSurfaceView = (SurfaceView) findViewById(R.id.SurfaceViewCamera);
		mSurfaceDrawView = (SurfaceView) findViewById(R.id.SurfaceViewDraw);
		mPictureView = (ImageView) findViewById(R.id.imageViewPicture);
		mCamera = new OCamera(getApplicationContext(), mCameraSurfaceView, mSurfaceDrawView, mHandle);
		mServo = new Servo();
		mServo.open();
		
		mServo.request();
		initPos();
		
	}

	@Override
	public void onDestroy()
	{
		bWork = false;
		mServo.close();
		mCamera.onRelease();
		super.onDestroy();
	}
	
	public void onClickRequest(View v)
	{
		mServo.request();
		Log.i(TAG, "onClickRequest");
	}
	public void onClickFree(View v)
	{
		mServo.free();
		Log.i(TAG, "onClickFree");
	}

	public void initPos() {
		iHorDegree = 90;
		mServo.setDegree(Servo.CHANNEL_HOR, iHorDegree);
	}
	
	public void  onClickLeft(View v)
	{
		iHorDegree -= 45;
		if (iHorDegree < 45)
			iHorDegree = 45;
		mServo.setDegree(Servo.CHANNEL_HOR, iHorDegree);

		Log.i(TAG, "onClickLeft");
	}
	
	public void onClickRight(View v)
	{
		iHorDegree += 45;
		if (iHorDegree > 135) {
			iHorDegree = 135;
		}
		mServo.setDegree(Servo.CHANNEL_HOR, iHorDegree);
		
		Log.i(TAG, "onClickRight");
	}
	
	public void onClickUp(View v)
	{
		iVerDegree += 45;
		if (iVerDegree > 180)
			iVerDegree = 180;
		mServo.setDegree(Servo.CHANNEL_VER, iVerDegree);
	}
	
	public void onClickDown(View v)
	{
		iVerDegree -= 45;
		if (iVerDegree < 0)
			iVerDegree = 0;
		
		mServo.setDegree(Servo.CHANNEL_VER, iVerDegree);
	}
	
	public void adjust(PointF p){
			bWork = true;
			int width = mCamera.getWidth();
			int height = mCamera.getHeight();
			
			int l, m, r ;
			l = 160;
			r = 660;
			if (p.x < l)
				onClickRight(null);
			else if (p.x >= l && p.x <= r)
				;
			else
				onClickLeft(null);
				
				
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bWork = false;
			
		
	}
	
	public class MHandle extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OCamera.GET_PICTURE:
			
				Bitmap bmp = (Bitmap) msg.obj;
				if (bmp == null || isDraw == true)
					return;
				
				isDraw = true;
				mPictureView.setImageBitmap(bmp);
				
				isDraw = false;
				break;
			case OCamera.GET_POINT:
				PointF p = (PointF) msg.obj;
				mTv.setTextColor(Color.RED);
				mTv.setTextSize(25);
				mTv.setText("Middle Point:" + "(" + (int)p.x + "," + (int)p.y + ")");
				if (bWork == false)
					adjust(p);
				//mTv.setText("Middle Point:" );
				break;
			}
		}
	}
}
