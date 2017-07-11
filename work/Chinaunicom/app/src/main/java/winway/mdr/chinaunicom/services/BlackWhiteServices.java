package winway.mdr.chinaunicom.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import winway.mdr.chinaunicom.activity.R;
import winway.mdr.chinaunicom.comm.BlackWhiteMyAdapter;
import winway.mdr.chinaunicom.comm.EditBlackWhiteMyAdapter;
import winway.mdr.chinaunicom.databases.tools.ConstantsSources;
import winway.mdr.chinaunicom.databases.tools.DataBaseHelper;
import winway.mdr.chinaunicom.entity.BlackWhiteEntity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.BaseAdapter;

import com.liz.cptr.TPhonebookReturn;
/*****************************************************
 * 黑白名单的服务类
 * @author zhaohao
 * 时间2011-11
 */
public class BlackWhiteServices {
  
	DataBaseHelper readdatabasehelper;
	DataBaseHelper writedatabasehelper;
	DataBaseHelper baseHelper;
	Context context;
    public BlackWhiteServices(Context _context) {
	      context=_context;
	      readdatabasehelper=new DataBaseHelper(_context,ConstantsSources.MDRDATABASENAME, null, 1, ConstantsSources.READ_DATABASE);
	      writedatabasehelper=new DataBaseHelper(_context,ConstantsSources.MDRDATABASENAME, null, 1, ConstantsSources.WRITE_DATABASE);
	}
    /*****************************************************************
	   * 函数名称 :InsertNewSettingData
	   * 参 数说明 :BlackWhiteEntity entity,int readorwrite 黑名单和表名单实体  读写数据库的标识
	   * 时         间 :2011-11
	   * 返回值:boolean
	   * 功能说明:向黑白名单数据表中添加数据
	   ****************************************************************/
    public boolean InsertNewSettingData(BlackWhiteEntity entity,int readorwrite){
    	boolean Status = false;
    	baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
    	ContentValues contentValues=new ContentValues();
    	contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_NAME, entity.getPhone_name());
    	contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_NUMBER, entity.getPhone_number());
    	contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_BEIZHU, entity.getPhone_beizhu());
    	contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_BLACKORWHITE, entity.getBlack_or_white());
    	long result=baseHelper.UpdataData(ConstantsSources.TABLE_BLACK_WHITE_TABLENAME, contentValues, null, ConstantsSources.OP_ADD);
    	Status=result>=0?true:false;
    	return Status;
    }
    /*****************************************************************
	   * 函数名称 :SearchAllData
	   * 参 数说明 :int readorwrite,int blackorwhite  读写数据库和黑白标识
	   * 时         间 :2011-11
	   * 返回值:ArrayList<BlackWhiteEntity>
	   * 功能说明:根据标识查找相关的数据
	   ****************************************************************/
    public ArrayList<BlackWhiteEntity> SearchAllData(int readorwrite,int blackorwhite){
    	ArrayList<BlackWhiteEntity> arrayList=new ArrayList<BlackWhiteEntity>();
    	baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
    	Cursor cursor= baseHelper.Query(ConstantsSources.TABLE_BLACK_WHITE_TABLENAME, null, ConstantsSources.TABLE_BLACK_WHITE_PHONE_BLACKORWHITE+"="+blackorwhite);
    	while (cursor.moveToNext()) {
    		   BlackWhiteEntity entity=new BlackWhiteEntity();
			  entity.setId(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TABLE_BLACK_WHITE_ID)));
			  entity.setPhone_name(cursor.getString(cursor.getColumnIndex(ConstantsSources.TABLE_BLACK_WHITE_PHONE_NAME)));
			  entity.setPhone_number(cursor.getString(cursor.getColumnIndex(ConstantsSources.TABLE_BLACK_WHITE_PHONE_NUMBER)));
			  entity.setPhone_beizhu(cursor.getString(cursor.getColumnIndex(ConstantsSources.TABLE_BLACK_WHITE_PHONE_BEIZHU)));
			  entity.setBlack_or_white(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TABLE_BLACK_WHITE_PHONE_BLACKORWHITE)));
			  arrayList.add(entity);
		}
    	return arrayList;
    }
    
    public BaseAdapter GetMyListViewAdapter(String name){
   	 BaseAdapter adapter=null;
   	 int blackorwhite="黑名单".equals(name)?0:1;
   	 ArrayList<BlackWhiteEntity> entities=SearchAllData(2,blackorwhite);
   	 ArrayList<HashMap<String, Object>> lists=new ArrayList<HashMap<String,Object>>();
   	 for (int i = 0; i <entities.size(); i++) {
   		BlackWhiteEntity blackWhiteEntity=entities.get(i);
   		HashMap<String, Object> map=new HashMap<String, Object>();
   		map.put("phone_name", blackWhiteEntity.getPhone_name());
   		map.put("phone_number", blackWhiteEntity.getPhone_number());
   		map.put("phone_id", blackWhiteEntity.getId()+"");
   		lists.add(map);
   	}
   	 adapter=new BlackWhiteMyAdapter(context, lists, R.layout.black_write_listview_item, new String[]{"phone_name","phone_number","phone_id"}, new int[]{R.id.tv_phone_name,R.id.tv_phone_number,R.id.tv_phone_id});
   	return adapter;
    }
    /***********************
     * 创建编辑项的适配器适配器
     * @param name 标识名称
     * @return 编辑适配器
     */
    public EditBlackWhiteMyAdapter InitAdapter_edit(String name){
    	  EditBlackWhiteMyAdapter adapter = null;
		 int blackorwhite="黑名单".equals(name)?0:1;
		ArrayList<BlackWhiteEntity> arrayList=SearchAllData(2, blackorwhite);
		String[] items={"phone_id","phone_name","phone_number","blackorwhite"};
		int[] ids={R.id.btndeleteblack_orwhite_edit,R.id.tvblack_white_phone_name,R.id.tvblack_white_phone_number,R.id.tvblack_whitelistitem_id_edit};
		ArrayList<HashMap<String, Object>> lists=new ArrayList<HashMap<String,Object>>();
		for (int i = 0; i <arrayList.size(); i++) {
			BlackWhiteEntity blackWhiteEntity=arrayList.get(i);
	   		HashMap<String, Object> map=new HashMap<String, Object>();
	   		map.put("phone_id", blackWhiteEntity.getId());
	   		map.put("phone_name", blackWhiteEntity.getPhone_name());
	   		map.put("phone_number", blackWhiteEntity.getPhone_number());
	   		map.put("blackorwhite", blackorwhite+"");
	   		lists.add(map);
		}
		adapter=new EditBlackWhiteMyAdapter(context, lists, R.layout.black_white_listview_item_edit,items ,ids);
		return adapter;
	}
	public BaseAdapter getadapter(int temp,String name){
		if(temp==1){
			return  GetMyListViewAdapter(name);
		}else if(temp==2){
			return InitAdapter_edit(name);
		}
		return null;
	}
	/*********************************
	 * 根据_id进行删除数据
	 * @param _id 标识ID
	 * @param readorwrite   读数据库或写数据库
	 * @return 返回的标识
	 */
	public int DeleteById(int _id,int readorwrite){
		try {
			baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
			baseHelper.DeleteBlackOrWhite(_id);
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	/*************************************
	 * 根据标识Id进行查找相关的数据
	 * @param _id 标识ID
	 * @param readorwrite 读写数据库标识
	 * @return 返回查找的实体
	 */
	public BlackWhiteEntity GetMyBlackEntity(int _id,int readorwrite){
		BlackWhiteEntity blackWhiteEntity=null;
		baseHelper=readorwrite==1?writedatabasehelper:readdatabasehelper;
		Cursor cursor=baseHelper.GetBlack_WhiteEntity(_id);
		System.out.println("cursor长度是:"+cursor.getCount());
		if(cursor.moveToNext()){
			 blackWhiteEntity=new BlackWhiteEntity();
			blackWhiteEntity.setId(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TABLE_BLACK_WHITE_ID)));
			blackWhiteEntity.setPhone_name(cursor.getString(cursor.getColumnIndex(ConstantsSources.TABLE_BLACK_WHITE_PHONE_NAME)));
			blackWhiteEntity.setPhone_number(cursor.getString(cursor.getColumnIndex(ConstantsSources.TABLE_BLACK_WHITE_PHONE_NUMBER)));
			blackWhiteEntity.setPhone_beizhu(cursor.getString(cursor.getColumnIndex(ConstantsSources.TABLE_BLACK_WHITE_PHONE_BEIZHU)));
			blackWhiteEntity.setBlack_or_white(cursor.getInt(cursor.getColumnIndex(ConstantsSources.TABLE_BLACK_WHITE_PHONE_BLACKORWHITE)));
		}
		return blackWhiteEntity;
	}
    /*************************************
     * 通过id更改黑白名单
     * @param blackWhiteEntity  黑白名单实体
     * @param readorwhite  读数据和写数据
     * @return boolean true更改成功 false更改失败
     */
	public boolean UpdateBlack_white(BlackWhiteEntity blackWhiteEntity,int readorwhite){
		baseHelper=readorwhite==1?writedatabasehelper:readdatabasehelper;
		ContentValues contentValues=new ContentValues();
		contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_NAME, blackWhiteEntity.getPhone_name());
		contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_NUMBER, blackWhiteEntity.getPhone_number());
		contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_BEIZHU, blackWhiteEntity.getPhone_beizhu());
		contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_BLACKORWHITE, blackWhiteEntity.getBlack_or_white());
		long result=baseHelper.UpdataData(ConstantsSources.TABLE_BLACK_WHITE_TABLENAME, contentValues, ConstantsSources.TABLE_BLACK_WHITE_ID+"="+blackWhiteEntity.getId(), ConstantsSources.OP_UPDATA);
		if(result>0) {
			return true;
		} else {
			return false;
		}
		
	}
	/********************************************
	 * 从网络上获取相关的数据
	 * @param tphone   联系人列表
	 * @param whiteorblack 标识黑白联系人
	 * @param blackorreadStatus 读些数据库标识
	 */
	public void AddBlackOrWhite(Vector<TPhonebookReturn> tphone,int whiteorblack,int blackorreadStatus){
		baseHelper=blackorreadStatus==1?writedatabasehelper:readdatabasehelper;
		if(tphone!=null){
			baseHelper.DeleteAllBlackOrWhite(whiteorblack);
			for (TPhonebookReturn book : tphone) {
				ContentValues contentValues=new ContentValues();
				contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_NAME, book.getUserName());
		    	contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_NUMBER,book.getPhoneNumber());
		    	contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_BEIZHU,book.getComment());
		    	contentValues.put(ConstantsSources.TABLE_BLACK_WHITE_PHONE_BLACKORWHITE, String.valueOf(whiteorblack));
		    	baseHelper.UpdataData(ConstantsSources.TABLE_BLACK_WHITE_TABLENAME, contentValues, null, ConstantsSources.OP_ADD);
		  }
		}
	}
	/********************************************
	 * 没有相关数据的时候进行删除相关的数据信息
	 * @param whiteorblack 标识黑白联系人
	 * @param blackorreadStatus 读些数据库标识
	 */
	public void DeletedetailBlackWhite(int whiteorblack,int blackorreadStatus){
		    try {
				baseHelper=blackorreadStatus==1?writedatabasehelper:readdatabasehelper;
				baseHelper.DeleteAllBlackOrWhite(whiteorblack);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
