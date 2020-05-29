package com.z.exoplayertest.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.tencent.stat.StatService;
import com.z.exoplayertest.R;
import com.z.exoplayertest.adpter.ChannelAdaptr;
import com.z.exoplayertest.database.Channel;
import com.z.exoplayertest.database.ChannelDao;

import java.util.List;
import java.util.Properties;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * http://www.hdpfans.com/thread-834810-1-2.html
 * http://www.hdpfans.com/thread-820088-1-1.html
 * https://exoplayer.dev/hls.html
 */
public class ChannelFragment extends BaseFragment {

    private View view = null;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private EditText etQuery;
    private ImageView ivCancel;
    private ChannelAdaptr mAdapter;
    private List<Channel> channelList;
    private ChannelDao channelDao;
    private Properties prop;
    private OnRefreshListener onRefreshListener;
    public static ChannelFragment instance = null;

    public ChannelFragment() {
    }

    public static ChannelFragment newInstance() {
        if (instance == null) {
            instance = new ChannelFragment();
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
            initData();
            initView(view);
        }
        return view;
    }

    private void initView(View view) {
        etQuery = view.findViewById(R.id.query);
        ivCancel = view.findViewById(R.id.cancel);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuery.setText("");
            }
        });
        etQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s.toString())){
                    ivCancel.setVisibility(View.INVISIBLE);
                    ivCancel.setEnabled(false);
                    refresh();
                }else {
                    ivCancel.setVisibility(View.VISIBLE);
                    ivCancel.setEnabled(true);
                    List<Channel> list = channelDao.queryByName(s.toString());
                    channelList.clear();
                    channelList.addAll(list);
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mRecyclerView = view.findViewById(R.id.rv_main);
        mAdapter = new ChannelAdaptr(getContext(), channelList);
        mAdapter.setOnItemClickListener(new ChannelAdaptr.OnItemClickListener() {
            @Override
            public void onClickItem(int position, Channel channel, boolean isClickLike) {
                if (isClickLike) {
                    int like;
                    if (channel.getIsLike() == 1) {
                        like = 0;
                    } else {
                        like = 1;
                        statisticalLike(channelList.get(position).getName());
                    }
                    channelList.get(position).setIsLike(like);
                    if (channelDao != null) {
                        channelDao.update(new String[]{String.valueOf(like), String.valueOf(channel.getId())});
                    }
                    mAdapter.notifyDataSetChanged();
                    onRefreshListener.onRefresh();
                } else {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("name", channel.getName());
                    intent.putExtra("url", channel.getUrl());
                    intent.putExtra("id", channel.getId());
                    intent.putExtra("isLike", channel.getIsLike());
                    startActivityForResult(intent, 1000);
                    statistical(channel.getName());
                }
            }
        });
        mLayoutManager = new LinearLayoutManager(getContext());
        //设置布局管理器
        mRecyclerView.setLayoutManager(mLayoutManager);
        //设置adapter
        mRecyclerView.setAdapter(mAdapter);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(
//                getActivity(), DividerItemDecoration.VERTICAL));

    }

    /**
     * 统计用户播放
     */
    private void statistical(String name) {
        // 统计按钮状态
        prop = new Properties();
        prop.setProperty("电视台", name);
        StatService.trackCustomKVEvent(getContext(), "播放情况", prop);
    }

    /**
     * 统计用户收藏
     */
    private void statisticalLike(String name) {
        // 统计按钮状态
        prop = new Properties();
        prop.setProperty("电视台", name);
        StatService.trackCustomKVEvent(getContext(), "收藏情况", prop);
    }

    private void initData() {
        channelDao = ChannelDao.getInstance(getContext());
        channelList = channelDao.getAllData();
    }

    @Override
    public View setLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channel, container, false);
    }

    /**
     * 刷新
     */
    public void refresh() {
        List<Channel> list = channelDao.getAllData();
        channelList.clear();
        channelList.addAll(list);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    interface OnRefreshListener {
        void onRefresh();
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }
}