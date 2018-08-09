package com.crystaltowerdesigns.newsapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

@SuppressWarnings("ConstantConditions")
class MyNetworkTools {

    private final Context context;

    public MyNetworkTools(Context context) {
        this.context = context;
    }

    /**
     * @return boolean true if we are not network connected, false if we are connected
     */
    public boolean Offline() {

        ConnectivityManager connectivityManager;
        boolean connected = false;
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = (networkInfo != null) && networkInfo.isConnected();
            return !connected;
        } catch (Exception e) {
            // catch block intentionally left empty
        }
        return !connected;
    }

}
