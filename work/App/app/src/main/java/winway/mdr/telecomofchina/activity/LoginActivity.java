package winway.mdr.telecomofchina.activity;

import java.util.Vector;

import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.HTTPCommentTools;
import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.cpth.TSacServerUrl;
import com.liz.cptr.TUserLoginResult;
/***********************************
 * �������Ͱ���������
 * @author zhaohao
 * time:2011-11-15
 * ��ع��ܽ���:����
 * ����:�Զ���Ի���
 */
public class LoginActivity extends BaseActivity 
						   implements OnClickListener{
	Button btnlogincancel,btnlogin;
	EditText etuser_phone_number,etuser_phone_password;
	SharedPreferences saveuser_preferences;
	//���û���¼�ɹ�����б����û�����ʡ�ݵķ�������ַ
	SharedPreferences sharedPreferences_uris;
	CheckBox cbremenduserdata,cbautologin;
	String user_phone_number,user_phone_pwd;
	boolean IsAutoLogin;
	ProgressDialog progressDialog_login;
	HttpDataAccess dataAccess=null;
    String phone_status="����״̬";
    String detail="";
    HTTPCommentTools httpCommentTools;
    TextView tvforgetpwd,tvkaitong;
    public static LoginActivity activity;
    Vector<TSacServerUrl> sacServerUrls;
	@Override
protected void onCreate(Bundle savedInstanceState) {
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	super.onCreate(savedInstanceState);
	setContentView(R.layout.login_layout);
	httpCommentTools=HTTPCommentTools.getInstance();
	dataAccess=HttpDataAccess.getInstance();
	InitMydata();
	activity=this;
}
   /*****************************************************************
    * �������� :InitMydata
    * �� ��˵�� :��
    * ʱ         �� :2011-11
    * ����ֵ:��
    * ����˵��:��ʼ����¼�����пؼ���Ϣ
    ****************************************************************/
   public void InitMydata(){
	   progressDialog_login = new ProgressDialog(this);
	   progressDialog_login.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	   progressDialog_login.setMessage("���ڵ�¼ ���Ժ�...");
	   saveuser_preferences=getSharedPreferences("saveuser_preferences", MODE_PRIVATE);
	   sharedPreferences_uris=getSharedPreferences("all_haslogin_detail", MODE_PRIVATE);
	   btnlogin=(Button) this.findViewById(R.id.btnlogin);
	   btnlogincancel=(Button) this.findViewById(R.id.btnlogiccancel);
	   etuser_phone_number=(EditText) this.findViewById(R.id.etuser_phone_number);
	   etuser_phone_password=(EditText) this.findViewById(R.id.etuser_phone_password);
	   cbautologin=(CheckBox) this.findViewById(R.id.cbautologin);
	   cbremenduserdata=(CheckBox) this.findViewById(R.id.cbremenduserdata);
	   tvforgetpwd=(TextView) this.findViewById(R.id.tvforgetpwd);
	   tvkaitong=(TextView) this.findViewById(R.id.tvkaitong);
	   tvkaitong.setText(Html.fromHtml("<a><u>"+"��ͨҵ��"+"</u></a>"));
	   tvforgetpwd.setText(Html.fromHtml("<a><u>"+"ȡ������"+"</u></a>"));
	   tvforgetpwd.setOnClickListener(this);
	   tvkaitong.setOnClickListener(this);
	   cbautologin.setOnClickListener(this);
	   btnlogin.setOnClickListener(this);
	   btnlogincancel.setOnClickListener(this);
	   user_phone_number=saveuser_preferences.getString("user_phone_number", "");
	   user_phone_pwd=saveuser_preferences.getString("user_phone_pwd", "");
	   IsAutoLogin=saveuser_preferences.getBoolean("autologin", false);
	   cbautologin.setChecked(IsAutoLogin);
	   if(!TextUtils.isEmpty(user_phone_number)){
		   etuser_phone_number.setText(user_phone_number);
	   }
	   if(!TextUtils.isEmpty(user_phone_pwd)){
		   etuser_phone_password.setText(user_phone_pwd);
	   }
	   etuser_phone_number.setOnKeyListener(new OnKeyListener() {
		
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
		      if(!TextUtils.isEmpty(etuser_phone_password.getText().toString()))etuser_phone_password.setText("");
			return false;
		}
	});
   }
	@Override
	public void onClick(View v) {
		 switch (v.getId()) {
		case R.id.btnlogin:
			   try {
				if(CheckInput()){
					   InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
						imm.hideSoftInputFromWindow(btnlogin.getWindowToken(), 0);
						//���м���¼�Ƿ�����ִ��
					     new LoginTask().execute(new String[]{String.valueOf(27),etuser_phone_number.getText().toString(),etuser_phone_password.getText().toString()});
				   }
			} catch (Exception e) {
				 Toast.makeText(LoginActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
				e.printStackTrace();
			}
			break;
		case R.id.btnlogiccancel:
			this.finish();
			break;
		case R.id.cbautologin:
			 cbremenduserdata.setChecked(true);
			break;
		case R.id.tvforgetpwd:
			myDialogKaiTong(0);
			break;
		case R.id.tvkaitong:
			myDialogKaiTong(1);
			break;
		default:
			break;
		}
	}
	   /*****************************************************************
	    * �������� :CheckInput  
	    * �� ��˵�� :��
	    * ʱ         �� :2011-11
	    * ����ֵ:��
	    * ����˵��:����Ƿ���������
	    ****************************************************************/
	public boolean CheckInput(){
		if(TextUtils.isEmpty(etuser_phone_number.getText().toString())||etuser_phone_number.getText().length()<11){
			Toast.makeText(this, "�ֻ���Ϊ�ջ����ֻ���������!", 3).show();
			return false;
		}else if(TextUtils.isEmpty(etuser_phone_password.getText().toString())){
			Toast.makeText(this, "����������", 3).show();
			return false;
		}
		return true;
	}
	   /*****************************************************************
	    * �������� (��):LoginTask  
	    * �� ��˵�� :��
	    * ʱ         �� :2011-11
	    * ����ֵ:��
	    * ����˵��:�첽����(����ִ�е�¼����)
	    ****************************************************************/
	 class LoginTask extends AsyncTask<String, String, String> {  
	         
	        @SuppressWarnings("static-access")
			protected String doInBackground(String... params)
	        {
	        	try { 
					dataAccess.setHttpServerUrl(HttpDataAccess.HTTP_SERVER_URL);
					TUserLoginResult.Enum enums= dataAccess.loginRequest(params[1], params[2]);
			    	if(dataAccess.getLastError().equals(dataAccess.getLastError().SUCCESS)){
			    		if(null!=enums){
			    			if(enums.equals(TUserLoginResult.SUCCESS)){
								 String resultexecute=dataAccess.getDurationThisScene();
								    if(!resultexecute.contains("_")&&!resultexecute.contains(",")){
								    	phone_status=resultexecute;
								    }else if(resultexecute.contains(",")&&!resultexecute.contains("_")){
								    	phone_status=resultexecute.substring(0, resultexecute.indexOf(","));
								    	detail=resultexecute.substring(resultexecute.indexOf(",")+1, resultexecute.length());
								    }
							}
				               return enums.toString();
			    		}else{
			    			return "";
			    		}
			    	}else{
			    		return dataAccess.getLastError().toString();
			    	}
				
	        	} catch (Exception e) {
					 return "HTTP_EXCEPTION";
				}
	        
	        	
				
	        }  
	        protected void onCancelled() {  
	            super.onCancelled();  
	        }  
	        @SuppressWarnings("static-access")
			protected void onPostExecute(String result) 
	        {
	        	if(dataAccess.getLastError().equals(dataAccess.getLastError().SUCCESS)){
	        		if(!"failed_msisdn_not_found".equals(result)&&!TextUtils.isEmpty(result)){
		        		if("failed_not_registed".equals(result)){
			        		etuser_phone_password.setFocusable(true);
			        		etuser_phone_password.setText("");
			        		etuser_phone_number.setText("");
			        		 progressDialog_login.hide();
			        		 UserKaitongDialog();
			        		etuser_phone_number.setFocusable(true);
		        		}else if("failed_password_error".equals(result)){
			        		Toast.makeText(LoginActivity.this, "�������", 3).show();
			        		etuser_phone_password.setText("");
			        		etuser_phone_password.setFocusable(true);
			        		 progressDialog_login.hide();
			        	}else if(result.equals(TUserLoginResult.SUCCESS.toString())) {
			        		Toast.makeText(LoginActivity.this, "��¼�ɹ���", 3).show();
			        		getSharedPreferences("Isonline",MODE_PRIVATE).edit().putInt("isline_status", 1).commit();
			        		  Editor editor= saveuser_preferences.edit();
							   if(cbremenduserdata.isChecked()){
								   if(!cbautologin.isChecked()){
									   editor.putBoolean("isremenduser", true);
									   editor.putString("user_phone_number", etuser_phone_number.getText().toString());
									   editor.putString("user_phone_pwd", etuser_phone_password.getText().toString());
									   editor.putBoolean("autologin", false);
									   editor.commit();
								   }
								   if(cbautologin.isChecked()){
									   editor.putBoolean("isremenduser", true);
									   editor.putString("user_phone_number", etuser_phone_number.getText().toString());
									   editor.putString("user_phone_pwd", etuser_phone_password.getText().toString());
									   editor.putBoolean("autologin", true);
									   editor.commit();
								   }  
							   }else {
								      editor.clear().commit();
							   }
							    progressDialog_login.hide();
							    Intent intent=new Intent();
							    intent.putExtra("phone_number_user", etuser_phone_number.getText().toString());
							    intent.putExtra("phone_status", phone_status);
							    intent.putExtra("detail", detail);
							    setResult(DataResours.RESULT_LOGIN_CODE, intent);
						        LoginActivity.this.finish();
			        	}
		        	} 
	        		//������ĺ��뷵��failed_msisdn_not_found��ʱ��(δ����ȷ�Ļ�ȡ�ֻ�����Ĺ�����)������������ϵ�������Ϣ���û�����ѡ��
	        		if("failed_msisdn_not_found".equals(result)&&!TextUtils.isEmpty(result))
		        	{
		        		progressDialog_login.hide();
		        	}
	        		if("failed_msisdn_not_support".equals(result))
		        	{
	        			etuser_phone_number.setText("");
	        			etuser_phone_password.setText("");
		        		Toast.makeText(LoginActivity.this, "�Բ���,���ĺ��벻֧�ִ�ҵ��!", 3).show();
		        		progressDialog_login.hide();
		        	}
	        		if(TextUtils.isEmpty(result))
		        	{
		        		Toast.makeText(LoginActivity.this, "��¼ʧ��,������", 3).show();
		        		progressDialog_login.hide();
		        	} 
	        	} 
	        	else{
		        		     Toast.makeText(LoginActivity.this,httpCommentTools.GetEcption(dataAccess.getLastError().toString()), 3).show();
			        	     progressDialog_login.hide();
		        	}
	        	}
	        
		        protected void onPreExecute() {  
		        	progressDialog_login.show();
		        }  
	        }  
}
