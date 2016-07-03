package com.android.playerdemo;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.baseproject.image.Utils;
import com.baseproject.utils.Profile;
import com.youku.player.base.Plantform;
import com.youku.player.base.YoukuBasePlayerActivity;
import com.youku.player.base.YoukuPlayer;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.plugin.MediaPlayerDelegate;

public class MainActivity extends YoukuBasePlayerActivity {

    private MediaPlayerDelegate mediaPlayerDelegate;
    private YoukuPlayerView mYoukuPlayerView;
    private YoukuPlayer mYoukuPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mYoukuPlayerView = (YoukuPlayerView) findViewById(R.id.player_view);
        mYoukuPlayerView.initialize(this, Plantform.TUDOU,
                Profile.getPid(), Profile.VER,
                /*TudouLiteApplication.User_Agent,*/ "TudouLite;" + Utils.getVersionName(getApplicationContext()) + ";Android;" + Build.VERSION.RELEASE + ";" + Build.MODEL,
                false, Profile.TIMESTAMP,
                Profile.NEWSECRET);
    }

    @Override
    public void onInitializationSuccess(YoukuPlayer player) {
        try {
            super.onInitializationSuccess(player);
            mYoukuPlayer = player;
            player.getPlayerUiControl().setScreenChangeListener(this);
            mediaPlayerDelegate = player.getPlayerUiControl().getMediaPlayerDelegate();
//            mPluginSmall = new PluginSmall(mActivity, mediaPlayerDelegate, mYoukuPlayerView);
//            mPluginSmall.setDataHelper(mPlayerDataHelper);
//            mPluginFullscreen = new PluginFullScreen(mActivity, mediaPlayerDelegate, mYoukuPlayerView);
//            mPluginFullscreen.setDataHelper(mPlayerDataHelper);
//            this.setmPluginSmallScreenPlay(mPluginSmall);
//            this.setmPluginFullScreenPlay(mPluginFullscreen);
            this.addPlugins();
            PlayVideoInfo playVideoInfo = null;
            playVideoInfo = new PlayVideoInfo.Builder("ooOEF1ZE1vM").setPlaylistId(null)
                    .setPassword(null).setCache(false).setNoAdv(true).setCache(false)
                    .setTudouAlbum(false).setFromYouku(false).setPoint(0)
                    .setLanguageCode(null).setFullScreen(false).build();
            mYoukuPlayer.playVideo(playVideoInfo);
        } catch (Exception e) {
        }
    }

    @Override
    public void setPadHorizontalLayout() {

    }

    @Override
    public void onFullscreenListener() {

    }

    @Override
    public void onSmallscreenListener() {

    }

    @Override
    public void onGoSmall() {

    }

    @Override
    public void onGoFull() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
