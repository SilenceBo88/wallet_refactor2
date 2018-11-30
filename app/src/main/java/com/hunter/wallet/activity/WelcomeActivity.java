package com.hunter.wallet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.hunter.wallet.R;
import com.hunter.wallet.entity.UserInfo;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.service.UserManageService;
import com.hunter.wallet.utils.RemindUtils;

public class WelcomeActivity extends FragmentActivity {
    private UserManageService userManageService = UserManageService.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);

        new Handler().postDelayed(() -> {
            try {
                UserInfo userInfo = userManageService.getUserInfo();
                if (!userInfo.isHasInit()) {
                    // 进入初始化界面
                    startActivity(new Intent(WelcomeActivity.this, InitActivity.class));
                } else if (userInfo.isPinHasLock()) {
                    // 进入解锁界面
                    startActivity(new Intent(this, UnlockActivity.class));
                } else {
                    // 进入主界面
                    startActivity(new Intent(WelcomeActivity.this, MainFragmentActivity.class));
                }
                WelcomeActivity.this.finish();
            } catch (UnexpectedException e) {
                e.printStackTrace();
                RemindUtils.toastShort(WelcomeActivity.this, e.getMessage());
            }
        }, 1500);
    }
}
