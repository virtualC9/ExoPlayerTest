package com.z.exoplayertest.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.z.exoplayertest.adpter.PagerAdapter;
import com.z.exoplayertest.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

public class SelectTvActivity extends AppCompatActivity{
    private TabLayout tablayout;
    private ViewPager viewpager;
    private String[] titles = {"频道", "收藏", "设置"};
    private int textMinWidth = 0;
    private int textMaxWidth = 0;
    private boolean isClickTab;
    private float mLastPositionOffsetSum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_tv);
        setStatusBarFullTransparent();
        setAndroidNativeLightStatusBar(this,true);
        setFitSystemWindow(false);
        tablayout = findViewById(R.id.tablayout);
        viewpager = findViewById(R.id.viewpager);
        viewpager.setAdapter(new PagerAdapter(this, getSupportFragmentManager()));
        tablayout.setupWithViewPager(viewpager);
        checkPermission();
        initSize();
        for (int i = 0; i < titles.length; i++) {
            TabLayout.Tab tab = tablayout.getTabAt(i);
            assert tab != null;
            //给tab自定义样式
            tab.setCustomView(R.layout.tab_item);
            assert tab.getCustomView() != null;
            AppCompatTextView textView = tab.getCustomView().findViewById(R.id.tab_text);
            textView.setText(titles[i]);
            AppCompatTextView compatTextView = ((AppCompatTextView) tab.getCustomView().findViewById(R.id.tab_text));
            if (i == 0) {
                //第一个tab被选中
                compatTextView.setSelected(true);
                compatTextView.setWidth(textMaxWidth);
                compatTextView.setTypeface(Typeface.DEFAULT_BOLD);
//                ((WaveView) tab.getCustomView().findViewById(R.id.wave)).setWaveWidth(textMaxWidth, true);
            } else {
                compatTextView.setWidth(textMinWidth);
//                ((WaveView) tab.getCustomView().findViewById(R.id.wave)).setWaveWidth(textMinWidth, false);
            }
        }

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // 当前总的偏移量
                float currentPositionOffsetSum = position + positionOffset;
                // 上次滑动的总偏移量大于此次滑动的总偏移量，页面从右向左进入(手指从右向左滑动)
                boolean rightToLeft = mLastPositionOffsetSum <= currentPositionOffsetSum;
                if (currentPositionOffsetSum == mLastPositionOffsetSum) {
                    return;
                }
                int enterPosition;
                int leavePosition;
                float percent;
                if (rightToLeft) {
                    // 从右向左滑
                    enterPosition = (positionOffset == 0.0f) ? position : position + 1;
                    leavePosition = enterPosition - 1;
                    percent = (positionOffset == 0.0f) ? 1.0f : positionOffset;
                } else {
                    // 从左向右滑
                    enterPosition = position;
                    leavePosition = position + 1;
                    percent = 1 - positionOffset;
                }
                if (!isClickTab) {
                    int width = (int) (textMinWidth + (textMaxWidth - textMinWidth) * (1 - percent));
                    ((AppCompatTextView) (tablayout.getTabAt(leavePosition).getCustomView().findViewById(R.id.tab_text)))
                            .setWidth(width);
                    ((AppCompatTextView) (tablayout.getTabAt(enterPosition).getCustomView().findViewById(R.id.tab_text)))
                            .setWidth((int) (textMinWidth + (textMaxWidth - textMinWidth) * percent));
                }

                mLastPositionOffsetSum = currentPositionOffsetSum;
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < 3; i++) {
                    TabLayout.Tab tab = tablayout.getTabAt(i);
                    assert tab != null;
                    if (i == position) {
                        ((AppCompatTextView) (tab.getCustomView().findViewById(R.id.tab_text))).setTypeface(Typeface.DEFAULT_BOLD);
//                        ((WaveView) tab.getCustomView().findViewById(R.id.wave)).setWaveWidth(textMaxWidth, true);
                    } else {
                        ((AppCompatTextView) (tab.getCustomView().findViewById(R.id.tab_text))).setTypeface(Typeface.DEFAULT);
//                        ((WaveView) tab.getCustomView().findViewById(R.id.wave)).setWaveWidth(textMinWidth, false);
                    }
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 0) {
                    isClickTab = false;
                }
            }
        });

        tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isClickTab = true;
                tab.getCustomView().findViewById(R.id.tab_text).setSelected(true);
                viewpager.setCurrentItem(tab.getPosition());
                ((AppCompatTextView) (tab.getCustomView().findViewById(R.id.tab_text))).setWidth(textMaxWidth);
//                ((WaveView) tab.getCustomView().findViewById(R.id.wave)).setWaveWidth(textMaxWidth, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getCustomView().findViewById(R.id.tab_text).setSelected(false);
                ((AppCompatTextView) (tab.getCustomView().findViewById(R.id.tab_text))).setWidth(textMinWidth);
//                ((WaveView) tab.getCustomView().findViewById(R.id.wave)).setWaveWidth(textMinWidth, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {


            }
        });
        viewpager.setCurrentItem(0);
        viewpager.setOffscreenPageLimit(2);
        ChannelFragment.newInstance().setOnRefreshListener(onRefreshLikeListener);
        LikeFragment.newInstance().setOnRefreshListener(onRefreshChannelListener);
        SettingFragment.newInstance().setOnRefreshListener(onRefreshListener);
    }

    private void initSize() {
        TextView tv = new TextView(this);
        tv.setTextSize(getResources().getDimension(R.dimen.title_no_selected));
        TextPaint textPaint = tv.getPaint();
        textMinWidth = (int) textPaint.measureText("频道");
        tv = new TextView(this);
        tv.setTextSize(getResources().getDimension(R.dimen.title_selected));
        textPaint = tv.getPaint();
        textMaxWidth = (int) textPaint.measureText("频道");
    }

    public void checkPermission() {
        boolean isGranted = true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //如果没有写sd卡权限
                isGranted = false;
            }
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
            if (!isGranted) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission
                                .ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        102);
            }
        }
    }

    /**
     * 全透状态栏
     */
    protected void setStatusBarFullTransparent() {
        if (Build.VERSION.SDK_INT >= 21) {
            //21表示5.0
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //19表示4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    private static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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

    /**
     * 刷新收藏页面
     */
    ChannelFragment.OnRefreshListener onRefreshLikeListener = new ChannelFragment.OnRefreshListener() {
        @Override
        public void onRefresh() {
            LikeFragment.newInstance().refresh();
        }
    };

    LikeFragment.OnRefreshListener onRefreshChannelListener = new LikeFragment.OnRefreshListener() {
        @Override
        public void onRefresh() {
            ChannelFragment.newInstance().refresh();
        }
    };

    SettingFragment.OnRefreshListener onRefreshListener = new SettingFragment.OnRefreshListener() {
        @Override
        public void onRefresh() {
            LikeFragment.newInstance().refresh();
            ChannelFragment.newInstance().refresh();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (resultCode==200){
                ChannelFragment.newInstance().refresh();
                LikeFragment.newInstance().refresh();
            }
    }
}
