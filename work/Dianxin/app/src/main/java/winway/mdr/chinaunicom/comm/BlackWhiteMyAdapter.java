package winway.mdr.chinaunicom.comm;

import java.util.ArrayList;
import java.util.HashMap;

import winway.mdr.telecomofchina.activity.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
/***************************************
 * 黑白名单的适配器
 * @author zhaohao
 * 时间:2011-11
 */
public class BlackWhiteMyAdapter extends BaseAdapter {
    private class buttonViewHolder {
   
        TextView tv_phone_name;
        TextView tv_phone_number;
        TextView tv_phone_id;
    }
    
    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;
    private buttonViewHolder holder;
    
    public BlackWhiteMyAdapter(Context c, ArrayList<HashMap<String, Object>> appList, int resource,
            String[] from, int[] to) {
        mAppList = appList;
        mContext = c;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyString = new String[from.length];
        valueViewID = new int[to.length];
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
            convertView = mInflater.inflate(R.layout.black_write_listview_item, null);
            holder = new buttonViewHolder();
            holder.tv_phone_name = (TextView)convertView.findViewById(valueViewID[0]);
            holder.tv_phone_number = (TextView)convertView.findViewById(valueViewID[1]);
            holder.tv_phone_id=(TextView) convertView.findViewById(valueViewID[2]);
            convertView.setTag(holder);
        }
        
        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {
            String phone_name = (String) appInfo.get(keyString[0]);
            String phone_number=(String) appInfo.get(keyString[1]);
            String phone_id=(String) appInfo.get(keyString[2]);
            holder.tv_phone_name.setText(phone_name);
            holder.tv_phone_number.setText(phone_number);
            holder.tv_phone_id.setText(phone_id);
        }        
        return convertView;
    }
}

