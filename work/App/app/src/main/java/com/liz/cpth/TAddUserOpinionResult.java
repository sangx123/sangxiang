package com.liz.cpth;

import lizxsd.parse.StringEnumTable;

public class TAddUserOpinionResult { 

	public static final Enum SUCCESS = Enum.forString("success");
	public static final Enum SYSTEM_ERROR = Enum.forString("system_error");

	public static final int INT_SUCCESS = 1;
	public static final int INT_SYSTEM_ERROR = 2;

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
					new Enum("system_error", INT_SYSTEM_ERROR),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
