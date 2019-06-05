package com.dunk.eats.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import com.dunk.eats.models.User;

public class Common {
    public static User currentUser;
    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PSD_KEY = "Password";

    public static String convertCodeToStatus(String status) {
        if (status != null && status.equals("0"))
            return "Placed";
        else if (status != null && status.equals("1"))
            return "On my way";
        else
            return "Shipped";
    }

    public static boolean isConnectedInternet(Context context){

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks=connectivityManager.getAllNetworks();
        if (connectivityManager != null) {
            for (Network netinfo : networks) {
                NetworkInfo ni = connectivityManager.getNetworkInfo(netinfo);
                if (ni.isConnected() && ni.isAvailable()) {
                    return true;
                }
            }
        }
        return false;
    }
}
