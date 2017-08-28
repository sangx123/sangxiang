package winway.mdr.chinaunicom.activity;

import java.util.Vector;

import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.HTTPCommentTools;
import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.liz.cptr.TGetMultipleOptionsResult;
import com.liz.cptr.TGetMultipleOptionsRsp;
import com.liz.cptr.TOptionPair;
import com.liz.cptr.TSetMultipleOptionsResult;
/***********************************
 * 配置设置
 * @author zhaohao
 * time:2011-11-18
 * 相关功能介绍:暂无
 */
public class SettingActivity extends Activity 
						    implements OnClickListener{
	Button btnsetting_back;
	ToggleButton 
		tbtnsetting_black_function,
		tbtnsetting_white_function,
		tbtnsetting_fjwr_function,
		tbtnsetting_qhdr_function,
		tbtnsetting_black_warn_function,
		tbtnsetting_white_warn_function,
		tbtnsetting_hfzc_warn_function,
		tbtnsetting_dssz_success_warn_function,
		tbtnsetting_dssz_end_warn_function,
		tbtnsetting_zczt_isok_function;
	    HttpDataAccess dataAccess=null;
	    ProgressDialog dialog=null;
	    boolean BlackEnable=false,WhiteEnable=false,BlackZcztEable=false;
	    boolean oneenable=false,twoenable=false,threeable=false,fourable=false,fiveable=false,sixable=false,sevenable=false;
	    boolean BlackEnabledetail=false,WhiteEnabledetail=false,BlackZcztEabledetail=false;
	    boolean oneenabledetail=false,twoenabledetail=false,threeabledetail=false,fourabledetail=false,fiveabledetail=false,sixabledetail=false,sevenabledetail=false;
       Vector<TOptionPair> lists=null;
       HTTPCommentTools httpCommentTools=null;
       int linestatus=0;
	    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.setting_main);
    	dataAccess=HttpDataAccess.getInstance();
    	dialog=new ProgressDialog(this);
    	dialog.setMessage("正在配置,请稍等...");
    	linestatus=getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status", 0);
    	System.out.println("linestatuslinestatuslinestatuslinestatus---->>>"+linestatus);
    	 httpCommentTools=HTTPCommentTools.getInstance();
    	if(linestatus==1){
	    	  try {
				new SettingTask().execute(new String[]{"getallstatus",""});
			} catch (Exception e) {
				 Toast.makeText(SettingActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
				e.printStackTrace();
			}
    	}
    	else{
    		Toast.makeText(SettingActivity.this, "您尚未登录", 3).show();
    		Intent intent_login_detail=new  Intent(this, LoginActivity.class);
	        startActivityForResult(intent_login_detail, DataResours.REQUEST_LOGIN_CODE);
    	}
    	
    	InitMydata();
    }
	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    	 if(requestCode==DataResours.REQUEST_LOGIN_CODE&&resultCode==DataResours.RESULT_LOGIN_CODE){
	    		 if(getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status", 0)==1)
					try {
						new SettingTask().execute(new String[]{"getallstatus",""});
					} catch (Exception e) {
						 Toast.makeText(SettingActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
						e.printStackTrace();
					}
	    	 }
	    	super.onActivityResult(requestCode, resultCode, data);
	    }
    /*****************************************************************
     * 函数名称 :InitMydata  
     * 参 数说明 :无
     * 时         间 :2011-11
     * 返回值:无
     * 功能说明:初始化设置界面中控件信息
     ****************************************************************/
    public void InitMydata(){
    	btnsetting_back=(Button) this.findViewById(R.id.btnsetting_back);
    	tbtnsetting_black_function=(ToggleButton) this.findViewById(R.id.tbtnsetting_black_function);
		tbtnsetting_white_function=(ToggleButton) this.findViewById(R.id.tbtnsetting_white_function);
		tbtnsetting_fjwr_function=(ToggleButton) this.findViewById(R.id.tbtnsetting_fjwr_function);
		tbtnsetting_qhdr_function=(ToggleButton) this.findViewById(R.id.tbtnsetting_qhdr_function);
		tbtnsetting_black_warn_function=(ToggleButton) this.findViewById(R.id.tbtnsetting_black_warn_function);
		tbtnsetting_white_warn_function=(ToggleButton) this.findViewById(R.id.tbtnsetting_white_warn_function);
		tbtnsetting_hfzc_warn_function=(ToggleButton) this.findViewById(R.id.tbtnsetting_hfzc_warn_function);
		tbtnsetting_dssz_success_warn_function=(ToggleButton) this.findViewById(R.id.tbtnsetting_dssz_success_warn_function);
		tbtnsetting_dssz_end_warn_function=(ToggleButton) this.findViewById(R.id.tbtnsetting_dssz_end_warn_function);
		tbtnsetting_zczt_isok_function=(ToggleButton) this.findViewById(R.id.tbtnsetting_zczt_isok_function);
		btnsetting_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 SettingActivity.this.finish();
			}
		});
    	tbtnsetting_black_function.setOnClickListener(this);
		tbtnsetting_white_function.setOnClickListener(this);
		tbtnsetting_fjwr_function.setOnClickListener(this);
		tbtnsetting_qhdr_function.setOnClickListener(this);
		tbtnsetting_black_warn_function.setOnClickListener(this);
		tbtnsetting_white_warn_function.setOnClickListener(this);
		tbtnsetting_hfzc_warn_function.setOnClickListener(this);
		tbtnsetting_dssz_success_warn_function.setOnClickListener(this);
		tbtnsetting_dssz_end_warn_function.setOnClickListener(this);
		tbtnsetting_zczt_isok_function.setOnClickListener(this);
    }
    /*****************************************************************
     * 函数名称 :onClick  
     * 参 数说明 :无
     * 时         间 :2011-11
     * 返回值:无
     * 功能说明:系统方法用于标识相关的状态
     ****************************************************************/
	@Override
	public void onClick(View v) {
		try {
			if(linestatus==1){
				  switch (v.getId()) {
					case R.id.tbtnsetting_black_function:
						if(tbtnsetting_black_function.isChecked())
						  new SettingTask().execute(new String[]{"setblack","on"});
						else
					      new SettingTask().execute(new String[]{"setblack","off"});
						break;
					case R.id.tbtnsetting_white_function:
						if(tbtnsetting_white_function.isChecked())
							  new SettingTask().execute(new String[]{"setwhite","on"});
							else
						      new SettingTask().execute(new String[]{"setwhite","off"});
						break;
					case R.id.tbtnsetting_fjwr_function:
						if(tbtnsetting_fjwr_function.isChecked())
						 new SettingTask().execute(new String[]{"one","on"});
						else
						 new SettingTask().execute(new String[]{"one","off"});
						break;
					case R.id.tbtnsetting_qhdr_function:
						if(tbtnsetting_qhdr_function.isChecked())
							 new SettingTask().execute(new String[]{"two","on"});
							else
							 new SettingTask().execute(new String[]{"two","off"});
						break;
					case R.id.tbtnsetting_black_warn_function:
						if(tbtnsetting_black_warn_function.isChecked())
							 new SettingTask().execute(new String[]{"three","on"});
							else
							 new SettingTask().execute(new String[]{"three","off"});
						break;
					case R.id.tbtnsetting_white_warn_function:
						if(tbtnsetting_white_warn_function.isChecked())
							 new SettingTask().execute(new String[]{"four","on"});
							else
							 new SettingTask().execute(new String[]{"four","off"});
						break;
					case R.id.tbtnsetting_hfzc_warn_function:
						if(tbtnsetting_hfzc_warn_function.isChecked())
							 new SettingTask().execute(new String[]{"five","on"});
							else
							 new SettingTask().execute(new String[]{"five","off"});
						break;
					case R.id.tbtnsetting_dssz_success_warn_function:
						if(tbtnsetting_dssz_success_warn_function.isChecked())
							 new SettingTask().execute(new String[]{"six","on"});
							else
							 new SettingTask().execute(new String[]{"six","off"});
						break;
					case R.id.tbtnsetting_dssz_end_warn_function:
						if(tbtnsetting_dssz_end_warn_function.isChecked())
							 new SettingTask().execute(new String[]{"seven","on"});
							else
							 new SettingTask().execute(new String[]{"seven","off"});
						break;
					case R.id.tbtnsetting_zczt_isok_function:
						if(tbtnsetting_zczt_isok_function.isChecked())
							 new SettingTask().execute(new String[]{"eight","on"});
							else
							 new SettingTask().execute(new String[]{"eight","off"});
						break;
					default:
						break;
				   }
			}else{
				 Toast.makeText(SettingActivity.this, "您尚未登录", 3).show();
			}
		} catch (Exception e) {
			 Toast.makeText(SettingActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
			e.printStackTrace();
		}
	
	}
	 class SettingTask extends AsyncTask<String, String, String> {  
         
			protected String doInBackground(String... params)
	        { 
				int temp="on".equals(params[1])?1:0;
				int _id=0;
				if("getallstatus".equals(params[0])){

					InitMyStatus();
					if(GetLastErrorStatus()){
						return "status";
					}
					return dataAccess.getLastError().toString();
				}else{
					 if("setblack".equals(params[0]))_id=11;
					 else if("setwhite".equals(params[0]))_id=12;
					 else if("one".equals(params[0]))_id=1;
					 else if("two".equals(params[0]))_id=2;
					 else if("three".equals(params[0]))_id=3;
					 else if("four".equals(params[0]))_id=6;
					 else if("five".equals(params[0]))_id=4;
					 else if("six".equals(params[0]))_id=5;
					 else if("seven".equals(params[0]))_id=7;
					 else if("eight".equals(params[0]))_id=15;
					TSetMultipleOptionsResult.Enum result=dataAccess.setMultipleOptions(_id, temp);
					if(result==null)return dataAccess.getLastError().toString();
					return result.toString();
				}
	        }  
	        protected void onCancelled() {  
	            super.onCancelled();  
	        }  
			@SuppressWarnings("static-access")
			protected void onPostExecute(String result) 
	        {
				dialog.hide();
				if(GetLastErrorStatus()){
					 if(result.equals(TSetMultipleOptionsResult.SUCCESS.toString())){
			        	   Toast.makeText(SettingActivity.this, "设置成功", 3).show();
			           }else if("status".equals(result)){
			        	    tbtnsetting_black_function.setEnabled(BlackEnabledetail);
				       		tbtnsetting_white_function.setEnabled(WhiteEnabledetail);
				       		tbtnsetting_fjwr_function.setEnabled(oneenabledetail);
				       		tbtnsetting_qhdr_function.setEnabled(twoenabledetail);
				       		tbtnsetting_black_warn_function.setEnabled(threeabledetail);
				       		tbtnsetting_white_warn_function.setEnabled(fourabledetail);
				       		tbtnsetting_hfzc_warn_function.setEnabled(fiveabledetail);
				       		tbtnsetting_dssz_success_warn_function.setEnabled(sixabledetail);
				       		tbtnsetting_dssz_end_warn_function.setEnabled(sevenabledetail);
				       		tbtnsetting_zczt_isok_function.setEnabled(BlackZcztEabledetail);
				        	tbtnsetting_black_function.setChecked(BlackEnable);
				       		tbtnsetting_white_function.setChecked(WhiteEnable);
				       		tbtnsetting_fjwr_function.setChecked(oneenable);
				       		tbtnsetting_qhdr_function.setChecked(twoenable);
				       		tbtnsetting_black_warn_function.setChecked(threeable);
				       		tbtnsetting_white_warn_function.setChecked(fourable);
				       		tbtnsetting_hfzc_warn_function.setChecked(fiveable);
				       		tbtnsetting_dssz_success_warn_function.setChecked(sixable);
				       		tbtnsetting_dssz_end_warn_function.setChecked(sevenable);
				       		tbtnsetting_zczt_isok_function.setChecked(BlackZcztEable);
			           }
				}else{
					if(dataAccess.getLastError().HTTP_NEED_RELOGIN.toString().equals(dataAccess.getLastError().toString())){
        			    Intent intent=new  Intent(SettingActivity.this, LoginActivity.class);
                        startActivityForResult(intent,111);
        		}
					Toast.makeText(SettingActivity.this, httpCommentTools.GetEcption(result), 3).show();
				}
	        }  
	        protected void onPreExecute() { 
	        	dialog.show();
	        }  
	        protected void onProgressUpdate(Integer... values) { 
	        	 
	        }  
	     }  
		public void InitMyStatus() {
			TGetMultipleOptionsRsp response = dataAccess.getMultipleOptions();
			if (response == null) {
				return;
			}

			if (!response.getResult().equals(TGetMultipleOptionsResult.SUCCESS)) {
				return;
			}
		  lists=response.getOptions();
		  System.out.println("总长度----------------"+lists.size());
		  if(lists!=null){
			  for (int i = 0; i < lists.size(); i++) {
				  if(lists.get(i).getId()==1)oneenabledetail=true;
				  if(lists.get(i).getId()==2)twoenabledetail=true;
				  if(lists.get(i).getId()==3)threeabledetail=true;
				  if(lists.get(i).getId()==4)fourabledetail=true;
				  if(lists.get(i).getId()==5)fiveabledetail=true;
				  if(lists.get(i).getId()==6)sixabledetail=true;
				  if(lists.get(i).getId()==7)sevenabledetail=true;
				  if(lists.get(i).getId()==11)BlackEnabledetail=true;
				  if(lists.get(i).getId()==12)WhiteEnabledetail=true;
				  if(lists.get(i).getId()==15)BlackZcztEabledetail=true;
				  if(lists.get(i).getId()==1&&lists.get(i).getValue()==1)oneenable=true;
				  if(lists.get(i).getId()==2&&lists.get(i).getValue()==1)twoenable=true;
				  if(lists.get(i).getId()==3&&lists.get(i).getValue()==1)threeable=true;
				  if(lists.get(i).getId()==4&&lists.get(i).getValue()==1)fourable=true;
				  if(lists.get(i).getId()==5&&lists.get(i).getValue()==1)fiveable=true;
				  if(lists.get(i).getId()==6&&lists.get(i).getValue()==1)sixable=true;
				  if(lists.get(i).getId()==7&&lists.get(i).getValue()==1)sevenable=true;
				  if(lists.get(i).getId()==11&&lists.get(i).getValue()==1)BlackEnable=true;
				  if(lists.get(i).getId()==12&&lists.get(i).getValue()==1)WhiteEnable=true;
				  if(lists.get(i).getId()==15&&lists.get(i).getValue()==1)BlackZcztEable=true;
			   }
			}
		  }
 @SuppressWarnings("static-access")
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	 if(keyCode==event.KEYCODE_BACK&&event.getRepeatCount()==0){
		 lists=null;
		 System.gc();
	 }
	return super.onKeyDown(keyCode, event);
}
		 @SuppressWarnings("static-access")
			public boolean GetLastErrorStatus(){
				 if(dataAccess.getLastError().equals(dataAccess.getLastError().SUCCESS))return true;
				 else return false;
			 }
}
