package com.oo.pwm;

import android.util.Log;

public class MotoThread extends Thread{
	private static final String TAG = "MotoThread";
	private Pwm mPwm;
	private MotoFinshCall mCall;
	private int iDegree;
	private int iDirection;
	private int iFrequence;
	private int iGrp;
	private final int STEP_TOTAL = 4096;
	private final int STEP = STEP_TOTAL/9; 
	private final double STEP_ANGLE = 5.625/ 64;
	private final double GEAR_PRECENT = 38.0/48.0;
	public MotoThread(Pwm pwm, int grp, int addDegree, int frequence, int direction, MotoFinshCall call) {
		// TODO Auto-generated constructor stub
		mPwm = pwm;
		iDegree = addDegree;
		iDirection = direction;
		iFrequence = frequence;
		iGrp = grp;
		mCall = call;
		mCall.setGrpState(iGrp, true);
	}

	@Override
	public void run() {
	
		
		if (iDirection == Moto.DIRECTION_RIGHT || iDirection == Moto.DIRECTION_UP) {
			mPwm.config(iGrp * 4 	, STEP * 0, STEP * 3 - 1);
			mPwm.config(iGrp * 4 + 1, STEP * 2, STEP * 5 - 1);
			mPwm.config(iGrp * 4 + 2, STEP * 4, STEP * 7 - 1);
			mPwm.config(iGrp * 4 + 3, STEP * 6, STEP * 9 - 1);
		} else {
			mPwm.config(iGrp * 4 + 3, STEP * 0, STEP * 3 - 1);
			mPwm.config(iGrp * 4 + 2, STEP * 2, STEP * 5 - 1);
			mPwm.config(iGrp * 4 + 1, STEP * 4, STEP * 7 - 1);
			mPwm.config(iGrp * 4 + 0, STEP * 6, STEP * 9 - 1);
		}
		int time = calcuteTime();
		try {
			sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mPwm.unconfig(iGrp * 4);
		mPwm.unconfig(iGrp * 4 + 1);
		mPwm.unconfig(iGrp * 4 + 2);
		mPwm.unconfig(iGrp * 4 + 3);
		mCall.setGrpState(iGrp, false);
	}
	
	/*
	 * 9 * (5.625/64) * (38/48) ==> 1/(int)T
	 * degree         ===>x
	 */
	private int calcuteTime()
	{
		double cycleAngle = 9 * STEP_ANGLE * GEAR_PRECENT;
	//	Log.i(TAG, "STEP_ANGLE:" + STEP_ANGLE + " GEAR_PRECENT:" + GEAR_PRECENT);
		if (cycleAngle == 0) {
			Log.i(TAG, "caclute error");
			return 0;
		}
	//	Log.i(TAG, "iDegree:" + iDegree + " iFrequeneceL" + iFrequence + " cycle:" + cycleAngle);
		int time = (int) ((1000 * iDegree/iFrequence)/cycleAngle);
		Log.i(TAG, "time:" + time);
		return time;
	}

}
