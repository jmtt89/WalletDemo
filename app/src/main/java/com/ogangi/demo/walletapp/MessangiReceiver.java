package com.ogangi.demo.walletapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MessangiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String json = intent.getStringExtra("message");
        Log.d("MessangiReceiver", "onReceive: " +json);
    }
}
