package com.hunter.wallet.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunter.wallet.R;
import com.hunter.wallet.entity.InvokeResult;
import com.hunter.wallet.entity.Secp256k1Signature;
import com.hunter.wallet.entity.UserInfo;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.KeystoreResolveException;
import com.hunter.wallet.exception.MnemonicTransferException;
import com.hunter.wallet.exception.PinLockedException;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.VerifyFailException;
import com.hunter.wallet.exception.WalletLockedException;
import com.hunter.wallet.exception.WalletOverflowException;
import com.hunter.wallet.exception.WalletRepeatException;
import com.hunter.wallet.utils.StringUtils;

import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Bytes;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SecurityService {

    private static SecurityService instance = new SecurityService();

    public static SecurityService getInstance() {
        return instance;
    }

    private SecurityService() {
    }

    public boolean checkPwdFormat(String pwd) {
        if (StringUtils.matchExp(pwd, "^[0-9a-zA-Z]{6,30}$")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPwdStrength(String pwd) {
        if (checkPwdFormat(pwd) && StringUtils.matchExp(pwd, "^.*(?=.{8,})(?=.*\\d)(?=.*[a-zA-Z]).*$")) {
            // 包含数字字母
            return true;
        } else {
            return false;
        }
    }

    public interface AlertCallback {
        void onContinue();

        void onBack();
    }


    //<uses-permission android:name="android.permission.FORCE_STOP_PACKAGES"/>

    /**
     * 关闭非系统应用
     *
     * @param context
     */
    private void killNonsystemProcess(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        try {
            Method method = Class.forName("android.app.ActivityManager")
                    .getMethod("forceStopPackage", String.class);
            for (PackageInfo packageInfo : packages) {
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                        && !packageInfo.applicationInfo.packageName.equals(context.getPackageName())) {
                    //mActivityManager.forceStopPackage(packageInfo.packageName);
                    method.invoke(mActivityManager, packageInfo.packageName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void alertKillNonsystemProcess(Activity activity, AlertCallback callback) {
        AlertDialog.Builder reminderBuilder = new AlertDialog.Builder(activity);
        View reminderView = activity.getLayoutInflater().inflate(R.layout.reminder_dialog, null);
        reminderBuilder.setView(reminderView);
        AlertDialog show = reminderBuilder.show();
        show.setCancelable(false);
        reminderView.findViewById(R.id.agreeBut).setOnClickListener(v -> {
            killNonsystemProcess(activity);
            show.dismiss();
            callback.onContinue();
        });
        reminderView.findViewById(R.id.closeBut).setOnClickListener(v -> {
            show.dismiss();
            callback.onBack();
        });
    }

    public void alertPwdStrength(String password, Activity activity, AlertCallback callback) {
        if (checkPwdStrength(password) && callback != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    callback.onContinue();
                }
            }).start();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            View daView = activity.getLayoutInflater().inflate(R.layout.danger_pwd_dialog, null);
            builder.setView(daView);
            AlertDialog show = builder.show();
            daView.findViewById(R.id.confirmBut).setOnClickListener(v -> {
                show.dismiss();
                if (callback != null) {
                    callback.onContinue();
                }
            });
            daView.findViewById(R.id.closeBut).setOnClickListener(v -> {
                show.dismiss();
                if (callback != null) {
                    callback.onBack();
                }
            });
        }
    }
}
