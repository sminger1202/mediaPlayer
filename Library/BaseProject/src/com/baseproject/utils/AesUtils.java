/*
 * Copyright © 2012-2013 LiuZhongnan. All rights reserved.
 * 
 * Email:qq81595157@126.com
 * 
 * PROPRIETARY/CONFIDENTIAL.
 */

package com.baseproject.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

/**
 * AesUtils.AES解密工具
 * 
 * @author 刘仲男 qq81595157@126.com
 * @version v3.5
 * @created time 2013-12-4 下午1:16:02
 */
public class AesUtils {
	// 密钥算法
	private static final String ALGORITHM = "AES/ECB/NoPadding";

	/** 密钥 */
	private static final String PASSWORD = "qwer3as2jin4fdsa";

	/**
	 * aes 解密
	 * 
	 * @param content
	 * @param password
	 * @return
	 */
	public static byte[] decrypt(byte[] content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			byte[] raw = password.getBytes("utf-8");
			kgen.init(new SecureRandom(raw));
			SecretKeySpec key = new SecretKeySpec(raw, ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			return cipher.doFinal(content);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * aes 解密
	 */
	public static String decrypt(String str) {
		byte[] bytes = Base64.decode(str.getBytes(), Base64.DEFAULT);
		byte[] b = decrypt(bytes, PASSWORD);
		return new String(b);
	}
}
