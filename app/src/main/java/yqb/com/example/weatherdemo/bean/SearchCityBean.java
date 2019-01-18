package yqb.com.example.weatherdemo.bean;

public class SearchCityBean {
	private String name;
    private String province;
    private String ID;

    public String getName() {
        return name;
    }

    public SearchCityBean setName(String name) {
        this.name = name;
        return this;
    }

    public String getProvince() {
        return province;
    }

    public SearchCityBean setProvince(String province) {
        this.province = province;
        return this;
    }

    public String getID(){
        return ID;
    }

    public SearchCityBean setID(String ID){
        this.ID = ID;
        return this;
    }

    @Override
    public String toString() {
        return "SearchCityBean{" +
                "name='" + name + '\'' +
                ", province='" + province + '\'' +
                ", ID='" + ID + '\'' +
                '}';
    }
}
