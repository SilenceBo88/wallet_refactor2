package com.hunter.wallet.service;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hunter.wallet.R;
import com.hunter.wallet.common.Constants;
import com.hunter.wallet.entity.BtcToken;
import com.hunter.wallet.entity.EthToken;
import com.hunter.wallet.entity.Token;
import com.hunter.wallet.enums.TokenType;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.utils.JsonUtils;
import com.hunter.wallet.utils.SharedPreferencesUtils;
import com.hunter.wallet.utils.StringUtils;
import com.hunter.wallet.utils.Web3jUtils;

import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TokenManageService {

    private static final TokenManageService instance = new TokenManageService();

    private TokenManageService() {

    }

    public static TokenManageService getInstance() {
        return instance;
    }

    // 默认显示代币
    public List<Token> getDefaultToken(Context context) {
        List<Token> list = new ArrayList<>();
        list.add(new BtcToken("Bitcoin", "BTC ", R.drawable.coin_gnt_icon, null, "BTC"));
        list.add(new EthToken("Ethereum", "ETH", R.drawable.coin_eth, null, Numeric.hexStringToByteArray("0x0000000000000000000000000000000000000000"), 18, EthToken.TOKEN_TYPE_ETH));
        return list;
    }

    // 预置代币
    public List<Token> getPreToken(Context context) {
        List<Token> list = new ArrayList<>();
        list.add(new BtcToken("Bitcash", "BCH", R.drawable.coin_gnt_icon, null, "BCH"));
        list.add(new EthToken("KBI", "KBI", R.drawable.coin_eth, null, Numeric.hexStringToByteArray("0x6f6eef16939b8327d53afdcaf08a72bba99c1a7f"), 18, EthToken.TOKEN_TYPE_PRE));
        list.add(new EthToken("BNB", "BNB", R.drawable.coin_eth, null, Numeric.hexStringToByteArray("0xB8c77482e45F1F44dE1745F52C74426C631bDD52"), 18, EthToken.TOKEN_TYPE_PRE));
        return list;
    }

    //获取用户添加ETH代币
    public List<EthToken> getEthImportedToken(Context context) {
        List<EthToken> ethTokens = new ArrayList<>();
        Map<String, ?> tokenMap = SharedPreferencesUtils.getAll(context, Constants.preferences_eth_imported_token);
        if (tokenMap != null) {
            for (Object object : tokenMap.values()) {
                ethTokens.add(JsonUtils.jsonToPojo((String) object, EthToken.class));
            }
        }
        return ethTokens;
    }

    // 获取所有代币
    public List<Token> getAllTokens(Context context) {
        List<Token> tokens = new ArrayList<>();
        tokens.addAll(getDefaultToken(context));
        tokens.addAll(getPreToken(context));
        tokens.addAll(getEthImportedToken(context));
        return tokens;
    }

    // 获取所有ETH类型代币
    private List<EthToken> getAllEthToken(Context context) {
        List<EthToken> ethTokens = new ArrayList<>();
        for (Token token : getAllTokens(context)) {
            if (token.getTokenType() == TokenType.eth) {
                ethTokens.add((EthToken) token);
            }
        }
        return ethTokens;
    }

    public interface ImportEthTokenCallback {
        void onSuccess(String msg);

        void onFail(String msg);
    }

    // 导入ETH类型代币
    public void importEthToken(Context context, byte[] contract, ImportEthTokenCallback callback) {
        new Thread(() -> {
            if (contract.length != 20) {
                callback.onFail("合约地址格式错误");
                return;
            }
            for (EthToken ethToken : getAllEthToken(context)) {
                if (Arrays.equals(ethToken.getContract(), contract)) {
                    callback.onFail("该合约地址已存在，请勿重复添加");
                    return;
                }
            }
            String name = Web3jUtils.getTokenName(contract);
            String symbol = Web3jUtils.getTokenSymbol(contract);
            Integer decimals = Web3jUtils.getTokenDecimals(contract);
            if (StringUtils.hasText(name) && StringUtils.hasText(symbol) && decimals != null) {
                // TODO 查找代币图标
                EthToken ethToken = new EthToken(name, symbol, null, null, contract, decimals, EthToken.TOKEN_TYPE_IMPORT);
                SharedPreferencesUtils.writeString(context, Constants.preferences_eth_imported_token, Numeric.toHexString(contract), JsonUtils.objectToJson(ethToken));
                callback.onSuccess("添加成功");
            } else {
                callback.onFail("无效合约地址");
            }
        }).start();
    }

    // 钱包自选代币
    public List<Token> getWalletSelectToken(Context context, WalletInfo walletInfo) {
        List<Token> list = new ArrayList<>();
        // 默认显示代币
        list.addAll(getDefaultToken(context));
        String addr = Numeric.toHexString(walletInfo.getAddr());
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_wallet_selected_token, addr, "[]");
        List<String> uids = JsonUtils.readValue(s, new TypeReference<List<String>>() {
        });
        if (uids != null) {
            // 先遍历所有代币，确保钱包代币列表按所有代币列表顺序显示
            for (Token token : getAllTokens(context)) {
                for (String uid : uids) {
                    if (token.getUid().equals(uid)) {
                        list.add(token);
                    }
                }
            }
        }
        return list;
    }

    // 添加钱包自选代币
    public void addWalletSelectToken(Context context, WalletInfo walletInfo, Token token) {
        String addr = Numeric.toHexString(walletInfo.getAddr());
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_wallet_selected_token, addr, "[]");
        List<String> uids = JsonUtils.readValue(s, new TypeReference<List<String>>() {
        });
        if (uids == null) {
            uids = new ArrayList<>();
        }
        if (!uids.contains(token.getUid())) {
            uids.add(token.getUid());
            SharedPreferencesUtils.writeString(context, Constants.preferences_wallet_selected_token, addr, JsonUtils.objectToJson(uids));
        }
    }

    // 删除钱包自选代币
    public void removeWalletSelectToken(Context context, WalletInfo walletInfo, Token token) {
        String addr = Numeric.toHexString(walletInfo.getAddr());
        String s = SharedPreferencesUtils.getString(context, Constants.preferences_wallet_selected_token, addr, "[]");
        List<String> uids = JsonUtils.readValue(s, new TypeReference<List<String>>() {
        });
        if (uids != null && uids.contains(token.getUid())) {
            uids.remove(token.getUid());
            SharedPreferencesUtils.writeString(context, Constants.preferences_wallet_selected_token, addr, JsonUtils.objectToJson(uids));
        }
    }

    // 更新代币单价
    public void saveTokenPriceUsd(Context context, Token token, BigDecimal priceUsd) {
        Log.i("saveTokenPriceUsd", token.getUid() + " / " + priceUsd);
        SharedPreferencesUtils.writeString(context, Constants.preferences_token_price_usd, token.getUid(), priceUsd.toString());
    }

    // 获取代币单价
    public BigDecimal getTokenPriceUsd(Context context, Token token) {
        return new BigDecimal(SharedPreferencesUtils.getString(context, Constants.preferences_token_price_usd, token.getUid(), "0"));
    }

    public EthToken getEthToken(Context context) {
        return new EthToken("ETH", "Ethereum ", R.drawable.coin_eth, null, Numeric.hexStringToByteArray("0x0000000000000000000000000000000000000000"), 18, EthToken.TOKEN_TYPE_ETH);
    }
}
