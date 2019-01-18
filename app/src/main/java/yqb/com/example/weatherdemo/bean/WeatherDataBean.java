package yqb.com.example.weatherdemo.bean;

import java.util.ArrayList;
import java.util.List;
import org.litepal.crud.DataSupport;

public class WeatherDataBean extends DataSupport {
	private String areaId;
	private String name;
	private long saveTime;
	private String province;
	private String serveTime;

	private String tmp;
	private String aqi;
	private int icon;
	private String weather;

	private List<Integer> maxTmp = new ArrayList<Integer>();
	private List<Integer> minTmp = new ArrayList<Integer>();
	private List<Integer> dailyIcon = new ArrayList<Integer>();
	private List<String> livingIndex = new ArrayList<String>();

	public String getServeTime() {
		return serveTime;
	}

	public void setServeTime(String serveTime) {
		this.serveTime = serveTime;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public List<String> getLivingIndex() {
		return livingIndex;
	}

	public void setLivingIndex(List<String> livingIndex) {
		this.livingIndex = livingIndex;
	}

	public String getTmp() {
		return tmp;
	}

	public void setTmp(String tmp) {
		this.tmp = tmp;
	}

	public String getAqi() {
		return aqi;
	}

	public void setAqi(String aqi) {
		this.aqi = aqi;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getWeather() {
		return weather;
	}

	public void setWeather(String weather) {
		this.weather = weather;
	}

	public List<Integer> getMaxTmp() {
		return maxTmp;
	}

	public void setMaxTmp(List<Integer> maxTmp) {
		this.maxTmp = maxTmp;
	}

	public List<Integer> getMinTmp() {
		return minTmp;
	}

	public void setMinTmp(List<Integer> minTmp) {
		this.minTmp = minTmp;
	}

	public List<Integer> getDailyIcon() {
		return dailyIcon;
	}

	public void setDailyIcon(List<Integer> dailyIcon) {
		this.dailyIcon = dailyIcon;
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSaveTime() {
		return saveTime;
	}

	public void setSaveTime(long saveTime) {
		this.saveTime = saveTime;
	}

	@Override
	public String toString() {
		return "WeatherDataBean{" +
				"areaId='" + areaId + '\'' +
				", name='" + name + '\'' +
				", saveTime=" + saveTime +
				", province='" + province + '\'' +
				", serveTime='" + serveTime + '\'' +
				", tmp='" + tmp + '\'' +
				", aqi='" + aqi + '\'' +
				", icon=" + icon +
				", weather='" + weather + '\'' +
				", maxTmp=" + maxTmp +
				", minTmp=" + minTmp +
				", dailyIcon=" + dailyIcon +
				", livingIndex=" + livingIndex +
				'}';
	}
}
