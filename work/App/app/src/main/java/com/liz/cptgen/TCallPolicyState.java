package com.liz.cptgen;

import lizxsd.parse.StringEnumTable;

public class TCallPolicyState {

	public static final Enum ZC = Enum.forString("zc"); // Õý³£×´Ì¬
	public static final Enum SM = Enum.forString("sm"); // ·Ç¼±ÎðÈÅ
	public static final Enum GJ = Enum.forString("gj"); // ÇëÎð´òÈÅ

	public static final int INT_ZC = 1;
	public static final int INT_SM = 2;
	public static final int INT_GJ = 3;

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
			case INT_ZC:
				return ZC;
			case INT_SM:
				return SM;
			case INT_GJ:
				return GJ;
			default:
				return null;
			}
		}

		private Enum(java.lang.String s, int i) {
			super(s, i);
		}

		public static final Table table = new Table(
				new Enum[] {
					new Enum("zc", INT_ZC),
					new Enum("sm", INT_SM),
					new Enum("gj", INT_GJ),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
