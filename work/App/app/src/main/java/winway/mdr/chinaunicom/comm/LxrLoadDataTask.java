package winway.mdr.chinaunicom.comm;


import java.util.ArrayList;

import winway.mdr.chinaunicom.entity.LxrEntity;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
 /******************************************
  * 内部类，用户获取联系人列表信息
  * @author zhaohao
  * 时间:2011-11
  * 功能:采用异步任务
  */
@TargetApi(Build.VERSION_CODES.ECLAIR)
public class LxrLoadDataTask extends AsyncTask<String, Object, String> {  
	private static Context context=null;
    private ArrayList<LxrEntity> arrayList=new ArrayList<LxrEntity>();
    private static ArrayList<LxrEntity> listLxtList=new ArrayList<LxrEntity>();
	public static ArrayList<LxrEntity> getListLxtList() {
		return listLxtList;
	}
	public static void setListLxtList(ArrayList<LxrEntity> listLxtList) {
		LxrLoadDataTask.listLxtList = listLxtList;
	}
	private int tempnumber=0;
	static LxrLoadDataTask dataTask=new LxrLoadDataTask();
	private boolean executeTag=false;
	public boolean isExecuteTag() {
		return executeTag;
	}
	public void setExecuteTag(boolean executeTag) {
		this.executeTag = executeTag;
	}
	public static LxrLoadDataTask getIns(Context _context){
		context=_context;
		return dataTask;
	}
	 public void executePhoneList(){
		 dataTask.execute();
	 }
	 public ArrayList<LxrEntity> getArrayList() {
		return arrayList;
	}
	public void setArrayList(ArrayList<LxrEntity> arrayList) {
		this.arrayList = arrayList;
	}
	private static final String[] PHONES_PROJECTION = new String[] {Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID };  
	    /**联系人显示名称**/  
	    private int PHONES_DISPLAY_NAME_INDEX = 0;  
	    /**电话号码**/  
	    private int PHONES_NUMBER_INDEX = 1;  
	protected String doInBackground(String... params)
    { 
    	executeTag=true;
    	ContentResolver resolver = context.getContentResolver();
    	Cursor peopleCursor = resolver.query(People.CONTENT_URI, null, null, null, null); // 返回所有联系人信息
    	 Cursor phoneCursor = resolver.query(Uri.parse("content://icc/adn"), PHONES_PROJECTION, null, null, null);
        int allcount=peopleCursor.getCount()+((null!=phoneCursor)?phoneCursor.getCount():0);
        int loadCount=0;
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				// 得到手机号码  
		        String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);  
		        String phoneName=phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
		        LxrEntity entity=new LxrEntity();
		        entity.setUname(phoneName);
		        entity.setUphone(phoneNumber);
		        arrayList.add(entity);
		        listLxtList.add(entity);
		    	loadCount++;
		    	publishProgress(new Object[]{loadCount,allcount,entity});
		    	
			}
		 }
		 phoneCursor.close();
    	while (peopleCursor.moveToNext()) {
    				String idColumn = peopleCursor.getString(peopleCursor.getColumnIndex(People._ID));
    				String nameColumn = peopleCursor.getString(peopleCursor.getColumnIndex(People.NAME));
    				Cursor phonesCursor =((Activity)context).managedQuery(Phones.CONTENT_URI, new String[]{Phones.PERSON_ID,Phones.NUMBER}, Phones.PERSON_ID + "=" + idColumn, null, null);
    				while(phonesCursor.moveToNext()){
    					String phoneColumn = phonesCursor.getString(phonesCursor.getColumnIndex(Phones.NUMBER));
    					  LxrEntity entity=new LxrEntity();
				          entity.setUname(nameColumn);
				          entity.setUphone(phoneColumn);
				          arrayList.add(entity);
				          listLxtList.add(entity);
				          publishProgress(new Object[]{loadCount,allcount,entity});
    				}
    				loadCount++;
    				phonesCursor.close();
    				
    			}
    			peopleCursor.close();
    		 
    	return "";
    }  
    protected void onCancelled() {  
        super.onCancelled();  
    }  
    protected void onPostExecute(String result) 
    { 
    	context.sendBroadcast(new Intent("getphonelist"));
    	executeTag=false;
    }  
    protected void onPreExecute() { 
    }  
    protected void onProgressUpdate(Object... values) {
    	tempnumber++;
    	if(tempnumber==20){
    	     tempnumber=0;
    		 context.sendBroadcast(new Intent("getphonelist"));
    	}
      
    }  
  } 