package winway.mdr.chinaunicom.databases.tools;

public interface ConstantsSources {
	/************��������ݿ�����*************/
    String MDRDATABASENAME="mdr.db";
	/************��ʱ���ñ�����*************/
    String TABLE_TIMESETTINGNAME="timesetting_table";
  /**����**/
	String TIMESEETING_KEY = "timesetting_key";

	/**
	 * ����������ID
	 */
    String TIMESEETING_ID="timesetting_id";
    
   /**�ظ��Ĳ���**/
    String TIMESEETING_REPEAT="timesetting_repeat";
    
   /**��ʼʱ��**/
    String TIMESEETING_BEGINTIME="timesetting_begintime";
    
   /***����ʱ��**************/
    String TIMESEETING_ENDTIME="timesetting_endtime";
    
    /***״̬**************/
    String TIMESEETING_STATUS="timesetting_status";
    
    /***�龰**************/
    String TIMESEETING_SCENE="timesetting_scene";
    
    /***�Ƿ���**************/
    String TIMESEETING_ISOPENORNO="timesetting_isopen";
    String TIMESETTING_ITEM_ICON_ID="timesetting_icon_id";
    
    /***ʱ�����õ�sql���*******/
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
    
    /*********************����������ݿ�****************************/
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
    
    
    
    /***�ڰ�����������**************/
    String TABLE_BLACK_WHITE_TABLENAME="blackwhite_table";
    /*****************�ڰ�����ID*/
    String TABLE_BLACK_WHITE_ID="_id";
    /*****************�ڰ���������*/
    String TABLE_BLACK_WHITE_PHONE_NAME="blackwhite_phonename";
    /*****************�ڰ������绰*/
    String TABLE_BLACK_WHITE_PHONE_NUMBER="blackwhite_phonenumber";
    /*****************�ڰ�������ע*/
    String TABLE_BLACK_WHITE_PHONE_BEIZHU="blackwhite_beizhu";
    /*****************��ʾ�Ƿ��Ǻ��������ǰ�����*/
    String TABLE_BLACK_WHITE_PHONE_BLACKORWHITE="blackwhite_black_or_white";
    
    
   /***ʱ�����õ�sql���*******/
    String CREATE_TABLE_BLACK_WHITE_SQL="create table  "+TABLE_BLACK_WHITE_TABLENAME+"("+
    TABLE_BLACK_WHITE_ID+" Integer primary key autoincrement, "+
    TABLE_BLACK_WHITE_PHONE_NAME+" varchar(20) , "+
    TABLE_BLACK_WHITE_PHONE_NUMBER+" varchar(20) , "+
    TABLE_BLACK_WHITE_PHONE_BEIZHU+" varchar(20) ,"+
    TABLE_BLACK_WHITE_PHONE_BLACKORWHITE+" Integer ) " ;
  
 /******************************************************************************************/
   /************������ɾ�ĵ����ݶ���*********/
     int READ_DATABASE=1;
    /****���в����ݿ����*************************/
     int WRITE_DATABASE=2;
    /*************************************���ݲ�������(��ɾ�Ĳ�)********************************/
    /************�������Ӳ���*****/
     int OP_ADD=0;
    /************����ɾ������*****/
     int OP_DEL=1;
    /************���и��²���*****/
     int OP_UPDATA=2;
    /************���в�ѯ����*****/
     int OP_QUERY=3;
   

}
