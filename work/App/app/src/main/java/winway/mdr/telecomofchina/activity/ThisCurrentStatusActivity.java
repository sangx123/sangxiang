package winway.mdr.telecomofchina.activity;

import java.util.Calendar;

import winway.mdr.chinaunicom.comm.ArrayUnits;
import winway.mdr.telecomofchina.activity.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ThisCurrentStatusActivity extends Activity implements OnClickListener {
	RelativeLayout rlthiscurrentstatus;
	TextView tvthiscurrent_policy,thiscurrent_sence,thiscurrent_begintime,thiscurrent_endtime;
	ImageView thiscurrent_sence_icon;
	Calendar calendar;
	 int thisstrm;
	 int thisstrh;
	 String strs;
	 String strm;
	 String strh;
   @Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(R.layout.thiscurrentstatus_layout);
	calendar=Calendar.getInstance();
	rlthiscurrentstatus=(RelativeLayout) this.findViewById(R.id.rlthiscurrentstatus);
	tvthiscurrent_policy=(TextView) this.findViewById(R.id.tvthiscurrent_policy);
    thiscurrent_sence=(TextView) this.findViewById(R.id.thiscurrent_sence);
    thiscurrent_begintime=(TextView) this.findViewById(R.id.thiscurrent_begintime);
    thiscurrent_endtime=(TextView) this.findViewById(R.id.thiscurrent_endtime);
    thiscurrent_sence_icon=(ImageView) this.findViewById(R.id.thiscurrent_sence_icon);
	rlthiscurrentstatus.setOnClickListener(this);
	String detail=getIntent().getStringExtra("detail");
	String policy=getIntent().getStringExtra("policy");
	String scene=getIntent().getStringExtra("scene");
	tvthiscurrent_policy.setText(policy);
	thiscurrent_sence.setText(scene);
	String year=detail.substring(detail.indexOf("年")+1, detail.indexOf(","));
	String month=detail.substring(detail.indexOf("月")+1, detail.indexOf("日")-1);
	String day=detail.substring(detail.indexOf("日")+1, detail.indexOf("时")-1);
	String hour=detail.substring(detail.indexOf("时")+1, detail.indexOf("分")-1);
	String minute=detail.substring(detail.indexOf("分")+1, detail.lastIndexOf(","));
	thiscurrent_sence_icon.setImageResource(ArrayUnits.GetPolicyIconPosition(policy, scene));
	hour=Integer.valueOf(hour)>=10?hour:"0"+hour;
	minute=Integer.valueOf(minute)>=10?minute:"0"+minute;
	day=Integer.valueOf(day)>=10?day:"0"+day;
	month=Integer.valueOf(month)>=10?month:"0"+month;
	thiscurrent_begintime.setText(year+"-"+month+"-"+day+" "+hour+":"+minute);
	 thisstrh=calendar.get(Calendar.HOUR_OF_DAY);
     thisstrm=calendar.get(Calendar.MINUTE);
     String endtime=getIntent().getStringExtra("endtime");
	 thiscurrent_endtime.setText(endtime);
}
	@Override
	public void onClick(View v) {
	    switch (v.getId()) {
		case R.id.rlthiscurrentstatus:
                this.finish();			
			break;

		default:
			break;
		}
	}
}
