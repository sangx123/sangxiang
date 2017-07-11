package winway.mdr.chinaunicom.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowDetailActivity extends Activity implements OnClickListener {
	LinearLayout llshowdetail_id;
	TextView tvshowdetail_policy,tvshowdetail_content;
	String phone_static="",detail="";
   @Override
protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	   setContentView(R.layout.show_detail_layout);
	   llshowdetail_id=(LinearLayout) this.findViewById(R.id.llshowdetail_id);
	   llshowdetail_id.setOnClickListener(this);
	   tvshowdetail_policy=(TextView) this.findViewById(R.id.tvshowdetail_policy);
	   tvshowdetail_content=(TextView) this.findViewById(R.id.tvshowdetail_content);
	   phone_static=getIntent().getStringExtra("phone_status");
	   detail=getIntent().getStringExtra("detail");
	   tvshowdetail_policy.setText("您当前的状态:\n\t\t\t\t\t"+phone_static);
	   tvshowdetail_content.setText(detail);
	   System.out.println(detail);
	String havesec=detail.substring(detail.lastIndexOf(",")+1,detail.length());
	   String begin=detail.substring(0, detail.lastIndexOf(","));
	   tvshowdetail_content.setText(begin+",剩余时间:"+getAllTime(Long.parseLong(havesec)));
   }
@Override
public void onClick(View v) {
	 if(v.getId()==R.id.llshowdetail_id){
		 finish();
	 }
}

/**      
 * 进行对秒的转化    
 * @param time 秒数
 * @return 字符串为小时分钟和秒    
 */
private String getAllTime(long time){
  		long  s; 
  		long  h; 
  		long  m;  
  		String strs;
  		String strm;
  		String strh;
  		h=time/60/60;
  	    if(h<10){
  	    	strh="0"+h;
  	    }else strh=""+h;
  		m=(time-h*60*60)/60; 
  		if(m<10){
  			strm = "0"+m;
  		}else{
  			strm=""+m;
  		}
  		s=time-h*60*60-m*60;  
  		if(s<10){
  			strs = "0"+s;
  		}else{
  			strs=""+s;
  		}
  		return  strh+":"+strm+":"+strs;
   }

}
