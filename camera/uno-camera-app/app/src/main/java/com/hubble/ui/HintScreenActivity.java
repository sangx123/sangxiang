package com.hubble.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hubbleconnected.camera.R;
import com.util.CirclePageIndicator;

/**
 * Created by Admin on 16-02-2017.
 */
public class HintScreenActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hint_screen);

		ViewPager viewPager = (ViewPager) findViewById(R.id.hint_view_pager);
		HintPageAdapter hintPageAdapter = new HintPageAdapter();
		viewPager.setAdapter(hintPageAdapter);

		CirclePageIndicator pageIndicator = (CirclePageIndicator)findViewById(R.id.hint_page_indicator);
		pageIndicator.setViewPager(viewPager);
		pageIndicator.setFillColor(Color.WHITE);
		pageIndicator.setPageColor(Color.BLACK);
		pageIndicator.setStrokeColor(Color.WHITE);
	}

	@Override
	public void onBackPressed() {
	}

	private class HintPageAdapter extends PagerAdapter{

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			try {
				LayoutInflater inflater = LayoutInflater.from(getApplicationContext().getApplicationContext());
				View layout = inflater.inflate(R.layout.hintscreen_swipe_layout, null);
				switch (position){
					case 0:
						layout.findViewById(R.id.hint1_layout).setVisibility(View.VISIBLE);
						break;
					case 1:
						layout.findViewById(R.id.hint2_layout).setVisibility(View.VISIBLE);
						break;
					case 2:
						layout.findViewById(R.id.hint3_layout).setVisibility(View.VISIBLE);
						break;
					case 3:
						layout.findViewById(R.id.hint4_layout).setVisibility(View.VISIBLE);
						break;
					case 4:
						layout.findViewById(R.id.hint5_layout).setVisibility(View.VISIBLE);
						Button gotit = (Button) layout.findViewById(R.id.button_got_it);
						gotit.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								finish();
							}
						});
						break;
				}

				container.addView(layout);
				return layout;
			}catch (Exception ex){
				finish();
				return null;
			}

		}


	}
}
