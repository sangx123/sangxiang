package winway.mdr.chinaunicom.services;

import java.util.ArrayList;
import java.util.HashMap;

import winway.mdr.telecomofchina.activity.R;
import winway.mdr.chinaunicom.comm.CustomMyAdapter;
import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.EditCustomMyAdapter;
import winway.mdr.chinaunicom.databases.tools.ConstantsSources;
import winway.mdr.chinaunicom.databases.tools.DataBaseHelper;
import winway.mdr.chinaunicom.entity.TimeSettingEntity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.BaseAdapter;
/***********************************************
 * 时间设置的服务类
 * @author zhaohao
 * 时间:2011-11
 */
public class TimeSettingService {
	DataBaseHelper readdatabasehelper;
	DataBaseHelper writedatabasehelper;
	DataBaseHelper baseHelper;
	Context context;

	public final static int WRITE = 1;
	public final static int READ = 2;

    public TimeSettingService(Context _context) {
	      context=_context;
	      readdatabasehelper=new DataBaseHelper(_context,ConstantsSources.MDRDATABASENAME, null, 1, ConstantsSources.READ_DATABASE);
	      writedatabasehelper=new DataBaseHelper(_context,ConstantsSources.MDRDATABASENAME, null, 1, ConstantsSources.WRITE_DATABASE);
	}
    
    public boolean InsertNewSettingData(TimeSettingEntity entity,int readorwrite){
    	boolean Status = false;
		baseHelper = readorwrite == WRITE ? writedatabasehelper : readdatabasehelper;
    	ContentValues contentValues=new ContentValues();
		contentValues.put(ConstantsSources.TIMESEETING_ID, entity.getId());
    	contentValues.put(ConstantsSources.TIMESEETING_REPEAT, entity.getDetailrepeat());
    	contentValues.put(ConstantsSources.TIMESEETING_BEGINTIME, entity.getBegintime());
    	contentValues.put(ConstantsSources.TIMESEETING_ENDTIME, entity.getEndtime());
    	contentValues.put(ConstantsSources.TIMESEETING_STATUS, entity.getTimesetting_status());
    	contentValues.put(ConstantsSources.TIMESEETING_SCENE, entity.getTimesetting_scene());
    	contentValues.put(ConstantsSources.TIMESEETING_ISOPENORNO, 0);
    	contentValues.put(ConstantsSources.TIMESETTING_ITEM_ICON_ID, entity.getTimesetting_icon_id());
    	long result=baseHelper.UpdataData(ConstantsSources.TABLE_TIMESETTINGNAME, contentValues, null, ConstantsSources.OP_ADD);
    	Status=result>=0?true:false;
    	return Status;
    }

    public ArrayList<TimeSettingEntity> SearchAllData(int readorwrite){
    	ArrayList<TimeSettingEntity> arrayList=new ArrayList<TimeSettingEntity>();
		baseHelper = readorwrite == WRITE ? writedatabasehelper : readdatabasehelper;
    	Cursor cursor= baseHelper.Query(ConstantsSources.TABLE_TIMESETTINGNAME, null, null);
    	while (cursor.moveToNext()) {
			  TimeSettingEntity entity=new TimeSettingEntity();
			  entity.setId(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TIMESEETING_ID)));
			  entity.setDetailrepeat(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_REPEAT)));
			  entity.setBegintime(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_BEGINTIME)));
			  entity.setEndtime(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_ENDTIME)));
			  entity.setTimesetting_status(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_STATUS)));
			  entity.setTimesetting_scene(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_SCENE)));
			  entity.setTimesetting_isopen(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TIMESEETING_ISOPENORNO)));
			  entity.setTimesetting_icon_id(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TIMESETTING_ITEM_ICON_ID)));
			  arrayList.add(entity);
		}
    	return arrayList;
    }
	public CustomMyAdapter InitAdapter(){
		int[] temp;
		CustomMyAdapter adapter = null;
		ArrayList<TimeSettingEntity> arrayList = SearchAllData(READ);
		String[] items={"imgage","time","repeat","id","openyesorno","scene_policy"};
		int[] ids={R.id.ivtimesettinglistitemicon,R.id.tvtimedetail_timesetting,R.id.tvrepeat_timesetting,R.id.tbtnonoff,R.id.tvlistitem_id,R.id.tvitemopenyesorno,R.id.tvrepeat_timesettig_scene_policy};
		ArrayList<HashMap<String, Object>> lists=new ArrayList<HashMap<String,Object>>();
		for (int i = 0; i <arrayList.size(); i++) {
			TimeSettingEntity entity=arrayList.get(i);
			HashMap<String, Object> map=new HashMap<String, Object>();
			String timeresult="";
			if(GetTimeTrue(entity.getBegintime(), entity.getEndtime())){
				timeresult=entity.getBegintime()+"-"+entity.getEndtime();
			}else{
				timeresult=entity.getBegintime()+"-"+"次日"+entity.getEndtime();
			}
			temp="非急勿扰".equals(entity.getTimesetting_status())?DataResours.fjwricons:DataResours.qhdricons;
			map.put("imgage", temp[entity.getTimesetting_icon_id()]);
			map.put("time", timeresult);
			map.put("repeat", entity.getDetailrepeat());
			map.put("id", String.valueOf(entity.getId()));
			map.put("openyesorno", String.valueOf(entity.getTimesetting_isopen()));
			map.put("scene_policy", "("+entity.getTimesetting_status()+"-"+entity.getTimesetting_scene()+")");
			lists.add(map);
		}
		adapter=new CustomMyAdapter(context, lists, R.layout.timesetting_listview_item,items ,ids);
		return adapter;
	}
    public boolean GetTimeTrue(String begintime,String endtime){
   	 String begin_hour=begintime.substring(0, begintime.indexOf(":"));
   	 String begin_minute=begintime.substring(begintime.indexOf(":")+1, begintime.length());
   	 String end_hour=endtime.substring(0, endtime.indexOf(":"));
   	 String end_minute=endtime.substring(endtime.indexOf(":")+1, endtime.length());
   	 if(Integer.parseInt(end_hour)==Integer.parseInt(begin_hour)&&Integer.parseInt(end_minute)>Integer.parseInt(begin_minute)){
   		 return true;
   	 }else if(Integer.parseInt(end_hour)>Integer.parseInt(begin_hour)){
   		 return true;
   	 }else{
   		 return false;
   	 }
    }
	public EditCustomMyAdapter InitAdapter_edit(){
		int[] temp;
		EditCustomMyAdapter adapter = null;
		ArrayList<TimeSettingEntity> arrayList = SearchAllData(READ);
		String[] items={"imgage","time","repeat","id","openyesorno","scene_policy"};
		int[] ids={R.id.ivtimesettinglistitemicon_edit,R.id.tvtimedetail_timesetting_edit,R.id.tvrepeat_timesetting_edit,R.id.btndelete_edit,R.id.tvlistitem_id_edit,R.id.tvitemopenyesorno_edit,R.id.tvrepeat_timesettig_scene_policy_edit};
		ArrayList<HashMap<String, Object>> lists=new ArrayList<HashMap<String,Object>>();
		for (int i = 0; i <arrayList.size(); i++) {
			TimeSettingEntity entity=arrayList.get(i);
			HashMap<String, Object> map=new HashMap<String, Object>();
			String timeresult=entity.getBegintime()+"-"+entity.getEndtime();
			temp="非急勿扰".equals(entity.getTimesetting_status())?DataResours.fjwricons:DataResours.qhdricons;
			map.put("imgage", temp[entity.getTimesetting_icon_id()]);
			map.put("time", timeresult);
			map.put("repeat", entity.getDetailrepeat());
			map.put("id", String.valueOf(entity.getId()));
			map.put("openyesorno", String.valueOf(entity.getTimesetting_isopen()));
			map.put("scene_policy", "("+entity.getTimesetting_status()+"-"+entity.getTimesetting_scene()+")");
			lists.add(map);
		}
		adapter=new EditCustomMyAdapter(context, lists, R.layout.timesetting_listview_item,items ,ids);
		return adapter;
	}
	public BaseAdapter getadapter(int temp){
		if(temp==1){
			return InitAdapter();
		}else if(temp==2){
			return InitAdapter_edit();
		}
		return null;
	}

	public int DeleteById(int setting_id,int readorwrite){
		try {
			baseHelper = readorwrite == WRITE ? writedatabasehelper : readdatabasehelper;
			baseHelper.DeleteByPlanId(setting_id);
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int clearData(int readorwrite) {
		try {
			baseHelper = readorwrite == WRITE ? writedatabasehelper : readdatabasehelper;
			baseHelper.DeleteAllTimeData();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public TimeSettingEntity GetMySearch(int searchid,int readorwrite){
		baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
		TimeSettingEntity settingEntity=new TimeSettingEntity();
		Cursor cursor=baseHelper.GetMyNeedEntity(searchid);
		if(cursor.moveToNext()){
			settingEntity.setId(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TIMESEETING_ID)));
			settingEntity.setDetailrepeat(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_REPEAT)));
			settingEntity.setBegintime(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_BEGINTIME)));
			settingEntity.setEndtime(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_ENDTIME)));
			settingEntity.setTimesetting_status(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_STATUS)));
			settingEntity.setTimesetting_scene(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_SCENE)));
			settingEntity.setTimesetting_isopen(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TIMESEETING_ISOPENORNO)));
			settingEntity.setTimesetting_icon_id(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TIMESETTING_ITEM_ICON_ID)));
		}
		return settingEntity;
	}
	public int UpdateOldData(TimeSettingEntity settingEntity,int readorwrite){
		try {
			baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
			baseHelper.UpdataTimeSetting(settingEntity);
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	public int UpdateStatus(int id,int status,int readorwrite){
		try {
			baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
			baseHelper.UpdateStatusOpenOrNo(id, status);
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
