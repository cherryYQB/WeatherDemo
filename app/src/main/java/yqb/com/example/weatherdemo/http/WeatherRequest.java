package yqb.com.example.weatherdemo.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import yqb.com.example.weatherdemo.bean.HeWeather;

public class WeatherRequest {
	public static final String BASE_URL = "";
    private static final int CONNECT_TIMEOUT = 10;
    private static final int WRITE_TIMEOUT = 10;
    private static final int READ_TIMEOUT = 20;
    private Retrofit retrofit;
    private WeatherService service;
    
    private WeatherRequest(){
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new Interceptor(){

					@Override
					public Response intercept(Chain arg0) throws IOException {
						Response response = arg0.proceed(arg0.request());
						return response;
					}
                	
                });
        retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        service = retrofit.create(WeatherService.class);
    }

    private static class SingletonFactory{
        private static final WeatherRequest INSTANCE = new WeatherRequest();
    }

    public static WeatherRequest getInstance(){
        return SingletonFactory.INSTANCE;
    }
    
    public void getWeather(Action1<HeWeather> a1,Action1<Throwable> a2,String city){
        service.getWeather(city)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(a1,a2);
    }
}
