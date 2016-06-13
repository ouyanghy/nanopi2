package com.oo.pwm;



import com.oo.camera.OCamera;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

public class PwmActivity extends Activity {
	private static final String TAG = "PWM";
	private Moto mMoto;
	private int HZ = 100;
	private SurfaceView mCameraSurfaceView;
	private SurfaceView mSurfaceDrawView;
	private OCamera mCamera;
	private MHandle mHandle;
	private ImageView mPictureView;
	private boolean isDraw =false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pwm);
		//mSurfaceHolder = findViewById(id)
		mHandle = new MHandle();
		mCameraSurfaceView = (SurfaceView) findViewById(R.id.SurfaceViewCamera);
		mSurfaceDrawView = (SurfaceView) findViewById(R.id.SurfaceViewDraw);
		mPictureView = (ImageView) findViewById(R.id.imageViewPicture);
		mCamera = new OCamera(getApplicationContext(), mCameraSurfaceView, mSurfaceDrawView, mHandle);
		mMoto = new Moto();
		mMoto.open();
		mMoto.setFrequence(HZ);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pwm, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onDestroy()
	{
		mMoto.free();
		mMoto.close();
		mCamera.onRelease();
		super.onDestroy();
	}
	
	public void onClickRequest(View v)
	{
		mMoto.request();
		Log.i(TAG, "onClickRequest");
	}
	public void onClickFree(View v)
	{
		mMoto.free();
		Log.i(TAG, "onClickFree");
	}

	public void onClickHz(View v)
	{
		
		int ret = mMoto.setFrequence(HZ);
		Log.i(TAG, "HZ:" + HZ + "ret:" + ret);
		HZ += 10;
	}
	
	public void  onClickLeft(View v)
	{
		mMoto.runHorOneTime(Moto.DIRECTION_LEFT);
		Log.i(TAG, "onClickLeft");
	}
	
	public void onClickRight(View v)
	{
		mMoto.runHorOneTime(Moto.DIRECTION_RIGHT);
		Log.i(TAG, "onClickRight");
	}
	
	public void onClickUp(View v)
	{
		mMoto.runVerOneTime(Moto.DIRECTION_UP);
		Log.i(TAG, "onClickUp");
	}
	
	public void onClickDown(View v)
	{
		mMoto.runVerOneTime(Moto.DIRECTION_DOWN);
		Log.i(TAG, "onClickDown");
	}
	
	public void onClickOk()
	{
		mMoto.adjustPostionFinish();
		Log.i(TAG, "onClickOk");
	}
	
	public class MHandle extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OCamera.GET_PICTURE:
				//Log.i(TAG, "get picture1");
				Bitmap bmp = (Bitmap) msg.obj;
				if (bmp == null || isDraw == true)
					return;
				//Log.i(TAG, "get picture2");
				isDraw = true;
				mPictureView.setImageBitmap(bmp);
		
				isDraw = false;
			}
		}
	}
}
