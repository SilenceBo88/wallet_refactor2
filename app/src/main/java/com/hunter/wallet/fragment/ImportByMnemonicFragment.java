package com.hunter.wallet.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.hunter.wallet.R;
import com.hunter.wallet.exception.MnemonicTransferException;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.WalletOverflowException;
import com.hunter.wallet.exception.WalletRepeatException;
import com.hunter.wallet.service.SecurityService;
import com.hunter.wallet.service.WalletManageService;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.StringUtils;


public class ImportByMnemonicFragment extends Fragment implements View.OnClickListener, TextWatcher {

    private WalletManageService walletManageService = WalletManageService.getInstance();
    private SecurityService securityService = SecurityService.getInstance();

    public static final String ETH_JAXX_TYPE = "m/44'/60'/0'/0/0";
    public static final String ETH_LEDGER_TYPE = "m/44'/60'/0'/0";
    public static final String ETH_CUSTOM_TYPE = "m/44'/60'/1'/0/0";

    private EditText importInPut;
    private EditText passWord;
    private EditText reImportPassword;
    private EditText importWalletName;
    private String mnemoincTypeStr = ETH_JAXX_TYPE;
    private ImageView importPasswordIcon;
    private ImageView reImportPasswordIcon;
    private Button importButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.import_mnemonic_fragment, null);
        super.onCreate(savedInstanceState);

        importInPut = view.findViewById(R.id.importInPut);
        importWalletName = view.findViewById(R.id.importWalletName);
        passWord = view.findViewById(R.id.importPassword);
        importPasswordIcon = view.findViewById(R.id.importPasswordIcon);
        reImportPassword = view.findViewById(R.id.reImportPassword);
        reImportPasswordIcon = view.findViewById(R.id.reImportPasswordIcon);
        importButton = view.findViewById(R.id.importButton);

        importInPut.addTextChangedListener(this);
        importWalletName.addTextChangedListener(this);
        passWord.addTextChangedListener(this);
        reImportPassword.addTextChangedListener(this);

        Spinner mnemoincType = view.findViewById(R.id.mnemoincType);
        mnemoincType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mnemoincTypeStr = ETH_JAXX_TYPE;
                        break;
                    case 1:
                        mnemoincTypeStr = ETH_LEDGER_TYPE;
                        break;
                    case 2:
                        mnemoincTypeStr = ETH_CUSTOM_TYPE;
                        break;
                    default:
                        mnemoincTypeStr = ETH_JAXX_TYPE;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        importButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String mne = importInPut.getText().toString();
        String nameStr = importWalletName.getText().toString();
        String passStr = passWord.getText().toString();
        String repassStr = reImportPassword.getText().toString();

        if (securityService.checkPwdStrength(passStr)) {
            importPasswordIcon.setImageResource(R.drawable.dui_on);
            if (StringUtils.equal(passStr, repassStr)) {
                reImportPasswordIcon.setImageResource(R.drawable.dui_on);
            }
        } else {
            importPasswordIcon.setImageResource(R.drawable.dui_off);
            reImportPasswordIcon.setImageResource(R.drawable.dui_off);
        }
        if (StringUtils.hasText(mne)
                && StringUtils.hasText(nameStr)
                && securityService.checkPwdFormat(passStr)
                && StringUtils.equal(passStr, repassStr)) {
            importButton.setEnabled(true);
            importButton.setBackgroundResource(R.drawable.fillet_fill_blue_on);
        } else {
            importButton.setEnabled(false);
            importButton.setBackgroundResource(R.drawable.fillet_fill_blue_off);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.importButton: {
                importButton.setEnabled(false);
                String mne = importInPut.getText().toString();
                String nameStr = importWalletName.getText().toString();
                String passStr = passWord.getText().toString();
                securityService.alertPwdStrength(passStr, getActivity(), new SecurityService.AlertCallback() {
                    @Override
                    public void onContinue() {
                        try {
                            walletManageService.recoverByMnemonic(nameStr, passStr, mne, mnemoincTypeStr);
                            RemindUtils.toastShort(getActivity(), "导入成功");
                        } catch (WalletOverflowException e) {
                            RemindUtils.toastShort(getActivity(), "超出钱包数量限制");
                        } catch (MnemonicTransferException e) {
                            RemindUtils.toastShort(getActivity(), "助记词解析失败");
                        } catch (WalletRepeatException e) {
                            RemindUtils.toastShort(getActivity(), "钱包已存在");
                        } catch (UnexpectedException e) {
                            e.printStackTrace();
                            RemindUtils.toastShort(getActivity(), e.getMessage());
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().finish();
                            }
                        });
                    }
                    @Override
                    public void onBack() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                importButton.setEnabled(true);
                            }
                        });
                    }
                });
            }
            break;
        }
    }
}
