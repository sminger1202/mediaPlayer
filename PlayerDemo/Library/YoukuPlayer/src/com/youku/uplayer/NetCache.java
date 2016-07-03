package com.youku.uplayer;


public class NetCache {
    static {
        System.loadLibrary("netcache");
    }

    public static native int start(String path, long capacity);

    public static native void stop();

    public static native void DNSPreParse();

    public static native void SetUserAgent(String userAgent);

    public static native int memory_count();
    public static native void memory_dump();

}
