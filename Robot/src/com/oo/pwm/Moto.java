package com.oo.pwm;

public class Moto {
	private final int H_LIMIT = 180;
	private final int V_LIMIT = 180;
	private final int H_INIT_POSITION = 90;
	private final int V_INIT_POSITION = 90;
	private final int ADJUST_STEP = 30;
	public final static boolean DIRECTION_LEFT = true;
	public final static boolean DIRECTION_RIGHT = false;
	public final static boolean DIRECTION_UP = true;
	public final static boolean DIRECTION_DOWN = false;
	public final static int GROUP_HORIZONTAL = 1;
	public final static int GROUP_VERTICAL = 0;
	private Pwm mPwm;
	private int iFrequence = 200;
	private int iHorDegree;
	private int iVerDegree;

	private boolean bGroupState[] = new boolean[4];
	
	public Moto( ) {
		mPwm = new Pwm();
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
	private boolean addDegree(int grp, int degree, boolean direction) {
		if (grp > 3)
			return false;
		
		if (bGroupState[grp])
			return false;
		
		new MotoThread(mPwm, grp, Math.abs(degree), iFrequence, direction, mCall).start();
		
		return true;
	}
	/*
	 * horizontal step 
	 */
	public boolean addHorDegree(int degree, boolean direction, boolean force)
	{
		if (!force)
			if (degree + iVerDegree < 0 || (degree + iHorDegree) > H_LIMIT)
				return false;
		
		return addDegree(GROUP_HORIZONTAL, degree, direction);
	}
	/*
	 * vertical step 
	 */
	public boolean addVerDegree(int degree, boolean direction, boolean force)
	{
		if (!force)
			if (degree + iVerDegree < 0 || (degree + iVerDegree) > V_LIMIT)
				return false;
		
		return addDegree(GROUP_VERTICAL, degree, direction);
	}
	
	/*
	 * horizontal degree
	 */
	public boolean setHorDegree(int degree, boolean force) {
		if (!force)
			if (degree < 0 || degree > H_LIMIT)
				return false;

		boolean direction;
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

		boolean direction;
		boolean ret;
		int add = iVerDegree - degree;
		if (add > 0)
			direction = DIRECTION_UP;
		else
			direction = DIRECTION_DOWN;

		ret = addDegree(GROUP_VERTICAL ,add, direction);
		if (ret) {
			iVerDegree = degree;
		}
		return ret;
	}

	/*
	 * move to midlle position
	 * ensure your previous position is right 
	 */
	public void setInitPostion( ) {
		setHorDegree( H_INIT_POSITION, true);
		setVerDegree( V_INIT_POSITION, true);
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
	public void runHorOneTime(boolean direction) {
		int step;
		if (direction)
			step = ADJUST_STEP;
		else
			step = -ADJUST_STEP;

		addHorDegree(step, direction, true);
	}

	public void runVerOneTime(boolean direction) {
		int step;
		if (direction)
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
			bGroupState[grp] = state;
		}
	};
}
