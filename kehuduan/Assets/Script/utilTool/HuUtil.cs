using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public static class HuUtil
{
	//胡牌
	//胡牌
	private const int CHK_NULL = 0;                                          //非胡类型
	private const int CHK_CHI_HU = 1;                                        //胡类型-------普通的四个搭子一对将。如：123 345 789万 234 66筒（1番）
	private const int CHR_QIANG_GANG = 2;                                    //抢杠
	private const int CHR_GANG_SHANG_PAO = 3;                                //杠上炮
	private const int CHR_GANG_KAI = 4;                                      //杠上花
	private const int CHR_DI_HU = 5;                                         //地胡
	private const int CHR_DA_DUI_ZI = 6;                                     //大对子-------四个搭子均为三张一样的牌。如：111 333 444万222 66筒（2番）
	private const int CHR_QING_YI_SE = 7;                                    //清一色-------胡牌时只有一色牌。如：111 234 345 666 88万（4番）
	private const int CHR_QI_XIAO_DUI = 8;                                   //七对-------胡牌时为7个对子。如1133446677万5588筒（4番）
	//private const int CHR_DAI_YAO = 9;                                       //带幺---------所有牌都带1或9。如123 123 99万 123 789条。（4番）
	private const int CHR_JIANG_DUI = 10;                                    //将对---------大对子，且都是258的牌
	private const int CHR_SHU_FAN = 11;                                      //素番
	private const int CHR_QING_DUI = 12;                                     //清对（清一色*大对子（4*2））---------清一色大对子（8番）
	private const int CHR_LONG_QI_DUI = 13;                                  //龙七对(七对*杠数（4*2）)-------1133446666万5588筒（8番，每多一根翻倍）
	private const int CHR_QING_QI_DUI = 14;                                  //清七对(七对*清一色（4*4）)-------清一色暗七对（16番）
	private const int CHR_QING_YAO_JIU = 15;                                 //清幺九-------清一色带幺。（16番）
	private const int CHR_QING_LONG_QI_DUI = 16;                             //清龙七对（七对*清一色*杠个数）-----清一色龙七对（32番，每多一根翻倍）
	private const int CHR_TIAN_HU = 17;                                      //天胡


	private static int JIANG;

	/// <summary>
	/// 清一色，缺门分析
	/// </summary>
	/// <returns>The me shu.</returns>
	/// <param name="list">List.</param>
	public static int checkMeShu(List<int> list)
	{

		List<int> list1 = new List<int>();
		List<int> list2 = new List<int>();
		List<int> list3 = new List<int>();
		int menShu = 0;
		for (int i = 0; i < list.Count; i++)
		{
			if (list[i] >= 0 && list[i] < 0 + 9)
			{
				list1.Add(list[i] + 1);
			}
			else if (list[i] >= 0 + 9 && list[i] < 0 + 9 + 9)
			{
				list2.Add(list[i] - 9 + 1);
			}
			else if (list[i] >= 0 + 9 + 9 && list[i] < 0 + 9 + 9 + 9)
			{
				list3.Add(list[i] - 9 - 9 + 1);
			}
		}

		if (list1.Count > 0)
		{
			menShu++;

		}
		if (list2.Count > 0)
		{
			menShu++;

		}
		if (list3.Count > 0)
		{
			menShu++;

		}
		return menShu;
	}

	/// <summary>
	///  大对子,胡牌判断
	/// </summary>
	public static bool IsHuPengPeng(List<int> list)
	{
		bool result = false;
		int count2 = 0;
		int count1 = 0;
		int count4 = 0;
		List<int> paiList = ToCardAndCardNumArray(list);
		for (int i = 0; i < paiList.Count; i++)
		{
			if (paiList[i] == 1)
			{
				count1++;
			}
			else if (paiList[i] == 2)
			{
				count2++;
			}
			else if (paiList[i] == 4)
			{
				count4++;
			}
		}
		if (count2 == 1 && count4 == 0 && count1 == 0)
		{
			result = true;
		}
		return result;
	}


	/// <summary>
	/// 七对，胡牌判断
	/// </summary>
	public static bool IsQiDui(List<int> list)
	{
		//如果手上的牌没有14张的话就不能是七对
		if (list.Count != 14) return false;
		int count2 = 0;
		int count4 = 0;
		List<int> paiList = ToCardAndCardNumArray(list);
		for (int i = 0; i < paiList.Count; i++)
		{
			if (paiList[i] == 2)
			{
				count2++;
			}
			else if (paiList[i] == 4)
			{
				count4++;
			}
		}

		if ((count4 * 2 + count2) == 7)
		{
			return true;
		}
		return false;
	}

	//算法对应的博客----http://blog.csdn.net/cuixiping/article/details/8715799
	/// <summary>
	/// 普通胡牌数据转换
	/// </summary>
	/// <returns><c>true</c>, if nomal hu pai was ised, <c>false</c> otherwise.</returns>
	/// <param name="list">List.</param>
	public static bool isNomalHuPai(List<int> list)
	{
		//初始化将牌
		JIANG = 0;
		return isHuPai(ToCardAndCardNumArray(list));
	}

	/// <summary>
	/// 普通胡牌算法
	/// </summary>
	/// <returns><c>true</c>, if hu pai was ised, <c>false</c> otherwise.</returns>
	/// <param name="paiList">Pai list.</param>
	public static bool isHuPai(List<int> paiList)
	{


		//0,代表万，1代表条，2代表筒
		if (Remain(paiList) == 0)
		{
			return true;           //   递归退出条件：如果没有剩牌，则胡牌返回。
		}
		for (int i = 0; i < paiList.Count; i++)
		{//   找到有牌的地方，i就是当前牌,   PAI[i]是个数
		 //   跟踪信息
		 //   4张组合(杠子)
			if (paiList[i] != 0)
			{
				if (paiList[i] == 4)                               //   如果当前牌数等于4张
				{
					paiList[i] = 0;                                     //   除开全部4张牌
					if (isHuPai(paiList))
					{
						return true;             //   如果剩余的牌组合成功，和牌
					}
					paiList[i] = 4;                                     //   否则，取消4张组合
				}
				//   3张组合(大对)
				if (paiList[i] >= 3)                               //   如果当前牌不少于3张
				{
					paiList[i] -= 3;                                   //   减去3张牌
					if (isHuPai(paiList))
					{
						return true;             //   如果剩余的牌组合成功，胡牌
					}
					paiList[i] += 3;                                   //   取消3张组合
				}
				//   2张组合(将牌)
				if (JIANG == 0 && paiList[i] >= 2)           //   如果之前没有将牌，且当前牌不少于2张
				{
					JIANG = 1;                                       //   设置将牌标志
					paiList[i] -= 2;                                   //   减去2张牌
					if (isHuPai(paiList))
					{
						return true;
					}
					//   如果剩余的牌组合成功，胡牌
					paiList[i] += 2;                                   //   取消2张组合
					JIANG = 0;                                       //   清除将牌标志
				}
				if (i > 27)
				{
					return false;               //   “东南西北中发白”没有顺牌组合，不胡
				}
				//   顺牌组合，注意是从前往后组合！
				//   排除数值为8和9的牌
				if (i % 9 != 0 && i % 9 != 8 && paiList[i + 1] != 0 && paiList[i + 2] != 0)             //   如果后面有连续两张牌
				{
					paiList[i]--;
					paiList[i + 1]--;
					paiList[i + 2]--;                                     //   各牌数减1
					if (isHuPai(paiList))
					{
						return true;             //   如果剩余的牌组合成功，胡牌
					}
					paiList[i]++;
					paiList[i + 1]++;
					paiList[i + 2]++;                                     //   恢复各牌数
				}
			}

		}
		//   无法全部组合，不胡！
		return false;
	}
	private static int Remain(List<int> paiList)
	{
		int sum = 0;
		for (int i = 0; i < paiList.Count; i++)
		{
			sum += paiList[i];
		}
		return sum;
	}
	/// <summary>
	/// 获取杠的个数
	/// </summary>
	/// <returns>The gang number.</returns>
	/// <param name="list">List.</param>
	public static int getGangNum(List<int> list)
	{
		int count4 = 0;
		List<int> paiList = ToCardAndCardNumArray(list);
		for (int i = 0; i < paiList.Count; i++)
		{
			if (paiList[i] == 4)
			{
				count4++;
			}
		}
		return count4;
	}



	/// <summary>
	/// 转换成1到27对应的牌的个数数组
	/// </summary>
	/// <returns>The card and card number array.</returns>
	/// <param name="list">List.</param>
	public static List<int> ToCardAndCardNumArray(List<int> list)
	{
		List<int> paiList = new List<int>();
		//添加第一个空数据
		paiList.Add(0);
		for (int i = 1; i <= 27; i++)
		{
			paiList.Add(0);
			for (int j = 0; j < list.Count; j++)
			{
				if (list[j] + 1 == i)
				{
					paiList[i]++;
				}
			}
		}
		return paiList;
	}

	/// <summary>
	/// 将对，胡牌判断
	/// </summary>
	/// <returns><c>true</c>, if jiang dui was ised, <c>false</c> otherwise.</returns>
	/// <param name="list">List.</param>
	public static bool IsJiangDui(List<int> list)
	{
		bool result = false;
		int count2 = 0;
		int count1 = 0;
		int count4 = 0;
		List<int> paiList = ToCardAndCardNumArray(list);
		for (int i = 0; i < paiList.Count; i++)
		{
			if (paiList[i] == 1)
			{
				count1++;
			}
			else if (paiList[i] == 2)
			{
				count2++;
			}
			else if (paiList[i] == 4)
			{
				count4++;
			}
		}
		if (count2 == 1 && count4 == 0 && count1 == 0)
		{
			result = true;
			for (int i = 0; i < paiList.Count; i++)
			{
				if (paiList[i] > 0)
				{
					if (!(paiList[i] % 9 == 2 || paiList[i] % 9 == 5 || paiList[i] % 9 == 8))
					{
						result = false;
					}
				}
			}
		}
		return result;

	}
}