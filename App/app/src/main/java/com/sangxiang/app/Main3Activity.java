package com.sangxiang.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import static com.sangxiang.app.R.layout.activity_main3;

public class Main3Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main3);
        Log.e("sangxiang", "onCreate: " );
        Intent intent=new Intent(this,Main3Activity.class);
        startActivity(intent);

    }
}