package yqb.com.example.weatherdemo.db;

import java.util.List;
import org.litepal.crud.DataSupport;
import yqb.com.example.weatherdemo.bean.WeatherDataBean;

public class WeatherDao {
    public static final String COLUMN_AREA_ID = "areaId";
    private static final int NUMBER = 20;
    
	public static boolean isExist(String id){
        List<WeatherDataBean> list = DataSupport.where(COLUMN_AREA_ID + " = ?",id).find(WeatherDataBean.class);
        if(list.size() > 0){
            return true;
        }
        return false;
    }
	
	public static WeatherDataBean getDataByID(String id){
        List<WeatherDataBean> list = DataSupport.where(COLUMN_AREA_ID + " = ?",id).find(WeatherDataBean.class);
        if(list.size() > 0){
            return list.get(0);
        }
        return null;
    }
	
	public static List<WeatherDataBean> getCityList(int page){
		return DataSupport.limit(NUMBER)
						.offset(NUMBER*page)
						.find(WeatherDataBean.class);
	}
}
