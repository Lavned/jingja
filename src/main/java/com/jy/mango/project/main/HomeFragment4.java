package com.jy.mango.project.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jy.mango.project.R;
import com.jy.mango.project.base.BaseFragment;
import com.jy.mango.project.home.SetActivity;
import com.jy.mango.project.utils.PreferenceUtils;

/**
 * Created by mango on 2017/10/28.
 */

public class HomeFragment4 extends BaseFragment {

    private  TextView tv_Name,tv_address,tv_tel,tv_address2;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    protected void init(View rootView) {
        context = getActivity();
        tv_address = getView(R.id.TV_address);
        tv_Name = getView(R.id.TV_name);
        tv_tel = getView(R.id.TV_tel);
        tv_address2 = getView(R.id.TV_address2);
        if(PreferenceUtils.getPrefBoolean(activity,"login_state",false)){
            tv_Name.setText(PreferenceUtils.getPrefString(activity,"nName","管理员"));
            tv_address.setText(PreferenceUtils.getPrefString(activity,"sName","管理员"));
            tv_tel.setText(PreferenceUtils.getPrefString(activity,"contact","110"));
            tv_address2.setText(PreferenceUtils.getPrefString(activity,"address","虹桥"));
        }
        getView(R.id.TV_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(context,SetActivity.class),1);
            }
        });

    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mine;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
