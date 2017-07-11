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
 * �������������
 * @author zhaohao
 * ʱ��:2011-11
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
	   * �������� :InitMydata  
	   * �� ��˵�� :��
	   * ʱ         �� :2011-11
	   * ����ֵ:��
	   * ����˵��:��ʼ����ص�����
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
	   * �������� :onItemClick  
	   * �� ��˵�� :AdapterView<?> parent, View view, int position, long id
	   * ʱ         �� :2011-11
	   * ����ֵ:��
	   * ����˵��:ListView����¼�
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
	   * �������� :onResume  
	   * �� ��˵�� :��
	   * ʱ         �� :2011-11
	   * ����ֵ:��
	   * ����˵��:�ص������ж��Ƿ�ˢ��
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
