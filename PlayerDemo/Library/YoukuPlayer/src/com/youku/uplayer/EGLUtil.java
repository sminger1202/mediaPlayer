package com.youku.uplayer;

import android.opengl.GLDebugHelper;
import android.view.SurfaceHolder;

import com.baseproject.utils.Logger;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


public class EGLUtil {

	private static EGL10 mEGL = null;
	private static EGLDisplay mEGLDisplay = EGL10.EGL_NO_DISPLAY;
	private static EGLConfig mEGLConfig = null;
	private static EGLSurface mEGLSurface = EGL10.EGL_NO_SURFACE;
	private static EGLContext mEGLContext = EGL10.EGL_NO_CONTEXT;
	
	private static SurfaceHolder mSHolder = null;
	
	private static String TAG = "EGLUtil";
	
	private static boolean inited = false;
	
	private static int mSurfaceWidth = 0;
	private static int mSurfaceHeight = 0;
	
	private static int[] mVersions = new int[2];

	private static int EGL_OPENGL_ES2_BIT = 4;
	private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
	
	public static String getClassPath() {
        String strClassName = null;
        
        strClassName = EGLUtil.class.getName();
        strClassName = strClassName.replace('.', '/');
        
        return strClassName;
    }
	public static void setSurfaceHolder(SurfaceHolder sh){
		mSHolder = sh;
	}
	public static boolean InitEGL(){
		
		mEGL = (EGL10)GLDebugHelper.wrap(EGLContext.getEGL(), GLDebugHelper.CONFIG_CHECK_GL_ERROR | GLDebugHelper.CONFIG_CHECK_THREAD, null);
		
		if(null == mEGL){
		    Logger.d(TAG, "EGLContext.getEGL failed");
			return false;
		}
		
		mEGLDisplay = mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		
		if(EGL10.EGL_NO_DISPLAY == mEGLDisplay){
		    Logger.d(TAG, "mEGL.eglGetDisplay failed");
			return false;			
		}
		
		if(!mEGL.eglInitialize(mEGLDisplay, mVersions)){
		    Logger.d(TAG, "mEGL.eglInitialize failed");
			return false;			
		}
		//配置设备属性
		int[] config_attribs = {
				/*
			EGL10.EGL_BLUE_SIZE,    	8,
			EGL10.EGL_GREEN_SIZE,	  	8,
			EGL10.EGL_RED_SIZE,     	8,
			*/
			EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
			EGL10.EGL_SURFACE_TYPE,    EGL10.EGL_WINDOW_BIT,
			EGL10.EGL_NONE
		};
		EGLConfig[] configs = new EGLConfig[1];
		int[] num_config = new int[1];
		
		if(!mEGL.eglChooseConfig(mEGLDisplay, config_attribs, configs, 1, num_config)){
			//LogOutput.log(TAG, "mEGL.eglChooseConfig failed");
			return false;			
		}
		
		mEGLConfig = configs[0];
		
		//初始化绘图表面属性
		if(null == mSHolder || !mSHolder.getSurface().isValid()){
			Logger.d(TAG, "mSHolder == null");
			return false;			
		}
		try{
		mEGLSurface = mEGL.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, mSHolder, null);
		}catch(Exception e){
			Logger.e(TAG, e);
		}
		if(EGL10.EGL_NO_SURFACE == mEGLSurface){
		    Logger.d(TAG, "mEGL.eglCreateWindowSurface failed");
			return false;
		}
		//获取绘图表面宽和高
		int[] surface_width = new int[1];
		int[] surface_height = new int[1];
		if(!mEGL.eglQuerySurface(mEGLDisplay, mEGLSurface, EGL10.EGL_WIDTH, surface_width) || !mEGL.eglQuerySurface(mEGLDisplay, mEGLSurface, EGL10.EGL_HEIGHT, surface_height)){
		    Logger.d(TAG, "mEGL.eglQuerySurface failed");
			return false;
		}
		
		mSurfaceWidth = surface_width[0];
		mSurfaceHeight = surface_height[0];
		
		//错误检测
		if(0 == mSurfaceWidth || 0 == mSurfaceHeight){
		    Logger.d(TAG, "0 == mSurfaceWidth || 0 == mSurfaceHeight");
			return false;
		}
		Logger.d(TAG, "mSurfaceWidth=" + Integer.toString(mSurfaceWidth) + ",mSurfaceHeight=" + Integer.toString(mSurfaceHeight));

		//初始化设备上下文
		int[] context_attrib = {
			EGL_CONTEXT_CLIENT_VERSION, 2,
			EGL10.EGL_NONE
		};
		mEGLContext = mEGL.eglCreateContext(mEGLDisplay, mEGLConfig, EGL10.EGL_NO_CONTEXT, context_attrib);
		
		if(EGL10.EGL_NO_CONTEXT == mEGLContext){
		    Logger.d(TAG, "mEGL.eglCreateContext failed");
			return false;
		}
		
		//关联上下文和绘图表面,作为当前绘图状态机和绘图目标
		if(!mEGL.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)){
		    Logger.d(TAG, "mEGL.eglMakeCurrent failed");
			return false;
		}
		
		inited = true;
		
		//LogOutput.log(TAG, "EGLUtil.InitEGL ok");
		
		return true;
	}

    public static void UpdateSurface(){
		if(inited)
             mEGL.eglSwapBuffers(mEGLDisplay, mEGLSurface);
    }

    public static int eglGetError(){
        if (inited)
            return mEGL.eglGetError();
        return 0;
    }

	public static void UpdateSize(int[] size){
		
		if(!inited)
			return;
		
		//获取绘图表面宽和高
		int[] surface_width = new int[1];
		int[] surface_height = new int[1];
		
		if(!mEGL.eglQuerySurface(mEGLDisplay, mEGLSurface, EGL10.EGL_WIDTH, surface_width) || !mEGL.eglQuerySurface(mEGLDisplay, mEGLSurface, EGL10.EGL_HEIGHT, surface_height)){
		    Logger.d(TAG, "mEGL.eglQuerySurface failed");
			return;
		}
		size[0] = mSurfaceWidth = surface_width[0];
		size[1] = mSurfaceHeight = surface_height[0];	
				
	}
	public static void ReleaseEGL(){
		
		if (mEGLDisplay != EGL10.EGL_NO_DISPLAY) {
			if(!mEGL.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)){
				//LogOutput.log(TAG, "mEGL.eglMakeCurrent failed");
			}
	        if (mEGLContext != EGL10.EGL_NO_CONTEXT) {
	        	//LogOutput.log(TAG, "mEGL.eglDestroyContext enter");
	            if(!mEGL.eglDestroyContext(mEGLDisplay, mEGLContext)){
	            	//LogOutput.log(TAG, "mEGL.eglDestroyContext failed");
	            }

	        }
	        if (mEGLSurface != EGL10.EGL_NO_SURFACE) {
	            if(!mEGL.eglDestroySurface(mEGLDisplay, mEGLSurface)){
	            	//LogOutput.log(TAG, "mEGL.eglDestroySurface failed");
	            }

	        }
	        if(!mEGL.eglTerminate(mEGLDisplay)){
	        	//LogOutput.log(TAG, "mEGL.eglTerminate failed");
	        }
	    }
		mEGLDisplay = EGL10.EGL_NO_DISPLAY;
		mEGLContext = EGL10.EGL_NO_CONTEXT;
		mEGLSurface = EGL10.EGL_NO_SURFACE;
		
		mEGL = null;
	}
}
