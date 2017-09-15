package com.hubble.videobrowser;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Son Nguyen on 11/12/2015.
 */
public class MyRecyclerView extends RecyclerView {
  private int columnWidth;
  private GridLayoutManager manager;

  private View emptyView;
  private AdapterDataObserver emptyObserver = new AdapterDataObserver() {

    @Override
    public void onChanged() {
      Adapter<?> adapter =  getAdapter();
      if(adapter != null && emptyView != null) {
        if(adapter.getItemCount() == 0) {
          emptyView.setVisibility(View.VISIBLE);
          MyRecyclerView.this.setVisibility(View.GONE);
        } else {
          emptyView.setVisibility(View.GONE);
          MyRecyclerView.this.setVisibility(View.VISIBLE);
        }
      }
    }
  };

  public MyRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);

    if (attrs != null) {
      // Read android:columnWidth from xml
      int[] attrsArray = {
          android.R.attr.columnWidth
      };
      TypedArray array = context.obtainStyledAttributes(attrs, attrsArray);
      columnWidth = array.getDimensionPixelSize(0, -1);
      array.recycle();
    }

    manager = new GridLayoutManager(getContext(), 1);
    setLayoutManager(manager);
  }

  protected void onMeasure(int widthSpec, int heightSpec) {
    super.onMeasure(widthSpec, heightSpec);
    if (columnWidth > 0) {
      int spanCount = Math.max(1, getMeasuredWidth() / columnWidth);
      manager.setSpanCount(spanCount);
    }
  }

  @Override
  public void setAdapter(Adapter adapter) {
    super.setAdapter(adapter);

    if(adapter != null) {
      adapter.registerAdapterDataObserver(emptyObserver);
    }
    emptyObserver.onChanged();
  }

  public void setEmptyView(View emptyView) {
    this.emptyView = emptyView;
  }
}
