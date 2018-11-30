package com.hunter.wallet.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.hunter.wallet.R;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.VerifyFailException;
import com.hunter.wallet.exception.WalletLockedException;
import com.hunter.wallet.service.SecurityService;
import com.hunter.wallet.service.WalletManageService;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.StringUtils;

public class UpdatePassActivity extends Activity implements TextWatcher, View.OnClickListener {
    private WalletManageService walletManageService = WalletManageService.getInstance();
    private SecurityService securityService = SecurityService.getInstance();

    private EditText oldPassword;
    private EditText newPassword;
    private EditText rePassword;
    private ImageView newPasswordIcon;
    private ImageView rePasswordIcon;
    private Button updatePassBut;
    private WalletInfo walletInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_pass_layout);
        walletInfo = (WalletInfo) getIntent().getSerializableExtra(WalletInfo.class.getName());
        oldPassword = findViewById(R.id.oldPassword);
        newPassword = findViewById(R.id.newPassword);
        rePassword = findViewById(R.id.rePassword);
        newPasswordIcon = findViewById(R.id.newPasswordIcon);
        rePasswordIcon = findViewById(R.id.rePasswordIcon);
        updatePassBut = findViewById(R.id.updatePassBut);

        newPassword.addTextChangedListener(this);
        rePassword.addTextChangedListener(this);
        findViewById(R.id.updatePreBut).setOnClickListener(this);
        updatePassBut.setOnClickListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String oldPass = oldPassword.getText().toString();
        String passStr = newPassword.getText().toString();
        String repassStr = rePassword.getText().toString();

        if (securityService.checkPwdStrength(passStr)) {
            newPasswordIcon.setImageResource(R.drawable.dui_on);
            if (StringUtils.equal(passStr, repassStr)) {
                rePasswordIcon.setImageResource(R.drawable.dui_on);
            }
        } else {
            newPasswordIcon.setImageResource(R.drawable.dui_off);
            rePasswordIcon.setImageResource(R.drawable.dui_off);
        }

        if (StringUtils.hasText(oldPass) && securityService.checkPwdFormat(passStr) && StringUtils.equal(passStr, repassStr)) {
            updatePassBut.setEnabled(true);
            updatePassBut.setBackgroundResource(R.drawable.fillet_fill_blue_on);
        } else {
            updatePassBut.setEnabled(false);
            updatePassBut.setBackgroundResource(R.drawable.fillet_fill_blue_off);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.updatePreBut:
                UpdatePassActivity.this.finish();
                break;
            case R.id.updatePassBut: {
                String passStr = newPassword.getText().toString();
                securityService.alertPwdStrength(passStr, this, new SecurityService.AlertCallback() {
                    @Override
                    public void onContinue() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    walletManageService.changePassword(walletInfo.getId(), oldPassword.getText().toString(), passStr);
                                    RemindUtils.toastShort(UpdatePassActivity.this, "修改成功");
                                } catch (UnexpectedException e) {
                                    e.printStackTrace();
                                    RemindUtils.toastShort(UpdatePassActivity.this, e.getMessage());
                                } catch (WalletLockedException e) {
                                    RemindUtils.toastShort(UpdatePassActivity.this, "钱包已被锁定");
                                } catch (VerifyFailException e) {
                                    RemindUtils.toastShort(UpdatePassActivity.this, "密码错误");
                                }
                                UpdatePassActivity.this.finish();
                            }
                        });
                    }

                    @Override
                    public void onBack() {
                    }
                });
                break;
            }
        }
    }
}
