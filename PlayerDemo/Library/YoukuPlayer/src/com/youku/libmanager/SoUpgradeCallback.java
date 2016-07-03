package com.youku.libmanager;

public interface SoUpgradeCallback {
    void onDownloadEnd(String soName);

    void onDownloadFailed(String soName);
}
