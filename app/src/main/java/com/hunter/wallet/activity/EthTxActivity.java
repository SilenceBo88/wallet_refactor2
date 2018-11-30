package com.hunter.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.entity.EthToken;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.VerifyFailException;
import com.hunter.wallet.exception.WalletLockedException;
import com.hunter.wallet.service.TokenManageService;
import com.hunter.wallet.service.TxManageService;
import com.hunter.wallet.utils.AddressEncoder;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.StringUtils;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class EthTxActivity extends Activity implements View.OnClickListener, TextWatcher, SeekBar.OnSeekBarChangeListener {
    private TxManageService txManageService = TxManageService.getInstance();
    private TokenManageService tokenManageService = TokenManageService.getInstance();

    private static final int REQUEST_CODE_CAPTURE = 0;
    private static final int REQUEST_CODE_CONSTACT = 1;

    private static final int defaultPercent = 40;

    private WalletInfo wallet;
    private EthToken ethToken;
    private BigDecimal balance;

    private EditText toAddress;
    private EditText TNum;
    private TextView txPrice;
    private BigInteger gas;
    private BigInteger gasPrice;
    private LayoutInflater inflater;
    private Button transcationBut;

    private boolean tranBalance = false;
    private ImageView tranBalanceImg;
    private BigInteger gasPriceUnit;
    private BigDecimal gasTotal;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wallet = (WalletInfo) getIntent().getSerializableExtra(WalletInfo.class.getName());
        ethToken = (EthToken) getIntent().getSerializableExtra(EthToken.class.getName());
        balance = txManageService.getWalletBalance(this, wallet, ethToken);
        BigDecimal ethBalance = txManageService.getWalletBalance(this, wallet, tokenManageService.getEthToken(this));

        if(ethToken.getType()== EthToken.TOKEN_TYPE_ETH){
            gas = new BigInteger("21000");
        } else {
            gas = new BigInteger("60000");
        }
        gasPriceUnit = txManageService.getEthGasPrice(this).divide(BigInteger.valueOf(defaultPercent));

        // 判断ETH余额是否足以支付矿工费
        BigInteger gasMax = gasPriceUnit.multiply(BigInteger.valueOf(100)).multiply(gas);
        BigInteger ethBalanceWei = Convert.toWei(ethBalance, Convert.Unit.ETHER).toBigInteger();
        if (gasMax.compareTo(ethBalanceWei) > 0) {
            // 不足则最大矿工费=ETH余额
            gasPriceUnit = ethBalanceWei.divide(BigInteger.valueOf(100)).divide(gas);
        }

        setContentView(R.layout.tx_layout);
        inflater = getLayoutInflater();
        TextView tokenSymbol = findViewById(R.id.tokenSymbol);
        TextView tokenUnit = findViewById(R.id.tokenUnit);
        TextView num = findViewById(R.id.num);
        TNum = findViewById(R.id.TNum);
        toAddress = findViewById(R.id.toAddress);
        tranBalanceImg = findViewById(R.id.TranBalanceImg);
        txPrice = findViewById(R.id.txPrice);
        SeekBar seekBar = findViewById(R.id.seekBar);
        transcationBut = findViewById(R.id.TranscationBut);

        tokenSymbol.setText(ethToken.getSymbol());
        tokenUnit.setText(ethToken.getSymbol());
        num.setText(balance.toString());

        toAddress.addTextChangedListener(this);
        TNum.addTextChangedListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        transcationBut.setOnClickListener(this);
        findViewById(R.id.trxPreBut).setOnClickListener(this);
        findViewById(R.id.saoyisao).setOnClickListener(this);
        findViewById(R.id.selectContacts).setOnClickListener(this);
        findViewById(R.id.TranBalance).setOnClickListener(this);

        seekBar.setProgress(defaultPercent);
    }

    /**
     * 扫过二维码回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CAPTURE: {
                    // TODO 待优化
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");
                    try {
                        if (result.startsWith("0x") || result.startsWith("0X")) {
                            toAddress.setText(result);
                        } else if (result.startsWith("iban:XE") || result.startsWith("IBAN:XE")) {
                            toAddress.setText(AddressEncoder.decodeICAP(result).getAddress());
                        } else if (result.startsWith("iban:") || result.startsWith("IBAN:")) {
                            toAddress.setText(AddressEncoder.decodeLegacyLunary(result).getAddress());
                        } else if (result.startsWith("ethereum:") || result.startsWith("ETHEREUM:")) {
                            toAddress.setText(AddressEncoder.decodeERC(result).getAddress());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        RemindUtils.toastShort(EthTxActivity.this, "二维码解析失败");
                    }
                }
                break;
                case REQUEST_CODE_CONSTACT: {
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");
                    toAddress.setText(result);
                }
                break;
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.TranBalance:
                tranBalance = !tranBalance;
                if (tranBalance) {
                    tranBalanceImg.setImageResource(R.drawable.blue_on);
                    if (ethToken.getType() != EthToken.TOKEN_TYPE_ETH) {
                        TNum.setText(balance.toString());
                    } else {
                        TNum.setText(balance.subtract(gasTotal).toString());
                    }
                    TNum.setFocusable(false);
                    TNum.setFocusableInTouchMode(false);
                } else {
                    tranBalanceImg.setImageResource(R.drawable.blue_off);
                    TNum.setFocusable(true);
                    TNum.setFocusableInTouchMode(true);
                }
                break;
            case R.id.selectContacts: {
                startActivityForResult(new Intent(EthTxActivity.this, ContactsActivity.class), REQUEST_CODE_CONSTACT);
            }
            break;
            case R.id.saoyisao:
                startActivityForResult(new Intent(EthTxActivity.this, CaptureActivity.class), REQUEST_CODE_CAPTURE);
                break;
            case R.id.trxPreBut:
                EthTxActivity.this.finish();
                break;
            case R.id.TranscationBut: {
                // 防止重复点击
                transcationBut.setEnabled(false);
                byte[] to = Numeric.hexStringToByteArray(toAddress.getText().toString().trim());
                BigDecimal tNum = new BigDecimal(TNum.getText().toString().trim());
                BigDecimal gasNum = Convert.fromWei(new BigDecimal(gas.multiply(gasPrice)), Convert.Unit.ETHER);

                AlertDialog.Builder alertbBuilder = new AlertDialog.Builder(EthTxActivity.this);
                View orderView = inflater.inflate(R.layout.tx_order_layout, null);
                TextView toAddressMassage = orderView.findViewById(R.id.toAddressMassage);
                TextView payAddressMassage = orderView.findViewById(R.id.payAddressMassage);
                TextView costMassage = orderView.findViewById(R.id.costMassage);
                TextView numMassage = orderView.findViewById(R.id.numMassage);
                toAddressMassage.setText(toAddress.getText());
                payAddressMassage.setText(Numeric.toHexString(wallet.getAddr()));
                costMassage.setText(gasNum.toString() +  "\b\beth");
                numMassage.setText(tNum + "\b\b" + ethToken.getSymbol());
                alertbBuilder.setView(orderView);

                AlertDialog show = alertbBuilder.show();
                orderView.findViewById(R.id.closeBut).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        show.dismiss();
                        // 防止重复点击
                        transcationBut.setEnabled(true);
                    }
                });
                orderView.findViewById(R.id.payBut).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(EthTxActivity.this);
                        View pwdView = inflater.inflate(R.layout.input_pwd_dialog, null);
                        builder.setView(pwdView);
                        AlertDialog dialog = builder.show();
                        pwdView.findViewById(R.id.closeBut).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        pwdView.findViewById(R.id.inputPassBut).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                EditText text = pwdView.findViewById(R.id.inPwdEdit);
                                String pwd = text.getText().toString();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (txManageService.doEthTransfer(EthTxActivity.this, wallet, ethToken, pwd, to, tNum, gasPrice, gas)) {
                                                RemindUtils.toastShort(EthTxActivity.this, "交易发起成功，等待确认");
                                            } else {
                                                RemindUtils.toastShort(EthTxActivity.this, "交易发起失败");
                                            }
                                        } catch (WalletLockedException e) {
                                            RemindUtils.toastShort(EthTxActivity.this, "钱包已被锁定");
                                        } catch (UnexpectedException e) {
                                            e.printStackTrace();
                                            RemindUtils.toastShort(EthTxActivity.this, e.getMessage());
                                        } catch (VerifyFailException e) {
                                            RemindUtils.toastShort(EthTxActivity.this, "密码错误");
                                        }
                                    }
                                }).start();
                                RemindUtils.toastShort(EthTxActivity.this, "正在发起交易");
                                dialog.dismiss();
                                show.dismiss();
                                EthTxActivity.this.finish();
                            }
                        });
                    }
                });
            }
            break;
        }
    }

    private String tNumBefore;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        tNumBefore = TNum.getText().toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String tNumStr = TNum.getText().toString();
        if (StringUtils.hasText(tNumStr) && !StringUtils.matchExp(tNumStr, "^[0-9]{1,9}([.]{1}[0-9]{0," + ethToken.getDecimals() + "}){0,1}$")) {
            RemindUtils.toastShort(this, "请输入正确金额");
            TNum.setText(tNumBefore);
            return;
        }
        BigDecimal tNum = new BigDecimal(StringUtils.hasText(tNumStr) ? tNumStr : "0");
        if ((ethToken.getType() == EthToken.TOKEN_TYPE_ETH && tNum.add(gasTotal).compareTo(balance) > 0)
                || ethToken.getType() != EthToken.TOKEN_TYPE_ETH && tNum.compareTo(balance) > 0) {
            RemindUtils.toastShort(this, "超出余额");
            TNum.setText(tNumBefore);
            return;
        }

        String addrStr = toAddress.getText().toString();
        if (tNum.compareTo(BigDecimal.ZERO) > 0 && StringUtils.matchExp(addrStr, "^([0][xX]){0,1}[0-9a-fA-F]{40}$")) {
            transcationBut.setEnabled(true);
            transcationBut.setBackgroundResource(R.drawable.fillet_fill_blue_on);
        } else {
            transcationBut.setEnabled(true);
            transcationBut.setBackgroundResource(R.drawable.fillet_fill_blue_off);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress < 1) {
            // 矿工费不能为0
            seekBar.setProgress(1);
            return;
        }
        gasPrice = gasPriceUnit.multiply(BigInteger.valueOf(progress));
        gasTotal = Convert.fromWei(new BigDecimal(gas.multiply(gasPrice)), Convert.Unit.ETHER);
        txPrice.setText(gasTotal + "\b\beth");
        if (ethToken.getType() == EthToken.TOKEN_TYPE_ETH) {
            if (tranBalance) {
                //  全额转出
                TNum.setText(balance.subtract(gasTotal).toString());
            } else {
                String tNumStr = TNum.getText().toString();
                BigDecimal tNum = new BigDecimal(StringUtils.hasText(tNumStr) ? tNumStr : "0");
                if (gasTotal.add(tNum).compareTo(balance) >= 0) {
                    //  矿工费+转账金额 > ETH余额
                    TNum.setText(balance.subtract(gasTotal).toString());
                }
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}

