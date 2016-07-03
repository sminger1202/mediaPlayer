package com.youku.libmanager;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Profile;


public class SoUpgradeManager {

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ISoUpgradeService mSoUpgradeService;

    private SoUpgradeCallback mCallback;

    private boolean mStarted;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d(SoUpgradeService.TAG, "onServiceConnected");
            mSoUpgradeService = ISoUpgradeService.Stub.asInterface(service);
            try {
                mSoUpgradeService.registerCallback(mSoUpgradeCallback);
            } catch (RemoteException e) {
                Logger.e(SoUpgradeService.TAG, e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(SoUpgradeService.TAG, "onServiceDisconnected");
            if (mCallback != null && mStarted) {
                Logger.d(SoUpgradeService.TAG, "Service Disconnected, rebind and show failed.");
                bindService(Profile.mContext);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null)
                            mCallback.onDownloadFailed("");
                    }
                }, 1000);
            }

        }
    };

    private ISoUpgradeCallback.Stub mSoUpgradeCallback = new ISoUpgradeCallback.Stub() {
        @Override
        public void onDownloadEnd(final String soName) throws RemoteException {
            mStarted = false;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null)
                        mCallback.onDownloadEnd(soName);
                }
            });

        }

        @Override
        public void onDownloadFailed(final String soName) throws RemoteException {
            mStarted = false;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null)
                        mCallback.onDownloadFailed(soName);
                }
            });
        }
    };

    public static SoUpgradeManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        public static final SoUpgradeManager INSTANCE = new SoUpgradeManager();
    }

    public void bindService(Context context) {
        Intent intent = new Intent(context, SoUpgradeService.class);
        context.getApplicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void startDownloadSo(Context context, String soName) {
        mStarted = true;
        try {
            if (mSoUpgradeService != null) {
                mSoUpgradeService.startDownloadSo(soName);
            }
        } catch (RemoteException e) {
            Logger.e(SoUpgradeService.TAG, e);
        }
        Intent intent = new Intent(context, SoUpgradeService.class);
        intent.putExtra("flag", SoUpgradeService.FLAG_START_SERVICE_FROM_MANAGER);
        context.startService(intent);
    }

    public boolean isSoDownloaded(String soName) {
        try {
            if (mSoUpgradeService != null)
                return mSoUpgradeService.isSoDownloaded(soName);
        } catch (RemoteException e) {
            Logger.e(SoUpgradeService.TAG, e);
        }
        return false;
    }

    public void setSoUpgradeCallback(SoUpgradeCallback callback) {
        mCallback = callback;
    }
}
