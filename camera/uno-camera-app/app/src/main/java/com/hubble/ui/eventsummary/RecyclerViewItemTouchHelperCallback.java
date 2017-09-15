package com.hubble.ui.eventsummary;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by Admin on 22-05-2017.
 */
public class RecyclerViewItemTouchHelperCallback extends ItemTouchHelper.Callback {

	private ItemSwipeHelper mItemSwipeHelper;


	public RecyclerViewItemTouchHelperCallback(ItemSwipeHelper itemSwipeHelper){
		mItemSwipeHelper = itemSwipeHelper;
	}

	@Override
	public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
		int swipeFlags = ItemTouchHelper.END;
		return makeMovementFlags(dragFlags, swipeFlags);
	}

	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
		mItemSwipeHelper.onSwipe(viewHolder.getAdapterPosition());
	}

	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
		return false;
	}

	@Override
	public boolean isItemViewSwipeEnabled() {
		return true;
	}

}
