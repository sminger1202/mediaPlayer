// ISoUpgradeService.aidl
package com.youku.libmanager;

import com.youku.libmanager.ISoUpgradeCallback;
// Declare any non-default types here with import statements

interface ISoUpgradeService {
    void startDownloadSo(String soName);

    boolean isSoDownloaded(String soName);

    void registerCallback(ISoUpgradeCallback callback);
}
