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
 * ��ӭ����(��������)
 * @author zhaohao
 * time:2011-11-15
 * ��ع��ܽ���:����
 */
public class WelcomeActivity extends Activity {
     ImageView imageView;
     String networkstatus="";
	// ��������
	@Override
	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ʵ��ȫ��
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_layout);
        imageView=(ImageView) this.findViewById(R.id.ivwelcome);
        imageView.setImageResource(welcome());
		if (isAirMode()) {
			show_msg("�벻Ҫ�ڷ���ģʽ��ʹ�ñ����.");
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
	   * �������� : isNetworkAvailable
	   * �� ��˵�� :��
	   * ʱ         �� :2011-11
	   * ����ֵ:��
	   * ����˵��:�ж����統ǰ��״̬
	   ****************************************************************/ 
	public void isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) {
			show_msg("����������������뷽ʽ");
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
			x.postDelayed(new splashhandler(), 2000); // ����������ͣ����
		}
	}

	public boolean isAirMode() {
		return Settings.System.getInt(getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}

	public void show_msg(String msg) {
		new AlertDialog.Builder(this).setTitle("��ʾ:").setMessage(msg)// .show();
				.setMessage(msg).setIcon(R.drawable.icon).setPositiveButton(
						"ȷ��", new DialogInterface.OnClickListener() {
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
