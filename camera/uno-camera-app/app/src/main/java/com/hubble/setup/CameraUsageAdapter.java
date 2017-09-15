package com.hubble.setup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hubbleconnected.camera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sonikas on 21/02/17.
 */

public class CameraUsageAdapter extends BaseAdapter {

    List<String> mCameraUsageList;
    Context mContext;

    public CameraUsageAdapter(List<String> cameraUsageList, Context context) {
        mCameraUsageList=cameraUsageList;
        mContext=context;
    }

    @Override
    public int getCount() {
        return mCameraUsageList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCameraUsageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View row;
        row = inflater.inflate(R.layout.camera_usage_list_row, parent, false);
        TextView usageText=(TextView)row.findViewById(R.id.usage_text);
        usageText.setText(mCameraUsageList.get(position));
        return row;
    }


}
