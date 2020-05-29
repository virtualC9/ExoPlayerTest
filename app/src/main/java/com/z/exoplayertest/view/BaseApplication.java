package com.z.exoplayertest.view;

import android.app.Application;
import android.content.Context;

import com.tencent.bugly.Bugly;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;
import com.tencent.tinker.entry.ApplicationLike;
import com.tinkerpatch.sdk.TinkerPatch;
import com.tinkerpatch.sdk.loader.TinkerPatchApplicationLike;
import com.z.exoplayertest.BuildConfig;
import com.z.exoplayertest.database.Channel;
import com.z.exoplayertest.database.ChannelDao;
import com.z.exoplayertest.utils.Density;
import com.z.exoplayertest.utils.FileUtil;

import java.util.List;

public class BaseApplication extends Application {
    private ApplicationLike tinkerApplicationLike;
    @Override
    public void onCreate() {
        super.onCreate();
        Bugly.init(getApplicationContext(), "ff995c6793", false);
        Density.setDensity(this);
        ChannelDao channelDao = ChannelDao.getInstance(this);
        List<Channel> list = channelDao.getAllData();
        if (list == null || list.size() < 1) {
            channelDao.addList(FileUtil.getChannelFromTxt(this));
        }
        // [可选]设置是否打开debug输出，上线时请关闭，Logcat标签为"MtaSDK"
        StatConfig.setDebugEnable(false);
        // 基础统计API
        StatService.registerActivityLifecycleCallbacks(this);

        if (BuildConfig.TINKER_ENABLE) {
            // 我们可以从这里获得Tinker加载过程的信息
            tinkerApplicationLike = TinkerPatchApplicationLike.getTinkerPatchApplicationLike();

            // 初始化TinkerPatch SDK, 更多配置可参照API章节中的,初始化SDK
            TinkerPatch.init(tinkerApplicationLike)
                    .reflectPatchLibrary()
                    .setPatchRollbackOnScreenOff(true)
                    .setPatchRestartOnSrceenOff(true)
                    .setFetchPatchIntervalByHours(3);

            // 每隔3个小时(通过setFetchPatchIntervalByHours设置)去访问后台时候有更新,通过handler实现轮训的效果
            TinkerPatch.with().fetchPatchUpdateAndPollWithInterval();
        }
    }
}