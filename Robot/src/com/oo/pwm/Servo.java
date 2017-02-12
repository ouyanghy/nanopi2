package com.oo.pwm;

public class Servo {
	private final int HZ = 50;
	private Pwm mPwm;
	public static final int CHANNEL_HOR = 0;
	public static final int CHANNEL_VER = 1;
	public Servo(){
		mPwm = new Pwm();
		
	}
	
	public void request()
	{
		mPwm.request();
	}
	
	public void free()
	{
		mPwm.free();
	}
	
	public void open()
	{
		mPwm.open();
		setFrequence();
	}
	
	public void close()
	{
		mPwm.free();
		mPwm.close();
	}
	
	private void setFrequence()
	{
		int hz = HZ;//T = 20ms
		mPwm.hz(hz);
	}
	
	//0,45,90,135,180
	public void setDegree(int channel, int degree)
	{
		if (degree < 0 ) degree = 0;
		else if (degree > 180) degree = 180;
		
		int div = degree/45;
		int duty_on = (div + 1)*500;//us
		int off = (duty_on * 4096)/ (20*1000);
		
		mPwm.config(channel, 0, off);
		
	}

}
