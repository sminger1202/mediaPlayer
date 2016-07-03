package com.decapi;

public class Decryptions {

	static {
		System.loadLibrary("algms");
	}
	
	private static native byte[] nativeAESEnc(String plaintxt, String key);
	private static native String nativeAESDec(byte[] ciphertxt, int len, String key);
	private static native String nativeDESEnc(String plaintxt, String key);
	private static native String nativeDESDec(String ciphertxt, int len, String key);
	
	public String AESEnc(String plaintxt, String key)
	{
		byte[] b =  nativeAESEnc(plaintxt, key);
		char[] c = new char[b.length];
		for(int i = 0; i < b.length; i++)
			c[i] = (char)b[i];
		String ret = String.valueOf(c);
		return ret;
	}
	
	public String AESDec(String ciphertxt, int len, String key)
	{
		byte[] b = new byte[ciphertxt.length()];
		char[] c = ciphertxt.toCharArray();
		for(int i = 0; i < b.length; i++) {
			b[i] = (byte)c[i];
		}
		return nativeAESDec(b, len, key);
	}
	
	private String DESEnc(String plaintxt, String key)
	{
		return nativeDESEnc(plaintxt, key);
	}
	
	private String DESDec(String ciphertxt, int len, String key)
	{
		return nativeDESDec(ciphertxt, len, key);
	}
}
