package winway.mdr.chinaunicom.comm;

import java.util.ArrayList;
import java.util.HashMap;

import winway.mdr.telecomofchina.activity.R;
import winway.mdr.chinaunicom.services.QuickSetupServices;
import winway.mdr.chinaunicom.services.TimeSettingService;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/******************************
 * 编辑时间设置的适配器
 * @author zhaohao
 * 时间:2011-11
 */
public class EditQuickMyAdapter extends BaseAdapter {
	TimeSettingService timeSettingService;
    private class buttonViewHolder {
    	
    	Button btnquick_item_line_edit;
        ImageView ivquick_item_lineicon_edit;
        TextView tvquick_item_line_one_edit;
        TextView tvquick_item_line_two_edit;
        TextView tvquick_item_line_three_edit;
        TextView tvquick_item_line_four_edit;
        TextView tvquick_item_line_id_edit;
    }
    
    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;
    private buttonViewHolder holder;
	ProgressDialog progressDialog;
	QuickSetupServices quickSetupServices;
    
    public EditQuickMyAdapter(Context c, ArrayList<HashMap<String, Object>> appList, int resource,
            String[] from, int[] to) {
        mAppList = appList;
        mContext = c;
		progressDialog = new ProgressDialog(c);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("正在删除定时状态......");
		progressDialog.hide();
		quickSetupServices=new QuickSetupServices(mContext);
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
            convertView = mInflater.inflate(R.layout.quick_list_line_item_edit, null);
            holder = new buttonViewHolder();
            holder.ivquick_item_lineicon_edit = (ImageView)convertView.findViewById(valueViewID[0]);
            holder.tvquick_item_line_one_edit = (TextView)convertView.findViewById(valueViewID[1]);
            holder.tvquick_item_line_two_edit = (TextView)convertView.findViewById(valueViewID[2]);
            holder.tvquick_item_line_three_edit=(TextView)convertView.findViewById(valueViewID[3]);
            holder.tvquick_item_line_four_edit=(TextView)convertView.findViewById(valueViewID[4]);
            holder.tvquick_item_line_id_edit = (TextView)convertView.findViewById(valueViewID[5]);
            holder.btnquick_item_line_edit=(Button) convertView.findViewById(valueViewID[6]);
            convertView.setTag(holder);
        }
        
        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {
            String scence = (String) appInfo.get(keyString[1]);
            String time_detail=(String) appInfo.get(keyString[2]);
            String policy=(String) appInfo.get(keyString[3]);
            String bieming=(String) appInfo.get(keyString[4]);
            String id=(String) appInfo.get(keyString[5]);
            int mid = (Integer)appInfo.get(keyString[0]);
            holder.tvquick_item_line_one_edit.setText(scence);
            holder.tvquick_item_line_two_edit.setText(time_detail);
            holder.tvquick_item_line_three_edit.setText(policy);
            holder.tvquick_item_line_four_edit.setText(bieming);
            holder.ivquick_item_lineicon_edit.setImageDrawable(holder.ivquick_item_lineicon_edit.getResources().getDrawable(mid));
            holder.tvquick_item_line_id_edit.setText(id+"");
            holder.btnquick_item_line_edit.setOnClickListener(new lvButtonListener(position));
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
              if(vid==holder.btnquick_item_line_edit.getId()){ }
                  DeleteItem(position);
            }
    }
    public void DeleteItem(final int position)
	{
		Builder builder=new Builder(mContext);
		builder.setTitle("温馨提示");
		builder.setMessage("您确定要删除吗?");
		builder.setIcon(android.R.drawable.ic_menu_delete);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
            	String id=(String) mAppList.get(position).get(keyString[5]);
				new DeleteTimeScene().execute(new Integer[] { Integer.parseInt(id), position });
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}

	class DeleteTimeScene extends AsyncTask<Integer, Integer, String> {
		int index;
		int position;
		@Override
		protected String doInBackground(Integer... params) {
			index = params[0];
			position = params[1];
			boolean delete_status=quickSetupServices.DeleteQuickSetupById(index,1);
			return  delete_status?"success":"error";
		}

		@Override
		protected void onPostExecute(String result) {
			progressDialog.hide();
		    if("success".equals(result)){
		    	mAppList.remove(position);
        		notifyDataSetChanged();
				Toast.makeText(mContext, "删除成功！", 3).show();
		    }
		}

		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}
	}
}

