package yqb.com.example.weatherdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.jakewharton.rxbinding.widget.RxAdapterView;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import yqb.com.example.weatherdemo.adapter.MyCityListViewAdapter;
import yqb.com.example.weatherdemo.bean.CityID;
import yqb.com.example.weatherdemo.bean.WeatherDataBean;
import yqb.com.example.weatherdemo.db.WeatherDao;
import yqb.com.example.weatherdemo.utils.BaseUtils;
import yqb.com.example.weatherdemo.utils.MyConst;
import yqb.com.example.weatherdemo.utils.NetUtils;
import yqb.com.example.weatherdemo.utils.PrefUtils;

public class CityActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private ListView lv;
    private int page = 0;
    private RotateLoading rl;
    private MyCityListViewAdapter adapter;
    private ArrayList<WeatherDataBean> dataList;
    private boolean isFinish = true;
    private static final String NULL = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        initView();
        loadListView();
    }

    private void loadListView() {
        Observable
                .just(page)
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        rl.setVisibility(View.VISIBLE);
                        rl.start();
                    }
                })
                .observeOn(Schedulers.io())
                .map(new Func1<Integer, ArrayList<WeatherDataBean>>() {
                    @Override
                    public ArrayList<WeatherDataBean> call(Integer arg0) {
                        ArrayList<WeatherDataBean> list = (ArrayList<WeatherDataBean>) WeatherDao
                                .getCityList(arg0);
                        page = arg0 + 1;
                        return list;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<WeatherDataBean>>() {
                    @Override
                    public void call(ArrayList<WeatherDataBean> arg0) {
                        rl.stop();
                        updataListView(arg0);
                    }
                });
    }

    private void updataListView(ArrayList<WeatherDataBean> list) {
        if (dataList == null) {
            dataList = list;
        } else {
            dataList.addAll(list);
        }

        if (adapter == null) {
            adapter = new MyCityListViewAdapter(dataList);
            lv.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        isFinish = true;
    }

    private void initView() {
        BaseUtils.clickEvent(findViewById(R.id.tv_add), new Action1<Void>() {

            @Override
            public void call(Void arg0) {
                if (NetUtils.isConnected()) {
                    startActivity(new Intent(BaseUtils.getContext(),
                            AddCityAciticty.class));
                    finish();
                    return;
                }
                BaseUtils.showToast(R.string.no_net);
            }
        });
        BaseUtils.clickEvent(findViewById(R.id.iv_back), new Action1<Void>() {

            @Override
            public void call(Void arg0) {
                finish();
            }
        });
        lv = (ListView) findViewById(R.id.lv_city);
        rl = (RotateLoading) findViewById(R.id.rl_load);
        rl.setLoadingColor(Color.parseColor("#54B6FC"));
        rl.setOnAnimationEndListeren(new RotateLoading.onAnimationEndListener() {

            @Override
            public void onAnimationEnd() {
                rl.setVisibility(View.GONE);
                lv.setVisibility(View.VISIBLE);
            }
        });
        RxAdapterView.itemClicks(lv).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer arg0) {
                        CityID cityID = new CityID();
                        cityID.setId(adapter.getItem(arg0).getAreaId());
                        EventBus.getDefault().postSticky(cityID);
                        finish();
                    }
                });
        RxAdapterView.itemLongClicks(lv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Integer>() {

                    @Override
                    public void call(Integer arg0) {
                        showAlertDialog(arg0.intValue());
                    }
                });
        lv.setOnScrollListener(this);
    }

    protected void showAlertDialog(final int intValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_msg);
        builder.setPositiveButton(R.string.bt_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String id = adapter.getItem(intValue).getAreaId();
                DataSupport.deleteAll(WeatherDataBean.class, WeatherDao.COLUMN_AREA_ID+" = ?",id);
                dataList.remove(intValue);
                if(id.equals(PrefUtils.getString(MyConst.CURRENT_CITY, NULL))){
                    WeatherDataBean tmp = DataSupport.findFirst(WeatherDataBean.class);
                    if(tmp==null){
                        tmp = new WeatherDataBean();
                    }
                    Log.i("tag_yqb", "showAlertDialog tmp.getAreaId():"+tmp.getAreaId());
                    PrefUtils.putString(MyConst.CURRENT_CITY, tmp.getAreaId() == null ? NULL : tmp.getAreaId());
                    sendBroadcast(new Intent(MyConst.ACTION_UPDATA_ALL));
                    EventBus.getDefault().postSticky(tmp);
                }
                adapter.notifyDataSetChanged();

            }
        });
        builder.setNegativeButton(R.string.bt_cancel, null);
        builder.show();

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        int lastItemid = lv.getLastVisiblePosition() + 1;
        if (totalItemCount == 0) {
            return;
        }
        if (lastItemid == totalItemCount) {
            if (lastItemid != DataSupport.count(WeatherDataBean.class) && isFinish) {
                isFinish = false;
                loadListView();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
