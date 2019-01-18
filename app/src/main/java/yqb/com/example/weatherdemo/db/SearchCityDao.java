package yqb.com.example.weatherdemo.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import yqb.com.example.weatherdemo.utils.BaseUtils;
import yqb.com.example.weatherdemo.utils.LogUtils;

public class SearchCityDao {
	public static final String DBNAME = "weathercity.db";
    public static final String TABLENAME = "CITY_LIST";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PROVINCE = "province";
    public static final String COLUMN_AREA_ID = "areaid";
    private File DBFile;
    private SQLiteDatabase db;
    
    public SearchCityDao(){
        DBFile = new File(BaseUtils.getContext().getFilesDir(), DBNAME);
        db = SQLiteDatabase.openDatabase(DBFile.getAbsolutePath(),null, SQLiteDatabase.OPEN_READONLY);
    }
    
    public Cursor searchCity(String s){
        if(TextUtils.isEmpty(s)){
            return null;
        }

        Cursor cursor = db.rawQuery("select "+COLUMN_NAME+","+COLUMN_PROVINCE+","+COLUMN_AREA_ID+" from "+TABLENAME+" where "+COLUMN_NAME+" like ?;"
                                    ,new String[]{"%"+s+"%"});
        LogUtils.d("select "+COLUMN_NAME+","+COLUMN_PROVINCE+","+COLUMN_AREA_ID+" from "+TABLENAME+" where "+COLUMN_NAME+" like "+"%"+s+"%");

        return cursor;
    }
    
    public String getIDByName(String s){
        if(TextUtils.isEmpty(s)){
            return null;
        }
        String id = null;
        Cursor cursor = db.query(TABLENAME,new String[]{COLUMN_AREA_ID},COLUMN_NAME + "=?",new String[]{s},null,null,null);
        if(cursor.moveToNext()){
            id = cursor.getString(0);
        }
        cursor.close();
        return id;
    }
    
    public void closeDB(){
        if(db!=null){
            db.close();
        }
    }
}
