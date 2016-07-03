package com.youku.player.subtitle;

public class DownloadedSubtitle {
	
	public String name;
	public String fullName;
	public String lang;
	public String path;
	public int mode;
	
	
	public DownloadedSubtitle(String name, String lang, String path, int mode) {
		super();
		this.name = name;
		this.lang = lang;
		this.path = path;
		this.mode = mode;
	}
	
}
