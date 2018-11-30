package com.hunter.wallet.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioGroup;

import com.hunter.wallet.fragment.ImportByKeystoreFragment;
import com.hunter.wallet.fragment.ImportByMnemonicFragment;
import com.hunter.wallet.fragment.ImportByPrikeyFragment;
import com.hunter.wallet.R;

import java.util.HashMap;
import java.util.Map;

public class ImportActivity extends SecurityFragmentActivity implements View.OnClickListener {

    private Map<FragmentTab, Fragment> fragmentsMap;

    private enum FragmentTab {
        keystore, mnemonic, prikey
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_wallet_layout);

        fragmentsMap = new HashMap<>();
        fragmentsMap.put(FragmentTab.keystore, new ImportByKeystoreFragment());
        fragmentsMap.put(FragmentTab.mnemonic, new ImportByMnemonicFragment());
        fragmentsMap.put(FragmentTab.prikey, new ImportByPrikeyFragment());

        onTabSelected(FragmentTab.keystore);
        RadioGroup rd = findViewById(R.id.radioGroup);
        findViewById(R.id.importPreBut).setOnClickListener(this);

        rd.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.keyStoreRadio:
                        onTabSelected(FragmentTab.keystore);
                        break;
                    case R.id.prvRadio:
                        onTabSelected(FragmentTab.prikey);
                        break;
                    case R.id.WordRadio:
                        onTabSelected(FragmentTab.mnemonic);
                        break;
                }
            }
        });
    }

    //点击item时跳转不同的碎片

    public void onTabSelected(FragmentTab tab) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        Fragment fragment = fragmentsMap.get(tab);
        if (!manager.getFragments().contains(fragment)) {
            ft.add(R.id.importFrame, fragment);
        }
        for(Fragment f : fragmentsMap.values()){
            ft.hide(f);
        }
        ft.show(fragment);
        ft.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.importPreBut:
                ImportActivity.this.finish();
                break;
        }
    }
}
