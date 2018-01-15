package com.jy.mango.project.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jy.mango.project.R;

/**
 * Created by mango on 2017/11/23.
 */

public class CustomDialog  extends Dialog {


    private LinearLayout mLayout;
    private TextView mLoadingText;
    private boolean showWindow = true;



    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int themeResId){
        super(context, themeResId);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_my);

        TextView msg = findViewById(R.id.id_tv_loadingmsg);
        msg.setText("卖力加载中");

        if (showWindow) {
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.WHITE);
            gd.setCornerRadius(10);
            gd.setStroke(2, Color.WHITE);
            mLayout.setBackgroundDrawable(gd);

        }

    }

    public LinearLayout getLayout()
    {
        return mLayout;
    }



}