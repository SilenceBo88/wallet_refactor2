package com.hunter.wallet.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;


/**
 * @auther: zb
 * @Date: 2018/10/8 17:34
 * @Description: 比特币交易类
 */
public class BtcTx implements Serializable {

    //交易Hash
    private String txHash;

    //交易类型：1,发送交易；2,接受交易
    private Integer txType;

    //付款地址
    private String fromAddress;

    //收款地址
    private String toAddress;

    //交易所在块高度
    private Integer blockheight;

    //交易确认数
    private Integer confirmations;

    //交易时间
    private Integer time;

    //交易总输出
    private Double valueOut;

    //交易总输入
    private Double valueIn;

    //交易手续费
    private String fees;

    //交易大小
    private Integer size;

    //交易金额
    private Double amount;
    
    /**
     * 
     * @Description: 把节点返回的交易结构转换为自己的结构
     * @param: result, address
     * @return: BtcTx
     * @auther: zb
     * @date: 2018/10/9 20:09
     */
    public static BtcTx fromJsonObject(JSONObject result, String address){
        return new BtcTx(result, address);
    }

    private BtcTx(JSONObject result, String address) {
        this.txHash = result.getString("txid");
        this.blockheight = result.getInteger("blockheight");
        this.confirmations = result.getInteger("confirmations");
        this.time = result.getInteger("time");
        this.valueOut = result.getDouble("valueOut");
        this.valueIn = result.getDouble("valueIn");
        this.fees = String.valueOf(result.get("fees"));
        this.size = result.getInteger("size");

        //通过获取交易输入地址是否与当前地址相同来判断是发送交易还是接收交易
        JSONArray vin = result.getJSONArray("vin");
        for (int i=0; i<vin.size(); i++){
            String vinAddr = vin.getJSONObject(i).getString("addr");
            if (vinAddr.equals(address)){
                this.txType = 1;
                break;
            }else {
                this.txType = 2;
            }
        }

        JSONArray vout = result.getJSONArray("vout");
        String addr;
        //通过判断交易类型来给付款地址和收款地址赋值
        if (txType == 1){
            //交易类型为发送，则主地址就是付款地址
            this.fromAddress = address;

            for (int i=0; i<vout.size(); i++){
                addr = vout.getJSONObject(i).getJSONObject("scriptPubKey").getJSONArray("addresses").getString(0);
                if (!addr.equals(address)){
                    this.amount = vout.getJSONObject(i).getDouble("value");
                    this.toAddress = addr;
                }
            }
        }else if (txType == 2){
            for (int i=0; i<vout.size(); i++){
                addr = vout.getJSONObject(i).getJSONObject("scriptPubKey").getJSONArray("addresses").getString(0);
                if (addr.equals(address)){
                    this.amount = vout.getJSONObject(i).getDouble("value");
                }
            }
            //交易类型为接收，获取第一个交易输入的地址
            this.fromAddress = vin.getJSONObject(0).getString("addr");
            //交易类型为接收，则主地址也是收款地址
            this.toAddress = address;
        }
    }

    public BtcTx() {
    }

    @Override
    public String toString() {
        return "BtcTx{" +
                "txHash='" + txHash + '\'' +
                ", txType=" + txType +
                ", fromAddress='" + fromAddress + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", blockheight=" + blockheight +
                ", confirmations=" + confirmations +
                ", time=" + time +
                ", valueOut=" + valueOut +
                ", valueIn=" + valueIn +
                ", fees='" + fees + '\'' +
                ", size=" + size +
                ", amount=" + amount +
                '}';
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public Integer getBlockheight() {
        return blockheight;
    }

    public void setBlockheight(Integer blockheight) {
        this.blockheight = blockheight;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(Integer confirmations) {
        this.confirmations = confirmations;
    }

    public Integer getTime() { return time; }

    public void setTime(Integer time) { this.time = time; }

    public Double getValueOut() {
        return valueOut;
    }

    public void setValueOut(Double valueOut) {
        this.valueOut = valueOut;
    }

    public Double getValueIn() {
        return valueIn;
    }

    public void setValueIn(Double valueIn) {
        this.valueIn = valueIn;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public Integer getTxType() {
        return txType;
    }

    public void setTxType(Integer txType) {
        this.txType = txType;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
