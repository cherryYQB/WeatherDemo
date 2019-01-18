package yqb.com.example.weatherdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import yqb.com.example.weatherdemo.adapter.MyLivingDetailGridViewAdapter;
import yqb.com.example.weatherdemo.bean.CityID;
import yqb.com.example.weatherdemo.bean.HeWeather;
import yqb.com.example.weatherdemo.bean.WeatherDataBean;
import yqb.com.example.weatherdemo.db.WeatherDao;
import yqb.com.example.weatherdemo.http.WeatherRequest;
import yqb.com.example.weatherdemo.utils.BaseUtils;
import yqb.com.example.weatherdemo.utils.IOUtils;
import yqb.com.example.weatherdemo.utils.LogUtils;
import yqb.com.example.weatherdemo.utils.MyConst;
import yqb.com.example.weatherdemo.utils.NetUtils;
import yqb.com.example.weatherdemo.utils.PrefUtils;

public class MainActivity extends AppCompatActivity {

    private static final String DBNAME = "weathercity.db";
    private static final int COPYBUFFER = 1024;
    private static final String NULL = "null";
    private static final long UPDATA_INTERVAL = 6 * 60 * 60 * 1000;
    private static final String TEMP = BaseUtils.getContext().getString(R.string.temp);
    private static final String AQI = "AQI ";
    private static final int DAILY_NUM = 3;
    private static final String WIND = " m/s";

    private AssetManager am;
    private GridView gv;
    private TextView tvWeatherCity;
    private TextView tvWeatherTime;
    private TextView tvWeatherTemp;
    private TextView tvWeatherDetail;
    private TextView tvWeatherAQI;

    private LinearLayout llMain;
    private RotateLoading rotateLoading;
    private ImageView ivIcon;
    private WeatherDataBean data;
    private WeatherChart wChart;
    private MyLivingDetailGridViewAdapter adapter;
    private ArrayList<View> viewList;
    private boolean isRefresh = false;

    private static final int ID_READ_EXTERNAL_STORAGE =100;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            requestPermissions( REQUIRED_PERMISSIONS, ID_READ_EXTERNAL_STORAGE );
        }
        checkNET(this);
        initDBData();
        initView();
        initData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,grantResults);
        switch(requestCode) {
            //requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case ID_READ_EXTERNAL_STORAGE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //获取到权限，做相应处理
                    //调用定位SDK应确保相关权限均被授权，否则会引起定位失败
                } else{
                    Toast.makeText(this, "权限被拒", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            default:
                break;
        }
    }

    private boolean isFirst() {
        return PrefUtils.getBoolean(MyConst.IS_FIRST, true);
    }

    private void checkNET(Context context) {
        if (NetUtils.isConnected()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.no_net_msg);
        builder.setCancelable(false);
        if (isFirst()) {
            builder.setPositiveButton(R.string.bt_ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                            int i) {
                            checkNET(MainActivity.this);
                        }
                    });
            builder.setNegativeButton(R.string.bt_cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                            int i) {
                            android.os.Process.killProcess(BaseUtils
                                    .getMainThreadId());
                        }
                    });
            builder.show();
            return;
        }
        builder.setPositiveButton(R.string.bt_ok, null);
        builder.show();
        return;
    }

    private void initDBData() {
        am = getAssets();
        final File dbFile = new File(getFilesDir(), DBNAME);
        if (dbFile.exists()) {
            LogUtils.d("file exits !");
            return;
        }
        LogUtils.d("copy db file !");
        Observable.just(DBNAME).observeOn(Schedulers.io())
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return copyDB(s, dbFile);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (!aBoolean) {
                            BaseUtils.showToast(R.string.db_copy_fail);
                            android.os.Process.killProcess(android.os.Process
                                    .myPid());
                            return;
                        }
                        BaseUtils.showToast(R.string.db_copy_success);
                    }
                });
    }

    private boolean copyDB(String s, File dbFile) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = am.open(s);
            out = new FileOutputStream(dbFile);
            byte[] buffer = new byte[COPYBUFFER];
            int len = -1;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

        } catch (IOException ioe) {
            return false;
        } finally {
            IOUtils.closeIO(in);
            IOUtils.closeIO(out);
        }
        return true;
    }

    private void initView() {
        BaseUtils.clickEvent(findViewById(R.id.tv_add), new Action1<Void>() {
            @Override
            public void call(Void arg0) {
                if(NetUtils.isConnected()){
                    startActivity(new Intent(BaseUtils.getContext(),
                            AddCityAciticty.class));
                    return;
                }
                BaseUtils.showToast(R.string.no_net);
            }
        });
        BaseUtils.clickEvent(findViewById(R.id.tv_refresh), new Action1<Void>() {

            @Override
            public void call(Void arg0) {
                if(DataSupport.count(WeatherDataBean.class) < 1){
                    BaseUtils.showToast(R.string.no_city);
                    return;
                }
                if(NetUtils.isConnected()){
                    if(!isRefresh){
                        isRefresh = true;
                        initData();
                        return ;
                    }
                    BaseUtils.showToast(R.string.is_refreshing);
                    return;
                }
                BaseUtils.showToast(R.string.no_net);
            }
        });
        BaseUtils.clickEvent(findViewById(R.id.tv_city), new Action1<Void>() {

            @Override
            public void call(Void arg0) {
                if(DataSupport.count(WeatherDataBean.class) < 1){
                    BaseUtils.showToast(R.string.no_city);
                    return;
                }
                startActivity(new Intent(BaseUtils.getContext(), CityActivity.class));
            }
        });
        gv = (GridView) findViewById(R.id.gv_living_details);
        tvWeatherCity = (TextView) findViewById(R.id.tv_weather_city);
        tvWeatherTime = (TextView) findViewById(R.id.tv_weather_time);
        tvWeatherTemp = (TextView) findViewById(R.id.tv_weather_temp);
        tvWeatherDetail = (TextView) findViewById(R.id.tv_weather_detail);
        tvWeatherAQI = (TextView) findViewById(R.id.tv_weather_aqi);
        wChart = (WeatherChart) findViewById(R.id.weather_chart);
        llMain = (LinearLayout) findViewById(R.id.ll_main);
        ivIcon = (ImageView) findViewById(R.id.iv_icon);
        rotateLoading = (RotateLoading) findViewById(R.id.rl_load);
        rotateLoading.setOnAnimationEndListeren(new RotateLoading.onAnimationEndListener() {

            @Override
            public void onAnimationEnd() {
                rotateLoading.setVisibility(View.GONE);
                llMain.setVisibility(View.VISIBLE);
            }
        });
        viewList = new ArrayList<View>();
        viewList.add(tvWeatherCity);
        viewList.add(tvWeatherTime);
        viewList.add(tvWeatherTemp);
        viewList.add(tvWeatherDetail);
        viewList.add(tvWeatherAQI);
        viewList.add(ivIcon);
        viewList.add(wChart);
        viewList.add(gv);
    }

    private void initData() {
        String currentId = PrefUtils.getString(MyConst.CURRENT_CITY, NULL);
        if (NULL.equals(currentId)) {
            isRefresh = false;
            if (NetUtils.isConnected()) {
                startActivity(new Intent(BaseUtils.getContext(),
                        AddCityAciticty.class));
                return;
            }
            BaseUtils.showToast(R.string.no_net);
            return;
        }
        loadAnimWithDisplayData(currentId);
    }

    private void loadAnimWithDisplayData(final String id) {
        showLoading(true);
        new Thread() {
            public void run() {
                displayWeatherData(id);
            };
        }.start();
    }

    private void showLoading(boolean isShow) {
        if (isShow) {
            llMain.setVisibility(View.GONE);
            rotateLoading.setVisibility(View.VISIBLE);
            rotateLoading.start();
            return;
        }
        if(rotateLoading.isStart()){
            rotateLoading.stop();
        }
    }

    private void displayWeatherData(String id) {
        data = null;
        data = WeatherDao.getDataByID(id);
        //Log.i("tag_yqb", "displayWeatherData data:"+data.toString());
        if (isNeedUpData(data.getSaveTime())) {
            BaseUtils.showToast(R.string.is_loading);
            WeatherRequest.getInstance().getWeather(new Action1<HeWeather>() {

                @Override
                public void call(HeWeather arg0) {
                    isRefresh = false;
                    stateAnalysis(arg0.getHeWeather5().get(0), data);
                }
            }, new Action1<Throwable>() {

                @Override
                public void call(Throwable arg0) {
                    isRefresh = false;
                    netError(data);
                }
            }, id);
            return;
        }
        isRefresh = false;
        readDataByDB(data);
    }

    private boolean isNeedUpData(long time) {
        long nowTime = System.currentTimeMillis();
        if (Math.abs(nowTime-time) < UPDATA_INTERVAL) {
            Date saveData = new Date(time);
            Calendar saveCalendar = Calendar.getInstance();
            saveCalendar.setTime(saveData);
            int saveDay = saveCalendar.get(Calendar.DAY_OF_MONTH);
            Calendar nowCalendar = Calendar.getInstance();
            int nowDay = nowCalendar.get(Calendar.DAY_OF_MONTH);
            if(saveDay!=nowDay){
                return true;
            }
            return false;
        }
        return true;
    }

    protected void stateAnalysis(HeWeather.HeWeather5Bean heWeather5Bean,
                                 WeatherDataBean data) {
        showLoading(false);
        String responseStatus = heWeather5Bean.getStatus();
        if(MyConst.OK.equals(responseStatus)){
            loadData(heWeather5Bean, data);
        }else if(MyConst.AGAIN_REQUEST.equals(responseStatus)){
            againRequest(data);
        }else if(MyConst.UNKNOWN_CITY.equals(responseStatus)){
            unknowCity(data);
        }

    }

    protected void netError(WeatherDataBean data) {
        showLoading(false);
        BaseUtils.showToast(R.string.net_error);
        if(data.getSaveTime()!=0){
            readDataByDB(data);
            return;
        }
        tvWeatherCity.setText(data.getName());
        tvWeatherTime.setText(R.string.request_refresh);
        for (View v : viewList) {
            if(v != tvWeatherCity || v != tvWeatherTime){
                v.setVisibility(View.GONE);
            }
        }
    }

    private void readDataByDB(final WeatherDataBean data) {
        BaseUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(data.getAreaId()==null){
                    PrefUtils.putString(MyConst.CURRENT_CITY, NULL);
                    updataWidget();
                    for (View v : viewList) {
                        v.setVisibility(View.GONE);
                    }
                    return;
                }
                PrefUtils.putString(MyConst.CURRENT_CITY, data.getAreaId());
                if(data.getSaveTime() == 0){
                    loadAnimWithDisplayData(data.getAreaId());
                    return;
                }
                showLoading(false);
                for (View v : viewList) {
                    v.setVisibility(View.VISIBLE);
                }
                tvWeatherCity.setText(data.getName());
                tvWeatherTime.setText(data.getServeTime());
                tvWeatherTemp.setText(data.getTmp());
                tvWeatherDetail.setText(data.getWeather());
                tvWeatherAQI.setText(data.getAqi());
                ivIcon.setImageResource(data.getIcon());

                initGridView((ArrayList<String>) data.getLivingIndex());
                int[] maxTemp = new int[DAILY_NUM];
                for (int i = 0; i < DAILY_NUM; i++) {
                    maxTemp[i] = data.getMaxTmp().get(i);
                }

                int[] minTemp = new int[DAILY_NUM];
                for (int i = 0; i < DAILY_NUM; i++) {
                    minTemp[i] = data.getMinTmp().get(i);
                }

                int[] dailyIcon = new int[DAILY_NUM];
                for (int i = 0; i < DAILY_NUM; i++) {
                    dailyIcon[i] = data.getDailyIcon().get(i);
                }
                wChart.setVisibility(View.VISIBLE);
                wChart.setTemperatureAndIcon(maxTemp, minTemp, dailyIcon);
                updataWidget();
            }
        });

    }

    private void loadData(HeWeather.HeWeather5Bean heWeather5Bean, WeatherDataBean data) {
        for (View v : viewList) {
            v.setVisibility(View.VISIBLE);
        }
        tvWeatherCity.setText(data.getName());

        String serveTime = heWeather5Bean.getDaily_forecast().get(0).getDate();
        tvWeatherTime.setText(getNowTime(serveTime));
        data.setServeTime(serveTime);

        data.setSaveTime(System.currentTimeMillis());

        String temp = heWeather5Bean.getNow().getTmp() + TEMP;
        tvWeatherTemp.setText(temp);
        data.setTmp(temp);

        String weather = heWeather5Bean.getNow().getCond().getTxt();
        tvWeatherDetail.setText(weather);
        data.setWeather(weather);

        if (heWeather5Bean.getAqi() != null) {
            String aqi = AQI + heWeather5Bean.getAqi().getCity().getAqi()
                    + " (" + heWeather5Bean.getAqi().getCity().getQlty() + ")";
            tvWeatherAQI.setText(aqi);
            data.setAqi(aqi);
        } else {
            tvWeatherAQI.setText(getString(R.string.null_data));
            data.setAqi(getString(R.string.null_data));
        }

        int icon = BaseUtils.getResourceByName("s"+heWeather5Bean.getNow().getCond().getCode());
        ivIcon.setImageResource(icon);
        data.setIcon(icon);

        int[] maxTempS = new int[3];
        ArrayList<Integer> maxTempA = new ArrayList<Integer>();
        for (int i = 0; i < DAILY_NUM; i++) {
            int tmp = Integer.parseInt(heWeather5Bean.getDaily_forecast()
                    .get(i).getTmp().getMax());
            maxTempS[i] = tmp;
            maxTempA.add(i, tmp);
        }
        data.setMaxTmp(maxTempA);

        int[] minTempS = new int[3];
        ArrayList<Integer> minTempA = new ArrayList<Integer>();
        for (int i = 0; i < DAILY_NUM; i++) {
            int tmp = Integer.parseInt(heWeather5Bean.getDaily_forecast()
                    .get(i).getTmp().getMin());
            minTempS[i] = tmp;
            minTempA.add(i, tmp);
        }
        data.setMinTmp(minTempA);

        int[] weatherIcon = new int[3];
        ArrayList<Integer> weatherIconA = new ArrayList<Integer>();
        for (int i = 0; i < DAILY_NUM; i++) {
            int tmp = BaseUtils.getResourceByName("s"+Integer.parseInt(heWeather5Bean.getDaily_forecast()
                    .get(i).getCond().getCode_d()));
            weatherIcon[i] = tmp;
            weatherIconA.add(i, tmp);
        }
        data.setDailyIcon(weatherIconA);

        wChart.setVisibility(View.VISIBLE);
        wChart.setTemperatureAndIcon(maxTempS, minTempS, weatherIcon);

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
        initGridView(content);
        data.setLivingIndex(content);

        data.save();
        updataWidget();
    }

    private void againRequest(WeatherDataBean data) {
        BaseUtils.showToast(R.string.again_request);
        if(data.getSaveTime()!=0){
            readDataByDB(data);
            return;
        }
        tvWeatherCity.setText(data.getName());
        tvWeatherTime.setText(R.string.request_refresh);
        for (View v : viewList) {
            if(v != tvWeatherCity || v != tvWeatherTime){
                v.setVisibility(View.GONE);
            }
        }
    }

    private void unknowCity(WeatherDataBean data) {
        BaseUtils.showToast(R.string.unknow_city);
        DataSupport.delete(WeatherDataBean.class, DataSupport.count(WeatherDataBean.class));
        if(DataSupport.count(WeatherDataBean.class)>0){
            WeatherDataBean firstData = DataSupport.findFirst(WeatherDataBean.class);
            String newID = firstData.getAreaId();
            PrefUtils.putString(MyConst.CURRENT_CITY, newID);
            loadAnimWithDisplayData(newID);
            return;
        }
        if(NetUtils.isConnected()){
            startActivity(new Intent(BaseUtils.getContext(),
                    AddCityAciticty.class));
            return;
        }
        BaseUtils.showToast(R.string.no_net);
        return;
    }

    private void updataWidget(){
        sendBroadcast(new Intent(MyConst.ACTION_UPDATA_ALL));
    }

    private void initGridView(ArrayList<String> content) {
        gv.setVisibility(View.VISIBLE);
        if (adapter == null) {
            adapter = new MyLivingDetailGridViewAdapter(content);
            gv.setAdapter(adapter);
        } else {
            adapter.setContentList(content);
            adapter.notifyDataSetChanged();
        }
    }

    private String getNowTime(String serveTime) {
        String[] time = serveTime.split("-");
        return time[0] + getString(R.string.year) + time[1]
                + getString(R.string.mouth) + time[2] + getString(R.string.day);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    public void getCityID(CityID id) {
        String cityid = id.getId();
        EventBus.getDefault().removeStickyEvent(CityID.class);
        if ("error".equals(cityid)) {
            if (DataSupport.count(WeatherDataBean.class) > 0) {
                cityid = DataSupport.findFirst(WeatherDataBean.class)
                        .getAreaId();
                PrefUtils.putString(MyConst.CURRENT_CITY, cityid);
                displayWeatherData(cityid);
                return;
            }
            BaseUtils.showToast(getString(R.string.error));
            startActivity(new Intent(BaseUtils.getContext(),
                    AddCityAciticty.class));
            return;
        }
        PrefUtils.putString(MyConst.CURRENT_CITY, cityid);
        if (isFirst()) {
            PrefUtils.putBoolean(MyConst.IS_FIRST, false);
        }
        loadAnimWithDisplayData(cityid);

    }

    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    public void getData(WeatherDataBean data){
        EventBus.getDefault().removeStickyEvent(WeatherDataBean.class);
        readDataByDB(data);
    }
}
