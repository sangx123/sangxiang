package winway.mdr.chinaunicom.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.http.util.ByteArrayBuffer;

import winway.mdr.chinaunicom.comm.ArrayUnits;
import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.FQMyAdapter;
import winway.mdr.chinaunicom.comm.HTTPCommentTools;
import winway.mdr.chinaunicom.comm.MyAdapter;
import winway.mdr.chinaunicom.comm.MyResources;
import winway.mdr.chinaunicom.comm.NetWorkConnectionUtil;
import winway.mdr.chinaunicom.current.listview.MyListView;
import winway.mdr.chinaunicom.current.listview.MyListView.OnRefreshListener;
import winway.mdr.chinaunicom.custom.controls.CustomListView;
import winway.mdr.chinaunicom.entity.TimeSettingEntity;
import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
import winway.mdr.chinaunicom.services.QuickSetupServices;
import winway.mdr.chinaunicom.services.TimeSettingService;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.cpth.TCheckVersionResult;
import com.liz.cpth.TCheckVersionRsp;
import com.liz.cptr.TGetTimeSceneResult;
import com.liz.cptr.TGetTimeSceneRsp;
import com.liz.cptr.TSetDurationSceneResult;
import com.liz.cptr.TTimeSceneData;
import com.liz.cptr.TUserLoginResult;
/*****************************************************
 * �����������
 * @author zhaohao
 * time:2012-5-11
 * ���˵��:���������л���ģʽ���д���� ʵ�ַ�ʽ��ȴ������
 */
public class MainActivity extends BaseActivity  implements OnClickListener,OnItemClickListener,OnGestureListener,OnTouchListener{
	LinearLayout mLayout;
	private ImageButton mSetting;
	LinearLayout llindex,llzhixun,llzhibo,lldianbo,llmore;
	ImageButton ibindex,ibfjwr,ibqhdr,ibdssz,ibmore,ibtnhelpaboutmysoft;
	ImageButton ib_forback,ib_refresh,ib_about;
	TextView tvaboutcontent,tvtitle_name;
	ImageView ivaboutlogo;
	Button cancel;
	int defaultposition=1;
	CustomListView lvhfzc=null;//�ָ�������ListView
	CustomListView lvkjsz_list=null;//������õ�ListView��Ϣ
	ListView lvchoosescene=null;//�Ǽ����ź�������ŵ�ListView
	ListView lvmoredetail;
	SharedPreferences share_first;
	QuickSetupServices quickSetupServices;
	RelativeLayout relation_main,relation_qhdr_orfjwr,relation_dssz_setting,relation_more;
    ImageButton ibtneditquick,ibtnaddquick,ibtneditquxiaoquick;//��������еı༭��ť	����Ӱ�ť
	FQMyAdapter adapter=null;
	String checkbacktext="";
	int checkbackid=0;
	int positionValues=0;
	String savescence="";
	boolean IsEdit=true;
	boolean DsszIsEdit=true;
	boolean autologin;
	String phone_num_user,phone_pwd_user;
	SharedPreferences preferences;//��ȡ�Ƿ�洢��ص��û���Ϣ
	//�����洢�Ƿ��¼�ı�ʶ
	SharedPreferences line_or_offline;
	int islinestatus=0;
	TextView tvshowseconduserdetail;
	String detail="";
	String phone_status="";
	Button btnfjwrsave=null;//�ڷǼ����ź�������Ž����еı��水ť����
	EditText etkeephours,etkeepdays,etkeepminutes;//�Ǽ����ź���������е�����ʱ�ɵ���ؿؼ�
	String savepolicy="";
	HttpDataAccess dataAccess=null;
	HTTPCommentTools commentTools=null;
	ProgressDialog dialog=null;
	TextView tvpolicyscene;
	/**************��ǰ״̬����ʾ��ǰ**************/
	ImageView ivthiscurrentimage;
	TextView tvthiscurrentendtime;//�������е���ʾ������ʱ��
	String scene = "",policy = "";
	/********************ʱ�������е�*********************/
	Button btn_time_setting_edit,btn_time_setting_add;
	MyListView lvtimesettinglistview=null;
	ProgressDialog progressDialog;
	TimeSettingService timeSettingService;
	 int linestatus=0;
	 public static boolean updateServerData = true;
	 String statusrefish="";
	 int thisstrm;
	 int thisstrh;
   	  Calendar calendar;
   	ProgressDialog progressDialog_login;
   	ImageButton ibdetailsome_login,ibdetailsome_thisstatus;
   	String endtime_detail="";
    String year="",month="",day="";
    String dssz="";
    /**********�����º�����ز���***************/
    boolean IsBegin=true;
  //����Ƿ�ǿ�Ƹ���
	String updateCanOrMust="";
	ProgressDialog progressUpdata; 
	String yesorno="";
	AlertDialog alertUpdate;
	int warningstatus=0;
	CheckBox chnowarning;
	int alllength=0;
	
	RelativeLayout rlloginlayout,rldetailLayout;
	String status="";
	private GestureDetector mGestureDetector;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.second_main);
        getSharedPreferences("firstshow", MODE_PRIVATE).edit().putInt("myfirst", 1).commit();
        if (isAirMode()) {
			show_msg("�벻Ҫ�ڷ���ģʽ��ʹ�ñ����.");
		} else {
			isNetworkAvailable();
	    	InitMyCont();
		}
       
	}

	 /*****************************************************************
	   * �������� : isNetworkAvailable
	   * �� ��˵�� :��
	   * ʱ         �� :2011-11
	   * ����ֵ:��
	   * ����˵��:�ж����統ǰ��״̬
	   ****************************************************************/ 
	public boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) {
			Toast.makeText(MainActivity.this, "����������������뷽ʽ", 3).show();
			return false;
		} 
		return true;
	}
	public void show_msg(String msg) {
		new AlertDialog.Builder(this).setTitle("��ʾ:").setMessage(msg)// .show();
				.setMessage(msg).setIcon(R.drawable.icon).setCancelable(false).setPositiveButton(
						"ȷ��", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								 if(line_or_offline!=null)
					            	   line_or_offline.edit().putInt("isline_status",0).commit();
					            		CookieManager.getInstance().removeAllCookie();
					            		android.os.Process.killProcess(android.os.Process.myPid()); 
							}
						}).show();

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if(requestCode==DataResours.REQUEST_LOGIN_CODE&&resultCode==DataResours.RESULT_LOGIN_CODE){
			String user_phone= data.getStringExtra("phone_number_user");
		    phone_status=data.getStringExtra("phone_status");
			detail=data.getStringExtra("detail");
			tvshowseconduserdetail.setText(user_phone+",��ӭ����");
		    if(!"����״̬".equals(phone_status)){
		    	ivthiscurrentimage.setVisibility(View.VISIBLE);
		    	scene=phone_status.substring(0, phone_status.indexOf("("));
		    	policy=phone_status.substring(phone_status.indexOf("(")+1, phone_status.length()-1);
		    	ivthiscurrentimage.setImageResource(ArrayUnits.GetPolicyIconPosition(policy, scene));
                tvthiscurrentendtime.setVisibility(View.VISIBLE);
                String durations=detail.substring(detail.lastIndexOf(",")+1,detail.length());
                  year=detail.substring(detail.indexOf("��")+1, detail.indexOf(","));
            	  month=detail.substring(detail.indexOf("��")+1, detail.indexOf("��")-1);
            	  day=detail.substring(detail.indexOf("��")+1, detail.indexOf("ʱ")-1);
		    	 thisstrh=Integer.valueOf(detail.substring(detail.indexOf("ʱ")+1, detail.indexOf("��")-1));
		         thisstrm=Integer.valueOf(detail.substring(detail.indexOf("��")+1, detail.lastIndexOf(",")));
		         endtime_detail=getEndTime(Integer.valueOf(year),Integer.valueOf(month),Integer.valueOf(day),thisstrh, thisstrm, Integer.valueOf(durations));
		         tvthiscurrentendtime.setText("����ʱ��:"+endtime_detail.substring(10, endtime_detail.length()));
		         System.out.println("��¼��detail��Ϣ---->>>>"+detail);
		    }else{
		    	ivthiscurrentimage.setVisibility(View.GONE);
		    	tvthiscurrentendtime.setVisibility(View.GONE);
		    }
			 if(phone_status.contains("��������")) {
				 tvpolicyscene.setText("�������� ");
			 }else {
				 tvpolicyscene.setText(phone_status);
			 }

			 if("dssz".equals(dssz)){
				dssz="";
				new GetAllTimeScenes().execute(new String[] {});
			}
		 }else if(requestCode==DataResours.REQUEST_SETMYSCENCE&&resultCode==DataResours.RESULT_SETMYSCENCE){
//			 new QuickSetupTask().execute(new String[]{"","",""});
		 }else if(requestCode==DataResours.QREQUEST_ADD_DSSZ&&resultCode==DataResours.RESULT_ADD_DSSZ){
			 DsszIsEdit=true;
			 new GetAllTimeScenes().execute();
		 }else if(requestCode==DataResours.REQUEST_UPDATETIMESETTING&&resultCode==DataResours.RESULT_UPDATETIMESETTING){
			 DsszIsEdit=true;
			 if("back".equals(data.getStringExtra("back"))) 
				 lvtimesettinglistview.setAdapter(timeSettingService.getadapter(1));
			  else
			    new GetAllTimeScenes().execute();
		 }else if(requestCode==DataResours.REQUEST_QUICKSETUP_UPDATE&&resultCode==DataResours.RESULT_QUICKSETUP_UPDATE){
			 IsEdit=true;
			 ibtneditquxiaoquick.setVisibility(View.GONE);
			 ibtneditquick.setVisibility(View.VISIBLE);
			 lvkjsz_list.setAdapter(quickSetupServices.getadapter(1));
		 }else if(requestCode==DataResours.REQURST_ADD_QUICK&&resultCode==DataResours.RESULt_ADD_QUICK){
			 IsEdit=true;
			 ibtneditquxiaoquick.setVisibility(View.GONE);
			 ibtneditquick.setVisibility(View.VISIBLE);
			 lvkjsz_list.setAdapter(quickSetupServices.getadapter(1));
		 }
		super.onActivityResult(requestCode, resultCode, data);
	}
	public void InitMyCont(){
		mGestureDetector = new GestureDetector(this);
        dataAccess=HttpDataAccess.getInstance();
        commentTools=HTTPCommentTools.getInstance();
    	dialog=new ProgressDialog(this);
    	calendar=Calendar.getInstance();
        mydialog=new ProgressDialog(MainActivity.this);
		mydialog.setTitle("�����");
		mydialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
		mydialog.setMax(100);
		mydialog.setMessage("���Ե�...�����������°汾!");
		dialog.setMessage("���Ե�...��������...");
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("���ڶ�ȡ��ʱ״̬��Ϣ......");
		progressDialog_login = new ProgressDialog(this);
	    progressDialog_login.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	  	progressDialog_login.setMessage("���ڵ�¼ ���Ժ�...");
	  	progressUpdata = new ProgressDialog(MainActivity.this);
		progressUpdata.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressUpdata.setMessage("���ڼ�����Ժ�...");
		timeSettingService=new TimeSettingService(this);
        CookieSyncManager.createInstance(this); // cookie����
		CookieSyncManager.getInstance().startSync();
        share_first=getSharedPreferences("share_this_first", MODE_PRIVATE);
        preferences=getSharedPreferences("saveuser_preferences", MODE_PRIVATE);
        line_or_offline=getSharedPreferences("Isonline", MODE_PRIVATE);
        quickSetupServices=new QuickSetupServices(this);
		lvhfzc=(CustomListView) this.findViewById(R.id.lvhfzc); //�ָ�������ListView�б�
		lvhfzc.setHaveScrollbar(false);
		lvhfzc.setOnTouchListener(this);
		lvkjsz_list=(CustomListView) this.findViewById(R.id.lvkjsz_list);//������õ�List
		lvkjsz_list.setHaveScrollbar(false);
		lvkjsz_list.setOnTouchListener(this);
		lvkjsz_list.setOnItemClickListener(new KjszListOnitemClickEvent());
		relation_main=(RelativeLayout) this.findViewById(R.id.relation_main);
		relation_qhdr_orfjwr=(RelativeLayout) this.findViewById(R.id.relation_qhdr_orfjwr);
		relation_dssz_setting=(RelativeLayout) this.findViewById(R.id.relation_dssz_setting);
		tvshowseconduserdetail=(TextView) this.findViewById(R.id.tvshowseconduserdetail);
		lvchoosescene=(ListView) this.findViewById(R.id.lvchoosescene);
		lvchoosescene.setOnItemClickListener(new FjwrOrQhdrOnitemclickEvent());
		relation_more=(RelativeLayout) this.findViewById(R.id.relation_more);
		lvmoredetail=(ListView) this.findViewById(R.id.lvmoredetail);
		lvmoredetail.setOnItemClickListener(new BlackOrWhiteonitemClickEvent());
		lvhfzc.setAdapter(InitMyAdapter());
		lvhfzc.setOnItemClickListener(new HfzcOnitemClickEvent());
		llindex=(LinearLayout) this.findViewById(R.id.llindex);
		llzhixun=(LinearLayout) this.findViewById(R.id.llzhixun);
		llzhibo=(LinearLayout) this.findViewById(R.id.llzhibo);
		lldianbo=(LinearLayout) this.findViewById(R.id.lldianbo);
		llmore=(LinearLayout) this.findViewById(R.id.llmore);
		ibindex=(ImageButton) this.findViewById(R.id.ibindex);
		ibfjwr=(ImageButton) this.findViewById(R.id.ibfjwr);
		ibqhdr=(ImageButton) this.findViewById(R.id.ibqhdr);
		ibdssz=(ImageButton) this.findViewById(R.id.ibdssz);
		ibmore=(ImageButton) this.findViewById(R.id.ibmore);
		mSetting=(ImageButton)this.findViewById(R.id.mSetting);
		mLayout=(LinearLayout)this.findViewById(R.id.mLayout);
		mSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSettingClick();
			}
		});

		cancel=(Button)this.findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goBack();
			}
		});


		ibtnhelpaboutmysoft=(ImageButton) this.findViewById(R.id.ibtnhelpaboutmysoft);
		ibtnhelpaboutmysoft.setOnClickListener(this);
		tvtitle_name=(TextView) this.findViewById(R.id.tvtitle_name);
		ibtneditquick=(ImageButton) this.findViewById(R.id.ibtneditquick);
		ibtnaddquick=(ImageButton) this.findViewById(R.id.ibtnaddquick);
		ibtneditquxiaoquick=(ImageButton) this.findViewById(R.id.ibtneditquxiaoquick);
		btnfjwrsave=(Button) this.findViewById(R.id.btnfjwrsave);
		tvpolicyscene=(TextView) this.findViewById(R.id.tvpolicyscene);
		btnfjwrsave.setOnClickListener(this);
		ibtneditquick.setOnClickListener(this);
		ibtnaddquick.setOnClickListener(this);
		ibtneditquxiaoquick.setOnClickListener(this);
		etkeepdays=(EditText) this.findViewById(R.id.etkeepdays);
		etkeephours=(EditText) this.findViewById(R.id.etkeephours);
		etkeepminutes=(EditText) this.findViewById(R.id.etkeepminutes);
		ivthiscurrentimage=(ImageView) this.findViewById(R.id.ivthiscurrentimage);
		btn_time_setting_add=(Button) this.findViewById(R.id.btn_time_setting_add);
		btn_time_setting_edit=(Button) this.findViewById(R.id.btn_time_setting_edit);
		lvtimesettinglistview=(MyListView) this.findViewById(R.id.lvtimesettinglistview);
		tvthiscurrentendtime=(TextView) this.findViewById(R.id.tvthiscurrentendtime);
		ibdetailsome_login=(ImageButton) this.findViewById(R.id.ibdetailsome_login);
		ibdetailsome_thisstatus=(ImageButton) this.findViewById(R.id.ibdetailsome_thisstatus);
		ibdetailsome_thisstatus.setOnClickListener(this);
		ibdetailsome_login.setOnClickListener(this);
		lvtimesettinglistview.setOnItemClickListener(this);
		lvtimesettinglistview.setonRefreshListener(new MyListViewTimeDetailOnRefreshEvent());
		btn_time_setting_add.setOnClickListener(this);
		btn_time_setting_edit.setOnClickListener(this);
		
		llindex.setOnClickListener(new MycontOnclickEvent());
		llzhixun.setOnClickListener(new MycontOnclickEvent());
		llzhibo.setOnClickListener(new MycontOnclickEvent());
		lldianbo.setOnClickListener(new MycontOnclickEvent());
		llmore.setOnClickListener(new MycontOnclickEvent());
		ibindex.setOnClickListener(new MycontOnclickEvent());
		ibfjwr.setOnClickListener(new MycontOnclickEvent());
		ibqhdr.setOnClickListener(new MycontOnclickEvent());
		ibdssz.setOnClickListener(new MycontOnclickEvent());
		ibmore.setOnClickListener(new MycontOnclickEvent());
		rlloginlayout=(RelativeLayout) this.findViewById(R.id.rlloginlayout);
		rldetailLayout=(RelativeLayout) this.findViewById(R.id.rldetailLayout);
		rlloginlayout.setOnClickListener(this);
		rldetailLayout.setOnClickListener(this);
		 int first=share_first.getInt("share_first_status", 0);
	    	if(first==0){
	    		quickSetupServices.InsertArrayListMydata(InitQuickSetUpdata(),1);
	    		share_first.edit().putInt("share_first_status", 1).commit();
	    	}
	    	lvkjsz_list.setAdapter(quickSetupServices.InitMyAdapter_Databases());
	    	 autologin=preferences.getBoolean("autologin", false);
	         phone_num_user=preferences.getString("user_phone_number", "");
	         phone_pwd_user=preferences.getString("user_phone_pwd", "");
	         islinestatus=line_or_offline.getInt("isline_status", 0);
	        refresh(1);
	        //���Ƶڶ��ε����Activity���ٽ������ѣ��Ƿ���Ҫ����
	        warningstatus=preferences.getInt("warn", 0);
			if(warningstatus==0){
					UpdateAsynTask_MDR updateAsynTask = new UpdateAsynTask_MDR();
					updateAsynTask.execute();
		    }
	}

	 
  class MycontOnclickEvent implements OnClickListener{
	  @Override
	public void onClick(View v) {
		  if(v.getId()==R.id.llindex||v.getId()==R.id.ibindex){
			    defaultposition=1;
			    IsEdit=true;
			    ibtneditquxiaoquick.setVisibility(View.GONE);
				 ibtneditquick.setVisibility(View.VISIBLE);
			    lvkjsz_list.setAdapter(quickSetupServices.getadapter(1));
		  }
		  if(v.getId()==R.id.llzhixun||v.getId()==R.id.ibfjwr){
			    positionValues=1;
			    defaultposition=2;
			    savepolicy="sm";
				tvtitle_name.setText("�Ǽ�����");
				lvchoosescene.setAdapter(InitMyAdapter(DataResours.fjwricons,DataResours.fjwrValues));
				
		  }
		  if(v.getId()==R.id.llzhibo||v.getId()==R.id.ibqhdr){
			    positionValues=2;
			    defaultposition=3;
			    savepolicy="gj";
				tvtitle_name.setText("�������");
				lvchoosescene.setAdapter(InitMyAdapter(DataResours.qhdricons,DataResours.qhdrValues));
				
		  }
		  if(v.getId()==R.id.lldianbo||v.getId()==R.id.ibdssz){
			    defaultposition=4;
			    linestatus=getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status", 0);
			   	  if(linestatus==1){
			   		  if(getServerStatus()){
			   			  new GetAllTimeScenes().execute(new String[] {});
			   		  }else{
			   			Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
			   		  }
			   	  }else{
			   		  dssz="dssz";
			   		  Toast.makeText(MainActivity.this, "����δ��¼", 3).show();
			   		Intent intent_login_detail=new  Intent(MainActivity.this, LoginActivity.class);
			        startActivityForResult(intent_login_detail, DataResours.REQUEST_LOGIN_CODE);
			   		  
			   	  }
		  }
		  if(v.getId()==R.id.llmore||v.getId()==R.id.ibmore){
			    defaultposition=5;
				lvmoredetail.setAdapter(InitMyAdapter_more());
		  }
		   etkeepdays.setText("00");
		   etkeephours.setText("02");
		   etkeepminutes.setText("00");
		  DsszIsEdit=true;
		  savescence="";
		  if(adapter!=null)
		    adapter.ForBack();
		  refresh(defaultposition);
	}
  }

  
  
		  @Override
		protected void onResume() {		 
		  phone_num_user=preferences.getString("user_phone_number", "");
	      phone_pwd_user=preferences.getString("user_phone_pwd", "");
		  if(getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status", 0)!=0){
			  if(getServerStatus()){
				  statusrefish="statusrefish";
				  if(!TextUtils.isEmpty(phone_num_user)&&!TextUtils.isEmpty(phone_pwd_user)){
					  new LoginTask().execute(new String[]{String.valueOf(27),phone_num_user,phone_pwd_user});
           	  }
			  }else{
				   Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
			  }
			}
		   super.onResume();
		}
  public void refresh(int select){
	  switch (select) {
	case 1:
		dssz="";
		llindex.setBackgroundResource(R.drawable.item_bg);
		ibindex.setBackgroundResource(R.drawable.index_on);
		llzhixun.setBackgroundColor(0x00000000);
		ibfjwr.setBackgroundResource(R.drawable.fjwr_item);
		llzhibo.setBackgroundColor(0x00000000);
		ibqhdr.setBackgroundResource(R.drawable.qhdr_item);
		lldianbo.setBackgroundColor(0x00000000);
		ibdssz.setBackgroundResource(R.drawable.dssz_item);
		llmore.setBackgroundColor(0x00000000);
		ibmore.setBackgroundResource(R.drawable.more);
		relation_main.setVisibility(View.VISIBLE);
	    relation_qhdr_orfjwr.setVisibility(View.GONE);
		relation_dssz_setting.setVisibility(View.GONE);
		relation_more.setVisibility(View.GONE);
		if(getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status", 0)!=0){
			if(getServerStatus()){
				statusrefish="statusrefish";
				if(!TextUtils.isEmpty(phone_num_user)&&!TextUtils.isEmpty(phone_pwd_user)){
					new LoginTask().execute(new String[]{String.valueOf(27),phone_num_user,phone_pwd_user});
           	  }
			 }else{
				 Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
			 }
		}
		break;
	case 2:
		dssz="";
		llzhixun.setBackgroundResource(R.drawable.item_bg);
		ibfjwr.setBackgroundResource(R.drawable.fjwr_item_on);
		llindex.setBackgroundColor(0x00000000);
		ibindex.setBackgroundResource(R.drawable.index);
		llzhibo.setBackgroundColor(0x00000000);
		ibqhdr.setBackgroundResource(R.drawable.qhdr_item);
		lldianbo.setBackgroundColor(0x00000000);
		ibdssz.setBackgroundResource(R.drawable.dssz_item);
		llmore.setBackgroundColor(0x00000000);
		ibmore.setBackgroundResource(R.drawable.more);
		relation_main.setVisibility(View.GONE);
		relation_qhdr_orfjwr.setVisibility(View.VISIBLE);
		relation_dssz_setting.setVisibility(View.GONE);
		relation_more.setVisibility(View.GONE);
		break;
	case 3:
		dssz="";
		llzhibo.setBackgroundResource(R.drawable.item_bg);
		ibqhdr.setBackgroundResource(R.drawable.qhdr_item_on);
		llzhixun.setBackgroundColor(0x00000000);
		ibfjwr.setBackgroundResource(R.drawable.fjwr_item);
		llindex.setBackgroundColor(0x00000000);
		ibindex.setBackgroundResource(R.drawable.index);
		lldianbo.setBackgroundColor(0x00000000);
		ibdssz.setBackgroundResource(R.drawable.dssz_item);
		llmore.setBackgroundColor(0x00000000);
		ibmore.setBackgroundResource(R.drawable.more);
		relation_main.setVisibility(View.GONE);
		relation_qhdr_orfjwr.setVisibility(View.VISIBLE);
		relation_dssz_setting.setVisibility(View.GONE);
		relation_more.setVisibility(View.GONE);
		break;
	case 4:
		lldianbo.setBackgroundResource(R.drawable.item_bg);
		ibdssz.setBackgroundResource(R.drawable.dssz_item_on);
		llzhixun.setBackgroundColor(0x00000000);
		ibfjwr.setBackgroundResource(R.drawable.fjwr_item);
		llzhibo.setBackgroundColor(0x00000000);
		ibqhdr.setBackgroundResource(R.drawable.qhdr_item);
		llindex.setBackgroundColor(0x00000000);
		ibindex.setBackgroundResource(R.drawable.index);
		llmore.setBackgroundColor(0x00000000);
		ibmore.setBackgroundResource(R.drawable.more);
		relation_main.setVisibility(View.GONE);
		relation_qhdr_orfjwr.setVisibility(View.GONE);
		relation_dssz_setting.setVisibility(View.VISIBLE);
		relation_more.setVisibility(View.GONE);
		break;
	case 5:
		dssz="";
		llmore.setBackgroundResource(R.drawable.item_bg);
		ibmore.setBackgroundResource(R.drawable.more_on);
		llzhixun.setBackgroundColor(0x00000000);
		ibfjwr.setBackgroundResource(R.drawable.fjwr_item);
		llzhibo.setBackgroundColor(0x00000000);
		ibqhdr.setBackgroundResource(R.drawable.qhdr_item);
		lldianbo.setBackgroundColor(0x00000000);
		ibdssz.setBackgroundResource(R.drawable.dssz_item);
		llindex.setBackgroundColor(0x00000000);
		ibindex.setBackgroundResource(R.drawable.index);
		relation_main.setVisibility(View.GONE);
		relation_qhdr_orfjwr.setVisibility(View.GONE);
		relation_dssz_setting.setVisibility(View.GONE);
		relation_more.setVisibility(View.VISIBLE);
		break;
		  case 6:
			  dssz="";
			  llzhixun.setBackgroundResource(R.drawable.item_bg);
			  ibfjwr.setBackgroundResource(R.drawable.fjwr_item_on);
			  llindex.setBackgroundColor(0x00000000);
			  ibindex.setBackgroundResource(R.drawable.index);
			  llzhibo.setBackgroundColor(0x00000000);
			  ibqhdr.setBackgroundResource(R.drawable.qhdr_item);
			  lldianbo.setBackgroundColor(0x00000000);
			  ibdssz.setBackgroundResource(R.drawable.dssz_item);
			  llmore.setBackgroundColor(0x00000000);
			  ibmore.setBackgroundResource(R.drawable.more);
			  relation_main.setVisibility(View.GONE);
			  relation_qhdr_orfjwr.setVisibility(View.VISIBLE);
			  relation_dssz_setting.setVisibility(View.GONE);
			  relation_more.setVisibility(View.GONE);
			  break;

	  }
  }

  
 /*****************************************************************
  * �������� :InitMyAdapter  
  * �� ��˵�� :icons--ͼƬ����  values--�������� 
  * ʱ         �� :2011-11
  * ����ֵ:FQMyAdapter ������
  * ����˵��:����������
  ****************************************************************/
public FQMyAdapter InitMyAdapter(int[] icons,String[] values)
{
	
	  String[] from={"image","resources","ischeck","status","ibstatus"};
	  int[] to={R.id.ivshowimage,R.id.ivshowtext,R.id.ivischeck,R.id.tvstatus,R.id.ibsoundlister};
	  ArrayList<HashMap<String, Object>> lists=new ArrayList<HashMap<String,Object>>();
	  int[] imagetemp=icons;
	  String[] GridValues=values;
	  for (int i = 0; i < GridValues.length; i++) {
		 HashMap<String, Object> map=new HashMap<String, Object>();
		 map.put("image", imagetemp[i]);
		 map.put("resources", GridValues[i]);
		 map.put("ischeck", R.drawable.checkok);
		 map.put("status", String.valueOf(0));
		 map.put("ibstatus", String.valueOf(0));
		 lists.add(map);
	} 
	  adapter=new FQMyAdapter(MainActivity.this,lists,R.layout.temp_listview_item, from, to);
	  return adapter;
}
class FjwrOrQhdrOnitemclickEvent implements OnItemClickListener{
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		TextView tvshowtext=(TextView) view.findViewById(R.id.ivshowtext);
		 checkbacktext=tvshowtext.getText().toString();
		 checkbackid=position;
		 String[] temp=positionValues==1?DataResours.fjwrValues:DataResours.qhdrValues;
		 savescence=ArrayUnits.getResult(temp[position]);
	     UpdataCheck(view, position);
	     
	}
	   /*****************************************************************
	    * �������� :UpdataCheck  
	    * �� ��˵�� :view--view��Ϣ  values--position
	    * ʱ         �� :2011-11
	    * ����ֵ:��
	    * ����˵��:����ListViewѡ�е�״̬
	    ****************************************************************/
	public void UpdataCheck(View view, int position){
		TextView tv=(TextView) view.findViewById(R.id.tvstatus);
		String values=tv.getText().toString();
		int num="0".equals(values)?1:0;
     if(num==0)savescence="";
		adapter.updateData(position,num);
		adapter.itemStop();
		adapter.notifyDataSetChanged();
	}
}

@Override
public void onClick(View v) {
	  switch (v.getId()) {
	case R.id.ibtneditquick:
		if(CheckHasLogin()){
		 IsEdit=false;
		 lvkjsz_list.setAdapter(quickSetupServices.getadapter(2));
		 ibtneditquxiaoquick.setVisibility(View.VISIBLE);
		 ibtneditquick.setVisibility(View.GONE);
		}
		break;
	case R.id.ibtneditquxiaoquick:
		 IsEdit=true;
		 lvkjsz_list.setAdapter(quickSetupServices.getadapter(1));
		 ibtneditquick.setVisibility(View.VISIBLE);
		 ibtneditquxiaoquick.setVisibility(View.GONE);
		break;
	case R.id.ibtnaddquick:
		   if(CheckHasLogin()){
			      Intent intent_add=new Intent();
			      intent_add.setClass(MainActivity.this, TimeSettingDetailActivity.class);
			      intent_add.putExtra("quicksetup_status", "quicksetup_status");
			      intent_add.putExtra("quickstatus_op", "add");
				  startActivityForResult(intent_add, DataResours.REQURST_ADD_QUICK);
		   }
		break;
	case R.id.btnfjwrsave:
		if(CheckInputIsnotEntity()){
			       if(adapter!=null)
				    adapter.ForBack();
			if(line_or_offline.getInt("isline_status", 0)==0){
				Toast.makeText(this, "����δ��¼", 3).show();
				Intent intent_login_detail=new  Intent(this, LoginActivity.class);
		        startActivityForResult(intent_login_detail, DataResours.REQUEST_LOGIN_CODE);
			}else{
			  try {
				  if(getServerStatus()){
					  if(savepolicy.equals("dj")){
						  new SetPhoneTask().execute();
					  }else
						  new SaveMyScenceTask().execute();
				  }else{
					  Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
				  }
			} catch (Exception e) {
				e.printStackTrace();
			}
			}
		}
		break;
	case R.id.btn_time_setting_add:
		if(CheckHasLogin()){
			if(getServerStatus()){
				Intent intent=new Intent(this, TimeSettingDetailActivity.class);
				startActivityForResult(intent, DataResours.QREQUEST_ADD_DSSZ);
			}else{
				 Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
			}
		}
		break;
	case R.id.btn_time_setting_edit:
		if(CheckHasLogin()){
			if(getServerStatus()){
				if(!DsszIsEdit){
					DsszIsEdit=true;
					  lvtimesettinglistview.setAdapter(timeSettingService.getadapter(1));
				}else if(DsszIsEdit){
					   DsszIsEdit=false;
					   lvtimesettinglistview.setAdapter(timeSettingService.getadapter(2));
				}
			}else{
				 Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
			}
		}

		break;
	case R.id.ibdetailsome_login:
			Intent intent_login_detail_1=new  Intent(this, LoginActivity.class);
	        startActivityForResult(intent_login_detail_1, DataResours.REQUEST_LOGIN_CODE);
		break;
	case R.id.rlloginlayout:
		Intent intent_login_detail_login=new  Intent(this, LoginActivity.class);
        startActivityForResult(intent_login_detail_login, DataResours.REQUEST_LOGIN_CODE);
	break;
	case R.id.ibdetailsome_thisstatus:
		if(line_or_offline.getInt("isline_status", 0)==0){
			Toast.makeText(this, "����δ��¼", 3).show();
			Intent intent_login_detail=new  Intent(this, LoginActivity.class);
	        startActivityForResult(intent_login_detail, DataResours.REQUEST_LOGIN_CODE);
		}else{
			if("����״̬".equals(phone_status)){
				Toast.makeText(this, "��ǰΪ����״̬", 3).show();
			}else{
				if(getServerStatus()){
				Intent intent_detail=new Intent(this, ThisCurrentStatusActivity.class);
				intent_detail.putExtra("policy", policy);
				intent_detail.putExtra("scene", scene);
				intent_detail.putExtra("detail", detail);
				intent_detail.putExtra("endtime", endtime_detail);
				startActivity(intent_detail);
				}else{
					 Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
				}
			}
		}
		break;
	case R.id.rldetailLayout:
		if(line_or_offline.getInt("isline_status", 0)==0){
			Toast.makeText(this, "����δ��¼", 3).show();
			Intent intent_login_detail=new  Intent(this, LoginActivity.class);
	        startActivityForResult(intent_login_detail, DataResours.REQUEST_LOGIN_CODE);
		}else{
			if("����״̬".equals(phone_status)){
				Toast.makeText(this, "��ǰΪ����״̬", 3).show();
			}else{
				if(getServerStatus()){
					Intent intent_detail=new Intent(this, ThisCurrentStatusActivity.class);
					intent_detail.putExtra("policy", policy);
					intent_detail.putExtra("scene", scene);
					intent_detail.putExtra("detail", detail);
					intent_detail.putExtra("endtime", endtime_detail);
					startActivity(intent_detail);
				}else{
					 Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
				}
				
			}
		}
		break;
	case R.id.ibtnhelpaboutmysoft:
		startActivity(new Intent(this, YeWuActivity.class));
		break;
	default:
		break;
	}
  }
/********************************************
 * ע��ϵͳmenu�˵�
 */
@Override
public boolean onCreateOptionsMenu(Menu menu) {
	  menu.add(0, 1, 0, "�˳�");
	  menu.add(0, 2, 0, "������");
	return super.onCreateOptionsMenu(menu);
}
@Override
public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
	case 1:
		showDialog();
		break;
	case 2:
		yesorno="yesorno";
		 UpdateAsynTask_MDR asynTask=new UpdateAsynTask_MDR();
		 asynTask.execute();
		break;
	}
	return super.onOptionsItemSelected(item);
}
/*****************************************************************
 * �������� :InitMyAdapter  
 * �� ��˵�� :��
 * ʱ         �� :2011-11
 * ����ֵ:MyAdapter
 * ����˵��:������������GridView��������
 ****************************************************************/
	public MyAdapter InitMyAdapter_more()
	{
	  MyAdapter adapter=null;
	  String[] from={"image","resources"};
	  int[] to={R.id.ivmoredetail_icons,R.id.tvmoredetail_text};
	  ArrayList<Map<String, Object>> lists=new ArrayList<Map<String,Object>>();
	  int[] imagetemp=DataResours.moreicon;
	  String[] GridValues=DataResours.moretext;
	  for (int i = 0; i < GridValues.length; i++) {
		 Map<String, Object> map=new HashMap<String, Object>();
		 map.put("image", imagetemp[i]);
		 map.put("resources", GridValues[i]);
		 lists.add(map);
	} 
	  adapter=new MyAdapter(MainActivity.this,lists,R.layout.more_detail_listview_item, from, to);
	  return adapter;
	}
	
/************************************************************
 * ������-����ҳ����ListView����ĵ���¼�
 * @author zhaohao
 * ����˵��:���� ������,������,����,����,���� ���ѡ��
 */
class BlackOrWhiteonitemClickEvent implements OnItemClickListener{
	@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
		{
			  switch (position)
			  {
				  case 0:
					  mDingShiSheZhi();
					  break;

				  case 1:
					 if(CheckHasLogin()){
							 if(getServerStatus()){
								 Intent black_intent=new Intent(MainActivity.this, BlackWhiteListActivity.class);
								 black_intent.putExtra("name", "������");
								 startActivity(black_intent);
							 }else{
								 Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
							 }
						}
					break;
		         case 2:
		        	 if(CheckHasLogin()){
		        		 if(getServerStatus()){
				        	 Intent write_intent=new Intent(MainActivity.this, BlackWhiteListActivity.class);
							 write_intent.putExtra("name", "������");
							 startActivity(write_intent);
		        		 }else{
		        			 Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
		        		 }
		        	 }
					break;
		         case 3:
		        	   if(CheckHasLogin()){
		        		   if(getServerStatus()){
		        		      startActivity(new Intent(MainActivity.this, SettingActivity.class));
		        		   }else{
		        			   Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
		        		   }
		        	   }
		 			break;
		         case 4:
		        	 if(CheckHasLogin())
		        	 {
		        		 startActivity(new Intent(MainActivity.this, EditPwdActivity.class));	
		        	 }
		        	 break;
		          case 5:
		        	   myDialogKaiTong(1);
		 			break;
		          case 6:
		        	  startActivity(new Intent(MainActivity.this, FunctionIntroductionActivity.class));
		        	  break;
		          case 7:
//		        	  myDialogKaiTong(2);
		        	  startActivity(new Intent(MainActivity.this, YeWuActivity.class));
		  			break;
		          case 8:
		        	  yesorno="yesorno";
		     		 UpdateAsynTask_MDR asynTask=new UpdateAsynTask_MDR();
		     		 asynTask.execute();
		        	  break;
		          case 9:
		        	  AboutMDR();
		        	  break;
			 }
		}
	
    }
	/*********************************
	 * ���û�����Ľ��м���ж��û��Ƿ���������
	 * @return ������������true ���򷵻�false
	 */
public boolean CheckInputIsnotEntity(){
	if(defaultposition==6)savescence="xx";

	if(TextUtils.isEmpty(etkeepdays.getText().toString())||TextUtils.isEmpty(etkeephours.getText().toString())||TextUtils.isEmpty(etkeepminutes.getText().toString())){
		Toast.makeText(this, "�����ó���ʱ��", 3).show();
		return false;
	}
	if(Integer.valueOf(etkeepdays.getText().toString())==0&&Integer.valueOf(etkeephours.getText().toString())==0&&Integer.valueOf(etkeepminutes.getText().toString())==0){
		Toast.makeText(this, "�����ó���ʱ��", 3).show();
		return false;
	}
	if(Integer.valueOf(etkeepdays.getText().toString())<416){
		if(Integer.valueOf(etkeephours.getText().toString())>=24||
				 Integer.valueOf(etkeepminutes.getText().toString())>=60||
				 Integer.valueOf(etkeepdays.getText().toString())>415){
					if(Integer.valueOf(etkeephours.getText().toString())>=24){
						Toast.makeText(this, "Сʱ��ӦΪ0-23������", 3).show();
					}
					if(Integer.valueOf(etkeepminutes.getText().toString())>=60){
						Toast.makeText(this, "������ӦΪ0-59������", 3).show();
					}
			 return false;
		}
	}else if(Integer.valueOf(etkeepdays.getText().toString())==416){
		  if(Integer.valueOf(etkeephours.getText().toString())>15||
					 Integer.valueOf(etkeepminutes.getText().toString())>=60){
				            Toast.makeText(this, "����ϵͳ����ʱ������", 3).show();
				 return false;
		  }
	}else if(Integer.valueOf(etkeepdays.getText().toString())>416){
        Toast.makeText(this, "����ϵͳ����ʱ������", 3).show();
		 return false;
	}
	if(TextUtils.isEmpty(savescence)){
		Toast.makeText(this, "�������龰", 3).show();
		return false;
	}
   return true;		 
}
	 /***************************************
	   * �����龰��������Ϣ
	   * @author zhaohao
	   * ���˵��:����ѡ��ĳ���������صı���
	   */
	  class SaveMyScenceTask extends AsyncTask<String, String, String> {  
	      
	      protected String doInBackground(String... params)
	      { 
	    	  int day=Integer.parseInt(etkeepdays.getText().toString());
			  int hour=Integer.parseInt(etkeephours.getText().toString());
			  int min=Integer.parseInt(etkeepminutes.getText().toString());
			  TSetDurationSceneResult.Enum result=dataAccess.setDurationScene(savepolicy, savescence, day*24*60+hour*60+min);
			  if(GetLastErrorStatus()){
				   if("success".equals(result.toString())){
					   return result.toString();
				   }else if("failed_not_registed".equals(result.toString())){
					   return "failed_not_registed";
				   }else{
					   return "unkonwn_error";
				   }
			  }else{
				  return dataAccess.getLastError().toString();
			  }
			
	      }  
	      protected void onCancelled() {  
	          super.onCancelled();  
	      }  
	      @SuppressWarnings("static-access")
		protected void onPostExecute(String result) 
	      { 
	    	  if(GetLastErrorStatus()){
	    		  if(result.equals(TSetDurationSceneResult.SUCCESS.toString())){
	        		  Toast.makeText(MainActivity.this, "���óɹ�!", 3).show();
	        		  savescence="";
	        		  new QuickSetupTask_ListView().execute(new String[]{"","",""});
	        		  refresh(1);
	        	  }else if(result.equals(TSetDurationSceneResult.FAILED_SS_ERROR_STATUS.toString())){
	        		   Toast.makeText(MainActivity.this, "����״̬ʧ�ܣ����Ժ����ԡ������γ���ʧ�ܣ��벦��ͷ��绰10010��ȡ����",3).show();
	        	  }else if(result.equals(TSetDurationSceneResult.FAILED_GSM_ERROR.toString())){
	        		   Toast.makeText(MainActivity.this, "״̬����ʧ�ܣ����Ժ�����",3).show();
	        	  }else if(result.equals(TSetDurationSceneResult.SYSTEM_ERROR.toString())){
	        		      Toast.makeText(MainActivity.this, "״̬����ʧ�ܣ����Ժ�����",3).show();
	        	  }else if(result.equals("unkonwn_error")){
	        		     Toast.makeText(MainActivity.this, "״̬����ʧ�ܣ����Ժ�����",3).show();
	        	  }else if(result.equals(TSetDurationSceneResult.FAILED_NOT_REGISTED.toString())){
	        		  MyDialog("����û�п�ͨ�����ҵ�񡣷��Ͷ���5��11631234������ͨ��", "��ͨ");
	        	  }
	    	  }else{
	    		  if(dataAccess.getLastError().HTTP_NEED_RELOGIN.toString().equals(dataAccess.getLastError().toString())){
	  			    Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
	                  startActivityForResult(intent,111);
	  		       }
	    		  Toast.makeText(MainActivity.this, commentTools.GetEcption(dataAccess.getLastError().toString()), 3).show();
	    	  }
	    	  dialog.hide();
	      }  
	      protected void onPreExecute() {   
	    	  dialog.show();
	      }  
	      protected void onProgressUpdate(Integer... values) { 
	      	 
	      }  
	   }
	  /*************************************
	   * ����ύ���ݺ��״̬�Ƿ�ΪSuccess״̬ 
	   * @return true or false
	   */
	  @SuppressWarnings("static-access")
	  public boolean GetLastErrorStatus(){
	  	 if(dataAccess.getLastError().equals(dataAccess.getLastError().SUCCESS))return true;
	  	 else return false;
	  }
		@SuppressWarnings("static-access")
		public boolean getServerStatus(){

		     try {
		    	 String serverurl=dataAccess.getHttpServerUrl();
		    	 if(TextUtils.isEmpty(serverurl)){
		    		 dataAccess.setHttpServerUrl(HttpDataAccess.HTTP_SERVER_URL);
		    	 }
				return  NetWorkConnectionUtil.getServerStatus(dataAccess.getHttpServerUrl());
			} catch (Exception e) {
				 return false;
			}
		}
	  /*********************************
	   * �ָ�����Listview�����ĵ���¼�
	   * @author zhaohao
	   *
	   */
	  class HfzcOnitemClickEvent implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
           if(position==3){
        	   
        	   if(line_or_offline.getInt("isline_status", 0)==0){
        		   Toast.makeText(MainActivity.this, "����δ��¼",3).show();
        		   Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
                   startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
        	   }else{
        		   if(getServerStatus()){
        			   new ReturnOkTask().execute();
        		   }else{
        			   Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
        		   }
        	   }
        		    
//        	  MyDialog();
           } else if(position==0){
				  mFeiJiWuRao();
			  }else if(position==1){
				  mQingWuDaRao();
			  }else if(position==2){
				  mFangHuSiNi();
			  }

	  }
		   
	  }
	  /*****************************************
		 * �ָ�����״̬���첽����
		 * @author zhaohao
		 *���û�����ָ�������ʱ����лָ�����
		 */
		 class ReturnOkTask extends AsyncTask<String, String, String> {  
	         
		        protected String doInBackground(String... params)
		        { 
		        	           detail="";
		        	           TSetDurationSceneResult.Enum ex_result=dataAccess.Returntonormal();
		        	           if(GetLastErrorStatus()){
		        	        	   System.out.println("�ɹ�״̬------------------>>>>"+dataAccess.getLastError().toString());
		        	        	   return ex_result.toString();
		        	           }
		        	           else{
		        	        	   System.out.println("ʧ��״̬------------------>>>>"+dataAccess.getLastError().toString());
		        	        	   return dataAccess.getLastError().toString();
		        	           }
		        }  
		        protected void onCancelled() {  
		            super.onCancelled();  
		        }  
		        @SuppressWarnings("static-access")
				protected void onPostExecute(String result) 
		        {
		        	if(GetLastErrorStatus()){
		        		if("success".equals(result)){
		        			  phone_status="����״̬";
			        		  tvpolicyscene.setText(phone_status);
			        		  Toast.makeText(MainActivity.this,"�ָ�����", 3).show();
			        		  ivthiscurrentimage.setVisibility(View.GONE);
			        		  tvthiscurrentendtime.setVisibility(View.GONE);
			        	}
		        	}else{
		        		 
		        		 Toast.makeText(MainActivity.this,commentTools.GetEcption(dataAccess.getLastError().toString()), 3).show();
		        			if(dataAccess.getLastError().HTTP_NEED_RELOGIN.toString().equals(dataAccess.getLastError().toString())){
		        			    Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
		                        startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
		        		}
		        	}
		        	
		        }  
		        protected void onPreExecute() { 
		        	 
		        }  
		        protected void onProgressUpdate(Integer... values){ 
		        	 
		        }  
		     } 
		 /*********************************
		  * �ָ����������ĶԻ�����ʾ
		  */
	 @SuppressWarnings("unused")
	private void MyDialog(){
			    AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    builder.setMessage("��ȷ��Ҫ�ָ�����״̬��?")
			           .setCancelable(false).setTitle("��ܰ��ʾ")
			           .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int id) {
			            	   if(line_or_offline.getInt("isline_status", 0)==0){
			            		   Toast.makeText(MainActivity.this, "����δ��¼",3).show();
			            		   Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
		                           startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
			            	   }else{
			            		   new ReturnOkTask().execute();
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
	 
	 
	 /**********************************
	  * ���û������龰����и��µ�ǰ״̬����ϸ��Ϣ
	  * @author zhaohao
	  * ���˵������ȡ��ǰ��Ϣ����ϸ״̬
	  */
	 class QuickSetupTask_ListView extends AsyncTask<String, String, String> { 
			protected String doInBackground(String... params)
	        { 
	        	try {
					if(!TextUtils.isEmpty(params[0])&&!TextUtils.isEmpty(params[1])&&!TextUtils.isEmpty(params[2])){
						TSetDurationSceneResult.Enum result=dataAccess.setDurationScene(params[0],params[1], Integer.valueOf(params[2]));
						if(GetLastErrorStatus()){
							if(result.equals(TSetDurationSceneResult.SUCCESS)){
							    String resultexecute=dataAccess.getDurationThisScene();
							    if(!resultexecute.contains("_")&&!resultexecute.contains(",")){
							    	phone_status=resultexecute;
							    }else if(resultexecute.contains(",")&&!resultexecute.contains("_")){
							    	phone_status=resultexecute.substring(0, resultexecute.indexOf(","));
							    	detail=resultexecute.substring(resultexecute.indexOf(",")+1, resultexecute.length());
							    }
							    return result.toString();
					    	}else if(result.equals(TSetDurationSceneResult.FAILED_SS_ERROR_STATUS)){
					    		return "failed_ss_error_status";
					    	}else if("failed_not_registed".equals(result.toString())){
					    		return "failed_not_registed";
					    	}else{
					    		return "unkonwn_error";
					    	}
						}else{
							return dataAccess.getLastError().toString();
						}
					}else{
						if(GetLastErrorStatus()){
							 String resultexecute=dataAccess.getDurationThisScene();
							    if(!resultexecute.contains("_")&&!resultexecute.contains(",")){
							    	phone_status=resultexecute;
							    }else if(resultexecute.contains(",")&&!resultexecute.contains("_")){
							    	phone_status=resultexecute.substring(0, resultexecute.indexOf(","));
							    	detail=resultexecute.substring(resultexecute.indexOf(",")+1, resultexecute.length());
							    }
							   return "success"; 
						}else 
							   return dataAccess.getLastError().toString();
					}
				} catch (NumberFormatException e) {
					 return "HTTP_SYSTEM_ERROR";
				}
	        	
	        }  
	        protected void onCancelled() {  
	            super.onCancelled();  
	        }  
			@SuppressWarnings("static-access")
			protected void onPostExecute(String result) 
	        { 
	        	if(GetLastErrorStatus()){
	        		 if(result.equals("success")){
	        			 System.out.println(" statusrefish;-------->>>"+!"statusrefish".equals(statusrefish));
	        			 System.out.println(" statusrefish;-------->>>"+statusrefish);
	        			 if(!"statusrefish".equals(statusrefish)){
	        			   Toast.makeText(MainActivity.this, "���óɹ�!", 3).show();
	        			   statusrefish="";
	        			 }
	        			 if(!"����״̬".equals(phone_status)){
	        				   ivthiscurrentimage.setVisibility(View.VISIBLE);
	        			    	scene=phone_status.substring(0, phone_status.indexOf("("));
	        			    	policy=phone_status.substring(phone_status.indexOf("(")+1, phone_status.length()-1);
	        			    	 ivthiscurrentimage.setImageResource(ArrayUnits.GetPolicyIconPosition(policy, scene));
	        			    	 tvthiscurrentendtime.setVisibility(View.VISIBLE);
	        			    	 String durations=detail.substring(detail.lastIndexOf(",")+1,detail.length());
	        	                  year=detail.substring(detail.indexOf("��")+1, detail.indexOf(","));
	        	            	  month=detail.substring(detail.indexOf("��")+1, detail.indexOf("��")-1);
	        	            	  day=detail.substring(detail.indexOf("��")+1, detail.indexOf("ʱ")-1);
	        			    	 thisstrh=Integer.valueOf(detail.substring(detail.indexOf("ʱ")+1, detail.indexOf("��")-1));
	        			         thisstrm=Integer.valueOf(detail.substring(detail.indexOf("��")+1, detail.lastIndexOf(",")));
	        			         endtime_detail=getEndTime(Integer.valueOf(year),Integer.valueOf(month),Integer.valueOf(day),thisstrh, thisstrm, Integer.valueOf(durations));
	        			         tvthiscurrentendtime.setText("����ʱ��:"+endtime_detail.substring(10, endtime_detail.length()));
	        			    	 System.out.println("��������е�detail��Ϣ----->>>>"+detail);
	        			    }else{
	        			    	 ivthiscurrentimage.setVisibility(View.GONE);
	        			    	 tvthiscurrentendtime.setVisibility(View.GONE);
	        			    }
						 if(phone_status.contains("��������")) {
							 tvpolicyscene.setText("�������� ");
						 }else {
							 tvpolicyscene.setText(phone_status);
						 }
	                  }else if("failed_ss_error_status".equals(result)){
	                	  Toast.makeText(MainActivity.this, "����״̬ʧ�ܣ����Ժ����ԡ������γ���ʧ�ܣ��벦��ͷ��绰10010��ȡ����", 3).show();
	                  }else if("failed_not_registed".equals(result)){
	                	  MyDialog("����û�п�ͨ�����ҵ�񡣷��Ͷ���5��11631234������ͨ��", "��ͨ");
	                  }else if(result.equals("unkonwn_error")){
		        		     Toast.makeText(MainActivity.this, "״̬����ʧ�ܣ����Ժ�����",3).show();
		        	  }
	        	}else{
	        		if(dataAccess.getLastError().HTTP_NEED_RELOGIN.toString().equals(dataAccess.getLastError().toString())){
	        				Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
	                        startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
	        		}
             	  Toast.makeText(MainActivity.this,commentTools.GetEcption(dataAccess.getLastError().toString()), 3).show();
               }
                
	        	
	        }  
	        protected void onPreExecute() { 
	        	
	        }
	        protected void onProgressUpdate(Integer... values) { 
	        	 
	        }  
	     } 
	 /***********************************
	  * �õ����е�ʱ������
	  * @author zhaohao
	  * ����˵��:��ȡ��ص�ʱ����������
	  */
	 class GetAllTimeScenes extends AsyncTask<String, Integer, TGetTimeSceneRsp> {

			@Override
			protected TGetTimeSceneRsp doInBackground(String... params) {
				return HttpDataAccess.getInstance().getTimeSceneScene();
			}

			@Override
			protected void onPostExecute(TGetTimeSceneRsp response) {
				if(TextUtils.isEmpty(status))progressDialog.hide();

				TGetTimeSceneResult.Enum result = null;
				if (response != null) {
					result = response.getResult();
				}

				Integer errorId = MyResources.getHttpErrorIndex(result);
				if (errorId != null) {
					if (errorId == R.string.http_access_need_relogin) {
						// ��ʾ��¼�Ի���
						Intent intent = new Intent(MainActivity.this, LoginActivity.class);
						startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
					} else {
						Toast.makeText(MainActivity.this, getResources().getString(errorId), 3).show();
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
				lvtimesettinglistview.setOnItemClickListener(MainActivity.this);
				status="";
				lvtimesettinglistview.onRefreshComplete();
			}

			@Override
			protected void onPreExecute() {
				if(TextUtils.isEmpty(status))progressDialog.show();
			}

			@Override
			protected void onProgressUpdate(Integer... values) {

			}

			@Override
			protected void onCancelled() {
				if(TextUtils.isEmpty(status))progressDialog.hide();
				super.onCancelled();
			}
		}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		 if(!DsszIsEdit){
			 DsszIsEdit=true;
			  TextView tvlistitem_id_edit=(TextView) view.findViewById(R.id.tvlistitem_id_edit);
			  String update_id=tvlistitem_id_edit.getText().toString();
			  Intent intent=new Intent();
			  intent.setClass(MainActivity.this, TimeSettingDetailActivity.class);
			  intent.putExtra("update_id", update_id);
			  intent.putExtra("updataolddata", "updataolddata");
			  startActivityForResult(intent, DataResours.REQUEST_UPDATETIMESETTING);
		  }else{
			  DsszIsEdit=true;
			  TextView tvlistitem_id_edit=(TextView) view.findViewById(R.id.tvlistitem_id);
			  String update_id=tvlistitem_id_edit.getText().toString();
			  Intent intent=new Intent();
			  intent.setClass(MainActivity.this, TimeSettingDetailActivity.class);
			  intent.putExtra("update_id", update_id);
			  intent.putExtra("updataolddata", "updataolddata");
			  startActivityForResult(intent, DataResours.REQUEST_UPDATETIMESETTING);
		  }
	}
 /***************************************************
  * ��������еĵ���¼�
  * @author zhaohao
  * ����˵��:������õĵ���¼�--�����������
  */
 class KjszListOnitemClickEvent implements OnItemClickListener{

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
      		try {
				if(!IsEdit){
					  IsEdit=true;
					  String quicksetup_edit_id=((TextView)view.findViewById(R.id.tvquick_item_line_id_edit)).getText().toString();
					  Intent intent=new Intent();
					  intent.setClass(MainActivity.this, TimeSettingDetailActivity.class);
					  intent.putExtra("quicksetup_edit_id", quicksetup_edit_id);
					  intent.putExtra("quicksetup_status", "quicksetup_status");
					  intent.putExtra("quickstatus_op", "update");
					  startActivityForResult(intent, DataResours.REQUEST_QUICKSETUP_UPDATE);
				}else{
					 final String policy=((TextView)(view.findViewById(R.id.tv_policy_quick))).getText().toString();
					final String scence=((TextView)(view.findViewById(R.id.tv_scence_quick))).getText().toString();
					 final String allminute=((TextView)(view.findViewById(R.id.tvsecondallminutes))).getText().toString();
					 if(getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status",0)==0) {
						   Toast.makeText(MainActivity.this, "����δ��¼", 3).show();
						   Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
				         startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
					   }
					   else{
						       if(getServerStatus()){
						    	   statusrefish="";
								    String _policy="�Ǽ�����".equals(policy)?"sm":"gj";
									String _scence=ArrayUnits.getResult(scence);
									new QuickSetupTask_ListView().execute(new String[]{_policy,_scence,allminute});
						       }else{
									 Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();

						       }
					   }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	 
 }
 /**      
  * ���ж����ת��    
  * @param time ����
  * @return �ַ���ΪСʱ���Ӻ���    
  */
 private String getEndTime(int year,int month,int day,int beginhour,int beginminute,int durations){
	 String mydata="";
	 try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
			  String begintime=year+"-"+month+"-"+day+" "+beginhour+":"+beginminute;
			   java.util.Date data1=format.parse(begintime);  
            long time=(data1.getTime()/1000)+60*durations;
            data1.setTime(time*1000);
              mydata=format.format(data1);
		} catch (ParseException e) {
			e.printStackTrace();
		}  
		return mydata;
    }
 /***************************************************
	 * ��¼���첽����
	 * @author zhaohao
	 * ���˵��:���û�����Զ���¼���´ν����ʱ���ִ���Զ���¼����
	 */
	 class LoginTask extends AsyncTask<String, String, String> {  
			@SuppressWarnings("static-access")
			protected String doInBackground(String... params)
	        {
				String url=getSharedPreferences("all_haslogin_detail", MODE_PRIVATE).getString(params[1], null);
				dataAccess.setHttpServerUrl(url);
	        	TUserLoginResult.Enum enums= dataAccess.loginRequest(params[1], params[2]);
	        	if(GetLastErrorStatus())
	        	{
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
	        		return dataAccess.getLastError().toString();
	        	}
				
	        }  
	        protected void onCancelled() {  
	            super.onCancelled();  
	        }  
			@SuppressWarnings("static-access")
			protected void onPostExecute(String result) 
	        {
	        	if(GetLastErrorStatus())
	        	{
	        		if("failed_not_registed".equals(result)){
		        		Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
		                startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
		                UserKaitongDialog();
		        	}else if("failed_password_error".equals(result)){
		        		Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
		                startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
		        		Toast.makeText(MainActivity.this, "�������", 3).show();
		        		 progressDialog_login.hide();
		        	}else if("success".equals(result)) {
		        		if(!statusrefish.equals("statusrefish"))
		        		Toast.makeText(MainActivity.this, "��¼�ɹ���", 3).show();
		        		line_or_offline.edit().putInt("isline_status", 1).commit();
						 progressDialog_login.hide();
						   tvshowseconduserdetail.setText(phone_num_user+",��ӭ����");
						 if(!"����״̬".equals(phone_status)){
						    	ivthiscurrentimage.setVisibility(View.VISIBLE);
						    	scene=phone_status.substring(0, phone_status.indexOf("("));
						    	policy=phone_status.substring(phone_status.indexOf("(")+1, phone_status.length()-1);
						    	ivthiscurrentimage.setImageResource(ArrayUnits.GetPolicyIconPosition(policy, scene));
				                tvthiscurrentendtime.setVisibility(View.VISIBLE);
				                String durations=detail.substring(detail.lastIndexOf(",")+1,detail.length());
				                  year=detail.substring(detail.indexOf("��")+1, detail.indexOf(","));
				            	  month=detail.substring(detail.indexOf("��")+1, detail.indexOf("��")-1);
				            	  day=detail.substring(detail.indexOf("��")+1, detail.indexOf("ʱ")-1);
						    	 thisstrh=Integer.valueOf(detail.substring(detail.indexOf("ʱ")+1, detail.indexOf("��")-1));
						         thisstrm=Integer.valueOf(detail.substring(detail.indexOf("��")+1, detail.lastIndexOf(",")));
						         endtime_detail=getEndTime(Integer.valueOf(year),Integer.valueOf(month),Integer.valueOf(day),thisstrh, thisstrm, Integer.valueOf(durations));
						         tvthiscurrentendtime.setText("����ʱ��:"+endtime_detail.substring(10, endtime_detail.length()));
						    }else{
						    	ivthiscurrentimage.setVisibility(View.GONE);
						    	tvthiscurrentendtime.setVisibility(View.GONE);
						    }
						if(phone_status.contains("��������")) {
							tvpolicyscene.setText("�������� ");
						}else {
							tvpolicyscene.setText(phone_status);
						}
		        	} 
	        	}else{
	        		if(dataAccess.getLastError().HTTP_NEED_RELOGIN.toString().equals(dataAccess.getLastError().toString())){
        			    Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
        		}
	        		  Toast.makeText(MainActivity.this,commentTools.GetEcption(dataAccess.getLastError().toString()), 3).show();
	        		  if(!statusrefish.equals("statusrefish"))
	        		  progressDialog_login.hide();
	        	}
	        
	        }  
	        protected void onPreExecute() {  
	        	if(!statusrefish.equals("statusrefish"))
	        	progressDialog_login.show();
	        }  
	        protected void onProgressUpdate(Integer... values) { 
	        	 
	        }  
	     } 
	 /********************************************************
	  * ���û�ʹ��3G�����2G����������ͨ�����ػ�ȥ�ֻ������Ȼ��ִ�����½����
	  * @author zhaohao
	  *
	  */
	 class MDLLoginTask extends AsyncTask<String, String, String> {  
			protected String doInBackground(String... params)
	        {
					    String resultexecute=dataAccess.getDurationThisScene();
					    if(GetLastErrorStatus()&&!TextUtils.isEmpty(resultexecute)){
					    	 if(!resultexecute.contains("_")&&!resultexecute.contains(",")){
							    	phone_status=resultexecute;
							    }else if(resultexecute.contains(",")&&!resultexecute.contains("_")){
							    	phone_status=resultexecute.substring(0, resultexecute.indexOf(","));
							    	detail=resultexecute.substring(resultexecute.indexOf(",")+1, resultexecute.length());
							    }
					    		return "success";
					    }else{
					    	return dataAccess.getLastError().toString(); 
					    }
					   
		
	        }  
	        protected void onCancelled() {  
	            super.onCancelled();  
	        }  
			protected void onPostExecute(String result) 
	        {
	        	if(GetLastErrorStatus())
	        	{
	        		  if("success".equals(result)) {
		        		Toast.makeText(MainActivity.this, "��¼�ɹ���", 3).show();
		        		line_or_offline.edit().putInt("isline_status", 1).commit();
						 progressDialog_login.hide();
						   tvshowseconduserdetail.setText(phone_num_user+",��ӭ����");
						 if(!"����״̬".equals(phone_status)){
						    	ivthiscurrentimage.setVisibility(View.VISIBLE);
						    	scene=phone_status.substring(0, phone_status.indexOf("("));
						    	policy=phone_status.substring(phone_status.indexOf("(")+1, phone_status.length()-1);
						    	ivthiscurrentimage.setImageResource(ArrayUnits.GetPolicyIconPosition(policy, scene));
				                tvthiscurrentendtime.setVisibility(View.VISIBLE);
				                String durations=detail.substring(detail.lastIndexOf(",")+1,detail.length());
				                  year=detail.substring(detail.indexOf("��")+1, detail.indexOf(","));
				            	  month=detail.substring(detail.indexOf("��")+1, detail.indexOf("��")-1);
				            	  day=detail.substring(detail.indexOf("��")+1, detail.indexOf("ʱ")-1);
						    	 thisstrh=Integer.valueOf(detail.substring(detail.indexOf("ʱ")+1, detail.indexOf("��")-1));
						         thisstrm=Integer.valueOf(detail.substring(detail.indexOf("��")+1, detail.lastIndexOf(",")));
						         endtime_detail=getEndTime(Integer.valueOf(year),Integer.valueOf(month),Integer.valueOf(day),thisstrh, thisstrm, Integer.valueOf(durations));
						         tvthiscurrentendtime.setText("����ʱ��:"+endtime_detail.substring(10, endtime_detail.length()));
						    }else{
						    	ivthiscurrentimage.setVisibility(View.GONE);
						    	tvthiscurrentendtime.setVisibility(View.GONE);
						    }
						  if(phone_status.contains("��������")) {
							  tvpolicyscene.setText("�������� ");
						  }else {
							  tvpolicyscene.setText(phone_status);
						  }
		        	} 
	        	}else{
	        		  Toast.makeText(MainActivity.this,commentTools.GetEcption(dataAccess.getLastError().toString()), 3).show();
		        	     progressDialog_login.hide();
	        	}
	        
	        }  
	        protected void onPreExecute() {  
	        	progressDialog_login.show();
	        }  
	        protected void onProgressUpdate(Integer... values) { 
	        }  
	     } 
	 /**********************************
	  * ��wifi״̬�µ�ʱ����ж��û������ж���ע���û����Ƿ�ע���û���
	  * ������ע���û���ʾ��ص�ע�᷽ʽ
	  * @author zhaohao
	  *
	  */
	 class NotWifiAsyncTask extends AsyncTask<String, String, String> {  
			protected String doInBackground(String... params)
	        { 
	        	String msisdn=dataAccess.getMsisdnFromServer();
	        	if (msisdn != null) { 
	    			if (dataAccess.isRegistered()) {
	    				 phone_num_user=msisdn.substring(2, msisdn.length());
                          preferences.edit().putString("user_phone_number",phone_num_user).commit();
	    				return "success";
	    			} else {
	    			  return "noregister";
	    			}
	    		} else {
	    			 return "";
	    		}
	        }  
	        protected void onCancelled() {  
	            super.onCancelled();  
	        }  
			protected void onPostExecute(String result) 
	        { 
				if(!TextUtils.isEmpty(result)&&"success".equals(result)){
               		  new MDLLoginTask().execute();
				}else if("noregister".equals(result)){
					MyDialog("����û�п�ͨ�����ҵ�񡣷��Ͷ���5��11631234������ͨ��", "��ͨ");
				}
	        }  
	        protected void onPreExecute() { 
	        }   
	        protected void onProgressUpdate(Integer... values) { 
	        }  
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
	                           PendingIntent sentIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(), 0);
	                           smsManager.sendTextMessage("11631234", null, "5", sentIntent, null);
	                           Toast.makeText(MainActivity.this, "���ŷ������", Toast.LENGTH_LONG).show();
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
	  /********************************
	   * ����û��Ƿ��¼
	   * @return
	   */
	  public boolean CheckHasLogin(){
		  if(getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status", 0)==0){
			    Toast.makeText(MainActivity.this, "����δ��¼", 3).show();
		   		Intent intent_login_detail=new  Intent(MainActivity.this, LoginActivity.class);
		        startActivityForResult(intent_login_detail, DataResours.REQUEST_LOGIN_CODE);
		        return false;
		  }
		  return true;
	  }
 class UpdateAsynTask_MDR extends AsyncTask<String, Integer, String> {  
	    String updateurl="";
	    protected String doInBackground(String... params) {
	    	if(IsBegin)
	    	{
	    		String thiscurrentversionname="";
				  try {
		    		  PackageManager pm=getPackageManager();
		    		  PackageInfo pi=pm.getPackageInfo(getPackageName(), 0);
		    		  thiscurrentversionname = pi.versionName;//��ȡ��AndroidManifest.xml�����õİ汾Name
		    	} catch (NameNotFoundException e) {
		    		thiscurrentversionname="";
		    		e.printStackTrace();
		    	}
		    	TCheckVersionRsp resultRsp=dataAccess.CheckNewVersion(thiscurrentversionname);
		    	if(GetLastErrorStatus()&&resultRsp!=null){
		    		updateurl=resultRsp.getUrl();
			    	TCheckVersionResult.Enum myenum=resultRsp.getResult();
					updateCanOrMust = myenum.toString();
				}else{
					return dataAccess.getLastError().toString();
				}
	    	}
	    	return "";  
	    }  
	    protected void onCancelled() {  
	        super.onCancelled();  
	    }  
	    //���м����صİ汾��Ϣ���Ƿ������°汾
	    protected void onPostExecute(String result) {   //�˷��������߳�ִ�У�����ִ�еĽ����Ϊ�˷����Ĳ�������
	    	if(GetLastErrorStatus()){
	    		if(!TextUtils.isEmpty(yesorno))
	  	    	  progressUpdata.hide();
	  	    	if(!"matched".equals(updateCanOrMust)){
	  	    		 LayoutInflater inflater = getLayoutInflater();
	  	    	        View layout = inflater.inflate(R.layout.my_dialog,
	  	    	          (ViewGroup) findViewById(R.id.dialog));
	  	    	        
	  	    	    String statucic=IsBegin?"��������":"�鿴���ؽ���";
	  				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	  				builder.setView(layout).setTitle("�汾����").setCancelable(false)
	  						.setPositiveButton(statucic, new DialogInterface.OnClickListener() {
	  							public void onClick(DialogInterface dialog, int id) {
	  								//���sdCard�Ƿ����
	  						    	 if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
	  						    	 {
	  						    		 Toast.makeText(MainActivity.this, "����sdCard�Ƿ�װ", Toast.LENGTH_SHORT).show();
	  						    		 if(null != alertUpdate)
	  						    		 {
	  						    			 alertUpdate.hide();
	  						    		 }
	  						    		 //�������ʱ��û��sdCardֱ���˳�
	  						    		if(!TextUtils.isEmpty(updateCanOrMust) && updateCanOrMust.equals("must_update"))
	  						    		{
	  						    			 UpdateFailedExitTask updateFailedExitTask = new UpdateFailedExitTask();
	  							    		 updateFailedExitTask.execute();
	  						    		}
	  						    	 }
	  						    	 else
	  						    	 {
	  						    		if(IsBegin){
	  	                                	IsBegin=false;
	  	                                	if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
	  	                                	{
	  	                                		Toast.makeText(MainActivity.this, "δ��⵽SD��", 3).show();
	  	                                	}else{
	  	                                		new UploadTask(MainActivity.this).execute(new String[]{updateurl});
	  	                                	}
	  	                                }else{
	  	                                	if(mydialog!=null){
	  	                                		mydialog.show();
	  	                                	}
	  	                                }
	  						    	 }
	  							}
	  						});
	  				
	  						chnowarning =(CheckBox) layout.findViewById(R.id.chnowarning);
	  						if(!TextUtils.isEmpty(updateCanOrMust) && updateCanOrMust.equals("can_update"))
	  						{
	  							//ѡ�����
	  							builder.setNegativeButton("�Ժ���˵",
	  									new DialogInterface.OnClickListener() {
	  										public void onClick(DialogInterface dialog, int id) {
	  											if(TextUtils.isEmpty(yesorno)){ 
	  												commExecute();
	  											}
	  										}
	  									});
	  							//����Ѿ�ѡ���˲����Զ����ѣ��´�������ʾ�Զ�����ѡ��
	  							warningstatus=preferences.getInt("warn", 0);
	  							if(warningstatus ==1)
	  							{
	  								chnowarning.setVisibility(View.GONE);
	  							}
	  							else
	  							{
	  								chnowarning.setOnClickListener(new OnClickListener() {
	  				    				@Override
	  				    				public void onClick(View v) {
	  				    					if(chnowarning.isChecked()){
	  				    						preferences.edit().putInt("warn", 1).commit();
	  				    					}else if(!chnowarning.isChecked()){
	  				    						preferences.edit().putInt("warn", 0).commit();
	  				    					}
	  				    				}
	  				    			});
	  							}
	  						}
	  						else
	  						{
	  							//ǿ�Ƹ���
	  							chnowarning.setVisibility(View.GONE);
	  							TextView tvUpdateAlert=(TextView) layout.findViewById(R.id.tvUpdateAlert);
	  							String strText = "<font color='red'>*</font>Ϊ����������ʹ����ѡ�����";
	  							tvUpdateAlert.setText(Html.fromHtml(strText));
	  							tvUpdateAlert.setVisibility(View.VISIBLE);
	  							builder.setNegativeButton("�˳�",
	  									new DialogInterface.OnClickListener() {
	  										public void onClick(DialogInterface dialog, int id) {
	  											 line_or_offline.edit().putInt("isline_status",0).commit();
	  							            		CookieManager.getInstance().removeAllCookie();
	  							            		android.os.Process.killProcess(android.os.Process.myPid()); 
	  										}
	  									});
	  						}
	  			   if("must_update".equals(updateCanOrMust)||("can_update".equals(updateCanOrMust)&&!TextUtils.isEmpty(yesorno))){
	  					alertUpdate = builder.create();
	  					alertUpdate.show();
	  			   }else{
                        commExecute();
	  			   }
	  				
	  			}else{
	  				if(!TextUtils.isEmpty(yesorno)){
	  					String thiscurrentversionname="";
	  					  try {
	  			    		  PackageManager pm=getPackageManager();
	  			    		  PackageInfo pi=pm.getPackageInfo(getPackageName(), 0);
	  			    		  thiscurrentversionname = pi.versionName;//��ȡ��AndroidManifest.xml�����õİ汾Name
	  			    	} catch (NameNotFoundException e) {
	  			    		thiscurrentversionname="";
	  			    		e.printStackTrace();
	  			    	}
	  					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	  					builder.setMessage("����ǰ�İ汾�Ѿ�������!\n��ǰ�汾��:"+thiscurrentversionname).setTitle("��ܰ��ʾ").setCancelable(false)
	  							.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
	  								public void onClick(DialogInterface dialog, int id) {
	  								}
	  							});
	  					AlertDialog alert = builder.create();
	  					alert.show();
	  				}else{ 
	  					commExecute();
	  				}
	  			}
	    	}else{
	    		progressUpdata.hide();
	    		commExecute();
	    		Toast.makeText(MainActivity.this, commentTools.GetEcption(result), 3).show();
	    	}
	    	
	    }  
	    //��ʼ���м��汾
	    protected void onPreExecute() {
	    	if(!TextUtils.isEmpty(yesorno))
	    	progressUpdata.show();
	    }  
	    protected void onProgressUpdate(Integer... values) {   
	        // ���½���  
	    }  
	 }   
	
 /**
	 * ����ʧ���˳�
	 * @type:   UpdateFailedExitTask
	 *
	 */
	class UpdateFailedExitTask extends AsyncTask<String, Integer, String> { 
	     protected String doInBackground(String... params) {
	    	 try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	 return "";
	     }  
	     protected void onCancelled() {  
	         super.onCancelled();  
	     }  
	     protected void onPostExecute(String result) { 
			 CookieManager.getInstance().removeAllCookie();
			 android.os.Process.killProcess(android.os.Process.myPid());
	     }  
	     protected void onPreExecute() {  
	     }  
	     protected void onProgressUpdate(Integer... values) { 

	     }  
	  }
	
	 class UploadTask extends AsyncTask<String, Integer, String> { 
		 
	     // �ɱ䳤�������������AsyncTask.exucute()��Ӧ  
	 	   File file=null;
	 	  Context context=null;
	 	  public UploadTask(Context _context) {
		           context=_context;
			}
	 	  
	 	 
	     protected String doInBackground(String... params) {
	    	
	     		File tmpFile = new File("/sdcard/mdr");
	     		if (!tmpFile.exists()) {
	     			tmpFile.mkdir();
	     		}
	     	    file = new File(tmpFile,"mdr.apk");
	     		if(!file.exists())
						try {
							file.createNewFile();
						} catch (IOException e1) {
							e1.printStackTrace();
						}

	     		try {
	     			URL url = new URL(params[0]);
	     			try {
	     				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	     				alllength=conn.getContentLength();
	     				InputStream is = conn.getInputStream();
	     				FileOutputStream fos = new FileOutputStream(file);
	     				ByteArrayBuffer arrayBuffer=new ByteArrayBuffer(1000000);
	     				byte[] buf = new byte[256];
	     				conn.connect();
	     				double count = 0;
	     				if (conn.getResponseCode() >= 400) {
	     				 
	     				} else {
	     					while (count <= 100) {
	     						if (is != null) {
	     							int numRead = is.read(buf);
	     							if (numRead <= 0) {
	     								break;
	     							} else {
	     								arrayBuffer.append(buf, 0, numRead);
	     						        publishProgress((int) ((arrayBuffer.length() / (float)alllength) * 100));
	     							}
	     						} else {
	     							break;
	     						}
	     					}
	     					fos.write(arrayBuffer.toByteArray());
	     				}
	     				conn.disconnect();
	     				fos.close();
	     				is.close();
	     			} catch (IOException e) {

	     				e.printStackTrace();
	     			}
	     		} catch (MalformedURLException e) {
	     			e.printStackTrace();
	     		}
	     	return "";
	     }  
	     protected void onCancelled() {  
	         super.onCancelled();  
	     }  
	     protected void onPostExecute(String result) { 
	    	mydialog.setProgress(0);
	     	mydialog.hide();
	     	IsBegin=true;
	 		openFile(file);
	     	
	     }  
	     protected void onPreExecute() {  
	         // ����������������������ʾһ���Ի�������򵥴���  
	     	mydialog.show();
	     }  
	     protected void onProgressUpdate(Integer... values) { 
	     	mydialog.setProgress(values[0]);

	     }  
	  }  
	//��APK�������
		private void openFile(File file) {
			
			Log.e("OpenFile", file.getName());
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file),
			"application/vnd.android.package-archive");
			startActivity(intent);
		}
		
		 /***********************************
		 * myListViewˢ���¼�
		 * @author zhaohao
		 * ����˵��:�û�������קListVIew���л�ȡ���µ����������Ϣ
		 */
		class MyListViewTimeDetailOnRefreshEvent implements OnRefreshListener{

			@Override
			public void onRefresh() {
				  status="status";
				 new GetAllTimeScenes().execute(new String[]{});
			}
			
		}
		/****************************************************
		 * �ж��Ƿ����Զ���¼�͵�¼״̬�������ݴ���
		 */
		public void commExecute(){
			 if(!autologin&&islinestatus==0){
					 if(!TextUtils.isEmpty(phone_num_user)&&!TextUtils.isEmpty(phone_pwd_user)&&"1".equals("2")){
		             	Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
		                 startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
					 }
	             }else if(autologin&&islinestatus==0){
	             	  if(!TextUtils.isEmpty(phone_num_user)&&!TextUtils.isEmpty(phone_pwd_user)){
	             		  new LoginTask().execute(new String[]{String.valueOf(27),phone_num_user,phone_pwd_user});
	             	  }else{
	             		   Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
	                       startActivityForResult(intent, DataResours.REQUEST_LOGIN_CODE);
	             	  }
	             }
		}

		@Override
		public boolean onDown(MotionEvent e) {
			
			return false;
		}
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			
			return false;
		}
		@Override
		public void onLongPress(MotionEvent e) {
			
			
		}
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			
			return false;
		}
		@Override
		public void onShowPress(MotionEvent e) {
			
			
		}
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			
			return false;
		}
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			try {
				if(null != mGestureDetector)
				{
					return mGestureDetector.onTouchEvent(event);
				}
			} catch (Exception e) {
			}
			return false;
		}

	//��ת���Ǽ�����״̬
	private void mFeiJiWuRao() {
		mLayout.setVisibility(View.VISIBLE);
		positionValues=1;
		defaultposition=2;
		savepolicy="sm";
		tvtitle_name.setText("�Ǽ�����");
		lvchoosescene.setAdapter(InitMyAdapter(DataResours.fjwricons,DataResours.fjwrValues));
		mRefresh();
	}
	//��ת���������״̬
	private void mQingWuDaRao() {
		mLayout.setVisibility(View.VISIBLE);
		positionValues=2;
		defaultposition=3;
		savepolicy="gj";
		tvtitle_name.setText("�������");
		lvchoosescene.setAdapter(InitMyAdapter(DataResours.qhdricons,DataResours.qhdrValues));
		mRefresh();
	}
	//��ת�����ý���
	private void mSettingClick() {
		defaultposition=5;
		lvmoredetail.setAdapter(InitMyAdapter_more());
		mRefresh();
	}

	//��ת����������
	private void mFangHuSiNi(){
		defaultposition=6;
		savepolicy="dj";
		savescence="xx";
		tvtitle_name.setText("��������");
		mLayout.setVisibility(View.GONE);

		mRefresh();
		//lvchoosescene.setAdapter(InitMyAdapter(DataResours.qhdricons,DataResours.qhdrValues));
	}
	private void mRefresh(){
		if(defaultposition!=6) {
			etkeepdays.setText("00");
			etkeephours.setText("02");
			etkeepminutes.setText("00");
		}else {
			etkeepdays.setText("01");
			etkeephours.setText("00");
			etkeepminutes.setText("00");
		}
		DsszIsEdit=true;
		savescence="";
		if(adapter!=null)
			adapter.ForBack();
		refresh(defaultposition);
	}
	//���ö�ʱ����
	private void mDingShiSheZhi(){
		defaultposition=4;
		linestatus=getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status", 0);
		if(linestatus==1){
			if(getServerStatus()){
				new GetAllTimeScenes().execute(new String[] {});
			}else{
				Toast.makeText(MainActivity.this, "���ʷ���������ʧ�ܣ�����������æ���߷���������ά����", 3).show();
			}
		}else{
			dssz="dssz";
			Toast.makeText(MainActivity.this, "����δ��¼", 3).show();
			Intent intent_login_detail=new  Intent(MainActivity.this, LoginActivity.class);
			startActivityForResult(intent_login_detail, DataResours.REQUEST_LOGIN_CODE);

		}
		mRefresh();
	}

	private void mHomeClick(){
		defaultposition=1;
		IsEdit=true;
		ibtneditquxiaoquick.setVisibility(View.GONE);
		ibtneditquick.setVisibility(View.VISIBLE);
		lvkjsz_list.setAdapter(quickSetupServices.getadapter(1));
		mRefresh();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(null != alertUpdate && alertUpdate.isShowing() && updateCanOrMust.equals("must_update"))
			{
				alertUpdate.hide();
				CookieManager.getInstance().removeAllCookie();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			else if(defaultposition!=1){
				mHomeClick();
			}else
			{
				showDialog();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/***************************************
	 * �����龰��������Ϣ
	 * @author zhaohao
	 * ���˵��:����ѡ��ĳ���������صı���
	 */
	class SetPhoneTask extends AsyncTask<String, String, String> {

		protected String doInBackground(String... params)
		{
			TUserLoginResult.Enum result=dataAccess.setPhoneRequest("02158541240");
			if(GetLastErrorStatus()){
				if("success".equals(result.toString())){
					return result.toString();
				}else {
					return "error";
				}
			}else{
				return dataAccess.getLastError().toString();
			}

		}
		protected void onCancelled() {
			super.onCancelled();
		}
		@SuppressWarnings("static-access")
		protected void onPostExecute(String result)
		{
			if(GetLastErrorStatus()){
				if(result.equals(TSetDurationSceneResult.SUCCESS.toString())){
					//�ֻ��������ɹ�֮�󱣴�����
					new SaveMyScenceTask().execute();

				}else if(result.equals(TSetDurationSceneResult.SYSTEM_ERROR.toString())){
					Toast.makeText(MainActivity.this, "״̬����ʧ�ܣ����Ժ�����",3).show();
				}
			}else{
				if(dataAccess.getLastError().HTTP_NEED_RELOGIN.toString().equals(dataAccess.getLastError().toString())){
					Intent intent=new  Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent,111);
				}
				Toast.makeText(MainActivity.this, commentTools.GetEcption(dataAccess.getLastError().toString()), 3).show();
			}
			dialog.hide();
		}
		protected void onPreExecute() {
			dialog.show();
		}
		protected void onProgressUpdate(Integer... values) {

		}
	}

	private void goBack(){
		if(null != alertUpdate && alertUpdate.isShowing() && updateCanOrMust.equals("must_update"))
		{
			alertUpdate.hide();
			CookieManager.getInstance().removeAllCookie();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		else if(defaultposition!=1){
			mHomeClick();
		}else
		{
			showDialog();
		}
	}


}
