package winway.mdr.chinaunicom.activity;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import winway.mdr.chinaunicom.comm.DataResours;
import winway.mdr.chinaunicom.comm.HTTPCommentTools;
import winway.mdr.chinaunicom.comm.NetWorkConnectionUtil;
import winway.mdr.chinaunicom.comm.StringUnits;
import winway.mdr.chinaunicom.entity.BlackWhiteEntity;
import winway.mdr.chinaunicom.entity.LxrEntity;
import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
import winway.mdr.chinaunicom.services.BlackWhiteServices;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.cptr.TBlackwhiteState;
import com.liz.cptr.TSetPhonebooksResult;
import com.liz.cptr.TSetPhonebooksRsp;

/***********************************
 * 添加黑白名单中的添加界面
 * @author zhaohao
 * time:2011-11-18
 * 相关功能介绍:暂无
 */
public class AddBlackWhiteActivity extends Activity 
                                  implements OnClickListener{
	String status_name="";
	TextView tvaddblack_white_title;
	Button 	btnaddblack_white_back ,btnaddblack_white_save;
	EditText et_phone_name,et_phone_number,et_phone_beizhu;
	BlackWhiteServices blackWhiteServices;
	BlackWhiteEntity blackWhiteEntity;
	BlackWhiteEntity _blackWhiteEntity;
	int black_or_white=0;
	String phone_id="";
	String enable="";
	String update_id="";
	Button btngetdatafromuserphonelist;
	HttpDataAccess dataAccess=HttpDataAccess.getInstance();
	String oldphonenumber="";
	HTTPCommentTools commentTools=HTTPCommentTools.getInstance();
	ProgressDialog progressDialog=null;
	public static ArrayList<LxrEntity> lxrEntities=new ArrayList<LxrEntity>();
   @Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.add_black_white_main);
	progressDialog = new ProgressDialog(this);
	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	progressDialog.setMessage("正在设置,请稍等...");
	InitMydata();
}
   /*****************************************************************
    * 函数名称 :InitMydata
    * 参 数说明 :无
    * 时         间 :2011-11
    * 返回值:无
    * 功能说明:初始化控件信息以及对修改时数据的恢复
    ****************************************************************/
  public void InitMydata(){
	   blackWhiteServices=new BlackWhiteServices(this);
	   status_name=getIntent().getStringExtra("status_name");
	   enable=getIntent().getStringExtra("enable");
	   phone_id=getIntent().getStringExtra("phone_id");
	   update_id=getIntent().getStringExtra("update_id");
	   tvaddblack_white_title=(TextView) this.findViewById(R.id.tvaddblack_white_title);
	   btnaddblack_white_back=(Button) this.findViewById(R.id.btnaddblack_white_back);
	   btnaddblack_white_save=(Button) this.findViewById(R.id.btnaddblack_white_save);
	   btngetdatafromuserphonelist=(Button) this.findViewById(R.id.btngetdatafromuserphonelist);
	   et_phone_name=(EditText) this.findViewById(R.id.et_phone_name);
	   lengthFilter(this, et_phone_name, 16, "超过了最大限制");
	   et_phone_number=(EditText) this.findViewById(R.id.et_phone_number);
	   et_phone_beizhu=(EditText) this.findViewById(R.id.et_phone_beizhu);
	   lengthFilterBeiZhu(this, et_phone_beizhu, 16,"超过了最大限制");
	   tvaddblack_white_title.setText(status_name);
	   black_or_white="黑名单".equals(status_name)?0:1;
	   btnaddblack_white_back.setOnClickListener(this);
	   btnaddblack_white_save.setOnClickListener(this);
	   btngetdatafromuserphonelist.setOnClickListener(this);
	   
	   if("yes".equals(enable)){
		   BlackWhiteEntity blackWhiteEntity=blackWhiteServices.GetMyBlackEntity(Integer.valueOf(phone_id), 2);
		   if(blackWhiteEntity!=null){
			   et_phone_name.setText(blackWhiteEntity.getPhone_name());
			   et_phone_number.setText(blackWhiteEntity.getPhone_number());
			   et_phone_beizhu.setText(blackWhiteEntity.getPhone_beizhu());
			   et_phone_name.setEnabled(false);
			   et_phone_number.setEnabled(false);
			   et_phone_beizhu.setEnabled(false);
			   btnaddblack_white_save.setEnabled(false);
			   et_phone_name.setFocusable(false);
			   et_phone_number.setFocusable(false);
			   et_phone_beizhu.setFocusable(false);
			   btngetdatafromuserphonelist.setVisibility(View.GONE);
		   }
	   }
	   if(!TextUtils.isEmpty(update_id)){
		    _blackWhiteEntity=blackWhiteServices.GetMyBlackEntity(Integer.valueOf(update_id), 2);
		     btngetdatafromuserphonelist.setVisibility(View.GONE);
		   if(_blackWhiteEntity!=null){
			   blackWhiteEntity=_blackWhiteEntity;
			   oldphonenumber=_blackWhiteEntity.getPhone_number();
			   et_phone_name.setText(_blackWhiteEntity.getPhone_name());
			   et_phone_number.setText(_blackWhiteEntity.getPhone_number());
			   et_phone_beizhu.setText(_blackWhiteEntity.getPhone_beizhu());
		   }
	   }
   }
  /**************************************************
   * 点击事件，相关操作有添加和修改相关操作
   */
	@Override
	public void onClick(View v) {
		 try {
			switch (v.getId()) {
			case R.id.btnaddblack_white_back:
				 this.finish();
				break;
			case R.id.btnaddblack_white_save:
				if(TextUtils.isEmpty(update_id)){
					if(CheckInpue()){ 
						if(getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status", 0)==0){
							Toast.makeText(AddBlackWhiteActivity.this, "您尚未登录", 3).show();
							Intent intent_login_detail=new  Intent(AddBlackWhiteActivity.this, LoginActivity.class);
					        startActivityForResult(intent_login_detail, DataResours.REQUEST_LOGIN_CODE);
						}else{
							if(getServerStatus()){
							  new SaveOrUpdate().execute("add");
							  }else{
								  Toast.makeText(AddBlackWhiteActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
							  }
						}
						
					}
				}else{
					if(CheckInpue())
					{
					    if(getSharedPreferences("Isonline", MODE_PRIVATE).getInt("isline_status", 0)==0){
					    	Toast.makeText(AddBlackWhiteActivity.this, "您尚未登录", 3).show();
							Intent intent_login_detail=new  Intent(AddBlackWhiteActivity.this, LoginActivity.class);
					        startActivityForResult(intent_login_detail, DataResours.REQUEST_LOGIN_CODE);
						}else{
						    if(getServerStatus()){
						    	new SaveOrUpdate().execute("update");
						    }else{
						    	 Toast.makeText(AddBlackWhiteActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
						    }
						}
					}
					
				}
			
				break;
			case R.id.btngetdatafromuserphonelist:
				  Intent i = new Intent(); 
				  i.setAction(Intent.ACTION_PICK); 
				  i.setData(ContactsContract.Contacts.CONTENT_URI); 
				  startActivityForResult(i, DataResours.REQUEST_ADD_FROMUSERPHONELIST); 
			break;
			default:
				break;
			}
		} catch (Exception e) {
			 Toast.makeText(AddBlackWhiteActivity.this, "访问服务器数据失败，可能是网络忙或者服务器正在维护！", 3).show();
			e.printStackTrace();
		}
	}
   /*****************************************************************
    * 函数名称 :CheckInpue
    * 参 数说明 :无
    * 时         间 :2011-11
    * 返回值:无
    * 功能说明:检查相关的数据是否为空
    ****************************************************************/
	public boolean CheckInpue(){
		 if(TextUtils.isEmpty(et_phone_number.getText())){
			Toast.makeText(this, "您必须输入要增加或修改的号码", 3).show();
			return false;
		}
		 if(!TextUtils.isEmpty(et_phone_name.getText())&&!checkdetail(et_phone_number.getText().toString())){
			Toast.makeText(this, "输入的电话号码有误!", 3).show();
			return false;
		}
		 if(et_phone_number.getText().toString().trim().length()<5||et_phone_number.getText().toString().trim().length()>16){
			 Toast.makeText(this, "号码应在5～20位之间，请重新输入号码", 3).show();
			 return false;
		 }
		return true;
	}
	/************************
	 * 验证号码的是否正确
	 * @param phonenumber 手机号码
	 * @return 验证通过返回true 否则返回false
	 */
	 public  boolean checkdetail(String phonenumber){
		 if(phonenumber.length()==11){
			 return isMobileNO(phonenumber);
		 }else if(phonenumber.length()!=11&&phonenumber.contains("-")){
			 return useMatches(phonenumber);
		 }else if(phonenumber.length()<8&&phonenumber.substring(0, 1).equals("0")) {
			 return false;
		 } 
		 return true;
	 }
	/*******************************************
	 * 检查用户输入的固定电话是否是正确
	 * @param phoneNum 输入的电话号码
	 * @return 检查通过的时候返回true 否则返回false
	 */
	public  boolean useMatches(String phoneNum) {
		 boolean threebool=false,fourbool=false;
		if (phoneNum != null) {
			threebool=phoneNum.matches("^([0-9]{4}-?[0-9]{8})|([0-9]{4}-?[0-9]{7})$");
			fourbool= phoneNum.matches("^([0-9]{3}-?[0-9]{8})|([0-9]{4}-?[0-9]{7})$");
			if(threebool||fourbool)return true;else return false;
		} else {
			return false;
		}
	}
	/*******************************************
	 * 检查用户输入的手机号码是否是正确
	 * @param phoneNum 输入的手机号码号码
	 * @return 检查通过的时候返回true 否则返回false
	 */
	  public  boolean isMobileNO(String mobiles){       
	        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");       
	        Matcher m = p.matcher(mobiles);       
	        return m.matches();       
	    }  
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 String phoneNumber = ""; 
         String name="";
		try {
			if (requestCode == DataResours.REQUEST_ADD_FROMUSERPHONELIST) {
				lxrEntities.clear();
				if (resultCode == RESULT_OK) {
					if (data == null) {
						return;
					}
					Uri contactData = data.getData();
					if (contactData == null) {
						return;
					}
					Cursor cursor = managedQuery(contactData, null, null, null,
							null);
					if (cursor.moveToFirst()) {
						//	                  String name = cursor.getString(cursor 
						//	                          .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)); 
						String hasPhone = cursor
								.getString(cursor
										.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
						String id = cursor.getString(cursor
								.getColumnIndex(ContactsContract.Contacts._ID));
						if (hasPhone.equalsIgnoreCase("1")) {
							hasPhone = "true";
						} else {
							hasPhone = "false";
						}
						if (Boolean.parseBoolean(hasPhone)) {
							Cursor phones = getContentResolver()
									.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
											null,
											ContactsContract.CommonDataKinds.Phone.CONTACT_ID
													+ " = " + id, null, null);
							while (phones.moveToNext()) {
								phoneNumber = phones
										.getString(phones
												.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
								name = phones
										.getString(phones
												.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
							LxrEntity entity=new LxrEntity();
							entity.setUname(name);
							entity.setUphone(phoneNumber);
							lxrEntities.add(entity);
							}
							phones.close();
						}
					}
				}
				if(lxrEntities.size()==1){
					LxrEntity lxrEntity=lxrEntities.get(0);
					String tempname=lxrEntity.getUname();
					String tempphonenumber=lxrEntity.getUphone();
					et_phone_name.setText(tempname);
					if (!TextUtils.isEmpty(tempphonenumber)) {
						if (tempphonenumber.contains("-")) {
							tempphonenumber = tempphonenumber.replace("-", "");
						}
					}
					et_phone_number.setText(tempphonenumber);
				}else{
					if(lxrEntities.size()>1){
						  Intent intent=new Intent();
						  intent.setClass(this, PhoneListActivity.class);
						  startActivityForResult(intent, DataResours.ADDNEW);
					}
				}
			}else if(requestCode == DataResours.ADDNEW&&resultCode==DataResours.RETURNNEW){
				String returnname=data.getStringExtra("user_name");
				String returnnumber=data.getStringExtra("user_phone_number");
				et_phone_name.setText(returnname);
				if (!TextUtils.isEmpty(returnnumber)) {
					if (returnnumber.contains("-")) {
						returnnumber = returnnumber.replace("-", "");
					}
				}
				et_phone_number.setText(returnnumber);
			}
		} catch (Exception e) {
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	/*******************************************
	 * 退出的时候将相关的数据集合清空掉
	 */
	@SuppressWarnings("static-access")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if(keyCode==event.KEYCODE_BACK&&event.getRepeatCount()==0){
			 lxrEntities.clear();
			 System.gc();
		 }
		return super.onKeyDown(keyCode, event);
	}
	/*****************************************
	 *  保存黑白名单的异步任务
	 * @author zhaohao
	 * 相关说明:保存黑白名单是异步任务同时提交给相关的服务器相关数据
	 * 相关参数:params中参数是add or update 添加或者是修改的标识
	 */
	 class SaveOrUpdate extends AsyncTask<String, String, String> {  
		 String add_orupdate="";
		 TSetPhonebooksRsp phonebooksRsp=null;
			protected String doInBackground(String... params)
	        { 
				add_orupdate=params[0];
				if("add".equals(params[0])){
					blackWhiteEntity=new BlackWhiteEntity();
					blackWhiteEntity.setPhone_name(et_phone_name.getText().toString().replace("", ""));
					blackWhiteEntity.setPhone_number(et_phone_number.getText().toString().toString().replace("", ""));
					blackWhiteEntity.setPhone_beizhu(et_phone_beizhu.getText().toString().toString().replace("", ""));
					blackWhiteEntity.setBlack_or_white(black_or_white);
					TBlackwhiteState.Enum mystatus=null;
					mystatus=black_or_white==0?TBlackwhiteState.BLACKLIST:TBlackwhiteState.WHITELIST;
				    phonebooksRsp=dataAccess.setBlacklist(null,et_phone_number.getText().toString().replace(" ", "").trim(), et_phone_name.getText().toString().toString().replace(" ", "").trim(),et_phone_beizhu.getText().toString().toString().replace(" ", "").trim(),mystatus);
					TSetPhonebooksResult.Enum resultsaceblackwhite=phonebooksRsp.getResult();
					if(GetLastErrorStatus()){
						if("success".equals(resultsaceblackwhite.toString())){
							boolean result=blackWhiteServices.InsertNewSettingData(blackWhiteEntity, 1);
							if(result) getSharedPreferences("black_white", MODE_PRIVATE).edit().putInt("black_white_update_status", 1).commit();
							return resultsaceblackwhite.toString();
						}else if("failed_phone_number_too_long".equals(resultsaceblackwhite.toString())){
							return "failed_phone_number_too_long";
						}else if("failed_phone_number_existed".equals(resultsaceblackwhite.toString()))
						{
							return "failed_phone_number_existed";
						}else if("failed_over_maximum_count".equals(resultsaceblackwhite.toString())){
							return "failed_over_maximum_count";
						}else if("failed_not_registed".equals(resultsaceblackwhite.toString())){
							return "failed_not_registed";
						}else if("failed_phone_number_too_short".equals(resultsaceblackwhite.toString())){
							return "failed_phone_number_too_short";
						}else {
							return "";
						}
					   
					}else{
						return dataAccess.getLastError().toString();
					}
				}else if("update".equals(params[0])){
					blackWhiteEntity.setPhone_name(et_phone_name.getText().toString());
					blackWhiteEntity.setPhone_number(et_phone_number.getText().toString());
					blackWhiteEntity.setPhone_beizhu(et_phone_beizhu.getText().toString());
		            blackWhiteEntity.setBlack_or_white(_blackWhiteEntity.getBlack_or_white());
					TBlackwhiteState.Enum mystatus=null;
					mystatus=_blackWhiteEntity.getBlack_or_white()==0?TBlackwhiteState.BLACKLIST:TBlackwhiteState.WHITELIST;
					phonebooksRsp=dataAccess.setBlacklist(oldphonenumber.replace(" ", "").trim(),et_phone_number.getText().toString().replace(" ", "").trim(), et_phone_name.getText().toString().replace(" ", "").trim(),et_phone_beizhu.getText().toString().replace(" ", "").trim(),mystatus);
					TSetPhonebooksResult.Enum _update=phonebooksRsp.getResult();
					if(GetLastErrorStatus()){
		        		 if("success".equals(_update.toString())){
		        			 boolean result=blackWhiteServices.UpdateBlack_white(blackWhiteEntity, 1);
		        			 if(result)getSharedPreferences("black_white", MODE_PRIVATE).edit().putInt("black_white_update_status", 1).commit();
		        			 return _update.toString();
		        		 }else if("failed_phone_number_too_long".equals(_update.toString())){
								return "failed_phone_number_too_long";
							}else if("failed_phone_number_existed".equals(_update.toString()))
							{
								return "failed_phone_number_existed";
							}else if("failed_over_maximum_count".equals(_update.toString())){
								return "failed_over_maximum_count";
							}else if("failed_not_registed".equals(_update.toString())){
								return "failed_not_registed";
							}else if("failed_phone_number_too_short".equals(_update.toString())){
								return "failed_phone_number_too_short";
							}else {
		        			 return "";
		        		 }
		        		
		        	 }else{
		        		 return dataAccess.getLastError().toString();
		        	 }
				}else{
					return "";
				}
			
	        }  
	        protected void onCancelled() {  
	            super.onCancelled();  
	        }  
			@SuppressWarnings("static-access")
			protected void onPostExecute(String result) 
	        { 
				progressDialog.hide();
				if(GetLastErrorStatus()){
					if("add".equals(add_orupdate)){
						 if(result.equals(TSetPhonebooksResult.SUCCESS.toString())){
							  Toast.makeText(AddBlackWhiteActivity.this, "添加成功!", 3).show();
							  AddBlackWhiteActivity.this.finish();
						  }else if(result.equals(TSetPhonebooksResult.FAILED_PHONE_NUMBER_EXISTED.toString())){
							  String detailinfo=phonebooksRsp.getExistedType().toString().equals("blacklist")?"黑名单":"白名单";
							  Toast.makeText(AddBlackWhiteActivity.this, "您要添加的号码在"+detailinfo+"中已经存在", 3).show();
						  }else if(result.equals(TSetPhonebooksResult.FAILED_OVER_MAXIMUM_COUNT.toString())){
							  Toast.makeText(AddBlackWhiteActivity.this, "您的黑白名单总号码数已经达到系统允许最大数量限制，请先删除不重要的号码再重新添加!", 3).show();
						  }else if(result.equals(TSetPhonebooksResult.FAILED_PHONE_NUMBER_TOO_SHORT.toString())){
							  Toast.makeText(AddBlackWhiteActivity.this, "输入的号码太短，不符合系统要求!", 3).show();
						  }else if(result.equals(TSetPhonebooksResult.FAILED_PHONE_NUMBER_TOO_LONG.toString())){
							  Toast.makeText(AddBlackWhiteActivity.this, "输入的号码太长，不符合系统要求!", 3).show();
						  }else if(result.equals(TSetPhonebooksResult.FAILED_NOT_REGISTED.toString())){
							  MyDialog("您还没有开通免打扰业务。发送短信5到11631234立即开通。", "开通");
						  }
					}else if("update".equals(add_orupdate)){
						if(TSetPhonebooksResult.FAILED_PHONE_NUMBER_EXISTED.toString().equals(result)){
							String detailinfo=phonebooksRsp.getExistedType().toString().equals("blacklist")?"黑名单":"白名单";
							Toast.makeText(AddBlackWhiteActivity.this, "您要修改的号码在"+detailinfo+"中已经存在", 3).show();
						}else if(TSetPhonebooksResult.SUCCESS.toString().equals(result)){
							Toast.makeText(AddBlackWhiteActivity.this, "修改成功!", 3).show();
							AddBlackWhiteActivity.this.finish();
						}else if(TSetPhonebooksResult.INVALID_PARAMETERS.toString().equals(result)){
							Toast.makeText(AddBlackWhiteActivity.this, "参数错误!", 3).show();
						}else if(result.equals(TSetPhonebooksResult.FAILED_PHONE_NUMBER_TOO_SHORT.toString())){
							  Toast.makeText(AddBlackWhiteActivity.this, "输入的号码太短，不符合系统要求!", 3).show();
						  }else if(result.equals(TSetPhonebooksResult.FAILED_PHONE_NUMBER_TOO_LONG.toString())){
							  Toast.makeText(AddBlackWhiteActivity.this, "输入的号码太长，不符合系统要求!", 3).show();
						  }else if(result.equals(TSetPhonebooksResult.FAILED_NOT_REGISTED.toString())){
							  MyDialog("您还没有开通免打扰业务。发送短信5到11631234立即开通。", "开通");
						  }
						
					}
				}else{
					  if(dataAccess.getLastError().HTTP_NEED_RELOGIN.toString().equals(dataAccess.getLastError().toString())){
			  			    Intent intent=new  Intent(AddBlackWhiteActivity.this, LoginActivity.class);
			                  startActivityForResult(intent,111);
			  		       }
					 Toast.makeText(AddBlackWhiteActivity.this,commentTools.GetEcption(dataAccess.getLastError().toString()), 3).show();
				}
				
	        }  
	        protected void onPreExecute() { 
	        	progressDialog.show();
	        	
	        }  
	        protected void onProgressUpdate(Integer... values) { 
	        	 
	        }  
	     } 
	 /**********************************
	  * 在每次请求后查看相关的错误信息
	  * @return true or false 当没有错误信息的时候进行返回true
	  */
	 @SuppressWarnings("static-access")
	 public boolean GetLastErrorStatus(){
	 	 if(dataAccess.getLastError().equals(dataAccess.getLastError().SUCCESS))return true;
	 	 else return false;
	 }
	 /**********************************
	   * 相关的提示信息
	   * @param msg 消息的内容
	   * @param itemValues Button相关的提示信息
	   */
	  private void MyDialog(String msg,final String itemValues){
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setMessage(msg)
		           .setCancelable(false).setTitle("温馨提示")
		           .setPositiveButton(itemValues, new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		            	   if("开通".equals(itemValues))
		            	   {
	            		       SmsManager smsManager = SmsManager.getDefault();
	                           PendingIntent sentIntent = PendingIntent.getBroadcast(AddBlackWhiteActivity.this, 0, new Intent(), 0);
	                           smsManager.sendTextMessage("11631234", null, "5", sentIntent, null);
	                           Toast.makeText(AddBlackWhiteActivity.this, "短信发送完成", Toast.LENGTH_LONG).show();
		            	   }
		            		
		               }
		           })
		           .setNegativeButton("取消", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    dialog.cancel();
		               }
		           });
		    AlertDialog alert = builder.create();
		    alert.show();
		    }
	  /************************************
	   * 限制输入的方法
	   * @param context
	   * @param editText
	   * @param max_length
	   * @param err_msg
	   */
	   public static void lengthFilter(final Context context, final EditText editText, final int max_length, final String err_msg) {
		     
		    InputFilter[] filters = new InputFilter[1];
		     
		    filters[0] = new InputFilter.LengthFilter(max_length) {
		    @Override
		    public CharSequence filter(CharSequence source, int start, int end,
		    Spanned dest, int dstart, int dend) {
		    int destLen = StringUnits.getCharacterNum(dest.toString()); //获取字符个数(一个中文算2个字符)
		    int sourceLen =  StringUnits.getCharacterNum(source.toString());
		    if (destLen + sourceLen > max_length) {
		    Toast.makeText(context, err_msg,Toast.LENGTH_SHORT).show();
		    return "";
		    }
		    return source;
		    }
		    };
		    editText.setFilters(filters);
		    }
	   /******************************
	    * 备注只能输入16个字符或汉字
	    * @param context
	    * @param editText
	    * @param max_length
	    * @param err_msg
	    */
	   public static void lengthFilterBeiZhu(final Context context, final EditText editText, final int max_length, final String err_msg) {
	        
	        InputFilter[] filters = new InputFilter[1];
	         
	        filters[0] = new InputFilter.LengthFilter(max_length) {
	        @Override
	        public CharSequence filter(CharSequence source, int start, int end,
	        Spanned dest, int dstart, int dend) {
	        int destLen = dest.toString().length(); //获取字符个数(一个中文算2个字符)
	        int sourceLen = source.toString().length();
	        if (destLen + sourceLen > max_length) {
	        Toast.makeText(context, err_msg,Toast.LENGTH_SHORT).show();
	        return "";
	        }
	        return source;
	        }
	        };
	        editText.setFilters(filters);
	        }
		@SuppressWarnings("static-access")
		public boolean getServerStatus(){
		     try {
				return  NetWorkConnectionUtil.getServerStatus(dataAccess.getHttpServerUrl());
			} catch (Exception e) {
				 return false;
			}
		}
}
