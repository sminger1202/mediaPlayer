package com.youku.player.network;

import android.content.Intent;

/**
 * 
 * @author 张宇
 * @create-time Oct 15, 2012 6:05:45 PM
 * @version $Id
 * 
 */
public class HttpIntent extends Intent {

	public static final String URI = "uri";

	public static final String METHOD = "method";

	public static final String IS_SET_COOKIE = "is_set_cookie";
	
	public static final String READ_TIMEOUT = "read_timeout";
	
	public static final String CONNECT_TIMEOUT = "connect_timeout";
	
	/**
	 * 是否缓存接口返回数据到本地
	 */
	public static final String IS_CACHE_DATA = "is_cache_data";
	
	/**
	 * 连接超时，读取超时
	 */
	public int connectTimeout, readTimeout;
	
	private Object parseObject;

	public HttpIntent(String uri) {
		this(uri, HttpRequestManager.METHOD_GET, false);
	}

	public HttpIntent(String uri, String reqMethod) {
		this(uri, reqMethod, false);
	}

	public HttpIntent(String uri, boolean isSetCookie) {
		this(uri, HttpRequestManager.METHOD_GET, isSetCookie);
	}

	public HttpIntent(String uri, String reqMethod, boolean isSetCookie) {
		putExtra(URI, uri);
		putExtra(METHOD, reqMethod);
		putExtra(IS_SET_COOKIE, isSetCookie);
		putExtra(IS_CACHE_DATA, true);
		putExtra(CONNECT_TIMEOUT, 15000);
		putExtra(READ_TIMEOUT, 20000);
	}
	
	

	/**
	 * @param connectTimeout the connectTimeout to set
	 */
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
		putExtra(CONNECT_TIMEOUT, connectTimeout);
	}

	/**
	 * @param readTimeout the readTimeout to set
	 */
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
		putExtra(READ_TIMEOUT, readTimeout);
	}

	public void putParseObject(Object o){
		parseObject = o;
	}
	
	public Object getParseObject(){
		return parseObject;
	}

}
