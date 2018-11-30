package com.hunter.wallet.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import com.hunter.wallet.activity.BtcTxInfoActivity;
import com.hunter.wallet.activity.EthTxInfoActivity;
import com.hunter.wallet.activity.TokenSelectActivity;
import com.hunter.wallet.entity.EthToken;
import com.hunter.wallet.entity.Token;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.service.TokenManageService;
import com.hunter.wallet.service.TxManageService;
import com.hunter.wallet.service.WalletManageService;
import com.hunter.wallet.utils.CustomDrawerLayout;
import com.hunter.wallet.utils.RemindUtils;

import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 资产页
 */
public class PropertyFragment extends Fragment implements View.OnClickListener {

    private WalletManageService walletManageService = WalletManageService.getInstance();
    private TokenManageService tokenManageService = TokenManageService.getInstance();
    private TxManageService txManageService = TxManageService.getInstance();

    private ImageView icon;
    private TextView walletName;
    //        private TextView homeShowAddress;
    private TextView totalProperty;
    private ListView coinListView;
    private CustomDrawerLayout drawerLayout; //侧边导航栏
    private View view;
    private Timer updateBalanceTimer;
    private WalletInfo wallet;
    //    private List<EthToken> walletEthTokens = new ArrayList<>();
    private List<Token> walletTokens = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.main_property_fragment, null);
        icon = view.findViewById(R.id.walletIcon);
        walletName = view.findViewById(R.id.walletName);
        totalProperty = view.findViewById(R.id.totalProperty);
//        homeShowAddress = view.findViewById(R.id.homeShowAddress);
        drawerLayout = view.findViewById(R.id.home_drawer_layout);
        coinListView = view.findViewById(R.id.coinListView);

        icon.setOnClickListener(this);
        walletName.setOnClickListener(this);
//        homeShowAddress.setOnClickListener(this);
//        view.findViewById(R.id.toAddressLayout).setOnClickListener(this);
        view.findViewById(R.id.addCoinBut).setOnClickListener(this);
        view.findViewById(R.id.changeWalletBtn).setOnClickListener(this);

        // 进入代币详情
        coinListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Token token = (Token) adapterView.getItemAtPosition(i);
                switch (token.getTokenType()) {
                    case eth: {
                        Intent intent = new Intent(getActivity(), EthTxInfoActivity.class);
                        intent.putExtra(WalletInfo.class.getName(), wallet);
                        intent.putExtra(EthToken.class.getName(), token);
                        startActivity(intent);
                    }
                    break;
                    case btc: {
                        Intent intent = new Intent(getActivity(), BtcTxInfoActivity.class);
                        intent.putExtra(WalletInfo.class.getName(), wallet);
                        startActivity(intent);
                    }
                    break;
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            reRender();
            startUpdateBalance();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isHidden()) {
            stopUpdateBalance();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            reRender();
            startUpdateBalance();
        } else {
            stopUpdateBalance();
        }
    }

    // 重新渲染
    private void reRender() {
        // 每次渲染重新获取当前钱包
        try {
            wallet = walletManageService.getCurrentWallet(getActivity());
            // 重新获取钱包代币列表
            walletTokens.clear();
            walletTokens.addAll(tokenManageService.getWalletSelectToken(getActivity(), wallet));
//
//            walletEthTokens = tokenManageService.getWalletSelectedToken(getActivity(), wallet);

            icon.setImageResource(walletManageService.getWalletIcon(getActivity(), wallet));
            walletName.setText(wallet.getName());
//            homeShowAddress.setText(Numeric.toHexString(wallet.getAddr()));
            updateTotalValue();
            // 重新渲染代币资产
            drawTokenList();
            renderDrawerLayout();
        } catch (UnexpectedException e) {
            e.printStackTrace();
            RemindUtils.toastShort(getActivity(), e.getMessage());
        }
    }

    private void startUpdateBalance() {
        if (updateBalanceTimer != null) {
            updateBalanceTimer.cancel();
        }
        updateBalanceTimer = new Timer();
        updateBalanceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTotalValue();
                        ((ArrayAdapter) coinListView.getAdapter()).notifyDataSetChanged();
                    }
                });
            }
        }, 0, 5000);
    }

    private void updateTotalValue() {
        BigDecimal totalValue = BigDecimal.ZERO;
        for (Token token : walletTokens) {
            BigDecimal balance = txManageService.getWalletBalance(getActivity(), wallet, token);
            totalValue = totalValue.add(tokenManageService.getTokenPriceUsd(getActivity(), token).multiply(balance));
        }
        totalProperty.setText(totalValue.toString());
    }

    // 停止更新余额
    private void stopUpdateBalance() {
        Log.i("PropertyFragment", "stopUpdateBalance");
        if (updateBalanceTimer != null) {
            updateBalanceTimer.cancel();
            updateBalanceTimer = null;
        }
    }

    private void drawTokenList() {
        coinListView.setAdapter(new ArrayAdapter<Token>(getContext(), R.layout.coin_list_view, walletTokens) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.coin_list_view, null);
                }
                TextView coinName = convertView.findViewById(R.id.coinName);
                TextView coinValue = convertView.findViewById(R.id.coinValue);
                TextView coinNum = convertView.findViewById(R.id.coinNum);
                ImageView coinIcon = convertView.findViewById(R.id.CoinIcon);
                Token token = getItem(position);

                BigDecimal balance = txManageService.getWalletBalance(getActivity(), wallet, token);

                coinNum.setText(balance.toString());
                coinValue.setText("≈\b$\b" + tokenManageService.getTokenPriceUsd(getActivity(), token).multiply(balance).toString());

                if (token.getIcon() != null) {
                    // 预置图标
                    coinIcon.setImageResource(token.getIcon());
                } else if (token.getIconUrl() != null) {
                    // TODO 获取网路图标
                    coinIcon.setImageResource(R.drawable.coin_eth);
                } else {
                    // 默认图标
                    coinIcon.setImageResource(R.drawable.coin_eth);
                }
                coinName.setText(token.getSymbol());
                return convertView;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addCoinBut: {
                Intent intent = new Intent(getActivity(), TokenSelectActivity.class);
                intent.putExtra(WalletInfo.class.getName(), wallet);
                startActivity(intent);
            }
            break;
//            case R.id.toAddressLayout:
//            case R.id.homeShowAddress: {
//                Intent intent = new Intent(getActivity(), AddressShowActivity.class);
//                intent.putExtra(WalletInfo.class.getName(), wallet);
//                startActivity(intent);
//            }
//            break;
            case R.id.changeWalletBtn:
                drawerLayout.openDrawer(GravityCompat.END);//打开侧边导航栏
                break;
        }
    }

    //  右侧导航栏
    private void renderDrawerLayout() {
        try {
            List<WalletInfo> allWallet = walletManageService.getAllWallet();
            NavigationView navigationView = view.findViewById(R.id.nav_view);
            View nView = navigationView.getHeaderView(0);
            RecyclerView recyclerViewWallet = nView.findViewById(R.id.listView);
            StaggeredGridLayoutManager layoutManager2 = new
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
            recyclerViewWallet.setLayoutManager(layoutManager2);
            recyclerViewWallet.setNestedScrollingEnabled(false);

            WalletSymAdapter adapter = new WalletSymAdapter(allWallet, drawerLayout);
            recyclerViewWallet.setAdapter(adapter);
        } catch (UnexpectedException e) {
            e.printStackTrace();
            RemindUtils.toastShort(getActivity(), e.getMessage());
        }
    }

    //切换钱包
    private class WalletSymAdapter extends RecyclerView.Adapter<WalletSymAdapter.ViewHolder> {
        private List<WalletInfo> walletList;
        private CustomDrawerLayout drawerLayout;

        private WalletSymAdapter(List<WalletInfo> walletList, CustomDrawerLayout drawerLayout) {
            this.walletList = walletList;
            this.drawerLayout = drawerLayout;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            View bg;
            ImageView touxiang;
            TextView name;
            TextView address;

            public ViewHolder(View view) {
                super(view);
                bg = view.findViewById(R.id.bg);
                touxiang = view.findViewById(R.id.walletIcon);
                name = view.findViewById(R.id.name);
                address = view.findViewById(R.id.address);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.wallet_list_sym_item, parent, false);
            ViewHolder holder = new ViewHolder(view);
            holder.bg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (walletList.get(position).getId() != wallet.getId()) {
                        walletManageService.setCurrentWallet(getActivity(), walletList.get(position));
                        reRender();
                    }
                    drawerLayout.closeDrawers();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            WalletInfo wallet = walletList.get(position);
            holder.name.setText(wallet.getName());
            holder.address.setText(Numeric.toHexString(wallet.getAddr()));
            holder.touxiang.setImageResource(walletManageService.getWalletIcon(getActivity(), wallet));
            switch (position % 5) {
                case 4:
                    holder.bg.setBackgroundResource(R.drawable.walletbg_5);
                    break;
                case 3:
                    holder.bg.setBackgroundResource(R.drawable.walletbg_4);
                    break;
                case 2:
                    holder.bg.setBackgroundResource(R.drawable.walletbg_1);
                    break;
                case 1:
                    holder.bg.setBackgroundResource(R.drawable.walletbg_3);
                    break;
                case 0:
                    holder.bg.setBackgroundResource(R.drawable.walletbg_2);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return walletList.size();
        }
    }

}
