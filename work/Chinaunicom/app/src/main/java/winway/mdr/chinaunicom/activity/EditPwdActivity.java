package winway.mdr.chinaunicom.activity;

import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.cptr.TEditPasswordResult;

/***********************************
 * 修改密码
 * @author zhaohao
 * time:2011-11-18
 * 相关功能介绍:暂无
 */
public class EditPwdActivity extends Activity 
                                  implements OnClickListener{
	TextView tvaddblack_white_title;
	Button 	btnaddblack_white_back ,btnaddblack_white_save;
	EditText et_pwd,et_pwd_again,et_pwd_again_two;
	ProgressDialog progressDialog=null;
	HttpDataAccess dataAccess=HttpDataAccess.getInstance();
   @Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.editpwd_activity);
	progressDialog = new ProgressDialog(this);
	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	progressDialog.setMessage("正在设置,请稍等...");
	InitMydata();
}
   /*****************************************************************
    * 函数名称 :InitMydata
    * 参 数说明 :无
    * 时         间 :2011-11
    * 返回值:无
    * 功能说明:初始化控件信息以及对修改时数据的恢复
    ****************************************************************/
  public void InitMydata(){
	   et_pwd_again_two=(EditText) findViewById(R.id.et_pwd_again_two);
	   tvaddblack_white_title=(TextView) this.findViewById(R.id.tvaddblack_white_title);
	   btnaddblack_white_back=(Button) this.findViewById(R.id.btnaddblack_white_back);
	   btnaddblack_white_save=(Button) this.findViewById(R.id.btnaddblack_white_save);
	   et_pwd=(EditText) this.findViewById(R.id.et_pwd);
	   et_pwd_again=(EditText) this.findViewById(R.id.et_pwd_again);
	   tvaddblack_white_title.setText("修改密码");
	   btnaddblack_white_back.setOnClickListener(this);
	   btnaddblack_white_save.setOnClickListener(this);
   }
  /**************************************************
   * 点击事件，相关操作有添加和修改相关操作
   */
	@Override
	public void onClick(View v) {
		 try {
			switch (v.getId()) {
			case R.id.btnaddblack_white_back:
				 this.finish();
				break;
			case R.id.btnaddblack_white_save:
				if(CheckInpue()){
					new EditPwdAsync().execute();
				}
				break;
			}
		} catch (Exception e) {
			 Toast.makeText(EditPwdActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
			e.printStackTrace();
		}
	}
   /*****************************************************************
    * 函数名称 :CheckInpue
    * 参 数说明 :无
    * 时         间 :2011-11
    * 返回值:无
    * 功能说明:检查相关的数据是否为空
    ****************************************************************/
	public boolean CheckInpue(){
		 if(TextUtils.isEmpty(et_pwd.getText().toString().trim())){
			Toast.makeText(this, "请输入您的密码", 3).show();
			return false;
		}else{
			if(!(et_pwd.getText().length()>=6&&et_pwd.getText().length()<=12)){
				Toast.makeText(this, "密码是6-12位的数字", 3).show();
				return false;
			}
		}
		 if(TextUtils.isEmpty(et_pwd_again.getText().toString().trim())){
			Toast.makeText(this, "请输入新密码", 3).show();
			return false;
		}else{
			if(!(et_pwd_again.getText().length()>=6&&et_pwd_again.getText().length()<=12)){
				Toast.makeText(this, "密码是6-12位的数字", 3).show();
				return false;
			}
		}
		 if(TextUtils.isEmpty(et_pwd_again_two.getText().toString().trim())){
			    Toast.makeText(this, "请再次输入密码", 3).show();
				return false;
		 }else{
			 if(!et_pwd_again.getText().toString().trim().equals(et_pwd_again_two.getText().toString().trim())){
				 Toast.makeText(this, "两次输入的密码不一致", 3).show();
				 return false;
			 }
		 }
		return true;
	}
 
	/*******************************************
	 * 退出的时候将相关的数据集合清空掉
	 */
	@SuppressWarnings("static-access")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if(keyCode==event.KEYCODE_BACK&&event.getRepeatCount()==0){
			 System.gc();
		 }
		return super.onKeyDown(keyCode, event);
	}
	
		/**
		 * 修改密码
		 * @author xinhua
		 */
	 class EditPwdAsync extends AsyncTask<String, String, String> {  
			protected String doInBackground(String... params)
	        { 
				return dataAccess.editpassword(et_pwd.getText().toString(), et_pwd_again.getText().toString());
	        }  
	        protected void onCancelled() {  
	            super.onCancelled();  
	        }  
			protected void onPostExecute(String result) 
	        { 
				progressDialog.hide();
				if(TEditPasswordResult.SUCCESS.toString().equals(result)){
					Toast.makeText(EditPwdActivity.this, "密码修改成功!", 3).show();
					getSharedPreferences("saveuser_preferences", MODE_PRIVATE).edit().putString("user_phone_pwd", et_pwd_again.getText().toString()).commit();
					EditPwdActivity.this.finish();
				}else if(TEditPasswordResult.FAILED_OLD_PASSWORD_ERROR_STRING.toString().equals(result)){
					Toast.makeText(EditPwdActivity.this, "旧密码错误", 3).show();
				}
	        }  
	        protected void onPreExecute() { 
	        	progressDialog.show();
	        }  
	        protected void onProgressUpdate(Integer... values) { 
	        }  
	     } 
}
