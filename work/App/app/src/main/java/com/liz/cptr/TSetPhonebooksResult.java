package com.liz.cptr;

import lizxsd.parse.StringEnumTable;

public class TSetPhonebooksResult { 

	public static final Enum SUCCESS = Enum.forString("success");
	public static final Enum INVALID_PARAMETERS = Enum.forString("invalid_parameters");
	public static final Enum SYSTEM_ERROR = Enum.forString("system_error");
	public static final Enum DATABASE_ERROR = Enum.forString("database_error");
	public static final Enum FAILED_NOT_REGISTED = Enum.forString("failed_not_registed");
	public static final Enum FAILED_PHONE_NUMBER_EXISTED = Enum.forString("failed_phone_number_existed");
	public static final Enum FAILED_ORIGINAL_NUMBER_NOT_EXISTED = Enum.forString("failed_original_number_not_existed");
	public static final Enum FAILED_OVER_MAXIMUM_COUNT = Enum.forString("failed_over_maximum_count");
	public static final Enum FAILED_PHONE_NUMBER_TOO_SHORT = Enum.forString("failed_phone_number_too_short");
	public static final Enum FAILED_START_WITH_NAT = Enum.forString("failed_start_with_nat");
	public static final Enum FAILED_START_WITH_SUBTRACTION_SIGN = Enum.forString("failed_start_with_subtraction_sign");
	public static final Enum FAILED_UNSUPPORTED_CHARACTER = Enum.forString("failed_unsupported_character");
	public static final Enum FAILED_PHONE_NUMBER_TOO_LONG = Enum.forString("failed_phone_number_too_long");

	public static final int INT_SUCCESS = 1;
	public static final int INT_INVALID_PARAMETERS = 2;
	public static final int INT_SYSTEM_ERROR = 3;
	public static final int INT_DATABASE_ERROR = 4;
	public static final int INT_FAILED_NOT_REGISTED = 5;
	public static final int INT_FAILED_PHONE_NUMBER_EXISTED = 6;
	public static final int INT_FAILED_ORIGINAL_NUMBER_NOT_EXISTED = 7;
	public static final int INT_FAILED_OVER_MAXIMUM_COUNT = 8;
	public static final int INT_FAILED_PHONE_NUMBER_TOO_SHORT = 9;
	public static final int INT_FAILED_START_WITH_NAT = 10;
	public static final int INT_FAILED_START_WITH_SUBTRACTION_SIGN = 11;
	public static final int INT_FAILED_UNSUPPORTED_CHARACTER = 12;
	public static final int INT_FAILED_PHONE_NUMBER_TOO_LONG = 13;

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
			case INT_FAILED_PHONE_NUMBER_EXISTED:
				return FAILED_PHONE_NUMBER_EXISTED;
			case INT_FAILED_ORIGINAL_NUMBER_NOT_EXISTED:
				return FAILED_ORIGINAL_NUMBER_NOT_EXISTED;
			case INT_FAILED_OVER_MAXIMUM_COUNT:
				return FAILED_OVER_MAXIMUM_COUNT;
			case INT_FAILED_PHONE_NUMBER_TOO_SHORT:
				return FAILED_PHONE_NUMBER_TOO_SHORT;
			case INT_FAILED_START_WITH_NAT:
				return FAILED_START_WITH_NAT;
			case INT_FAILED_START_WITH_SUBTRACTION_SIGN:
				return FAILED_START_WITH_SUBTRACTION_SIGN;
			case INT_FAILED_UNSUPPORTED_CHARACTER:
				return FAILED_UNSUPPORTED_CHARACTER;
			case INT_FAILED_PHONE_NUMBER_TOO_LONG:
				return FAILED_PHONE_NUMBER_TOO_LONG;
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
					new Enum("failed_phone_number_existed", INT_FAILED_PHONE_NUMBER_EXISTED),
					new Enum("failed_original_number_not_existed", INT_FAILED_ORIGINAL_NUMBER_NOT_EXISTED),
					new Enum("failed_over_maximum_count", INT_FAILED_OVER_MAXIMUM_COUNT),
					new Enum("failed_phone_number_too_short", INT_FAILED_PHONE_NUMBER_TOO_SHORT),
					new Enum("failed_start_with_nat", INT_FAILED_START_WITH_NAT),
					new Enum("failed_start_with_subtraction_sign", INT_FAILED_START_WITH_SUBTRACTION_SIGN),
					new Enum("failed_unsupported_character", INT_FAILED_UNSUPPORTED_CHARACTER),
					new Enum("failed_phone_number_too_long", INT_FAILED_PHONE_NUMBER_TOO_LONG),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
