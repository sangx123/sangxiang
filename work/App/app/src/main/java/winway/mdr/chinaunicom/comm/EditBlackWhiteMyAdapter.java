package winway.mdr.chinaunicom.comm;

import java.util.ArrayList;
import java.util.HashMap;

import winway.mdr.telecomofchina.activity.R;
import winway.mdr.chinaunicom.internet.data.tools.HttpDataAccess;
import winway.mdr.chinaunicom.services.BlackWhiteServices;
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
import android.widget.TextView;
import android.widget.Toast;

import com.liz.cptr.TBlackwhiteState;
import com.liz.cptr.TDelPhonebooksResult;
/*******************************
 * 编辑黑白名单的适配器
 * @author zhaohao
 * 时间:2011-11
 */
public class EditBlackWhiteMyAdapter extends BaseAdapter {
	 BlackWhiteServices blackWhiteServices;
    private class buttonViewHolder {
    	Button btndeleteblack_orwhite_edit;
        TextView tvblack_white_phone_name;
        TextView tvblack_white_phone_number;
        TextView tvblack_whitelistitem_id_edit;
    }
    
    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;
    private buttonViewHolder holder;
    private HttpDataAccess dataAccess;
    private ProgressDialog progressDialog;
    public EditBlackWhiteMyAdapter(Context c, ArrayList<HashMap<String, Object>> appList, int resource,
            String[] from, int[] to) {
        mAppList = appList;
        mContext = c;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyString = new String[from.length];
        valueViewID = new int[to.length];
        blackWhiteServices=new BlackWhiteServices(c);
        dataAccess=HttpDataAccess.getInstance();
        progressDialog=new ProgressDialog(c);
        progressDialog.setMessage("正在删除请稍等...");
        System.arraycopy(from, 0, keyString, 0, from.length);
        System.arraycopy(to, 0, valueViewID, 0, to.length);
    }
    
 
	@Override
    public int getCount() {
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void removeItem(int position){
        mAppList.remove(position);
        this.notifyDataSetChanged();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView != null) {
            holder = (buttonViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.black_white_listview_item_edit, null);
            holder = new buttonViewHolder();
            holder.btndeleteblack_orwhite_edit = (Button)convertView.findViewById(valueViewID[0]);
            holder.tvblack_white_phone_name = (TextView)convertView.findViewById(valueViewID[1]);
            holder.tvblack_white_phone_number = (TextView)convertView.findViewById(valueViewID[2]);
            holder.tvblack_whitelistitem_id_edit=(TextView)convertView.findViewById(valueViewID[3]);
            convertView.setTag(holder);
        }
        
        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {
        	 int phone_id = (Integer)appInfo.get(keyString[0]);
            String phone_name = (String) appInfo.get(keyString[1]);
            String phone_number=(String) appInfo.get(keyString[2]);
           
            holder.tvblack_white_phone_name.setText(phone_name);
            holder.tvblack_white_phone_number.setText(phone_number);
            holder.tvblack_whitelistitem_id_edit.setText(String.valueOf(phone_id));
            holder.btndeleteblack_orwhite_edit.setOnClickListener(new lvButtonListener(position));
        }        
        return convertView;
    }

    class lvButtonListener implements OnClickListener {
        private int position;

        lvButtonListener(int pos) {
            position = pos;
        }
        
        @Override
        public void onClick(View v) {
            int vid=v.getId();
              if(vid==holder.btndeleteblack_orwhite_edit.getId()){ }
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
				new DeleteBlackWhite().execute(new Integer[]{position});
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}
    
    @SuppressWarnings("static-access")
	 public boolean GetLastErrorStatus(){
	 	 if(dataAccess.getLastError().equals(dataAccess.getLastError().SUCCESS))return true;
	 	 else return false;
	 }
    class DeleteBlackWhite extends AsyncTask<Integer, Integer, TDelPhonebooksResult.Enum> {
		int _id;
		int position;
		@Override
		protected TDelPhonebooksResult.Enum doInBackground(Integer... params) {
			position = params[0];
			String id=mAppList.get(position).get(keyString[0]).toString();
			_id=Integer.parseInt(id);
        	String phonenum=mAppList.get(position).get(keyString[2]).toString();
        	 String black_white=mAppList.get(position).get(keyString[3]).toString();
        	 TBlackwhiteState.Enum reEnum=null;
        	 reEnum=Integer.parseInt(black_white)==0?TBlackwhiteState.BLACKLIST:TBlackwhiteState.WHITELIST;
        	 TDelPhonebooksResult.Enum del_phone_num=dataAccess.delPhonebook(reEnum, phonenum);
			return del_phone_num;
		}

		@Override
		protected void onPostExecute(TDelPhonebooksResult.Enum result) {
			progressDialog.hide();
			Integer errorId = MyResources.getHttpErrorIndex(result);
			if (errorId != null) {
				Toast.makeText(mContext, mContext.getResources().getString(errorId), 3).show();
				return;
			}
			switch (result.intValue()) {
			case TDelPhonebooksResult.INT_SUCCESS:
				blackWhiteServices.DeleteById(_id, 1);
        		mAppList.remove(position);
        		notifyDataSetChanged();
				Toast.makeText(mContext, "删除成功！", 3).show();
				break;
			case TDelPhonebooksResult.INT_FAILED_PHONE_NUMBER_NOT_EXISTED:
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

