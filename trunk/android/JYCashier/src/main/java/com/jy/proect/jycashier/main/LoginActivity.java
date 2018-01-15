package com.jy.proect.jycashier.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.jy.proect.jycashier.MainActivity;
import com.jy.proect.jycashier.R;
import com.jy.proect.jycashier.base.BaseActivity;
import com.jy.proect.jycashier.bean.LoginBean;
import com.jy.proect.jycashier.utils.PreferenceUtils;
import com.jy.proect.jycashier.utils.T;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.UnsupportedEncodingException;

public class LoginActivity extends BaseActivity {



    //上下文
   private Context context;

   @ViewInject(R.id.ed_number)
    EditText edNumber;
   @ViewInject(R.id.ed_pass)
   EditText edPass;
   @ViewInject(R.id.btn_login)
    Button bunLogin;

   @Event(value = R.id.btn_login, type = View.OnClickListener.class)
   private void loginOn(View view){
       if(checkTrim(edNumber) || checkTrim(edPass)){
//           loginBtn();
           startActivity(new Intent(context, MainActivity.class));
           finish();
       }
   }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context= this;
        x.view().inject(this);


    }

    /**
     * 非空
     */
    public boolean checkTrim(EditText view ){
        if(view.getText().toString().trim().equals("")){
            T.showShort("不可为空");
            return false;
        }
        return  true;
    }

    /**
     * 登录请求
     */
    public void loginBtn() {
        LoginBean req = new LoginBean();
        RequestParams params = new RequestParams("");
        req.userName = edNumber.getText().toString().trim();
        req.pass= edPass.getText().toString().trim();;
        String json = JSON.toJSONString(req);
        params.setBodyContent(json);
        params.setAsJsonContent(true);
        params.setHeader("Content-Type", "application/json; charset=utf-8");
//        params.addBodyParameter("", json, "application/json");
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onSuccess(String result) {
                //打印服务端返回结果
                T.showShort(result);
                saveUserStatus();
                new Intent(context, MainActivity.class);
            }
        });
    }

    /**
     * 保存登录状态
     */
    public void saveUserStatus(){
        PreferenceUtils.setPrefBoolean(context,"loginStatus",true);
    }
}
