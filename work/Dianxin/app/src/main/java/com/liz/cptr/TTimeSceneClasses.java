package com.liz.cptr;

import lizxsd.parse.StringEnumTable;

public class TTimeSceneClasses { 

	public static final Enum ONCE = Enum.forString("once");
	public static final Enum WEEKLY = Enum.forString("weekly");
	public static final Enum EVERYDAY = Enum.forString("everyday");

	public static final int INT_ONCE = 1;
	public static final int INT_WEEKLY = 2;
	public static final int INT_EVERYDAY = 3;

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
			case INT_ONCE:
				return ONCE;
			case INT_WEEKLY:
				return WEEKLY;
			case INT_EVERYDAY:
				return EVERYDAY;
			default:
				return null;
			}
		}

		private Enum(java.lang.String s, int i) {
			super(s, i);
		}

		public static final Table table = new Table(
				new Enum[] {
					new Enum("once", INT_ONCE),
					new Enum("weekly", INT_WEEKLY),
					new Enum("everyday", INT_EVERYDAY),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
