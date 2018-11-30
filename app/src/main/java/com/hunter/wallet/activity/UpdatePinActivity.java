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

import com.hunter.wallet.R;
import com.hunter.wallet.exception.PinLockedException;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.VerifyFailException;
import com.hunter.wallet.service.UserManageService;
import com.hunter.wallet.utils.AppUtils;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.StringUtils;

public class UpdatePinActivity extends Activity implements View.OnClickListener, TextWatcher {
    private UserManageService userManageService = UserManageService.getInstance();

    private Button updateBut;
    private EditText oldPin;
    private EditText newPin;
    private EditText reNewPin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_info_fragment_update_pin_layout);

        oldPin = findViewById(R.id.oldPin);
        newPin = findViewById(R.id.newPin);
        reNewPin = findViewById(R.id.reNewPin);
        updateBut = findViewById(R.id.updateBut);

        oldPin.addTextChangedListener(this);
        newPin.addTextChangedListener(this);
        reNewPin.addTextChangedListener(this);

        updateBut.setOnClickListener(this);
        findViewById(R.id.updatePinPreBut).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.updatePinPreBut:
                UpdatePinActivity.this.finish();
                break;
            case R.id.updateBut:
                try {
                    userManageService.changePin(oldPin.getText().toString(), newPin.getText().toString());
                    RemindUtils.toastShort(UpdatePinActivity.this, "PIN码修改成功");
                    UpdatePinActivity.this.finish();
                } catch (VerifyFailException e) {
                    RemindUtils.toastShort(UpdatePinActivity.this, "PIN码错误");
                } catch (UnexpectedException e) {
                    e.printStackTrace();
                    RemindUtils.toastShort(UpdatePinActivity.this, e.getMessage());
                } catch (PinLockedException e) {
                    RemindUtils.toastShort(UpdatePinActivity.this, "PIN码已被锁定");
                    AppUtils.startActivity(UpdatePinActivity.this, UnlockActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String oldPinStr = oldPin.getText().toString();
        String pinStr = newPin.getText().toString();
        String rePinStr = reNewPin.getText().toString();
        if (StringUtils.hasText(oldPinStr)
                && StringUtils.matchExp(pinStr, "[0-9]{6}")
                && StringUtils.equal(pinStr, rePinStr)) {
            updateBut.setEnabled(true);
            updateBut.setBackgroundResource(R.drawable.fillet_fill_blue_on);
        } else {
            updateBut.setEnabled(false);
            updateBut.setBackgroundResource(R.drawable.fillet_fill_blue_off);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
