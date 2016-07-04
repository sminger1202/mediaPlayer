package com.decapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.baseproject.R;
import com.luajava.LuaState;
import com.luajava.LuaStateFactory;
import com.youku.analytics.data.Device;

public class DecAPI {

	private static LuaState mLuaState;

	private static String mTag = "BGMUSIC";

	static {
		mLuaState = LuaStateFactory.newLuaState();
		mLuaState.openLibs();
	}

	// ----------------------- public ------------------------------
	public DecAPI() {
	}

	public static String doDec(InputStream is, String ciphertxt) {
		mLuaState.LdoString(readStream(is));

		mLuaState.getField(LuaState.LUA_GLOBALSINDEX, "doDec");

		mLuaState.pushString(ciphertxt);
		mLuaState.pushNumber(ciphertxt.length());

		mLuaState.pcall(2, 1, 0);

		mLuaState.setField(LuaState.LUA_GLOBALSINDEX, "result");
		mLuaState.getGlobal("result");

		return mLuaState.toString(-1);
	}

	public static byte[] doEnc(InputStream is, String plaintxt, int flag) {
		mLuaState.LdoString(readStream(is));
		
		mLuaState.getField(LuaState.LUA_GLOBALSINDEX, "doEnc");
		
		mLuaState.pushString(plaintxt);
		mLuaState.pushInteger(flag);
		
		mLuaState.pcall(2, 1, 0);
		
		mLuaState.setField(LuaState.LUA_GLOBALSINDEX, "result");
		mLuaState.getGlobal("result");
		
		String ciphertxt = mLuaState.toString(-1);
		char[] cipher_c = ciphertxt.toCharArray();
		byte[] b = new byte[cipher_c.length];
		for(int i=0; i<cipher_c.length; i++) {
			b[i] = (byte)cipher_c[i];
		}
		return b;
	}

	// ----------------------- 改造 ------------------------------
	private static String lua;

	/** 初始化 */
	public static synchronized void init(Context context) {
		InputStream is = context.getResources().openRawResource(R.raw.aes);
		lua = readStream(is);
	}

	@SuppressLint("NewApi")
	public static String getEncreptUrl(String url, String fileId, String token,
			String oip, String sid, int flag) {
		StringBuilder s = new StringBuilder();
		s.append(url)
				.append("&oip=")
				.append(oip)
				.append("&sid=")
				.append(sid)
				.append("&token=")
				.append(token)
				.append("&did=")
				.append(Device.gdid)
				.append("&ev=1");
				if(flag==0)
					s.append("&ctype=20&ep=");
				else
					s.append("&ctype=64&ep=");
				s.append(URLEncoder.encode(Base64.encodeToString(
						DecAPI.doEnc(sid + "_" + fileId + "_" + token,flag),
						Base64.NO_WRAP)));
		return s.toString();
	}

	public static byte[] doEnc(String plaintxt, int flag) {
		mLuaState.LdoString(lua);

		mLuaState.getField(LuaState.LUA_GLOBALSINDEX, "doEnc");

		mLuaState.pushString(plaintxt);
		mLuaState.pushInteger(flag);
		
		mLuaState.pcall(2, 1, 0);

		mLuaState.setField(LuaState.LUA_GLOBALSINDEX, "result");
		mLuaState.getGlobal("result");

		String ciphertxt = mLuaState.toString(-1);
		char[] cipher_c = ciphertxt.toCharArray();
		byte[] b = new byte[cipher_c.length];
		for (int i = 0; i < cipher_c.length; i++) {
			b[i] = (byte) cipher_c[i];
		}
		return b;
	}

	// ------------------------ private -----------------------------

	private static String readStream(InputStream is) {
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			int i = is.read();
			while (i != -1) {
				bo.write(i);
				i = is.read();
			}
			return bo.toString();
		} catch (IOException e) {
			Log.e("LuaDemo", "Read file stream failed");
			return null;
		}
	}
	
	private static ExecutorService service = Executors.newSingleThreadExecutor();
	
	/**
	 * 加密方法在一个单线程中调用，会阻塞调用线程
	 */
	public static String getEncreyptStringInSingleThread(final InputStream is,
			final String plaintxt, final int flag) {
		Callable<String> call = new Callable<String>() {

			@Override
			public String call() throws Exception {
				return URLEncoder.encode(Base64.encodeToString(
						DecAPI.doEnc(is, plaintxt, flag), Base64.NO_WRAP));
			}
		};
		Future<String> future = service.submit(call);
		try {
			return future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
}
