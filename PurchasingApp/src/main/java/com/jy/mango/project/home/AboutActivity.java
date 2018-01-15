package com.jy.mango.project.home;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.jy.mango.project.R;
import com.jy.mango.project.base.BaseTopActivity;

public class AboutActivity extends BaseTopActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textView= getView(R.id.tvTopTitle);
        textView.setText("关于我们");
        getView(R.id.back).setVisibility(View.VISIBLE);
        getView(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
