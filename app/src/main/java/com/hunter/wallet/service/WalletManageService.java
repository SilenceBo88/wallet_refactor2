package com.hunter.wallet.service;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.hunter.wallet.R;
import com.hunter.wallet.common.Constants;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.KeystoreResolveException;
import com.hunter.wallet.exception.MnemonicTransferException;
import com.hunter.wallet.exception.PinLockedException;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.VerifyFailException;
import com.hunter.wallet.exception.WalletLockedException;
import com.hunter.wallet.exception.WalletOverflowException;
import com.hunter.wallet.exception.WalletRepeatException;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.SharedPreferencesUtils;
import com.hunter.wallet.utils.StringUtils;

import java.util.List;

public class WalletManageService {


    static {
        System.loadLibrary("wsservice");
    }

    private final static WalletManageService instance = new WalletManageService();

    private WalletManageService() {
    }

    public static WalletManageService getInstance() {
        return instance;
    }

    public native List<WalletInfo> getAllWallet() throws UnexpectedException;

    public native WalletInfo createWallet(String name, String password)
            throws WalletOverflowException, UnexpectedException;

    public native void deleteWallet(int id, String password)
            throws WalletLockedException, VerifyFailException, UnexpectedException;

    public native WalletInfo recoverByMnemonic(String name, String password, String mnemonic, String path)
            throws MnemonicTransferException, WalletRepeatException, WalletOverflowException, UnexpectedException;

    public native WalletInfo recoverByKeystore(String name, String password, String keystore, String ksPwd)
            throws KeystoreResolveException, WalletRepeatException, WalletOverflowException, UnexpectedException;

    public native WalletInfo recoverByPrikey(String name, String password, byte[] prikey)
            throws WalletRepeatException, WalletOverflowException, UnexpectedException;

    public native String getKeystore(int id, String password)
            throws WalletLockedException, VerifyFailException, UnexpectedException;

    public native String getMnemonic(int id, String password)
            throws WalletLockedException, VerifyFailException, UnexpectedException;

    public native byte[] getPrikey(int id, String password)
            throws WalletLockedException, VerifyFailException, UnexpectedException;

    public native void changeName(int id, String newName) throws UnexpectedException;

    public native void changePassword(int id, String password, String newPassword)
            throws WalletLockedException, VerifyFailException, UnexpectedException;

    public native void unlockWallet(int id, byte[] pin)
            throws PinLockedException, VerifyFailException, UnexpectedException;


    public WalletInfo getWallet(int id) throws UnexpectedException {
        for (WalletInfo walletInfo : getAllWallet()) {
            if (walletInfo.getId() == id) {
                return walletInfo;
            }
        }
        return null;
    }

    public WalletInfo getCurrentWallet(Context context) throws UnexpectedException {
        int id = SharedPreferencesUtils.getInt(context, Constants.preferences_wallet_setting, "current_wallet_id", 0);
        List<WalletInfo> walletInfos = getAllWallet();
        for (WalletInfo walletInfo : walletInfos) {
            if (walletInfo.getId() == id) {
                return walletInfo;
            }
        }
        setCurrentWallet(context, walletInfos.get(0));
        return walletInfos.get(0);
    }

    public void setCurrentWallet(Context context, WalletInfo walletInfo) {
        SharedPreferencesUtils.writeInt(context, Constants.preferences_wallet_setting, "current_wallet_id", walletInfo.getId());
    }

    public int getWalletIcon(Context context, WalletInfo walletInfo) {
        return SharedPreferencesUtils.getInt(context, Constants.preferences_wallet_setting, "wallet_icon_" + walletInfo.getId(), R.drawable.touxiang_1);
    }

    public void setWalletIcon(Context context, WalletInfo walletInfo, int icon) {
        SharedPreferencesUtils.writeInt(context, Constants.preferences_wallet_setting, "wallet_icon_" + walletInfo.getId(), icon);
    }

    public interface UnlockWalletCallback {
        void onBack();
        void onSuccess();
        void onFail(String msg);
        void onPinLock();
    }

    public void unlockWallet(Activity activity, WalletInfo wallet, UnlockWalletCallback callback) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                View inputPINView = activity.getLayoutInflater().inflate(R.layout.input_pin_dialog, null);
                builder.setView(inputPINView);
                AlertDialog dialog = builder.show();
                dialog.setCancelable(false);
                EditText pinText = inputPINView.findViewById(R.id.inPINEdit);
                inputPINView.findViewById(R.id.closeBut).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        callback.onBack();
                    }
                });
                inputPINView.findViewById(R.id.inputPINBut).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String pinStr = pinText.getText().toString();
                        try {
                            unlockWallet(wallet.getId(), StringUtils.toUTF8(pinStr));
                            //RemindUtils.toastShort(activity, "解锁成功");
                            callback.onSuccess();
                        } catch (VerifyFailException e) {
                            //RemindUtils.toastShort(activity, "PIN码错误");
                            callback.onFail("PIN码错误");
                        } catch (UnexpectedException e) {
                            e.printStackTrace();
                           // RemindUtils.toastShort(activity, e.getMessage());
                            callback.onFail(e.getMessage());
                        } catch (PinLockedException e) {
                            //RemindUtils.toastShort(activity, "PIN码已被锁定");
                            callback.onPinLock();
                        }
                        dialog.dismiss();
                    }
                });
            }
        });
    }

}

