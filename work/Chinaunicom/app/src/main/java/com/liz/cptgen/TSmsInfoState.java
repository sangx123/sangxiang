package com.liz.cptgen;

import lizxsd.parse.StringEnumTable;

public class TSmsInfoState {

	public static final Enum SM = Enum.forString("sm"); // �Ǽ����������������
	public static final Enum GJ = Enum.forString("gj"); // �����������������ѣ�
	public static final Enum HMD = Enum.forString("hmd"); // �����������������
	public static final Enum HF_ZC = Enum.forString("hf_zc"); // �ָ�����״̬��������
	public static final Enum DS_SM = Enum.forString("ds_sm"); // ��ʱ˯��״̬��������
	public static final Enum BMD = Enum.forString("bmd"); // �������������Ѷ���
	public static final Enum DS_HF_ZC = Enum.forString("ds_hf_zc"); // ��ʱ״̬�ָ�Ϊ����״̬����

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
