package winway.mdr.chinaunicom.comm;

import java.util.ArrayList;

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
		int[] myposition;
		ArrayList<Integer> pos=new ArrayList<Integer>();
        if(result.contains("一")) pos.add(0);
        if(result.contains("二")) pos.add(1);
        if(result.contains("三")) pos.add(2);
        if(result.contains("四"))pos.add(3);
        if(result.contains("五"))pos.add(4);
        if(result.contains("六"))pos.add(5);
        if(result.contains("日")) pos.add(6);
        myposition=new int[2];
        if(pos.size()==2){
        	 for (int i = 0; i < pos.size(); i++) {
     			myposition[i]=pos.get(i);
     		}
        }else if(pos.size()==1){
        	 myposition[0]=pos.get(0);
        	 myposition[1]=pos.get(0);
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
}
