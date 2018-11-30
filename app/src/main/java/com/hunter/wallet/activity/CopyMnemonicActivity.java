package com.hunter.wallet.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hunter.wallet.R;

public class CopyMnemonicActivity extends SecurityActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.copy_mnemonic_layout);
        String mne = getIntent().getStringExtra("mne");
        TextView prvText = findViewById(R.id.MnemonicText);
        prvText.setText(mne);
        findViewById(R.id.copyMnemonicPerBut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CopyMnemonicActivity.this.finish();
            }
        });
    }
}
