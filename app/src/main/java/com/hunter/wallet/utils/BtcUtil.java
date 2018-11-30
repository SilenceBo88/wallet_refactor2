package com.hunter.wallet.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.hunter.wallet.entity.*;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.VerifyFailException;
import com.hunter.wallet.exception.WalletLockedException;
import com.hunter.wallet.service.TxManageService;
import okhttp3.FormBody;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @auther: zb
 * @Date: 2018/11/5 15:35
 * @Description: 比特币相关工具类
 */
public class BtcUtil {

    //浏览器选择：测试网/主网
    public static final String BTC_URL = "https://testnet.blockchain.info/tx/";
    /* public static final String BTC_URL = "https://www.blockchain.com/btc/tx/";*/

    //请求节点地址，测试网节点
    private static String baseUrl = "http://192.168.10.69:3001/insight-api";
    //请求节点地址，主网节点
    /*private static String baseUrl = "http://192.168.10.56:3001/insight-api";*/

    //测试网或主网
    private static NetworkParameters networkParameters = TestNet3Params.get();
    /*private static NetworkParameters networkParameters = MainNetParams.get();*/

    //校验比特币地址
    public static boolean isInvalidAddress(String base58ToAddress) {
        try {
            Address.fromBase58(networkParameters, base58ToAddress);
            return true;
        } catch (AddressFormatException e) {
            return false;
        }
    }

    //压缩公钥
    public static byte[] compressPubkey(byte[] unCompressPubkey){
        byte[] newUnCompressPubkey = new byte[unCompressPubkey.length + 1];
        newUnCompressPubkey[0] = 4;
        for (int i=0; i<unCompressPubkey.length; i++) {
            newUnCompressPubkey[i + 1] = unCompressPubkey[i];
        }
        byte[] compressPubkey = ECKey.CURVE.getCurve().decodePoint(newUnCompressPubkey).getEncoded(true);
        return compressPubkey;
    }

    //根据公钥计算比特币地址
    public static String getAddress(byte[] pubKey) {
        return new Address(networkParameters, Utils.sha256hash160(compressPubkey(pubKey))).toString();
    }

    //获取地址余额
    public static String getBalance(String address) throws IOException {
        String url = baseUrl + "/addr/" + address + "/balance";
        String result = HTTPUtils.doGetSync(url);

        if (result != null && result != ""){
            Integer balance = Integer.valueOf(result);
            return String.valueOf(Coin.valueOf(balance).toPlainString());
        } else {
            return null;
        }
    }

    //获取比特币汇率
    public static String getRate() {
        List<Price> list = HTTPUtils.getList("http://wallet.hdayun.com/market/getPrice?start=0&limit=" + 1, Price.class);

        if (null != list && list.size() != 0) {
            Double rate = list.get(0).getPriceUSD();
            return String.valueOf(rate);
        } else {
            return null;
        }
    }

    //根据地址获取交易列表
    public static List<BtcTx> getTxByAddr(String addr) throws IOException {
        List<BtcTx> btcTxList = new ArrayList<>();
        String url = baseUrl + "/txs?address=" + addr;

        String result = HTTPUtils.doGetSync(url);
        if (result != null && result != "") {
            JSONArray txsArray = JSON.parseObject(result).getJSONArray("txs");
            List<JSONObject> txsList = txsArray.toJavaList(JSONObject.class);
            for (JSONObject tx : txsList) {
                BtcTx btcTx = BtcTx.fromJsonObject(tx, addr);
                btcTxList.add(btcTx);
            }
            return btcTxList;
        }else {
            return null;
        }
    }

    //获取比特币最近平均手续费
    public static String getEstimateFee() throws IOException {
        String url = baseUrl + "/utils/estimatefee";

        String result = HTTPUtils.doGetSync(url);
        if (result != null && result != "") {
            String estimateFee = JSON.parseObject(result).getString("2");
            return estimateFee;
        }else {
            return null;
        }
    }

    //发送交易
    public static boolean doBtcTransfer(String from, String to, String value, String fee, WalletInfo walletInfo, String passWord) throws IOException, WalletLockedException, UnexpectedException, VerifyFailException {
        Transaction transaction = new Transaction(networkParameters);

        long longValue = Coin.parseCoin(value).getValue();
        long longFee = Coin.parseCoin(fee).getValue();

        //获取发送方的utxo
        String siteUrl = "/addr/" + from + "/utxo";
        String url = baseUrl + siteUrl;
        String result = HTTPUtils.doGetSync(url);
        System.out.println(result);

        List<MyUTXO> myUtxos = JSON.parseArray(result, MyUTXO.class);
        List<UTXO> utxos = new ArrayList<>();
        long totalMoney = 0;
        //遍历未花费列表，组装合适的item
        for (MyUTXO myUTXO : myUtxos) {
            if (totalMoney >= (longValue + longFee))
                break;
            if (myUTXO.getConfirmations() > 0){
                UTXO utxo = new UTXO(Sha256Hash.wrap(myUTXO.getTxid()), myUTXO.getVout(), Coin.valueOf(myUTXO.getSatoshis()),
                        myUTXO.getHeight(), false, new Script(Hex.decode(myUTXO.getScriptPubKey())));
                utxos.add(utxo);
                totalMoney += myUTXO.getSatoshis();
            }
        }

        //输出-转给接收者
        transaction.addOutput(Coin.valueOf(longValue), Address.fromBase58(networkParameters, to));
        //消费列表总金额 - 已经转账的金额 - 手续费 就等于需要返回给自己的金额了
        long balance = totalMoney - longValue - longFee;
        if (balance < 0){
            return false;
        }
        //输出-转给自己
        if (balance > 0) {
            transaction.addOutput(Coin.valueOf(balance), Address.fromBase58(networkParameters, from));
        }

        //输入未消费列表项
        for (UTXO utxo : utxos) {
            TransactionOutPoint outPoint = new TransactionOutPoint(networkParameters, utxo.getIndex(), utxo.getHash());
            //交易输入签名
            signedInput(transaction, outPoint, utxo.getScript(), Transaction.SigHash.ALL, true, walletInfo, passWord);
        }
        String rawtx = Hex.toHexString(transaction.bitcoinSerialize());

        //广播交易
        FormBody formBody = new FormBody.Builder().add("rawtx",rawtx).build();//设置参数名称和参数值
        String postUrl = baseUrl + "/tx/send";
        String res = HTTPUtils.doPostSync(postUrl ,formBody);
        String txid = JSON.parseObject(res).getString("txid");
        if (txid != null && txid != ""){
            return true;
        }else {
            return false;
        }
    }

    //交易输入的签名
    public static TransactionInput signedInput(Transaction transaction, TransactionOutPoint prevOut, Script scriptPubKey, Transaction.SigHash sigHash, boolean anyoneCanPay, WalletInfo walletInfo, String passWord) throws ScriptException, WalletLockedException, UnexpectedException, VerifyFailException {
        Preconditions.checkState(!transaction.getOutputs().isEmpty(), "Attempting to sign tx without outputs.");
        TransactionInput input = new TransactionInput(transaction.getParams(), transaction, new byte[0], prevOut);
        transaction.addInput(input);
        Sha256Hash hash = transaction.hashForSignature(transaction.getInputs().size() - 1, scriptPubKey, sigHash, anyoneCanPay);

        //调用底层Tee签名
        TxManageService txManageService= TxManageService.getInstance();
        Secp256k1Signature signature = txManageService.signMsg(walletInfo.getId(), passWord, hash.getBytes());

        //底层签名结果转为通用签名结果
        ECKey.ECDSASignature ecSig = new ECKey.ECDSASignature(new BigInteger(signature.getR()), new BigInteger(signature.getS()));
        //构建签名交易
        TransactionSignature txSig = new TransactionSignature(ecSig, sigHash, anyoneCanPay);

        if (scriptPubKey.isSentToRawPubKey()) {
            input.setScriptSig(ScriptBuilder.createInputScript(txSig));
        } else {
            if (!scriptPubKey.isSentToAddress()) {
                throw new ScriptException("Don't know how to sign for this kind of scriptPubKey: " + scriptPubKey);
            }

            //设置交易脚本，公钥信息
            byte[] pubkeyBytes = walletInfo.getPubkey();
            byte[] sigBytes = signature != null ? txSig.encodeToBitcoin() : new byte[0];
            input.setScriptSig((new ScriptBuilder()).data(sigBytes).data(compressPubkey(pubkeyBytes)).build());
        }
        return input;
    }
}
