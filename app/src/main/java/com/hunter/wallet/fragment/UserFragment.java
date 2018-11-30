package com.hunter.wallet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hunter.wallet.R;
import com.hunter.wallet.activity.AgreementActivity;
import com.hunter.wallet.activity.ContactsActivity;
import com.hunter.wallet.activity.AboutActivity;
import com.hunter.wallet.activity.HelpActivity;
import com.hunter.wallet.activity.ReSetActivity;
import com.hunter.wallet.activity.RebindPhoneActivity;
import com.hunter.wallet.activity.UpdatePinActivity;
import com.hunter.wallet.utils.RemindUtils;

public class UserFragment extends Fragment implements View.OnClickListener {

    private FragmentActivity activity;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i("UserFragment", "onHiddenChanged hidden:" + hidden);
        if (hidden) {
        } else {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_info_fragment, null);
        super.onCreate(savedInstanceState);
        activity = getActivity();
        view.findViewById(R.id.guanyu).setOnClickListener(this);
        view.findViewById(R.id.contacts).setOnClickListener(this);
        view.findViewById(R.id.message).setOnClickListener(this);
        view.findViewById(R.id.helpLayout).setOnClickListener(this);
        view.findViewById(R.id.agreement).setOnClickListener(this);
        view.findViewById(R.id.updatePIN).setOnClickListener(this);
        view.findViewById(R.id.reBindPhone).setOnClickListener(this);
        view.findViewById(R.id.reSetWallet).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.guanyu:
                startActivity(new Intent(activity, AboutActivity.class));
                break;
            case R.id.agreement:
                startActivity(new Intent(activity, AgreementActivity.class));
                break;
            case R.id.helpLayout:
                startActivity(new Intent(activity, HelpActivity.class));
                break;
            case R.id.contacts: {
                Intent intent = new Intent(activity, ContactsActivity.class);
                intent.putExtra("startFrom", UserFragment.class.getName());
                startActivity(intent);
            }
            break;
            case R.id.updatePIN:
                startActivity(new Intent(activity, UpdatePinActivity.class));
                break;
            case R.id.reBindPhone:
                startActivity(new Intent(activity, RebindPhoneActivity.class));
                break;
            case R.id.reSetWallet:
                startActivity(new Intent(activity, ReSetActivity.class));
                break;
            default:
                RemindUtils.toastShort(activity, "          功能开发中        ");
        }
    }
}
