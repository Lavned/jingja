package com.jy.proect.jycashier.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jy.proect.jycashier.MainActivity;
import com.jy.proect.jycashier.R;
import com.jy.proect.jycashier.base.BaseActivity;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class ItemDetailActivity extends BaseActivity{

    Context context;

    @ViewInject(R.id.my_listView)
    ListView myListView;

    //支付选择
    @ViewInject(R.id.pay_wx_img)
    ImageView pay_wx_img;
    @ViewInject(R.id.pay_zfb_img)
    ImageView pay_zfb_img;
    @ViewInject(R.id.pay_xj_img)
    ImageView pay_xj_img;


    public ListViewAdpater lvAdpater;

    @ViewInject(R.id.submitPay)
    Button submitPay;

    private int payType = 2;

    @Event(value = R.id.submitPay , type = View.OnClickListener.class)
    private void setSubmitPay(View view){
        Intent intent = new Intent(context,ZxingMainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("payType",payType+"");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        context = this;
        x.view().inject(this);
        myListView.setAdapter(new ListViewAdpater(context));
    }

    @Event(value ={ R.id.Pay_wx ,R.id.Pay_zfb,R.id.Pay_money } ,type = View.OnClickListener.class)
    private void onClick(View view){
        setImageHidden();
        switch (view.getId()){
            case R.id.Pay_wx :
                pay_wx_img.setVisibility(View.VISIBLE);
                payType = 2;
                break;
            case R.id.Pay_zfb :
                payType = 1;
                pay_zfb_img.setVisibility(View.VISIBLE);
                break;
            case R.id.Pay_money :
                pay_xj_img.setVisibility(View.VISIBLE);
                break;
        }
    }

    //设置选中按钮消失
    private void setImageHidden() {
        pay_wx_img.setVisibility(View.GONE);
        pay_xj_img.setVisibility(View.GONE);
        pay_zfb_img.setVisibility(View.GONE);
    }


    /**
     * 适配器
     */
    class ListViewAdpater  extends BaseAdapter {
        public LayoutInflater inflater;

        public ListViewAdpater(Context context){
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return 18;
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
                convertView = inflater.inflate(R.layout.detail_items, null);
                holder.itemName = convertView.findViewById(R.id.items_name);
                holder.itemNum = convertView.findViewById(R.id.items_num);
                holder.itemPrice = convertView.findViewById(R.id.items_price);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            return convertView;
        }

        public final class ViewHolder {
            public TextView itemName;
            public TextView itemNum;
            public TextView itemPrice;
        }
    }
}
