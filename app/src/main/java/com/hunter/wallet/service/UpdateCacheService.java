package com.hunter.wallet.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.hunter.wallet.common.Constants;
import com.hunter.wallet.entity.BtcToken;
import com.hunter.wallet.entity.EthToken;
import com.hunter.wallet.entity.EthTransfer;
import com.hunter.wallet.entity.Token;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.utils.AppUtils;
import com.hunter.wallet.utils.EtherscanUtils;
import com.hunter.wallet.utils.StringUtils;

import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateCacheService extends Service {
    private WalletManageService walletManageService = WalletManageService.getInstance();
    private TxManageService txManageService = TxManageService.getInstance();
    private TokenManageService tokenManageService = TokenManageService.getInstance();

    private Timer updateTimer;
    private Context context;

    public UpdateCacheService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("wallet_application_onResume");
        intentFilter.addAction("wallet_application_onPaused");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("wallet_application_onResume") && AppUtils.isFront(context) && updateTimer == null) {
                    // 应用在前台启动更新
                    updateTimer = new Timer();
                    updateTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            doUpdate();
                        }
                    }, 0, Constants.period_update_cache);
                } else if (action.equals("wallet_application_onPaused") && !AppUtils.isFront(context) && updateTimer != null) {
                    // 应用在后台停止更新
                    updateTimer.cancel();
                    updateTimer = null;
                }
            }
        }, intentFilter);
    }

    private void doUpdate() {
        updatePrice();
        updateTransfer();
    }

    // 更新行情
    private void updatePrice() {
        for (Token token : tokenManageService.getAllTokens(context)) {
            switch (token.getTokenType()) {
                case eth: {
                    EthToken ethToken = (EthToken) token;
                    if (ethToken.getType() == EthToken.TOKEN_TYPE_ETH) {
                        try {
                            tokenManageService.saveTokenPriceUsd(context, ethToken, EtherscanUtils.getEthPriceUsd());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        tokenManageService.saveTokenPriceUsd(context, ethToken, BigDecimal.ZERO);
                    }
                }
                break;
                case btc: {
                    BtcToken btcToken = (BtcToken) token;
                    tokenManageService.saveTokenPriceUsd(context, btcToken, BigDecimal.ZERO);
                }
                break;
                default:
                    tokenManageService.saveTokenPriceUsd(context, token, BigDecimal.ZERO);
                    break;
            }
        }
    }

    //更新交易信息
    private void updateTransfer() {
        updateGasPrice();
        updateEthResentBlock();
        updateBtcResentBlock();

        try {
            for (WalletInfo walletInfo : walletManageService.getAllWallet()) {

                List<Token> tokens = tokenManageService.getWalletSelectToken(context, walletInfo);
                for (Token token : tokens) {
                    updateBalance(walletInfo, token);
                    updateTransfer(walletInfo, token);
                }
            }
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
    }

    private void updateGasPrice() {
        try {
            txManageService.saveEthGasPrice(context, EtherscanUtils.getGasPrice());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateEthResentBlock() {
        try {
            txManageService.saveEthRecentBlock(context, EtherscanUtils.getRecentBlock());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateBtcResentBlock() {
        // TODO 更新BTC区块信息
    }

    // 更新余额
    private void updateBalance(WalletInfo walletInfo, Token token) {
        try {
            BigDecimal balance = BigDecimal.ZERO;
            switch (token.getTokenType()) {
                case eth: {
                    EthToken ethToken = (EthToken) token;
                    if (ethToken.getType() == EthToken.TOKEN_TYPE_ETH) {
                        balance = EtherscanUtils.getEthBalance(walletInfo.getAddr());
                    } else {
                        balance = EtherscanUtils.getTokenBalance(walletInfo.getAddr(), ethToken.getContract(), ethToken.getDecimals());
                    }
                }
                break;
                case btc: {
                    //  TODO
                    balance = BigDecimal.ZERO;
                }
                break;
            }
            txManageService.saveWalletBalance(context, walletInfo, token, balance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTransfer(WalletInfo walletInfo, Token token) {
        try {
            switch (token.getTokenType()) {
                case eth: {
                    EthToken ethToken = (EthToken) token;
                    BigInteger startBlock = txManageService.getTransferLastUpdateBlock(context, walletInfo, ethToken).add(BigInteger.ONE);
                    BigInteger endBlock = txManageService.getEthRecentBlock(context);
                    List<EthTransfer> transfers = new ArrayList<>();
                    if (ethToken.getType() == EthToken.TOKEN_TYPE_ETH) {
                        for (EtherscanUtils.Transaction transaction : EtherscanUtils.getTransactions(walletInfo.getAddr(), startBlock, endBlock)) {
                            if (!StringUtils.hasText(transaction.getContractAddress()) && transaction.getInput().toLowerCase().equals("0x")) {
                                EthTransfer ethTransfer = new EthTransfer();
                                ethTransfer.setFrom(Numeric.hexStringToByteArray(transaction.getFrom()));
                                ethTransfer.setTo(Numeric.hexStringToByteArray(transaction.getTo()));
                                ethTransfer.setGas(new BigInteger(transaction.getGas()));
                                ethTransfer.setGasUsed(new BigInteger(transaction.getGasUsed()));
                                ethTransfer.setValue(new BigInteger(transaction.getValue()));
                                ethTransfer.setGasPrice(new BigInteger(transaction.getGasPrice()));
                                ethTransfer.setHash(transaction.getHash());
                                ethTransfer.setTime(new Date(Long.valueOf(transaction.getTimeStamp()) * 1000));
                                ethTransfer.setBlockNumber(transaction.getBlockNumber());
                                ethTransfer.setStatus(EthTransfer.STATUS_SUCCESS);
                                transfers.add(ethTransfer);
                            }
                        }
                    } else {
                        for (EtherscanUtils.TokenTx tokenTx : EtherscanUtils.getTokenTx(walletInfo.getAddr(), ethToken.getContract(), startBlock, endBlock)) {
                            EthTransfer ethTransfer = new EthTransfer();
                            ethTransfer.setFrom(Numeric.hexStringToByteArray(tokenTx.getFrom()));
                            ethTransfer.setTo(Numeric.hexStringToByteArray(tokenTx.getTo()));
                            ethTransfer.setGas(new BigInteger(tokenTx.getGas()));
                            ethTransfer.setGasUsed(new BigInteger(tokenTx.getGasUsed()));
                            ethTransfer.setValue(new BigInteger(tokenTx.getValue()));
                            ethTransfer.setGasPrice(new BigInteger(tokenTx.getGasPrice()));
                            ethTransfer.setHash(tokenTx.getHash());
                            ethTransfer.setTime(new Date(Long.valueOf(tokenTx.getTimeStamp()) * 1000));
                            ethTransfer.setBlockNumber(tokenTx.getBlockNumber());
                            ethTransfer.setStatus(EthTransfer.STATUS_SUCCESS);
                            transfers.add(ethTransfer);
                        }
                    }
                    txManageService.updateTransfer(context, walletInfo, ethToken, transfers, endBlock);
                }
                break;
                case btc: {
                    //  TODO
                }
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
