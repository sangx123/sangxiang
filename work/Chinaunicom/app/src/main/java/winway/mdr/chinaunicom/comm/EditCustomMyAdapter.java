package winway.mdr.chinaunicom.comm;

import java.util.ArrayList;
import java.util.HashMap;

import winway.mdr.chinaunicom.activity.R;
import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
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

import com.liz.cptr.TReplaceTimeSceneResult;
/******************************
 * 编辑时间设置的适配器
 * @author zhaohao
 * 时间:2011-11
 */
public class EditCustomMyAdapter extends BaseAdapter {
	TimeSettingService timeSettingService;
    private class buttonViewHolder {
    	
    	Button btndelete_edit;
        ImageView ivtimesettinglistitemicon_edit;
        TextView tvtimedetail_timesetting_edit;
        TextView tvrepeat_timesetting_edit;
        TextView tvlistitem_id_edit;
        TextView tvitemopenyesorno_edit;
        TextView scene_policy_edit;
    }
    
    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;
    private buttonViewHolder holder;
	ProgressDialog progressDialog;
    
    public EditCustomMyAdapter(Context c, ArrayList<HashMap<String, Object>> appList, int resource,
            String[] from, int[] to) {
        mAppList = appList;
        mContext = c;
		progressDialog = new ProgressDialog(c);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("正在删除定时状态......");
		progressDialog.hide();

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
            convertView = mInflater.inflate(R.layout.timesetting_listview_item_edit, null);
            holder = new buttonViewHolder();
            holder.ivtimesettinglistitemicon_edit = (ImageView)convertView.findViewById(valueViewID[0]);
            holder.tvtimedetail_timesetting_edit = (TextView)convertView.findViewById(valueViewID[1]);
            holder.tvrepeat_timesetting_edit = (TextView)convertView.findViewById(valueViewID[2]);
            holder.btndelete_edit=(Button) convertView.findViewById(valueViewID[3]);
            holder.tvlistitem_id_edit=(TextView)convertView.findViewById(valueViewID[4]);
            holder.tvitemopenyesorno_edit=(TextView)convertView.findViewById(valueViewID[5]);
            holder.scene_policy_edit=(TextView)convertView.findViewById(valueViewID[6]);
            convertView.setTag(holder);
        }
        
        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {
            String aname = (String) appInfo.get(keyString[1]);
            String repeat=(String) appInfo.get(keyString[2]);
            String id=(String) appInfo.get(keyString[3]);
            String openyesorno=(String) appInfo.get(keyString[4]);
            String scene_policy_edit=(String) appInfo.get(keyString[5]);
            int mid = (Integer)appInfo.get(keyString[0]);
            holder.tvtimedetail_timesetting_edit.setText(aname);
            holder.ivtimesettinglistitemicon_edit.setImageDrawable(holder.ivtimesettinglistitemicon_edit.getResources().getDrawable(mid));
            holder.tvrepeat_timesetting_edit.setText(repeat);
            holder.tvlistitem_id_edit.setText(id+"");
            holder.tvitemopenyesorno_edit.setText(openyesorno);
            holder.scene_policy_edit.setText(scene_policy_edit);
            holder.btndelete_edit.setOnClickListener(new lvButtonListener(position));
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
              if(vid==holder.btndelete_edit.getId()){ }
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

            	String id=(String) mAppList.get(position).get(keyString[3]);
				new DeleteTimeScene().execute(new Integer[] { Integer.parseInt(id), position });
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}

	class DeleteTimeScene extends AsyncTask<Integer, Integer, TReplaceTimeSceneResult.Enum> {
		int index;
		int position;
		@Override
		protected TReplaceTimeSceneResult.Enum doInBackground(Integer... params) {
			index = params[0];
			position = params[1];
			return HttpDataAccess.getInstance().replaceTimeScene(index, null);
		}

		@Override
		protected void onPostExecute(TReplaceTimeSceneResult.Enum result) {
			progressDialog.hide();
			Integer errorId = MyResources.getHttpErrorIndex(result);
			if (errorId != null) {
				Toast.makeText(mContext, mContext.getResources().getString(errorId), 3).show();
				return;
			}
			
			switch (result.intValue()) {
			case TReplaceTimeSceneResult.INT_SUCCESS:
				timeSettingService.DeleteById(index, TimeSettingService.WRITE);
        		mAppList.remove(position);
        		notifyDataSetChanged();
				Toast.makeText(mContext, "删除成功！", 3).show();
				break;
			case TReplaceTimeSceneResult.INT_FAILED_INDEX_NOT_EXISTED:
				Toast.makeText(mContext, "删除失败，服务器已不存在此数据！", 3).show();
				break;
			default:
				break;
        	}
		}

		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}
	}
}

