package com.liz.cptr;

import lizxsd.parse.StringEnumTable;

public class TCalledStateEnable { 

	public static final Enum BLACKLIST = Enum.forString("blacklist");
	public static final Enum WHITELIST = Enum.forString("whitelist");
	public static final Enum UNDISTURBED = Enum.forString("undisturbed");

	public static final int INT_BLACKLIST = 1;
	public static final int INT_WHITELIST = 2;
	public static final int INT_UNDISTURBED = 3;

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
			case INT_BLACKLIST:
				return BLACKLIST;
			case INT_WHITELIST:
				return WHITELIST;
			case INT_UNDISTURBED:
				return UNDISTURBED;
			default:
				return null;
			}
		}

		private Enum(java.lang.String s, int i) {
			super(s, i);
		}

		public static final Table table = new Table(
				new Enum[] {
					new Enum("blacklist", INT_BLACKLIST),
					new Enum("whitelist", INT_WHITELIST),
					new Enum("undisturbed", INT_UNDISTURBED),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
