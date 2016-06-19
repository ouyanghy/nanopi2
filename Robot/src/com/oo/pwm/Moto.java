package com.oo.pwm;

import android.util.Log;

public class Moto {
	public final static int H_LIMIT = 300;
	public final static int V_LIMIT = 100;
	public final static int H_INIT_POSITION = 180;
	public final static int V_INIT_POSITION = 60;
	private final int ADJUST_STEP = 20;
	public final static int DIRECTION_LEFT = 0;
	public final static int DIRECTION_RIGHT = 1;
	public final static int DIRECTION_UP = 2;
	public final static int DIRECTION_DOWN = 3;
	public final static int GROUP_HORIZONTAL = 1;
	public final static int GROUP_VERTICAL = 0;
	private static final String TAG = "Moto";
	private Pwm mPwm;
	private int iFrequence = 200;
	private int iHorDegree = 180;
	private int iVerDegree = 60;

	private boolean bGroupState[] = new boolean[4];

	public Moto() {
		mPwm = new Pwm();
		mPwm.request();
		bGroupState[0] = false;
		bGroupState[1] = false;
		bGroupState[2] = false;
		bGroupState[3] = false;
	}

	public int open() {
		return mPwm.open();
	}

	public int close() {
		return mPwm.close();
	}

	public int request() {
		return mPwm.request();
	}

	public int free() {
		return mPwm.free();
	}

	public int setFrequence(int hz) {
		int ret = mPwm.hz(hz);
		if (ret == 0)
			iFrequence = hz;

		return ret;
	}

	public int getHorDegree() {
		return iHorDegree;
	}

	public int getVerDegree() {
		return iVerDegree;
	}

	public int getFrequence() {
		return iFrequence;
	}

	/*
	 * add degree
	 */
	private boolean addDegree(int grp, int degree, int direction) {
		if (degree == 0) {
				Log.i(TAG, "degree is zero");
				return true;
		}

		if (grp > 3)
			return false;
		synchronized (this) {
			if (bGroupState[grp]) {
				Log.e(TAG, "addDegree failed,device is busy");
				return false;
			}
		}

		new MotoThread(mPwm, grp, Math.abs(degree), iFrequence, direction, mCall).start();

		return true;
	}

	/*
	 * horizontal step
	 */
	public boolean addHorDegree(int degree, int direction, boolean force) {
		int calc = 0;
		if (direction == Moto.DIRECTION_LEFT)
			calc = iHorDegree - degree;
		else
			calc = iHorDegree + degree;

		if (!force) {
			if (calc < 0 || calc > H_LIMIT) {
				Log.i(TAG, "degree is out of bround,degree + iHorDegree" + calc);
				return false;
			}
		}

		boolean ret = addDegree(GROUP_HORIZONTAL, degree, direction);
		if (ret) {
			iHorDegree = calc;
		}
		return ret;
	}

	/*
	 * vertical step
	 */
	public boolean addVerDegree(int degree, int direction, boolean force) {
		int calc = 0;
		if (direction == Moto.DIRECTION_UP)
			calc = iVerDegree - degree;
		else
			calc = iVerDegree + degree;

		if (!force)
			if (calc < 0 || calc > V_LIMIT) {
				Log.i(TAG, "degree is out of bround,degree + iVerDegree:" + calc);
				return false;
			}
		boolean ret = addDegree(GROUP_VERTICAL, degree, direction);
		if (ret) {
			iVerDegree = calc;
		}
		return ret;
	}

	/*
	 * horizontal degree
	 */
	public boolean setHorDegree(int degree, boolean force) {
		if (!force)
			if (degree < 0 || degree > H_LIMIT)
				return false;

		int direction;
		boolean ret;
		int add = iHorDegree - degree;
		if (add > 0)
			direction = DIRECTION_LEFT;
		else
			direction = DIRECTION_RIGHT;

		ret = addDegree(GROUP_HORIZONTAL, add, direction);
		if (ret) {
			iHorDegree = degree;
		}
		return ret;
	}

	/*
	 * vertical degree
	 */
	public boolean setVerDegree(int degree, boolean force) {
		if (!force)
			if (degree < 0 || degree > V_LIMIT)
				return false;

		int direction;
		boolean ret;
		int add = iVerDegree - degree;
		if (add > 0)
			direction = DIRECTION_UP;
		else
			direction = DIRECTION_DOWN;

		ret = addDegree(GROUP_VERTICAL, add, direction);
		if (ret) {
			iVerDegree = degree;
		}
		return ret;
	}

	/*
	 * move to midlle position ensure your previous position is right
	 */
	public void setInitPostion() {
		setHorDegree(H_INIT_POSITION, true);
		setVerDegree(V_INIT_POSITION, true);
	}

	/*
	 * set init position after finsh adjust
	 */
	public void adjustPostionFinish() {
		iHorDegree = H_INIT_POSITION;
		iVerDegree = V_INIT_POSITION;
	}

	/*
	 * for adjust by your hand
	 */
	public void runHorOneTime(int direction) {
		int step;
		if (direction == DIRECTION_LEFT)
			step = ADJUST_STEP;
		else
			step = -ADJUST_STEP;

		addHorDegree(step, direction, true);
	}

	public void runVerOneTime(int direction) {
		int step;
		if (direction == DIRECTION_RIGHT)
			step = ADJUST_STEP;
		else
			step = -ADJUST_STEP;

		addVerDegree(step, direction, true);
	}

	/*
	 * tell me MotoThread work finish
	 */
	private MotoFinshCall mCall = new MotoFinshCall() {

		@Override
		public void setGrpState(int grp, boolean state) {
			// TODO Auto-generated method stub
			synchronized (this) {
				bGroupState[grp] = state;
			}

		}
	};

	
	public boolean getWorkState(int grp) {
	//	Log.i("MOVE", "grp:" + grp + " state:" + bGroupState[grp]);
		synchronized (this) {
			return bGroupState[grp];
		}

	}
}
