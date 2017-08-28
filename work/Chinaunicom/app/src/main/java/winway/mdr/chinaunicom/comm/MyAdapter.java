package winway.mdr.chinaunicom.comm;

/*
 * Copyright (C) 2006 The Undried Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import winway.mdr.chinaunicom.activity.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
public class MyAdapter extends BaseAdapter implements Filterable {
	
    private int[] mTo;
    private String[] mFrom;
    private ViewBinder mViewBinder;

    private List<Map<String, Object>> mData;
    
    public static String  IS_SELECTED ="is_selected";
    
    public  static String IS_CECKED ="is_checked";

    private int mResource;
    private int mDropDownResource;
    private LayoutInflater mInflater;

    private SimpleFilter mFilter;
    private ArrayList<Map<String, Object>> mUnfilteredData;
     boolean IStrue=false;
    /**
     * Constructor
     * 
     * @param context The context where the View associated with this SimpleAdapter is running
     * @param data A List of Maps. Each entry in the List corresponds to one row in the list. The
     *        Maps contain the data for each row, and should include all the entries specified in
     *        "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *        item. The layout file should include at least those named views defined in "to"
     * @param from A list of column names that will be added to the Map associated with each
     *        item.
     * @param to The views that should display column in the "from" parameter. These should all be
     *        TextViews. The first N views in this list are given the values of the first N columns
     *        in the from parameter.
     */
    public MyAdapter(Context context, List<Map<String, Object>> data,
            int resource, String[] from, int[] to) {
        mData = data;
        mResource = mDropDownResource = resource;
        mFrom = from;
        mTo = to;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public void updateData(int position,int values){
    	for (int i = 0; i < mData.size(); i++) {
			mData.get(i).put("status", 0);
		}
		Map<String,Object> oldMap=mData.get(position);
        	oldMap.put("status",values);
    }
    
    /**
     * @see android.widget.Adapter#getCount()
     */
    public int getCount() {
        return mData.size();
    }

    /**
     * @see android.widget.Adapter#getItem(int)
     */
    public Object getItem(int position) {
        return mData.get(position);
    }

    /**
     * @see android.widget.Adapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * @see android.widget.Adapter#getView(int, View, ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
    	
	    return createViewFromResource(position, convertView, parent, mResource);
    	
       
    }

    private View createViewFromResource(int position, View convertView,
    		
            ViewGroup parent, int resource) {
    	
        View v;
        
        if (convertView == null) {
        	
            v = mInflater.inflate(resource, parent, false);

            final int[] to = mTo;
            final int count = to.length;
            final View[] holder = new View[count];
     
            for (int i = 0; i < count; i++) {
                holder[i] = v.findViewById(to[i]);
            }
            v.setTag(holder);
            
        } else {
        	
            v = convertView;
        }
        bindView(position, v);

        return v;
    }

    /**
     * <p>Sets the layout resource to create the drop down views.</p>
     *
     * @param resource the layout resource defining the drop down views
     * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    public void setDropDownViewResource(int resource) {
        this.mDropDownResource = resource;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
    	
        return createViewFromResource(position, convertView, parent, mDropDownResource);
        
    }

    @SuppressWarnings("unchecked")
	private void bindView(int position, View view) {
		final Map dataSet = mData.get(position);
        if (dataSet == null) {
            return;
        }
        final ViewBinder binder = mViewBinder;
        final View[] holder = (View[]) view.getTag();
        final String[] from = mFrom;
        final int[] to = mTo;
        final int count = to.length;
        for (int i = 0; i < count; i++) {
        	
            final View v = holder[i];
            
            if (v != null) {
            	
                final Object data = dataSet.get(from[i]);
                
                String text = data == null ? "" : data.toString();
                 
                if (text == null) {
                	
                    text = "";
                }

                if("1".equals(text)&&i==3){
                	view.findViewById(R.id.ivischeck).setVisibility(View.VISIBLE);
                	((ImageView)view.findViewById(R.id.ivsoundlister)).setImageResource(R.drawable.sound_on);
                }else if("0".equals(text)&&i==3){
                	view.findViewById(R.id.ivischeck).setVisibility(View.INVISIBLE);
                	((ImageView)view.findViewById(R.id.ivsoundlister)).setImageResource(R.drawable.sound);
                }
                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, data, text);
                }

                if (!bound) {
                	
                    if (v instanceof Checkable) {
                    		if (data instanceof Boolean) {
                    			((Checkable) v).setChecked((Boolean) data);
                                
                                
                            } else {
                            	
                                throw new IllegalStateException(v.getClass().getName() +
                                        " should be bound to a Boolean, not a " + data.getClass());
                            }
                        
                    } else if (v instanceof TextView) {
                   
                        setViewText((TextView) v, text);
                        if("ÉèÎª·Ç¼±ÎðÈÅ×´Ì¬".equals(text)){
                            ((TextView) v).setTextColor(Color.parseColor("#e3bc2f"));
                        }else if("ÉèÎªÇëÎð´òÈÅ×´Ì¬".equals(text)){
                            ((TextView) v).setTextColor(Color.parseColor("#f0121d"));
                        }else if("ÉèÎª·ÀºôËÀÄã×´Ì¬".equals(text)){
                            ((TextView) v).setTextColor(Color.parseColor("#eda014"));
                        }else if("»Ö¸´ÎªÕý³£×´Ì¬".equals(text)){
                            ((TextView) v).setTextColor(Color.parseColor("#319A45"));
                        }

                        
                    } else if (v instanceof ImageView) {
                    	
                        if (data instanceof Integer) {
                        	 
                            setViewImage((ImageView) v, (Integer) data,null);
                            
                        } else {
                        	
                        	if (data instanceof Drawable) {
                        		
                        		setViewImage((ImageView) v, 0,(Drawable)data);
								
							}else {
								
								setViewImage((ImageView) v, text);
								
							}
                        	
                            
                        }
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleAdapter");
                    }
                }
            }
        }
    }

    /**
     * Returns the {@link ViewBinder} used to bind data to views.
     *
     * @return a ViewBinder or null if the binder does not exist
     *
     * @see #setViewBinder(android.widget.SimpleAdapter.ViewBinder)
     */
    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    /**
     * Sets the binder used to bind data to views.
     *
     * @param viewBinder the binder used to bind data to views, can be null to
     *        remove the existing binder
     *
     * @see #getViewBinder()
     */
    public void setViewBinder(ViewBinder viewBinder) {
    	
        mViewBinder = viewBinder;
    }

    /**
     * Called by bindView() to set the image for an ImageView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to an ImageView.
     *
     * This method is called instead of {@link #setViewImage(ImageView, String)}
     * if the supplied data is an int or Integer.
     *
     * @param v ImageView to receive an image
     * @param value the value retrieved from the data set
     *
     * @see #setViewImage(ImageView, String)
     */
    public void setViewImage(ImageView v,int value,Drawable argDrawable) {
    	
    	if (null!=argDrawable) {
    		
    		
    		v.setImageDrawable(argDrawable);
    		
		}else {
					
			v.setImageResource(value);
					
		}
        
    }

    /**
     * Called by bindView() to set the image for an ImageView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to an ImageView.
     *
     * By default, the value will be treated as an image resource. If the
     * value cannot be used as an image resource, the value is used as an
     * image Uri.
     *
     * This method is called instead of {@link #setViewImage(ImageView, int)}
     * if the supplied data is not an int or Integer.
     *
     * @param v ImageView to receive an image
     * @param value the value retrieved from the data set
     *
     * @see #setViewImage(ImageView, int) 
     */
    public void setViewImage(ImageView v, String value) {
    		
    	     try {
    	            v.setImageResource(Integer.parseInt(value));
    	            
    	        } catch (NumberFormatException nfe) {
    	        	
    	            v.setImageURI(Uri.parse(value));
    	        }
		}

    	
   
  

    /**
     * Called by bindView() to set the text for a TextView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to an TextView.
     *
     * @param v TextView to receive text
     * @param text the text to be set for the TextView
     */
    public void setViewText(TextView v, String text) {
    	
        v.setText(text);
    }

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SimpleFilter();
        }
        return mFilter;
    }

    /**
     * This class can be used by external clients of SimpleAdapter to bind
     * values to views.
     *
     * You should use this class to bind values to views that are not
     * directly supported by SimpleAdapter or to change the way binding
     * occurs for views supported by SimpleAdapter.
     *
     * @see SimpleAdapter#setViewImage(ImageView, int)
     * @see SimpleAdapter#setViewImage(ImageView, String)
     * @see SimpleAdapter#setViewText(TextView, String)
     */
    public static interface ViewBinder {
        /**
         * Binds the specified data to the specified view.
         *
         * When binding is handled by this ViewBinder, this method must return true.
         * If this method returns false, SimpleAdapter will attempts to handle
         * the binding on its own.
         *
         * @param view the view to bind the data to
         * @param data the data to bind to the view
         * @param textRepresentation a safe String representation of the supplied data:
         *        it is either the result of data.toString() or an empty String but it
         *        is never null
         *
         * @return true if the data was bound to the view, false otherwise
         */
        boolean setViewValue(View view, Object data, String textRepresentation);
    }

    /**
     * <p>An array filters constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class SimpleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<Map<String, Object>>(mData);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<Map<String, Object>> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<Map<String, Object>> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<Map<String, ?>> newValues = new ArrayList<Map<String, ?>>(count);

                for (int i = 0; i < count; i++) {
                    Map<String, ?> h = unfilteredValues.get(i);
                    if (h != null) {
                        
                        int len = mTo.length;

                        for (int j=0; j<len; j++) {
                            String str =  (String)h.get(mFrom[j]);
                            
                            String[] words = str.split(" ");
                            int wordCount = words.length;
                            
                            for (int k = 0; k < wordCount; k++) {
                                String word = words[k];
                                
                                if (word.toLowerCase().startsWith(prefixString)) {
                                    newValues.add(h);
                                    break;
                                }
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

		protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mData = (List<Map<String, Object>>) results.values;
            
            if (results.count > 0) {
            	
                notifyDataSetChanged();
                
            } else {
            	
                notifyDataSetInvalidated();
            }
        }
        
        
    
    }
}
