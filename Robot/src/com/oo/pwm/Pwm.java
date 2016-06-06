package com.oo.pwm;

public class Pwm {
	static {
		System.loadLibrary("Pwm");
	}
	public native int open();
	public native int close();	
	public native int unconfig(int channel);

	public native int request();
	public native int free();
	public native int config(int channel, int on, int off);
	public native int hz(int hz);
}
