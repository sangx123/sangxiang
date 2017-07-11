package winway.mdr.telecomofchina.activity;

import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.services.QuickSetupServices;
import winway.mdr.telecomofchina.activity.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
/*************************************
 * 快捷设置主界面
 * @author zhaohao
 * 时间:2011-11
 */
public class QuickSetupActivity extends Activity  
							   implements OnClickListener ,OnItemClickListener{
	Button btnquicksetupforback;
	ListView lvquicksetuplistview;
	QuickSetupServices quickSetupServices=null;
	SharedPreferences sharedPreferences;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.quicksetup_main);
    	InitMydata();
    }
	 /*****************************************************************
	   * 函数名称 :InitMydata  
	   * 参 数说明 :无
	   * 时         间 :2011-11
	   * 返回值:无
	   * 功能说明:初始化相关的数据
	   ****************************************************************/
     public void InitMydata(){
    	 sharedPreferences=getSharedPreferences("update_quick", MODE_PRIVATE);
    	 quickSetupServices=new QuickSetupServices(this);
    	 btnquicksetupforback=(Button) this.findViewById(R.id.btnquicksetupforback);
    	 lvquicksetuplistview=(ListView) this.findViewById(R.id.lvquicksetuplistview);
    	 lvquicksetuplistview.setAdapter(quickSetupServices.GetSimepleAdapter());
    	 btnquicksetupforback.setOnClickListener(this);
    	 lvquicksetuplistview.setOnItemClickListener(this);
     }
     
	@Override
	public void onClick(View v) {
	    switch (v.getId()) {
		case R.id.btnquicksetupforback:
			  this.finish();
			break;
		default:
			break;
		}
		
	}
	 /*****************************************************************
	   * 函数名称 :onItemClick  
	   * 参 数说明 :AdapterView<?> parent, View view, int position, long id
	   * 时         间 :2011-11
	   * 返回值:无
	   * 功能说明:ListView点击事件
	   ****************************************************************/
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		 String quicksetup_edit_id=((TextView)view.findViewById(R.id.tvquicksetup_id)).getText().toString();
		 Intent intent=new Intent();
		 intent.setClass(QuickSetupActivity.this, TimeSettingDetailActivity.class);
		  intent.putExtra("quicksetup_edit_id", quicksetup_edit_id);
		  intent.putExtra("quicksetup_status", "quicksetup_status");
		  startActivityForResult(intent, DataResours.REQUEST_QUICKSETUP_UPDATE);
		
	}
	 /*****************************************************************
	   * 函数名称 :onResume  
	   * 参 数说明 :无
	   * 时         间 :2011-11
	   * 返回值:无
	   * 功能说明:回调函数判断是否刷新
	   ****************************************************************/
	@Override
	protected void onResume() {
		int updatequick_status=sharedPreferences.getInt("updatequick_status",0);
		if(updatequick_status==1){
			lvquicksetuplistview.setAdapter(quickSetupServices.GetSimepleAdapter());
			sharedPreferences.edit().putInt("updatequick_status", 0).commit();
		}
		super.onResume();
	}
}
