package com.jy.mango.project.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.jy.mango.project.R;
import com.jy.mango.project.base.BaseFragment;
import com.jy.mango.project.bean.GoodDetail;
import com.jy.mango.project.bean.GoodDetailByID;
import com.jy.mango.project.bean.GoodType;
import com.jy.mango.project.bean.TaskListBean;
import com.jy.mango.project.home.LoginActivity;
import com.jy.mango.project.request.ConfimItemsRequest;
import com.jy.mango.project.request.GetListRequest;
import com.jy.mango.project.utils.DateUtil;
import com.jy.mango.project.utils.NoDoubleClickUtils;
import com.jy.mango.project.utils.PreferenceUtils;
import com.jy.mango.project.utils.ProgressDialogUtil;
import com.jy.mango.project.utils.T;
import com.jy.mango.project.view.MyGridView;
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
import java.util.concurrent.TimeUnit;

import cn.jpush.android.api.JPushInterface;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by mango on 2017/10/28.
 */

public class TodayFragment extends BaseFragment implements  View.OnClickListener,PullToRefreshBase.OnRefreshListener2<GridView> {

    private ListView lv_left;//左侧分类
    LeftAdapter leftAdapter;

    private String userID;
    private ImageView button;//删除搜索框
    private EditText editText;//搜索框

    private TextView tv_Undone, tv_done;//完成、未完成

    //弹出框相关
    private PopupWindow popupWindow;
    private View contentView;

    LinearLayout all_Button;
    private int seleted = 1;

    ImageView floatButton; //回到顶部
    View view_undone, view_done;//布局
    private int pageIndex = 1;//分页
    private int pageSize = 10;

    //右侧
    private PullToRefreshGridView gv_right;
    private RightAdapter rightAdapter;


    public static String TAG = "JYTEST";
    public String itemID = "";
    List<GoodDetail> goodDetails;

    ProgressDialog progressDialo;

    private ImageView dataIsNull;
    private boolean isClick = false;

    Button btnConfim, btnCancel, btnException;


    private String storehouseID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    protected void init(View rootView) {
        progressDialo = new ProgressDialog(activity);
//        TextView tvTitle = getView(R.id.tvTopTitle);
//        tvTitle.setText("今日采购");
        dataIsNull = getView(R.id.datanull);
        userID = PreferenceUtils.getPrefString(activity, "userId", "管理员");
        storehouseID = PreferenceUtils.getPrefString(activity,"storehouseID","");
        lv_left = getView(R.id.lv_left);
        gv_right = getView(R.id.gv_right);
        gv_right.setMode(PullToRefreshBase.Mode.BOTH);
        gv_right.setOnRefreshListener(this);
        tv_Undone = getView(R.id.TV_undone);
        tv_done = getView(R.id.TV_done);
        tv_Undone.setOnClickListener(this);
        tv_done.setOnClickListener(this);
        view_undone = getView(R.id.view_undone);
        view_done = getView(R.id.view_done);
        floatButton = getView(R.id.top);
        floatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gv_right.getRefreshableView().setSelection(0);
            }
        });

        gv_right.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GoodDetail detail = (GoodDetail) parent.getItemAtPosition(position);
                if (!NoDoubleClickUtils.isDoubleClick()) {
                    if (seleted == 1) {
                        getGoodDetailById(detail.purchaseitemid, 0);
                    } else {
                        getGoodDetailById(detail.purchaseitemid, 1);
                    }
                }
            }
        });
        //滑动事件
        gv_right.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    floatButton.setVisibility(View.GONE);
                if (visibleItemCount > 5 && visibleItemCount + firstVisibleItem == totalItemCount)
                    floatButton.setVisibility(View.VISIBLE);
            }
        });
        button = getView(R.id.button);
        button.setOnClickListener(this);
        editText = getView(R.id.ED_word);

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == event.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    isClick = true;
                    button.setVisibility(View.VISIBLE);
                    if (seleted == 1) {
                        getGoodDetail(itemID, editText.getText().toString(), "1");
                    } else {
                        getGoodDetail(itemID, editText.getText().toString(), "2,3");
                    }
                }
                if (keyCode == event.KEYCODE_DEL && editText.getText().toString().trim().equals("") && isClick == true) {
                    button.setVisibility(View.GONE);
                    isClick = false;
                    if (seleted == 1) {
                        getGoodDetail(itemID, "", "1");
                    } else {
                        getGoodDetail(itemID, "", "2,3");
                    }
                }
                return false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editText.getText().toString().trim().equals("")) {
                    button.setVisibility(View.GONE);
                    if (delayRun != null) {//每次editText有变化的时候，则移除上次发出的延迟线程
                        handler.removeCallbacks(delayRun);
                    }
                    //延迟800ms，如果不再输入字符，则执行该线程的run方法
                    handler.postDelayed(delayRun, 200);
                }
            }
        });
        getView(R.id.clickSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().trim().equals("")) {
                    T.showShort("请输入商品名称");
                } else {
                    if (seleted == 1) {
                        getGoodDetail(itemID, editText.getText().toString().trim(), "1");
                    } else {
                        getGoodDetail(itemID, editText.getText().toString().trim(), "2,3");
                    }
                }
            }
        });

        getGoodType();
        getGoodDetail("", "", "1");

    }

    private Handler handler = new Handler();
    /**
     * 延迟线程，看是否还有下一个字符输入
     */
    private Runnable delayRun = new Runnable() {

        @Override
        public void run() {
            if (editText.getText().toString().trim().equals("")) {
                button.setVisibility(View.GONE);
            }
        }
    };


    /**
     * 判断隐藏
     *
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) { //界面显示
            itemID = "";
            gv_right.setMode(PullToRefreshBase.Mode.BOTH);
            pageIndex = 1;
            tv_Undone.setTextColor(Color.parseColor("#00A961"));
            tv_done.setTextColor(Color.parseColor("#808080"));
            view_undone.setVisibility(View.VISIBLE);
            view_done.setVisibility(View.GONE);
            seleted = 1;
            getGoodType();
            getGoodDetail(itemID, "", "1");
            pullGetData(itemID, editText.getText().toString().trim(), "1");
        } else {
            //隐藏
        }
    }

    /**
     * 下拉刷新
     *
     * @param data
     */
    protected void updateView(List<GoodDetail> data) {
        if (pageIndex == 1) {
            rightAdapter = new RightAdapter(activity, data);
            gv_right.setAdapter(rightAdapter);
        } else {
            rightAdapter.getmData().addAll(data);//累计显示
//            rightAdapter = new RightAdapter(activity, data);
            rightAdapter.notifyDataSetChanged();
            gv_right.setAdapter(rightAdapter);
        }
        pageIndex++;
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
        gv_right.setMode(PullToRefreshBase.Mode.BOTH);
        pageIndex = 1;
        switch (seleted) {
            case 1:
                if (itemID.equals("000"))
                    pullGetData("", editText.getText().toString().trim(), "1");
                else
                    pullGetData(itemID, editText.getText().toString().trim(), "1");
                break;
            case 2:
                if (itemID.equals("000"))
                    pullGetData("", editText.getText().toString().trim(), "2,3");
                else
                    pullGetData(itemID, editText.getText().toString().trim(), "2,3");
                break;
        }

    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
        switch (seleted) {
            case 1:
                if (itemID.equals("000"))
                    pullGetData("", editText.getText().toString().trim(), "1");
                else
                    pullGetData(itemID, editText.getText().toString().trim(), "1");
                break;
            case 2:
                if (itemID.equals("000"))
                    pullGetData("", editText.getText().toString().trim(), "2,3");
                else
                    pullGetData(itemID, editText.getText().toString().trim(), "2,3");
                break;
        }
    }

    GoodDetailByID byIDlists = new GoodDetailByID();
    public GoodDetailByID getGoodDetailById(final String ID, final int type) {
        if (GulidActivity.HOST.equals("")) {
            GulidActivity.HOST = "http://";
        }
        if(!activity.isFinishing() && activity!=null){
            MyProgressView.buildProgressDialog(activity,"加载中");
        }
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .readTimeout(1000,TimeUnit.SECONDS)//设置读取超时时间
                        .writeTimeout(1000,TimeUnit.SECONDS)//设置写的超时时间
                        .connectTimeout(1000,TimeUnit.SECONDS)//设置连接超时时间
                        .build();
//        OkHttpClient.Builder builder = new OkHttpClient.Builder()
//                .connectTimeout(3000, TimeUnit.SECONDS);
//        OkHttpClient mHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(GulidActivity.HOST + "/GetGoodsDetail?purchaseitemid=" + ID)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                T.showNetworkError(activity);
                if(!activity.isFinishing() && activity!=null){
                    MyProgressView.cancelProgressDialog();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                if(!activity.isFinishing() && activity!=null){
                    MyProgressView.cancelProgressDialog();
                }
                if(response.code()==200 || response.code() == 0){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(jsonData);
                                Gson gson = new Gson();
                                byIDlists = gson.fromJson(jsonObject.getJSONObject("data").toString(), GoodDetailByID.class);
                                popShow(byIDlists, type ,ID);
                                if(!activity.isFinishing() && activity!=null && !popupWindow.isShowing()){
                                    popupWindow.dismiss();
                                    popupWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
                                    btnException.setClickable(true);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }else {
                    T.showNetworkError(activity);
                }
//
            }
        });
        return byIDlists;
    }

    /**
     * 商品详情
     */


    /**
     * show pop
     */
    private void popShow(final GoodDetailByID goodDetail, int type,final String id) {
        //加载弹出框的布局
        contentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.good_detail, null);
        //绑定控件
        all_Button = contentView.findViewById(R.id.all_button);
        final EditText eTnum = contentView.findViewById(R.id.ed_number);
        final EditText eTprice = contentView.findViewById(R.id.ed_number2);
        TextView back = contentView.findViewById(R.id.tv_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        ImageView imageView = contentView.findViewById(R.id.im_photo);
        TextView name = contentView.findViewById(R.id.tv_name);
        TextView tv_spe = contentView.findViewById(R.id.tv_spe);
        TextView tv_supplier = contentView.findViewById(R.id.tv_supplier);
        TextView tv_price = contentView.findViewById(R.id.tv_price);
        TextView tv_number = contentView.findViewById(R.id.tv_number);
        name.setText("名称：" + setEmpty(goodDetail.goodsname));
        tv_supplier.setText("供应商：" + setEmpty(goodDetail.suppliername));
        tv_price.setText("单价：￥" + setEmpty(goodDetail.price));
        tv_number.setText("数量：" + setEmpty(goodDetail.number) + setEmpty(goodDetail.mesurementname));
        tv_spe.setText("规格：" + setEmpty(goodDetail.goodsspec));

        eTnum.setText(setEmpty(goodDetail.actualnumber));
        eTprice.setHint("不填为默认");

        GridView gv_Remarks = contentView.findViewById(R.id.gv_remark);
        if (goodDetail.remarks != null && !goodDetail.remarks.equals("")) {
            gv_Remarks.setVisibility(View.VISIBLE);
            String[] remarkData = goodDetail.remarks.split("/");
            List<String> textList = new ArrayList<String>();
            for (String a : remarkData) {
                textList.add(a);
            }
            if(textList.size() > 30){
                // 获取listview的布局参数
                ViewGroup.LayoutParams params = gv_Remarks.getLayoutParams();
                // 设置高度
                params.height = 550;
                // 设置margin
                ((ViewGroup.MarginLayoutParams) params).setMargins(10, 10, 10, 10);
                // 设置参数
                gv_Remarks.setLayoutParams(params);
            }
            gv_Remarks.setAdapter(new RemarkAdapter(textList));
        }
        //事件
        btnConfim = contentView.findViewById(R.id.btn_confim);
        btnCancel = contentView.findViewById(R.id.btn_cancel);
        btnException =contentView.findViewById(R.id.btn_excption);
        if(setEmpty(goodDetail.mesurementname).equals("斤")&& setEmpty(goodDetail.is_weighting).equals("1")){
            eTnum.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
        btnException.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(activity).setTitle("提示")//设置对话框标题
                        .setMessage("确定任务异常吗？")//设置显示的内容
                        .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮
                            @Override
                            public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                if(isFirst(setEmpty(eTnum.getText().toString().trim()))){
                                    if(isFirst(setEmpty(eTprice.getText().toString().trim()))){
                                        checkUserStatus(1,"1", id,eTprice.getText().toString(),goodDetail.purchaseid, eTnum.getText().toString());
                                    }
                                    else{
                                        T.showShort("实采单价输入不合法！");
                                    }
                                }
                                else{
                                    T.showShort("实采数量输入不合法！");
                                }
                            }
                        }).setNegativeButton("取消",new DialogInterface.OnClickListener() {//添加返回按钮
                    @Override

                    public void onClick(DialogInterface dialog, int which) {//响应事件
                    }
                }).show();//
            }
        });
        btnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(activity).setTitle("提示")//设置对话框标题
                        .setMessage("确定要提交任务吗？")//设置显示的内容
                        .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮
                            @Override
                            public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                    if(isFirst(setEmpty(eTnum.getText().toString().trim()))){
                                        if(isFirst(setEmpty(eTprice.getText().toString().trim()))){
                                            checkUserStatus(0,"0", id,eTprice.getText().toString(),goodDetail.purchaseid, eTnum.getText().toString());
                                        }
                                        else{
                                            T.showShort("实采单价输入不合法！");
                                        }
                                    }
                                    else{
                                        T.showShort("实采数量输入不合法！");
                                    }
                            }
                        }).setNegativeButton("取消",new DialogInterface.OnClickListener() {//添加返回按钮
                    @Override

                    public void onClick(DialogInterface dialog, int which) {//响应事件
                    }
                }).show();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        Glide.with(activity)
                .load(goodDetail.goodsimageurl)
                .error(R.mipmap.loaderror)
                .into(imageView);
        if(type == 1) {
            all_Button.setVisibility(View.GONE);
            if(goodDetail.status.equals("3")){
                eTprice.setText(setEmpty("￥" ));
            }else {
                eTprice.setText(setEmpty("￥" + goodDetail.price));
            }

            eTnum.setEnabled(false);
            eTprice.setEnabled(false);
        }
        //设置弹出框的宽度和高度
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);// 取得焦点
        //注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //点击外部消失
        popupWindow.setOutsideTouchable(false);
        //设置可以点击
        popupWindow.setTouchable(true);
        //进入退出的动画
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
//        getActivity().setBackgroundAlpha(0.5f);//设置屏幕透明度
//        activity.setFeatureDrawableAlpha();
        backgroundAlpha(0.5f);
        popupWindow.setOnDismissListener(new poponDismissListener());
    }

    //检查用户是否生效
    public void checkUserStatus(int ststus,String type,final String purchaseitemid,
                                String purchaseprice,String purchaseid,String  purchasenumber) {
        if(!activity.isFinishing() && activity!=null){
            MyProgressView.buildProgressDialog(activity,"请稍后...");
        }
        new HttpUtils().send(HttpRequest.HttpMethod.GET,
                GulidActivity.HOST+"/CheckUserStatus?userid=" +userID,
                new RequestCallBack<String>() {
                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        if(!activity.isFinishing() && activity!=null){
                            MyProgressView.cancelProgressDialog();
                        }
                        T.showNetworkError(activity);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> resp) {
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(resp.result.toString());
                            if(jsonObject.getString("data").equals("true")){
                                if(ststus == 0){
                                    confimItems("0", purchaseitemid,purchaseprice,purchaseid, purchasenumber);
                                }else {
                                    confimItems("1", purchaseitemid,purchaseprice,purchaseid, purchasenumber);
                                }
                            }else {
                                T.showShort("该用户无效！");
                                try {
                                    Thread.sleep(500);
                                    PreferenceUtils.setPrefBoolean(activity,"login_state",false);
                                    JPushInterface.stopPush(activity.getApplicationContext());
                                    startActivity(new Intent(activity, GulidActivity.class));
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

    boolean isFirst(String v){
        if(v.startsWith(".")){
            return false;
        }else {
            return true;
        }
    }
    /**
     * 添加新笔记时弹出的popWin关闭的事件，主要是为了将背景透明度改回来
     */
    class poponDismissListener implements PopupWindow.OnDismissListener {

        @Override
        public void onDismiss() {
            backgroundAlpha(1f);
        }

    }
    /**
     * \
     * 设置背景透明
     * @param v
     */
    private void backgroundAlpha(float v) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = v; //0.0-1.0
        activity.getWindow().setAttributes(lp);
    }


    /**
     * @param name
     * @return
     */
    public String setEmpty(String name) {
        if (name == null) {
            name = "";
        }
        return name;
    }

    /**
     *   商品remark适配
     */
    class RemarkAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        List<String> text;
        public RemarkAdapter(List<String> text) {
            this.text = text;
            this.mInflater = LayoutInflater.from(activity);
        }

        @Override
        public int getCount() {
            return text.size();
        }

        @Override
        public Object getItem(int position) {
            return text.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.remark_items, null);
                holder.titleText = convertView.findViewById(R.id.remark_detail);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.titleText.setText(text.get(position).trim());

            return convertView;
        }

        public final class ViewHolder {
            public TextView titleText;

        }
    }


    /**
     *确认任务
     */
    private void confimItems(String type,final String purchaseitemid,
                             String purchaseprice,String purchaseid,String  purchasenumber) {
        final ConfimItemsRequest req = new ConfimItemsRequest();
        req.purchaseid = purchaseid;
        req.purchaseitemid = purchaseitemid;
        req.purchasenumber = purchasenumber;
        req.purchaseprice = purchaseprice.replace("￥","");
        req.type = type;
        Log.i("tijiaocanshu" , req.purchaseid +"TTT" +req.purchaseitemid +"TTT"
                +req.purchasenumber +"TTT"+req.purchaseprice+"TTT" +req.type);
        RequestParams params = new RequestParams();
        try {
            params.setHeader("Content-Type", "application/json; charset=utf-8");
            params.setBodyEntity(new StringEntity(req.toJson()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new HttpUtils().send(HttpRequest.HttpMethod.POST,
                GulidActivity.HOST + "/ConfirmItems",
                params, new RequestCallBack<String>() {
                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
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
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(resp.result.toString());
                            if(jsonObject.getString("data").contains("true")){
                                getGoodDetail(itemID,editText.getText().toString().trim(),seleted+"");
                                popupWindow.dismiss();
                                T.showShort("提交成功");
                            }else {
                                T.showShort("提交失败");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    /**
     * 获取商品类别
     */
    private List<GoodType> lists;

    private void getGoodType() {
        final HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000 * 10); //设置超时时间   10s
        http.send(HttpRequest.HttpMethod.GET, GulidActivity.HOST + "/GetRecommend?storehousid=" +storehouseID,
            new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseInfo.result.toString());
                        Gson gson = new Gson();
                        List<GoodType> ss = gson.fromJson(jsonObject.getJSONArray("data").toString(),
                                new TypeToken<List<GoodType>>() {
                                }.getType());
                        lists = new ArrayList<GoodType>();
                        for (GoodType user : ss) {
                            lists.add(user);
                        }
                        GoodType goodType = new GoodType();
                        goodType.name ="全部";
                        goodType.id ="000";
                        lists.add(0,goodType);
                        leftAdapter = new LeftAdapter(activity, lists);
                        leftAdapter. setSelectItem(0);
                        lv_left.setAdapter(leftAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(HttpException error, String msg) {
                    T.showNetworkError(activity);
                }
            });
    }

    /**
     * 获取商品列表
     */
    private void pullGetData(String categoryid,String kword,String status) {
        GetListRequest req = new GetListRequest();
        req.userid = userID;
        req.pageindex = pageIndex;
        req.pagesize = pageSize;
        req.storehousid = storehouseID;
        if(categoryid.equals("000")){
            req.categoryid = "";
        } else{
            req.categoryid = categoryid;
        }
        req.status = status;
        req.kword = kword;
//        Log.i(TAG ,"参数" + pageIndex + pageSize + "id" +categoryid + "status" + status + "gj" + kword);
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000 * 10); //设置超时时间   10s
        RequestParams params = new RequestParams();
        try {
            params.setHeader("Content-Type", "application/json; charset=utf-8");
            params.setBodyEntity(new StringEntity(req.toJson()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        http.send(HttpRequest.HttpMethod.POST, GulidActivity.HOST + "/GetGoodsList",params,
            new RequestCallBack<String>() {

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    ProgressDialogUtil.dismissProgressDlg();
                    Log.i(TAG ,TAG +responseInfo.result.toString());
                    gv_right.onRefreshComplete();
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(responseInfo.result.toString());
                        Gson gson = new Gson();
                        List<GoodDetail> ss = gson.fromJson(jsonObject.getJSONArray("data").toString(),
                                new TypeToken<List<GoodDetail>>() { }.getType());
                        if(ss==null || ss.size()<=0) {
                            T.showShort("商品已经加载完了！");
                            gv_right.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                        }else{
                            updateView(ss);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG , "TESTSmsg" +msg);
                    gv_right.onRefreshComplete();
                    T.showNetworkError(activity);
                }
            });
    }

    /**
     * 获取商品列表
     */
    private void getGoodDetail(String categoryid,String kword,String status) {
        if(!activity.isFinishing() && activity!=null){
            MyProgressView.buildProgressDialog(activity,"卖力加载中");
        }
        GetListRequest req = new GetListRequest();
        req.userid = userID;
        req.pageindex = 1;
        req.pagesize = 10;
        if(categoryid.equals("000")){
            req.categoryid = "";
        } else{
            req.categoryid = categoryid;
        }
//        Log.i(TAG ,"参数啊啊啊啊" +req.categoryid +"kword"  + kword +"userid" + userID + "page" + pageIndex + pageSize + "status" +status) ;
        req.status = status;
        req.kword = kword;
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000 * 10); //设置超时时间   10s
        RequestParams params = new RequestParams("utf-8");
        try {
            params.setHeader("Content-Type", "application/json; charset=utf-8");
            params.setBodyEntity(new StringEntity(req.toJson(),"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        http.send(HttpRequest.HttpMethod.POST, GulidActivity.HOST + "/GetGoodsList",params,
                new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(!activity.isFinishing() && activity!=null){
                            MyProgressView.cancelProgressDialog();
                        }
                        gv_right.onRefreshComplete();
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(responseInfo.result.toString());
                            Gson gson = new Gson();
                            List<GoodDetail> ss = gson.fromJson(jsonObject.getJSONArray("data").toString(),
                                    new TypeToken<List<GoodDetail>>() { }.getType());
                            byID(ss);
                            if(ss==null || ss.size()<=0) {
//                                T.showShort("暂无采购信息");
                                dataIsNull.setVisibility(View.VISIBLE);
                                rightAdapter = new RightAdapter(activity,ss);
                                rightAdapter.notifyDataSetChanged();
                                gv_right.setAdapter(rightAdapter);
                            }else{
                                dataIsNull.setVisibility(View.GONE);
                                rightAdapter = new RightAdapter(activity,ss);
                                rightAdapter.notifyDataSetChanged();
                                gv_right.setAdapter(rightAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(HttpException error, String msg) {
                        if(!activity.isFinishing() && activity!=null){
                            MyProgressView.cancelProgressDialog();
                        }
                        Log.i(TAG , "TESTSmsg" +msg);
                        gv_right.onRefreshComplete();
                        T.showNetworkError(activity);
                    }
                });
    }

    //传参存储数据
    public List<GoodDetail> byID(List<GoodDetail> detailList) {
        goodDetails = detailList;
        return detailList;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_today;
    }

    @Override
    public void onClick(View v) {
        gv_right.setMode(PullToRefreshBase.Mode.BOTH);
        editText.setText("");
        editText.clearFocus();
        switch (v.getId()) {
            case R.id.TV_undone:
                tv_Undone.setTextColor(Color.parseColor("#00A961"));
                tv_done.setTextColor(Color.parseColor("#808080"));
                view_undone.setVisibility(View.VISIBLE);
                view_done.setVisibility(View.GONE);
                seleted = 1;
                getGoodDetail(itemID,"","1");
                break;
            case R.id.TV_done:
                tv_Undone.setTextColor(Color.parseColor("#808080"));
                tv_done.setTextColor(Color.parseColor("#00A961"));
                view_undone.setVisibility(View.GONE);
                view_done.setVisibility(View.VISIBLE);
                seleted = 2;
                getGoodDetail(itemID,"","2,3");
                break;
            case R.id.button :
                button.setVisibility(View.GONE);
                editText.setText("");
                if(seleted == 1){
                    getGoodDetail(itemID,"","1");
                }else {
                    getGoodDetail(itemID,"","2,3");
                }
                break;
        }
    }

    /**
     * 左侧菜单的适配器
     */
    class LeftAdapter extends BaseAdapter {
        private Context context;//用于接收传递过来的Context对象
        private LayoutInflater mInflater;
        List<GoodType> lists;

        public LeftAdapter(Context context, List<GoodType> lists) {
            this.context = context;
            this.lists = lists;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return lists.size();
        }

        @Override
        public Object getItem(int position) {
            return lists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.today_leftview, null);
                holder.titleText = convertView.findViewById(R.id.leftName);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.titleText.setText(lists.get(position).name);
            holder.titleText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pageIndex = 1;
                    gv_right.setMode(PullToRefreshBase.Mode.BOTH);
                    leftAdapter.setSelectItem(position);
                    leftAdapter.notifyDataSetChanged();
                    String ids = lists.get(position).id;
                    itemID = ids;
                    if(ids != null && !ids.equals("000")){
                        if(seleted == 1){
                            getGoodDetail(ids,"","1");
                        }else{
                            getGoodDetail(ids,"","2,3");
                        }
                    }
                    else if(ids.equals("000")){
                        if(seleted == 1){
                            getGoodDetail("","","1");
                        }else{
                            getGoodDetail("","","2,3");
                        }
                    }
                }
            });

            if (position == selectItem) {
                Drawable img = convertView.getResources().getDrawable(R.mipmap.lefttag);
                // 调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
                img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                holder.titleText.setCompoundDrawables(img, null, null, null); //设置左图标
                convertView.setBackgroundColor(Color.parseColor("#ffffff"));
                holder.titleText.setTextColor(Color.parseColor("#00A961"));
            } else {
                Drawable img = convertView.getResources().getDrawable(R.mipmap.lefttag2);
                // 调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
                img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                holder.titleText.setCompoundDrawables(img, null, null, null); //设置左图标
                convertView.setBackgroundColor(Color.TRANSPARENT);
                holder.titleText.setTextColor(Color.parseColor("#808080"));
            }
            return convertView;
        }

        public void setSelectItem(int selectItem) {
            this.selectItem = selectItem;
        }

        private int selectItem = -1;

        public final class ViewHolder {
            public TextView titleText;
//            public TextView leftTag;
        }
    }



    /**
     * 右侧商品的适配器
     */
    class RightAdapter extends BaseAdapter {
        private Context context;//用于接收传递过来的Context对象
        private LayoutInflater mInflater;
        List<GoodDetail> lists;

        public RightAdapter(Context context, List<GoodDetail> lists) {
            this.context = context;
            this.lists = lists;
            this.mInflater = LayoutInflater.from(context);
        }

            public List<GoodDetail> getmData() {
                return lists;
            }

        @Override
        public int getCount() {
            return lists.size();
        }

        @Override
        public Object getItem(int position) {
            return lists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.today_rightview, null);
                holder.titleText =  convertView.findViewById(R.id.leftName);
                holder.tNum = convertView.findViewById(R.id.leftCount);
                holder.imageView = convertView.findViewById(R.id.image);
                holder.tDate = convertView.findViewById(R.id.leftDate);
                holder.right_lin = convertView.findViewById(R.id.right_lin);
                holder.statusError = convertView.findViewById(R.id.statusError);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.titleText.setText(lists.get(position).goodsname);
            holder.tDate.setText("时间："+ DateUtil.getTime(Long.parseLong(lists.get(position).createtime),1));
            holder.tNum.setText(setEmpty(lists.get(position).number) + setEmpty(lists.get(position).measurename));
            if(lists.get(position).status.equals("3")){
                holder.statusError.setVisibility(View.VISIBLE);
            }else {
                holder.statusError.setVisibility(View.GONE);
            }
            Glide.with(activity)
                    .load(lists.get(position).goodsimageurl)
                    .error(R.mipmap.loaderror)
                    .into(holder.imageView);
            return convertView;
        }

            public final class ViewHolder {
            public TextView titleText;
            public TextView tDate;
            public TextView tNum;
            public ImageView imageView;
            public LinearLayout right_lin;
            public ImageView statusError;

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
