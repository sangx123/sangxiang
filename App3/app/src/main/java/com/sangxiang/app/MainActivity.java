package com.sangxiang.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;




public class MainActivity extends LogActivity {

    RecyclerView mRecyclerView;
    private ArrayList<String> date=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);


        for (int i=0;i<100;i++){
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
        mRecyclerView.addItemDecoration(new MyItemDecoration());
    }

    public class MyItemDecoration extends RecyclerView.ItemDecoration{


        private final Paint dividerPaint;
        private int mTopHeight=30;

        public MyItemDecoration(){
            dividerPaint = new Paint();
            dividerPaint.setColor(Color.RED);
            dividerPaint.setStrokeWidth(10);
            dividerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }
        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            // 得到item真实的left和right（减去parent的padding）
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            for (int i = 0; i != parent.getChildCount(); i++) {
                // 直接获得的child只有当前显示的，所以就算i是0的index也只是当前第一个，而不是所有第一个
                View child = parent.getChildAt(i);
                int index = parent.getChildAdapterPosition(child);
                if (isFirstOfGroup(index)) {
                    // 每组第一个item都留有空间来绘制
                    int top = child.getTop() - mTopHeight;
                    int bottom = child.getTop();
                    // 绘制背景色
                    Paint paint = new Paint();
                    paint.setColor(Color.YELLOW);
                    c.drawRect(left, top, right, bottom, paint);
                    // 绘制组名
                    paint.setColor(Color.BLACK);
                    paint.setTextSize(60);
                    paint.setTextAlign(Paint.Align.LEFT);
                    paint.setAntiAlias(true);
                    c.drawText(getGroupName(index), left, bottom, paint);
                }
            }
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);

            int bottom = 0;
            if (isFirstOfGroup(index + 1)) {
                // 下一个组马上到达顶部
                bottom = Math.min(child.getBottom(), mTopHeight);
            } else {
                // 普通情况
                bottom = mTopHeight;
            }


        }
        private boolean isFirstOfGroup(int index) {
            return index % 7 == 0;
        }
        private String getGroupName(int index) {
            return "第" + (index / 7 + 1) + "组";
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int index = parent.getChildAdapterPosition(view);
            if (isFirstOfGroup(index)) {
                outRect.top = mTopHeight;
            }
        }
    }
}
