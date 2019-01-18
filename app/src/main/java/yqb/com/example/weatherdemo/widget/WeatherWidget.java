package yqb.com.example.weatherdemo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.System;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import yqb.com.example.weatherdemo.MainActivity;
import yqb.com.example.weatherdemo.R;
import yqb.com.example.weatherdemo.bean.WeatherDataBean;
import yqb.com.example.weatherdemo.db.WeatherDao;
import yqb.com.example.weatherdemo.service.UpdataService;
import yqb.com.example.weatherdemo.utils.BaseUtils;
import yqb.com.example.weatherdemo.utils.MyConst;
import yqb.com.example.weatherdemo.utils.NetUtils;
import yqb.com.example.weatherdemo.utils.PrefUtils;

public class WeatherWidget extends AppWidgetProvider {
	private static final String NULL = "null";
	private RemoteViews rv;
	private AppWidgetManager appWidgetManager = null;
	private static final long UPDATA_INTERVAL = 6 * 60 * 60 * 1000;
	private long tempTime ;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		register(context);
		displayAll();
		startTimeService(context);
		setClick();
	}

	private void register(Context context) {
		context.getContentResolver().registerContentObserver(
				Settings.System.getUriFor(System.TIME_12_24), true,
				timeObserver);
	}

	private void setClick() {
		getRemoteViews();
		Intent weatherIntent = new Intent(BaseUtils.getContext(),
				MainActivity.class);
		weatherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent weatherPending = PendingIntent.getActivity(
				BaseUtils.getContext(), 0, weatherIntent, 0);
		rv.setOnClickPendingIntent(R.id.ll_weather, weatherPending);

		Intent clockIntent = new Intent();
		clockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		clockIntent.setComponent(new ComponentName("com.android.deskclock",
				"com.android.deskclock.DeskClock"));
		PendingIntent clockPending = PendingIntent.getActivity(
				BaseUtils.getContext(), 0, clockIntent, 0);
		rv.setOnClickPendingIntent(R.id.ll_clock, clockPending);
		updataWidget();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String receiverAction = intent.getAction();
		if(MyConst.ACTION_UPDATA_ALL.equals(receiverAction)){
			startTimeService(context);
			displayAll();
			setClick();
		}else if(MyConst.ACTION_UPDATA_TIME.equals(receiverAction)){
			startTimeService(context);
			updataTime();
			updataData(context);
			setClick();
		}else if(MyConst.ACTION_UPDATA_SUCCESS.equals(receiverAction)){
			startTimeService(context);
			displayAll();
			setClick();
			stopUpdataService(context);
		}else if(MyConst.ACTION_UPDATA_FAIL.equals(receiverAction)){
			stopUpdataService(context);
		}else if(Intent.ACTION_TIME_CHANGED.equals(receiverAction)
				|| Intent.ACTION_TIMEZONE_CHANGED.equals(receiverAction)
				|| Intent.ACTION_DATE_CHANGED.equals(receiverAction)){
			startTimeService(context);
			updataTime();
			updataData(context);
			setClick();
		}
		
	}

	private void updataData(Context context) {
		if(NetUtils.isConnected()){
			if(isNeedUpData(tempTime)){
				startUpdataService(context);
			}
		}
	}


	private void startTimeService(Context context) {
		context.startService(new Intent(context, WidgetService.class));
	}
	
	private void startUpdataService(Context context){
		context.startService(new Intent(context, UpdataService.class));
	}
	private void stopUpdataService(Context context){
		context.stopService(new Intent(context, UpdataService.class));
	}

	private void updataTime() {
		getRemoteViews();
		setTime();
		updataWidget();
	}

	private void displayAll() {
		getRemoteViews();
		setTime();
		getAndSetData();
		updataWidget();
	}

	private void setTime() {
		String[] time = formatTime();
		rv.setTextViewText(R.id.tv_hour, time[1]);
		rv.setTextViewText(R.id.tv_day, time[0]);
		rv.setTextViewText(R.id.tv_week, time[2]);
	}

	private String[] formatTime() {
		String is24 = System.getString(BaseUtils.getContext()
				.getContentResolver(), System.TIME_12_24);
		String timeFormat = "24".equals(is24) ? "HH:mm-EEEE" : "hh:mm-EEEE";
		SimpleDateFormat sf = new SimpleDateFormat("yyyy"
				+ getString(R.string.year) + "MM" + getString(R.string.mouth)
				+ "dd" + getString(R.string.day) + "-" + timeFormat);
		return sf.format(new Date()).split("-");
	}

	private String getString(int resID) {
		return BaseUtils.getContext().getString(resID);
	}

	private void getRemoteViews() {
		if (rv == null) {
			rv = new RemoteViews(BaseUtils.getContext().getPackageName(),
					R.layout.widget_layout);
		}
	}

	private void updataWidget() {
		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(BaseUtils
					.getContext());
		}
		int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(new ComponentName(BaseUtils.getContext(),
						WeatherWidget.class));
		getRemoteViews();
		appWidgetManager.updateAppWidget(appWidgetIds, rv);
	}

	private void getAndSetData() {
		String cityID = PrefUtils.getString(MyConst.CURRENT_CITY, NULL);
		setWeatherData(WeatherDao.getDataByID(cityID));
	}

	protected void setWeatherData(WeatherDataBean arg0) {
		if (arg0 == null) {
			rv.setImageViewResource(R.id.iv_icon, R.drawable.s100);
			rv.setTextViewText(R.id.tv_temp, getString(R.string.beijing));
			rv.setTextViewText(R.id.tv_city, "");
			return;
		}
		tempTime = arg0.getSaveTime();
		if(tempTime==0){
			rv.setImageViewResource(R.id.iv_icon, R.drawable.s999);
			rv.setTextViewText(R.id.tv_temp, getString(R.string.request_refresh));
			rv.setTextViewText(R.id.tv_city, "");
			return;
		}
		rv.setImageViewResource(R.id.iv_icon, arg0.getIcon());
		rv.setTextViewText(R.id.tv_temp, arg0.getTmp());
		rv.setTextViewText(R.id.tv_city, arg0.getName());

	}

	private ContentObserver timeObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			updataTime();
		}
	};

	private boolean isNeedUpData(long time) {
		long nowTime = java.lang.System.currentTimeMillis();
		if (Math.abs(nowTime-time)<UPDATA_INTERVAL) {
			Date saveData = new Date(time);
			Calendar saveCalendar = Calendar.getInstance();
			saveCalendar.setTime(saveData);
			int saveDay = saveCalendar.get(Calendar.DAY_OF_MONTH);
			Calendar nowCalendar = Calendar.getInstance();
			int nowDay = nowCalendar.get(Calendar.DAY_OF_MONTH);
			if (saveDay != nowDay) {
				return true;
			}
			return false;
		}
		return true;
	}
}
