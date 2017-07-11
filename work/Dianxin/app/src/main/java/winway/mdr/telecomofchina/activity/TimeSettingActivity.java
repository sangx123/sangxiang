package winway.mdr.telecomofchina.activity;

import java.util.Vector;

import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.MyResources;
import winway.mdr.chinaunicom.entity.TimeSettingEntity;
import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
import winway.mdr.chinaunicom.services.TimeSettingService;
import winway.mdr.telecomofchina.activity.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.liz.cptr.TGetTimeSceneResult;
import com.liz.cptr.TGetTimeSceneRsp;
import com.liz.cptr.TTimeSceneData;
/***********************************
 * ��ʱ����������
 * @author zhaohao
 * time:2011-11-16
 * ��ع��ܽ���:����
 */
public class TimeSettingActivity extends Activity
								implements OnClickListener,OnItemClickListener{
	Button btnaddnewdata,btneditolddata,btnforback;
	ListView lvtimesettinglistview;
	TimeSettingService timeSettingService;
	SharedPreferences sharedPreferences;
	boolean IsEdit=true;
	public static boolean updateServerData = true;
	ProgressDialog progressDialog;
	  int linestatus=0;
      @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.time_setting_main);
    	initMyData();
    }
      /*****************************************************************
       * �������� :onResume
       * �� ��˵�� :��
       * ʱ         �� :2011-11
       * ����ֵ:��
       * ����˵��:ˢ����ؽ�����Ϣ
       ****************************************************************/
      @Override
    protected void onResume() {
		int updatelistviewstatus = sharedPreferences.getInt("update", 0);
		if (updatelistviewstatus == 1) {
			IsEdit = true;
			lvtimesettinglistview.setAdapter(timeSettingService.InitAdapter());
			sharedPreferences.edit().putInt("update", 0).commit();
		} else if (updatelistviewstatus == 5) {
			try {
				new GetAllTimeScenes().execute(new String[] {});
				sharedPreferences.edit().putInt("update", 0).commit();
			} catch (Exception e) {
				 Toast.makeText(TimeSettingActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
				e.printStackTrace();
			}
		}
    	  super.onResume();
    }
      /*****************************************************************
       * �������� :InitMydata
       * �� ��˵�� :��
       * ʱ         �� :2011-11
       * ����ֵ:��
       * ����˵��:��ʼ����¼�����пؼ���Ϣ
       ****************************************************************/
      public void initMyData(){
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("���ڶ�ȡ��ʱ״̬��Ϣ......");
		progressDialog.hide();
    	  sharedPreferences=getSharedPreferences("updatelistview", MODE_PRIVATE);
    	  timeSettingService=new TimeSettingService(this);
    	  btnaddnewdata=(Button) this.findViewById(R.id.btnaddnewdata);
    	  btneditolddata=(Button) this.findViewById(R.id.btneditolddata);
    	  btnforback=(Button) this.findViewById(R.id.btnforback);
    	  lvtimesettinglistview=(ListView) this.findViewById(R.id.lvtimesettinglistview);
    	  btnaddnewdata.setOnClickListener(this);
    	  btneditolddata.setOnClickListener(this);
    	  btnforback.setOnClickListener(this);
    	  linestatus=getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status", 0);
    	  if(linestatus==1){
    		  boolean bUserLogin = true; // ������ζ�ȡ�û��Ƿ��Ѿ���¼
    			if (updateServerData && bUserLogin) {
    				updateServerData = false;
    				// get data from the server.
    				try {
						new GetAllTimeScenes().execute(new String[] {});
					} catch (Exception e) {
						 Toast.makeText(TimeSettingActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show(); 
					}
    			} else {
    				lvtimesettinglistview.setAdapter(timeSettingService.InitAdapter());
    				lvtimesettinglistview.setOnItemClickListener(this);
    			}
    	  }else{
    		  Toast.makeText(TimeSettingActivity.this, "����δ��¼", 3).show();
    	  }
		
      }

	public void onClick(View v) {
		 switch (v.getId()) {
		case R.id.btnaddnewdata:
			startActivity(new Intent(this, TimeSettingDetailActivity.class));
			break;
		case R.id.btneditolddata:
			if(!IsEdit){
				   IsEdit=true;
				  lvtimesettinglistview.setAdapter(timeSettingService.getadapter(1));
			}else if(IsEdit){
				   IsEdit=false;
				  lvtimesettinglistview.setAdapter(timeSettingService.getadapter(2));
			}
			break;
		case R.id.btnforback:
		   this.finish();
		   break;
		default:
			break;
		}
	}
	  /*****************************************************************
     * �������� :onItemClick
     * �� ��˵�� :��
     * ʱ         �� :2011-11
     * ����ֵ:��
     * ����˵��:ϵͳ����(��ĵ���¼�)
     ****************************************************************/
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		  if(!IsEdit){
			  TextView tvlistitem_id_edit=(TextView) view.findViewById(R.id.tvlistitem_id_edit);
			  String update_id=tvlistitem_id_edit.getText().toString();
			  Intent intent=new Intent();
			  intent.setClass(TimeSettingActivity.this, TimeSettingDetailActivity.class);
			  intent.putExtra("update_id", update_id);
			  intent.putExtra("updataolddata", "updataolddata");
			  startActivityForResult(intent, DataResours.REQUEST_UPDATETIMESETTING);
		  }
	}

	class GetAllTimeScenes extends AsyncTask<String, Integer, TGetTimeSceneRsp> {

		@Override
		protected TGetTimeSceneRsp doInBackground(String... params) {
			return HttpDataAccess.getInstance().getTimeSceneScene();
		}

		@Override
		protected void onPostExecute(TGetTimeSceneRsp response) {
			progressDialog.hide();

			TGetTimeSceneResult.Enum result = null;
			if (response != null) {
				result = response.getResult();
			}

			Integer errorId = MyResources.getHttpErrorIndex(result);
			if (errorId != null) {
				if (errorId == R.string.http_access_need_relogin) {
					// ��ʾ��¼�Ի���
					Intent intent = new Intent(TimeSettingActivity.this, LoginActivity.class);
					startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
				} else {
					Toast.makeText(TimeSettingActivity.this, getResources().getString(errorId), 3).show();
				}
				return;
			}

			Vector<TTimeSceneData> datas = response.getData();
			timeSettingService.clearData(TimeSettingService.WRITE);
           if(datas!=null){	
        	   for (TTimeSceneData data : datas) {
   				TimeSettingEntity entity = new TimeSettingEntity();
   				entity.setByTimeSceneData(data);
   				timeSettingService.InsertNewSettingData(entity, TimeSettingService.WRITE);
   			}
           } 
			lvtimesettinglistview.setAdapter(timeSettingService.InitAdapter());
			lvtimesettinglistview.setOnItemClickListener(TimeSettingActivity.this);
		}

		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

		}

		@Override
		protected void onCancelled() {
			progressDialog.hide();
			super.onCancelled();
		}
	}

}
