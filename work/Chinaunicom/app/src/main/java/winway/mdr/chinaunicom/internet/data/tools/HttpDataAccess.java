/************************* File Information ****************************
 * Copyright(C): This is my company secrecy java source code. Any copy or change
 * from this file must agreed by my company.
 *
 ************************* History **********************************
 * Create: zhou.h.m,  Date: 2011-12-1.
 * Create Description
 *
 *  $Id$
 *
 ************************* To  Do ***********************************
 *
 ************************* Others ***********************************
 * Add anything you want to write here.
 * 
 * 
 ******************************************************************
 */
package winway.mdr.chinaunicom.internet.data.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import lizxsd.parse.IPacket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import winway.mdr.chinaunicom.comm.ArrayUnits;

import com.liz.cptgen.TCallPolicyState;
import com.liz.cptgen.TScenesState;
import com.liz.cpth.CptHPacketParse;
import com.liz.cpth.TCheckVersionReq;
import com.liz.cpth.TCheckVersionRsp;
import com.liz.cpth.TGetSacUrlByMsisdnReq;
import com.liz.cpth.TGetSacUrlByMsisdnRsp;
import com.liz.cptr.CptRPacketParse;
import com.liz.cptr.TAddTimeSceneReq;
import com.liz.cptr.TAddTimeSceneResult;
import com.liz.cptr.TAddTimeSceneRsp;
import com.liz.cptr.TBlackwhiteState;
import com.liz.cptr.TDelPhonebooksReq;
import com.liz.cptr.TDelPhonebooksResult;
import com.liz.cptr.TDelPhonebooksRsp;
import com.liz.cptr.TDurationSceneData;
import com.liz.cptr.TEditPasswordReq;
import com.liz.cptr.TEditPasswordResult;
import com.liz.cptr.TEditPasswordRsp;
import com.liz.cptr.TGetDurationStateReq;
import com.liz.cptr.TGetDurationStateResult;
import com.liz.cptr.TGetDurationStateRsp;
import com.liz.cptr.TGetMultipleOptionsReq;
import com.liz.cptr.TGetMultipleOptionsRsp;
import com.liz.cptr.TGetPhonebooksReq;
import com.liz.cptr.TGetPhonebooksResult;
import com.liz.cptr.TGetPhonebooksRsp;
import com.liz.cptr.TGetTimeSceneReq;
import com.liz.cptr.TGetTimeSceneRsp;
import com.liz.cptr.TOptionPair;
import com.liz.cptr.TOptionPairIdDefine;
import com.liz.cptr.TPhonebookReturn;
import com.liz.cptr.TReplaceTimeSceneReq;
import com.liz.cptr.TReplaceTimeSceneResult;
import com.liz.cptr.TReplaceTimeSceneRsp;
import com.liz.cptr.TSetDurationSceneReq;
import com.liz.cptr.TSetDurationSceneResult;
import com.liz.cptr.TSetDurationSceneRsp;
import com.liz.cptr.TSetMultipleOptionsReq;
import com.liz.cptr.TSetMultipleOptionsResult;
import com.liz.cptr.TSetMultipleOptionsRsp;
import com.liz.cptr.TSetPhoneReq;
import com.liz.cptr.TSetPhoneRsp;
import com.liz.cptr.TSetPhonebooksReq;
import com.liz.cptr.TSetPhonebooksRsp;
import com.liz.cptr.TTimeSceneData;
import com.liz.cptr.TUserLoginReq;
import com.liz.cptr.TUserLoginResult;
import com.liz.cptr.TUserLoginRsp;

/**
 * The access for cpt_r.
 * 
 * @author
 */
public class HttpDataAccess {


	static private HttpDataAccess instance = new HttpDataAccess();
	Vector<TOptionPair> options=null;
	public static HttpDataAccess getInstance() {
		return instance;
	}
 
	static final public String HTTP_SERVER_URL = "http://ms.zj165.com/sac/android.r";
	static final public String HTTP_SERVER_URL_VERSION = "http://ms.zj165.com/sac/android.h";
	static final public String HTTP_GETURL_SAC_BY_MISI="http://www.uc186.com/sac/android.h";
	public static String HttpServerUrl="";
 
	public static String getHttpServerUrl() {
		return HttpServerUrl;
	}
	public static void setHttpServerUrl(String httpServerUrl) {
		HttpServerUrl = httpServerUrl;
	}
	public static enum LastError {
		SUCCESS, HTTP_EXCEPTION, // send the request to the server got an  exception.
		HTTP_SYSTEM_ERROR, // the sac server error
		HTTP_NEED_RELOGIN, // the session expired, need relogin.
		MSISDN_EMPTY, // not set the msisdn.
		INPUT_PARAMETER_ERROR
	};

	private HttpDataAccess() {
		options=new Vector<TOptionPair>();
	}
	protected List<Cookie> cookies = null;
    LastError lastError = LastError.SUCCESS;
	protected String msisdn = null;

	/**
	 * @return Returns the msisdn.
	 */
	public String getMsisdn() {
		return msisdn;
	}

	boolean registered = false;

	/**
	 * @return Returns the registered.
	 */
	public boolean isRegistered() {
		return registered;
	}
	/**
	 * @return Returns the globalResponse.
	 */
	public LastError getLastError() {
		return lastError;
	}

	static private int seqId = 0;
	/**
	 * get the seqId for the communication.
	 * @return
	 */
	public static synchronized int getSeqId() {
		return ++seqId;
	}

	/**
	 * Convert the InputStream to the String.
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public String inputStreamToString(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int bytesRead = 0;
		while (true) {
			bytesRead = is.read(b);
			if (bytesRead < 1) {
				break;
			}
			baos.write(b, 0, bytesRead);
		}
		return baos.toString(HTTP.UTF_8);
	}

	/**
	 * invoke the response by the request.
	 * 
	 * @param request
	 * @return
	 */
	public IPacket httpInvoke(IPacket request) {
		try {
			HttpPost httppost = new HttpPost(getHttpServerUrl());
			StringEntity stringEntity = new StringEntity(CptRPacketParse.encode(request), HTTP.UTF_8);
			httppost.setEntity(stringEntity);
			httppost.addHeader("charset", HTTP.UTF_8);

			HttpParams parms = new BasicHttpParams();
			parms.setParameter("charset", HTTP.UTF_8);

			DefaultHttpClient httpclient = new DefaultHttpClient(parms);

			if (cookies != null) {
				for (Cookie cookie : cookies) {
					httpclient.getCookieStore().addCookie(cookie);
				}
			}
			HttpResponse httpResponse = httpclient.execute(httppost);
			HttpEntity httpEntity = httpResponse.getEntity();
			cookies = httpclient.getCookieStore().getCookies();

			String responseText = inputStreamToString(httpEntity.getContent());
			System.out.println("responseText----------------->>>>"+responseText);
			if (// When in this case, the background server may update or stop
			responseText.equals("DATA LOGIC UNAVAILABLE") ||
			// Send the request that the sac-server is not support.
					responseText.equals("UNSUPPORTED PACKET!") ||
					// The packet string is not correct, may be using the old
					// version or XML encoder error.
					responseText.startsWith("ERROR PACKET, ERROR CONTENT:")) {
				lastError = LastError.HTTP_SYSTEM_ERROR;
				return null;
			}
			// When user long time doesn't operate, the cookie will be expired,
			// so need send the login request again.
			else if (responseText.equals("FAILED_RE_LOGIN")) {
				lastError = LastError.HTTP_NEED_RELOGIN;
				return null;
			}
			return CptRPacketParse.parse(responseText);

		} catch (Exception e) {
			e.printStackTrace();
			lastError = LastError.HTTP_EXCEPTION;
			return null;
		}
	}

	public IPacket httpInvokeVersion(IPacket request) {
		try { 
			//HTTP_SERVER_URL_VERSION
			HttpPost httppost = new HttpPost(HTTP_GETURL_SAC_BY_MISI);
			StringEntity stringEntity = new StringEntity(CptHPacketParse.encode(request), HTTP.UTF_8);
			httppost.setEntity(stringEntity);
			httppost.addHeader("charset", HTTP.UTF_8);
			HttpParams parms = new BasicHttpParams();
			parms.setParameter("charset", HTTP.UTF_8);
			DefaultHttpClient httpclient = new DefaultHttpClient(parms);
			HttpResponse httpResponse = httpclient.execute(httppost);
			HttpEntity httpEntity = httpResponse.getEntity();
			String responseText = inputStreamToString(httpEntity.getContent());
			if (
			 responseText.equals("DATA LOGIC UNAVAILABLE") ||
					responseText.equals("UNSUPPORTED PACKET!") ||
					responseText.startsWith("ERROR PACKET, ERROR CONTENT:")) {
				lastError = LastError.HTTP_SYSTEM_ERROR;
				return null;
			}
			return CptHPacketParse.parse(responseText);

		} catch (Exception e) {
			e.printStackTrace();
			lastError = LastError.HTTP_EXCEPTION;
			return null;
		}
	}
	/**
	 * When the user using the Unicom-GPRS for network, can get the msisdn by
	 * this request.
	 * 
	 * @return the msisdn if exist, null
	 */
	public String getMsisdnFromServer() {
		lastError=LastError.SUCCESS;
		String url = HTTP_SERVER_URL + "?getmsisdn=true";
		try {
			registered = false;
			msisdn = null;
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(new StringEntity(""));

			// just for test.
			HttpResponse httpResponse = new DefaultHttpClient().execute(httppost);
			HttpEntity httpEntity = httpResponse.getEntity();
			String responseText = inputStreamToString(httpEntity.getContent());

			if (responseText.equals("FAILED_NO_MSISDN") || responseText.equals("FAILD_AUTH_ERROR")) {
//				lastError = LastError.MSISDN_EMPTY;
				return msisdn;
			} else if (responseText.equals("FAILED_SERVER_ERROR")) {
				lastError = LastError.HTTP_SYSTEM_ERROR;
			} else {
				String[] texts = responseText.split(";");
				for (String t : texts) {
					if (t.startsWith("msisdn=")) {
						msisdn = t.substring("msisdn=".length());
					} else if (t.startsWith("register=")) {
						registered = Boolean.parseBoolean(t.substring("register=".length()));
					}
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
			lastError = LastError.HTTP_EXCEPTION;
		}

		return msisdn;
	}

	/**
	 * the login request.
	 * 
	 * @param msisdn
	 * @param password
	 * @return
	 */
	public TUserLoginResult.Enum loginRequest(String msisdn, String password) {
		lastError=LastError.SUCCESS;
		if (msisdn == null || msisdn.length() < 11) {
			return null;
		}
		TUserLoginReq request = TUserLoginReq.Factory.newInstance();
		request.setSeqId(getSeqId());
		request.setMsisdn(msisdn);
		request.setPassword(password);
		IPacket response = httpInvoke(request);
		if (response != null) {
			TUserLoginResult.Enum result = ((TUserLoginRsp) response).getResult();
			if (TUserLoginResult.SUCCESS.equals(result)) {
				this.msisdn = msisdn;
			}
			return result;
		}
		return null;
	}
	/**
	 * the login request.
	 * 
	 * @param msisdn
	 * @param password
	 * @return
	 */
	public TUserLoginResult.Enum loginRequestTest(String msisdn, String password,String url) {
		lastError=LastError.SUCCESS;
		if (msisdn == null || msisdn.length() < 11) {
			return null;
		}
		TUserLoginReq request = TUserLoginReq.Factory.newInstance();
		request.setSeqId(getSeqId());
		request.setMsisdn(msisdn);
		request.setPassword(password);

		IPacket response = httpInvokeLogin(request, url);
		if (response != null) {
			TUserLoginResult.Enum result = ((TUserLoginRsp) response).getResult();
			if (TUserLoginResult.SUCCESS.equals(result)) {
				this.msisdn = msisdn;
			}
			return result;
		}
		return null;
	}
	/**
	 * get the duration state.
	 * 
	 * @return
	 */
	public TGetDurationStateRsp getDurationScene() {
		lastError=LastError.SUCCESS;
		if (msisdn == null) {
			lastError = LastError.MSISDN_EMPTY;
			return null;
		}

		TGetDurationStateReq request = TGetDurationStateReq.Factory.newInstance();
		request.setSeqId(getSeqId());
		request.setMsisdn(msisdn);

		IPacket response = httpInvoke(request);
		if (response != null) {
			return (TGetDurationStateRsp) response;
		}

		return null;
	}

	/**
	 * set the duration scene.
	 * 
	 * @param msisdn
	 * @param policy
	 *            must be zc,sm or gj.
	 * @param scene
	 *            see the document for help.
	 * @param duration
	 *            the minute of duration.
	 * @return
	 */
	public TSetDurationSceneResult.Enum setDurationScene(String policy, String scene, int duration) {
		lastError=LastError.SUCCESS;
		if (msisdn == null) {
			lastError = LastError.MSISDN_EMPTY;
			return null;
		}

		TSetDurationSceneReq request = TSetDurationSceneReq.Factory.newInstance();
		request.setSeqId(getSeqId());
		request.setMsisdn(msisdn);

		TDurationSceneData data = request.addNewData();
		TCallPolicyState.Enum p = TCallPolicyState.Enum.forString(policy);
		
		// this should be code error, when code correct, this will never be
		// null.
		if (p == null) {
			lastError = LastError.INPUT_PARAMETER_ERROR;
			return null;
		}
		data.setPolicy(p);

		// when the policy is not ZC, so need must set the scene.
		if (!p.equals(TCallPolicyState.ZC)) {
			TScenesState.Enum s = TScenesState.Enum.forString(scene);
			if (s == null) {
				lastError = LastError.INPUT_PARAMETER_ERROR;
				return null;
			}
			data.setScene(s);
			data.setDurations(duration);
		}

		IPacket response = httpInvoke(request);
		
		System.out.println("response------------->>>>"+response);
		if (response != null) {
			return ((TSetDurationSceneRsp) response).getResult();
		}
		return null;
	}

	/**
	 * 取回当前定时状态
	 * 
	 * @param msisdn
	 * @return
	 */
	public TGetTimeSceneRsp getTimeSceneScene() {
		lastError=LastError.SUCCESS;
		if (msisdn == null) {
			lastError = LastError.MSISDN_EMPTY;
			return null;
		}

		TGetTimeSceneReq request = TGetTimeSceneReq.Factory.newInstance();
		request.setSeqId(getSeqId());
		request.setMsisdn(msisdn);
		IPacket response = httpInvoke(request);
		if (response != null) {
			return ((TGetTimeSceneRsp) response);
		}
		return null;
	}

	/**
	 * 增加定时状态
	 * 
	 * @param data
	 * @return
	 */
	public TAddTimeSceneResult.Enum addTimeScene(TTimeSceneData data) {
		lastError=LastError.SUCCESS;
		if (msisdn == null) {
			lastError = LastError.MSISDN_EMPTY;
			return null;
		}
		TAddTimeSceneReq request = TAddTimeSceneReq.Factory.newInstance();
		request.setSeqId(seqId);
		request.setMsisdn(msisdn);
		request.setData(data);
		IPacket response = httpInvoke(request);
		if (response != null) {
			return ((TAddTimeSceneRsp) response).getResult();
		}
		return null;
	}

	/**
	 * 规换定时状态，如果不指定data，则就是将指定的定时状态删除。
	 * 
	 * @param index
	 * @param data
	 * @return
	 */
	public TReplaceTimeSceneResult.Enum replaceTimeScene(int index, TTimeSceneData data) {
		lastError=LastError.SUCCESS;
		if (msisdn == null) {
			lastError = LastError.MSISDN_EMPTY;
			return null;
		}

		TReplaceTimeSceneReq request = TReplaceTimeSceneReq.Factory.newInstance();
		request.setSeqId(seqId);
		request.setMsisdn(msisdn);
		request.setIndex(index);
		if (data != null) {
			request.setData(data);
		}

		IPacket response = httpInvoke(request);
		if (response != null) {
			return ((TReplaceTimeSceneRsp) response).getResult();
		}
		return null;
	}

	/**
	 * 取回黑名单名信息
	 * 
	 * @param listType
	 * @param start
	 * @param count
	 * @return
	 */
	public TGetPhonebooksRsp getPhonebooks(TBlackwhiteState.Enum listType, Integer start, Integer count) {
		lastError=LastError.SUCCESS;
		if (msisdn == null) {
			lastError = LastError.MSISDN_EMPTY;
			return null;
		}

		TGetPhonebooksReq request = TGetPhonebooksReq.Factory.newInstance();
		request.setSeqId(seqId);
		request.setMsisdn(msisdn);
		request.setState(listType);

		if (start != null) {
			request.setOffset(start);
		}

		if (count != null) {
			request.setCount(count);
		}

		IPacket response = httpInvoke(request);
		if (response != null) {
			return ((TGetPhonebooksRsp) response);
		}
		return null;
	}

	/**
	 * 设置黑白名单信息，
	 * 
	 * @param originalNumber
	 *            -- 修改以前号码时，要填写以前号码
	 * @param info
	 *            -- 新的号码信息，如果不指定 originalNumber 就是添加新号码
	 * @return
	 */
	public TSetPhonebooksRsp setPhonebooks(String originalNumber, TPhonebookReturn info) {
		lastError=LastError.SUCCESS;
		if (msisdn == null) {
			lastError = LastError.MSISDN_EMPTY;
			return null;
		}

		TSetPhonebooksReq request = TSetPhonebooksReq.Factory.newInstance();
		request.setSeqId(seqId);
		request.setMsisdn(msisdn);

		if (originalNumber != null) {
			request.setOriginalNumber(originalNumber);
		}

		if (info != null) {
			request.setInfo(info);
		}

		IPacket response = httpInvoke(request);
		if (response != null) {
			return   (TSetPhonebooksRsp) response ;
		}
		return null;
	}

	/**
	 * 删除黑白名单。
	 * 
	 * @param state
	 * @param phoneNumber
	 *            -- 如果不指定号码，则表示清空指定的黑名单或白名单
	 * @return
	 */
	public TDelPhonebooksResult.Enum delPhonebook(TBlackwhiteState.Enum state, String phoneNumber) {
		lastError=LastError.SUCCESS;
		if (msisdn == null) {
			lastError = LastError.MSISDN_EMPTY;
			return null;
		}
		TDelPhonebooksReq request = TDelPhonebooksReq.Factory.newInstance();
		request.setSeqId(seqId);
		request.setMsisdn(msisdn);

		if (phoneNumber != null) {
			request.setState(state);
		}

		if (phoneNumber != null) {
			request.setPhoneNumber(phoneNumber);
		}

		IPacket response = httpInvoke(request);
		if (response != null) {
			return ((TDelPhonebooksRsp) response).getResult();
		}
		return null;
	}
   /************************************************************
    * 配置选项中的基本设置相关的详细信息
    * @return 请求后的基本信息
    */
	public TGetMultipleOptionsRsp getMultipleOptions() {
		lastError=LastError.SUCCESS;
		if (msisdn == null) {
			lastError = LastError.MSISDN_EMPTY;
			return null;
		}

		TGetMultipleOptionsReq request = TGetMultipleOptionsReq.Factory.newInstance();
		request.setSeqId(getSeqId());
		request.setMsisdn(msisdn);

		Vector<Integer> newIds = request.addNewIds();
		newIds.add(TOptionPairIdDefine.INT_SMS_SM);
		newIds.add(TOptionPairIdDefine.INT_SMS_GJ);
		newIds.add(TOptionPairIdDefine.INT_SMS_HMD);
		newIds.add(TOptionPairIdDefine.INT_SMS_HF_ZC);
		newIds.add(TOptionPairIdDefine.INT_SMS_DS_SM);
		newIds.add(TOptionPairIdDefine.INT_SMS_BMD);
		newIds.add(TOptionPairIdDefine.INT_SMS_DS_HF_ZC);
		newIds.add(TOptionPairIdDefine.INT_HMD_ENABLED);
		newIds.add(TOptionPairIdDefine.INT_BMD_ENABLED);
		newIds.add(TOptionPairIdDefine.INT_ZC_BLACKLIST_ENABLED);

		IPacket response = httpInvoke(request);
		if (response != null) {
			return (TGetMultipleOptionsRsp) response;
		}

		return null;
	}
	/************************************************************
	    * 设置配置选项中的详细信息
	    * @return 
	    */
		public TSetMultipleOptionsResult.Enum setMultipleOptions(int id,int value) {
			lastError=LastError.SUCCESS;
			if (msisdn == null) {
				lastError = LastError.MSISDN_EMPTY;
				return null;
			}
           
			TSetMultipleOptionsReq request = TSetMultipleOptionsReq.Factory.newInstance();
			request.setSeqId(getSeqId());
			request.setMsisdn(msisdn);
			TOptionPair optionPair=TOptionPair.Factory.newInstance();
			optionPair.setId(id);
			optionPair.setValue(value);
			options.add(optionPair);
			request.setOptions(options);
			IPacket response = httpInvoke(request);
			if (response != null) {
				return ((TSetMultipleOptionsRsp) response).getResult();
			}

			return null;
		}

	public TSetMultipleOptionsResult.Enum setOptions(int id, int v) {
		if (msisdn == null) {
			lastError = LastError.MSISDN_EMPTY;
			return null;
		}

		
		TSetMultipleOptionsReq request = TSetMultipleOptionsReq.Factory.newInstance();
		request.setSeqId(getSeqId());
		request.setMsisdn(msisdn);

		TOptionPair pair = TOptionPair.Factory.newInstance();
		pair.setId(id);
		pair.setId(v == 0 ? 0 : 1);

		request.addNewOptions().add(pair);

		IPacket response = httpInvoke(request);
		if (response != null) {
			return ((TSetMultipleOptionsRsp) response).getResult();
		}

		return null;
	}
	/*******************************
	 * 恢复正常函数调用
	 * @return 是否恢复正常的标志
	 */
	public TSetDurationSceneResult.Enum Returntonormal(){
		TSetDurationSceneResult.Enum result=setDurationScene("zc", null, 0);
		return result;
	}
	/*****************************
	 * 获取关于本号码的所有黑名单记录
	 */
	public Vector<TPhonebookReturn> showBlackList(TBlackwhiteState.Enum mystatus) {
		lastError=LastError.SUCCESS;
		// 取回所以的黑名单
		TGetPhonebooksRsp response =getPhonebooks(mystatus, null, null);
		if (response == null) {
			return null;
		}
		TGetPhonebooksResult.Enum result = response.getResult();
		switch (result.intValue()) {
		case TGetPhonebooksResult.INT_SUCCESS:
			Vector<TPhonebookReturn> phoneBooks = response.getValues();
			return phoneBooks;
//			@SuppressWarnings("unused")
//			int count = phoneBooks.size();
//			// 是否要保存到本地或者分页显示
//			for (TPhonebookReturn book : phoneBooks) {
//				book.getPhoneNumber(); // 号码
//				book.getUserName(); // 用户名
//				book.getComment(); // 注释
//
//			}
		}
		return null;
	}
	/**
	 * 设置黑名单
	 * 
	 * @param originalNumber
	 *            旧的号码，当修改时使用此参数，不填写此参数表示增加
	 * @param phoneNumber
	 *            增加或修改的黑名单号码
	 * @param name
	 *            名单姓名
	 * @param comment
	 *            名单备注
	 */
	public TSetPhonebooksRsp setBlacklist(String originalNumber, String phoneNumber, String name, String comment,TBlackwhiteState.Enum black_or_white_enum) {
		lastError=LastError.SUCCESS;
		TPhonebookReturn info = TPhonebookReturn.Factory.newInstance();
		info.setPhoneNumber(phoneNumber);
		info.setState(black_or_white_enum);
		if (name != null) {
					 
						info.setUserName(name);
					 
		}
		if (comment != null) {
			info.setComment(comment);
		}
		TSetPhonebooksRsp result =setPhonebooks(originalNumber, info);
         
		return result;
		// 处理返回结果
	}
	
	public  String getDurationThisScene() {
		lastError=LastError.SUCCESS;
		TGetDurationStateRsp response = HttpDataAccess.getInstance().getDurationScene();
		if (response == null) {
			@SuppressWarnings("unused")
			HttpDataAccess.LastError lastError = HttpDataAccess.getInstance().getLastError();
			// TODO:由lastError 显示对应错误提示信息。

			return "";
		}

		TGetDurationStateResult.Enum result = response.getResult();
		if (TGetDurationStateResult.SUCCESS.equals(result)) {
			// 取回成功
			TDurationSceneData data = response.getData();
			if(data==null) {
				return "正常状态";
			}
			TCallPolicyState.Enum policy = data.getPolicy();
			if (TCallPolicyState.ZC.equals(policy)) {
				// TODO: 正常状态
				return "正常状态";
			} else {
				String temp="";
				String _temp="";
				if (TCallPolicyState.SM.equals(policy)) {
					_temp="非急勿扰";
				} else if (TCallPolicyState.GJ.equals(policy)) {
					_temp="请勿打扰";
				} else if(TCallPolicyState.DJ.equals(policy)){
					_temp="防呼死你";
				}

			    TScenesState.Enum scene = data.getScene();
				// 情景
				int info=data.getDurations(); // 持续时长
				System.out.println("持续时长------------->>>>>"+info);
				data.getStartTime(); // 开始时间
				response.getLeftSeconds(); // 剩余秒数。
				temp+=ArrayUnits.getReturnResult(scene.toString())+"("+_temp+")"+",年"+data.getStartTime().get(Calendar.YEAR)
				+",月"+(data.getStartTime().get(Calendar.MONTH)+1)+",日"+data.getStartTime().get(Calendar.DAY_OF_MONTH)+",时"
				+data.getStartTime().get(Calendar.HOUR_OF_DAY)+",分"+data.getStartTime().get(Calendar.MINUTE)+","+data.getDurations();
			    return temp;
			}
		} else if (TGetDurationStateResult.FAILED_NOT_REGISTED.equals(result)) {
			// TODO: 提示没有注册错误
			return "_error";
		} else {
			return "error_ok";
			// TODO: 系统忙请稍后再试之类
		}
	}
	
	/** 
	 * 检查新的版本信息
	 * @return   返回相关的版本请求
	 */
	public TCheckVersionRsp CheckNewVersion(String thiscurrent) {
	   TCheckVersionReq request = TCheckVersionReq.Factory.newInstance();
		request.setSeqId(seqId);
		request.setVersion(thiscurrent);
		IPacket response = httpInvokeVersion(request);
		if (response != null) {
			return ((TCheckVersionRsp) response);
		}
		return null;
	}
/********************************************单元测试模块开始******************************************************************/
   public TGetSacUrlByMsisdnRsp getResultByMsis(String phonenum){
	   lastError=LastError.SUCCESS;
	   TGetSacUrlByMsisdnReq request=TGetSacUrlByMsisdnReq.Factory.newInstance();
	   request.setSeqId(getSeqId());
	   request.setMsisdn(phonenum);
	   IPacket response=httpInvokeByMsis(request);
	   if(response!=null){
		   return (TGetSacUrlByMsisdnRsp) response;
	   }
	   return null;
   }
	
	
	public IPacket httpInvokeByMsis(IPacket request) {
		try {
			HttpPost httppost = new HttpPost(HTTP_GETURL_SAC_BY_MISI);
			StringEntity stringEntity = new StringEntity(CptHPacketParse.encode(request), HTTP.UTF_8);
			httppost.setEntity(stringEntity);
			httppost.addHeader("charset", HTTP.UTF_8);
			HttpParams parms = new BasicHttpParams();
			parms.setParameter("charset", HTTP.UTF_8);
			DefaultHttpClient httpclient = new DefaultHttpClient(parms);
			HttpResponse httpResponse = httpclient.execute(httppost);
			HttpEntity httpEntity = httpResponse.getEntity();
			String responseText = inputStreamToString(httpEntity.getContent());
			if (
			 responseText.equals("DATA LOGIC UNAVAILABLE") ||
					responseText.equals("UNSUPPORTED PACKET!") ||
					responseText.startsWith("ERROR PACKET, ERROR CONTENT:")) {
				lastError = LastError.HTTP_SYSTEM_ERROR;
				return null;
			}
			return CptHPacketParse.parse(responseText);

		} catch (Exception e) {
			e.printStackTrace();
			lastError = LastError.HTTP_EXCEPTION;
			return null;
		}
	}
	/**
	 * invoke the response by the request.
	 * 
	 * @param request
	 * @return
	 */
	public IPacket httpInvokeLogin(IPacket request,String url) {
		try {
			HttpPost httppost = new HttpPost(url);
			StringEntity stringEntity = new StringEntity(CptRPacketParse.encode(request), HTTP.UTF_8);
			httppost.setEntity(stringEntity);
			httppost.addHeader("charset", HTTP.UTF_8);

			HttpParams parms = new BasicHttpParams();
			parms.setParameter("charset", HTTP.UTF_8);

			DefaultHttpClient httpclient = new DefaultHttpClient(parms);

			if (cookies != null) {
				for (Cookie cookie : cookies) {
					httpclient.getCookieStore().addCookie(cookie);
				}
			}
			HttpResponse httpResponse = httpclient.execute(httppost);
			HttpEntity httpEntity = httpResponse.getEntity();
			cookies = httpclient.getCookieStore().getCookies();

			String responseText = inputStreamToString(httpEntity.getContent());
			System.out.println("responseText----------------->>>>"+responseText);
			if (// When in this case, the background server may update or stop
			responseText.equals("DATA LOGIC UNAVAILABLE") ||
			// Send the request that the sac-server is not support.
					responseText.equals("UNSUPPORTED PACKET!") ||
					// The packet string is not correct, may be using the old
					// version or XML encoder error.
					responseText.startsWith("ERROR PACKET, ERROR CONTENT:")) {
				lastError = LastError.HTTP_SYSTEM_ERROR;
				return null;
			}
			// When user long time doesn't operate, the cookie will be expired,
			// so need send the login request again.
			else if (responseText.equals("FAILED_RE_LOGIN")) {
				lastError = LastError.HTTP_NEED_RELOGIN;
				return null;
			}
			return CptRPacketParse.parse(responseText);

		} catch (Exception e) {
			e.printStackTrace();
			lastError = LastError.HTTP_EXCEPTION;
			return null;
		}
	}
 public void initLastError(){
	 lastError=LastError.SUCCESS;
 }
 

	/**
	 * the login request.
	 * 
	 * @param msisdn
	 * @param password
	 * @return
	 */
	public String editpassword(String oldpassword,String newpassword) {
		lastError=LastError.SUCCESS;
		if (msisdn == null || msisdn.length() < 11) {
			return null;
		}
		TEditPasswordReq request = TEditPasswordReq.Factory.newInstance();
		request.setSeqId(getSeqId());
		request.setMsisdn(msisdn);
		request.setPassword(newpassword);
		request.setOld_password(oldpassword);
		IPacket response = httpInvoke(request);
		if (response != null) {
			TEditPasswordResult.Enum result = ((TEditPasswordRsp) response).getResult();
			return result.toString();
		}
		return "";
	}


	/**
	 * the login request.
	 *
	 * @param msisdn
	 * @param password
	 * @return
	 */
	public TUserLoginResult.Enum setPhoneRequest(String phone) {
		lastError=LastError.SUCCESS;
		if (msisdn == null || msisdn.length() < 11) {
			return null;
		}
		TSetPhoneReq request = TSetPhoneReq.Factory.newInstance();
		request.setSeqId(getSeqId());
		request.setMsisdn(msisdn);
		request.setPhone(phone);
		IPacket response = httpInvoke(request);
		if (response != null) {
			TUserLoginResult.Enum result = ((TSetPhoneRsp) response).getResult();
			return result;
		}
		return null;
	}

}
