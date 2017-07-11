package winway.mdr.chinaunicom.databases.tools;

public interface ConstantsSources {
	/************免打扰数据库名称*************/
    String MDRDATABASENAME="mdr.db";
	/************定时设置表名称*************/
    String TABLE_TIMESETTINGNAME="timesetting_table";
  /**主键**/
	String TIMESEETING_KEY = "timesetting_key";

	/**
	 * 服务器数据ID
	 */
    String TIMESEETING_ID="timesetting_id";
    
   /**重复的操作**/
    String TIMESEETING_REPEAT="timesetting_repeat";
    
   /**开始时间**/
    String TIMESEETING_BEGINTIME="timesetting_begintime";
    
   /***结束时间**************/
    String TIMESEETING_ENDTIME="timesetting_endtime";
    
    /***状态**************/
    String TIMESEETING_STATUS="timesetting_status";
    
    /***情景**************/
    String TIMESEETING_SCENE="timesetting_scene";
    
    /***是否开启**************/
    String TIMESEETING_ISOPENORNO="timesetting_isopen";
    String TIMESETTING_ITEM_ICON_ID="timesetting_icon_id";
    
    /***时间设置的sql语句*******/
    String CREATE_TABLE_TIMESETTING_SQL="create table  "+TABLE_TIMESETTINGNAME+"("+
    TIMESEETING_KEY +" Integer primary key autoincrement, "+
    TIMESEETING_ID +" varchar(20) , "+
    TIMESEETING_REPEAT+" varchar(20) , "+
    TIMESEETING_BEGINTIME+" varchar(20) , "+
    TIMESEETING_ENDTIME+" varchar(20) ,"+
    TIMESEETING_STATUS+" varchar(20)  , "+
    TIMESEETING_ISOPENORNO+" Integer , "+
    TIMESETTING_ITEM_ICON_ID+" Integer , "+
    TIMESEETING_SCENE+" varchar(20) )";
    
    /*********************快捷设置数据库****************************/
    String BIEMING="bieming";
    String TABLE_QUICKSETUP_TABLENAME="quicksetup_table";
    String CREATE_TABLE_QUICKSETUP_SQL="create table  "+TABLE_QUICKSETUP_TABLENAME+"("+
    TIMESEETING_ID+" Integer primary key autoincrement, "+
    TIMESEETING_REPEAT+" varchar(20) , "+
    TIMESEETING_BEGINTIME+" varchar(20) , "+
    TIMESEETING_ENDTIME+" varchar(20) ,"+
    TIMESEETING_STATUS+" varchar(20)  , "+
    TIMESEETING_ISOPENORNO+" Integer , "+
    TIMESETTING_ITEM_ICON_ID+" Integer , "+
    TIMESEETING_SCENE+" varchar(20) ,"+BIEMING+" varchar(20))";
    
    
    
    /***黑白名单表名称**************/
    String TABLE_BLACK_WHITE_TABLENAME="blackwhite_table";
    /*****************黑白名单ID*/
    String TABLE_BLACK_WHITE_ID="_id";
    /*****************黑白名单姓名*/
    String TABLE_BLACK_WHITE_PHONE_NAME="blackwhite_phonename";
    /*****************黑白名单电话*/
    String TABLE_BLACK_WHITE_PHONE_NUMBER="blackwhite_phonenumber";
    /*****************黑白名单备注*/
    String TABLE_BLACK_WHITE_PHONE_BEIZHU="blackwhite_beizhu";
    /*****************表示是否是黑名单还是白名单*/
    String TABLE_BLACK_WHITE_PHONE_BLACKORWHITE="blackwhite_black_or_white";
    
    
   /***时间设置的sql语句*******/
    String CREATE_TABLE_BLACK_WHITE_SQL="create table  "+TABLE_BLACK_WHITE_TABLENAME+"("+
    TABLE_BLACK_WHITE_ID+" Integer primary key autoincrement, "+
    TABLE_BLACK_WHITE_PHONE_NAME+" varchar(20) , "+
    TABLE_BLACK_WHITE_PHONE_NUMBER+" varchar(20) , "+
    TABLE_BLACK_WHITE_PHONE_BEIZHU+" varchar(20) ,"+
    TABLE_BLACK_WHITE_PHONE_BLACKORWHITE+" Integer ) " ;
  
 /******************************************************************************************/
   /************进行增删改的数据对象*********/
     int READ_DATABASE=1;
    /****进行查数据库对象*************************/
     int WRITE_DATABASE=2;
    /*************************************数据操作类型(增删改查)********************************/
    /************进行增加操作*****/
     int OP_ADD=0;
    /************进行删除操作*****/
     int OP_DEL=1;
    /************进行更新操作*****/
     int OP_UPDATA=2;
    /************进行查询操作*****/
     int OP_QUERY=3;
   

}
