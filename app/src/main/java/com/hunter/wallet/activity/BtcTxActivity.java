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
import android.widget.*;
import com.hunter.wallet.R;
import com.hunter.wallet.entity.EthToken;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.VerifyFailException;
import com.hunter.wallet.exception.WalletLockedException;
import com.hunter.wallet.service.TokenManageService;
import com.hunter.wallet.service.TxManageService;
import com.hunter.wallet.utils.AddressEncoder;
import com.hunter.wallet.utils.BtcUtil;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.StringUtils;
import com.xys.libzxing.zxing.activity.CaptureActivity;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class BtcTxActivity extends Activity implements View.OnClickListener, TextWatcher, SeekBar.OnSeekBarChangeListener {

    private static final int REQUEST_CODE_CAPTURE = 0;
    private static final int REQUEST_CODE_CONSTACT = 1;
    private static final int defaultPercent = 40;

    private WalletInfo wallet;
    private EditText toAddress;
    private EditText TNum;
    private TextView txPrice;

    private LayoutInflater inflater;
    private Button transcationBut;

    private boolean tranBalance = false;
    private ImageView tranBalanceImg;

    //余额
    private String btcBalance;
    //手续费
    private String fee;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wallet = (WalletInfo) getIntent().getSerializableExtra(WalletInfo.class.getName());
        btcBalance = getIntent().getStringExtra("btcBalance");
        fee = getIntent().getStringExtra("fee");

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

        tokenSymbol.setText("BTC");
        tokenUnit.setText("BTC");
        num.setText(btcBalance);

        toAddress.addTextChangedListener(this);
        toAddress.setHint("输入比特币地址");
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
                        //识别比特币二维码
                        if (result.startsWith("bitcoin:") || result.startsWith("BITCOIN:")) {
                            toAddress.setText(AddressEncoder.decodeBtc(result).getAddress());
                        }else {
                            toAddress.setText(result);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        RemindUtils.toastShort(BtcTxActivity.this, "二维码解析失败");
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
                    TNum.setText(new BigDecimal(btcBalance).subtract(new BigDecimal(fee)).toString());
                    TNum.setFocusable(false);
                    TNum.setFocusableInTouchMode(false);
                } else {
                    tranBalanceImg.setImageResource(R.drawable.blue_off);
                    TNum.setFocusable(true);
                    TNum.setFocusableInTouchMode(true);
                }
                break;
            case R.id.selectContacts: {
                startActivityForResult(new Intent(BtcTxActivity.this, ContactsActivity.class), REQUEST_CODE_CONSTACT);
            }
            break;
            case R.id.saoyisao:
                startActivityForResult(new Intent(BtcTxActivity.this, CaptureActivity.class), REQUEST_CODE_CAPTURE);
                break;
            case R.id.trxPreBut:
                BtcTxActivity.this.finish();
                break;
            case R.id.TranscationBut: {
                // 防止重复点击
                transcationBut.setEnabled(false);
                String value = TNum.getText().toString().trim();
                String to = toAddress.getText().toString().trim();
                String from = BtcUtil.getAddress(wallet.getPubkey());

                AlertDialog.Builder alertbBuilder = new AlertDialog.Builder(BtcTxActivity.this);
                View orderView = inflater.inflate(R.layout.tx_order_layout, null);
                TextView toAddressMassage = orderView.findViewById(R.id.toAddressMassage);
                TextView payAddressMassage = orderView.findViewById(R.id.payAddressMassage);
                TextView costMassage = orderView.findViewById(R.id.costMassage);
                TextView numMassage = orderView.findViewById(R.id.numMassage);
                toAddressMassage.setText(to);
                payAddressMassage.setText(from);
                costMassage.setText(fee +  "\b\bbtc");
                numMassage.setText(value + "\b\bbtc");
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(BtcTxActivity.this);
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
                                            //比特币交易
                                            if (BtcUtil.doBtcTransfer(from, to, value, fee, wallet, pwd)) {
                                                RemindUtils.toastShort(BtcTxActivity.this, "交易发起成功，等待确认");
                                            } else {
                                                RemindUtils.toastShort(BtcTxActivity.this, "交易发起失败");
                                            }
                                        } catch (WalletLockedException e) {
                                            RemindUtils.toastShort(BtcTxActivity.this, "钱包已被锁定");
                                        } catch (UnexpectedException e) {
                                            e.printStackTrace();
                                            RemindUtils.toastShort(BtcTxActivity.this, e.getMessage());
                                        } catch (VerifyFailException e) {
                                            RemindUtils.toastShort(BtcTxActivity.this, "密码错误");
                                        } catch (IOException e) {
                                            RemindUtils.toastShort(BtcTxActivity.this, "网络错误");
                                        }
                                    }
                                }).start();
                                RemindUtils.toastShort(BtcTxActivity.this, "正在发起交易");
                                dialog.dismiss();
                                show.dismiss();
                                BtcTxActivity.this.finish();
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
        if (StringUtils.hasText(tNumStr) && !StringUtils.matchExp(tNumStr, "^[0-9]{1,9}([.]{1}[0-9]{0,9}){0,1}$")) {
            RemindUtils.toastShort(this, "请输入正确金额");
            TNum.setText(tNumBefore);
            return;
        }
        BigDecimal tNum = new BigDecimal(StringUtils.hasText(tNumStr) ? tNumStr : "0");
        if (tNum.add(new BigDecimal(fee)).compareTo(new BigDecimal(btcBalance)) > 0 || tNum.compareTo(new BigDecimal(btcBalance)) > 0) {
            RemindUtils.toastShort(this, "超出余额");
            TNum.setText(tNumBefore);
            return;
        }

        String addrStr = toAddress.getText().toString().trim();
        if (tNum.compareTo(BigDecimal.ZERO) > 0 && BtcUtil.isInvalidAddress(addrStr)) {
            transcationBut.setEnabled(true);
            transcationBut.setBackgroundResource(R.drawable.fillet_fill_blue_on);
        } else {
            transcationBut.setEnabled(false);
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
        /*gasPrice = gasPriceUnit.multiply(BigInteger.valueOf(progress));
        gasTotal = Convert.fromWei(new BigDecimal(gas.multiply(gasPrice)), Convert.Unit.ETHER);*/
        txPrice.setText(fee + "\b\bbtc");

        if (tranBalance) {
            //  全额转出
            TNum.setText(new BigDecimal(btcBalance).subtract(new BigDecimal(fee)).toString());
        } else {
            String tNumStr = TNum.getText().toString();
            BigDecimal tNum = new BigDecimal(StringUtils.hasText(tNumStr) ? tNumStr : "0");
            if (new BigDecimal(fee).add(tNum).compareTo(new BigDecimal(btcBalance)) >= 0) {
                //  矿工费+转账金额 > ETH余额
                TNum.setText(new BigDecimal(btcBalance).subtract(new BigDecimal(fee)).toString());
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

