package winway.mdr.chinaunicom.databases.tools;




import winway.mdr.chinaunicom.entity.TimeSettingEntity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DataBaseHelper {
	 public SQLiteDatabase database;
	 
	/****************
	 * 进行创建数据库
	 * @param context 当前上下文
	 * @param databasename 数据库名称
	 * @param factory
	 * @param version
	 * CreateTemp.READ_DATABASE  读
	 * CreateTemp.WRITE_DATABASE 写
	 */
   public DataBaseHelper(Context context,String databasename,CursorFactory factory,int version,int ReadOrWrite) {
	   SQLiteHelper sqlite=new SQLiteHelper(context, databasename, factory, version);
	   switch(ReadOrWrite) {
		   case ConstantsSources.READ_DATABASE:
			     database=sqlite.getReadableDatabase();
			break;
		   case ConstantsSources.WRITE_DATABASE:
				 database=sqlite.getWritableDatabase();
				break;
	         }
    }
   /**********************
    * 进行操作数据(增删改)
    * @param tableName 表名称
    * @param values 数据
    * @param condition 条件
    * @param op 操作
    * @return 执行后的结果
    */
   public long UpdataData(String tableName,ContentValues values,String condition,int op)
   {
	   long result=0;
	    switch (op) {
		case ConstantsSources.OP_ADD:
			result=database.insert(tableName, null, values);
			break;
		case ConstantsSources.OP_DEL:
				result=database.delete(tableName, condition, null);
		    break;
		case ConstantsSources.OP_UPDATA:
			result=database.update(tableName, values, condition, null);
			break;
		}
	    return result;
   }
   /**********************
    * 进行相关的查询操作
    * @param tableName 表名称
    * @param columns 查询的列
    * @param condition 查询的条件
    * @return 返回查询的结果
    */
   public Cursor Query(String tableName,String[] columns,String condition)
   {
	   return database.query(tableName, columns, condition, null,null,null,null);
   }
   /*************************************
    * 根据ID进行删除
    * @param timesetting_id 时间计划中的ID
    */
   public void DeleteByPlanId(int timesetting_id){
		String sql = "delete from timesetting_table where timesetting_id = " + timesetting_id;
		   database.execSQL(sql);
   }

	/**
	 * 删除所有定时状态数据
	 * 
	 */
	public void DeleteAllTimeData() {
		String sql = "delete from timesetting_table";
		database.execSQL(sql);
	}
   /*****************************
    * 删除黑白名单中的数据
    * @param _id
    */
  public void DeleteBlackOrWhite(int _id){
	   String sql="delete from blackwhite_table where _id="+_id;
	   database.execSQL(sql);
  }
  /*****************************
   * 删除黑白名单中的数据
   * @param _id
   */
 public void DeleteAllBlackOrWhite(int black_white){
	   String sql="delete from blackwhite_table where blackwhite_black_or_white="+black_white;
	   database.execSQL(sql);
 }
  /*******************************
   * 更改数据
   * @param entity
   */
   public void UpdataTimeSetting(TimeSettingEntity entity)
   {
	   String sql="update timesetting_table set timesetting_repeat='"+entity.getDetailrepeat()+"',timesetting_begintime='"+
            entity.getBegintime()+"',timesetting_endtime='"+entity.getEndtime()+"',timesetting_status='"
            +entity.getTimesetting_status()+"',timesetting_scene='"+entity.getTimesetting_scene()+
            "', timesetting_isopen="+entity.getTimesetting_isopen()+" ,timesetting_icon_id="+entity.getTimesetting_icon_id()+"  where timesetting_id="+entity.getId();
	      database.execSQL(sql);
   }
   /*****************************
    * 根据ID进行更新数据信息
    * @param entity
    */
   public void UpdataQuicksetup(TimeSettingEntity entity)
   {
	   String sql="update quicksetup_table set timesetting_repeat='"+entity.getDetailrepeat()+"',timesetting_begintime='"+
            entity.getBegintime()+"',timesetting_endtime='"+entity.getEndtime()+"',timesetting_status='"
            +entity.getTimesetting_status()+"',timesetting_scene='"+entity.getTimesetting_scene()+
            "', timesetting_isopen="+entity.getTimesetting_isopen()+" ,timesetting_icon_id="+entity.getTimesetting_icon_id()+",bieming='"+entity.getBieming()+"'  where timesetting_id="+entity.getId();
	      database.execSQL(sql);
   }
   /**************************
    * 根据ID进行查找数据
    * @param id
    * @return
    */
   public Cursor GetMyNeedEntity(int id)
   {
	   String sql="select * from timesetting_table  where timesetting_id ="+id;
	   return database.rawQuery(sql, null);
   }
    /****************************
     * 查询黑白名单中的数据
     * @param id id
     * @return
     */
   public Cursor GetBlack_WhiteEntity(int id)
   {
	   String sql="select * from blackwhite_table  where _id ="+id;
	   return database.rawQuery(sql, null);
   }
   /******************************
    * 根据ID进行查找快捷表中的数据
    * @param id 查找ID
    * @return 返回Cursor数据集
    */
   public Cursor GetQuicksetupEntity(int id)
   {
	   String sql="select * from quicksetup_table  where timesetting_id ="+id;
	   return database.rawQuery(sql, null);
   }
   /************************
    * 更改设置时间中是否开启的标识
    * @param id 更改ID
    * @param status 状态
    */
   public void UpdateStatusOpenOrNo(int id,int status){
	   String sql="update timesetting_table set timesetting_isopen="+status+" where timesetting_id="+id;
	   database.execSQL(sql);
   }
   /*******************************
    * 通过Id进行删除快捷设置表中的数据
    * @param id 快捷设置表中的ID
    * @return 1表示删除成功 0表示删除失败
    */
   public int DeleteQuickSetupByID(int id){
	   try {
		   String sql="delete from quicksetup_table where timesetting_id="+id;
		   database.execSQL(sql);
		   return 1;
	} catch (SQLException e) {
		e.printStackTrace();
		return 0;
	}
   }
}
