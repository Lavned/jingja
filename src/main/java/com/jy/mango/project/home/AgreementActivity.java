package com.jy.mango.project.home;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.jy.mango.project.R;
import com.jy.mango.project.base.BaseTopActivity;

public class AgreementActivity extends BaseTopActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        TextView textView= getView(R.id.tvTopTitle);
        textView.setText("服务协议");
        getView(R.id.back).setVisibility(View.VISIBLE);
        getView(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
