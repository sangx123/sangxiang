/************************* File Information ****************************
 * Copyright(C): This is my company secrecy java source code. Any copy or change
 * from this file must agreed by my company.
 *
 ************************* History **********************************
 * Create: zhou.h.m,  Date: 2011-12-11.
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
package winway.mdr.chinaunicom.comm;

import winway.mdr.telecomofchina.activity.R;
import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;

/**
 * ��Դ��ص���
 * 
 * @author zhou.h.m
 */
public class MyResources {
	/**
	 * �� result �õ�ͨ�õĴ��������������ֵΪ�գ�����
	 * 
	 * @param result
	 * @return
	 */
	public static Integer getHttpErrorIndex(Object result) {
		Integer errorResourceId = null;
		if (result == null) {
			HttpDataAccess.LastError lastError =  HttpDataAccess.getInstance().getLastError();
			if (lastError != null) {
				switch (lastError) {
				case HTTP_EXCEPTION:
					errorResourceId = R.string.http_access_http_exception;
					break;
				case HTTP_SYSTEM_ERROR:
					errorResourceId = R.string.http_access_system_error;
					break;
				case HTTP_NEED_RELOGIN:
					errorResourceId = R.string.http_access_need_relogin;
					break;
				case MSISDN_EMPTY:
					errorResourceId = R.string.http_access_msisdn_empty;
					break;
				case INPUT_PARAMETER_ERROR:
					errorResourceId = R.string.http_access_input_parameter_error;
					break;
				default:
					errorResourceId = R.string.http_access_unknown_error;
					break;
				}
			} else {
				errorResourceId = R.string.http_access_unknown_error;
			}
		} else {
			String resultStr = result.toString();
			if (resultStr.equals("invalid_parameters")) {
				errorResourceId = R.string.http_server_invalid_parameters;
			} else if (resultStr.equals("system_error")) {
				errorResourceId = R.string.http_server_system_error;
			} else if (resultStr.equals("database_error")) {
				errorResourceId = R.string.http_server_database_error;
			} else if (resultStr.equals("failed_not_registed")) {
				errorResourceId = R.string.http_server_failed_not_registed;
			}
		}

		return errorResourceId;
	}
}
