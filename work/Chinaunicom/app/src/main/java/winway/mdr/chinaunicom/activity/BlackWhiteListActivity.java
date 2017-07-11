package winway.mdr.chinaunicom.activity;

import java.util.Vector;

import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.HTTPCommentTools;
import winway.mdr.chinaunicom.comm.NetWorkConnectionUtil;
import winway.mdr.chinaunicom.current.listview.MyListView;
import winway.mdr.chinaunicom.current.listview.MyListView.OnRefreshListener;
import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
import winway.mdr.chinaunicom.services.BlackWhiteServices;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.liz.cptr.TBlackwhiteState;
import com.liz.cptr.TPhonebookReturn;
/***********************************
 * 黑名单和白名单界面
 * @author zhaohao
 * time:2011-11-16
 * 相关功能介绍:关于项目中黑白名单显示以及添加界面
 */
public class BlackWhiteListActivity extends Activity 
								   implements OnClickListener ,OnItemClickListener{
	TextView tvblack_white_view;
	Button btnforback,btnaddnewdata,btneditolddata;
	String name;
	MyListView lvblack_white_listview;
	BlackWhiteServices blackWhiteServices;
	SharedPreferences sharedPreferences;
	boolean IsEdit=true;
	HttpDataAccess dataAccess=HttpDataAccess.getInstance();
	ProgressDialog dialog=null;
	HTTPCommentTools httpCommentTools;
	String status="";
   @Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.black_white_main);
	httpCommentTools=HTTPCommentTools.getInstance();
	InitMyData();
}
   /*****************************************************************
    * 函数名称 :InitMyData
    * 参 数说明 :无
    * 时         间 :2011-11
    * 返回值:无
    * 功能说明:初始化相关的控件信息
    ****************************************************************/
  public void InitMyData(){
	  dialog=new ProgressDialog(this);
	  dialog.setMessage("正在初始化数据...");
	  sharedPreferences=getSharedPreferences("black_white", MODE_PRIVATE);
	  blackWhiteServices=new BlackWhiteServices(this);
	  Intent intent=getIntent();
	  tvblack_white_view=(TextView) this.findViewById(R.id.tvblack_white_view);
	  btnaddnewdata=(Button) this.findViewById(R.id.btnaddnewdata);
	  btneditolddata=(Button) this.findViewById(R.id.btneditolddata);
	  lvblack_white_listview=(MyListView) this.findViewById(R.id.lvblack_white_listview);
	  btnaddnewdata.setOnClickListener(this);
	  btneditolddata.setOnClickListener(this);
	  lvblack_white_listview.setonRefreshListener(new MyListViewOnRefreshEvent());
	  btnforback=(Button) this.findViewById(R.id.btnforback);
	  btnforback.setOnClickListener(this);
	  name=intent.getStringExtra("name");
     if(getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status",0)==0) {
    	    Toast.makeText(this, "您尚未登录", 3).show();
    	    Intent intent_login_detail=new  Intent(this, LoginActivity.class);
	        startActivityForResult(intent_login_detail, DataResours.REQUEST_LOGIN_CODE);
     }
      else{
	    try {
			new BlackOrWhiteListPhoneTask().execute(new String[]{name});
		} catch (Exception e) {
			 Toast.makeText(BlackWhiteListActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
			e.printStackTrace();
		}
      }
	  tvblack_white_view.setText(name);
	  lvblack_white_listview.setAdapter(blackWhiteServices.GetMyListViewAdapter(name));
	  lvblack_white_listview.setOnItemClickListener(this);
  }
  @Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  if(requestCode==DataResours.REQUEST_LOGIN_CODE&&resultCode==DataResours.RESULT_LOGIN_CODE)
	  {
		  try {
			new BlackOrWhiteListPhoneTask().execute(new String[]{name});
		} catch (Exception e) {
			 Toast.makeText(BlackWhiteListActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
			e.printStackTrace();
		}
	  }
	super.onActivityResult(requestCode, resultCode, data);
}
  /*****************************************************************
   * 函数名称 :onClick系统方法
   * 参 数说明 :无
   * 时         间 :2011-11
   * 返回值:无
   * 功能说明:控件的点击事件
   ****************************************************************/
@Override
public void onClick(View v) {
		   switch (v.getId()) {
				case R.id.btnforback:
					 this.finish();
					break;
				case R.id.btnaddnewdata:
						Intent intent_black=new Intent(this, AddBlackWhiteActivity.class);
						intent_black.putExtra("status_name", name);
						System.out.println("status_name---->>>"+name);
						startActivity(intent_black);
					break;
				case R.id.btneditolddata:
					if(getServerStatus()){
						if(!IsEdit){
							   IsEdit=true;
							  lvblack_white_listview.setAdapter(blackWhiteServices.getadapter(1, name));
						}else if(IsEdit){
							   IsEdit=false;
							   lvblack_white_listview.setAdapter(blackWhiteServices.getadapter(2, name));
						}
					}else{
						Toast.makeText(BlackWhiteListActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
					}
					
					break;
				default:
					break;
			 }
			
		}
/*****************************************************************
 * 函数名称 :onResume
 * 参 数说明 :无
 * 时         间 :2011-11
 * 返回值:无
 * 功能说明:系统方法(当用户在添加黑白名单返回的时候会回调
 * 这个方法当添加新数据或更改相关数据的时候会刷新列表)
 ****************************************************************/
 @Override
protected void onResume() {
	 int updateListview=sharedPreferences.getInt("black_white_update_status", 0);
	 if(updateListview==1){
		 IsEdit=true;
		 lvblack_white_listview.setAdapter(blackWhiteServices.GetMyListViewAdapter(name));
		 sharedPreferences.edit().putInt("black_white_update_status", 0).commit();
	 }else{
		 IsEdit=true;
		 lvblack_white_listview.setAdapter(blackWhiteServices.GetMyListViewAdapter(name));
	 }
	super.onResume();
}
 /*****************************************************************
  * 函数名称 :onItemClick
  * 参 数说明 :无
  * 时         间 :2011-11
  * 返回值:无
  * 功能说明:系统方法(ListView点击事件)
  ****************************************************************/
@Override
public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	 if(IsEdit){
		String _id=((TextView)view.findViewById(R.id.tv_phone_id)).getText().toString();
		Intent intent=new Intent(this, AddBlackWhiteActivity.class);
		 intent.putExtra("update_id", _id);
		 startActivityForResult(intent, DataResours.REQURST_UPDATEBLACK_WHITE);
	 }else{
		 String update_id=((TextView)view.findViewById(R.id.tvblack_whitelistitem_id_edit)).getText().toString();
		 Intent intent=new Intent(this, AddBlackWhiteActivity.class);
		 intent.putExtra("update_id", update_id);
		 startActivityForResult(intent, DataResours.REQURST_UPDATEBLACK_WHITE);
	 }
	
}
class BlackOrWhiteListPhoneTask extends AsyncTask<String, String, String> {  
    protected String doInBackground(String... params)
    { 
  	  if("黑名单".equals(params[0])){
		 Vector<TPhonebookReturn> black_phonelist=dataAccess.showBlackList(TBlackwhiteState.BLACKLIST);
		 //从服务器上获取黑名单列表并存入到相关的数据库中
		  if(GetLastErrorStatus()){
			  if(black_phonelist!=null){
				  if(black_phonelist.size()>0)
					  blackWhiteServices.AddBlackOrWhite(black_phonelist, 0,1);
			  }
			  else
				  blackWhiteServices.DeletedetailBlackWhite(0,1);
		  }
			  
		 
	  }else if("白名单".equals(params[0])){
		  Vector<TPhonebookReturn> black_phonelist=dataAccess.showBlackList(TBlackwhiteState.WHITELIST);
			 //从服务器上获取黑名单列表并存入到相关的数据库中
		     if(GetLastErrorStatus()){
		    	 if(black_phonelist!=null){
		    		 if(black_phonelist.size()>0)
			    		 blackWhiteServices.AddBlackOrWhite(black_phonelist,1,1);
		    	 } else
		    		 blackWhiteServices.DeletedetailBlackWhite(1,1);
		     }
			 
	  }
    	return "";
    }  
    protected void onCancelled() {  
        super.onCancelled();  
    }  
    @SuppressWarnings("static-access")
	protected void onPostExecute(String result) 
    { 
    	if(GetLastErrorStatus()){
    		if(TextUtils.isEmpty(status))dialog.hide();
        	lvblack_white_listview.setAdapter(blackWhiteServices.GetMyListViewAdapter(name));
    	}else{
    		if(TextUtils.isEmpty(status))dialog.hide();
    		  if(dataAccess.getLastError().HTTP_NEED_RELOGIN.toString().equals(dataAccess.getLastError().toString())){
    			    Intent intent=new  Intent(BlackWhiteListActivity.this, LoginActivity.class);
                    startActivityForResult(intent,111);
    		       }
    		Toast.makeText(BlackWhiteListActivity.this, httpCommentTools.GetEcption(dataAccess.getLastError().toString()), 3).show();
    	}
    	status="";
        lvblack_white_listview.onRefreshComplete();
    }  
    protected void onPreExecute() { 
    	if(TextUtils.isEmpty(status))dialog.show();
    }  
    protected void onProgressUpdate(Integer... values) { 
    	 
    }  
 } 
@SuppressWarnings("static-access")
public boolean GetLastErrorStatus(){
	 if(dataAccess.getLastError().equals(dataAccess.getLastError().SUCCESS))return true;
	 else return false;
}

/***********************************
 * myListView刷新事件
 * @author zhaohao
 * 功能说明:用户进行拖拽ListVIew进行获取最新的相关数据信息
 */
class MyListViewOnRefreshEvent implements OnRefreshListener{

	@Override
	public void onRefresh() {
		 try {
			status="status";
			 new BlackOrWhiteListPhoneTask().execute(new String[]{name});
		} catch (Exception e) {
			 Toast.makeText(BlackWhiteListActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
		}
	}
	
}
@SuppressWarnings("static-access")
public boolean getServerStatus(){
     try {
		return  NetWorkConnectionUtil.getServerStatus(dataAccess.getHttpServerUrl());
	} catch (Exception e) {
		 return false;
	}
}
}
