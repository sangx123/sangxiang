using System;
using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using UnityEngine.EventSystems;

public class bottomScript : MonoBehaviour, IPointerDownHandler, IPointerUpHandler, IDragHandler
{
    private Rigidbody2D pai;
   
   // public GameObject Bigmajiang;
   // public GameObject Image;
    private float timer = 0;
    private int cardPoint;
    private Vector3 RawPosition;
    private Vector3 oldPosition;
	private bool dragFlag = false;
    //==================================================
    public Image image;
    public Text showLabel;
    public float speed = 1.0f;
    public float ShowTime = 1.5f;
    public GameObject bgImage;
    //
    public delegate void EventHandler(GameObject obj);
    public event EventHandler onSendMessage;
	public event EventHandler reSetPoisiton;
	public bool selected = false;

    // Use this for initialization
    void Start()
    {
    }

    // Update is called once per frame
    void Update()
    {

    }
    //再拖拽过程中的事件
    public void OnDrag(PointerEventData eventData)
    {
        if (GlobalDataScript.isDrag)
        {
			dragFlag = true;
            GetComponent<RectTransform>().pivot.Set(0, 0);
            transform.position = Input.mousePosition;
        }
    }

	//鼠标点击事件
	//鼠标点击A对象，按下鼠标时A对象响应此事件
    //鼠标点击一个的时候 让他的选中状态设置为true，其他的都设置为false
    //在选择定缺牌的时候，其他的都不应该是false，只能有3个不是
	public void OnPointerDown(PointerEventData eventData)
    {
		//以前的逻辑是第一次鼠标按下seleted设置为true
		//当selected设置为true的时候，就执行onSendMessage事件即cardChange事件
		Debug.Log("OnPointerDown"+ Convert.ToString(selected));
		if (GlobalDataScript.isHuan3zhang)
		{
			if (selected == false)
			{
                GlobalDataScript.huan3zhangNum++;
				selected = true;
				oldPosition = transform.localPosition;
			}
			else
			{
                GlobalDataScript.huan3zhangNum--;
                sendObjectToCallBack();
                selected = false;
			}
		}
		else if (GlobalDataScript.isDrag) {
			if (selected == false) {
				selected = true;
				oldPosition = transform.localPosition;
			} else {
				sendObjectToCallBack ();
			}
		}
    }
	//鼠标弹起事件
	//鼠标点击A对象，抬起鼠标时响应
	//无论鼠标在何处抬起（即不在A对象中）
	//都会在A对象中响应此事件
	//注：响应此事件的前提是A对象必须响应过OnPointerDown事件
	//Debug.Log("OnPointerUp " + name);
    public void OnPointerUp(PointerEventData eventData)
    {
		//以前的逻辑，如果鼠标的位置超过了一定的值就执行onSendMessage事件即cardChange事件
		//reSetPoisitonCallBack即执行了cardSelect事件
		//Debug.Log("OnPointerUp="+Convert.ToString(selected));
        Debug.Log("dragFlag="+Convert.ToString(dragFlag));
		if (GlobalDataScript.isHuan3zhang)
		{
			if (transform.localPosition.y > -122f)
			{
				sendObjectToCallBack();
			}
			else
			{
                //表示没有拖动麻将
				if (dragFlag)
				{
					transform.localPosition = oldPosition;
				}
				else
				{
					reSetPoisitonCallBack();
				}
			}
			dragFlag = false;
		
		}
		else if (GlobalDataScript.isDrag) {
			if (transform.localPosition.y > -122f) {
				sendObjectToCallBack ();
			} else {
				if (dragFlag) {
					transform.localPosition = oldPosition;
				} else {
					reSetPoisitonCallBack ();
				}
			}
			dragFlag = false;
		}
    }

	private void sendObjectToCallBack(){
		if (onSendMessage != null)     //发送消息
		{
			Debug.Log("onSendMessage(gameObject)");
			onSendMessage(gameObject);//发送当前游戏物体消息
		}
	}

	private void reSetPoisitonCallBack(){
		if (reSetPoisiton != null) {
			reSetPoisiton (gameObject);
		}
	}

    public void setPoint(int _cardPoint)
    {
        cardPoint = _cardPoint;//设置所有牌指针
		image.sprite = Resources.Load("Cards/Big/b"+cardPoint,typeof(Sprite)) as Sprite;

    }

    public int getPoint()
    {
        return cardPoint;
    }

    private void destroy()
    {
       // Destroy(this.gameObject);
    }

}
