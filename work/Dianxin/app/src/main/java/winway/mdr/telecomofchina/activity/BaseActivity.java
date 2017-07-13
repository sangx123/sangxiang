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
	 * 判断飞行模式
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
    		  thiscurrentversionname = pi.versionName;//获取在AndroidManifest.xml中配置的版本Name
    	} catch (NameNotFoundException e) {
    		thiscurrentversionname="";
    		e.printStackTrace();
    	}
    	Builder builder=new Builder(this);
    	builder.setTitle("关于");
    	builder.setMessage("当前版本号:"+thiscurrentversionname+"\n永为科技 版权所有"+"\n客服邮箱 support@iwinway.com");
    	builder.create().show();
	}
	 /*****************************************************************
	  * 函数名称 :showDialog  
	  * 参 数说明 :无
	  * 时         间 :2011-11
	  * 返回值:无
	  * 功能说明:退出时候的对话框
	  ****************************************************************/
	protected void showDialog(){
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setMessage("您确定要退出免打扰吗?")
		           .setCancelable(false).setIcon(R.drawable.icon).setTitle("免打扰")
		           .setPositiveButton("是", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		            	   if(getSharedPreferences("Isonline", MODE_PRIVATE)!=null)
		            		 getSharedPreferences("Isonline", MODE_PRIVATE).edit().putInt("isline_status",0).commit();
		            		CookieManager.getInstance().removeAllCookie();
		            		android.os.Process.killProcess(android.os.Process.myPid());
		            		
		               }
		           })
		           .setNegativeButton("否", new DialogInterface.OnClickListener() {
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
		  map.put("resources","设为非急勿扰状态");
		  lists.add(map);
		  map=new HashMap<String, Object>();
		  map.put("image", R.drawable.hfzc);
		  map.put("resources","设为请勿打扰状态");
		  lists.add(map);
			map=new HashMap<String, Object>();
			 map.put("image", R.drawable.hfzc);
			 map.put("resources","设为防呼死你状态");
			 lists.add(map);
		  map=new HashMap<String, Object>();
		  map.put("image", R.drawable.hfzc);
		  map.put("resources","恢复为正常状态");
		  lists.add(map);

		  adapter=new MyAdapter(BaseActivity.this,lists,R.layout.listhfzc_item, from, to);
		  return adapter;
	  }
	  /*****************************************************************
	   * 函数名称 :InitQuickSetUpdata  
	   * 参 数说明 :无
	   * 时         间 :2011-11
	   * 返回值: ArrayList<TimeSettingEntity>
	   * 功能说明:第一次填充相关的数据信息
	   ****************************************************************/
	 public ArrayList<TimeSettingEntity> InitQuickSetUpdata(){
		 ArrayList<TimeSettingEntity> lists=new ArrayList<TimeSettingEntity>();
		 TimeSettingEntity entity_one=new TimeSettingEntity();
		 entity_one.setDetailrepeat("每天");
		 entity_one.setBegintime("1");
		 entity_one.setEndtime("00");
		 entity_one.setTimesetting_scene("休息");
		 entity_one.setTimesetting_status("非急勿扰");
		 entity_one.setTimesetting_icon_id(0);
		 entity_one.setBieming("");
		 TimeSettingEntity entity_two=new TimeSettingEntity();
		 entity_two.setDetailrepeat("每天");
		 entity_two.setBegintime("1");
		 entity_two.setEndtime("00");
		 entity_two.setTimesetting_scene("休息");
		 entity_two.setTimesetting_status("请勿打扰");
		 entity_two.setTimesetting_icon_id(0);
		 entity_two.setBieming("");
		 TimeSettingEntity entity_three=new TimeSettingEntity();
		 entity_three.setDetailrepeat("单次");
		 entity_three.setBegintime("2");
		 entity_three.setEndtime("00");
		 entity_three.setTimesetting_scene("开会");
		 entity_three.setTimesetting_status("非急勿扰");
		 entity_three.setTimesetting_icon_id(2);
		 entity_three.setBieming("");
		 TimeSettingEntity entity_four=new TimeSettingEntity();
		 entity_four.setDetailrepeat("单次");
		 entity_four.setBegintime("2");
		 entity_four.setEndtime("00");
		 entity_four.setTimesetting_scene("开会");
		 entity_four.setTimesetting_status("请勿打扰");
		 entity_four.setTimesetting_icon_id(2);
		 entity_four.setBieming("");
		 lists.add(entity_one);
		 lists.add(entity_two);
		 lists.add(entity_three);
		 lists.add(entity_four);
		 return lists;
	 }
	 /**********************************
	   * 相关的提示信息
	   * @param msg 消息的内容
	   * @param itemValues Button相关的提示信息
	   */
	  public void FindAndSendMyDialog(String msg,final String itemValues,final String messageVales,final String sendnum){
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setMessage(msg)
		           .setCancelable(false).setTitle("温馨提示")
		           .setPositiveButton(itemValues, new DialogInterface.OnClickListener() {
		               @SuppressWarnings("static-access")
					public void onClick(DialogInterface dialog, int id) {
		            	   if("开通业务".equals(itemValues))
		            	   {
		            		  int simState=((TelephonyManager) getSystemService(BaseActivity.this.TELEPHONY_SERVICE)).getSimState(); 
		            		   if(simState!= TelephonyManager.SIM_STATE_ABSENT){
		            			   SmsManager smsManager = SmsManager.getDefault();
		                           PendingIntent sentIntent = PendingIntent.getBroadcast(BaseActivity.this, 0, new Intent(), 0);
		                           smsManager.sendTextMessage(sendnum, null, messageVales, sentIntent, null);
		                           Toast.makeText(BaseActivity.this, "短信发送完成", Toast.LENGTH_LONG).show();
		            		   }else{
		            			   Toast.makeText(BaseActivity.this, "无SIM卡", 3).show();
		            		   }
	            		      
		            	   }else if("找回密码".equals(itemValues)){
		            		   System.out.println("sendnum---"+sendnum+"-----messageValues---->>>"+messageVales);
		            		   int simState=((TelephonyManager) getSystemService(BaseActivity.this.TELEPHONY_SERVICE)).getSimState(); 
		            		   if(simState!= TelephonyManager.SIM_STATE_ABSENT){
		            		   SmsManager smsManager = SmsManager.getDefault();
	                           PendingIntent sentIntent = PendingIntent.getBroadcast(BaseActivity.this, 0, new Intent(), 0);
	                           smsManager.sendTextMessage(sendnum, null, messageVales, sentIntent, null);
	                           Toast.makeText(BaseActivity.this, "登录密码将会通过短信发送到您的手机上,请注意查收", Toast.LENGTH_LONG).show();
		            	      }else{
		            	    	  Toast.makeText(BaseActivity.this, "无SIM卡", 3).show();
		            	      }
		            	   }
		               }
		           })
		           .setNegativeButton("取消", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    dialog.cancel();
		               }
		           });
		    AlertDialog alert = builder.create();
		    alert.show();
		    } 
	  /*********************************
		 * 开通业务并发送相关的信息
		 * @param serverUrls
		 */
		  public void myDialogKaiTong(final int choose){ 
				if(choose==0){
           		 int simState=((TelephonyManager) getSystemService(BaseActivity.this.TELEPHONY_SERVICE)).getSimState(); 
           		   if(simState!= TelephonyManager.SIM_STATE_ABSENT){
           			   FindAndSendMyDialog("发送短信QHMM到1180622找回密码", "找回密码","QHMM",1180622+"");
           		  
           	      }else{
           	    	  Toast.makeText(BaseActivity.this, "无SIM卡", 3).show();
           	      }
           	}else if(choose==1){
           		FindAndSendMyDialog("发送短信KT到10659862，收到二次确认短信，回复后，开通免打扰业务", "开通业务","KT",10659862+"");
           	}else if(choose==2){
           		Intent intent=new Intent(BaseActivity.this, YeWuActivity.class);
           		startActivity(intent);
           	}
		  }
		  /*********************************
			 *找回密码
			 * @param serverUrls
			 */
			  public void myDialogForGetPwd(){
		        AlertDialog.Builder builder = new AlertDialog.Builder(
		                BaseActivity.this);
		        builder.setTitle("请选择归属省份") // 标题
		                .setIcon(R.drawable.icon) // icon
		                .setCancelable(true) // 不响应back按钮
		                .setItems(DataResours.allValues, new DialogInterface.OnClickListener() {
		                    @SuppressWarnings("static-access")
							@Override
		                    public void onClick(DialogInterface dialog, int which) {
		                    		 int simState=((TelephonyManager) getSystemService(BaseActivity.this.TELEPHONY_SERVICE)).getSimState(); 
				            		   if(simState!= TelephonyManager.SIM_STATE_ABSENT){
				            		   SmsManager smsManager = SmsManager.getDefault();
			                           PendingIntent sentIntent = PendingIntent.getBroadcast(BaseActivity.this, 0, new Intent(), 0);
			                           smsManager.sendTextMessage(DataResours.sendnum[which], null, "QHMM", sentIntent, null);
			                           Toast.makeText(BaseActivity.this, "登录密码将会通过短信发送到您的手机上,请注意查收", Toast.LENGTH_LONG).show();
				            	      }else{
				            	    	  Toast.makeText(BaseActivity.this, "无SIM卡", 3).show();
				            	      }
		                    	
		                    }
		                });
		        // 创建Dialog对象
		        AlertDialog dlg = builder.create();
		        dlg.show();
			  }
			  
			  
			  /*****************************************************************
				  * 函数名称 :UserKaitongDialog  
				  * 参 数说明 :无
				  * 时         间 :2012-7-3
				  * 返回值:无
				  ****************************************************************/
				protected void UserKaitongDialog(){
					    AlertDialog.Builder builder = new AlertDialog.Builder(this);
					    builder.setMessage("此号码未开通免打扰业务，请先开通业务")
					           .setCancelable(false).setIcon(R.drawable.icon).setTitle("免打扰")
					           .setPositiveButton("开通业务", new DialogInterface.OnClickListener() {
					               public void onClick(DialogInterface dialog, int id) {
					            	   myDialogKaiTong(1);
					               }
					           })
					           .setNegativeButton("取消", new DialogInterface.OnClickListener() {
					               public void onClick(DialogInterface dialog, int id) {
					                    dialog.cancel();
					               }
					           });
					    AlertDialog alert = builder.create();
					    alert.show();
					    }
				
				
}
