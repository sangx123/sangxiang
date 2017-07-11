package winway.mdr.telecomofchina.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.MyAdapter;
import winway.mdr.chinaunicom.entity.TimeSettingEntity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.webkit.CookieManager;
import android.widget.Toast;

public class BaseActivity extends Activity {
	ProgressDialog mydialog;
	/***********************
	 * �жϷ���ģʽ
	 * @return true or false
	 */
	public boolean isAirMode() {
		return Settings.System.getInt(getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}
	public void AboutMDR(){
		String thiscurrentversionname="";
		  try {
    		  PackageManager pm=getPackageManager();
    		  PackageInfo pi=pm.getPackageInfo(getPackageName(), 0);
    		  thiscurrentversionname = pi.versionName;//��ȡ��AndroidManifest.xml�����õİ汾Name
    	} catch (NameNotFoundException e) {
    		thiscurrentversionname="";
    		e.printStackTrace();
    	}
    	Builder builder=new Builder(this);
    	builder.setTitle("����");
    	builder.setMessage("��ǰ�汾��:"+thiscurrentversionname+"\n��Ϊ�Ƽ� ��Ȩ����"+"\n�ͷ����� support@iwinway.com");
    	builder.create().show();
	}
	 /*****************************************************************
	  * �������� :showDialog  
	  * �� ��˵�� :��
	  * ʱ         �� :2011-11
	  * ����ֵ:��
	  * ����˵��:�˳�ʱ��ĶԻ���
	  ****************************************************************/
	protected void showDialog(){
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setMessage("��ȷ��Ҫ�˳��������?")
		           .setCancelable(false).setIcon(R.drawable.icon).setTitle("�����")
		           .setPositiveButton("��", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		            	   if(getSharedPreferences("Isonline", MODE_PRIVATE)!=null)
		            		 getSharedPreferences("Isonline", MODE_PRIVATE).edit().putInt("isline_status",0).commit();
		            		CookieManager.getInstance().removeAllCookie();
		            		android.os.Process.killProcess(android.os.Process.myPid());
		            		
		               }
		           })
		           .setNegativeButton("��", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    dialog.cancel();
		               }
		           });
		    AlertDialog alert = builder.create();
		    alert.show();
		    }
	
	  public MyAdapter InitMyAdapter()
	  {
		  MyAdapter adapter=null;
		  String[] from={"image","resources"};
		  int[] to={R.id.ivitemicon,R.id.tvitemtext};
		  ArrayList<Map<String, Object>> lists=new ArrayList<Map<String,Object>>();
			 Map<String, Object> map=new HashMap<String, Object>();
			 map.put("image", R.drawable.hfzc);
			 map.put("resources","恢复为正常状态");
			 lists.add(map);
		  adapter=new MyAdapter(BaseActivity.this,lists,R.layout.listhfzc_item, from, to);
		  return adapter;
	  }
	  /*****************************************************************
	   * �������� :InitQuickSetUpdata  
	   * �� ��˵�� :��
	   * ʱ         �� :2011-11
	   * ����ֵ: ArrayList<TimeSettingEntity>
	   * ����˵��:��һ�������ص�������Ϣ
	   ****************************************************************/
	 public ArrayList<TimeSettingEntity> InitQuickSetUpdata(){
		 ArrayList<TimeSettingEntity> lists=new ArrayList<TimeSettingEntity>();
		 TimeSettingEntity entity_one=new TimeSettingEntity();
		 entity_one.setDetailrepeat("ÿ��");
		 entity_one.setBegintime("1");
		 entity_one.setEndtime("00");
		 entity_one.setTimesetting_scene("��Ϣ");
		 entity_one.setTimesetting_status("�Ǽ�����");
		 entity_one.setTimesetting_icon_id(0);
		 entity_one.setBieming("");
		 TimeSettingEntity entity_two=new TimeSettingEntity();
		 entity_two.setDetailrepeat("ÿ��");
		 entity_two.setBegintime("1");
		 entity_two.setEndtime("00");
		 entity_two.setTimesetting_scene("��Ϣ");
		 entity_two.setTimesetting_status("�������");
		 entity_two.setTimesetting_icon_id(0);
		 entity_two.setBieming("");
		 TimeSettingEntity entity_three=new TimeSettingEntity();
		 entity_three.setDetailrepeat("����");
		 entity_three.setBegintime("2");
		 entity_three.setEndtime("00");
		 entity_three.setTimesetting_scene("����");
		 entity_three.setTimesetting_status("�Ǽ�����");
		 entity_three.setTimesetting_icon_id(2);
		 entity_three.setBieming("");
		 TimeSettingEntity entity_four=new TimeSettingEntity();
		 entity_four.setDetailrepeat("����");
		 entity_four.setBegintime("2");
		 entity_four.setEndtime("00");
		 entity_four.setTimesetting_scene("����");
		 entity_four.setTimesetting_status("�������");
		 entity_four.setTimesetting_icon_id(2);
		 entity_four.setBieming("");
		 lists.add(entity_one);
		 lists.add(entity_two);
		 lists.add(entity_three);
		 lists.add(entity_four);
		 return lists;
	 }
	 /**********************************
	   * ��ص���ʾ��Ϣ
	   * @param msg ��Ϣ������
	   * @param itemValues Button��ص���ʾ��Ϣ
	   */
	  public void FindAndSendMyDialog(String msg,final String itemValues,final String messageVales,final String sendnum){
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setMessage(msg)
		           .setCancelable(false).setTitle("��ܰ��ʾ")
		           .setPositiveButton(itemValues, new DialogInterface.OnClickListener() {
		               @SuppressWarnings("static-access")
					public void onClick(DialogInterface dialog, int id) {
		            	   if("��ͨҵ��".equals(itemValues))
		            	   {
		            		  int simState=((TelephonyManager) getSystemService(BaseActivity.this.TELEPHONY_SERVICE)).getSimState(); 
		            		   if(simState!= TelephonyManager.SIM_STATE_ABSENT){
		            			   SmsManager smsManager = SmsManager.getDefault();
		                           PendingIntent sentIntent = PendingIntent.getBroadcast(BaseActivity.this, 0, new Intent(), 0);
		                           smsManager.sendTextMessage(sendnum, null, messageVales, sentIntent, null);
		                           Toast.makeText(BaseActivity.this, "���ŷ������", Toast.LENGTH_LONG).show();
		            		   }else{
		            			   Toast.makeText(BaseActivity.this, "��SIM��", 3).show();
		            		   }
	            		      
		            	   }else if("�һ�����".equals(itemValues)){
		            		   System.out.println("sendnum---"+sendnum+"-----messageValues---->>>"+messageVales);
		            		   int simState=((TelephonyManager) getSystemService(BaseActivity.this.TELEPHONY_SERVICE)).getSimState(); 
		            		   if(simState!= TelephonyManager.SIM_STATE_ABSENT){
		            		   SmsManager smsManager = SmsManager.getDefault();
	                           PendingIntent sentIntent = PendingIntent.getBroadcast(BaseActivity.this, 0, new Intent(), 0);
	                           smsManager.sendTextMessage(sendnum, null, messageVales, sentIntent, null);
	                           Toast.makeText(BaseActivity.this, "��¼���뽫��ͨ�����ŷ��͵������ֻ���,��ע�����", Toast.LENGTH_LONG).show();
		            	      }else{
		            	    	  Toast.makeText(BaseActivity.this, "��SIM��", 3).show();
		            	      }
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
	  /*********************************
		 * ��ͨҵ�񲢷�����ص���Ϣ
		 * @param serverUrls
		 */
		  public void myDialogKaiTong(final int choose){ 
				if(choose==0){
           		 int simState=((TelephonyManager) getSystemService(BaseActivity.this.TELEPHONY_SERVICE)).getSimState(); 
           		   if(simState!= TelephonyManager.SIM_STATE_ABSENT){
           			   FindAndSendMyDialog("���Ͷ���QHMM��1180622�һ�����", "�һ�����","QHMM",1180622+"");
           		  
           	      }else{
           	    	  Toast.makeText(BaseActivity.this, "��SIM��", 3).show();
           	      }
           	}else if(choose==1){
           		FindAndSendMyDialog("���Ͷ���KT��10659862���յ�����ȷ�϶��ţ��ظ��󣬿�ͨ�����ҵ��", "��ͨҵ��","KT",10659862+"");
           	}else if(choose==2){
           		Intent intent=new Intent(BaseActivity.this, YeWuActivity.class);
           		startActivity(intent);
           	}
		  }
		  /*********************************
			 *�һ�����
			 * @param serverUrls
			 */
			  public void myDialogForGetPwd(){
		        AlertDialog.Builder builder = new AlertDialog.Builder(
		                BaseActivity.this);
		        builder.setTitle("��ѡ�����ʡ��") // ����
		                .setIcon(R.drawable.icon) // icon
		                .setCancelable(true) // ����Ӧback��ť
		                .setItems(DataResours.allValues, new DialogInterface.OnClickListener() {
		                    @SuppressWarnings("static-access")
							@Override
		                    public void onClick(DialogInterface dialog, int which) {
		                    		 int simState=((TelephonyManager) getSystemService(BaseActivity.this.TELEPHONY_SERVICE)).getSimState(); 
				            		   if(simState!= TelephonyManager.SIM_STATE_ABSENT){
				            		   SmsManager smsManager = SmsManager.getDefault();
			                           PendingIntent sentIntent = PendingIntent.getBroadcast(BaseActivity.this, 0, new Intent(), 0);
			                           smsManager.sendTextMessage(DataResours.sendnum[which], null, "QHMM", sentIntent, null);
			                           Toast.makeText(BaseActivity.this, "��¼���뽫��ͨ�����ŷ��͵������ֻ���,��ע�����", Toast.LENGTH_LONG).show();
				            	      }else{
				            	    	  Toast.makeText(BaseActivity.this, "��SIM��", 3).show();
				            	      }
		                    	
		                    }
		                });
		        // ����Dialog����
		        AlertDialog dlg = builder.create();
		        dlg.show();
			  }
			  
			  
			  /*****************************************************************
				  * �������� :UserKaitongDialog  
				  * �� ��˵�� :��
				  * ʱ         �� :2012-7-3
				  * ����ֵ:��
				  ****************************************************************/
				protected void UserKaitongDialog(){
					    AlertDialog.Builder builder = new AlertDialog.Builder(this);
					    builder.setMessage("�˺���δ��ͨ�����ҵ�����ȿ�ͨҵ��")
					           .setCancelable(false).setIcon(R.drawable.icon).setTitle("�����")
					           .setPositiveButton("��ͨҵ��", new DialogInterface.OnClickListener() {
					               public void onClick(DialogInterface dialog, int id) {
					            	   myDialogKaiTong(1);
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
				
				
}
