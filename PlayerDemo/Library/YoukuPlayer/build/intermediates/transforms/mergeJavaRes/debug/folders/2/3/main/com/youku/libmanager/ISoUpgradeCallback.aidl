// ISoUpgradeCallback.aidl
package com.youku.libmanager;

// Declare any non-default types here with import statements

interface ISoUpgradeCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onDownloadEnd(String soName);

    void onDownloadFailed(String soName);
}
