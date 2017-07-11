package winway.mdr.chinaunicom.activity;

import winway.mdr.chinaunicom.comm.DataResours;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
/***********************************
 * 开始时间和结束时间的设置
 * @author zhaohao
 * time:2011-11-16
 * 相关功能介绍:对定时设置中的开始时间和结束时间的设置
 */
public class BeginEndSettingActivity extends Activity  
                                    implements OnClickListener{
	TimePicker tptimesetting;
	Button btngetsettime,btngettimecancel;
	int begin_or_end=0,hour=0,mminute=0;
	TextView tvbeginandendstatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.begin_end_setting_layout);
    	begin_or_end=getIntent().getIntExtra("begin_or_end", 1);
    	initMydata();
    }
    /*****************************************************************
     * 函数名称 :initMydata
     * 参 数说明 :无
     * 时         间 :2011-11
     * 返回值:无
     * 功能说明:初始化相关的控件信息
     ****************************************************************/
    public void initMydata(){
    	tptimesetting=(TimePicker) this.findViewById(R.id.tptimesetting);
    	btngetsettime=(Button) this.findViewById(R.id.btngetsettime);
    	btngettimecancel=(Button) this.findViewById(R.id.btngettimecancel);
    	tvbeginandendstatus=(TextView) this.findViewById(R.id.tvbeginandendstatus);
    	tptimesetting.setOnTimeChangedListener(new TimeParkChangEvent());
    	btngetsettime.setOnClickListener(this);
    	btngettimecancel.setOnClickListener(this);
    	tptimesetting.setIs24HourView(true);
    	String timedetail=begin_or_end==1?"开始时间":"结束时间";
        tvbeginandendstatus.setText(timedetail);
        if(begin_or_end==1){
        	tptimesetting.setCurrentHour(22);
        	tptimesetting.setCurrentMinute(0);
        }else{
        	tptimesetting.setCurrentHour(7);
        	tptimesetting.setCurrentMinute(0);
        }
    	 
    }
	@Override
	public void onClick(View v) {
		 switch (v.getId()) {
		case R.id.btngetsettime:
			 Intent intent=new Intent();
			  String _hour=hour>=10?hour+"":"0"+hour;
			  String _minute=mminute>=10?mminute+"":"0"+mminute;
	    	  String result=_hour+":"+_minute;
			  intent.putExtra("resulttime", result);
			  if(begin_or_end==1){
		           setResult(DataResours.RESULT_BEGINTIME, intent);
			  }else if(begin_or_end==2){
				   setResult(DataResours.RESULT_ENDTIME, intent);
			  }
			  finish();
			break;
		case R.id.btngettimecancel:
			 finish();
			 break;
		default:
			break;
		}
	}
	class TimeParkChangEvent implements OnTimeChangedListener
	{
		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
			   hour=tptimesetting.getCurrentHour();
			   mminute=tptimesetting.getCurrentMinute();
		}
	}
	 
}
