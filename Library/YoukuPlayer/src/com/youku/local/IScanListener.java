package com.youku.local;

import java.util.List;

public interface IScanListener {
	/**
	 * 搜索开始
	 */
	void onScanStart();

	/**
	 * 搜索结束，list为搜索结果
	 * @param list
	 */
	void onScanStop(List<Media> list);

	/**
	 * 搜索过程中的当前数和总数
	 * @param count
	 * @param total
	 */
	void onItemAdded(int count, int total);

	/**
	 * 缩略图更新
	 * @param media
	 */
	void onThumbnailUpdate(Media media);
}
