package com.hunter.wallet.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.activity.CreateWalletActivity;
import com.hunter.wallet.activity.ImportActivity;
import com.hunter.wallet.activity.WalletInfoActivity;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.service.SecurityService;
import com.hunter.wallet.service.TokenManageService;
import com.hunter.wallet.service.TxManageService;
import com.hunter.wallet.service.WalletManageService;
import com.hunter.wallet.utils.RemindUtils;

import org.web3j.utils.Numeric;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WalletFragment extends Fragment implements View.OnClickListener {

    private SecurityService securityService = SecurityService.getInstance();
    private WalletManageService walletManageService = WalletManageService.getInstance();
    private TxManageService txManageService = TxManageService.getInstance();
    private TokenManageService tokenManageService = TokenManageService.getInstance();

    private ListView walletListView;
    private Timer updateTimer;
    private List<WalletInfo> walletInfos;

    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_wallet_fragment, null);
        walletListView = view.findViewById(R.id.walletList);

        view.findViewById(R.id.createWallet).setOnClickListener(this);
        view.findViewById(R.id.inWallet).setOnClickListener(this);

        walletListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WalletInfo wallet = (WalletInfo) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(getActivity(), WalletInfoActivity.class);
                intent.putExtra(WalletInfo.class.getName(), wallet);
                intent.putExtra("walletId", wallet.getId());
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            // 获取焦点且页面非隐藏状态，重新渲染资产
            reRender();
            startUpdateBalance();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isHidden()) {
            // 失去焦点且非隐藏状态，停止更新余额线程
            stopUpdateBalance();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {

        // 页面状态发生变化
        super.onHiddenChanged(hidden);
        if (!hidden) {
            // 显示页面时，重新渲染资产
            reRender();
            startUpdateBalance();
        } else {
            // 隐藏页面时，停止更新余额线程
            stopUpdateBalance();
        }
    }

    private void startUpdateBalance() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ArrayAdapter) walletListView.getAdapter()).notifyDataSetChanged();
                    }
                });
            }
        }, 0, 30000);
    }

    // 停止更新余额
    private void stopUpdateBalance() {
        Log.i("WalletFragment", "stopUpdateBalance");
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    private void reRender() {
        try {
            walletInfos = walletManageService.getAllWallet();
        } catch (UnexpectedException e) {
            e.printStackTrace();
            RemindUtils.toastShort(getActivity(), e.getMessage());
        }
        walletListView.setAdapter(new ArrayAdapter<WalletInfo>(getContext(), R.layout.wallet_item, walletInfos) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.wallet_item, null);
                }
                WalletInfo walletInfo = walletInfos.get(position);
                View bg = convertView.findViewById(R.id.bg);
                ImageView icon = convertView.findViewById(R.id.wallet_item_img);
                TextView walletName = convertView.findViewById(R.id.wallet_item_name);
                TextView tranNum = convertView.findViewById(R.id.wallet_item_address);
                TextView walletCacheNum = convertView.findViewById(R.id.walletCacheNum);

                switch (position % 5) {
                    case 4:
                        bg.setBackgroundResource(R.drawable.walletbg_5);
                        break;
                    case 3:
                        bg.setBackgroundResource(R.drawable.walletbg_4);
                        break;
                    case 2:
                        bg.setBackgroundResource(R.drawable.walletbg_1);
                        break;
                    case 1:
                        bg.setBackgroundResource(R.drawable.walletbg_3);
                        break;
                    case 0:
                        bg.setBackgroundResource(R.drawable.walletbg_2);
                        break;
                }
                icon.setImageResource(walletManageService.getWalletIcon(getActivity(), walletInfo));
                walletName.setText(walletInfo.getName());
                tranNum.setText(Numeric.toHexString(walletInfo.getAddr()));
                walletCacheNum.setText(txManageService.getWalletBalance(getActivity(), walletInfo, tokenManageService.getEthToken(getActivity())).toString());
                return convertView;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.inWallet:
                securityService.alertKillNonsystemProcess(getActivity(), new SecurityService.AlertCallback() {
                    @Override
                    public void onContinue() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getActivity(), ImportActivity.class));
                            }
                        });

                    }

                    @Override
                    public void onBack() {
                    }
                });
                break;
            case R.id.createWallet:
                securityService.alertKillNonsystemProcess(getActivity(), new SecurityService.AlertCallback() {
                    @Override
                    public void onContinue() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getActivity(), CreateWalletActivity.class));
                            }
                        });
                    }

                    @Override
                    public void onBack() {
                    }
                });
                break;
        }
    }

}
