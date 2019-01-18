package yqb.com.example.weatherdemo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import yqb.com.example.weatherdemo.R;
import yqb.com.example.weatherdemo.adapter.viewholder.MyCityViewHolder;
import yqb.com.example.weatherdemo.bean.WeatherDataBean;
import yqb.com.example.weatherdemo.utils.BaseUtils;

public class MyCityListViewAdapter extends BaseAdapter {
	private ArrayList<WeatherDataBean> list;
	private WeatherDataBean bean;

	public MyCityListViewAdapter(ArrayList<WeatherDataBean> list){
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public WeatherDataBean getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MyCityViewHolder viewHolder = null;
		if (convertView==null) {
			convertView = LayoutInflater.from(BaseUtils.getContext()).inflate(R.layout.citylist_item, null,true);
			viewHolder = new MyCityViewHolder();
			viewHolder.tvCity = (TextView) convertView.findViewById(R.id.tv_city);
			viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.icon);
			viewHolder.tvTemp = (TextView) convertView.findViewById(R.id.tv_temp);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (MyCityViewHolder) convertView.getTag();
		}
		bean = getItem(position);
		viewHolder.tvCity.setText(bean.getName()+"\n"+bean.getProvince());
		viewHolder.ivIcon.setImageResource(bean.getIcon());
		viewHolder.tvTemp.setText(bean.getTmp());
		return convertView;
	}
	
}
