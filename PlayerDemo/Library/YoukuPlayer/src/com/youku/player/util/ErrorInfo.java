package com.youku.player.util;

public class ErrorInfo{
	private String exceptionString = null;
	private String responseString = null;
	
	public ErrorInfo(String resoponseSring,String exceptionString){
		this.responseString = resoponseSring;
		this.exceptionString = exceptionString;
	}
	public ErrorInfo(){
		this.responseString = "";
		this.exceptionString = "";
	}
	public void addExceptionString(String exceptionStr) {
		exceptionString+=("\n******分割线**************\n"+exceptionStr);
	}

	public void addResponseString(String responseStr) {
		responseString+=("\n*******分割线*************\n"+responseStr);;
	}
	
	public String getExceptionString() {
		return exceptionString;
	}

	public String getResponseString() {
		return responseString;
	}	
	
}