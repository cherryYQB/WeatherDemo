package yqb.com.example.weatherdemo;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.greenrobot.eventbus.EventBus;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import android.app.Activity;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import yqb.com.example.weatherdemo.adapter.MySearchCityListViewAdapter;
import yqb.com.example.weatherdemo.bean.CityID;
import yqb.com.example.weatherdemo.bean.SearchCityBean;
import yqb.com.example.weatherdemo.bean.WeatherDataBean;
import yqb.com.example.weatherdemo.db.SearchCityDao;
import yqb.com.example.weatherdemo.db.WeatherDao;
import yqb.com.example.weatherdemo.utils.BaseUtils;
import yqb.com.example.weatherdemo.utils.LogUtils;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.jakewharton.rxbinding.widget.RxTextView;

public class AddCityAciticty extends AppCompatActivity {

    private ListView lvSearch;
    private TextView tvMsg;
    private EditText etSearch;
    private String key;
    private ArrayList<SearchCityBean> searchCityList = null;
    private SearchCityBean searchCity = null;
    private MySearchCityListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city_aciticty);

        initView();
        citySearch();
    }

    private void citySearch() {
        if(searchCityList==null){
            searchCityList = new ArrayList<SearchCityBean>();
        }
        RxTextView.textChanges(etSearch)
                .observeOn(AndroidSchedulers.mainThread())
        		.map(new Func1<CharSequence, String>() {

                    @Override
                    public String call(CharSequence charSequence) {
                        isDisplayHint(TextUtils.isEmpty(charSequence.toString()));
                        return charSequence.toString();
                    }
                })
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .map(new Func1<String, ArrayList<SearchCityBean>>() {
                    @Override
                    public ArrayList<SearchCityBean> call(String s) {
                        if(TextUtils.isEmpty(s)){
                            return null;
                        }
                        searchCity(s);
                        key = s;
                        return searchCityList;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<SearchCityBean>>() {
                    @Override
                    public void call(ArrayList<SearchCityBean> arrayList) {
                        if(arrayList == null){
                            return ;
                        }
                        if(arrayList.size()==0 ){
                            tvMsg.setVisibility(View.VISIBLE);
                            tvMsg.setText(getString(R.string.add_msg_null));
                            lvSearch.setVisibility(View.GONE);
                            return ;
                        }
                        tvMsg.setVisibility(View.GONE);
                        lvSearch.setVisibility(View.VISIBLE);

                        initOrRefreshListView(arrayList,key);
                    }
                });
	}

    protected void initOrRefreshListView(ArrayList<SearchCityBean> list, String key) {
        if(adapter == null){
            adapter = new MySearchCityListViewAdapter(list,key);
            lvSearch.setAdapter(adapter);
            return;
        }
        adapter.setKey(key)
                .setList(searchCityList);
        adapter.notifyDataSetChanged();

    }

    protected void searchCity(String s) {
        searchCityList.clear();
        key = null;
        SearchCityDao searchCityDao = new SearchCityDao();
        Cursor cursor = searchCityDao.searchCity(s);
        LogUtils.d(cursor.getCount()+"");
        if(cursor.getCount()!=0){
            while (cursor.moveToNext()){
                searchCity = new SearchCityBean();
                searchCity.setName(cursor.getString(0))
                        .setProvince(cursor.getString(1))
                        .setID(cursor.getString(2));
                searchCityList.add(searchCity);
            }
        }
        if(cursor!=null){
            cursor.close();
            searchCityDao.closeDB();
        }
    }

    protected void isDisplayHint(boolean empty) {
        if(empty){
            tvMsg.setText(getString(R.string.add_msg));
            tvMsg.setVisibility(View.VISIBLE);
            lvSearch.setVisibility(View.GONE);
            return ;
        }
        tvMsg.setVisibility(View.GONE);
    }

	private void initView() {
		BaseUtils.clickEvent(findViewById(R.id.iv_back), new Action1<Void>() {
			@Override
			public void call(Void arg0) {
				finish();
			}
		});
		lvSearch = (ListView) findViewById(R.id.lv_search_city);
		tvMsg = (TextView) findViewById(R.id.add_msg);
		etSearch = (EditText) findViewById(R.id.et_search);
		RxAdapterView.itemClicks(lvSearch)
					.throttleFirst(500, TimeUnit.MILLISECONDS)
					.subscribe(new Action1<Integer>() {
						@Override
						public void call(Integer arg0) {
							saveAndDisplay(arg0);
							finish();
						}
					});
	}

    protected void saveAndDisplay(Integer arg0) {
        String id =  searchCityList.get(arg0).getID();
        String name = searchCityList.get(arg0).getName();
        String province = searchCityList.get(arg0).getProvince();
        Log.i("tag_yqb", "saveAndDisplay id:"+id+", name:"+name+", province:"+province);
        CityID cityID = new CityID();
        if(TextUtils.isEmpty(id) || TextUtils.isEmpty(name) || TextUtils.isEmpty(province)){
            cityID.setId("error");
            EventBus.getDefault().postSticky(cityID);
            BaseUtils.showToast(getString(R.string.add_fail));
            return;
        }
        cityID.setId(id);
        if(WeatherDao.isExist(id)){
            EventBus.getDefault().postSticky(cityID);
            return;
        }
        WeatherDataBean data = new WeatherDataBean();
        data.setAreaId(id);
        data.setName(name);
        data.setProvince(province);
        data.setSaveTime(0);
        if(data.save()){
            EventBus.getDefault().postSticky(cityID);
            BaseUtils.showToast(getString(R.string.add_success));
            return ;
        }
        BaseUtils.showToast(getString(R.string.add_fail));
    }
}
