package winway.mdr.chinaunicom.activity;

import java.util.ArrayList;
import java.util.List;

import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.MyPagerAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**************************
 * 主界面（GridView 网格界面的导航） Time:2011-06-24
 * 
 * @author zhaohao
 */
public class FunctionIntroductionActivity extends Activity implements OnClickListener{

	String imageurl_1, imageurl_2, imageurl_3;
	ImageView ivfirstimage, ivsecondimage, ivthreeimage, ivfourimage;
	// 相关的菜单选项
	RelativeLayout layouthide = null;
	private ViewPager viewPager1;
	private List<View> alViews;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.functionintroduction_activity);
		InitData();
	}

	public void InitData() {
		ivfirstimage = (ImageView) this.findViewById(R.id.ivfirstimage);
		ivsecondimage = (ImageView) this.findViewById(R.id.ivsecondimage);
		ivthreeimage = (ImageView) this.findViewById(R.id.ivthreeimage);
		ivfourimage = (ImageView) this.findViewById(R.id.ivfourimage);
		layouthide = (RelativeLayout) this.findViewById(R.id.hide);
		viewPager1 = (ViewPager) findViewById(R.id.viewPager1);
		alViews = new ArrayList<View>();
		
		LayoutInflater layoutInflater =LayoutInflater.from(FunctionIntroductionActivity.this);
		int size = DataResours.welcomeIcon.length;
		for(int i = 0;i<size;i++)
		{
			int imageId = DataResours.welcomeIcon[i];
			View view =layoutInflater.inflate(R.layout.welcom_img_item, null);
			view.setTag(i+"");
			view.setBackgroundResource(imageId);
			Button button = (Button) view.findViewById(R.id.btnbegin);
			if(i==size-1)
			{
				button.setOnClickListener(this);
				button.setVisibility(View.VISIBLE);
			}
			else
			{
				button.setVisibility(View.GONE);
			}
			alViews.add(view);
		}
		MyPagerAdapter myAdapter = new MyPagerAdapter(alViews);
		viewPager1.setAdapter(myAdapter);
		viewPager1.setCurrentItem(0);
		// 配置适配器的页面变化事件
		viewPager1.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	/** 191 * 页卡切换监听192 */
	public class MyOnPageChangeListener implements OnPageChangeListener {
		// 页卡1 -> 页卡3 偏移量  页面选择
		@Override
		public void onPageSelected(int arg0) {
			UpdataImageView(arg0+1);
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		@Override
		public void onPageScrollStateChanged(int arg0) {
		
		}
	}
	public void UpdataImageView(int index) {
		switch (index) {
		case 1:
			ivfirstimage.setImageResource(R.drawable.initdot3);
			ivsecondimage.setImageResource(R.drawable.initdot1);
			ivthreeimage.setImageResource(R.drawable.initdot1);
			ivfourimage.setImageResource(R.drawable.initdot1);
			break;
		case 2:
			ivfirstimage.setImageResource(R.drawable.initdot1);
			ivsecondimage.setImageResource(R.drawable.initdot3);
			ivthreeimage.setImageResource(R.drawable.initdot1);
			ivfourimage.setImageResource(R.drawable.initdot1);
			break;
		case 3:
			ivfirstimage.setImageResource(R.drawable.initdot1);
			ivsecondimage.setImageResource(R.drawable.initdot1);
			ivthreeimage.setImageResource(R.drawable.initdot3);
			ivfourimage.setImageResource(R.drawable.initdot1);
			break;
		case 4:
			ivfirstimage.setImageResource(R.drawable.initdot1);
			ivsecondimage.setImageResource(R.drawable.initdot1);
			ivthreeimage.setImageResource(R.drawable.initdot1);
			ivfourimage.setImageResource(R.drawable.initdot3);
			break;
		default:
			break;
		}

	}
/* (non-Javadoc)
 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
 */
@SuppressWarnings("static-access")
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	if(keyCode==event.KEYCODE_BACK){
		 int first=getSharedPreferences("firstshow", MODE_PRIVATE).getInt("myfirst", 0);
		 if(first==0){
			  Intent intent=new Intent();
				intent.setClass(FunctionIntroductionActivity.this,MainActivity.class);
				startActivity(intent);
				FunctionIntroductionActivity.this.finish();
		 }else{
			 FunctionIntroductionActivity.this.finish();
		 }
	}
	return super.onKeyDown(keyCode, event);
}
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.btnbegin:
			 int first=getSharedPreferences("firstshow", MODE_PRIVATE).getInt("myfirst", 0);
			 if(first==0){
				    Intent intent=new Intent();
					intent.setClass(FunctionIntroductionActivity.this,MainActivity.class);
					startActivity(intent);
					FunctionIntroductionActivity.this.finish();
			 }else{
				 FunctionIntroductionActivity.this.finish();
			 }
			break;
		}
	}
}