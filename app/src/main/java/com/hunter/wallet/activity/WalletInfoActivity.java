package com.hunter.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.PinLockedException;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.VerifyFailException;
import com.hunter.wallet.exception.WalletLockedException;
import com.hunter.wallet.service.SecurityService;
import com.hunter.wallet.service.WalletManageService;
import com.hunter.wallet.utils.AppUtils;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.StringUtils;

import org.web3j.utils.Numeric;

public class WalletInfoActivity extends Activity implements View.OnClickListener, WalletManageService.UnlockWalletCallback {
    private SecurityService securityService = SecurityService.getInstance();
    private WalletManageService walletManageService = WalletManageService.getInstance();

    @Override
    public void onBack() {

    }

    @Override
    public void onSuccess() {
        walletInfo.setHasLock(false);
        RemindUtils.toastShort(this, "解锁成功");
    }

    @Override
    public void onFail(String msg) {
        RemindUtils.toastShort(this, msg);
    }

    @Override
    public void onPinLock() {
        AppUtils.startActivity(this, UnlockActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    private enum KeyType {
        mnemonic, keystore, prikey
    }

    private EditText nameEdit;
    LayoutInflater inflater;
    AlertDialog.Builder alertbBuilder;
    private ImageView touXiangImg;
    private TextView walletName;
    private TextView addressText;
    TextView ethNum;
    private WalletInfo walletInfo;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        walletInfo = (WalletInfo) getIntent().getSerializableExtra(WalletInfo.class.getName());
        setContentView(R.layout.wallet_info_layout);
        inflater = getLayoutInflater();
        walletName = findViewById(R.id.walletName);
        addressText = findViewById(R.id.addressText);
        nameEdit = findViewById(R.id.nameEdit);
        touXiangImg = findViewById(R.id.touXiangImg);
        alertbBuilder = new AlertDialog.Builder(this);
        ethNum = findViewById(R.id.ethNum);

        findViewById(R.id.copyPrvkeyBut).setOnClickListener(this);
        findViewById(R.id.copyKeyStoreBut).setOnClickListener(this);

        if (walletInfo.isHasMne()) {
            findViewById(R.id.copyMnemonicBut).setOnClickListener(this);
        } else {
            findViewById(R.id.copyMnemonicBut).setVisibility(View.GONE);
        }
        findViewById(R.id.updatePassBut).setOnClickListener(this);
        findViewById(R.id.deleteWalletBut).setOnClickListener(this);
        findViewById(R.id.saveBut).setOnClickListener(this);
        findViewById(R.id.walletInfoPreBut).setOnClickListener(this);
        touXiangImg.setOnClickListener(this);
        walletName.setText("钱包");

    }

    @Override
    protected void onResume() {

        super.onResume();
        // 每次获取焦点更新钱包信息
        try {
            walletInfo = walletManageService.getWallet(walletInfo.getId());
        } catch (UnexpectedException e) {
            e.printStackTrace();
            RemindUtils.toastShort(this, e.getMessage());
        }
        touXiangImg.setImageResource(walletManageService.getWalletIcon(this, walletInfo));
        nameEdit.setText(walletInfo.getName());
        ethNum.setText(walletInfo.getName());
        addressText.setText(Numeric.toHexString(walletInfo.getAddr()));
        Log.i("walletinfo onResume", "onResume");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.walletInfoPreBut:
                this.finish();
                break;
            case R.id.copyPrvkeyBut:
                exportKey(KeyType.prikey);
                break;
            case R.id.copyKeyStoreBut:
                exportKey(KeyType.keystore);
                break;
            case R.id.copyMnemonicBut:
                exportKey(KeyType.mnemonic);
                break;
            case R.id.updatePassBut:
                if (walletInfo.isHasLock()) {
                    walletManageService.unlockWallet(WalletInfoActivity.this, walletInfo, WalletInfoActivity.this);
                } else {
                    Intent toUpdateIntent = new Intent(WalletInfoActivity.this, UpdatePassActivity.class);
                    toUpdateIntent.putExtra(WalletInfo.class.getName(), walletInfo);
                    startActivity(toUpdateIntent);
                }
                break;
            case R.id.saveBut:
                String text = nameEdit.getText().toString();
                if (StringUtils.hasText(text)) {
                    try {
                        walletManageService.changeName(walletInfo.getId(), text);
                        RemindUtils.toastShort(WalletInfoActivity.this, "保存成功");
                        WalletInfoActivity.this.finish();
                    } catch (UnexpectedException e) {
                        e.printStackTrace();
                        RemindUtils.toastShort(WalletInfoActivity.this, e.getMessage());
                    }
                } else {
                    RemindUtils.toastShort(WalletInfoActivity.this, "钱包名不能为空");
                }
                break;
            case R.id.deleteWalletBut:
                if (walletInfo.isHasLock()) {
                    walletManageService.unlockWallet(WalletInfoActivity.this, walletInfo, WalletInfoActivity.this);
                } else {
                    View inPass = inflater.inflate(R.layout.input_pwd_dialog, null);
                    alertbBuilder.setView(inPass);
                    AlertDialog inPassDialog = alertbBuilder.show();
                    inPass.findViewById(R.id.closeBut).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            inPassDialog.dismiss();
                        }
                    });
                    inPass.findViewById(R.id.inputPassBut).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText editText = inPass.findViewById(R.id.inPwdEdit);
                            String passWord = editText.getText().toString();
                            try {
                                walletManageService.deleteWallet(walletInfo.getId(), passWord);
                                RemindUtils.toastShort(WalletInfoActivity.this, "删除成功");
                                WalletInfoActivity.this.finish();
                            } catch (WalletLockedException e) {
                                RemindUtils.toastShort(WalletInfoActivity.this, "钱包已被锁定");
                                walletInfo.setHasLock(true);
                            } catch (VerifyFailException e) {
                                RemindUtils.toastShort(WalletInfoActivity.this, "密码错误");
                            } catch (UnexpectedException e) {
                                e.printStackTrace();
                            }
                            inPassDialog.dismiss();
                        }
                    });
                }
                break;
        }
    }

    public interface InputPassListener {
        void onCommit(String password);
    }

    private void inputPass(InputPassListener listener) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View inPass = inflater.inflate(R.layout.input_pwd_dialog, null);
                alertbBuilder.setView(inPass);
                AlertDialog inPassDialog = alertbBuilder.show();
                inPass.findViewById(R.id.closeBut).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inPassDialog.dismiss();
                    }
                });
                inPass.findViewById(R.id.inputPassBut).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editText = inPass.findViewById(R.id.inPwdEdit);
                        String passWord = editText.getText().toString();
                        inPassDialog.dismiss();
                        listener.onCommit(passWord);
                    }
                });
            }
        });
    }

    private void exportKey(KeyType type) {
        if (walletInfo.isHasLock()) {
            walletManageService.unlockWallet(WalletInfoActivity.this, walletInfo, WalletInfoActivity.this);
        } else {
            securityService.alertKillNonsystemProcess(WalletInfoActivity.this, new SecurityService.AlertCallback() {
                @Override
                public void onContinue() {
                    inputPass(new InputPassListener() {
                        @Override
                        public void onCommit(String password) {
                            try {
                                switch (type) {
                                    case prikey:
                                        Intent prvIntent = new Intent(WalletInfoActivity.this, CopyPrvActivity.class);
                                        prvIntent.putExtra("prv", Numeric.toHexString(walletManageService.getPrikey(walletInfo.getId(), password)));
                                        startActivity(prvIntent);
                                        break;
                                    case keystore:
                                        Intent keyIntent = new Intent(WalletInfoActivity.this, CopyKeyStoreActivity.class);
                                        keyIntent.putExtra("key", walletManageService.getKeystore(walletInfo.getId(), password));
                                        startActivity(keyIntent);
                                        break;
                                    case mnemonic:
                                        String mnemonic = walletManageService.getMnemonic(walletInfo.getId(), password);
                                        if (!StringUtils.hasText(mnemonic)) {
                                            RemindUtils.toastShort(WalletInfoActivity.this
                                                    , "该钱包没有助记词"
                                            );
                                        } else {
                                            Intent mnIntent = new Intent(WalletInfoActivity.this, CopyMnemonicActivity.class);
                                            mnIntent.putExtra("mne", mnemonic.trim());
                                            startActivity(mnIntent);
                                        }
                                        break;
                                }
                            } catch (VerifyFailException e) {
                                RemindUtils.toastShort(WalletInfoActivity.this, "密码错误");
                            } catch (UnexpectedException e) {
                                e.printStackTrace();
                                RemindUtils.toastShort(WalletInfoActivity.this, e.getMessage());
                            } catch (WalletLockedException e) {
                                RemindUtils.toastShort(WalletInfoActivity.this, "钱包已被锁定");
                                walletInfo.setHasLock(true);
                            }
                        }
                    });
                }

                @Override
                public void onBack() {
                }
            });
        }
    }
}
