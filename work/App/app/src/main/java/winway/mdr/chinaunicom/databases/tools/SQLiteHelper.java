package winway.mdr.chinaunicom.databases.tools;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
	/***********************
	 * 创建数据库以及创建相关的表
	 * @author zhaohao
	 *
	 */
public class SQLiteHelper extends SQLiteOpenHelper {
	public SQLiteHelper(Context context,String databasename,CursorFactory cursor,int version) {
		 super(context,databasename,cursor,version);
	}
	  @Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(ConstantsSources.CREATE_TABLE_TIMESETTING_SQL);
			db.execSQL(ConstantsSources.CREATE_TABLE_BLACK_WHITE_SQL);
			/******************快捷设置数据*************/
			db.execSQL(ConstantsSources.CREATE_TABLE_QUICKSETUP_SQL);
		}
	  @Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		     db.execSQL("drop table if exists notes");
			 onCreate(db);
		}
}
