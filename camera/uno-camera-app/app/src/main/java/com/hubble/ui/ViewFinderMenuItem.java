package com.hubble.ui;


import com.hubbleconnected.camera.R;

/**
 * Created by sonikas on 07/10/16.
 */
public class ViewFinderMenuItem {

    String menuName;
    int image;
    int pressedImage;
    boolean pressed;


    public ViewFinderMenuItem(String menuName, int image, int pressedImage, boolean pressed) {
        this.menuName = menuName;
        this.image = image;
        this.pressedImage = pressedImage;
        this.pressed = pressed;
    }


    public static final ViewFinderMenuItem MUTE = new ViewFinderMenuItem("MUTE", R.drawable.soundon, R.drawable.soundoff, false);
    public static final ViewFinderMenuItem HD = new ViewFinderMenuItem("HD", R.drawable.hdoff, R.drawable.hdon, false);
    public static final ViewFinderMenuItem MIC = new ViewFinderMenuItem("MIC", R.drawable.vector_drawable_mic, R.drawable.vector_drawable_mic, false);
    public static final ViewFinderMenuItem PAN = new ViewFinderMenuItem("PAN", R.drawable.navigate, R.drawable.navigate, false);
    public static final ViewFinderMenuItem MELODY = new ViewFinderMenuItem("MELODY", R.drawable.music, R.drawable.music, false);
    public static final ViewFinderMenuItem RECORD = new ViewFinderMenuItem("RECORD", R.drawable.vector_drawable_camera_menu, R.drawable.vector_drawable_camera_menu, false);
}
