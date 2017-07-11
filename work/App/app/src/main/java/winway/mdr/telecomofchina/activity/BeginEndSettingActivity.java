package winway.mdr.telecomofchina.activity;

import winway.mdr.chinaunicom.comm.DataResours;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Window;

import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
/***********************************
 * ��ʼʱ��ͽ���ʱ�������
 * @author zhaohao
 * time:2011-11-16
 * ��ع��ܽ���:�Զ�ʱ�����еĿ�ʼʱ��ͽ���ʱ�������
 */
public class BeginEndSettingActivity extends FragmentActivity implements TimePickerDialog.OnTimeSetListener{
	private int begin_or_end=0;
	private TimePickerDialog timePickerDialog =null;
	 public static final String TIMEPICKER_TAG = "timepicker";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	begin_or_end=getIntent().getIntExtra("begin_or_end", 1);
    	initMydata();
    	timePickerDialog.show(getSupportFragmentManager(),TIMEPICKER_TAG);
    	 if (savedInstanceState != null) {
             TimePickerDialog tpd = (TimePickerDialog) getSupportFragmentManager().findFragmentByTag(TIMEPICKER_TAG);
             if (tpd != null) {
                 tpd.setOnTimeSetListener(this);
             }
         }
    }
    /*****************************************************************
     * �������� :initMydata
     * �� ��˵�� :��
     * ʱ         �� :2011-11
     * ����ֵ:��
     * ����˵��:��ʼ����صĿؼ���Ϣ
     ****************************************************************/
    public void initMydata(){
        if(begin_or_end==1){
        	 timePickerDialog = TimePickerDialog.newInstance(this, 22 ,0, true, false);
        }else{
        	 timePickerDialog = TimePickerDialog.newInstance(this, 7 ,0, true, false);
        }
    }
	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		 Intent intent=new Intent();
		  String _hour=hourOfDay>=10?hourOfDay+"":"0"+hourOfDay;
		  String _minute=minute>=10?minute+"":"0"+minute;
   	     String result=_hour+":"+_minute;
		  intent.putExtra("resulttime", result);
		  if(begin_or_end==1){
	           setResult(DataResours.RESULT_BEGINTIME, intent);
		  }else if(begin_or_end==2){
			   setResult(DataResours.RESULT_ENDTIME, intent);
		  }
		  finish();
	}
	@SuppressWarnings("static-access")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==event.KEYCODE_BACK){
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
