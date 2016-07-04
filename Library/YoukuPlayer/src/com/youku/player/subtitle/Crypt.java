package com.youku.player.subtitle;

import java.util.Random;

import android.util.Base64;
import android.util.Log;

import com.baseproject.utils.Logger;
import com.youku.libmanager.MD5;

public class Crypt {

	public static String encryptKey;

	public static final String DEFAULT_KEY = "For the crack behavior, we regret!";

	public static byte[] intToByteArray(int i) {
		byte[] result = new byte[4];
		// 由高位到低位
		result[0] = (byte) ((i >> 24) & 0xFF);
		result[1] = (byte) ((i >> 16) & 0xFF);
		result[2] = (byte) ((i >> 8) & 0xFF);
		result[3] = (byte) (i & 0xFF);
		return result;
	}
	
	public static int generateRandom() {
		int max = 32000;
		int min = 0;
		
		Random random = new Random();
		int value = random.nextInt(max) % (max - min + 1) + min;
		
		return value;
	}

//	public static byte[] encode(String txt) {
//		
//		int random = generateRandom();
//		
//		encryptKey = MD5.md5(intToByteArray(random));
//		
//		int ctr = 0;
//		String tmp = "";
//		
//		for (int i  = 0; i < txt.length(); i++) {
//			if (ctr == encryptKey.length()) {
//				ctr = 0;
//			}
//			tmp += "" + encryptKey.charAt(ctr) + (char)((txt.charAt(i) ^ encryptKey.charAt(ctr)));
//			ctr++;
//		}
//		
//		return Base64.encode(encode_key(tmp, DEFAULT_KEY).getBytes(), Base64.DEFAULT);
//	}
	
	public static String decode(String str) {
		
		byte[] base64Bytes = android.util.Base64.decode(str, android.util.Base64.DEFAULT);
		
		byte[] txtBytes = encode_key(base64Bytes, DEFAULT_KEY);

		byte[] decoded = new byte[txtBytes.length / 2];
		
		if (txtBytes.length % 2 != 0)
			return "";

		for (int i = 0, j = 0; i < txtBytes.length - 1; j++, i += 2) {
			decoded[j] = (byte)(txtBytes[i] ^ txtBytes[i + 1]);
		}
		
		return new String(decoded);
	}
	
	private static byte[] encode_key(byte[] txtBytes, String encrypt_key) {
		byte[] keyBytes = new String(MD5.md5(encrypt_key.getBytes())).getBytes();
		
		byte[] tmpBytes = new byte[txtBytes.length];
		
		for (int i = 0, ctr = 0; i < txtBytes.length; i++ , ctr = (ctr + 1) % keyBytes.length) {
			tmpBytes[i] = (byte)(txtBytes[i] ^ keyBytes[ctr]);
		}
		
		return tmpBytes;
	}

}
