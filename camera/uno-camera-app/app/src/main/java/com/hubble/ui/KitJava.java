package com.hubble.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;


/**
 * Created by sonikas on 18/08/16.
 */
public class KitJava implements View.OnClickListener{

    View view;

    public KitJava() {
    }

    public KitJava(View view) {
        this.view = view;
    }

    public KitJava click(){
        view.setOnClickListener(this);
        return this;
    }

    @Override
    public void onClick(View v) {

    }

    public void gone() {
        view.setVisibility(View.GONE);
    }

    public void visible() {
        view.setVisibility(View.VISIBLE);
    }

    public void invisible() {
        view.setVisibility(View.INVISIBLE);
    }

    public BaseAdapter create(final List<Object> backingList,final IKitView view){
        BaseAdapter viewAdapter=new BaseAdapter() {
            @Override
            public int getCount() {
                return backingList.size();
            }

            @Override
            public Object getItem(int position) {
                return backingList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return view.viewFunc(position,convertView,parent);
            }
        };
        return viewAdapter;
    }



}
