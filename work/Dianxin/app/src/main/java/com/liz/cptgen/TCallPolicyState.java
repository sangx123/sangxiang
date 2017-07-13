package com.liz.cptgen;

import lizxsd.parse.StringEnumTable;

public class TCallPolicyState {

	public static final Enum ZC = Enum.forString("zc"); // ����״̬
	public static final Enum SM = Enum.forString("sm"); // �Ǽ�����
	public static final Enum GJ = Enum.forString("gj"); // �������
	public static final Enum DJ = Enum.forString("dj"); // �������

	public static final int INT_ZC = 1;
	public static final int INT_SM = 2;
	public static final int INT_GJ = 3;
	public static final int INT_DJ = 4;

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
			case INT_DJ:
				return DJ;
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
					new Enum("dj", INT_DJ),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
