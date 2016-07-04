package com.baseproject.image;

import android.content.Context;
import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 *
 * @author FengQve
 *
 */
public class ImageLoaderManager {

	private static ImageLoader mImageLoader;

	private static ImageLoaderConfiguration mImageLoaderConfiguration;

	private static DisplayImageOptions.Builder mDisplayImageOptionsBuilder;

	private static DisplayImageOptions mRoundPicOpt;

	public static final ImageLoader getInstance() {
		if (null == mImageLoader) {
			synchronized (ImageLoaderManager.class) {
				if (null == mImageLoader) {
					mImageLoader = ImageLoader.getInstance();
					initImageLoader();
				}
			}
		}
		return mImageLoader;
	}

	public static final DisplayImageOptions getRoundPicOpt() {
		mRoundPicOpt = new DisplayImageOptions.Builder()
				.resetViewBeforeLoading(true)
				.cacheOnDisk(true)
				.bitmapConfig(
						isMemoryLimited ? Bitmap.Config.RGB_565
								: Bitmap.Config.ARGB_8888)
				.imageScaleType(ImageScaleType.EXACTLY)
				.displayer(new RoundedBitmapDisplayer(540))
				.build();
		return mRoundPicOpt;
	}

	/**
	 * 内存是否紧张
	 */
	private static boolean isMemoryLimited;

	public static final void initImageLoaderConfiguration(Context context) {
		if (null == mImageLoaderConfiguration) {
			final int memoryCache = Utils.getMemoryClass(context);
			isMemoryLimited = memoryCache <= 64;
			final DisplayImageOptions displayImageOptions = getDisplayImageOptionsBuilder()
					.build();
			mImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(
					context)
					.threadPoolSize(isMemoryLimited ? 3 : 5)
							// default
					.threadPriority(Thread.NORM_PRIORITY - 1)
							// default
					.tasksProcessingOrder(QueueProcessingType.FIFO)
							// default
					.denyCacheImageMultipleSizesInMemory()
					.memoryCache(
							isMemoryLimited ? new WeakMemoryCache()
									: new LruMemoryCache(
									1024 * 1024 * memoryCache / 8))
							// default
					.diskCache(
							new UnlimitedDiscCache(StorageUtils
									.getCacheDirectory(context)))
							// default
					.diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
							// default
					.imageDownloader(new BaseImageDownloader(context))
							// default
					.defaultDisplayImageOptions(displayImageOptions)
					.build();
		}

	}

	public static final void initImageLoaderConfigurationTudou(Context context) {
		if (null == mImageLoaderConfiguration) {
			final DisplayImageOptions displayImageOptions = getDisplayImageOptionsBuilderTudou()
					.build();
			mImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(
					context)
					.threadPoolSize(3)
							// default
					.threadPriority(Thread.NORM_PRIORITY - 1)
							// default
					.tasksProcessingOrder(QueueProcessingType.FIFO)
							// default
					.denyCacheImageMultipleSizesInMemory()
					.memoryCache(
							new LruMemoryCache(1024 * 1024 * Utils
									.getMemoryClass(context) / 8))
							// default
					.diskCache(
							new UnlimitedDiscCache(StorageUtils
									.getCacheDirectory(context)))
							// default
					.diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
							// default
					.imageDownloader(new BaseImageDownloader(context))
							// default
					.defaultDisplayImageOptions(displayImageOptions).build();
		}

	}

	public static final DisplayImageOptions.Builder getDisplayImageOptionsBuilder() {
		if (null == mDisplayImageOptionsBuilder) {
			synchronized (ImageLoaderManager.class) {
				if (null == mDisplayImageOptionsBuilder) {
					mDisplayImageOptionsBuilder = new DisplayImageOptions.Builder();
					mDisplayImageOptionsBuilder
							.resetViewBeforeLoading(true)
							.cacheInMemory(true)
							.cacheOnDisk(true)
							.bitmapConfig(
									isMemoryLimited ? Bitmap.Config.RGB_565
											: Bitmap.Config.ARGB_8888)
							.imageScaleType(ImageScaleType.EXACTLY);
				}
			}
		}
		return mDisplayImageOptionsBuilder;
	}

	public static final DisplayImageOptions.Builder getDisplayImageOptionsBuilderTudou() {
		if (null == mDisplayImageOptionsBuilder) {
			synchronized (ImageLoaderManager.class) {
				if (null == mDisplayImageOptionsBuilder) {
					mDisplayImageOptionsBuilder = new DisplayImageOptions.Builder();
					mDisplayImageOptionsBuilder
							.resetViewBeforeLoading(true)
							.cacheInMemory(true)
							.cacheOnDisk(true)
							.bitmapConfig(
									isMemoryLimited ? Bitmap.Config.RGB_565
											: Bitmap.Config.ARGB_8888)
							.imageScaleType(ImageScaleType.EXACTLY)
							.displayer(new FadeInBitmapDisplayer(800,true,false,false));
				}
			}
		}
		return mDisplayImageOptionsBuilder;
	}

	public static final ImageLoaderConfiguration getImageLoaderConfiguration() {
		if (null == mImageLoaderConfiguration) {
			throw new IllegalArgumentException("没有初始化 ImageLoaderConfiguration");
		} else {
			return mImageLoaderConfiguration;
		}
	}

	public static final void initImageLoader() {
		ImageLoaderConfiguration config = getImageLoaderConfiguration();
		mImageLoader.init(config);
	}

}
