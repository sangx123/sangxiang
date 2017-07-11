package winway.mdr.chinaunicom.comm;


public class ArrayUnits {
	/**********************
	 * ʱ�������
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
	   * �������� : Getposition
	   * �� ��˵�� :result
	   * ʱ         �� :2011-11
	   * ����ֵ:int[]
	   * ����˵��:���ݴ����result���й��˳����������е�����
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
		 if("��Ϣ".equals(result))return "xx";
		 else if("����".equals(result))return "kc";
		 else if("����".equals(result))return "kh";
		 else if("�Ͽ�".equals(result))return "sk";
		 else if("����".equals(result))return "cg";
		 else if("ͨ��".equals(result))return "ty";
		 else if("�ɻ�".equals(result))return "fj";
		 else if("�ػ�".equals(result))return "gj_tsy";
		 else if("���ڷ�����".equals(result))return "oos";
		 else return "";
	}
	public static String getReturnResult(String result){
		 if("xx".equals(result))return "��Ϣ";
		 else if("kc".equals(result))return "����";
		 else if("kh".equals(result))return "����";
		 else if("sk".equals(result))return "�Ͽ�";
		 else if("cg".equals(result))return "����";
		 else if("ty".equals(result))return "ͨ��";
		 else if("fj".equals(result))return "�ɻ�";
		 else if("gj_tsy".equals(result))return "�ػ�";
		 else if("oos".equals(result))return "���ڷ�����";
		 else return "";
	}
	public static int GetPolicyIconPosition(String policy,String scene){
		String[] itenvalues=policy.equals("�Ǽ�����")?DataResours.fjwrValues:DataResours.qhdrValues;
		int[] itemicons=policy.equals("�Ǽ�����")?DataResours.fjwricons:DataResours.qhdricons;
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
		   if(string.contains("һ")) return 0;
	        if(string.contains("��")) return 1;
	        if(string.contains("��")) return 2;
	        if(string.contains("��"))return 3;
	        if(string.contains("��"))return 4;
	        if(string.contains("��"))return 5;
	        if(string.contains("��")) return 6;
	        return 0;
	}
}
