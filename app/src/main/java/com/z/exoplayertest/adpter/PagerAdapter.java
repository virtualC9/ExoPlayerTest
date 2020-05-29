package com.z.exoplayertest.adpter;

import android.content.Context;

import com.z.exoplayertest.view.ChannelFragment;
import com.z.exoplayertest.view.LikeFragment;
import com.z.exoplayertest.view.SettingFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerAdapter  extends FragmentPagerAdapter {
    private static final int PAGE_COUNT = 3;

    public PagerAdapter(Context context, FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ChannelFragment.newInstance();
            case 1:
                return LikeFragment.newInstance();
            case 2:
                return SettingFragment.newInstance();
            default:
                return ChannelFragment.newInstance();
        }

    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "频道";
            case 1:
                return "收藏";
            case 2:
                return "设置";
            default:
                return "";
        }
    }
}