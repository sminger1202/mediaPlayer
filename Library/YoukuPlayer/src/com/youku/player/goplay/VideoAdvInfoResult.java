package com.youku.player.goplay;

public class VideoAdvInfoResult {
	
	private static String exceptionString = null;
	private static String responseString = null;

	public VideoAdvInfoResult(String mResoponseSring, String mExceptionString) {
		responseString = mResoponseSring;
		exceptionString = mExceptionString;
	}
	
	public VideoAdvInfoResult() {
		responseString = "";
		exceptionString = "";
	}

	public static String getExceptionString() {
		return exceptionString;
	}

	public static String getResponseString() {
		return responseString;
	}

	
}