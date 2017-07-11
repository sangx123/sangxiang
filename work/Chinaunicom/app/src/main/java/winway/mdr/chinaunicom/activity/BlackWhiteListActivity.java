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
 * �������Ͱ���������
 * @author zhaohao
 * time:2011-11-16
 * ��ع��ܽ���:������Ŀ�кڰ�������ʾ�Լ���ӽ���
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
    * �������� :InitMyData
    * �� ��˵�� :��
    * ʱ         �� :2011-11
    * ����ֵ:��
    * ����˵��:��ʼ����صĿؼ���Ϣ
    ****************************************************************/
  public void InitMyData(){
	  dialog=new ProgressDialog(this);
	  dialog.setMessage("���ڳ�ʼ������...");
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
    	    Toast.makeText(this, "����δ��¼", 3).show();
    	    Intent intent_login_detail=new  Intent(this, LoginActivity.class);
	        startActivityForResult(intent_login_detail, DataResours.REQUEST_LOGIN_CODE);
     }
      else{
	    try {
			new BlackOrWhiteListPhoneTask().execute(new String[]{name});
		} catch (Exception e) {
			 Toast.makeText(BlackWhiteListActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
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
			 Toast.makeText(BlackWhiteListActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
			e.printStackTrace();
		}
	  }
	super.onActivityResult(requestCode, resultCode, data);
}
  /*****************************************************************
   * �������� :onClickϵͳ����
   * �� ��˵�� :��
   * ʱ         �� :2011-11
   * ����ֵ:��
   * ����˵��:�ؼ��ĵ���¼�
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
						Toast.makeText(BlackWhiteListActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
					}
					
					break;
				default:
					break;
			 }
			
		}
/*****************************************************************
 * �������� :onResume
 * �� ��˵�� :��
 * ʱ         �� :2011-11
 * ����ֵ:��
 * ����˵��:ϵͳ����(���û�����Ӻڰ��������ص�ʱ���ص�
 * �����������������ݻ����������ݵ�ʱ���ˢ���б�)
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
  * �������� :onItemClick
  * �� ��˵�� :��
  * ʱ         �� :2011-11
  * ����ֵ:��
  * ����˵��:ϵͳ����(ListView����¼�)
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
  	  if("������".equals(params[0])){
		 Vector<TPhonebookReturn> black_phonelist=dataAccess.showBlackList(TBlackwhiteState.BLACKLIST);
		 //�ӷ������ϻ�ȡ�������б����뵽��ص����ݿ���
		  if(GetLastErrorStatus()){
			  if(black_phonelist!=null){
				  if(black_phonelist.size()>0)
					  blackWhiteServices.AddBlackOrWhite(black_phonelist, 0,1);
			  }
			  else
				  blackWhiteServices.DeletedetailBlackWhite(0,1);
		  }
			  
		 
	  }else if("������".equals(params[0])){
		  Vector<TPhonebookReturn> black_phonelist=dataAccess.showBlackList(TBlackwhiteState.WHITELIST);
			 //�ӷ������ϻ�ȡ�������б����뵽��ص����ݿ���
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
 * myListViewˢ���¼�
 * @author zhaohao
 * ����˵��:�û�������קListVIew���л�ȡ���µ����������Ϣ
 */
class MyListViewOnRefreshEvent implements OnRefreshListener{

	@Override
	public void onRefresh() {
		 try {
			status="status";
			 new BlackOrWhiteListPhoneTask().execute(new String[]{name});
		} catch (Exception e) {
			 Toast.makeText(BlackWhiteListActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
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
