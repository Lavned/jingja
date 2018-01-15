package com.jy.mango.project.main;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jy.mango.project.R;
import com.jy.mango.project.base.BaseFragment;
import com.jy.mango.project.bean.HistoryList;
import com.jy.mango.project.request.History;
import com.jy.mango.project.utils.DateUtil;
import com.jy.mango.project.utils.PreferenceUtils;
import com.jy.mango.project.utils.T;
import com.jy.mango.project.view.AutoSplitTextView;
import com.jy.mango.project.view.DoubleDatePickerDialog;
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

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by mango on 2017/10/28.
 */

public class HomeFragment3 extends BaseFragment {

    private ListView history_lv;
    private TextView search;
    private TextView tv_date;
    private ImageView history_datanull;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void init(View rootView) {
        TextView textView = getView(R.id.tvTopTitle);
        textView.setText("历史采购");
        history_lv = getView(R.id.lv_history);
        search = getView(R.id.tv_search);
        tv_date = getView(R.id.tv_date);
        history_datanull= getView(R.id.history_datanull);
        tv_date.setText(DateUtil.getTime(System.currentTimeMillis(),2));
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(tv_date.getText().toString());
            }
        });
        tv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                    // 最后一个false表示不显示日期，如果要显示日期，最后参数可以是true或者不用输入
                    new DoubleDatePickerDialog(activity, 0, new DoubleDatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker startDatePicker, int startYear, int startMonthOfYear,
                                              int startDayOfMonth) {
                            String textString = String.format("%d-%d-%d", startYear,
                                    startMonthOfYear + 1, startDayOfMonth);
                            tv_date.setText(textString);
                        }
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), true).show();
                }
        });
        history_datanull.setVisibility(View.VISIBLE);

    }

    /**
     * 获取数据列表
     */
    private void getData(String date) {
        if(!activity.isFinishing() && activity!=null){
            MyProgressView.buildProgressDialog(activity,"卖力加载中...");
        }
        History req = new History();
        req.UserID = PreferenceUtils.getPrefString(activity,"userId","");
        req.Datetime = date;
        RequestParams params = new RequestParams();
        try {
            params.setHeader("Content-Type", "application/json; charset=utf-8");
            params.setBodyEntity(new StringEntity(req.toJson()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new HttpUtils().send(HttpRequest.HttpMethod.POST,
                GulidActivity.HOST + "/QueryHistoryList",
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
                            Gson gson = new Gson();
                            List<HistoryList> ss = gson.fromJson(jsonObject.getJSONArray("data").toString(),
                                    new TypeToken<List<HistoryList>>() {
                                    }.getType());
                            if(ss.size() == 0){
                                T.showShort("该日期暂无采购信息");
                                history_datanull.setVisibility(View.VISIBLE);
                            }else {
                                history_datanull.setVisibility(View.GONE);
                                ListAdapter leftAdapter = new ListAdapter(activity, ss);
                                history_lv.setAdapter(leftAdapter);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    /**
     * 适配器
     */
    class ListAdapter extends BaseAdapter {
        private Context context;//用于接收传递过来的Context对象
        private LayoutInflater mInflater;
        List<HistoryList> lists;

        public ListAdapter(Context context, List<HistoryList> lists) {
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.history_item, null);
                holder.titleText =  convertView.findViewById(R.id.itemn_name);
                holder.tNum = (AutoSplitTextView) convertView.findViewById(R.id.itemn_number);
                holder.tPirce = convertView.findViewById(R.id.itemn_price);
                holder.imageView = convertView.findViewById(R.id.item_image);
                holder.cratetime = convertView.findViewById(R.id.itemn_createtime);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)  convertView.getTag();
            }
            holder.titleText.setText(lists.get(position).goodsname);
            if(!setEmpty(lists.get(position).createtime).equals("")){
                String datetime = DateUtil.getTime(Long.parseLong(lists.get(position).createtime),1);
                holder.cratetime.setText(datetime);
            }
            holder.tNum.setText(setEmpty(lists.get(position).number.trim()) +
                    setEmpty(lists.get(position).measurename.trim()));
            holder.tPirce.setText("￥"+ setEmpty(lists.get(position).price));

            Glide.with(activity)
                    .load(lists.get(position).goodsimageurl)
                    .error(R.mipmap.loaderror)
                    .into(holder.imageView);

            return convertView;
        }

        public final class ViewHolder {
            public TextView titleText;
            public AutoSplitTextView tNum;
            public TextView tPirce;
            public ImageView imageView;
            public TextView cratetime;
        }
    }





    @Override
    protected int getLayoutId() {
        return R.layout.fragment_history;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public static String stampToDate(String s){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String sd = sdf.format(new Date(Long.parseLong(String.valueOf(s))));   // 时间戳转换成时间
        return sd;
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

}
