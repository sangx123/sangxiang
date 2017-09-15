package com.hubble.ui;


import com.hubbleconnected.camera.R;

/**
 * Created by sonikas on 18/08/16.
 */
public class LiveMenuItem {
    String itemName;
    int image;
    int pressedImage;
    boolean toggleable;
    boolean reset;
    boolean autoUpdate;
    boolean pressed=false;

    public LiveMenuItem(String itemName, int image, int pressedImage, boolean toggleable, boolean reset, boolean autoUpdate) {
        this.itemName = itemName;
        this.image = image;
        this.pressedImage = pressedImage;
        this.toggleable = toggleable;
        this.reset = reset;
        this.autoUpdate = autoUpdate;
    }

    public static final LiveMenuItem ZOOM =new LiveMenuItem("ZOOM", R.drawable.ic_zoom, R.drawable.ic_zoom, true, false, true);
    public static final LiveMenuItem MUTE =new LiveMenuItem("MUTE", R.drawable.ic_mute_off, R.drawable.ic_mute_on, true, false, true);
    public static final LiveMenuItem MIC =new LiveMenuItem("MIC", R.drawable.video_action_mic, R.drawable.video_action_mic_pressed, true, true, true);
    public static final LiveMenuItem PAN =new LiveMenuItem("PAN", R.drawable.video_action_pan, R.drawable.video_action_pan_pressed, true, true, true);
    public static final LiveMenuItem RECORD =new LiveMenuItem("RECORD", R.drawable.video_action_video, R.drawable.video_action_video_pressed, true, true, true);
    public static final LiveMenuItem STOARGE =new LiveMenuItem("STORAGE", R.drawable.ic_sd_card, R.drawable.ic_sd_card_pressed, true, true, true);
    public static final LiveMenuItem MELODY =new LiveMenuItem("MELODY", R.drawable.video_action_music, R.drawable.video_action_music_pressed, true, true, true);
    public static final LiveMenuItem TEMP =new LiveMenuItem("TEMP", R.drawable.video_action_temp, R.drawable.video_action_temp_pressed, true, true, true);
    public static final LiveMenuItem SETTINGS =new LiveMenuItem("SETTINGS", R.drawable.action_gear, R.drawable.video_action_gear_pressed, true, true, true);
    public static final LiveMenuItem HD =new LiveMenuItem("HD", R.drawable.ic_action_hd_icon, R.drawable.ic_action_hd_icon_pressed, true, false, false);
    public static final LiveMenuItem PRESET =new LiveMenuItem("PRESET", R.drawable.preset_icon, R.drawable.preset_press, true, true, true);
    public static final LiveMenuItem HUMIDITY =new LiveMenuItem("HUMIDITY", R.drawable.ic_humidity, R.drawable.ic_humidity_pressed, true, true, true);
    public static final LiveMenuItem CLB=new LiveMenuItem("CLB", R.drawable.ic_pulse, R.drawable.ic_pulse_pressed, true, true, true);
    public static final LiveMenuItem BTA=new LiveMenuItem("BTA", R.drawable.baby_bta_menu, R.drawable.baby_bta_menu, true, true, true);


}
