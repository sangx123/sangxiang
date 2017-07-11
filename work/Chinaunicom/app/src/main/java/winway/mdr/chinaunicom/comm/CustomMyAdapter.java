package winway.mdr.chinaunicom.comm;

import java.util.ArrayList;
import java.util.HashMap;

import winway.mdr.chinaunicom.activity.R;
import winway.mdr.chinaunicom.services.TimeSettingService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
/************************************
 * 定时设置的适配器(自定义适配器)
 * @author zhaohao
 * 时间:2011-11
 */
public class CustomMyAdapter extends BaseAdapter {
    private class buttonViewHolder {
    	ToggleButton tbtnonoff;
        ImageView ivtimesettinglistitemicon;
        TextView tvtimedetail_timesetting;
        TextView tvrepeat_timesetting;
        TextView tvlistitem_id;
        TextView tvitemopenyesorno;
        TextView scene_policy;
    }
    
    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;
    private buttonViewHolder holder;
    TimeSettingService timeSettingService;
    public CustomMyAdapter(Context c, ArrayList<HashMap<String, Object>> appList, int resource,
            String[] from, int[] to) {
        mAppList = appList;
        mContext = c;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyString = new String[from.length];
        valueViewID = new int[to.length];
        timeSettingService=new TimeSettingService(c);
        System.arraycopy(from, 0, keyString, 0, from.length);
        System.arraycopy(to, 0, valueViewID, 0, to.length);
    }
    
 
    public int getCount() {
        return mAppList.size();
    }

    public Object getItem(int position) {
        return mAppList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void removeItem(int position){ 
        mAppList.remove(position);
        this.notifyDataSetChanged();
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView != null) {
            holder = (buttonViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.timesetting_listview_item, null);
            holder = new buttonViewHolder();
            holder.ivtimesettinglistitemicon = (ImageView)convertView.findViewById(valueViewID[0]);
            holder.tvtimedetail_timesetting = (TextView)convertView.findViewById(valueViewID[1]);
            holder.tvrepeat_timesetting = (TextView)convertView.findViewById(valueViewID[2]);
            holder.tbtnonoff=(ToggleButton) convertView.findViewById(valueViewID[3]);
            holder.tvlistitem_id=(TextView)convertView.findViewById(valueViewID[4]);
            holder.tvitemopenyesorno=(TextView)convertView.findViewById(valueViewID[5]);
            holder.scene_policy=(TextView)convertView.findViewById(valueViewID[6]);
            convertView.setTag(holder);
        }
        
        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {
            String aname = (String) appInfo.get(keyString[1]);
            String repeat=(String) appInfo.get(keyString[2]);
            String id=(String) appInfo.get(keyString[3]);
            String openyesorno=(String) appInfo.get(keyString[4]);
            String  scene_policy=(String) appInfo.get(keyString[5]);
            int mid = (Integer)appInfo.get(keyString[0]);
            holder.scene_policy.setText(scene_policy);
            holder.tvtimedetail_timesetting.setText(aname);
            holder.ivtimesettinglistitemicon.setImageDrawable(holder.ivtimesettinglistitemicon.getResources().getDrawable(mid));
            holder.tvrepeat_timesetting.setText(repeat);
            holder.tvlistitem_id.setText(id+"");
            holder.tvitemopenyesorno.setText(openyesorno);
            if(Integer.parseInt(openyesorno)==0){
            	holder.tbtnonoff.setChecked(false);
            }else{
            	holder.tbtnonoff.setChecked(true);
            }
            holder.tbtnonoff.setOnClickListener(new lvButtonListener(position));
        }
        return convertView;
    }

    class lvButtonListener implements OnClickListener {
        private int position;

        lvButtonListener(int pos) {
            position = pos;
        }

        public void onClick(View v) {
            int vid=v.getId();
              if(vid==holder.tbtnonoff.getId()){
            	String id=(String) mAppList.get(position).get("id");
            	String status=(String) mAppList.get(position).get("openyesorno");
            	int temp=Integer.parseInt(status)==0?1:0;
            	mAppList.get(position).put("openyesorno", String.valueOf(temp));
            	notifyDataSetChanged();
            	timeSettingService.UpdateStatus(Integer.parseInt(id), temp, 1);
               }
            }
    }
}

