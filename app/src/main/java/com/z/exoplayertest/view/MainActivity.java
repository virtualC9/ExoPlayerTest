package com.z.exoplayertest.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.tencent.stat.StatService;
import com.z.exoplayertest.BuildConfig;
import com.z.exoplayertest.R;
import com.z.exoplayertest.database.ChannelDao;

import java.io.IOException;
import java.util.Properties;

import androidx.appcompat.app.AppCompatActivity;

import static com.google.android.exoplayer2.ui.PlayerView.SHOW_BUFFERING_ALWAYS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DefaultTrackSelector trackSelector;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private Activity activity;
    private String url;
    private String tvName;
    private int isLike;
    private int id;
    /**
     * 保持屏幕常亮
     */
    private PowerManager.WakeLock mWakeLock;
    private ImageView ivFull;
    /**
     * 自适应高度
     */
    private boolean isWrapContent = false;
    private ImageView ivLike;
    private TextView tvError;
    private int like;
    private int width;
    private Properties prop;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setHalfTransparent();
        setFitSystemWindow(false);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        }

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        tvName = intent.getStringExtra("name");
        isLike = intent.getIntExtra("isLike", 0);
        id = intent.getIntExtra("id", 1);
        playerView = findViewById(R.id.player_view);
        tvError = findViewById(R.id.play_error);
        ivLike = findViewById(R.id.like);
        like = isLike;
        if (isLike == 1) {
            ivLike.setImageDrawable(getDrawable(R.mipmap.like));
        } else {
            ivLike.setImageDrawable(getDrawable(R.mipmap.unlike));
        }
        ivLike.setOnClickListener(this);
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        playerView.getLayoutParams().height = (width / 16) * 9;
        TextView name = findViewById(R.id.tv_name);
        name.setText(tvName);
        playerView.setShowBuffering(SHOW_BUFFERING_ALWAYS);
        ivFull = findViewById(R.id.full);
        ivFull.setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        if (player != null && player.isPlaying()) {
            player.stop(true);
            player.release();
            player = null;
        }
        playerView.getVideoSurfaceView().setKeepScreenOn(true);
        try {
            TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
            RenderersFactory renderersFactory = buildRenderersFactory(false);
            trackSelector = new DefaultTrackSelector(trackSelectionFactory);
            trackSelector.setParameters(new DefaultTrackSelector.ParametersBuilder().build());
            Uri uri = Uri.parse(url);
            player = ExoPlayerFactory.newSimpleInstance(activity, renderersFactory, trackSelector);
            player.setPlayWhenReady(true);
            player.addAnalyticsListener(new EventLogger(trackSelector));
            playerView.setPlayer(player);
            playerView.setPlaybackPreparer(new PlaybackPreparer() {
                @Override
                public void preparePlayback() {
                    if (tvError.getVisibility() == View.VISIBLE) {
                        tvError.setVisibility(View.GONE);
                    }
                    player.retry();
                }
            });
            player.addListener(eventListener);
            if (url.contains("rtmp")) {
                //rtmp://58.200.131.2:1935/livetv/cctv1hd
                DataSource.Factory rtmpDataSourceFactory = new RtmpDataSourceFactory();
                MediaSource rtmpMediaSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory).createMediaSource(uri);
                player.prepare(rtmpMediaSource, true, false);
            }else if (url.contains("m3u8")){
                //hls
//                https://cdn.letv-cdn.com/2018/12/05/JOCeEEUuoteFrjCg/playlist.m3u8
                //http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8
                DataSource.Factory hlsDataSourceFactory =
                        new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "MainActivity"));
                HlsMediaSource hlsMediaSource =
                        new HlsMediaSource.Factory(hlsDataSourceFactory).createMediaSource(uri);
                player.prepare(hlsMediaSource, true, false);
            }else {
                //普通网络地址
                //https://media.w3.org/2010/05/sintel/trailer.mp4
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "MainActivity"));
                MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
                player.prepare(mediaSource, true, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case ExoPlayer.STATE_ENDED:
                    //Stop playback and return to start position
                    break;
                case ExoPlayer.STATE_READY:
                    if (tvError.getVisibility() == View.VISIBLE) {
                        tvError.setVisibility(View.GONE);
                    }
                    if (!isWrapContent) {
                        //设置自适应高度
                        playerView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                        playerView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        isWrapContent = true;
                    }
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    break;
                case ExoPlayer.STATE_IDLE:
                    break;
                default:
            }
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                tvError.setVisibility(View.VISIBLE);
                IOException cause = error.getSourceException();
                if (cause instanceof HttpDataSource.HttpDataSourceException) {
                    // An HTTP error occurred.
                    HttpDataSource.HttpDataSourceException httpError = (HttpDataSource.HttpDataSourceException) cause;
                    // This is the request for which the error occurred.
                    DataSpec requestDataSpec = httpError.dataSpec;
                    // It's possible to find out more about the error both by casting and by
                    // querying the cause.
                    if (httpError instanceof HttpDataSource.InvalidResponseCodeException) {
                        // Cast to InvalidResponseCodeException and retrieve the response code,
                        // message and headers.
                    } else {
                        // Try calling httpError.getCause() to retrieve the underlying cause,
                        // although note that it may be null.
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop(true);
            player.release();
            player = null;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(like == isLike ? 100 : 200);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.stop(false);
        }
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    /**
     * Returns whether extension renderers should be used.
     */
    public boolean useExtensionRenderers() {
        return "withExtensions".equals(BuildConfig.FLAVOR);
    }

    public RenderersFactory buildRenderersFactory(boolean preferExtensionRenderer) {
        @DefaultRenderersFactory.ExtensionRendererMode
        int extensionRendererMode =
                useExtensionRenderers()
                        ? (preferExtensionRenderer
                        ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
        return new DefaultRenderersFactory(this)
                .setExtensionRendererMode(extensionRendererMode);
    }

    /**
     * 半透明状态栏
     */
    protected void setHalfTransparent() {

        if (Build.VERSION.SDK_INT >= 21) {
            //21表示5.0
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        } else if (Build.VERSION.SDK_INT >= 19) {
            //19表示4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * 如果需要内容紧贴着StatusBar
     * 应该在对应的xml布局文件中，设置根布局fitsSystemWindows=true。
     */
    private View contentViewGroup;

    protected void setFitSystemWindow(boolean fitSystemWindow) {
        if (contentViewGroup == null) {
            contentViewGroup = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        }
        contentViewGroup.setFitsSystemWindows(fitSystemWindow);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.like) {
            if (like == 1) {
                like = 0;
                ivLike.setImageDrawable(getDrawable(R.mipmap.unlike));
            } else {
                like = 1;
                ivLike.setImageDrawable(getDrawable(R.mipmap.like));
                statisticalLike(tvName);
            }
            ChannelDao.getInstance(this).update(new String[]{String.valueOf(like), String.valueOf(this.id)});
        } else if (id == R.id.full) {
            //获取设置的配置信息
            Configuration mConfiguration = getResources().getConfiguration();
            //获取屏幕方向
            int ori = mConfiguration.orientation;
            if (ori == Configuration.ORIENTATION_PORTRAIT) {
                //竖屏 强制为横屏
                if (!isWrapContent) {
                    playerView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    playerView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    isWrapContent = true;
                }
                ivFull.setVisibility(View.GONE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else if (id == R.id.back) {
            //获取设置的配置信息
            Configuration mConfiguration = getResources().getConfiguration();
            //获取屏幕方向
            int ori = mConfiguration.orientation;
            if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                //横屏 强制为竖屏
                ivFull.setVisibility(View.VISIBLE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                if (!player.isPlaying()){
                    playerView.getLayoutParams().height = (width / 16) * 9;
                    isWrapContent = false;
                }
            } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
                //竖屏 退出
                if (player != null) {
                    player.stop();
                    player.release();
                }
                setResult(like == isLike ? 100 : 200);
                finish();
            }
        }
    }
    /**
     * 统计用户收藏
     */
    private void statisticalLike(String name) {
        // 统计按钮状态
        prop = new Properties();
        prop.setProperty("电视台", name);
        StatService.trackCustomKVEvent(this, "收藏情况", prop);
    }
}
