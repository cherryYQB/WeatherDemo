package yqb.com.example.weatherdemo.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import java.util.Calendar;
import yqb.com.example.weatherdemo.utils.MyConst;

public class WidgetService extends Service {
	private static final int SECOND = 1000;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
		int current  = calendar.get(Calendar.SECOND);
		long trigger = SystemClock.elapsedRealtime() + SECOND * (60-current+1);
		Intent i = new Intent(MyConst.ACTION_UPDATA_TIME);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		am.setExact(AlarmManager.ELAPSED_REALTIME, trigger, pi);
		return Service.START_STICKY;
	}
}
