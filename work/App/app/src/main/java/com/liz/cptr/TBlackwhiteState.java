package com.liz.cptr;

import lizxsd.parse.StringEnumTable;

public class TBlackwhiteState { 

	public static final Enum NORMAL = Enum.forString("normal");
	public static final Enum BLACKLIST = Enum.forString("blacklist");
	public static final Enum WHITELIST = Enum.forString("whitelist");

	public static final int INT_NORMAL = 1;
	public static final int INT_BLACKLIST = 2;
	public static final int INT_WHITELIST = 3;

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
			case INT_NORMAL:
				return NORMAL;
			case INT_BLACKLIST:
				return BLACKLIST;
			case INT_WHITELIST:
				return WHITELIST;
			default:
				return null;
			}
		}

		private Enum(java.lang.String s, int i) {
			super(s, i);
		}

		public static final Table table = new Table(
				new Enum[] {
					new Enum("normal", INT_NORMAL),
					new Enum("blacklist", INT_BLACKLIST),
					new Enum("whitelist", INT_WHITELIST),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
