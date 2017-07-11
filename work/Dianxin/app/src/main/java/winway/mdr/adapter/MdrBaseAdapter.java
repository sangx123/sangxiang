package winway.mdr.adapter;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

public class MdrBaseAdapter extends BaseAdapter {
   protected ArrayList<Object> alObjects = new ArrayList<Object>();
   protected  AbsListView absListView;
   protected  Context mContext;
	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
	/**
	 * 设置集合
	 * @param alObjects
	 * @param boo
	 */
	public void setList(List<?> alObjects,boolean boo)
	{
		if(null!=alObjects&&alObjects.size()>0){
			this.alObjects.addAll(alObjects);
			if(boo)
			{
				notifyDataSetChanged();
			}
		}
	}
	public void addObject(Object alObjects,boolean boo)
	{
		if(null!=alObjects){
			this.alObjects.add(alObjects);
			if(boo)
			{
				notifyDataSetChanged();
			}
		}
	}
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		return null;
	}
	public ArrayList<Object> getAlObjects() {
		return alObjects;
	}	

	public void setAlObjects(ArrayList<Object> alObjects) {
		this.alObjects = alObjects;
	}
	public AbsListView getAbsListView() {
	return absListView;
}

	public void setAbsListView(AbsListView absListView) {
		this.absListView = absListView;
	}
}
