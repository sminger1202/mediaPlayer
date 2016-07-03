package com.youku.local;

import java.lang.Thread.State;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.baseproject.utils.Logger;
import com.youku.thumbnailer.UThumbnailer;

import android.content.Context;

public class Thumbnailer implements Runnable {
	public final static String TAG = "Thumbnailer";

	private final Queue<Media> mItems = new LinkedList<Media>();

	private boolean isStopping = false;
	private final Lock lock = new ReentrantLock();
	private final Condition notEmpty = lock.newCondition();

	protected Thread mThread;
	private int totalCount;
	public static int mWidth = 120;
	public static int mHeight = 100;
	private Context mContext;

	public Thumbnailer(Context context) {
		mContext = context.getApplicationContext();
	}

	public void start() {

		isStopping = false;
		if (mThread == null || mThread.getState() == State.TERMINATED) {
			mThread = new Thread(this);
			mThread.start();
		}
	}

	public void stop() {
		isStopping = true;
		if (mThread != null && mThread.getState() == State.WAITING)
			mThread.interrupt();
		clearJobs();
	}

	/**
	 * Remove all the thumbnail jobs.
	 */
	public void clearJobs() {
		lock.lock();
		mItems.clear();
		totalCount = 0;
		lock.unlock();
	}

	/**
	 * Add a new id of the file browser item to create its thumbnail.
	 * 
	 * @param id
	 *            the if of the file browser item.
	 */
	public void addJob(Media item) {
		lock.lock();
		mItems.add(item);
		totalCount++;
		notEmpty.signal();
		lock.unlock();
		Logger.d(TAG, "Job added!");
	}

	/**
	 * Thread main function.
	 */
	@Override
	public void run() {
		int count = 0;
		int total = 0;

		Logger.d(TAG, "Thumbnailer started");

		while (!isStopping) {
			lock.lock();
			boolean interrupted = false;
			while (mItems.size() == 0) {
				try {
					totalCount = 0;
					notEmpty.await();
				} catch (InterruptedException e) {
					interrupted = true;
					Logger.e(TAG, "interruption probably requested by stop()");
					break;
				}
			}
			if (interrupted) {
				lock.unlock();
				break;
			}
			total = totalCount;
			final Media item = mItems.poll();
			lock.unlock();

			count++;
			if (Scanner.isUplayerSupported) {
				UThumbnailer.creatThumbnailFolder();
				UThumbnailer.genThumbnail(item.getLocation(),
						UThumbnailer.getThumailPath(item.getLocation()),
						"JPEG", mWidth, mHeight, 1, false);
			}
			Logger.d(TAG, "Thumbnail created for " + item.getTitle());
			if (Scanner.getInstance(mContext).getScanListener() != null
					&& Scanner.getInstance(mContext).getHandler() != null) {
				Scanner.getInstance(mContext).getHandler().post(new Runnable() {

					@Override
					public void run() {
						if (Scanner.getInstance(mContext).getScanListener() != null)
							Scanner.getInstance(mContext).getScanListener()
									.onThumbnailUpdate(item);
					}
				});

			}
		}
		Logger.d(TAG, "Thumbnailer stopped");
	}

	/**
	 * 设置缩略图尺寸
	 */
	public static void setThumbnailSize(int width, int height) {
		mWidth = width;
		mHeight = height;
	}
}
