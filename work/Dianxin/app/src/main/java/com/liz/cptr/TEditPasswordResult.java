package com.liz.cptr;

import lizxsd.parse.StringEnumTable;

public class TEditPasswordResult { 

	public static final Enum SUCCESS = Enum.forString("success");
	public static final Enum FAILED_PASSWORD_TOO_SHORT_STRING = Enum.forString("failed_password_too_short");
	public static final Enum FAILED_OLD_PASSWORD_ERROR_STRING=Enum.forString("failed_old_password_error");

	public static final int INT_SUCCESS = 1;
	public static final int FAILED_PASSWORD_TOO_SHORT = 2;
	public static final int FAILED_OLD_PASSWORD_ERROR = 3;

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
			case FAILED_PASSWORD_TOO_SHORT:
				return FAILED_PASSWORD_TOO_SHORT_STRING;
			case FAILED_OLD_PASSWORD_ERROR:
				return FAILED_OLD_PASSWORD_ERROR_STRING;
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
					new Enum("failed_old_password_error", FAILED_OLD_PASSWORD_ERROR),
					new Enum("failed_password_too_short", FAILED_PASSWORD_TOO_SHORT),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
