package com.liz.cptr;

import lizxsd.parse.StringEnumTable;

public class TOptionPairIdDefine { 

	public static final Enum SMS_SM = Enum.forString("sms_sm");
	public static final Enum SMS_GJ = Enum.forString("sms_gj");
	public static final Enum SMS_HMD = Enum.forString("sms_hmd");
	public static final Enum SMS_HF_ZC = Enum.forString("sms_hf_zc");
	public static final Enum SMS_DS_SM = Enum.forString("sms_ds_sm");
	public static final Enum SMS_BMD = Enum.forString("sms_bmd");
	public static final Enum SMS_DS_HF_ZC = Enum.forString("sms_ds_hf_zc");
	public static final Enum NOT_DEFINE_1 = Enum.forString("not_define_1");
	public static final Enum NOT_DEFINE_2 = Enum.forString("not_define_2");
	public static final Enum POLICY_ENABLED = Enum.forString("policy_enabled");
	public static final Enum HMD_ENABLED = Enum.forString("hmd_enabled");
	public static final Enum BMD_ENABLED = Enum.forString("bmd_enabled");
	public static final Enum ADVERTISEMENT = Enum.forString("advertisement");
	public static final Enum SMS_CFB = Enum.forString("sms_cfb");
	public static final Enum ZC_BLACKLIST_ENABLED = Enum.forString("zc_blacklist_enabled");
	public static final Enum NOT_DEFINE_16 = Enum.forString("not_define_16");
	public static final Enum NOT_DEFINE_17 = Enum.forString("not_define_17");
	public static final Enum NOT_DEFINE_18 = Enum.forString("not_define_18");
	public static final Enum NOT_DEFINE_19 = Enum.forString("not_define_19");
	public static final Enum NOT_DEFINE_20 = Enum.forString("not_define_20");
	public static final Enum SMS_END_SEND_XX = Enum.forString("sms_end_send_xx");
	public static final Enum SMS_END_SEND_KC = Enum.forString("sms_end_send_kc");
	public static final Enum SMS_END_SEND_KH = Enum.forString("sms_end_send_kh");
	public static final Enum SMS_END_SEND_CC = Enum.forString("sms_end_send_cc");
	public static final Enum SMS_END_SEND_SK = Enum.forString("sms_end_send_sk");
	public static final Enum SMS_END_SEND_TY = Enum.forString("sms_end_send_ty");
	public static final Enum SMS_END_SEND_FJ = Enum.forString("sms_end_send_fj");
	public static final Enum SMS_END_SEND_GJ_TSY = Enum.forString("sms_end_send_gj_tsy");
	public static final Enum SMS_END_SEND_KONG_HAO = Enum.forString("sms_end_send_kong_hao");
	public static final Enum SMS_END_SEND_OOS = Enum.forString("sms_end_send_oos");
	public static final Enum SMS_END_SEND_SS = Enum.forString("sms_end_send_ss");
	public static final Enum SMS_END_SEND_TS = Enum.forString("sms_end_send_ts");
	public static final Enum SMS_END_SEND_DY = Enum.forString("sms_end_send_dy");
	public static final Enum SMS_END_SEND_YC = Enum.forString("sms_end_send_yc");
	public static final Enum SMS_END_SEND_MD = Enum.forString("sms_end_send_md");
	public static final Enum SMS_END_SEND_XJ = Enum.forString("sms_end_send_xj");
	public static final Enum SMS_END_SEND_CG = Enum.forString("sms_end_send_cg");

	public static final int INT_SMS_SM = 1;
	public static final int INT_SMS_GJ = 2;
	public static final int INT_SMS_HMD = 3;
	public static final int INT_SMS_HF_ZC = 4;
	public static final int INT_SMS_DS_SM = 5;
	public static final int INT_SMS_BMD = 6;
	public static final int INT_SMS_DS_HF_ZC = 7;
	public static final int INT_NOT_DEFINE_1 = 8;
	public static final int INT_NOT_DEFINE_2 = 9;
	public static final int INT_POLICY_ENABLED = 10;
	public static final int INT_HMD_ENABLED = 11;
	public static final int INT_BMD_ENABLED = 12;
	public static final int INT_ADVERTISEMENT = 13;
	public static final int INT_SMS_CFB = 14;
	public static final int INT_ZC_BLACKLIST_ENABLED = 15;
	public static final int INT_NOT_DEFINE_16 = 16;
	public static final int INT_NOT_DEFINE_17 = 17;
	public static final int INT_NOT_DEFINE_18 = 18;
	public static final int INT_NOT_DEFINE_19 = 19;
	public static final int INT_NOT_DEFINE_20 = 20;
	public static final int INT_SMS_END_SEND_XX = 21;
	public static final int INT_SMS_END_SEND_KC = 22;
	public static final int INT_SMS_END_SEND_KH = 23;
	public static final int INT_SMS_END_SEND_CC = 24;
	public static final int INT_SMS_END_SEND_SK = 25;
	public static final int INT_SMS_END_SEND_TY = 26;
	public static final int INT_SMS_END_SEND_FJ = 27;
	public static final int INT_SMS_END_SEND_GJ_TSY = 28;
	public static final int INT_SMS_END_SEND_KONG_HAO = 29;
	public static final int INT_SMS_END_SEND_OOS = 30;
	public static final int INT_SMS_END_SEND_SS = 31;
	public static final int INT_SMS_END_SEND_TS = 32;
	public static final int INT_SMS_END_SEND_DY = 33;
	public static final int INT_SMS_END_SEND_YC = 34;
	public static final int INT_SMS_END_SEND_MD = 35;
	public static final int INT_SMS_END_SEND_XJ = 36;
	public static final int INT_SMS_END_SEND_CG = 37;

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
			case INT_SMS_SM:
				return SMS_SM;
			case INT_SMS_GJ:
				return SMS_GJ;
			case INT_SMS_HMD:
				return SMS_HMD;
			case INT_SMS_HF_ZC:
				return SMS_HF_ZC;
			case INT_SMS_DS_SM:
				return SMS_DS_SM;
			case INT_SMS_BMD:
				return SMS_BMD;
			case INT_SMS_DS_HF_ZC:
				return SMS_DS_HF_ZC;
			case INT_NOT_DEFINE_1:
				return NOT_DEFINE_1;
			case INT_NOT_DEFINE_2:
				return NOT_DEFINE_2;
			case INT_POLICY_ENABLED:
				return POLICY_ENABLED;
			case INT_HMD_ENABLED:
				return HMD_ENABLED;
			case INT_BMD_ENABLED:
				return BMD_ENABLED;
			case INT_ADVERTISEMENT:
				return ADVERTISEMENT;
			case INT_SMS_CFB:
				return SMS_CFB;
			case INT_ZC_BLACKLIST_ENABLED:
				return ZC_BLACKLIST_ENABLED;
			case INT_NOT_DEFINE_16:
				return NOT_DEFINE_16;
			case INT_NOT_DEFINE_17:
				return NOT_DEFINE_17;
			case INT_NOT_DEFINE_18:
				return NOT_DEFINE_18;
			case INT_NOT_DEFINE_19:
				return NOT_DEFINE_19;
			case INT_NOT_DEFINE_20:
				return NOT_DEFINE_20;
			case INT_SMS_END_SEND_XX:
				return SMS_END_SEND_XX;
			case INT_SMS_END_SEND_KC:
				return SMS_END_SEND_KC;
			case INT_SMS_END_SEND_KH:
				return SMS_END_SEND_KH;
			case INT_SMS_END_SEND_CC:
				return SMS_END_SEND_CC;
			case INT_SMS_END_SEND_SK:
				return SMS_END_SEND_SK;
			case INT_SMS_END_SEND_TY:
				return SMS_END_SEND_TY;
			case INT_SMS_END_SEND_FJ:
				return SMS_END_SEND_FJ;
			case INT_SMS_END_SEND_GJ_TSY:
				return SMS_END_SEND_GJ_TSY;
			case INT_SMS_END_SEND_KONG_HAO:
				return SMS_END_SEND_KONG_HAO;
			case INT_SMS_END_SEND_OOS:
				return SMS_END_SEND_OOS;
			case INT_SMS_END_SEND_SS:
				return SMS_END_SEND_SS;
			case INT_SMS_END_SEND_TS:
				return SMS_END_SEND_TS;
			case INT_SMS_END_SEND_DY:
				return SMS_END_SEND_DY;
			case INT_SMS_END_SEND_YC:
				return SMS_END_SEND_YC;
			case INT_SMS_END_SEND_MD:
				return SMS_END_SEND_MD;
			case INT_SMS_END_SEND_XJ:
				return SMS_END_SEND_XJ;
			case INT_SMS_END_SEND_CG:
				return SMS_END_SEND_CG;
			default:
				return null;
			}
		}

		private Enum(java.lang.String s, int i) {
			super(s, i);
		}

		public static final Table table = new Table(
				new Enum[] {
					new Enum("sms_sm", INT_SMS_SM),
					new Enum("sms_gj", INT_SMS_GJ),
					new Enum("sms_hmd", INT_SMS_HMD),
					new Enum("sms_hf_zc", INT_SMS_HF_ZC),
					new Enum("sms_ds_sm", INT_SMS_DS_SM),
					new Enum("sms_bmd", INT_SMS_BMD),
					new Enum("sms_ds_hf_zc", INT_SMS_DS_HF_ZC),
					new Enum("not_define_1", INT_NOT_DEFINE_1),
					new Enum("not_define_2", INT_NOT_DEFINE_2),
					new Enum("policy_enabled", INT_POLICY_ENABLED),
					new Enum("hmd_enabled", INT_HMD_ENABLED),
					new Enum("bmd_enabled", INT_BMD_ENABLED),
					new Enum("advertisement", INT_ADVERTISEMENT),
					new Enum("sms_cfb", INT_SMS_CFB),
					new Enum("zc_blacklist_enabled", INT_ZC_BLACKLIST_ENABLED),
					new Enum("not_define_16", INT_NOT_DEFINE_16),
					new Enum("not_define_17", INT_NOT_DEFINE_17),
					new Enum("not_define_18", INT_NOT_DEFINE_18),
					new Enum("not_define_19", INT_NOT_DEFINE_19),
					new Enum("not_define_20", INT_NOT_DEFINE_20),
					new Enum("sms_end_send_xx", INT_SMS_END_SEND_XX),
					new Enum("sms_end_send_kc", INT_SMS_END_SEND_KC),
					new Enum("sms_end_send_kh", INT_SMS_END_SEND_KH),
					new Enum("sms_end_send_cc", INT_SMS_END_SEND_CC),
					new Enum("sms_end_send_sk", INT_SMS_END_SEND_SK),
					new Enum("sms_end_send_ty", INT_SMS_END_SEND_TY),
					new Enum("sms_end_send_fj", INT_SMS_END_SEND_FJ),
					new Enum("sms_end_send_gj_tsy", INT_SMS_END_SEND_GJ_TSY),
					new Enum("sms_end_send_kong_hao", INT_SMS_END_SEND_KONG_HAO),
					new Enum("sms_end_send_oos", INT_SMS_END_SEND_OOS),
					new Enum("sms_end_send_ss", INT_SMS_END_SEND_SS),
					new Enum("sms_end_send_ts", INT_SMS_END_SEND_TS),
					new Enum("sms_end_send_dy", INT_SMS_END_SEND_DY),
					new Enum("sms_end_send_yc", INT_SMS_END_SEND_YC),
					new Enum("sms_end_send_md", INT_SMS_END_SEND_MD),
					new Enum("sms_end_send_xj", INT_SMS_END_SEND_XJ),
					new Enum("sms_end_send_cg", INT_SMS_END_SEND_CG),
				}
		);
		private static final long serialVersionUID = 1L;
	}
}
