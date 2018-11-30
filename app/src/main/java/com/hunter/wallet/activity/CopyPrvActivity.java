package com.hunter.wallet.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hunter.wallet.R;

/**
 * Created by DT0814 on 2018/8/24.
 */

public class CopyPrvActivity extends SecurityActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.copy_prv_layout);
        String prv = getIntent().getStringExtra("prv");
        if (null == prv || prv.trim().equals("")) {
            Log.e("CopyPrvActivityErr", "noPrv");
        } else {
      /*      AlertDialog.Builder builder = new AlertDialog.Builder(CopyPrvActivity.this);
            View dangerView = getLayoutInflater().inflate(R.layout.danger_msg_dialog, null);
            builder.setView(dangerView);
            AlertDialog show = builder.show();
            show.setCancelable(false);
            ((TextView) dangerView.findViewById(R.id.dangerMsgText)).setText(R.string.prvDangerMsg);
            dangerView.findViewById(R.id.dangerMsgBut).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    show.dismiss();
                }
            });
*/
            TextView prvText = findViewById(R.id.prvText);
            prvText.setText(prv);
            findViewById(R.id.copyPrvPerBut).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CopyPrvActivity.this.finish();
                }
            });
            /*Button copyBut = findViewById(R.id.copyBut);
            copyBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipData mClipData;
                    ClipboardManager clipManager;
                    clipManager = (ClipboardManager) CopyPrvActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    mClipData = ClipData.newPlainText("Label", prv);
                    clipManager.setPrimaryClip(mClipData);
                    RemindUtils.toastShort(CopyPrvActivity.this, "复制成功");
                    copyBut.setText("已复制");
                }
            });*/
        }

    }
}
