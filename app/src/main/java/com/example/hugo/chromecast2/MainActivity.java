package com.example.hugo.chromecast2;

import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.widgets.IntroductoryOverlay;

public class MainActivity extends AppCompatActivity {

    private VideoCastManager mCastManager;
    private VideoCastConsumerImpl mCastConsumer;
    private MenuItem mMediaRouteMenuItem;
    private MediaInfo mMediaInfo;

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

        BaseCastManager.checkGooglePlayServices(this);
        CastConfiguration options = new CastConfiguration. Builder(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
                .enableAutoReconnect()
                //. enableCaptionManagement()
                .enableDebug()
                .enableLockScreen()
                .enableWifiReconnection()
                .enableNotification()
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_PLAY_PAUSE, true)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_DISCONNECT, true)
                .build();
        VideoCastManager.initialize(this, options);
        mCastManager = VideoCastManager.getInstance();

        mMediaInfo = buildMediaInfo();
    }

    private MediaInfo buildMediaInfo()
    {
        MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_GENERIC);
        metadata.putString(MediaMetadata.KEY_SUBTITLE, "Subtitle");
        metadata.putString(MediaMetadata.KEY_TITLE, "Title");
        return new MediaInfo.Builder("http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8")
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType("video/mp4")
                .setMetadata(metadata)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        // INFO - Will display an overlay on first time cast button is shown. Doesn't seem to work
        mCastConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onCastAvailabilityChanged(boolean castPresent) {
                if (castPresent) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mMediaRouteMenuItem.isVisible()) {
                                showOverlay();
                            }
                        }
                    }, 1000);
                }
            }
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata,
                                               String sessionId, boolean wasLaunched)
            {
                try
                {
                    mCastManager.loadMedia(buildMediaInfo(), true, 0);
                }
                catch (Exception ex)
                {
                    // TODO do something
                }

            }
        };
        mCastManager.addVideoCastConsumer(mCastConsumer);
        return true;
    }

    private void showOverlay() {
        IntroductoryOverlay overlay = new IntroductoryOverlay.Builder(this)
                . setMenuItem(mMediaRouteMenuItem)
                . setTitleText(R.string.intro_overlay_text)
                . setSingleTime()
                . build();
        overlay.show();
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

    @Override
    public void onResume()
    {
        super.onResume();
        mCastManager = VideoCastManager.getInstance();
        mCastManager. incrementUiCounter();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mCastManager.decrementUiCounter();
    }
}
