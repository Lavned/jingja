package com.jy.proect.jycashier.main;

import android.content.Intent;
import android.os.Bundle;

import com.jy.proect.jycashier.MainActivity;
import com.jy.proect.jycashier.R;
import com.jy.proect.jycashier.base.BaseActivity;
import com.jy.proect.jycashier.utils.PreferenceUtils;

import java.util.Timer;
import java.util.TimerTask;

public class GulidActivity extends BaseActivity {

    /**
     * 欢迎
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gulid);

        if(PreferenceUtils.getPrefBoolean(GulidActivity.this,"loginStatu",true))
            intentClass(2);
        else
            intentClass(1);


    }

    /**
     * 选择跳转
     * @param type
     */
    private void intentClass(final int type) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(type ==1){
                    Intent intent = new Intent(GulidActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(GulidActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, 3000);
    }
}
