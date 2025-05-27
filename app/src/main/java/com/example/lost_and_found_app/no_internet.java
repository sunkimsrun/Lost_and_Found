package com.example.lost_and_found_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class no_internet extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isConnected(context)) {
            if (!(context instanceof NoConnectionActivity)) {
                Intent noInternetIntent = new Intent(context, NoConnectionActivity.class);
                noInternetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(noInternetIntent);
            }
        }
    }

    public static boolean isConnected(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
