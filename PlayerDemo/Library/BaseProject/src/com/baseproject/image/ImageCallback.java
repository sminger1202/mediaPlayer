package com.baseproject.image;

import android.graphics.Bitmap;

/**
 *   图片实体得到后的回调
 *   @author          张宇 
 *   @create-time     Oct 29, 2012   11:44:55 AM   
 *   @version         $Id
 *
 */
public interface ImageCallback {
	public void imageLoaded(Bitmap bitmap, String imageUrl);
}
