package yqb.com.example.weatherdemo.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import yqb.com.example.weatherdemo.R;
import yqb.com.example.weatherdemo.adapter.viewholder.MyLivingDetailViewHolder;
import yqb.com.example.weatherdemo.utils.BaseUtils;
import yqb.com.example.weatherdemo.utils.MyConst;

public class MyLivingDetailGridViewAdapter extends BaseAdapter {
	private ArrayList<String> contentList;
    private String[] livingDetailTitle;
    public ArrayList<String> getContentList() {
		return contentList;
	}
	public void setContentList(ArrayList<String> contentList) {
		this.contentList = contentList;
	}
	public MyLivingDetailGridViewAdapter(ArrayList<String> contentList){
        this.contentList = contentList;
        livingDetailTitle = BaseUtils.getContext().getResources().getStringArray(R.array.living_detail_title);
    }
    @Override
    public int getCount() {
        return livingDetailTitle.length;
    }

    @Override
    public String getItem(int i) {
        return livingDetailTitle[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MyLivingDetailViewHolder viewHolder;
        if(view == null){
            view = View.inflate(BaseUtils.getContext(), R.layout.living_detail_item,null);
            viewHolder = new MyLivingDetailViewHolder();
            viewHolder.tvLivingTitle = (TextView) view.findViewById(R.id.tv_detail_title);
            viewHolder.ivLivingIcon = (ImageView) view.findViewById(R.id.iv_detail_icon);
            viewHolder.tvLivingContent = (TextView) view.findViewById(R.id.tv_detail_content);
            view.setTag(viewHolder);
        }else{
            viewHolder = (MyLivingDetailViewHolder) view.getTag();
        }
        viewHolder.tvLivingTitle.setText(getItem(i));
        viewHolder.tvLivingContent.setText(contentList.get(i));
        viewHolder.ivLivingIcon.setImageResource(MyConst.LIVING_DETAIL_ICON[i]);
        return view;
    }
}
