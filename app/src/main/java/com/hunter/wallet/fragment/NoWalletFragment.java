package com.hunter.wallet.fragment;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.activity.CreateWalletActivity;
import com.hunter.wallet.activity.ImportActivity;
import com.hunter.wallet.service.SecurityService;

public class NoWalletFragment extends Fragment implements View.OnClickListener {
    private  SecurityService securityService = SecurityService.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.no_have_wallet_layout, null);
        view.findViewById(R.id.createBut).setOnClickListener(this);
        view.findViewById(R.id.importBut).setOnClickListener(this);
        AssetManager assets = getActivity().getAssets();
        Typeface tf = Typeface.createFromAsset(assets, "fonts/franklin_gothic_medium.ttf");
        ((TextView) view.findViewById(R.id.tokenSafe)).setTypeface(tf);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createBut:
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
            case R.id.importBut:
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
        }
    }
}
