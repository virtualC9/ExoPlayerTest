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

import com.z.exoplayertest.R;
import com.z.exoplayertest.adpter.ChannelAdaptr;
import com.z.exoplayertest.adpter.LikeAdaptr;
import com.z.exoplayertest.database.Channel;
import com.z.exoplayertest.database.ChannelDao;

import java.util.List;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LikeFragment extends BaseFragment {

    private View view = null;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private EditText etQuery;
    private ImageView ivCancel;
    private LikeAdaptr mAdapter;
    private List<Channel> channelList;
    private ChannelDao channelDao;
    OnRefreshListener onRefreshListener;
    public static LikeFragment instance = null;

    public LikeFragment() {
    }

    public static LikeFragment newInstance() {
        if (instance==null){
            instance = new LikeFragment();
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
                    List<Channel> list = channelDao.queryByNameAndLike(s.toString());
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
        mAdapter = new LikeAdaptr(getContext(), channelList);
        mAdapter.setOnItemClickListener(new LikeAdaptr.OnItemClickListener() {
            @Override
            public void onClickItem(int position, Channel channel, boolean isClickLike) {
                if (isClickLike) {
                    if (channelDao!=null){
                        channelDao.update(new String[]{"0",String.valueOf(channel.getId())});
                    }
                    channelList.remove(position);
                    mAdapter.notifyDataSetChanged();
                    onRefreshListener.onRefresh();
                } else {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("name", channel.getName());
                    intent.putExtra("url", channel.getUrl());
                    intent.putExtra("id", channel.getId());
                    intent.putExtra("isLike", channel.getIsLike());
                    startActivityForResult(intent,1000);
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

    private void initData() {
        channelDao = ChannelDao.getInstance(getContext());
        channelList = channelDao.queryUserLike();
    }

    @Override
    public View setLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channel, container, false);
    }

    public void refresh(){
        List<Channel> list = channelDao.queryUserLike();
        channelList.clear();
        channelList.addAll(list);
        if (mAdapter!=null){
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