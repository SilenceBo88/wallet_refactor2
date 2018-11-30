package com.hunter.wallet;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.hunter.wallet.service.UpdateCacheService;

public class WalletApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                activity.startService(new Intent(activity, UpdateCacheService.class));
                Intent intent = new Intent();
                intent.setAction("wallet_application_onResume");
                activity.sendBroadcast(intent);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Intent intent = new Intent();
                intent.setAction("wallet_application_onPaused");
                activity.sendBroadcast(intent);
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

    }
}
