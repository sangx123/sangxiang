package winway.mdr.chinaunicom.custom.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
/**
 * �Զ���listView,���ڽ����scrollView�����ͻ����
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
     * �����Ƿ���ScrollBar����Ҫ��ScollView����ʾʱ��Ӧ������Ϊfalse�� Ĭ��Ϊ true  
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
