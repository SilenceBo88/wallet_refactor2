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
import com.hunter.wallet.utils.AppUtils;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.StringUtils;

import java.util.Timer;
import java.util.TimerTask;

public class RebindPhoneActivity extends Activity implements View.OnClickListener, TextWatcher {
    private UserManageService userManageService = UserManageService.getInstance();

    private TextView areaCode;
    private TextView oldPhoneText;
    private EditText oldCodeText;
    private Button oldCodeBut;
    private TextView newAreaCode;
    private EditText newPhoneText;
    private EditText newCodeText;
    private Button newCodeBut;
    private EditText pinText;
    private Button reBindPhoneBut;
    private UserInfo userInfo;
    private Timer updateTimer;
    private int oldWaittime;
    private int newWaittime;
    private boolean oldSending;
    private boolean newSending;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_info_fragment_rebind_phone_layout);
        try {
            userInfo = userManageService.getUserInfo();
        } catch (UnexpectedException e) {
            e.printStackTrace();
            RemindUtils.toastShort(this, e.getMessage());
            finish();
            return;
        }
        initOldPhoneText();
        oldCodeBut = findViewById(R.id.oldCodeBut);
        oldCodeText = findViewById(R.id.oldCodeText);
        newAreaCode = findViewById(R.id.newAreaCode);
        newPhoneText = findViewById(R.id.newPhoneText);
        newCodeText = findViewById(R.id.newCodeText);
        newCodeBut = findViewById(R.id.newCodeBut);
        pinText = findViewById(R.id.pinText);
        reBindPhoneBut = findViewById(R.id.reBindPhoneBut);

        oldCodeText.addTextChangedListener(this);
        newPhoneText.addTextChangedListener(this);
        newCodeText.addTextChangedListener(this);
        pinText.addTextChangedListener(this);

        oldCodeBut.setOnClickListener(this);
        newCodeBut.setOnClickListener(this);
        reBindPhoneBut.setOnClickListener(this);
        findViewById(R.id.newSelectAreaCode).setOnClickListener(this);
        findViewById(R.id.reBindPhonePreBut).setOnClickListener(this);

        startUpdateWaitTime();
    }

    private void initOldPhoneText() {
        oldPhoneText = findViewById(R.id.oldPhoneText);
        areaCode = findViewById(R.id.areaCode);
        String[] split = userInfo.getBindMobile().split("-");
        String oldPhoneStr = split[1];
        areaCode.setText(split[0]);
        String rex = "****";
        StringBuilder sb = new StringBuilder(oldPhoneStr);
        sb.replace(3, 7, rex);
        oldPhoneText.setText(sb.toString());
    }

    private void startUpdateWaitTime() {
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
                        if (oldSending) {
                            oldCodeBut.setEnabled(false);
                            oldCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
                        } else {
                            if (oldWaittime > 0) {
                                oldCodeBut.setEnabled(false);
                                oldCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
                                oldCodeBut.setText("已发送（" + oldWaittime + "）");
                                oldWaittime--;
                            } else {
                                oldCodeBut.setEnabled(true);
                                oldCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_on);
                                oldCodeBut.setText("获取验证码");
                            }

                        }
                        if (newSending) {
                            newCodeBut.setEnabled(false);
                            newCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
                        } else if (newWaittime > 0) {
                            newCodeBut.setEnabled(false);
                            newCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
                            newCodeBut.setText("已发送（" + newWaittime + "）");
                            newWaittime--;
                        } else {
                            newCodeBut.setEnabled(true);
                            newCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_on);
                            newCodeBut.setText("获取验证码");
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.newSelectAreaCode:
//                showdialog();
//                break;
            case R.id.oldCodeBut: {
                oldSending = true;
                oldCodeBut.setEnabled(false);
                oldCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
                userManageService.sendAuthcode(userInfo.getBindMobile(), new UserManageService.UserManageCallback() {
                    @Override
                    public void onSuccess(String msg) {

                        RemindUtils.toastShort(RebindPhoneActivity.this, msg);
                        // 更新等待时间
                        oldWaittime = 60;
                        oldSending = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                oldCodeBut.setText("已发送（" + oldWaittime + "）");
                            }
                        });
                    }

                    @Override
                    public void onFail(String msg) {
                        RemindUtils.toastShort(RebindPhoneActivity.this, msg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                oldSending = false;
                                oldCodeBut.setEnabled(true);
                                oldCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_on);
                            }
                        });

                    }
                });
            }
            break;
            case R.id.newCodeBut: {
                newSending = true;
                newCodeBut.setEnabled(false);
                newCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_off);
                String newMobile = newAreaCode.getText().toString() + "-" + newPhoneText.getText().toString();
                userManageService.sendAuthcode(newMobile, new UserManageService.UserManageCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        RemindUtils.toastShort(RebindPhoneActivity.this, msg);
                        newWaittime = 60;
                        newSending = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                newCodeBut.setText("已发送（" + newWaittime + "）");
                            }
                        });
                    }

                    @Override
                    public void onFail(String msg) {
                        RemindUtils.toastShort(RebindPhoneActivity.this, msg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                newSending = false;
                                newCodeBut.setEnabled(true);
                                newCodeBut.setBackgroundResource(R.drawable.fillet_fill_jinse_on);
                            }
                        });
                    }
                });
            }
            break;
            case R.id.reBindPhoneBut:
                reBindPhoneBut.setEnabled(false);
                String pin = pinText.getText().toString();
                String oldCodeString = oldCodeText.getText().toString();
                String newCodeString = newCodeText.getText().toString();
                String newMobile = newAreaCode.getText().toString() + "-" + newPhoneText.getText().toString();
                try {
                    userManageService.rebindMobile(pin, oldCodeString, newMobile, newCodeString, new UserManageService.RebindMobileCallback() {
                        @Override
                        public void onSuccess() {
                            RemindUtils.toastShort(RebindPhoneActivity.this, "重新绑定成功");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RebindPhoneActivity.this.finish();
                                }
                            });
                        }

                        @Override
                        public void onFail(String msg) {
                            RemindUtils.toastShort(RebindPhoneActivity.this, msg);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    reBindPhoneBut.setEnabled(true);
                                    oldCodeText.setText("");
                                    newCodeText.setText("");
                                }
                            });
                        }

                        @Override
                        public void onPinlock() {
                            AppUtils.startActivity(RebindPhoneActivity.this, UnlockActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        }
                    });
                } catch (UnexpectedException e) {
                    e.printStackTrace();
                    RemindUtils.toastShort(RebindPhoneActivity.this, e.getMessage());
                    reBindPhoneBut.setEnabled(true);
                }
                break;
            case R.id.reBindPhonePreBut:
                RebindPhoneActivity.this.finish();
                break;
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String pin = pinText.getText().toString();
        String oldCodeString = oldCodeText.getText().toString();
        String newCodeString = newCodeText.getText().toString();
        String newPhoneStr = newPhoneText.getText().toString();

        if (StringUtils.hasText(oldCodeString) && StringUtils.hasText(newCodeString) && StringUtils.hasText(newPhoneStr) && StringUtils.hasText(pin)) {
            reBindPhoneBut.setEnabled(true);
            reBindPhoneBut.setBackgroundResource(R.drawable.fillet_fill_blue_on);
        } else {
            reBindPhoneBut.setEnabled(false);
            reBindPhoneBut.setBackgroundResource(R.drawable.fillet_fill_blue_off);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}

