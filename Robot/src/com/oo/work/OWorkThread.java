package com.oo.work;

import com.oo.camera.OCamera;
import com.oo.pwm.Moto;

import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;

public class OWorkThread extends Thread {
	public final int DISTANCE_MOVE = 30;
	public final int MIDDLE_HORZONTAL = 400;
	public final int MIDDLE_VIRTICAL = 300;
	public final int DISTANCE_LEVEL1 = 100;
	public final int DISTANCE_LEVEL2 = 200;
	public final int DISTANCE_LEVEL3 = 300;
	public final int DEGREE_LEVEL1 = 20;
	public final int DEGREE_LEVEL2 = 40;
	public final int DEGREE_LEVEL3 = 60;
	private Moto mMoto;

	private boolean bWork = true, bContinue = false;
	private PointF mPoint;

	public OWorkThread(Moto moto) {
		mMoto = moto;
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

				calcuteAndMove(mPoint);

				mPoint = null;
				bContinue = false;
			}
		}
	}

	private boolean getDirection(int x, int y) {
		return x >= y ? true : false;
	}

	private int getDegree(int dis) {
		int degree = -1;
		if (dis >= DISTANCE_LEVEL3) {
			degree = DEGREE_LEVEL1;
		} else if (dis >= DISTANCE_LEVEL2) {
			degree = DEGREE_LEVEL2;
		} else if (dis >= DISTANCE_LEVEL1) {
			degree = DEGREE_LEVEL3;
		}
		return degree;
	}

	private void calcuteAndMove(PointF point) {

		boolean hdirection = getDirection(MIDDLE_HORZONTAL, (int) point.x);
		boolean vdirection = getDirection(MIDDLE_VIRTICAL, (int) point.y);

		int h_dis = Math.abs((int) (MIDDLE_HORZONTAL - point.x));
		int v_dis = Math.abs((int) (MIDDLE_VIRTICAL - point.y));
		int hordegree = getDegree(h_dis);
		int verdegree = getDegree(v_dis);

		if (hordegree > 0) {
			mMoto.addHorDegree(hordegree, hdirection, false);
		}

		if (verdegree > 0) {
			mMoto.addVerDegree(verdegree, vdirection, false);
		}
	}

	public class OHandle extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OCamera.GET_POINT:
				mPoint = (PointF) msg.obj;
				bContinue = true;
			}
		}
	}
}
