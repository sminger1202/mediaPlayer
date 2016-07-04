/**
 * 文件名：RegisterCodec
 * 功能：该类声明了两个native注册函数
 * 作者：李庆燕
 * 创建时间：2012-02-08 
 * 修改：
 * 		2012-03-29	贾磊 	添加注释
 */
package com.youku.uplayer;

public class RegisterCodec {

	public RegisterCodec() throws Exception {
		native_avcodec_register_all();
		native_av_register_all();
	}

	private native void native_avcodec_register_all();

	private native void native_av_register_all();
}
