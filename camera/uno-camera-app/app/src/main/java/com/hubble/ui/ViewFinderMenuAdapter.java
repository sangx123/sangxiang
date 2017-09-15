package com.hubble.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.LiveCameraActionButtonListener;

import java.util.List;

/**
 * Created by sonikas on 10/10/16.
 */
public class ViewFinderMenuAdapter extends BaseAdapter {

    Context context;
    List<ViewFinderMenuItem> menuItemList;
    LiveCameraActionButtonListener liveCameraActionButtonListener;
    int mCurrentPressedItem=-1;

    public ViewFinderMenuAdapter(List<ViewFinderMenuItem> menuItemList, Context context,LiveCameraActionButtonListener liveCameraActionButtonListener) {
        this.menuItemList = menuItemList;
        this.context = context;
        this.liveCameraActionButtonListener=liveCameraActionButtonListener;
        //none of the menu items are in pressed state
        mCurrentPressedItem=-1;
        for(ViewFinderMenuItem item:menuItemList)
            item.pressed=false;
    }

    @Override
    public int getCount() {
        return menuItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return menuItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View gridItem;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        gridItem = inflater.inflate(R.layout.view_finder_menu_item, null);
        final LinearLayout menuItemLayout=(LinearLayout)gridItem.findViewById(R.id.menu_item);
        ImageView imageView = (ImageView) gridItem.findViewById(R.id.menu_item_image);
        imageView.setImageResource(menuItemList.get(position).image);
        gridItem.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(mCurrentPressedItem!=-1 && mCurrentPressedItem<menuItemList.size() && mCurrentPressedItem!=position)
                    selectItem(menuItemList.get(mCurrentPressedItem));
                mCurrentPressedItem=position;
                selectItem(menuItemList.get(position));
                notifyDataSetChanged();

            }
        });


        if(mCurrentPressedItem==position){
            menuItemLayout.setBackgroundColor(ContextCompat.getColor(context,R.color.viewfinder_primary_bg));
        }else{
            menuItemLayout.setBackgroundColor(ContextCompat.getColor(context,R.color.viewfinder_marine_bg));
        }

        return gridItem;

    }

    private void selectItem(ViewFinderMenuItem menuItem){
        menuItem.pressed=!menuItem.pressed;
        if(menuItem==ViewFinderMenuItem.MELODY){
            liveCameraActionButtonListener.onMelody(menuItem.pressed);
        }else if(menuItem==ViewFinderMenuItem.MIC){
            liveCameraActionButtonListener.onMic(menuItem.pressed);
        }else if(menuItem==ViewFinderMenuItem.RECORD){
            liveCameraActionButtonListener.onRecord(menuItem.pressed);
        }else if(menuItem==ViewFinderMenuItem.PAN){
            liveCameraActionButtonListener.onPan(menuItem.pressed);
        }
    }

   public void resetSelection(){
       mCurrentPressedItem=-1;
       for(ViewFinderMenuItem item:menuItemList)
           item.pressed=false;
   }

   // this is hack, we should implement Item click, not view click for grid view adaper
    public void setSelection(int position)
    {
        if(position >= 0 && position < menuItemList.size())
        {
            mCurrentPressedItem = position;
            selectItem(menuItemList.get(position));
        }
    }


}
