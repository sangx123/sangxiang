package com.liz.cptgen;

import lizxsd.parse.StringEnumTable;

public class TScenesState {

	public static final Enum XX = Enum.forString("xx"); // 休息
	public static final Enum KC = Enum.forString("kc"); // 开车
	public static final Enum KH = Enum.forString("kh"); // 开会
	public static final Enum SK = Enum.forString("sk"); // 上课
	public static final Enum TY = Enum.forString("ty"); // 通用
	public static final Enum FJ = Enum.forString("fj"); // 飞机
	public static final Enum GJ_TSY = Enum.forString("gj_tsy"); // 关机提示音
	public static final Enum OOS = Enum.forString("oos"); // 不在服务区
	public static final Enum CG = Enum.forString("cg"); // 出国

	public static final int INT_XX = 1;
	public static final int INT_KC = 2;
	public static final int INT_KH = 3;
	public static final int INT_SK = 5;
	public static final int INT_TY = 6;
	public static final int INT_FJ = 7;
	public static final int INT_GJ_TSY = 8;
	public static final int INT_OOS = 10;
	public static final int INT_CG = 17;

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
			case INT_XX:
				return XX;
			case INT_KC:
				return KC;
			case INT_KH:
				return KH;
			case INT_SK:
				return SK;
			case INT_TY:
				return TY;
			case INT_FJ:
				return FJ;
			case INT_GJ_TSY:
				return GJ_TSY;
			case INT_OOS:
				return OOS;
			case INT_CG:
				return CG;
			default:
				return null;
			}
		}

		private Enum(java.lang.String s, int i) {
			super(s, i);
		}

		public static final Table table = new Table(
				new Enum[] {
					new Enum("xx", INT_XX),
					new Enum("kc", INT_KC),
					new Enum("kh", INT_KH),
					new Enum("sk", INT_SK),
					new Enum("ty", INT_TY),
					new Enum("fj", INT_FJ),
					new Enum("gj_tsy", INT_GJ_TSY),
					new Enum("oos", INT_OOS),
					new Enum("cg", INT_CG),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
