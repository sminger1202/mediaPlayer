/*
 * @(#)Captcha.java	 2012-11-6
 *
 * Copyright 2005-2012 YOUKU.com
 * All rights reserved.
 * 
 * YOUKU.com PROPRIETARY/CONFIDENTIAL.
 */

package com.youku.player.module;



public class LanguageBean{
	public int id;
	public String code;
	public String desc;
	
	public LanguageBean(int lanid, String lanCode,String lanDesc){
		id = lanid;
		code = lanCode;
		desc=lanDesc;
	}
}
