package com.msc3.registration;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.util.SettingsPrefUtils;

/**
 * Created by aruna on 07/02/17.
 */

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsPrefUtils.SHOULD_READ_SETTINGS = true;
        setContentView(R.layout.bb_is_first_screen);
        Intent entry = new Intent(this, MainActivity.class);
      //  entry.putExtra(MainActivity.EXTRA_OFFLINE_MODE, isOfflineMode);
        entry.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        entry.putExtra(MainActivity.EXTRA_FLAG_SKIP_SERVER_SYNC, true);
       this.startActivity(entry);
        this.finish();


    }
}
