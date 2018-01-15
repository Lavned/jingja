package com.jy.mango.project.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jy.mango.project.R;
import com.jy.mango.project.base.BaseFragment;
import com.jy.mango.project.bean.TaskListBean;
import com.jy.mango.project.home.LoginActivity;
import com.jy.mango.project.net.CheckUpdate;
import com.jy.mango.project.request.ConfimsItmes;
import com.jy.mango.project.request.History;
import com.jy.mango.project.utils.PreferenceUtils;
import com.jy.mango.project.utils.ProgressDialogUtil;
import com.jy.mango.project.utils.T;
import com.jy.mango.project.view.MyProgressView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by mango on 2017/10/28.
 */

public class HomeFragment extends BaseFragment {

    private TextView tv_exit,tv_title;
    private TextView tv_Name,tv_address;
    private static TextView tv_Confim;
    private TextView tv_Count;
    private TextView tv_SevenCount;

    private static TextView tv_Message;
    private static String userID;
    private static List<TaskListBean> lists;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void init(View rootView) {
        initView();
        tv_Confim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUnDoneOH();
                checkUserStatus(activity);
            }
        });


        getUnDoneOH();
        //今日与累计日期的总数
        getTodayCount(userID);
        getHistoryCount(userID);

    }

    public  void initView(){
        tv_title =getView(R.id.tvTopTitle);
        tv_title.setText("首页");
        tv_title.requestFocus();
        tv_title.setFocusable(true);
        tv_title.setFocusableInTouchMode(false);
        tv_Count = getView(R.id.TV_Count);
        tv_SevenCount = getView(R.id.tv_SevenCount);
        tv_address = getView(R.id.TV_address);
        tv_Name = getView(R.id.TV_name);
        tv_Message = getView(R.id.TV_message);
        tv_Confim = getView(R.id.TV_confim);
        if(PreferenceUtils.getPrefBoolean(activity,"login_state",false)){
            tv_Name.setText(PreferenceUtils.getPrefString(activity,"nName","管理员"));
            tv_address.setText(PreferenceUtils.getPrefString(activity,"sName","管理员"));
        }
        userID = PreferenceUtils.getPrefString(activity,"userId","管理员");
    }

    public void checkUserStatus(Context context) {
        if(GulidActivity.HOST.equals("")){
            GulidActivity.HOST = "http://";
        }
        if(!activity.isFinishing() && activity!=null){
            MyProgressView.buildProgressDialog(activity,"加载中");
        }
        OkHttpClient mHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(GulidActivity.HOST +  "/CheckUserStatus?userid=" +userID)
                .build();
        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(!activity.isFinishing() && activity!=null){
                    MyProgressView.cancelProgressDialog();
                }
                T.showNetworkError(context);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        if(jsonObject.getString("data").equals("true")){
                            clickConfim(lists);
                        }else {
                            T.showShort("该用户无效！");
                            try {
                                Thread.sleep(500);
                                PreferenceUtils.setPrefBoolean(context,"login_state",false);
                                JPushInterface.stopPush(context.getApplicationContext());
                                startActivity(new Intent(activity, GulidActivity.class));
                                activity.overridePendingTransition(0,R.anim.activity_close);
                                activity.finish();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
    }


    /**
     * 判断隐藏
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){ //界面显示
            getUnDoneOH();
            //今日与累计日期的总数
            getTodayCount(userID);
            getHistoryCount(userID);
        } else{
            //隐藏
        }
    }


    /**
     *1)	查询未接收任务
     */
    public void  getUnDoneOH(){
        if(GulidActivity.HOST.equals("")){
            GulidActivity.HOST = "http://";
        }
            OkHttpClient mHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(GulidActivity.HOST +  "/QueryMisson?userid=" + userID)
                    .build();
            mHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    T.showNetworkError(activity);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        Gson gson = new Gson();
                        List<TaskListBean> ss = gson.fromJson(jsonObject.getJSONArray("data").toString(),
                                new TypeToken<List<TaskListBean>>(){}.getType());
                        lists = new ArrayList<>() ;
                        for (TaskListBean user : ss) {
                            lists.add(user);
                        }
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(lists!=null){
                                    if(lists.size() == 0){
                                        tv_Message.setText("暂时没有未读消息");
                                        tv_Confim.setVisibility(View.GONE);
                                    }else{
                                        tv_Confim.setVisibility(View.VISIBLE);
                                        tv_Message.setText("您有新的任务，请点击确认进行接收 !");
                                    }
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
    }




    /**
     * 点击确认
     */
    private void clickConfim(List<TaskListBean> dataSize) {
        StringBuffer ss= new StringBuffer();
        for (int i = 0; i < dataSize.size(); i++) {
            String taskID = dataSize.get(i).id;
            ss.append(taskID).append(",");
            if(i == dataSize.size() -1 ){
                if (',' == ss.charAt(ss.length() - 1))
                    ss = ss.deleteCharAt(ss.length() - 1);
            }
        }
        ConfimsItmes req = new ConfimsItmes();
        req.taskids = ss.toString();
        RequestParams params = new RequestParams();
        try {
            params.setHeader("Content-Type", "application/json; charset=utf-8");
            params.setBodyEntity(new StringEntity(req.toJson()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new HttpUtils().send(HttpRequest.HttpMethod.POST,  GulidActivity.HOST + "/ConfirmMisson", params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException arg0, String arg1){
                if(!activity.isFinishing() && activity!=null){
                    MyProgressView.cancelProgressDialog();
                }
                T.showNetworkError(activity);
            }

            @Override
            public void onSuccess(ResponseInfo<String> resp) {
                if(!activity.isFinishing() && activity!=null){
                    MyProgressView.cancelProgressDialog();
                }
                int count = Integer.parseInt(tv_Count.getText().toString());
                try {
                    if(!new JSONObject(resp.result.toString()).getString("data").equals("null")){
                        count = count +Integer.parseInt(new JSONObject(resp.result.toString()).getString("data"));
                        tv_Count.setText(count+"");
                        tv_Confim.setVisibility(View.GONE);
//                        T.showShort(resp.result + "");
                        tv_Message.setText("当前没有未读消息");
                        MainActivity.textView.setVisibility(View.GONE);
                        JPushInterface.clearAllNotifications(activity);
                        PreferenceUtils.setPrefString(activity,"message","0");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 获取未完成总数
     * @return
     */
    public void getTodayCount(String userID) {
        final HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(100 * 10); //设置超时时间   10s
        http.send(HttpRequest.HttpMethod.GET, GulidActivity.HOST + "/QueryItemCount?userid=" + userID,
                new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            tv_Count.setText(new JSONObject(responseInfo.result.toString()).getJSONObject("data").getString("count"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(HttpException error, String msg) {
                    }
                });
    }
    /**
     * 获取未完成总数
     * @return
     */
    public void getHistoryCount(String userID) {
        final HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000 * 10); //设置超时时间   10s
        http.send(HttpRequest.HttpMethod.GET, GulidActivity.HOST + "/QueryHistoryCount?userid=" + userID,
                new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            tv_SevenCount.setText(new JSONObject(responseInfo.result.toString()).
                                            getJSONObject("data").getString("count"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                    }
                });
    }



    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //释放资源//fragment中的资源在onDestroy中不进行释放，如有内存泄漏，在该方法中进行释放
    }

    //推送消息handler
    public static class nocatiHandler extends Handler {
        private Context context;

        public nocatiHandler() {
        }
        public nocatiHandler(Context context) {
            this.context = context;
        }

        // 子类必须重写此方法,接受数据
        @Override
        public void handleMessage(Message msg) {
            try{
                if(msg.what ==1){
                    // 此处可以更新UI
                    PreferenceUtils.setPrefString(context,"message","1");
//                    getUnCount();
                    tv_Message.setText("您有新的任务，请点击确认进行接收 !");
                    tv_Confim.setVisibility(View.VISIBLE);
                    Intent intent = new Intent();
                    intent.setAction("com.push");
                    intent.putExtra("name", "push");
                    context.sendBroadcast(intent);
//                    T.showShort("广播发送成功");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
