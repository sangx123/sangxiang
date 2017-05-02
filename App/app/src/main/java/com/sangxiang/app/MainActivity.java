package com.sangxiang.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;


public class MainActivity extends LogActivity {

    RecyclerView mRecyclerView;
    private ArrayList<String> date = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);


        for (int i = 0; i < 100; i++) {
            date.add("我是第" + i + "个");
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        mRecyclerView.setAdapter(new BaseQuickAdapter<String, BaseViewHolder>(R.layout.adapter_main, date) {
                                     @Override
                                     protected void convert(BaseViewHolder helper, String item) {
                                         helper.setText(R.id.mTxt, item);
                                     }
                                 }
        );
    }

    public class MyItemDecoration extends RecyclerView.ItemDecoration {


        private final Paint dividerPaint;
        private int mTopHeight = 100;

        public MyItemDecoration() {
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

            int right = parent.getWidth();
            //得到第一个可见item的位置
            int index = ((LinearLayoutManager) (parent.getLayoutManager())).findFirstVisibleItemPosition();
            //出现一个奇怪的bug，有时候child为空，所以将 child = parent.getChildAt(i)。-》 parent.findViewHolderForLayoutPosition(index).itemView
            View child = parent.findViewHolderForLayoutPosition(index).itemView;
            //第一个类型的Item 的 header 在 position==2（firstItemPosition）处，如果index==2，则说明此item在顶部
            //index>=2，说明之后的item都有吸顶效果
            Paint paint = new Paint();
            paint.setColor(Color.BLUE);
            int top = parent.getPaddingTop();
            int bottom = parent.getPaddingTop() + mTopHeight;
            //制造"最新"（下一组第一个item）和"推荐"交接时，被顶上去的效果
            //在还没开始交接的时候，"最新"的getBottom()>bottom
            if (isFirstOfGroup(index + 1)) {
                bottom = Math.min(child.getBottom(), bottom);
            }
            c.drawRect(0, top, right, bottom, paint);
            /*paint.setColor(Color.BLACK);
            paint.setTextSize(60);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setAntiAlias(true);
            c.drawText(tag, left, bottom, paint);*/
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
