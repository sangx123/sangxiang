/**
 * 
 */
package winway.mdr.chinaunicom.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 
 * @author �Ժ� 
 * E-mail:yuafen821@126.com
 * @version ����ʱ�䣺2012-7-13 ����01:42:53
 * ��˵��
 */
/**
 * @author Administrator
 *
 */
public class FirstEntryActivity extends Activity{
     /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	 int first=getSharedPreferences("firstshow", MODE_PRIVATE).getInt("myfirst", 0);
         if(first==0){
         	startActivity(new Intent(FirstEntryActivity.this, FunctionIntroductionActivity.class));
         	FirstEntryActivity.this.finish();
         }else{
         	 startActivity(new Intent(FirstEntryActivity.this, MainActivity.class));
         	 FirstEntryActivity.this.finish();
         }
    }
}
