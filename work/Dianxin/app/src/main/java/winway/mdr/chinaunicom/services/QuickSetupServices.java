package winway.mdr.chinaunicom.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import winway.mdr.telecomofchina.activity.R;
import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.EditQuickMyAdapter;
import winway.mdr.chinaunicom.comm.MyAdapter;
import winway.mdr.chinaunicom.databases.tools.ConstantsSources;
import winway.mdr.chinaunicom.databases.tools.DataBaseHelper;
import winway.mdr.chinaunicom.entity.TimeSettingEntity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
/***********************************************
 * 快捷设置的服务类
 * @author zhaohao
 * time:2011-11
 */
public class QuickSetupServices {
	DataBaseHelper readdatabasehelper;
	DataBaseHelper writedatabasehelper;
	DataBaseHelper baseHelper;
	Context context;
    public QuickSetupServices(Context _context) {
	      context=_context;
	      readdatabasehelper=new DataBaseHelper(_context,ConstantsSources.MDRDATABASENAME, null, 1, ConstantsSources.READ_DATABASE);
	      writedatabasehelper=new DataBaseHelper(_context,ConstantsSources.MDRDATABASENAME, null, 1, ConstantsSources.WRITE_DATABASE);
	}
    /**********************************
     * 插入数据
     * @param entity  插入的实体
     * @param readorwrite 读或写
     * @return
     */
    public boolean InsertNewSettingData(TimeSettingEntity entity,int readorwrite){
    	boolean Status = false;
    	baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
    	ContentValues contentValues=new ContentValues();
    	contentValues.put(ConstantsSources.TIMESEETING_REPEAT, entity.getDetailrepeat());
    	contentValues.put(ConstantsSources.TIMESEETING_BEGINTIME, entity.getBegintime());
    	contentValues.put(ConstantsSources.TIMESEETING_ENDTIME, entity.getEndtime());
    	contentValues.put(ConstantsSources.TIMESEETING_STATUS, entity.getTimesetting_status());
    	contentValues.put(ConstantsSources.TIMESEETING_SCENE, entity.getTimesetting_scene());
    	contentValues.put(ConstantsSources.TIMESEETING_ISOPENORNO, 0);
    	contentValues.put(ConstantsSources.TIMESETTING_ITEM_ICON_ID, entity.getTimesetting_icon_id());
    	contentValues.put(ConstantsSources.BIEMING, entity.getBieming());
    	long result=baseHelper.UpdataData(ConstantsSources.TABLE_QUICKSETUP_TABLENAME, contentValues, null, ConstantsSources.OP_ADD);
    	Status=result>=0?true:false;
    	return Status;
    }
    public void InsertArrayListMydata(ArrayList<TimeSettingEntity> entities,int readorwrite){
    	for (int i = 0; i < entities.size(); i++) {
			TimeSettingEntity entity=entities.get(i);
			 InsertNewSettingData(entity, readorwrite);
		}
    }
    public ArrayList<TimeSettingEntity> SearchAllData(int readorwrite){
    	ArrayList<TimeSettingEntity> arrayList=new ArrayList<TimeSettingEntity>();
    	baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
    	Cursor cursor= baseHelper.Query(ConstantsSources.TABLE_QUICKSETUP_TABLENAME, null, null);
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
			  entity.setBieming(cursor.getString(cursor.getColumnIndex(ConstantsSources.BIEMING)));
			  arrayList.add(entity);
		}
    	return arrayList;
    }
    public SimpleAdapter GetSimepleAdapter(){
		int[] temp;
		SimpleAdapter adapter=null;
		ArrayList<TimeSettingEntity> lists_entitys=SearchAllData(2); 
		ArrayList<HashMap<String, Object>> lists=new ArrayList<HashMap<String,Object>>();
		for (int i = 0; i < lists_entitys.size(); i++) {
			TimeSettingEntity entity=lists_entitys.get(i);
			temp="非急勿扰".equals(entity.getTimesetting_status())?DataResours.fjwricons:DataResours.qhdricons;
			String timeresult="持续:"+entity.getBegintime()+"小时"+entity.getEndtime()+"分钟";
			HashMap<String, Object> map=new HashMap<String, Object>();
			map.put("id", entity.getId());
			map.put("img", temp[entity.getTimesetting_icon_id()]);
			map.put("time", timeresult);
			map.put("repeat", entity.getTimesetting_status()+"-"+entity.getTimesetting_scene());
			lists.add(map);
		}
		adapter=new SimpleAdapter(context, lists,R.layout.quicksetup_listview_item, new String[]{"id","img","time","repeat"}, new int[]{R.id.tvquicksetup_id,R.id.ivquicksetuplistitemicon,R.id.tvquicksetuptimedetail,R.id.tvquicksetup_repeat});
		return adapter;
	}
	public TimeSettingEntity GetMySearch(int searchid,int readorwrite){
		baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
		TimeSettingEntity settingEntity=new TimeSettingEntity();
		Cursor cursor=baseHelper.GetQuicksetupEntity(searchid);
		if(cursor.moveToNext()){
			settingEntity.setId(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TIMESEETING_ID)));
			settingEntity.setDetailrepeat(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_REPEAT)));
			settingEntity.setBegintime(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_BEGINTIME)));
			settingEntity.setEndtime(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_ENDTIME)));
			settingEntity.setTimesetting_status(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_STATUS)));
			settingEntity.setTimesetting_scene(cursor.getString(cursor.getColumnIndex(ConstantsSources.TIMESEETING_SCENE)));
			settingEntity.setTimesetting_isopen(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TIMESEETING_ISOPENORNO)));
			settingEntity.setTimesetting_icon_id(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TIMESETTING_ITEM_ICON_ID)));
			settingEntity.setBieming(cursor.getString(cursor.getColumnIndex(ConstantsSources.BIEMING)));
		}
		return settingEntity;
	}
	public int UpdateOldData(TimeSettingEntity settingEntity,int readorwrite){
		try {
			baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
			baseHelper.UpdataQuicksetup(settingEntity);
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	/**********************************************
	 * 通过快捷设置表中的ID进行删除相关的数据信息
	 * @param del_id 所要删除的ID
	 * @param readorwrite 读写数据库
	 * @return 删除成功返回true 否则返回false
	 */
	public boolean DeleteQuickSetupById(int del_id,int readorwrite){
		baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
		int result=baseHelper.DeleteQuickSetupByID(del_id);
		return result==1?true:false;
	}
	 public MyAdapter InitMyAdapter_quick()
	  {
		  MyAdapter adapter=null;
		  String[] from={"image","number"};
		  int[] to={R.id.ivquick_item_pic,R.id.tvquick_item_timenumber};
		  ArrayList<Map<String, Object>> lists=new ArrayList<Map<String,Object>>();
		  for (int i = 0; i < 4; i++) {
			 Map<String, Object> map=new HashMap<String, Object>();
			 map.put("image", DataResours.temp[i]);
			 map.put("number", i+1+"");
			 lists.add(map);
		} 
		  adapter=new MyAdapter(context,lists,R.layout.quicksetup_item, from, to);
		  return adapter;
	  }
	 /**********************************************
	  * 初始化快捷设置适配器
	  * @return 适配器
	  */
	 public MyAdapter InitMyAdapter_Databases()
	  {
		 int[] temp;
		 ArrayList<TimeSettingEntity> arrayList=SearchAllData(2);
		 System.out.println("arrayList长度是:"+arrayList.size());
		  MyAdapter adapter=null;
		  String[] from={"image","scence","scence_time","policy","bieming","allminutes"};
		  int[] to={R.id.ivsecond_icon,R.id.tv_scence_quick,R.id.tv_scence_time_quick,R.id.tv_policy_quick,R.id.tv_bieming_quick,R.id.tvsecondallminutes};
		  ArrayList<Map<String, Object>> lists=new ArrayList<Map<String,Object>>();
		  for (int i = 0; i < arrayList.size(); i++) {
			 Map<String, Object> map=new HashMap<String, Object>();
			  TimeSettingEntity entity=arrayList.get(i);
			  int begin=Integer.valueOf(entity.getBegintime());
			  String _begin=begin>=10?begin+"":"0"+begin;
			  int end=Integer.valueOf(entity.getEndtime());
			  String _end=end>=10?end+"":"0"+end;
			  temp="非急勿扰".equals(entity.getTimesetting_status())?DataResours.fjwricons:DataResours.qhdricons;
			 map.put("image", temp[entity.getTimesetting_icon_id()]);
			 map.put("scence",entity.getTimesetting_scene());
			 map.put("scence_time", _begin+"小时"+_end+"分钟");
			 map.put("policy", entity.getTimesetting_status());
			 map.put("bieming", TextUtils.isEmpty(entity.getBieming())?entity.getTimesetting_scene():entity.getBieming());
			 map.put("allminutes",(begin*60+end)+"");
			 lists.add(map);
		} 
		  adapter=new MyAdapter(context,lists,R.layout.quick_list_item_line, from, to);
		  return adapter;
	  }
	  /*************************************************
	   * 获取快捷设置的编辑
	   * @return 适配器
	   */
	 public EditQuickMyAdapter InitMyAdapter_Databases_edit()
	  {
		 int[] temp;
		 ArrayList<TimeSettingEntity> arrayList=SearchAllData(2);
		  String[] from={"image","scence","time","policy","bieming","quick_id"};
		  int[] to={R.id.ivquick_item_lineicon_edit,R.id.tvquick_item_line_one_edit,R.id.tvquick_item_line_two_edit,R.id.tvquick_item_line_three_edit,R.id.tvquick_item_line_four_edit,R.id.tvquick_item_line_id_edit,R.id.btnquick_item_line_edit};
		  ArrayList<HashMap<String, Object>> lists=new ArrayList<HashMap<String,Object>>();
		  for (int i = 0; i < arrayList.size(); i++) {
			 HashMap<String, Object> map=new HashMap<String, Object>();
			  TimeSettingEntity entity=arrayList.get(i);
			  int begin=Integer.valueOf(entity.getBegintime());
			  int end=Integer.valueOf(entity.getEndtime());
			  String _begin=begin>=10?begin+"":"0"+begin;
			  String _end=end>=10?end+"":"0"+end;
			  temp="非急勿扰".equals(entity.getTimesetting_status())?DataResours.fjwricons:DataResours.qhdricons;
			 map.put("image", temp[entity.getTimesetting_icon_id()]);
			 map.put("scence",entity.getTimesetting_scene());
			 map.put("time", _begin+"小时"+_end+"分钟");
			 map.put("policy", entity.getTimesetting_status());
			 map.put("bieming",TextUtils.isEmpty(entity.getBieming())?entity.getTimesetting_scene():entity.getBieming());
			 map.put("allminutes",(begin*60+end)+"");
			 map.put("quick_id", entity.getId()+"");
			 lists.add(map);
		} 
		  return  new EditQuickMyAdapter(context,lists,R.layout.quick_list_line_item_edit, from, to);
	  }
	 
	 
	 
	 /********************************
	  * 返回是否是编辑的适配器
	  * @param temp
	  * @return
	  */
	 public BaseAdapter getadapter(int temp){
			if(temp==1){
				return  InitMyAdapter_Databases();
			}else if(temp==2){
				return InitMyAdapter_Databases_edit();
			}
			return null;
		}
	
}
