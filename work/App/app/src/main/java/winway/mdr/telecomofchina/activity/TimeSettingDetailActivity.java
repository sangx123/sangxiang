package winway.mdr.telecomofchina.activity;

import java.util.ArrayList;
import java.util.Calendar;

import winway.mdr.chinaunicom.comm.ArrayUnits;
import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.MyResources;
import winway.mdr.chinaunicom.comm.NetWorkConnectionUtil;
import winway.mdr.chinaunicom.entity.TimeSettingEntity;
import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
import winway.mdr.chinaunicom.services.QuickSetupServices;
import winway.mdr.chinaunicom.services.TimeSettingService;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.cptr.TAddTimeSceneResult;
import com.liz.cptr.TReplaceTimeSceneResult;
import com.liz.cptr.TTimeSceneData;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
/***********************************
 * ��ʱ���õ���ϸ�����Լ���ʱ�޸Ľ���
 * @author zhaohao
 * time:2011-11-17
 * ��ع��ܽ���:����
 */
public class TimeSettingDetailActivity extends FragmentActivity
                                      implements OnClickListener,TimePickerDialog.OnTimeSetListener{
	 public static final String TIMEPICKER_TAG = "timepicker";
	RelativeLayout rlrepeat_day,rlrepeat_begintime,rlrepeat_endtime,rlrepeat_status,rlrepeat_scene;
	TextView tvresultrepeat,tvresultbegintime,tvresultendtime,tvresultstatus,tvresultscene;
    Button btndetailnewback,btndetailnewsave;
	String temptext="";
	int choosestatus=3;
	String update_id,updataolddata;
    Calendar calCalendar = null;
    TimeSettingEntity settingEntity;
    TimeSettingService timeSettingService;
    QuickSetupServices quickSetupServices;
    CheckBox cbeverydays,cbonlyone,cbfromtoend;
    LinearLayout llfromtoend_layout;
    Spinner spfromdata,spenddata;
    String detaukrepeat="";
    int[] arrayupdata;
    String quicksetup_id;
	String quicksetup_status;
	String ischeck_values="";
	RelativeLayout rlkeeptime,relbiemingtop;
	LinearLayout llshowdesplay_timesetting;
	EditText etkeeptime_hour,etkeeptime_mins;
    ArrayList<String> collections=new ArrayList<String>();
	ProgressDialog progressDialog;
	int islinestatus=0;
	TextView tvtitle_name;
	String quickstatus_op="";
	EditText etbieming;
	private TimePickerDialog timePickerDialog =null;
	private int begin_end_status=0;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.timesettingdetail_main);
    	
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.hide();
		islinestatus=getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status", 0);
    	initMyData();
    	 Intent intent=getIntent();
    	 quicksetup_id=intent.getStringExtra("quicksetup_edit_id");
    	 quicksetup_status=intent.getStringExtra("quicksetup_status");
    	 update_id=intent.getStringExtra("update_id");
	     updataolddata=intent.getStringExtra("updataolddata");
	     quickstatus_op=intent.getStringExtra("quickstatus_op");
    	if(!TextUtils.isEmpty(quicksetup_id)&&"quicksetup_status".equals(quicksetup_status)){
    		rlkeeptime.setVisibility(View.VISIBLE);
    		relbiemingtop.setVisibility(View.VISIBLE);
    		llshowdesplay_timesetting.setVisibility(View.GONE);
    		tvtitle_name.setText("�������");
    		RestorationQuicksetupMyData(quicksetup_id,1);
    	}else if(!TextUtils.isEmpty(update_id)&&"updataolddata".equals(updataolddata)){
    		RestorationQuicksetupMyData(update_id,2);
    	}else if("quicksetup_status".equals(quicksetup_status)&&quickstatus_op.equals("add")){
    		rlkeeptime.setVisibility(View.VISIBLE);
    		llshowdesplay_timesetting.setVisibility(View.GONE);
    		relbiemingtop.setVisibility(View.VISIBLE);
    		tvtitle_name.setText("�������");
    	}
     timePickerDialog = TimePickerDialog.newInstance(this, 0 ,0, true, false);
   	 if (savedInstanceState != null) {
            TimePickerDialog tpd = (TimePickerDialog) getSupportFragmentManager().findFragmentByTag(TIMEPICKER_TAG);
            if (tpd != null) {
                tpd.setOnTimeSetListener(this);
            }
        }
    }
	  /*****************************************************************
      * �������� :initMyData
      * �� ��˵�� :��
      * ʱ         �� :2011-11
      * ����ֵ:��
      * ����˵��:��ʼ����ص�������Ϣ
      ****************************************************************/
     public void initMyData(){
    	 timeSettingService=new TimeSettingService(this);
    	 quickSetupServices=new QuickSetupServices(this);
    	 settingEntity=new TimeSettingEntity();
    	 calCalendar = Calendar.getInstance();
    	 rlkeeptime=(RelativeLayout) this.findViewById(R.id.rlkeeptime);
    	 relbiemingtop=(RelativeLayout) this.findViewById(R.id.relbiemingtop);
    	 llshowdesplay_timesetting=(LinearLayout) this.findViewById(R.id.llshowdesplay_timesetting);
    	 rlrepeat_day=(RelativeLayout) this.findViewById(R.id.rlrepeat_day);
    	 rlrepeat_begintime=(RelativeLayout) this.findViewById(R.id.rlrepeat_begintime);
    	 rlrepeat_endtime=(RelativeLayout) this.findViewById(R.id.rlrepeat_endtime);
    	 rlrepeat_status=(RelativeLayout) this.findViewById(R.id.rlrepeat_status);
    	 rlrepeat_scene=(RelativeLayout) this.findViewById(R.id.rlrepeat_scene);
    	 tvresultrepeat=(TextView) this.findViewById(R.id.tvrepeatdetai);
    	 tvresultbegintime=(TextView) this.findViewById(R.id.tvresultbegintime);
    	 tvresultendtime=(TextView) this.findViewById(R.id.tvresultendtime);
    	 tvresultstatus=(TextView) this.findViewById(R.id.tvresultstatus);
    	 tvresultscene=(TextView) this.findViewById(R.id.tvresultscene);
    	 btndetailnewback=(Button) this.findViewById(R.id.btndetailnewback);
    	 btndetailnewsave=(Button) this.findViewById(R.id.btndetailnewsave);
    	 etkeeptime_hour=(EditText) this.findViewById(R.id.etkeeptime_hour);
    	 etkeeptime_mins=(EditText) this.findViewById(R.id.etkeeptime_mins);
    	 tvtitle_name=(TextView) this.findViewById(R.id.tvtitle_name);
    	 etbieming=(EditText) this.findViewById(R.id.etbieming);
    	 btndetailnewback.setOnClickListener(this);
    	 btndetailnewsave.setOnClickListener(this);
    	 rlrepeat_day.setOnClickListener(this);
    	 rlrepeat_begintime.setOnClickListener(this);
    	 rlrepeat_endtime.setOnClickListener(this);
    	 rlrepeat_status.setOnClickListener(this);
    	 rlrepeat_scene.setOnClickListener(this);
    	 /**************Ĭ����ص�������Ϣ********************/
    	 choosestatus=0;
		 tvresultstatus.setText("�Ǽ�����");
		 settingEntity.setTimesetting_status("�Ǽ�����");
		 tvresultscene.setText("��Ϣ");
		 settingEntity.setTimesetting_scene("��Ϣ");
		 ischeck_values="��Ϣ";
		 settingEntity.setBegintime("22:00");
    	 tvresultbegintime.setText("22:00");
    	 settingEntity.setEndtime("07:00");
    	 tvresultendtime.setText("07:00");
    	 tvresultrepeat.setText("ÿ��");
    	 settingEntity.setDetailrepeat("ÿ��");
     }
	  /*****************************************************************
      * �������� :onClick
      * �� ��˵�� :V  ��صĲ����ļ�
      * ʱ         �� :2011-11
      * ����ֵ:��
      * ����˵��:��صĵ���¼�  ��������Լ��޸�
      ****************************************************************/
	public void onClick(View v) {
		 try {
			switch (v.getId()) {
			case R.id.rlrepeat_day:
				RepeatResult();
				break;
			case R.id.rlrepeat_begintime:
				try {
					//				 Intent intent_begintime=new Intent();
					//				 intent_begintime.setClass(this, BeginEndSettingActivity.class);
					//				 intent_begintime.putExtra("begin_or_end", 1);
					//				 startActivityForResult(intent_begintime, DataResours.REQUEST_BEFINTIME);
					String resulttime = tvresultbegintime.getText().toString();
					begin_end_status = 1;
					String[] strings = resulttime.split(":");
					int beginhour = Integer.parseInt(strings[0]);
					int beginmin = Integer.parseInt(strings[1]);
					timePickerDialog.setStartTime(beginhour, beginmin);
					timePickerDialog.show(getSupportFragmentManager(),
							TIMEPICKER_TAG);
				} catch (Exception e) {
				}
				break;
			case R.id.rlrepeat_endtime:
				try {
					begin_end_status = 2;
					//				 Intent intent_endtime=new Intent();
					//				 intent_endtime.setClass(this, BeginEndSettingActivity.class);
					//				 intent_endtime.putExtra("begin_or_end", 2);
					//				 startActivityForResult(intent_endtime, DataResours.REQUEST_ENDTIME);
					String endtime = tvresultendtime.getText().toString();
					String[] endstrings = endtime.split(":");
					int endhour = Integer.parseInt(endstrings[0]);
					int endmin = Integer.parseInt(endstrings[1]);
					timePickerDialog.setStartTime(endhour, endmin);
					timePickerDialog.show(getSupportFragmentManager(),
							TIMEPICKER_TAG);
				} catch (Exception e) {
				}
				break;
			case R.id.rlrepeat_status:
				ChooseDialog("״̬", DataResours.status);
				break;
			case R.id.rlrepeat_scene:
				if(choosestatus==3){
					Toast.makeText(this, "��������״̬", 3).show();
				}else{
					Intent intent=new Intent();
					intent.setClass(this, FJWRActivity.class);
					if (choosestatus==0) {
						intent.putExtra("position", 1);
					}else if(choosestatus==1){
						intent.putExtra("position", 2);
					}
					intent.putExtra("tophiddle", "tophiddle");
					intent.putExtra("ischeck_values",ischeck_values);
					startActivityForResult(intent, DataResours.REQUEST_GETSTATUS);
				}
				break;
			case R.id.btndetailnewback:
				System.out.println("quicksetup_status---->>>"+"!"+quicksetup_status+"!");
				if(!TextUtils.isEmpty(quicksetup_status))
				{
					 setResult(DataResours.RESULT_QUICKSETUP_UPDATE);
				}else if(TextUtils.isEmpty(quicksetup_status)){
					Intent intent=new Intent();
					intent.putExtra("back", "back");
					setResult(DataResours.RESULT_UPDATETIMESETTING, intent);
				}
				finish();
				break;
			case R.id.btndetailnewsave:
				if(islinestatus==0){
					Toast.makeText(TimeSettingDetailActivity.this, "����δ��¼!", 3).show();
				}else{
					if(CheckSave()){
						if(getServerStatus()){
							if(!"updataolddata".equals(updataolddata)&&!"quicksetup_status".equals(quicksetup_status)){
								TTimeSceneData timeSceneData = settingEntity.getTimeSceneData();
								new AddTimeScene().execute(new TTimeSceneData[] { timeSceneData });
							}else if("updataolddata".equals(updataolddata)&&!"quicksetup_status".equals(quicksetup_status)){
								TTimeSceneData timeSceneData = settingEntity.getTimeSceneData();
								new ReplaceTimeScene().execute(new TTimeSceneData[] { timeSceneData });
							}else if(!"updataolddata".equals(updataolddata)&&"quicksetup_status".equals(quicksetup_status)&&!"add".equals(quickstatus_op)){
								 if(!TextUtils.isEmpty(etkeeptime_hour.getText().toString())&&!TextUtils.isEmpty(etkeeptime_mins.getText().toString())){
									 settingEntity.setBegintime(etkeeptime_hour.getText().toString());
									 settingEntity.setEndtime(etkeeptime_mins.getText().toString());
									 settingEntity.setBieming(etbieming.getText().toString());
									 int update_oldMydata=quickSetupServices.UpdateOldData(settingEntity, 1);
									    if(update_oldMydata>0){
									    	Toast.makeText(this, "�޸Ŀ�����óɹ�", 3).show();
									    	getSharedPreferences("update_quick", MODE_PRIVATE).edit().putInt("updatequick_status", 1).commit();
									    	getSharedPreferences("quick_refish", MODE_PRIVATE).edit().putInt("quick_refish_status", 1).commit();
									    	setResult(DataResours.RESULT_QUICKSETUP_UPDATE);
									    	this.finish();
									    }else {
									    	
									    	Toast.makeText(this, "�޸Ŀ������ʧ��", 3).show();
									    }
								 }else
								 {
									         Toast.makeText(this, "�����ó���ʱ��", 3).show();
								 }
							}else if(!"updataolddata".equals(updataolddata)&&"quicksetup_status".equals(quicksetup_status)&&"add".equals(quickstatus_op)){
							    if(CheckSave()){
							    	settingEntity.setBegintime(etkeeptime_hour.getText().toString());
								    settingEntity.setEndtime(etkeeptime_mins.getText().toString());
								    settingEntity.setBieming(etbieming.getText().toString());
								    boolean boolresult=quickSetupServices.InsertNewSettingData(settingEntity, 1);
								    if(boolresult){
								    	Toast.makeText(TimeSettingDetailActivity.this, "���������ӳɹ�!", 3).show();
								    	setResult(DataResours.RESULt_ADD_QUICK);
								    	TimeSettingDetailActivity.this.finish();
								    }else{
								    	Toast.makeText(TimeSettingDetailActivity.this, "����������ʧ��!������!", 3).show();
								    }
							    }
							}
						}else{
							 Toast.makeText(TimeSettingDetailActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show(); 
						}
					}
				}
				break;
			case R.id.cbonlyone:
				cbonlyone.setChecked(true);
				cbfromtoend.setChecked(false);
				cbeverydays.setChecked(false);
				llfromtoend_layout.setVisibility(View.GONE);
				break;
			case R.id.cbeverydays:
				cbonlyone.setChecked(false);
				cbeverydays.setChecked(true);
				cbfromtoend.setChecked(false);
				llfromtoend_layout.setVisibility(View.GONE);
				break;
			case R.id.cbfromtoend:
				cbonlyone.setChecked(false);
				cbeverydays.setChecked(false);
				cbfromtoend.setChecked(true);
				llfromtoend_layout.setVisibility(View.VISIBLE);
				break;
				
			default:
				break;
			}
		} catch (Exception e) {
			 Toast.makeText(TimeSettingDetailActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
			e.printStackTrace();
		}
	}
	 /*****************************************************************
     * �������� :ChooseDialog
     * �� ��˵�� :title,items ����������Values
     * ʱ         �� :2011-11
     * ����ֵ:��
     * ����˵��:������ݲ˵�(�Ƿ��ǷǼ����Ż����������)
     ****************************************************************/
	public void ChooseDialog(String title,final String[] items){
		
		new AlertDialog.Builder(this).setTitle(title).setItems(
				items, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						choosestatus=which;
						tvresultstatus.setText(items[which]);
						settingEntity.setTimesetting_status(items[which]);
						tvresultscene.setText("");
						settingEntity.setTimesetting_scene("");
						ischeck_values="";
					}
				}).show();
	}
	 /*****************************************************************
     * �������� :onActivityResult
     * �� ��˵�� :requestCode,resultCode,data
     * ʱ         �� :2011-11
     * ����ֵ:��
     * ����˵��:ϵͳ�ص����� ����������ͽ�����ȡ��ص�������Ϣ
     ****************************************************************/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     if(requestCode==DataResours.REQUEST_GETSTATUS&&resultCode==DataResours.RESULT_GETSTATUS){
	    	 ischeck_values=data.getStringExtra("status_detail");
	    	int detail_position_id=data.getIntExtra("status_detail_position", 0);
	    	settingEntity.setTimesetting_icon_id(detail_position_id);
	    	settingEntity.setTimesetting_scene(ischeck_values);
	    	tvresultscene.setText(ischeck_values);
	    	System.out.println("ѡ�е���:"+ischeck_values);
	     }else if(requestCode==DataResours.REQUEST_BEFINTIME&&resultCode==DataResours.RESULT_BEGINTIME){
	    	 String resulttime=data.getStringExtra("resulttime");
	    	 settingEntity.setBegintime(resulttime);
	    	 tvresultbegintime.setText(resulttime);
	    	 
	     }else if(requestCode==DataResours.REQUEST_ENDTIME&&resultCode==DataResours.RESULT_ENDTIME){
	    	 String resulttime=data.getStringExtra("resulttime");
	    	 settingEntity.setEndtime(resulttime);
	    	 tvresultendtime.setText(resulttime);
	    	 
	     }
		super.onActivityResult(requestCode, resultCode, data);
	}
	 /*****************************************************************
     * �������� :SetBeginAndEndtime_For_Back
     * �� ��˵�� :begin_end_status  ��ʶ��ʼʱ�仹�ǽ���ʱ��
     * ʱ         �� :2011-11
     * ����ֵ:��
     * ����˵��:���ڻ�ȡ��ʼʱ��ͽ���ʱ��
     ****************************************************************/
	 public void SetBeginAndEndtime_For_Back(int begin_end_status){
			 Intent intent=new Intent();
			 intent.setClass(this, BeginEndSettingActivity.class);
		     if(begin_end_status==1){
		    	intent.putExtra("begin_or_end", 1);
		    	startActivityForResult(intent, DataResours.REQUEST_BEFINTIME);
		     }else if(begin_end_status==2){
		       intent.putExtra("begin_or_end", 2);
		       startActivityForResult(intent, DataResours.REQUEST_ENDTIME);
		     }
	 }
	 /*****************************************************************
	     * �������� :CheckSave
	     * �� ��˵�� :��
	     * ʱ         �� :2011-11
	     * ����ֵ:��
	     * ����˵��:����Ƿ���д����
	     ****************************************************************/
	public boolean CheckSave(){
		if(!"add".equals(quickstatus_op)&&!"update".equals(quickstatus_op)){
			if(TextUtils.isEmpty(settingEntity.getDetailrepeat())){
				Toast.makeText(this, "�������ظ�", 3).show();
				return false;
			}
			if("�ظ����ô���".equals(settingEntity.getDetailrepeat())){
				Toast.makeText(this, "�ظ����ô���", 3).show();
				return false;
			}
			if(settingEntity.getEndtime().equals(settingEntity.getBegintime())){
				Toast.makeText(this, "��ʼʱ��ͽ���ʱ�䲻����ͬ", 3).show();
				return false;
			}
		} 
		if("add".equals(quickstatus_op)||"update".equals(quickstatus_op)){
			if(TextUtils.isEmpty(etkeeptime_hour.getText().toString())||TextUtils.isEmpty(etkeeptime_mins.getText().toString())){
				Toast.makeText(this, "�����ó���ʱ��", 3).show();
				return false;
			}
			if(!TextUtils.isEmpty(etkeeptime_mins.getText().toString())&&Integer.parseInt(etkeeptime_mins.getText().toString())>=60){
				Toast.makeText(this, "����ʱ�����ô���", 3).show();
				return false;
			}
		}
		if(TextUtils.isEmpty(settingEntity.getTimesetting_status())){
			Toast.makeText(this, "������״̬", 3).show();
			return false;
		}
		if(TextUtils.isEmpty(settingEntity.getTimesetting_scene())){
			Toast.makeText(this, "�������龰", 3).show();
			return false;
		}
		return true;
	}
	 /*****************************************************************
     * �������� : RepeatResult
     * �� ��˵�� :��
     * ʱ         �� :2011-11
     * ����ֵ:��
     * ����˵��:���ظ����ý������ʵ�Լ��ָ��ظ����ý����е�����
     ****************************************************************/
	public void RepeatResult(){
		LayoutInflater inflter=LayoutInflater.from(this);
		View layout=inflter.inflate(R.layout.repeat_item_one_layout, null);
		cbonlyone=(CheckBox) layout.findViewById(R.id.cbonlyone);
		cbeverydays=(CheckBox) layout.findViewById(R.id.cbeverydays);
		cbfromtoend=(CheckBox) layout.findViewById(R.id.cbfromtoend);
		spfromdata=(Spinner) layout.findViewById(R.id.spfromdata);
		spenddata=(Spinner) layout.findViewById(R.id.spenddata);
		spfromdata.setAdapter(GetArrayAdapter());
		spenddata.setAdapter(GetArrayAdapter());
		llfromtoend_layout=(LinearLayout) layout.findViewById(R.id.llfromtoend_layout);
		llfromtoend_layout.setVisibility(View.GONE);
		if(!TextUtils.isEmpty(detaukrepeat)){
			 if("����".equals(detaukrepeat)){
				 cbonlyone.setChecked(true);
				 cbeverydays.setChecked(false);
				 cbfromtoend.setChecked(false);
			 }else if("ÿ��".equals(detaukrepeat))
			 {
				 cbonlyone.setChecked(false);
				 cbeverydays.setChecked(true);
				 cbfromtoend.setChecked(false);
			 }else{
				 cbonlyone.setChecked(false);
				 cbeverydays.setChecked(false);
				 cbfromtoend.setChecked(true);
				 llfromtoend_layout.setVisibility(View.VISIBLE);
				 spfromdata.setSelection(arrayupdata[0]);
				 spenddata.setSelection(arrayupdata[1]);
			 }
		}
		cbonlyone.setOnClickListener(this);
		cbeverydays.setOnClickListener(this);
		cbfromtoend.setOnClickListener(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(TimeSettingDetailActivity.this);
		builder.setCancelable(true).setView(layout).setTitle("�ظ�����")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if(cbonlyone.isChecked()){
							settingEntity.setDetailrepeat("����");
							tvresultrepeat.setText("����");
						}else if(cbeverydays.isChecked()){
							settingEntity.setDetailrepeat("ÿ��");
							tvresultrepeat.setText("ÿ��");
						}else if(cbfromtoend.isChecked()){
							String resultdata="";
							int from_position=spfromdata.getSelectedItemPosition();
							int end_position=spenddata.getSelectedItemPosition();
//							if(end_position<from_position) {
//								resultdata="�ظ����ô���";
//							} else {
								resultdata=DataResours.datas[from_position]+"-"+DataResours.datas[end_position];
//							}
							settingEntity.setDetailrepeat(resultdata);
							String _dayrepeat=from_position==end_position?DataResours.datas[from_position]:resultdata;
							tvresultrepeat.setText(_dayrepeat);
						}
					}
				}).setNegativeButton("ȡ��",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
   /******************************
    * �ָ���ص�����
    * @param temp_id ��ص�ID ���ID �� ��ʱ����ID
    * @param choose_id 1��ʾΪ���ID 2��ʾΪ��ʱ����ID
    */
  public void RestorationQuicksetupMyData(String temp_id,int choose_id){
	     TimeSettingEntity _entity=null;
	      if(choose_id==1) {
			_entity=quickSetupServices.GetMySearch(Integer.parseInt(temp_id), 2);
		} else if(choose_id==2) {
			_entity=timeSettingService.GetMySearch(Integer.parseInt(temp_id), 2);
		}
	      
	      if(settingEntity==null) {
			settingEntity=new TimeSettingEntity();
		}
			 settingEntity=_entity;
			 tvresultrepeat.setText(settingEntity.getDetailrepeat());
			 if(choose_id==2){
				 tvresultbegintime.setText(settingEntity.getBegintime());
				 tvresultendtime.setText(settingEntity.getEndtime());
			 }else  if(choose_id==1){
				 etkeeptime_hour.setText(settingEntity.getBegintime());
				 etkeeptime_mins.setText(settingEntity.getEndtime());
				 etbieming.setText(settingEntity.getBieming());
				 
			 }
			 tvresultstatus.setText(settingEntity.getTimesetting_status());
			 tvresultscene.setText(settingEntity.getTimesetting_scene());
			 if(settingEntity.getDetailrepeat()!=null){
			      detaukrepeat=settingEntity.getDetailrepeat().toString();
				 if(!"����".equals(detaukrepeat)&&!"ÿ��".equals(detaukrepeat)) {
						arrayupdata=ArrayUnits.Getposition(settingEntity.getDetailrepeat().toString());
					}
			 }
			 ischeck_values=settingEntity.getTimesetting_scene();
			 choosestatus="�Ǽ�����".equals(settingEntity.getTimesetting_status())?0:1;
			
		
}
	 /*****************************************************************
   * �������� : GetArrayAdapter
   * �� ��˵�� :��
   * ʱ         �� :2011-11
   * ����ֵ:ArrayAdapter<String>
   * ����˵��:��ȡһ��ArrayAdapter<String> ��������  ����Spinner����ʵ���һ�������
   ****************************************************************/
  public ArrayAdapter<String> GetArrayAdapter(){
	  ArrayAdapter<String> adapter=null;
	  adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, DataResours.datas);
	  adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	  return adapter;
  }
 
	class ReplaceTimeScene extends AsyncTask<TTimeSceneData, Integer, TReplaceTimeSceneResult.Enum> {
		int index;
		int position;

		@Override
		protected void onPreExecute() {
			progressDialog.setMessage("���ڸ��¶�ʱ״̬......");
			progressDialog.show();
		}

		@Override
		protected TReplaceTimeSceneResult.Enum doInBackground(TTimeSceneData... params) {
			TTimeSceneData data = params[0];
			int index = data.getIndex();
			data.setIndex(null);
			return HttpDataAccess.getInstance().replaceTimeScene(index, data);
		}

		@Override
		protected void onPostExecute(TReplaceTimeSceneResult.Enum result) {
			progressDialog.hide();
			Integer errorId = MyResources.getHttpErrorIndex(result);
			if (errorId != null) {
				if (errorId == R.string.http_access_need_relogin) {
					// ��ʾ��¼�Ի���
					Intent intent = new Intent(TimeSettingDetailActivity.this, LoginActivity.class);
					startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
				} else {
					Toast.makeText(TimeSettingDetailActivity.this, getResources().getString(errorId), 3).show();
				}
				return;
			}

			switch (result.intValue()) {
			case TReplaceTimeSceneResult.INT_SUCCESS:
				timeSettingService.UpdateOldData(settingEntity, 1);
				Toast.makeText(TimeSettingDetailActivity.this, "�޸ĳɹ�", 3).show();
				getSharedPreferences("updatelistview", MODE_PRIVATE).edit().putInt("update", 1).commit();
				Intent intent=new Intent();
				intent.putExtra("back", "noback");
				setResult(DataResours.RESULT_UPDATETIMESETTING,intent);
				TimeSettingDetailActivity.this.finish();
				break;
			case TReplaceTimeSceneResult.INT_FAILED_INDEX_NOT_EXISTED:
				Toast.makeText(TimeSettingDetailActivity.this, "ɾ��ʧ�ܣ��������Ѳ����ڴ����ݣ�", 3).show();
				break;
			case TReplaceTimeSceneResult.INT_FAILED_PERIOD_CONFLICT:
				Toast.makeText(TimeSettingDetailActivity.this, "��������ʱ״̬ʱ���ͻ��", 3).show();
				break;
			default:
				break;
			}
		}
	}

	class AddTimeScene extends AsyncTask<TTimeSceneData, Integer, TAddTimeSceneResult.Enum> {
		int index;
		int position;

		@Override
		protected void onPreExecute() {
			progressDialog.setMessage("������Ӷ�ʱ״̬......");
			progressDialog.show();
		}

		@Override
		protected TAddTimeSceneResult.Enum doInBackground(TTimeSceneData... params) {
			TTimeSceneData data = params[0];
			return HttpDataAccess.getInstance().addTimeScene(data);
		}

		@Override
		protected void onPostExecute(TAddTimeSceneResult.Enum result) {
			progressDialog.hide();
			Integer errorId = MyResources.getHttpErrorIndex(result);
			if (errorId != null) {
				if (errorId == R.string.http_access_need_relogin) {
					// ��ʾ��¼�Ի���
					Intent intent = new Intent(TimeSettingDetailActivity.this, LoginActivity.class);
					startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
				} else {
					Toast.makeText(TimeSettingDetailActivity.this, getResources().getString(errorId), 3).show();
				}
				return;
			}

			switch (result.intValue()) {
			case TAddTimeSceneResult.INT_SUCCESS:
				// NEED UPDATE ALL THE DATA
				timeSettingService.InsertNewSettingData(settingEntity, TimeSettingService.WRITE);
				Toast.makeText(TimeSettingDetailActivity.this, "���ӳɹ�", 3).show();

				getSharedPreferences("updatelistview", MODE_PRIVATE).edit().putInt("update", 5).commit();
				setResult(DataResours.RESULT_ADD_DSSZ);
				TimeSettingDetailActivity.this.finish();
				break;
			case TAddTimeSceneResult.INT_FAILED_OVER_MAX_COUNT:
				Toast.makeText(TimeSettingDetailActivity.this, "�Ѿ��ﵽϵͳ�������ö�ʱ״̬�ĸ�������������µĶ�ʱ״̬��", 3).show();
				break;
			case TAddTimeSceneResult.INT_FAILED_PERIOD_CONFLICT:
				Toast.makeText(TimeSettingDetailActivity.this, "��������ʱ״̬ʱ���ͻ��", 3).show();
				break;
			case TAddTimeSceneResult.INT_FAILED_NOT_REGISTED:
				 MyDialog("����δ��ͨ,���Ͷ���5��11631234��ͨ�����ҵ��!", "��ͨ");
				break;
			default:
				break;
			}
		}
	}
	@SuppressWarnings("static-access")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if(keyCode==event.KEYCODE_BACK&&event.getRepeatCount()==0){
			 if(!TextUtils.isEmpty(quicksetup_status))
			   setResult(DataResours.RESULT_QUICKSETUP_UPDATE);
			 else if(TextUtils.isEmpty(quicksetup_status)){
				    Intent intent=new Intent();
					intent.putExtra("back", "back");
					setResult(DataResours.RESULT_UPDATETIMESETTING, intent);
			 }
			 TimeSettingDetailActivity.this.finish();
		 }
		return super.onKeyDown(keyCode, event);
	}
	 /**********************************
	   * ��ص���ʾ��Ϣ
	   * @param msg ��Ϣ������
	   * @param itemValues Button��ص���ʾ��Ϣ
	   */
	  private void MyDialog(String msg,final String itemValues){
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setMessage(msg)
		           .setCancelable(false).setTitle("��ܰ��ʾ")
		           .setPositiveButton(itemValues, new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		            	   if("��ͨ".equals(itemValues))
		            	   {
	            		       SmsManager smsManager = SmsManager.getDefault();
	                           PendingIntent sentIntent = PendingIntent.getBroadcast(TimeSettingDetailActivity.this, 0, new Intent(), 0);
	                           smsManager.sendTextMessage("11631234", null, "5", sentIntent, null);
	                           Toast.makeText(TimeSettingDetailActivity.this, "���ŷ������", Toast.LENGTH_LONG).show();
		            	   }
		            		
		               }
		           })
		           .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    dialog.cancel();
		               }
		           });
		    AlertDialog alert = builder.create();
		    alert.show();
		    }
	  @SuppressWarnings("static-access")
	  public boolean getServerStatus(){
	       try {
	  		return  NetWorkConnectionUtil.getServerStatus(HttpDataAccess.getInstance().getHttpServerUrl());
	  	} catch (Exception e) {
	  		 return false;
	  	}
	  }
	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		 String _hour=hourOfDay>=10?hourOfDay+"":"0"+hourOfDay;
		  String _minute=minute>=10?minute+"":"0"+minute;
  	      String result=_hour+":"+_minute;
		//��ʼʱ��
		if(begin_end_status==1){
	    	 settingEntity.setBegintime(result);
	    	 tvresultbegintime.setText(result);
		}
		//����ʱ��
		else if(begin_end_status==2){
			 settingEntity.setEndtime(result);
	    	 tvresultendtime.setText(result);
		}
	}
}
