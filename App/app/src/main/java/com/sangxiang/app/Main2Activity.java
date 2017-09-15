package com.sangxiang.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;



import butterknife.OnClick;

public class Main2Activity extends LogActivity {

    Button btn;

    Button btn1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        btn = (Button) findViewById(R.id.btn);
        btn1 = (Button) findViewById(R.id.btn1);

        btn = (Button) findViewById(R.id.btn);
        btn1 = (Button) findViewById(R.id.btn1);

        Log.e(TAG, "onCreate: ");

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "onNewIntent: ");
    }

    @OnClick(R.id.btn)
    public void onViewClicked() {
        Intent intent = new Intent(Main2Activity.this, Main2Activity.class);
        Main2Activity.this.startActivity(intent);
    }

    @OnClick(R.id.btn1)
    public void onViewClicked1() {
        Main2Activity.this.startActivity(new Intent(Main2Activity.this, MainActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
    }

    @OnClick({R.id.btn, R.id.btn1})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn:
                break;
            case R.id.btn1:
                break;
        }
    }
}
