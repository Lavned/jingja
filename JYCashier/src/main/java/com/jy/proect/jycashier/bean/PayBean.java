package com.jy.proect.jycashier.bean;

import com.jy.proect.jycashier.base.BaseRequest;

/**
 * Created by mango on 2018/1/12.
 */

public class PayBean extends BaseRequest{
    public String id ; //	是	发起请求唯一标识（如果多次请求发送相同id则更新记录表里最后一条数据内容我当前内容并发起收款活）
    public String  paytype; 	//是	支付来源，1为支付宝，2为微信支付
    public String  out_trade_no; 	//是	外部订单号
    public String auth_code; 	//是	授权付款码(由手机端生成，扫码枪读取)
    public String  total_amount; 	//是	订单总金额,单位为元，如15.25
    public String  subject; 	//是	订单名称
    public String  body; 	//否	支付描述
    public String  store_id; 	//是	门店编号，很重要的参数，可以用作之后的营销
    public String  operator_id; 	//否	操作员编号，很重要的参数，可以用作之后的营销
    public String  goods[]; 	//否	商品列表
}
