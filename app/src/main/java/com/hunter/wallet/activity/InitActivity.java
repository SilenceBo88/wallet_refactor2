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
import android.widget.EditText;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.service.UserManageService;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.StringUtils;

import java.util.Timer;
import java.util.TimerTask;

public class InitActivity extends Activity implements View.OnClickListener, TextWatcher {
    // TODO 选择地区

    private UserManageService userManageService = UserManageService.getInstance();

    private EditText phone;
    private Button commitBtn;
    private Button getCode;
    private String areaCodeStr;
    private EditText code;
    private EditText pin;
    private TextView areaCode;
    private EditText rePin;
    private Timer updateTimer;
    private int waitTime;
    private boolean sending;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init_wallet_layout);
        areaCode = findViewById(R.id.areaCode);
        phone = findViewById(R.id.phone);
        getCode = findViewById(R.id.getCode);
        code = findViewById(R.id.code);
        pin = findViewById(R.id.pin);
        rePin = findViewById(R.id.rePin);
        commitBtn = findViewById(R.id.commitBtn);

        phone.addTextChangedListener(this);
        code.addTextChangedListener(this);
        pin.addTextChangedListener(this);
        rePin.addTextChangedListener(this);
        getCode.setOnClickListener(this);
        commitBtn.setOnClickListener(this);
        startUpdateTimer();
    }

    private void startUpdateTimer() {
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
                        if (sending) {
                            getCode.setEnabled(false);
                            getCode.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
                        } else {
                            if (waitTime > 0) {
                                getCode.setEnabled(false);
                                getCode.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
                                getCode.setText("已发送（" + waitTime + "）");
                                waitTime--;
                            } else {
                                getCode.setEnabled(true);
                                getCode.setBackgroundResource(R.drawable.fillet_fill_jinse_on);
                                getCode.setText("获取验证码");
                            }
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    private void stopUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopUpdateTimer();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getCode:
                onClickGetCode();
                break;
            case R.id.commitBtn:
                onClickCommitBtn();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String phoneStr = phone.getText().toString();
        String authCode = code.getText().toString();
        String pinStr = pin.getText().toString();
        String rePinStr = rePin.getText().toString();
        if (StringUtils.hasText(phoneStr)
                && StringUtils.hasText(authCode)
                && StringUtils.matchExp(pinStr, "[0-9]{6}")
                && StringUtils.equal(pinStr, rePinStr)) {
            commitBtn.setEnabled(true);
            commitBtn.setBackgroundResource(R.drawable.fillet_fill_blue_on);
        } else {
            commitBtn.setEnabled(false);
            commitBtn.setBackgroundResource(R.drawable.fillet_fill_blue_off);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void onClickGetCode() {
        if (!sending) {
            sending = true;
            getCode.setEnabled(false);
            getCode.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
            String mobile = (areaCode.getText().toString() + "-" + phone.getText().toString()).trim();
            userManageService.sendAuthcode(mobile, new UserManageService.UserManageCallback() {
                @Override
                public void onSuccess(String msg) {
                    sending = false;
                    waitTime = 60;
                    RemindUtils.toastShort(InitActivity.this, msg);

                }

                @Override
                public void onFail(String msg) {
                    sending = false;
                    waitTime = 0;
                    RemindUtils.toastShort(InitActivity.this, msg);
                }
            });
        }
    }

    private void onClickCommitBtn() {
        try {
            commitBtn.setEnabled(false);
            String mobile = (areaCode.getText().toString() + "-" + phone.getText().toString()).trim();
            String authcode = code.getText().toString();
            String pinStr = pin.getText().toString();
            userManageService.userInit(pinStr, mobile, authcode, new UserManageService.UserManageCallback() {
                        @Override
                        public void onSuccess(String msg) {
                            RemindUtils.toastShort(InitActivity.this, msg);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(InitActivity.this, MainFragmentActivity.class));
                                    InitActivity.this.finish();
                                }
                            });
                        }

                        @Override
                        public void onFail(String msg) {
                            RemindUtils.toastShort(InitActivity.this, msg);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    commitBtn.setEnabled(true);
                                }
                            });
                        }
                    }
            );
        } catch (UnexpectedException e) {
            e.printStackTrace();
            RemindUtils.toastShort(InitActivity.this, e.getMessage());
            commitBtn.setEnabled(true);
        }
    }

}
