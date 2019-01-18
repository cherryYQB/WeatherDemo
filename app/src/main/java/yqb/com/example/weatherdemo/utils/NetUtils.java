package yqb.com.example.weatherdemo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {
    public static boolean isConnected() {
        return getCurrentNetworkState() == NetworkInfo.State.CONNECTED;
    }


    public static NetworkInfo.State getCurrentNetworkState() {
        NetworkInfo networkInfo
                = ((ConnectivityManager) BaseUtils.getContext().getSystemService(
                Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return networkInfo != null ? networkInfo.getState() : null;
    }
	
}
