package yqb.com.example.weatherdemo.service;

import rx.functions.Action1;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.util.ArrayList;
import yqb.com.example.weatherdemo.R;
import yqb.com.example.weatherdemo.bean.HeWeather;
import yqb.com.example.weatherdemo.bean.WeatherDataBean;
import yqb.com.example.weatherdemo.db.WeatherDao;
import yqb.com.example.weatherdemo.http.WeatherRequest;
import yqb.com.example.weatherdemo.utils.BaseUtils;
import yqb.com.example.weatherdemo.utils.MyConst;
import yqb.com.example.weatherdemo.utils.PrefUtils;

public class UpdataService extends Service {
	private static final String NULL = "null";
	private static final String TEMP = BaseUtils.getContext().getString(R.string.temp);
	private static final String AQI = "AQI ";
	private static final String WIND = " m/s";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String id = PrefUtils.getString(MyConst.CURRENT_CITY, NULL);
		if (!NULL.equals(id)) {
			requestAndSaveData(id);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void requestAndSaveData(final String id) {
		WeatherRequest.getInstance().getWeather(new Action1<HeWeather>() {

			@Override
			public void call(HeWeather arg0) {
				HeWeather.HeWeather5Bean arg = arg0.getHeWeather5().get(0);
				String responseStatus = arg.getStatus();
				if(MyConst.OK.equals(responseStatus)){
					saveData(arg,id);
				}else{
					sendBroadcast(new Intent(MyConst.ACTION_UPDATA_FAIL));
				}
			}
		}, new Action1<Throwable>() {

			@Override
			public void call(Throwable arg0) {
				sendBroadcast(new Intent(MyConst.ACTION_UPDATA_FAIL));
			}
		}, id);
	}

	protected void saveData(HeWeather.HeWeather5Bean heWeather5Bean, String id) {
		WeatherDataBean data = WeatherDao.getDataByID(id);
		
		data.setSaveTime(System.currentTimeMillis());
		
		String serveTime = heWeather5Bean.getDaily_forecast().get(0).getDate();
		data.setServeTime(getNowTime(serveTime)); 
		
		data.setTmp(heWeather5Bean.getNow().getTmp() + TEMP);
		
		data.setWeather(heWeather5Bean.getNow().getCond().getTxt());
		
		if (heWeather5Bean.getAqi() != null) {
			String aqi = AQI + heWeather5Bean.getAqi().getCity().getAqi()
					+ " (" + heWeather5Bean.getAqi().getCity().getQlty() + ")";
			data.setAqi(aqi);
		} else {
			data.setAqi(getString(R.string.null_data));
		}
		
		int icon = BaseUtils.getResourceByName("s"+heWeather5Bean.getNow().getCond().getCode());
		data.setIcon(icon);
		
		ArrayList<Integer> maxTempA = new ArrayList<Integer>();
		for (int i = 0; i < 3; i++) {
			int tmp = Integer.parseInt(heWeather5Bean.getDaily_forecast()
					.get(i).getTmp().getMax());
			maxTempA.add(i, tmp);
		}
		data.setMaxTmp(maxTempA);
		
		ArrayList<Integer> minTempA = new ArrayList<Integer>();
		for (int i = 0; i < 3; i++) {
			int tmp = Integer.parseInt(heWeather5Bean.getDaily_forecast()
					.get(i).getTmp().getMin());
			minTempA.add(i, tmp);
		}
		data.setMinTmp(minTempA);
		
		ArrayList<Integer> weatherIconA = new ArrayList<Integer>();
		for (int i = 0; i < 3; i++) {
			int tmp = BaseUtils.getResourceByName("s"+ Integer.parseInt(heWeather5Bean.getDaily_forecast()
					.get(i).getCond().getCode_d()));
			weatherIconA.add(i, tmp);
		}
		data.setDailyIcon(weatherIconA);
		
		String uv = heWeather5Bean.getSuggestion().getUv().getBrf();

		String sunset = heWeather5Bean.getDaily_forecast().get(0).getAstro()
				.getSs();

		String wind = heWeather5Bean.getDaily_forecast().get(0).getWind()
				.getSpd()
				+ WIND;

		String dress = heWeather5Bean.getSuggestion().getDrsg().getBrf();

		String outdoor = heWeather5Bean.getSuggestion().getSport().getBrf();

		String clean = heWeather5Bean.getSuggestion().getCw().getBrf();

		String comf = heWeather5Bean.getSuggestion().getComf().getBrf();

		String cold = heWeather5Bean.getSuggestion().getFlu().getBrf();
		
		ArrayList<String> content = new ArrayList<String>();
		content.add(uv);
		content.add(sunset);
		content.add(wind);
		content.add(dress);
		content.add(outdoor);
		content.add(clean);
		content.add(comf);
		content.add(cold);
		data.setLivingIndex(content);
		
		if(data.saveOrUpdate("areaId = ?", data.getAreaId())){
			sendBroadcast(new Intent(MyConst.ACTION_UPDATA_SUCCESS));
			return;
		}
		sendBroadcast(new Intent(MyConst.ACTION_UPDATA_FAIL));
		
	}
	
	private String getNowTime(String serveTime) {
		String[] time = serveTime.split("-");
		return time[0] + getString(R.string.year) + time[1]
				+ getString(R.string.mouth) + time[2] + getString(R.string.day);
	}
}
