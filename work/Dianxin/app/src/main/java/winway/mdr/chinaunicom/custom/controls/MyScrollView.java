package winway.mdr.chinaunicom.custom.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;
public class MyScrollView extends ScrollView {
    GestureDetector gestureDetector;
    
    public MyScrollView(Context context) {
            super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
    }

    public void setGestureDetector(GestureDetector gestureDetector) {
            this.gestureDetector = gestureDetector;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	super.onTouchEvent(ev);
    	if(null != gestureDetector)
        {
            return gestureDetector.onTouchEvent(ev);
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        super.dispatchTouchEvent(ev);
         return true;
    }
}






