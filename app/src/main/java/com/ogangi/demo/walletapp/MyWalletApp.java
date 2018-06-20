package com.ogangi.demo.walletapp;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;

import com.google.firebase.iid.FirebaseInstanceId;
import com.ogangi.messangi.android.sdk.MessangiInstanceId;
import com.ogangi.messangi.android.sdk.notifications.MessangiNotificationManager;
import com.ogangi.oneworldwallets.wallet.WalletApp;
import com.ogangi.oneworldwallets.wallet.sdk.WalletManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Need a Application Implementation
public class MyWalletApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialize WalletApp
        WalletApp.initialize(getApplicationContext());

        // Set Unique ID per device
        WalletApp.getInstance().setDeviceId(FirebaseInstanceId.getInstance().getId());

        // Process Incoming Notifications
        MessangiNotificationManager.getInstances().addOnNotificationReceiveListener(messangiNotification -> {
            String passUrl = null;
            try {
                JSONObject meta = new JSONObject(messangiNotification.getMetadata());
                passUrl = meta.optString("pass", null);
            } catch (JSONException ignore) { }

            if(passUrl == null || passUrl.length() == 0){
                String text = String.valueOf(Html.fromHtml(messangiNotification.getText()));
                List<String> urls = extractUrls(text);
                passUrl = urls.size() > 0 ? urls.get(0) : null;
            }

            //ADD PASS
            Log.d("ADD_PASS", passUrl);
            WalletManager.getInstance()
                .addPass(Uri.parse(passUrl))
                .addOnSuccessListener(passWallet -> {
                    Intent intent = new Intent("wallet.demo.update");
                    intent.putExtra("id", passWallet.getId());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                });
        });

    }

    public static List<String> extractUrls(String input){
        List<String> result = new ArrayList<>();
        String[] words = input.split("\\s+");
        Pattern pattern = Patterns.WEB_URL;
        for(String word : words)
        {
            if(pattern.matcher(word).find())
            {
                if(!word.toLowerCase().contains("http://") && !word.toLowerCase().contains("https://")){
                    word = "http://" + word;
                }
                result.add(word);
            }
        }
        return result;
    }

}
