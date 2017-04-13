package com.sangxiang.app;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends LogActivity {
    @BindView(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    private ArrayList<String> date=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        for (int i=0;i<10;i++){
            date.add("我是第"+i+"个");
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new BaseQuickAdapter<String, BaseViewHolder>(R.layout.adapter_main,date) {
                                     @Override
                                     protected void convert(BaseViewHolder helper, String item) {
                                         helper.setText(R.id.mTxt,item);
                                     }
                                 }
        );
    }
}
