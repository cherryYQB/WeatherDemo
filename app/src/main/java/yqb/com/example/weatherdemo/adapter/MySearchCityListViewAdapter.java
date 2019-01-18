package yqb.com.example.weatherdemo.adapter;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import yqb.com.example.weatherdemo.R;
import yqb.com.example.weatherdemo.adapter.viewholder.MySearchCityViewHolder;
import yqb.com.example.weatherdemo.bean.SearchCityBean;
import yqb.com.example.weatherdemo.utils.BaseUtils;

public class MySearchCityListViewAdapter extends BaseAdapter {
	private ArrayList<SearchCityBean> list;
	private String key;
	private String name;
	private int length;
	private SpannableStringBuilder style;

	public ArrayList<SearchCityBean> getList() {
		return list;
	}

	public MySearchCityListViewAdapter setList(ArrayList<SearchCityBean> list) {
		this.list = list;
		return this;
	}

	public String getKey() {
		return key;
	}

	public MySearchCityListViewAdapter setKey(String key) {
		this.key = key;
		length = key.length();
		return this;
	}

	public MySearchCityListViewAdapter(ArrayList<SearchCityBean> list,
			String key) {
		this.list = list;
		this.key = key;
		length = key.length();
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public SearchCityBean getItem(int i) {
		return list.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		MySearchCityViewHolder viewHolder;
		if (view == null) {
			view = LayoutInflater.from(BaseUtils.getContext()).inflate(
					R.layout.search_city_item, null, false);
			viewHolder = new MySearchCityViewHolder();
			viewHolder.tvLine1 = (TextView) view
					.findViewById(R.id.search_city_line1);
			viewHolder.tvLine2 = (TextView) view
					.findViewById(R.id.search_city_line2);
			view.setTag(viewHolder);
		} else {
			viewHolder = (MySearchCityViewHolder) view.getTag();
		}
		name = getItem(i).getName();
		style = new SpannableStringBuilder(name);
		int start = name.indexOf(key);
		int end = start + length;
		style.setSpan(new ForegroundColorSpan(Color.parseColor("#499EBD")),
				start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

		viewHolder.tvLine1.setText(style);
		viewHolder.tvLine2.setText(getItem(i).getProvince());
		return view;
	}
}
