package com.oo.work;

import com.oo.camera.OCamera;
import com.oo.pwm.Moto;

import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class OWorkThread extends Thread {
	private static final String TAG = "MOVE";
	public final int DISTANCE_MOVE = 30;
	public final int MIDDLE_HORZONTAL = 400;
	public final int MIDDLE_VIRTICAL = 300;
	public final int DISTANCE_LEVEL1 = 100;
	public final int DISTANCE_LEVEL2 = 200;
	public final int DISTANCE_LEVEL3 = 300;
	public final int DEGREE_LEVEL1 = 10;
	public final int DEGREE_LEVEL2 = 15;
	public final int DEGREE_LEVEL3 = 20;
	private Moto mMoto;
	private OHandle mHandle;
	private boolean bWork = true, bContinue = false;
	private PointF mPoint;
	private boolean bFind = false;
	private long lTime = 0;

	public OWorkThread(Moto moto) {
		mMoto = moto;
		mHandle = new OHandle();
		synchronized (this) {
			bFind = false;
		}
		observe();

	}

	public Handler getHandle() {
		return mHandle;
	}

	public void setWork(boolean b) {
		bWork = b;
	}

	public void setContinue(boolean b) {
		bContinue = b;
	}

	@Override
	public void run() {
		while (bWork) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (bContinue) {
				if (mPoint == null)
					break;
				synchronized (this) {
					lTime = System.currentTimeMillis();
				}
				calcuteAndMove(mPoint);

				mPoint = null;
				
				synchronized (this) {
					bContinue = false;
				}
			}
		}
	}

	private int getDirection(int x, int y) {
		if (x == MIDDLE_HORZONTAL) {
			return x >= y ? Moto.DIRECTION_RIGHT : Moto.DIRECTION_LEFT;
		} else {
			return x >= y ? Moto.DIRECTION_UP : Moto.DIRECTION_DOWN;
		}
	}

	private int getDegree(int dis, int direction) {
		int degree = -1;
		if (dis >= DISTANCE_LEVEL3) {
			degree = DEGREE_LEVEL1;
		} else if (dis >= DISTANCE_LEVEL2) {
			degree = DEGREE_LEVEL2;
		} else if (dis >= DISTANCE_LEVEL1) {
			degree = DEGREE_LEVEL3;
		}

		if (direction == Moto.DIRECTION_UP || direction == Moto.DIRECTION_DOWN)
			degree /= 2;
		return degree;
	}

	private void calcuteAndMove(PointF point) {
		Log.i(TAG, "(x," + point.x + "," + point.y + ")");
		int hdirection = getDirection(MIDDLE_HORZONTAL, (int) point.x);
		int vdirection = getDirection(MIDDLE_VIRTICAL, (int) point.y);

		int h_dis = Math.abs((int) (MIDDLE_HORZONTAL - point.x));
		int v_dis = Math.abs((int) (MIDDLE_VIRTICAL - point.y));
		int hordegree = getDegree(h_dis, hdirection);
		int verdegree = getDegree(v_dis, vdirection);

		if (hordegree > 0) {
			mMoto.addHorDegree(hordegree, hdirection, false);
			Log.i(TAG, "h degree" + hordegree + " direction:" + hdirection);
		}

		if (verdegree > 0) {
			mMoto.addVerDegree(verdegree, vdirection, false);
			Log.i(TAG, "v degree" + hordegree + " direction:" + hdirection);
		}
	}


	private void waitFinish() throws InterruptedException {
		while (mMoto.getWorkState(Moto.GROUP_HORIZONTAL) == true || mMoto.getWorkState(Moto.GROUP_VERTICAL) == true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Thread.sleep(3000);
	}

	long last_time;

	private int findFaceAround() throws InterruptedException {
		Log.i(TAG, "enter");
		synchronized (this) {
			if (bFind) {
				Log.i(TAG, "Found Face");
				return 0;
			}	
		}
		
		mMoto.setHorDegree(Moto.H_INIT_POSITION, false);
		mMoto.setVerDegree(Moto.V_INIT_POSITION, false);
		waitFinish();
		synchronized (this) {
			if (bFind) {
				Log.i(TAG, "Found Face");
				return 0;
			}	
		}
	
		Log.i(TAG, "adjust to middle positon");
		int h_step = 40;
		int h_cnt = (Moto.H_LIMIT / 2) / (h_step);
		int v_step = (Moto.V_LIMIT / 2) / h_cnt;

		if (!bFind) {
			for (int i = 0; i < h_cnt; i++) {
				last_time = System.currentTimeMillis();
				waitFinish();
				mMoto.addVerDegree(v_step, Moto.DIRECTION_UP, false);
				mMoto.addHorDegree(h_step, Moto.DIRECTION_LEFT, false);
				synchronized (this) {
					if (bFind) {
						Log.i(TAG, "Found Face");
						return 0;
					}	
				}
						Log.i(TAG, "turn left and up,i:" + i + "time:" + (System.currentTimeMillis() - last_time));
				last_time = System.currentTimeMillis();
			}
			Thread.sleep(2000);
			for (int i = 0; i < h_cnt; i++) {
				waitFinish();
				mMoto.addVerDegree(v_step, Moto.DIRECTION_DOWN, false);
				mMoto.addHorDegree(h_step, Moto.DIRECTION_RIGHT, false);
				Log.i(TAG, "turn right and down,i:" + i);
				synchronized (this) {
					if (bFind) {
						Log.i(TAG, "Found Face");
						return 0;
					}	
				}
			
			}

			for (int i = 0; i < h_cnt; i++) {
				last_time = System.currentTimeMillis();
				waitFinish();
				mMoto.addVerDegree(v_step, Moto.DIRECTION_UP, false);
				mMoto.addHorDegree(h_step, Moto.DIRECTION_RIGHT, false);
				// waitFinish();

				Log.i(TAG, "turn right and up,i:" + i + "time:" + (System.currentTimeMillis() - last_time));
				last_time = System.currentTimeMillis();
				synchronized (this) {
					if (bFind) {
						Log.i(TAG, "Found Face");
						return 0;
					}	
				}
			
			}
			Thread.sleep(2000);
			for (int i = 0; i < h_cnt; i++) {
				waitFinish();
				mMoto.addVerDegree(v_step, Moto.DIRECTION_DOWN, false);
				mMoto.addHorDegree(h_step, Moto.DIRECTION_LEFT, false);
				Log.i(TAG, "turn left and down,i:" + i);
				if (bFind) {
					Log.i(TAG, "Found Face");
					return 0;
				}

			}
		}

		return 0;
	}

	public void observe() {

		Runnable r = new Runnable() {

			@Override
			public void run() {
				while (bWork) {
					// TODO Auto-generated method stub
					long now = System.currentTimeMillis();
					long spec = 0;
					synchronized (this) {
						spec = now - lTime;
					}

					if (spec > 1000 * 20) {
						synchronized (this) {
							bFind = false;
						}
					
						try {
							findFaceAround();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(21 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		new Thread(r).start();
	}

	public class OHandle extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OCamera.GET_POINT:
				mPoint = (PointF) msg.obj;
				synchronized (this) {
					bFind = true;
					bContinue = true;
				}
			
				Log.i(TAG, "get point");
			}
		}
	}
	
	public void onRelease() 
	{
		bWork = false;
		bContinue = false;
	}
}
