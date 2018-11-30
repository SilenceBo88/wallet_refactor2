package com.hunter.wallet.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.service.TokenManageService;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.StringUtils;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import org.web3j.utils.Numeric;

public class EthTokenImportActivity extends Activity implements View.OnClickListener, TextWatcher {

    private TokenManageService tokenManageService = TokenManageService.getInstance();

    private TextView coinAddressInput;
    private Button addCoinBut;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coin_add_by_contract_layout);
        coinAddressInput = findViewById(R.id.coinAddressInput);
        addCoinBut = findViewById(R.id.addCoinBut);

        coinAddressInput.addTextChangedListener(this);

        findViewById(R.id.coinAddByContractPreBut).setOnClickListener(this);
        findViewById(R.id.addCoinSaoyisaoBut).setOnClickListener(this);
        addCoinBut.setOnClickListener(this);
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
            Bundle bundle = data.getExtras();
            String result = bundle.getString("result");
            coinAddressInput.setText(result);
        } else {
            RemindUtils.toastShort(EthTokenImportActivity.this, "二维码解析失败");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.coinAddByContractPreBut:
                finish();
                break;
            case R.id.addCoinSaoyisaoBut:
                startActivityForResult(new Intent(EthTokenImportActivity.this, CaptureActivity.class), 0);
                break;
            case R.id.addCoinBut:
                addCoinBut.setEnabled(false);
                byte[] contract = Numeric.hexStringToByteArray(coinAddressInput.getText().toString());
                tokenManageService.importEthToken(EthTokenImportActivity.this, contract, new TokenManageService.ImportEthTokenCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        RemindUtils.toastShort(EthTokenImportActivity.this, msg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }
                    @Override
                    public void onFail(String msg) {
                        RemindUtils.toastShort(EthTokenImportActivity.this, msg);
                        addCoinBut.setEnabled(true);
                    }
                });
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String contact = coinAddressInput.getText().toString();
        if (StringUtils.matchExp(contact, "^([0][xX]){0,1}[0-9a-fA-F]{40}$")) {
            addCoinBut.setEnabled(true);
            addCoinBut.setBackgroundResource(R.drawable.fillet_fill_blue_on);
        } else {
            addCoinBut.setEnabled(false);
            addCoinBut.setBackgroundResource(R.drawable.fillet_fill_blue_off);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
