package com.youku.player.apiservice;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.KeyEvent;

public interface ActivityCallback {
    void onCreate();

    void onPause();

    void onResume();

    void onStart();

    void onStop();

    void onDestroy();

    void onConfigurationChanged(Configuration newConfig);

    void onNewIntent(Intent intent);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    boolean onSearchRequested();

    public void onLowMemory();

    public void onBackPressed();

    public boolean onKeyDown(int keyCode, KeyEvent event);
}
