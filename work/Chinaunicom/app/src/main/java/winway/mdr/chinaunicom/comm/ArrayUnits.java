package winway.mdr.chinaunicom.comm;

import java.util.ArrayList;

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
		int[] myposition;
		ArrayList<Integer> pos=new ArrayList<Integer>();
        if(result.contains("һ")) pos.add(0);
        if(result.contains("��")) pos.add(1);
        if(result.contains("��")) pos.add(2);
        if(result.contains("��"))pos.add(3);
        if(result.contains("��"))pos.add(4);
        if(result.contains("��"))pos.add(5);
        if(result.contains("��")) pos.add(6);
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
}
