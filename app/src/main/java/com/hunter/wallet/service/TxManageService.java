package com.hunter.wallet.service;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hunter.wallet.common.Constants;
import com.hunter.wallet.entity.BtcToken;
import com.hunter.wallet.entity.BtcTransfer;
import com.hunter.wallet.entity.EthTransfer;
import com.hunter.wallet.entity.Secp256k1Signature;
import com.hunter.wallet.entity.EthToken;
import com.hunter.wallet.entity.Token;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.VerifyFailException;
import com.hunter.wallet.exception.WalletLockedException;
import com.hunter.wallet.utils.JsonUtils;
import com.hunter.wallet.utils.SharedPreferencesUtils;
import com.hunter.wallet.utils.Web3jUtil;

import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Bytes;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TxManageService {

    static {
        System.loadLibrary("wsservice");
    }

    private static TxManageService instance = new TxManageService();

    private TxManageService() {
    }

    public static TxManageService getInstance() {
        return instance;
    }

    public native Secp256k1Signature signMsg(int id, String password, byte[] msgHash)
            throws WalletLockedException, VerifyFailException, UnexpectedException;


    public byte[] signEthRawTransaction(WalletInfo walletInfo, String password, RawTransaction rawTransaction)
            throws UnexpectedException, VerifyFailException, WalletLockedException {

        byte[] encodedTransaction = encode(rawTransaction);
        byte[] messageHash = Hash.sha3(encodedTransaction);

        Secp256k1Signature signature = signMsg(walletInfo.getId(), password, messageHash);

        ECDSASignature sig = new ECDSASignature(new BigInteger(signature.getR()), new BigInteger(signature.getS()));
        int recId = -1;
        for (int i = 0; i < 4; i++) {
            BigInteger k = Sign.recoverFromSignature(i, sig, messageHash);
            if (k != null && k.equals(new BigInteger(1, walletInfo.getPubkey()))) {
                recId = i;
                break;
            }
        }
        if (recId == -1) {
            throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        }
        int headerByte = recId + 27;
        return encode(rawTransaction, new Sign.SignatureData((byte) headerByte, signature.getR(), signature.getS()));
    }

    private byte[] encode(RawTransaction rawTransaction) {
        return encode(rawTransaction, null);
    }

    private byte[] encode(RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> values = asRlpValues(rawTransaction, signatureData);
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }

    private List<RlpType> asRlpValues(RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> result = new ArrayList<>();

        result.add(RlpString.create(rawTransaction.getNonce()));
        result.add(RlpString.create(rawTransaction.getGasPrice()));
        result.add(RlpString.create(rawTransaction.getGasLimit()));

        // an empty to address (contract creation) should not be encoded as a numeric 0 value
        String to = rawTransaction.getTo();
        if (to != null && to.length() > 0) {
            // addresses that start with zeros should be encoded with the zeros included, not
            // as numeric values
            result.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            result.add(RlpString.create(""));
        }

        result.add(RlpString.create(rawTransaction.getValue()));

        // value field will already be hex encoded, so we need to convert into binary first
        byte[] data = Numeric.hexStringToByteArray(rawTransaction.getData());
        result.add(RlpString.create(data));

        if (signatureData != null) {
            result.add(RlpString.create(signatureData.getV()));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
        }

        return result;
    }


//    public BigDecimal getWalletBalance(Context context, WalletInfo walletInfo, EthToken ethToken) {
//        EthTransferCache cache = loadEthTransferCache(context, walletInfo);
//        BigDecimal balance = cache.getBalanceCacheMap().get(Numeric.toHexString(ethToken.getContract()));
//        if (balance == null) {
//            balance = BigDecimal.ZERO;
//        }
//        return balance;
//    }

//
//    public void updateEthTransferCache(Context context, WalletInfo walletInfo, List<EthToken> ethTokens) {
//        EthTransferCache ethTransferCache = loadEthTransferCache(context, walletInfo);
//        BigInteger startBlock = ethTransferCache.getLastUpdateBlocknum().add(new BigInteger("1"));
//        BigInteger endBlock = getEthRecentBlock(context);
//        if (startBlock.compareTo(endBlock) >= 0) {
//            return;
//        }
//        try {
//            for (EthToken ethToken : ethTokens) {
//                String contract = Numeric.toHexString(ethToken.getContract());
//                List<EthTransfer> ethTransferList = ethTransferCache.getTransferCacheMap().get(contract);
//                if (ethTransferList == null) {
//                    ethTransferList = new ArrayList<>();
//                    ethTransferCache.getTransferCacheMap().put(contract, ethTransferList);
//                }
//                if (ethToken.getType() == EthToken.TOKEN_TYPE_ETH) {
//                    ethTransferCache.getBalanceCacheMap().put(contract, EtherscanUtils.getEthBalance(walletInfo.getAddr()));
//                    for (EtherscanUtils.Transaction transaction : EtherscanUtils.getTransactions(walletInfo.getAddr(), startBlock, endBlock)) {
//                        if (!StringUtils.hasText(transaction.getContractAddress()) && transaction.getInput().toLowerCase().equals("0x")) {
//                            EthTransfer ethTransfer = new EthTransfer();
//                            ethTransfer.setFrom(Numeric.hexStringToByteArray(transaction.getFrom()));
//                            ethTransfer.setTo(Numeric.hexStringToByteArray(transaction.getTo()));
//                            ethTransfer.setGas(new BigInteger(transaction.getGas()));
//                            ethTransfer.setGasUsed(new BigInteger(transaction.getGasUsed()));
//                            ethTransfer.setValue(new BigInteger(transaction.getValue()));
//                            ethTransfer.setGasPrice(new BigInteger(transaction.getGasPrice()));
//                            ethTransfer.setHash(transaction.getHash());
//                            ethTransfer.setTime(new Date(Long.valueOf(transaction.getTimeStamp()) * 1000));
//                            ethTransfer.setBlockNumber(transaction.getBlockNumber());
//                            ethTransfer.setStatus(EthTransfer.STATUS_SUCCESS);
//                            ethTransferList.add(ethTransfer);
//                        }
//                    }
//                } else {
//                    ethTransferCache.getBalanceCacheMap().put(contract, EtherscanUtils.getWalletBalance(walletInfo.getAddr(), ethToken.getContract(), ethToken.getDecimals()));
//                    for (EtherscanUtils.TokenTx tokenTx : EtherscanUtils.getTokenTx(walletInfo.getAddr(), ethToken.getContract(), startBlock, endBlock)) {
//                        EthTransfer ethTransfer = new EthTransfer();
//                        ethTransfer.setFrom(Numeric.hexStringToByteArray(tokenTx.getFrom()));
//                        ethTransfer.setTo(Numeric.hexStringToByteArray(tokenTx.getTo()));
//                        ethTransfer.setGas(new BigInteger(tokenTx.getGas()));
//                        ethTransfer.setGasUsed(new BigInteger(tokenTx.getGasUsed()));
//                        ethTransfer.setValue(new BigInteger(tokenTx.getValue()));
//                        ethTransfer.setGasPrice(new BigInteger(tokenTx.getGasPrice()));
//                        ethTransfer.setHash(tokenTx.getHash());
//                        ethTransfer.setTime(new Date(Long.valueOf(tokenTx.getTimeStamp()) * 1000));
//                        ethTransfer.setBlockNumber(tokenTx.getBlockNumber());
//                        ethTransfer.setStatus(EthTransfer.STATUS_SUCCESS);
//                        ethTransferList.add(ethTransfer);
//                    }
//                }
//            }
//            ethTransferCache.setLastUpdateBlocknum(endBlock);
//            saveEthTransferCache(context, walletInfo, ethTransferCache);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//    public static class EthTransferCache {
//        private BigInteger lastUpdateBlocknum = new BigInteger("-1");
//        private Map<String, BigDecimal> balanceCacheMap = new HashMap<>();
//        private Map<String, List<EthTransfer>> transferCacheMap = new HashMap<>();
//
//
//        BigInteger getLastUpdateBlocknum() {
//            return lastUpdateBlocknum;
//        }
//
//        void setLastUpdateBlocknum(BigInteger lastUpdateBlocknum) {
//            this.lastUpdateBlocknum = lastUpdateBlocknum;
//        }
//
//        Map<String, BigDecimal> getBalanceCacheMap() {
//            return balanceCacheMap;
//        }
//
//        public void setBalanceCacheMap(Map<String, BigDecimal> balanceCacheMap) {
//            this.balanceCacheMap = balanceCacheMap;
//        }
//
//        Map<String, List<EthTransfer>> getTransferCacheMap() {
//            return transferCacheMap;
//        }
//
//        public void setTransferCacheMap(Map<String, List<EthTransfer>> transferCacheMap) {
//            this.transferCacheMap = transferCacheMap;
//        }
//    }

//    private void saveEthTransferCache(Context context, WalletInfo walletInfo, EthTransferCache ethTransferCache) {
//        SharedPreferencesUtils.writeString(context, Constants.preferences_eth_transfer_cache, Numeric.toHexString(walletInfo.getAddr()), JsonUtils.objectToJson(ethTransferCache));
//    }
//
//    private EthTransferCache loadEthTransferCache(Context context, WalletInfo walletInfo) {
//        EthTransferCache ethTransferCache = null;
//        String s = SharedPreferencesUtils.getString(context, Constants.preferences_eth_transfer_cache, Numeric.toHexString(walletInfo.getAddr()), null);
//        if (StringUtils.hasText(s)) {
//            ethTransferCache = JsonUtils.jsonToPojo(s, EthTransferCache.class);
//        }
//        if (ethTransferCache == null) {
//            ethTransferCache = new EthTransferCache();
//        }
//        return ethTransferCache;
//    }

    public void saveEthGasPrice(Context context, BigInteger gasPrice) {
        Log.i("saveEthGasPrice", gasPrice.toString());
        SharedPreferencesUtils.writeString(context, Constants.preferences_global_cache, "ethGasPrice", JsonUtils.objectToJson(gasPrice));
    }

    public BigInteger getEthGasPrice(Context context) {
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_global_cache, "ethGasPrice", "1000000000");
        BigInteger gasPrice = JsonUtils.jsonToPojo(s, BigInteger.class);
        if (gasPrice == null) {
            gasPrice = new BigInteger("1000000000");
        }
        Log.i("getEthGasPrice", gasPrice.toString());
        return gasPrice;
    }

    public void saveEthRecentBlock(Context context, BigInteger recentBlock) {
        Log.i("saveEthRecentBlock", recentBlock.toString());
        SharedPreferencesUtils.writeString(context, Constants.preferences_global_cache, "ethRecentBlock", JsonUtils.objectToJson(recentBlock));
    }

    public BigInteger getEthRecentBlock(Context context) {
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_global_cache, "ethRecentBlock", "0");
        BigInteger recentBlock = JsonUtils.jsonToPojo(s, BigInteger.class);
        if (recentBlock == null) {
            recentBlock = BigInteger.ZERO;
        }
        Log.i("getEthRecentBlock", recentBlock.toString());
        return recentBlock;
    }

    public void saveBtcRecentBlock(Context context, BigInteger recentBlock) {
        SharedPreferencesUtils.writeString(context, Constants.preferences_global_cache, "btcRecentBlock", JsonUtils.objectToJson(recentBlock));
    }

    public BigInteger getBtcRecentBlock(Context context) {
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_global_cache, "btcRecentBlock", "0");
        BigInteger recentBlock = JsonUtils.jsonToPojo(s, BigInteger.class);
        if (recentBlock == null) {
            recentBlock = BigInteger.ZERO;
        }
        return recentBlock;
    }

    public void saveWalletBalance(Context context, WalletInfo walletInfo, Token token, BigDecimal balance) {

        String addr = Numeric.toHexString(walletInfo.getAddr());
        Log.i("saveWalletBalance", addr + "/ " + token.getUid() + "/ " + balance);
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_wallet_balance, addr, "");
        Map<String, BigDecimal> balanceMap = JsonUtils.readValue(s, new TypeReference<Map<String, BigDecimal>>() {
        });
        if (balanceMap == null) {
            balanceMap = new HashMap<>();
        }
        balanceMap.put(token.getUid(), balance);
        SharedPreferencesUtils.writeString(context, Constants.preferences_wallet_balance, addr, JsonUtils.objectToJson(balanceMap));
    }

    public BigDecimal getWalletBalance(Context context, WalletInfo walletInfo, Token token) {
        BigDecimal balance = null;
        String addr = Numeric.toHexString(walletInfo.getAddr());
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_wallet_balance, addr, "");
        Map<String, BigDecimal> balanceMap = JsonUtils.readValue(s, new TypeReference<Map<String, BigDecimal>>() {
        });
        if (balanceMap != null) {
            balance = balanceMap.get(token.getUid());
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        Log.i("getWalletBalance", addr + "/ " + token.getUid() + "/ " + balance);
        return balance;
    }


    public synchronized void updateTransfer(Context context, WalletInfo walletInfo, EthToken ethToken, List<EthTransfer> ethTransfers, BigInteger endBlock) {
        String addr = Numeric.toHexString(walletInfo.getAddr());
        Log.i("updateTransfer", addr + "/ " + ethToken.getUid() + "/ " + endBlock);
        for (EthTransfer transfer : ethTransfers) {
            Log.i("updateTransfer", transfer.getHash());
        }
        if (ethTransfers != null && ethTransfers.size() > 0) {
            String s = SharedPreferencesUtils.getString(context, Constants.preferences_eth_transfer, addr, "");
            Map<String, List<EthTransfer>> transferMap = JsonUtils.readValue(s, new TypeReference<Map<String, List<EthTransfer>>>() {
            });
            if (transferMap == null) {
                transferMap = new HashMap<>();
            }
            List<EthTransfer> list = transferMap.get(ethToken.getUid());
            if (list == null) {
                list = new ArrayList<>();
                transferMap.put(ethToken.getUid(), list);
            }
            for (EthTransfer ethTransfer : ethTransfers) {
                if (!list.contains(ethTransfer)) {
                    list.add(ethTransfer);
                }
            }
            SharedPreferencesUtils.writeString(context, Constants.preferences_eth_transfer, addr, JsonUtils.objectToJson(transferMap));
        }
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_eth_transfer_endblock, addr, "");
        Map<String, BigInteger> endBlockMap = JsonUtils.readValue(s, new TypeReference<Map<String, BigInteger>>() {
        });
        if (endBlockMap == null) {
            endBlockMap = new HashMap<>();
        }
        BigInteger lastEndBlock = endBlockMap.get(ethToken.getUid());
        if (lastEndBlock == null || lastEndBlock.compareTo(endBlock) < 0) {
            endBlockMap.put(ethToken.getUid(), endBlock);
            SharedPreferencesUtils.writeString(context, Constants.preferences_eth_transfer_endblock, addr, JsonUtils.objectToJson(endBlockMap));
        }

    }

    public BigInteger getTransferLastUpdateBlock(Context context, WalletInfo walletInfo, EthToken ethToken) {
        BigInteger lastEndBlock = null;
        String addr = Numeric.toHexString(walletInfo.getAddr());
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_eth_transfer_endblock, addr, "");
        Map<String, BigInteger> endBlockMap = JsonUtils.readValue(s, new TypeReference<Map<String, BigInteger>>() {
        });
        if (endBlockMap != null) {
            lastEndBlock = endBlockMap.get(ethToken.getUid());
        }
        return lastEndBlock != null ? lastEndBlock : BigInteger.ZERO;
    }

    public List<EthTransfer> getEthTokenTransfer(Context context, WalletInfo walletInfo, EthToken ethToken) {
        List<EthTransfer> list = null;
        String addr = Numeric.toHexString(walletInfo.getAddr());
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_eth_transfer, addr, "");
        Map<String, List<EthTransfer>> transferMap = JsonUtils.readValue(s, new TypeReference<Map<String, List<EthTransfer>>>() {
        });
        if (transferMap != null) {
            list = transferMap.get(ethToken.getUid());
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public void updateTransfer(Context context, WalletInfo walletInfo, BtcToken btcToken, List<BtcTransfer> btcTransfers, BigInteger endBlock) {

    }

    public BigInteger getTransferLastUpdateBlock(Context context, WalletInfo walletInfo, BtcToken btcToken) {
        return BigInteger.ZERO;
    }

    public List<BtcTransfer> getBtcTokenTransfer(Context context, WalletInfo walletInfo, BtcToken btcToken) {
        return null;
    }

    public List<EthTransfer> getEthTransfer(Context context, WalletInfo walletInfo, EthToken ethToken) {
        List<EthTransfer> ethTransfers = new ArrayList<>();
        List<EthTransfer> cacheEthTransfers = getEthTokenTransfer(context, walletInfo, ethToken);
        List<EthTransfer> uncommitEthTransfers = getEthUncommitTransfer(context, walletInfo, ethToken);
        if (cacheEthTransfers != null) {
            ethTransfers.addAll(cacheEthTransfers);
        }
        for (EthTransfer ethTransfer : uncommitEthTransfers) {
            if (ethTransfers.contains(ethTransfer)) {
                removeEthUncommitTransfer(context, walletInfo, ethToken, ethTransfer);
            }
        }
        ethTransfers.addAll(uncommitEthTransfers);
        Collections.sort(ethTransfers, new Comparator<EthTransfer>() {
            @Override
            public int compare(EthTransfer o1, EthTransfer o2) {
                return o2.getTime().compareTo(o1.getTime());
            }
        });
        return ethTransfers;
    }


    public boolean doEthTransfer(Context context, WalletInfo walletInfo, EthToken ethToken, String password, byte[] to, BigDecimal amount, BigInteger gasPrice, BigInteger gasLimit)
            throws WalletLockedException, UnexpectedException, VerifyFailException {
        BigInteger value = amount.multiply(BigDecimal.TEN.pow(ethToken.getDecimals())).toBigInteger();
        RawTransaction rawTransaction = null;
        try {
            if (ethToken.getType() == EthToken.TOKEN_TYPE_ETH) {
                rawTransaction = Web3jUtil.createEthTransferRawTransaction(Numeric.toHexString(walletInfo.getAddr()), Numeric.toHexString(to), gasPrice, gasLimit, value);
            } else {
                rawTransaction = Web3jUtil.createTokenTransferRawTransaction(Numeric.toHexString(walletInfo.getAddr()), Numeric.toHexString(ethToken.getContract()), Numeric.toHexString(to), gasPrice, gasLimit, value);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (rawTransaction == null) {
            return false;
        }
        byte[] signedMsg = signEthRawTransaction(walletInfo, password, rawTransaction);
        String hash = null;
        try {
            hash = Web3jUtil.sendTransaction(Numeric.toHexString(signedMsg));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (hash == null) {
            return false;
        }
        EthTransfer ethTransfer = new EthTransfer();
        ethTransfer.setFrom(walletInfo.getAddr());
        ethTransfer.setTo(to);
        ethTransfer.setValue(value);
        ethTransfer.setGas(gasLimit);
        ethTransfer.setGasUsed(gasLimit);
        ethTransfer.setGasPrice(gasPrice);
        ethTransfer.setTime(new Date());
        ethTransfer.setHash(hash);
        ethTransfer.setStatus(EthTransfer.STATUS_UNCOMMIT);
        saveEthUncommitTransfer(context, walletInfo, ethToken, ethTransfer);
        return true;
    }

    private void saveEthUncommitTransfer(Context context, WalletInfo walletInfo, EthToken ethToken, EthTransfer ethTransfer) {
        String addr = Numeric.toHexString(walletInfo.getAddr());
        String contract = Numeric.toHexString(ethToken.getContract());
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_eth_uncommit_transfer, addr, "");
        Map<String, List<EthTransfer>> map = JsonUtils.readValue(s, new TypeReference<Map<String, List<EthTransfer>>>() {
        });
        if (map == null) {
            map = new HashMap<>();
        }
        List<EthTransfer> ethTransfers = map.get(contract);
        if (ethTransfers == null) {
            ethTransfers = new ArrayList<>();
            map.put(contract, ethTransfers);
        }
        if (!ethTransfers.contains(ethTransfer)) {
            ethTransfers.add(ethTransfer);
            SharedPreferencesUtils.writeString(context, Constants.preferences_eth_uncommit_transfer, addr, JsonUtils.objectToJson(map));
        }
    }

    private void removeEthUncommitTransfer(Context context, WalletInfo walletInfo, EthToken ethToken, EthTransfer ethTransfer) {
        String addr = Numeric.toHexString(walletInfo.getAddr());
        String contract = Numeric.toHexString(ethToken.getContract());
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_eth_uncommit_transfer, addr, "");
        Map<String, List<EthTransfer>> map = JsonUtils.readValue(s, new TypeReference<Map<String, List<EthTransfer>>>() {
        });
        if (map != null) {
            List<EthTransfer> ethTransfers = map.get(contract);
            if (ethTransfers != null && ethTransfers.contains(ethTransfer)) {
                ethTransfers.remove(ethTransfer);
                SharedPreferencesUtils.writeString(context, Constants.preferences_eth_uncommit_transfer, addr, JsonUtils.objectToJson(map));
            }
        }
    }

    private List<EthTransfer> getEthUncommitTransfer(Context context, WalletInfo walletInfo, EthToken ethToken) {
        List<EthTransfer> ethTransfers = null;
        String addr = Numeric.toHexString(walletInfo.getAddr());
        String contract = Numeric.toHexString(ethToken.getContract());
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_eth_uncommit_transfer, addr, "");
        Map<String, List<EthTransfer>> map = JsonUtils.readValue(s, new TypeReference<Map<String, List<EthTransfer>>>() {
        });
        if (map != null) {
            ethTransfers = map.get(contract);
        }
        if (ethTransfers == null) {
            ethTransfers = new ArrayList<>();
        }
        return ethTransfers;
    }

}
