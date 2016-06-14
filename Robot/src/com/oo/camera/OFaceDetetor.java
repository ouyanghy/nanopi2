package com.oo.camera;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.util.Log;

public class OFaceDetetor {
	private static final String TAG = "OFACE";
	private FaceDetector mFaceDetetor;
	private int mWidth, mHeight;
	private Face [] mFaces;
	public OFaceDetetor(int width, int height, int maxFaces) 
	{
		
		mWidth = width;
		mHeight = height;
		mFaces = new Face[maxFaces];
		mFaceDetetor = new FaceDetector(mWidth, mHeight, maxFaces);
		
		//Log.i(TAG, "init width:" +mWidth + " height:" +mHeight);
	}
	
	public PointF findFace(Bitmap bmp)
	{
		if (bmp == null) {
			Log.i(TAG, "null ptr");
			return null;
		}
		//Log.i(TAG, "width:" +bmp.getWidth() + " height:" + bmp.getHeight());
		//Log.i("CTIME", "====================================================");
		//long time = System.currentTimeMillis();
		//Log.i("CTIME ", "enter  :" + (System.currentTimeMillis() - time));
		
		//time = System.currentTimeMillis();
		Bitmap conv = convert(bmp, Config.RGB_565);
		//Log.i("CTIME ", "convert:" + (System.currentTimeMillis() - time));
		//time = System.currentTimeMillis();
		
		int number = mFaceDetetor.findFaces(conv, mFaces);
		//Log.i("CTIME ", "find   :" + (System.currentTimeMillis() - time));
		//time = System.currentTimeMillis();
		if (number > 0) {
			PointF p = drawEye(bmp, mFaces);
		//	Log.i("CTIME ", "draw   :" + (System.currentTimeMillis() - time));
		//	time = System.currentTimeMillis();
			return p;
		}
		Log.i(TAG, "face:" + number);
		return null;
	}
	
	private PointF drawEye(Bitmap bmp, Face[] faces) {
		// TODO Auto-generated method stub
		Face face = faces[0];
		Point left = new Point();
		Point right = new Point();
		int distance;
		PointF middle = new PointF();
		face.getMidPoint(middle);
		distance = (int) face.eyesDistance()/2;
		left.x = (int) (middle.x - distance);
		right.x = (int) (middle.x + distance);
		left.y = (int) middle.y;
		right.y = (int) middle.y;
		
		Canvas canvas = new Canvas(bmp);
		
		canvas.drawCircle(left.x, left.y, 8, new Paint(Color.RED));
		canvas.drawCircle(right.x, right.y, 8, new Paint(Color.RED));
		return middle;
	}

	private Bitmap convert(Bitmap bitmap, Bitmap.Config config) {
	    Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
	    Canvas canvas = new Canvas(convertedBitmap);
	    Paint paint = new Paint();
	 //   paint.setColor(Color.BLACK);
	    canvas.drawBitmap(bitmap, 0, 0, paint);
	    return convertedBitmap;
	}
}
