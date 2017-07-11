package winway.mdr.adapter;
import winway.mdr.chinaunicom.entity.LxrEntity;
import winway.mdr.telecomofchina.activity.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

 
public class LxrAdapter extends MdrBaseAdapter {
	private LayoutInflater inflater = null;
	public LxrAdapter(Context context){
		inflater = LayoutInflater.from(context);
		this.mContext = context;
	}
	public void setAbsListView(AbsListView absListView) {
		this.absListView = absListView;
	}
	public void clear()
	{
		this.alObjects.clear();
		notifyDataSetChanged();
	}
	public void addObject(LxrEntity lxrEntity,boolean notifi){
		getAlObjects().add(lxrEntity);
		if(notifi){
			notifyDataSetChanged();
		}
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.phone_list_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.tvusername = (TextView)convertView.findViewById(R.id.tvusername);
			viewHolder.tvuseruserphonenumber = (TextView)convertView.findViewById(R.id.tvuseruserphonenumber);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		LxrEntity indexEntity =(LxrEntity) alObjects.get(position);
		viewHolder.tvusername.setText(indexEntity.getUname());
		viewHolder.tvuseruserphonenumber.setText(indexEntity.getUphone());
		return convertView;
	}
	
	static class ViewHolder{
		TextView tvusername,tvuseruserphonenumber;
	}

	@Override
	public int getCount() {
		return alObjects.size();
	}

	@Override
	public Object getItem(int arg0) {
		return    alObjects.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
 
   
  
}
