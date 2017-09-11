using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PutCard {

    public bool leftHu = false;
	public bool TopHu = false;
    public bool RightHu = false;
    public bool BottomHu = false;

    //是否是杠牌
    public bool IsGangCard = false;

    //那位玩家打出的牌
    public int CardPosition = -1;

    //牌对应的数字
    public int CardToNum = -1;

    public void resetData(){
        leftHu = false;
        TopHu = false;
        RightHu = false;
        BottomHu = false;
        IsGangCard = false;  
        CardPosition = -1;
        CardToNum = -1;
    }
}
