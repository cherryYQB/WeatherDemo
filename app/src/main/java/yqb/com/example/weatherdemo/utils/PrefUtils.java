package yqb.com.example.weatherdemo.utils;

import android.content.Context;

public class PrefUtils {
    public static String PREFERENCE_NAME = "MyWeatherData";

    public static boolean putBoolean(String key, boolean value){
        return BaseUtils.getContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(key,value).commit();
    }

    public static boolean getBoolean(String key, boolean defValue){
        return BaseUtils.getContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getBoolean(key,defValue);
    }
    
    public static boolean putString(String key, String value){
    	return BaseUtils.getContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    			.edit().putString(key, value).commit();
    }

    public static String getString(String key, String defValue){
        return BaseUtils.getContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getString(key,defValue);
    }
}
