package yqb.com.example.weatherdemo.http;

import retrofit2.http.GET;
import retrofit2.http.Query;
import yqb.com.example.weatherdemo.bean.HeWeather;
import rx.Observable;

public interface WeatherService {
	@GET("weather/")
    Observable<HeWeather> getWeather(@Query("city") String cityid);
}
