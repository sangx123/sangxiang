package winway.mdr.chinaunicom.custom.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
/**
 * 自定义listView,用于解决和scrollView焦点冲突问题
 * @date:   2012-2-25
 * @type:   MyGridView
 */
public class CustomListView extends ListView {  
	  
    private boolean haveScrollbar = true;  
  
    public CustomListView(Context context) { 
        super(context);
    }  
  
    public CustomListView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public CustomListView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
    }
  
    /**  
     * 设置是否有ScrollBar，当要在ScollView中显示时，应当设置为false。 默认为 true  
     *   
     * @param haveScrollbars  
     */  
    public void setHaveScrollbar(boolean haveScrollbar) {  
        this.haveScrollbar = haveScrollbar;  
    }  
  
    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
        if (this.haveScrollbar == false) {  
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);  
            super.onMeasure(widthMeasureSpec, expandSpec);  
        } else {  
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
        }  
    }  
} 
