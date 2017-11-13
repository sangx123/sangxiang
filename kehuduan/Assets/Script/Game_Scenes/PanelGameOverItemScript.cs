using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class PanelGameOverItemScript : MonoBehaviour {

    public Text rightPlayer;
    public Text topPlayer;
    public Text leftPlayer;
    public Text bottomPlayer;
    public Text scoreName;

    public GameObject scroll;

	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

    public void setUI(List<MajiangResult> list){
        //for (int i = 0;i<list.s)
        MajiangResult model = list[0];
        rightPlayer.text = model.rightScore.ToString();
        topPlayer.text = model.topScore.ToString();
        leftPlayer.text = model.leftScore.ToString();
        bottomPlayer.text = model.bottomScore.ToString();
        scoreName.text = model.huName;
    }
}
