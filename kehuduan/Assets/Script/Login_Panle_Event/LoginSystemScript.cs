using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using AssemblyCSharp;
using LitJson;
using System.Collections.Generic;


public class LoginSystemScript : MonoBehaviour {
	

	//public ShareSDK shareSdk;
	private GameObject panelCreateDialog;

	public Toggle agreeProtocol;

	public Text versionText;

	private int tapCount = 0;//点击次数
	public GameObject watingPanel;


	void Start () {

		CustomSocket.hasStartTimer = false;
		GlobalDataScript.isonLoginPage = true;
        //PrefabManage.loadPerfab("Prefab/card/PutOutCard");
        //GameObject tempGameObject = createGameObjectAndReturn("Prefab/card/PutOutCard", GameObject.Find("Panel_Start").transform, new Vector3(0, -100f));
		//LoginCallBack();
		versionText.text ="版本号：" +Application.version;
	}
	
	// Update is called once per frame
	void Update () {
		if(Input.GetKey(KeyCode.Escape)){ //Android系统监听返回键，由于只有Android和ios系统所以无需对系统做判断
			if (panelCreateDialog == null) {
				panelCreateDialog = Instantiate (Resources.Load("Prefab/Panel_Exit")) as GameObject;
				panelCreateDialog.transform.parent = gameObject.transform;
				panelCreateDialog.transform.localScale = Vector3.one;
				//panelCreateDialog.transform.localPosition = new Vector3 (200f,150f);
				panelCreateDialog.GetComponent<RectTransform>().offsetMax = new Vector2(0f, 0f);
				panelCreateDialog.GetComponent<RectTransform>().offsetMin = new Vector2(0f, 0f);
			}
			
		} 

	}

	public void login(){
		
		GlobalDataScript.reinitData ();//初始化界面数据
		if (agreeProtocol.isOn) {
			doLogin ();
			watingPanel.SetActive(true);
		} else {
			MyDebug.Log ("请先同意用户使用协议");
			TipsManagerScript.getInstance ().setTips ("请先同意用户使用协议");
		}

		tapCount += 1;
		Invoke ("resetClickNum", 10f);
       
	}

	public void doLogin(){
        //GlobalDataScript.getInstance ().wechatOperate.login ();
        //RoomBackResponse();
        LoginCallBack();
	}

	public void LoginCallBack(){
		if (watingPanel != null) {
			watingPanel.SetActive(false);
		}
	
		//SoundCtrl.getInstance ().playBGM ();
	
			if (GlobalDataScript.homePanel != null) {
				GlobalDataScript.homePanel.GetComponent<HomePanelScript> ().removeListener ();
				Destroy (GlobalDataScript.homePanel);
			}


			if (GlobalDataScript.gamePlayPanel != null) {
				GlobalDataScript.gamePlayPanel.GetComponent<MyMahjongScript> ().exitOrDissoliveRoom ();
			}
            //实例化一个prefab，给该prefab赋值
		    panelCreateDialog = Instantiate (Resources.Load("Prefab/Panel_Home")) as GameObject;
            //将该prefab放在一个指定的容器中(container)
			panelCreateDialog.transform.parent = GlobalDataScript.getInstance().canvsTransfrom;
            //设置缩放比例是1
			panelCreateDialog.transform.localScale = Vector3.one;
			panelCreateDialog.GetComponent<RectTransform>().offsetMax = new Vector2(0f, 0f);
			panelCreateDialog.GetComponent<RectTransform>().offsetMin = new Vector2(0f, 0f);
		    //将该prefeb对象赋值给GlobalDataScript变量
		    GlobalDataScript.homePanel = panelCreateDialog;
            //删除游戏脚本
			Destroy (this);
            //销毁物体
			Destroy (gameObject);
	
	}
		
	private void RoomBackResponse(){

		watingPanel.SetActive(false);

		if (GlobalDataScript.homePanel != null) {
			GlobalDataScript.homePanel.GetComponent<HomePanelScript> ().removeListener ();
			Destroy (GlobalDataScript.homePanel);
		}


		if (GlobalDataScript.gamePlayPanel != null) {
			GlobalDataScript.gamePlayPanel.GetComponent<MyMahjongScript> ().exitOrDissoliveRoom ();
		}
		//GlobalDataScript.reEnterRoomData = JsonMapper.ToObject<RoomJoinResponseVo> (response.message);

//		for (int i = 0; i < GlobalDataScript.reEnterRoomData.playerList.Count; i++) {
//			AvatarVO itemData =	GlobalDataScript.reEnterRoomData.playerList [i];
//			if (itemData.account.openid == GlobalDataScript.loginResponseData.account.openid) {
//				//GlobalDataScript.loginResponseData.account.uuid = itemData.account.uuid;
//				//ChatSocket.getInstance ().sendMsg (new LoginChatRequest(GlobalDataScript.loginResponseData.account.uuid));
//				break;
//			}
//		}

		GlobalDataScript.gamePlayPanel =  PrefabManage.loadPerfab ("Prefab/Panel_GamePlay");
		//removeListener ();
		Destroy (this);
		Destroy (gameObject);
	
	}


	private void resetClickNum(){
		tapCount = 0;
	}


	//--------------------------------------------------------------------------------------------------------------------------
	private GameObject createGameObjectAndReturn(string path, Transform parent, Vector3 position)
	{
		GameObject obj = Instantiate(Resources.Load(path)) as GameObject;
		obj.transform.SetParent(parent);
		//obj.transform.parent = parent;
		obj.transform.localScale = Vector3.one;
		obj.transform.localPosition = position;
		return obj;
	}


}
