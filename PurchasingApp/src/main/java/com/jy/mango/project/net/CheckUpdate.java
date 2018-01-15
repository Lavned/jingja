package com.jy.mango.project.net;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.jy.mango.project.bean.CheckBean;
import com.jy.mango.project.bean.UtilsBean;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by mango on 2017/10/31.
 *
 */

public class CheckUpdate {


    public static String checkUpdate(final Context context){
        final UtilsBean req = new UtilsBean();
        req.systemcode = "01";
        req.version = getVersion(context);
        req.serviceid = "";
        RequestParams params = new RequestParams();
        try {
            params.setHeader("Content-Type", "application/json; charset=utf-8");
            params.setBodyEntity(new StringEntity(req.toJson()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new HttpUtils().send(HttpRequest.HttpMethod.POST,
                "http://servicequery.jinyoufarm.com/api/ServicesQuery/CheckUpdate",
                params, new RequestCallBack<String>() {
                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        T.showNetworkError(context);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> resp) {
                        Log.i("TEST" , "test"+resp.result.toString());
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(resp.result.toString());
                            PreferenceUtils.setPrefString(context,"ID",jsonObject.getString("serviceid"));
                            PreferenceUtils.setPrefString(context,"result",jsonObject.getString("result"));
                            PreferenceUtils.setPrefString(context,"data",jsonObject.getString("data"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        return  PreferenceUtils.getPrefString(context,"data","");
    }


    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public static String getVersion(Context context) {
        String version ="";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
            Log.i("TEST",version + version);
            return  version ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String getServiceUrl(final  Context context){
        checkUpdate(context);
        final UtilsBean req = new UtilsBean();
        req.systemcode = "01";
        req.version = getVersion(context);
        req.serviceid = PreferenceUtils.getPrefString(context,"ID","");
        RequestParams params = new RequestParams();
        try {
            params.setHeader("Content-Type", "application/json; charset=utf-8");
            params.setBodyEntity(new StringEntity(req.toJson()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new HttpUtils().send(HttpRequest.HttpMethod.POST,
                "http://servicequery.jinyoufarm.com/api/ServicesQuery/GetServiceUrl",
                params, new RequestCallBack<String>() {
                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        T.showNetworkError(context);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> resp) {
                        JSONObject jsonObject;
                        T.showShort(resp.result + "toast");
                        try {
                            jsonObject = new JSONObject(resp.result.toString());
                            jsonObject.getString("data");
                            req.serviceid = jsonObject.getString("data");
                            PreferenceUtils.setPrefString(context,"serviceurl",jsonObject.getString("data"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return  PreferenceUtils.getPrefString(context,"serviceurl","");
    }

}
