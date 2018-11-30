package com.hunter.wallet.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.hunter.wallet.R;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.service.WalletManageService;
import com.hunter.wallet.utils.AddressEncoder;
import com.hunter.wallet.utils.BtcUtil;
import com.hunter.wallet.utils.RemindUtils;
import com.hunter.wallet.utils.ZXingUtils;
import org.web3j.utils.Numeric;

public class BtcAddressShowActivity extends Activity implements View.OnClickListener {
    private WalletManageService walletManageService = WalletManageService.getInstance();

    private ClipboardManager clipManager;
    private String addr;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WalletInfo wallet = (WalletInfo) getIntent().getSerializableExtra(WalletInfo.class.getName());
        //比特币地址
        addr = BtcUtil.getAddress(wallet.getPubkey());
        setContentView(R.layout.address_show_layout);

        TextView addressText = findViewById(R.id.addressText);
        ImageView addressQr = findViewById(R.id.addressQr);
        ImageView imageView = findViewById(R.id.touXiang);

        imageView.setImageResource(walletManageService.getWalletIcon(this, wallet));
        addressText.setText(addr);

        addressQr.post(new Runnable() {
            @Override
            public void run() {
                Log.i("addressQr", addressQr.getWidth() + "  " + addressQr.getHeight());
                //比特币二维码
                Bitmap qrImage = ZXingUtils.createQRImage(AddressEncoder.encodeBtc(new AddressEncoder(addr)), addressQr.getWidth(), addressQr.getHeight());
                addressQr.setImageBitmap(qrImage);
            }
        });
        findViewById(R.id.addressPreBut).setOnClickListener(this);
        findViewById(R.id.copyAddressButton).setOnClickListener(this);

        clipManager = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addressPreBut:
                BtcAddressShowActivity.this.finish();
                break;
            case R.id.copyAddressButton:
                ClipData mClipData = ClipData.newPlainText("Label", addr);
                clipManager.setPrimaryClip(mClipData);
                RemindUtils.toastShort(BtcAddressShowActivity.this, "钱包收款地址复制成功");
                break;
        }
    }
}


