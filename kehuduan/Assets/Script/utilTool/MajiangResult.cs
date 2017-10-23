using UnityEngine;
using System.Collections;

public class MajiangResult
{
    /// 上家 对家 下家  自己   胡牌名称                  
    /// 合计 点炮，自摸（清一色，杠*1）

    //上家分数
    public int leftScore;

	//下家分数
	public int rightScore;

	//对家分数
	public int topScore;

	//自己分数
	public int bottomScore;

	//杠的个数
	public int gangCount;

	//1,七对
    //2,碰碰胡
    //3,屁胡
    //4,查花猪
    //5,查大叫
	public  int  huType;

	//是否是自摸
    public bool isZiMo;

    //总得分
    public int allScore;
}
