package com.youku.player.subtitle;

public class Attachment {
	// 语言类型
	public String lang;

	// 下载地址
	public String attrachmentUrl;

	// 附件类型
	public String type;

	public Attachment(String lang, String attrachmentUrl, String type) {
		this.lang = lang;
		this.attrachmentUrl = attrachmentUrl;
		this.type = type;
	}
	
}
