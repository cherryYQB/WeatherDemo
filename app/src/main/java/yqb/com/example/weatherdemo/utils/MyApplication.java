package yqb.com.example.weatherdemo.utils;

import android.content.Context;
import android.os.Handler;
import org.litepal.LitePalApplication;

public class MyApplication extends LitePalApplication {
	private static Context mContext;
    private static int mainThread;
    private static Handler handler;
    
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
		mainThread = android.os.Process.myTid();
		handler = new Handler();
		//Stetho.initializeWithDefaults(mContext);
	}
	public static Context getContext(){
        return mContext;
    }
    public static int getMainThread(){
        return mainThread;
    }
    public static Handler getHandler() {
		return handler;
	}
}
