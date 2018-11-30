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
import com.hunter.wallet.entity.UserInfo;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.service.UserManageService;
import com.hunter.wallet.utils.RemindUtils;

import java.util.Timer;
import java.util.TimerTask;


public class UnlockActivity extends Activity implements View.OnClickListener, TextWatcher {
    private UserManageService userManageService = UserManageService.getInstance();

    private Button getCode;
    private UserInfo userInfo;
    private EditText codeText;
    private Button unlockBut;
    private Timer updateTimer;
    private int waitTime;
    private boolean sending;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            userInfo = userManageService.getUserInfo();
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.main_info_fragment_unlock_layout);
        getCode = findViewById(R.id.getCodeBut);
        unlockBut = findViewById(R.id.unlockBut);
        codeText = findViewById(R.id.codeText);

        getCode.setOnClickListener(this);
        unlockBut.setOnClickListener(this);
        codeText.addTextChangedListener(this);
        setMobileText();
        // 更新等待时间
        startUpdateTimer();
    }

    private void setMobileText() {
        TextView areaCode = findViewById(R.id.areaCode);
        TextView phoneText = findViewById(R.id.phoneText);
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
        stopUpdateTimer();
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

    private void onClickGetCodeBut() {
        if (!sending) {
            sending = true;
            getCode.setEnabled(false);
            getCode.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
            userManageService.sendAuthcode(userInfo.getBindMobile(), new UserManageService.UserManageCallback() {
                @Override
                public void onSuccess(String msg) {
                    sending = false;
                    waitTime = 60;
                    RemindUtils.toastShort(UnlockActivity.this, msg);

                }
                @Override
                public void onFail(String msg) {
                    sending = false;
                    waitTime = 0;
                    RemindUtils.toastShort(UnlockActivity.this, msg);
                }
            });
        }
    }

    private void onClickUnlockBut() {
        unlockBut.setEnabled(false);
        try {
            userManageService.unlockPin(codeText.getText().toString(), new UserManageService.UnlockPinCallback() {
                        @Override
                        public void onSuccess(String msg) {
                            RemindUtils.toastShort(UnlockActivity.this, msg);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(UnlockActivity.this, MainFragmentActivity.class));
                                    UnlockActivity.this.finish();
                                }
                            });
                        }

                        @Override
                        public void onFail(String msg) {
                            RemindUtils.toastShort(UnlockActivity.this, msg);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    unlockBut.setEnabled(false);
                                }
                            });
                        }
                    }
            );
        } catch (UnexpectedException e) {
            e.printStackTrace();
            RemindUtils.toastShort(UnlockActivity.this, e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getCodeBut:
                onClickGetCodeBut();
                break;
            case R.id.unlockBut:
                onClickUnlockBut();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (codeText.getText().toString().length() > 0) {
            unlockBut.setEnabled(true);
            unlockBut.setBackgroundResource(R.drawable.fillet_fill_blue_on);
        } else {
            unlockBut.setEnabled(false);
            unlockBut.setBackgroundResource(R.drawable.fillet_fill_blue_off);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
