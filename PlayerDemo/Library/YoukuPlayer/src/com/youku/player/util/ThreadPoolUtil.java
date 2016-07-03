/*
 *
 *  ? Copyright Ericsson AB 2007. All rights reserved.
 *
 *  Reproduction in whole or in part is prohibited without the written consent of the copyright owner.
 *
 *  The contents of this file are subject to revision without notice due to continued progress in
 *  methodology, design and manufacturing. Ericsson shall have no liability for any error or
 *  damage of any kind resulting from the use of this file.
 *
 *  ERICSSON MAKES NO REPRESENTATIONS OR WARRANTIES OF ANY NATURE
 *  WHATSOEVER (NEITHER EXPRESSED NOR IMPLIED) WITH RESPECT TO THE
 *  SOFTWARE, INCLUDING BUT NOT LIMITED TO, IMPLIED WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, INTERPRETABILITY,
 *  INTEROPERABILITY OR NON-INFRINGEMENT OF THIRD PARTY INTELLECTUAL
 *  PROPERTY RIGHTS OR ANY OTHER PROPRIETARY RIGHTS OF A THIRD PARTY,
 *  AND IN NO EVENT SHALL ERICSSON BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL AND OR CONSEQUENTIAL DAMAGES AND OR LOSS WHATSOEVER
 *  (INCLUDING BUT NOT LIMITED TO MONETARY LOSSES OR LOSS OF DATA)
 *  ARISING FROM USING, MODIFYING, REPRODUCING AND/OR DISTRIBUTING
 *  THIS SOFTWARE AND/OR ITS DERIVATIVES.
 *
 */
package com.youku.player.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {
	// private static final ThreadPoolUtil instance = new ThreadPoolUtil();
	private static ThreadPoolExecutor threadPool;
	private static ArrayBlockingQueue<Runnable> arrayBlockingQueue;

	private ThreadPoolUtil() {
		arrayBlockingQueue = new ArrayBlockingQueue<Runnable>(50);
		threadPool = new ThreadPoolExecutor(1,// the min size of thread pool
				1, // the max size of thread pool
				10, TimeUnit.SECONDS,// thread idle max time 3seconds
				arrayBlockingQueue,// the queue to store the task
				new ThreadFactory() {

					@Override
					public Thread newThread(Runnable r) {

						return new Thread(r, "T" + arrayBlockingQueue.size());
					}
				}, new ThreadPoolExecutor.DiscardPolicy()); // the default
															// policy

	}

	public static synchronized ThreadPoolExecutor getThreadPool() {
		if (threadPool == null) {
			new ThreadPoolUtil();
		}
		return threadPool;

	}

	public static void clearBlockingQueue2() {
		if ((arrayBlockingQueue != null) && (!arrayBlockingQueue.isEmpty())) {
			arrayBlockingQueue.clear();
		}
	}
}
