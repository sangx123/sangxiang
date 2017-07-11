package com.liz.cpth;

import lizxsd.parse.StringEnumTable;

public class TGetSacUrlByMsisdnResult { 

	public static final Enum SUCCESS = Enum.forString("success");
	public static final Enum FAILED_MSISDN_NOT_SUPPORT = Enum.forString("failed_msisdn_not_support");
	public static final Enum FAILED_MSISDN_NOT_FOUND = Enum.forString("failed_msisdn_not_found");
	public static final Enum FAILED_TYPE_NOT_FOUND = Enum.forString("failed_type_not_found");
	public static final Enum SYSTEM_ERROR = Enum.forString("system_error");

	public static final int INT_SUCCESS = 1;
	public static final int INT_FAILED_MSISDN_NOT_SUPPORT = 2;
	public static final int INT_FAILED_MSISDN_NOT_FOUND = 3;
	public static final int INT_FAILED_TYPE_NOT_FOUND = 4;
	public static final int INT_SYSTEM_ERROR = 5;

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
			case INT_FAILED_MSISDN_NOT_SUPPORT:
				return FAILED_MSISDN_NOT_SUPPORT;
			case INT_FAILED_MSISDN_NOT_FOUND:
				return FAILED_MSISDN_NOT_FOUND;
			case INT_FAILED_TYPE_NOT_FOUND:
				return FAILED_TYPE_NOT_FOUND;
			case INT_SYSTEM_ERROR:
				return SYSTEM_ERROR;
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
					new Enum("failed_msisdn_not_support", INT_FAILED_MSISDN_NOT_SUPPORT),
					new Enum("failed_msisdn_not_found", INT_FAILED_MSISDN_NOT_FOUND),
					new Enum("failed_type_not_found", INT_FAILED_TYPE_NOT_FOUND),
					new Enum("system_error", INT_SYSTEM_ERROR),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
