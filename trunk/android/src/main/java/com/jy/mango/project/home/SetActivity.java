package com.jy.mango.project.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jy.mango.project.R;
import com.jy.mango.project.base.BaseTopActivity;
import com.jy.mango.project.main.GulidActivity;
import com.jy.mango.project.utils.PreferenceUtils;
import com.jy.mango.project.utils.T;

import java.util.Set;

public class SetActivity extends BaseTopActivity {

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        TextView textView= getView(R.id.tvTopTitle);
        textView.setText("系统设置");
        context = this;
        getView(R.id.back).setVisibility(View.VISIBLE);
        getView(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getView(R.id.TV_agreement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context,AgreementActivity.class ));
            }
        });
        getView(R.id.TV_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setMessage("是否退出当前账号？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("退出登录", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                Intent intent = new Intent(context, GulidActivity.class);
//                                intent.putExtra("data","exit");
//                                startActivity(intent);
//                                PreferenceUtils.setPrefBoolean(context,"login_state",false);
                                setResult(1);
                                finish();
                            }
                        }).show();
            }
        });


        getView(R.id.TV_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(context,AboutActivity.class ));
            }
        });
        getView(R.id.TV_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                T.showShort("当前版本号为"+getVersion());
            }
        });
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return  version ;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
