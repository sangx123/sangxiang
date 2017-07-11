package com.liz.cptgen;

import lizxsd.parse.StringEnumTable;

public class TSmsInfoState {

	public static final Enum SM = Enum.forString("sm"); // 非急勿扰来电短信提醒
	public static final Enum GJ = Enum.forString("gj"); // 请勿打扰来电短信提醒；
	public static final Enum HMD = Enum.forString("hmd"); // 黑名单来电短信提醒
	public static final Enum HF_ZC = Enum.forString("hf_zc"); // 恢复正常状态短信提醒
	public static final Enum DS_SM = Enum.forString("ds_sm"); // 定时睡眠状态短信提醒
	public static final Enum BMD = Enum.forString("bmd"); // 白名单来能提醒短信
	public static final Enum DS_HF_ZC = Enum.forString("ds_hf_zc"); // 定时状态恢复为正常状态短信

	public static final int INT_SM = 1;
	public static final int INT_GJ = 2;
	public static final int INT_HMD = 3;
	public static final int INT_HF_ZC = 4;
	public static final int INT_DS_SM = 5;
	public static final int INT_BMD = 6;
	public static final int INT_DS_HF_ZC = 7;

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
			case INT_SM:
				return SM;
			case INT_GJ:
				return GJ;
			case INT_HMD:
				return HMD;
			case INT_HF_ZC:
				return HF_ZC;
			case INT_DS_SM:
				return DS_SM;
			case INT_BMD:
				return BMD;
			case INT_DS_HF_ZC:
				return DS_HF_ZC;
			default:
				return null;
			}
		}

		private Enum(java.lang.String s, int i) {
			super(s, i);
		}

		public static final Table table = new Table(
				new Enum[] {
					new Enum("sm", INT_SM),
					new Enum("gj", INT_GJ),
					new Enum("hmd", INT_HMD),
					new Enum("hf_zc", INT_HF_ZC),
					new Enum("ds_sm", INT_DS_SM),
					new Enum("bmd", INT_BMD),
					new Enum("ds_hf_zc", INT_DS_HF_ZC),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
