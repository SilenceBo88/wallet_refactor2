package com.hunter.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.entity.Token;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.service.TokenManageService;

import java.util.ArrayList;
import java.util.List;

public class TokenSelectActivity extends Activity implements View.OnClickListener {

    private TokenManageService tokenManageService = TokenManageService.getInstance();

    private ListView listView;
    private WalletInfo wallet;
    private List<Token> tokens = new ArrayList<>();
    private List<Token> walletSelectedTokens = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wallet = (WalletInfo) getIntent().getSerializableExtra(WalletInfo.class.getName());
        setContentView(R.layout.coin_add_layout);
        listView = findViewById(R.id.addCoinListView);

        findViewById(R.id.coinAddSousuo).setOnClickListener(this);
        findViewById(R.id.coinAddPreBut).setOnClickListener(this);
        drawTokenList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tokens.clear();
        tokens.addAll(tokenManageService.getPreToken(this));
        tokens.addAll(tokenManageService.getEthImportedToken(this));
        walletSelectedTokens.clear();
        walletSelectedTokens.addAll(tokenManageService.getWalletSelectToken(this, wallet));
        ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    private void drawTokenList() {
        listView.setAdapter(new ArrayAdapter<Token>(this, R.layout.coin_add_list_view, tokens) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.coin_add_list_view, null);
                }
                Token token = getItem(position);
                ImageView coinIcon = convertView.findViewById(R.id.CoinAddIcon);
                TextView coinName = convertView.findViewById(R.id.coinSymbolName);
                TextView coinLongName = convertView.findViewById(R.id.coinAddName);
                Switch swith = convertView.findViewById(R.id.addCoinSwitch);

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
                coinLongName.setText(token.getName());

                if (walletSelectedTokens.contains(token)) {
                    swith.setChecked(true);
                } else {
                    swith.setChecked(false);
                }
                swith.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            tokenManageService.addWalletSelectToken(TokenSelectActivity.this, wallet, token);
                            walletSelectedTokens.add(token);
                        } else {
                            tokenManageService.removeWalletSelectToken(TokenSelectActivity.this, wallet, token);
                            walletSelectedTokens.remove(token);
                        }
                    }
                });
                return convertView;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.coinAddPreBut:
                TokenSelectActivity.this.finish();
                break;
            case R.id.coinAddSousuo:
                startActivity(new Intent(TokenSelectActivity.this, EthTokenImportActivity.class));
                break;
        }
    }
}

