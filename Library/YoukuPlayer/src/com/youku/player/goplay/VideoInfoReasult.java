package com.youku.player.goplay;

public class VideoInfoReasult {
	private static String exceptionString = null;
	private static String responseString = null;

	public VideoInfoReasult(String mResoponseSring, String mExceptionString) {
		responseString = mResoponseSring;
		exceptionString = mExceptionString;
	}

	public VideoInfoReasult() {
		responseString = "";
		exceptionString = "";
	}

	public void addExceptionString(String exceptionStr) {
		exceptionString += ("\n******分割线**************\n" + exceptionStr);
	}

	public void addResponseString(String responseStr) {
		responseString += ("\n*******分割线*************\n" + responseStr);
		;
	}

	public static String getExceptionString() {
		return exceptionString;
	}

	public static String getResponseString() {
		return responseString;
	}

}