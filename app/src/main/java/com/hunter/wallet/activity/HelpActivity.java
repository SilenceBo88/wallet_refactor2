package com.hunter.wallet.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.hunter.wallet.R;

public class HelpActivity extends Activity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_info_fragment_help_layout);
        findViewById(R.id.helpPreBut).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                HelpActivity.this.finish();
            }
        });
    }
}