package com.liz.cpth;

import lizxsd.parse.StringEnumTable;

public class TCheckVersionResult { 

	public static final Enum MATCHED = Enum.forString("matched");
	public static final Enum CAN_UPDATE = Enum.forString("can_update");
	public static final Enum MUST_UPDATE = Enum.forString("must_update");
	public static final Enum SYSTEM_ERROR = Enum.forString("system_error");

	public static final int INT_MATCHED = 1;
	public static final int INT_CAN_UPDATE = 2;
	public static final int INT_MUST_UPDATE = 3;
	public static final int INT_SYSTEM_ERROR = 4;

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
			case INT_MATCHED:
				return MATCHED;
			case INT_CAN_UPDATE:
				return CAN_UPDATE;
			case INT_MUST_UPDATE:
				return MUST_UPDATE;
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
					new Enum("matched", INT_MATCHED),
					new Enum("can_update", INT_CAN_UPDATE),
					new Enum("must_update", INT_MUST_UPDATE),
					new Enum("system_error", INT_SYSTEM_ERROR),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
