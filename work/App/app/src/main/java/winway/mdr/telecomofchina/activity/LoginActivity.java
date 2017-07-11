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
 * 黑名单和白名单界面
 * @author zhaohao
 * time:2011-11-15
 * 相关功能介绍:暂无
 * 其他:自定义对话框
 */
public class LoginActivity extends BaseActivity 
						   implements OnClickListener{
	Button btnlogincancel,btnlogin;
	EditText etuser_phone_number,etuser_phone_password;
	SharedPreferences saveuser_preferences;
	//当用户登录成功后进行保存用户所在省份的服务器地址
	SharedPreferences sharedPreferences_uris;
	CheckBox cbremenduserdata,cbautologin;
	String user_phone_number,user_phone_pwd;
	boolean IsAutoLogin;
	ProgressDialog progressDialog_login;
	HttpDataAccess dataAccess=null;
    String phone_status="正常状态";
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
    * 函数名称 :InitMydata
    * 参 数说明 :无
    * 时         间 :2011-11
    * 返回值:无
    * 功能说明:初始化登录界面中控件信息
    ****************************************************************/
   public void InitMydata(){
	   progressDialog_login = new ProgressDialog(this);
	   progressDialog_login.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	   progressDialog_login.setMessage("正在登录 请稍候...");
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
	   tvkaitong.setText(Html.fromHtml("<a><u>"+"开通业务"+"</u></a>"));
	   tvforgetpwd.setText(Html.fromHtml("<a><u>"+"取回密码"+"</u></a>"));
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
						//进行检查登录是否正在执行
					     new LoginTask().execute(new String[]{String.valueOf(27),etuser_phone_number.getText().toString(),etuser_phone_password.getText().toString()});
				   }
			} catch (Exception e) {
				 Toast.makeText(LoginActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
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
	    * 函数名称 :CheckInput  
	    * 参 数说明 :无
	    * 时         间 :2011-11
	    * 返回值:无
	    * 功能说明:检查是否输入完整
	    ****************************************************************/
	public boolean CheckInput(){
		if(TextUtils.isEmpty(etuser_phone_number.getText().toString())||etuser_phone_number.getText().length()<11){
			Toast.makeText(this, "手机号为空或者手机号码有误!", 3).show();
			return false;
		}else if(TextUtils.isEmpty(etuser_phone_password.getText().toString())){
			Toast.makeText(this, "请输入密码", 3).show();
			return false;
		}
		return true;
	}
	   /*****************************************************************
	    * 函数名称 (类):LoginTask  
	    * 参 数说明 :无
	    * 时         间 :2011-11
	    * 返回值:无
	    * 功能说明:异步任务(用来执行登录操作)
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
			        		Toast.makeText(LoginActivity.this, "密码错误！", 3).show();
			        		etuser_phone_password.setText("");
			        		etuser_phone_password.setFocusable(true);
			        		 progressDialog_login.hide();
			        	}else if(result.equals(TUserLoginResult.SUCCESS.toString())) {
			        		Toast.makeText(LoginActivity.this, "登录成功！", 3).show();
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
	        		//若请求的号码返回failed_msisdn_not_found的时候(未能正确的获取手机号码的归属地)并处理服务器上的数据信息让用户自行选择
	        		if("failed_msisdn_not_found".equals(result)&&!TextUtils.isEmpty(result))
		        	{
		        		progressDialog_login.hide();
		        	}
	        		if("failed_msisdn_not_support".equals(result))
		        	{
	        			etuser_phone_number.setText("");
	        			etuser_phone_password.setText("");
		        		Toast.makeText(LoginActivity.this, "对不起,您的号码不支持此业务!", 3).show();
		        		progressDialog_login.hide();
		        	}
	        		if(TextUtils.isEmpty(result))
		        	{
		        		Toast.makeText(LoginActivity.this, "登录失败,请重试", 3).show();
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
