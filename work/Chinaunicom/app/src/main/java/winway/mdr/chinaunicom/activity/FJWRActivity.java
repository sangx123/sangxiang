package winway.mdr.chinaunicom.activity;

import java.util.ArrayList;
import java.util.HashMap;

import winway.mdr.chinaunicom.comm.ArrayUnits;
import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.FQMyAdapter;
import winway.mdr.chinaunicom.comm.HTTPCommentTools;
import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.liz.cptr.TSetDurationSceneResult;
/***********************************
 * 非急勿扰和请勿打扰界面
 * @author zhaohao
 * time:2011-11-17
 * 相关功能介绍: 用来设置相关的界面信息
 */
public class FJWRActivity extends Activity 
						  implements OnItemClickListener,OnClickListener,OnTouchListener{
	ListView lvchoosescene;
	int positionValues=0;
	String ischeck_values="";
	TextView tvtititlename;
	FQMyAdapter adapter=null;
	LinearLayout layouttop;
	String tophiddle="";
	String checkbacktext="";
	int checkbackid=0;
	Button btnfjwrback,btnfjwrsave;
	EditText etkeephours,etkeepdays,etkeepminutes;
	String savepolicy="";
	String savescence="";
	DatePickerDialog datePickerDialog;
	HTTPCommentTools commentTools;
	ProgressDialog dialog=null;
	HttpDataAccess dataAccess=HttpDataAccess.getInstance();
   @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fjwr_layout);
		commentTools=HTTPCommentTools.getInstance();
		dialog=new ProgressDialog(this);
		dialog.setMessage("请稍等...正在设置...");
		initMyCurrent();
	}
   
   /*****************************************************************
    * 函数名称 :initMyCurrent
    * 参 数说明 :无
    * 时         间 :2011-11
    * 返回值:无
    * 功能说明:初始化相关控件信息
    ****************************************************************/
   public void initMyCurrent(){
	   btnfjwrback=(Button) this.findViewById(R.id.btnfjwrback);
	   btnfjwrsave=(Button) this.findViewById(R.id.btnfjwrsave);
	   etkeepdays=(EditText) this.findViewById(R.id.etkeepdays);
	   etkeephours=(EditText) this.findViewById(R.id.etkeephours);
	   etkeepminutes=(EditText) this.findViewById(R.id.etkeepminutes);
        etkeephours.setFocusable(true);
	   etkeepdays.setInputType(InputType.TYPE_NULL);
	   etkeephours.setInputType(InputType.TYPE_NULL);
	   etkeepminutes.setInputType(InputType.TYPE_NULL);
       etkeepdays.setOnTouchListener(this);
       etkeephours.setOnTouchListener(this);
       etkeepminutes.setOnTouchListener(this);
	   btnfjwrback.setOnClickListener(this);
	   btnfjwrsave.setOnClickListener(this);
	   layouttop=(LinearLayout) this.findViewById(R.id.fjwr_top_layout);
	   positionValues=getIntent().getIntExtra("position", 1);
	   ischeck_values=getIntent().getStringExtra("ischeck_values");
	   checkbacktext=ischeck_values;
	   tophiddle=getIntent().getStringExtra("tophiddle");
	   savepolicy=positionValues==1?"sm":"gj";
	   if("tophiddle".equals(tophiddle)){
		   layouttop.setVisibility(View.GONE);
	   }
	   lvchoosescene=(ListView) this.findViewById(R.id.lvchoosescene);
	   lvchoosescene.setOnItemClickListener(this);
	   tvtititlename=(TextView) this.findViewById(R.id.tvtitle_name);
	   tvtititlename.setText(DataResours.itemValues[positionValues]);
	   if(positionValues==1){
		     if(TextUtils.isEmpty(ischeck_values)){
		    	 lvchoosescene.setAdapter(InitMyAdapter(DataResours.fjwricons,DataResours.fjwrValues));
		     }else{
		    	    int _position=ArrayUnits.Getitempostion(ischeck_values, 1);
				    lvchoosescene.setAdapter(InitMyAdapter(DataResours.fjwricons,DataResours.fjwrValues));
				    adapter.updateData(_position,1);
					adapter.notifyDataSetChanged();
		     }
	   }else if(positionValues==2){
		   if(TextUtils.isEmpty(ischeck_values)){
			   lvchoosescene.setAdapter(InitMyAdapter(DataResours.qhdricons,DataResours.qhdrValues));
		     }else{
	    	   int _position=ArrayUnits.Getitempostion(ischeck_values, 2);
			   lvchoosescene.setAdapter(InitMyAdapter(DataResours.qhdricons,DataResours.qhdrValues));
			   adapter.updateData(_position,1);
			   adapter.notifyDataSetChanged();
		     }
	   }
	 
   }
   /*****************************************************************
    * 函数名称 :InitMyAdapter  
    * 参 数说明 :icons--图片数组  values--字体数组 
    * 时         间 :2011-11
    * 返回值:FQMyAdapter 适配器
    * 功能说明:创建适配器
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
	  adapter=new FQMyAdapter(FJWRActivity.this,lists,R.layout.temp_listview_item, from, to);
	  return adapter;
 }


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
	    * 函数名称 :UpdataCheck  
	    * 参 数说明 :view--view信息  values--position
	    * 时         间 :2011-11
	    * 返回值:无
	    * 功能说明:更新ListView选中的状态
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


	@Override
	public void onClick(View v) {
		 switch (v.getId()) {
		case R.id.btnfjwrback:
			adapter.ForBack();
			finish();
			break;
		case R.id.btnfjwrsave:
			if("tophiddle".equals(tophiddle)){
				adapter.ForBack();
				Intent intent=new Intent();
				intent.putExtra("status_detail", checkbacktext);
				intent.putExtra("status_detail_position", checkbackid);
				setResult(DataResours.RESULT_GETSTATUS, intent);
				finish();
			  }else{
				  if(CheckInputIsnotEntity()){
					   if(getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status",0)==0) 
						 Toast.makeText(FJWRActivity.this, "您尚未登录", 3).show();
					    else new SaveMyScenceTask().execute();
				  }
			  }
			break;
		default:
			break;
		}
	}
	public boolean CheckInputIsnotEntity(){
		if(TextUtils.isEmpty(etkeepdays.getText().toString())||TextUtils.isEmpty(etkeephours.getText().toString())||TextUtils.isEmpty(etkeepminutes.getText().toString())){
			Toast.makeText(this, "请设置持续时间", 3).show();
			return false;
		}
		if(!TextUtils.isEmpty(etkeepdays.getText().toString())&&!TextUtils.isEmpty(etkeephours.getText().toString())&&!TextUtils.isEmpty(etkeepminutes.getText().toString())){
			 if(Integer.valueOf(etkeephours.getText().toString())>=24||Integer.valueOf(etkeepminutes.getText().toString())>=60){
				 Toast.makeText(this, "持续时间设置错误", 3).show();
				 return false;
			 }
		}  
		if(TextUtils.isEmpty(savescence)){
			Toast.makeText(this, "请设置情景", 3).show();
			return false;
		}
       return true;		 
	}
  @SuppressWarnings("static-access")
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	  if(event.getRepeatCount()==0&&keyCode==event.KEYCODE_BACK)
	  {
		  adapter.ForBack();
	  }
	  
	return super.onKeyDown(keyCode, event);
}
  /***************************************
   * 保存情景的任务信息
   * @author zhaohao
   * 相关说明:根据选择的场景进行相关的保存
   */
  class SaveMyScenceTask extends AsyncTask<String, String, String> {  
      
      protected String doInBackground(String... params)
      { 
    	  int day=Integer.parseInt(etkeepdays.getText().toString());
		  int hour=Integer.parseInt(etkeephours.getText().toString());
		  int min=Integer.parseInt(etkeepminutes.getText().toString());
		  TSetDurationSceneResult.Enum result=dataAccess.setDurationScene(savepolicy, savescence, day*24*60+hour*60+min);
		  if(GetLastErrorStatus()){
	    	  return result.toString();
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
        		  Intent intent=new Intent();
        		  intent.putExtra("add_status", result);
        		  setResult(DataResours.RESULT_SETMYSCENCE, intent);
        		  FJWRActivity.this.finish();
        	  }
    	  }else{
    		  if(dataAccess.getLastError().HTTP_NEED_RELOGIN.toString().equals(dataAccess.getLastError().toString())){
  			    Intent intent=new  Intent(FJWRActivity.this, LoginActivity.class);
                  startActivityForResult(intent,111);
  		       }
    		  Toast.makeText(FJWRActivity.this, commentTools.GetEcption(dataAccess.getLastError().toString()), 3).show();
    	  }
    	  dialog.hide();
    	  
      }  
      protected void onPreExecute() {   
    	  dialog.show();
      }  
      protected void onProgressUpdate(Integer... values) { 
      	 
      }  
   }
@Override
public boolean onTouch(View v, MotionEvent event) {
	 switch (v.getId()) {
	case R.id.etkeepdays:
		etkeepdays.setInputType(InputType.TYPE_CLASS_DATETIME);
		break;
	case R.id.etkeephours:
		etkeephours.setInputType(InputType.TYPE_CLASS_DATETIME);
		break;
	case R.id.etkeeptime_mins:
		etkeepminutes.setInputType(InputType.TYPE_CLASS_DATETIME);
		break;
	default:
		break; 
	}
	return false;
} 
@SuppressWarnings("static-access")
public boolean GetLastErrorStatus(){
	 if(dataAccess.getLastError().equals(dataAccess.getLastError().SUCCESS))return true;
	 else return false;
}
}
