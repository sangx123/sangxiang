package winway.mdr.chinaunicom.comm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import winway.mdr.chinaunicom.services.BlackWhiteServices;
import winway.mdr.telecomofchina.activity.R;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FQMyAdapter extends BaseAdapter {
	 BlackWhiteServices blackWhiteServices;
    private class buttonViewHolder {
    	ImageButton ImageButton;
        ImageView ivshowimage;
        TextView ivshowtext;
        ImageView ivischeck;
        TextView tvstatus;
    
    }
    
    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;
    private buttonViewHolder holder;
    String first_position;
    public static MediaPlayer mediaPlayer=new MediaPlayer();
    static int _position=100;
    public FQMyAdapter(Context c, ArrayList<HashMap<String, Object>> appList, int resource,
            String[] from, int[] to) {
        mAppList = appList;
        mContext = c;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyString = new String[from.length];
        valueViewID = new int[to.length];
        blackWhiteServices=new BlackWhiteServices(c);
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
            convertView = mInflater.inflate(R.layout.temp_listview_item, null);
            holder = new buttonViewHolder();
            holder.ivshowimage = (ImageView)convertView.findViewById(valueViewID[0]);
            holder.ivshowtext = (TextView)convertView.findViewById(valueViewID[1]);
            holder.ivischeck=(ImageView) convertView.findViewById(valueViewID[2]);
            holder.tvstatus = (TextView)convertView.findViewById(valueViewID[3]);
            holder.ImageButton=(ImageButton)convertView.findViewById(valueViewID[4]);
            convertView.setTag(holder);
        }
        
        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {
        	 int mid = (Integer)appInfo.get(keyString[0]);
            String showtext = (String) appInfo.get(keyString[1]);
            int ivischeck_mid = (Integer)appInfo.get(keyString[2]);
            String phone_tvstatus=(String) appInfo.get(keyString[3]);
            String ibstatus=(String) appInfo.get(keyString[4]);
            holder.ivshowimage.setImageDrawable(holder.ivshowimage.getResources().getDrawable(mid));
            holder.ivshowtext.setText(showtext);
            holder.tvstatus.setText(phone_tvstatus);
            holder.ivischeck.setVisibility(View.VISIBLE);
            if(phone_tvstatus.equals("0")) {
            	holder.ivischeck.setImageResource(R.drawable.check_no);
			} else if(phone_tvstatus.equals("1")) {
            	holder.ivischeck.setImageResource(R.drawable.check_pre);
			}
            
            if(ibstatus.equals("0")) {
				holder.ImageButton.setImageResource(R.drawable.sound);
			} else {
				holder.ImageButton.setImageResource(R.drawable.sound_on);
			}
            holder.ImageButton.setOnClickListener(new lvButtonListener(position));
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
              if(vid==holder.ImageButton.getId())
              {
            	     String res= mAppList.size()==5?"fjwrsound":"qhdrsound";
            	     playMusic(res, position);
            		 String info=(String) mAppList.get(position).get(keyString[4]);
                	 String values=info.equals("0")?"1":"0";
                	 updateData_ibstatus(position, values);
                	 notifyDataSetChanged();
            	
              }
           
            }
    }
    public void updateData(int position,int values){
    	for (int i = 0; i < mAppList.size(); i++) {
    		mAppList.get(i).put("status", String.valueOf(0));
    		mAppList.get(i).put("ibstatus", String.valueOf(0));
    		
		}
		Map<String,Object> oldMap=mAppList.get(position);
        	oldMap.put("status",String.valueOf(values));
    }
    
    
    public void updateData_ibstatus(int position,String values){
    	for (int i = 0; i < mAppList.size(); i++) {
    		mAppList.get(i).put("ibstatus", String.valueOf(0));
		}
		Map<String,Object> oldMap=mAppList.get(position);
        	oldMap.put("ibstatus",String.valueOf(values));
    }
    private void playMusic(String beforedetail,final int num){
		if(_position==num){
			    _position=100;
			  if(mediaPlayer!=null){
				   if(mediaPlayer.isPlaying()){
					   mediaPlayer.reset();
				   }
			  }
		}else{
			try {
				 _position=num;
				//重置MediaPlayer
				mediaPlayer.reset();
				AssetFileDescriptor afd = mContext.getAssets().openFd(beforedetail+(num+1)+".mp3");
				mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				mediaPlayer.prepare();
				mediaPlayer.start();
				mediaPlayer.setOnCompletionListener(new OnCompletionListener(){

					public void onCompletion(MediaPlayer mp) {
						_position=100;
						Map<String,Object> oldMap=mAppList.get(num);
			        	oldMap.put("ibstatus",String.valueOf(0));
			        	notifyDataSetChanged();
			        	Toast.makeText(mContext, "试听完毕", 3).show();
					}
				});
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
    /*************************************
     * 
     */
   public void itemStop(){
	   if(mediaPlayer!=null){
		   _position=100;
		   if(mediaPlayer.isPlaying()){
			   mediaPlayer.reset();
		   }
	   }
   }
   /**************************************
    * 返回的时候进行把mediaPlayer释放掉
    * 清理一下缓存相关数据
    */
    public void ForBack(){
    	mediaPlayer.reset();
    	_position=100;
    	System.gc();
//  	  if(mediaPlayer!=null){
//  		  System.out.println("播放器暂停操作");
//  		  if(mediaPlayer.isPlaying()){
//  			_position=100;
//  			  mediaPlayer.reset();
//  			  mediaPlayer.release();
//  			  mediaPlayer=null;
//  			  System.gc();
//  		  }
//  	  }
    }
}

