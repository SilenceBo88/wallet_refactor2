package com.hunter.wallet.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.fragment.NoWalletFragment;
import com.hunter.wallet.fragment.PriceFragment;
import com.hunter.wallet.fragment.PropertyFragment;
import com.hunter.wallet.fragment.UserFragment;
import com.hunter.wallet.fragment.WalletFragment;
import com.hunter.wallet.service.WalletManageService;
import com.hunter.wallet.utils.RemindUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFragmentActivity extends FragmentActivity implements View.OnClickListener {

    private WalletManageService walletManageService = WalletManageService.getInstance();

    private enum FragmentTab {
        nowallet, property, wallet, price, user
    }

    //底部导航切换按钮
    private ImageView propertyBtn;
    private ImageView walletBtn;
    private ImageView priceBtn;
    private ImageView userBtn;
    //底部导航功能提示文字
    private TextView propertyTextView;
    private TextView walletTextView;
    private TextView priceTextView;
    private TextView userTextView;

    private Map<FragmentTab, Fragment> fragmentMap;
    private View currentTabView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Android5.0之后的沉浸式状态栏写法
        Window window = this.getWindow();
        View decorView = window.getDecorView();
        // 两个标志位要结合使用，表示让应用的主体内容占用系统状态栏的空间
        // 第三个标志位可让底部导航栏变透明View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        decorView.setSystemUiVisibility(option);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        setContentView(R.layout.main_fragment_layout);

        //初始化控件
        propertyBtn = findViewById(R.id.propertyBtn);
        walletBtn = findViewById(R.id.walletBtn);
        priceBtn = findViewById(R.id.priceBtn);
        userBtn = findViewById(R.id.userBtn);

        propertyTextView = findViewById(R.id.propertyTextView);
        walletTextView = findViewById(R.id.walletTextView);
        priceTextView = findViewById(R.id.priceTextView);
        userTextView = findViewById(R.id.userTextView);

        //初始化底部点击布局文件
        //设置点击事件
        findViewById(R.id.propertyLayout).setOnClickListener(this);
        findViewById(R.id.priceLayout).setOnClickListener(this);
        findViewById(R.id.walletLayout).setOnClickListener(this);
        findViewById(R.id.userLayout).setOnClickListener(this);

        fragmentMap = new HashMap<>();
        fragmentMap.put(FragmentTab.nowallet, new NoWalletFragment());
        fragmentMap.put(FragmentTab.property, new PropertyFragment());
        fragmentMap.put(FragmentTab.wallet, new WalletFragment());
        fragmentMap.put(FragmentTab.price, new PriceFragment());
        fragmentMap.put(FragmentTab.user, new UserFragment());

        currentTabView = findViewById(R.id.propertyLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onClick(currentTabView);
    }

    @Override
    public void onClick(View view) {
        currentTabView = view;
        resetTab();
        switch (view.getId()) {
            case R.id.propertyLayout:
                propertyTextView.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                propertyBtn.setImageResource(R.drawable.main_property_on);
                List<WalletInfo> walletInfos = null;
                try {
                    walletInfos = walletManageService.getAllWallet();
                } catch (UnexpectedException e) {
                    e.printStackTrace();
                    RemindUtils.toastShort(MainFragmentActivity.this, e.getMessage());
                }
                if (walletInfos == null || walletInfos.size() == 0) {
                    fragmentChange(FragmentTab.nowallet);
                } else {
                    fragmentChange(FragmentTab.property);
                }
                break;
            case R.id.walletLayout:
                walletTextView.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                walletBtn.setImageResource(R.drawable.main_wallet_on);
                fragmentChange(FragmentTab.wallet);
                break;
            case R.id.priceLayout:
                priceTextView.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                priceBtn.setImageResource(R.drawable.main_price_on);
                fragmentChange(FragmentTab.price);
                break;
            case R.id.userLayout:
                userTextView.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                userBtn.setImageResource(R.drawable.main_user_on);
                fragmentChange(FragmentTab.user);
                break;
        }
    }

    //重置底部导航为未选中状态
    private void resetTab() {
        propertyBtn.setImageResource(R.drawable.main_property_off);
        propertyTextView.setTextColor(getResources().getColor(R.color.navigationOffClolor, null));
        userBtn.setImageResource(R.drawable.main_user_off);
        userTextView.setTextColor(getResources().getColor(R.color.navigationOffClolor, null));
        walletBtn.setImageResource(R.drawable.main_wallet_off);
        walletTextView.setTextColor(getResources().getColor(R.color.navigationOffClolor, null));
        priceBtn.setImageResource(R.drawable.main_price_off);
        priceTextView.setTextColor(getResources().getColor(R.color.navigationOffClolor, null));
    }

    private void fragmentChange(FragmentTab tab) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = fragmentMap.get(tab);
        Fragment currentFragment = null;
        for (Fragment f : manager.getFragments()) {
            if (!f.isHidden()) {
                currentFragment = f;
                if (currentFragment == fragment) {
                    return;
                } else {
                    break;
                }
            }
        }
        FragmentTransaction ft = manager.beginTransaction();
        if (currentFragment != null) {
            ft.hide(currentFragment);
        }
        if (!manager.getFragments().contains(fragment)) {
            // 添加fragment会自动显示
            ft.add(R.id.frame, fragment);
        } else {
            ft.show(fragment);
        }
        ft.commit();
    }
}
