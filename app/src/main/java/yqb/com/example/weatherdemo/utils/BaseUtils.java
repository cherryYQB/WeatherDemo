package yqb.com.example.weatherdemo.utils;

import rx.functions.Action1;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;
import com.jakewharton.rxbinding.view.RxView;

public class BaseUtils {
    private static Toast toast = null;
    private static final long UPDATA_INTERVAL = 6 * 60 * 60 * 1000;

    public static Context getContext() {
        return MyApplication.getContext();
    }

    public static int getMainThreadId(){
        return MyApplication.getMainThread();
    }
    
    public static Handler getHandler() {
		return MyApplication.getHandler();
	}
    
    public static boolean isRunOnUiThread() {
        return getMainThreadId() == android.os.Process.myTid();
    }

    public static void showToast(final String str) {
        runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (toast == null) {
		            toast = Toast.makeText(getContext(), str, Toast.LENGTH_SHORT);
		        }
		        else {
		            toast.setText(str);
		        }
		        toast.show();
			}
		});
    }

    public static void showToast(final int resId) {
        runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (toast == null) {
		            toast = Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT);
		        }
		        else {
		            toast.setText(resId);
		        }
		        toast.show();
			}
		});
    }
    
    public static void clickEvent(View v,Action1<Void> action1){
    	RxView.clicks(v)
        		.throttleFirst(500, TimeUnit.MILLISECONDS)
        		.subscribe(action1);
    }
    
    public static void runOnUiThread(Runnable runnable) {
		if (isRunOnUiThread()) {
			runnable.run();
		} else {
			getHandler().post(runnable);
		}
	}
    
    public static int getResourceByName(String name){
		return getContext().getResources().getIdentifier(name, "drawable", getContext().getPackageName());
	}
    
    public static boolean isSynchronization(long time){
    	return Math.abs(time  - System.currentTimeMillis()) < UPDATA_INTERVAL;
    }
}
