package com.liz.cptr;

import lizxsd.parse.StringEnumTable;

public class TSetDurationSceneResult { 

	public static final Enum SUCCESS = Enum.forString("success");
	public static final Enum INVALID_PARAMETERS = Enum.forString("invalid_parameters");
	public static final Enum SYSTEM_ERROR = Enum.forString("system_error");
	public static final Enum DATABASE_ERROR = Enum.forString("database_error");
	public static final Enum FAILED_NOT_REGISTED = Enum.forString("failed_not_registed");
	public static final Enum FAILED_GSM_ERROR = Enum.forString("failed_gsm_error");
	public static final Enum FAILED_SS_ERROR_STATUS = Enum.forString("failed_ss_error_status");
	public static final Enum FAILED_USER_NOT_SUPPORT_DJ = Enum.forString("failed_user_not_support_dj");
	public static final Enum FAILED_NO_DJ_PHONE_NUMBER = Enum.forString("failed_no_dj_phone_number");
	public static final Enum failed_not_support_Error = Enum.forString("failed_not_support");

	public static final int INT_SUCCESS = 1;
	public static final int INT_INVALID_PARAMETERS = 2;
	public static final int INT_SYSTEM_ERROR = 3;
	public static final int INT_DATABASE_ERROR = 4;
	public static final int INT_FAILED_NOT_REGISTED = 5;
	public static final int INT_FAILED_GSM_ERROR = 6;
	public static final int INT_FAILED_SS_ERROR_STATUS = 7;
	public static final int INT_FAILED_USER_NOT_SUPPORT_DJ = 8;
	public static final int INT_FAILED_NO_DJ_PHONE_NUMBER = 9;
	public static final int failed_not_support = 10;
	static final public class Enum extends StringEnumTable {
		public static Enum forString(java.lang.String s) {
			return (Enum) table.forString(s);
		}
		
		public static Enum parse(String s) {
			return Enum.forString(s);
		}
		
		public void encode(StringBuffer buffer) {
			buffer.append(this.toString());
		}

		public static Enum forInt(int i) {
			switch (i) {
			case INT_SUCCESS:
				return SUCCESS;
			case INT_INVALID_PARAMETERS:
				return INVALID_PARAMETERS;
			case INT_SYSTEM_ERROR:
				return SYSTEM_ERROR;
			case INT_DATABASE_ERROR:
				return DATABASE_ERROR;
			case INT_FAILED_NOT_REGISTED:
				return FAILED_NOT_REGISTED;
			case INT_FAILED_GSM_ERROR:
				return FAILED_GSM_ERROR;
			case INT_FAILED_SS_ERROR_STATUS:
				return FAILED_SS_ERROR_STATUS;
			case INT_FAILED_USER_NOT_SUPPORT_DJ:
				return FAILED_USER_NOT_SUPPORT_DJ;
			case INT_FAILED_NO_DJ_PHONE_NUMBER:
				return FAILED_NO_DJ_PHONE_NUMBER;
			case failed_not_support:
					return failed_not_support_Error;
			default:
				return null;
			}
		}

		private Enum(java.lang.String s, int i) {
			super(s, i);
		}

		public static final Table table = new Table(
				new Enum[] {
					new Enum("success", INT_SUCCESS),
					new Enum("invalid_parameters", INT_INVALID_PARAMETERS),
					new Enum("system_error", INT_SYSTEM_ERROR),
					new Enum("database_error", INT_DATABASE_ERROR),
					new Enum("failed_not_registed", INT_FAILED_NOT_REGISTED),
					new Enum("failed_gsm_error", INT_FAILED_GSM_ERROR),
					new Enum("failed_ss_error_status", INT_FAILED_SS_ERROR_STATUS),
					new Enum("failed_user_not_support_dj", INT_FAILED_USER_NOT_SUPPORT_DJ),
					new Enum("failed_no_dj_phone_number", INT_FAILED_NO_DJ_PHONE_NUMBER),
					new Enum("failed_not_support", failed_not_support)
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
