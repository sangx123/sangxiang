package winway.mdr.chinaunicom.comm;

import winway.mdr.telecomofchina.activity.R;


public interface DataResours {
	 int REQUEST_GETSTATUS=0;
	 int RESULT_GETSTATUS=1;
	 
	 //开始时间的请求码和结果码
	 int REQUEST_BEFINTIME=2;
	 int RESULT_BEGINTIME=3;
	 //结束时间的请求码和结果码	 
	 int REQUEST_ENDTIME=4;
	 int RESULT_ENDTIME=5;
	 
	 int REQUEST_UPDATETIMESETTING=6;
	 int RESULT_UPDATETIMESETTING=7;
	 
	 
	 int REQURST_UPDATEBLACK_WHITE=8;
	 int RESULT_UPDATEBLACK_WHITE=9;
	 int REQUEST_LOGIN_CODE=10;
	 int RESULT_LOGIN_CODE=11;
	 int REQUEST_QUICKSETUP_UPDATE=12;
	 int RESULT_QUICKSETUP_UPDATE=13;
	 
	 int REQUEST_ADD_FROMUSERPHONELIST=14;
	 int RESULT_ADD_FROMUSERPHONELIST=15;
	 int REQUEST_SETMYSCENCE=16;
	 int RESULT_SETMYSCENCE=17; 
	 
	 int QREQUEST_ADD_DSSZ=18;
	 int RESULT_ADD_DSSZ=19;
	 
	 int ADDNEW=1111;
	 int RETURNNEW=2222;
			 
	 int REQURST_ADD_QUICK=20;
	 int RESULt_ADD_QUICK=21;
	 
   String[] itemValues={
		   "恢复正常",
		   "非急勿扰",
		   "请勿打扰",
		   "定时设置",
		   "黑名单",
		   "白名单", 
		   "配置选项",
		   "业务帮助" 
		   };
   String[] datas={
		   "星期一",
		   "星期二",
		   "星期三",
		   "星期四",
		   "星期五",
		   "星期六",
		   "星期日"
		   };
   String[] status={
		   "非急勿扰",
		   "请勿打扰"
		   };

   int[] fjwricons={
		   R.drawable.xiuxi,
		   R.drawable.kaiche,
		   R.drawable.kaihui,
		   R.drawable.shangke,
//		   R.drawable.chuguo,
		   R.drawable.tongyong
   };
   String[] fjwrValues={"休息","开车","开会","上课",
//		   "出国",
		   "通用"};
   int[] qhdricons={
		   R.drawable.qhdr_xiuxi,
		   R.drawable.qhdr_kaiche,
		   R.drawable.qhdr_kaihui,
		   R.drawable.qhdr_shangke,
		   R.drawable.qhdr_feiji,
		   R.drawable.qhdr_guanji,
//		   R.drawable.qhdr_bzfwq,
//		   R.drawable.qhdr_chuguo,
		   R.drawable.qhdr_tongyong
   };
   int[] temp={
		   R.drawable.qhdr_guanji, 
		   R.drawable.tongyong,
		   R.drawable.qhdr_bzfwq,
		   R.drawable.qhdr_guanji
   };
   String[] qhdrValues={"休息","开车","开会","上课","飞机","关机",
//		   "不在服务区","出国",
		   "通用"};
   int[] moreicon={
		   R.drawable.dssz_item,
		   R.drawable.more_hmd,
		   R.drawable.more_bmd,
		   R.drawable.more_sz,
		   R.drawable.editpassword,
		   R.drawable.more_dg,
		   R.drawable.more_gnjs,
		   R.drawable.more_ywbz,
		   R.drawable.more_jcgx,
		   R.drawable.more_about
   };
   String[] moretext={"定时设置","黑名单","白名单","设置","修改密码","开通业务","新手引导","业务帮助","检查更新","关于"};
   String[] allValues={ 
		    "上海联通",
		    "湖北联通",
			"福建联通",
			"吉林联通",
			"宁夏联通",
			"山东联通",
			"山西联通",
			"浙江联通",
			"吉林移动",
			"陕西电信"};
		String[] sendnum={
				"10655185",
				"10655185",
				"10655185",
				"10655185",
				"10655185",
				"106551855",
				"10655811",
				"11631234",
				"10086",
				"106592090"
				};
		String[] sendnum_pwd={
				"10655185",
				"10655185",
				"10655185",
				"10655185",
				"10655185",
				"106551855",
				"10655811",
				"11631234",
				"10658678",
				"106592090"
				};
		String[] daima={"KT","KT","KT","KT","KT","KT","KT","5","KTZNMS","KT"};
		String[] requesturl={ 
				"http://sh.uc186.com/sac/android.r",
				"http://hb.uc186.com/sac/android.r",
				"http://fj.uc186.com/sac/android.r",
				"http://jl.uc186.com/sac/android.r",
				"http://nx.uc186.com/sac/android.r",
				"http://sd.uc186.com/sac/android.r",
				"http://sx.uc186.com/sac/android.r",
				"http://ms.zj165.com/sac/android.r",
				"http://www.jlznms.com/sac/android.r",
				"http://www.9600930.com/sac/android.r"};
		  /**
		    * 首页欢迎图片
		    */
		   int[] welcomeIcon =
		   {
			   R.drawable.one,
			   R.drawable.two,
			   R.drawable.three,
			   R.drawable.four,
		   };

}
