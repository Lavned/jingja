package com.jy.proect.jycashier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.jy.proect.jycashier.base.BaseActivity;
import com.jy.proect.jycashier.bean.MainBean;
import com.jy.proect.jycashier.bean.PayBean;
import com.jy.proect.jycashier.main.ItemDetailActivity;
import com.jy.proect.jycashier.main.LoginActivity;
import com.jy.proect.jycashier.utils.T;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class MainActivity extends BaseActivity {
    /**
     * 首页
     */


    private Context context;
    private GridAdpater gridAdapter;

    @ViewInject(R.id.my_gridView)
    GridView myGridView;
    @ViewInject(R.id.main_image)
    ImageView main_image;
    @ViewInject(R.id.main_tvAll)
    TextView main_All;


    @Event(value = {R.id.main_image , R.id.main_tvAll} ,type = View.OnClickListener.class)
    private void onClick(View view){
        switch (view.getId()){
            case R.id.main_image :
                startActivity(new Intent(context, LoginActivity.class));
                break;
            case R.id.main_tvAll :
                showAllDialog();
                break;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        x.view().inject(this);//注解绑定
        myGridView.setAdapter(new GridAdpater(context));
        myGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(context,ItemDetailActivity.class));
            }
        });

        loadData();
    }

    /**
     * 桌子
     */
    private void loadData() {
//        DialogUIUtils.showLoadingHorizontal(this, "加载中...").show();
        RequestParams params = new RequestParams("");
        MainBean req = new MainBean();
        req.id = "1";
        String json = JSON.toJSONString(req);
        params.setBodyContent(json);
        params.setAsJsonContent(true);
        params.setHeader("Content-Type", "application/json; charset=utf-8");
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
            }
        });
    }


    /**
     * 弹出选择框
     */
    private void showAllDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.MyDialog);
        //builder.setIcon(R.drawable.ic_launcher);
//        builder.setTitle("选择一个");
        //    指定下拉列表的显示数据
        final String[] items = {"全部","1人桌", "2人桌", "5人桌", "10人桌", "20人桌"};
        //    设置一个下拉的列表选择项
        builder.setItems(items, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                T.showShort(items[which]);
                main_All.setText(items[which]);
            }
        });
        builder.show();
    }


    /**
     * 桌子适配器
     */
    class GridAdpater  extends BaseAdapter{
        public LayoutInflater inflater;

        public GridAdpater(Context context){
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return 30;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.main_items, null);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            return convertView;
        }

        public final class ViewHolder {
            public TextView main_tvNum;
            public TextView main_tvPeopleNum;
            public ImageView main_imStatus;
        }
     }

}
