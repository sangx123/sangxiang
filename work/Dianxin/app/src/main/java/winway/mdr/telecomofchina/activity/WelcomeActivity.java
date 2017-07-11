package winway.mdr.telecomofchina.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import winway.mdr.telecomofchina.activity.R;

/***********************************
 * 欢迎界面(启动界面)
 * @author zhaohao
 * time:2011-11-15
 * 相关功能介绍:暂无
 */
public class WelcomeActivity extends Activity {
     ImageView imageView;
     String networkstatus="";
	// 启动界面
	@Override
	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 实现全屏
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_layout);
        imageView=(ImageView) this.findViewById(R.id.ivwelcome);
        imageView.setImageResource(welcome());
		if (isAirMode()) {
			show_msg("请不要在飞行模式下使用本软件.");
		} else {
			isNetworkAvailable();
		}
	}
	class splashhandler implements Runnable {

		public void run() {
			Intent intent=new Intent();
			intent.setClass(WelcomeActivity.this,MainActivity.class);
			intent.putExtra("networkstatus", networkstatus);
			startActivity(intent);
			WelcomeActivity.this.finish();
		}

	}
	 /*****************************************************************
	   * 函数名称 : isNetworkAvailable
	   * 参 数说明 :无
	   * 时         间 :2011-11
	   * 返回值:无
	   * 功能说明:判断网络当前的状态
	   ****************************************************************/ 
	public void isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) {
			show_msg("请设置您的网络接入方式");
		} else {
			  int nType = networkinfo.getType(); 
		        if(nType==ConnectivityManager.TYPE_MOBILE){ 
		            if(networkinfo.getExtraInfo().toLowerCase().equals("cmnet")){ 
		            	networkstatus="cmnet";
		            } 
		            else{ 
		            	networkstatus="cmwap";;
		            } 
		        } 
		        else if(nType==ConnectivityManager.TYPE_WIFI){ 
		        	networkstatus="wifi";
		        } 
			Handler x = new Handler();
			x.postDelayed(new splashhandler(), 2000); // 启动画面暂停两秒
		}
	}

	public boolean isAirMode() {
		return Settings.System.getInt(getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}

	public void show_msg(String msg) {
		new AlertDialog.Builder(this).setTitle("提示:").setMessage(msg)// .show();
				.setMessage(msg).setIcon(R.drawable.icon).setPositiveButton(
						"确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								finish();
							}
						}).show();

	}
	public int welcome()
	{
		
		 DisplayMetrics dm = new DisplayMetrics();   
	     getWindowManager().getDefaultDisplay().getMetrics(dm);   
	     int screenWidth = dm.widthPixels;   
	     int screenHeight = dm.heightPixels;
	     if(screenWidth==320&&screenHeight==480)
	     {
	         return  R.drawable.welcome;
	     }else if(screenWidth==480&&screenHeight==800)
	     {
	    	 return  R.drawable.welcome;
	     }else if(screenWidth==240&&screenHeight==320)
	     {
	    	 return  R.drawable.welcome;
	     }
	     else if(screenWidth==480&&screenHeight==854)
	     {
	    	 return  R.drawable.welcome_854;
	     }else if(screenWidth==540&&screenHeight==960){
	    	 return  R.drawable.welcome_960;
	     }
	       return  R.drawable.welcome_960;
	}
}
