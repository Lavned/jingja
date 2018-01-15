package com.jy.mango.project.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jy.mango.project.R;
import com.jy.mango.project.base.BaseTopActivity;
import com.jy.mango.project.bean.UtilsBean;
import com.jy.mango.project.home.LoginActivity;
import com.jy.mango.project.net.Api;
import com.jy.mango.project.net.CheckUpdate;
import com.jy.mango.project.utils.PreferenceUtils;
import com.jy.mango.project.utils.T;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import okio.Timeout;

/**
 * Created by mango on 2017/10/28.
 */

public class GulidActivity extends BaseTopActivity {

    ImageView imageView;

//    public static String HOST = "http://purchase.jinyoufarm.cn/api/AppService/v1";
      public static String HOST = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gulid);
        imageView = findViewById(R.id.gu_images);
        checkUpdate(GulidActivity.this);

    }


    public void checkUpdate(final Context context) {
        HttpUtils httpUtils = new HttpUtils(5000);
        UtilsBean req = new UtilsBean();
        req.systemcode = "3";
        req.version = getVersion(context);
        req.serviceid = "";
        RequestParams params = new RequestParams();
        try {
            params.setHeader("Content-Type", "application/json; charset=utf-8");
            params.setBodyEntity(new StringEntity(req.toJson()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpUtils.send(HttpRequest.HttpMethod.POST,
                "http://servicequery.jinyoufarm.com/api/ServicesQuery/CheckUpdate",

                params, new RequestCallBack<String>() {
                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        T.showNetworkError(context);
                        chooseIntent(context);
                    }


                    @Override
                    public void onSuccess(ResponseInfo<String> resp) {
                        if(resp.statusCode == 200 || resp.statusCode == 0){
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(resp.result.toString());
                                getServiceUrl(context, jsonObject.getString("serviceid"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else {
                            T.showNetworkError(context);
                        }


                    }
                });
    }


    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getVersion(Context context) {
        String version = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    String serviceUrl;

    public void getServiceUrl(final Context context, final String serviceId) {
        HttpUtils httpUtils = new HttpUtils(5000);
        UtilsBean req = new UtilsBean();
        req.systemcode = "3";
        req.version = getVersion(context);
        req.serviceid = serviceId;
        RequestParams params = new RequestParams();
        try {
            params.setHeader("Content-Type", "application/json; charset=utf-8");
            params.setBodyEntity(new StringEntity(req.toJson()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpUtils.send(HttpRequest.HttpMethod.POST,
                "http://servicequery.jinyoufarm.com/api/ServicesQuery/GetServiceUrl",
                params, new RequestCallBack<String>() {
                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        T.showNetworkError(context);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> resp) {
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(resp.result.toString());
                            String result = jsonObject.getString("result");
                            if(result.equals("true")){
                                serviceUrl = jsonObject.getString("data");
                                if (serviceUrl != null) {
                                    HOST = serviceUrl;
                                    PreferenceUtils.setPrefString(context,"host" ,HOST);
                                }
                            }
                            chooseIntent(context);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    public void chooseIntent(Context context){
        if (PreferenceUtils.getPrefBoolean(context, "login_state", false)) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    intentClass(context, MainActivity.class, serviceUrl);
                }
            }, 1500);
        } else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    intentClass(context, LoginActivity.class, serviceUrl);
                }
            }, 1500);
        }
    }

    public void intentClass(Context context, Class tClass, String data ) {
        Intent intent = new Intent(context, tClass);
        intent.putExtra("data", data);
        startActivity(intent);
        finish();
    }
}
