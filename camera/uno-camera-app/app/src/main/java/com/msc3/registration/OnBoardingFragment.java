package com.msc3.registration;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.util.CirclePageIndicator;


/**
 * Created by connovatech on 6/17/2016.
 */
public class OnBoardingFragment extends AppCompatActivity {


	Button mButton;
    static String productIdDeatial = null;
	private Dialog dialog;
	private TextView enable_gps;
	private TextView enable_bt;
	private ListView deviceListView;
	private int position;
	private boolean isDialogOpen = false ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		    setContentView(R.layout.onboarding_layout);
			ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
			ImagePagerAdapter adapter = new ImagePagerAdapter();
			viewPager.setAdapter(adapter);

			CirclePageIndicator pageIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
			pageIndicator.setViewPager(viewPager);

			Button skipToSetup =(Button)findViewById(R.id.skiptosetup);
			skipToSetup.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(getApplicationContext(), MainActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);

					finish();
				}
			} );



		}



	@Override
	public void onResume() {
		super.onResume();

	}



	private class ImagePagerAdapter extends PagerAdapter {
		private int[] mfeatureTitles = new int[]{
				R.string.orbit_on_header_pir,
				R.string.orbit_ob_header_wirefree,
				R.string.orbit_on_header_dontmiss,
		};


		private int[] mfeatureImages = new int[]{
				R.drawable.p_i_r_detection,
				R.drawable.indoor_outdoor,
				R.drawable.sd_and_cloud_saving
		};

		private int[] mfeatureDescription = new int[]{
				R.string.orbit_ob_pir,
				R.string.orbit_ob_wirefree,
				R.string.orbit_ob_dontmiss
		};

		@Override
		public int getCount() {
			return mfeatureImages.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			try {
				LayoutInflater inflater = LayoutInflater.from(getApplicationContext().getApplicationContext());
				View layout = inflater.inflate(R.layout.onbording_slide_layout, null);
				TextView onboardingTitle = (TextView) layout.findViewById(R.id.onboard_heading);
				onboardingTitle.setText(getApplicationContext().getString(mfeatureTitles[position]));
				ImageView imageView = (ImageView) layout.findViewById(R.id.imageView);
				imageView.setImageResource(mfeatureImages[position]);
				TextView textView = (TextView) layout.findViewById(R.id.onboarding_feature_desc);
				textView.setText(getApplicationContext().getString(mfeatureDescription[position]));
				container.addView(layout);
				return layout;
			}catch (Exception ex){
				ex.printStackTrace();
				Intent i = new Intent(getApplicationContext(), MainActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();
				return null;
			}
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {

			LayoutInflater inflater = LayoutInflater.from(container.getContext());
			View layout = inflater.inflate(R.layout.onbording_slide_layout, null);
			container.removeView(layout);
		}
	}
}
