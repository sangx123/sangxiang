package winway.mdr.telecomofchina.activity;

import winway.mdr.adapter.LxrAdapter;
import winway.mdr.chinaunicom.comm.DataResours;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
/************************************************
 * �绰��ϵ���б�
 * @author zhaohao
 * ʱ��:2011-11
 * ����:���ڼ�����ϵ�˽����б�
 */
@TargetApi(Build.VERSION_CODES.ECLAIR)
public class PhoneListActivity extends Activity implements OnItemClickListener {
	ListView lvphonelist;
	private LxrAdapter lxrAdapter=null;
	public boolean isExecute=true;
   @Override
protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	super.onCreate(savedInstanceState);
	setContentView(R.layout.phonelist);
	lxrAdapter=new LxrAdapter(this);
	lvphonelist=(ListView) this.findViewById(R.id.lvphonelist);
	lvphonelist.setAdapter(lxrAdapter);
	lxrAdapter.setList(AddBlackWhiteActivity.lxrEntities,true);
	lvphonelist.setOnItemClickListener(this);
	
  }
/**************************************
 * ϵͳ����   ListView����¼�
 * ����:���ڵõ��û��û�����Ϣ
 */
@Override
public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	TextView tvusername=(TextView) view.findViewById(R.id.tvusername);
	TextView tvuserphone=(TextView) view.findViewById(R.id.tvuseruserphonenumber);
	Intent intent=new Intent();
	intent.putExtra("user_name", tvusername.getText().toString());
	intent.putExtra("user_phone_number", tvuserphone.getText().toString());
	setResult(DataResours.RETURNNEW, intent);
	this.finish();
	
}
@SuppressWarnings("static-access")
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	if(keyCode==event.KEYCODE_BACK){
		isExecute=false;
		System.gc();
		finish();
	}
	return super.onKeyDown(keyCode, event);
}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
