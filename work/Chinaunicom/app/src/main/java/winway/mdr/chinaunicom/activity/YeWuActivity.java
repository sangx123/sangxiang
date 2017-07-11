package winway.mdr.chinaunicom.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
/****************************************
 * 业务帮助界面
 * @author zhaohao
 */
public class YeWuActivity extends Activity implements OnClickListener {
	Button btnyewu_back;
	WebView wbyewudetail;
	private WebSettings ws = null;
	ProgressDialog progressDialog=null;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
         setContentView(R.layout.yewu_main);
         btnyewu_back=(Button) this.findViewById(R.id.btnyewu_back);
         wbyewudetail=(WebView) this.findViewById(R.id.wbyewudetail);
         wbyewudetail.requestFocus();
         progressDialog=new ProgressDialog(this);
         progressDialog.setMessage("请稍等,正在加载...");
		ws = wbyewudetail.getSettings();
		ws.setBuiltInZoomControls(false);
		ws.setJavaScriptEnabled(true);
		wbyewudetail.setVerticalScrollBarEnabled(false);
		ws.setBuiltInZoomControls(true);// 设置支持缩放
         btnyewu_back.setOnClickListener(this);
         wbyewudetail.loadUrl("http://220.196.52.191/sachelp.htm");
         wbyewudetail.setWebViewClient(new WebViewClient(){
        	 @Override
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	    wbyewudetail.loadUrl(url);
        		return super.shouldOverrideUrlLoading(view, url);
        	}
        	 @Override
        	public void onPageStarted(WebView view, String url, Bitmap favicon) {
        		super.onPageStarted(view, url, favicon);
        		progressDialog.show();
        	}
        	 @Override
        	public void onPageFinished(WebView view, String url) {
        		super.onPageFinished(view, url);
        		progressDialog.hide();
        	}
         });
     }
	@Override
	public void onClick(View v) {
		 switch (v.getId()) {
		case R.id.btnyewu_back:
			 this.finish();
			break;
		default:
			break;
		}
		
	}
}
