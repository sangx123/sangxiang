package com.hubble.ui;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.hubble.HubbleApplication;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.LiveCameraActionButtonListener;

import java.util.List;

/**
 * Created by sonikas on 18/08/16.
 */
public class PlaybackMenuAdapterJava extends BaseAdapter {

    Context context;
    List<LiveMenuItem> items;
    LiveCameraActionButtonListener menuItemListener;
    GridView gv;
    boolean changeResolutionStarted;
    private EventData eventData;
    public PlaybackMenuAdapterJava() {
    }

    public PlaybackMenuAdapterJava(Context context, List<LiveMenuItem> items, LiveCameraActionButtonListener menuItemListener, GridView gv) {
        this.context = context;
        this.items = items;
        this.menuItemListener = menuItemListener;
        this.gv = gv;
        eventData = new EventData();
    }

    Animation animation=  AnimationUtils.loadAnimation(HubbleApplication.AppContext, R.anim.processing_animation);
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView img = new ImageView(context);
        LiveMenuItem item = items.get(position);
        if(item.toggleable){
            setupToggleable(item, img);
        }else{
            setupRegular(item, img);
        }


        setImageState(item, img);

        if(item == LiveMenuItem.HD) {
            if(changeResolutionStarted) {
                img.setImageResource(R.drawable.ic_action_loading);
                img.startAnimation(animation);
            } else {
                img.setAnimation(null);
            }
        }
        return img;
    }

    public void startChangeResolution() {
        changeResolutionStarted = true;
    }

    public void stopChangeResolution() {
        changeResolutionStarted = false;
        notifyDataSetChanged();
    }

    public void setImageState(LiveMenuItem item, ImageView img) {
        if(item.pressed){
            img.setImageResource(item.pressedImage);
        }else{
            img.setImageResource(item.image);

        }

    }

    public void setupRegular(final LiveMenuItem item, final ImageView img) {
        img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    selectItem(item);
                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    clearPressed();
                }
                setImageState(item, img);
                gv.invalidateViews();
                return true;

            }
        });



    }

    public void setupToggleable(final LiveMenuItem item , final ImageView img) {
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(item);
               // setItemPressed(item,true);
                setImageState(item, img);
                gv.invalidateViews();
            }
        });

    }

    public void selectItem(LiveMenuItem item) {
        // call our callbacks
        Log.d("mbp", "selectItem name" + item.itemName + ", autoUpdate? " + item.autoUpdate);
        boolean pressed;
        if(item.toggleable){
            pressed=!item.pressed;
        }else{
            pressed=true;
        }

        if (item.autoUpdate)
        {
            if (item.reset) {
                clearPressed();
            }
            item.pressed = pressed;

        }
        if(item== LiveMenuItem.PRESET){
            menuItemListener.onPreset(pressed);
        }else if(item== LiveMenuItem.ZOOM){
            menuItemListener.onZoom(pressed);
            if (pressed) {
                AnalyticsInterface.getInstance().trackEvent("Pan_Tilt_Zoom","Pan_Tilt_Zoom",eventData);
            }
        }else if(item== LiveMenuItem.MIC){
            menuItemListener.onMic(pressed);
            if (pressed) {
                AnalyticsInterface.getInstance().trackEvent("Use_Microphone","Use_Microphone",eventData);
            }
        }else if(item== LiveMenuItem.PAN){
            menuItemListener.onPan(pressed);
            if (pressed) {
                AnalyticsInterface.getInstance().trackEvent("Pan_Tilt_Zoom","Pan_Tilt_Zoom",eventData);
            }
        }else if(item== LiveMenuItem.RECORD){
            menuItemListener.onRecord(pressed);
        }else if(item== LiveMenuItem.STOARGE){
            menuItemListener.onStorage(pressed);
        }else if(item== LiveMenuItem.MUTE){
            menuItemListener.onAudioEnable(pressed);
        }else if(item== LiveMenuItem.MELODY){
            menuItemListener.onMelody(pressed);
        }else if(item== LiveMenuItem.TEMP){
            menuItemListener.onTemperature(pressed);
            if (pressed) {
                AnalyticsInterface.getInstance().trackEvent("Select_Thermometer","Select_Thermometer",eventData);
            }
        }else if(item== LiveMenuItem.SETTINGS){
            menuItemListener.onSettings(pressed);
        }else if(item== LiveMenuItem.CLB){
            menuItemListener.onMotionCalibration(pressed);
        }else if(item== LiveMenuItem.HD){
            if(!changeResolutionStarted) {
                menuItemListener.onHD(pressed);
            } else {
                Log.i("mbp", "Change resolution is in progress. Do not notify listener");
            }
        }else if(item== LiveMenuItem.HUMIDITY){
            menuItemListener.onHumidity(pressed);
        }

        notifyDataSetChanged();
    }



    public void clearPressed() {
        for (LiveMenuItem i : items ) {
            if (i.reset) {
                i.pressed = false;
            }
        }
    }

    public void clearAll() {
        for (LiveMenuItem i : items) {
            i.pressed = false;
        }
    }

    public void setItemPressed(LiveMenuItem item, boolean isPressed) {
        if (item.reset) {
            clearPressed();
        }
        item.pressed = isPressed;
        notifyDataSetChanged();
    }

    public int getPressedItemPosition(){
        int i = 0;
        for (LiveMenuItem it : items) {
            if (it.pressed && it.reset) {
                return i;
            }
            i += 1;
        }
        return -1;
    }

   public LiveMenuItem getPressedItem(){
        for (LiveMenuItem i :items) {
            if (i.pressed && i.reset) {
                return i;
            }
        }
        return null;
    }




}
