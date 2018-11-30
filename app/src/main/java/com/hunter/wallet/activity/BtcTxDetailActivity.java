package com.hunter.wallet.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.hunter.wallet.R;
import com.hunter.wallet.entity.BtcTx;
import com.hunter.wallet.entity.EthToken;
import com.hunter.wallet.entity.EthTransfer;
import com.hunter.wallet.utils.BtcUtil;
import com.hunter.wallet.utils.ZXingUtils;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;


/**
 * Created by dt0814 on 2018/7/17.
 */

public class BtcTxDetailActivity extends FragmentActivity implements View.OnClickListener {
    private AlertDialog.Builder alertbBuilder;
    private BtcTx btcTx;
    private String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tx_info_layout);
        btcTx = (BtcTx) getIntent().getSerializableExtra(BtcTx.class.getName());
        alertbBuilder = new AlertDialog.Builder(this);
        findViewById(R.id.txInfoUrlBut).setOnClickListener(this);
        findViewById(R.id.txInfoPreBut).setOnClickListener(this);
        ((TextView) findViewById(R.id.txInfoEthNum)).setText(btcTx.getAmount().toString());
        ((TextView) findViewById(R.id.txNumSymName)).setText("BTC");
        ((TextView) findViewById(R.id.txInfoFrom)).setText(btcTx.getFromAddress());
        ((TextView) findViewById(R.id.txInfoTo)).setText(btcTx.getToAddress());
        ((TextView) findViewById(R.id.txInfoHash)).setText(btcTx.getTxHash());
        long time = new Long(btcTx.getTime()) * 1000;
        ((TextView) findViewById(R.id.txInfoTime)).setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time));
        ((TextView) findViewById(R.id.txInfoGas)).setText(btcTx.getFees());
        ((TextView) findViewById(R.id.txInfoBlock)).setText(btcTx.getBlockheight().toString());

        initTxInfoQr();
    }

    private void initTxInfoQr() {
        ImageView txInfoQr = findViewById(R.id.txInfoQr);
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        txInfoQr.measure(width, height);
        height = txInfoQr.getMeasuredHeight();
        width = txInfoQr.getMeasuredWidth();
        url = BtcUtil.BTC_URL + btcTx.getTxHash();
        Bitmap qrImage = ZXingUtils.createQRImage(url, width, height);
        txInfoQr.setImageBitmap(qrImage);
        txInfoQr.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txInfoUrlBut:
                ClipData clipData = ClipData.newPlainText("Label", url);
                ClipboardManager clipManager = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                clipManager.setPrimaryClip(clipData);
                Toast.makeText(BtcTxDetailActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                break;
            case R.id.txInfoPreBut:
                BtcTxDetailActivity.this.finish();
                break;
            case R.id.txInfoQr:
                LayoutInflater inflater = BtcTxDetailActivity.this.getLayoutInflater();
                View QrView = inflater.inflate(R.layout.qr_image_layout, null);
                ImageView image = QrView.findViewById(R.id.qr_image);
                Bitmap qrImage = ZXingUtils.createQRImage(url, 300, 300);
                image.setImageBitmap(qrImage);
                alertbBuilder.setView(QrView);
                alertbBuilder.setPositiveButton("关闭",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertbBuilder.show();
                break;
        }
    }
}
