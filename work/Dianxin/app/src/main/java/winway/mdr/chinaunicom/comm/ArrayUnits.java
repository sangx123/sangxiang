package winway.mdr.chinaunicom.comm;


public class ArrayUnits {
	/**********************
	 * 时间的排序
	 * @param in
	 * @return
	 */
	public static int[] efferArray(int[] in){
		int[] lasts=new int[in.length];
		int tem = 0;
		int num = 0;

		for(int i = 0;i < in.length;i++){
			for(int j = i;j < in.length - 1;j++)
			{
				num++;
				if(in[j+1] < in[i]){
					tem = in[j+1];
					in[j+1] = in[i];
					in[i] = tem;
				}
			}
		}
		
		for (int i = 0; i < in.length; i++) {
			lasts[i]=in[i];
			if(i < in.length - 1)
			{
				System.out.print(",");
			}
		}
		return lasts;
	}
	 /*****************************************************************
	   * 函数名称 : Getposition
	   * 参 数说明 :result
	   * 时         间 :2011-11
	   * 返回值:int[]
	   * 功能说明:根据传入的result进行过滤出星期数组中的索引
	   ****************************************************************/ 
	public static int[] Getposition(String result){
		System.out.println("result-------------->>>>>"+result);
		String[] strings=result.split("-");
		int[] myposition=new int[2];
		if(strings.length==2){
			myposition[0]=callBackString(strings[0]);
			myposition[1]=callBackString(strings[1]);
		}else if(strings.length==1){
			 myposition[0]=callBackString(strings[0]);
        	 myposition[1]=callBackString(strings[0]);
		}
        return myposition;
	
	}
	public static int Getitempostion(String result,int item){
		String[] temp;
		temp=item==1?DataResours.fjwrValues:DataResours.qhdrValues;
		for (int i = 0; i < temp.length; i++) {
			if(result.equals(temp[i]))return i;
		}
		return 0;
	}
	public static String getResult(String result){
		 if("休息".equals(result))return "xx";
		 else if("开车".equals(result))return "kc";
		 else if("开会".equals(result))return "kh";
		 else if("上课".equals(result))return "sk";
		 else if("出国".equals(result))return "cg";
		 else if("通用".equals(result))return "ty";
		 else if("飞机".equals(result))return "fj";
		 else if("关机".equals(result))return "gj_tsy";
		 else if("不在服务区".equals(result))return "oos";
		 else return "";
	}
	public static String getReturnResult(String result){
		 if("xx".equals(result))return "休息";
		 else if("kc".equals(result))return "开车";
		 else if("kh".equals(result))return "开会";
		 else if("sk".equals(result))return "上课";
		 else if("cg".equals(result))return "出国";
		 else if("ty".equals(result))return "通用";
		 else if("fj".equals(result))return "飞机";
		 else if("gj_tsy".equals(result))return "关机";
		 else if("oos".equals(result))return "不在服务区";
		 else return "";
	}
	public static int GetPolicyIconPosition(String policy,String scene){
		String[] itenvalues=policy.equals("非急勿扰")?DataResours.fjwrValues:DataResours.qhdrValues;
		int[] itemicons=policy.equals("非急勿扰")?DataResours.fjwricons:DataResours.qhdricons;
		for (int i = 0; i < itenvalues.length; i++) {
			if(itenvalues[i].equals(scene)){
				return itemicons[i];
			}
		}
		return itemicons[0];
	}
	public static String endtimecalc(int thishour,int thisminute,int hour,int minute){
		System.out.println("h--->>>"+thishour+"    m-------->>"+thisminute+"--------------------"+hour+"     -------------"+minute);
		int allminute=thisminute+minute;
		int allhour=thishour+hour;
		if(allminute>60){
			allhour=allhour+1;
			allminute=allminute-60;
		}
		String hour_temp=allhour<10?"0"+allhour:allhour+"";
		String minute_temp=allminute<10?"0"+allminute:allminute+"";
		return hour_temp+":"+minute_temp;
	}
	
	public static  int callBackString(String string){
		   if(string.contains("一")) return 0;
	        if(string.contains("二")) return 1;
	        if(string.contains("三")) return 2;
	        if(string.contains("四"))return 3;
	        if(string.contains("五"))return 4;
	        if(string.contains("六"))return 5;
	        if(string.contains("日")) return 6;
	        return 0;
	}
}
