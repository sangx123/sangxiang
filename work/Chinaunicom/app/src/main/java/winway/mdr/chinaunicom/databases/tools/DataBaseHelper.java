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
	 * ���д������ݿ�
	 * @param context ��ǰ������
	 * @param databasename ���ݿ�����
	 * @param factory
	 * @param version
	 * CreateTemp.READ_DATABASE  ��
	 * CreateTemp.WRITE_DATABASE д
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
    * ���в�������(��ɾ��)
    * @param tableName ������
    * @param values ����
    * @param condition ����
    * @param op ����
    * @return ִ�к�Ľ��
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
    * ������صĲ�ѯ����
    * @param tableName ������
    * @param columns ��ѯ����
    * @param condition ��ѯ������
    * @return ���ز�ѯ�Ľ��
    */
   public Cursor Query(String tableName,String[] columns,String condition)
   {
	   return database.query(tableName, columns, condition, null,null,null,null);
   }
   /*************************************
    * ����ID����ɾ��
    * @param timesetting_id ʱ��ƻ��е�ID
    */
   public void DeleteByPlanId(int timesetting_id){
		String sql = "delete from timesetting_table where timesetting_id = " + timesetting_id;
		   database.execSQL(sql);
   }

	/**
	 * ɾ�����ж�ʱ״̬����
	 * 
	 */
	public void DeleteAllTimeData() {
		String sql = "delete from timesetting_table";
		database.execSQL(sql);
	}
   /*****************************
    * ɾ���ڰ������е�����
    * @param _id
    */
  public void DeleteBlackOrWhite(int _id){
	   String sql="delete from blackwhite_table where _id="+_id;
	   database.execSQL(sql);
  }
  /*****************************
   * ɾ���ڰ������е�����
   * @param _id
   */
 public void DeleteAllBlackOrWhite(int black_white){
	   String sql="delete from blackwhite_table where blackwhite_black_or_white="+black_white;
	   database.execSQL(sql);
 }
  /*******************************
   * ��������
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
    * ����ID���и���������Ϣ
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
    * ����ID���в�������
    * @param id
    * @return
    */
   public Cursor GetMyNeedEntity(int id)
   {
	   String sql="select * from timesetting_table  where timesetting_id ="+id;
	   return database.rawQuery(sql, null);
   }
    /****************************
     * ��ѯ�ڰ������е�����
     * @param id id
     * @return
     */
   public Cursor GetBlack_WhiteEntity(int id)
   {
	   String sql="select * from blackwhite_table  where _id ="+id;
	   return database.rawQuery(sql, null);
   }
   /******************************
    * ����ID���в��ҿ�ݱ��е�����
    * @param id ����ID
    * @return ����Cursor���ݼ�
    */
   public Cursor GetQuicksetupEntity(int id)
   {
	   String sql="select * from quicksetup_table  where timesetting_id ="+id;
	   return database.rawQuery(sql, null);
   }
   /************************
    * ��������ʱ�����Ƿ����ı�ʶ
    * @param id ����ID
    * @param status ״̬
    */
   public void UpdateStatusOpenOrNo(int id,int status){
	   String sql="update timesetting_table set timesetting_isopen="+status+" where timesetting_id="+id;
	   database.execSQL(sql);
   }
   /*******************************
    * ͨ��Id����ɾ��������ñ��е�����
    * @param id ������ñ��е�ID
    * @return 1��ʾɾ���ɹ� 0��ʾɾ��ʧ��
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
