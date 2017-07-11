/**
 * 
 */
package winway.mdr.telecomofchina.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 
 * @author 赵豪 
 * E-mail:yuafen821@126.com
 * @version 创建时间：2012-7-13 下午01:42:53
 * 类说明
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
//    	 long savetime=getSharedPreferences("timecalc", MODE_PRIVATE).getLong("time", 0);
//    	 long calcsavetime=System.currentTimeMillis()-savetime;
//    	 当间隔为为12小时时候再同步通讯录
//    	 if(calcsavetime>12*60*60*1000){
//    		 getSharedPreferences("timecalc", MODE_PRIVATE).edit().putLong("time", System.currentTimeMillis()).commit();
//    		 LxrLoadDataTask.getIns(this).executePhoneList();
//    	 }
         if(first==0){
         	startActivity(new Intent(FirstEntryActivity.this, FunctionIntroductionActivity.class));
         	FirstEntryActivity.this.finish();
         }else{
         	 startActivity(new Intent(FirstEntryActivity.this, MainActivity.class));
         	 FirstEntryActivity.this.finish();
         }
    }
}
