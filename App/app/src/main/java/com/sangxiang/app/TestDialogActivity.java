package com.sangxiang.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.sangxiang.app.widgets.Custom8View;

import java.util.Timer;
import java.util.TimerTask;

public class TestDialogActivity extends AppCompatActivity {
    int count=0;
    private Custom8View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_dialog);
        view=(Custom8View)findViewById(R.id.test);
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                count++;
                view.setValue(count);
                Log.e("sangxiang", "run: "+count );
                view.postInvalidate();
            }
        },0,50);
    }
}
