﻿using System;
using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using AssemblyCSharp;
using DG.Tweening;
using UnityEngine.UI;
using LitJson;



public class MyMahjongScript : MonoBehaviour
{
	public double lastTime;
	public Text Number;
	public Text roomRemark;
	public Image headIconImg;
	public GameObject pengEffectGame;
	public GameObject gangEffectGame;
	public GameObject huEffectGame;
	public GameObject liujuEffectGame;
	public int otherPengCard;
	public int otherGangCard;
	public ButtonActionScript btnActionScript;
	public List<Transform> parentList;
	public List<Transform> outparentList;
	public List<GameObject> dirGameList;
	public List<PlayerItemScript> playerItems;
	public Text LeavedCastNumText;//剩余牌的张数
	public Text LeavedRoundNumText;//剩余局数
	//public int StartRoundNum;
	public Transform pengGangParenTransformB;
	public Transform pengGangParenTransformL;
	public Transform pengGangParenTransformR;
	public Transform pengGangParenTransformT;
	public List<AvatarVO> avatarList;
	public Image weipaiImg;
	public Button inviteFriendButton;
	public  Button ExitRoomButton;

	public Image live1;
	public Image live2;
	public Image centerImage;
	public GameObject noticeGameObject;
	public Text noticeText;
	public GameObject genZhuang;
	public Text versionText;

	//======================================
	private int uuid;
	private float timer = 0;
	private int LeavedCardsNum;
	private int MoPaiCardPoint;
	private List<List<GameObject>> PengGangCardList; //自己碰杠牌组 存放的是Gameobjct数组
	private List<List<GameObject>> PengGangList_L;   //左边碰杠牌组 存放的是Gameobjct数组
	private List<List<GameObject>> PengGangList_T;   //上边碰杠牌组 存放的是Gameobjct数组
	private List<List<GameObject>> PengGangList_R;   //右边碰杠牌组 存放的是Gameobjct数组
	private string effectType;
	private List<List<int>> mineList;
	private int gangKind;
	private int otherGangType;
	private GameObject cardOnTable;
	/// <summary>
	/// 
	/// </summary>
	private int useForGangOrPengOrChi;
    /// <summary>
    /// 杠的牌数
    /// </summary>
	private int selfGangCardPoint;
	/// <summary>
	/// 庄家的索引
	/// </summary>
	private int bankerId;
	private int curDirIndex;
	private GameObject curCard;
	/// <summary>
	/// 打出来的牌
	/// </summary>
	private GameObject putOutCard;

	private int otherMoCardPoint;
	private GameObject Pointertemp;
	private int putOutCardPoint = -1;//打出的牌
	private int putOutCardPointAvarIndex=-1;//最后一个打出牌的人的index
	private string outDir;
	private int SelfAndOtherPutoutCard = -1;
	/// <summary>
	/// 当前摸的牌
	/// </summary>
	private GameObject pickCardItem;
	private GameObject otherPickCardItem;
	/// <summary>
	/// 当前的方向字符串
	/// </summary>
	private string curDirString = "B";
	/// <summary>
	/// 普通胡牌算法
	/// </summary>
	private NormalHuScript norHu;
	/// <summary>
	/// 赖子胡牌算法
	/// </summary>
	private NaiziHuScript naiziHu;
	// Use this for initialization
	private GameToolScript gameTool;
	/**抓码动态面板**/
	private GameObject zhuamaPanel;
	/**游戏单局结束动态面板**/
	//private GameObject singalEndPanel;
	//private List<int> GameOverPlayerCoins;


	private int showTimeNumber = 0;
	private int showNoticeNumber = 0;
	private bool timeFlag = false;
	/// <summary>
	/// 手牌数组，0自己，1-右边。2-上边。3-左边
	/// </summary>
	public List<List<GameObject>> handerCardList;
	/// <summary>
	/// 打在桌子上的牌
	/// </summary>
	public List<List<GameObject>> tableCardList;
	/**后台传过来的杠牌**/
	private string[] gangPaiList;

	/**所有的抓码数据字符串**/
	private string allMas;

	private bool isFirstOpen = true;

	/**是否为抢胡 游戏结束时需置为false**/
	private bool isQiangHu = false;
	/**更否申请退出房间申请**/
	private bool canClickButtonFlag = false;

	private string passType = "";

    //private bool isSelfPickCard = false;

    private bool waitting = false;

    //======================新增=========================
    List<int> allList = new List<int>();
    List<List<int>> rightList = new List<List<int>>();
    List<List<int>> topList = new List<List<int>>();
    List<List<int>> leftList = new List<List<int>>();


	void Start()
	{
		randShowTime ();
		timeFlag = true;
        SoundCtrl.getInstance ().stopBGM ();
		//===========================================================================================
		norHu = new NormalHuScript();
		naiziHu = new NaiziHuScript ();
		gameTool = new GameToolScript ();
		//versionText.text = "V" + Application.version;
		//===========================================================================================
		btnActionScript = gameObject.GetComponent<ButtonActionScript> ();
		initPanel ();
		initArrayList ();
		startGame ();
		//initPerson ();//初始化每个成员1000分
//
//		GlobalDataScript.isonLoginPage = false;
//		if (GlobalDataScript.reEnterRoomData != null) {
//			GlobalDataScript.loginResponseData.roomId = GlobalDataScript.reEnterRoomData.roomId;
//			reEnterRoom ();
//		} else {
//			//readyGame();
//			//markselfReadyGame ();
//		}
//		GlobalDataScript.reEnterRoomData = null;

	}

	void randShowTime(){
		showTimeNumber = (int)(UnityEngine.Random.Range(5000,10000));
	}

	void initPanel(){
		clean ();
		btnActionScript.cleanBtnShow ();
		//masContaner.SetActive (false);
	}




	private void initArrayList(){
		mineList = new List<List<int>>();
		handerCardList = new List<List<GameObject>> ();
		tableCardList = new List<List<GameObject>> ();
		for (int i = 0; i < 4; i++) {
			handerCardList.Add (new List<GameObject>());
			tableCardList.Add (new List<GameObject>());
		}

		PengGangList_L = new List<List<GameObject>> ();
		PengGangList_R = new List<List<GameObject>> ();
		PengGangList_T = new List<List<GameObject>> ();
		PengGangCardList=new List<List<GameObject>>();


	}

	/**
	private void initPerson(){
		GameOverPlayerCoins = new List<int> (4);
		GameOverPlayerCoins.Add(1000);
		GameOverPlayerCoins.Add(1000);
		GameOverPlayerCoins.Add(1000);
		GameOverPlayerCoins.Add(1000);
	}
	*/
	/// <summary>
	/// Cards the select.
	/// </summary>
	/// <param name="obj">Object.</param>
	public void cardSelect(GameObject obj)
	{
		Debug.Log("cardSelect begin");
		for (int i = 0; i < handerCardList[0].Count; i++)
		{
			if (handerCardList[0] [i] == null) {
				handerCardList[0].RemoveAt (i);
				i--;
			} else {
				handerCardList[0] [i].transform.localPosition = new Vector3 (handerCardList[0] [i].transform.localPosition.x, -292f); //从右到左依次对齐
				handerCardList[0] [i].transform.GetComponent<bottomScript> ().selected = false;
			}
		}
		if (obj != null)
		{
			obj.transform.localPosition = new Vector3(obj.transform.localPosition.x, -272f);
			obj.transform.GetComponent<bottomScript>().selected = true;
		}
        Debug.Log("cardSelect finish");
	}

	/// <summary>
	/// 开始游戏
	/// </summary>
	/// <param name="response">Response.</param>
	public void startGame()
	{
        faPai();
		//GlobalDataScript.surplusTimes -= 1;
		cleanGameplayUI ();
		//开始游戏后不显示
		//LeavedRoundNumText.text = GlobalDataScript.surplusTimes+"";//刷新剩余圈数
		if (!isFirstOpen) {
			btnActionScript = gameObject.GetComponent<ButtonActionScript> ();
			initPanel ();
			initArrayList ();
		}
			
		initArrayList ();
		curDirString = getDirection (bankerId);
		playerItems [curDirIndex].setbankImgEnable (true);
		SetDirGameObjectAction();
		isFirstOpen = false;
		GlobalDataScript.isOverByPlayer = false;

		//mineList = sgvo.paiArray;
		UpateTimeReStart ();

		setAllPlayerReadImgVisbleToFalse ();
		initMyCardListAndOtherCard (13,13,13);

		ShowLeavedCardsNumForInit();

		if (curDirString == DirectionEnum.Bottom) {
			//isSelfPickCard = true;
			GlobalDataScript.isDrag = true;
		} else {
			//isSelfPickCard = false;
			GlobalDataScript.isDrag = false;
		}
	}

	private void cleanGameplayUI(){
		canClickButtonFlag = true;
		weipaiImg.transform.gameObject.SetActive(false);
		inviteFriendButton.transform.gameObject.SetActive (false);
		ExitRoomButton.transform.gameObject.SetActive (false);
		live1.transform.gameObject.SetActive (true);
		live2.transform.gameObject.SetActive (true);
		centerImage.transform.gameObject.SetActive (true);
		liujuEffectGame.SetActive (false);
	}


	public void ShowLeavedCardsNumForInit()
	{
		//RoomCreateVo roomCreateVo = GlobalDataScript.roomVo;

		//bool hong = (bool) roomCreateVo.hong  ;
		//int RoomType = (int) roomCreateVo.roomType;
		//if (RoomType == 1)//转转麻将
		//{
			
  //          if (hong)
		//	{
		//		LeavedCardsNum = 112;
		//	}
		//}
		//else if (RoomType == 2)//划水麻将
		//{
		//	LeavedCardsNum = 108;
		//	if (roomCreateVo.addWordCard) {
		//		LeavedCardsNum = 136;
		//	}
		//}
		//else if (RoomType == 3)
		//{
		//	LeavedCardsNum = 108;
		//}
        LeavedCardsNum = 108;
		LeavedCardsNum = LeavedCardsNum - 53;
		LeavedCastNumText.text = (LeavedCardsNum)+"";


		/**
		GlobalDataScript.roomVo.roundNumber--;
		StartRoundNum = roomCreateVo.roundNumber;
		LeavedRoundNumText.text = StartRoundNum + "";
		*/
	}

	public void CardsNumChange()
	{
        //LeavedCardsNum--;
        //if (LeavedCardsNum < 0)
        //{
        //	LeavedCardsNum = 0;
        //}
        //LeavedCastNumText.text = LeavedCardsNum+ "";
        LeavedCastNumText.text = allList.Count.ToString();
	}

	/// <summary>
	/// 别人摸牌通知
	/// </summary>
	public void otherPickCard()
	{
		UpateTimeReStart ();
		//下一个摸牌人的索引
        int avatarIndex =curDirIndex;
		MyDebug.Log ("otherPickCard avatarIndex = "+avatarIndex);
		otherPickCardAndCreate (avatarIndex);
		SetDirGameObjectAction ();
        LeavedCastNumText.text = allList.Count.ToString();
	}

	private void otherPickCardAndCreate(int avatarIndex){
		//getDirection (avatarIndex);
		int myIndex = getMyIndexFromList ();
		int seatIndex = avatarIndex - myIndex;
		if (seatIndex < 0) {
			seatIndex = 4 + seatIndex;
		}
		curDirString = playerItems [seatIndex].dir;
		//SetDirGameObjectAction ();
		otherMoPaiCreateGameObject (curDirString);
	}

	public void otherMoPaiCreateGameObject(string dir){
		Vector3 tempVector3 = new Vector3(0,0);
		//Transform tempParent = null;
		switch (dir)
		{
		case DirectionEnum.Top://上
			//tempParent = topParent.transform;
			tempVector3 = new Vector3(-273,0f);
			break;
		case DirectionEnum.Left://左
			//tempParent = leftParent.transform;
			tempVector3 = new Vector3(0, -173f);

			break;
		case DirectionEnum.Right://右
			//tempParent = rightParent.transform;
			tempVector3 = new Vector3(0, 183f);
			break;
		}

		String path = "prefab/card/Bottom_" + dir;
		MyDebug.Log("path  = "+ path);
		otherPickCardItem = createGameObjectAndReturn (path,parentList[getIndexByDir(dir)],tempVector3);//实例化当前摸的牌
		otherPickCardItem.transform.localScale = Vector3.one;//原大小

	}

	/// <summary>
	/// 自己摸牌
	/// </summary>
	/// <param name="response">Response.</param>
	public void pickCard(ClientResponse response)
	{

		UpateTimeReStart ();
		CardVO cardvo = JsonMapper.ToObject<CardVO>(response.message);
		MoPaiCardPoint = cardvo.cardPoint;
		MyDebug.Log ("摸牌" + MoPaiCardPoint);
		SelfAndOtherPutoutCard = MoPaiCardPoint; 
		useForGangOrPengOrChi = cardvo.cardPoint;
		putCardIntoMineList (MoPaiCardPoint);
		moPai ();
		curDirString = DirectionEnum.Bottom;
		SetDirGameObjectAction ();
		CardsNumChange();
		//checkHuOrGangOrPengOrChi (MoPaiCardPoint,2);
		GlobalDataScript.isDrag = true;
	//	isSelfPickCard = true;
	}
	/// <summary>
	/// 胡，杠，碰，吃，pass按钮显示.
	/// </summary>
	/// <param name="response">Response.</param>
	//public void actionBtnShow(ClientResponse response){
	//	GlobalDataScript.isDrag = false;
	//	string[] strs=response.message.Split (new char[1]{','});
	//	if (curDirString == DirectionEnum.Bottom) {
	//		passType = "selfPickCard";
	//	} else {
	//		passType = "otherPickCard";
	//	}

	//	for (int i = 0; i < strs.Length; i++) {
	//		if (strs [i].Equals ("hu")) {
	//			btnActionScript.showBtn (1);

	//		}else if(strs[i].Contains("qianghu")){
				
	//			try{
	//				SelfAndOtherPutoutCard = int.Parse( strs[i].Split(new char[1]{':'})[1]);
	//			}catch (Exception e){
				
	//			}

	//			btnActionScript.showBtn (1);
	//			isQiangHu = true;
	//		}else if(strs[i].Contains("peng")){
	//			btnActionScript.showBtn (3);
	//			putOutCardPoint =int.Parse(strs [i].Split (new char[1]{ ':' }) [2]);


	//		}else if(strs[i].Equals("chi")){
	//			//btnActionScript.showBtn (3);
	//		}
	//		if(strs[i].Contains("gang")){
				
	//			btnActionScript.showBtn (2);
	//			gangPaiList = strs [i].Split (new char[1]{ ':' });
	//			List<string> gangPaiListTemp = gangPaiList.ToList ();
	//			gangPaiListTemp.RemoveAt (0);
	//			gangPaiList = gangPaiListTemp.ToArray ();
	//		}
	//	}
	//}

	private void initMyCardListAndOtherCard(int topCount,int leftCount,int rightCount){
        addList(mineList,14);
		for (int a = 0; a < mineList[0].Count; a++)//我的牌13张
		{
					GameObject gob = Instantiate(Resources.Load("prefab/card/Bottom_B")) as GameObject;
					//GameObject.Instantiate ("");
					if (gob != null)//
					{
						gob.transform.SetParent(parentList[0]);//设置父节点
						gob.transform.localScale =  new Vector3(1.1f,1.1f,1);
						gob.GetComponent<bottomScript>().onSendMessage += cardChange;//发送消息fd
						gob.GetComponent<bottomScript>().reSetPoisiton += cardSelect;
                        gob.GetComponent<bottomScript>().setPoint(mineList[0][a]);//设置指针          
						SetPosition(false);
						handerCardList[0].Add(gob);//增加游戏对象
					}
					else
					{
						Debug.Log("--> gob is null");//游戏对象为空
					}
		}

		initOtherCardList (DirectionEnum.Left,leftCount);
		initOtherCardList (DirectionEnum.Right,rightCount);
		initOtherCardList (DirectionEnum.Top,topCount);

		if (bankerId == getMyIndexFromList ()) {
			SetPosition (true);//设置位置
			MyDebug.Log ("初始化数据自己为庄家");
		//	checkHuPai();
		} else {
			SetPosition (false);
			otherPickCardAndCreate (bankerId);
		}
	}




	/// <summary>
	/// 检测胡牌
	/// </summary>
	/**
	private bool checkHuPai(){
		RoomCreateVo roomvo = GlobalDataScript.roomVo;
		if (roomvo.hong) {
			if (naiziHu.isHu (mineList)) {
				MyDebug.Log ("赖子胡牌了");
				effectType = "hu";
				pengGangHuEffectCtrl();
				return true;

			} else {
				GlobalDataScript.isDrag = true;
				return false;
			}
		} else {
			if (roomvo.sevenDouble) {
				int result = norHu.checkSevenDouble (mineList [0]);
				if (result == 0) {
				} else {
					effectType = "hu";
					pengGangHuEffectCtrl();
					return true;
				}
			}
			if (norHu.isHuPai (mineList [0]))
			{
				MyDebug.Log ("胡牌了");
				effectType = "hu";
				pengGangHuEffectCtrl();
				return true;
			} else {
				GlobalDataScript.isDrag = true;
				return false;
			}
		}

	}
	*/

	/*
	private bool addPointAndCheckHu(int cardPoint){
		bool result = false;
		putCardIntoMineList (cardPoint);
		result = checkHuPai ();
		pushOutFromMineList (cardPoint);
		return result;
	}
	*/

	private void setAllPlayerReadImgVisbleToFalse(){
		for (int i = 0; i < playerItems.Count; i++) {
			playerItems [i].readyImg.enabled=false;
		}
	}
	private void setAllPlayerHuImgVisbleToFalse(){
		for (int i = 0; i < playerItems.Count; i++) {
			playerItems [i].setHuFlagHidde ();
		}
	}

	/// <summary>
	/// Gets the index by dir.
	/// </summary>
	/// <returns>The index by dir.</returns>
	/// <param name="dir">Dir.</param>
	private int getIndexByDir(string dir){
		int result = 0;
		switch (dir)
		{
		case DirectionEnum.Top: //上
			result = 2;
			break;
		case DirectionEnum.Left : //左
			result = 3;
			break;
		case DirectionEnum .Right: //右
			result = 1;
			break;
		case DirectionEnum.Bottom: //下
			result = 0;
			break;
		}
		return result;
	}
	/// <summary>
	/// 
	/// </summary>
	/// <param name="initDirection"></param>
	private void initOtherCardList(string initDiretion,int count) //初始化
	{
		for (int i = 0; i < count; i++)
		{
			GameObject temp = Instantiate(Resources.Load("Prefab/card/Bottom_" + initDiretion)) as GameObject; //实例化当前牌
			if (temp != null) //有可能没牌了
			{
				temp.transform.SetParent(parentList[getIndexByDir(initDiretion)]); //父节点
                temp.transform.localScale=Vector3.one;
				switch (initDiretion)
				{
				case DirectionEnum.Top: //上
                        if(topList.Count==0){
                            addList(topList,count);
                        }    
					temp.transform.localPosition = new Vector3(-204+ 38*i, 0); //位置   
					handerCardList[2].Add(temp);
				temp.transform.localScale = Vector3.one; //原大小
                        break;
				case DirectionEnum.Left: //左
                        if (leftList.Count == 0)
						{
							addList(leftList, count);
						}
						temp.transform.localPosition = new Vector3(0, -105 + i*30); //位置   
				        temp.transform.SetSiblingIndex(0);
                        handerCardList[3].Add(temp);
					break;
				case DirectionEnum.Right: //右
                        if (rightList.Count == 0)
						{
							addList(rightList, count);
						}
					temp.transform.localPosition = new Vector3(0, 119 - i*30); //位置     
                        handerCardList[1].Add(temp);
					break;
				}
			}

		}
	}

	/// <summary>
	/// 
	/// </summary>
	public void moPai() //摸牌
	{
		pickCardItem = Instantiate(Resources.Load("prefab/card/Bottom_B")) as GameObject; //实例化当前摸的牌
		MyDebug.Log ("摸牌 === >> "+MoPaiCardPoint);
		if (pickCardItem != null) //有可能没牌了
		{
			pickCardItem.name = "pickCardItem";
			pickCardItem.transform.SetParent(parentList[0]); //父节点
			pickCardItem.transform.localScale = new Vector3(1.1f,1.1f,1);//原大小
			pickCardItem.transform.localPosition = new Vector3(580f, -292f); //位置
			pickCardItem.GetComponent<bottomScript>().onSendMessage += cardChange; //发送消息
			pickCardItem.GetComponent<bottomScript>().reSetPoisiton += cardSelect;
			pickCardItem.GetComponent<bottomScript>().setPoint(MoPaiCardPoint); //得到索引
			insertCardIntoList(pickCardItem);
		}
		MyDebug.Log ("moPai  goblist count === >> "+ handerCardList[0].Count);

	}

	public void putCardIntoMineList(int cardPoint)
	{
		if (mineList[0][cardPoint] < 4)
		{
			mineList[0][cardPoint]++;

		}
	}
    //将该点数的数字移除
	public void pushOutFromMineList(int cardPoint)
	{
        for (var i = 0; i < mineList.Count();i++){
            if(mineList[0][i]==cardPoint){
                mineList[0].RemoveAt(i);
            }
        }
	}

	/// <summary>
	/// 接收到其它人的出牌通知
	/// </summary>
	public void otherPutOutCard()
	{
		
        int cardPoint = allList[0]/4;
		allList.RemoveAt(0);
        putOutCardPoint = cardPoint;
        SelfAndOtherPutoutCard = cardPoint;
        int curAvatarIndex =curDirIndex;
		putOutCardPointAvarIndex =getIndexByDir(getDirection(curAvatarIndex)) ;
		MyDebug.Log ("otherPickCard avatarIndex = "+curAvatarIndex);
		useForGangOrPengOrChi = cardPoint;
		if (otherPickCardItem != null) {
			int dirIndex = getIndexByDir (getDirection (curAvatarIndex));
			Destroy (otherPickCardItem);
			otherPickCardItem = null;

		} else {
			int dirIndex = getIndexByDir (getDirection (curAvatarIndex));
			GameObject obj = handerCardList [dirIndex] [0];
			handerCardList [dirIndex].RemoveAt (0);
			Destroy (obj);

		}
		createPutOutCardAndPlayAction(cardPoint, curAvatarIndex);

      
	}
	/// <summary>
	/// 创建打来的的牌对象，并且开始播放动画
	/// </summary>
	/// <param name="cardPoint">Card point.</param>
	/// <param name="curAvatarIndex">Current avatar index.</param>
	private void createPutOutCardAndPlayAction(int cardPoint, int curAvatarIndex)
	{
		MyDebug.Log ("put out cardPoint"+cardPoint);
        //暂时屏蔽掉声音
		SoundCtrl.getInstance ().playSound (cardPoint,0);
		Vector3 tempVector3 = new Vector3(0, 0);

		outDir = getDirection(curAvatarIndex);
		switch (outDir)
		{
		case DirectionEnum.Top: //上
			tempVector3 = new Vector3(0, 130f);
			break;
		case DirectionEnum.Left: //左
			tempVector3 = new Vector3(-370, 0f);
			break;
		case DirectionEnum.Right: //右
			tempVector3 = new Vector3(420f, 0f);
			break;
		case DirectionEnum.Bottom:
			tempVector3 = new Vector3(0, -100f);
			break;
		}

		GameObject tempGameObject = createGameObjectAndReturn ("Prefab/card/PutOutCard",parentList[0],tempVector3);
		tempGameObject.name = "putOutCard";
		tempGameObject.transform.localScale = Vector3.one;
		tempGameObject.GetComponent<TopAndBottomCardScript>().setPoint(cardPoint);
		putOutCardPoint = cardPoint;
		SelfAndOtherPutoutCard = cardPoint;
		putOutCard = tempGameObject;
        Debug.Log("destroyPutOutCard begin");
		destroyPutOutCard(cardPoint);
        Debug.Log("destroyPutOutCard end");
		if (putOutCard != null)
		{
			Destroy(putOutCard, 1f);
		}
	}


	/// <summary>
	/// 根据一个人在数组里的索引，得到这个人所在的方位，L-左，T-上,R-右，B-下（自己的方位永远都是在下方）
	/// </summary>
	/// <returns>The direction.</returns>
	/// <param name="avatarIndex">Avatar index.</param>
	private String getDirection(int avatarIndex)
	{
		String result = DirectionEnum.Bottom;
		int myselfIndex = getMyIndexFromList();
		if (myselfIndex == avatarIndex)
		{
			MyDebug.Log ("getDirection == B");
			curDirIndex = 0;
			return result;
		}
		//从自己开始计算，下一位的索引
		for (int i = 0; i < 4; i++)
		{
			myselfIndex++;
			if (myselfIndex >= 4)
			{
				myselfIndex = 0;
			}
			if (myselfIndex == avatarIndex)
			{
				if (i == 0)
				{
					MyDebug.Log ("getDirection == R");
					curDirIndex = 1;
					return DirectionEnum.Right;
				}
				else if (i == 1)
				{
					MyDebug.Log ("getDirection == T");
					curDirIndex = 2;
					return DirectionEnum.Top;
				}
				else
				{
					MyDebug.Log ("getDirection == L");
					curDirIndex = 3;
					return DirectionEnum.Left;
				}
			}
		}
		MyDebug.Log ("getDirection == B");
		curDirIndex = 0;
		return DirectionEnum.Bottom;
	}
	/// <summary>
	/// 设置红色箭头的显示方向
	/// </summary>
	public void SetDirGameObjectAction() //设置方向
	{
		//UpateTimeReStart();
		for (int i = 0; i < dirGameList.Count; i++) {
			dirGameList [i].SetActive (false);
		}
		dirGameList[getIndexByDir(curDirString)].SetActive(true);
	}

	public void ThrowBottom(int index)//
	{
		GameObject temp = null;
		String path = "";
		Vector3 poisVector3 = Vector3.one;

		if (outDir == DirectionEnum.Bottom)
		{
			path = "Prefab/ThrowCard/TopAndBottomCard";
			poisVector3 = new Vector3(-261 + tableCardList[0].Count%14*37, (int)(tableCardList[0].Count/14)*67f);
			GlobalDataScript.isDrag = false;
		}
		else if (outDir == DirectionEnum.Right)
		{
			path = "Prefab/ThrowCard/ThrowCard_R";
			poisVector3 = new Vector3((int)(-tableCardList[1].Count/13*54f), -180f + tableCardList[1].Count%13*28);
		}
		else if (outDir == DirectionEnum.Top)
		{
			path = "Prefab/ThrowCard/TopAndBottomCard";
			poisVector3 = new Vector3(289f - tableCardList[2].Count%14*37, -(int)(tableCardList[2].Count/14)*67f);
		}
		else if (outDir == DirectionEnum.Left)
		{
			path = "Prefab/ThrowCard/ThrowCard_L";
			poisVector3 = new Vector3(tableCardList[3].Count/13*54f, 152f - tableCardList[3].Count%13*28);
			//     parenTransform = leftOutParent;
		}

		temp = createGameObjectAndReturn (path,outparentList[curDirIndex],poisVector3);
		temp.transform.localScale = Vector3.one;
		if (outDir == DirectionEnum.Right || outDir == DirectionEnum.Left) {
			temp.GetComponent<TopAndBottomCardScript> ().setLefAndRightPoint (index);
		} else {
			temp.GetComponent<TopAndBottomCardScript>().setPoint(index);
		}

		cardOnTable = temp;
		//temp.transform.SetAsLastSibling();
		tableCardList[getIndexByDir(outDir)].Add(temp);
		if (outDir == DirectionEnum.Right) {
			temp.transform.SetSiblingIndex(0);
		}
		//丢牌上
		//顶针下
		setPointGameObject(temp);
	}

	public void otherPeng(ClientResponse response)//其他人碰牌
	{
		UpateTimeReStart ();
		OtherPengGangBackVO cardVo = JsonMapper.ToObject<OtherPengGangBackVO>(response.message);
		otherPengCard = cardVo.cardPoint;
		curDirString = getDirection (cardVo.avatarId);
		print("Current Diretion==========>" + curDirString);
		SetDirGameObjectAction ();
		effectType = "peng";
		pengGangHuEffectCtrl();
		SoundCtrl.getInstance ().playSoundByAction ("peng",0);
		if (cardOnTable != null) {
			reSetOutOnTabelCardPosition (cardOnTable);
			Destroy (cardOnTable);
		}


		if (curDirString == DirectionEnum.Bottom) {  //==============================================自己碰牌
			mineList [0] [putOutCardPoint]++;
			mineList [1] [putOutCardPoint] = 2;
			int removeCount = 0;
			for (int i = 0; i < handerCardList [0].Count; i++) {
				GameObject temp = handerCardList [0] [i];
				int tempCardPoint = temp.GetComponent<bottomScript> ().getPoint ();
				if (tempCardPoint == putOutCardPoint) {

					handerCardList [0].RemoveAt (i);
					Destroy (temp);
					i--;
					removeCount++;
					if (removeCount == 2) {
						break;
					}
				}
			}
			SetPosition (true);
			bottomPeng ();
		
		} else {//==============================================其他人碰牌
			List<GameObject> tempCardList = handerCardList[getIndexByDir(curDirString)];
			string path= "Prefab/PengGangCard/PengGangCard_"+curDirString;
			if (tempCardList != null) {
				MyDebug.Log ("tempCardList.count======前"+tempCardList.Count);
				for (int i = 0; i < 2; i++)//消除其他的人牌碰牌长度
				{
					GameObject temp = tempCardList[0];
					Destroy(temp);
					tempCardList.RemoveAt(0);

				}
				MyDebug.Log ("tempCardList.count======前"+tempCardList.Count);

				otherPickCardItem = tempCardList [0];
				gameTool.setOtherCardObjPosition(tempCardList, curDirString, 1);
				//Destroy (tempCardList [0]);
				tempCardList.RemoveAt (0);
			}
			Vector3 tempvector3 = new Vector3(0, 0, 0);
			List<GameObject> tempList = new List<GameObject> ();

			switch (curDirString) {
			case DirectionEnum.Right:
				for (int i = 0; i < 3; i++) {
					GameObject obj = Instantiate(Resources.Load(path)) as GameObject;
					obj.GetComponent<TopAndBottomCardScript>().setLefAndRightPoint(cardVo.cardPoint);
					tempvector3 = new Vector3(0, -122 + PengGangList_R.Count*95 + i*26f);
					//+ new Vector3(0, i * 26, 0);
					obj.transform.parent = pengGangParenTransformR.transform;
					obj.transform.SetSiblingIndex(0);
					obj.transform.localScale = Vector3.one;
					obj.transform.localPosition = tempvector3;
					tempList.Add(obj);
				}
				break;
			case DirectionEnum.Top:
				for (int i = 0; i < 3; i++) {
					GameObject obj = Instantiate(Resources.Load(path)) as GameObject;
					obj.GetComponent<TopAndBottomCardScript>().setPoint(cardVo.cardPoint);
					tempvector3 = new Vector3(251 - PengGangList_T.Count*120f + i*37, 0, 0);
					obj.transform.parent = pengGangParenTransformT.transform;
					obj.transform.localScale = Vector3.one;
					obj.transform.localPosition = tempvector3;
					tempList.Add(obj);
				}
				break;
			case DirectionEnum.Left:
				for (int i = 0; i < 3; i++) {
					GameObject obj = Instantiate(Resources.Load(path)) as GameObject;
					obj.GetComponent<TopAndBottomCardScript>().setLefAndRightPoint(cardVo.cardPoint);
					tempvector3 = new Vector3(0, 122 - PengGangList_L.Count*95f - i*26f, 0);
					obj.transform.parent = pengGangParenTransformL.transform;
					obj.transform.localScale = Vector3.one;
					obj.transform.localPosition = tempvector3;
					tempList.Add(obj);
				}
				break;
			}
			addListToPengGangList(curDirString, tempList);
		}


	}

	private void bottomPeng()
	{
		List<GameObject> templist = new List<GameObject>();
		for (int j = 0; j < 3; j++)

		{
			GameObject obj1 = createGameObjectAndReturn("Prefab/PengGangCard/PengGangCard_B",
				pengGangParenTransformB.transform,
				new Vector3(-370 + PengGangCardList.Count * 190 + j * 60f, 0));
			obj1.GetComponent<TopAndBottomCardScript>().setPoint(putOutCardPoint);
			obj1.transform.localScale = Vector3.one;
			templist.Add(obj1);
		}
		PengGangCardList.Add(templist);
		GlobalDataScript.isDrag = true;
	}
	private void  pengGangHuEffectCtrl()
	{
		if (effectType == "peng")
		{
			pengEffectGame.SetActive (true);
			// pengEffectGameList[getIndexByDir(curDirString)].SetActive(true);
		}
		else if (effectType == "gang")
		{
			gangEffectGame.SetActive (true);
			// gangEffectGameList[getIndexByDir(curDirString)].SetActive(true);
		}
		else if (effectType == "hu")
		{
			huEffectGame.SetActive (true);
			// huEffectGameList[getIndexByDir(curDirString)].SetActive(true);
		}else if(effectType == "liuju"){
			liujuEffectGame.SetActive (true);
		}
		invokeHidePengGangHuEff();
	}

	private void invokeHidePengGangHuEff()
	{
		Invoke("HidePengGangHuEff", 1f);
	}

	private void HidePengGangHuEff()
	{
		//   pengEffectGameList[getIndexByDir(curDirString)].SetActive(false);
		// gangEffectGameList[getIndexByDir(curDirString)].SetActive(false);
		// huEffectGameList[getIndexByDir(curDirString)].SetActive(false);
		pengEffectGame.SetActive(false);
		gangEffectGame.SetActive (false);
		huEffectGame.SetActive (false);
	}

	private void otherGang(ClientResponse response) //其他人杠牌
	{

		GangNoticeVO gangNotice = JsonMapper.ToObject<GangNoticeVO>(response.message);
		otherGangCard = gangNotice.cardPoint;
		otherGangType = gangNotice.type;
		string path = "";
		string path2 = "";
		Vector3 tempvector3 = new Vector3(0, 0, 0);
		curDirString = getDirection(gangNotice.avatarId);
		effectType = "gang";
		pengGangHuEffectCtrl ();
		SetDirGameObjectAction();
		SoundCtrl.getInstance().playSoundByAction("gang", 0);
		List<GameObject> tempCardList = null;


		//确定牌背景（明杠，暗杠）
		switch (curDirString)
		{
		case DirectionEnum.Right:
			tempCardList = handerCardList[1];
			path = "Prefab/PengGangCard/PengGangCard_R";
			path2 = "Prefab/PengGangCard/GangBack_L&R";
			break;
		case DirectionEnum.Top:
			tempCardList = handerCardList[2];
			path = "Prefab/PengGangCard/PengGangCard_T";
			path2 = "Prefab/PengGangCard/GangBack_T";
			break;
		case DirectionEnum.Left:
			tempCardList = handerCardList[3];
			path = "Prefab/PengGangCard/PengGangCard_L";
			path2 = "Prefab/PengGangCard/GangBack_L&R";
			break;
		}


		List<GameObject> tempList = new List<GameObject>();
		if(getPaiInpeng(otherGangCard,curDirString) == -1){


			//删除玩家手牌，当玩家碰牌牌组里面的有碰牌时，不用删除手牌
			for (int i = 0; i < 3; i++)
			{
				GameObject temp = tempCardList[0];
				tempCardList.RemoveAt(0);
				Destroy(temp);
			}
			SetPosition (false);

			if( tempCardList != null)
			{
				gameTool.setOtherCardObjPosition(tempCardList, curDirString, 2);
			}

			//创建杠牌，当玩家碰牌牌组里面的无碰牌，才创建

			if (otherGangType == 0)
			{
				if (cardOnTable != null)
				{
					reSetOutOnTabelCardPosition(cardOnTable);
					Destroy(cardOnTable);
				}
				for (int i = 0; i < 4; i++) //实例化其他人杠牌
				{
					GameObject obj1 = Instantiate(Resources.Load(path)) as GameObject;


					switch (curDirString)
					{
					case DirectionEnum.Right:
						obj1 .GetComponent< TopAndBottomCardScript >().setLefAndRightPoint(otherGangCard);
						if (i == 3) {
							tempvector3 = new Vector3 (0f, -122 + PengGangList_R.Count * 95 + 33f);
							obj1.transform.parent = pengGangParenTransformR.transform;
						} else {
							tempvector3 = new Vector3 (0, -122 + PengGangList_R.Count * 95 + i * 28f);
							obj1.transform.parent = pengGangParenTransformR.transform;
							obj1.transform.SetSiblingIndex (0);
						}

						break;
					case DirectionEnum.Top:
						obj1 .GetComponent< TopAndBottomCardScript >().setPoint(otherGangCard);
						if (i == 3) {
							tempvector3 = new Vector3 (251 - PengGangList_T.Count * 120f + 37f, 20f);
						} else {
							tempvector3 = new Vector3 (251 - PengGangList_T.Count * 120f + i * 37, 0f);
						}

						obj1.transform.parent = pengGangParenTransformT.transform;
						break;
					case DirectionEnum.Left:
						obj1 .GetComponent< TopAndBottomCardScript >().setLefAndRightPoint(otherGangCard);
						if (i == 3) {
							tempvector3 = new Vector3 (0f, 122 - PengGangList_L.Count * 95f - 18f);
						} else {
							tempvector3 = new Vector3 (0f, 122 - PengGangList_L.Count * 95f - i * 28f);
						}

						obj1.transform.parent = pengGangParenTransformL.transform;
						break;
					}
					obj1.transform.localScale = Vector3.one;
					obj1.transform.localPosition = tempvector3;
					tempList.Add(obj1);
				}
			}
			else if (otherGangType == 1)
			{
				Destroy (otherPickCardItem);
				for (int j = 0; j < 4; j++)
				{
					GameObject obj2;
					if (j == 3) {
						obj2 = Instantiate (Resources.Load (path)) as GameObject;
					} else {
						obj2 = Instantiate (Resources.Load (path2)) as GameObject;
					}

					switch (curDirString)
					{
					case DirectionEnum.Right:
						obj2.transform.parent = pengGangParenTransformR.transform;
						if (j == 3) {
							tempvector3 = new Vector3(0f, -122 + PengGangList_R.Count*95+33f);
							obj2.GetComponent<TopAndBottomCardScript> ().setLefAndRightPoint (otherGangCard);

						} else {
							tempvector3 = new Vector3(0, -122 + PengGangList_R.Count*95+j*28);
						}

						break;
					case DirectionEnum.Top:
						obj2.transform.parent = pengGangParenTransformT.transform;
						if (j == 3) {
							tempvector3 = new Vector3 (251 - PengGangList_T.Count * 120f + 37f, 10f);
							obj2.GetComponent<TopAndBottomCardScript> ().setPoint (otherGangCard);
						} else {
							tempvector3 = new Vector3 (251 - PengGangList_T.Count * 120f + j * 37, 0f);
						}

						break;
					case DirectionEnum.Left:
						obj2.transform.parent = pengGangParenTransformL.transform;
						if (j == 3) {
							tempvector3 = new Vector3 (0f, 122 - PengGangList_L.Count * 95f -18f, 0);
							obj2.GetComponent<TopAndBottomCardScript> ().setLefAndRightPoint (otherGangCard);
						} else {
							tempvector3 = new Vector3(0, 122 - PengGangList_L.Count*95-j*28f,0);
						}

						break;
					}

					obj2.transform.localScale = Vector3.one;
					obj2.transform.localPosition = tempvector3;
					tempList.Add(obj2);
				}
			

			}
			addListToPengGangList(curDirString,tempList);
			//Destroy (otherPickCardItem);

		} else if (getPaiInpeng(otherGangCard,curDirString) != -1){/////////end of if(getPaiInpeng(otherGangCard,curDirString) == -1)

			int gangIndex = getPaiInpeng(otherGangCard,curDirString);

			if (otherPickCardItem != null) {
				Destroy (otherPickCardItem);
			}

			GameObject objTemp = Instantiate(Resources.Load(path)) as GameObject;
			switch (curDirString){
			case DirectionEnum.Top:
				objTemp.transform.parent = pengGangParenTransformT.transform;
				tempvector3 = new Vector3 (251 - gangIndex * 120f + 37f, 20f);
				objTemp.GetComponent<TopAndBottomCardScript> ().setPoint (otherGangCard);
				PengGangList_T[gangIndex].Add (objTemp);
				break;
			case DirectionEnum.Left:
				objTemp.transform.parent = pengGangParenTransformL.transform;
				tempvector3 = new Vector3 (0f, 122 - gangIndex * 95f - 26f, 0);
				objTemp.GetComponent<TopAndBottomCardScript> ().setLefAndRightPoint (otherGangCard);

				PengGangList_L[gangIndex].Add(objTemp);
				break;
			case DirectionEnum.Right:
				objTemp.transform.parent = pengGangParenTransformR.transform;
				tempvector3 = new Vector3 (0f, -122 + gangIndex * 95f + 26f);
				objTemp.GetComponent<TopAndBottomCardScript> ().setLefAndRightPoint (otherGangCard);

				PengGangList_R[gangIndex]. Add(objTemp);
				break;
			}
			objTemp.transform.localScale = Vector3.one;
			objTemp.transform.localPosition = tempvector3;

		}



	}


	private void addListToPengGangList(string dirString,List<GameObject> tempList){
		switch (dirString)
		{
		case DirectionEnum.Right:
			PengGangList_R.Add (tempList);
			break;
		case DirectionEnum.Top:
			PengGangList_T.Add (tempList);
			break;
		case DirectionEnum .Left:
			PengGangList_L.Add (tempList);
			break;
		}
	}

	/**
	 * 
	 * 判断碰牌的牌组里面是否包含某个牌，用于判断是否实例化一张牌还是三张牌
	 * cardpoint：牌点
	 * direction：方向
	 * 返回-1  代表没有牌
	 * 其余牌在list的位置
	 */
	private int getPaiInpeng(int cardPoint,string direction){
		List<List<GameObject>> jugeList = new List<List<GameObject>>();
		switch (direction) {
		case DirectionEnum.Bottom://自己
			jugeList = PengGangCardList;
			break;
		case DirectionEnum.Right:
			jugeList = PengGangList_R;
			break;
		case DirectionEnum.Left:
			jugeList = PengGangList_L;
			break;
		case DirectionEnum.Top:
			jugeList = PengGangList_T;
			break;
		}

		if (jugeList == null ||jugeList.Count ==0) {

			return -1;
		} 

		//循环遍历比对点数
		for (int i = 0; i < jugeList.Count; i++) {

			try{
				if (jugeList [i] [0].GetComponent<TopAndBottomCardScript> ().getPoint () == cardPoint) {
					return i;
				}
			}catch (Exception e){
				return -1;
			}

		}

		return -1;
	}


	private void setPointGameObject(GameObject parent)
	{
		if (parent != null) {
			if(Pointertemp == null){
				Pointertemp = Instantiate(Resources.Load("Prefab/Pointer")) as GameObject;
			}
			Pointertemp.transform.SetParent (parent.transform);
			Pointertemp.transform.localScale = Vector3.one;
			Pointertemp.transform.localPosition = new Vector3 (0f, parent.transform.GetComponent<RectTransform> ().sizeDelta.y / 2 + 10);
		}
	}//顶针实现
	/// <summary>
	/// 自己打出来的牌
	/// </summary>
	/// <param name="obj">Object.</param>
	public void cardChange(GameObject obj)//
	{
		Debug.Log("cardChange begin");
		int handCardCount = handerCardList [0].Count -1;
		if (handCardCount == 13 || handCardCount == 10 || handCardCount == 7 || handCardCount == 4 || handCardCount == 1) {
			GlobalDataScript.isDrag = false;
			obj.GetComponent<bottomScript> ().onSendMessage -= cardChange;
			obj.GetComponent<bottomScript> ().reSetPoisiton -= cardSelect;
			MyDebug.Log("card change over");
			int putOutCardPointTemp = obj.GetComponent<bottomScript>().getPoint();//将当期打出牌的点数传出
            SelfAndOtherPutoutCard = putOutCardPointTemp;
			pushOutFromMineList(putOutCardPointTemp);                         //将牌的索引从minelist里面去掉
			handerCardList[0].Remove (obj);
			MyDebug.Log ("cardchange  goblist count = > "+handerCardList[0].Count);
			Destroy(obj);
			SetPosition (false);
			createPutOutCardAndPlayAction (putOutCardPointTemp,getMyIndexFromList());//讲拖出牌进行第一段动画的播放
			outDir = DirectionEnum.Bottom;
			//========================================================================
			CardVO cardvo = new CardVO ();
			cardvo.cardPoint = putOutCardPointTemp;
			putOutCardPointAvarIndex =getIndexByDir(getDirection(getMyIndexFromList ())) ;
			//CustomSocket.getInstance ().sendMsg (new PutOutCardRequest(cardvo));
			toNext();
            Debug.Log("cardChange finish");
           
		}

	}

	private void cardGotoTable() //动画第二段
	{
		MyDebug.Log ("==cardGotoTable=Invoke=====>");

		if (outDir == DirectionEnum.Bottom)
		{
			if (putOutCard != null)
			{
				putOutCard.transform.DOLocalMove(new Vector3(-261f+tableCardList[0].Count * 39, -133f), 0.4f);
				putOutCard.transform.DOScale(new Vector3(0.5f, 0.5f), 0.4f);
			}
		}
		else if(outDir == DirectionEnum.Right)
		{
			if (putOutCard!= null)
			{
				putOutCard.transform.DOLocalRotate(new Vector3(0, 0, 95), 0.4f);
				putOutCard.transform.DOLocalMove(new Vector3(448f, -140f+tableCardList[1].Count * 28), 0.4f);
				putOutCard.transform.DOScale(new Vector3(0.5f, 0.5f), 0.4f);
			}
		}
		else if (outDir == DirectionEnum.Top)
		{
			if (putOutCard != null)
			{
				putOutCard.transform.DOLocalMove(new Vector3(250f-tableCardList[2].Count * 39, 173f), 0.4f);
				putOutCard.transform.DOScale(new Vector3(0.5f, 0.5f), 0.4f);
			}
		}
		else if (outDir == DirectionEnum.Left)
		{
			if (putOutCard != null)
			{
				putOutCard.transform.DOLocalRotate(new Vector3(0, 0, -95), 0.4f);
				putOutCard.transform.DOLocalMove(new Vector3(-364f, 160f- tableCardList[3].Count * 28), 0.4f);
				putOutCard.transform.DOScale(new Vector3(0.5f, 0.5f), 0.4f);
			}
		}
		Invoke("destroyPutOutCard", 0.5f);
	}

	public void insertCardIntoList(GameObject item)//插入牌的方法
	{
		if(item != null){
			int curCardPoint = item.GetComponent<bottomScript>().getPoint();//得到当前牌指针
			for (int i = 0; i < handerCardList[0].Count; i++)//i<游戏物体个数 自增
			{
				int cardPoint = handerCardList[0][i].GetComponent<bottomScript>().getPoint();//得到所有牌指针
				if (cardPoint >=curCardPoint )//牌指针>=当前牌的时候插入
				{
					handerCardList[0].Insert(i, item);//在
					return;
				}
			}
			handerCardList[0].Add(item);//游戏对象列表添加当前牌
		}
		item = null;
	}

	public void SetPosition(bool flag)//设置位置
	{
		int count = handerCardList[0].Count;
		//int startX = 594 - count*79;
		int startX = 594 - count*80;
		if (flag) {
			for (int i = 0; i < count-1; i++) {
				handerCardList[0] [i].transform.localPosition = new Vector3 (startX + i * 80f, -292f); //从左到右依次对齐
			}
			handerCardList[0] [count-1].transform.localPosition = new Vector3 (580f, -292f); //从左到右依次对齐

		} else {
			for (int i = 0; i < count; i++) {
				handerCardList[0] [i].transform.localPosition = new Vector3 (startX + i * 80f -80f, -292f); //从左到右依次对齐
			}
		}
	}
	/// <summary>
	/// 销毁出的牌，并且检测是否可以碰
	/// </summary>
	private void destroyPutOutCard(int cardPoint)
	{
		ThrowBottom(cardPoint);

		if (outDir != DirectionEnum.Bottom)
		{
			gangKind = 0;
			//ai打出来的之后，让别人判断是否有碰，杠，胡的情况
			if (checkPengGangHuFromOthersPutCard())
			{
				//如果有碰，杠，胡的情况，什么都不做，等待别人点击碰，杠，胡

			}
			else
			{
				toNext();
			}
		}

	}
	
	void Update()
	{
		//Update()的刷新是按照每帧来显示的，但是Time.deltaTime是按照秒来统计的。
		timer -= Time.deltaTime;
		if (timer < 0)
		{
			timer = 0;
			//UpateTimeReStart();
		}
		Number.text = Math.Floor(timer) + "";

		if (timeFlag) {
			showTimeNumber--;
			if (showTimeNumber < 0) {
				timeFlag = false;
				showTimeNumber = 0;
				//playNoticeAction ();
			}
		}
	}

	private void playNoticeAction(){
		noticeGameObject.SetActive (true);
		if (GlobalDataScript.noticeMegs != null && GlobalDataScript.noticeMegs.Count != 0) {
			noticeText.transform.localPosition = new Vector3 (500,noticeText.transform.localPosition.y);
			noticeText.text = GlobalDataScript.noticeMegs [showNoticeNumber];
			float time = noticeText.text.Length*0.5f+422f/56f;

			Tweener tweener=noticeText.transform.DOLocalMove(
				new Vector3(-noticeText.text.Length*28, noticeText.transform.localPosition.y), time)
				.OnComplete(moveCompleted);
			tweener.SetEase (Ease.Linear);
			//tweener.SetLoops(-1);
		}
	}

	void moveCompleted(){
		showNoticeNumber++;
		if (showNoticeNumber == GlobalDataScript.noticeMegs.Count) {
			showNoticeNumber = 0;
		}
		noticeGameObject.SetActive (false);
		randShowTime ();
		timeFlag = true;
	}
	/// <summary>
	/// 重新开始计时
	/// </summary>
	void UpateTimeReStart()
	{
		timer = 16;
	}
	



    /// <summary>
    /// 
    /// </summary>
    /// <param name="path"></param>
    /// <param name="parent"></param>
    /// <param name="position"></param>
    /// <returns></returns>
    private GameObject createGameObjectAndReturn(string path,Transform parent,Vector3 position)
	{
		GameObject obj = Instantiate(Resources.Load(path)) as GameObject;
		obj.transform.SetParent (parent);
		//obj.transform.parent = parent;
		obj.transform.localScale = Vector3.one;
		obj.transform.localPosition = position;
		return obj;
	}

	/// <summary>
	/// 清理桌面
	/// </summary>
	public void clean(){
		cleanArrayList (handerCardList);
		cleanArrayList (tableCardList);
		cleanArrayList (PengGangList_L);
		cleanArrayList (PengGangCardList); 
		cleanArrayList (PengGangList_R);
		cleanArrayList (PengGangList_T);
		if (mineList != null) {
			mineList.Clear ();
		}

		if (curCard != null) {
			Destroy (curCard);
		}


		if (putOutCard != null) {
			Destroy (putOutCard);
		}

		if (pickCardItem != null) {
			Destroy (pickCardItem);
		}

		if (otherPickCardItem != null) {
			Destroy (otherPickCardItem);
		}

	}

	private void cleanArrayList(List<List<GameObject>> list){
		if (list != null) {
			while (list.Count > 0) {
				List<GameObject> tempList = list [0];
				list.RemoveAt (0);
				cleanList (tempList);
			}
		}
	}

	private void cleanList(List<GameObject> tempList){
		if (tempList != null) {
			while (tempList.Count > 0) {
				GameObject temp = tempList [0];
				tempList.RemoveAt (0);
				GameObject.Destroy (temp);
			}
		}
	}

	public void setRoomRemark(){
		RoomCreateVo roomvo = GlobalDataScript.roomVo;
		GlobalDataScript.totalTimes = roomvo.roundNumber;
		GlobalDataScript.surplusTimes = roomvo.roundNumber;
	//	LeavedRoundNumText.text = GlobalDataScript.surplusTimes + "";
		string str = "房间号：\n"+roomvo.roomId+"\n";
		str += "圈数："+roomvo.roundNumber+"\n";

		if (roomvo.roomType == 3) {
			str += "长沙麻将\n";
		} else {
			if (roomvo.hong) {
				str += "红中麻将\n";
			} else {
				if (roomvo.roomType == 1) {
					str += "转转麻将\n";
				} else if (roomvo.roomType == 2){
					str += "划水麻将\n";
				}else if (roomvo.roomType == 3){
					str += "长沙麻将\n";
				}
			}
			if (roomvo.ziMo == 1) {
				str += "只能自摸\n";
			} else {
				str += "可抢杠胡\n";
			}
			if(roomvo.sevenDouble && roomvo.roomType != GameConfig.GAME_TYPE_HUASHUI){
				str += "可胡七对\n";
			}

			if (roomvo.addWordCard) {
				str += "有风牌\n";
			}
			if (roomvo.xiaYu > 0) {
				str += "下鱼数：" + roomvo.xiaYu+"";
			}

			if (roomvo.ma > 0) {
				str += "抓码数：" + roomvo.ma+"";
			}
		}
		if (roomvo.magnification > 0) {
			str += "倍率：" + roomvo.magnification+"";
		}
		roomRemark.text = str;
	}

	private void addAvatarVOToList(AvatarVO avatar){
		if (avatarList == null) {
			avatarList = new List<AvatarVO> ();
		}
		avatarList.Add (avatar);
		setSeat (avatar);

	}

	public void createRoomAddAvatarVO(AvatarVO avatar){
		avatar.scores = 1000;
		addAvatarVOToList (avatar);
		setRoomRemark ();
		readyGame();
	
		markselfReadyGame ();
	
	}


	public void joinToRoom(List<AvatarVO> avatars){
		avatarList = avatars;
		for (int i = 0; i < avatars.Count; i++) {
			setSeat (avatars[i]);
		}
		setRoomRemark ();
		readyGame();
		markselfReadyGame ();
	}
	/// <summary>
	/// 设置当前角色的座位
	/// </summary>
	/// <param name="avatar">Avatar.</param>
	private void setSeat(AvatarVO avatar){
		//游戏结束后用的数据，勿删！！！

		//GlobalDataScript.palyerBaseInfo.Add (avatar.account.uuid, avatar.account);

		if (avatar.account.uuid == GlobalDataScript.loginResponseData.account.uuid) {
			playerItems [0].setAvatarVo (avatar);
		} else {
			int myIndex = getMyIndexFromList ();
			int curAvaIndex = avatarList.IndexOf (avatar);
			int seatIndex = curAvaIndex - myIndex;
			if (seatIndex < 0) {
				seatIndex = 4 + seatIndex;
			}
			playerItems [seatIndex].setAvatarVo (avatar);
		}

	}
	/// <summary>
	/// Gets my index from list.
	/// </summary>
	/// <returns>The my index from list.</returns>
	private int getMyIndexFromList(){
		return 0;
	}

	private int getIndex(int uuid){
		if (avatarList != null) {
			for (int i = 0; i < avatarList.Count; i++) {
				if(avatarList[i].account != null){
					if (avatarList[i].account.uuid ==uuid) {
						return i;
					}
				}
			}
		}
		return 0;
	}

	public void otherUserJointRoom(ClientResponse response){
		AvatarVO avatar = JsonMapper.ToObject<AvatarVO> (response.message);
		addAvatarVOToList (avatar);
	}


	

	private void hupaiCoinChange(string scores){
		string[] scoreList = scores.Split (new char[1]{ ',' });
		if (scoreList != null && scoreList.Length > 0) {
			for (int i = 0; i < scoreList.Length-1; i++) {
				string itemstr = scoreList [i];
				int uuid =int.Parse( itemstr.Split (new char[1]{ ':' })[0]);
				int score = int.Parse( itemstr.Split (new char[1]{ ':' })[1]) +1000;
				playerItems [getIndexByDir (getDirection (getIndex (uuid)))].scoreText.text = score + "";
				avatarList [getIndex (uuid)].scores = score;
			}
		}

	}


	private void openGameOverPanelSignal(){//单局结算
		liujuEffectGame.SetActive (false);
		setAllPlayerHuImgVisbleToFalse ();
		if (zhuamaPanel != null) {
			Destroy (zhuamaPanel.GetComponent<ZhuMaScript>());
			Destroy (zhuamaPanel);
		}

		//GlobalDataScript.singalGameOver = PrefabManage.loadPerfab("prefab/Panel_Game_Over");
		GameObject obj =  PrefabManage.loadPerfab("Prefab/Panel_Game_Over");
		//avatarList [bankerId].main = false;
		//getDirection (bankerId);
		//playerItems [curDirIndex].setbankImgEnable (false);
		//if (handerCardList != null && handerCardList.Count > 0 && handerCardList [0].Count > 0) {
		//	for (int i = 0; i < handerCardList[0].Count; i++) {
		//		handerCardList [0] [i].GetComponent<bottomScript> ().onSendMessage -= cardChange;
		//		handerCardList [0] [i].GetComponent<bottomScript> ().reSetPoisiton -= cardSelect;
		//	}
		//}

		initPanel ();
		obj.GetComponent<GameOverScript>().setDisplaContent(0,null,"",null);
		GlobalDataScript.singalGameOverList.Add (obj);
		allMas = "";//初始化码牌数据为空
		//GlobalDataScript.singalGameOver.GetComponent<GameOverScript> ().setDisplaContent (0,avatarList,allMas,GlobalDataScript.hupaiResponseVo.validMas);	
	}

	/**

	//全局结束请求回调
	private void finalGameOverCallBack(ClientResponse response){
		GlobalDataScript.finalGameEndVo = JsonMapper.ToObject<FinalGameEndVo> (response.message);
		Invoke ("finalGameOver",12);
	}

	private void finalGameOver(){

		loadPerfab ("prefab/Panel_Game_Over", 1);
		initPanel ();
		weipaiImg.transform.gameObject.SetActive(false);
		inviteFriendButton.transform.gameObject.SetActive (false);
		ExitRoomButton.transform.gameObject.SetActive (false);
		live1.transform.gameObject.SetActive (true);
		live2.transform.gameObject.SetActive (true);
		centerImage.transform.gameObject.SetActive (true);

		Destroy (GlobalDataScript.singalGameOver.GetComponent<GameOverScript> ());
		Destroy (GlobalDataScript.singalGameOver);
		exitOrDissoliveRoom ();
	}
	*/


	private void  loadPerfab(string perfabName ,int openFlag){
		GameObject obj= PrefabManage.loadPerfab (perfabName);
		obj.GetComponent<GameOverScript> ().setDisplaContent (openFlag,avatarList,allMas,GlobalDataScript.hupaiResponseVo.validMas);
	}

	private void reSetOutOnTabelCardPosition(GameObject cardOnTable){
		MyDebug.Log ("putOutCardPointAvarIndex===========:"+putOutCardPointAvarIndex);
		if (putOutCardPointAvarIndex != -1) {
			int objIndex = tableCardList [putOutCardPointAvarIndex].IndexOf (cardOnTable);
			if (objIndex != -1) {
				tableCardList [putOutCardPointAvarIndex].RemoveAt (objIndex);
				return;
			}
		}

	}

	/***
	 * 退出房间请求
	 */ 
	public void quiteRoom(){
		OutRoomRequestVo vo = new OutRoomRequestVo ();
		vo.roomId = GlobalDataScript.roomVo.roomId ;
		string sendMsg = JsonMapper.ToJson (vo);
		CustomSocket.getInstance().sendMsg(new OutRoomRequest(sendMsg));
	}

	public void outRoomCallbak(ClientResponse response){
		OutRoomResponseVo responseMsg = JsonMapper.ToObject<OutRoomResponseVo> (response.message);
		if (responseMsg.status_code == "0") {
			if (responseMsg.type == "0") {

				int uuid = responseMsg.uuid;
				if (uuid != GlobalDataScript.loginResponseData.account.uuid) {
					int index = getIndex (uuid);
					avatarList.RemoveAt (index);

					for (int i = 0; i < playerItems.Count; i++) {
						playerItems [i].setAvatarVo (null);
					}

					if (avatarList != null) {
						for (int i = 0; i < avatarList.Count; i++) {
							setSeat (avatarList[i]);
						}
						markselfReadyGame ();
					}
				} else {
					exitOrDissoliveRoom ();
				}

			} else {
				exitOrDissoliveRoom ();
			}

		} else {
			TipsManagerScript.getInstance ().setTips ("退出房间失败：" + responseMsg.error);
		}
	}


	private string dissoliveRoomType = "0";
	public void dissoliveRoomRequest(){
		if (canClickButtonFlag) {
			dissoliveRoomType = "0";
			TipsManagerScript.getInstance ().loadDialog ("申请解散房间", "你确定要申请解散房间？", doDissoliveRoomRequest, cancle);
		} else {
			TipsManagerScript.getInstance ().setTips ("还没有开始游戏，不能申请退出房间");
		}

	}

	/***
	 * 申请解散房间回调
	 */ 
	GameObject dissoDialog ;
	public void dissoliveRoomResponse( ClientResponse response){
		MyDebug.Log ("dissoliveRoomResponse" +response.message);
		DissoliveRoomResponseVo dissoliveRoomResponseVo = JsonMapper.ToObject<DissoliveRoomResponseVo> (response.message);
		string plyerName = dissoliveRoomResponseVo.accountName;
		if (dissoliveRoomResponseVo.type == "0") {
			GlobalDataScript.isonApplayExitRoomstatus = true;
			dissoliveRoomType = "1";
			dissoDialog = PrefabManage.loadPerfab ("Prefab/Panel_Apply_Exit");
			dissoDialog.GetComponent<VoteScript> ().iniUI (plyerName, avatarList);
		} else if (dissoliveRoomResponseVo.type == "3") {
			
		
			if (zhuamaPanel != null && GlobalDataScript.isonApplayExitRoomstatus ) {
				Destroy (zhuamaPanel.GetComponent<ZhuMaScript>());
				Destroy (zhuamaPanel);
			}
			GlobalDataScript.isonApplayExitRoomstatus = false;
			if (dissoDialog != null) {
				GlobalDataScript.isOverByPlayer = true;
				//dissoDialog.GetComponent<VoteScript> ().removeListener ();
				Destroy (dissoDialog.GetComponent<VoteScript> ());
				Destroy (dissoDialog);
			}
		
		}  
	}


	/**
	 * 申请或同意解散房间请求
	 * 
	 */ 
	public void  doDissoliveRoomRequest(){
		DissoliveRoomRequestVo dissoliveRoomRequestVo = new DissoliveRoomRequestVo ();
		dissoliveRoomRequestVo.roomId = GlobalDataScript.loginResponseData.roomId;
		dissoliveRoomRequestVo.type = dissoliveRoomType;
		string sendMsg = JsonMapper.ToJson (dissoliveRoomRequestVo);
		CustomSocket.getInstance().sendMsg(new DissoliveRoomRequest(sendMsg));
		GlobalDataScript.isonApplayExitRoomstatus = true;
	}

	private void cancle(){

	}

	private void cancle1(){
		dissoliveRoomType = "2";
		doDissoliveRoomRequest ();
	}

	public void exitOrDissoliveRoom(){
		GlobalDataScript.loginResponseData.resetData ();//复位房间数据
		GlobalDataScript.loginResponseData.roomId = 0;//复位房间数据
		GlobalDataScript.roomVo.roomId = 0;
		GlobalDataScript.soundToggle = true;
		clean ();
		//removeListener ();

		SoundCtrl.getInstance ().playBGM ();
		if (GlobalDataScript.homePanel != null) {
			GlobalDataScript.homePanel.SetActive (true);
			GlobalDataScript.homePanel.transform.SetSiblingIndex (1);
		} else {
			GlobalDataScript.homePanel=PrefabManage.loadPerfab ("Prefab/Panel_Home");
			GlobalDataScript.homePanel.transform.SetSiblingIndex (1);
		}

		while(playerItems.Count >0){
			PlayerItemScript item = playerItems [0];
			playerItems.RemoveAt (0);
			item.clean ();
			Destroy (item.gameObject);
			Destroy (item);
		}
		Destroy (this);
		Destroy (gameObject);
	}

	public void gameReadyNotice(ClientResponse response){

		//===============================================
		JsonData json = JsonMapper.ToObject(response.message);
		int avatarIndex = Int32.Parse(json["avatarIndex"].ToString());
		int myIndex = getMyIndexFromList ();
		int seatIndex = avatarIndex - myIndex;
		if (seatIndex < 0) {
			seatIndex = 4 + seatIndex;
		}
		playerItems [seatIndex].readyImg.enabled = true;
		avatarList [avatarIndex].isReady = true;
	}


	private void gameFollowBanderNotice(ClientResponse response){
		genZhuang.SetActive (true);
		Invoke ("hideGenzhuang", 2f);
	}
	private void hideGenzhuang(){
		genZhuang.SetActive (false);
	}

	/*************************断线重连*********************************/
	private void reEnterRoom(){
		
		if (GlobalDataScript.reEnterRoomData != null) {
			//显示房间基本信息
			GlobalDataScript.roomVo.addWordCard = GlobalDataScript.reEnterRoomData.addWordCard;
			GlobalDataScript.roomVo.hong = GlobalDataScript.reEnterRoomData.hong;
			GlobalDataScript.roomVo.name = GlobalDataScript.reEnterRoomData.name;
			GlobalDataScript.roomVo.roomId = GlobalDataScript.reEnterRoomData.roomId;
			GlobalDataScript.roomVo.roomType = GlobalDataScript.reEnterRoomData.roomType;
			GlobalDataScript.roomVo.roundNumber = GlobalDataScript.reEnterRoomData.roundNumber;
			GlobalDataScript.roomVo.sevenDouble = GlobalDataScript.reEnterRoomData.sevenDouble;
			GlobalDataScript.roomVo.xiaYu = GlobalDataScript.reEnterRoomData.xiaYu;
			GlobalDataScript.roomVo.ziMo = GlobalDataScript.reEnterRoomData.ziMo;
			GlobalDataScript.roomVo.magnification = GlobalDataScript.reEnterRoomData.magnification;
			GlobalDataScript.roomVo.ma = GlobalDataScript.reEnterRoomData.ma;
			setRoomRemark();
			//设置座位

			avatarList = GlobalDataScript.reEnterRoomData.playerList;
			GlobalDataScript.roomAvatarVoList = GlobalDataScript.reEnterRoomData.playerList;
			for (int i = 0; i < avatarList.Count; i++) {
				setSeat (avatarList [i]);
			}

			recoverOtherGlobalData ();
			int[][] selfPaiArray = GlobalDataScript.reEnterRoomData.playerList [getMyIndexFromList ()].paiArray;
			if (selfPaiArray == null || selfPaiArray.Length == 0) {//游戏还没有开始
			  

			} else {//牌局已开始
				setAllPlayerReadImgVisbleToFalse ();
				cleanGameplayUI ();
				//显示打牌数据
				displayTableCards ();
				//显示碰牌
				displayOtherHandercard();//显示其他玩家的手牌
				displayallGangCard();//显示杠牌
				displayPengCard();//显示碰牌
				dispalySelfhanderCard();//显示自己的手牌
				CustomSocket.getInstance ().sendMsg (new CurrentStatusRequest ());
			}



		}

	}




	//恢复其他全局数据
	private void recoverOtherGlobalData (){
		int selfIndex = getMyIndexFromList ();
		GlobalDataScript.loginResponseData.account.roomcard = GlobalDataScript.reEnterRoomData.playerList [selfIndex].account.roomcard;//恢复房卡数据，此时主界面还没有load所以无需操作界面显示

	}




	private void dispalySelfhanderCard(){
		mineList =ToList( GlobalDataScript.reEnterRoomData.playerList [getMyIndexFromList()].paiArray);
		for (int i = 0; i < mineList [0].Count; i++) {
			if (mineList [0] [i] > 0) {
				for (int j = 0; j < mineList [0] [i]; j++) {
					GameObject gob = Instantiate(Resources.Load("prefab/card/Bottom_B")) as GameObject;
					//GameObject.Instantiate ("");

					if (gob != null)//
					{
						gob.transform.SetParent(parentList[0]);//设置父节点
						gob.transform.localScale =new Vector3(1.1f,1.1f,1);
						gob.GetComponent<bottomScript>().onSendMessage += cardChange;//发送消息fd
						gob.GetComponent<bottomScript>().reSetPoisiton += cardSelect;
						gob.GetComponent<bottomScript>().setPoint(i);//设置指针                                                                                         
						handerCardList[0].Add(gob);//增加游戏对象
					}
				}

			}
		}
		SetPosition(false);
	}

	private List<List<int>> ToList(int [][] param){
		List<List<int>> returnData = new List<List<int>> ();
		for(int i= 0;i<param.Length;i++){
			List<int> temp = new List<int> ();
			for (int j = 0; j < param [i].Length; j++) {
				temp.Add (param [i] [j]);
			}
			returnData.Add (temp);
		}
		return returnData;
	}

	public void myselfSoundActionPlay(){
		playerItems [0].showChatAction ();
	}


	/**显示打牌数据在桌面**/
	private void displayTableCards(){
		//List<int[]> chupaiList = new List<int[]> ();
		for (int i = 0; i < GlobalDataScript.reEnterRoomData.playerList.Count; i++) {
			int[] chupai = GlobalDataScript.reEnterRoomData.playerList [i].chupais;
			outDir = getDirection (getIndex (GlobalDataScript.reEnterRoomData.playerList [i].account.uuid));
			if (chupai != null && chupai.Length > 0) {
				for (int j = 0; j < chupai.Length; j++) {
					ThrowBottom (chupai[j]);
				}
			}

		}
	}

	/**显示其他人的手牌**/
	private void displayOtherHandercard(){
		for(int i=0 ;i<GlobalDataScript.reEnterRoomData.playerList.Count ;i++){
			string dir = getDirection (getIndex (GlobalDataScript.reEnterRoomData.playerList [i].account.uuid));
			int count = GlobalDataScript.reEnterRoomData.playerList[i].commonCards;
			if (dir != DirectionEnum.Bottom) {
				initOtherCardList (dir,count);
			}

		}
	}

	/**显示杠牌**/
	private void displayallGangCard(){
		for (int i = 0; i < GlobalDataScript.reEnterRoomData.playerList.Count; i++) {
			int[] paiArrayType = GlobalDataScript.reEnterRoomData.playerList [i].paiArray[1];
			string dirstr =  getDirection (getIndex (GlobalDataScript.reEnterRoomData.playerList [i].account.uuid));
			if (paiArrayType.Contains<int> (2)) {
				string gangString = GlobalDataScript.reEnterRoomData.playerList [i].huReturnObjectVO.totalInfo.gang;
				if (gangString != null) {
					string[] gangtemps = gangString.Split (new char[1]{','});
					for (int j = 0; j < gangtemps.Length; j++) {
						string item = gangtemps [j];
						GangpaiObj gangpaiObj = new GangpaiObj ();
						gangpaiObj.uuid  =item.Split (new char[1]{':'})[0];
						gangpaiObj.cardPiont =int.Parse( item.Split (new char[1]{':'})[1]);
						gangpaiObj.type = item.Split (new char[1]{':'})[2];
						//增加判断是否为自己的杠牌的操作
						GlobalDataScript.reEnterRoomData.playerList [i].paiArray [0] [gangpaiObj.cardPiont] -= 4;


						if (gangpaiObj.type == "an") {
							doDisplayPengGangCard (dirstr, gangpaiObj.cardPiont,4,1);

						} else {
							doDisplayPengGangCard (dirstr, gangpaiObj.cardPiont,4,0);

						}
					}
				}
			}

		}
	}

	private void displayPengCard(){
		for (int i = 0; i < GlobalDataScript.reEnterRoomData.playerList.Count; i++) {
			int[] paiArrayType = GlobalDataScript.reEnterRoomData.playerList [i].paiArray[1];
			string dirstr =  getDirection (getIndex (GlobalDataScript.reEnterRoomData.playerList [i].account.uuid));
			if (paiArrayType.Contains<int> (1)) {
				for (int j = 0; j < paiArrayType.Length; j++) {
					if (paiArrayType [j] == 1 && GlobalDataScript.reEnterRoomData.playerList [i].paiArray [0] [j]>0 ) {
						GlobalDataScript.reEnterRoomData.playerList [i].paiArray [0] [j] -= 3;
						doDisplayPengGangCard (dirstr, j,3,2);

					}
				}
			}
		}
	}


	/**
	 * 显示杠碰牌
	 * cloneCount 代表clone的次数  若为3则表示碰   若为4则表示杠
	 */ 
	private void doDisplayPengGangCard(string dirstr,int point,int cloneCount,int flag){
		List<GameObject> gangTempList ;
		switch (dirstr) {
		case DirectionEnum.Bottom:
			gangTempList = new List<GameObject> ();
			for (int i = 0; i <cloneCount; i++) {
				GameObject obj;
				if (i < 3) {
					if (flag !=1) {
						obj = createGameObjectAndReturn ("Prefab/PengGangCard/PengGangCard_B",
							pengGangParenTransformB.transform, new Vector3 (-370f + PengGangCardList.Count * 190f + i * 60f, 0));
						obj.GetComponent<TopAndBottomCardScript> ().setPoint (point);
						obj.transform.localScale = Vector3.one;
					} else{
						obj = createGameObjectAndReturn ("Prefab/PengGangCard/gangBack",
							pengGangParenTransformB.transform, new Vector3 (-370f + PengGangCardList.Count * 190f + i * 60f, 0));
						obj.transform.localScale = Vector3.one;
					}
				}else{
					obj= createGameObjectAndReturn ("Prefab/PengGangCard/PengGangCard_B",
						pengGangParenTransformB.transform, new Vector3 (-370f + PengGangCardList.Count * 190f + i * 60f, 0));
					obj.GetComponent<TopAndBottomCardScript> ().setPoint (point);
					obj.transform.localPosition = new Vector3 (-310f + PengGangCardList.Count * 190f, 24f);
				}


				gangTempList.Add (obj);
			}
			PengGangCardList.Add (gangTempList);
			break;
		case DirectionEnum.Top:
			gangTempList = new List<GameObject> ();
			for (int i = 0; i < cloneCount; i++) {
				GameObject obj;
				if (i < 3) {
					if (flag  !=1) {
						obj = createGameObjectAndReturn ("Prefab/PengGangCard/PengGangCard_T",
							pengGangParenTransformT.transform, new Vector3 (-370f + PengGangList_T.Count * 190f + i * 60f, 0));
						obj.transform.parent = pengGangParenTransformT.transform;
						obj.GetComponent<TopAndBottomCardScript> ().setPoint (point);
					} else{
						obj = createGameObjectAndReturn ("Prefab/PengGangCard/GangBack_T",
							pengGangParenTransformT.transform, new Vector3 (-370f + PengGangCardList.Count * 190f + i * 60f, 0));
						obj.transform.localScale = Vector3.one;
					}
					obj.transform.localPosition = new Vector3 (251 - PengGangList_T.Count * 120f + i * 37, 0f);
				}else{
					obj = createGameObjectAndReturn ("Prefab/PengGangCard/PengGangCard_T",
						pengGangParenTransformT.transform, new Vector3 (-370f + PengGangList_T.Count * 190f + i * 60f, 0));
					
					obj.GetComponent<TopAndBottomCardScript> ().setPoint (point);
					obj.transform.localPosition  = new Vector3 (251 - PengGangList_T.Count * 120f + 37f, 20f);

				} 
				gangTempList.Add (obj);
			}
			PengGangList_T.Add (gangTempList);
			break;
		case DirectionEnum.Left:
			gangTempList = new List<GameObject> ();
			for (int i = 0; i < cloneCount; i++) {
				GameObject obj;
				if (i < 3) {
					if (flag  !=1) {
						obj = createGameObjectAndReturn ("Prefab/PengGangCard/PengGangCard_L",
							pengGangParenTransformL.transform, new Vector3 (-370f + PengGangList_L.Count * 190f + i * 60f, 0));
						obj.transform.parent = pengGangParenTransformL.transform;
						obj.GetComponent<TopAndBottomCardScript> ().setLefAndRightPoint (point);
					} else{
						obj = createGameObjectAndReturn ("Prefab/PengGangCard/GangBack_L&R",
							pengGangParenTransformL.transform, new Vector3 (-370f + PengGangList_L.Count * 190f + i * 60f, 0));
					}
					obj.transform.localPosition = new Vector3 (0f, 122 - PengGangList_L.Count * 95f - i * 28f);
				}else{
					obj = createGameObjectAndReturn ("Prefab/PengGangCard/PengGangCard_L",
						pengGangParenTransformL.transform, new Vector3 (-370f + PengGangList_L.Count * 190f + i * 60f, 0));
					obj.GetComponent<TopAndBottomCardScript> ().setLefAndRightPoint (point);
					obj.transform.localPosition = new Vector3 (0f, 122 - PengGangList_L.Count * 95f - 18f);

				}


				gangTempList.Add (obj);
			}
			PengGangList_L.Add (gangTempList);
			break;
		case DirectionEnum.Right:
			gangTempList = new List<GameObject> ();
			for (int i = 0; i < cloneCount; i++) {
				GameObject obj;
				if (i < 3) {
					if (flag != 1) {
						obj = createGameObjectAndReturn ("Prefab/PengGangCard/PengGangCard_R",
							pengGangParenTransformR.transform, new Vector3 (-370f + PengGangList_R.Count * 190f + i * 60f, 0));
						obj.GetComponent<TopAndBottomCardScript> ().setLefAndRightPoint (point);
						obj.transform.parent = pengGangParenTransformR.transform;
					} else {
						obj = createGameObjectAndReturn ("Prefab/PengGangCard/GangBack_L&R",
							pengGangParenTransformR.transform, new Vector3 (-370f + PengGangList_R.Count * 190f + i * 60f, 0));
					}
					obj.transform.localPosition  = new Vector3 (0, -122 + PengGangList_R.Count * 95 + i * 28f);

					obj.transform.SetSiblingIndex (0);
				} else{
					obj = createGameObjectAndReturn ("Prefab/PengGangCard/PengGangCard_R",
						pengGangParenTransformR.transform, new Vector3 (-370f + PengGangList_R.Count * 190f + i * 60f, 0));
					obj.GetComponent<TopAndBottomCardScript> ().setLefAndRightPoint (point);
					obj.transform.localPosition  = new Vector3 (0f, -122 + PengGangList_R.Count * 95 + 33f);
				}

				gangTempList.Add (obj);
			}
			PengGangList_R.Add (gangTempList);
			break;
		}
	}

	public void inviteFriend(){
		//GlobalDataScript.getInstance ().wechatOperate.inviteFriend ();
	}



	/**用户离线回调**/
	public void offlineNotice(ClientResponse response){
		int uuid =int.Parse( response.message);
		int index = getIndex (uuid);
		string dirstr = getDirection (index);
		switch (dirstr) {
		case DirectionEnum.Bottom:
			playerItems [0].GetComponent<PlayerItemScript> ().setPlayerOffline ();
			break;
		case DirectionEnum.Right:
			playerItems [1].GetComponent<PlayerItemScript> ().setPlayerOffline ();
			break;
		case DirectionEnum.Top:
			playerItems [2].GetComponent<PlayerItemScript> ().setPlayerOffline ();
			break;
		case DirectionEnum.Left:
			playerItems [3].GetComponent<PlayerItemScript> ().setPlayerOffline ();
			break;
		

		}

		//申请解散房间过程中，有人掉线，直接不能解散房间
		if (GlobalDataScript.isonApplayExitRoomstatus) {
			if (dissoDialog != null) {
				//dissoDialog.GetComponent<VoteScript> ().removeListener ();
				Destroy (dissoDialog.GetComponent<VoteScript> ());
				Destroy (dissoDialog);
			}
			TipsManagerScript.getInstance ().setTips ("由于" + avatarList [index].account.nickname + "离线，系统不能解散房间。");

		}
	}

	/**用户上线提醒**/
	public void onlineNotice(ClientResponse  response){
		int uuid =int.Parse( response.message);
		int index = getIndex (uuid);
		string dirstr = getDirection (index);
		switch (dirstr) {
		case DirectionEnum.Bottom:
			playerItems [0].GetComponent<PlayerItemScript> ().setPlayerOnline ();
			break;
		case DirectionEnum.Right:
			playerItems [1].GetComponent<PlayerItemScript> ().setPlayerOnline ();
			break;
		case DirectionEnum.Top:
			playerItems [2].GetComponent<PlayerItemScript> ().setPlayerOnline ();
			break;
		case DirectionEnum.Left:
			playerItems [3].GetComponent<PlayerItemScript> ().setPlayerOnline ();
			break;

		}
	}


	public void messageBoxNotice(ClientResponse response){
		string[] arr = response.message.Split (new char[1]{ '|' });
		int uuid = int.Parse(arr[1]);
		int myIndex = getMyIndexFromList ();
		int curAvaIndex = getIndex (uuid);
		int seatIndex = curAvaIndex - myIndex;
		if (seatIndex < 0) {
			seatIndex = 4 + seatIndex;
		}
		playerItems [seatIndex].showChatMessage (int.Parse(arr[0]));
	}


	/*显示自己准备*/
	private void markselfReadyGame(){
		playerItems [0].readyImg.transform.gameObject.SetActive (true);
	}

	/**
    *准备游戏
	*/
	public void  readyGame(){
		CustomSocket.getInstance ().sendMsg (new GameReadyRequest ());
	}

	public void micInputNotice(ClientResponse response){
		int sendUUid = int.Parse(response.message);
		if (sendUUid > 0) {
			for (int i = 0; i < playerItems.Count; i++) {
				if (playerItems [i].getUuid() != -1) {
					if (sendUUid == playerItems [i].getUuid ()) {
						playerItems [i].showChatAction ();
					}
				}
			}
		}
	}


	//public void returnGameResponse(ClientResponse response){
	//	string returnstr = response.message;
	//	JsonData returnJsonData = new JsonData (returnstr);
	//	//1.显示剩余牌的张数和圈数
	//	JsonData returnJsonData = JsonMapper.ToObject(response.message);
	//	string surplusCards = returnJsonData["surplusCards"].ToString();
	//	LeavedCastNumText.text = surplusCards;
	//	LeavedCardsNum = int.Parse(surplusCards);
	//	int gameRound =int.Parse( returnJsonData ["gameRound"].ToString ());
	//	LeavedRoundNumText.text =gameRound+ "";
	//	GlobalDataScript.surplusTimes = gameRound;


	//	int curAvatarIndexTemp = -1;//当前出牌人的索引
	//	int pickAvatarIndexTemp = -1; //当前摸牌人的索引
	//	int putOffCardPointTemp = -1;//当前打得点数
	//	int currentCardPointTemp = -1;//当前摸的点数


	//	//不是自己摸牌
	//	try{

	//		curAvatarIndexTemp =int.Parse( returnJsonData["curAvatarIndex"].ToString());//当前打牌人的索引
	//		putOffCardPointTemp =int.Parse( returnJsonData["putOffCardPoint"].ToString());//当前打得点数

	//		putOutCardPointAvarIndex =getIndexByDir(getDirection(curAvatarIndexTemp));

	//		putOutCardPoint = putOffCardPointTemp;//碰
	//		//useForGangOrPengOrChi = putOutCardPoint;//杠
	//	//	selfGangCardPoint = useForGangOrPengOrChi;
	//		SelfAndOtherPutoutCard = putOutCardPoint;
	//		pickAvatarIndexTemp =int.Parse( returnJsonData["pickAvatarIndex"].ToString()); //当前摸牌牌人的索引

	//		/**这句代码有可能引发catch  所以后面的 SelfAndOtherPutoutCard = currentCardPointTemp; 可能不执行**/
	//		currentCardPointTemp =int.Parse( returnJsonData["currentCardPoint"].ToString());//当前摸得的点数  
	//		SelfAndOtherPutoutCard = currentCardPointTemp; 


	//	}catch( Exception ex){

	//	}


	//	if (pickAvatarIndexTemp == getMyIndexFromList()) {//自己摸牌
	//		if (currentCardPointTemp == -2) {
	//			MoPaiCardPoint = handerCardList[0][handerCardList[0].Count-1].GetComponent<bottomScript>().getPoint();
	//			SelfAndOtherPutoutCard = MoPaiCardPoint; 
	//			useForGangOrPengOrChi = curAvatarIndexTemp;
	//			Destroy(handerCardList[0][handerCardList[0].Count-1]);
	//			handerCardList[0].Remove(handerCardList[0][handerCardList[0].Count-1]);
	//			SetPosition (false);
	//			putCardIntoMineList (MoPaiCardPoint);
	//			moPai ();
	//			curDirString = DirectionEnum.Bottom;
	//			SetDirGameObjectAction ();
	//			GlobalDataScript.isDrag = true;	
	//			MyDebug.Log ("自己摸牌");

	//		} else {
	//			if ((handerCardList [0].Count) % 3 != 1) {
	//				MoPaiCardPoint = currentCardPointTemp;
	//				MyDebug.Log ("摸牌" + MoPaiCardPoint);
	//				SelfAndOtherPutoutCard = MoPaiCardPoint; 
	//				useForGangOrPengOrChi = curAvatarIndexTemp;
	//				for(int i=0;i<handerCardList[0].Count;i++){
	//					if(handerCardList[0][i].GetComponent<bottomScript>().getPoint()== currentCardPointTemp){
	//						Destroy(handerCardList[0][i]);
	//						handerCardList[0].Remove(handerCardList[0][i]);
	//						break;
	//					}
	//				}
	//				SetPosition (false);
	//				putCardIntoMineList (MoPaiCardPoint);
	//				moPai ();
	//				curDirString = DirectionEnum.Bottom;
	//				SetDirGameObjectAction ();
	//				GlobalDataScript.isDrag = true;	
	//			}

	//		}

	//	} else { //别人摸牌
	//		curDirString = getDirection(pickAvatarIndexTemp);
	//		//	otherMoPaiCreateGameObject (curDirString);
	//		SetDirGameObjectAction ();
	//	}





	//	//光标指向打牌人
	//	int dirindex = getIndexByDir (getDirection(curAvatarIndexTemp) );
	//	cardOnTable = tableCardList [dirindex] [tableCardList [dirindex].Count - 1];
	//	if (tableCardList [dirindex] == null || tableCardList [dirindex].Count == 0) {//刚启动


	//		/**
	//		if (pickAvatarIndexTemp == getMyIndexFromList ()) {
	//			int cardPoint = handerCardList [0] [handerCardList [0].Count - 1].GetComponent<bottomScript> ().getPoint ();

	//			Destroy (handerCardList [0] [handerCardList [0].Count - 1]);
	//			handerCardList [0].RemoveAt (handerCardList [0].Count - 1);

	//			currentCardPointTemp = cardPoint;
	//			MoPaiCardPoint = currentCardPointTemp;
	//			MyDebug.Log ("摸牌" + MoPaiCardPoint);
	//			SelfAndOtherPutoutCard = MoPaiCardPoint; 
	//			useForGangOrPengOrChi = curAvatarIndexTemp;
	//			putCardIntoMineList (MoPaiCardPoint);
	//			moPai ();
	//			curDirString = DirectionEnum.Bottom;
	//			SetDirGameObjectAction ();
	//			GlobalDataScript.isDrag = true;	
	//		} else {//别人摸牌
	//			curDirString = getDirection (pickAvatarIndexTemp);
	//			SetDirGameObjectAction ();

	//		}
	//		*/
	//	} else {
	//		//otherPickCardItem = handerCardList[dirindex][0];
	//	//	gameTool.setOtherCardObjPosition(handerCardList[dirindex],getDirection(curAvatarIndexTemp) , 1);
	//		GameObject temp = tableCardList[dirindex][tableCardList[dirindex].Count-1]; 
	//		setPointGameObject (temp);
	//	}


	//}

    //当自己打完牌之后轮到下家摸牌和打牌
	public void toNext()
	{
        if (allList.Count == 0){
			Invoke("openGameOverPanelSignal", 3);
            //做流局结算
            return;
        } 
        //检测碰，杠，胡
        LeavedCastNumText.text = allList.Count.ToString();
        putOutCardPointAvarIndex = getIndexByDir(getDirection(curDirIndex));
        //改变下一家打牌的索引
		curDirIndex++;
		if (curDirIndex >= 4)
		{
			curDirIndex = 0;
		}
		
		if (curDirIndex == getMyIndexFromList())
		{//自己摸牌
			MoPaiCardPoint =allList[0] / 4;
            allList.RemoveAt(0);
            LeavedCastNumText.text = allList.Count.ToString();
			SelfAndOtherPutoutCard = MoPaiCardPoint;
			useForGangOrPengOrChi = MoPaiCardPoint;
			SetPosition(false);
			moPai();
			curDirString = DirectionEnum.Bottom;
			SetDirGameObjectAction();
			GlobalDataScript.isDrag = true;
            checkGangHuFromSelf();
		}
		else
		{ //别人摸牌
			//
            otherPickCard();
            coroutine=StartCoroutine(putCard());
           
		}

		//光标指向打牌人
		int dirindex = getIndexByDir(getDirection(curDirIndex));
		//cardOnTable = tableCardList[dirindex][tableCardList[dirindex].Count - 1];
		if (tableCardList[dirindex] == null || tableCardList[dirindex].Count == 0)
		{
		}
		else
		{
			//otherPickCardItem = handerCardList[dirindex][0];
			//  gameTool.setOtherCardObjPosition(handerCardList[dirindex],getDirection(curAvatarIndexTemp) , 1);
			GameObject temp = tableCardList[dirindex][tableCardList[dirindex].Count - 1];
			setPointGameObject(temp);
		}


	}

    //麻将算法数据
    private void faPai(){
        allList.Clear();
		int N = 9 * 3 * 4;
		var list = new List<int>();
		for (var i = 0; i < N; i++)
		{
			list.Add(i);
		}
		System.Random random = new System.Random();
		for (int i = 0; i < N; i++)
		{
			int x = random.Next(N - i);
			allList.Add(list[x]);
			list.RemoveAt(x);
		}
    }

    private void addList(List<List<int>> list,int count){
		var list1 = new List<int>();
		for (int i = 0; i < count; i++)
		{
		    list1.Add(allList[0] / 4);
			allList.RemoveAt(0);
		}
		list1.Sort();
		list.Add(list1);
    }
	Coroutine coroutine;
    //每一个ai的打牌时间都限定为2s
    //打牌之后判定碰杠胡的情况（ai暂时自己设定 摸牌杠，胡的情况）
	IEnumerator putCard()
	{
		yield return new WaitForSeconds(2);// 
		otherPutOutCard();
	}
	/// <summary>
    /// 检查，碰，杠，胡(别人打得牌)
	/// </summary>
	/// <returns><c>true</c>, if peng gang hu was checked, <c>false</c> otherwise.</returns>
	private bool checkPengGangHuFromOthersPutCard(){
        Debug.Log("checkPengGangHuFromOthersPutCard");
        bool result = false;
        int count = 0;
        for (int i = 0; i < handerCardList[0].Count;i++){
			GameObject temp = handerCardList[0][i];
			int tempCardPoint = temp.GetComponent<bottomScript>().getPoint();
            if(tempCardPoint==putOutCardPoint){
                count++;
            }
        }
        if (count == 2)
        {
            btnActionScript.showBtn(GameConfig.PENG);
            result = true;
		}
        if(count==3){
            //别人打出来的牌为明杠
            gangKind = 0;
            btnActionScript.showBtn(GameConfig.PENG);
            btnActionScript.showBtn(GameConfig.GANG);
            result = true;
           
		}
        if(checkHuPai(putOutCardPoint)){
            btnActionScript.showBtn(GameConfig.HU);
			result = true;
        }
        return result;
    }
    /// <summary>
    /// 检查自摸杠，或者自摸胡牌
    /// </summary>
    private void checkGangHuFromSelf(){
        Debug.Log("checkGangHuFromSelf");
		int count = 0;
		for (int i = 0; i < handerCardList[0].Count; i++)
		{
			GameObject temp = handerCardList[0][i];
			int tempCardPoint = temp.GetComponent<bottomScript>().getPoint();
			if (tempCardPoint == MoPaiCardPoint)
			{
				count++;
			}
		}
		if (count == 4)
		{
            //自己摸的牌为暗杠
            gangKind = 1;
            btnActionScript.showBtn(GameConfig.GANG);
		}
        if (checkHuPai())
		{
            btnActionScript.showBtn(GameConfig.HU);	
		} 
    }
	/// <summary>
	/// 检查是否胡牌
	/// </summary>
	/// <returns><c>true</c>, if hu pai was checked, <c>false</c> otherwise.</returns>
	/// <param name="card">Card.</param>
	private bool checkHuPai(int card){
		Debug.Log("checkHuPai");
        List<int> list = new List<int>();
		for (int i = 0; i < handerCardList[0].Count; i++)
		{
			GameObject temp = handerCardList[0][i];
			int tempCardPoint = temp.GetComponent<bottomScript>().getPoint();
            list.Add(tempCardPoint);
		}
		list.Add(card);
        list.Sort();
        return huPai(list);
    }
	private bool checkHuPai()
	{
		Debug.Log("checkHuPai");
		List<int> list = new List<int>();
		for (int i = 0; i < handerCardList[0].Count; i++)
		{
			GameObject temp = handerCardList[0][i];
			int tempCardPoint = temp.GetComponent<bottomScript>().getPoint();
			list.Add(tempCardPoint);
		}
		list.Sort();
		return huPai(list);
	}
	private static bool huPai(List<int> list)
	{
		list.Sort();
		bool result = false;
		int currentMenshu = HuUtil.checkMeShu(list);
		if (currentMenshu < 3)
		{
            if (HuUtil.IsQiDui(list))
			{
				result = true;
			}
			if (HuUtil.IsHuPengPeng(list))
			{
				result = true;
			}

			if (HuUtil.isNomalHuPai(list))
			{
				result = true;
			}

		}
		return result;

	}
    /// <summary>
    /// 碰的相关操作
    /// </summary>
    private  void  peng(){
		SoundCtrl.getInstance().playSoundByAction("peng", 0);
		if (cardOnTable != null)
		{
			reSetOutOnTabelCardPosition(cardOnTable);
			Destroy(cardOnTable);
		}
			int removeCount = 0;
			for (int i = 0; i < handerCardList[0].Count; i++)
			{
				GameObject temp = handerCardList[0][i];
				int tempCardPoint = temp.GetComponent<bottomScript>().getPoint();
				if (tempCardPoint == putOutCardPoint)
				{
					handerCardList[0].RemoveAt(i);
					Destroy(temp);
					i--;
					removeCount++;
					if (removeCount == 2)
					{
						break;
					}
				}
			}
			SetPosition(true);
			bottomPeng();
            curDirIndex = 0;
		}


	/// <summary>
	/// 点击“过”按钮
	/// </summary>
	public void myPassBtnClick()
	{
		Debug.Log("pass");
		
		btnActionScript.cleanBtnShow();
		if (passType == "selfPickCard")
		{
			GlobalDataScript.isDrag = true;
		}
		passType = "";
        //点击过的情况
		toNext();

	}
	/// <summary>
	/// 点击“碰”按钮
	/// </summary>
	public void myPengBtnClick()
	{
        //StopCoroutine(coroutine);
		Debug.Log("peng");
		GlobalDataScript.isDrag = true;
		UpateTimeReStart();
		peng();
		btnActionScript.cleanBtnShow();
	}


	/// <summary>
	/// 点击“胡”按钮
	/// </summary>
	public void myHupaiBtnClick()
	{
		SoundCtrl.getInstance().playSoundByAction("hu", 0);
		effectType = "hu";
		pengGangHuEffectCtrl();
		SoundCtrl.getInstance().playSoundByAction("hu", 0);
		Invoke("openGameOverPanelSignal", 3);
	}

	public void myGangBtnClick()
	{
		//useForGangOrPengOrChi = int.Parse (gangPaiList [0]);
		GlobalDataScript.isDrag = true;
		selfGangCardPoint = putOutCardPoint;
		SoundCtrl.getInstance().playSoundByAction("gang", 0);
		btnActionScript.cleanBtnShow();
		effectType = "gang";
		pengGangHuEffectCtrl();
		gang();
		return;
	}

	/// <summary>
	/// Gang 操作
	/// </summary>
	public void gang()
	{
		UpateTimeReStart();
		//gangKind = gangBackVo.type;
		//int Num = 0;
		//bool pengOrNot = false;
		//checkHuOrGangOrPengOrChi (MoPaiCardPoint,2);
		// GlobalDataScript.isDrag = true;


		/*创建一个摸的牌***/
		/**
		SelfAndOtherPutoutCard = gangBackVo.cardList[0]; 
		//useForGangOrPengOrChi = gangBackVo.cardList[0];
		putCardIntoMineList (gangBackVo.cardList[0]);
		moPai ();
		curDirString = DirectionEnum.Bottom;
		SetDirGameObjectAction ();
		CardsNumChange();
		**/


		if (gangKind == 0)
		{   //明杠
			//mineList[1][selfGangCardPoint] = 3;
			/**杠牌点数**/
			//int gangpaiPonitTemp = gangBackVo.cardList [0];
			if (getPaiInpeng(selfGangCardPoint, DirectionEnum.Bottom) == -1)
			{//杠牌不在碰牌数组以内，一定为别人打得牌

				//销毁别人打的牌
				if (putOutCard != null)
				{
					Destroy(putOutCard);
				}
				if (cardOnTable != null)
				{
					reSetOutOnTabelCardPosition(cardOnTable);
					Destroy(cardOnTable);

				}

				//销毁手牌中的三张牌
				int removeCount = 0;
				for (int i = 0; i < handerCardList[0].Count; i++)
				{
					GameObject temp = handerCardList[0][i];
					int tempCardPoint = handerCardList[0][i].GetComponent<bottomScript>().getPoint();
					if (selfGangCardPoint == tempCardPoint)
					{
						handerCardList[0].RemoveAt(i);
						Destroy(temp);
						i--;
						removeCount++;
						if (removeCount == 3)
						{
							break;
						}
					}
				}

				//创建杠牌序列

				List<GameObject> gangTempList = new List<GameObject>();
				for (int i = 0; i < 4; i++)
				{
					GameObject obj = createGameObjectAndReturn("Prefab/PengGangCard/PengGangCard_B",
						pengGangParenTransformB.transform, new Vector3(-370f + PengGangCardList.Count * 190f + i * 60f, 0));
					obj.GetComponent<TopAndBottomCardScript>().setPoint(selfGangCardPoint);
					obj.transform.localScale = Vector3.one;
					if (i == 3)
					{

						obj.transform.localPosition = new Vector3(-310f + PengGangCardList.Count * 190f, 24f);
					}
					gangTempList.Add(obj);
				}

				//添加到杠牌数组里面
				PengGangCardList.Add(gangTempList);

			}
			else
			{//在碰牌数组以内，则一定是自摸的牌

				for (int i = 0; i < handerCardList[0].Count; i++)
				{
					if (handerCardList[0][i].GetComponent<bottomScript>().getPoint() == selfGangCardPoint)
					{
						GameObject temp = handerCardList[0][i];
						handerCardList[0].RemoveAt(i);
						Destroy(temp);
						break;
					}

				}

				int index = getPaiInpeng(selfGangCardPoint, DirectionEnum.Bottom);
				//将杠牌放到对应位置
				GameObject obj = createGameObjectAndReturn("Prefab/PengGangCard/PengGangCard_B",
					pengGangParenTransformB.transform, new Vector3(-370f + PengGangCardList.Count * 190f + 0 * 60f, 0));
				obj.GetComponent<TopAndBottomCardScript>().setPoint(selfGangCardPoint);
				obj.transform.localScale = Vector3.one;
				obj.transform.localPosition = new Vector3(-310f + index * 190f, 24f);
				PengGangCardList[index].Add(obj);

			}
			//MoPaiCardPoint = gangBackVo.cardList [0];
			//putCardIntoMineList (gangBackVo.cardList [0]);


		}
		else if (gangKind == 1)
		{ //===================================================================================暗杠

			mineList[1][selfGangCardPoint] = 4;
			int removeCount = 0;

			for (int i = 0; i < handerCardList[0].Count; i++)
			{
				GameObject temp = handerCardList[0][i];
				int tempCardPoint = handerCardList[0][i].GetComponent<bottomScript>().getPoint();
				if (selfGangCardPoint == tempCardPoint)
				{
					handerCardList[0].RemoveAt(i);
					Destroy(temp);
					i--;
					removeCount++;
					if (removeCount == 4)
					{
						break;
					}
				}
			}
			List<GameObject> tempGangList = new List<GameObject>();
			for (int i = 0; i < 4; i++)
			{

				if (i < 3)
				{
					GameObject obj = createGameObjectAndReturn("Prefab/PengGangCard/gangBack",
						pengGangParenTransformB.transform, new Vector3(-370 + PengGangCardList.Count * 190f + i * 60, 0));
					tempGangList.Add(obj);
				}
				else if (i == 3)
				{
					GameObject obj1 = createGameObjectAndReturn("Prefab/PengGangCard/PengGangCard_B",
						pengGangParenTransformB.transform, new Vector3(-310f + PengGangCardList.Count * 190f, 24f));
					obj1.GetComponent<TopAndBottomCardScript>().setPoint(selfGangCardPoint);
					tempGangList.Add(obj1);
				}

			}

			PengGangCardList.Add(tempGangList);
		}

		SetPosition(false);
		moPai();
		CardsNumChange();
		GlobalDataScript.isDrag = true;
	}
}