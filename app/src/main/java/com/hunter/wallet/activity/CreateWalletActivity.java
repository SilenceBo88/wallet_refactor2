package com.hunter.wallet.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.WalletOverflowException;
import com.hunter.wallet.service.SecurityService;
import com.hunter.wallet.service.WalletManageService;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.StringUtils;

public class CreateWalletActivity extends SecurityActivity implements TextWatcher, View.OnClickListener {
    private WalletManageService walletManageService = WalletManageService.getInstance();
    private SecurityService securityService = SecurityService.getInstance();

    private TextView pass;
    private TextView repass;
    private TextView walletName;
    private Button createBut;
    private ImageView inPassIcon;
    private ImageView rePassIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_wallet_layout);
        walletName = findViewById(R.id.walletName);
        createBut = findViewById(R.id.createBut);
        pass = findViewById(R.id.inPass);
        repass = findViewById(R.id.rePass);
        inPassIcon = findViewById(R.id.inPassIcon);
        rePassIcon = findViewById(R.id.rePassIcon);

        walletName.addTextChangedListener(this);
        pass.addTextChangedListener(this);
        repass.addTextChangedListener(this);

        createBut.setOnClickListener(this);
        findViewById(R.id.createPreBut).setOnClickListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String nameStr = walletName.getText().toString();
        String passStr = pass.getText().toString();
        String repassStr = repass.getText().toString();

        if (securityService.checkPwdStrength(passStr)) {
            inPassIcon.setImageResource(R.drawable.dui_on);
            if (StringUtils.equal(passStr, repassStr)) {
                rePassIcon.setImageResource(R.drawable.dui_on);
            }
        } else {
            inPassIcon.setImageResource(R.drawable.dui_off);
            rePassIcon.setImageResource(R.drawable.dui_off);
        }
        if (StringUtils.hasText(nameStr) && securityService.checkPwdFormat(passStr) && StringUtils.equal(passStr, repassStr)) {
            createBut.setEnabled(true);
            createBut.setBackgroundResource(R.drawable.create_but_fill);
        } else {
            createBut.setEnabled(false);
            createBut.setBackgroundResource(R.drawable.create_but_fill_off);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createPreBut:
                CreateWalletActivity.this.finish();
                break;
            case R.id.createBut: {
                createBut.setEnabled(false);
                String pasStr = pass.getText().toString();
                securityService.alertPwdStrength(pasStr, this, new SecurityService.AlertCallback() {
                    @Override
                    public void onContinue() {
                        String walletNameStr = walletName.getText().toString();
                        try {
                            walletManageService.createWallet(walletNameStr, pasStr);
                            RemindUtils.toastShort(CreateWalletActivity.this, "创建成功");
                        } catch (WalletOverflowException e) {
                            RemindUtils.toastShort(CreateWalletActivity.this, "超出钱包数量限制");
                        } catch (UnexpectedException e) {
                            e.printStackTrace();
                            RemindUtils.toastShort(CreateWalletActivity.this, e.getMessage());
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CreateWalletActivity.this.finish();
                            }
                        });
                    }

                    @Override
                    public void onBack() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                createBut.setEnabled(true);
                            }
                        });
                    }
                });
            }
            break;
        }
    }
}
