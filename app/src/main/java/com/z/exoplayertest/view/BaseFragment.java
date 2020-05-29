package com.z.exoplayertest.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {


    protected Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (AppCompatActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return setLayoutView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        findViewById(view);
        setViewData(view);
        setClickEvent(view);
    }

    public void showToast(String value){
        Toast toast = Toast.makeText(getContext(),value,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
    /**
     * 设置布局
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public abstract View setLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * findViewById
     */
    public void findViewById(View view) {
    }

    /**
     * setViewData
     */
    public void setViewData(View view) {
    }

    /**
     * setClickEvent
     */
    public void setClickEvent(View view) {
    }

}