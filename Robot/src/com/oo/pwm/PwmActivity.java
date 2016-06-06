package com.oo.pwm;



import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class PwmActivity extends Activity {
	private static final String TAG = "PWM";
	private Moto mMoto;
	private final int STEP = 444; 
	private boolean bT = false;
	private int HZ = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pwm);
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
}
