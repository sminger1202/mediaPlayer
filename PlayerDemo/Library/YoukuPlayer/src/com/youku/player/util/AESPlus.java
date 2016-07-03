/*
 * @(#)AESPlus.java	 2013-7-9
 *
 * Copyright 2005-2013 YOUKU.com
 * All rights reserved.
 * 
 * YOUKU.com PROPRIETARY/CONFIDENTIAL.
 */

package com.youku.player.util;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

import com.youku.player.goplay.Profile;


public class AESPlus {
	// 密钥算法
	public static String ALGORITHM = "AES/ECB/NoPadding";

	/**
	 * 转换密钥
	 * 
	 * @param key
	 *            二进制密钥
	 * @return Key 密钥
	 * @throws Exception
	 */
	private static Key toKey(byte[] key) throws Exception {
		// 实例化AES密钥材料
		SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
		return secretKey;
	}

	/**
	 * 加密
	 * 
	 * @param data
	 *            待加密数据
	 * @param key
	 *            密钥
	 * @return byte[] 加密数据
	 * @throws Exception
	 */
	private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		// 还原密钥
		Key k = toKey(key);
		// 实例化
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		// 初始化,设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);
		// 执行操作
		return cipher.doFinal(data);
	}

	/**
	 * 加密
	 * 
	 * @param data
	 *            待加密数据
	 * @param key
	 *            密钥
	 * @return byte[] 加密数据
	 * @throws Exception
	 */
	public static String encrypt(String data) {
		try {
			return Base64.encodeToString(encrypt(data.getBytes(), Profile.YOUCANGUESS.getBytes()),
					Base64.DEFAULT);
		} catch (Exception e) {
			return "";
		}
	}

}
