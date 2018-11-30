package com.hunter.wallet.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.common.Constants;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.entity.UserInfo;
import com.hunter.wallet.service.UserManageService;
import com.hunter.wallet.utils.AppUtils;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.SharedPreferencesUtils;
import com.hunter.wallet.utils.StringUtils;

import java.util.Timer;
import java.util.TimerTask;

public class ReSetActivity extends Activity implements View.OnClickListener, TextWatcher {

    private UserManageService userManageService = UserManageService.getInstance();

    private Button getCodeBut;
    private TextView areaCode;
    private TextView phoneText;
    private EditText codeText;
    private Button reSetBut;
    private TextView pinText;
    private UserInfo userInfo;
    private int waitTime;
    private Timer updateTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_info_fragment_reset_wallet_layout);
        try {
            userInfo = userManageService.getUserInfo();
        } catch (UnexpectedException e) {
            e.printStackTrace();
            RemindUtils.toastShort(this, e.getMessage());
            finish();
            return;
        }

        setMobileText();
        codeText = findViewById(R.id.codeText);
        getCodeBut = findViewById(R.id.getCodeBut);
        pinText = findViewById(R.id.pinText);
        reSetBut = findViewById(R.id.reSetBut);

        codeText.addTextChangedListener(this);
        pinText.addTextChangedListener(this);
        getCodeBut.setOnClickListener(this);
        reSetBut.setOnClickListener(this);
        findViewById(R.id.resetPreBut).setOnClickListener(this);
    }

    private void setMobileText() {
        areaCode = findViewById(R.id.areaCode);
        phoneText = findViewById(R.id.phoneText);
        String[] s = userInfo.getBindMobile().split("-");
        areaCode.setText(s[0]);
        String phone = s[1];
        String rex = "****";
        StringBuilder sb = new StringBuilder(phone);
        sb.replace(3, 7, rex);
        phoneText.setText(sb.toString());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }

    private void startUpdateWaitTime() {
        waitTime = 60;
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (waitTime > 0) {
                            getCodeBut.setEnabled(false);
                            getCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
                            getCodeBut.setText("已发送（" + waitTime + "）");
                            waitTime--;
                        } else {
                            getCodeBut.setEnabled(true);
                            getCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_on);
                            getCodeBut.setText("重新获取");
                            updateTimer.cancel();
                            updateTimer = null;
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resetPreBut:
                ReSetActivity.this.finish();
                break;
            case R.id.getCodeBut: {
                getCodeBut.setEnabled(false);
                getCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
                userManageService.sendAuthcode(userInfo.getBindMobile(), new UserManageService.UserManageCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        RemindUtils.toastShort(ReSetActivity.this, msg);
                        // 更新等待时间
                        startUpdateWaitTime();
                    }

                    @Override
                    public void onFail(String msg) {
                        RemindUtils.toastShort(ReSetActivity.this, msg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getCodeBut.setEnabled(true);
                                getCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_on);
                            }
                        });
                    }
                });
            }
            break;
            case R.id.reSetBut: {
                reSetBut.setEnabled(false);
                String pin = pinText.getText().toString();
                String authcode = codeText.getText().toString();
                try {
                    userManageService.reset(pin, authcode, new UserManageService.ResetCallback() {
                        @Override
                        public void onSuccess() {
                            RemindUtils.toastShort(ReSetActivity.this, "重置成功");
                            cleanSharedPrefernences();
                            AppUtils.restartApp(ReSetActivity.this);
                        }

                        @Override
                        public void onFail(String msg) {
                            RemindUtils.toastShort(ReSetActivity.this, msg);
                            reSetBut.setEnabled(true);
                        }

                        @Override
                        public void onPinLock() {
                            AppUtils.startActivity(ReSetActivity.this, UnlockActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        }
                    });
                } catch (UnexpectedException e) {
                    e.printStackTrace();
                    RemindUtils.toastShort(ReSetActivity.this, e.getMessage());
                    reSetBut.setEnabled(true);
                }
            }
            break;
        }
    }

    private void cleanSharedPrefernences() {
        SharedPreferencesUtils.clear(this, Constants.preferences_wallet_setting);
        SharedPreferencesUtils.clear(this, Constants.preferences_global_cache);
        SharedPreferencesUtils.clear(this, Constants.preferences_eth_uncommit_transfer);
        SharedPreferencesUtils.clear(this, Constants.preferences_eth_imported_token);
        SharedPreferencesUtils.clear(this, Constants.preferences_eth_selected_token);
        SharedPreferencesUtils.clear(this, Constants.preferences_eth_transfer_cache);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (StringUtils.hasText(pinText.getText().toString()) && StringUtils.hasText(codeText.getText().toString())) {
            reSetBut.setEnabled(true);
            reSetBut.setBackgroundResource(R.drawable.fillet_fill_blue_on);
        } else {
            reSetBut.setEnabled(false);
            reSetBut.setBackgroundResource(R.drawable.fillet_fill_blue_off);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
