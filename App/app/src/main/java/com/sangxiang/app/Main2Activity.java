package com.sangxiang.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Main2Activity extends LogActivity {

    @Bind(R.id.btn)
    Button btn;
    @Bind(R.id.btn1)
    Button btn1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Log.e(TAG, "onCreate: ");
        ButterKnife.bind(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "onNewIntent: ");
    }

    @OnClick(R.id.btn)
    public void onViewClicked() {
        Intent intent=new Intent(Main2Activity.this, Main2Activity.class);
        Main2Activity.this.startActivity(intent);
    }

    @OnClick(R.id.btn1)
    public void onViewClicked1() {
        Main2Activity.this.startActivity(new Intent(Main2Activity.this,MainActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: " );
    }
}
