package com.sangxiang.app.widgets.RecyclerView;

/**
 * Created by sangxiang on 26/4/17.
 */

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public abstract class KaishiRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected List<T> dataSet = new ArrayList<>();

    public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void addSilently(T newItem) {
        dataSet.add(newItem);
    }

    public void add(T newItem) {
        dataSet.add(newItem);
        deDupeItems();
        sortItems();
        notifyDataSetChanged();
    }

    public void add(int index, T newItem) {
        dataSet.add(index, newItem);
        deDupeItems();
        sortItems();
        notifyDataSetChanged();
    }

    public void addAll(List<T> newItems) {
        dataSet.addAll(newItems);
        deDupeItems();
        sortItems();
        notifyDataSetChanged();
    }

    public void replaceAll(List<T> newItems) {
        List<T> newItemsCopy = new ArrayList<>(newItems);
        dataSet.clear(); // this clear has a chance of clearing newItems as well if the same object was passed back; this is why we are making a copy first
        dataSet.addAll(newItemsCopy); // don't inline in case newItems is the same dataSet object
        deDupeItems();
        sortItems();
        notifyDataSetChanged();
    }

    public void removeSilently(T item) {
        if (dataSet.contains(item)) {
            dataSet.remove(item);
        }
    }

    public void remove(T item) {
        if (dataSet.contains(item)) {
            dataSet.remove(item);
            notifyDataSetChanged();
        }
    }

    public List<T> getItems() {
        return dataSet;
    }

    public T getItem(int position) {
        return dataSet.get(position);
    }

    public int getIndexOf(T item) {
        return dataSet.indexOf(item);
    }

    public int getCount() {
        return dataSet.size();
    }

    protected void sortItems() {
    }

    protected void deDupeItems() {
        if (!dataSet.isEmpty()) {
            dataSet = new ArrayList<>(new LinkedHashSet<>(dataSet));
        }
    }

    public List<T> getDataSet() {
        return dataSet;
    }
}
