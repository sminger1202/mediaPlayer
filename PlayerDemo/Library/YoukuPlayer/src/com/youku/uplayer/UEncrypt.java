package com.youku.uplayer;

public class UEncrypt {

	static {
		System.loadLibrary("uencrypt");
	}
	
	/**
	 * 获得加密信息
	 * @param headerInfo
	 * @param version
	 * @param format
	 */
	public static void getEncryptHeaderInfo(EncryptHeaderInfo headerInfo, int version, int format){
		get_encrypt_header_info(headerInfo, version, format);
	}
	
	public static void freeHeader(){
		free_header();
	}
	
	/**
	 * 
	 * @param headerInfo
	 * @param version 加密的版本
	 * @param format 实际格式
	 */
	private static native void get_encrypt_header_info(EncryptHeaderInfo headerInfo, int version, int format);
	
	/**
	 * 
	 */
	private static native void free_header();
}
