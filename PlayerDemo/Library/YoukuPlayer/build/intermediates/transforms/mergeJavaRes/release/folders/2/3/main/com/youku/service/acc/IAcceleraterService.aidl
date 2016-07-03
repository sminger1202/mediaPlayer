package com.youku.service.acc;

interface IAcceleraterService{
	void start();
	void stop();
	int getHttpProxyPort();
	String getAccPort();
	int pause();
	int resume();
	int isAvailable();
	boolean canDownloadWithP2p();
	boolean canPlayWithP2P();
	String getVersionName();
	int getVersionCode();
	boolean isACCEnable();
	boolean getDownloadSwitch();
	boolean getPlaySwitch();
	int getCurrentStatus();
}