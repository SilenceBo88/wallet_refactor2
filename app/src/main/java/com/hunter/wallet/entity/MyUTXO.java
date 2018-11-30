package com.hunter.wallet.entity;

/**
 * @auther: zb
 * @Date: 2018/10/19 17:19
 * @Description: 请求返回的utxo
 */
public class MyUTXO {
    private String address;
    private String txid;
    private Integer vout;
    private String scriptPubKey;
    private Double amount;
    private Long satoshis;
    private Integer height;
    private Integer confirmations;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public Integer getVout() {
        return vout;
    }

    public void setVout(Integer vout) {
        this.vout = vout;
    }

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    public void setScriptPubKey(String scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getSatoshis() {
        return satoshis;
    }

    public void setSatoshis(Long satoshis) {
        this.satoshis = satoshis;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(Integer confirmations) {
        this.confirmations = confirmations;
    }

    @Override
    public String toString() {
        return "MyUTXO{" +
                "address='" + address + '\'' +
                ", txid='" + txid + '\'' +
                ", vout=" + vout +
                ", scriptPubKey='" + scriptPubKey + '\'' +
                ", amount='" + amount + '\'' +
                ", satoshis='" + satoshis + '\'' +
                ", height='" + height + '\'' +
                ", confirmations='" + confirmations + '\'' +
                '}';
    }
}
