package yqb.com.example.weatherdemo.utils;

import android.util.Log;

public class LogUtils {
    public static final String TAG = "MyWeather";
    public static boolean showLog = true;


    public static void e(String msg){
        if(showLog){
            Log.e(TAG,msg);
        }
    }

    public static void d(String msg){
        if(showLog){
            Log.d(TAG,msg);
        }
    }


}
