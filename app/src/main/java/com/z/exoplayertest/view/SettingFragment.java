package com.z.exoplayertest.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.stat.StatService;
import com.z.exoplayertest.R;
import com.z.exoplayertest.database.Channel;
import com.z.exoplayertest.database.ChannelDao;
import com.z.exoplayertest.utils.FileUtil;

import java.io.File;
import java.util.Properties;

import androidx.appcompat.app.AlertDialog;

public class SettingFragment extends BaseFragment implements View.OnClickListener {

    private View view = null;
    private LinearLayout llCreateChannel;
    private LinearLayout llReset;
    private LinearLayout llCheckUpdate;
    private LinearLayout llFeedback;
    private AlertDialog.Builder builder;
    private ChannelDao channelDao;
    private Properties prop;
    private OnRefreshListener onRefreshListener;
    public static SettingFragment instance = null;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        if (instance == null) {
            instance = new SettingFragment();
        }
        Bundle args = new Bundle();
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent) {
                parent.removeView(view);
            }
        } else {
            view = setLayoutView(inflater, container, savedInstanceState);
            initView(view);
        }
        return view;
    }

    private void initView(View view) {
        channelDao = ChannelDao.getInstance(getContext());
        llCreateChannel = view.findViewById(R.id.create_channel);
        llCreateChannel.setOnClickListener(this);
        llReset = view.findViewById(R.id.reset_channel);
        llReset.setOnClickListener(this);
        llCheckUpdate = view.findViewById(R.id.check_update);
        llCheckUpdate.setOnClickListener(this);
        llFeedback = view.findViewById(R.id.feedback);
        llFeedback.setOnClickListener(this);
        TextView tvAppName = view.findViewById(R.id.app_name);
        tvAppName.setText(getString(R.string.app_name) + " " + versionName(getContext()));
    }

    @Override
    public View setLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.create_channel) {
            showAddChannelDialog();
        } else if (id == R.id.reset_channel) {
            //重新读取文件中的数据
            showResetDialog();
        } else if (id == R.id.check_update) {
            Beta.checkUpgrade();
        } else if (id == R.id.feedback) {
//            CrashReport.testJavaCrash();
            showFeedbackDialog();
        }
    }

    public String versionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 添加频道弹窗
     */
    private void showAddChannelDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit, null);
        final EditText etName = view.findViewById(R.id.name);
        final EditText etUrl = view.findViewById(R.id.url);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setPositiveButton("添加", null).create();
        alertDialog.setTitle("添加频道");
        alertDialog.setIcon(R.mipmap.ic_launcher);
        alertDialog.setView(view);
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //为了避免点击 positive 按钮后直接关闭 dialog,把点击事件拿出来设置
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String name = etName.getText().toString();
                                String url = etUrl.getText().toString();
                                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(url)) {
                                    showToast("名称和链接不能为空");
                                    return;
                                }
                                Channel channel = new Channel(name, url, 1);
                                channelDao.add(channel);
                                FileUtil.writeFile(FileUtil.USER_CHANNEL_FILE_PATH, name + "&&" + url);
                                onRefreshListener.onRefresh();
                                showToast("频道添加成功");
                                alertDialog.dismiss();
                                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(etUrl.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                statisticalAdd(name + "&&" + url);
                            }
                        });
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(etUrl.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                dialog.dismiss();
            }
        });
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(R.color.black));
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(R.color.black));
        alertDialog.setCancelable(false);
    }

    /**
     * 反馈弹窗
     */
    private void showFeedbackDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_feedback, null);
        final EditText editText = view.findViewById(R.id.dialog_feedback);
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setPositiveButton("提交", null).create();
        alertDialog.setTitle("反馈与建议");
        alertDialog.setIcon(R.mipmap.ic_launcher);
        alertDialog.setView(view);
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                //为了避免点击 positive 按钮后直接关闭 dialog,把点击事件拿出来设置
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String str = editText.getText().toString();
                                if (str.length() < 5) {
                                    showToast("请输入五个字以上");
                                    return;
                                }
                                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                statisticalFeedBack(str);
                                alertDialog.dismiss();
                                showToast("感谢您的宝贵意见");
                            }
                        });
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                dialog.dismiss();
            }
        });
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(R.color.black));
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(R.color.black));
        alertDialog.setCancelable(false);
    }

    /**
     * 重置
     */
    private void showResetDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setPositiveButton("继续", null).create();
        alertDialog.setTitle("更新");
        alertDialog.setMessage("是否更新频道列表？");
        alertDialog.setIcon(R.mipmap.ic_launcher);
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                //为了避免点击 positive 按钮后直接关闭 dialog,把点击事件拿出来设置
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                channelDao.deleteAll();
                                channelDao.addList(FileUtil.getChannelFromTxt(getContext()));
                                onRefreshListener.onRefresh();
                                showToast("更新成功");
                                dialog.dismiss();
                            }
                        });
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(R.color.black));
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(R.color.black));
        alertDialog.setCancelable(false);
    }

    /**
     * 统计用户新增电视台
     */
    private void statisticalAdd(String name) {
        // 统计按钮状态
        prop = new Properties();
        prop.setProperty("电视台", name);
        StatService.trackCustomKVEvent(getContext(), "新增电视台", prop);
    }

    /**
     * 统计用户反馈
     */
    private void statisticalFeedBack(String value) {
        // 统计按钮状态
        prop = new Properties();
        prop.setProperty("内容", value);
        StatService.trackCustomKVEvent(getContext(), "反馈", prop);
    }

    interface OnRefreshListener {
        void onRefresh();
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }
}